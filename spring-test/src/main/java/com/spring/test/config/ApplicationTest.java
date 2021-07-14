package com.spring.test.config;

import com.spring.lu.lu.context.SpringApplicationContext;

public class ApplicationTest {

    public static void main(String[] args) {
        SpringApplicationContext context = new SpringApplicationContext(SpringConfig.class);

       System.out.println(context.getBean("userDao"));
        System.out.println(context.getBean("userDao"));
        System.out.println(context.getBean("userDao"));
    }


}
