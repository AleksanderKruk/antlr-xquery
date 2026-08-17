package com.github.akruk.antlrquery.evaluator.functionmanager.functions;

import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.antlr.v4.runtime.Parser;
import com.github.akruk.antlrquery.evaluator.AntlrQueryVisitingContext;
import com.github.akruk.antlrquery.evaluator.collations.Collations;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;
import com.github.akruk.antlrquery.evaluator.values.operations.EffectiveBooleanValue;
import com.github.akruk.antlrquery.evaluator.values.operations.ValueAtomizer;
public class ProcessingStrings {

    private final AntlrQueryValueFactory valueFactory;
    // private final Parser targetParser;
    private final Map<String, Collator> collationUriToCollator;
    // private final Collator defaultCollation;
    private final Locale defaultLocale;
    private final EffectiveBooleanValue ebv;
    private final ValueAtomizer atomizer;

    public ProcessingStrings(final AntlrQueryValueFactory valueFactory,
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

    public AntlrQueryValue codepointsToString(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        AntlrQueryValue values = args.get(0);

        if (values.isEmptySequence) {
            return valueFactory.string("");
        }

        StringBuilder sb = new StringBuilder();

        final var atomized = atomizer.atomize(values);
        for (AntlrQueryValue value : atomized) {
            Integer codepoint = value.numericValue.intValue();
            if (codepoint < 0 || codepoint > 0x10FFFF ||
                (codepoint >= 0xD800 && codepoint <= 0xDFFF)) {
                return valueFactory.error(AntlrQueryError.InvalidCodepoint, "");
            }

            sb.appendCodePoint(codepoint);
        }

        return valueFactory.string(sb.toString());
    }

    public AntlrQueryValue stringToCodepoints(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        AntlrQueryValue value = args.get(0);

        if (value.isEmptySequence) {
            return value;
        }

        String str = value.stringValue;
        List<AntlrQueryValue> codepoints = new ArrayList<>();

        for (int i = 0; i < str.length(); ) {
            int codepoint = str.codePointAt(i);
            codepoints.add(valueFactory.number(codepoint));
            i += Character.charCount(codepoint);
        }

        return valueFactory.sequence(codepoints);
    }

    public AntlrQueryValue codepointEqual(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        AntlrQueryValue value1 = args.get(0);
        AntlrQueryValue value2 = args.get(1);

        if (value1.isEmptySequence || value2.isEmptySequence) {
            return valueFactory.emptySequence();
        }

        String str1 = value1.stringValue;
        String str2 = value2.stringValue;

        if (str1 == null || str2 == null) {
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        }

        return valueFactory.bool(str1.equals(str2));
    }

    public AntlrQueryValue collation(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args)
    {

        AntlrQueryValue optionsArg = args.get(0);
        Map<AntlrQueryValue, AntlrQueryValue> map = optionsArg.mapEntries;

        String baseUri = Collations.CODEPOINT_URI;
        List<String> queryParts = new ArrayList<>();
        for (Map.Entry<AntlrQueryValue, AntlrQueryValue> entry : map.entrySet()) {
            AntlrQueryValue key = entry.getKey();
            AntlrQueryValue val = entry.getValue();

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
                case "quaternary": case "4", "identical", "5": coll.setStrength(Collator.IDENTICAL); break;
                default: break;
            }
        }

        if (coll instanceof RuleBasedCollator rbc) {
            final AntlrQueryValue backwards = valueFactory.string("backwards");
            if (map.containsKey(backwards)) {
                ebv.effectiveBooleanValue(map.get(backwards));
            }// TODO: ...
// rbc.getAlternateHandlingShifted(true);

            final AntlrQueryValue normalization = map.get(valueFactory.string("normalization"));
            if (map.containsKey(valueFactory.string("normalization"))
                    && ebv.effectiveBooleanValue(map.get(normalization)).booleanValue)
            {
                rbc.setDecomposition(RuleBasedCollator.CANONICAL_DECOMPOSITION);
            }

            final AntlrQueryValue caseLevel = valueFactory.string("caseLevel");
            // TODO: ...
            // rbc.setCaseLevel(map.get(caseLevel).effectiveBooleanValue());

            final AntlrQueryValue numeric = valueFactory.string("numeric");
            if (map.containsKey(numeric)) {
                ebv.effectiveBooleanValue(map.get(numeric));
            }// TODO: ...

            final AntlrQueryValue caseFirst = valueFactory.string("caseFirst");
            // String cf = map.get(caseFirst).stringValue;
            // TODO: ...
            // rbc.setUpperCaseFirst("upper".equals(cf));
        }

        collationUriToCollator.put(uri, coll);
        return valueFactory.string(uri);
    }

    public AntlrQueryValue collationAvailable(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args)
    {

        final AntlrQueryValue collation = args.get(0);
        // final XQueryValue usage = args.get(1);
        String collationStr = collation.stringValue;
        boolean available = this.collationUriToCollator.containsKey(collationStr);
        return valueFactory.bool(available);
    }


    public AntlrQueryValue containsToken(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        AntlrQueryValue value = args.get(0);
        AntlrQueryValue token = args.get(1);
        AntlrQueryValue collationArg = args.get(2);

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
        for (AntlrQueryValue item : atomized) {
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
