package com.github.bdqfork.core.util;

/**
 * @author bdq
 * @since 2020/09/25
 */
public class StringUtils {
    public static boolean isEmpty(String s) {
        return s == null || s.equals("");
    }

    public static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}
