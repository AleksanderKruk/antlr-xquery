package com.github.akruk.antlrxquery.evaluationfunctiontests.thematic;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.evaluationfunctiontests.FunctionsEvaluationTests;
import com.github.akruk.antlrxquery.values.*;

public class FunctionsOnStringValues extends FunctionsEvaluationTests {
    @Test
    public void charFromInteger() {
        assertResult("fn:char(65)", new XQueryString("A", valueFactory));
        assertResult("fn:char(0x1F600)", new XQueryString("\uD83D\uDE00", valueFactory)); // grinning face
    }

    @Test
    public void charFromIntegerOutOfRange() {
        assertError("fn:char(0x110000)", XQueryError.UnrecognizedOrInvalidCharacterName);
    }

    @Test
    public void charFromIntegerSurrogate() {
        assertError("fn:char(0xD800)", XQueryError.UnrecognizedOrInvalidCharacterName);
    }

    @Test
    public void charFromNamedEntity() {
        assertResult("fn:char('amp')", new XQueryString("&", valueFactory));
        assertResult("fn:char('quot')", new XQueryString("\"", valueFactory));
        assertResult("fn:char('NotEqualTilde')", new XQueryString("\u2242\u0338", valueFactory));
    }

    @Test
    public void charFromUnknownEntity() {
        assertError("fn:char('unknown')", XQueryError.UnrecognizedOrInvalidCharacterName);
    }

    @Test
    public void charFromEscape() {
        assertResult("fn:char('\\n')", new XQueryString("\n", valueFactory));
        assertResult("fn:char('\\t')", new XQueryString("\t", valueFactory));
        assertResult("fn:char('\\r')", new XQueryString("\r", valueFactory));
    }

    @Test
    public void charFromInvalidEscape() {
        assertError("fn:char('\\x')", XQueryError.UnrecognizedOrInvalidCharacterName);
    }

    @Test
    public void charWrongArity() {
        assertError("fn:char()", XQueryError.WrongNumberOfArguments);
        assertError("fn:char(65, 'A')", XQueryError.WrongNumberOfArguments);
    }

    @Test
    public void charInvalidType() {
        assertError("fn:char(true())", XQueryError.UnrecognizedOrInvalidCharacterName);
    }

    @Test
    public void charactersFromString() {
        List<XQueryValue> expected = List.of(
            valueFactory.string("T"),
            valueFactory.string("h"),
            valueFactory.string("é"),
            valueFactory.string("r"),
            valueFactory.string("è"),
            valueFactory.string("s"),
            valueFactory.string("e")
        );
        assertResult("characters('Thérèse')",
            expected
        );
    }

    @Test
    public void charactersEmptyString() {
        assertResult("characters('')", List.of());
    }

    @Test
    public void charactersEmptySequence() {
        assertResult("characters(())", List.of());
    }


    @Test
    public void charactersWithContextItem() {
        List<XQueryValue> expected = List.of(
            valueFactory.string("d"),
            valueFactory.string("e"),
            valueFactory.string("f")
        );
        assertResult("('abc', 'def')[2] => characters()", expected);
    }

    @Test
    public void charactersSurrogatePair() {
        // "A𝄞B" where 𝄞 is U+1D11E
        List<XQueryValue> expected = List.of(
            valueFactory.string("A"),
            valueFactory.string("\uD834\uDD1E"),
            valueFactory.string("B")
        );
        assertResult("characters('A\uD834\uDD1E' || 'B')", expected
        );
    }

    @Test
    public void charactersAutoConversion() {
        assertResult("characters(123)", List.of(valueFactory.string("1"), valueFactory.string("2"), valueFactory.string("3")));
    }

    @Test
    public void charactersWrongArity() {
        assertError("characters('a', 'b')", XQueryError.WrongNumberOfArguments);
    }

    @Test
    public void concat() {
        assertResult("concat(('a', 'b', 'c'))", new XQueryString("abc", valueFactory));
    }

    @Test
    public void stringJoin() {
        assertResult("string-join(('a', 'b', 'c'))", new XQueryString("abc", valueFactory));
        assertResult("string-join(('a', 'b', 'c'), '-')", new XQueryString("a-b-c", valueFactory));
    }

    @Test
    public void substring() {
        assertResult("substring('abcde', 4)", new XQueryString("de", valueFactory));
        assertResult("substring('abcde', 3, 2)", new XQueryString("cd", valueFactory));
    }

    @Test
    public void stringLength() {
        assertResult("string-length('abcde')", new XQueryNumber(5, valueFactory));
        assertResult("string-length('')", new XQueryNumber(0, valueFactory));
    }

    @Test
    public void normalization() {
        assertResult("normalize-space(' \t\n\r a    b \t \t c   \t')", new XQueryString("a b c", valueFactory));
    }

    @Test
    public void lowercase() {
        assertResult("lower-case('AbCdE')", new XQueryString("abcde", valueFactory));
    }

    @Test
    public void uppercase() {
        assertResult("upper-case('AbCdE')", new XQueryString("ABCDE", valueFactory));
    }


}
