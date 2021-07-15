package com.spring.test.config.service.impl;

import com.spring.lu.lu.annototaion.Autowired;
import com.spring.lu.lu.annototaion.Component;

@Component
public class AService {
    @Autowired
    BService bService;

    public void say() {
        System.out.println("a");
        bService.say();
    }


}
