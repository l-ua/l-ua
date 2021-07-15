package com.spring.test.config.service.impl;

import com.spring.lu.lu.annototaion.Component;
import com.spring.lu.lu.interfaces.BeanNameAware;
import com.spring.lu.lu.interfaces.BeanPostProcessor;
import com.spring.lu.lu.interfaces.InitializingBean;
import com.spring.test.config.service.UserService;

@Component
public class UserServiceImpl implements UserService, BeanNameAware, InitializingBean {
    @Override
    public void setBeanName(String name) {
        System.out.println("BeanNameAware setBeanName " + name);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println(" InitializingBean afterPropertiesSet ");
    }

    @Override
    public void sayHi() {
        System.out.println("say hello world");
    }
}
