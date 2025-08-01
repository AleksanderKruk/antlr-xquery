package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.math.BigDecimal;
import java.text.BreakIterator;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.htmlentities.HTMLEntities;
import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.evaluator.values.operations.Stringifier;
import com.github.akruk.antlrxquery.evaluator.values.operations.ValueAtomizer;

public class FunctionsOnStringValues {
    private final XQueryValueFactory valueFactory;
    private final ValueAtomizer atomizer;
    // private final EffectiveBooleanValue ebv;
    private final Stringifier stringifier;

    public FunctionsOnStringValues(final XQueryValueFactory valueFactory, final ValueAtomizer atomizer, final Stringifier stringifier) {
        this.valueFactory = valueFactory;
        this.atomizer = atomizer;
        // this.ebv = new EffectiveBooleanValue(valueFactory);
        this.stringifier = stringifier;
    }

    public XQueryValue concat(final XQueryVisitingContext context, final List<XQueryValue> args) {
        StringBuilder builder = new StringBuilder();

        for (XQueryValue arg : args) {
            List<XQueryValue> atomized = atomizer.atomize(arg);
            for (XQueryValue value : atomized) {
                if (value.isError) {
                    return value;
                }
                var stringified = stringifier.stringify(value);
                if (stringified.isError)
                    return stringified;
                builder.append(stringified.stringValue);
            }
        }

        return valueFactory.string(builder.toString());
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
            List<XQueryValue> args) {

        // must have 2 or 3 args
        if (args.size() != 2 && args.size() != 3) {
            return valueFactory.error(XQueryError.WrongNumberOfArguments, "");
        }

        // get input string (empty‐sequence → "")
        XQueryValue targetString = args.get(0);
        String input = targetString.isEmptySequence
            ? ""
            : targetString.stringValue;

        // parse and round start
        XQueryValue startArg = args.get(1);
        if (!startArg.isNumeric) {
            return valueFactory.error(XQueryError.InvalidArgumentType, "");
        }
        double startD = startArg.numericValue.doubleValue();
        long startPos = roundXQ(startD);

        // two‐arg or length omitted/empty
        boolean omitLength = args.size() == 2
            || args.get(2).isEmptySequence;
        if (omitLength) {
            return substring_(input, startPos, /* length=∞ */ Long.MAX_VALUE);
        }

        // parse and round length
        XQueryValue lenArg = args.get(2);
        if (!lenArg.isNumeric) {
            return valueFactory.error(XQueryError.InvalidArgumentType, "");
        }
        double lenD = lenArg.numericValue.doubleValue();
        long length = roundXQ(lenD);
        return substring_(input, startPos, length);
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
            List<XQueryValue> args)
    {

        // Determine the sequence of values to join
        List<XQueryValue> atomized = atomizer.atomize(args.get(0));

        // If the sequence is empty, return the zero‐length string
        if (atomized.isEmpty()) {
            return valueFactory.string("");
        }

        // Determine the separator
        String sep = "";
        if (args.size() == 2) {
            XQueryValue sepArg = args.get(1);
            // Empty sequence or omitted => zero‐length string
            if (!sepArg.isEmptySequence) {
                sep = sepArg.stringValue;
            }
        }

        // Concatenate the atomized strings with the separator
        String result = atomized.stream()
            .map(stringifier::stringify)
            .map(v->v.stringValue)
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
            List<XQueryValue> args)
    {

        if (args.size() > 1) {
            return valueFactory.error(XQueryError.WrongNumberOfArguments, "");
        }

        String input;
        if (args.isEmpty()) {
            XQueryValue ctxItem = context.getValue();
            if (ctxItem == null) {
                return valueFactory.error(XQueryError.MissingDynamicContextComponent, "");
            }
            List<XQueryValue> atoms = atomizer.atomize(ctxItem);
            if (atoms.size() != 1) {
                return valueFactory.error(XQueryError.InvalidArgumentType, "");
            }
            input = atoms.get(0).stringValue;
        } else {
            XQueryValue arg = args.get(0);
            if (arg.isEmptySequence) {
                return valueFactory.string("");
            }
            input = arg.stringValue;
        }

        // replace any run of whitespace (\s) with a single space, then trim ends
        String normalized = WHITESPACE_RE
            .matcher(input)
            .replaceAll(" ")
            .trim();

        return valueFactory.string(normalized);
    }

    public XQueryValue upperCase(
            XQueryVisitingContext context,
            List<XQueryValue> args) {

        final XQueryValue input = args.get(0);
        if (input.isEmptySequence) {
            return valueFactory.emptyString();
        }
        if (!input.isString) {
            return valueFactory.error(XQueryError.InvalidArgumentType, "");
        }

        String original = input.stringValue;
        String transformed = original.toUpperCase(Locale.ROOT);
        return valueFactory.string(transformed);
    }

    public XQueryValue lowerCase(
            XQueryVisitingContext context,
            List<XQueryValue> args) {

        final XQueryValue input = args.get(0);
        if (input.isEmptySequence) {
            return valueFactory.emptyString();
        }
        if (!input.isString) {
            return valueFactory.error(XQueryError.InvalidArgumentType, "");
        }

        String original = input.stringValue;
        String transformed = original.toLowerCase(Locale.ROOT);
        return valueFactory.string(transformed);
    }

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
            final List<XQueryValue> args) {
        final XQueryValue arg = args.get(0);
        // A Unicode codepoint, supplied as an integer. For example fn:char(9) returns
        // the tab character.
        if (arg.isNumeric) {
            final BigDecimal dec = arg.numericValue;
            try {
                final int cp = dec.intValueExact();
                // Unicode range and surrogates
                if (cp < 0
                        || cp > Character.MAX_CODE_POINT
                        || (cp >= 0xD800 && cp <= 0xDFFF)) {
                    return valueFactory.error(XQueryError.UnrecognizedOrInvalidCharacterName, "");
                }
                final String s = new String(Character.toChars(cp));
                return valueFactory.string(s);

            } catch (final ArithmeticException ex) {
                return valueFactory.error(XQueryError.InvalidArgumentType, "");
            }
        }

        // A backslash-escape sequence from the set \n (U+000A (NEWLINE) ), \r (U+000D
        // (CARRIAGE RETURN) ), or \t (U+0009 (TAB) ).
        final XQueryValue stringified = stringifier.stringify(arg);
        if (stringified.isError)
            return stringified;

        switch (stringified.stringValue) {
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

        if (getEntities().containsKey(stringified.stringValue)) {
            return valueFactory.string(HTML5_ENTITIES.get(stringified.stringValue));
        }

        return valueFactory.error(XQueryError.UnrecognizedOrInvalidCharacterName, "");
    }


    /**
     * fn:characters($value as xs:string?) as xs:string*
     */
    public XQueryValue characters(
            final XQueryVisitingContext context,
            final List<XQueryValue> args)
    {

        // obtain the string: either argument or context item
        final XQueryValue inputValue = args.get(0);
        // empty‐string or empty‐sequence -> empty sequence
        if (inputValue.isEmptySequence) {
            return valueFactory.emptySequence();
        }
        if (inputValue.isString && inputValue.stringValue.isEmpty()) {
            return valueFactory.emptySequence();
        }
        final String input = stringifier.stringify_(inputValue);

        // split into codepoints -> single‐char strings
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
            List<XQueryValue> args)
    {

        // obtain the input string (argument or context item)
        final XQueryValue inputValue;
        if (args.isEmpty()) {
            inputValue = context.getValue();
        } else {
            inputValue = args.get(0);
        }
        if (inputValue.isEmptySequence)
            return valueFactory.emptySequence();

        if (!inputValue.isString) {
            return valueFactory.error(XQueryError.InvalidArgumentType, "");
        }
        final String input = inputValue.stringValue;

        // BreakIterator for extended grapheme clusters
        final BreakIterator iter = BreakIterator.getCharacterInstance(Locale.ROOT);
        iter.setText(input);

        final List<XQueryValue> clusters = new ArrayList<>();
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
            List<XQueryValue> args) {

        // arity check
        if (args.size() > 2) {
            return valueFactory.error(XQueryError.WrongNumberOfArguments, "");
        }

        // determine input string (arg0 or context item)
        String input;
        if (args.isEmpty()) {
            XQueryValue ctxItem = context.getValue();
            if (ctxItem == null) {
                return valueFactory.error(XQueryError.MissingDynamicContextComponent, "");
            }
            input = ctxItem.stringValue;
        } else {
            XQueryValue arg0 = args.get(0);
            if (arg0.isEmptySequence) {
                // empty-sequence => zero-length string
                input = "";
            } else {
                input = arg0.stringValue;
            }
        }

        // determine effective form
        String rawForm = "NFC";
        if (args.size() == 2) {
            XQueryValue formArg = args.get(1);
            if (!formArg.isEmptySequence) {
                // normalize-space on raw form, then upper-case
                String tmp = WHITESPACE_RE
                    .matcher(formArg.stringValue)
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
                return valueFactory.error(XQueryError.UnsupportedNormalizationForm, "");
        }

        return valueFactory.string(result);
    }

    /**
     * fn:translate(
     *   $value   as xs:string?,
     *   $replace as xs:string,
     *   $with    as xs:string
     * ) as xs:string
     */
    public XQueryValue translate(
            XQueryVisitingContext context,
            List<XQueryValue> args) {

        XQueryValue valArg = args.get(0);

        // If $value is the empty sequence, the function returns the zero-length string.
        if (valArg.isEmptySequence)
            return valueFactory.string("");

        // Otherwise, the function returns a result string constructed by processing each character in $value, in order, according to the following rules:
        String input = valArg.stringValue;

        // obtain replace and with strings
        XQueryValue repArg = args.get(1);
        XQueryValue withArg = args.get(2);

        String replace = repArg.stringValue;
        String with = withArg.stringValue;

        // build codepoint arrays
        int[] inCps = input.codePoints().toArray();
        int[] repCps = replace.codePoints().toArray();
        int[] withCps = with.codePoints().toArray();

        // map each codepoint in replace to its first index
        Map<Integer, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < repCps.length; i++) {
            indexMap.putIfAbsent(repCps[i], i);
        }

        // translate each character
        StringBuilder sb = new StringBuilder();
        for (int cp : inCps) {
            Integer idx = indexMap.get(cp);
            if (idx == null) {
                // not in replace => unchanged
                sb.appendCodePoint(cp);
            } else if (idx < withCps.length) {
                // replace with corresponding codepoint
                sb.appendCodePoint(withCps[idx]);
            }
            // else: idx >= withCps.length => omit
        }

        return valueFactory.string(sb.toString());
    }


    /**
     * fn:string-length(
     *   $value as xs:string? := fn:string(.)
     * ) as xs:integer
     */
    public XQueryValue stringLength(
            XQueryVisitingContext context,
            List<XQueryValue> args) {

        String input;

        if (args.isEmpty()) {
            // zero‐arg form: use context item
            final XQueryValue ctxItem = context.getValue();
            if (ctxItem == null) {
                return valueFactory.error(XQueryError.MissingDynamicContextComponent, "");
            }
            // atomize
            final List<XQueryValue> atoms = atomizer.atomize(ctxItem);
            if (atoms.size() != 1) {
                return valueFactory.error(XQueryError.InvalidArgumentType, "");
            }
            input = atoms.get(0).stringValue;
        } else {
            // one‐arg form
            XQueryValue arg = args.get(0);
            // empty‐sequence => length 0
            if (arg.isEmptySequence) {
                return valueFactory.number(0);
            }
            input = arg.stringValue;
        }

        // count codepoints (each surrogate pair counts as one)
        long length = input.codePoints().count();
        return valueFactory.number(BigDecimal.valueOf(length));
    }


}
