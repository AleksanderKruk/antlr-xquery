
package com.github.akruk.antlrxquery.languageserver;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;

import com.github.akruk.antlrxquery.AntlrXqueryLexer;

public class AntlrXqueryLexerSavingTokens extends AntlrXqueryLexer {
    public final List<Token> tokens = new ArrayList<>();
    public AntlrXqueryLexerSavingTokens(CharStream input) {
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