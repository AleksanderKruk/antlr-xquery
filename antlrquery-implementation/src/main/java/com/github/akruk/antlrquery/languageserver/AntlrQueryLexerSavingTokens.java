
package com.github.akruk.antlrquery.languageserver;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;

import com.github.akruk.antlrquery.AntlrQueryLexer;

public class AntlrQueryLexerSavingTokens extends AntlrQueryLexer {
    public final List<Token> tokens = new ArrayList<>();
    public AntlrQueryLexerSavingTokens(CharStream input) {
        super(input);
    }

    @Override
    public Token getToken()
    {
        final Token token = super.getToken();
        if (token != null)
            tokens.add(token);
        return token;
    }


}