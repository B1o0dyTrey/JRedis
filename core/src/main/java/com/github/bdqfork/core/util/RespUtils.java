package com.github.bdqfork.core.util;

import java.util.*;

/**
 * @author bdq
 * @since 2020/09/25
 */
public class RespUtils {

    public static Map<String, Object> parserArray(String commands) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isEmpty(commands)) {
            map.put("res", Collections.emptyList());
            return map;
        }
        char[] chs = commands.toCharArray();
        if (chs[0] != '*') {
            throw new IllegalStateException("illegal command array with first character " + chs[0] + " !");
        }
        int size = chs[1] - '0';
        List<Object> results = new ArrayList<>(size);
        int i = 2;
        for (int flag = size; flag > 0; i++) {
            char c = chs[i];
            if (c == '\r' || c == '\n') {
                continue;
            }
            if (c == '+') {
                flag--;
                i++;
                StringBuilder builder = new StringBuilder();
                while (chs[i] != '\r') {
                    builder.append(chs[i]);
                    i++;
                }
                results.add(builder.toString());
                continue;
            }
            if (c == '$') {
                flag--;
                while (chs[i] != '\r') {
                    i++;
                }
                i += 2;
                StringBuilder builder = new StringBuilder();
                while (chs[i] != '\r') {
                    builder.append(chs[i]);
                    i++;
                }
                results.add(builder.toString());
                continue;
            }
            if (c == '-') {
                flag--;
                i++;
                StringBuilder builder = new StringBuilder();
                while (chs[i] != '\r') {
                    builder.append(chs[i]);
                    i++;
                }
                results.add(builder.toString());
                continue;
            }
            if (c == ':') {
                flag--;
                i++;
                StringBuilder builder = new StringBuilder();
                while (chs[i] != '\r') {
                    builder.append(chs[i]);
                    i++;
                }
                results.add(Long.parseLong(builder.toString()));
                continue;
            }
            if (c == '*') {
                flag--;
                StringBuilder builder = new StringBuilder().append("*");
                i++;
                int a = 0;
                while (i < chs.length && chs[i] != '*') {
                    builder.append(chs[i]);
                    i++;
                    a++;
                }
                Map<String, Object> res = parserArray(builder.toString());
                results.add(res.get("res"));

                i = i - a + (int) res.get("size") - 2;
            }
        }
        map.put("size", i);
        map.put("res", results);
        return map;
    }
}
