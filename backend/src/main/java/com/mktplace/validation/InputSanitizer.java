package com.mktplace.validation;

public final class InputSanitizer {
    private InputSanitizer() {}

    public static String clean(String value) {
        if (value == null) return null;
        return value.replace("<", "&lt;").replace(">", "&gt;").trim();
    }

    public static String search(String value) {
        if (value == null) return null;
        return clean(value).replaceAll("[^\\p{L}\\p{N}\\s\\-_.]", "");
    }
}
