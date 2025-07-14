package com.github.akruk.antlrxquery.languagefeatures.evaluation.evaluationfunctiontests.thematic;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

class FunctionsBasedOnSubstringMatching extends EvaluationTestsBase {

    @Test
    public void contains() {
        assertResult("contains('abc', 'bc')", baseFactory.bool(true));
        assertResult("contains('', 'bc')", baseFactory.bool(false));
        assertResult("contains('abc', '')", baseFactory.bool(true));
        assertResult("contains('', ())", baseFactory.bool(true));
    }

    @Test
    public void startsWith() {
        assertResult("starts-with('tattoo', 'tat')", baseFactory.bool(true));
        assertResult("starts-with('tattoo', 'att')", baseFactory.bool(false));
    }

    @Test
    public void endsWith() {
        assertResult("ends-with('tattoo', 'oo')", baseFactory.bool(true));
        assertResult("ends-with('tattoo', 'tatt')", baseFactory.bool(false));
    }


    @Test
    public void substringBefore() {
        assertResult("substring-before('tattoo', 'attoo')", baseFactory.string("t"));
        assertResult("substring-before('tattoo', 'tatto')", baseFactory.string(""));
        assertResult("substring-before('abcde', 'f')", baseFactory.string(""));
    }

    @Test
    public void substringAfter() {
        assertResult("substring-after('tattoo', 'tat')", baseFactory.string("too"));
        assertResult("substring-after('tattoo', 'tattoo')", baseFactory.string(""));
        assertResult("substring-after('abcde', 'f')", baseFactory.string(""));
    }



}
