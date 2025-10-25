package com.github.akruk.antlrxquery;

import java.util.function.Function;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class HelperTrees {
    public final ParseTree CONTEXT_VALUE = getTree(".", AntlrXqueryParser::contextValueRef);
    public final ParseTree DEFAULT_COLLATION = getTree("fn:default-collation()", AntlrXqueryParser::functionCall);
    public final ParseTree EMPTY_SEQUENCE = getTree("()", AntlrXqueryParser::parenthesizedExpr);
    public final ParseTree DEFAULT_ROUNDING_MODE = getTree("'half-to-ceiling'", AntlrXqueryParser::literal);
    public final ParseTree ZERO_LITERAL = getTree("0", AntlrXqueryParser::literal);
    public final ParseTree NFC = getTree("\"NFC\"", AntlrXqueryParser::literal);
    public final ParseTree STRING_AT_CONTEXT_VALUE = getTree("fn:string(.)", AntlrXqueryParser::functionCall);
    public final ParseTree EMPTY_STRING = getTree("\"\"", AntlrXqueryParser::literal);
    public final ParseTree EMPTY_MAP = getTree("map {}", AntlrXqueryParser::mapConstructor);
    public final ParseTree IDENTITY$1 = getTree("fn:identity#1", AntlrXqueryParser::namedFunctionRef);
    public final ParseTree BOOLEAN$1 = getTree("fn:boolean#1", AntlrXqueryParser::namedFunctionRef);
    public final ParseTree DATA$1 = getTree("fn:data#1", AntlrXqueryParser::namedFunctionRef);
    public final ParseTree TRUE$0 = getTree("fn:true#0", AntlrXqueryParser::namedFunctionRef);
    public final ParseTree FALSE$0 = getTree("fn:false#0", AntlrXqueryParser::namedFunctionRef);
    public final ParseTree DEFAULT_COMPARATOR = getTree("fn:deep-equal#2", AntlrXqueryParser::namedFunctionRef);
    public final ParseTree TEN = getTree("10", AntlrXqueryParser::primaryExpr);


    private static ParseTree getTree(final String xquery, Function<AntlrXqueryParser, ParseTree> initialRule) {
        final CodePointCharStream charStream = CharStreams.fromString(xquery);
        final AntlrXqueryLexer lexer = new AntlrXqueryLexer(charStream);
        final CommonTokenStream stream = new CommonTokenStream(lexer);
        final AntlrXqueryParser parser = new AntlrXqueryParser(stream);
        return initialRule.apply(parser);
    }



}
