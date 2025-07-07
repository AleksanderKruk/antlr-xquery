package com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.defaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.tree.ParseTree;
import com.github.akruk.antlrxquery.AntlrXqueryLexer;
import com.github.akruk.antlrxquery.AntlrXqueryParser;
import com.github.akruk.antlrxquery.AntlrXqueryParser.ParenthesizedExprContext;
import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticError;
import com.github.akruk.antlrxquery.semanticanalyzer.XQueryVisitingSemanticContext;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.IXQuerySemanticFunctionManager;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQuerySemanticFunctionManager implements IXQuerySemanticFunctionManager {

    private static final ParseTree CONTEXT_VALUE = getTree(".", parser -> parser.contextItemExpr());
    private static final ParseTree DEFAULT_COLLATION = getTree("fn:default-collation()", parser->parser.functionCall());

    public interface XQuerySemanticFunction {
        public CallAnalysisResult call(final XQueryTypeFactory typeFactory,
                final XQueryVisitingSemanticContext context,
                final List<XQuerySequenceType> types);
    }
    private final XQueryTypeFactory typeFactory;

    public XQuerySemanticFunctionManager(final XQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
        this.namespaces = new HashMap<>(6);

        final XQuerySequenceType optionalString = typeFactory.zeroOrOne(typeFactory.itemString());

        register("fn", "true", List.of(), typeFactory.boolean_());
        register("fn", "false", List.of(), typeFactory.boolean_());
        register("fn", "position", List.of(), typeFactory.number());
        register("fn", "last", List.of(), typeFactory.number());


        final ParseTree defaultRoundingMode = getTree("'half-to-ceiling'", parser->parser.literal());
        final ParseTree zeroLiteral = getTree("0", parser->parser.literal());

        final XQuerySequenceType zeroOrMoreItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        final ArgumentSpecification argItems = new ArgumentSpecification("input", zeroOrMoreItems, null);
        register("fn", "not", List.of(argItems), typeFactory.boolean_());

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
                                        defaultRoundingMode);
        final ArgumentSpecification precision = new ArgumentSpecification("precision", optionalNumber, zeroLiteral);

        // fn:abs(
        // $value	as xs:numeric?
        // ) as xs:numeric?
        register("fn", "abs", List.of(valueNum), optionalNumber);

        // fn:ceiling(
        // $value	as xs:numeric?
        // ) as xs:numeric?
        register("fn", "ceiling", List.of(valueNum), optionalNumber);


        // fn:floor(
        // $value	as xs:numeric?
        // ) as xs:numeric?
        register("fn", "floor", List.of(valueNum), optionalNumber);


        // fn:round(
        // $value	as xs:numeric?,
        // $precision	as xs:integer?	:= 0,
        // $mode	as enum('floor',
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

        // final ArgumentSpecification sequence = new ArgumentSpecification("input", zeroOrMoreItems, null);
        // final ArgumentSpecification position = new ArgumentSpecification("position", typeFactory.number()), null);

        // final var optionalItem = typeFactory.zeroOrOne(typeFactory.itemAnyItem());

        // register("fn", "empty", List.of(sequence), typeFactory.boolean_());
        // register("fn", "exists", List.of(sequence), typeFactory.boolean_());
        // register("fn", "head", List.of(sequence), optionalItem);
        // register("fn", "tail", List.of(sequence), zeroOrMoreItems);

        // final ArgumentSpecification insert = new ArgumentSpecification("insert", zeroOrMoreItems, null);

        // register("fn", "insert-before",
        //         List.of(sequence, position, insert),
        //         zeroOrMoreItems);

        // final var zeroOrMoreNumbers = typeFactory.zeroOrMore(typeFactory.itemNumber());

        // final ArgumentSpecification positions = new ArgumentSpecification("positions", zeroOrMoreNumbers, null);

        // register("fn", "remove",
        //         List.of(sequence, positions), zeroOrMoreItems);

        // register("fn", "reverse",
        //         List.of(sequence), zeroOrMoreItems);

        // final var start = new ArgumentSpecification("start", typeFactory.number(), null);
        // final var optionalLength = new ArgumentSpecification("length", false, optionalNumber);
        // register("fn", "subsequence",
        //         List.of(sequence, start, optionalLength), zeroOrMoreItems);

        // final ArgumentSpecification optionalStringValue = new ArgumentSpecification("value", optionalString), null);
        // register("fn", "substring",
        //         List.of(optionalStringValue, start, optionalLength),
        //         typeFactory.string());

        // final ArgumentSpecification optionalSubstring = new ArgumentSpecification("substring", optionalString), null);

        // // TODO: collation
        // register("fn", "contains",
        //         List.of(optionalStringValue, optionalSubstring),
        //         typeFactory.boolean_());

        // // TODO: collation
        // register("fn", "starts-with",
        //         List.of(optionalStringValue, optionalSubstring),
        //         typeFactory.boolean_());

        // // TODO: collation
        // register("fn", "ends-with",
        //         List.of(optionalStringValue, optionalSubstring),
        //         typeFactory.boolean_());

        // // TODO: collation
        // register("fn", "substring-before",
        //         List.of(optionalStringValue, optionalSubstring),
        //         typeFactory.string());

        // // TODO: collation
        // register("fn", "substring-after",
        //         List.of(optionalStringValue, optionalSubstring),
        //         typeFactory.string());

        // final var normalizedValue = new ArgumentSpecification("value", false, typeFactory.string());

        // register("fn", "normalize-space",
        //         List.of(normalizedValue), typeFactory.string());

        // register("fn", "upper-case", List.of(optionalStringValue), typeFactory.string());
        // register("fn", "lower-case", List.of(optionalStringValue), typeFactory.string());

        // // fn:replace(
        // //  as xs:string? := (),
        // //  as xs:string,
        // //  as (xs:string | fn(xs:untypedAtomic, xs:untypedAtomic*) as
        // // item()?)? := (),
        // //  as xs:string? := ''
        // // ) as xs:string
        // final ArgumentSpecification replaceValue = new ArgumentSpecification("value", false, optionalString));
        // final ArgumentSpecification replacePattern = new ArgumentSpecification("pattern", typeFactory.string(), null);
        // final ArgumentSpecification replacement = new ArgumentSpecification("replacement", false,
        //         // string or function – approximate as any item?
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        // final ArgumentSpecification replaceFlags = new ArgumentSpecification("flags", false,
        //         optionalString));
        // register("fn", "replace",
        //         List.of(replaceValue, replacePattern, replacement, replaceFlags),
        //         typeFactory.string());

        // fn:string(
        //  as item()? := .
        // ) as xs:string
        final ArgumentSpecification stringValue = new ArgumentSpecification("value", typeFactory.zeroOrOne(typeFactory.itemAnyItem()), CONTEXT_VALUE);
        register("fn", "string",
                List.of(stringValue),
                typeFactory.string());

        // fn:zero-or-one(
        //  as item()*
        // ) as item()?
        final ArgumentSpecification anyItemsRequiredInput = new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null);
        register("fn", "zero-or-one",
                List.of(anyItemsRequiredInput),
                typeFactory.zeroOrOne(typeFactory.itemAnyItem()));

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

        // // fn:data(
        // //  as item()* := .
        // // ) as xs:anyAtomicType*
        // final ArgumentSpecification dataInput = new ArgumentSpecification("input", false,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("fn", "data",
        //         List.of(dataInput),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:distinct-values(
        // //  as xs:anyAtomicType*,
        // //  as xs:string? := fn:default-collation()
        // // ) as xs:anyAtomicType*
        // final ArgumentSpecification distinctValues = new ArgumentSpecification("values", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification collation = new ArgumentSpecification("collation", false,
        //         optionalString));
        // register("fn", "distinct-values",
        //         List.of(distinctValues, collation),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // op:numeric-multiply(
        // //  as xs:numeric,
        // //  as xs:numeric
        // // ) as xs:numeric
        // final ArgumentSpecification nmArg1 = new ArgumentSpecification("arg1", true,
        //         typeFactory.number()));
        // final ArgumentSpecification nmArg2 = new ArgumentSpecification("arg2", true,
        //         typeFactory.number()));
        // register("op", "numeric-multiply",
        //         List.of(nmArg1, nmArg2),
        //         typeFactory.number()));

        // // 2) fn:median(
        // //  as xs:double*
        // // ) as xs:double?
        // final ArgumentSpecification medianArg = new ArgumentSpecification("arg", true,
        //         typeFactory.zeroOrMore(typeFactory.itemNumber()));
        // register("fn", "median",
        //         List.of(medianArg),
        //         typeFactory.zeroOrOne(typeFactory.itemNumber()));

        // // 3) fn:node-name(
        // //  as node()? := .
        // // ) as xs:QName?
        // final ArgumentSpecification nodeNameNode = new ArgumentSpecification("node", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyNode()));
        // register("fn", "node-name",
        //         List.of(nodeNameNode),
        //         optionalString));

        // // 4) fn:nilled(
        // //  as node()? := .
        // // ) as xs:boolean?
        // final ArgumentSpecification nilledNode = new ArgumentSpecification("node", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyNode()));
        // register("fn", "nilled",
        //         List.of(nilledNode),
        //         typeFactory.zeroOrOne(typeFactory.itemBoolean()));

        // // 5) fn:base-uri(
        // //  as node()? := .
        // // ) as xs:anyURI?
        // final ArgumentSpecification baseUriNode = new ArgumentSpecification("node", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        // register("fn", "base-uri",
        //         List.of(baseUriNode),
        //         optionalString));

        // // 6) fn:document-uri(
        // //  as node()? := .
        // // ) as xs:anyURI?
        // final ArgumentSpecification docUriNode = new ArgumentSpecification("node", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyNode()));
        // register("fn", "document-uri",
        //         List.of(docUriNode),
        //         optionalString));

        // // 7) fn:name(
        // //  as node()? := .
        // // ) as xs:string
        // final ArgumentSpecification nameNode = new ArgumentSpecification("node", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyNode()));
        // register("fn", "name",
        //         List.of(nameNode),
        //         typeFactory.string());

        // // 8) fn:local-name(
        // //  as node()? := .
        // // ) as xs:string
        // final ArgumentSpecification localNameNode = new ArgumentSpecification("node", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyNode()));
        // register("fn", "local-name",
        //         List.of(localNameNode),
        //         typeFactory.string());

        // // 9) fn:namespace-uri(
        // //  as node()? := .
        // // ) as xs:anyURI
        // final ArgumentSpecification nsUriNode = new ArgumentSpecification("node", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyNode()));
        // register("fn", "namespace-uri",
        //         List.of(nsUriNode),
        //         typeFactory.string());

        // // 10) fn:lang(
        // //  as xs:string?,
        // //  as node() := .
        // // ) as xs:boolean
        // final ArgumentSpecification langValue = new ArgumentSpecification("language", false,
        //         optionalString));
        // final ArgumentSpecification langNode = new ArgumentSpecification("node", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyNode()));
        // register("fn", "lang",
        //         List.of(langValue, langNode),
        //         typeFactory.boolean_());

        // // 11) fn:root(
        // //  as node()? := .
        // // ) as node()?
        // final ArgumentSpecification rootNode = new ArgumentSpecification("node", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyNode()));
        // register("fn", "root",
        //         List.of(rootNode),
        //         typeFactory.zeroOrOne(typeFactory.itemAnyNode()));

        // // 12) fn:path(
        // //  as node()? := .
        // //  as map(*)? := {}
        // // ) as xs:string?
        // final ArgumentSpecification pathNode = new ArgumentSpecification("node", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyNode()));
        // final ArgumentSpecification pathOptions = new ArgumentSpecification("options", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyMap()));
        // register("fn", "path",
        //         List.of(pathNode, pathOptions),
        //         optionalString));

        // // fn:has-children(
        // //  as node()? := .
        // // ) as xs:boolean
        // final ArgumentSpecification hasChildrenNode = new ArgumentSpecification("node", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyNode()));
        // register("fn", "has-children",
        //         List.of(hasChildrenNode),
        //         typeFactory.boolean_());

        // // fn:siblings(
        // //  as node()? := .
        // // ) as node()*
        // final ArgumentSpecification siblingsNode = new ArgumentSpecification("node", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyNode()));
        // register("fn", "siblings",
        //         List.of(siblingsNode),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyNode()));

        // // fn:distinct-ordered-nodes(
        // //  as node()*
        // // ) as node()*
        // final ArgumentSpecification distinctOrderedInput = new ArgumentSpecification("nodes", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyNode()));
        // register("fn", "distinct-ordered-nodes",
        //         List.of(distinctOrderedInput),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyNode()));

        // // fn:innermost(
        // //  as node()*
        // // ) as node()*
        // final ArgumentSpecification innermostInput = new ArgumentSpecification("nodes", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyNode()));
        // register("fn", "innermost",
        //         List.of(innermostInput),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyNode()));

        // // fn:outermost(
        // //  as node()*
        // // ) as node()*
        // final ArgumentSpecification outermostInput = new ArgumentSpecification("nodes", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyNode()));
        // register("fn", "outermost",
        //         List.of(outermostInput),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyNode()));

        // // fn:error(
        // //  as xs:QName? := (),
        // //  as xs:string? := (),
        // //  as item()* := .
        // // ) as item()*
        // final ArgumentSpecification errorCode = new ArgumentSpecification("code", false,
        //         optionalString));
        // final ArgumentSpecification errorDescription = new ArgumentSpecification("description", false,
        //         optionalString));
        // final ArgumentSpecification errorValue = new ArgumentSpecification("value", false,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("fn", "error",
        //         List.of(errorCode, errorDescription, errorValue),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:trace(
        // //  as item()*,
        // //  as xs:string? := ()
        // // ) as item()*
        // final ArgumentSpecification traceInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification traceLabel = new ArgumentSpecification("label", false,
        //         optionalString));
        // register("fn", "trace",
        //         List.of(traceInput, traceLabel),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:message(
        // //  as item()*,
        // //  as xs:string? := ()
        // // ) as empty-sequence()
        // final ArgumentSpecification messageInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification messageLabel = new ArgumentSpecification("label", false,
        //         optionalString));
        // register("fn", "message",
        //         List.of(messageInput, messageLabel),
        //         typeFactory.emptySequence());

        // // op:numeric-add(
        // //  as xs:numeric,
        // //  as xs:numeric
        // // ) as xs:numeric
        // final ArgumentSpecification addArg1 = new ArgumentSpecification("arg1", true,
        //         typeFactory.number()));
        // final ArgumentSpecification addArg2 = new ArgumentSpecification("arg2", true,
        //         typeFactory.number()));
        // register("op", "numeric-add",
        //         List.of(addArg1, addArg2),
        //         typeFactory.number()));

        // // op:numeric-subtract(
        // //  as xs:numeric,
        // //  as xs:numeric
        // // ) as xs:numeric
        // final ArgumentSpecification subArg1 = new ArgumentSpecification("arg1", true,
        //         typeFactory.number()));
        // final ArgumentSpecification subArg2 = new ArgumentSpecification("arg2", true,
        //         typeFactory.number()));
        // register("op", "numeric-subtract",
        //         List.of(subArg1, subArg2),
        //         typeFactory.number()));

        // // op:numeric-multiply(
        // //  as xs:numeric,
        // //  as xs:numeric
        // // ) as xs:numeric
        // final ArgumentSpecification mulArg1 = new ArgumentSpecification("arg1", true,
        //         typeFactory.number()));
        // final ArgumentSpecification mulArg2 = new ArgumentSpecification("arg2", true,
        //         typeFactory.number()));
        // register("op", "numeric-multiply",
        //         List.of(mulArg1, mulArg2),
        //         typeFactory.number()));

        // // op:numeric-divide(
        // //  as xs:numeric,
        // //  as xs:numeric
        // // ) as xs:numeric
        // final ArgumentSpecification divArg1 = new ArgumentSpecification("arg1", true,
        //         typeFactory.number()));
        // final ArgumentSpecification divArg2 = new ArgumentSpecification("arg2", true,
        //         typeFactory.number()));
        // register("op", "numeric-divide",
        //         List.of(divArg1, divArg2),
        //         typeFactory.number()));

        // // op:numeric-integer-divide(
        // //  as xs:numeric,
        // //  as xs:numeric
        // // ) as xs:integer
        // final ArgumentSpecification idivArg1 = new ArgumentSpecification("arg1", true,
        //         typeFactory.number()));
        // final ArgumentSpecification idivArg2 = new ArgumentSpecification("arg2", true,
        //         typeFactory.number()));
        // register("op", "numeric-integer-divide",
        //         List.of(idivArg1, idivArg2),
        //         typeFactory.number());

        // // op:numeric-mod(
        // //  as xs:numeric,
        // //  as xs:numeric
        // // ) as xs:numeric
        // final ArgumentSpecification modArg1 = new ArgumentSpecification("arg1", true,
        //         typeFactory.number()));
        // final ArgumentSpecification modArg2 = new ArgumentSpecification("arg2", true,
        //         typeFactory.number()));
        // register("op", "numeric-mod",
        //         List.of(modArg1, modArg2),
        //         typeFactory.number()));

        // // op:numeric-unary-plus(
        // //  as xs:numeric
        // // ) as xs:numeric
        // final ArgumentSpecification upArg = new ArgumentSpecification("arg", true,
        //         typeFactory.number()));
        // register("op", "numeric-unary-plus",
        //         List.of(upArg),
        //         typeFactory.number()));

        // // op:numeric-unary-minus(
        // //  as xs:numeric
        // // ) as xs:numeric
        // final ArgumentSpecification umArg = new ArgumentSpecification("arg", true,
        //         typeFactory.number()));
        // register("op", "numeric-unary-minus",
        //         List.of(umArg),
        //         typeFactory.number()));

        // // fn:parse-integer(
        // //  as xs:string?,
        // //  as xs:integer? := 10
        // // ) as xs:integer?
        // final ArgumentSpecification parseIntValue = new ArgumentSpecification("value", false,
        //         optionalString));
        // final ArgumentSpecification parseIntRadix = new ArgumentSpecification("radix", false,
        //         typeFactory.zeroOrOne(typeFactory.itemNumber()));
        // register("fn", "parse-integer",
        //         List.of(parseIntValue, parseIntRadix),
        //         typeFactory.zeroOrOne(typeFactory.itemNumber()));

        // // fn:format-integer(
        // //  as xs:integer?,
        // //  as xs:string,
        // //  as xs:string? := ()
        // // ) as xs:string
        // final ArgumentSpecification fmtIntValue = new ArgumentSpecification("value", true,
        //         typeFactory.zeroOrOne(typeFactory.itemNumber()));
        // final ArgumentSpecification fmtIntPicture = new ArgumentSpecification("picture", true,
        //         typeFactory.string());
        // final ArgumentSpecification fmtIntLanguage = new ArgumentSpecification("language", false,
        //         optionalString));
        // register("fn", "format-integer",
        //         List.of(fmtIntValue, fmtIntPicture, fmtIntLanguage),
        //         typeFactory.string());

        // // fn:format-number(
        // //  as xs:numeric?,
        // //  as xs:string,
        // //  as (xs:string | map(*))? := ()
        // // ) as xs:string
        // final ArgumentSpecification fmtNumValue = new ArgumentSpecification("value", true,
        //         typeFactory.zeroOrOne(typeFactory.itemNumber()));
        // final ArgumentSpecification fmtNumPicture = new ArgumentSpecification("picture", true,
        //         typeFactory.string());
        // final ArgumentSpecification fmtNumOptions = new ArgumentSpecification("options", false,
        //         // approximate union of string or map(*) with any-item
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        // register("fn", "format-number",
        //         List.of(fmtNumValue, fmtNumPicture, fmtNumOptions),
        //         typeFactory.string());

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

        // // fn:codepoints-to-string(
        // //  as xs:integer*
        // // ) as xs:string
        // final ArgumentSpecification cpsValues = new ArgumentSpecification("values", true,
        //         typeFactory.zeroOrMore(typeFactory.itemNumber()));
        // register("fn", "codepoints-to-string",
        //         List.of(cpsValues),
        //         typeFactory.string());

        // // fn:string-to-codepoints(
        // //  as xs:string?
        // // ) as xs:integer*
        // final ArgumentSpecification stcpValue = new ArgumentSpecification("value", true,
        //         optionalString));
        // register("fn", "string-to-codepoints",
        //         List.of(stcpValue),
        //         typeFactory.zeroOrMore(typeFactory.itemNumber()));

        // // fn:codepoint-equal(
        // //  as xs:string?,
        // //  as xs:string?
        // // ) as xs:boolean?
        // final ArgumentSpecification cpEq1 = new ArgumentSpecification("value1", true,
        //         optionalString));
        // final ArgumentSpecification cpEq2 = new ArgumentSpecification("value2", true,
        //         optionalString));
        // register("fn", "codepoint-equal",
        //         List.of(cpEq1, cpEq2),
        //         typeFactory.zeroOrOne(typeFactory.itemBoolean()));

        // // fn:collation(
        // //  as map(*)
        // // ) as xs:string
        // final ArgumentSpecification collationOpts = new ArgumentSpecification("options", true,
        //         typeFactory.one(typeFactory.itemAnyMap()));
        // register("fn", "collation",
        //         List.of(collationOpts),
        //         typeFactory.string());

        // // fn:collation-available(
        // //  as xs:string,
        // //  as enum('compare','key','substring')* := ()
        // // ) as xs:boolean
        // final ArgumentSpecification colAvailColl = new ArgumentSpecification("collation", true,
        //         typeFactory.string());
        // final ArgumentSpecification colAvailUsage = new ArgumentSpecification("usage", false,
        //         typeFactory.zeroOrMore(typeFactory.itemEnum(Set.of("compare", "key", "substring"))));
        // register("fn", "collation-available",
        //         List.of(colAvailColl, colAvailUsage),
        //         typeFactory.boolean_());
        // // fn:contains-token(
        // //  as xs:string*,
        // //  as xs:string,
        // //  as xs:string? := fn:default-collation()
        // // ) as xs:boolean
        // final ArgumentSpecification ctValue = new ArgumentSpecification("value", true,
        //         typeFactory.zeroOrMore(typeFactory.itemString()));
        // final ArgumentSpecification ctToken = new ArgumentSpecification("token", true,
        //         typeFactory.string());
        // final ArgumentSpecification ctColl = new ArgumentSpecification("collation", false,
        //         optionalString));
        // register("fn", "contains-token",
        //         List.of(ctValue, ctToken, ctColl),
        //         typeFactory.boolean_());

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

        final ArgumentSpecification sjValues = new ArgumentSpecification("values", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null);
        final ArgumentSpecification sjSeparator = new ArgumentSpecification("separator", optionalString, EMPTY_STRING);
        register("fn", "string-join",
                List.of(sjValues, sjSeparator),
                typeFactory.string());

        final ParseTree EMPTY_LIST = getTree("()", p->p.parenthesizedExpr());

        // fn:concat(
        //  as xs:anyAtomicType* := ()
        // ) as xs:string
        final ArgumentSpecification concatValues = new ArgumentSpecification("values", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), EMPTY_LIST);
        register("fn", "concat",
                List.of(concatValues),
                typeFactory.string());

        // fn:string-join(
        //  as xs:anyAtomicType* := (),
        //  as xs:string? := ""
        // ) as xs:string
        final ArgumentSpecification joinValues = new ArgumentSpecification("values", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), EMPTY_LIST);
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

        // // fn:string-length(
        // //  as xs:string? := fn:string(.)
        // // ) as xs:integer
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
        final ParseTree nfc = XQuerySemanticFunctionManager.getTree("\"NFC\"", parser -> parser.literal());
        final ArgumentSpecification nuValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification nuForm = new ArgumentSpecification("form", optionalString, nfc);
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
        final ArgumentSpecification cColl = new ArgumentSpecification("collation", optionalString, DEFAULT_COLLATION);
        register("fn", "contains", List.of(cValue, cSubstr, cColl), typeFactory.boolean_());

        // fn:starts-with(
        //  as xs:string?,
        //  as xs:string?,
        //  as xs:string? := fn:default-collation()
        // ) as xs:boolean
        final ArgumentSpecification swValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification swSubstring = new ArgumentSpecification("substring", optionalString, null);
        final ArgumentSpecification swCollation = new ArgumentSpecification("collation", optionalString, DEFAULT_COLLATION);
        register("fn", "starts-with", List.of(swValue, swSubstring, swCollation), typeFactory.boolean_());

        // fn:ends-with(
        //  as xs:string?,
        //  as xs:string?,
        //  as xs:string? := fn:default-collation()
        // ) as xs:boolean
        final ArgumentSpecification ewValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification ewSubstring = new ArgumentSpecification("substring", optionalString, null);
        final ArgumentSpecification ewCollation = new ArgumentSpecification("collation", optionalString, DEFAULT_COLLATION);
        register("fn", "ends-with", List.of(ewValue, ewSubstring, ewCollation), typeFactory.boolean_());

        // fn:substring-before(
        //  as xs:string?,
        //  as xs:string?,
        //  as xs:string? := fn:default-collation()
        // ) as xs:string
        final ArgumentSpecification sbValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification sbSubstring = new ArgumentSpecification("substring", optionalString, null);
        final ArgumentSpecification sbCollation = new ArgumentSpecification("collation", optionalString, DEFAULT_COLLATION);
        register("fn", "substring-before", List.of(sbValue, sbSubstring, sbCollation), typeFactory.string());

        // fn:substring-after(
        //  as xs:string?,
        //  as xs:string?,
        //  as xs:string? := fn:default-collation()
        // ) as xs:string
        final ArgumentSpecification saValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification saSubstring = new ArgumentSpecification("substring", optionalString, null);
        final ArgumentSpecification saCollation = new ArgumentSpecification("collation", optionalString, DEFAULT_COLLATION);
        register("fn", "substring-after",
                List.of(saValue, saSubstring, saCollation),
                typeFactory.string());

        // // fn:matches(
        // //  as xs:string?,
        // //  as xs:string,
        // //  as xs:string? := ""
        // // ) as xs:boolean
        // final ArgumentSpecification mValue = new ArgumentSpecification("value", true,
        //         optionalString));
        // final ArgumentSpecification mPattern = new ArgumentSpecification("pattern", true,
        //         typeFactory.string());
        // final ArgumentSpecification mFlags = new ArgumentSpecification("flags", false,
        //         optionalString));
        // register("fn", "matches",
        //         List.of(mValue, mPattern, mFlags),
        //         typeFactory.boolean_());

        // // fn:replace(
        // //  as xs:string?,
        // //  as xs:string,
        // //  as (xs:string | fn(...))? := (),
        // //  as xs:string? := ''
        // // ) as xs:string
        // final ArgumentSpecification rValue = new ArgumentSpecification("value", true,
        //         optionalString));
        // final ArgumentSpecification rPattern = new ArgumentSpecification("pattern", true,
        //         typeFactory.string());
        // final ArgumentSpecification rReplacement = new ArgumentSpecification("replacement", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        // final ArgumentSpecification rFlags = new ArgumentSpecification("flags", false,
        //         optionalString));
        // register("fn", "replace",
        //         List.of(rValue, rPattern, rReplacement, rFlags),
        //         typeFactory.string());

        // // fn:tokenize(
        // //  as xs:string?,
        // //  as xs:string? := (),
        // //  as xs:string? := ""
        // // ) as xs:string*
        // final ArgumentSpecification tValue = new ArgumentSpecification("value", true,
        //         optionalString));
        // final ArgumentSpecification tPattern = new ArgumentSpecification("pattern", false,
        //         optionalString));
        // final ArgumentSpecification tFlags2 = new ArgumentSpecification("flags", false,
        //         optionalString));
        // register("fn", "tokenize",
        //         List.of(tValue, tPattern, tFlags2),
        //         typeFactory.zeroOrMore(typeFactory.itemString()));

        // // fn:analyze-string(
        // //  as xs:string?,
        // //  as xs:string,
        // //  as xs:string? := ""
        // // ) as element(fn:analyze-string-result)
        // final ArgumentSpecification aValue = new ArgumentSpecification("value", true,
        //         optionalString));
        // final ArgumentSpecification aPattern = new ArgumentSpecification("pattern", true,
        //         typeFactory.string());
        // final ArgumentSpecification aFlags = new ArgumentSpecification("flags", false,
        //         optionalString));
        // register("fn", "analyze-string",
        //         List.of(aValue, aPattern, aFlags),
        //         typeFactory.one(typeFactory.itemElement(Set.of("fn:analyze-string-result"))));

        // // fn:true() as xs:boolean
        // register("fn", "true",
        //         List.of(),
        //         typeFactory.boolean_());

        // // fn:false() as xs:boolean
        // register("fn", "false",
        //         List.of(),
        //         typeFactory.boolean_());

        // // op:boolean-equal( as xs:boolean,  as xs:boolean) as xs:boolean
        // final ArgumentSpecification boolEq1 = new ArgumentSpecification("value1", true,
        //         typeFactory.boolean_();
        // final ArgumentSpecification boolEq2 = new ArgumentSpecification("value2", true,
        //         typeFactory.boolean_();
        // register("op", "boolean-equal",
        //         List.of(boolEq1, boolEq2),
        //         typeFactory.boolean_());

        // // op:boolean-less-than( as xs:boolean,  as xs:boolean) as xs:boolean
        // final ArgumentSpecification boolLt1 = new ArgumentSpecification("arg1", true,
        //         typeFactory.boolean_();
        // final ArgumentSpecification boolLt2 = new ArgumentSpecification("arg2", true,
        //         typeFactory.boolean_();
        // register("op", "boolean-less-than",
        //         List.of(boolLt1, boolLt2),
        //         typeFactory.boolean_());

        // // fn:boolean( as item()*) as xs:boolean
        // final ArgumentSpecification booleanInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("fn", "boolean",
        //         List.of(booleanInput),
        //         typeFactory.boolean_());

        // // fn:not( as item()*) as xs:boolean
        // final ArgumentSpecification notInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("fn", "not",
        //         List.of(notInput),
        //         typeFactory.boolean_());

        // // common input spec
        // final ArgumentSpecification input = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:empty( as item()*) as xs:boolean
        // register("fn", "empty",
        //         List.of(input),
        //         typeFactory.boolean_());

        // // fn:exists( as item()*) as xs:boolean
        // register("fn", "exists",
        //         List.of(input),
        //         typeFactory.boolean_());

        // // fn:foot( as item()*) as item()?
        // register("fn", "foot",
        //         List.of(input),
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));

        // // fn:head( as item()*) as item()?
        // register("fn", "head",
        //         List.of(input),
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));

        // // fn:identity( as item()*) as item()*
        // register("fn", "identity",
        //         List.of(input),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:items-at( as item()*,  as xs:integer*) as item()*
        // final ArgumentSpecification at = new ArgumentSpecification("at", true,
        //         typeFactory.zeroOrMore(typeFactory.itemNumber()));
        // register("fn", "items-at",
        //         List.of(input, at),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:replicate( as item()*,  as xs:nonNegativeInteger) as item()*
        // final ArgumentSpecification count = new ArgumentSpecification("count", true,
        //         typeFactory.number()));
        // register("fn", "replicate",
        //         List.of(input, count),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:reverse( as item()*) as item()*
        // register("fn", "reverse",
        //         List.of(input),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:sequence-join( as item()*,  as item()*) as item()*
        // final ArgumentSpecification seqJoinInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification seqJoinSeparator = new ArgumentSpecification("separator", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("fn", "sequence-join",
        //         List.of(seqJoinInput, seqJoinSeparator),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:slice( as item()*,  as xs:integer? := (),  as xs:integer?
        // // := (),  as xs:integer? := ()) as item()*
        // final ArgumentSpecification sliceInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification sliceStart = new ArgumentSpecification("start", false,
        //         typeFactory.zeroOrOne(typeFactory.itemNumber()));
        // final ArgumentSpecification sliceEnd = new ArgumentSpecification("end", false,
        //         typeFactory.zeroOrOne(typeFactory.itemNumber()));
        // final ArgumentSpecification sliceStep = new ArgumentSpecification("step", false,
        //         typeFactory.zeroOrOne(typeFactory.itemNumber()));
        // register("fn", "slice",
        //         List.of(sliceInput, sliceStart, sliceEnd, sliceStep),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:subsequence( as item()*,  as xs:double,  as xs:double?
        // // := ()) as item()*
        // final ArgumentSpecification subInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification subStart = new ArgumentSpecification("start", true,
        //         typeFactory.number()));
        // final ArgumentSpecification subLength = new ArgumentSpecification("length", false,
        //         typeFactory.zeroOrOne(typeFactory.itemNumber()));
        // register("fn", "subsequence",
        //         List.of(subInput, subStart, subLength),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:tail( as item()*) as item()*
        // final ArgumentSpecification tailInput2 = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("fn", "tail",
        //         List.of(tailInput2),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:trunk( as item()*) as item()*
        // final ArgumentSpecification trunkInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("fn", "trunk",
        //         List.of(trunkInput),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:unordered( as item()*) as item()*
        // final ArgumentSpecification unorderedInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("fn", "unordered",
        //         List.of(unorderedInput),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:void( as item()* := ()) as empty-sequence()
        // final ArgumentSpecification voidInput = new ArgumentSpecification("input", false,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("fn", "void",
        //         List.of(voidInput),
        //         typeFactory.emptySequence());

        // // fn:atomic-equal( as xs:anyAtomicType,  as xs:anyAtomicType) as
        // // xs:boolean
        // final ArgumentSpecification atomicEq1 = new ArgumentSpecification("value1", true,
        //         typeFactory.one(typeFactory.itemAnyItem()));
        // final ArgumentSpecification atomicEq2 = new ArgumentSpecification("value2", true,
        //         typeFactory.one(typeFactory.itemAnyItem()));
        // register("fn", "atomic-equal",
        //         List.of(atomicEq1, atomicEq2),
        //         typeFactory.boolean_());

        // // fn:deep-equal( as item()*,  as item()*,  as
        // // (xs:string|map(*))? := {}) as xs:boolean
        // final ArgumentSpecification deepInput1 = new ArgumentSpecification("input1", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification deepInput2 = new ArgumentSpecification("input2", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification deepOptions = new ArgumentSpecification("options", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        // register("fn", "deep-equal",
        //         List.of(deepInput1, deepInput2, deepOptions),
        //         typeFactory.boolean_());

        // // fn:compare( as xs:anyAtomicType?,  as xs:anyAtomicType?,
        // //  as xs:string? := fn:default-collation()) as xs:integer?
        // final ArgumentSpecification compareValue1 = new ArgumentSpecification("value1", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        // final ArgumentSpecification compareValue2 = new ArgumentSpecification("value2", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        // final ArgumentSpecification compareCollation = new ArgumentSpecification("collation", false,
        //         optionalString));
        // register("fn", "compare",
        //         List.of(compareValue1, compareValue2, compareCollation),
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        // // fn:distinct-values(
        // //  as xs:anyAtomicType*,
        // //  as xs:string? := fn:default-collation()
        // // ) as xs:anyAtomicType*
        // final ArgumentSpecification distinctVals = new ArgumentSpecification("values", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification distinctColl = new ArgumentSpecification("collation", false,
        //         optionalString));
        // register("fn", "distinct-values",
        //         List.of(distinctVals, distinctColl),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:duplicate-values(
        // //  as xs:anyAtomicType*,
        // //  as xs:string? := fn:default-collation()
        // // ) as xs:anyAtomicType*
        // final ArgumentSpecification duplicateVals = new ArgumentSpecification("values", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification duplicateColl = new ArgumentSpecification("collation", false,
        //         optionalString));
        // register("fn", "duplicate-values",
        //         List.of(duplicateVals, duplicateColl),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:index-of(
        // //  as xs:anyAtomicType*,
        // //  as xs:anyAtomicType,
        // //  as xs:string? := fn:default-collation()
        // // ) as xs:integer*
        // final ArgumentSpecification indexInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification indexTarget = new ArgumentSpecification("target", true,
        //         typeFactory.one(typeFactory.itemAnyItem()));
        // final ArgumentSpecification indexColl = new ArgumentSpecification("collation", false,
        //         optionalString));
        // register("fn", "index-of",
        //         List.of(indexInput, indexTarget, indexColl),
        //         typeFactory.zeroOrMore(typeFactory.itemNumber()));

        // // fn:starts-with-subsequence(
        // //  as item()*,
        // //  as item()*,
        // //  as (fn(item(),item()) as xs:boolean?)? := fn:deep-equal#2
        // // ) as xs:boolean
        // final ArgumentSpecification swsInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification swsSubseq = new ArgumentSpecification("subsequence", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification swsCompare = new ArgumentSpecification("compare", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        // register("fn", "starts-with-subsequence",
        //         List.of(swsInput, swsSubseq, swsCompare),
        //         typeFactory.boolean_());

        // // fn:ends-with-subsequence(
        // //  as item()*,
        // //  as item()*,
        // //  as (fn(item(),item()) as xs:boolean?)? := fn:deep-equal#2
        // // ) as xs:boolean
        // final ArgumentSpecification ewsInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification ewsSubseq = new ArgumentSpecification("subsequence", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification ewsCompare = new ArgumentSpecification("compare", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        // register("fn", "ends-with-subsequence",
        //         List.of(ewsInput, ewsSubseq, ewsCompare),
        //         typeFactory.boolean_());

        // // fn:contains-subsequence(
        // //  as item()*,
        // //  as item()*,
        // //  as (fn(item(),item()) as xs:boolean?)? := fn:deep-equal#2
        // // ) as xs:boolean
        // final ArgumentSpecification cssInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification cssSubseq = new ArgumentSpecification("subsequence", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification cssCompare = new ArgumentSpecification("compare", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        // register("fn", "contains-subsequence",
        //         List.of(cssInput, cssSubseq, cssCompare),
        //         typeFactory.boolean_());

        // // fn:zero-or-one( as item()*) as item()?
        // final ArgumentSpecification z1Input2 = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("fn", "zero-or-one",
        //         List.of(z1Input2),
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));

        // // fn:one-or-more( as item()*) as item()+
        // final ArgumentSpecification o1Input2 = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("fn", "one-or-more",
        //         List.of(o1Input2),
        //         typeFactory.oneOrMore(typeFactory.itemAnyItem()));

        // // fn:exactly-one( as item()*) as item()
        // final ArgumentSpecification e1Input2 = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("fn", "exactly-one",
        //         List.of(e1Input2),
        //         typeFactory.one(typeFactory.itemAnyItem()));

        // // fn:count( as item()*) as xs:integer
        // final ArgumentSpecification countInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("fn", "count",
        //         List.of(countInput),
        //         typeFactory.number()));

        // // fn:avg( as xs:anyAtomicType*) as xs:anyAtomicType?
        // final ArgumentSpecification avgValues = new ArgumentSpecification("values", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("fn", "avg",
        //         List.of(avgValues),
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));

        // // fn:max(
        // //  as xs:anyAtomicType*,
        // //  as xs:string? := fn:default-collation()
        // // ) as xs:anyAtomicType?
        // final ArgumentSpecification maxValues = new ArgumentSpecification("values", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification maxColl = new ArgumentSpecification("collation", false,
        //         optionalString));
        // register("fn", "max",
        //         List.of(maxValues, maxColl),
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));

        // // fn:min(
        // //  as xs:anyAtomicType*,
        // //  as xs:string? := fn:default-collation()
        // // ) as xs:anyAtomicType?
        // final ArgumentSpecification minValues = new ArgumentSpecification("values", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification minColl = new ArgumentSpecification("collation", false,
        //         optionalString));
        // register("fn", "min",
        //         List.of(minValues, minColl),
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));

        // // fn:sum(
        // //  as xs:anyAtomicType*,
        // //  as xs:anyAtomicType? := 0
        // // ) as xs:anyAtomicType?
        // final ArgumentSpecification sumValues = new ArgumentSpecification("values", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification sumZero = new ArgumentSpecification("zero", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        // register("fn", "sum",
        //         List.of(sumValues, sumZero),
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));

        // // fn:all-equal(
        // //  as xs:anyAtomicType*,
        // //  as xs:string? := fn:default-collation()
        // // ) as xs:boolean
        // final ArgumentSpecification allEqualValues = new ArgumentSpecification("values", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification allEqualCollation = new ArgumentSpecification("collation", false,
        //         optionalString));
        // register("fn", "all-equal",
        //         List.of(allEqualValues, allEqualCollation),
        //         typeFactory.boolean_());

        // // // fn:all-different(
        // // //  as xs:anyAtomicType*,
        // // //  as xs:string? := fn:default-collation()
        // // // ) as xs:boolean
        // // final ArgumentSpecification allDiffValues = new ArgumentSpecification("values", true,
        // //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // // final ArgumentSpecification allDiffCollation = new ArgumentSpecification("collation", false,
        // //         optionalString));
        // // register("fn", "all-different",
        // //         List.of(allDiffValues, allDiffCollation),
        // //         typeFactory.boolean_());

        // // // fn:collection(
        // // //  as xs:string? := ()
        // // // ) as item()*
        // // final ArgumentSpecification colSource = new ArgumentSpecification("source", false,
        // //         optionalString));
        // // register("fn", "collection",
        // //         List.of(colSource),
        // //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:unparsed-text(
        // //  as xs:string?,
        // //  as (xs:string|map(*))? := ()
        // // ) as xs:string?
        // final ArgumentSpecification utSource = new ArgumentSpecification("source", false,
        //         optionalString));
        // final ArgumentSpecification utOptions = new ArgumentSpecification("options", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        // register("fn", "unparsed-text",
        //         List.of(utSource, utOptions),
        //         optionalString));

        // // fn:unparsed-text-lines(
        // //  as xs:string?,
        // //  as (xs:string|map(*))? := ()
        // // ) as xs:string*
        // final ArgumentSpecification utlSource = new ArgumentSpecification("source", false,
        //         optionalString));
        // final ArgumentSpecification utlOptions = new ArgumentSpecification("options", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        // register("fn", "unparsed-text-lines",
        //         List.of(utlSource, utlOptions),
        //         typeFactory.zeroOrMore(typeFactory.itemString()));

        // // fn:unparsed-text-available(
        // //  as xs:string?,
        // //  as (xs:string|map(*))? := ()
        // // ) as xs:boolean
        // final ArgumentSpecification utaSource = new ArgumentSpecification("source", false,
        //         optionalString));
        // final ArgumentSpecification utaOptions = new ArgumentSpecification("options", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        // register("fn", "unparsed-text-available",
        //         List.of(utaSource, utaOptions),
        //         typeFactory.boolean_());

        // // fn:environment-variable(
        // //  as xs:string
        // // ) as xs:string?
        // final ArgumentSpecification envName = new ArgumentSpecification("name", true,
        //         typeFactory.string());
        // register("fn", "environment-variable",
        //         List.of(envName),
        //         optionalString));

        // // fn:available-environment-variables() as xs:string*
        // register("fn", "available-environment-variables",
        //         List.of(),
        //         typeFactory.zeroOrMore(typeFactory.itemString()));

        // // fn:position() as xs:integer
        // register("fn", "position",
        //         List.of(),
        //         typeFactory.number()));

        // // fn:last() as xs:integer
        // register("fn", "last",
        //         List.of(),
        //         typeFactory.number()));

        // // fn:function-lookup(
        // //  as xs:QName,
        // //  as xs:integer
        // // ) as fn(*)?
        // final ArgumentSpecification lookupName = new ArgumentSpecification("name", true, typeFactory.string());
        // final ArgumentSpecification lookupArity = new ArgumentSpecification("arity", true, typeFactory.number());
        // register("fn", "function-lookup",
        //         List.of(lookupName, lookupArity), typeFactory.zeroOrOne(typeFactory.itemAnyFunction()));

        // // fn:function-name(
        // //  as fn(*)
        // // ) as xs:QName?
        // final ArgumentSpecification fnNameArg = new ArgumentSpecification("function", true, typeFactory.anyFunction());
        // register("fn", "function-name",
        //         List.of(fnNameArg),
        //         optionalString));

        // // fn:function-arity(
        // //  as fn(*)
        // // ) as xs:integer
        // final ArgumentSpecification fnArityArg = new ArgumentSpecification("function", true, typeFactory.anyFunction());
        // register("fn", "function-arity",
        //         List.of(fnArityArg), typeFactory.number());
        // // fn:function-identity(
        // //  as fn(*)
        // // ) as xs:string
        // final ArgumentSpecification functionIdentityFn = new ArgumentSpecification("function", true,
        //         typeFactory.anyFunction());
        // register("fn", "function-identity",
        //         List.of(functionIdentityFn), typeFactory.string());

        // // fn:apply(
        // //  as fn(*),
        // //  as array(*)
        // // ) as item()*
        // final ArgumentSpecification applyFn = new ArgumentSpecification("function", true, typeFactory.anyFunction());
        // final ArgumentSpecification applyArgs = new ArgumentSpecification("arguments", true, typeFactory.anyArray());
        // register("fn", "apply",
        //         List.of(applyFn, applyArgs),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:do-until(
        // //  as item()*,
        // //  as fn(item()*, xs:integer) as item()*,
        // //  as fn(item()*, xs:integer) as xs:boolean?
        // // ) as item()*
        // final var predicateItem = typeFactory.itemFunction(typeFactory.zeroOrOne(typeFactory.itemBoolean()),
        //         List.of(zeroOrMoreItems, typeFactory.number()));

        // final ArgumentSpecification doUntilInput = new ArgumentSpecification("input", true, zeroOrMoreItems);
        // final ArgumentSpecification doUntilAction = new ArgumentSpecification("action", true,
        //         typeFactory.one(typeFactory.itemFunction(zeroOrMoreItems,
        //                 List.of(zeroOrMoreItems, typeFactory.number()))));
        // final ArgumentSpecification doUntilPredicate = new ArgumentSpecification("predicate", true,
        //         typeFactory.one(predicateItem));
        // register("fn", "do-until",
        //         List.of(doUntilInput, doUntilAction, doUntilPredicate),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:every(
        // //  as item()*,
        // //  as fn(item(), xs:integer) as xs:boolean? := fn:boolean#1
        // // ) as xs:boolean
        // final ArgumentSpecification everyInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification everyPredicate = new ArgumentSpecification("predicate", false,
        //         typeFactory.zeroOrOne(predicateItem));
        // register("fn", "every",
        //         List.of(everyInput, everyPredicate),
        //         typeFactory.boolean_());

        // // fn:filter(
        // //  as item()*,
        // //  as fn(item(), xs:integer) as xs:boolean?
        // // ) as item()*
        // final ArgumentSpecification filterInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification filterPredicate = new ArgumentSpecification("predicate", true,
        //         typeFactory.one(predicateItem));
        // register("fn", "filter",
        //         List.of(filterInput, filterPredicate),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final var leftActionItem = typeFactory.itemFunction(zeroOrMoreItems,
        //         List.of(zeroOrMoreItems, typeFactory.anyItem()));

        // // fn:fold-left(
        // //  as item()*,
        // //  as item()*,
        // //  as fn(item()*, item()) as item()*
        // // ) as item()*
        // final ArgumentSpecification foldLeftInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification foldLeftInit = new ArgumentSpecification("init", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification foldLeftAction = new ArgumentSpecification("action", true,
        //         typeFactory.one(leftActionItem));
        // register("fn", "fold-left",
        //         List.of(foldLeftInput, foldLeftInit, foldLeftAction),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // // fn:fold-right(
        // //  as item()*,
        // //  as item()*,
        // //  as fn(item(), item()*) as item()*
        // // ) as item()*
        // final ArgumentSpecification frInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification frInit = new ArgumentSpecification("init", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification frAction = new ArgumentSpecification("action", true,
        //         typeFactory.one(predicateItem));
        // register("fn", "fold-right",
        //         List.of(frInput, frInit, frAction),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // final var rightActionItem = typeFactory.itemFunction(zeroOrMoreItems,
        //         List.of(typeFactory.anyItem(), zeroOrMoreItems));
        // // fn:for-each(
        // //  as item()*,
        // //  as fn(item(), xs:integer) as item()*
        // // ) as item()*
        // final ArgumentSpecification feInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification feAction = new ArgumentSpecification("action", true,
        //         typeFactory.one(rightActionItem));
        // register("fn", "for-each",
        //         List.of(feInput, feAction),
        //         zeroOrMoreItems);

        // // fn:for-each-pair(
        // //  as item()*,
        // //  as item()*,
        // //  as fn(item(), item(), xs:integer) as item()*
        // // ) as item()*
        // final ArgumentSpecification fepInput1 = new ArgumentSpecification("input1", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification fepInput2 = new ArgumentSpecification("input2", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification fepAction = new ArgumentSpecification("action", true,
        //         typeFactory.one(predicateItem));
        // register("fn", "for-each-pair",
        //         List.of(fepInput1, fepInput2, fepAction),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:highest(
        // //  as item()*,
        // //  as xs:string? := fn:default-collation(),
        // //  as (fn(item()) as xs:anyAtomicType*)? := fn:data#1
        // // ) as item()*
        // final ArgumentSpecification hiInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification hiColl = new ArgumentSpecification("collation", false,
        //         optionalString));
        // final ArgumentSpecification hiKey = new ArgumentSpecification("key", false,
        //         typeFactory.zeroOrOne(predicateItem));
        // register("fn", "highest",
        //         List.of(hiInput, hiColl, hiKey),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:index-where(
        // //  as item()*,
        // //  as fn(item(), xs:integer) as xs:boolean?
        // // ) as xs:integer*
        // final ArgumentSpecification iwInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification iwPred = new ArgumentSpecification("predicate", true,
        //         typeFactory.one(predicateItem));
        // register("fn", "index-where",
        //         List.of(iwInput, iwPred),
        //         typeFactory.zeroOrMore(typeFactory.itemNumber()));

        // // fn:lowest(
        // //  as item()*,
        // //  as xs:string? := fn:default-collation(),
        // //  as (fn(item()) as xs:anyAtomicType*)? := fn:data#1
        // // ) as item()*
        // final ArgumentSpecification loInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification loColl = new ArgumentSpecification("collation", false,
        //         optionalString));
        // final ArgumentSpecification loKey = new ArgumentSpecification("key", false,
        //         typeFactory.zeroOrOne(typeFactory.itemFunction(typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
        //                 List.of(typeFactory.anyItem()))));
        // register("fn", "lowest",
        //         List.of(loInput, loColl, loKey),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:partial-apply(
        // //  as fn(*),
        // //  as map(xs:positiveInteger, item()*)
        // // ) as fn(*)
        // final ArgumentSpecification paFn = new ArgumentSpecification("function", true,
        //         typeFactory.one(typeFactory.itemAnyFunction()));
        // final ArgumentSpecification paArgs = new ArgumentSpecification("arguments", true,
        //         typeFactory.one(typeFactory.itemMap(typeFactory.itemNumber(), zeroOrMoreItems)));
        // register("fn", "partial-apply",
        //         List.of(paFn, paArgs),
        //         typeFactory.one(typeFactory.itemAnyFunction()));

        // // fn:partition(
        // //  as item()*,
        // // -when as fn(item()*, item(), xs:integer) as xs:boolean?
        // // ) as array(item())*
        // final ArgumentSpecification pInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification pSplitWhen = new ArgumentSpecification("split-when", true,
        //         typeFactory.one(typeFactory.itemFunction(typeFactory.zeroOrOne(typeFactory.itemBoolean()),
        //                 List.of(zeroOrMoreItems, typeFactory.anyItem(), typeFactory.number()))));
        // register("fn", "partition",
        //         List.of(pInput, pSplitWhen),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyArray()));

        // // fn:scan-left(
        // //  as item()*,
        // //  as item()*,
        // //  as fn(item()*, item()) as item()*
        // // ) as array()*
        // final ArgumentSpecification slInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification slInit = new ArgumentSpecification("init", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification slAction = new ArgumentSpecification("action", true,
        //         typeFactory.one(leftActionItem));
        // register("fn", "scan-left",
        //         List.of(slInput, slInit, slAction),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyArray()));

        // // fn:scan-right(
        // //  as item()*,
        // //  as item()*,
        // //  as fn(item(), item()*) as item()*
        // // ) as array()*
        // final ArgumentSpecification srInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification srInit = new ArgumentSpecification("init", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification srAction = new ArgumentSpecification("action", true,
        //         typeFactory.one(rightActionItem));
        // register("fn", "scan-right",
        //         List.of(srInput, srInit, srAction),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyArray()));

        // // fn:some(
        // //  as item()*,
        // //  as fn(item(), xs:integer) as xs:boolean? := fn:boolean#1
        // // ) as xs:boolean
        // final ArgumentSpecification someInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification somePred = new ArgumentSpecification("predicate", false,
        //         typeFactory.zeroOrOne(predicateItem));
        // register("fn", "some",
        //         List.of(someInput, somePred),
        //         typeFactory.boolean_());

        // // // fn:sort(
        // // //  as item()*,
        // // //  as xs:string? := fn:default-collation(),
        // // //  as fn(item()) as xs:anyAtomicType* := fn:data#1
        // // // ) as item()*
        // // ArgumentSpecification sortInput =
        // // new ArgumentSpecification("input", true,
        // // typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // // ArgumentSpecification sortColl =
        // // new ArgumentSpecification("collation", false,
        // // optionalString));
        // // ArgumentSpecification sortKey =
        // // new ArgumentSpecification("key", false,
        // // typeFactory.one(typeFactory.itemFunction(typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
        // // List.of(typeFactory.anyItem()))));
        // // register("fn", "sort",
        // // List.of(sortInput, sortColl, sortKey),
        // // typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        // // );
        // // // fn:sort-by(
        // // //  as item()*,
        // // //  as record(key? as (fn(item()) as xs:anyAtomicType*)?,
        // // // collation? as xs:string?,
        // // // order? as enum('ascending','descending')?)*
        // // // ) as item()*
        // // ArgumentSpecification sortByInput =
        // // new ArgumentSpecification("input", true,
        // // typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // // ArgumentSpecification sortByKeys =
        // // new ArgumentSpecification("keys", true,
        // // typeFactory.zeroOrMore(typeFactory.itemRecord()));
        // // register("fn", "sort-by",
        // // List.of(sortByInput, sortByKeys),
        // // typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        // // );

        // final var comparator = typeFactory.itemFunction(typeFactory.number(),
        //         List.of(typeFactory.anyItem(), typeFactory.anyItem()));

        // // fn:sort-with(
        // //  as item()*,
        // //  as (fn(item(),item()) as xs:integer)*
        // // ) as item()*
        // final ArgumentSpecification sortWithInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification sortWithComparators = new ArgumentSpecification("comparators", true,
        //         typeFactory.zeroOrMore(comparator));
        // register("fn", "sort-with",
        //         List.of(sortWithInput, sortWithComparators),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:subsequence-where(
        // //  as item()*,
        // //  as fn(item(),xs:integer) as xs:boolean? := true#0,
        // //  as fn(item(),xs:integer) as xs:boolean? := false#0
        // // ) as item()*
        // final ArgumentSpecification subseqWhereInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification subseqWhereFrom = new ArgumentSpecification("from", false,
        //         typeFactory.zeroOrOne(predicateItem));
        // final ArgumentSpecification subseqWhereTo = new ArgumentSpecification("to", false,
        //         typeFactory.zeroOrOne(predicateItem));
        // register("fn", "subsequence-where",
        //         List.of(subseqWhereInput, subseqWhereFrom, subseqWhereTo),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:take-while(
        // //  as item()*,
        // //  as fn(item(),xs:integer) as xs:boolean?
        // // ) as item()*
        // final ArgumentSpecification takeWhileInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification takeWhilePredicate = new ArgumentSpecification("predicate", true,
        //         typeFactory.one(predicateItem));
        // register("fn", "take-while",
        //         List.of(takeWhileInput, takeWhilePredicate),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // // fn:transitive-closure(
        // // //  as node()?,
        // // //  as fn(node()) as node()*
        // // // ) as node()*
        // // ArgumentSpecification tcNode =
        // // new ArgumentSpecification("node", false,
        // // typeFactory.zeroOrOne(typeFactory.itemNode()));
        // // ArgumentSpecification tcStep =
        // // new ArgumentSpecification("step", true,
        // // typeFactory.one(typeFactory.itemFunction()));
        // // register("fn", "transitive-closure",
        // // List.of(tcNode, tcStep),
        // // typeFactory.zeroOrMore(typeFactory.itemNode())
        // // );

        // final var numberActionItem = typeFactory.itemFunction(zeroOrMoreItems,
        //         List.of(zeroOrMoreItems, typeFactory.number()));

        // // fn:while-do(
        // //  as item()*,
        // //  as fn(item()*,xs:integer) as xs:boolean?,
        // //  as fn(item()*,xs:integer) as item()*
        // // ) as item()*
        // final ArgumentSpecification whileDoInput = new ArgumentSpecification("input", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification whileDoPredicate = new ArgumentSpecification("predicate", true,
        //         typeFactory.one(predicateItem));
        // final ArgumentSpecification whileDoAction = new ArgumentSpecification("action", true,
        //         typeFactory.one(numberActionItem));
        // register("fn", "while-do",
        //         List.of(whileDoInput, whileDoPredicate, whileDoAction),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // fn:transform(
        // //  as map(*)
        // // ) as map(*)
        // final ArgumentSpecification transformOptions = new ArgumentSpecification("options", true,
        //         typeFactory.one(typeFactory.itemAnyMap()));
        // register("fn", "transform",
        //         List.of(transformOptions),
        //         typeFactory.one(typeFactory.itemAnyMap()));

        // // fn:op(
        // //  as xs:string
        // // ) as fn(item()*,item()) as item()*
        // final ArgumentSpecification opOperator = new ArgumentSpecification("operator", true,
        //         typeFactory.string());
        // register("fn", "op",
        //         List.of(opOperator),
        //         typeFactory.one(leftActionItem));

        // // map:build(
        // //  as item()*,
        // //  as (fn( as item(),  as xs:integer) as
        // xs:anyAtomicType*)? := fn:identity#1,
        // //  as (fn( as item(),  as xs:integer) as item()*)? :=
        // fn:identity#1,
        // //  as map(*)? := {}
        // // ) as map(*)
        // ArgumentSpecification mbInput =
        // new ArgumentSpecification("input", true,
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // ArgumentSpecification mbKey =
        // new ArgumentSpecification("key", false,
        // typeFactory.zeroOrOne(typeFactory.itemFunction()));
        // ArgumentSpecification mbValue =
        // new ArgumentSpecification("value", false,
        // typeFactory.zeroOrOne(typeFactory.itemFunction()));
        // ArgumentSpecification mbOptions =
        // new ArgumentSpecification("options", false,
        // typeFactory.zeroOrOne(typeFactory.itemAnyMap()));
        // register("map", "build",
        // List.of(mbInput, mbKey, mbValue, mbOptions),
        // typeFactory.one(typeFactory.itemAnyMap())
        // );

        // // map:contains( as map(*),  as xs:anyAtomicType) as xs:boolean
        // ArgumentSpecification mcMap =
        // new ArgumentSpecification("map", true,
        // typeFactory.one(typeFactory.itemAnyMap()));
        // ArgumentSpecification mcKey =
        // new ArgumentSpecification("key", true,
        // typeFactory.one(typeFactory.itemAnyItem()));
        // register("map", "contains",
        // List.of(mcMap, mcKey),
        // typeFactory.boolean_()
        // );

        // // map:empty( as map(*)) as xs:boolean
        // ArgumentSpecification meMap =
        // new ArgumentSpecification("map", true,
        // typeFactory.one(typeFactory.itemAnyMap()));
        // register("map", "empty",
        // List.of(meMap),
        // typeFactory.boolean_()
        // );

        // // map:entries( as map(*)) as map(*)*
        // ArgumentSpecification mentMap =
        // new ArgumentSpecification("map", true,
        // typeFactory.one(typeFactory.itemAnyMap()));
        // register("map", "entries",
        // List.of(mentMap),
        // typeFactory.zeroOrMore(typeFactory.itemAnyMap())
        // );

        // // map:entry( as xs:anyAtomicType,  as item()*) as map(*)
        // ArgumentSpecification mentKey =
        // new ArgumentSpecification("key", true,
        // typeFactory.one(typeFactory.itemAnyItem()));
        // ArgumentSpecification mentValue =
        // new ArgumentSpecification("value", true,
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("map", "entry",
        // List.of(mentKey, mentValue),
        // typeFactory.one(typeFactory.itemAnyMap())
        // );

        // // map:filter( as map(*),  as fn( as xs:anyAtomicType,
        //  as item()*) as xs:boolean?) as map(*)
        // ArgumentSpecification mfMap =
        // new ArgumentSpecification("map", true,
        // typeFactory.one(typeFactory.itemAnyMap()));
        // ArgumentSpecification mfPred =
        // new ArgumentSpecification("predicate", true,
        // typeFactory.one(typeFactory.itemFunction()));
        // register("map", "filter",
        // List.of(mfMap, mfPred),
        // typeFactory.one(typeFactory.itemAnyMap())
        // );

        // // map:find( as item()*,  as xs:anyAtomicType) as array(*)
        // ArgumentSpecification mfindInput =
        // new ArgumentSpecification("input", true,
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // ArgumentSpecification mfindKey =
        // new ArgumentSpecification("key", true,
        // typeFactory.one(typeFactory.itemAnyItem()));
        // register("map", "find",
        // List.of(mfindInput, mfindKey),
        // typeFactory.one(typeFactory.itemAnyArray())
        // );

        // // map:for-each( as map(*),  as fn( as xs:anyAtomicType,
        //  as item()*) as item()*) as item()*
        // ArgumentSpecification mfeMap =
        // new ArgumentSpecification("map", true,
        // typeFactory.one(typeFactory.itemAnyMap()));
        // ArgumentSpecification mfeAction =
        // new ArgumentSpecification("action", true,
        // typeFactory.one(typeFactory.itemFunction()));
        // register("map", "for-each",
        // List.of(mfeMap, mfeAction),
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        // );

        // // map:get( as map(*),  as xs:anyAtomicType,  as item()* :=
        // ()) as item()*
        // ArgumentSpecification mgMap =
        // new ArgumentSpecification("map", true,
        // typeFactory.one(typeFactory.itemAnyMap()));
        // ArgumentSpecification mgKey =
        // new ArgumentSpecification("key", true,
        // typeFactory.one(typeFactory.itemAnyItem()));
        // ArgumentSpecification mgDefault =
        // new ArgumentSpecification("default", false,
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("map", "get",
        // List.of(mgMap, mgKey, mgDefault),
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        // );

        // // map:items( as map(*)) as item()*
        // ArgumentSpecification mitemsMap =
        // new ArgumentSpecification("map", true,
        // typeFactory.one(typeFactory.itemAnyMap()));
        // register("map", "items",
        // List.of(mitemsMap),
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        // );

        // // map:keys( as map(*)) as xs:anyAtomicType*
        // ArgumentSpecification mkeysMap =
        // new ArgumentSpecification("map", true,
        // typeFactory.one(typeFactory.itemAnyMap()));
        // register("map", "keys",
        // List.of(mkeysMap),
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        // );
        // // map:keys-where(
        // //  as map(*),
        // //  as fn( as xs:anyAtomicType,  as item()*) as
        // xs:boolean?
        // // ) as xs:anyAtomicType*
        // ArgumentSpecification kwMap =
        // new ArgumentSpecification("map", true,
        // typeFactory.one(typeFactory.itemAnyMap()));
        // ArgumentSpecification kwPred =
        // new ArgumentSpecification("predicate", true,
        // typeFactory.one(typeFactory.itemFunction()));
        // register("map", "keys-where",
        // List.of(kwMap, kwPred),
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        // );

        // // map:merge(
        // //  as map(*)*,
        // //  as map(*)? := {}
        // // ) as map(*)
        // ArgumentSpecification mmMaps =
        // new ArgumentSpecification("maps", true,
        // typeFactory.zeroOrMore(typeFactory.itemAnyMap()));
        // ArgumentSpecification mmOptions =
        // new ArgumentSpecification("options", false,
        // typeFactory.zeroOrOne(typeFactory.itemAnyMap()));
        // register("map", "merge",
        // List.of(mmMaps, mmOptions),
        // typeFactory.one(typeFactory.itemAnyMap())
        // );

        // // map:of-pairs(
        // //  as key-value-pair*,
        // //  as map(*)? := {}
        // // ) as map(*)
        // ArgumentSpecification opInput =
        // new ArgumentSpecification("input", true,
        // typeFactory.zeroOrMore(typeFactory.item("key-value-pair")));
        // ArgumentSpecification opOptions =
        // new ArgumentSpecification("options", false,
        // typeFactory.zeroOrOne(typeFactory.itemAnyMap()));
        // register("map", "of-pairs",
        // List.of(opInput, opOptions),
        // typeFactory.one(typeFactory.itemAnyMap())
        // );

        // // map:pair(
        // //  as xs:anyAtomicType,
        // //  as item()*
        // // ) as key-value-pair
        // ArgumentSpecification mpKey =
        // new ArgumentSpecification("key", true,
        // typeFactory.one(typeFactory.itemAnyItem()));
        // ArgumentSpecification mpValue =
        // new ArgumentSpecification("value", true,
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("map", "pair",
        // List.of(mpKey, mpValue),
        // typeFactory.one(typeFactory.item("key-value-pair"))
        // );

        // // map:pairs(
        // //  as map(*)
        // // ) as key-value-pair*
        // ArgumentSpecification mpsMap =
        // new ArgumentSpecification("map", true,
        // typeFactory.one(typeFactory.itemAnyMap()));
        // register("map", "pairs",
        // List.of(mpsMap),
        // typeFactory.zeroOrMore(typeFactory.item("key-value-pair"))
        // );

        // // map:put(
        // //  as map(*),
        // //  as xs:anyAtomicType,
        // //  as item()*
        // // ) as map(*)
        // ArgumentSpecification mputMap =
        // new ArgumentSpecification("map", true,
        // typeFactory.one(typeFactory.itemAnyMap()));
        // ArgumentSpecification mputKey =
        // new ArgumentSpecification("key", true,
        // typeFactory.one(typeFactory.itemAnyItem()));
        // ArgumentSpecification mputValue =
        // new ArgumentSpecification("value", true,
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("map", "put",
        // List.of(mputMap, mputKey, mputValue),
        // typeFactory.one(typeFactory.itemAnyMap())
        // );

        // // map:remove(
        // //  as map(*),
        // //  as xs:anyAtomicType*
        // // ) as map(*)
        // ArgumentSpecification mremMap =
        // new ArgumentSpecification("map", true,
        // typeFactory.one(typeFactory.itemAnyMap()));
        // ArgumentSpecification mremKeys =
        // new ArgumentSpecification("keys", true,
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("map", "remove",
        // List.of(mremMap, mremKeys),
        // typeFactory.one(typeFactory.itemAnyMap())
        // );

        // // map:size(
        // //  as map(*)
        // // ) as xs:integer
        // ArgumentSpecification msizeMap =
        // new ArgumentSpecification("map", true,
        // typeFactory.one(typeFactory.itemAnyMap()));
        // register("map", "size",
        // List.of(msizeMap),
        // typeFactory.number())
        // );

        // // fn:element-to-map-plan(
        // //  as (document-node() | element(*))*
        // // ) as map(xs:string, record(*))
        // ArgumentSpecification etmpInput =
        // new ArgumentSpecification("input", true,
        // typeFactory.zeroOrMore(typeFactory.union(
        // typeFactory.itemDocumentNode(),
        // typeFactory.itemElement()
        // )));
        // register("fn", "element-to-map-plan",
        // List.of(etmpInput),
        // typeFactory.one(typeFactory.itemAnyMap())
        // );

        // // fn:element-to-map(
        // //  as element()?,
        // //  as map(*)? := {}
        // // ) as map(xs:string, item()?)?
        // ArgumentSpecification etmElement =
        // new ArgumentSpecification("element", false,
        // typeFactory.zeroOrOne(typeFactory.itemElement()));
        // ArgumentSpecification etmOptions =
        // new ArgumentSpecification("options", false,
        // typeFactory.zeroOrOne(typeFactory.itemAnyMap()));
        // register("fn", "element-to-map",
        // List.of(etmElement, etmOptions),
        // typeFactory.zeroOrOne(typeFactory.itemAnyMap())
        // );
        // // array:append(
        // //  as array(*),
        // //  as item()*
        // // ) as array(*)
        // ArgumentSpecification appendArr =
        // new ArgumentSpecification("array", true,
        // typeFactory.one(typeFactory.itemAnyArray()));
        // ArgumentSpecification appendMember =
        // new ArgumentSpecification("member", true,
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("array", "append",
        // List.of(appendArr, appendMember),
        // typeFactory.one(typeFactory.itemAnyArray())
        // );

        // // array:build(
        // //  as item()*,
        // //  as fn(item(), xs:integer) as item()* := fn:identity#1
        // // ) as array(*)
        // ArgumentSpecification buildInput =
        // new ArgumentSpecification("input", true,
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // ArgumentSpecification buildAction =
        // new ArgumentSpecification("action", false,
        // typeFactory.zeroOrOne(typeFactory.itemFunction()));
        // register("array", "build",
        // List.of(buildInput, buildAction),
        // typeFactory.one(typeFactory.itemAnyArray())
        // );

        // // array:empty( as array(*)) as xs:boolean
        // ArgumentSpecification emptyArr =
        // new ArgumentSpecification("array", true,
        // typeFactory.one(typeFactory.itemAnyArray()));
        // register("array", "empty",
        // List.of(emptyArr),
        // typeFactory.boolean_()
        // );

        // // array:filter(
        // //  as array(*),
        // //  as fn(item(), xs:integer) as xs:boolean?
        // // ) as array(*)
        // ArgumentSpecification filterArr =
        // new ArgumentSpecification("array", true,
        // typeFactory.one(typeFactory.itemAnyArray()));
        // ArgumentSpecification filterPred =
        // new ArgumentSpecification("predicate", true,
        // typeFactory.one(typeFactory.itemFunction()));
        // register("array", "filter",
        // List.of(filterArr, filterPred),
        // typeFactory.one(typeFactory.itemAnyArray())
        // );

        // // array:flatten( as item()) as item()*
        // ArgumentSpecification flattenInput =
        // new ArgumentSpecification("input", true,
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("array", "flatten",
        // List.of(flattenInput),
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        // );

        // // array:fold-left(
        // //  as array(*),
        // //  as item()*,
        // //  as fn(item(), item()*) as item()*
        // // ) as item()*
        // ArgumentSpecification foldLArr =
        // new ArgumentSpecification("array", true,
        // typeFactory.one(typeFactory.itemAnyArray()));
        // ArgumentSpecification foldLInit =
        // new ArgumentSpecification("init", true,
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // ArgumentSpecification foldLAction =
        // new ArgumentSpecification("action", true,
        // typeFactory.one(typeFactory.itemFunction()));
        // register("array", "fold-left",
        // List.of(foldLArr, foldLInit, foldLAction),
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        // );

        // // array:fold-right(
        // //  as array(*),
        // //  as item()*,
        // //  as fn(item(), item()*) as item()*
        // // ) as item()*
        // ArgumentSpecification foldRArr =
        // new ArgumentSpecification("array", true,
        // typeFactory.one(typeFactory.itemAnyArray()));
        // ArgumentSpecification foldRInit =
        // new ArgumentSpecification("init", true,
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // ArgumentSpecification foldRAction =
        // new ArgumentSpecification("action", true,
        // typeFactory.one(typeFactory.itemFunction()));
        // register("array", "fold-right",
        // List.of(foldRArr, foldRInit, foldRAction),
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        // );

        // // array:foot( as array(*)) as item()*
        // ArgumentSpecification footArr =
        // new ArgumentSpecification("array", true,
        // typeFactory.one(typeFactory.itemAnyArray()));
        // register("array", "foot",
        // List.of(footArr),
        // typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        // );

        // // array:for-each(
        // //  as array(*),
        // //  as fn(item(), xs:integer) as item()*
        // // ) as array(*)
        // ArgumentSpecification feArr =
        // new ArgumentSpecification("array", true,
        // typeFactory.one(typeFactory.itemAnyArray()));
        // ArgumentSpecification feAction =
        // new ArgumentSpecification("action", true,
        // typeFactory.one(typeFactory.itemFunction()));
        // register("array", "for-each",
        // List.of(feArr, feAction),
        // typeFactory.one(typeFactory.itemAnyArray())
        // );

        // // array:for-each-pair(
        // //  as array(*),
        // //  as array(*),
        // //  as fn(item(), item(), xs:integer) as item()*
        // // ) as array(*)
        // ArgumentSpecification fepArr1 =
        // new ArgumentSpecification("array1", true,
        // typeFactory.one(typeFactory.itemAnyArray()));
        // ArgumentSpecification fepArr2 =
        // new ArgumentSpecification("array2", true,
        // typeFactory.one(typeFactory.itemAnyArray()));
        // ArgumentSpecification fepAction =
        // new ArgumentSpecification("action", true,
        // typeFactory.one(typeFactory.itemFunction()));
        // register("array", "for-each-pair",
        // List.of(fepArr1, fepArr2, fepAction),
        // typeFactory.one(typeFactory.itemAnyArray())
        // );

        // // array:get( as array(*),  as xs:integer) as item()*
        // final ArgumentSpecification getArr = new ArgumentSpecification("array", true,
        //         typeFactory.one(typeFactory.itemAnyArray()));
        // final ArgumentSpecification getPos = new ArgumentSpecification("position", true,
        //         typeFactory.number()));
        // register("array", "get",
        //         List.of(getArr, getPos),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // array:get(
        // //  as array(*),
        // //  as xs:integer,
        // //  as item()*
        // // ) as item()*
        // final ArgumentSpecification getArrDef = new ArgumentSpecification("array", true,
        //         typeFactory.one(typeFactory.itemAnyArray()));
        // final ArgumentSpecification getPosDef = new ArgumentSpecification("position", true,
        //         typeFactory.number()));
        // final ArgumentSpecification getDefault = new ArgumentSpecification("default", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("array", "get",
        //         List.of(getArrDef, getPosDef, getDefault),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // array:head( as array(*)) as item()*
        // final ArgumentSpecification headArr = new ArgumentSpecification("array", true,
        //         typeFactory.one(typeFactory.itemAnyArray()));
        // register("array", "head",
        //         List.of(headArr),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // array:index-of(
        // //  as array(*),
        // //  as item()*,
        // //  as xs:string? := fn:default-collation()
        // // ) as xs:integer*
        // final ArgumentSpecification aioArray = new ArgumentSpecification("array", true,
        //         typeFactory.one(typeFactory.itemAnyArray()));
        // final ArgumentSpecification aioTarget = new ArgumentSpecification("target", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // final ArgumentSpecification aioCollation = new ArgumentSpecification("collation", false,
        //         optionalString));
        // register("array", "index-of",
        //         List.of(aioArray, aioTarget, aioCollation),
        //         typeFactory.zeroOrMore(typeFactory.itemNumber()));

        // // array:index-where(
        // //  as array(*),
        // //  as fn(item(), xs:integer) as xs:boolean?
        // // ) as xs:integer*
        // final ArgumentSpecification aiwArray = new ArgumentSpecification("array", true,
        //         typeFactory.one(typeFactory.itemAnyArray()));
        // final ArgumentSpecification aiwPred = new ArgumentSpecification("predicate", true,
        //         typeFactory.one(predicateItem));
        // register("array", "index-where",
        //         List.of(aiwArray, aiwPred),
        //         typeFactory.zeroOrMore(typeFactory.itemNumber()));

        // // array:insert-before(
        // //  as array(*),
        // //  as xs:integer,
        // //  as item()*
        // // ) as array(*)
        // final ArgumentSpecification aibArray = new ArgumentSpecification("array", true,
        //         typeFactory.one(typeFactory.itemAnyArray()));
        // final ArgumentSpecification aibPosition = new ArgumentSpecification("position", true,
        //         typeFactory.number()));
        // final ArgumentSpecification aibMember = new ArgumentSpecification("member", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("array", "insert-before",
        //         List.of(aibArray, aibPosition, aibMember),
        //         typeFactory.one(typeFactory.itemAnyArray()));

        // // array:items(
        // //  as array(*)
        // // ) as item()*
        // final ArgumentSpecification aitArray = new ArgumentSpecification("array", true,
        //         typeFactory.one(typeFactory.itemAnyArray()));
        // register("array", "items",
        //         List.of(aitArray),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));

        // // array:join(
        // //  as array(*)*,
        // //  as array(*)? := ()
        // // ) as array(*)
        // final ArgumentSpecification ajgArrays = new ArgumentSpecification("arrays", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyArray()));
        // final ArgumentSpecification ajgSep = new ArgumentSpecification("separator", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyArray()));
        // register("array", "join",
        //         List.of(ajgArrays, ajgSep),
        //         typeFactory.one(typeFactory.itemAnyArray()));

        // // // array:members(
        // // //  as array(*)
        // // // ) as record(value as item())*
        // // ArgumentSpecification amMembers =
        // // new ArgumentSpecification("array", true,
        // // typeFactory.one(typeFactory.itemAnyArray()));
        // // register("array", "members",
        // // List.of(amMembers),
        // // typeFactory.zeroOrMore(
        // // typeFactory.mapOf(
        // // typeFactory.itemString(),
        // // typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        // // )
        // // )
        // // );

        // // // array:of-members(
        // // //  as record(value as item())*
        // // // ) as array(*)
        // // ArgumentSpecification aomInput =
        // // new ArgumentSpecification("input", true,
        // // typeFactory.zeroOrMore(
        // // typeFactory.itemRecord()
        // // ));
        // // register("array", "of-members",
        // // List.of(aomInput),
        // // typeFactory.one(typeFactory.itemAnyArray())
        // // );

        // // array:put(
        // //  as array(*),
        // //  as xs:integer,
        // //  as item()*
        // // ) as array(*)
        // final ArgumentSpecification apArray = new ArgumentSpecification("array", true,
        //         typeFactory.one(typeFactory.itemAnyArray()));
        // final ArgumentSpecification apPosition = new ArgumentSpecification("position", true,
        //         typeFactory.number()));
        // final ArgumentSpecification apMember = new ArgumentSpecification("member", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("array", "put",
        //         List.of(apArray, apPosition, apMember),
        //         typeFactory.one(typeFactory.itemAnyArray()));

        // // array:remove(
        // //  as array(*),
        // //  as xs:integer*
        // // ) as array(*)
        // final ArgumentSpecification arArray = new ArgumentSpecification("array", true,
        //         typeFactory.one(typeFactory.itemAnyArray()));
        // final ArgumentSpecification arPositions = new ArgumentSpecification("positions", true,
        //         typeFactory.zeroOrMore(typeFactory.itemNumber()));
        // register("array", "remove",
        //         List.of(arArray, arPositions),
        //         typeFactory.one(typeFactory.itemAnyArray()));

        // // array:reverse(
        // //  as array(*)
        // // ) as array(*)
        // final ArgumentSpecification arRevArray = new ArgumentSpecification("array", true,
        //         typeFactory.one(typeFactory.itemAnyArray()));
        // register("array", "reverse",
        //         List.of(arRevArray),
        //         typeFactory.one(typeFactory.itemAnyArray()));

        // // array:size(
        // //  as array(*)
        // // ) as xs:integer
        // final ArgumentSpecification aszArray = new ArgumentSpecification("array", true,
        //         typeFactory.one(typeFactory.itemAnyArray()));
        // register("array", "size",
        //         List.of(aszArray),
        //         typeFactory.number()));

        // // array:slice(
        // //  as array(*),
        // //  as xs:integer? := (),
        // //  as xs:integer? := (),
        // //  as xs:integer? := ()
        // // ) as array(*)
        // final ArgumentSpecification aslArray = new ArgumentSpecification("array", true,
        //         typeFactory.one(typeFactory.itemAnyArray()));
        // final ArgumentSpecification aslStart = new ArgumentSpecification("start", false,
        //         typeFactory.zeroOrOne(typeFactory.itemNumber()));
        // final ArgumentSpecification aslEnd = new ArgumentSpecification("end", false,
        //         typeFactory.zeroOrOne(typeFactory.itemNumber()));
        // final ArgumentSpecification aslStep = new ArgumentSpecification("step", false,
        //         typeFactory.zeroOrOne(typeFactory.itemNumber()));
        // register("array", "slice",
        //         List.of(aslArray, aslStart, aslEnd, aslStep),
        //         typeFactory.one(typeFactory.itemAnyArray()));

        // // // array:sort(
        // // //  as array(*),
        // // //  as xs:string? := fn:default-collation(),
        // // //  as fn(item()*) as xs:anyAtomicType* := fn:data#1
        // // // ) as array(*)
        // // ArgumentSpecification asrArray =
        // // new ArgumentSpecification("array", true,
        // // typeFactory.one(typeFactory.itemAnyArray()));
        // // ArgumentSpecification asrColl =
        // // new ArgumentSpecification("collation", false,
        // // optionalString));
        // // ArgumentSpecification asrKey =
        // // new ArgumentSpecification("key", false,
        // // typeFactory.zeroOrOne(typeFactory.itemFunction()));
        // // register("array", "sort",
        // // List.of(asrArray, asrColl, asrKey),
        // // typeFactory.one(typeFactory.itemAnyArray())
        // // );

        // // // array:sort-by(
        // // //  as array(*),
        // // //  as record(
        // // // key? as fn(item()*) as xs:anyAtomicType*,
        // // // collation? as xs:string?,
        // // // order? as enum('ascending','descending')?
        // // // )*
        // // // ) as item()*
        // // ArgumentSpecification asbArray =
        // // new ArgumentSpecification("array", true,
        // // typeFactory.one(typeFactory.itemAnyArray()));
        // // ArgumentSpecification asbKeys =
        // // new ArgumentSpecification("keys", true,
        // // typeFactory.zeroOrMore(typeFactory.itemRecord()));
        // // register("array", "sort-by",
        // // List.of(asbArray, asbKeys),
        // // typeFactory.zeroOrMore(typeFactory.itemAnyItem())
        // // );

        // // array:split( as array(*)) as array(*)*
        // final ArgumentSpecification splitArray = new ArgumentSpecification("array", true,
        //         typeFactory.one(typeFactory.itemAnyArray()));
        // register("array", "split",
        //         List.of(splitArray),
        //         typeFactory.zeroOrMore(typeFactory.itemAnyArray()));

        // // array:subarray( as array(*),  as xs:integer,  as
        // // xs:integer? := ()) as array(*)
        // final ArgumentSpecification subarrayArr = new ArgumentSpecification("array", true,
        //         typeFactory.one(typeFactory.itemAnyArray()));
        // final ArgumentSpecification subarrayStart = new ArgumentSpecification("start", true,
        //         typeFactory.number()));
        // final ArgumentSpecification subarrayLength = new ArgumentSpecification("length", false,
        //         typeFactory.zeroOrOne(typeFactory.itemNumber()));
        // register("array", "subarray",
        //         List.of(subarrayArr, subarrayStart, subarrayLength),
        //         typeFactory.one(typeFactory.itemAnyArray()));

        // // array:tail( as array(*)) as array(*)
        // final ArgumentSpecification arrayTail = new ArgumentSpecification("array", true,
        //         typeFactory.one(typeFactory.itemAnyArray()));
        // register("array", "tail",
        //         List.of(arrayTail),
        //         typeFactory.one(typeFactory.itemAnyArray()));

        // // array:trunk( as array(*)) as array(*)
        // final ArgumentSpecification arrayTrunk = new ArgumentSpecification("array", true,
        //         typeFactory.one(typeFactory.itemAnyArray()));
        // register("array", "trunk",
        //         List.of(arrayTrunk),
        //         typeFactory.one(typeFactory.itemAnyArray()));

        // // fn:type-of( as item()*) as xs:string
        // final ArgumentSpecification typeOfValue = new ArgumentSpecification("value", true,
        //         typeFactory.zeroOrMore(typeFactory.itemAnyItem()));
        // register("fn", "type-of",
        //         List.of(typeOfValue),
        //         typeFactory.string());
        // // xs:unsignedInt( as xs:anyAtomicType? := .) as xs:unsignedInt?
        // final ArgumentSpecification unsignedIntArg = new ArgumentSpecification("arg", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        // register("xs", "unsignedInt",
        //         List.of(unsignedIntArg),
        //         typeFactory.zeroOrOne(typeFactory.itemNumber()));

        // // xs:string( as xs:anyAtomicType? := .) as xs:string?
        // final ArgumentSpecification castStringValue = new ArgumentSpecification("value", false,
        //         typeFactory.zeroOrOne(typeFactory.itemAnyItem()));
        // register("xs", "string",
        //         List.of(castStringValue),
        //         optionalString));



        // fn:default-collation() as xs:string
        register("fn", "default-collation", List.of(), typeFactory.string());




    }

    private static ParseTree getTree(final String xquery, Function<AntlrXqueryParser, ParseTree> initialRule) {
        final CodePointCharStream charStream = CharStreams.fromString(xquery);
        final AntlrXqueryLexer lexer = new AntlrXqueryLexer(charStream);
        final CommonTokenStream stream = new CommonTokenStream(lexer);
        final AntlrXqueryParser parser = new AntlrXqueryParser(stream);
        return initialRule.apply(parser);
    }

    record FunctionSpecification(
            long minArity,
            long maxArity,
            List<ArgumentSpecification> args,
            XQuerySequenceType returnedType,
            XQuerySequenceType requiredContextValueType,
            boolean requiresPosition,
            boolean requiresSize) {
    }

    final Map<String, Map<String, List<FunctionSpecification>>> namespaces;
    private static final ParseTree STRING_AT_CONTEXT_VALUE = XQuerySemanticFunctionManager.getTree("fn:string(.)", (parser) -> parser.functionCall());
    private static final ParseTree EMPTY_STRING = XQuerySemanticFunctionManager.getTree("\"\"", (parser)->parser.literal());

    private CallAnalysisResult handleUnknownNamespace(final String namespace, final String errorMessageSupplier,
            final XQuerySequenceType fallbackType) {
        final List<String> errors = List.of(errorMessageSupplier);
        return new CallAnalysisResult(fallbackType, List.of(), errors);
    }

    private CallAnalysisResult handleUnknownFunction(final String namespace, final String name,
            final String errorMessageSupplier, final XQuerySequenceType fallbackType) {
        final List<String> errors = List.of(errorMessageSupplier);
        return new CallAnalysisResult(fallbackType, List.of(), errors);
    }

    private CallAnalysisResult handleNoMatchingFunction(final String errorMessageSupplier,
            final XQuerySequenceType fallbackType) {
        final List<String> errors = List.of(errorMessageSupplier);
        return new CallAnalysisResult(fallbackType, List.of(), errors);
    }

    record SpecAndErrors(FunctionSpecification spec, List<String> errors) {
    }

    SpecAndErrors getFunctionSpecification(final String namespace, final String name,
            final List<FunctionSpecification> namedFunctions, final long requiredArity) {
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
        return new SpecAndErrors(null, List.of(errorMessage));
    }

    @Override
    public CallAnalysisResult call(
            final String namespace,
            final String name,
            final List<XQuerySequenceType> positionalargs,
            final Map<String, XQuerySequenceType> keywordArgs,
            final XQueryVisitingSemanticContext context)
    {
        final var anyItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        if (!namespaces.containsKey(namespace)) {
            return handleUnknownNamespace(namespace, "Unknown function namespace: " + namespace, anyItems);
        }

        final var namespaceFunctions = namespaces.get(namespace);
        if (!namespaceFunctions.containsKey(name)) {
            return handleUnknownFunction(namespace, name, "Unknown function: " + namespace + ":" + name, anyItems);
        }
        final var namedFunctions = namespaceFunctions.get(name);
        final int positionalArgsCount = positionalargs.size();
        final var requiredArity = positionalArgsCount + keywordArgs.size();

        final List<String> mismatchReasons = new ArrayList<>();

        final SpecAndErrors specAndErrors = getFunctionSpecification(namespace, name, namedFunctions, requiredArity);
        if (specAndErrors.spec == null) {
            return new CallAnalysisResult(anyItems, List.of(), specAndErrors.errors);
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
            return new CallAnalysisResult(spec.returnedType, defaultArgs.toList(), List.of());
        }
        final String message = getNoMatchingFunctionMessage(namespace, name, requiredArity, mismatchReasons);
        return handleNoMatchingFunction(message, spec.returnedType);
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
        stringBuilder.append((mismatchReasons.isEmpty() ? "" : ". Reasons:\n" + String.join("\n", mismatchReasons)));
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

    @Override
    public CallAnalysisResult getFunctionReference(final String namespace, final String functionName, final int arity) {
        // TODO: Verify logic
        final var fallback = typeFactory.anyFunction();
        if (!namespaces.containsKey(namespace)) {
            return handleUnknownNamespace(namespace, "Unknown function namespace: " + namespace, fallback);
        }
        final var namespaceFunctions = namespaces.get(namespace);
        if (!namespaceFunctions.containsKey(functionName)) {
            return handleUnknownFunction(namespace, functionName, "Unknown function: " + namespace + ":" + functionName,
                    fallback);
        }

        final var namedFunctions = namespaceFunctions.get(functionName);
        final SpecAndErrors specAndErrors = getFunctionSpecification(namespace, functionName, namedFunctions, arity);
        if (specAndErrors.spec == null) {
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unknown function reference: ");
            stringBuilder.append(namespace);
            stringBuilder.append(":");
            stringBuilder.append(functionName);
            stringBuilder.append("#");
            stringBuilder.append(arity);
            return new CallAnalysisResult(fallback, List.of(), List.of(stringBuilder.toString()));
        }
        return new CallAnalysisResult(specAndErrors.spec.returnedType, List.of(), specAndErrors.errors);

    }

    private String wrongNumberOfArguments(final String functionName, final int expected, final int actual) {
        return "Wrong number of arguments for function" + functionName + " : expected " + expected + ", got " + actual;
    }

    public CallAnalysisResult not(final XQueryTypeFactory typeFactory, final XQueryVisitingSemanticContext context,
            final List<XQuerySequenceType> args) {
        if (args.size() != 1) {
            final String message = wrongNumberOfArguments("fn:not()", 1, args.size());
            return new CallAnalysisResult(typeFactory.boolean_(), List.of(), List.of(message));
        }
        return new CallAnalysisResult(typeFactory.boolean_(), List.of(), List.of());
    }

    public XQuerySemanticError register(
            final String namespace,
            final String functionName,
            final List<ArgumentSpecification> args,
            final XQuerySequenceType returnedType) {
        return register(namespace, functionName, args, returnedType, null, false, false);
    }

    @Override
    public XQuerySemanticError register(
            final String namespace,
            final String functionName,
            final List<ArgumentSpecification> args,
            final XQuerySequenceType returnedType,
            final XQuerySequenceType requiredContextValueType,
            final boolean requiresPosition,
            final boolean requiresLength)
    {
        final long minArity = args.stream().filter(arg -> arg.defaultArgument() == null).collect(Collectors.counting());
        final long maxArity = args.size();
        if (!namespaces.containsKey(namespace)) {
            final Map<String, List<FunctionSpecification>> functions = new HashMap<>();
            final List<FunctionSpecification> functionList = new ArrayList<>();
            functionList.add(new FunctionSpecification(minArity, maxArity, args, returnedType, requiredContextValueType,
                    requiresPosition, requiresLength));
            functions.put(functionName, functionList);
            namespaces.put(namespace, functions);
            return null;
        }
        final var namespaceMapping = namespaces.get(namespace);
        if (!namespaceMapping.containsKey(functionName)) {
            final List<FunctionSpecification> functionList = new ArrayList<>();
            functionList.add(new FunctionSpecification(minArity, maxArity, args, returnedType, requiredContextValueType,
                    requiresPosition, requiresLength));
            namespaceMapping.put(functionName, functionList);
            return null;
        }
        final List<FunctionSpecification> alreadyRegistered = namespaceMapping.get(functionName);
        final var overlapping = alreadyRegistered.stream().filter(f -> {
            return f.minArity <= maxArity;
        }).toList();
        if (!overlapping.isEmpty()) {
            return XQuerySemanticError.FunctionNameArityConflict;
        }
        return null;
    }
}
