package com.github.akruk.antlrxquery.charescaper;

import java.util.ArrayList;
import java.util.List;

public class XQuerySemanticCharEscaper {

    public record XQuerySemanticCharEscaperResult(String unescaped, List<Error> errors) {}

    public record Error(String message, int position) {
        @Override
        public String toString() {
            return "Error at " + position + ": " + message;
        }
    }


    public XQuerySemanticCharEscaperResult escapeWithDiagnostics(final String str) {
        final StringBuilder result = new StringBuilder();
        final List<Error> errors = new ArrayList<>();
        final int length = str.length();

        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if (c == '&' && i + 1 < length && str.charAt(i + 1) == '#') {
                int semi = str.indexOf(';', i);
                if (semi > i) {
                    String code = str.substring(i + 2, semi);
                    try {
                        int charCode = code.startsWith("x") || code.startsWith("X")
                                ? Integer.parseInt(code.substring(1), 16)
                                : Integer.parseInt(code, 10);

                        if (!Character.isValidCodePoint(charCode)) {
                            errors.add(new Error("Invalid Unicode code point: " + charCode, i));
                        }

                        result.append((char) charCode);
                        i = semi;
                        continue;
                    } catch (Exception e) {
                        errors.add(new Error("Invalid character reference: " + code, i));
                        i = semi;
                        continue;
                    }
                } else {
                    errors.add(new Error("Unterminated character reference", i));
                }
            }

            result.append(c);
        }

        String finalResult = result.toString()
            .replace("\"\"", "\"")
            .replace("''", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&apos;", "'")
            .replace("&quot;", "\"");

        return new XQuerySemanticCharEscaperResult(finalResult, errors);
    }

}
