package com.spring.lu.lu.context;

import com.spring.lu.lu.annototaion.Autowired;
import com.spring.lu.lu.annototaion.Component;
import com.spring.lu.lu.annototaion.ComponentScan;
import com.spring.lu.lu.annototaion.Scope;
import com.spring.lu.lu.bean.BeanDefinition;
import com.spring.lu.lu.util.EmptyCheckUtil;
import com.spring.lu.lu.util.StringHelp;


import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SpringApplicationContext {
    private Class clazz;

    // 存储类定义集合，用ConcurrentHashMap 是为了线程安全
    private ConcurrentHashMap<String, BeanDefinition> beanName2BeanDefinitionMap = new ConcurrentHashMap<>(32);


    /**
     * Cache of singleton objects: bean name to bean instance.
     */
    // 一级缓存
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
    // 二级缓存： 为了将 成熟Bean和纯净Bean分离，避免读取到不完整得Bean
    public static Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>();
    // 三级缓存
    public static Map<String, ObjectFactory> singletonFactories = new ConcurrentHashMap<>();
    // 循环依赖标识
    public static Set<String> singletonsCurrennlyInCreation = new HashSet<>();


    public SpringApplicationContext(Class clazz) {
        this.clazz = clazz;

        // 扫描
        scan(clazz);

        // 初始化bean
        initBean();
    }

    private void initBean() {
        for (Map.Entry<String, BeanDefinition> entry : beanName2BeanDefinitionMap.entrySet()) {
            if ("singleton".equals(entry.getValue().getScope())) {
                Object o = getSingleton(entry.getValue());
            }
        }
    }

    private Object getSingleton(BeanDefinition beanDefinition) {
        String beanName = beanDefinition.getBeanName();
        Object bean = singletonObjects.get(beanName);
        if (null == bean && singletonsCurrennlyInCreation.contains(beanName)) {
            bean = earlySingletonObjects.get(beanName);
            if (null == bean) {
                ObjectFactory factory = singletonFactories.get(beanName);
                if (null != factory) {
                    bean = factory.getObject();
                    earlySingletonObjects.put(beanName, bean);
                }
            }
        }
        return bean;
    }

    private void scan(Class clazz) {
        if (clazz.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan ComponentScan = (ComponentScan) clazz.getDeclaredAnnotation(ComponentScan.class);
            // 扫描跟目录
            String[] pathArr = ComponentScan.value();

            if (EmptyCheckUtil.isEmpty(pathArr)) {
                throw new RuntimeException("ComponentScan path is null");
            }

            for (String path : pathArr) {
                path = path.replace(".", "/");
                ClassLoader classLoader = this.getClass().getClassLoader();
                URL resource = classLoader.getResource(path);
                File file = new File(resource.getFile());

                doScan(file, path, classLoader);

            }
        } else {
            throw new RuntimeException("not found ComponentScan Annotation at  " + clazz);
        }
    }

    private void doScan(File file, String path, ClassLoader classLoader) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File tempFile : files) {
                if (tempFile.isDirectory()) {
                    doScan(tempFile, path, classLoader);
                } else {
                    String absolutePath = tempFile.getAbsolutePath();
                    if (absolutePath.endsWith(".class")) {
                        String classPath = absolutePath.substring(absolutePath.indexOf(path.replace("/", "\\")), absolutePath.indexOf(".class"));
                        System.out.println(classPath);
                        Class<?> clazz = null;
                        try {
                            clazz = classLoader.loadClass(classPath.replace("\\", "."));
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        if (clazz.isAnnotationPresent(Component.class)) {
                            Component component = clazz.getDeclaredAnnotation(Component.class);
                            String beanName = component.value();
                            beanName = EmptyCheckUtil.isEmpty(beanName) ? StringHelp.firstCharToLower(clazz.getSimpleName()) :
                                    beanName;

                            if (null != beanName2BeanDefinitionMap.get(beanName)) {
                                throw new RuntimeException(String.format("beanName %s is not pk ", beanName));
                            }
                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setBeanName(beanName);
                            beanDefinition.setClazz(clazz);

                            if (clazz.isAnnotationPresent(Scope.class)) {
                                Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                                beanDefinition.setScope(scopeAnnotation.value());
                            }

                            if (EmptyCheckUtil.isEmpty(beanDefinition.getScope())) {
                                beanDefinition.setScope("singleton");
                            }

                            beanName2BeanDefinitionMap.put(beanDefinition.getBeanName(), beanDefinition);
                        }
                    }
                }
            }
        }
    }


    public Object getBean(String name) {
        if (EmptyCheckUtil.isNotEmpty(beanName2BeanDefinitionMap.get(name))) {
            BeanDefinition beanDefinition = beanName2BeanDefinitionMap.get(name);
            if ("singleton".equals(beanDefinition.getScope())) {
                Object bean = getSingleton(beanDefinition);
                String beanName = beanDefinition.getBeanName();
                if (null != bean) {
                    return bean;
                }
                if (!singletonsCurrennlyInCreation.contains(beanDefinition.getBeanName())) {
                    singletonsCurrennlyInCreation.add(beanDefinition.getBeanName());
                }

                Class<?> beanClass = beanDefinition.getClazz();
                try {
                    Object instanceBean = beanClass.newInstance();
                    Object finalInstanceBean = instanceBean;
                    singletonFactories.put(beanDefinition.getBeanName(), () -> finalInstanceBean);

                    Field[] declaredFields = beanClass.getDeclaredFields();
                    for (Field declaredField : declaredFields) {
                        Autowired annotation = declaredField.getDeclaredAnnotation(Autowired.class);
                        // 说明属性上面有Autowired
                        if (annotation != null) {
                            declaredField.setAccessible(true);
                            // byname  bytype  byconstrator
                            // instanceB
                            String name2 = declaredField.getName();
                            // 递归创建依赖注入的对象
                            Object fileObject = getBean(name2);   //拿到B得Bean
                            declaredField.set(instanceBean, fileObject);
                        }
                    }
                    // 由于递归完后A 还是原实例，， 所以要从二级缓存中拿到proxy 。
                    if (earlySingletonObjects.containsKey(beanName)) {
                        instanceBean = earlySingletonObjects.get(beanName);
                    }
                    // 添加到一级缓存   A
                    singletonObjects.put(beanName, instanceBean);

                    //删除二三级缓存
                    earlySingletonObjects.remove(beanName);
                    singletonFactories.remove(beanName);

                    return instanceBean;

                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            } else {
                try {
                    return beanDefinition.getClazz().newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                return null;
            }

            return null;
        } else {
            throw new RuntimeException("can not found bean by name = " + name);
        }

    }
}
