package com.github.akruk.antlrxquery.languagefeatures.evaluation.functions;


import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

public class ComparisonFunctions extends EvaluationTestsBase {

    @Test public void testAtomicEqual_IntegerLiterals() {
        assertResult("atomic-equal(3, 3)",
            valueFactory.bool(true)
        );
    }

    @Test public void testAtomicEqual_IntegerAndScientific() {
        assertResult("atomic-equal(3, 3e0)",
            valueFactory.bool(true)
        );
    }

    @Test public void testAtomicEqual_SameString() {
        assertResult("atomic-equal(\"a\", \"a\")",
            valueFactory.bool(true)
        );
    }

    @Test public void testAtomicEqual_DifferentCaseString() {
        assertResult("atomic-equal(\"a\", \"A\")",
            valueFactory.bool(false)
        );
    }
    @Test public void testAtomicEqual_NumberAndString() {
        assertResult("atomic-equal(12, \"12\")",
            valueFactory.bool(false)
        );
    }

}
