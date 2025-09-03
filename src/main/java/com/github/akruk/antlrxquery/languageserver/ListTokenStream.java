package com.github.akruk.antlrxquery.languageserver;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;

import java.util.ArrayList;
import java.util.List;

public final class ListTokenStream extends CommonTokenStream {
    public final List<Token> tokens = new ArrayList<>();

    public ListTokenStream(Lexer lexer) {
        super(lexer);
    }

    @Override
    protected int fetch(int n) {
        int fetched = super.fetch(n);
        for (int i = tokens.size(); i < tokens.size() + fetched; i++) {
            tokens.add(get(i));
        }
        return fetched;
    }

    @Override
    protected int nextTokenOnChannel(int i, int channel) {
        var tokenIndex = super.nextTokenOnChannel(i, channel);
        var token = get(tokenIndex);
        tokens.add(token);
        return tokenIndex;
    }
}
