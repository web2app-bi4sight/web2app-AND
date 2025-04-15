package com.huntmobi.web2app;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Matcher;

public class HM_W2ADataValidator {
    private static final Pattern W2A_DATA_PATTERN;

    static {
        Pattern pattern;
        try {
            pattern = Pattern.compile("w2a_data(:|%3[aA])[a-zA-Z0-9_/]*?_bi");
        } catch (PatternSyntaxException e) {
            pattern = null;
        }
        W2A_DATA_PATTERN = pattern;
    }

    public static boolean isW2ADataString(String inputString) {
        if (inputString == null || inputString.isEmpty() || W2A_DATA_PATTERN == null) {
            return false;
        }

        final Matcher matcher = W2A_DATA_PATTERN.matcher(inputString);
        return matcher.find();
    }
}