package com.github.akruk.antlrquery.languagefeatures.semantics.rangeexpression;

import com.github.akruk.antlrquery.semanticanalyzer.ErrorType;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Ranges;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;

import java.util.List;

public class RangeExpressionSemanticTests extends SemanticTestsBase {

    @Test
    public void rangeExpression() {
        assertType("""
                    1 to 5
                """,
                typeFactory.number(Ranges.integers(1, 6))
        );
        assertType("""
                    let $x as 5? := 5
                    return ($x to 5)
                """,
                typeFactory.zeroOrOne(typeFactory.itemNumber(NumericRange.of(5)))
        );

        assertType("""
                    () to 5
                """, typeFactory.emptySequence());
        assertType("""
                    5 to ()
                """, typeFactory.emptySequence());
        assertType("""
                    () to ()
                """, typeFactory.emptySequence());

        assertType("""
                    let $x as 5 := 5,
                        $y as 6 := 6
                    return ($x to $y)
                """, typeFactory.number(Ranges.integers(5, 7)));
        assertDiagnostics("""
                    "a" to 6
                """, List.of(ErrorType.RANGE__INVALID_FROM), List.of());
        assertDiagnostics("""
                    4 to "d"
                """, List.of(ErrorType.RANGE__INVALID_TO), List.of()
        );
        assertDiagnostics("""
                    true() to "d"
                """, List.of(ErrorType.RANGE__INVALID_BOTH), List.of()
        );
        assertDiagnostics("""
                    let $x := (1, 2, 3, 4),
                        $y := (4, 5, 6, 7)
                    return ($x to $y)
                """, List.of(ErrorType.RANGE__INVALID_BOTH), List.of()
        );
        assertDiagnostics("""
                    let $x := (1, 2, 3, 4),
                        $y := (4, 5, 6, 7)
                    return ($x to $y)
                """, List.of(ErrorType.RANGE__INVALID_BOTH), List.of()
        );
        assertDiagnostics("""
                    let $x as item()+ := (1, 2, 3, 4),
                        $y as item()+ := (4, 5, 6, 7)
                    return ($x to $y)
                """, List.of(ErrorType.RANGE__INVALID_BOTH), List.of()
        );
    }


}
