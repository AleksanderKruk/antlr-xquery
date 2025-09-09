package com.github.akruk.antlrxquery.languagefeatures.evaluation.functions;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.languagefeatures.evaluation.EvaluationTestsBase;

public class UserDefined extends EvaluationTestsBase {
    @Test
    public void nodeName() throws Exception {
        String query = """
            declare function x($arg as number) as number+ {
                1, 2, 3, $arg
            }

            x(4)
        """;
        assertResult(query, List.of(
            valueFactory.number(1),
            valueFactory.number(2),
            valueFactory.number(3),
            valueFactory.number(4))
        );
    }


}
