package com.github.akruk.antlrxquery.languagefeatures.evaluation.functions;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

public class FunctionsBasedOnSubstringMatching extends EvaluationTestsBase {

    @Test
    public void contains() {
        assertResult("contains('abc', 'bc')", valueFactory.bool(true));
        assertResult("contains('', 'bc')", valueFactory.bool(false));
        assertResult("contains('abc', '')", valueFactory.bool(true));
        assertResult("contains('', ())", valueFactory.bool(true));
    }

    @Test
    public void startsWith() {
        assertResult("starts-with('tattoo', 'tat')", valueFactory.bool(true));
        assertResult("starts-with('tattoo', 'att')", valueFactory.bool(false));
    }

    @Test
    public void endsWith() {
        assertResult("ends-with('tattoo', 'oo')", valueFactory.bool(true));
        assertResult("ends-with('tattoo', 'tatt')", valueFactory.bool(false));
    }


    @Test
    public void substringBefore() {
        assertResult("substring-before('tattoo', 'attoo')", valueFactory.string("t"));
        assertResult("substring-before('tattoo', 'tatto')", valueFactory.string(""));
        assertResult("substring-before('abcde', 'f')", valueFactory.string(""));
    }

    @Test
    public void substringAfter() {
        assertResult("substring-after('tattoo', 'tat')", valueFactory.string("too"));
        assertResult("substring-after('tattoo', 'tattoo')", valueFactory.string(""));
        assertResult("substring-after('abcde', 'f')", valueFactory.string(""));
    }



}
