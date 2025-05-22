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
        // If casting should be done, then the type of $x should be number
        // assertType("let $x as boolean := 1 return $x", typeFactory.boolean_());
    }

    @Test
    public void forClauseBinding() {
        assertType("for $x in (1, 2, 3) return $x", typeFactory.oneOrMore(typeFactory.itemNumber()));
    }

    @Test
    public void indexVariableBinding() {
        assertType("for $x at $i in (1, 2, 3) return $i", typeFactory.oneOrMore(typeFactory.itemNumber()));
    }


    @Test
    public void countVariableClause() {
        assertType("""
            for $x at $i in (1, 2, 3)
            count $count
            return $count
        """, typeFactory.oneOrMore(typeFactory.itemNumber()));
    }


    @Test
    public void whereClause() {
        assertType("""
            for $x at $i in (1, 2, 3)
            where $x > 3
            return $x
        """, typeFactory.zeroOrMore(typeFactory.itemNumber()));
    }

    @Test
    public void whileClause() {
        assertType("""
            for $x at $i in (1, 2, 3)
            while $x > 3
            return $x
        """, typeFactory.zeroOrMore(typeFactory.itemNumber()));
    }

    @Test
    public void rangeExpression() {
        var numbers = typeFactory.zeroOrMore(typeFactory.itemNumber());
        assertType("""
            1 to 5
        """, numbers);
        assertType("""
            let $x as number? := 5
            return $x to 5
        """, numbers);
        assertType("""
            let $x as number? := 5
            return 5 to $x
        """, numbers);
        assertType("""
            let $x as number? := 5,
                $y as number? := 6
            return $x to $y
        """, numbers);
        assertThereAreErrors("""
            let $x as string? := "a",
                $y as number? := 6
            return $x to $y
        """);
        assertThereAreErrors("""
            let $x as number? := 4,
                $y as string? := "a"
            return $x to $y
        """);
        assertThereAreErrors("""
            let $x := (1, 2, 3, 4),
                $y := (4, 5, 6, 7)
            return $x to $y
        """);
        assertThereAreErrors("""
            let $x as number+ := (1, 2, 3, 4),
                $y as number+ := (4, 5, 6, 7)
            return $x to $y
        """);
        assertThereAreErrors("""
            let $x as item()+ := (1, 2, 3, 4),
                $y as item()+ := (4, 5, 6, 7)
            return $x to $y
        """);
    }


}



