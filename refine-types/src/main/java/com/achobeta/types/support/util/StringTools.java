package com.achobeta.types.support.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.DigestUtils;

public class StringTools {

    public static boolean isEmpty(String str) {
        if (null == str || "".equals(str) || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else if ("".equals(str.trim())) {
            return true;
        }
        return false;
    }

    public static final String encodeMD5(String originString) {
        return StringTools.isEmpty(originString) ? null : DigestUtils.md5DigestAsHex(originString.getBytes());
    }

    public static String getRandomString(Integer count) {
        return RandomStringUtils.random(count, true, true);
    }

    public static boolean isEmail(String userEmail) {
        return userEmail.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
    }

    public static boolean isPhone(String phone) {
        return phone.matches("^1[3-9]\\d{8}$");
    }
}
