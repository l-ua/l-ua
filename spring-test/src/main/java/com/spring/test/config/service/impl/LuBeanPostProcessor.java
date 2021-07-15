package com.spring.test.config.service.impl;

import com.spring.lu.lu.annototaion.Component;
import com.spring.lu.lu.interfaces.BeanPostProcessor;

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
        return bean;
    }
}
