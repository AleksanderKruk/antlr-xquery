package com.github.akruk.antlrquery.languagefeatures.semantics.functions.processingarrays;

import com.github.akruk.antlrquery.languagefeatures.semantics.SemanticTestsBase;
import com.github.akruk.antlrquery.semanticanalyzer.ErrorType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import org.junit.jupiter.api.Test;

import java.util.List;

public class FunctionArrayEmptySemanticTests extends SemanticTestsBase {
    // array:empty($array as array(*)) as xs:boolean
    @Test
    public void arrayEmpty_valid() {
        assertType(
                "array:empty(array{})",
                typeFactory.sequence(typeFactory.itemTrue(), Cardinality.ONE));

        assertType(
                "array:empty(array{()})",
                typeFactory.sequence(typeFactory.itemTrue(), Cardinality.ONE));

        assertType(
                "array:empty(array{1})",
                typeFactory.sequence(typeFactory.itemFalse(), Cardinality.ONE));

        assertType(
                "array:empty(array{1, 2, 3})",
                typeFactory.sequence(typeFactory.itemFalse(), Cardinality.ONE));

        assertType(
                "array:empty(array{[]})",
                typeFactory.sequence(typeFactory.itemFalse(), Cardinality.ONE));

        assertType(
                "array:empty(array{array{}})",
                typeFactory.sequence(typeFactory.itemFalse(), Cardinality.ONE));
    }

    @Test
    public void arrayEmpty_cardinality() {
        assertType(
                "array:empty(array{})",
                typeFactory.sequence(typeFactory.itemTrue(), Cardinality.ONE));

        assertType(
                "array:empty(array{1})",
                typeFactory.sequence(typeFactory.itemFalse(), Cardinality.ONE));
    }

    @Test
    public void arrayEmpty_errors() {
        assertDiagnostics(
                "array:empty()",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of());

        assertDiagnostics(
                "array:empty(1)",
                List.of(ErrorType.FUNCTION__NO_MATCHING_FUNCTION),
                List.of());
    }
}
