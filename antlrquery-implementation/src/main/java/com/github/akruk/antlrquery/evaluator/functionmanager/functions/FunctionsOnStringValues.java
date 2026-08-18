package com.github.akruk.antlrquery.evaluator.functionmanager.functions;

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

import com.github.akruk.antlrquery.evaluator.AntlrQueryVisitingContext;
import com.github.akruk.antlrquery.evaluator.functionmanager.functions.htmlentities.HTMLEntities;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;
import com.github.akruk.antlrquery.evaluator.values.operations.Stringifier;
import com.github.akruk.antlrquery.evaluator.values.operations.ValueAtomizer;

public class FunctionsOnStringValues {
    private final AntlrQueryValueFactory valueFactory;
    private final ValueAtomizer atomizer;
    // private final EffectiveBooleanValue ebv;
    private final Stringifier stringifier;

    public FunctionsOnStringValues(final AntlrQueryValueFactory valueFactory, final ValueAtomizer atomizer, final Stringifier stringifier) {
        this.valueFactory = valueFactory;
        this.atomizer = atomizer;
        // this.ebv = new EffectiveBooleanValue(valueFactory);
        this.stringifier = stringifier;
    }

    public AntlrQueryValue concat(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        StringBuilder builder = new StringBuilder();

        for (AntlrQueryValue arg : args) {
            List<AntlrQueryValue> atomized = atomizer.atomize(arg);
            for (AntlrQueryValue value : atomized) {
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
     *   $cardinality as xs:double? := ()
     * ) as xs:string
     */
    public AntlrQueryValue substring(
            AntlrQueryVisitingContext ctx,
            List<AntlrQueryValue> args) {

        // must have 2 or 3 args
        if (args.size() != 2 && args.size() != 3) {
            return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        }

        // get input string (empty‐sequence → "")
        AntlrQueryValue targetString = args.get(0);
        String input = targetString.isEmptySequence
            ? ""
            : targetString.stringValue;

        // parse and round start
        AntlrQueryValue startArg = args.get(1);
        if (!startArg.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }
        double startD = startArg.numericValue.doubleValue();
        long startPos = roundXQ(startD);

        // two‐arg or cardinality omitted/empty
        boolean omitLength = args.size() == 2
            || args.get(2).isEmptySequence;
        if (omitLength) {
            return substring_(input, startPos, /* cardinality=∞ */ Long.MAX_VALUE);
        }

        // parse and round cardinality
        AntlrQueryValue lenArg = args.get(2);
        if (!lenArg.isNumeric) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
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
     * @param lengthRounded rounded cardinality (Long.MAX_VALUE → to end)
     */
    private AntlrQueryValue substring_(String input, long startRounded, long lengthRounded) {
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

        // determine end position p < start + cardinality
        long endExclusivePos = startRounded + lengthRounded;
        // if lengthRounded infinite, endExclusivePos large, we'll cap by n+1
        int endIndex = (lengthRounded == Long.MAX_VALUE || endExclusivePos > n + 1)
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
    public AntlrQueryValue stringJoin(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args)
    {

        // Determine the sequence of values to join
        List<AntlrQueryValue> atomized = atomizer.atomize(args.getFirst());

        // If the sequence is empty, return the zero‐cardinality string
        if (atomized.isEmpty()) {
            return valueFactory.string("");
        }

        // Determine the separator
        String sep = "";
        if (args.size() == 2) {
            AntlrQueryValue sepArg = args.get(1);
            // Empty sequence or omitted => zero‐cardinality string
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
    public AntlrQueryValue normalizeSpace(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args)
    {

        if (args.size() > 1) {
            return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        }

        String input;
        if (args.isEmpty()) {
            AntlrQueryValue ctxItem = context.getValue();
            if (ctxItem == null) {
                return valueFactory.error(AntlrQueryError.MissingDynamicContextComponent, "");
            }
            List<AntlrQueryValue> atoms = atomizer.atomize(ctxItem);
            if (atoms.size() != 1) {
                return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
            }
            input = atoms.getFirst().stringValue;
        } else {
            AntlrQueryValue arg = args.getFirst();
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

    public AntlrQueryValue upperCase(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        final AntlrQueryValue input = args.getFirst();
        if (input.isEmptySequence) {
            return valueFactory.emptyString();
        }
        if (!input.isString) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        String original = input.stringValue;
        String transformed = original.toUpperCase(Locale.ROOT);
        return valueFactory.string(transformed);
    }

    public AntlrQueryValue lowerCase(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        final AntlrQueryValue input = args.getFirst();
        if (input.isEmptySequence) {
            return valueFactory.emptyString();
        }
        if (!input.isString) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
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
    public AntlrQueryValue char_(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args) {
        final AntlrQueryValue arg = args.getFirst();
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
                    return valueFactory.error(AntlrQueryError.UnrecognizedOrInvalidCharacterName, "");
                }
                final String s = new String(Character.toChars(cp));
                return valueFactory.string(s);

            } catch (final ArithmeticException ex) {
                return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
            }
        }

        // A backslash-escape sequence from the set \n (U+000A (NEWLINE) ), \r (U+000D
        // (CARRIAGE RETURN) ), or \t (U+0009 (TAB) ).
        final AntlrQueryValue stringified = stringifier.stringify(arg);
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

        return valueFactory.error(AntlrQueryError.UnrecognizedOrInvalidCharacterName, "");
    }


    /**
     * fn:characters($value as xs:string?) as xs:string*
     */
    public AntlrQueryValue characters(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {

        // obtain the string: either argument or context item
        final AntlrQueryValue inputValue = args.getFirst();
        // empty‐string or empty‐sequence -> empty sequence
        if (inputValue.isEmptySequence) {
            return valueFactory.emptySequence();
        }
        if (inputValue.isString && inputValue.stringValue.isEmpty()) {
            return valueFactory.emptySequence();
        }
        final String input = stringifier.stringify_(inputValue);

        // split into codepoints -> single‐char strings
        final List<AntlrQueryValue> parts = input
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
    public AntlrQueryValue graphemes(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args)
    {

        // obtain the input string (argument or context item)
        final AntlrQueryValue inputValue;
        if (args.isEmpty()) {
            inputValue = context.getValue();
        } else {
            inputValue = args.getFirst();
        }
        if (inputValue.isEmptySequence)
            return valueFactory.emptySequence();

        if (!inputValue.isString) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }
        final String input = inputValue.stringValue;

        // BreakIterator for extended grapheme clusters
        final BreakIterator iter = BreakIterator.getCharacterInstance(Locale.ROOT);
        iter.setText(input);

        final List<AntlrQueryValue> clusters = new ArrayList<>();
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
    public AntlrQueryValue normalizeUnicode(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        // arity check
        if (args.size() > 2) {
            return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        }

        // determine input string (arg0 or context item)
        String input;
        if (args.isEmpty()) {
            AntlrQueryValue ctxItem = context.getValue();
            if (ctxItem == null) {
                return valueFactory.error(AntlrQueryError.MissingDynamicContextComponent, "");
            }
            input = ctxItem.stringValue;
        } else {
            AntlrQueryValue arg0 = args.getFirst();
            if (arg0.isEmptySequence) {
                // empty-sequence => zero-cardinality string
                input = "";
            } else {
                input = arg0.stringValue;
            }
        }

        // determine effective form
        String rawForm = "NFC";
        if (args.size() == 2) {
            AntlrQueryValue formArg = args.get(1);
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
                return valueFactory.error(AntlrQueryError.UnsupportedNormalizationForm, "");
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
    public AntlrQueryValue translate(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        AntlrQueryValue valArg = args.getFirst();

        // If $value is the empty sequence, the function returns the zero-cardinality string.
        if (valArg.isEmptySequence)
            return valueFactory.string("");

        // Otherwise, the function returns a result string constructed by processing each character in $value, in order, according to the following rules:
        String input = valArg.stringValue;

        // obtain replace and with strings
        AntlrQueryValue repArg = args.get(1);
        AntlrQueryValue withArg = args.get(2);

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
            // else: idx >= withCps.cardinality => omit
        }

        return valueFactory.string(sb.toString());
    }


    /**
     * fn:string-length(
     *   $value as xs:string? := fn:string(.)
     * ) as xs:integer
     */
    public AntlrQueryValue stringLength(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        String input;

        if (args.isEmpty()) {
            // zero‐arg form: use context item
            final AntlrQueryValue ctxItem = context.getValue();
            if (ctxItem == null) {
                return valueFactory.error(AntlrQueryError.MissingDynamicContextComponent, "");
            }
            // atomize
            final List<AntlrQueryValue> atoms = atomizer.atomize(ctxItem);
            if (atoms.size() != 1) {
                return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
            }
            input = atoms.getFirst().stringValue;
        } else {
            // one‐arg form
            AntlrQueryValue arg = args.getFirst();
            // empty‐sequence => cardinality 0
            if (arg.isEmptySequence) {
                return valueFactory.number(0);
            }
            input = arg.stringValue;
        }

        // count codepoints (each surrogate pair counts as one)
        long length = input.codePoints().count();
        return valueFactory.number(BigDecimal.valueOf(length));
    }

    /**
     * fn:string-empty(
     *   $value as xs:string := fn:string(.)
     * ) as xs:integer
     */
    public AntlrQueryValue stringEmpty(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args)
    {

        String input;

        if (args.isEmpty()) {
            // zero‐arg form: use context item
            final AntlrQueryValue ctxItem = context.getValue();
            if (ctxItem == null) {
                return valueFactory.error(AntlrQueryError.MissingDynamicContextComponent, "");
            }
            // atomize
            final List<AntlrQueryValue> atoms = atomizer.atomize(ctxItem);
            if (atoms.size() != 1) {
                return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
            }
            input = atoms.getFirst().stringValue;
        } else {
            // one‐arg form
            AntlrQueryValue arg = args.getFirst();
            return valueFactory.bool(arg.stringValue.isEmpty());
        }

        return valueFactory.bool(input.isEmpty());
    }


}
