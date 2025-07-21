package com.github.akruk.antlrxquery.languagefeatures.evaluation.functions;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

public class Accessors extends EvaluationTestsBase {
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
        XQueryValue expected = valueFactory.string("x");
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
        XQueryValue expected = valueFactory.string("");
        assertDynamicGrammarQuery(grammarName, grammar, startRuleName, textualTree, xquery, expected);
    }

    @Test
    public void data() {
        assertResult("data(1)", List.of(valueFactory.number(1)));
        assertResult("data('a')", List.of(valueFactory.string("a")));
    }

    @Test
    public void string() {
        assertResult("string(1)", valueFactory.string("1"));
        assertResult("string('a')", valueFactory.string("a"));
    }


}
