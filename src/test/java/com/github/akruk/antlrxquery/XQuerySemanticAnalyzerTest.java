package com.github.akruk.antlrxquery;

import org.junit.Test;

import com.github.akruk.antlrxquery.XQueryTest.TestParserAndTree;
import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticAnalyzer;
import com.github.akruk.antlrxquery.testgrammars.TestLexer;
import com.github.akruk.antlrxquery.testgrammars.TestParser;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryEnumBasedType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryOccurence;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

import static org.junit.Assert.assertEquals;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import static org.junit.Assert.*;

public class XQuerySemanticAnalyzerTest {
    final XQueryEnumBasedType string = XQueryEnumBasedType.string();
    final XQueryEnumBasedType integer = XQueryEnumBasedType.integer();
    final XQueryEnumBasedType number = XQueryEnumBasedType.number();
    final XQueryEnumBasedType anyNode = XQueryEnumBasedType.anyNode();
    final XQueryEnumBasedType emptySequence = XQueryEnumBasedType.emptySequence();
    final XQueryEnumBasedType stringSequenceOneOrMore = XQueryEnumBasedType.sequence(XQueryEnumBasedType.string, XQueryOccurence.ONE_OR_MORE);
    final XQueryEnumBasedType stringSequenceZeroOrMore = XQueryEnumBasedType.sequence(XQueryEnumBasedType.string, XQueryOccurence.ZERO_OR_MORE);
    final XQueryEnumBasedType stringSequenceZeroOrOne = XQueryEnumBasedType.sequence(XQueryEnumBasedType.string,
            XQueryOccurence.ZERO_OR_ONE);


    void analyze(String text) {
        CharStream characters = CharStreams.fromString(xquery);
        var xqueryLexer = new AntlrXqueryLexer(characters);
        var xqueryTokens = new CommonTokenStream(xqueryLexer);
        var xqueryParser = new AntlrXqueryParser(xqueryTokens);
        var xqueryTree = xqueryParser.xquery();
        XQuerySemanticAnalyzer visitor = new XQuerySemanticAnalyzer(null, );
    }

    @Test
    void emptySequence() {

    }

}
