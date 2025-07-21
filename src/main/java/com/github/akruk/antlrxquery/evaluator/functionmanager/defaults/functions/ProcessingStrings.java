package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.antlr.v4.runtime.Parser;
import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.evaluator.collations.Collations;
import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.evaluator.values.operations.EffectiveBooleanValue;
import com.github.akruk.antlrxquery.evaluator.values.operations.ValueAtomizer;

public class ProcessingStrings {

    private final XQueryValueFactory valueFactory;
    // private final Parser targetParser;
    private final Map<String, Collator> collationUriToCollator;
    // private final Collator defaultCollation;
    private final Locale defaultLocale;
    private final EffectiveBooleanValue ebv;
    private final ValueAtomizer atomizer;

    public ProcessingStrings(final XQueryValueFactory valueFactory,
                            final Parser targetParser,
                            final Collator defaultCollation,
                            final Map<String, Collator> collationUriToCollator,
                            final Locale defaultLocale,
                            final ValueAtomizer atomizer, EffectiveBooleanValue ebv)
    {
        this.valueFactory = valueFactory;
        // this.targetParser = targetParser;
        // this.defaultCollation = defaultCollation;
        this.collationUriToCollator = collationUriToCollator;
        this.defaultLocale = defaultLocale;
        this.ebv = ebv;
        this.atomizer = atomizer;
    }

    public XQueryValue codepointsToString(
            XQueryVisitingContext context,
            List<XQueryValue> args) {

        XQueryValue values = args.get(0);

        if (values.isEmptySequence) {
            return valueFactory.string("");
        }

        StringBuilder sb = new StringBuilder();

        final var atomized = atomizer.atomize(values);
        for (XQueryValue value : atomized) {
            Integer codepoint = value.numericValue.intValue();
            if (codepoint < 0 || codepoint > 0x10FFFF ||
                (codepoint >= 0xD800 && codepoint <= 0xDFFF)) {
                return valueFactory.error(XQueryError.InvalidCodepoint, "");
            }

            sb.appendCodePoint(codepoint);
        }

        return valueFactory.string(sb.toString());
    }

    public XQueryValue stringToCodepoints(
            XQueryVisitingContext context,
            List<XQueryValue> args) {

        XQueryValue value = args.get(0);

        if (value.isEmptySequence) {
            return value;
        }

        String str = value.stringValue;
        List<XQueryValue> codepoints = new ArrayList<>();

        for (int i = 0; i < str.length(); ) {
            int codepoint = str.codePointAt(i);
            codepoints.add(valueFactory.number(codepoint));
            i += Character.charCount(codepoint);
        }

        return valueFactory.sequence(codepoints);
    }

    public XQueryValue codepointEqual(
            XQueryVisitingContext context,
            List<XQueryValue> args) {

        XQueryValue value1 = args.get(0);
        XQueryValue value2 = args.get(1);

        if (value1.isEmptySequence || value2.isEmptySequence) {
            return valueFactory.emptySequence();
        }

        String str1 = value1.stringValue;
        String str2 = value2.stringValue;

        if (str1 == null || str2 == null) {
            return valueFactory.error(XQueryError.InvalidArgumentType, "");
        }

        return valueFactory.bool(str1.equals(str2));
    }

    public XQueryValue collation(
            XQueryVisitingContext context,
            List<XQueryValue> args)
    {

        XQueryValue optionsArg = args.get(0);
        Map<XQueryValue, XQueryValue> map = optionsArg.mapEntries;

        String baseUri = Collations.CODEPOINT_URI;
        List<String> queryParts = new ArrayList<>();
        for (Map.Entry<XQueryValue, XQueryValue> entry : map.entrySet()) {
            XQueryValue key = entry.getKey();
            XQueryValue val = entry.getValue();

            String name = key.stringValue;

            String valueStr = val.stringValue;

            queryParts.add(name + "=" + valueStr);
        }

        String uri = queryParts.isEmpty()
            ? baseUri
            : baseUri + "?" + String.join(";", queryParts);

        if (collationUriToCollator.containsKey(uri)) {
            return valueFactory.string(uri);
        }

        // Defaults
        //   fallback=true, lang=defaultLanguage(), strength=IDENTICAL, maxVariable=punct,
        //   alternate=non-ignorable, backwards=false, normalization=false,
        //   caseLevel=false, caseFirst=lower, numeric=false

        Locale locale = defaultLocale;
        if (map.containsKey(valueFactory.string("lang"))) {
            String langTag = map.get(valueFactory.string("lang")).stringValue;
            locale = Locale.forLanguageTag(langTag);
        }

        Collator coll = Collator.getInstance(locale);

        if (map.containsKey(valueFactory.string("strength"))) {
            String s = map.get(valueFactory.string("strength")).stringValue;
            switch (s) {
                case "primary": case "1": coll.setStrength(Collator.PRIMARY); break;
                case "secondary": case "2": coll.setStrength(Collator.SECONDARY); break;
                case "tertiary": case "3": coll.setStrength(Collator.TERTIARY); break;
                case "quaternary": case "4": coll.setStrength(Collator.IDENTICAL); break;
                case "identical": case "5": coll.setStrength(Collator.IDENTICAL); break;
                default: break;
            }
        }

        if (coll instanceof RuleBasedCollator rbc) {
            final XQueryValue backwards = valueFactory.string("backwards");
            if (map.containsKey(backwards)
                    && ebv.effectiveBooleanValue(map.get(backwards)).booleanValue)
            {
                // TODO: ...
                // rbc.getAlternateHandlingShifted(true);
            }

            final XQueryValue normalization = map.get(valueFactory.string("normalization"));
            if (map.containsKey(valueFactory.string("normalization"))
                    && ebv.effectiveBooleanValue(map.get(normalization)).booleanValue)
            {
                rbc.setDecomposition(RuleBasedCollator.CANONICAL_DECOMPOSITION);
            }

            final XQueryValue caseLevel = valueFactory.string("caseLevel");
            if (map.containsKey(caseLevel))
            {
                // TODO: ...
                // rbc.setCaseLevel(map.get(caseLevel).effectiveBooleanValue());
            }

            final XQueryValue numeric = valueFactory.string("numeric");
            if (map.containsKey(numeric)
                    && ebv.effectiveBooleanValue(map.get(numeric)).booleanValue)
            {
                // TODO: ...
            }

            final XQueryValue caseFirst = valueFactory.string("caseFirst");
            if (map.containsKey(caseFirst)) {
                // String cf = map.get(caseFirst).stringValue;
                // TODO: ...
                // rbc.setUpperCaseFirst("upper".equals(cf));
            }
        }

        collationUriToCollator.put(uri, coll);
        return valueFactory.string(uri);
    }

    public XQueryValue collationAvailable(
            XQueryVisitingContext context,
            List<XQueryValue> args) {

        final XQueryValue collation = args.get(0);
        // final XQueryValue usage = args.get(1);
        String collationStr = collation.stringValue;
        boolean available = this.collationUriToCollator.containsKey(collationStr);
        return valueFactory.bool(available);
    }


    public XQueryValue containsToken(
            XQueryVisitingContext context,
            List<XQueryValue> args) {

        XQueryValue value = args.get(0);
        XQueryValue token = args.get(1);
        XQueryValue collationArg = args.get(2);

        if (value.isEmptySequence) {
            return valueFactory.bool(false);
        }

        String rawToken = token.stringValue.strip();
        if (rawToken.isEmpty()) {
            return valueFactory.bool(false);
        }

        String collationUri = collationArg.stringValue;
        Collator collator = collationUriToCollator.get(collationUri);

        final var atomized = atomizer.atomize(value);
        for (XQueryValue item : atomized) {
            String str = item.stringValue;
            for (String t : str.split("\\s+")) {
                if (collator.compare(t, rawToken) == 0) {
                    return valueFactory.bool(true);
                }
            }
        }

        return valueFactory.bool(false);
    }


}
