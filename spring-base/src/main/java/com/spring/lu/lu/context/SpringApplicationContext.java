package com.spring.lu.lu.context;

import com.spring.lu.lu.annototaion.Component;
import com.spring.lu.lu.annototaion.ComponentScan;
import com.spring.lu.lu.annototaion.Scope;
import com.spring.lu.lu.bean.BeanDefinition;
import com.spring.lu.lu.util.EmptyCheckUtil;
import com.spring.lu.lu.util.StringHelp;
import com.sun.istack.internal.NotNull;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpringApplicationContext {
    private Class clazz;

    // 存储类定义集合，用ConcurrentHashMap 是为了线程安全
    private ConcurrentHashMap<String, BeanDefinition> beanName2BeanDefinitionMap = new ConcurrentHashMap<>(32);

    // 对象线程池
    private ConcurrentHashMap<String, Object> beanName2BeanMap = new ConcurrentHashMap<>(32);

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
                try {
                    Object o = entry.getValue().getClazz().newInstance();
                    beanName2BeanMap.put(entry.getKey(), o);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }
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
                return beanName2BeanMap.get(name);
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
        } else {
            throw new RuntimeException("can not found bean by name = " + name);
        }

    }
}
