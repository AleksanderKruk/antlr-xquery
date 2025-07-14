package com.github.akruk.languagefeatures.evaluation.evaluationfunctiontests.thematic;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.languagefeatures.evaluation.evaluationfunctiontests.FunctionsEvaluationTests;

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

    @Test
    public void nodeName_emptySequence() throws Exception {
        String grammarName = "Grammar";
        String grammar = """
            grammar Grammar;
            x: 'x';

                """;
        String startRuleName = "x";
        String textualTree = "x";
        String xquery = "fn:node-name(())";
        XQueryValue expected = baseFactory.string("");
        assertDynamicGrammarQuery(grammarName, grammar, startRuleName, textualTree, xquery, expected);
    }

    @Test
    public void data() {
        assertResult("data(1)", List.of(baseFactory.number(1)));
        assertResult("data('a')", List.of(baseFactory.string("a")));
    }

    @Test
    public void string() {
        assertResult("string(1)", baseFactory.string("1"));
        assertResult("string('a')", baseFactory.string("a"));
    }


}
