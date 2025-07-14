package com.github.akruk.antlrxquery.languagefeatures.semantics;

import java.util.List;

import org.junit.jupiter.api.Test;

public class InlineFunctionsTest extends SemanticTestsBase {

    @Test
    public void typedParametersAndTypedResult() {
        assertType(
            "function($x as number, $y as number) as number { $x + $y }",
            typeFactory.function(
                typeFactory.one(typeFactory.itemNumber()),
                List.of(
                    typeFactory.one(typeFactory.itemNumber()),
                    typeFactory.one(typeFactory.itemNumber())
                )
            )
        );
    }

    @Test
    public void untypedParametersAndNoResultType() {
        assertType(
            "fn($a, $b) {}",
            typeFactory.function(
                typeFactory.emptySequence(),
                List.of(
                    typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                    typeFactory.zeroOrMore(typeFactory.itemAnyItem())
                )
            )
        );
    }

    @Test
    public void zeroArityFunctionWithResultType() {
        assertType(
            "function() as number+ { 2, 3, 5 }",
            typeFactory.function(
                typeFactory.oneOrMore(typeFactory.itemNumber()),
                List.of()
            )
        );
    }

    @Test
    public void inlineFunctionCapturesOuterBinding() {
        assertType(
            "let $n := 10 return function($x as number) as number { $x + $n }",
            typeFactory.function(
                typeFactory.one(typeFactory.itemNumber()),
                List.of(typeFactory.one(typeFactory.itemNumber()))
            )
        );
    }

    // @Test
    // public void methodAnnotationIsAccepted() {
    //     assertType(
    //         "%method function($s as xs:string) as xs:string { $s }",
    //         typeFactory.function(
    //             typeFactory.one(typeFactory.itemString()),
    //             List.of(typeFactory.one(typeFactory.itemString()))
    //         )
    //     );
    // }

    // @Test
    // public void invalidAnnotationRaisesError() {
    //     assertError(
    //         "%private function($x) { $x }",
    //         XQuerySemanticError.InvalidInlineFunctionAnnotation
    //     );
    // }

    @Test
    public void duplicateParameterRaisesError() {
        assertThereAreErrors(
            "function($a, $a) { $a }"
        );
            // XQuerySemanticError.DuplicateFunctionParam
    }
}
