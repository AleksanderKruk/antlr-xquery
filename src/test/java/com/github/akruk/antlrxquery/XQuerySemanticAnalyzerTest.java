package com.github.akruk.antlrxquery;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.contextmanagement.semanticcontext.baseimplementation.XQueryBaseSemanticContextManager;
import com.github.akruk.antlrxquery.evaluator.functioncaller.XQueryFunctionCaller;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticFunctionCaller;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults.XQueryBaseSemanticFunctionCaller;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryEnumTypeFactory;
import com.github.akruk.antlrxquery.values.factories.defaults.XQueryMemoizedValueFactory;

public class XQuerySemanticAnalyzerTest {
    // final XQueryEnumBasedType string = XQueryEnumBasedType.string();
    // final XQueryEnumBasedType integer = XQueryEnumBasedType.integer();
    // final XQueryEnumBasedType number = XQueryEnumBasedType.number();
    // final XQueryEnumBasedType anyNode = XQueryEnumBasedType.anyNode();
    // final XQueryEnumBasedType emptySequence = XQueryEnumBasedType.emptySequence();
    // final XQueryEnumBasedType stringSequenceOneOrMore = XQueryEnumBasedType.sequence(XQueryEnumBasedType.string, XQueryOccurence.ONE_OR_MORE);
    // final XQueryEnumBasedType stringSequenceZeroOrMore = XQueryEnumBasedType.sequence(XQueryEnumBasedType.string, XQueryOccurence.ZERO_OR_MORE);
    // final XQueryEnumBasedType stringSequenceZeroOrOne = XQueryEnumBasedType.sequence(XQueryEnumBasedType.string,
    //         XQueryOccurence.ZERO_OR_ONE);


    void analyze(String text) {
        CharStream characters = CharStreams.fromString(text);
        Lexer xqueryLexer = new AntlrXqueryLexer(characters);
        CommonTokenStream xqueryTokens = new CommonTokenStream(xqueryLexer);
        AntlrXqueryParser xqueryParser = new AntlrXqueryParser(xqueryTokens);
        ParseTree xqueryTree = xqueryParser.xquery();
        XQuerySemanticFunctionCaller caller = new XQueryBaseSemanticFunctionCaller();
        XQuerySemanticAnalyzer analyzer = new XQuerySemanticAnalyzer(
            xqueryParser,
            new XQueryBaseSemanticContextManager(),
            new XQueryEnumTypeFactory(),
            new XQueryMemoizedValueFactory(),
            caller);
        analyzer.visit(xqueryTree);
    }

    // @Test
    // void emptySequence() {

    // }

}
