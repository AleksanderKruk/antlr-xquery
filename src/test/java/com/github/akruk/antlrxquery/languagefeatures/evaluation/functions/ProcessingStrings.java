package com.github.akruk.antlrxquery.languagefeatures.evaluation.functions;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

public class ProcessingStrings extends EvaluationTestsBase {

    @Test public void testEmptyOutOfBoundsFilter() {
        assertResult("empty((1, 2, 3)[10])", valueFactory.bool(true));
    }


    // Tests for fn:codepoint-equal
    @Test public void testCodepointEqualSameAscii() {
        assertResult("codepoint-equal('abc','abc')",
            valueFactory.bool(true)
        );
    }

    @Test public void testCodepointEqualDifferentAscii() {
        assertResult("codepoint-equal('a','b')",
            valueFactory.bool(false)
        );
    }

    @Test public void testCodepointEqualSurrogatePair() {
        // U+1D11E MUSICAL SYMBOL G CLEF
        String gClef = "\uD834\uDD1E";
        assertResult("codepoint-equal('" + gClef + "','" + gClef + "')",
            valueFactory.bool(true)
        );
    }

    @Test public void testCodepointEqualNoNormalization() {
        // "e" + combining acute vs. precomposed "é"
        String combined = "e\u0301";
        String single   = "\u00E9";
        assertResult("codepoint-equal('" + combined + "','" + single + "')",
            valueFactory.bool(false)
        );
    }

    @Test public void testCodepointEqualEmptyAndNonEmpty() {
        assertResult("codepoint-equal((), 'a')",
            valueFactory.bool(false)
        );
        assertResult("codepoint-equal('a', ())",
            valueFactory.bool(false)
        );
    }

    @Test public void testCodepointEqualBothEmpty() {
        assertResult("codepoint-equal((), ())",
            valueFactory.emptySequence()
        );
    }

    // Tests for fn:string-to-codepoints
    @Test public void testStringToCodepointsBasic() {
        assertResult("string-to-codepoints('ABC')",
            valueFactory.sequence(List.of(
                valueFactory.number(65),
                valueFactory.number(66),
                valueFactory.number(67)
            ))
        );
    }

    @Test public void testStringToCodepointsSurrogate() {
        // U+1D11E MUSICAL SYMBOL G CLEF
        String gClef = "\uD834\uDD1E";
        assertResult("string-to-codepoints('" + gClef + "')",
            valueFactory.sequence(List.of(
                valueFactory.number(0x1D11E)
            ))
        );
    }

    @Test public void testStringToCodepointsEmptyString() {
        assertResult("string-to-codepoints('')",
            valueFactory.emptySequence()
        );
    }

    @Test public void testStringToCodepointsEmptySequence() {
        assertResult("string-to-codepoints(())",
            valueFactory.emptySequence()
        );
    }

    // Tests for fn:codepoints-to-string
    @Test public void testCodepointsToStringBasic() {
        assertResult("codepoints-to-string((65,66,67))",
            valueFactory.string("ABC")
        );
    }

    @Test public void testCodepointsToStringSurrogate() {
        // U+1D11E MUSICAL SYMBOL G CLEF
        assertResult("codepoints-to-string((119070))",
            valueFactory.string("\uD834\uDD1E")
        );
    }

    @Test public void testCodepointsToStringEmptySequence() {
        assertResult("codepoints-to-string(())",
            valueFactory.string("")
        );
    }


}
