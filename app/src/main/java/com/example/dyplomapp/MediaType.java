package com.example.dyplomapp;

import java.util.HashMap;
import java.util.Map;

public enum MediaType {
    PDF(1), AUDIO(2), VIDEO(3), TXT(4), YT(5);

    private int value;
    private static Map map = new HashMap<>();

    private MediaType(int value) {
        this.value = value;
    }

    static {
        for (MediaType q : MediaType.values()) {
            map.put(q.value, q);
        }
    }

    public static MediaType valueOf(int q) {
        return (MediaType) map.get(q);
    }

    public int getValue() {
        return value;
    }
}
