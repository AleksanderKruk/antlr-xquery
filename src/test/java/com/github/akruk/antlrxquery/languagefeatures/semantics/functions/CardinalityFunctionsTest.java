package com.github.akruk.antlrxquery.languagefeatures.semantics.functions;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.semantics.SemanticTestsBase;

public class CardinalityFunctionsTest extends SemanticTestsBase {

    // fn:zero-or-one($input as item()*) as item()?
    @Test
    public void zeroOrOne_withMultipleItems() {
        assertType(
            "fn:zero-or-one((1, 2, 3))",
            typeFactory.zeroOrOne(typeFactory.itemNumber())
        );
        // assertErrors( "fn:zero-or-one((1, 2, 3))");
    }

    @Test
    public void zeroOrOne_namedArg() {
        assertType(
            "fn:zero-or-one(input := ('a','b'))",
            typeFactory.zeroOrOne(typeFactory.itemEnum(Set.of("a", "b")))
        );
    }

    @Test
    public void zeroOrOne_missingArg() {
        assertErrors("fn:zero-or-one()");
    }

    @Test
    public void zeroOrOne_tooManyArgs() {
        assertErrors("fn:zero-or-one(1, 2)");
    }


    // fn:one-or-more($input as item()*) as item()+
    @Test
    public void oneOrMore_singleItem() {
        assertType(
            "fn:one-or-more(42)",
            typeFactory.one(typeFactory.itemNumber())
        );
    }

    @Test
    public void oneOrMore_sequence() {
        assertType(
            "fn:one-or-more((true(), false()))",
            typeFactory.oneOrMore(typeFactory.itemBoolean())
        );
    }

    @Test
    public void oneOrMore_missingArg() {
        assertErrors("fn:one-or-more()");
    }

    @Test
    public void oneOrMore_tooManyArgs() {
        assertErrors("fn:one-or-more(1,2)");
    }


    // fn:exactly-one($input as item()*) as item()
    @Test
    public void exactlyOne_sequenceOfTwo() {
        assertType(
            "fn:exactly-one(1)",
            typeFactory.one(typeFactory.itemNumber())
        );
        assertType(
            "fn:exactly-one((1, 2, 3))",
            typeFactory.one(typeFactory.itemNumber())
        );
    }

    @Test
    public void exactlyOne_singleCall() {
        assertType(
            "fn:exactly-one(input := (1))",
            typeFactory.one(typeFactory.itemNumber())
        );
    }

    @Test
    public void exactlyOne_missingArg() {
        assertErrors("fn:exactly-one()");
    }

    @Test
    public void exactlyOne_tooManyArgs() {
        assertErrors("fn:exactly-one(1,2,3)");
    }
}
