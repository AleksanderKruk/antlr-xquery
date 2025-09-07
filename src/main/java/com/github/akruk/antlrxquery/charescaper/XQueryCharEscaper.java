package com.github.akruk.antlrxquery.charescaper;

public class XQueryCharEscaper {

    private static final int[] hexToDecimal = new int['f' - '0' + 1];
    {
        final int offset = '0';
        hexToDecimal['0' - offset] = 0;
        hexToDecimal['1' - offset] = 1;
        hexToDecimal['2' - offset] = 2;
        hexToDecimal['3' - offset] = 3;
        hexToDecimal['4' - offset] = 4;
        hexToDecimal['5' - offset] = 5;
        hexToDecimal['6' - offset] = 6;
        hexToDecimal['7' - offset] = 7;
        hexToDecimal['8' - offset] = 8;
        hexToDecimal['9' - offset] = 9;
        hexToDecimal['a' - offset] = 10;
        hexToDecimal['b' - offset] = 11;
        hexToDecimal['c' - offset] = 12;
        hexToDecimal['d' - offset] = 13;
        hexToDecimal['e' - offset] = 14;
        hexToDecimal['f' - offset] = 15;
        hexToDecimal['A' - offset] = 10;
        hexToDecimal['B' - offset] = 11;
        hexToDecimal['C' - offset] = 12;
        hexToDecimal['D' - offset] = 13;
        hexToDecimal['E' - offset] = 14;
        hexToDecimal['F' - offset] = 15;
    }

    public String escapeChars(final String str) {
        final StringBuilder result = new StringBuilder();
        final int length = str.length();

        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if (c == '&' && i + 1 < length && str.charAt(i + 1) == '#') {
                int semi = str.indexOf(';', i);
                if (semi > i) {
                    String code = str.substring(i + 2, semi);
                    int charCode;
                    try {
                        if (code.startsWith("x") || code.startsWith("X")) {
                            charCode = Integer.parseInt(code.substring(1), 16);
                        } else {
                            charCode = Integer.parseInt(code, 10);
                        }
                        result.append((char) charCode);
                        i = semi; // skip past the semicolon
                        continue;
                    } catch (NumberFormatException ignored) {
                        // error(XQueryError.UnrecognizedOrInvalidCharacterName, "Invalid character code: '" + code + "'",
                        //     null);
                        // TODO: ERROR
                        // fallback: treat as literal
                    }
                }
            }

            result.append(c);
        }

        // Entity replacements
        return result.toString()
            .replace("\"\"", "\"")
            .replace("''", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&apos;", "'")
            .replace("&quot;", "\"");
    }

}
