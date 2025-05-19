package com.github.akruk.antlrxquery;

import static org.junit.Assert.assertTrue;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.baseimplementation.XQueryBaseSemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticFunctionCaller;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQueryBaseSemanticFunctionCaller;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryEnumTypeFactory;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryMemoizedValueFactory;

public class XQuerySemanticAnalyzerTest {
    XQueryTypeFactory typeFactory = new XQueryEnumTypeFactory();
    // final XQueryEnumBasedType string = XQueryEnumBasedType.string();
    // final XQueryEnumBasedType integer = XQueryEnumBasedType.integer();
    // final XQueryEnumBasedType number = XQueryEnumBasedType.number();
    // final XQueryEnumBasedType anyNode = XQueryEnumBasedType.anyNode();
    // final XQueryEnumBasedType emptySequence = XQueryEnumBasedType.emptySequence();
    // final XQueryEnumBasedType stringSequenceOneOrMore = XQueryEnumBasedType.sequence(XQueryEnumBasedType.string, XQueryOccurence.ONE_OR_MORE);
    // final XQueryEnumBasedType stringSequenceZeroOrMore = XQueryEnumBasedType.sequence(XQueryEnumBasedType.string, XQueryOccurence.ZERO_OR_MORE);
    // final XQueryEnumBasedType stringSequenceZeroOrOne = XQueryEnumBasedType.sequence(XQueryEnumBasedType.string,
    //         XQueryOccurence.ZERO_OR_ONE);


    record AnalysisResult(XQuerySemanticAnalyzer analyzer, XQuerySequenceType expressionType) {};

    AnalysisResult analyze(final String text) {
        final CharStream characters = CharStreams.fromString(text);
        final Lexer xqueryLexer = new AntlrXqueryLexer(characters);
        final CommonTokenStream xqueryTokens = new CommonTokenStream(xqueryLexer);
        final AntlrXqueryParser xqueryParser = new AntlrXqueryParser(xqueryTokens);
        final ParseTree xqueryTree = xqueryParser.xquery();
        final XQuerySemanticFunctionCaller caller = new XQueryBaseSemanticFunctionCaller();
        final XQuerySemanticAnalyzer analyzer = new XQuerySemanticAnalyzer(
            xqueryParser,
            new XQueryBaseSemanticContextManager(),
            new XQueryEnumTypeFactory(),
            new XQueryMemoizedValueFactory(),
            caller);
        var lastVisitedType = analyzer.visit(xqueryTree);
        return new AnalysisResult(analyzer, lastVisitedType);
    }

    void assertNoErrors(AnalysisResult analyzer) {
        assertTrue(analyzer.analyzer.getErrors().size() == 0);
    }

    void assertThereAreErrors(String xquery) {
        var analysisResult = analyze(xquery);
        assertThereAreErrors(analysisResult);
    }

    void assertThereAreErrors(AnalysisResult analyzer) {
        assertTrue(analyzer.analyzer.getErrors().size() != 0);
    }

    void assertType(AnalysisResult result, XQuerySequenceType expectedType) {
        assertNoErrors(result);
        assertTrue(result.expressionType.isSubtypeOf(expectedType));
    }

    void assertType(String xquery, XQuerySequenceType expectedType) {
        var analysisResult = analyze(xquery);
        assertNoErrors(analysisResult);
        assertTrue(analysisResult.expressionType.isSubtypeOf(expectedType));
    }



    @Test
    public void parenthesizedExpression() {
        assertType("()", typeFactory.emptySequence());
        assertType("(1)", typeFactory.number());
        assertType("(1, 'a')", typeFactory.oneOrMore(typeFactory.itemAnyItem()));
        assertType("(1, 2, 3)", typeFactory.oneOrMore(typeFactory.itemNumber()));
        assertType("((), (), (1))", typeFactory.number());
        assertType("((), (1), (1))", typeFactory.oneOrMore(typeFactory.itemNumber()));
    }

    @Test
    public void orExpressions() {
        assertType("true() or false() or true()", typeFactory.boolean_());
        assertType("1 or false() or true()", typeFactory.boolean_());
    }

    @Test
    public void andExpressions() {
        assertType("true() and false() and true()", typeFactory.boolean_());
        assertType("1 and false() and true()", typeFactory.boolean_());
    }

    @Test
    public void notExpression() {
        assertType("not(true())", typeFactory.boolean_());
        assertType("not(4)", typeFactory.boolean_());
        assertType("fn:not(true())", typeFactory.boolean_());
        assertType("fn:not(4)", typeFactory.boolean_());
        assertThereAreErrors("fn:not()");
        assertThereAreErrors("fn:not(1, 2)");
    }

    @Test
    public void concatenation() {
        assertType("'a'|| 'b'", typeFactory.string());
        assertType("'a' || ()", typeFactory.string());
        assertType(" () || ()", typeFactory.string());
    }

    @Test
    public void variableBinding() {
        assertType("let $x := 1 return $x", typeFactory.number());
        assertType("let $x as item() := 1 return $x", typeFactory.anyItem());
        assertType("let $x as boolean := 1 return $x", typeFactory.boolean_());
    }



}
