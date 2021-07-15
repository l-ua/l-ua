package com.spring.test.config.service.impl;

import com.spring.lu.lu.annototaion.Component;
import com.spring.lu.lu.interfaces.BeanPostProcessor;
import com.spring.test.config.service.UserService;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class LuBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {
        System.out.println("LuBeanPostProcessor postProcessBeforeInitialization " + beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {
        System.out.println("LuBeanPostProcessor postProcessAfterInitialization " + beanName);

        if ("userServiceImpl".equals(beanName)) {
            Object proxyInstance = Proxy.newProxyInstance(LuBeanPostProcessor.class.getClassLoader(), UserService.class.getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("动态代理");
                    return method.invoke(bean, args);
                }
            });
            return proxyInstance;
        }

        return bean;
    }
}
