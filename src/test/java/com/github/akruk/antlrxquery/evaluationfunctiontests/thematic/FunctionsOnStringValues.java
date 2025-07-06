package com.github.akruk.antlrxquery.evaluationfunctiontests.thematic;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.evaluationfunctiontests.FunctionsEvaluationTests;
import com.github.akruk.antlrxquery.values.*;

public class FunctionsOnStringValues extends FunctionsEvaluationTests {
    @Test
    public void concat() {
        assertResult("concat(('a', 'b', 'c'))", new XQueryString("abc", baseFactory));
    }

    @Test
    public void stringJoin() {
        assertResult("string-join(('a', 'b', 'c'))", new XQueryString("abc", baseFactory));
        assertResult("string-join(('a', 'b', 'c'), '-')", new XQueryString("a-b-c", baseFactory));
    }

    @Test
    public void substring() {
        assertResult("substring('abcde', 4)", new XQueryString("de", baseFactory));
        assertResult("substring('abcde', 3, 2)", new XQueryString("cd", baseFactory));
    }

    @Test
    public void stringLength() {
        assertResult("string-length('abcde')", new XQueryNumber(5, baseFactory));
        assertResult("string-length('')", new XQueryNumber(0, baseFactory));
    }

    @Test
    public void normalization() {
        assertResult("normalize-space(' \t\n\r a    b \t \t c   \t')", new XQueryString("a b c", baseFactory));
    }

    @Test
    public void lowercase() {
        assertResult("lower-case('AbCdE')", new XQueryString("abcde", baseFactory));
    }

    @Test
    public void uppercase() {
        assertResult("upper-case('AbCdE')", new XQueryString("ABCDE", baseFactory));
    }


}
