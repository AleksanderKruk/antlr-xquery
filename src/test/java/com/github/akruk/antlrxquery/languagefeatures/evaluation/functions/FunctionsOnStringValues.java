package com.github.akruk.antlrxquery.languagefeatures.evaluation.functions;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.evaluator.values.*;
import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

public class FunctionsOnStringValues extends EvaluationTestsBase {
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
    public void graphemes_combiningMarkFormsCluster() {
        List<XQueryValue> expected = List.of(
            valueFactory.string("a\u0308"),
            valueFactory.string("b")
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
            valueFactory.string("\r\n")
        );
        assertResult("graphemes(fn:char(0xD) || fn:char(0xA))", expected);
    }

    @Test
    public void graphemes_emojiZwjSequenceIsSingleGrapheme() {
        // 👶 (U+1F476), ZWJ, 🛑 (U+1F6D1)
        String cluster = "\uD83D\uDC76\u200D\uD83D\uDED1";
        assertResult("graphemes(fn:char(0x1F476) || fn:char(0x200D) || fn:char(0x1F6D1))",
            List.of(valueFactory.string(cluster))
        );
    }

    @Test
    public void graphemes_simpleDevanagari() {
        List<XQueryValue> expected = List.of(
            valueFactory.string("क"),
            valueFactory.string("त")
        );
        assertResult("graphemes('कत')", expected);
    }

    @Test
    public void graphemes_complexDevanagariCluster() {
        // क (U+0915), ◌़ (U+093C), ZWJ, ◌् (U+094D), त (U+0924)
        String cluster = "क\u093C\u200D\u094Dत";
        assertResult("graphemes('क' || fn:char(0x93C) || fn:char(0x200D) "
                   + "|| fn:char(0x94D) || 'त')",
            List.of(valueFactory.string(cluster))
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
        assertResult("concat(('a', 'b', 'c'))", new XQueryString("abc", valueFactory));
    }

    @Test
    public void concatVariadic() {
        assertResult("concat('a', 'b', 'c')", new XQueryString("abc", valueFactory));
    }

    @Test
    public void stringJoin() {
        assertResult("string-join(('a', 'b', 'c'))", new XQueryString("abc", valueFactory));
        assertResult("string-join(('a', 'b', 'c'), '-')", new XQueryString("a-b-c", valueFactory));
    }
   @Test
    public void joinDigitsRange() {
        // string-join(1 to 9) => "123456789"
        assertResult(
            "string-join(1 to 9)",
            valueFactory.string("123456789")
        );
    }

    @Test
    public void joinWordsWithSpace() {
        // string-join(('Now', 'is', 'the', 'time', '...'), ' ') => "Now is the time ..."
        assertResult(
            "string-join(('Now', 'is', 'the', 'time', '...'), ' ')",
            valueFactory.string("Now is the time ...")
        );
    }

    @Test
    public void joinBlowWindNoSeparator() {
        // string-join(('Blow, ', 'blow, ', 'thou ', 'winter ', 'wind!'), '')
        // => "Blow, blow, thou winter wind!"
        assertResult(
            "string-join(('Blow, ', 'blow, ', 'thou ', 'winter ', 'wind!'), '')",
            valueFactory.string("Blow, blow, thou winter wind!")
        );
    }

    @Test
    public void joinEmptySequence() {
        // string-join((), 'separator') => ""
        assertResult(
            "string-join((), 'separator')",
            valueFactory.string("")
        );
    }

    @Test
    public void joinRangeWithCommaSpace() {
        // string-join(1 to 5, ', ') => "1, 2, 3, 4, 5"
        assertResult(
            "string-join(1 to 5, ', ')",
            valueFactory.string("1, 2, 3, 4, 5")
        );
    }


    @Test
    public void substring() {
        assertResult("substring('abcde', 4)", new XQueryString("de", valueFactory));
        assertResult("substring('abcde', 3, 2)", new XQueryString("cd", valueFactory));
    }

    @Test
    public void substringFromPositionToEnd() {
        assertResult(
            "substring(\"motor car\", 6)",
            valueFactory.string(" car")
        );
    }

    @Test
    public void substringWithLength() {
        assertResult(
            "substring(\"metadata\", 4, 3)",
            valueFactory.string("ada")
        );
    }

    @Test
    public void substringFractionalArguments() {
        assertResult(
            "substring(\"12345\", 1.5, 2.6)",
            valueFactory.string("234")
        );
    }

    @Test
    public void substringZeroStart() {
        assertResult(
            "substring(\"12345\", 0, 3)",
            valueFactory.string("12")
        );
    }

    @Test
    public void substringNegativeLengthYieldsEmpty() {
        assertResult(
            "substring(\"12345\", 5, -3)",
            valueFactory.string("")
        );
    }

    @Test
    public void substringNegativeStart() {
        assertResult(
            "substring(\"12345\", -3, 5)",
            valueFactory.string("1")
        );
    }

    @Test
    public void substringEmptyValueReturnsEmptyString() {
        assertResult(
            "substring((), 2, 3)",
            valueFactory.string("")
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
        assertResult("string-length('abcde')", new XQueryNumber(5, valueFactory));
        assertResult("string-length('')", new XQueryNumber(0, valueFactory));
    }

    @Test
    public void basicStringLength() {
        assertResult(
            "string-length(\"Harp not on that string, madam; that is past.\")",
            valueFactory.number(45)
        );
    }

    @Test
    public void contextStringLengthSingleGrapheme() {
        assertResult(
            "\"ᾧ\" => string-length()",
            valueFactory.number(1)
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
            valueFactory.number(0)
        );
    }


    @Test
    public void normalization() {
        assertResult("normalize-space(' \t\n\r a    b \t \t c   \t')", new XQueryString("a b c", valueFactory));
    }

    @Test
    public void lowercase() {
        assertResult("lower-case('AbCdE')", new XQueryString("abcde", valueFactory));
        assertResult("lower-case(())", new XQueryString("", valueFactory));
    }

    @Test
    public void uppercase() {
        assertResult("upper-case('AbCdE')", new XQueryString("ABCDE", valueFactory));
        assertResult("upper-case(())", new XQueryString("", valueFactory));
    }

        @Test
    public void translateBarAbc() {
        assertResult(
            "translate(\"bar\", \"abc\", \"ABC\")",
            valueFactory.string("BAr")
        );
    }

    @Test
    public void translateStripDashes() {
        assertResult(
            "translate(\"--aaa--\", \"abc-\", \"ABC\")",
            valueFactory.string("AAA")
        );
    }

    @Test
    public void translateAbcdabc() {
        assertResult(
            "translate(\"abcdabc\", \"abc\", \"AB\")",
            valueFactory.string("ABdAB")
        );
    }


}
