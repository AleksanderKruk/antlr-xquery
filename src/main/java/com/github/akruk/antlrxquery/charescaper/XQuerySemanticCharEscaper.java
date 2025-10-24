package com.github.akruk.antlrxquery.charescaper;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import com.github.akruk.antlrxquery.semanticanalyzer.DiagnosticError;
import com.github.akruk.antlrxquery.semanticanalyzer.ErrorType;

public class XQuerySemanticCharEscaper {

    public record XQuerySemanticCharEscaperResult(String unescaped, List<DiagnosticError> errors) {}


    public XQuerySemanticCharEscaperResult escapeWithDiagnostics(ParserRuleContext ctx, final String str) {
        final StringBuilder result = new StringBuilder();
        final List<DiagnosticError> errors = new ArrayList<>();
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
                            errors.add(
                                DiagnosticError.of(
                                    ctx,
                                    ErrorType.CHAR__INVALID_UNICODE_POINT,
                                    List.of(charCode, i)
                                    )
                                );
                        }

                        result.append((char) charCode);
                        i = semi;
                        continue;
                    } catch (Exception e) {
                        errors.add(DiagnosticError.of(ctx, ErrorType.CHAR__INVALID_CHARACTER_REFERENCE, List.of(code, i)));
                        i = semi;
                        continue;
                    }
                } else {
                    errors.add(DiagnosticError.of(ctx, ErrorType.CHAR__UNTERMINATED_CHARACTER_REFERENCE, List.of(i)));
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
