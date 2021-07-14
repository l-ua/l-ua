package com.spring.lu.lu.util;

public class StringHelp {

    public static final String firstCharToLower(String str) {
        if (EmptyCheckUtil.isEmpty(str)) {
            return null;
        }

        String part1 = str.substring(0, 1);
        String part2 = str.substring(1);

        return part1.toLowerCase() + part2;

    }
}
