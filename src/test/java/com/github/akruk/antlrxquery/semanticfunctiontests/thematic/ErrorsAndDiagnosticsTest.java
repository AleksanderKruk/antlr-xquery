package com.github.akruk.antlrxquery.semanticfunctiontests.thematic;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.semanticfunctiontests.FunctionsSemanticTest;

public class ErrorsAndDiagnosticsTest extends FunctionsSemanticTest {
    // --- fn:error(QName? :=(), string? :=(), item()* :=.) as item()* ---

    @Test
    public void error_default() {
        assertType("fn:error()",typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
    }

    @Test
    public void error_allNamed() {
        assertNoErrors(analyze(
                "fn:error(code := 'err', " +
                        "description := 'msg', value := 1)"));
    }

    @Test
    public void error_badDesc() {
        assertErrors("fn:error(description := 5)");
    }

    @Test
    public void error_tooMany() {
        assertErrors("fn:error(1,2,3,4)");
    }

    // --- fn:trace(item()*, string? :=()) as item()* ---

    @Test
    public void trace_missingInput() {
        assertErrors("fn:trace()");
    }

    // @Test
    // public void trace_onlyInput() {
    //     assertType("fn:trace(,))"),typeFactory.zeroOrMore(typeFactory.itemAnyItem());
    // }

    @Test
    public void trace_withLabel() {
        assertNoErrors(analyze("fn:trace(1, 'lbl')"));
    }

    @Test
    public void trace_badLabel() {
        assertErrors("fn:trace(1, 2)");
    }

    // --- fn:message(item()*, string? :=()) as empty-sequence() ---

    @Test
    public void message_onlyInput() {
        assertType("fn:message(1)",typeFactory.emptySequence());
    }

    @Test
    public void message_withLabel() {
        assertType("fn:message('x', 'lbl'))", typeFactory.emptySequence());
    }

    @Test
    public void message_missing() {
        assertErrors("fn:message()");
    }

    @Test
    public void message_badLabel() {
        assertErrors("fn:message(1, 2, 3)");
    }


}
