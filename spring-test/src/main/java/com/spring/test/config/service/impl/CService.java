package com.spring.test.config.service.impl;

import com.spring.lu.lu.annototaion.Autowired;
import com.spring.lu.lu.annototaion.Component;

@Component
public class CService {
    @Autowired
    AService aService;

}
