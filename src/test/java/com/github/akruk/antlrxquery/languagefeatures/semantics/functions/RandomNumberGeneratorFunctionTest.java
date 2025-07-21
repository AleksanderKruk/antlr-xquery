package com.github.akruk.antlrxquery.languagefeatures.semantics.functions;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;

import org.junit.jupiter.api.Test;

public class RandomNumberGeneratorFunctionTest extends SemanticTestsBase {

    /**
     * Helper to get the expected return type of fn:random-number-generator(...)
     */
    private XQuerySequenceType expectedRngType() {
        // random-number-generator-record wrapped in a single-occurrence sequence
        return typeFactory.namedType("fn:random-number-generator-record");
    }

    @Test
    public void rng_noArg_usesDefault() {
        // no seed argument uses the default
        assertType(
            "fn:random-number-generator()",
            expectedRngType()
        );
    }

    @Test
    public void rng_withNumericSeed() {
        // numeric seed is allowed (xs:anyAtomicType?)
        assertType(
            "fn:random-number-generator(123)",
            expectedRngType()
        );
    }

    @Test
    public void rng_withStringSeed() {
        // string seed is allowed
        assertType(
            "fn:random-number-generator('seed')",
            expectedRngType()
        );
    }

    @Test
    public void rng_namedSeed() {
        // named parameter syntax
        assertType(
            "fn:random-number-generator(seed := 42)",
            expectedRngType()
        );
    }

    @Test
    public void rng_wrongSeedType() {
        // element() is not an atomic type
        assertErrors("fn:random-number-generator(<a/>)");
    }

    @Test
    public void rng_tooManyArgs() {
        // only zero or one argument is allowed
        assertErrors("fn:random-number-generator(1, 2)");
    }
}
