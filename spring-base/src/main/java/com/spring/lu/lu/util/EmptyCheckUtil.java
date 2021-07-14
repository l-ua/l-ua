package com.spring.lu.lu.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

public class EmptyCheckUtil {
    public static final Predicate<String> isEmptyByString() {
        return (s) -> null == s || 0 == (s).trim().length();
    }

    public static final Predicate<Collection> isEmptyByCollection() {
        return (coll) -> null == coll || 0 == coll.size();
    }

    public static final Predicate<Map> isEmptyByMap() {
        return (map) -> null == map || 0 == map.size();
    }

    public static final Predicate isEmptyByArray() {
        return (arr) -> null == arr || 0 == Array.getLength(arr);
    }

    public static final boolean isEmpty(Object obj) {
        if (null == obj) {
            return true;
        }
        if (obj.getClass().isArray()) {
            return isEmptyByArray().test(obj);
        } else if (obj instanceof Map) {
            return isEmptyByMap().test((Map) obj);
        } else if (obj instanceof Collection) {
            return isEmptyByCollection().test((Collection) obj);
        } else if (obj instanceof String) {
            return isEmptyByString().test((String) obj);
        } else if (obj instanceof CharSequence) {
            return 0 == ((CharSequence) obj).toString().trim().length();
        }

        return false;

    }

    public static final boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }
}
