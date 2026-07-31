package com.github.akruk.antlrquery;

import java.util.function.Function;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class HelperTrees {
    public final ParseTree CONTEXT_VALUE = getTree(".", AntlrQueryParser::contextValueRef);
    public final ParseTree DEFAULT_COLLATION = getTree("fn:default-collation()", AntlrQueryParser::functionCall);
    public final ParseTree EMPTY_SEQUENCE = getTree("()", AntlrQueryParser::parenthesizedExpr);
    public final ParseTree DEFAULT_ROUNDING_MODE = getTree("'half-to-ceiling'", AntlrQueryParser::literal);
    public final ParseTree ZERO_LITERAL = getTree("0", AntlrQueryParser::literal);
    public final ParseTree NFC = getTree("\"NFC\"", AntlrQueryParser::literal);
    public final ParseTree STRING_AT_CONTEXT_VALUE = getTree("fn:string(.)", AntlrQueryParser::functionCall);
    public final ParseTree EMPTY_STRING = getTree("\"\"", AntlrQueryParser::literal);
    public final ParseTree EMPTY_MAP = getTree("map {}", AntlrQueryParser::mapConstructor);
    public final ParseTree IDENTITY$1 = getTree("fn:identity#1", AntlrQueryParser::namedFunctionRef);
    public final ParseTree BOOLEAN$1 = getTree("fn:boolean#1", AntlrQueryParser::namedFunctionRef);
    public final ParseTree DATA$1 = getTree("fn:data#1", AntlrQueryParser::namedFunctionRef);
    public final ParseTree TRUE$0 = getTree("fn:true#0", AntlrQueryParser::namedFunctionRef);
    public final ParseTree FALSE$0 = getTree("fn:false#0", AntlrQueryParser::namedFunctionRef);
    public final ParseTree DEFAULT_COMPARATOR = getTree("fn:deep-equal#2", AntlrQueryParser::namedFunctionRef);
    public final ParseTree TEN = getTree("10", AntlrQueryParser::primaryExpr);


    private static ParseTree getTree(final String xquery, Function<AntlrQueryParser, ParseTree> initialRule) {
        final CodePointCharStream charStream = CharStreams.fromString(xquery);
        final AntlrQueryLexer lexer = new AntlrQueryLexer(charStream);
        final CommonTokenStream stream = new CommonTokenStream(lexer);
        final AntlrQueryParser parser = new AntlrQueryParser(stream);
        return initialRule.apply(parser);
    }
}
