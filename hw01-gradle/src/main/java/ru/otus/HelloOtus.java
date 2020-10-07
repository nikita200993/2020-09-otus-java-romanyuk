package ru.otus;

import com.google.common.collect.HashBiMap;

import java.util.Map;

public class HelloOtus {

    public static void main(String[] args) {
        final Map<String, String> senselessMap = HashBiMap.create();
        senselessMap.put("message", "Hello Otus");
        System.out.println(senselessMap.get("message"));
    }
}
