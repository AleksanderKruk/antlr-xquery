package com.github.akruk.antlrxquery;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.baseimplementation.XQueryBaseSemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.IXQuerySemanticFunctionManager;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQuerySemanticFunctionManager;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryEnumTypeFactory;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryMemoizedValueFactory;

public class XQuerySemanticAnalyzerTest {
    final XQueryTypeFactory typeFactory = new XQueryEnumTypeFactory();

    record AnalysisResult(XQuerySemanticAnalyzer analyzer, XQuerySequenceType expressionType) {
    };

    AnalysisResult analyze(final String text) {
        final CharStream characters = CharStreams.fromString(text);
        final Lexer xqueryLexer = new AntlrXqueryLexer(characters);
        final CommonTokenStream xqueryTokens = new CommonTokenStream(xqueryLexer);
        final AntlrXqueryParser xqueryParser = new AntlrXqueryParser(xqueryTokens);
        final ParseTree xqueryTree = xqueryParser.xquery();
        final IXQuerySemanticFunctionManager caller = new XQuerySemanticFunctionManager(typeFactory);
        final XQuerySemanticAnalyzer analyzer = new XQuerySemanticAnalyzer(
                xqueryParser,
                new XQueryBaseSemanticContextManager(),
                new XQueryEnumTypeFactory(),
                new XQueryMemoizedValueFactory(),
                caller);
        final var lastVisitedType = analyzer.visit(xqueryTree);
        return new AnalysisResult(analyzer, lastVisitedType);
    }

    void assertNoErrors(final AnalysisResult analyzer) {
        assertTrue(analyzer.analyzer.getErrors().size() == 0);
    }

    void assertThereAreErrors(final String xquery) {
        final var analysisResult = analyze(xquery);
        assertThereAreErrors(analysisResult);
    }

    void assertThereAreErrors(final AnalysisResult analyzer) {
        assertTrue(analyzer.analyzer.getErrors().size() != 0);
    }

    void assertType(final AnalysisResult result, final XQuerySequenceType expectedType) {
        assertNoErrors(result);
        assertTrue(result.expressionType.equals(expectedType));
    }

    void assertType(final String xquery, final XQuerySequenceType expectedType) {
        final var analysisResult = analyze(xquery);
        assertNoErrors(analysisResult);
        assertTrue(analysisResult.expressionType.equals(expectedType));
    }

    @Test
    public void numericLiteralTypes() {
        final var number = typeFactory.number();

        // Integer literals
        assertType("123", number);
        assertType("1_000_000", number);

        // Hexadecimal literals
        assertType("0x1F", number);
        assertType("0xDE_AD_BE_EF", number);
        assertType("0x0", number);

        // Binary literals
        assertType("0b1010", number);
        assertType("0b0001_0001", number);

        // Decimal literals
        assertType(".75", number);
        assertType("42.", number);
        assertType("3.14", number);
        assertType("1_000.000_1", number);

        // Double literals
        assertType("1.23e3", number);
        assertType(".5e+2", number);
        assertType("4.56E-1", number);
        assertType("7e4", number);
        assertType("1_2.3_4e+1_0", number); // z podkreśleniami
    }

    @Test
    public void parenthesizedExpression() {
        assertType("()", typeFactory.emptySequence());
        assertType("(1)", typeFactory.number());
        assertType("(1, 'a')", typeFactory
                .oneOrMore(typeFactory.itemChoice(List.of(typeFactory.itemNumber(), typeFactory.itemString()))));
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

    @Test
    void nodeName_defaultContext() {
        var r = analyze("fn:node-name()");
        assertNoErrors(r);
        assertTrue(typeFactory.zeroOrOne(typeFactory.itemString()).equals(r.expressionType()));
    }

    @Test
    void nodeName_explicitNode() {
        assertNoErrors(analyze("fn:node-name(<a/>)"));
        assertTrue(typeFactory.zeroOrOne(typeFactory.itemString()).equals(analyze("fn:node-name(<a/>))").expressionType()));
    }

    @Test
    void nodeName_wrongType() {
        assertThereAreErrors("fn:node-name(1)");
    }

    @Test
    void nilled_default() {
        var r = analyze("fn:nilled()");
        assertNoErrors(r);
        assertTrue(typeFactory.zeroOrOne(typeFactory.itemBoolean()).equals(r.expressionType()));
    }

    @Test
    void nilled_onNode() {
        assertNoErrors(analyze("fn:nilled(<a nilled='true'/>)"));
    }

    @Test
    void nilled_bad() {
        assertThereAreErrors("fn:nilled('x')");
    }

    @Test
    void string_defaultContext() {
        var r = analyze("fn:string()");
        assertNoErrors(r);
        assertTrue(typeFactory.one(typeFactory.itemString()).equals(r.expressionType()));
    }

    @Test
    void string_fromNumber() {
        assertNoErrors(analyze("fn:string(123)"));
        assertTrue(typeFactory.one(typeFactory.itemString()).equals(analyze("fn:string(123))").expressionType()));
    }

    @Test
    void string_extraArg() {
        assertThereAreErrors("fn:string(1,2)");
    }

    @Test
    void data_default() {
        var r = analyze("fn:data()");
        assertNoErrors(r);
        assertTrue(typeFactory.zeroOrMore(typeFactory.itemAnyItem()).equals(r.expressionType()));
    }

    @Test
    void data_seq() {
        var r = analyze("fn:data((1,'x'))");
        assertNoErrors(r);
        assertTrue(typeFactory.zeroOrMore(typeFactory.itemAnyItem()).equals(r.expressionType()));
    }

    @Test
    void baseUri_default() {
        var r = analyze("fn:base-uri()");
        assertNoErrors(r);
        assertTrue(typeFactory.zeroOrOne(typeFactory.itemString()).equals(r.expressionType()));
    }

    @Test
    void baseUri_node() {
        assertNoErrors(analyze("fn:base-uri(<a xml:base='u'/>)"));
    }

    @Test
    void baseUri_wrong() {
        assertThereAreErrors("fn:base-uri(1)");
    }

    @Test
    void documentUri_default() {
        var r = analyze("fn:document-uri()");
        assertNoErrors(r);
        assertTrue(typeFactory.zeroOrOne(typeFactory.itemString()).equals(r.expressionType()));
    }

    @Test
    void documentUri_node() {
        assertNoErrors(analyze("fn:document-uri(<a/> )"));
    }

    @Test
    void documentUri_bad() {
        assertThereAreErrors("fn:document-uri('x')");
    }

    @Test
    void name_default() {
        var r = analyze("fn:name()");
        assertNoErrors(r);
        assertTrue(typeFactory.one(typeFactory.itemString()).equals(r.expressionType()));
    }

    @Test
    void name_node() {
        assertNoErrors(analyze("fn:name(<b/>)"));
    }

    @Test
    void name_invalid() {
        assertThereAreErrors("fn:name( true() )");
    }

    @Test
    void localName_default() {
        var r = analyze("fn:local-name()");
        assertNoErrors(r);
        assertTrue(typeFactory.one(typeFactory.itemString()).equals(r.expressionType()));
    }

    @Test
    void localName_node() {
        assertNoErrors(analyze("fn:local-name(<ns:a xmlns:ns='u'/>)"));
    }

    @Test
    void localName_bad() {
        assertThereAreErrors("fn:local-name(123)");
    }

    @Test
    void namespaceUri_default() {
        var r = analyze("fn:namespace-uri()");
        assertNoErrors(r);
        assertTrue(typeFactory.one(typeFactory.itemString()).equals(r.expressionType()));
    }

    @Test
    void namespaceUri_node() {
        assertNoErrors(analyze("fn:namespace-uri(<ns:a xmlns:ns='u'/>)"));
    }

    @Test
    void namespaceUri_bad() {
        assertThereAreErrors("fn:namespace-uri(1)");
    }

    @Test
    void lang_positional() {
        var r = analyze("fn:lang('en')");
        assertNoErrors(r);
        assertTrue(typeFactory.one(typeFactory.itemBoolean()).equals(r.expressionType()));
    }

    @Test
    void lang_namedNode() {
        var r = analyze("fn:lang(language := 'pl', node := <a xml:lang='pl'/>)");
        assertNoErrors(r);
    }

    @Test
    void lang_missingLang() {
        assertThereAreErrors("fn:lang()");
    }

    @Test
    void lang_badLangType() {
        assertThereAreErrors("fn:lang(1)");
    }

    @Test
    void root_default() {
        var r = analyze("fn:root()");
        assertNoErrors(r);
        assertTrue(typeFactory.zeroOrOne(typeFactory.itemAnyNode()).equals(r.expressionType()));
    }

    @Test
    void root_node() {
        assertNoErrors(analyze("fn:root(<a><b/></a>)"));
    }

    @Test
    void root_wrong() {
        assertThereAreErrors("fn:root('x')");
    }

    @Test
    void path_default() {
        var r = analyze("fn:path()");
        assertNoErrors(r);
        assertTrue(typeFactory.zeroOrOne(typeFactory.itemString()).equals(r.expressionType()));
    }

    @Test
    void path_withOptions() {
        var r = analyze("fn:path(., map{})");
        assertNoErrors(r);
    }

    @Test
    void path_bad() {
        assertThereAreErrors("fn:path(1,2)");
    }

    @Test
    void hasChildren_default() {
        var r = analyze("fn:has-children()");
        assertNoErrors(r);
        assertTrue(typeFactory.one(typeFactory.itemBoolean()).equals(r.expressionType()));
    }

    @Test
    void hasChildren_node() {
        assertNoErrors(analyze("fn:has-children(<a><b/></a>)"));
    }

    @Test
    void hasChildren_invalid() {
        assertThereAreErrors("fn:has-children(1)");
    }

    @Test
    void siblings_default() {
        var r = analyze("fn:siblings()");
        assertNoErrors(r);
        assertTrue(typeFactory.zeroOrMore(typeFactory.itemAnyNode()).equals(r.expressionType()));
    }

    @Test
    void siblings_node() {
        assertNoErrors(analyze("fn:siblings(<a/><b/>)"));
    }

    @Test
    void siblings_wrong() {
        assertThereAreErrors("fn:siblings('x')");
    }

    @Test
    void distinctOrderedNodes_noArgs() {
        var r = analyze("fn:distinct-ordered-nodes()");
        assertNoErrors(r);
        assertTrue(typeFactory.zeroOrMore(typeFactory.itemAnyNode()).equals(r.expressionType()));
    }

    @Test
    void distinctOrderedNodes_withNodes() {
        var r = analyze("fn:distinct-ordered-nodes(<a/>, <b/>)");
        assertNoErrors(r);
    }

    @Test
    void distinctOrderedNodes_wrongType() {
        assertThereAreErrors("fn:distinct-ordered-nodes(1, 'x')");
    }

    // --- fn:innermost(node()*) as node()* ---

    @Test
    void innermost_defaults() {
        var r = analyze("fn:innermost()");
        assertNoErrors(r);
        assertTrue(typeFactory.zeroOrMore(typeFactory.itemAnyNode()).equals(r.expressionType()));
    }

    @Test
    void innermost_withNodes() {
        assertNoErrors(analyze("fn:innermost(<a/><a><b/></a>)"));
    }

    @Test
    void innermost_bad() {
        assertThereAreErrors("fn:innermost( 'x' )");
    }

    // --- fn:outermost(node()*) as node()* ---

    @Test
    void outermost_defaults() {
        var r = analyze("fn:outermost()");
        assertNoErrors(r);
        assertTrue(typeFactory.zeroOrMore(typeFactory.itemAnyNode()).equals(r.expressionType()));
    }

    @Test
    void outermost_withNodes() {
        assertNoErrors(analyze("fn:outermost(<a/><a><b/></a>)"));
    }

    @Test
    void outermost_bad() {
        assertThereAreErrors("fn:outermost(0)");
    }

    // --- fn:error(QName? :=(), string? :=(), item()* :=.) as item()* ---

    @Test
    void error_default() {
        var r = analyze("fn:error()");
        assertNoErrors(r);
        assertTrue(typeFactory.zeroOrMore(typeFactory.itemAnyItem()).equals(r.expressionType()));
    }

    @Test
    void error_allNamed() {
        assertNoErrors(analyze(
                "fn:error(code := xs:QName('fn', 'err'), " +
                        "description := 'msg', value := 1)"));
    }

    @Test
    void error_badCode() {
        assertThereAreErrors("fn:error(code := 'notQName')");
    }

    @Test
    void error_badDesc() {
        assertThereAreErrors("fn:error(description := 5)");
    }

    @Test
    void error_tooMany() {
        assertThereAreErrors("fn:error(1,2,3,4)");
    }

    // --- fn:trace(item()*, string? :=()) as item()* ---

    @Test
    void trace_missingInput() {
        assertThereAreErrors("fn:trace()");
    }

    @Test
    void trace_onlyInput() {
        var r = analyze("fn:trace(1,)");
        assertNoErrors(r);
        assertTrue(typeFactory.zeroOrMore(typeFactory.itemAnyItem()).equals(r.expressionType()));
    }

    @Test
    void trace_withLabel() {
        assertNoErrors(analyze("fn:trace(<a/>, 'lbl')"));
    }

    @Test
    void trace_badLabel() {
        assertThereAreErrors("fn:trace(1, 2)");
    }

    // --- fn:message(item()*, string? :=()) as empty-sequence() ---

    @Test
    void message_onlyInput() {
        var r = analyze("fn:message(1)");
        assertNoErrors(r);
        assertTrue(typeFactory.emptySequence().equals(r.expressionType()));
    }

    @Test
    void message_withLabel() {
        var r = analyze("fn:message('x', 'lbl')");
        assertNoErrors(r);
        assertTrue(typeFactory.emptySequence().equals(r.expressionType()));
    }

    @Test
    void message_missing() {
        assertThereAreErrors("fn:message()");
    }

    @Test
    void message_badLabel() {
        assertThereAreErrors("fn:message(1, 2, 3)");
    }

    // numeric operators helper
    void assertNumericOp(String opName) {
        String call = "op:" + opName + "(2, 3)";
        var r = analyze(call);
        assertNoErrors(r);
        assertTrue(typeFactory.one(typeFactory.itemNumber()).equals(r.expressionType()));
    }

    void assertNumericOpError(String expr) {
        assertThereAreErrors(expr);
    }

    @Test
    void numericAdd_valid() {
        assertNumericOp("numeric-add");
    }

    @Test
    void numericAdd_wrong() {
        assertNumericOpError("op:numeric-add(1,'x')");
    }

    @Test
    void numericAdd_arity() {
        assertThereAreErrors("op:numeric-add(1)");
    }

    @Test
    void numericSub_valid() {
        assertNumericOp("numeric-subtract");
    }

    @Test
    void numericSub_wrong() {
        assertNumericOpError("op:numeric-subtract('a',2)");
    }

    @Test
    void numericSub_arity() {
        assertThereAreErrors("op:numeric-subtract()");
    }

    @Test
    void numericMul_valid() {
        assertNumericOp("numeric-multiply");
    }

    @Test
    void numericMul_wrong() {
        assertNumericOpError("op:numeric-multiply(1,true())");
    }

    @Test
    void numericDiv_valid() {
        assertNumericOp("numeric-divide");
    }

    @Test
    void numericDiv_wrong() {
        assertNumericOpError("op:numeric-divide(1,<x/>)");
    }

    @Test
    void intDivide_valid() {
        var r = analyze("op:numeric-integer-divide(5, 2)");
        assertNoErrors(r);
        assertTrue(typeFactory.one(typeFactory.itemNumber()).equals(r.expressionType()));
    }

    @Test
    void intDivide_wrong() {
        assertThereAreErrors("op:numeric-integer-divide(1,'y')");
    }

    @Test
    void mod_valid() {
        assertNumericOp("numeric-mod");
    }

    @Test
    void mod_wrong() {
        assertNumericOpError("op:numeric-mod('x',3)");
    }

    @Test
    void unaryPlus_valid() {
        var r = analyze("op:numeric-unary-plus(4)");
        assertNoErrors(r);
        assertTrue(typeFactory.one(typeFactory.itemNumber()).equals(r.expressionType()));
    }

    @Test
    void unaryMinus_valid() {
        var r = analyze("op:numeric-unary-minus(4)");
        assertNoErrors(r);
    }

    @Test
    void unary_wrongArity() {
        assertThereAreErrors("op:numeric-unary-plus()");
        assertThereAreErrors("op:numeric-unary-minus(1,2)");
    }

    @Test
    void unary_badType() {
        assertThereAreErrors("op:numeric-unary-plus('x')");
    }

    // op:numeric-equal($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
    @Test
    void numericEqual_valid() {
        assertType("op:op:numeric-equal(1, 2)", typeFactory.boolean_());
    }

    @Test
    void numericEqual_wrongType() {
        assertThereAreErrors("op:numeric-equal(1, 'x')");
    }

    @Test
    void numericEqual_arity() {
        assertThereAreErrors("op:numeric-equal(1)");
        assertThereAreErrors("op:numeric-equal()");
        assertThereAreErrors("op:numeric-equal(1,2,3)");
    }

    // op:numeric-less-than($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
    @Test
    void numericLessThan_valid() {
        assertType("op:op:numeric-less-than(1, 2)", typeFactory.boolean_());
    }

    @Test
    void numericLessThan_wrongType() {
        assertThereAreErrors("op:numeric-less-than('a', 2)");
    }

    @Test
    void numericLessThan_arity() {
        assertThereAreErrors("op:numeric-less-than(1)");
        assertThereAreErrors("op:numeric-less-than()");
    }

    // op:numeric-greater-than($arg1 as xs:numeric, $arg2 as xs:numeric) as
    // xs:boolean
    @Test
    void numericGreaterThan_valid() {
        assertType("op:op:numeric-greater-than(1, 2)", typeFactory.boolean_());
    }

    @Test
    void numericGreaterThan_wrongType() {
        assertThereAreErrors("op:numeric-greater-than(1, true())");
    }

    // op:numeric-less-than-or-equal($arg1 as xs:numeric, $arg2 as xs:numeric) as
    // xs:boolean
    @Test
    void numericLessThanOrEq_valid() {
        assertType("op:op:numeric-less-than-or-equal(1, 2)", typeFactory.boolean_());
    }

    // op:numeric-greater-than-or-equal($arg1 as xs:numeric, $arg2 as xs:numeric) as
    // xs:boolean
    @Test
    void numericGreaterThanOrEq_valid() {
        assertType("op:numeric-greater-than-or-equal(1, 2)", typeFactory.boolean_());
    }
}
