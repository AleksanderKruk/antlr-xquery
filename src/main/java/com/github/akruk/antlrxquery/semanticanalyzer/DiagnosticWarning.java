package com.github.akruk.antlrxquery.semanticanalyzer;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public record DiagnosticWarning(String message,
                            int startLine,
                            int charPositionInLine,
                            int endLine,
                            int endCharPositionInLine)
{

    public static DiagnosticWarning of(ParserRuleContext where, String message) {
        final Token start = where.getStart();
        final Token stop = where.getStop();
        final int line = start.getLine();
        final int charPositionInLine = start.getCharPositionInLine();
        final LineEndCharPosEnd lineEndCharPosEnd = getLineEndCharPosEnd(stop);

        return new DiagnosticWarning(
            message,
            line,
            charPositionInLine,
            lineEndCharPosEnd.lineEnd,
            lineEndCharPosEnd.charPosEnd
        );
    }

    public static DiagnosticWarning of(Token start, Token stop, String message) {
        return new DiagnosticWarning(
            message,
            start.getLine(),
            start.getCharPositionInLine(),
            stop.getLine(),
            stop.getCharPositionInLine() + stop.getText().length());
    }


    record LineEndCharPosEnd(int lineEnd, int charPosEnd) {
    }

    private static LineEndCharPosEnd getLineEndCharPosEnd(final Token end)
    {
        final var string = end.getText();
        final int length = string.length();

        int newlineCount = 0;
        int lastNewlineIndex = 0;
        for (int i = 0; i < length; i++) {
            if (string.codePointAt(i) == '\n') {
                newlineCount++;
                lastNewlineIndex = i;
            }
        }

        final int lineEnd = end.getLine() + newlineCount;
        final int charPositionInLineEnd = newlineCount == 0 ? end.getCharPositionInLine() + length
            : length - lastNewlineIndex;
        return new LineEndCharPosEnd(lineEnd, charPositionInLineEnd);
    }


};
