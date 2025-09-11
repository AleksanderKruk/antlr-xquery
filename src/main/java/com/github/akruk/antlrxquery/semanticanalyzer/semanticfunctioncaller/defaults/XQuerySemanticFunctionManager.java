package com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.tree.ParseTree;
import com.github.akruk.antlrxquery.AntlrXqueryLexer;
import com.github.akruk.antlrxquery.AntlrXqueryParser;
import com.github.akruk.antlrxquery.AntlrXqueryParser.FunctionBodyContext;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ParenthesizedExprContext;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.semanticanalyzer.DiagnosticError;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticError;
import com.github.akruk.antlrxquery.semanticanalyzer.XQueryVisitingSemanticContext;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQuerySemanticFunctionManager {
    public static record AnalysisResult(XQuerySequenceType result,
                                        List<ArgumentSpecification> requiredDefaultArguments,
                                        List<DiagnosticError> errors)
                                            {}
    public static record ArgumentSpecification(String name, XQuerySequenceType type, ParseTree defaultArgument) {}
    public static record UsedArg(XQuerySequenceType type, XQueryValue value) {}
    public interface GrainedAnalysis {
        XQuerySequenceType analyze(List<UsedArg> args, XQueryVisitingSemanticContext context, ParseTree functionBody);

    }
    public static record FunctionSpecification(
            long minArity,
            long maxArity,
            List<ArgumentSpecification> args,
            XQuerySequenceType returnedType,
            XQuerySequenceType requiredContextValueType,
            boolean requiresPosition,
            boolean requiresSize,
            ParseTree body,
            GrainedAnalysis grainedAnalysis)
    { }


    private static final ParseTree CONTEXT_ITEM = getTree(".", parser -> parser.contextItemExpr());
    private static final ParseTree DEFAULT_COLLATION = getTree("fn:default-collation()", parser->parser.functionCall());
    public static final ParseTree EMPTY_SEQUENCE = getTree("()", p->p.parenthesizedExpr());
    private static final ParseTree DEFAULT_ROUNDING_MODE = getTree("'half-to-ceiling'", parser->parser.literal());
    private static final ParseTree ZERO_LITERAL = getTree("0", parser->parser.literal());
    private static final ParseTree NFC = XQuerySemanticFunctionManager.getTree("\"NFC\"", parser -> parser.literal());
    private static final ParseTree STRING_AT_CONTEXT_VALUE = XQuerySemanticFunctionManager.getTree("fn:string(.)", (parser) -> parser.functionCall());
    private static final ParseTree EMPTY_STRING = XQuerySemanticFunctionManager.getTree("\"\"", (parser)->parser.literal());
    private static final ParseTree EMPTY_MAP = getTree("map {}", parser -> parser.mapConstructor());
    private static final ParseTree IDENTITY$1 = XQuerySemanticFunctionManager.getTree("fn:identity#1", p->p.namedFunctionRef());
    private static final ParseTree BOOLEAN$1 = XQuerySemanticFunctionManager.getTree("fn:boolean#1", p->p.namedFunctionRef());
    private static final ParseTree DATA$1 = XQuerySemanticFunctionManager.getTree("fn:data#1", p->p.namedFunctionRef());
    private static final ParseTree TRUE$0 = XQuerySemanticFunctionManager.getTree("fn:true#0", p->p.namedFunctionRef());
    private static final ParseTree FALSE$0 = XQuerySemanticFunctionManager.getTree("fn:false#0", p->p.namedFunctionRef());

    public interface XQuerySemanticFunction {
        public AnalysisResult call(final XQueryTypeFactory typeFactory,
                final XQueryVisitingSemanticContext context,
                final List<XQuerySequenceType> types);
    }
    private final XQueryTypeFactory typeFactory;

    public XQuerySemanticFunctionManager(final XQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
        this.namespaces = new HashMap<>(6);

        final XQuerySequenceType optionalString = typeFactory.zeroOrOne(typeFactory.itemString());
        final XQuerySequenceType zeroOrMoreNumbers = typeFactory.zeroOrMore(typeFactory.itemNumber());
        final XQuerySequenceType optionalItem = typeFactory.zeroOrOne(typeFactory.itemAnyItem());
        final XQuerySequenceType zeroOrMoreNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());

        final XQuerySequenceType zeroOrMoreItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        final ArgumentSpecification argItems = new ArgumentSpecification("input", zeroOrMoreItems, null);

        final XQuerySequenceType optionalNumber = typeFactory.zeroOrOne(typeFactory.itemNumber());
        final ArgumentSpecification valueNum = new ArgumentSpecification("value", optionalNumber, null);
        final ArgumentSpecification roundingMode = new ArgumentSpecification("mode",
                typeFactory.zeroOrOne(typeFactory.itemEnum(Set.of("floor",
                                        "ceiling",
                                        "toward-zero",
                                        "away-from-zero",
                                        "half-to-floor",
                                        "half-to-ceiling",
                                        "half-toward-zero",
                                        "half-away-from-zero",
                                        "half-to-even"))),
                                        DEFAULT_ROUNDING_MODE);
        final ArgumentSpecification precision = new ArgumentSpecification("precision", optionalNumber, ZERO_LITERAL);

        final ArgumentSpecification optionalCollation = new ArgumentSpecification(
                "collation", optionalString, DEFAULT_COLLATION);

        // fn:abs(
        // 	as xs:numeric?
        // ) as xs:numeric?
        register("fn", "abs", List.of(valueNum), optionalNumber);

        // fn:ceiling(
        // 	as xs:numeric?
        // ) as xs:numeric?
        register("fn", "ceiling", List.of(valueNum), optionalNumber);


        // fn:floor(
        // 	as xs:numeric?
        // ) as xs:numeric?
        register("fn", "floor", List.of(valueNum), optionalNumber);


        // fn:round(
        // 	as xs:numeric?,
        // 	as xs:integer?	:= 0,
        // 	as enum('floor',
        //                     'ceiling',
        //                     'toward-zero',
        //                     'away-from-zero',
        //                     'half-to-floor',
        //                     'half-to-ceiling',
        //                     'half-toward-zero',
        //                     'half-away-from-zero',
        //                     'half-to-even')?	:= 'half-to-ceiling'
        // ) as xs:numeric?
        register("fn", "round",
                List.of(valueNum, precision, roundingMode), optionalNumber);


        // fn:round-half-to-even(
        // $value	as xs:numeric?,
        // $precision	as xs:integer?	:= 0
        // ) as xs:numeric?
        register("fn", "round-half-to-even",
                List.of(valueNum, precision), optionalNumber);


        // fn:divide-decimals(
        // $value	as xs:decimal,
        // $divisor	as xs:decimal,
        // $precision	as xs:integer?	:= 0
        // ) as record(quotient as xs:decimal, remainder as xs:decimal)
        final var arg_value_number = new ArgumentSpecification("value", typeFactory.number(), null);
        final var arg_divisor_number = new ArgumentSpecification("value", typeFactory.number(), null);
        final XQueryRecordField numericField = new XQueryRecordField(typeFactory.number(), true);
        final var divisionResult = typeFactory.record(
            Map.of("quotient", numericField,
                   "remainder", numericField));
        register("fn", "divide-decimals",
                List.of(arg_value_number, arg_divisor_number, precision), divisionResult);


        // fn:is-NaN(
        // $value	as xs:anyAtomicType
        // ) as xs:boolean
        register("fn", "is-NaN",
                List.of(new ArgumentSpecification("value", typeFactory.anyItem(), null)),
                typeFactory.boolean_());


        // fn:zero-or-one(
        //  as item()*
        // ) as item()?
        final ArgumentSpecification anyItemsRequiredInput = new ArgumentSpecification("input", zeroOrMoreItems, null);
        register("fn", "zero-or-one",
                List.of(anyItemsRequiredInput),
                optionalItem);

        // fn:one-or-more(
        //  as item()*
        // ) as item()+
        register("fn", "one-or-more",
                List.of(anyItemsRequiredInput),
                typeFactory.oneOrMore(typeFactory.itemAnyItem()));

        // fn:exactly-one(
        //  as item()*
        // ) as item()
        register("fn", "exactly-one",
                List.of(anyItemsRequiredInput),
                typeFactory.one(typeFactory.itemAnyItem()));

        // fn:node-name($node as node()? := .) as xs:QName?
        ArgumentSpecification nodeNameNode = new ArgumentSpecification(
            "node",
            typeFactory.zeroOrOne(typeFactory.itemAnyNode()),
            CONTEXT_ITEM
        );
        register(
            "fn", "node-name",
            List.of(nodeNameNode),
            typeFactory.zeroOrOne(typeFactory.itemString())
        );

        // fn:nilled($node as node()? := .) as xs:boolean?
        ArgumentSpecification nilledNode = new ArgumentSpecification(
            "node",
            typeFactory.zeroOrOne(typeFactory.itemAnyNode()),
            CONTEXT_ITEM
        );
        register(
            "fn", "nilled",
            List.of(nilledNode),
            typeFactory.zeroOrOne(typeFactory.itemBoolean())
        );

        // fn:string(
        //  as item()? := .
        // ) as xs:string
        final ArgumentSpecification stringValue = new ArgumentSpecification("value", optionalItem, CONTEXT_ITEM);
        register("fn", "string",
                List.of(stringValue),
                typeFactory.string());

        // fn:data(
        //  as item()* := .
        // ) as xs:anyAtomicType*
        final ArgumentSpecification dataInput = new ArgumentSpecification(
            "input", zeroOrMoreItems, CONTEXT_ITEM);
        register("fn", "data",
                List.of(dataInput), zeroOrMoreItems);



        final ArgumentSpecification nodeArg = new ArgumentSpecification(
            "node",
            typeFactory.zeroOrOne(typeFactory.itemAnyNode()),
            CONTEXT_ITEM
        );

        // fn:base-uri($node as node()? := .) as xs:anyURI?
        register(
            "fn", "base-uri",
            List.of(nodeArg),
            typeFactory.zeroOrOne(typeFactory.itemString())
        );

        // fn:document-uri($node as node()? := .) as xs:anyURI?
        register(
            "fn", "document-uri",
            List.of(nodeArg),
            typeFactory.zeroOrOne(typeFactory.itemString())
        );

        // fn:root($node as node()? := .) as node()?
        register(
            "fn", "root",
            List.of(nodeArg),
            typeFactory.zeroOrOne(typeFactory.itemAnyNode())
        );

        final ArgumentSpecification mapOptionsArg = new ArgumentSpecification(
            "options",
            typeFactory.zeroOrOne(typeFactory.itemAnyMap()),
            EMPTY_MAP
        );

        // fn:path($node as node()? := ., $options as map(*)? := {}) as xs:string?
        register(
            "fn", "path",
            List.of(nodeArg, mapOptionsArg),
            typeFactory.zeroOrOne(typeFactory.itemString())
        );

        // fn:has-children($node as node()? := .) as xs:boolean
        register(
            "fn", "has-children",
            List.of(nodeArg),
            typeFactory.boolean_()
        );

        // fn:siblings( $node as node()? := .) as node()*
        register(
            "fn", "siblings",
            List.of(nodeArg),
            zeroOrMoreNodes
        );

        // fn:distinct-ordered-nodes($nodes as node()*) as node()*
        final ArgumentSpecification nodesArg = new ArgumentSpecification(
            "nodes",
            zeroOrMoreNodes,
            null
        );
        register(
            "fn", "distinct-ordered-nodes",
            List.of(nodesArg),
            zeroOrMoreNodes
        );

        // fn:innermost($nodes as node()*) as node()*
        register(
            "fn", "innermost",
            List.of(nodesArg),
            zeroOrMoreNodes
        );

        // fn:outermost($nodes as node()*) as node()*
        register(
            "fn", "outermost",
            List.of(nodesArg),
            zeroOrMoreNodes
        );

        // fn:error($code as xs:QName? := (), $description as xs:string? := (), $value as item()* := .) as item()*
        ArgumentSpecification errorCode = new ArgumentSpecification(
            "code",
            typeFactory.zeroOrOne(typeFactory.itemString()),
            EMPTY_SEQUENCE
        );
        ArgumentSpecification errorDescription = new ArgumentSpecification(
            "description",
            typeFactory.zeroOrOne(typeFactory.itemString()),
            EMPTY_SEQUENCE
        );
        ArgumentSpecification errorValue = new ArgumentSpecification(
            "value",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            CONTEXT_ITEM
        );
        register(
            "fn", "error",
            List.of(errorCode, errorDescription, errorValue),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // fn:trace($input as item()*, $label as xs:string? := ()) as item()*
        ArgumentSpecification traceInput = new ArgumentSpecification(
            "input",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null
        );
        ArgumentSpecification traceLabel = new ArgumentSpecification(
            "label",
            typeFactory.zeroOrOne(typeFactory.itemString()),
            EMPTY_SEQUENCE
        );
        register(
            "fn", "trace",
            List.of(traceInput, traceLabel),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // fn:message($input as item()*, $label as xs:string? := ()) as empty-sequence()
        ArgumentSpecification messageInput = new ArgumentSpecification(
            "input",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null
        );
        ArgumentSpecification messageLabel = new ArgumentSpecification(
            "label",
            typeFactory.zeroOrOne(typeFactory.itemString()),
            EMPTY_SEQUENCE
        );
        register(
            "fn", "message",
            List.of(messageInput, messageLabel),
            typeFactory.emptySequence()
        );

        // op:numeric-add($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:numeric
        ArgumentSpecification numericArg1 = new ArgumentSpecification(
            "arg1",
            typeFactory.number(),
            null
        );
        ArgumentSpecification numericArg2 = new ArgumentSpecification(
            "arg2",
            typeFactory.number(),
            null
        );
        register(
            "op", "numeric-add",
            List.of(numericArg1, numericArg2),
            typeFactory.number()
        );

        // op:numeric-subtract($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:numeric
        register(
            "op", "numeric-subtract",
            List.of(numericArg1, numericArg2),
            typeFactory.number()
        );

        // op:numeric-multiply($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:numeric
        register(
            "op", "numeric-multiply",
            List.of(numericArg1, numericArg2),
            typeFactory.number()
        );

        // op:numeric-divide($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:numeric
        register(
            "op", "numeric-divide",
            List.of(numericArg1, numericArg2),
            typeFactory.number()
        );

        // op:numeric-integer-divide($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:integer
        register(
            "op", "numeric-integer-divide",
            List.of(numericArg1, numericArg2),
            typeFactory.number()
        );

        // op:numeric-mod($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:numeric
        register(
            "op", "numeric-mod",
            List.of(numericArg1, numericArg2),
            typeFactory.number()
        );

        // op:numeric-unary-plus($arg as xs:numeric) as xs:numeric
        ArgumentSpecification numericArg = new ArgumentSpecification(
            "arg",
            typeFactory.number(),
            null
        );
        register(
            "op", "numeric-unary-plus",
            List.of(numericArg),
            typeFactory.number()
        );

        // op:numeric-unary-minus($arg as xs:numeric) as xs:numeric
        register(
            "op", "numeric-unary-minus",
            List.of(numericArg),
            typeFactory.number()
        );

        // op:numeric-equal($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
        register("op", "numeric-equal",
            List.of(numericArg1, numericArg2),
            typeFactory.boolean_()
        );

        // op:numeric-less-than($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
        register("op", "numeric-less-than",
            List.of(numericArg1, numericArg2),
            typeFactory.boolean_()
        );

        // op:numeric-greater-than($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
        register("op", "numeric-greater-than",
            List.of(numericArg1, numericArg2),
            typeFactory.boolean_()
        );

        // op:numeric-less-than-or-equal($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
        register("op", "numeric-less-than-or-equal",
            List.of(numericArg1, numericArg2),
            typeFactory.boolean_()
        );

        // op:numeric-greater-than-or-equal($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
        register("op", "numeric-greater-than-or-equal",
            List.of(numericArg1, numericArg2),
            typeFactory.boolean_()
        );



        // fn:parse-integer($value as xs:string?, $radix as xs:integer? := 10) as xs:integer?
        ArgumentSpecification parseIntValue = new ArgumentSpecification(
            "value",
            typeFactory.zeroOrOne(typeFactory.itemString()),
            null
        );
        ArgumentSpecification parseIntRadix = new ArgumentSpecification(
            "radix",
            typeFactory.zeroOrOne(typeFactory.itemNumber()),
            getTree("10", AntlrXqueryParser::primaryExpr)
        );
        register(
            "fn", "parse-integer",
            List.of(parseIntValue, parseIntRadix),
            typeFactory.zeroOrOne(typeFactory.itemNumber())
        );

        // fn:format-integer($value as xs:integer?, $picture as xs:string, $language as xs:string? := ()) as xs:string
        ArgumentSpecification fmtIntValue = new ArgumentSpecification(
            "value",
            typeFactory.zeroOrOne(typeFactory.itemNumber()),
            null
        );
        ArgumentSpecification pictureString = new ArgumentSpecification(
            "picture",
            typeFactory.string(),
            null
        );
        ArgumentSpecification optionalLangugae = new ArgumentSpecification(
            "language",
            typeFactory.zeroOrOne(typeFactory.itemString()),
            EMPTY_SEQUENCE
        );
        register(
            "fn", "format-integer",
            List.of(fmtIntValue, pictureString, optionalLangugae),
            typeFactory.string()
        );

        // fn:format-number($value as xs:numeric?, $picture as xs:string, $options as (xs:string | map(*))? := ()) as xs:string
        ArgumentSpecification fmtNumValue = new ArgumentSpecification(
            "value",
            typeFactory.zeroOrOne(typeFactory.itemNumber()),
            null
        );
        ArgumentSpecification fmtNumOptions = new ArgumentSpecification(
            "options",
            typeFactory.zeroOrOne(typeFactory.itemChoice(Set.of(typeFactory.itemString(), typeFactory.itemAnyMap()))),
            EMPTY_SEQUENCE
        );
        register(
            "fn", "format-number",
            List.of(fmtNumValue, pictureString, fmtNumOptions),
            typeFactory.string()
        );


        // math:pi() as xs:double
        register("math", "pi",
                List.of(),
                typeFactory.number());

        // math:e() as xs:double
        register("math", "e",
                List.of(),
                typeFactory.number());


        // math:exp(  as xs:double?  ) as xs:double?
        final ArgumentSpecification expValue = new ArgumentSpecification("value", optionalNumber, null);
        register("math", "exp",
                List.of(expValue),
                optionalNumber);

        // math:exp10(  as xs:double?  ) as xs:double?
        final ArgumentSpecification exp10Value = new ArgumentSpecification("value", optionalNumber, null);
        register("math", "exp10",
                List.of(exp10Value),
                optionalNumber);

        // math:log(  as xs:double?  ) as xs:double?
        final ArgumentSpecification logValue = new ArgumentSpecification("value", optionalNumber, null);
        register("math", "log",
                List.of(logValue),
                optionalNumber);

        // math:log10(  as xs:double?  ) as xs:double?
        final ArgumentSpecification log10Value = new ArgumentSpecification("value", optionalNumber, null);
        register("math", "log10",
                List.of(log10Value),
                optionalNumber);

        // math:pow(
        //  as xs:double?,
        //  as xs:numeric
        // ) as xs:double?
        final ArgumentSpecification powX = new ArgumentSpecification("x", optionalNumber, null);
        final ArgumentSpecification powY = new ArgumentSpecification("y", typeFactory.number(), null);
        register("math", "pow",
                List.of(powX, powY),
                optionalNumber);

        // math:sqrt(
        //  as xs:double?
        // ) as xs:double?
        final ArgumentSpecification sqrtValue = new ArgumentSpecification("value", optionalNumber, null);
        register("math", "sqrt",
                List.of(sqrtValue),
                optionalNumber);

        // math:sin(
        //  as xs:double?
        // ) as xs:double?
        final ArgumentSpecification sinValue = new ArgumentSpecification("radians", optionalNumber, null);
        register("math", "sin",
                List.of(sinValue),
                optionalNumber);

        // math:cos(
        //  as xs:double?
        // ) as xs:double?
        final ArgumentSpecification cosValue = new ArgumentSpecification("radians", optionalNumber, null);
        register("math", "cos",
                List.of(cosValue),
                optionalNumber);

        // math:tan(
        //  as xs:double?
        // ) as xs:double?
        final ArgumentSpecification tanValue = new ArgumentSpecification("radians", optionalNumber, null);
        register("math", "tan",
                List.of(tanValue),
                optionalNumber);

        // math:asin(
        //  as xs:double?
        // ) as xs:double?
        final ArgumentSpecification asinValue = new ArgumentSpecification("value", optionalNumber, null);
        register("math", "asin",
                List.of(asinValue),
                optionalNumber);

        // math:acos(
        //  as xs:double?
        // ) as xs:double?
        final ArgumentSpecification acosValue = new ArgumentSpecification("value", optionalNumber, null);
        register("math", "acos",
                List.of(acosValue),
                optionalNumber);
        // math:atan(
        //  as xs:double?
        // ) as xs:double?
        final ArgumentSpecification atanVal = new ArgumentSpecification("value", optionalNumber, null);
        register("math", "atan",
                List.of(atanVal),
                optionalNumber);

        // math:atan2(
        //  as xs:double,
        //  as xs:double
        // ) as xs:double
        final ArgumentSpecification atan2Y = new ArgumentSpecification("y", typeFactory.number(), null);
        final ArgumentSpecification atan2X = new ArgumentSpecification("x", typeFactory.number(), null);
        register("math", "atan2",
                List.of(atan2Y, atan2X),
                typeFactory.number());

        // math:sinh(
        //  as xs:double?
        // ) as xs:double?
        final ArgumentSpecification sinhVal = new ArgumentSpecification("value", optionalNumber, null);
        register("math", "sinh",
                List.of(sinhVal),
                optionalNumber);

        // math:cosh(
        //  as xs:double?
        // ) as xs:double?
        final ArgumentSpecification coshVal = new ArgumentSpecification("value", optionalNumber, null);
        register("math", "cosh",
                List.of(coshVal),
                optionalNumber);

        // math:tanh(
        //  as xs:double?
        // ) as xs:double?
        final ArgumentSpecification tanhVal = new ArgumentSpecification("value", optionalNumber, null);
        register("math", "tanh",
                List.of(tanhVal),
                optionalNumber);

        // fn:codepoints-to-string(
        //  as xs:integer*
        // ) as xs:string
        final ArgumentSpecification cpsValues = new ArgumentSpecification("values", zeroOrMoreNumbers, null);
        register("fn", "codepoints-to-string",
                List.of(cpsValues),
                typeFactory.string());

        // fn:string-to-codepoints(
        //  as xs:string?
        // ) as xs:integer*
        final ArgumentSpecification stcpValue = new ArgumentSpecification("value", optionalString, null);
        register("fn", "string-to-codepoints",
                List.of(stcpValue),
                typeFactory.zeroOrMore(typeFactory.itemNumber()));

        // fn:codepoint-equal(
        //  as xs:string?,
        //  as xs:string?
        // ) as xs:boolean?
        final ArgumentSpecification cpEq1 = new ArgumentSpecification("value1", optionalString, null);
        final ArgumentSpecification cpEq2 = new ArgumentSpecification("value2", optionalString, null);
        register("fn", "codepoint-equal",
                List.of(cpEq1, cpEq2),
                typeFactory.zeroOrOne(typeFactory.itemBoolean()));

        // fn:collation(
        //  as map(*)
        // ) as xs:string
        final ArgumentSpecification collationOpts = new ArgumentSpecification("options", typeFactory.one(typeFactory.itemAnyMap()), null);
        register("fn", "collation",
                List.of(collationOpts),
                typeFactory.string());

        // fn:collation-available(
        //  as xs:string,
        //  as enum('compare','key','substring')* := ()
        // ) as xs:boolean
        final ArgumentSpecification colAvailColl = new ArgumentSpecification("collation", typeFactory.string(), null);
        final ArgumentSpecification colAvailUsage = new ArgumentSpecification("usage",
            typeFactory.zeroOrMore(typeFactory.itemEnum(Set.of("compare", "key", "substring"))),
            EMPTY_SEQUENCE);
        register("fn", "collation-available",
                List.of(colAvailColl, colAvailUsage),
                typeFactory.boolean_());

        // fn:contains-token(
        //  as xs:string*,
        //  as xs:string,
        //  as xs:string? := fn:default-collation()
        // ) as xs:boolean
        final ArgumentSpecification ctValue = new ArgumentSpecification("value", typeFactory.zeroOrMore(typeFactory.itemString()), null);
        final ArgumentSpecification ctToken = new ArgumentSpecification("token", typeFactory.string(), null);
        register("fn", "contains-token",
                List.of(ctValue, ctToken, optionalCollation),
                typeFactory.boolean_());

        // fn:char(
        //  as (xs:string | xs:positiveInteger)
        // ) as xs:string
        XQuerySequenceType stringOrNumber = typeFactory.choice(List.of(typeFactory.itemString(), typeFactory.itemNumber()));
        ArgumentSpecification charVal = new ArgumentSpecification("value", stringOrNumber, null);
        register("fn", "char", List.of(charVal), typeFactory.string());



        // fn:characters( as xs:string?) as xs:string*
        final ArgumentSpecification charactersValue = new ArgumentSpecification("value", optionalString, null);
        register("fn", "characters",
                List.of(charactersValue),
                typeFactory.zeroOrMore(typeFactory.itemString()));

        // fn:graphemes( as xs:string?) as xs:string*
        final ArgumentSpecification graphemesValue = new ArgumentSpecification("value", optionalString, null);
        register("fn", "graphemes",
                List.of(graphemesValue),
                typeFactory.zeroOrMore(typeFactory.itemString()));

        // fn:concat(
        //  as xs:anyAtomicType* := ()
        // ) as xs:string
        final ArgumentSpecification concatValues = new ArgumentSpecification("values", zeroOrMoreItems, EMPTY_SEQUENCE);
        register("fn", "concat",
                List.of(concatValues),
                typeFactory.string());

        // fn:string-join(
        //  as xs:anyAtomicType* := (),
        //  as xs:string? := ""
        // ) as xs:string
        final ArgumentSpecification joinValues = new ArgumentSpecification("values", zeroOrMoreItems, EMPTY_SEQUENCE);
        final ArgumentSpecification separator = new ArgumentSpecification("separator", optionalString, EMPTY_STRING);
        register("fn", "string-join",
                List.of(joinValues, separator),
                typeFactory.string());

        // fn:substring(
        //  as xs:string?,
        //  as xs:double,
        //  as xs:double? := ()
        // ) as xs:string
        final ArgumentSpecification substrValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification substrStart = new ArgumentSpecification("start", typeFactory.number(), null);
        final ArgumentSpecification substrLength = new ArgumentSpecification("length", optionalNumber, new ParenthesizedExprContext(null, 0));
        register("fn", "substring",
                List.of(substrValue, substrStart, substrLength),
                typeFactory.string());

        // fn:string-length(
        //  as xs:string? := fn:string(.)
        // ) as xs:integer
        final ArgumentSpecification lengthValue = new ArgumentSpecification("value", optionalString, STRING_AT_CONTEXT_VALUE);
        register("fn", "string-length",
                List.of(lengthValue),
                typeFactory.number() // or typeFactory.integer() if available
        );



        // fn:normalize-space( as xs:string? := fn:string(.)) as xs:string
        final ArgumentSpecification nsValue = new ArgumentSpecification("value", optionalString, STRING_AT_CONTEXT_VALUE);
        register("fn", "normalize-space",
                List.of(nsValue),
                typeFactory.string());

        // fn:normalize-unicode(
        //  as xs:string?,
        //  as xs:string? := "NFC"
        // ) as xs:string
        final ArgumentSpecification nuValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification nuForm = new ArgumentSpecification("form", optionalString, NFC);
        register("fn", "normalize-unicode",
                List.of(nuValue, nuForm),
                typeFactory.string());

        // fn:upper-case( as xs:string?) as xs:string
        final ArgumentSpecification ucValue = new ArgumentSpecification("value", optionalString, null);
        register("fn", "upper-case",
                List.of(ucValue),
                typeFactory.string());

        // fn:lower-case( as xs:string?) as xs:string
        final ArgumentSpecification lcValue = new ArgumentSpecification("value", optionalString, null);
        register("fn", "lower-case",
                List.of(lcValue),
                typeFactory.string());

        // fn:translate(
        //  as xs:string?,
        //  as xs:string,
        //  as xs:string
        // ) as xs:string
        final ArgumentSpecification trValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification trFrom = new ArgumentSpecification("replace", typeFactory.string(), null);
        final ArgumentSpecification trTo = new ArgumentSpecification("with", typeFactory.string(), null);
        register("fn", "translate",
                List.of(trValue, trFrom, trTo),
                typeFactory.string());

        // // fn:hash(
        // //   as (xs:string | xs:hexBinary | xs:base64Binary)?,
        // //   as xs:string? := "MD5",
        // //   as map(*)? := {}
        // // ) as xs:hexBinary?
        // ArgumentSpecification hashValue = new ArgumentSpecification("value", optionalString, null);
        // ArgumentSpecification hashAlg = new ArgumentSpecification("algorithm", optionalString, getTree("\"MD5\"", p->p.literal()));
        // ArgumentSpecification hashOpts = new ArgumentSpecification("options", typeFactory.zeroOrOne(typeFactory.itemAnyMap()), getTree("{}", t -> t));
        // register("fn", "hash",
        // List.of(hashValue, hashAlg, hashOpts),
        // typeFactory.zeroOrOne(typeFactory.itemHexBinary())
        // );

        // fn:contains(
        //  as xs:string?,
        //  as xs:string?,
        //  as xs:string? := fn:default-collation()
        // ) as xs:boolean
        final ArgumentSpecification cValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification cSubstr = new ArgumentSpecification("substring", optionalString, null);
        final ArgumentSpecification cColl = optionalCollation;
        register("fn", "contains", List.of(cValue, cSubstr, cColl), typeFactory.boolean_());

        // fn:starts-with(
        //  as xs:string?,
        //  as xs:string?,
        //  as xs:string? := fn:default-collation()
        // ) as xs:boolean
        final ArgumentSpecification swValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification swSubstring = new ArgumentSpecification("substring", optionalString, null);
        final ArgumentSpecification swCollation = optionalCollation;
        register("fn", "starts-with", List.of(swValue, swSubstring, swCollation), typeFactory.boolean_());

        // fn:ends-with(
        //  as xs:string?,
        //  as xs:string?,
        //  as xs:string? := fn:default-collation()
        // ) as xs:boolean
        final ArgumentSpecification ewValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification ewSubstring = new ArgumentSpecification("substring", optionalString, null);
        final ArgumentSpecification ewCollation = optionalCollation;
        register("fn", "ends-with", List.of(ewValue, ewSubstring, ewCollation), typeFactory.boolean_());

        // fn:substring-before(
        //  as xs:string?,
        //  as xs:string?,
        //  as xs:string? := fn:default-collation()
        // ) as xs:string
        final ArgumentSpecification sbValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification sbSubstring = new ArgumentSpecification("substring", optionalString, null);
        final ArgumentSpecification sbCollation = optionalCollation;
        register("fn", "substring-before", List.of(sbValue, sbSubstring, sbCollation), typeFactory.string());

        // fn:substring-after(
        //  as xs:string?,
        //  as xs:string?,
        //  as xs:string? := fn:default-collation()
        // ) as xs:string
        final ArgumentSpecification saValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification saSubstring = new ArgumentSpecification("substring", optionalString, null);
        final ArgumentSpecification saCollation = optionalCollation;
        register("fn", "substring-after",
                List.of(saValue, saSubstring, saCollation),
                typeFactory.string());

        // fn:matches(
        //  as xs:string?,
        //  as xs:string,
        //  as xs:string? := ""
        // ) as xs:boolean
        final ArgumentSpecification optionalStringRequiredValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification pattern = new ArgumentSpecification("pattern", typeFactory.string(), null);
        final ArgumentSpecification flags = new ArgumentSpecification("flags", optionalString, EMPTY_STRING);
        register("fn", "matches",
                List.of(optionalStringRequiredValue, pattern, flags),
                typeFactory.boolean_());


        // fn:replace(
        // 	as xs:string?,
        // 	as xs:string,
        // 	as (xs:string | fn(xs:untypedAtomic, xs:untypedAtomic*) as item()?)?	:= (),
        // 	as xs:string?	:= ''
        // ) as xs:string
        final XQueryItemType dynamicReplacement = typeFactory.itemFunction(optionalItem, List.of(typeFactory.anyItem(), zeroOrMoreItems));
        final var replacementType = typeFactory.choice(List.of(typeFactory.itemString(), dynamicReplacement));
        final ArgumentSpecification replacement = new ArgumentSpecification("replacement", replacementType, EMPTY_SEQUENCE);
        register("fn", "replace",
                List.of(optionalStringRequiredValue, pattern, replacement, flags),
                typeFactory.string());

        // fn:tokenize(
        //  as xs:string?,
        //  as xs:string? := (),
        //  as xs:string? := ""
        // ) as xs:string*
        final ArgumentSpecification optionalPattern = new ArgumentSpecification("pattern", optionalString, EMPTY_SEQUENCE);
        register("fn", "tokenize",
                List.of(optionalStringRequiredValue, optionalPattern, flags),
                typeFactory.zeroOrMore(typeFactory.itemString()));

        // fn:analyze-string(
        //  as xs:string?,
        //  as xs:string,
        //  as xs:string? := ""
        // ) as element(fn:analyze-string-result)
        register("fn", "analyze-string",
                List.of(optionalStringRequiredValue, pattern, flags),
                typeFactory.one(typeFactory.itemElement(Set.of("fn:analyze-string-result"))));

        // fn:true() as xs:boolean
        register("fn", "true", List.of(), typeFactory.boolean_());

        // fn:false() as xs:boolean
        register("fn", "false", List.of(), typeFactory.boolean_());

        // op:boolean-equal( as xs:boolean,  as xs:boolean) as xs:boolean
        final ArgumentSpecification bool1ValueRequired = new ArgumentSpecification("value1", typeFactory.boolean_(), null);
        final ArgumentSpecification bool2ValueRequired = new ArgumentSpecification("value2", typeFactory.boolean_(), null);
        register("op", "boolean-equal",
                List.of(bool1ValueRequired, bool2ValueRequired),
                typeFactory.boolean_());

        // op:boolean-less-than( as xs:boolean,  as xs:boolean) as xs:boolean
        register("op", "boolean-less-than",
                List.of(bool1ValueRequired, bool2ValueRequired),
                typeFactory.boolean_());
        // op:boolean-greater-than( as xs:boolean,  as xs:boolean) as xs:boolean
        register("op", "boolean-greater-than",
                List.of(bool1ValueRequired, bool2ValueRequired),
                typeFactory.boolean_());

        // op:boolean-not-equal( as xs:boolean,  as xs:boolean) as xs:boolean
        register("op", "boolean-not-equal",
                List.of(bool1ValueRequired, bool2ValueRequired),
                typeFactory.boolean_());

        // op:boolean-less-than-or-equal( as xs:boolean,  as xs:boolean) as xs:boolean
        register("op", "boolean-less-than-or-equal",
                List.of(bool1ValueRequired, bool2ValueRequired),
                typeFactory.boolean_());

        // op:boolean-greater-than-or-equal( as xs:boolean,  as xs:boolean) as xs:boolean
        register("op", "boolean-greater-than-or-equal",
                List.of(bool1ValueRequired, bool2ValueRequired),
                typeFactory.boolean_());


        // fn:boolean( as item()*) as xs:boolean
        register("fn", "boolean",
                List.of(anyItemsRequiredInput),
                typeFactory.boolean_());

        // // fn:not( as item()*) as xs:boolean
        register("fn", "not", List.of(argItems), typeFactory.boolean_());

        // fn:empty( as item()*) as xs:boolean
        register("fn", "empty", List.of(anyItemsRequiredInput), typeFactory.boolean_());

        // fn:exists( as item()*) as xs:boolean
        register("fn", "exists", List.of(anyItemsRequiredInput), typeFactory.boolean_());

        // fn:foot( as item()*) as item()?
        register("fn", "foot", List.of(anyItemsRequiredInput), optionalItem);

        // fn:head( as item()*) as item()?
        register("fn", "head", List.of(anyItemsRequiredInput), optionalItem);

        // fn:identity( as item()*) as item()*
        register("fn", "identity", List.of(anyItemsRequiredInput), zeroOrMoreItems);

        // fn:insert-before(
        // 	as item()*,
        // 	as xs:integer,
        // 	as item()*
        // ) as item()*
        final ArgumentSpecification position = new ArgumentSpecification("position", typeFactory.number(), null);
        final ArgumentSpecification insert = new ArgumentSpecification("insert", zeroOrMoreItems, null);
        register("fn", "insert-before",
                List.of(anyItemsRequiredInput, position, insert),
                zeroOrMoreItems);


        // fn:items-at( as item()*,  as xs:integer*) as item()*
        final ArgumentSpecification at = new ArgumentSpecification("at",
                zeroOrMoreNumbers, null);
        register("fn", "items-at",
                List.of(anyItemsRequiredInput, at),
                zeroOrMoreItems);

        // fn:replicate( as item()*,  as xs:nonNegativeInteger) as item()*
        final ArgumentSpecification count = new ArgumentSpecification("count", typeFactory.number(), null);
        register("fn", "replicate",
                List.of(anyItemsRequiredInput, count),
                zeroOrMoreItems);


        final ArgumentSpecification positions = new ArgumentSpecification("positions", zeroOrMoreNumbers, null);


        // fn:remove(
        // 	as item()*,
        // 	as xs:integer*
        // ) as item()*
        register("fn", "remove",
                List.of(anyItemsRequiredInput, positions), zeroOrMoreItems);


        // fn:reverse(
        // 	as item()*
        // ) as item()*
        register("fn", "reverse",
                List.of(anyItemsRequiredInput),
                zeroOrMoreItems);

        // fn:sequence-join( as item()*,  as item()*) as item()*
        final ArgumentSpecification seqJoinSeparator = new ArgumentSpecification("separator", zeroOrMoreItems, null);
        register("fn", "sequence-join",
                List.of(anyItemsRequiredInput, seqJoinSeparator),
                zeroOrMoreItems);

        // fn:slice(
        //  as item()*,
        //  as xs:integer? := (),
        //  as xs:integer?  := (),
        //  as xs:integer? := ()
        // ) as item()*
        final ArgumentSpecification sliceStart = new ArgumentSpecification("start",
                optionalNumber, EMPTY_SEQUENCE);
        final ArgumentSpecification sliceEnd = new ArgumentSpecification("end",
                optionalNumber, EMPTY_SEQUENCE);
        final ArgumentSpecification sliceStep = new ArgumentSpecification("step",
                optionalNumber, EMPTY_SEQUENCE);
        register("fn", "slice",
                List.of(anyItemsRequiredInput, sliceStart, sliceEnd, sliceStep),
                zeroOrMoreItems);

        // fn:subsequence( as item()*,  as xs:double,  as xs:double?
        // := ()) as item()*
        final ArgumentSpecification subStart = new ArgumentSpecification("start",
                typeFactory.number(), null);
        final ArgumentSpecification subLength = new ArgumentSpecification("length",
                optionalNumber, EMPTY_SEQUENCE);
        register("fn", "subsequence",
                List.of(anyItemsRequiredInput, subStart, subLength),
                zeroOrMoreItems);

        // fn:tail( as item()*) as item()*
        register("fn", "tail",
                List.of(anyItemsRequiredInput),
                zeroOrMoreItems);

        // fn:trunk( as item()*) as item()*
        register("fn", "trunk",
                List.of(anyItemsRequiredInput),
                zeroOrMoreItems);

        // fn:unordered( as item()*) as item()*
        register("fn", "unordered",
                List.of(anyItemsRequiredInput),
                zeroOrMoreItems);

        // fn:void( as item()* := ()) as empty-sequence()
        final ArgumentSpecification voidInput = new ArgumentSpecification("input", zeroOrMoreItems, EMPTY_SEQUENCE);
        register("fn", "void",
                List.of(voidInput),
                typeFactory.emptySequence());

        // fn:atomic-equal( as xs:anyAtomicType,  as xs:anyAtomicType) as
        // xs:boolean
        final ArgumentSpecification arg_value1_anyItem = new ArgumentSpecification("value1", typeFactory.anyItem(), null);
        final ArgumentSpecification arg_value2_anyItem  = new ArgumentSpecification("value2", typeFactory.anyItem(), null);
        register("fn", "atomic-equal",
                List.of(arg_value1_anyItem, arg_value2_anyItem),
                typeFactory.boolean_());

        // fn:deep-equal( as item()*,  as item()*,  as
        // (xs:string|map(*))? := {}) as xs:boolean
        final ArgumentSpecification arg_value1_anyItems = new ArgumentSpecification("value1", zeroOrMoreItems, null);
        final ArgumentSpecification arg_value2_anyItems  = new ArgumentSpecification("value2", zeroOrMoreItems, null);
        final var stringOrMap = typeFactory.zeroOrOne(typeFactory.itemChoice(Set.of(typeFactory.itemString(), typeFactory.itemAnyMap())));
        final ArgumentSpecification optionalOptions = new ArgumentSpecification("options", stringOrMap, EMPTY_MAP);
        register("fn", "deep-equal",
                List.of(arg_value1_anyItems, arg_value2_anyItems, optionalOptions),
                typeFactory.boolean_());

        // fn:compare( as xs:anyAtomicType?,  as xs:anyAtomicType?,
        //  as xs:string? := fn:default-collation()) as xs:integer?
        register("fn", "compare",
                List.of(arg_value1_anyItem, arg_value2_anyItem, optionalCollation),
                typeFactory.zeroOrOne(typeFactory.itemNumber()));

        // fn:distinct-values(
        //  as xs:anyAtomicType*,
        //  as xs:string? := fn:default-collation()
        // ) as xs:anyAtomicType*
        final ArgumentSpecification required_arg_values_anyItems = new ArgumentSpecification("values", zeroOrMoreItems, null);
        register("fn", "distinct-values",
                List.of(required_arg_values_anyItems, optionalCollation),
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // fn:duplicate-values(
        //  as xs:anyAtomicType*,
        //  as xs:string? := fn:default-collation()
        // ) as xs:anyAtomicType*
        register("fn", "duplicate-values",
                List.of(required_arg_values_anyItems, optionalCollation),
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // fn:index-of(
        //  as xs:anyAtomicType*,
        //  as xs:anyAtomicType,
        //  as xs:string? := fn:default-collation()
        // ) as xs:integer*
        final ArgumentSpecification required_arg_target_anyItem = new ArgumentSpecification("target", typeFactory.one(typeFactory.itemAnyItem()), null);
        register("fn", "index-of",
                List.of(anyItemsRequiredInput, required_arg_target_anyItem, optionalCollation),
                typeFactory.zeroOrMore(typeFactory.itemNumber()));

        // fn:starts-with-subsequence(
        //  as item()*,
        //  as item()*,
        //  as (fn(item(),item()) as xs:boolean?)? := fn:deep-equal#2
        // ) as xs:boolean
        final ArgumentSpecification required_arg_subsequence_anyItems = new ArgumentSpecification("subsequence", zeroOrMoreItems, null);
        final var comparator = typeFactory.zeroOrOne(typeFactory.itemFunction(typeFactory.boolean_(),
                List.of(typeFactory.anyItem(), typeFactory.anyItem())));
        final var DEFAULT_COMPARATOR = getTree("fn:deep-equal#2", parser -> parser.namedFunctionRef());
        final ArgumentSpecification optional_arg_compare_comparator = new ArgumentSpecification("compare", comparator, DEFAULT_COMPARATOR);
        register("fn", "starts-with-subsequence",
                List.of(anyItemsRequiredInput, required_arg_subsequence_anyItems, optional_arg_compare_comparator),
                typeFactory.boolean_());

        // fn:ends-with-subsequence(
        //  as item()*,
        //  as item()*,
        //  as (fn(item(),item()) as xs:boolean?)? := fn:deep-equal#2
        // ) as xs:boolean
        register("fn", "ends-with-subsequence",
                List.of(anyItemsRequiredInput, required_arg_subsequence_anyItems, optional_arg_compare_comparator),
                typeFactory.boolean_());

        // fn:contains-subsequence(
        //  as item()*,
        //  as item()*,
        //  as (fn(item(),item()) as xs:boolean?)? := fn:deep-equal#2
        // ) as xs:boolean
        register("fn", "contains-subsequence",
                List.of(anyItemsRequiredInput, required_arg_subsequence_anyItems, optional_arg_compare_comparator),
                typeFactory.boolean_());

        // fn:count( as item()*) as xs:integer
        register("fn", "count", List.of(anyItemsRequiredInput), typeFactory.number());

        // fn:avg( as xs:anyAtomicType*) as xs:anyAtomicType?
        final ArgumentSpecification anyItemValues = new ArgumentSpecification("values", zeroOrMoreItems, null);
        register("fn", "avg", List.of(anyItemValues), optionalItem);

        // fn:max(
        //  as xs:anyAtomicType*,
        //  as xs:string? := fn:default-collation()
        // ) as xs:anyAtomicType?
        register("fn", "max",
                List.of(anyItemValues, optionalCollation),
                typeFactory.zeroOrOne(typeFactory.itemAnyItem()));

        // fn:min(
        //  as xs:anyAtomicType*,
        //  as xs:string? := fn:default-collation()
        // ) as xs:anyAtomicType?
        register("fn", "min", List.of(anyItemValues, optionalCollation), optionalItem);

        // fn:sum(
        //  as xs:anyAtomicType*,
        //  as xs:anyAtomicType? := 0
        // ) as xs:anyAtomicType?
        final ArgumentSpecification sumZero = new ArgumentSpecification("zero", optionalItem, ZERO_LITERAL);
        register("fn", "sum", List.of(anyItemValues, sumZero), optionalItem);

        // fn:all-equal(
        //  as xs:anyAtomicType*,
        //  as xs:string? := fn:default-collation()
        // ) as xs:boolean
        register("fn", "all-equal",
                List.of(anyItemValues, optionalCollation),
                typeFactory.boolean_());

        // fn:all-different(
        //  as xs:anyAtomicType*,
        //  as xs:string? := fn:default-collation()
        // ) as xs:boolean
        register("fn", "all-different",
                List.of(anyItemValues, optionalCollation),
                typeFactory.boolean_());

        // fn:doc($source as xs:string?, $options as map(*)? := {}) as document-node()?
        final ArgumentSpecification sourceArgNonDefault = new ArgumentSpecification(
            "source",
            typeFactory.zeroOrOne(typeFactory.itemString()),
            null
        );
        final ArgumentSpecification docOptions = new ArgumentSpecification(
            "options",
            typeFactory.zeroOrOne(typeFactory.itemAnyMap()),
            EMPTY_MAP
        );
        register(
            "fn", "doc",
            List.of(sourceArgNonDefault, docOptions),
            typeFactory.zeroOrOne(typeFactory.itemAnyNode())
        );

        // fn:doc-available($source as xs:string?, $options as map(*)? := {}) as xs:boolean
        ArgumentSpecification docAvailOptions = new ArgumentSpecification(
            "options",
            typeFactory.zeroOrOne(typeFactory.itemAnyMap()),
            EMPTY_MAP
        );
        register(
            "fn", "doc-available",
            List.of(sourceArgNonDefault, docAvailOptions),
            typeFactory.boolean_()
        );


        // fn:collection($source as xs:string? := ()) as item()*
        ArgumentSpecification colSource = new ArgumentSpecification(
            "source",
            typeFactory.zeroOrOne(typeFactory.itemString()),
            EMPTY_SEQUENCE
        );
        register(
            "fn", "collection",
            List.of(colSource),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // fn:unparsed-text($source as xs:string?, $options as (xs:string|map(*))? := ()) as xs:string?
        ArgumentSpecification utOptions = new ArgumentSpecification(
            "options",
            typeFactory.zeroOrOne(typeFactory.itemChoice(Set.of(typeFactory.itemString(),
                                                                typeFactory.itemAnyMap()))),
            EMPTY_SEQUENCE
        );
        register(
            "fn", "unparsed-text",
            List.of(sourceArgNonDefault, utOptions),
            typeFactory.zeroOrOne(typeFactory.itemString())
        );

        // fn:unparsed-text-lines($source as xs:string?, $options as (xs:string|map(*))? := ()) as xs:string*
        ArgumentSpecification utlOptions = new ArgumentSpecification(
            "options",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()),
            EMPTY_SEQUENCE
        );
        register(
            "fn", "unparsed-text-lines",
            List.of(sourceArgNonDefault, utlOptions),
            typeFactory.zeroOrMore(typeFactory.itemString())
        );

        // fn:unparsed-text-available($source as xs:string?, $options as (xs:string|map(*))? := ()) as xs:boolean
        ArgumentSpecification utaOptions = new ArgumentSpecification(
            "options",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()),
            EMPTY_SEQUENCE
        );
        register(
            "fn", "unparsed-text-available",
            List.of(sourceArgNonDefault, utaOptions),
            typeFactory.boolean_()
        );

        // fn:environment-variable($name as xs:string) as xs:string?
        ArgumentSpecification envName = new ArgumentSpecification(
            "name",
            typeFactory.string(),
            null
        );
        register(
            "fn", "environment-variable",
            List.of(envName),
            typeFactory.zeroOrOne(typeFactory.itemString())
        );

        // fn:available-environment-variables() as xs:string*
        register(
            "fn", "available-environment-variables",
            List.of(),
            typeFactory.zeroOrMore(typeFactory.itemString())
        );





        // fn:position() as xs:integer
        register("fn", "position",
            List.of(), typeFactory.number(),
            null,
            true,
            false, null, null);

        // fn:last() as xs:integer
        register("fn", "last", List.of(), typeFactory.number(), null, false, true, null, null);

        // fn:current-dateTime() as xs:dateTimeStamp
        register(
            "fn", "current-dateTime",
            List.of(),
            typeFactory.string()
        );

        // fn:current-date() as xs:date
        register(
            "fn", "current-date",
            List.of(),
            typeFactory.string()
        );

        // fn:current-time() as xs:time
        register(
            "fn", "current-time",
            List.of(),
            typeFactory.string()
        );

        // fn:implicit-timezone() as xs:dayTimeDuration
        register(
            "fn", "implicit-timezone",
            List.of(),
            typeFactory.string()
        );

        // fn:default-collation() as xs:string
        register(
            "fn", "default-collation",
            List.of(),
            typeFactory.string()
        );

        // fn:default-language() as xs:language
        register(
            "fn", "default-language",
            List.of(),
            typeFactory.string()
        );




        // fn:function-lookup($name as xs:QName, $arity as xs:integer) as function(*)?
        register(
            "fn", "function-lookup",
            List.of(
                new ArgumentSpecification("name", typeFactory.one(typeFactory.itemString()), null),
                new ArgumentSpecification("arity", typeFactory.one(typeFactory.itemNumber()), null)
            ),
            typeFactory.zeroOrOne(typeFactory.itemAnyFunction())
        );

        // fn:function-name($function as function(*)) as xs:QName?
        register(
            "fn", "function-name",
            List.of(
                new ArgumentSpecification("function", typeFactory.one(typeFactory.itemAnyFunction()), null)
            ),
            typeFactory.zeroOrOne(typeFactory.itemString())
        );

        // fn:function-arity($function as function(*)) as xs:integer
        register(
            "fn", "function-arity",
            List.of(
                new ArgumentSpecification("function", typeFactory.one(typeFactory.itemAnyFunction()), null)
            ),
            typeFactory.one(typeFactory.itemNumber())
        );

        // fn:function-identity($function as function(*)) as xs:string
        register(
            "fn", "function-identity",
            List.of(
                new ArgumentSpecification("function", typeFactory.one(typeFactory.itemAnyFunction()), null)
            ),
            typeFactory.string()
        );

        // fn:function-annotations( $function as fn(*) ) as map(xs:QName, xs:anyAtomicType*)*
        register(
            "fn", "function-annotations",
            List.of(
                new ArgumentSpecification(
                    "function",
                    typeFactory.one(typeFactory.itemAnyFunction()),
                    null
                )
            ),
            typeFactory.zeroOrMore(
                typeFactory.itemMap(
                    typeFactory.itemString(), // xs:QName treated as atomic string
                    typeFactory.zeroOrMore(typeFactory.itemAnyItem()) // xs:anyAtomicType*
                )
            )
        );


        // fn:apply($function as function(*), $arguments as array(*)) as item()*
        register(
            "fn", "apply",
            List.of(
                new ArgumentSpecification("function", typeFactory.one(typeFactory.itemAnyFunction()), null),
                new ArgumentSpecification("arguments", typeFactory.one(typeFactory.itemAnyArray()), null)
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // fn:do-until($input as item()*, $action as function(item()*, xs:integer) as item()*, $predicate as function(item()*, xs:integer) as xs:boolean?) as item()*
        register(
            "fn", "do-until",
            List.of(
                new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification(
                    "action",
                    typeFactory.one(typeFactory.itemFunction(
                        typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                        List.of(
                            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                            typeFactory.one(typeFactory.itemNumber())
                        )
                    )),
                    null
                ),
                new ArgumentSpecification(
                    "predicate",
                    typeFactory.one(typeFactory.itemFunction(
                        typeFactory.zeroOrOne(typeFactory.itemBoolean()),
                        List.of(
                            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                            typeFactory.one(typeFactory.itemNumber())
                        )
                    )),
                    null
                )
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // fn:every($input as item()*, $predicate as function(item(), xs:integer) as xs:boolean? := fn:boolean#1) as xs:boolean
        register(
            "fn", "every",
            List.of(
                new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification(
                    "predicate",
                    typeFactory.zeroOrOne(typeFactory.itemFunction(
                        typeFactory.zeroOrOne(typeFactory.itemBoolean()),
                        List.of(
                            typeFactory.one(typeFactory.itemAnyItem()),
                            typeFactory.one(typeFactory.itemNumber())
                        )
                    )),
                    BOOLEAN$1
                )
            ),
            typeFactory.boolean_()
        );

        // fn:filter($input as item()*, $predicate as function(item(), xs:integer) as xs:boolean?) as item()*
        register(
            "fn", "filter",
            List.of(
                new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification(
                    "predicate",
                    typeFactory.one(typeFactory.itemFunction(
                        typeFactory.zeroOrOne(typeFactory.itemBoolean()),
                        List.of(
                            typeFactory.one(typeFactory.itemAnyItem()),
                            typeFactory.one(typeFactory.itemNumber())
                        )
                    )),
                    null
                )
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // fn:fold-left($input as item()*, $init as item()*, $action as function(item()*, item()) as item()*) as item()*
        register(
            "fn", "fold-left",
            List.of(
                new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification("init", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification(
                    "action",
                    typeFactory.one(typeFactory.itemFunction(
                        typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                        List.of(
                            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                            typeFactory.one(typeFactory.itemAnyItem())
                        )
                    )),
                    null
                )
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // fn:fold-right($input as item()*, $init as item()*, $action as function(item(), item()*) as item()*) as item()*
        register(
            "fn", "fold-right",
            List.of(
                new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification("init", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification(
                    "action",
                    typeFactory.one(typeFactory.itemFunction(
                        typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                        List.of(
                            typeFactory.one(typeFactory.itemAnyItem()),
                            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
                        )
                    )),
                    null
                )
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // fn:for-each($input as item()*, $action as function(item(), item()*) as item()*) as item()*
        register(
            "fn", "for-each",
            List.of(
                new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification(
                    "action",
                    typeFactory.one(typeFactory.itemFunction(
                        typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                        List.of(
                            typeFactory.one(typeFactory.itemAnyItem()),
                            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
                        )
                    )),
                    null
                )
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );


        // fn:for-each-pair($input1 as item()*, $input2 as item()*, $action as function(item(), item(), xs:integer) as item()*) as item()*
        register(
            "fn", "for-each-pair",
            List.of(
                new ArgumentSpecification("input1", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification("input2", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification("action", typeFactory.one(typeFactory.itemFunction(
                    typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                    List.of(
                        typeFactory.one(typeFactory.itemAnyItem()),
                        typeFactory.one(typeFactory.itemAnyItem()),
                        typeFactory.one(typeFactory.itemNumber())
                    )
                )), null)
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // fn:highest($input as item()*, $collation as xs:string? := fn:default-collation(), $key as function(item()) as xs:anyAtomicType*)? := fn:data#1) as item()*
        register(
            "fn", "highest",
            List.of(
                new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification("collation", typeFactory.zeroOrOne(typeFactory.itemString()), DEFAULT_COLLATION),
                new ArgumentSpecification("key", typeFactory.zeroOrOne(typeFactory.itemFunction(
                    typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                    List.of(typeFactory.one(typeFactory.itemAnyItem()))
                )), DATA$1)
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // fn:index-where($input as item()*, $predicate as function(item(), xs:integer) as xs:boolean?) as xs:integer*
        register(
            "fn", "index-where",
            List.of(
                new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification("predicate", typeFactory.one(typeFactory.itemFunction(
                    typeFactory.zeroOrOne(typeFactory.itemBoolean()),
                    List.of(
                        typeFactory.one(typeFactory.itemAnyItem()),
                        typeFactory.one(typeFactory.itemNumber())
                    )
                )), null)
            ),
            typeFactory.zeroOrMore(typeFactory.itemNumber())
        );

        // fn:lowest($input as item()*, $collation as xs:string? := fn:default-collation(), $key as function(item()) as xs:anyAtomicType*)? := fn:data#1) as item()*
        register(
            "fn", "lowest",
            List.of(
                new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification("collation", typeFactory.zeroOrOne(typeFactory.itemString()), DEFAULT_COLLATION),
                new ArgumentSpecification("key", typeFactory.zeroOrOne(typeFactory.itemFunction(
                    typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                    List.of(typeFactory.one(typeFactory.itemAnyItem()))
                )), DATA$1)
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // fn:partial-apply($function as function(*), $arguments as map(xs:positiveInteger, item()*)) as function(*)
        register(
            "fn", "partial-apply",
            List.of(
                new ArgumentSpecification("function", typeFactory.one(typeFactory.itemAnyFunction()), null),
                new ArgumentSpecification("arguments", typeFactory.one(
                    typeFactory.itemMap(typeFactory.itemNumber(), typeFactory.zeroOrMore(typeFactory.itemAnyItem()))
                ), null)
            ),
            typeFactory.one(typeFactory.itemAnyFunction())
        );

        // fn:partition($input as item()*, $split-when as function(item()*, item(), xs:integer) as xs:boolean?) as array(item())*
        register(
            "fn", "partition",
            List.of(
                new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification("split-when", typeFactory.one(typeFactory.itemFunction(
                    typeFactory.zeroOrOne(typeFactory.itemBoolean()),
                    List.of(
                        typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                        typeFactory.one(typeFactory.itemAnyItem()),
                        typeFactory.one(typeFactory.itemNumber())
                    )
                )), null)
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyArray())
        );

        // fn:scan-left(...) and fn:scan-right(...) mirror fold-left and fold-right
        register(
            "fn", "scan-left",
            List.of(
                new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification("init", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification("action", typeFactory.one(typeFactory.itemFunction(
                    typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                    List.of(
                        typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                        typeFactory.one(typeFactory.itemAnyItem())
                    )
                )), null)
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyArray())
        );

        register(
            "fn", "scan-right",
            List.of(
                new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification("init", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification("action", typeFactory.one(typeFactory.itemFunction(
                    typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                    List.of(
                        typeFactory.one(typeFactory.itemAnyItem()),
                        typeFactory.zeroOrMore(typeFactory.itemAnyItem())
                    )
                )), null)
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyArray())
        );

        // fn:some($input as item()*, $predicate as function(item(), xs:integer) as xs:boolean?) := fn:boolean#1) as xs:boolean
        register(
            "fn", "some",
            List.of(
                new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification("predicate", typeFactory.zeroOrOne(typeFactory.itemFunction(
                    typeFactory.zeroOrOne(typeFactory.itemBoolean()),
                    List.of(
                        typeFactory.one(typeFactory.itemAnyItem()),
                        typeFactory.one(typeFactory.itemNumber())
                    )
                )), BOOLEAN$1)
            ),
            typeFactory.boolean_()
        );

        // fn:sort( $input as item()*,
        // $collation as xs:string? := fn:default-collation(),
        // $key as fn(item()) as xs:anyAtomicType* := fn:data#1
        // ) as item()*
        register(
            "fn", "sort",
            List.of(
                new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification(
                    "collation",
                    typeFactory.zeroOrOne(typeFactory.itemString()),
                    DEFAULT_COLLATION
                ),
                new ArgumentSpecification(
                    "key",
                    typeFactory.one(typeFactory.itemFunction(
                        typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                        List.of(typeFactory.one(typeFactory.itemAnyItem()))
                    )),
                    DATA$1
                )
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // TODO: refine parser name tokenization
        // fn:sort-by(
        // $input as item()*,
        // $keys as record(key? as (fn(item()) as xs:anyAtomicType*)?,
        //                 collation? as xs:string?,
        //                 order? as enum('ascending', 'descending')?
        //                 )*
        // ) as item()*

        register(
            "fn", "sort-by",
            List.of(
                new ArgumentSpecification(
                    "input",
                    typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                    null
                ),
                new ArgumentSpecification(
                    "keys",
                    typeFactory.zeroOrMore(
                        typeFactory.itemRecord(
                            Map.of(
                                "key", new XQueryRecordField(
                                    typeFactory.zeroOrOne(
                                        typeFactory.itemFunction(
                                            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                                            List.of(typeFactory.one(typeFactory.itemAnyItem()))
                                        )
                                    ),
                                    false
                                ),
                                "collation", new XQueryRecordField(
                                    typeFactory.zeroOrOne(typeFactory.itemString()),
                                    false
                                ),
                                "order", new XQueryRecordField(
                                    typeFactory.zeroOrOne(
                                        typeFactory.itemEnum(Set.of("ascending", "descending"))
                                    ),
                                    false
                                )
                            )
                        )
                    ),
                    null
                )
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
    );



        // fn:sort-with($input as item()*, $comparators as function(item(), item()) as xs:integer*) as item()*
        register(
            "fn", "sort-with",
            List.of(
                new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification("comparators", typeFactory.zeroOrMore(typeFactory.itemFunction(
                    typeFactory.one(typeFactory.itemNumber()),
                    List.of(
                        typeFactory.one(typeFactory.itemAnyItem()),
                        typeFactory.one(typeFactory.itemAnyItem())
                    )
                )), null)
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // fn:subsequence-where($input as item()*,
        //                      $from as fn(item(), xs:integer) as xs:boolean? := true#0,
        //                      $to as fn(item(), xs:integer) as xs:boolean? := false#0
        // ) as item()*
        register(
            "fn", "subsequence-where",
            List.of(
                new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification(
                    "from",
                    typeFactory.zeroOrOne(typeFactory.itemFunction(
                        typeFactory.zeroOrOne(typeFactory.itemBoolean()),
                        List.of(
                            typeFactory.one(typeFactory.itemAnyItem()),
                            typeFactory.one(typeFactory.itemNumber())
                        )
                    )),
                    TRUE$0
                ),
                new ArgumentSpecification(
                    "to",
                    typeFactory.zeroOrOne(typeFactory.itemFunction(
                        typeFactory.zeroOrOne(typeFactory.itemBoolean()),
                        List.of(
                            typeFactory.one(typeFactory.itemAnyItem()),
                            typeFactory.one(typeFactory.itemNumber())
                        )
                    )),
                    FALSE$0
                )
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // fn:take-while($input as item()*, $predicate as fn(item(), xs:integer) as xs:boolean?) as item()*
        register(
            "fn", "take-while",
            List.of(
                new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification(
                    "predicate",
                    typeFactory.one(typeFactory.itemFunction(
                        typeFactory.zeroOrOne(typeFactory.itemBoolean()),
                        List.of(
                            typeFactory.one(typeFactory.itemAnyItem()),
                            typeFactory.one(typeFactory.itemNumber())
                        )
                    )),
                    null
                )
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // fn:transitive-closure($node as node()?, $step as fn(node()) as node()*) as node()*
        register(
            "fn", "transitive-closure",
            List.of(
                new ArgumentSpecification("node", typeFactory.zeroOrOne(typeFactory.itemAnyNode()), null),
                new ArgumentSpecification(
                    "step",
                    typeFactory.one(typeFactory.itemFunction(
                        typeFactory.zeroOrMore(typeFactory.itemAnyNode()),
                        List.of(typeFactory.one(typeFactory.itemAnyNode()))
                    )),
                    null
                )
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyNode())
        );

        // fn:while-do($input as item()*, $predicate as fn(item()*, xs:integer) as xs:boolean?, $action as fn(item()*, xs:integer) as item()*) as item()*
        register(
            "fn", "while-do",
            List.of(
                new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification(
                    "predicate",
                    typeFactory.one(typeFactory.itemFunction(
                        typeFactory.zeroOrOne(typeFactory.itemBoolean()),
                        List.of(
                            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                            typeFactory.one(typeFactory.itemNumber())
                        )
                    )),
                    null
                ),
                new ArgumentSpecification(
                    "action",
                    typeFactory.one(typeFactory.itemFunction(
                        typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                        List.of(
                            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                            typeFactory.one(typeFactory.itemNumber())
                        )
                    )),
                    null
                )
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // fn:op(
        //  as xs:string
        // ) as fn(item()*,item()) as item()*
        final ArgumentSpecification opOperator = new ArgumentSpecification("operator", typeFactory.string(), null);
        register("fn", "op",
                List.of(opOperator),
                typeFactory.one(typeFactory.itemFunction(zeroOrMoreItems, List.of(
                    zeroOrMoreItems,
                    zeroOrMoreItems
                ))));

        // map:build(
        //   $input   as item()*,
        //   $key     as (fn($item as item(), $position as xs:integer) as xs:anyAtomicType*)? := fn:identity#1,
        //   $value   as (fn($item as item(), $position as xs:integer) as item()*)?           := fn:identity#1,
        //   $options as map(*)? := {}
        // ) as map(*)
        ArgumentSpecification mbInput = new ArgumentSpecification(
            "input",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null
        );
        XQueryItemType mapTransformer = typeFactory.itemFunction(zeroOrMoreItems, List.of(typeFactory.anyItem(), typeFactory.number()));
        ArgumentSpecification mbKey = new ArgumentSpecification( "key", typeFactory.zeroOrOne(mapTransformer), IDENTITY$1);
        ArgumentSpecification mbValue = new ArgumentSpecification( "value", typeFactory.zeroOrOne(mapTransformer), IDENTITY$1);
        register(
            "map", "build",
            List.of(mbInput, mbKey, mbValue, mapOptionsArg),
            typeFactory.one(typeFactory.itemAnyMap())
        );

        // map:contains($map as map(*), $key as xs:anyAtomicType) as xs:boolean
        ArgumentSpecification mcMap = new ArgumentSpecification(
            "map",
            typeFactory.one(typeFactory.itemAnyMap()),
            null
        );
        ArgumentSpecification mcKey = new ArgumentSpecification(
            "key",
            typeFactory.one(typeFactory.itemAnyItem()),
            null
        );
        register(
            "map", "contains",
            List.of(mcMap, mcKey),
            typeFactory.boolean_()
        );

        // map:empty($map as map(*)) as xs:boolean
        ArgumentSpecification meMap = new ArgumentSpecification(
            "map",
            typeFactory.one(typeFactory.itemAnyMap()),
            null
        );
        register(
            "map", "empty",
            List.of(meMap),
            typeFactory.boolean_()
        );

        // map:entries($map as map(*)) as map(*)*
        ArgumentSpecification mentMap = new ArgumentSpecification(
            "map",
            typeFactory.one(typeFactory.itemAnyMap()),
            null
        );
        register(
            "map", "entries",
            List.of(mentMap),
            typeFactory.zeroOrMore(typeFactory.itemAnyMap())
        );

        // map:entry($key as xs:anyAtomicType, $value as item()*) as map(*)
        ArgumentSpecification mentKey = new ArgumentSpecification(
            "key",
            typeFactory.one(typeFactory.itemAnyItem()),
            null
        );
        ArgumentSpecification mentValue = new ArgumentSpecification(
            "value",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null
        );
        register(
            "map", "entry",
            List.of(mentKey, mentValue),
            typeFactory.one(typeFactory.itemAnyMap())
        );

        // map:filter($map as map(*), $predicate as fn(xs:anyAtomicType, item()*) as xs:boolean?) as map(*)
        ArgumentSpecification mfMap = new ArgumentSpecification(
            "map",
            typeFactory.one(typeFactory.itemAnyMap()),
            null
        );
        final var optionalBoolean = typeFactory.zeroOrOne(typeFactory.itemBoolean());
        final XQueryItemType predicate = typeFactory.itemFunction(optionalBoolean, List.of(typeFactory.anyItem(), zeroOrMoreItems));
        ArgumentSpecification predicateArg = new ArgumentSpecification( "predicate", typeFactory.one(predicate), null);
        register(
            "map", "filter",
            List.of(mfMap, predicateArg),
            typeFactory.one(typeFactory.itemAnyMap())
        );

        // map:find($input as item()*, $key as xs:anyAtomicType) as array(*)
        ArgumentSpecification mfindInput = new ArgumentSpecification(
            "input",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null
        );
        ArgumentSpecification mfindKey = new ArgumentSpecification(
            "key",
            typeFactory.one(typeFactory.itemAnyItem()),
            null
        );
        register(
            "map", "find",
            List.of(mfindInput, mfindKey),
            typeFactory.one(typeFactory.itemAnyArray())
        );

        // map:for-each($map as map(*), $action as fn(xs:anyAtomicType, item()*) as item()*) as item()*
        ArgumentSpecification mfeMap = new ArgumentSpecification(
            "map",
            typeFactory.one(typeFactory.itemAnyMap()),
            null
        );
        XQuerySequenceType action = typeFactory.function(zeroOrMoreItems, List.of(typeFactory.anyItem(), zeroOrMoreItems));
        ArgumentSpecification mfeAction = new ArgumentSpecification( "action", action, null);
        register(
            "map", "for-each",
            List.of(mfeMap, mfeAction),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // map:get($map as map(*), $key as xs:anyAtomicType, $default as item()* := ()) as item()*
        ArgumentSpecification mgMap = new ArgumentSpecification(
            "map",
            typeFactory.one(typeFactory.itemAnyMap()),
            null
        );
        ArgumentSpecification mgKey = new ArgumentSpecification(
            "key",
            typeFactory.one(typeFactory.itemAnyItem()),
            null
        );
        ArgumentSpecification mgDefault = new ArgumentSpecification(
            "default",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            EMPTY_SEQUENCE
        );
        register(
            "map", "get",
            List.of(mgMap, mgKey, mgDefault),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // map:items($map as map(*)) as item()*
        ArgumentSpecification mitemsMap = new ArgumentSpecification(
            "map",
            typeFactory.one(typeFactory.itemAnyMap()),
            null
        );
        register(
            "map", "items",
            List.of(mitemsMap),
            zeroOrMoreItems
        );

        // map:keys($map as map(*)) as xs:anyAtomicType*
        ArgumentSpecification mkeysMap = new ArgumentSpecification(
            "map",
            typeFactory.one(typeFactory.itemAnyMap()),
            null
        );
        register(
            "map", "keys",
            List.of(mkeysMap),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // map:keys-where($map as map(*), $predicate as fn(xs:anyAtomicType, item()*) as xs:boolean?) as xs:anyAtomicType*
        ArgumentSpecification kwMap = new ArgumentSpecification(
            "map",
            typeFactory.one(typeFactory.itemAnyMap()),
            null
        );
        register(
            "map", "keys-where",
            List.of(kwMap, predicateArg),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // map:merge($maps as map(*)*, $options as map(*)? := {}) as map(*)
        ArgumentSpecification mmMaps = new ArgumentSpecification(
            "maps",
            typeFactory.zeroOrMore(typeFactory.itemAnyMap()),
            null
        );
        register(
            "map", "merge",
            List.of(mmMaps, mapOptionsArg),
            typeFactory.one(typeFactory.itemAnyMap())
        );

        // map:of-pairs($input as key-value-pair*, $options as map(*)? := {}) as map(*)
        ArgumentSpecification opInput = new ArgumentSpecification(
            "input",
            typeFactory.zeroOrMore(typeFactory.itemNamedType("fn:key-value-pair")),
            null
        );
        register(
            "map", "of-pairs",
            List.of(opInput, mapOptionsArg),
            typeFactory.one(typeFactory.itemAnyMap())
        );

        // map:pair($key as xs:anyAtomicType, $value as item()*) as key-value-pair
        ArgumentSpecification mpKey = new ArgumentSpecification(
            "key",
            typeFactory.one(typeFactory.itemAnyItem()),
            null
        );
        ArgumentSpecification mpValue = new ArgumentSpecification(
            "value",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null
        );
        register(
            "map", "pair",
            List.of(mpKey, mpValue),
            typeFactory.namedType("fn:key-value-pair")
        );

        register(
            "map", "pairs",
            List.of(
                new ArgumentSpecification(
                    "map",
                    typeFactory.one(typeFactory.itemAnyMap()),
                    null
                )
            ),
            typeFactory.zeroOrMore(typeFactory.itemNamedType("fn:key-value-pair"))
        );


        // map:put(
        //   $map   as map(*),
        //   $key   as xs:anyAtomicType,
        //   $value as item()*
        // ) as map(*)
        ArgumentSpecification mputMap = new ArgumentSpecification(
            "map",
            typeFactory.one(typeFactory.itemAnyMap()),
            null
        );
        ArgumentSpecification mputKey = new ArgumentSpecification(
            "key",
            typeFactory.one(typeFactory.itemAnyItem()),
            null
        );
        ArgumentSpecification mputValue = new ArgumentSpecification(
            "value",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null
        );
        register(
            "map", "put",
            List.of(mputMap, mputKey, mputValue),
            typeFactory.one(typeFactory.itemAnyMap())
        );

        // map:remove(
        //   $map  as map(*),
        //   $keys as xs:anyAtomicType*
        // ) as map(*)
        ArgumentSpecification mremMap = new ArgumentSpecification(
            "map",
            typeFactory.one(typeFactory.itemAnyMap()),
            null
        );
        ArgumentSpecification mremKeys = new ArgumentSpecification(
            "keys",
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null
        );
        register(
            "map", "remove",
            List.of(mremMap, mremKeys),
            typeFactory.one(typeFactory.itemAnyMap())
        );

        // map:size(
        //   $map as map(*)
        // ) as xs:integer
        ArgumentSpecification msizeMap = new ArgumentSpecification(
            "map",
            typeFactory.one(typeFactory.itemAnyMap()),
            null
        );
        register(
            "map", "size",
            List.of(msizeMap),
            typeFactory.number() // or typeFactory.integer() if you prefer strict type
        );


        // fn:element-to-map-plan(
        //  as (document-node() | element(*))*
        // ) as map(xs:string, record(*))
        ArgumentSpecification etmpInput = new ArgumentSpecification("input",
                                                                    typeFactory.zeroOrMore(typeFactory.itemAnyNode()),
                                                                    null);
        register("fn", "element-to-map-plan",
            List.of(etmpInput), typeFactory.map(typeFactory.itemString(), typeFactory.anyMap())
        );

        // fn:element-to-map(
        //  as element()?,
        //  as map(*)? := {}
        // ) as map(xs:string, item()?)?
        ArgumentSpecification etmElement = new ArgumentSpecification("element", typeFactory.zeroOrOne(typeFactory.itemAnyNode()), null);
        ArgumentSpecification etmOptions = new ArgumentSpecification("options", typeFactory.zeroOrOne(typeFactory.itemAnyMap()), EMPTY_MAP);
        register("fn", "element-to-map",
            List.of(etmElement, etmOptions),
            typeFactory.zeroOrOne(typeFactory.itemMap(typeFactory.itemString(), typeFactory.zeroOrOne(typeFactory.itemAnyItem())))
        );

        // array:append($array as array(*), $member as item()*) as array(*)
        register(
            "array", "append",
            List.of(
                new ArgumentSpecification(
                    "array",
                    typeFactory.one(typeFactory.itemAnyArray()),
                    null
                ),
                new ArgumentSpecification(
                    "member",
                    typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                    null
                )
            ),
            typeFactory.one(typeFactory.itemAnyArray())
        );

        // array:build($input as item()*, $action as function(item(), xs:integer) as item()* := fn:identity#1) as array(*)
        register(
            "array", "build",
            List.of(
                new ArgumentSpecification( "input", zeroOrMoreItems, null),
                new ArgumentSpecification(
                    "action",
                    typeFactory.zeroOrOne(typeFactory.itemFunction(zeroOrMoreItems, List.of(typeFactory.anyItem(), typeFactory.number()))),
                    IDENTITY$1
                )
            ),
            typeFactory.anyArray()
        );

        // array:empty($array as array(*)) as xs:boolean
        register(
            "array", "empty",
            List.of(
                new ArgumentSpecification(
                    "array",
                    typeFactory.one(typeFactory.itemAnyArray()),
                    null
                )
            ),
            typeFactory.boolean_()
        );

        // array:filter($array as array(*), $predicate as function(item(), xs:integer) as xs:boolean?) as array(*)
        final XQueryItemType itemIntegerActionFunction = typeFactory.itemFunction(optionalBoolean, List.of(typeFactory.anyItem(), typeFactory.number()));
        register(
            "array", "filter",
            List.of(
                new ArgumentSpecification(
                    "array",
                    typeFactory.one(typeFactory.itemAnyArray()),
                    null
                ),
                new ArgumentSpecification(
                    "predicate",
                    typeFactory.one(itemIntegerActionFunction),
                    null
                )
            ),
            typeFactory.one(typeFactory.itemAnyArray())
        );

        // array:flatten($input as item()) as item()*
        register(
            "array", "flatten",
            List.of(
                new ArgumentSpecification(
                    "input",
                    typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                    null
                )
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // array:fold-left($array as array(*), $init as item()*, $action as function(item(), item()*) as item()*) as item()*
        final XQueryItemType function_anyItem_zeroOrMoreItems$zeroOrMoreItems = typeFactory.itemFunction(zeroOrMoreItems, List.of(typeFactory.anyItem(), zeroOrMoreItems));
        register(
            "array", "fold-left",
            List.of(
                new ArgumentSpecification(
                    "array",
                    typeFactory.one(typeFactory.itemAnyArray()),
                    null
                ),
                new ArgumentSpecification(
                    "init",
                    typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                    null
                ),
                new ArgumentSpecification(
                    "action",
                    typeFactory.one(function_anyItem_zeroOrMoreItems$zeroOrMoreItems),
                    null
                )
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // array:fold-right($array as array(*), $init as item()*, $action as function(item(), item()*) as item()*) as item()*
        register(
            "array", "fold-right",
            List.of(
                new ArgumentSpecification(
                    "array",
                    typeFactory.one(typeFactory.itemAnyArray()),
                    null
                ),
                new ArgumentSpecification(
                    "init",
                    typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                    null
                ),
                new ArgumentSpecification(
                    "action",
                    typeFactory.one(function_anyItem_zeroOrMoreItems$zeroOrMoreItems),
                    null
                )
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // array:foot($array as array(*)) as item()*
        register(
            "array", "foot",
            List.of(
                new ArgumentSpecification(
                    "array",
                    typeFactory.one(typeFactory.itemAnyArray()),
                    null
                )
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // array:for-each($array as array(*),
        //                $action as function(item()*, xs:integer) as item()*
        // ) as array(*)
        final XQueryItemType function_zeroOrMoreItems_number$zeroOrMoreItems = typeFactory.itemFunction(zeroOrMoreItems, List.of(zeroOrMoreItems, typeFactory.number()));
        register(
            "array", "for-each",
            List.of(
                new ArgumentSpecification(
                    "array",
                    typeFactory.one(typeFactory.itemAnyArray()),
                    null
                ),
                new ArgumentSpecification(
                    "action",
                    typeFactory.one(function_zeroOrMoreItems_number$zeroOrMoreItems),
                    null
                )
            ),
            typeFactory.one(typeFactory.itemAnyArray())
        );

        // array:for-each-pair($array1 as array(*), $array2 as array(*), $action as function(item(), item(), xs:integer) as item()*) as array(*)
        register(
            "array", "for-each-pair",
            List.of(
                new ArgumentSpecification(
                    "array1",
                    typeFactory.one(typeFactory.itemAnyArray()),
                    null
                ),
                new ArgumentSpecification(
                    "array2",
                    typeFactory.one(typeFactory.itemAnyArray()),
                    null
                ),
                new ArgumentSpecification(
                    "action",
                    typeFactory.one(typeFactory.itemFunction(zeroOrMoreItems, List.of(typeFactory.anyItem(), typeFactory.anyItem(), typeFactory.number()))),
                    null
                )
            ),
            typeFactory.one(typeFactory.itemAnyArray())
        );

        // array:get($array as array(*), $position as xs:integer) as item()*
        register(
            "array", "get",
            List.of(
                new ArgumentSpecification(
                    "array",
                    typeFactory.one(typeFactory.itemAnyArray()),
                    null
                ),
                new ArgumentSpecification(
                    "position",
                    typeFactory.one(typeFactory.itemNumber()),
                    null
                )
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // array:get($array as array(*), $position as xs:integer, $default as item()*) as item()*
        register(
            "array", "get",
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("position", typeFactory.one(typeFactory.itemNumber()), null),
                new ArgumentSpecification("default", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null)
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // array:head($array as array(*)) as item()*
        register(
            "array", "head",
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null)
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // array:index-of($array as array(*), $target as item()*, $collation as xs:string? := fn:default-collation()) as xs:integer*
        register(
            "array", "index-of",
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("target", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification("collation", typeFactory.zeroOrOne(typeFactory.itemString()), DEFAULT_COLLATION)
            ),
            typeFactory.zeroOrMore(typeFactory.itemNumber())
        );

        final XQueryItemType integerPredicate = typeFactory.itemFunction(optionalBoolean, List.of(zeroOrMoreItems, typeFactory.number()));
        // array:index-where($array as array(*), $predicate as function(item()*, xs:integer) as xs:boolean?) as xs:integer*
        register(
            "array", "index-where",
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("predicate", typeFactory.one(integerPredicate), null)
            ),
            typeFactory.zeroOrMore(typeFactory.itemNumber())
        );

        // array:insert-before($array as array(*), $position as xs:integer, $member as item()*) as array(*)
        register(
            "array", "insert-before",
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("position", typeFactory.one(typeFactory.itemNumber()), null),
                new ArgumentSpecification("member", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null)
            ),
            typeFactory.one(typeFactory.itemAnyArray())
        );

        // array:items($array as array(*)) as item()*
        register(
            "array", "items",
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null)
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        );

        // array:join($arrays as array(*)*, $separator as array(*)? := ()) as array(*)
        register(
            "array", "join",
            List.of(
                new ArgumentSpecification("arrays", typeFactory.zeroOrMore(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("separator", typeFactory.zeroOrOne(typeFactory.itemAnyArray()), EMPTY_SEQUENCE)
            ),
            typeFactory.one(typeFactory.itemAnyArray())
        );

        register(
            "array", "members",
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null)
            ),
            typeFactory.zeroOrMore(
                typeFactory.itemRecord(
                    Map.of(
                        "value", new XQueryRecordField(
                            typeFactory.anyItem(),
                            true // field is required
                        )
                    )
                )
            )
        );

        register(
            "array", "of-members",
            List.of(
                new ArgumentSpecification(
                    "input",
                    typeFactory.zeroOrMore(
                        typeFactory.itemRecord(
                            Map.of(
                                "value", new XQueryRecordField( zeroOrMoreItems, true)
                            )
                        )
                    ),
                    null
                )
            ),
            typeFactory.anyArray()
        );



        // array:put($array as array(*), $position as xs:integer, $member as item()*) as array(*)
        register(
            "array", "put",
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("position", typeFactory.one(typeFactory.itemNumber()), null),
                new ArgumentSpecification("member", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null)
            ),
            typeFactory.one(typeFactory.itemAnyArray())
        );

        // array:remove($array as array(*), $positions as xs:integer*) as array(*)
        register(
            "array", "remove",
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("positions", typeFactory.zeroOrMore(typeFactory.itemNumber()), null)
            ),
            typeFactory.one(typeFactory.itemAnyArray())
        );

        // array:reverse($array as array(*)) as array(*)
        register(
            "array", "reverse",
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null)
            ),
            typeFactory.one(typeFactory.itemAnyArray())
        );

        // array:size($array as array(*)) as xs:integer
        register(
            "array", "size",
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null)
            ),
            typeFactory.one(typeFactory.itemNumber())
        );

        // array:slice($array as array(*), $start as xs:integer? := (), $end as xs:integer? := (), $step as xs:integer? := ()) as array(*)
        register(
            "array", "slice",
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("start", typeFactory.zeroOrOne(typeFactory.itemNumber()), EMPTY_SEQUENCE),
                new ArgumentSpecification("end", typeFactory.zeroOrOne(typeFactory.itemNumber()), EMPTY_SEQUENCE),
                new ArgumentSpecification("step", typeFactory.zeroOrOne(typeFactory.itemNumber()), EMPTY_SEQUENCE)
            ),
            typeFactory.one(typeFactory.itemAnyArray())
        );

        // array:sort($array as array(*), $collation as xs:string? := fn:default-collation(), $key as function(item()*) as xs:anyAtomicType* := fn:data#1) as array(*)
        register(
            "array", "sort",
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("collation", typeFactory.zeroOrOne(typeFactory.itemString()), DEFAULT_COLLATION),
                new ArgumentSpecification("key",
                    typeFactory.zeroOrOne(typeFactory.itemFunction(zeroOrMoreItems, List.of(zeroOrMoreItems))),
                    DATA$1)
            ),
            typeFactory.one(typeFactory.itemAnyArray())
        );


        // array:sort-by(
        // $array	as item()*,
        // $keys	as record(key? as (fn(item()*) as xs:anyAtomicType*)?,
        //                    collation? as xs:string?,
        //                    order? as enum('ascending', 'descending')?)*
        // ) as item()*

        final var keyType = typeFactory.zeroOrOne(typeFactory.itemFunction(zeroOrMoreItems, List.of(zeroOrMoreItems)));
        final var orderType = typeFactory.zeroOrOne(typeFactory.itemEnum(Set.of("ascending", "descending")));
        final var keysItemType = typeFactory.itemRecord(
                    Map.of(
                        "key", new XQueryRecordField(keyType, true),
                        "collation", new XQueryRecordField(optionalString, true),
                        "order", new XQueryRecordField(orderType, true)
                    ));

        final var keysType = typeFactory.zeroOrMore(keysItemType);

        List<ArgumentSpecification> sortByArgs = List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("keys", keysType, null));


        register("array", "sort-by", sortByArgs, zeroOrMoreItems);

        // array:split($array as array(*)) as array(*)*
        register(
            "array", "split",
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null)
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyArray())
        );

        // array:subarray($array as array(*), $start as xs:integer, $length as xs:integer? := ()) as array(*)
        register(
            "array", "subarray",
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("start", typeFactory.one(typeFactory.itemNumber()), null),
                new ArgumentSpecification("length", typeFactory.zeroOrOne(typeFactory.itemNumber()), EMPTY_SEQUENCE)
            ),
            typeFactory.one(typeFactory.itemAnyArray())
        );

        // array:tail($array as array(*)) as array(*)
        register(
            "array", "tail",
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null)
            ),
            typeFactory.one(typeFactory.itemAnyArray())
        );

        // array:trunk($array as array(*)) as array(*)
        register(
            "array", "trunk",
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null)
            ),
            typeFactory.one(typeFactory.itemAnyArray())
        );

        // fn:type-of( as item()*) as xs:string
        final ArgumentSpecification typeOfValue = new ArgumentSpecification("value", zeroOrMoreItems, null);
        register("fn", "type-of",
                List.of(typeOfValue),
                typeFactory.string());

        // // xs:string( as xs:anyAtomicType? := .) as xs:string?
        // final ArgumentSpecification castStringValue = new ArgumentSpecification("value", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        // register("xs", "string",
        //         List.of(castStringValue),
        //         optionalString));


        // fn:random-number-generator(
        //   $seed as xs:anyAtomicType? := ()
        // ) as random-number-generator-record

        ArgumentSpecification rngSeed = new ArgumentSpecification(
            "seed",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()),
            EMPTY_SEQUENCE
        );

        register(
            "fn", "random-number-generator",
            List.of(rngSeed),
            typeFactory.namedType("fn:random-number-generator-record")
        );

    }

    private static ParseTree getTree(final String xquery, Function<AntlrXqueryParser, ParseTree> initialRule) {
        final CodePointCharStream charStream = CharStreams.fromString(xquery);
        final AntlrXqueryLexer lexer = new AntlrXqueryLexer(charStream);
        final CommonTokenStream stream = new CommonTokenStream(lexer);
        final AntlrXqueryParser parser = new AntlrXqueryParser(stream);
        return initialRule.apply(parser);
    }

    final Map<String, Map<String, List<FunctionSpecification>>> namespaces;

    private AnalysisResult handleUnknownNamespace(final String namespace, final DiagnosticError errorMessageSupplier,
            final XQuerySequenceType fallbackType) {
        final List<DiagnosticError> errors = List.of(errorMessageSupplier);
        return new AnalysisResult(fallbackType, List.of(), errors);
    }

    private AnalysisResult handleUnknownFunction(final String namespace, final String name,
            final DiagnosticError errorMessageSupplier, final XQuerySequenceType fallbackType) {
        final List<DiagnosticError> errors = List.of(errorMessageSupplier);
        return new AnalysisResult(fallbackType, List.of(), errors);
    }

    private AnalysisResult handleNoMatchingFunction(
            final DiagnosticError errorMessageSupplier,
            final XQuerySequenceType fallbackType)
    {
        final List<DiagnosticError> errors = List.of(errorMessageSupplier);
        return new AnalysisResult(fallbackType, List.of(), errors);
    }

    record SpecAndErrors(FunctionSpecification spec, List<DiagnosticError> errors) {
    }

    SpecAndErrors getFunctionSpecification(
        final ParserRuleContext location, final String namespace, final String name,
        final List<FunctionSpecification> namedFunctions, final long requiredArity)
    {
        final List<String> mismatchReasons = new ArrayList<>();
        for (final FunctionSpecification spec : namedFunctions) {
            final List<String> reasons = new ArrayList<>();
            if (!(spec.minArity() <= requiredArity && requiredArity <= spec.maxArity())) {
                reasons.add("Arity mismatch: expected between " + spec.minArity() + " and " + spec.maxArity() + ", got "
                        + requiredArity);
                mismatchReasons.add("Function " + name + ": " + String.join("; ", reasons));
                continue;
            }
            // used positional arguments need to have matching types
            return new SpecAndErrors(spec, List.of());
        }
        // If no spec matched, return all mismatch reasons
        final String errorMessage = "No matching function " + namespace + ":" + name + " for arity " + requiredArity +
                (mismatchReasons.isEmpty() ? "" : ". Reasons:\n" + String.join("\n", mismatchReasons));

        DiagnosticError error = DiagnosticError.of(location, errorMessage);
        return new SpecAndErrors(null, List.of(error));
    }

    public AnalysisResult call(
            final ParserRuleContext location,
            final String namespace,
            final String name,
            final List<XQuerySequenceType> positionalargs,
            final Map<String, XQuerySequenceType> keywordArgs,
            final XQueryVisitingSemanticContext context)
    {
        final var anyItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        if (!namespaces.containsKey(namespace)) {
            DiagnosticError error = DiagnosticError.of(location, "Unknown function namespace: " + namespace);
            return handleUnknownNamespace(namespace, error, anyItems);
        }

        final var namespaceFunctions = namespaces.get(namespace);
        if (!namespaceFunctions.containsKey(name)) {
            DiagnosticError error = DiagnosticError.of(location, "Unknown function: " + namespace + ":" + name);
            return handleUnknownFunction(namespace, name, error, anyItems);
        }
        final var namedFunctions = namespaceFunctions.get(name);
        final int positionalArgsCount = positionalargs.size();
        final var requiredArity = positionalArgsCount + keywordArgs.size();

        final List<String> mismatchReasons = new ArrayList<>();

        final SpecAndErrors specAndErrors = getFunctionSpecification(location, namespace, name, namedFunctions, requiredArity);
        if (specAndErrors.spec == null) {
            return new AnalysisResult(anyItems, List.of(), specAndErrors.errors);
        }
        final var spec = specAndErrors.spec;
        // used positional arguments need to have matching types
        final List<String> reasons = new ArrayList<>();
        final boolean positionalTypeMismatch = tryToMatchPositionalArgs(positionalargs, positionalArgsCount, spec,
                reasons);

        if (positionalTypeMismatch) {
            mismatchReasons.add("Function " + name + ": " + String.join("; ", reasons));
        }

        checkIfCorrectContext(spec, context, mismatchReasons);

        final List<String> allArgNames = spec.args.stream().map(ArgumentSpecification::name).toList();
        // used keywords need to match argument names in function declaration
        checkIfCorrectKeywordNames(name, keywordArgs, mismatchReasons, reasons, allArgNames);

        // TODO: unique keyword names
        final int specifiedArgsSize = spec.args.size();
        final List<String> remainingArgNames = allArgNames.subList(positionalArgsCount, specifiedArgsSize);
        // used keywords mustn't be any of the used positional args
        checkIfKeywordNotAlreadyInPositionalArgs(name, keywordArgs, mismatchReasons, reasons, remainingArgNames);

        // args that have not been positionally assigned
        final var remainingArgs = spec.args.subList(positionalArgsCount, specifiedArgsSize);
        final var usedAsKeywordCriterion = Collectors
                .<ArgumentSpecification>partitioningBy(arg -> keywordArgs.containsKey(arg.name()));
        final var unusedArgs = remainingArgs.parallelStream().collect(usedAsKeywordCriterion);
        final var unusedArgs_ = unusedArgs.get(false);
        checkIfAllNotUsedArgumentsAreOptional(name, mismatchReasons, reasons, unusedArgs_);

        final Stream<ArgumentSpecification> defaultArgs = unusedArgs_.stream().filter(arg->arg.defaultArgument() != null);

        // all the arguments that HAVE been used as keywords in call need to have
        // matching type
        final boolean keywordTypeMismatch = checkIfTypesMatchForKeywordArgs(keywordArgs, reasons, unusedArgs);
        if (keywordTypeMismatch) {
            mismatchReasons.add("Function " + name + ": " + String.join("; ", reasons));
        }
        if (mismatchReasons.isEmpty()) {
            return new AnalysisResult(spec.returnedType, defaultArgs.toList(), List.of());
        }
        final String message = getNoMatchingFunctionMessage(namespace, name, requiredArity, mismatchReasons);
        final DiagnosticError error = DiagnosticError.of(location, message);
        return handleNoMatchingFunction(error, spec.returnedType);
    }

    private void checkIfCorrectContext(FunctionSpecification spec, XQueryVisitingSemanticContext context, List<String> mismatchReasons)
    {
        if (spec.requiresPosition && context.getPositionType() == null) {
            mismatchReasons.add("Function requires context position");
        }
        if (spec.requiresSize && context.getSizeType() == null) {
            mismatchReasons.add("Function requires context size");
        }
        if (spec.requiredContextValueType != null
            && !context.getType().isSubtypeOf(spec.requiredContextValueType))
        {
            String message = getIncorrectContextValueMessage(spec, context);
			mismatchReasons.add(message);
        }
	}

    private String getIncorrectContextValueMessage(FunctionSpecification spec, XQueryVisitingSemanticContext context) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Invalid context value: ");
        stringBuilder.append(context.getType().toString());
        stringBuilder.append(" is not subtype of ");
        stringBuilder.append(spec.requiredContextValueType.toString());
        return stringBuilder.toString();
    }

	private String getNoMatchingFunctionMessage(final String namespace, final String name, final int requiredArity,
            final List<String> mismatchReasons) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("No matching function ");
        stringBuilder.append(namespace);
        stringBuilder.append(":");
        stringBuilder.append(name);
        stringBuilder.append(" for arity ");
        stringBuilder.append(requiredArity);
        if (!mismatchReasons.isEmpty()) {
            for (final String reason : mismatchReasons) {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append("\t");
                stringBuilder.append(reason);
            }
        }
        return stringBuilder.toString();
    }

    private boolean checkIfTypesMatchForKeywordArgs(final Map<String, XQuerySequenceType> keywordArgs,
            final List<String> reasons,
            final Map<Boolean, List<ArgumentSpecification>> partitioned) {
        boolean keywordTypeMismatch = false;
        for (final ArgumentSpecification arg : partitioned.get(true)) {
            final XQuerySequenceType passedType = keywordArgs.get(arg.name());
            if (!passedType.isSubtypeOf(arg.type())) {
                reasons.add("Keyword argument '" + arg.name() + "' type mismatch: expected " + arg.type() + ", got "
                        + passedType);
                keywordTypeMismatch = true;
            }
        }
        return keywordTypeMismatch;
    }

    private void checkIfAllNotUsedArgumentsAreOptional(final String name, final List<String> mismatchReasons,
            final List<String> reasons,
            final List<ArgumentSpecification> unusedArgs)
    {
        // all the arguments that HAVE NOT been used as keywords in call need to be
        // optional
        final boolean missingRequired = unusedArgs.parallelStream()
                .anyMatch(arg->arg.defaultArgument() == null);
        if (missingRequired) {
            Stream<ArgumentSpecification> requiredUnusedArgs = unusedArgs.stream().filter(arg->arg.defaultArgument() == null);
            Stream<String> requiredUnusedArgsNames = requiredUnusedArgs.map(ArgumentSpecification::name);
            String missingRequiredArguments = requiredUnusedArgsNames.collect(Collectors.joining(", "));
            reasons.add("Missing required keyword argument(s): " + missingRequiredArguments);
            mismatchReasons.add("Function " + name + ": " + String.join("; ", reasons));
        }
    }

    private void checkIfKeywordNotAlreadyInPositionalArgs(final String name,
            final Map<String, XQuerySequenceType> keywordArgs,
            final List<String> mismatchReasons, final List<String> reasons, final List<String> remainingArgNames) {
        if (!remainingArgNames.containsAll(keywordArgs.keySet())) {
            reasons.add("Keyword argument(s) overlap with positional arguments: " + keywordArgs.keySet().stream()
                    .filter(k -> !remainingArgNames.contains(k)).collect(Collectors.joining(", ")));
            mismatchReasons.add("Function " + name + ": " + String.join("; ", reasons));
        }
    }

    private void checkIfCorrectKeywordNames(final String name, final Map<String, XQuerySequenceType> keywordArgs,
            final List<String> mismatchReasons, final List<String> reasons, final List<String> allArgNames) {
        if (!allArgNames.containsAll(keywordArgs.keySet())) {
            reasons.add("Unknown keyword argument(s): " + keywordArgs.keySet().stream()
                    .filter(k -> !allArgNames.contains(k)).collect(Collectors.joining(", ")));
            mismatchReasons.add("Function " + name + ": " + String.join("; ", reasons));
        }
    }

    private boolean tryToMatchPositionalArgs(final List<XQuerySequenceType> positionalargs,
            final int positionalArgsCount, final FunctionSpecification spec, final List<String> reasons) {
        boolean positionalTypeMismatch = false;
        for (int i = 0; i < positionalArgsCount; i++) {
            final var positionalArg = positionalargs.get(i);
            final var expectedArg = spec.args.get(i);
            if (!positionalArg.isSubtypeOf(expectedArg.type())) {
                reasons.add("Positional argument " + (i + 1) + " type mismatch: expected " + expectedArg.type()
                        + ", got " + positionalArg);
                positionalTypeMismatch = true;
            }
        }
        return positionalTypeMismatch;
    }

    public AnalysisResult getFunctionReference(final ParserRuleContext location,
                                                final String namespace,
                                                final String functionName,
                                                final int arity)
    {
        // TODO: Verify logic
        final var fallback = typeFactory.anyFunction();
        if (!namespaces.containsKey(namespace)) {
            DiagnosticError error = DiagnosticError.of(location, "Unknown function namespace: " + namespace);
            return handleUnknownNamespace(namespace, error, fallback);
        }
        final var namespaceFunctions = namespaces.get(namespace);
        if (!namespaceFunctions.containsKey(functionName)) {
            DiagnosticError error = DiagnosticError.of(location, "Unknown function: " + namespace + ":" + functionName);
            return handleUnknownFunction(namespace, functionName, error, fallback);
        }

        final var namedFunctions = namespaceFunctions.get(functionName);
        final SpecAndErrors specAndErrors = getFunctionSpecification(
            location, namespace, functionName, namedFunctions, arity);
        if (specAndErrors.spec == null) {
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unknown function reference: ");
            stringBuilder.append(namespace);
            stringBuilder.append(":");
            stringBuilder.append(functionName);
            stringBuilder.append("#");
            stringBuilder.append(arity);
            DiagnosticError error = DiagnosticError.of(location, stringBuilder.toString());
            return new AnalysisResult(fallback, List.of(), List.of(error));
        }
        XQuerySequenceType returnedType = specAndErrors.spec.returnedType;
        List<XQuerySequenceType> argTypes = specAndErrors.spec.args.stream().map(arg->arg.type()).toList().subList(0, arity);
        var functionItem = typeFactory.function(returnedType, argTypes);
        return new AnalysisResult(functionItem, List.of(), specAndErrors.errors);

    }

    public FunctionSpecification getNamedFunctionSpecification(final ParserRuleContext location,
                                                final String namespace,
                                                final String functionName,
                                                final int arity)
    {
        if (!namespaces.containsKey(namespace)) {
            return null;
        }
        final var namespaceFunctions = namespaces.get(namespace);
        if (!namespaceFunctions.containsKey(functionName)) {
            return null;
        }

        final var namedFunctions = namespaceFunctions.get(functionName);
        final SpecAndErrors specAndErrors = getFunctionSpecification(
            location, namespace, functionName, namedFunctions, arity);
        return specAndErrors.spec;
    }


    public XQuerySemanticError register(
            final String namespace,
            final String functionName,
            final List<ArgumentSpecification> args,
            final XQuerySequenceType returnedType) {
        return register(namespace, functionName, args, returnedType, null, false, false, null, ((_, _, _) -> returnedType));
    }

    public XQuerySemanticError register(
            final String namespace,
            final String functionName,
            final List<ArgumentSpecification> args,
            final XQuerySequenceType returnedType,
            final ParseTree body) {
        return register(namespace, functionName, args, returnedType, null, false, false, body, null);
    }

    public XQuerySemanticError register(
            final String namespace,
            final String functionName,
            final List<ArgumentSpecification> args,
            final XQuerySequenceType returnedType,
            final ParseTree body,
            final GrainedAnalysis analysis) {
        return register(namespace, functionName, args, returnedType, null, false, false, body, analysis);
    }

    public XQuerySemanticError register(
            final String namespace,
            final String functionName,
            final List<ArgumentSpecification> args,
            final XQuerySequenceType returnedType,
            final XQuerySequenceType requiredContextValueType,
            final boolean requiresPosition,
            final boolean requiresLength,
            final ParseTree body,
            final GrainedAnalysis analysis)
    {
        final long minArity = args.stream().filter(arg -> arg.defaultArgument() == null).collect(Collectors.counting());
        final long maxArity = args.size();
        if (!namespaces.containsKey(namespace)) {
            final Map<String, List<FunctionSpecification>> functions = new HashMap<>();
            final List<FunctionSpecification> functionList = new ArrayList<>();
            functionList.add(new FunctionSpecification(minArity, maxArity, args, returnedType, requiredContextValueType,
                    requiresPosition, requiresLength, body, analysis));
            functions.put(functionName, functionList);
            namespaces.put(namespace, functions);
            return null;
        }
        final var namespaceMapping = namespaces.get(namespace);
        if (!namespaceMapping.containsKey(functionName)) {
            final List<FunctionSpecification> functionList = new ArrayList<>();
            functionList.add(new FunctionSpecification(minArity, maxArity, args, returnedType, requiredContextValueType,
                    requiresPosition, requiresLength, body, analysis));
            namespaceMapping.put(functionName, functionList);
            return null;
        }
        final List<FunctionSpecification> alreadyRegistered = namespaceMapping.get(functionName);
        final var noOverlapping = alreadyRegistered.stream().noneMatch(f ->
            minArity <= f.maxArity && f.minArity <= maxArity
        );

        if (!noOverlapping) {
            return XQuerySemanticError.FunctionNameArityConflict;
        }
        alreadyRegistered.add(new FunctionSpecification(minArity, maxArity, args, returnedType, requiredContextValueType,
                requiresPosition, requiresLength, body, analysis));
        return null;
    }
}
