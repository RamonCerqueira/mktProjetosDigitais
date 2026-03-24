package com.mktplace.validation;

public final class DocumentValidator {
    private DocumentValidator() {}

    public static boolean isValidCpf(String cpf) {
        String value = digits(cpf);
        if (value.length() != 11 || value.chars().distinct().count() == 1) return false;
        return digit(value, 9) == Character.getNumericValue(value.charAt(9)) && digit(value, 10) == Character.getNumericValue(value.charAt(10));
    }

    public static boolean isValidCnpj(String cnpj) {
        String value = digits(cnpj);
        if (value.length() != 14 || value.chars().distinct().count() == 1) return false;
        return cnpjDigit(value, 12) == Character.getNumericValue(value.charAt(12)) && cnpjDigit(value, 13) == Character.getNumericValue(value.charAt(13));
    }

    private static int digit(String value, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) sum += Character.getNumericValue(value.charAt(i)) * ((length + 1) - i);
        int result = 11 - (sum % 11);
        return result > 9 ? 0 : result;
    }

    private static int cnpjDigit(String value, int length) {
        int[] weights = length == 12 ? new int[]{5,4,3,2,9,8,7,6,5,4,3,2} : new int[]{6,5,4,3,2,9,8,7,6,5,4,3,2};
        int sum = 0;
        for (int i = 0; i < length; i++) sum += Character.getNumericValue(value.charAt(i)) * weights[i];
        int result = sum % 11;
        return result < 2 ? 0 : 11 - result;
    }

    public static String digits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
