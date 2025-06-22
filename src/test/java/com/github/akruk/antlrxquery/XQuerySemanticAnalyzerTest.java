package com.github.akruk.antlrxquery;

import static org.junit.Assert.assertTrue;

import java.util.Set;

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
        assertType("() || 'con' || ('cat', 'enate')", typeFactory.string());
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
    public void arithmeticExpressions() {
        assertType("1 + 1", typeFactory.number());
        assertType("1 - 1", typeFactory.number());
        assertType("1 * 1", typeFactory.number());
        assertType("1 x 1", typeFactory.number());
        assertType("1 ÷ 1", typeFactory.number());
        assertType("1 div 1", typeFactory.number());
        assertType("1 mod 1", typeFactory.number());
        assertType("1 idiv 1", typeFactory.number());
        assertThereAreErrors("() + 1");
        assertThereAreErrors("1 + ()");
        assertThereAreErrors("() * 1");
        assertThereAreErrors("1 * 'a'");
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
    public void nodeComparisons() {
        assertType("""
                    let $x as node()? := (),
                        $y as node()? := ()
                    return $x is $y
                """, typeFactory.zeroOrOne(typeFactory.itemBoolean()));

        assertThereAreErrors("""
                    let $x as number+ := (1, 2, 3)
                    return $x is $x
                """);
        assertThereAreErrors("""
                    let $x as string? := "abc"
                    return $x is $x
                """);
    }

    @Test
    public void valueComparisons() {
        final XQuerySequenceType optionalBool = typeFactory.zeroOrOne(typeFactory.itemBoolean());
        final XQuerySequenceType bool = typeFactory.boolean_();
        assertType("1 eq 1", bool);
        assertType("1 ne 1", bool);
        assertType("1 lt 1", bool);
        assertType("1 gt 1", bool);
        assertType("1 le 1", bool);
        assertType("1 ge 1", bool);
        assertType("'a' eq 'b'", bool);
        assertType("'a' ne 'b'", bool);
        assertType("'a' lt 'b'", bool);
        assertType("'a' gt 'b'", bool);
        assertType("'a' le 'b'", bool);
        assertType("'a' ge 'b'", bool);
        assertType("'a' eq ()", optionalBool);
        assertType("'a' ne ()", optionalBool);
        assertType("'a' lt ()", optionalBool);
        assertType("'a' gt ()", optionalBool);
        assertType("'a' le ()", optionalBool);
        assertType("'a' ge ()", optionalBool);
        assertType("() eq 'b'", optionalBool);
        assertType("() ne 'b'", optionalBool);
        assertType("() lt 'b'", optionalBool);
        assertType("() gt 'b'", optionalBool);
        assertType("() le 'b'", optionalBool);
        assertType("() ge 'b'", optionalBool);
        assertType("() eq ()", optionalBool);
        assertType("() ne ()", optionalBool);
        assertType("() lt ()", optionalBool);
        assertType("() gt ()", optionalBool);
        assertType("() le ()", optionalBool);
        assertType("() ge ()", optionalBool);

        assertThereAreErrors("'1' eq 1");
        assertThereAreErrors("'1' ne 1");
        assertThereAreErrors("'1' lt 1");
        assertThereAreErrors("'1' gt 1");
        assertThereAreErrors("'1' le 1");
        assertThereAreErrors("'1' ge 1");

        assertThereAreErrors("'1' eq true()");
        assertThereAreErrors("'1' ne true()");
        assertThereAreErrors("'1' lt true()");
        assertThereAreErrors("'1' gt true()");
        assertThereAreErrors("'1' le true()");
        assertThereAreErrors("'1' ge true()");

        assertThereAreErrors("""
                    let $x as number+ := (1, 2, 3)
                    return $x eq $x
                """);
    }

    @Test
    public void mappingExpressions() {
        assertType("(1, 2, 3) ! (. gt 5)", typeFactory.oneOrMore(typeFactory.itemBoolean()));
        assertType("() ! (. gt 5)", typeFactory.emptySequence());
        assertType("""
                let $x as number? := 5
                return $x ! .
                """, typeFactory.zeroOrOne(typeFactory.itemNumber()));
        assertType("""
                let $x as number* := ()
                return $x ! .
                """, typeFactory.zeroOrMore(typeFactory.itemNumber()));

    }


    @Test
    public void stringConstructor() {
        assertType("``[]``", typeFactory.string());
    }








}



