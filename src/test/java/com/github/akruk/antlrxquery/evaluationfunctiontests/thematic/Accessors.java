package com.github.akruk.antlrxquery.evaluationfunctiontests.thematic;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.evaluationfunctiontests.FunctionsEvaluationTests;
import com.github.akruk.antlrxquery.values.XQueryValue;

public class Accessors extends FunctionsEvaluationTests {
    @Test
    public void nodeName() throws Exception {
        String grammarName = "Grammar";
        String grammar = """
            grammar Grammar;
            x: 'x';

                """;
        String startRuleName = "x";
        String textualTree = "x";
        String xquery = "fn:node-name(/x)";
        XQueryValue expected = baseFactory.string("x");
        assertDynamicGrammarQuery(grammarName, grammar, startRuleName, textualTree, xquery, expected);
    }


}
