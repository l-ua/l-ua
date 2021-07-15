package com.spring.test.config.service.impl;

import com.spring.lu.lu.annototaion.Autowired;
import com.spring.lu.lu.annototaion.Component;

@Component
public class BService {
    @Autowired
    CService cService;

    public void say() {
        System.out.println("b");

    }
}
