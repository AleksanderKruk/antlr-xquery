package com.github.akruk.antlrxquery.languagefeatures.evaluation.functions;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;
import com.github.akruk.antlrxquery.values.*;

public class FunctionsOnStringValues extends EvaluationTestsBase {
    @Test
    public void charFromInteger() {
        assertResult("fn:char(65)", new XQueryString("A", baseFactory));
        assertResult("fn:char(0x1F600)", new XQueryString("\uD83D\uDE00", baseFactory)); // grinning face
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
        assertResult("fn:char('amp')", new XQueryString("&", baseFactory));
        assertResult("fn:char('quot')", new XQueryString("\"", baseFactory));
        assertResult("fn:char('NotEqualTilde')", new XQueryString("\u2242\u0338", baseFactory));
    }

    @Test
    public void charFromUnknownEntity() {
        assertError("fn:char('unknown')", XQueryError.UnrecognizedOrInvalidCharacterName);
    }

    @Test
    public void charFromEscape() {
        assertResult("fn:char('\\n')", new XQueryString("\n", baseFactory));
        assertResult("fn:char('\\t')", new XQueryString("\t", baseFactory));
        assertResult("fn:char('\\r')", new XQueryString("\r", baseFactory));
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
            baseFactory.string("T"),
            baseFactory.string("h"),
            baseFactory.string("é"),
            baseFactory.string("r"),
            baseFactory.string("è"),
            baseFactory.string("s"),
            baseFactory.string("e")
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
            baseFactory.string("d"),
            baseFactory.string("e"),
            baseFactory.string("f")
        );
        assertResult("('abc', 'def')[2] => characters()", expected);
    }

    @Test
    public void charactersSurrogatePair() {
        // "A𝄞B" where 𝄞 is U+1D11E
        List<XQueryValue> expected = List.of(
            baseFactory.string("A"),
            baseFactory.string("\uD834\uDD1E"),
            baseFactory.string("B")
        );
        assertResult("characters('A\uD834\uDD1E' || 'B')", expected
        );
    }

    @Test
    public void charactersAutoConversion() {
        assertResult("characters(123)", List.of(baseFactory.string("1"), baseFactory.string("2"), baseFactory.string("3")));
    }

    @Test
    public void charactersWrongArity() {
        assertError("characters('a', 'b')", XQueryError.WrongNumberOfArguments);
    }

    @Test
    public void graphemes_combiningMarkFormsCluster() {
        List<XQueryValue> expected = List.of(
            baseFactory.string("a\u0308"),
            baseFactory.string("b")
        );
        assertResult("graphemes('a' || fn:char(0x308) || 'b')", expected);
    }

    @Test
    public void graphemes_emptyStringYieldsEmptySequence() {
        assertResult("graphemes('')", List.of());
    }

    @Test
    public void graphemes_emptySequenceYieldsEmptySequence() {
        assertResult("graphemes(())", List.of());
    }

    @Test
    public void graphemes_crLfIsSingleGrapheme() {
        List<XQueryValue> expected = List.of(
            baseFactory.string("\r\n")
        );
        assertResult("graphemes(fn:char(0xD) || fn:char(0xA))", expected);
    }

    @Test
    public void graphemes_emojiZwjSequenceIsSingleGrapheme() {
        // 👶 (U+1F476), ZWJ, 🛑 (U+1F6D1)
        String cluster = "\uD83D\uDC76\u200D\uD83D\uDED1";
        assertResult("graphemes(fn:char(0x1F476) || fn:char(0x200D) || fn:char(0x1F6D1))",
            List.of(baseFactory.string(cluster))
        );
    }

    @Test
    public void graphemes_simpleDevanagari() {
        List<XQueryValue> expected = List.of(
            baseFactory.string("क"),
            baseFactory.string("त")
        );
        assertResult("graphemes('कत')", expected);
    }

    @Test
    public void graphemes_complexDevanagariCluster() {
        // क (U+0915), ◌़ (U+093C), ZWJ, ◌् (U+094D), त (U+0924)
        String cluster = "क\u093C\u200D\u094Dत";
        assertResult("graphemes('क' || fn:char(0x93C) || fn:char(0x200D) "
                   + "|| fn:char(0x94D) || 'त')",
            List.of(baseFactory.string(cluster))
        );
    }

    @Test
    public void graphemes_invalidArgumentType() {
        assertError("graphemes(123)", XQueryError.InvalidArgumentType);
    }

    @Test
    public void graphemes_wrongArity() {
        assertError("graphemes('a', 'b')", XQueryError.WrongNumberOfArguments);
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
    public void joinDigitsRange() {
        // string-join(1 to 9) => "123456789"
        assertResult(
            "string-join(1 to 9)",
            baseFactory.string("123456789")
        );
    }

    @Test
    public void joinWordsWithSpace() {
        // string-join(('Now', 'is', 'the', 'time', '...'), ' ') => "Now is the time ..."
        assertResult(
            "string-join(('Now', 'is', 'the', 'time', '...'), ' ')",
            baseFactory.string("Now is the time ...")
        );
    }

    @Test
    public void joinBlowWindNoSeparator() {
        // string-join(('Blow, ', 'blow, ', 'thou ', 'winter ', 'wind!'), '')
        // => "Blow, blow, thou winter wind!"
        assertResult(
            "string-join(('Blow, ', 'blow, ', 'thou ', 'winter ', 'wind!'), '')",
            baseFactory.string("Blow, blow, thou winter wind!")
        );
    }

    @Test
    public void joinEmptySequence() {
        // string-join((), 'separator') => ""
        assertResult(
            "string-join((), 'separator')",
            baseFactory.string("")
        );
    }

    @Test
    public void joinRangeWithCommaSpace() {
        // string-join(1 to 5, ', ') => "1, 2, 3, 4, 5"
        assertResult(
            "string-join(1 to 5, ', ')",
            baseFactory.string("1, 2, 3, 4, 5")
        );
    }


    @Test
    public void substring() {
        assertResult("substring('abcde', 4)", new XQueryString("de", baseFactory));
        assertResult("substring('abcde', 3, 2)", new XQueryString("cd", baseFactory));
    }

    @Test
    public void substringFromPositionToEnd() {
        assertResult(
            "substring(\"motor car\", 6)",
            baseFactory.string(" car")
        );
    }

    @Test
    public void substringWithLength() {
        assertResult(
            "substring(\"metadata\", 4, 3)",
            baseFactory.string("ada")
        );
    }

    @Test
    public void substringFractionalArguments() {
        assertResult(
            "substring(\"12345\", 1.5, 2.6)",
            baseFactory.string("234")
        );
    }

    @Test
    public void substringZeroStart() {
        assertResult(
            "substring(\"12345\", 0, 3)",
            baseFactory.string("12")
        );
    }

    @Test
    public void substringNegativeLengthYieldsEmpty() {
        assertResult(
            "substring(\"12345\", 5, -3)",
            baseFactory.string("")
        );
    }

    @Test
    public void substringNegativeStart() {
        assertResult(
            "substring(\"12345\", -3, 5)",
            baseFactory.string("1")
        );
    }

    @Test
    public void substringEmptyValueReturnsEmptyString() {
        assertResult(
            "substring((), 2, 3)",
            baseFactory.string("")
        );
    }

    // @Test
    // public void substringNaNStart() {
    //     assertResult(
    //         "substring(\"12345\", number('NaN'), 2)",
    //         baseFactory.string("")
    //     );
    // }

    // @Test
    // public void substringInfiniteLength() {
    //     assertResult(
    //         "substring(\"abcde\", 3, number('Infinity'))",
    //         baseFactory.string("cde")
    //     );
    // }

    @Test
    public void substringWrongArity() {
        assertError("substring('a')", XQueryError.WrongNumberOfArguments);
        assertError("substring('a', 1, 1, 1)", XQueryError.WrongNumberOfArguments);
    }

    @Test
    public void substringInvalidTypes() {
        assertError("substring('abc', 'one', 2)", XQueryError.InvalidArgumentType);
        assertError("substring('abc', 1, 'two')", XQueryError.InvalidArgumentType);
    }


    @Test
    public void stringLength() {
        assertResult("string-length('abcde')", new XQueryNumber(5, baseFactory));
        assertResult("string-length('')", new XQueryNumber(0, baseFactory));
    }

    @Test
    public void basicStringLength() {
        assertResult(
            "string-length(\"Harp not on that string, madam; that is past.\")",
            baseFactory.number(45)
        );
    }

    @Test
    public void contextStringLengthSingleGrapheme() {
        assertResult(
            "\"ᾧ\" => string-length()",
            baseFactory.number(1)
        );
    }

    // @Test
    // public void combiningCharactersIncreaseLength() {
    //     assertResult(
    //         "\"ᾧ\" => normalize-unicode(\"NFD\") => string-length()",
    //         baseFactory.number(4)
    //     );
    // }

    @Test
    public void stringLengthEmptySequence() {
        assertResult(
            "string-length(())",
            baseFactory.number(0)
        );
    }


    @Test
    public void normalization() {
        assertResult("normalize-space(' \t\n\r a    b \t \t c   \t')", new XQueryString("a b c", baseFactory));
    }

    @Test
    public void lowercase() {
        assertResult("lower-case('AbCdE')", new XQueryString("abcde", baseFactory));
        assertResult("lower-case(())", new XQueryString("", baseFactory));
    }

    @Test
    public void uppercase() {
        assertResult("upper-case('AbCdE')", new XQueryString("ABCDE", baseFactory));
        assertResult("upper-case(())", new XQueryString("", baseFactory));
    }

        @Test
    public void translateBarAbc() {
        assertResult(
            "translate(\"bar\", \"abc\", \"ABC\")",
            baseFactory.string("BAr")
        );
    }

    @Test
    public void translateStripDashes() {
        assertResult(
            "translate(\"--aaa--\", \"abc-\", \"ABC\")",
            baseFactory.string("AAA")
        );
    }

    @Test
    public void translateAbcdabc() {
        assertResult(
            "translate(\"abcdabc\", \"abc\", \"AB\")",
            baseFactory.string("ABdAB")
        );
    }


}
