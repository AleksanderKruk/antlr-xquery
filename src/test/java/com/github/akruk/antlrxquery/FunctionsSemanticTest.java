package com.github.akruk.antlrxquery;
import static org.junit.jupiter.api.Assertions.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.baseimplementation.XQueryBaseSemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQuerySemanticFunctionManager;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryEnumTypeFactory;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryMemoizedValueFactory;

import java.util.List;

public class FunctionsSemanticTest {
    private XQueryEnumTypeFactory typeFactory;
    private XQuerySemanticFunctionManager fnManager;
    private XQuerySemanticAnalyzer analyzer;

    record AnalysisResult(XQuerySemanticAnalyzer analyzer, XQuerySequenceType type) {}

    @BeforeEach
    void init() {
        typeFactory = new XQueryEnumTypeFactory();
        fnManager   = new XQuerySemanticFunctionManager(typeFactory);
    }

    private AnalysisResult analyze(String xquery) {
        CharStream chars = CharStreams.fromString(xquery);
        var lexer = new AntlrXqueryLexer(chars);
        var tokens = new CommonTokenStream(lexer);
        var parser = new AntlrXqueryParser(tokens);
        var tree = parser.xquery();
        analyzer = new XQuerySemanticAnalyzer(
            parser,
            new XQueryBaseSemanticContextManager(),
            typeFactory,
            new XQueryMemoizedValueFactory(),
            fnManager);
        var resultType = analyzer.visit(tree);
        return new AnalysisResult(analyzer, resultType);
    }

    private void assertNoErrors(AnalysisResult r) {
        assertTrue(r.analyzer.getErrors().isEmpty(),
            "Expected no errors, got: " + r.analyzer.getErrors());
    }

    private void assertErrors(String xq) {
        var r = analyze(xq);
        assertFalse(r.analyzer.getErrors().isEmpty(),
            "Expected errors for [" + xq + "]");
    }

    private void assertType(String xq, XQuerySequenceType expected) {
        var r = analyze(xq);
        assertNoErrors(r);
        assertEquals(expected, r.type);
    }

    @Test void pi_correctArity() {
        assertType("math:pi()", typeFactory.one(typeFactory.itemNumber()));
    }
    @Test void pi_tooManyArgs() {
        assertErrors("math:pi(1)");
        assertErrors("math:pi(1,2)");
    }

    @Test void e_correctArity() {
        assertType("math:e()", typeFactory.one(typeFactory.itemNumber()));
    }
    @Test void e_wrongArity() {
        assertErrors("math:e(0.1)");
    }

    @Test void exp_positional() {
        assertType("math:exp(2.5)",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }
    @Test void exp_named() {
        assertType("math:exp(value := 3.14)",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }
    @Test void exp_missingArg() {
        assertErrors("math:exp()");
    }
    @Test void exp_wrongType() {
        assertErrors("math:exp('foo')");
    }
    @Test void exp_unknownKeyword() {
        assertErrors("math:exp(v := 2.5)");
    }

    @Test void log_variants() {
        for (String fn : List.of("log","log10","exp10","sqrt")) {
            String call = "math:" + fn + "(1.0)";
            assertType(call,
                typeFactory.zeroOrOne(typeFactory.itemNumber()));
            assertErrors("math:" + fn + "()");
            assertErrors("math:" + fn + "(1,2)");
        }
    }

    @Test void trig_functions() {
        for (String fn : List.of("sin","cos","tan","asin","acos")) {
            String ok   = "math:"+fn+"(0.0)";
            String err1 = "math:"+fn+"()";
            String err2 = "math:"+fn+"(0.0, 1.0)";
            String err3 = "math:"+fn+"(val:=0.0)";
            assertType(ok,
                typeFactory.zeroOrOne(typeFactory.itemNumber()));
            assertErrors(err1);
            assertErrors(err2);
            assertErrors(err3);
        }
    }

    @Test void pow_positional() {
        assertType("math:pow(2.0, 3)",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }
    @Test void pow_namedAll() {
        assertType("math:pow(x := 2.0, y := 3)",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }
    @Test void pow_mixed() {
        assertType("math:pow(2.0, y := 4)",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
        assertType("math:pow(x := 1.1, 5)",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }
    @Test void pow_wrongArity() {
        assertErrors("math:pow(2.0)");
        assertErrors("math:pow(y := 4)");
        assertErrors("math:pow()");
        assertErrors("math:pow(1,2,3)");
    }
    @Test void pow_wrongNames() {
        assertErrors("math:pow(a:=2.0, b:=3)");
    }
    @Test void pow_wrongTypes() {
        assertErrors("math:pow('x', 5)");
        assertErrors("math:pow(2.0, 'y')");
    }
}
