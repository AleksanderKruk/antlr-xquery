package com.github.akruk.antlrquery.languagefeatures.semantics.functions;

import java.util.Set;

import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;

public class CardinalityFunctionsTest extends SemanticTestsBase {

    // fn:zero-or-one($input as item()*) as item()?
    @Test
    public void zeroOrOne_withMultipleItems() {
        assertType(
            "fn:zero-or-one((1, 2, 3))",
            typeFactory.zeroOrOne(typeFactory.itemNumber(NumericRange.of(1,2,3)))
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
            typeFactory.one(typeFactory.itemNumber(NumericRange.of(42)))
        );
    }

    @Test
    public void oneOrMore_sequence() {
        assertType(
            "fn:one-or-more((true(), false()))",
            typeFactory.sequence(typeFactory.itemBoolean(), Cardinality.of(2))
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
            typeFactory.one(typeFactory.itemNumber(NumericRange.of(1)))
        );
        assertType(
            "fn:exactly-one((1, 2, 3))",
            typeFactory.one(typeFactory.itemNumber(NumericRange.of(1, 2,3 )))
        );
    }

    @Test
    public void exactlyOne_singleCall() {
        assertType(
            "fn:exactly-one(input := (1))",
            typeFactory.one(typeFactory.itemNumber(NumericRange.of(1)))
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
