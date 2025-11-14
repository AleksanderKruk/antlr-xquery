package com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.AntlrXqueryParser.ParenthesizedExprContext;
import com.github.akruk.antlrxquery.HelperTrees;
import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticSymbolManager.ArgumentSpecification;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticSymbolManager.GrainedAnalysis;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller.XQuerySemanticSymbolManager.SimplifiedFunctionSpecification;
import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class FunctionSets {
    public static List<List<SimplifiedFunctionSpecification>> ALL(XQueryTypeFactory typeFactory) {
        var fn = FN(typeFactory);
        var op = OP(typeFactory);
        var math = MATH(typeFactory);
        var antlr = ANTLR(typeFactory);
        return List.of(fn, op, math, antlr);
    }
    public static List<SimplifiedFunctionSpecification> FN(XQueryTypeFactory typeFactory) {
        List<SimplifiedFunctionSpecification> fn = new ArrayList<>(400);
        var helperTrees = new HelperTrees();
        final ParseTree CONTEXT_VALUE = helperTrees.CONTEXT_VALUE;
        final ParseTree DEFAULT_COLLATION = helperTrees.DEFAULT_COLLATION;
        final ParseTree EMPTY_SEQUENCE = helperTrees.EMPTY_SEQUENCE;
        final ParseTree DEFAULT_ROUNDING_MODE = helperTrees.DEFAULT_ROUNDING_MODE;
        final ParseTree ZERO_LITERAL = helperTrees.ZERO_LITERAL;
        final ParseTree NFC = helperTrees.NFC;
        final ParseTree STRING_AT_CONTEXT_VALUE = helperTrees.STRING_AT_CONTEXT_VALUE;
        final ParseTree EMPTY_STRING = helperTrees.EMPTY_STRING;
        final ParseTree EMPTY_MAP = helperTrees.EMPTY_MAP;
        final ParseTree IDENTITY$1 = helperTrees.IDENTITY$1;
        final ParseTree BOOLEAN$1 = helperTrees.BOOLEAN$1;
        final ParseTree DATA$1 = helperTrees.DATA$1;
        final ParseTree TRUE$0 = helperTrees.TRUE$0;
        final ParseTree FALSE$0 = helperTrees.FALSE$0;
        final ParseTree DEFAULT_COMPARATOR = helperTrees.DEFAULT_COMPARATOR;
        final ParseTree TEN = helperTrees.TEN;


        final XQuerySequenceType optionalString = typeFactory.zeroOrOne(typeFactory.itemString());
        final XQuerySequenceType zeroOrMoreNumbers = typeFactory.zeroOrMore(typeFactory.itemNumber());
        final XQuerySequenceType optionalItem = typeFactory.zeroOrOne(typeFactory.itemAnyItem());
        final XQuerySequenceType zeroOrMoreNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());

        final XQuerySequenceType zeroOrMoreItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        final ArgumentSpecification argItems = new ArgumentSpecification("input", zeroOrMoreItems, null);

        final XQuerySequenceType optionalNumber = typeFactory.zeroOrOne(typeFactory.itemNumber());
        final ArgumentSpecification valueNum = new ArgumentSpecification("value", optionalNumber, null);
        final ArgumentSpecification roundingMode = new ArgumentSpecification("mode",
            typeFactory.zeroOrOne(typeFactory.itemEnum(Set.of(
                "floor",
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
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "abs"),
            List.of(valueNum),
            optionalNumber,
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:ceiling(
        // 	as xs:numeric?
        // ) as xs:numeric?
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "ceiling"),
            List.of(valueNum),
            optionalNumber,
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:floor(
        // 	as xs:numeric?
        // ) as xs:numeric?
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "floor"),
            List.of(valueNum),
            optionalNumber,
            null,
            false,
            false,
            null,
            null
            )
        );

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
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "round"),
            List.of(valueNum, precision, roundingMode),
            optionalNumber,
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:round-half-to-even(
        // $value	as xs:numeric?,
        // $precision	as xs:integer?	:= 0
        // ) as xs:numeric?
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "round-half-to-even"),
            List.of(valueNum, precision),
            optionalNumber,
            null,
            false,
            false,
            null,
            null
            )
        );

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
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "divide-decimals"),
            List.of(arg_value_number, arg_divisor_number, precision),
            divisionResult,
            null,
            false,
            false,
            null,
            null
            )
        );


        // fn:zero-or-one(
        //  as item()*
        // ) as item()?
        final ArgumentSpecification anyItemsRequiredInput = new ArgumentSpecification("input", zeroOrMoreItems, null);
        GrainedAnalysis zeroOrOneAnalysis = (args, _, _, ctx) -> {
            TypeInContext unrefinedType = args.get(0).type();
            var refinedType = typeFactory.zeroOrOne(unrefinedType.type.itemType);
            if (unrefinedType.isSubtypeOf(refinedType)) {
            return unrefinedType;
            }
            return ctx.currentScope().typeInContext(refinedType);
        };
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "zero-or-one"),
            List.of(anyItemsRequiredInput),
            optionalItem,
            null,
            false,
            false,
            null,
            zeroOrOneAnalysis
            )
        );

        // fn:one-or-more(
        //  as item()*
        // ) as item()+
        GrainedAnalysis oneOrMoreAnalysis = (args, _, _, ctx) -> {
            TypeInContext unrefinedType = args.get(0).type();
            var refinedType = typeFactory.oneOrMore(unrefinedType.type.itemType);
            if (unrefinedType.isSubtypeOf(refinedType)) {
            return unrefinedType;
            }
            return ctx.currentScope().typeInContext(refinedType);
        };
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "one-or-more"),
            List.of(anyItemsRequiredInput),
            typeFactory.oneOrMore(typeFactory.itemAnyItem()),
            null,
            false,
            false,
            null,
            oneOrMoreAnalysis
            )
        );

        // fn:exactly-one(
        //  as item()*
        // ) as item()
        GrainedAnalysis exactlyOneAnalysis = (args, _, _, ctx) -> {
            TypeInContext unrefinedType = args.get(0).type();
            var refinedType = typeFactory.one(unrefinedType.type.itemType);
            if (unrefinedType.isSubtypeOf(refinedType)) {
            return unrefinedType;
            }
            return ctx.currentScope().typeInContext(refinedType);
        };
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "exactly-one"),
            List.of(anyItemsRequiredInput),
            typeFactory.one(typeFactory.itemAnyItem()),
            null,
            false,
            false,
            null,
            exactlyOneAnalysis
            )
        );

        // fn:node-name($node as node()? := .) as xs:QName?
        ArgumentSpecification optionalNodeArg = new ArgumentSpecification(
            "node",
            typeFactory.zeroOrOne(typeFactory.itemAnyNode()),
            CONTEXT_VALUE
        );
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "node-name"),
            List.of(optionalNodeArg),
            typeFactory.zeroOrOne(typeFactory.itemString()),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:nilled($node as node()? := .) as xs:boolean?
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "nilled"),
            List.of(optionalNodeArg),
            typeFactory.zeroOrOne(typeFactory.itemBoolean()),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:string(
        //  as item()? := .
        // ) as xs:string
        final ArgumentSpecification stringValue = new ArgumentSpecification("value", optionalItem, CONTEXT_VALUE);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "string"),
            List.of(stringValue),
            typeFactory.string(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:data(
        //  as item()* := .
        // ) as xs:anyAtomicType*
        final ArgumentSpecification dataInput = new ArgumentSpecification(
            "input", zeroOrMoreItems, CONTEXT_VALUE);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "data"),
            List.of(dataInput),
            zeroOrMoreItems,
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:base-uri($node as node()? := .) as xs:anyURI?
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "base-uri"),
            List.of(optionalNodeArg),
            typeFactory.zeroOrOne(typeFactory.itemString()),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:document-uri($node as node()? := .) as xs:anyURI?
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "document-uri"),
            List.of(optionalNodeArg),
            typeFactory.zeroOrOne(typeFactory.itemString()),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:root($node as node()? := .) as node()?
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "root"),
            List.of(optionalNodeArg),
            typeFactory.zeroOrOne(typeFactory.itemAnyNode()),
            null,
            false,
            false,
            null,
            null
            )
        );

        final ArgumentSpecification mapOptionsArg = new ArgumentSpecification(
            "options",
            typeFactory.zeroOrOne(typeFactory.itemAnyMap()),
            EMPTY_MAP
        );

        // fn:path($node as node()? := ., $options as map(*)? := {}) as xs:string?
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "path"),
            List.of(optionalNodeArg, mapOptionsArg),
            typeFactory.zeroOrOne(typeFactory.itemString()),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:has-children($node as node()? := .) as xs:boolean
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "has-children"),
            List.of(optionalNodeArg),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:siblings( $node as node()? := .) as node()*
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "siblings"),
            List.of(optionalNodeArg),
            zeroOrMoreNodes,
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:distinct-ordered-nodes($nodes as node()*) as node()*
        final ArgumentSpecification nodesArg = new ArgumentSpecification(
            "nodes",
            zeroOrMoreNodes,
            null
        );
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "distinct-ordered-nodes"),
            List.of(nodesArg),
            zeroOrMoreNodes,
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:innermost($nodes as node()*) as node()*
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "innermost"),
            List.of(nodesArg),
            zeroOrMoreNodes,
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:outermost($nodes as node()*) as node()*
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "outermost"),
            List.of(nodesArg),
            zeroOrMoreNodes,
            null,
            false,
            false,
            null,
            null
            )
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
            CONTEXT_VALUE
        );
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "error"),
            List.of(errorCode, errorDescription, errorValue),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null,
            false,
            false,
            null,
            null
            )
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
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "trace"),
            List.of(traceInput, traceLabel),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null,
            false,
            false,
            null,
            null
            )
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
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "message"),
            List.of(messageInput, messageLabel),
            typeFactory.emptySequence(),
            null,
            false,
            false,
            null,
            null
            )
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
            TEN
        );
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "parse-integer"),
            List.of(parseIntValue, parseIntRadix),
            typeFactory.zeroOrOne(typeFactory.itemNumber()),
            null,
            false,
            false,
            null,
            null
            )
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
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "format-integer"),
            List.of(fmtIntValue, pictureString, optionalLangugae),
            typeFactory.string(),
            null,
            false,
            false,
            null,
            null
            )
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
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "format-number"),
            List.of(fmtNumValue, pictureString, fmtNumOptions),
            typeFactory.string(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:codepoints-to-string(
        //  as xs:integer*
        // ) as xs:string
        final ArgumentSpecification cpsValues = new ArgumentSpecification("values", zeroOrMoreNumbers, null);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "codepoints-to-string"),
            List.of(cpsValues),
            typeFactory.string(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:string-to-codepoints(
        //  as xs:string?
        // ) as xs:integer*
        final ArgumentSpecification stcpValue = new ArgumentSpecification("value", optionalString, null);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "string-to-codepoints"),
            List.of(stcpValue),
            typeFactory.zeroOrMore(typeFactory.itemNumber()),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:codepoint-equal(
        //  as xs:string?,
        //  as xs:string?
        // ) as xs:boolean?
        final ArgumentSpecification cpEq1 = new ArgumentSpecification("value1", optionalString, null);
        final ArgumentSpecification cpEq2 = new ArgumentSpecification("value2", optionalString, null);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "codepoint-equal"),
            List.of(cpEq1, cpEq2),
            typeFactory.zeroOrOne(typeFactory.itemBoolean()),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:collation(
        //  as map(*)
        // ) as xs:string
        final ArgumentSpecification collationOpts = new ArgumentSpecification("options", typeFactory.one(typeFactory.itemAnyMap()), null);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "collation"),
            List.of(collationOpts),
            typeFactory.string(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:collation-available(
        //  as xs:string,
        //  as enum('compare','key','substring')* := ()
        // ) as xs:boolean
        final ArgumentSpecification colAvailColl = new ArgumentSpecification("collation", typeFactory.string(), null);
        final ArgumentSpecification colAvailUsage = new ArgumentSpecification("usage",
            typeFactory.zeroOrMore(typeFactory.itemEnum(Set.of("compare", "key", "substring"))),
            EMPTY_SEQUENCE);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "collation-available"),
            List.of(colAvailColl, colAvailUsage),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:contains-token(
        //  as xs:string*,
        //  as xs:string,
        //  as xs:string? := fn:default-collation()
        // ) as xs:boolean
        final ArgumentSpecification ctValue = new ArgumentSpecification("value", typeFactory.zeroOrMore(typeFactory.itemString()), null);
        final ArgumentSpecification ctToken = new ArgumentSpecification("token", typeFactory.string(), null);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "contains-token"),
            List.of(ctValue, ctToken, optionalCollation),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:char(
        //  as (xs:string | xs:positiveInteger)
        // ) as xs:string
        XQuerySequenceType stringOrNumber = typeFactory.choice(List.of(typeFactory.itemString(), typeFactory.itemNumber()));
        ArgumentSpecification charVal = new ArgumentSpecification("value", stringOrNumber, null);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "char"),
            List.of(charVal),
            typeFactory.string(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:characters( as xs:string?) as xs:string*
        final ArgumentSpecification charactersValue = new ArgumentSpecification("value", optionalString, null);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "characters"),
            List.of(charactersValue),
            typeFactory.zeroOrMore(typeFactory.itemString()),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:graphemes( as xs:string?) as xs:string*
        final ArgumentSpecification graphemesValue = new ArgumentSpecification("value", optionalString, null);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "graphemes"),
            List.of(graphemesValue),
            typeFactory.zeroOrMore(typeFactory.itemString()),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:concat(
        //  as xs:anyAtomicType* := ()
        // ) as xs:string
        final ArgumentSpecification concatValues = new ArgumentSpecification("values", zeroOrMoreItems, EMPTY_SEQUENCE);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "concat"),
            List.of(concatValues),
            typeFactory.string(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:string-join(
        //  as xs:anyAtomicType* := (),
        //  as xs:string? := ""
        // ) as xs:string
        final ArgumentSpecification joinValues = new ArgumentSpecification("values", zeroOrMoreItems, EMPTY_SEQUENCE);
        final ArgumentSpecification separator = new ArgumentSpecification("separator", optionalString, EMPTY_STRING);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "string-join"),
            List.of(joinValues, separator),
            typeFactory.string(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:substring(
        //  as xs:string?,
        //  as xs:double,
        //  as xs:double? := ()
        // ) as xs:string
        final ArgumentSpecification substrValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification substrStart = new ArgumentSpecification("start", typeFactory.number(), null);
        final ArgumentSpecification substrLength = new ArgumentSpecification("length", optionalNumber, new ParenthesizedExprContext(null, 0));
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "substring"),
            List.of(substrValue, substrStart, substrLength),
            typeFactory.string(),
            null,
            false,
            false,
            null,
            null
            )
        );
        // fn:string-length(
        //  as xs:string? := fn:string(.)
        // ) as xs:integer
        final ArgumentSpecification lengthValue = new ArgumentSpecification("value", optionalString, STRING_AT_CONTEXT_VALUE);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "string-length"),
            List.of(lengthValue),
            typeFactory.number(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:normalize-space( as xs:string? := fn:string(.)) as xs:string
        final ArgumentSpecification nsValue = new ArgumentSpecification("value", optionalString, STRING_AT_CONTEXT_VALUE);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "normalize-space"),
            List.of(nsValue),
            typeFactory.string(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:normalize-unicode(
        //  as xs:string?,
        //  as xs:string? := "NFC"
        // ) as xs:string
        final ArgumentSpecification nuValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification nuForm = new ArgumentSpecification("form", optionalString, NFC);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "normalize-unicode"),
            List.of(nuValue, nuForm),
            typeFactory.string(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:upper-case( as xs:string?) as xs:string
        final ArgumentSpecification ucValue = new ArgumentSpecification("value", optionalString, null);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "upper-case"),
            List.of(ucValue),
            typeFactory.string(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:lower-case( as xs:string?) as xs:string
        final ArgumentSpecification lcValue = new ArgumentSpecification("value", optionalString, null);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "lower-case"),
            List.of(lcValue),
            typeFactory.string(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:translate(
        //  as xs:string?,
        //  as xs:string,
        //  as xs:string
        // ) as xs:string
        final ArgumentSpecification trValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification trFrom = new ArgumentSpecification("replace", typeFactory.string(), null);
        final ArgumentSpecification trTo = new ArgumentSpecification("with", typeFactory.string(), null);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "translate"),
            List.of(trValue, trFrom, trTo),
            typeFactory.string(),
            null,
            false,
            false,
            null,
            null
            )
        );

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
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "contains"),
            List.of(cValue, cSubstr, cColl),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:starts-with(
        //  as xs:string?,
        //  as xs:string?,
        //  as xs:string? := fn:default-collation()
        // ) as xs:boolean
        final ArgumentSpecification swValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification swSubstring = new ArgumentSpecification("substring", optionalString, null);
        final ArgumentSpecification swCollation = optionalCollation;
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "starts-with"),
            List.of(swValue, swSubstring, swCollation),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:ends-with(
        //  as xs:string?,
        //  as xs:string?,
        //  as xs:string? := fn:default-collation()
        // ) as xs:boolean
        final ArgumentSpecification ewValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification ewSubstring = new ArgumentSpecification("substring", optionalString, null);
        final ArgumentSpecification ewCollation = optionalCollation;
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "ends-with"),
            List.of(ewValue, ewSubstring, ewCollation),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:substring-before(
        //  as xs:string?,
        //  as xs:string?,
        //  as xs:string? := fn:default-collation()
        // ) as xs:string
        final ArgumentSpecification sbValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification sbSubstring = new ArgumentSpecification("substring", optionalString, null);
        final ArgumentSpecification sbCollation = optionalCollation;
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "substring-before"),
            List.of(sbValue, sbSubstring, sbCollation),
            typeFactory.string(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:substring-after(
        //  as xs:string?,
        //  as xs:string?,
        //  as xs:string? := fn:default-collation()
        // ) as xs:string
        final ArgumentSpecification saValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification saSubstring = new ArgumentSpecification("substring", optionalString, null);
        final ArgumentSpecification saCollation = optionalCollation;
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "substring-after"),
            List.of(saValue, saSubstring, saCollation),
            typeFactory.string(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:matches(
        //  as xs:string?,
        //  as xs:string,
        //  as xs:string? := ""
        // ) as xs:boolean
        final ArgumentSpecification optionalStringRequiredValue = new ArgumentSpecification("value", optionalString, null);
        final ArgumentSpecification pattern = new ArgumentSpecification("pattern", typeFactory.string(), null);
        final ArgumentSpecification flags = new ArgumentSpecification("flags", optionalString, EMPTY_STRING);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "matches"),
            List.of(optionalStringRequiredValue, pattern, flags),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:replace(
        // 	as xs:string?,
        // 	as xs:string,
        // 	as (xs:string | fn(xs:untypedAtomic, xs:untypedAtomic*) as item()?)?	:= (),
        // 	as xs:string?	:= ''
        // ) as xs:string
        final XQueryItemType dynamicReplacement = typeFactory.itemFunction(optionalItem, List.of(typeFactory.anyItem(), zeroOrMoreItems));
        final var replacementType = typeFactory.choice(List.of(typeFactory.itemString(), dynamicReplacement));
        final ArgumentSpecification replacement = new ArgumentSpecification("replacement", replacementType, EMPTY_SEQUENCE);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "replace"),
            List.of(optionalStringRequiredValue, pattern, replacement, flags),
            typeFactory.string(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:tokenize(
        //  as xs:string?,
        //  as xs:string? := (),
        //  as xs:string? := ""
        // ) as xs:string*
        final ArgumentSpecification optionalPattern = new ArgumentSpecification("pattern", optionalString, EMPTY_SEQUENCE);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "tokenize"),
            List.of(optionalStringRequiredValue, optionalPattern, flags),
            typeFactory.zeroOrMore(typeFactory.itemString()),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:analyze-string(
        //  as xs:string?,
        //  as xs:string,
        //  as xs:string? := ""
        // ) as element(fn:analyze-string-result)
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "analyze-string"),
            List.of(optionalStringRequiredValue, pattern, flags),
            typeFactory.one(
                typeFactory.itemElement(
                    Set.of(
                        new QualifiedName("fn", "analyze-string-result")
                    )
                )
            ),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:true() as xs:boolean
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "true"),
            List.of(),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:false() as xs:boolean
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "false"),
            List.of(),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // fn:boolean( as item()*) as xs:boolean
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "boolean"),
            List.of(anyItemsRequiredInput),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );
        // fn:not( as item()*) as xs:boolean
        GrainedAnalysis notAnalysis =
            (args, _, _, typeContext) -> {
                var scope = typeContext.currentScope();
                var returned = typeContext.typeInContext(typeFactory.boolean_());
                scope.imply(returned, new NotImplication(returned, args.get(0).type(), false));
                scope.imply(returned, new NotImplication(returned, args.get(0).type(), true));
                return returned;
            };
        fn.add(
            new SimplifiedFunctionSpecification(
                new QualifiedName("fn", "not"),
                List.of(argItems),
                typeFactory.boolean_(),
                null,
                false,
                false,
                null,
                notAnalysis
            )
        );

        // fn:empty( as item()*) as xs:boolean
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "empty"),
            List.of(anyItemsRequiredInput),
            typeFactory.boolean_(),
            null, false, false, null, null
        ));

        // fn:exists( as item()*) as xs:boolean
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "exists"),
            List.of(anyItemsRequiredInput),
            typeFactory.boolean_(),
            null, false, false, null, null
        ));

        // fn:foot( as item()*) as item()?
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "foot"),
            List.of(anyItemsRequiredInput),
            optionalItem,
            null, false, false, null, null
        ));

        // fn:head( as item()*) as item()?
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "head"),
            List.of(anyItemsRequiredInput),
            optionalItem,
            null, false, false, null, null
        ));

        // fn:identity( as item()*) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "identity"),
            List.of(anyItemsRequiredInput),
            zeroOrMoreItems,
            null, false, false, null, null
        ));

        // fn:insert-before(
        // 	as item()*,
        // 	as xs:integer,
        // 	as item()*
        // ) as item()*
        final ArgumentSpecification position = new ArgumentSpecification("position", typeFactory.number(), null);
        final ArgumentSpecification insert = new ArgumentSpecification("insert", zeroOrMoreItems, null);
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "insert-before"),
            List.of(anyItemsRequiredInput, position, insert),
            zeroOrMoreItems,
            null, false, false, null, null
        ));

        // fn:items-at( as item()*,  as xs:integer*) as item()*
        final ArgumentSpecification at = new ArgumentSpecification("at",
            zeroOrMoreNumbers, null);
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "items-at"),
            List.of(anyItemsRequiredInput, at),
            zeroOrMoreItems,
            null, false, false, null, null
        ));

        // fn:replicate( as item()*,  as xs:nonNegativeInteger) as item()*
        final ArgumentSpecification count = new ArgumentSpecification("count", typeFactory.number(), null);
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "replicate"),
            List.of(anyItemsRequiredInput, count),
            zeroOrMoreItems,
            null, false, false, null, null
        ));

        final ArgumentSpecification positions = new ArgumentSpecification("positions", zeroOrMoreNumbers, null);

        // fn:remove(
        // 	as item()*,
        // 	as xs:integer*
        // ) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "remove"),
            List.of(anyItemsRequiredInput, positions),
            zeroOrMoreItems,
            null, false, false, null, null
        ));

        // fn:reverse(
        // 	as item()*
        // ) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "reverse"),
            List.of(anyItemsRequiredInput),
            zeroOrMoreItems,
            null, false, false, null, null
        ));

        // fn:sequence-join( as item()*,  as item()*) as item()*
        final ArgumentSpecification seqJoinSeparator = new ArgumentSpecification("separator", zeroOrMoreItems, null);
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "sequence-join"),
            List.of(anyItemsRequiredInput, seqJoinSeparator),
            zeroOrMoreItems,
            null, false, false, null, null
        ));

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
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "slice"),
            List.of(anyItemsRequiredInput, sliceStart, sliceEnd, sliceStep),
            zeroOrMoreItems,
            null, false, false, null, null
        ));

        // fn:subsequence( as item()*,  as xs:double,  as xs:double?
        // := ()) as item()*
        final ArgumentSpecification subStart = new ArgumentSpecification("start",
            typeFactory.number(), null);
        final ArgumentSpecification subLength = new ArgumentSpecification("length",
            optionalNumber, EMPTY_SEQUENCE);
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "subsequence"),
            List.of(anyItemsRequiredInput, subStart, subLength),
            zeroOrMoreItems,
            null, false, false, null, null
        ));

        // fn:tail( as item()*) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "tail"),
            List.of(anyItemsRequiredInput),
            zeroOrMoreItems,
            null, false, false, null, null
        ));

        // fn:trunk( as item()*) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "trunk"),
            List.of(anyItemsRequiredInput),
            zeroOrMoreItems,
            null, false, false, null, null
        ));

        // fn:unordered( as item()*) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "unordered"),
            List.of(anyItemsRequiredInput),
            zeroOrMoreItems,
            null, false, false, null, null
        ));

        // fn:void( as item()* := ()) as empty-sequence()
        final ArgumentSpecification voidInput = new ArgumentSpecification("input", zeroOrMoreItems, EMPTY_SEQUENCE);
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "void"),
            List.of(voidInput),
            typeFactory.emptySequence(),
            null, false, false, null, null
        ));

        // fn:atomic-equal( as xs:anyAtomicType,  as xs:anyAtomicType) as
        // xs:boolean
        final ArgumentSpecification arg_value1_anyItem = new ArgumentSpecification("value1", typeFactory.anyItem(), null);
        final ArgumentSpecification arg_value2_anyItem  = new ArgumentSpecification("value2", typeFactory.anyItem(), null);
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "atomic-equal"),
            List.of(arg_value1_anyItem, arg_value2_anyItem),
            typeFactory.boolean_(),
            null, false, false, null, null
        ));

        // fn:deep-equal( as item()*,  as item()*,  as
        // (xs:string|map(*))? := {}) as xs:boolean
        final ArgumentSpecification arg_value1_anyItems = new ArgumentSpecification("value1", zeroOrMoreItems, null);
        final ArgumentSpecification arg_value2_anyItems  = new ArgumentSpecification("value2", zeroOrMoreItems, null);
        final var stringOrMap = typeFactory.zeroOrOne(typeFactory.itemChoice(Set.of(typeFactory.itemString(), typeFactory.itemAnyMap())));
        final ArgumentSpecification optionalOptions = new ArgumentSpecification("options", stringOrMap, EMPTY_MAP);
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "deep-equal"),
            List.of(arg_value1_anyItems, arg_value2_anyItems, optionalOptions),
            typeFactory.boolean_(),
            null, false, false, null, null
        ));

        // fn:compare( as xs:anyAtomicType?,  as xs:anyAtomicType?,
        //  as xs:string? := fn:default-collation()) as xs:integer?
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "compare"),
            List.of(arg_value1_anyItem, arg_value2_anyItem, optionalCollation),
            typeFactory.zeroOrOne(typeFactory.itemNumber()),
            null, false, false, null, null
        ));

        // fn:distinct-values(
        //  as xs:anyAtomicType*,
        //  as xs:string? := fn:default-collation()
        // ) as xs:anyAtomicType*
        final ArgumentSpecification required_arg_values_anyItems = new ArgumentSpecification("values", zeroOrMoreItems, null);
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "distinct-values"),
            List.of(required_arg_values_anyItems, optionalCollation),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
        ));

        // fn:duplicate-values(
        //  as xs:anyAtomicType*,
        //  as xs:string? := fn:default-collation()
        // ) as xs:anyAtomicType*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "duplicate-values"),
            List.of(required_arg_values_anyItems, optionalCollation),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
        ));

        // fn:index-of(
        //  as xs:anyAtomicType*,
        //  as xs:anyAtomicType,
        //  as xs:string? := fn:default-collation()
        // ) as xs:integer*
        final ArgumentSpecification required_arg_target_anyItem = new ArgumentSpecification("target", typeFactory.one(typeFactory.itemAnyItem()), null);
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "index-of"),
            List.of(anyItemsRequiredInput, required_arg_target_anyItem, optionalCollation),
            typeFactory.zeroOrMore(typeFactory.itemNumber()),
            null, false, false, null, null
        ));

        // fn:starts-with-subsequence(
        //  as item()*,
        //  as item()*,
        //  as (fn(item(),item()) as xs:boolean?)? := fn:deep-equal#2
        // ) as xs:boolean
        final ArgumentSpecification required_arg_subsequence_anyItems = new ArgumentSpecification("subsequence", zeroOrMoreItems, null);
        final var comparator = typeFactory.zeroOrOne(typeFactory.itemFunction(typeFactory.boolean_(),
            List.of(typeFactory.anyItem(), typeFactory.anyItem())));
        final ArgumentSpecification optional_arg_compare_comparator = new ArgumentSpecification("compare", comparator, DEFAULT_COMPARATOR);
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "starts-with-subsequence"),
            List.of(anyItemsRequiredInput, required_arg_subsequence_anyItems, optional_arg_compare_comparator),
            typeFactory.boolean_(),
            null, false, false, null, null
        ));

        // fn:ends-with-subsequence(
        //  as item()*,
        //  as item()*,
        //  as (fn(item(),item()) as xs:boolean?)? := fn:deep-equal#2
        // ) as xs:boolean
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "ends-with-subsequence"),
            List.of(anyItemsRequiredInput, required_arg_subsequence_anyItems, optional_arg_compare_comparator),
            typeFactory.boolean_(),
            null, false, false, null, null
        ));

        // fn:contains-subsequence(
        //  as item()*,
        //  as item()*,
        //  as (fn(item(),item()) as xs:boolean?)? := fn:deep-equal#2
        // ) as xs:boolean
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "contains-subsequence"),
            List.of(anyItemsRequiredInput, required_arg_subsequence_anyItems, optional_arg_compare_comparator),
            typeFactory.boolean_(),
            null, false, false, null, null
        ));

        // fn:count( as item()*) as xs:integer
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "count"),
            List.of(anyItemsRequiredInput),
            typeFactory.number(),
            null, false, false, null, null
        ));

        // fn:avg( as xs:anyAtomicType*) as xs:anyAtomicType?
        final ArgumentSpecification anyItemValues = new ArgumentSpecification("values", zeroOrMoreItems, null);
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "avg"),
            List.of(anyItemValues),
            optionalItem,
            null, false, false, null, null
        ));

        // fn:max(
        //  as xs:anyAtomicType*,
        //  as xs:string? := fn:default-collation()
        // ) as xs:anyAtomicType?
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "max"),
            List.of(anyItemValues, optionalCollation),
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()),
            null, false, false, null, null
        ));

        // fn:min(
        //  as xs:anyAtomicType*,
        //  as xs:string? := fn:default-collation()
        // ) as xs:anyAtomicType?
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "min"),
            List.of(anyItemValues, optionalCollation),
            optionalItem,
            null, false, false, null, null
        ));

        // fn:sum(
        //  as xs:anyAtomicType*,
        //  as xs:anyAtomicType? := 0
        // ) as xs:anyAtomicType?
        final ArgumentSpecification sumZero = new ArgumentSpecification("zero", optionalItem, ZERO_LITERAL);
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "sum"),
            List.of(anyItemValues, sumZero),
            optionalItem,
            null, false, false, null, null
        ));

        // fn:all-equal(
        //  as xs:anyAtomicType*,
        //  as xs:string? := fn:default-collation()
        // ) as xs:boolean
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "all-equal"),
            List.of(anyItemValues, optionalCollation),
            typeFactory.boolean_(),
            null, false, false, null, null
        ));

        // fn:all-different(
        //  as xs:anyAtomicType*,
        //  as xs:string? := fn:default-collation()
        // ) as xs:boolean
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "all-different"),
            List.of(anyItemValues, optionalCollation),
            typeFactory.boolean_(),
            null, false, false, null, null
        ));

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
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "doc"),
            List.of(sourceArgNonDefault, docOptions),
            typeFactory.zeroOrOne(typeFactory.itemAnyNode()),
            null, false, false, null, null
        ));

        // fn:doc-available($source as xs:string?, $options as map(*)? := {}) as xs:boolean
        ArgumentSpecification docAvailOptions = new ArgumentSpecification(
            "options",
            typeFactory.zeroOrOne(typeFactory.itemAnyMap()),
            EMPTY_MAP
        );
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "doc-available"),
            List.of(sourceArgNonDefault, docAvailOptions),
            typeFactory.boolean_(),
            null, false, false, null, null
        ));

        // fn:collection($source as xs:string? := ()) as item()*
        ArgumentSpecification colSource = new ArgumentSpecification(
            "source",
            typeFactory.zeroOrOne(typeFactory.itemString()),
            EMPTY_SEQUENCE
        );
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "collection"),
            List.of(colSource),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
        ));

        // fn:unparsed-text($source as xs:string?, $options as (xs:string|map(*))? := ()) as xs:string?
        ArgumentSpecification utOptions = new ArgumentSpecification(
            "options",
            typeFactory.zeroOrOne(typeFactory.itemChoice(Set.of(typeFactory.itemString(),
                                    typeFactory.itemAnyMap()))),
            EMPTY_SEQUENCE
        );
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "unparsed-text"),
            List.of(sourceArgNonDefault, utOptions),
            typeFactory.zeroOrOne(typeFactory.itemString()),
            null, false, false, null, null
        ));

        // fn:unparsed-text-lines($source as xs:string?, $options as (xs:string|map(*))? := ()) as xs:string*
        ArgumentSpecification utlOptions = new ArgumentSpecification(
            "options",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()),
            EMPTY_SEQUENCE
        );
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "unparsed-text-lines"),
            List.of(sourceArgNonDefault, utlOptions),
            typeFactory.zeroOrMore(typeFactory.itemString()),
            null, false, false, null, null
        ));

        // fn:unparsed-text-available($source as xs:string?, $options as (xs:string|map(*))? := ()) as xs:boolean
        ArgumentSpecification utaOptions = new ArgumentSpecification(
            "options",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()),
            EMPTY_SEQUENCE
        );
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "unparsed-text-available"),
            List.of(sourceArgNonDefault, utaOptions),
            typeFactory.boolean_(),
            null, false, false, null, null
        ));

        // fn:environment-variable($name as xs:string) as xs:string?
        ArgumentSpecification envName = new ArgumentSpecification(
            "name",
            typeFactory.string(),
            null
        );
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "environment-variable"),
            List.of(envName),
            typeFactory.zeroOrOne(typeFactory.itemString()),
            null, false, false, null, null
        ));

        // fn:available-environment-variables() as xs:string*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "available-environment-variables"),
            List.of(),
            typeFactory.zeroOrMore(typeFactory.itemString()),
            null, false, false, null, null
        ));

        // fn:position() as xs:integer
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "position"),
            List.of(),
            typeFactory.number(),
            null,
            true,
            false,
            null,
            null
        ));


        // fn:last() as xs:integer
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "last"),
            List.of(),
            typeFactory.number(),
            null,
            false,
            true,
            null,
            null
        ));


        // fn:current-dateTime() as xs:dateTimeStamp
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "current-dateTime"),
            List.of(),
            typeFactory.string(),
            null, false, false, null, null
        ));

        // fn:current-date() as xs:date
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "current-date"),
            List.of(),
            typeFactory.string(),
            null, false, false, null, null
        ));

        // fn:current-time() as xs:time
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "current-time"),
            List.of(),
            typeFactory.string(),
            null, false, false, null, null
        ));

        // fn:implicit-timezone() as xs:dayTimeDuration
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "implicit-timezone"),
            List.of(),
            typeFactory.string(),
            null, false, false, null, null
        ));

        // fn:default-collation() as xs:string
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "default-collation"),
            List.of(),
            typeFactory.string(),
            null, false, false, null, null
        ));

        // fn:default-language() as xs:language
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "default-language"),
            List.of(),
            typeFactory.string(),
            null, false, false, null, null
        ));

        // fn:function-lookup($name as xs:QName, $arity as xs:integer) as function(*)?
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "function-lookup"),
            List.of(
            new ArgumentSpecification("name", typeFactory.one(typeFactory.itemString()), null),
            new ArgumentSpecification("arity", typeFactory.one(typeFactory.itemNumber()), null)
            ),
            typeFactory.zeroOrOne(typeFactory.itemAnyFunction()),
            null, false, false, null, null
        ));

        // fn:function-name($function as function(*)) as xs:QName?
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "function-name"),
            List.of(
            new ArgumentSpecification("function", typeFactory.one(typeFactory.itemAnyFunction()), null)
            ),
            typeFactory.zeroOrOne(typeFactory.itemString()),
            null, false, false, null, null
        ));

        // fn:function-arity($function as function(*)) as xs:integer
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "function-arity"),
            List.of(
            new ArgumentSpecification("function", typeFactory.one(typeFactory.itemAnyFunction()), null)
            ),
            typeFactory.one(typeFactory.itemNumber()),
            null, false, false, null, null
        ));

        // fn:function-identity($function as function(*)) as xs:string
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "function-identity"),
            List.of(
            new ArgumentSpecification("function", typeFactory.one(typeFactory.itemAnyFunction()), null)
            ),
            typeFactory.string(),
            null, false, false, null, null
        ));

        // fn:function-annotations( $function as fn(*) ) as map(xs:QName, xs:anyAtomicType*)*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "function-annotations"),
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
            ),
            null, false, false, null, null
        ));

        // fn:apply($function as function(*), $arguments as array(*)) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "apply"),
            List.of(
            new ArgumentSpecification("function", typeFactory.one(typeFactory.itemAnyFunction()), null),
            new ArgumentSpecification("arguments", typeFactory.one(typeFactory.itemAnyArray()), null)
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
        ));

        // fn:do-until($input as item()*, $action as function(item()*, xs:integer) as item()*, $predicate as function(item()*, xs:integer) as xs:boolean?) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "do-until"),
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
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
        ));

        // fn:every($input as item()*, $predicate as function(item(), xs:integer) as xs:boolean? := fn:boolean#1) as xs:boolean
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "every"),
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
            typeFactory.boolean_(),
            null, false, false, null, null
        ));

        // fn:filter($input as item()*, $predicate as function(item(), xs:integer) as xs:boolean?) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "filter"),
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
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
        ));

        // fn:fold-left($input as item()*, $init as item()*, $action as function(item()*, item()) as item()*) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "fold-left"),
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
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
        ));

        // fn:fold-right($input as item()*, $init as item()*, $action as function(item(), item()*) as item()*) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "fold-right"),
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
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
        ));

        // fn:for-each($input as item()*, $action as function(item(), item()*) as item()*) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "for-each"),
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
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
        ));

        // fn:for-each-pair($input1 as item()*, $input2 as item()*, $action as function(item(), item(), xs:integer) as item()*) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "for-each-pair"),
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
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
        ));

        // fn:highest($input as item()*, $collation as xs:string? := fn:default-collation(), $key as function(item()) as xs:anyAtomicType*)? := fn:data#1) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "highest"),
            List.of(
            new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
            new ArgumentSpecification("collation", typeFactory.zeroOrOne(typeFactory.itemString()), DEFAULT_COLLATION),
            new ArgumentSpecification("key", typeFactory.zeroOrOne(typeFactory.itemFunction(
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                List.of(typeFactory.one(typeFactory.itemAnyItem()))
            )), DATA$1)
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
        ));

        // fn:index-where($input as item()*, $predicate as function(item(), xs:integer) as xs:boolean?) as xs:integer*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "index-where"),
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
            typeFactory.zeroOrMore(typeFactory.itemNumber()),
            null, false, false, null, null
        ));

        // fn:lowest($input as item()*, $collation as xs:string? := fn:default-collation(), $key as function(item()) as xs:anyAtomicType*)? := fn:data#1) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "lowest"),
            List.of(
            new ArgumentSpecification("input", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
            new ArgumentSpecification("collation", typeFactory.zeroOrOne(typeFactory.itemString()), DEFAULT_COLLATION),
            new ArgumentSpecification("key", typeFactory.zeroOrOne(typeFactory.itemFunction(
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                List.of(typeFactory.one(typeFactory.itemAnyItem()))
            )), DATA$1)
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
        ));

        // fn:partial-apply($function as function(*), $arguments as map(xs:positiveInteger, item()*)) as function(*)
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "partial-apply"),
            List.of(
            new ArgumentSpecification("function", typeFactory.one(typeFactory.itemAnyFunction()), null),
            new ArgumentSpecification("arguments", typeFactory.one(
                typeFactory.itemMap(typeFactory.itemNumber(), typeFactory.zeroOrMore(typeFactory.itemAnyItem()))
            ), null)
            ),
            typeFactory.one(typeFactory.itemAnyFunction()),
            null, false, false, null, null
        ));

        // fn:partition($input as item()*, $split-when as function(item()*, item(), xs:integer) as xs:boolean?) as array(item())*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "partition"),
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
            typeFactory.zeroOrMore(typeFactory.itemAnyArray()),
            null, false, false, null, null
        ));

        // fn:scan-left(...) and fn:scan-right(...) mirror fold-left and fold-right
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "scan-left"),
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
            typeFactory.zeroOrMore(typeFactory.itemAnyArray()),
            null, false, false, null, null
        ));

        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "scan-right"),
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
            typeFactory.zeroOrMore(typeFactory.itemAnyArray()),
            null, false, false, null, null
        ));

        // fn:some($input as item()*, $predicate as function(item(), xs:integer) as xs:boolean?) := fn:boolean#1) as xs:boolean
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "some"),
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
            typeFactory.boolean_(),
            null, false, false, null, null
        ));

        // fn:sort( $input as item()*,
        // $collation as xs:string? := fn:default-collation(),
        // $key as fn(item()) as xs:anyAtomicType* := fn:data#1
        // ) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "sort"),
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
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
        ));

        // TODO: refine parser name tokenization
        // fn:sort-by(
        // $input as item()*,
        // $keys as record(key? as (fn(item()) as xs:anyAtomicType*)?,
        //                 collation? as xs:string?,
        //                 order? as enum('ascending', 'descending')?
        //                 )*
        // ) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "sort-by"),
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
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
        ));

        // fn:sort-with($input as item()*, $comparators as function(item(), item()) as xs:integer*) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "sort-with"),
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
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
        ));

        // fn:subsequence-where($input as item()*,
        //                      $from as fn(item(), xs:integer) as xs:boolean? := true#0,
        //                      $to as fn(item(), xs:integer) as xs:boolean? := false#0
        // ) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "subsequence-where"),
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
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
        ));

        // fn:take-while($input as item()*, $predicate as fn(item(), xs:integer) as xs:boolean?) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "take-while"),
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
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
        ));

        // fn:transitive-closure($node as node()?, $step as fn(node()) as node()*) as node()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "transitive-closure"),
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
            typeFactory.zeroOrMore(typeFactory.itemAnyNode()),
            null, false, false, null, null
        ));

        // fn:while-do($input as item()*, $predicate as fn(item()*, xs:integer) as xs:boolean?, $action as fn(item()*, xs:integer) as item()*) as item()*
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "while-do"),
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
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
        ));

        // fn:op(
        //  as xs:string
        // ) as fn(item()*,item()) as item()*
        final ArgumentSpecification opOperator = new ArgumentSpecification("operator", typeFactory.string(), null);
        fn.add(new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "op"),
            List.of(opOperator),
            typeFactory.one(typeFactory.itemFunction(zeroOrMoreItems, List.of(
            zeroOrMoreItems,
            zeroOrMoreItems
            ))),
            null, false, false, null, null
        ));

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
        ArgumentSpecification mbOptions = mapOptionsArg;
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("map", "build"),
            List.of(mbInput, mbKey, mbValue, mbOptions),
            typeFactory.one(typeFactory.itemAnyMap()),
            null, false, false, null, null
            )
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
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("map", "contains"),
            List.of(mcMap, mcKey),
            typeFactory.boolean_(),
            null, false, false, null, null
            )
        );

        // map:empty($map as map(*)) as xs:boolean
        ArgumentSpecification meMap = new ArgumentSpecification(
            "map",
            typeFactory.one(typeFactory.itemAnyMap()),
            null
        );
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("map", "empty"),
            List.of(meMap),
            typeFactory.boolean_(),
            null, false, false, null, null
            )
        );

        // map:entries($map as map(*)) as map(*)*
        ArgumentSpecification mentMap = new ArgumentSpecification(
            "map",
            typeFactory.one(typeFactory.itemAnyMap()),
            null
        );
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("map", "entries"),
            List.of(mentMap),
            typeFactory.zeroOrMore(typeFactory.itemAnyMap()),
            null, false, false, null, null
            )
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
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("map", "entry"),
            List.of(mentKey, mentValue),
            typeFactory.one(typeFactory.itemAnyMap()),
            null, false, false, null, null
            )
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
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("map", "filter"),
            List.of(mfMap, predicateArg),
            typeFactory.one(typeFactory.itemAnyMap()),
            null, false, false, null, null
            )
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
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("map", "find"),
            List.of(mfindInput, mfindKey),
            typeFactory.one(typeFactory.itemAnyArray()),
            null, false, false, null, null
            )
        );

        // map:for-each($map as map(*), $action as fn(xs:anyAtomicType, item()*) as item()*) as item()*
        ArgumentSpecification mfeMap = new ArgumentSpecification(
            "map",
            typeFactory.one(typeFactory.itemAnyMap()),
            null
        );
        XQuerySequenceType action = typeFactory.function(zeroOrMoreItems, List.of(typeFactory.anyItem(), zeroOrMoreItems));
        ArgumentSpecification mfeAction = new ArgumentSpecification( "action", action, null);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("map", "for-each"),
            List.of(mfeMap, mfeAction),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
            )
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
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("map", "get"),
            List.of(mgMap, mgKey, mgDefault),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
            )
        );

        // map:items($map as map(*)) as item()*
        ArgumentSpecification mitemsMap = new ArgumentSpecification(
            "map",
            typeFactory.one(typeFactory.itemAnyMap()),
            null
        );
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("map", "items"),
            List.of(mitemsMap),
            zeroOrMoreItems,
            null, false, false, null, null
            )
        );

        // map:keys($map as map(*)) as xs:anyAtomicType*
        ArgumentSpecification mkeysMap = new ArgumentSpecification(
            "map",
            typeFactory.one(typeFactory.itemAnyMap()),
            null
        );
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("map", "keys"),
            List.of(mkeysMap),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
            )
        );

        // map:keys-where($map as map(*), $predicate as fn(xs:anyAtomicType, item()*) as xs:boolean?) as xs:anyAtomicType*
        ArgumentSpecification kwMap = new ArgumentSpecification(
            "map",
            typeFactory.one(typeFactory.itemAnyMap()),
            null
        );
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("map", "keys-where"),
            List.of(kwMap, predicateArg),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
            )
        );

        // map:merge($maps as map(*)*, $options as map(*)? := {}) as map(*)
        ArgumentSpecification mmMaps = new ArgumentSpecification(
            "maps",
            typeFactory.zeroOrMore(typeFactory.itemAnyMap()),
            null
        );
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("map", "merge"),
            List.of(mmMaps, mapOptionsArg),
            typeFactory.one(typeFactory.itemAnyMap()),
            null, false, false, null, null
            )
        );

        // map:of-pairs($input as key-value-pair*, $options as map(*)? := {}) as map(*)
        ArgumentSpecification opInput = new ArgumentSpecification(
            "input",
            typeFactory.zeroOrMore(
            typeFactory.itemNamedType(
                new QualifiedName("fn", "key-value-pair")
            ).type()
            ),
            null
        );
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("map", "of-pairs"),
            List.of(opInput, mapOptionsArg),
            typeFactory.one(typeFactory.itemAnyMap()),
            null, false, false, null, null
            )
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
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("map", "pair"),
            List.of(mpKey, mpValue),
            typeFactory.namedType(new QualifiedName("fn", "key-value-pair")).type(),
            null, false, false, null, null
            )
        );

        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("map", "pairs"),
            List.of(
                new ArgumentSpecification(
                "map",
                typeFactory.one(typeFactory.itemAnyMap()),
                null
                )
            ),
            typeFactory.zeroOrMore(
                typeFactory.itemNamedType(new QualifiedName("fn", "key-value-pair")).type()
            ),
            null, false, false, null, null
            )
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
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("map", "put"),
            List.of(mputMap, mputKey, mputValue),
            typeFactory.one(typeFactory.itemAnyMap()),
            null, false, false, null, null
            )
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
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("map", "remove"),
            List.of(mremMap, mremKeys),
            typeFactory.one(typeFactory.itemAnyMap()),
            null, false, false, null, null
            )
        );

        // map:size(
        //   $map as map(*)
        // ) as xs:integer
        ArgumentSpecification msizeMap = new ArgumentSpecification(
            "map",
            typeFactory.one(typeFactory.itemAnyMap()),
            null
        );
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("map", "size"),
            List.of(msizeMap),
            typeFactory.number(),
            null, false, false, null, null
            )
        );

        // fn:element-to-map-plan(
        //  as (document-node() | element(*))*
        // ) as map(xs:string, record(*))
        ArgumentSpecification etmpInput = new ArgumentSpecification("input",
                                        typeFactory.zeroOrMore(typeFactory.itemAnyNode()),
                                        null);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "element-to-map-plan"),
            List.of(etmpInput),
            typeFactory.map(typeFactory.itemString(), typeFactory.anyMap()),
            null, false, false, null, null
            )
        );

        // fn:element-to-map(
        //  as element()?,
        //  as map(*)? := {}
        // ) as map(xs:string, item()?)?
        ArgumentSpecification etmElement = new ArgumentSpecification("element", typeFactory.zeroOrOne(typeFactory.itemAnyNode()), null);
        ArgumentSpecification etmOptions = new ArgumentSpecification("options", typeFactory.zeroOrOne(typeFactory.itemAnyMap()), EMPTY_MAP);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "element-to-map"),
            List.of(etmElement, etmOptions),
            typeFactory.zeroOrOne(typeFactory.itemMap(typeFactory.itemString(), typeFactory.zeroOrOne(typeFactory.itemAnyItem()))),
            null, false, false, null, null
            )
        );

        // array:append($array as array(*), $member as item()*) as array(*)
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "append"),
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
            typeFactory.one(typeFactory.itemAnyArray()),
            null, false, false, null, null
            )
        );

        // array:build($input as item()*, $action as function(item(), xs:integer) as item()* := fn:identity#1) as array(*)
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "build"),
            List.of(
                new ArgumentSpecification( "input", zeroOrMoreItems, null),
                new ArgumentSpecification(
                "action",
                typeFactory.zeroOrOne(typeFactory.itemFunction(zeroOrMoreItems, List.of(typeFactory.anyItem(), typeFactory.number()))),
                IDENTITY$1
                )
            ),
            typeFactory.anyArray(),
            null, false, false, null, null
            )
        );

        // array:empty($array as array(*)) as xs:boolean
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "empty"),
            List.of(
                new ArgumentSpecification(
                "array",
                typeFactory.one(typeFactory.itemAnyArray()),
                null
                )
            ),
            typeFactory.boolean_(),
            null, false, false, null, null
            )
        );

        // array:filter($array as array(*), $predicate as function(item(), xs:integer) as xs:boolean?) as array(*)
        final XQueryItemType itemIntegerActionFunction = typeFactory.itemFunction(optionalBoolean, List.of(typeFactory.anyItem(), typeFactory.number()));
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "filter"),
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
            typeFactory.one(typeFactory.itemAnyArray()),
            null, false, false, null, null
            )
        );

        // array:flatten($input as item()) as item()*
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "flatten"),
            List.of(
                new ArgumentSpecification(
                "input",
                typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
                null
                )
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
            )
        );

        // array:fold-left($array as array(*), $init as item()*, $action as function(item(), item()*) as item()*) as item()*
        final XQueryItemType function_anyItem_zeroOrMoreItems$zeroOrMoreItems = typeFactory.itemFunction(zeroOrMoreItems, List.of(typeFactory.anyItem(), zeroOrMoreItems));
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "fold-left"),
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
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
            )
        );

        // array:fold-right($array as array(*), $init as item()*, $action as function(item(), item()*) as item()*) as item()*
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "fold-right"),
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
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
            )
        );

        // array:foot($array as array(*)) as item()*
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "foot"),
            List.of(
                new ArgumentSpecification(
                "array",
                typeFactory.one(typeFactory.itemAnyArray()),
                null
                )
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
            )
        );

        // array:for-each($array as array(*),
        //                $action as function(item()*, xs:integer) as item()*
        // ) as array(*)
        final XQueryItemType function_zeroOrMoreItems_number$zeroOrMoreItems = typeFactory.itemFunction(zeroOrMoreItems, List.of(zeroOrMoreItems, typeFactory.number()));
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "for-each"),
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
            typeFactory.one(typeFactory.itemAnyArray()),
            null, false, false, null, null
            )
        );

        // array:for-each-pair($array1 as array(*), $array2 as array(*), $action as function(item(), item(), xs:integer) as item()*) as array(*)
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "for-each-pair"),
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
            typeFactory.one(typeFactory.itemAnyArray()),
            null, false, false, null, null
            )
        );

        // array:get($array as array(*), $position as xs:integer) as item()*
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "get"),
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
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
            )
        );

        // array:get($array as array(*), $position as xs:integer, $default as item()*) as item()*
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "get"),
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("position", typeFactory.one(typeFactory.itemNumber()), null),
                new ArgumentSpecification("default", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null)
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
            )
        );

        // array:head($array as array(*)) as item()*
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "head"),
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null)
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
            )
        );

        // array:index-of($array as array(*), $target as item()*, $collation as xs:string? := fn:default-collation()) as xs:integer*
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "index-of"),
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("target", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null),
                new ArgumentSpecification("collation", typeFactory.zeroOrOne(typeFactory.itemString()), DEFAULT_COLLATION)
            ),
            typeFactory.zeroOrMore(typeFactory.itemNumber()),
            null, false, false, null, null
            )
        );

        final XQueryItemType integerPredicate = typeFactory.itemFunction(optionalBoolean, List.of(zeroOrMoreItems, typeFactory.number()));
        // array:index-where($array as array(*), $predicate as function(item()*, xs:integer) as xs:boolean?) as xs:integer*
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "index-where"),
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("predicate", typeFactory.one(integerPredicate), null)
            ),
            typeFactory.zeroOrMore(typeFactory.itemNumber()),
            null, false, false, null, null
            )
        );

        // array:insert-before($array as array(*), $position as xs:integer, $member as item()*) as array(*)
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "insert-before"),
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("position", typeFactory.one(typeFactory.itemNumber()), null),
                new ArgumentSpecification("member", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null)
            ),
            typeFactory.one(typeFactory.itemAnyArray()),
            null, false, false, null, null
            )
        );

        // array:items($array as array(*)) as item()*
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "items"),
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null)
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyItem()),
            null, false, false, null, null
            )
        );

        // array:join($arrays as array(*)*, $separator as array(*)? := ()) as array(*)
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "join"),
            List.of(
                new ArgumentSpecification("arrays", typeFactory.zeroOrMore(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("separator", typeFactory.zeroOrOne(typeFactory.itemAnyArray()), EMPTY_SEQUENCE)
            ),
            typeFactory.one(typeFactory.itemAnyArray()),
            null, false, false, null, null
            )
        );

        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "members"),
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
            ),
            null, false, false, null, null
            )
        );

        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "of-members"),
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
            typeFactory.anyArray(),
            null, false, false, null, null
            )
        );

        // array:put($array as array(*), $position as xs:integer, $member as item()*) as array(*)
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "put"),
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("position", typeFactory.one(typeFactory.itemNumber()), null),
                new ArgumentSpecification("member", typeFactory.zeroOrMore(typeFactory.itemAnyItem()), null)
            ),
            typeFactory.one(typeFactory.itemAnyArray()),
            null, false, false, null, null
            )
        );

        // array:remove($array as array(*), $positions as xs:integer*) as array(*)
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "remove"),
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("positions", typeFactory.zeroOrMore(typeFactory.itemNumber()), null)
            ),
            typeFactory.one(typeFactory.itemAnyArray()),
            null, false, false, null, null
            )
        );

        // array:reverse($array as array(*)) as array(*)
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "reverse"),
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null)
            ),
            typeFactory.one(typeFactory.itemAnyArray()),
            null, false, false, null, null
            )
        );

        // array:size($array as array(*)) as xs:integer
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "size"),
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null)
            ),
            typeFactory.one(typeFactory.itemNumber()),
            null, false, false, null, null
            )
        );

        // array:slice($array as array(*), $start as xs:integer? := (), $end as xs:integer? := (), $step as xs:integer? := ()) as array(*)
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "slice"),
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("start", typeFactory.zeroOrOne(typeFactory.itemNumber()), EMPTY_SEQUENCE),
                new ArgumentSpecification("end", typeFactory.zeroOrOne(typeFactory.itemNumber()), EMPTY_SEQUENCE),
                new ArgumentSpecification("step", typeFactory.zeroOrOne(typeFactory.itemNumber()), EMPTY_SEQUENCE)
            ),
            typeFactory.one(typeFactory.itemAnyArray()),
            null, false, false, null, null
            )
        );

        // array:sort($array as array(*), $collation as xs:string? := fn:default-collation(), $key as function(item()*) as xs:anyAtomicType* := fn:data#1) as array(*)
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "sort"),
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("collation", typeFactory.zeroOrOne(typeFactory.itemString()), DEFAULT_COLLATION),
                new ArgumentSpecification("key",
                typeFactory.zeroOrOne(typeFactory.itemFunction(zeroOrMoreItems, List.of(zeroOrMoreItems))),
                DATA$1)
            ),
            typeFactory.one(typeFactory.itemAnyArray()),
            null, false, false, null, null
            )
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

        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "sort-by"),
            sortByArgs,
            zeroOrMoreItems,
            null, false, false, null, null
            )
        );

        // array:split($array as array(*)) as array(*)*
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "split"),
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null)
            ),
            typeFactory.zeroOrMore(typeFactory.itemAnyArray()),
            null, false, false, null, null
            )
        );

        // array:subarray($array as array(*), $start as xs:integer, $length as xs:integer? := ()) as array(*)
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "subarray"),
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null),
                new ArgumentSpecification("start", typeFactory.one(typeFactory.itemNumber()), null),
                new ArgumentSpecification("length", typeFactory.zeroOrOne(typeFactory.itemNumber()), EMPTY_SEQUENCE)
            ),
            typeFactory.one(typeFactory.itemAnyArray()),
            null, false, false, null, null
            )
        );

        // array:tail($array as array(*)) as array(*)
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "tail"),
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null)
            ),
            typeFactory.one(typeFactory.itemAnyArray()),
            null, false, false, null, null
            )
        );

        // array:trunk($array as array(*)) as array(*)
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("array", "trunk"),
            List.of(
                new ArgumentSpecification("array", typeFactory.one(typeFactory.itemAnyArray()), null)
            ),
            typeFactory.one(typeFactory.itemAnyArray()),
            null, false, false, null, null
            )
        );

        // fn:type-of( as item()*) as xs:string
        final ArgumentSpecification typeOfValue = new ArgumentSpecification("value", zeroOrMoreItems, null);
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "type-of"),
            List.of(typeOfValue),
            typeFactory.string(),
            null, false, false, null, null
            )
        );

        // fn:random-number-generator(
        //   $seed as xs:anyAtomicType? := ()
        // ) as random-number-generator-record
        ArgumentSpecification rngSeed = new ArgumentSpecification(
            "seed",
            typeFactory.zeroOrOne(typeFactory.itemAnyItem()),
            EMPTY_SEQUENCE
        );
        fn.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("fn", "random-number-generator"),
            List.of(rngSeed),
            typeFactory.namedType(new QualifiedName("fn", "random-number-generator-record")).type(),
            null, false, false, null, null
            )
        );

        return fn;
    }


    public static List<SimplifiedFunctionSpecification> OP(XQueryTypeFactory typeFactory) {
        final List<SimplifiedFunctionSpecification> op = new ArrayList<>(400);

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
        op.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("op", "numeric-add"),
            List.of(numericArg1, numericArg2),
            typeFactory.number(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // op:numeric-subtract($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:numeric
        op.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("op", "numeric-subtract"),
            List.of(numericArg1, numericArg2),
            typeFactory.number(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // op:numeric-multiply($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:numeric
        op.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("op", "numeric-multiply"),
            List.of(numericArg1, numericArg2),
            typeFactory.number(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // op:numeric-divide($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:numeric
        op.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("op", "numeric-divide"),
            List.of(numericArg1, numericArg2),
            typeFactory.number(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // op:numeric-integer-divide($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:integer
        op.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("op", "numeric-integer-divide"),
            List.of(numericArg1, numericArg2),
            typeFactory.number(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // op:numeric-mod($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:numeric
        op.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("op", "numeric-mod"),
            List.of(numericArg1, numericArg2),
            typeFactory.number(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // op:numeric-unary-plus($arg as xs:numeric) as xs:numeric
        ArgumentSpecification numericArg = new ArgumentSpecification(
            "arg",
            typeFactory.number(),
            null
        );
        op.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("op", "numeric-unary-plus"),
            List.of(numericArg),
            typeFactory.number(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // op:numeric-unary-minus($arg as xs:numeric) as xs:numeric
        op.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("op", "numeric-unary-minus"),
            List.of(numericArg),
            typeFactory.number(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // op:numeric-equal($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
        op.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("op", "numeric-equal"),
            List.of(numericArg1, numericArg2),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // op:numeric-less-than($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
        op.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("op", "numeric-less-than"),
            List.of(numericArg1, numericArg2),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // op:numeric-greater-than($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
        op.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("op", "numeric-greater-than"),
            List.of(numericArg1, numericArg2),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // op:numeric-less-than-or-equal($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
        op.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("op", "numeric-less-than-or-equal"),
            List.of(numericArg1, numericArg2),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // op:numeric-greater-than-or-equal($arg1 as xs:numeric, $arg2 as xs:numeric) as xs:boolean
        op.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("op", "numeric-greater-than-or-equal"),
            List.of(numericArg1, numericArg2),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );


        // op:boolean-equal( as xs:boolean,  as xs:boolean) as xs:boolean
        final ArgumentSpecification bool1ValueRequired = new ArgumentSpecification("value1", typeFactory.boolean_(), null);
        final ArgumentSpecification bool2ValueRequired = new ArgumentSpecification("value2", typeFactory.boolean_(), null);
        op.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("op", "boolean-equal"),
            List.of(bool1ValueRequired, bool2ValueRequired),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // op:boolean-less-than( as xs:boolean,  as xs:boolean) as xs:boolean
        op.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("op", "boolean-less-than"),
            List.of(bool1ValueRequired, bool2ValueRequired),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // op:boolean-greater-than( as xs:boolean,  as xs:boolean) as xs:boolean
        op.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("op", "boolean-greater-than"),
            List.of(bool1ValueRequired, bool2ValueRequired),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // op:boolean-not-equal( as xs:boolean,  as xs:boolean) as xs:boolean
        op.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("op", "boolean-not-equal"),
            List.of(bool1ValueRequired, bool2ValueRequired),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // op:boolean-less-than-or-equal( as xs:boolean,  as xs:boolean) as xs:boolean
        op.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("op", "boolean-less-than-or-equal"),
            List.of(bool1ValueRequired, bool2ValueRequired),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // op:boolean-greater-than-or-equal( as xs:boolean,  as xs:boolean) as xs:boolean
        op.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("op", "boolean-greater-than-or-equal"),
            List.of(bool1ValueRequired, bool2ValueRequired),
            typeFactory.boolean_(),
            null,
            false,
            false,
            null,
            null
            )
        );

        return op;

    }

    public static List<SimplifiedFunctionSpecification> MATH(XQueryTypeFactory typeFactory) {
        final List<SimplifiedFunctionSpecification> math = new ArrayList<>(50);
        final XQuerySequenceType optionalNumber = typeFactory.zeroOrOne(typeFactory.itemNumber());

        // math:pi() as xs:double
        math.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("math", "pi"),
            List.of(),
            typeFactory.number(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // math:e() as xs:double
        math.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("math", "e"),
            List.of(),
            typeFactory.number(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // math:exp(  as xs:double?  ) as xs:double?
        final ArgumentSpecification expValue = new ArgumentSpecification("value", optionalNumber, null);
        math.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("math", "exp"),
            List.of(expValue),
            optionalNumber,
            null,
            false,
            false,
            null,
            null
            )
        );

        // math:exp10(  as xs:double?  ) as xs:double?
        final ArgumentSpecification exp10Value = new ArgumentSpecification("value", optionalNumber, null);
        math.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("math", "exp10"),
            List.of(exp10Value),
            optionalNumber,
            null,
            false,
            false,
            null,
            null
            )
        );

        // math:log(  as xs:double?  ) as xs:double?
        final ArgumentSpecification logValue = new ArgumentSpecification("value", optionalNumber, null);
        math.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("math", "log"),
            List.of(logValue),
            optionalNumber,
            null,
            false,
            false,
            null,
            null
            )
        );

        // math:log10(  as xs:double?  ) as xs:double?
        final ArgumentSpecification log10Value = new ArgumentSpecification("value", optionalNumber, null);
        math.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("math", "log10"),
            List.of(log10Value),
            optionalNumber,
            null,
            false,
            false,
            null,
            null
            )
        );

        // math:pow(
        //  as xs:double?,
        //  as xs:numeric
        // ) as xs:double?
        final ArgumentSpecification powX = new ArgumentSpecification("x", optionalNumber, null);
        final ArgumentSpecification powY = new ArgumentSpecification("y", typeFactory.number(), null);
        math.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("math", "pow"),
            List.of(powX, powY),
            optionalNumber,
            null,
            false,
            false,
            null,
            null
            )
        );

        // math:sqrt(
        //  as xs:double?
        // ) as xs:double?
        final ArgumentSpecification sqrtValue = new ArgumentSpecification("value", optionalNumber, null);
        math.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("math", "sqrt"),
            List.of(sqrtValue),
            optionalNumber,
            null,
            false,
            false,
            null,
            null
            )
        );

        // math:sin(
        //  as xs:double?
        // ) as xs:double?
        final ArgumentSpecification sinValue = new ArgumentSpecification("radians", optionalNumber, null);
        math.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("math", "sin"),
            List.of(sinValue),
            optionalNumber,
            null,
            false,
            false,
            null,
            null
            )
        );

        // math:cos(
        //  as xs:double?
        // ) as xs:double?
        final ArgumentSpecification cosValue = new ArgumentSpecification("radians", optionalNumber, null);
        math.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("math", "cos"),
            List.of(cosValue),
            optionalNumber,
            null,
            false,
            false,
            null,
            null
            )
        );

        // math:tan(
        //  as xs:double?
        // ) as xs:double?
        final ArgumentSpecification tanValue = new ArgumentSpecification("radians", optionalNumber, null);
        math.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("math", "tan"),
            List.of(tanValue),
            optionalNumber,
            null,
            false,
            false,
            null,
            null
            )
        );

        // math:asin(
        //  as xs:double?
        // ) as xs:double?
        final ArgumentSpecification asinValue = new ArgumentSpecification("value", optionalNumber, null);
        math.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("math", "asin"),
            List.of(asinValue),
            optionalNumber,
            null,
            false,
            false,
            null,
            null
            )
        );

        // math:acos(
        //  as xs:double?
        // ) as xs:double?
        final ArgumentSpecification acosValue = new ArgumentSpecification("value", optionalNumber, null);
        math.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("math", "acos"),
            List.of(acosValue),
            optionalNumber,
            null,
            false,
            false,
            null,
            null
            )
        );

        // math:atan(
        //  as xs:double?
        // ) as xs:double?
        final ArgumentSpecification atanVal = new ArgumentSpecification("value", optionalNumber, null);
        math.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("math", "atan"),
            List.of(atanVal),
            optionalNumber,
            null,
            false,
            false,
            null,
            null
            )
        );

        // math:atan2(
        //  as xs:double,
        //  as xs:double
        // ) as xs:double
        final ArgumentSpecification atan2Y = new ArgumentSpecification("y", typeFactory.number(), null);
        final ArgumentSpecification atan2X = new ArgumentSpecification("x", typeFactory.number(), null);
        math.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("math", "atan2"),
            List.of(atan2Y, atan2X),
            typeFactory.number(),
            null,
            false,
            false,
            null,
            null
            )
        );

        // math:sinh(
        //  as xs:double?
        // ) as xs:double?
        final ArgumentSpecification sinhVal = new ArgumentSpecification("value", optionalNumber, null);
        math.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("math", "sinh"),
            List.of(sinhVal),
            optionalNumber,
            null,
            false,
            false,
            null,
            null
            )
        );

        // math:cosh(
        //  as xs:double?
        // ) as xs:double?
        final ArgumentSpecification coshVal = new ArgumentSpecification("value", optionalNumber, null);
        math.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("math", "cosh"),
            List.of(coshVal),
            optionalNumber,
            null,
            false,
            false,
            null,
            null
            )
        );

        // math:tanh(
        //  as xs:double?
        // ) as xs:double?
        final ArgumentSpecification tanhVal = new ArgumentSpecification("value", optionalNumber, null);
        math.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("math", "tanh"),
            List.of(tanhVal),
            optionalNumber,
            null,
            false,
            false,
            null,
            null
            )
        );

        return math;

    }



    public static List<SimplifiedFunctionSpecification> ANTLR(XQueryTypeFactory typeFactory) {
        final List<SimplifiedFunctionSpecification> antlr = new ArrayList<>(50);
        final XQuerySequenceType optionalNumber = typeFactory.zeroOrOne(typeFactory.itemNumber());
        var helperTrees = new HelperTrees();
        final ParseTree CONTEXT_VALUE = helperTrees.CONTEXT_VALUE;
        ArgumentSpecification optionalNodeArg = new ArgumentSpecification(
            "node",
            typeFactory.zeroOrOne(typeFactory.itemAnyNode()),
            CONTEXT_VALUE
        );
        // antlr:start
        antlr.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("antlr", "start"),
            List.of(optionalNodeArg),
            optionalNumber,
            null, false, false, null, null
            )
        );
        antlr.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("antlr", "stop"),
            List.of(optionalNodeArg),
            optionalNumber,
            null, false, false, null, null
            )
        );
        antlr.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("antlr", "pos"),
            List.of(optionalNodeArg),
            optionalNumber,
            null, false, false, null, null
            )
        );
        antlr.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("antlr", "index"),
            List.of(optionalNodeArg),
            optionalNumber,
            null, false, false, null, null
            )
        );
        antlr.add(
            new SimplifiedFunctionSpecification(
            new QualifiedName("antlr", "line"),
            List.of(optionalNodeArg),
            optionalNumber,
            null, false, false, null, null
            )
        );




        return antlr;

    }

}
