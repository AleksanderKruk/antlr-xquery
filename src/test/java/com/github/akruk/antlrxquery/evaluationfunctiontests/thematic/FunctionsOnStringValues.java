package com.github.akruk.antlrxquery.evaluationfunctiontests.thematic;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.evaluationfunctiontests.FunctionsEvaluationTests;
import com.github.akruk.antlrxquery.values.*;

public class FunctionsOnStringValues extends FunctionsEvaluationTests {
    // valid Unicode codepoint
    @Test
    public void charFromInteger() {
        assertResult("fn:char(65)", new XQueryString("A", baseFactory));
        assertResult("fn:char(0x1F600)", new XQueryString("\uD83D\uDE00", baseFactory)); // grinning face
    }

    // invalid codepoint (above Unicode max)
    @Test
    public void charFromIntegerOutOfRange() {
        assertError("fn:char(0x110000)", XQueryError.UnrecognizedOrInvalidCharacterName);
    }

    // invalid codepoint (surrogate range)
    @Test
    public void charFromIntegerSurrogate() {
        assertError("fn:char(0xD800)", XQueryError.UnrecognizedOrInvalidCharacterName);
    }

    // HTML5 named references
    @Test
    public void charFromNamedEntity() {
        assertResult("fn:char('amp')", new XQueryString("&", baseFactory));
        assertResult("fn:char('quot')", new XQueryString("\"", baseFactory));
        assertResult("fn:char('NotEqualTilde')", new XQueryString("\u2242\u0338", baseFactory));
    }

    // unrecognized named reference
    @Test
    public void charFromUnknownEntity() {
        assertError("fn:char('unknown')", XQueryError.UnrecognizedOrInvalidCharacterName);
    }

    // escape sequences
    @Test
    public void charFromEscape() {
        assertResult("fn:char('\\n')", new XQueryString("\n", baseFactory));
        assertResult("fn:char('\\t')", new XQueryString("\t", baseFactory));
        assertResult("fn:char('\\r')", new XQueryString("\r", baseFactory));
    }

    // invalid escape
    @Test
    public void charFromInvalidEscape() {
        assertError("fn:char('\\x')", XQueryError.UnrecognizedOrInvalidCharacterName);
    }

    // wrong arity
    @Test
    public void charWrongArity() {
        assertError("fn:char()", XQueryError.WrongNumberOfArguments);
        assertError("fn:char(65, 'A')", XQueryError.WrongNumberOfArguments);
    }

    // invalid argument type
    @Test
    public void charInvalidType() {
        assertError("fn:char(true())", XQueryError.UnrecognizedOrInvalidCharacterName);
    }


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
