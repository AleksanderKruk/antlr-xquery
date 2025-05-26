package com.github.akruk.antlrxquery;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.baseimplementation.XQueryBaseSemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticFunctionCaller;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQueryBaseSemanticFunctionCaller;
import com.github.akruk.antlrxquery.testgrammars.TestParser;
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
        final TestParser testParser = new TestParser(xqueryTokens);
        final XQuerySemanticFunctionCaller caller = new XQueryBaseSemanticFunctionCaller();
        final XQuerySemanticAnalyzer analyzer = new XQuerySemanticAnalyzer(
            testParser,
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
        assertTrue(result.expressionType.equals(expectedType));
    }

    void assertType(String xquery, XQuerySequenceType expectedType) {
        var analysisResult = analyze(xquery);
        assertNoErrors(analysisResult);
        assertTrue(analysisResult.expressionType.equals(expectedType));
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
        final var numbers = typeFactory.zeroOrMore(typeFactory.itemNumber());
        assertType("""
            1 to 5
        """, numbers);
        assertType("""
            let $x as number? := 5
            return ($x to 5)
        """, numbers);
        assertType("""
            let $x as number? := 5
            return (5 to $x)
        """, numbers);
        assertType("""
            let $x as number? := 5,
                $y as number? := 6
            return ($x to $y)
        """, numbers);
        assertThereAreErrors("""
            let $x as string? := "a",
                $y as number? := 6
            return ($x to $y)
        """);
        assertThereAreErrors("""
            let $x as number? := 4,
                $y as string? := "a"
            return ($x to $y)
        """);
        assertThereAreErrors("""
            let $x := (1, 2, 3, 4),
                $y := (4, 5, 6, 7)
            return ($x to $y)
        """);
        assertThereAreErrors("""
            let $x as number+ := (1, 2, 3, 4),
                $y as number+ := (4, 5, 6, 7)
            return ($x to $y)
        """);
        assertThereAreErrors("""
            let $x as item()+ := (1, 2, 3, 4),
                $y as item()+ := (4, 5, 6, 7)
            return ($x to $y)
        """);
    }

    @Test
    public void otherwiseExpression() {
        final var number = typeFactory.number();
        final var optionalNumber = typeFactory.zeroOrOne(typeFactory.itemNumber());
        assertType("""
            () otherwise 1
        """, optionalNumber);
        assertType("""
            1 otherwise 2
        """, number);
        assertType("""
            "napis" otherwise 2
        """, typeFactory.anyItem());
        assertType("""
            (1, 2, 3) otherwise () otherwise (1, 2, 3)
        """, typeFactory.zeroOrMore(typeFactory.itemNumber()));
        assertType("""
            (1, 2, 3) otherwise (1, 2, 3) otherwise (1, 2, 3)
        """, typeFactory.oneOrMore(typeFactory.itemNumber()));
    }

    @Test
    public void itemGetting() {
        final var optionalString = typeFactory.zeroOrOne(typeFactory.itemString());
        final var zeroOrMoreString = typeFactory.zeroOrMore(typeFactory.itemString());
        assertType("""
            ("a", "b", "c")[()]
        """, typeFactory.emptySequence());
        assertType("""
            ("a", "b", "c")[1]
        """, optionalString);
        assertType("""
            ("a", "b", "c")[1, 2]
        """, zeroOrMoreString);
        assertType("""
            let $x as number? := 1
            return ("a", "b", "c")[$x]
        """, optionalString);
        assertType("""
            let $x as number* := (1, 2)
            return ("a", "b", "c")[$x]
        """, zeroOrMoreString);
        assertType("""
            let $x as number+ := (1, 2)
            return ("a", "b", "c")[$x]
        """, zeroOrMoreString);
    }


    @Test
    public void unionExpression() {
        assertType("""
            let $x as node()* := (),
                $y as node()* := (),
                $z as node()* := ()
            return $x | $y | $z
        """, typeFactory.zeroOrMore(typeFactory.itemAnyNode()));

        assertType("""
            let $x as element(a)* := (),
                $y as element(b)* := (),
                $z as element(c)* := ()
            return $x | $y | $z
        """, typeFactory.zeroOrMore(typeFactory.itemElement(Set.of("a", "b", "c"))));

        assertThereAreErrors("""
            let $x as number+ := (1, 2, 3)
            return $x | $x
        """);


    }

    @Test
    public void pathExpressions() {
        final var elementtest = typeFactory.itemElement(Set.of("test"));
        final var anyNode = typeFactory.anyNode();
        final var zeroOrOneNodes = typeFactory.zeroOrOne(typeFactory.itemAnyNode());
        final var anyNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        final var oneOrMoreNodes = typeFactory.oneOrMore(typeFactory.itemAnyNode());
        final var oneOrMoretests = typeFactory.oneOrMore(elementtest);
        final var tests = typeFactory.zeroOrMore(elementtest);
        final var zeroOrOnetest = typeFactory.zeroOrOne(elementtest);
        final var as = typeFactory.zeroOrMore(typeFactory.itemElement(Set.of("a")));
        assertType(".", anyNode);
        assertType("/test", tests);
        assertType("/test/a", as);

        assertType("//*", anyNodes);
        assertType("//test", anyNodes);
        assertType("//test//a", as);

        assertThereAreErrors("(1, 2, 3)/test");
        assertThereAreErrors("(1, 2, 3)//test");

        assertType("/child::*", anyNodes);
        assertType("/child::test", tests);
        assertType("/parent::*", zeroOrOneNodes);
        assertType("/parent::test", zeroOrOnetest);
        assertType("/descendant::*", anyNodes);
        assertType("/descendant::test", tests);
        assertType("/descendant-or-self::*", oneOrMoreNodes);
        assertType("/descendant-or-self::test", oneOrMoretests);
        assertType("/following::*", anyNodes);
        assertType("/following::test", tests);
        assertType("/following-or-self::*", oneOrMoreNodes);
        assertType("/following-or-self::test", oneOrMoretests);
        assertType("/following-sibling::*", anyNodes);
        assertType("/following-sibling::test", tests);
        assertType("/following-sibling-or-self::*", oneOrMoreNodes);
        assertType("/following-sibling-or-self::test", oneOrMoretests);
        assertType("/preceding::*", anyNodes);
        assertType("/preceding::test", tests);
        assertType("/preceding-or-self::*", oneOrMoreNodes);
        assertType("/preceding-or-self::test", oneOrMoretests);
        assertType("/preceding-sibling::*", anyNodes);
        assertType("/preceding-sibling::test", tests);
        assertType("/preceding-sibling-or-self::*", oneOrMoreNodes);
        assertType("/preceding-sibling-or-self::test", oneOrMoretests);
        assertType("/ancestor::*", anyNodes);
        assertType("/ancestor::test", tests);
        assertType("/ancestor-or-self::*", oneOrMoreNodes);
        assertType("/ancestor-or-self::test", oneOrMoretests);
        assertType("/self::*", anyNode);
        assertType("/self::test", zeroOrOneNodes);

    }


}



