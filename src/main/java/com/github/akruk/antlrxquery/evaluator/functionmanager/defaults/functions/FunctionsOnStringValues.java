package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.math.BigDecimal;
import java.text.BreakIterator;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.values.XQueryError;
import com.github.akruk.antlrxquery.values.XQuerySequence;
import com.github.akruk.antlrxquery.values.XQueryString;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.htmlentities.HTMLEntities;

public class FunctionsOnStringValues {
    private final XQueryValueFactory valueFactory;

    public FunctionsOnStringValues(final XQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    // TODO: Reevaluate later
    public XQueryValue concat(final XQueryVisitingContext context, final List<XQueryValue> args, final Map<String, XQueryValue> kwargs) {
        if (args.size() >= 2)
            return XQueryError.WrongNumberOfArguments;
        if (args.size() == 0)
            return valueFactory.string(context.getItem().stringValue());
        final String joined = args.get(0).atomize().stream().map(XQueryValue::stringValue).collect(Collectors.joining());
        return valueFactory.string(joined);
    }



    /**
     * fn:substring(
     *   $value as xs:string?,
     *   $start as xs:double,
     *   $length as xs:double? := ()
     * ) as xs:string
     */
    public XQueryValue substring(
            XQueryVisitingContext ctx,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs) {

        // must have 2 or 3 args
        if (args.size() != 2 && args.size() != 3) {
            return XQueryError.WrongNumberOfArguments;
        }

        // get input string (empty‐sequence → "")
        XQueryValue targetString = args.get(0);
        String input = isEmptySequence(targetString)
            ? ""
            : targetString.stringValue();

        // parse and round start
        XQueryValue startArg = args.get(1);
        if (!startArg.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }
        double startD = startArg.numericValue().doubleValue();
        long startPos = roundXQ(startD);

        // two‐arg or length omitted/empty
        boolean omitLength = args.size() == 2
            || isEmptySequence(args.get(2));
        if (omitLength) {
            return substring_(input, startPos, /* length=∞ */ Long.MAX_VALUE);
        }

        // parse and round length
        XQueryValue lenArg = args.get(2);
        if (!lenArg.isNumericValue()) {
            return XQueryError.InvalidArgumentType;
        }
        double lenD = lenArg.numericValue().doubleValue();
        long length = roundXQ(lenD);
        return substring_(input, startPos, length);
    }

    private boolean isEmptySequence(XQueryValue targetString) {
        return targetString instanceof XQuerySequence && targetString.empty().booleanValue();
    }

    /** Round per XQuery fn:round: ties away from zero. */
    private long roundXQ(double d) {
        if (Double.isNaN(d)) {
            return Long.MIN_VALUE;     // so no positions satisfy p >= NaN
        }
        if (Double.isInfinite(d)) {
            return d > 0 ? Long.MAX_VALUE : Long.MIN_VALUE;
        }
        if (d >= 0) {
            return (long) Math.floor(d + 0.5);
        } else {
            return (long) -Math.floor(-d + 0.5);
        }
    }

    /**
     * Helper performing substring per XQuery rules, counting
     * surrogate pairs as single characters.
     *
     * @param input full string
     * @param startRounded rounded start position
     * @param lengthRounded rounded length (Long.MAX_VALUE → to end)
     */
    private XQueryValue substring_(String input, long startRounded, long lengthRounded) {
        // empty input always ""
        if (input.isEmpty()) {
            return valueFactory.string("");
        }

        // build list of code‐point chars
        int[] cps = input.codePoints().toArray();
        int n = cps.length;

        // determine start index (1‐based!)
        int startIndex = (int) startRounded;
        if (startIndex <= 0) {
            startIndex = 1;
        }
        if (startIndex > n) {
            return valueFactory.string("");
        }

        // determine end position p < start + length
        long endExclusivePos = startRounded + lengthRounded;
        // if lengthRounded infinite, endExclusivePos large, we'll cap by n+1
        int endIndex = (lengthRounded >= Long.MAX_VALUE || endExclusivePos > n + 1)
            ? n
            : (int) Math.min(n, endExclusivePos - 1);

        if (endIndex < startIndex) {
            return valueFactory.string("");
        }

        // build substring from cps[startIndex-1 .. endIndex-1]
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex - 1; i < endIndex; i++) {
            sb.appendCodePoint(cps[i]);
        }
        return valueFactory.string(sb.toString());
    }

    /**
     * fn:string-join(
     *   $values    as xs:anyAtomicType*,
     *   $separator as xs:string? := ""
     * ) as xs:string
     */
    public XQueryValue stringJoin(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs)
    {

        // Determine the sequence of values to join
        List<XQueryValue> atomized;
        if (args.isEmpty()) {
            // No explicit $values: use context item sequence
            atomized = context.getItem().atomize();
        } else {
            atomized = args.get(0).atomize();
        }

        // If the sequence is empty, return the zero‐length string
        if (atomized.isEmpty()) {
            return valueFactory.string("");
        }

        // Determine the separator
        String sep = "";
        if (args.size() == 2) {
            XQueryValue sepArg = args.get(1);
            // Empty sequence or omitted => zero‐length string
            if (!sepArg.isEmptySequence()) {
                sep = sepArg.stringValue();
            }
        }

        // Concatenate the atomized strings with the separator
        String result = atomized.stream()
            .map(XQueryValue::stringValue)
            .collect(Collectors.joining(sep));

        return valueFactory.string(result);
    }

    private static final Pattern WHITESPACE_RE = Pattern.compile("\\s+");

    /**
     * fn:normalize-space(
     *   $value as xs:string? := fn:string(.)
     * ) as xs:string
     */
    public XQueryValue normalizeSpace(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs)
    {

        if (args.size() > 1) {
            return XQueryError.WrongNumberOfArguments;
        }

        String input;
        if (args.isEmpty()) {
            XQueryValue ctxItem = context.getItem();
            if (ctxItem == null) {
                return XQueryError.MissingDynamicContextComponent;
            }
            List<XQueryValue> atoms = ctxItem.atomize();
            if (atoms.size() != 1) {
                return XQueryError.InvalidArgumentType;
            }
            input = atoms.get(0).stringValue();
        } else {
            XQueryValue arg = args.get(0);
            if (arg.isEmptySequence()) {
                return valueFactory.string("");
            }
            input = arg.stringValue();
        }

        // replace any run of whitespace (\s) with a single space, then trim ends
        String normalized = WHITESPACE_RE
            .matcher(input)
            .replaceAll(" ")
            .trim();

        return valueFactory.string(normalized);
    }

    public XQueryValue uppercase(final XQueryVisitingContext context, final List<XQueryValue> args,
            final Map<String, XQueryValue> kwargs) {
        if (args.size() != 1)
            return XQueryError.WrongNumberOfArguments;
        return args.get(0).uppercase();
    }

    public XQueryValue lowercase(final XQueryVisitingContext context, final List<XQueryValue> args,
            final Map<String, XQueryValue> kwargs) {
        if (args.size() != 1)
            return XQueryError.WrongNumberOfArguments;
        return args.get(0).lowercase();
    }

    // private static final Map<String,String> HTML5_ENTITIES = Map.ofEntries(
    // Map.entry("nbsp", "\u00A0"),
    // Map.entry("lt", "\u003C"),
    // Map.entry("gt", "\u003E"),
    // Map.entry("amp", "\u0026"),
    // Map.entry("quot", "\""),
    // Map.entry("apos", "\u0027"),
    // Map.entry("pi", "\u03C0"),
    // Map.entry("NotEqualTilde", "\u2242\u0338")
    // );

    private Map<String, String> HTML5_ENTITIES;

    Map<String, String> getEntities() {
        if (HTML5_ENTITIES == null) {
            HTML5_ENTITIES = (new HTMLEntities()).HTML5_ENTITIES;
        }
        return HTML5_ENTITIES;
    }

    /**
     * fn:char($value as xs:string|xs:positiveInteger) as xs:string
     * The function returns a string, generally containing a single character or
     * glyph, identified by $value.
     * The supplied value of $value must be one of the following:
     */
    public XQueryValue char_(
            final XQueryVisitingContext context,
            final List<XQueryValue> args,
            final Map<String, XQueryValue> kwargs) {
        final XQueryValue arg = args.get(0);
        // A Unicode codepoint, supplied as an integer. For example fn:char(9) returns
        // the tab character.
        if (arg.isNumericValue()) {
            final BigDecimal dec = arg.numericValue();
            try {
                final int cp = dec.intValueExact();
                // Unicode range and surrogates
                if (cp < 0
                        || cp > Character.MAX_CODE_POINT
                        || (cp >= 0xD800 && cp <= 0xDFFF)) {
                    return XQueryError.UnrecognizedOrInvalidCharacterName;
                }
                final String s = new String(Character.toChars(cp));
                return valueFactory.string(s);

            } catch (final ArithmeticException ex) {
                return XQueryError.InvalidArgumentType;
            }
        }

        // A backslash-escape sequence from the set \n (U+000A (NEWLINE) ), \r (U+000D
        // (CARRIAGE RETURN) ), or \t (U+0009 (TAB) ).
        final String s = arg.stringValue();
        switch (s) {
            case "\\n":
                return valueFactory.string("\n");
            case "\\r":
                return valueFactory.string("\r");
            case "\\t":
                return valueFactory.string("\t");
        }

        // An HTML5 character reference name (often referred to as an entity name) as
        // defined at https://html.spec.whatwg.org/multipage/named-characters.html. The
        // name is written with no leading ampersand and no trailing semicolon. For
        // example fn:char("pi") represents the character U+03C0 (GREEK SMALL LETTER PI,
        // π) and fn:char("nbsp") returns U+00A0 (NON-BREAKING SPACE, NBSP) .
        // A processor may recognize additional character reference names defined in
        // other versions of HTML. Character reference names are case-sensitive.
        // In the event that the HTML5 character reference name identifies a string
        // comprising multiple codepoints, that string is returned.

        if (getEntities().containsKey(s)) {
            return valueFactory.string(HTML5_ENTITIES.get(s));
        }

        return XQueryError.UnrecognizedOrInvalidCharacterName;
    }

    /**
     * fn:characters($value as xs:string?) as xs:string*
     */
    public XQueryValue characters(
            final XQueryVisitingContext context,
            final List<XQueryValue> args,
            final Map<String, XQueryValue> kwargs)
    {

        // obtain the string: either argument or context item
        final XQueryValue inputValue;
        if (args.isEmpty()) {
            inputValue = context.getItem();
        } else {
            inputValue = args.get(0);
        }
        final var empty = inputValue.empty();
        if (empty == null) {
            return XQueryError.InvalidArgumentType;
        }

        // empty‐string or empty‐sequence → empty sequence
        if (empty.booleanValue()) {
            return valueFactory.emptySequence();
        }
        final String input = inputValue.stringValue();

        // split into codepoints → single‐char strings
        final List<XQueryValue> parts = input
                .codePoints()
                .mapToObj(cp -> new String(Character.toChars(cp)))
                .map(valueFactory::string)
                .collect(Collectors.toList());

        return valueFactory.sequence(parts);
    }

    /**
     * fn:graphemes($value as xs:string?) as xs:string*
     * Splits into extended grapheme clusters per UAX #29.
     */
    public XQueryValue graphemes(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs) {

        // wrong arity?
        if (args.size() > 1) {
            return XQueryError.WrongNumberOfArguments;
        }

        // obtain the input string (argument or context item)
        final XQueryValue inputValue;
        if (args.isEmpty()) {
            inputValue = context.getItem();
        } else {
            inputValue = args.get(0);
        }
        final var empty = inputValue.empty();
        if (empty == null) {
            return XQueryError.InvalidArgumentType;
        }
        if (inputValue.empty().booleanValue())
            return valueFactory.emptySequence();

        if (!(inputValue instanceof XQueryString)) {
            return XQueryError.InvalidArgumentType;
        }
        final String input = inputValue.stringValue();

        // BreakIterator for extended grapheme clusters
        BreakIterator iter = BreakIterator.getCharacterInstance(Locale.ROOT);
        iter.setText(input);

        List<XQueryValue> clusters = new ArrayList<>();
        int start = iter.first();
        for (int end = iter.next(); end != BreakIterator.DONE; start = end, end = iter.next()) {
            String cluster = input.substring(start, end);
            clusters.add(valueFactory.string(cluster));
        }
        return valueFactory.sequence(clusters);
    }

    /**
     * fn:normalize-unicode(
     *   $value as xs:string?,
     *   $form  as xs:string? := "NFC"
     * ) as xs:string
     */
    public XQueryValue normalizeUnicode(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs) {

        // arity check
        if (args.size() > 2) {
            return XQueryError.WrongNumberOfArguments;
        }

        // determine input string (arg0 or context item)
        String input;
        if (args.isEmpty()) {
            XQueryValue ctxItem = context.getItem();
            if (ctxItem == null) {
                return XQueryError.MissingDynamicContextComponent;
            }
            input = ctxItem.stringValue();
        } else {
            XQueryValue arg0 = args.get(0);
            if (arg0.sequence().isEmpty()) {
                // empty-sequence => zero-length string
                input = "";
            } else {
                input = arg0.stringValue();
            }
        }

        // determine effective form
        String rawForm = "NFC";
        if (args.size() == 2) {
            XQueryValue formArg = args.get(1);
            if (!formArg.sequence().isEmpty()) {
                // normalize-space on raw form, then upper-case
                String tmp = WHITESPACE_RE
                    .matcher(formArg.stringValue())
                    .replaceAll(" ")
                    .trim();
                rawForm = tmp.toUpperCase(Locale.ROOT);
            }
        }

        // no normalization if form is empty
        if (rawForm.isEmpty()) {
            return valueFactory.string(input);
        }

        // apply selected normalization
        String result;
        switch (rawForm) {
            case "NFC":
                result = Normalizer.normalize(input, Form.NFC);
                break;
            case "NFD":
                result = Normalizer.normalize(input, Form.NFD);
                break;
            case "NFKC":
                result = Normalizer.normalize(input, Form.NFKC);
                break;
            case "NFKD":
                result = Normalizer.normalize(input, Form.NFKD);
                break;
            case "FULLY-NORMALIZED":
                // prepend space if first codepoint is a combining mark
                if (!input.isEmpty()) {
                    int cp = input.codePointAt(0);
                    int type = Character.getType(cp);
                    if (type == Character.NON_SPACING_MARK
                        || type == Character.COMBINING_SPACING_MARK
                        || type == Character.ENCLOSING_MARK) {
                        input = " " + input;
                    }
                }
                result = Normalizer.normalize(input, Form.NFC);
                break;
            default:
                return XQueryError.UnsupportedNormalizationForm;
        }

        return valueFactory.string(result);
    }

    public XQueryValue translate(final XQueryVisitingContext context, final List<XQueryValue> args,
            final Map<String, XQueryValue> kwargs) {
        return null;
    }


    /**
     * fn:string-length(
     *   $value as xs:string? := fn:string(.)
     * ) as xs:integer
     */
    public XQueryValue stringLength(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs) {

        String input;

        if (args.isEmpty()) {
            // zero‐arg form: use context item
            XQueryValue ctxItem = context.getItem();
            if (ctxItem == null) {
                return XQueryError.MissingDynamicContextComponent;
            }
            // atomize
            List<XQueryValue> atoms = ctxItem.atomize();
            if (atoms.size() != 1) {
                return XQueryError.InvalidArgumentType;
            }
            input = atoms.get(0).stringValue();
        } else {
            // one‐arg form
            XQueryValue arg = args.get(0);
            // empty‐sequence => length 0
            if (arg.isEmptySequence()) {
                return valueFactory.number(0);
            }
            input = arg.stringValue();
        }

        // count codepoints (each surrogate pair counts as one)
        long length = input.codePoints().count();
        return valueFactory.number(BigDecimal.valueOf(length));
    }


}
