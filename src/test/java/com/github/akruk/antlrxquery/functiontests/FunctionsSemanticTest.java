package com.github.akruk.antlrxquery.functiontests;
import static org.junit.jupiter.api.Assertions.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.AntlrXqueryLexer;
import com.github.akruk.antlrxquery.AntlrXqueryParser;
import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.baseimplementation.XQueryBaseSemanticContextManager;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQuerySemanticFunctionManager;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryEnumTypeFactory;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryMemoizedValueFactory;

import java.util.List;

public class FunctionsSemanticTest {
    protected XQueryEnumTypeFactory typeFactory;
    protected XQuerySemanticFunctionManager fnManager;
    protected XQuerySemanticAnalyzer analyzer;

    public record AnalysisResult(XQuerySemanticAnalyzer analyzer, XQuerySequenceType type) {}

    @BeforeEach
    protected void init() {
        typeFactory = new XQueryEnumTypeFactory();
        fnManager   = new XQuerySemanticFunctionManager(typeFactory);
    }

    protected AnalysisResult analyze(String xquery) {
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

    protected void assertNoErrors(String query) {
        assertNoErrors(analyze(query));
    }

    protected void assertNoErrors(AnalysisResult r) {
        assertTrue(r.analyzer.getErrors().isEmpty(),
            "Expected no errors, got: " + r.analyzer.getErrors());
    }

    protected void assertErrors(String xq) {
        var r = analyze(xq);
        assertFalse(r.analyzer.getErrors().isEmpty(),
            "Expected errors for [" + xq + "]");
    }

    protected void assertType(String xq, XQuerySequenceType expected) {
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

    // --- math:atan($value as xs:double?) as xs:double? ------------------------

    @Test void atan_positional() {
        assertType("math:atan(1.0)",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }
    @Test void atan_named() {
        assertType("math:atan(value := 0.0)",
            typeFactory.zeroOrOne(typeFactory.itemNumber()));
    }
    @Test void atan_missing() {
        assertErrors("math:atan()");
    }
    @Test void atan_tooMany() {
        assertErrors("math:atan(1.0, 2.0)");
    }
    @Test void atan_wrongType() {
        assertErrors("math:atan('foo')");
    }

    // --- math:atan2($y as xs:double, $x as xs:double) as xs:double ------------

    @Test void atan2_positional() {
        assertType("math:atan2(1.0, 1.0)",
            typeFactory.one(typeFactory.itemNumber()));
    }
    @Test void atan2_named() {
        assertType("math:atan2(y := 1.0, x := 2.0)",
            typeFactory.one(typeFactory.itemNumber()));
    }
    @Test void atan2_mixed() {
        assertType("math:atan2(3.0, x := 4.0)",
            typeFactory.one(typeFactory.itemNumber()));
    }

    @Test void atan2_missingArg() {
        assertErrors("math:atan2(1.0)");
        assertErrors("math:atan2()");
    }
    @Test void atan2_wrongNames() {
        assertErrors("math:atan2(a := 1.0, b := 2.0)");
    }
    @Test void atan2_wrongTypes() {
        assertErrors("math:atan2(1.0, 'abc')");
        assertErrors("math:atan2('y', 2.0)");
    }

    // --- math:sinh, math:cosh, math:tanh (identyczna sygnatura) ---------------

    @Test void hyperbolic_functions() {
        for (String fn : List.of("sinh", "cosh", "tanh")) {
            assertType("math:"+fn+"(0.0)",
                typeFactory.zeroOrOne(typeFactory.itemNumber()));
            assertType("math:"+fn+"(value := 1.1)",
                typeFactory.zeroOrOne(typeFactory.itemNumber()));
            assertErrors("math:"+fn+"()");
            assertErrors("math:"+fn+"(1.0, 2.0)");
            assertErrors("math:"+fn+"('abc')");
            assertErrors("math:"+fn+"(val := 2.0)");
        }
    }


    @Test void characters_valid() {
        assertType("fn:characters('abc')", typeFactory.zeroOrMore(typeFactory.itemString()));
    }
    @Test void characters_invalidType() {
        assertErrors("fn:characters(123)");
    }
    @Test void characters_missing() {
        assertErrors("fn:characters()");
    }


    @Test void graphemes_valid() {
        assertType("fn:graphemes('ąść')", typeFactory.zeroOrMore(typeFactory.itemString()));
    }
    @Test void graphemes_wrongType() {
        assertErrors("fn:graphemes(true())");
    }
    @Test void graphemes_missing() {
        assertErrors("fn:graphemes()");
    }


    @Test void concat_emptyValid() {
        assertType("fn:concat()", typeFactory.one(typeFactory.itemString()));
    }
    @Test void concat_withValues() {
        assertType("fn:concat('a', (), 'b')", typeFactory.one(typeFactory.itemString()));
    }
    @Test void concat_wrongType() {
        assertErrors("fn:concat(1, 2, 3)"); // node() not allowed
    }


    @Test void stringJoin_positionalValid() {
        assertType("fn:string-join(('a', 'b'), '-')", typeFactory.one(typeFactory.itemString()));
    }
    @Test void stringJoin_namedSeparator() {
        assertType("fn:string-join(('x','y'), separator := ',')", typeFactory.one(typeFactory.itemString()));
    }
    @Test void stringJoin_defaultSeparator() {
        assertType("fn:string-join(('x','y'))", typeFactory.one(typeFactory.itemString()));
    }
    @Test void stringJoin_invalidSeparatorType() {
        assertErrors("fn:string-join(('x','y'), 5)");
    }


    @Test void substring_valid() {
        assertType("fn:substring('abc', 1)", typeFactory.one(typeFactory.itemString()));
        assertType("fn:substring('abc', 2, 2)", typeFactory.one(typeFactory.itemString()));
    }
    @Test void substring_wrongTypes() {
        assertErrors("fn:substring(123, 1)");
        assertErrors("fn:substring('abc', '1')");
        assertErrors("fn:substring('abc', 1, '2')");
    }


    @Test void stringLength_valid() {
        assertType("fn:string-length('hello')", typeFactory.one(typeFactory.itemNumber()));
    }
    @Test void stringLength_defaultContextInvalid() {
        assertErrors("fn:string-length()");
    }


    @Test void normalizeSpace_valid() {
        assertType("fn:normalize-space('  a  b  ')", typeFactory.one(typeFactory.itemString()));
    }
    @Test void normalizeSpace_missing() {
        assertErrors("fn:normalize-space()");
    }


    @Test void normalizeUnicode_valid() {
        assertType("fn:normalize-unicode('zażółć', 'NFC')", typeFactory.one(typeFactory.itemString()));
    }
    @Test void normalizeUnicode_defaultForm() {
        assertType("fn:normalize-unicode('zażółć')", typeFactory.one(typeFactory.itemString()));
    }
    @Test void normalizeUnicode_wrongFormType() {
        assertErrors("fn:normalize-unicode('x', 5)");
    }


    @Test void upperCase_valid() {
        assertType("fn:upper-case('abc')", typeFactory.one(typeFactory.itemString()));
    }
    @Test void upperCase_wrongType() {
        assertErrors("fn:upper-case(1.2)");
    }
    @Test void upperCase_missing() {
        assertErrors("fn:upper-case()");
    }


    @Test void lowerCase_valid() {
        assertType("fn:lower-case('ABC')", typeFactory.one(typeFactory.itemString()));
    }
    @Test void lowerCase_wrongType() {
        assertErrors("fn:lower-case(false())");
    }


    @Test void translate_valid() {
        assertType("fn:translate('abc','a','A')", typeFactory.one(typeFactory.itemString()));
    }
    @Test void translate_wrongTypes() {
        assertErrors("fn:translate('abc', 1, 'A')");
        assertErrors("fn:translate('abc', 'a', true())");
    }
    @Test void translate_missingArgs() {
        assertErrors("fn:translate()");
        assertErrors("fn:translate('x','a')");
    }


        // fn:abs($value as xs:numeric?) as xs:numeric?
    private void assertBoolean(String xq) {
        var r = analyze(xq);
        assertNoErrors(r);
        assertEquals(typeFactory.one(typeFactory.itemBoolean()), r.type);
    }

    private void assertString(String xq) {
        var r = analyze(xq);
        assertNoErrors(r);
        assertEquals(typeFactory.one(typeFactory.itemString()), r.type);
    }

    // fn:contains($value as xs:string?, $substring as xs:string?, $collation as xs:string? := default) as xs:boolean

    @Test void contains_validPositional() {
        assertBoolean("fn:contains('abc','b')");
    }
    @Test void contains_namedArgs() {
        assertBoolean("fn:contains(value := 'x', substring := 'y')");
    }
    @Test void contains_withCollationPos() {
        assertBoolean("fn:contains('a','b','uci')");
    }
    @Test void contains_defaultCollation() {
        assertBoolean("fn:contains('','')");
    }
    @Test void contains_tooFew() {
        assertErrors("fn:contains('a')");
        assertErrors("fn:contains()");
    }
    @Test void contains_tooMany() {
        assertErrors("fn:contains('a','b','c','d')");
    }
    @Test void contains_badTypes() {
        assertErrors("fn:contains(1,'b')");
        assertErrors("fn:contains('a',true())");
        assertErrors("fn:contains('a','b', 5)");
    }

    // fn:starts-with(... same signature ...) as xs:boolean

    @Test void startsWith_validPositional() {
        assertBoolean("fn:starts-with('hello','he')");
    }
    @Test void startsWith_namedAndDefault() {
        assertBoolean("fn:starts-with(substring := 'lo', value := 'hello')");
    }
    @Test void startsWith_missing() {
        assertErrors("fn:starts-with('x')");
        assertErrors("fn:starts-with()");
    }
    @Test void startsWith_badTypes() {
        assertErrors("fn:starts-with(1,'x')");
        assertErrors("fn:starts-with('x',2)");
        assertErrors("fn:starts-with('x','y',true())");
    }

    // fn:ends-with(... same signature ...) as xs:boolean

    @Test void endsWith_valid() {
        assertBoolean("fn:ends-with('test','st')");
    }
    @Test void endsWith_named() {
        assertBoolean("fn:ends-with(value := 'abc', substring := 'bc')");
    }
    @Test void endsWith_missingOrExtra() {
        assertErrors("fn:ends-with('a')");
        assertErrors("fn:ends-with()");
        assertErrors("fn:ends-with('a','b','c')");
    }
    @Test void endsWith_invalidTypes() {
        assertErrors("fn:ends-with(1, 'x')");
        assertErrors("fn:ends-with('x', 1)");
    }

    // fn:substring-before($value as xs:string?, $substring as xs:string?, $collation as xs:string? := default) as xs:string

    @Test void substringBefore_valid() {
        assertString("fn:substring-before('abcd','bc')");
    }
    @Test void substringBefore_named() {
        assertString("fn:substring-before(substring := 'z', value := 'xyz')");
    }
    @Test void substringBefore_withDefault() {
        assertString("fn:substring-before('aaa','a')");
    }
    @Test void substringBefore_argCounts() {
        assertErrors("fn:substring-before('x')");
        assertErrors("fn:substring-before()");
        assertErrors("fn:substring-before('x','y','z','w')");
    }
    @Test void substringBefore_badTypes() {
        assertErrors("fn:substring-before(1,'a')");
        assertErrors("fn:substring-before('a',true())");
        assertErrors("fn:substring-before('a','b', 0)");
    }

    // fn:substring-after(... same signature ...) as xs:string

    @Test void substringAfter_valid() {
        assertString("fn:substring-after('abcd','cd')");
    }
    @Test void substringAfter_named() {
        assertString("fn:substring-after(value := 'hello', substring := 'll')");
    }
    @Test void substringAfter_defaultCollation() {
        assertString("fn:substring-after('foo','o')");
    }
    @Test void substringAfter_errors() {
        assertErrors("fn:substring-after('x')");
        assertErrors("fn:substring-after()");
        assertErrors("fn:substring-after('x','y',1)");
        assertErrors("fn:substring-after('x', 1)");
    }

}
