
package com.github.akruk.antlrquery.evaluator.functionmanager.functions;

import java.util.List;

import com.github.akruk.antlrquery.evaluator.AntlrQueryVisitingContext;
import com.github.akruk.antlrquery.evaluator.collations.Collations;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;

public class FunctionsBasedOnSubstringMatching {
    private final AntlrQueryValueFactory valueFactory;
    public FunctionsBasedOnSubstringMatching(final AntlrQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    /**
     * fn:contains(
     *   $value      as xs:string?,
     *   $substring  as xs:string?,
     *   $collation  as xs:string? := fn:default-collation()
     * ) as xs:boolean
     */
    public AntlrQueryValue contains(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args) {

        // arity check
        if (args.size() < 2 || args.size() > 3) {
            return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        }

        // extract and handle empty-sequence for $value
        final AntlrQueryValue valArg = args.get(0);
        final String value = valArg.isEmptySequence
            ? ""
            : valArg.stringValue;

        // extract and handle empty-sequence for $substring
        final AntlrQueryValue subArg = args.get(1);
        final String substring = subArg.isEmptySequence
            ? ""
            : subArg.stringValue;

        // determine collation URI
        String collationUri = Collations.CODEPOINT_URI;
        if (args.size() == 3) {
            final AntlrQueryValue collArg = args.get(2);
            if (!collArg.isEmptySequence) {
                collationUri = collArg.stringValue;
            }
        }

        // zero-cardinality substring → true
        if (substring.isEmpty()) {
            return valueFactory.bool(true);
        }
        // zero-cardinality value → false
        if (value.isEmpty()) {
            return valueFactory.bool(false);
        }

        // only support Unicode‐codepoint collation for now
        if (!Collations.CODEPOINT_URI.equals(collationUri)) {
            return valueFactory.error(AntlrQueryError.CollationUnitsNotSupported, "");
        }

        // simple substring search under codepoint collation
        final boolean found = value.contains(substring);
        return valueFactory.bool(found);
    }


    /**
     * fn:starts-with(
     *   $value      as xs:string?,
     *   $substring  as xs:string?,
     *   $collation  as xs:string? := fn:default-collation()
     * ) as xs:boolean
     */
    public AntlrQueryValue startsWith(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args) {

        // Arity check: only 2 or 3 arguments allowed
        if (args.size() < 2 || args.size() > 3) {
            return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        }

        // Handle $value
        final AntlrQueryValue valArg = args.get(0);
        final String value = valArg.isEmptySequence
            ? ""
            : valArg.stringValue;

        // Handle $substring
        final AntlrQueryValue subArg = args.get(1);
        final String substring = subArg.isEmptySequence
            ? ""
            : subArg.stringValue;

        // Determine collation URI (default if omitted or empty)
        String collationUri = Collations.CODEPOINT_URI;
        if (args.size() == 3) {
            final AntlrQueryValue collArg = args.get(2);
            if (!collArg.isEmptySequence) {
                collationUri = collArg.stringValue;
            }
        }

        // If substring is zero‐cardinality, always true
        if (substring.isEmpty()) {
            return valueFactory.bool(true);
        }
        // If value is zero‐cardinality and substring non‐empty, false
        if (value.isEmpty()) {
            return valueFactory.bool(false);
        }

        // Currently only support codepoint collation
        if (!Collations.CODEPOINT_URI.equals(collationUri)) {
            return valueFactory.error(AntlrQueryError.CollationUnitsNotSupported, "");
        }

        // Simple prefix check under codepoint collation
        final boolean result = value.startsWith(substring);
        return valueFactory.bool(result);
    }


    public AntlrQueryValue endsWith(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args) {

        // Ensure correct arity: must be 2 or 3 arguments
        if (args.size() < 2 || args.size() > 3) {
            return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        }

        // Normalize $value to zero-cardinality string if empty sequence
        final String value = args.get(0).isEmptySequence
            ? ""
            : args.get(0).stringValue;

        // Normalize $substring to zero-cardinality string if empty sequence
        final String substring = args.get(1).isEmptySequence
            ? ""
            : args.get(1).stringValue;

        // Determine collation URI, defaulting if omitted or empty
        String collationUri = Collations.CODEPOINT_URI;
        if (args.size() == 3) {
            final AntlrQueryValue collArg = args.get(2);
            if (!collArg.isEmptySequence) {
                collationUri = collArg.stringValue;
            }
        }

        // Handle empty substring → always true
        if (substring.isEmpty()) {
            return valueFactory.bool(true);
        }

        // Handle empty value → only true if substring is also empty
        if (value.isEmpty()) {
            return valueFactory.bool(false);
        }

        // Support only standard codepoint collation
        if (!Collations.CODEPOINT_URI.equals(collationUri)) {
            return valueFactory.error(AntlrQueryError.CollationUnitsNotSupported, "");
        }

        // Check if value ends with substring
        final boolean result = value.endsWith(substring);
        return valueFactory.bool(result);
    }

    /**
     * fn:substring-before(
     *   $value      as xs:string?,
     *   $substring  as xs:string?,
     *   $collation  as xs:string? := fn:default-collation()
     * ) as xs:string
     */
    public AntlrQueryValue substringBefore(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args) {

        // Ensure correct arity: must be 2 or 3 args
        if (args.size() < 2 || args.size() > 3) {
            return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        }

        // Normalize $value and $substring to zero-cardinality if empty sequence
        final String value = args.get(0).isEmptySequence
            ? ""
            : args.get(0).stringValue;
        final String substring = args.get(1).isEmptySequence
            ? ""
            : args.get(1).stringValue;

        // Determine collation URI (default if omitted or empty)
        String collationUri = Collations.CODEPOINT_URI;
        if (args.size() == 3) {
            final AntlrQueryValue collArg = args.get(2);
            if (!collArg.isEmptySequence) {
                collationUri = collArg.stringValue;
            }
        }

        // Special cases for empty substring/value
        if (substring.isEmpty()) {
            return valueFactory.string("");
        }
        if (value.isEmpty()) {
            return valueFactory.string("");
        }

        // For now, support only codepoint collation
        if (!Collations.CODEPOINT_URI.equals(collationUri)) {
            return valueFactory.error(AntlrQueryError.CollationUnitsNotSupported, "");
        }

        // Search for first occurrence
        final int index = value.indexOf(substring);
        if (index == -1) {
            return valueFactory.string("");
        }

        final String result = value.substring(0, index);
        return valueFactory.string(result);
    }

    /**
     * fn:substring-after(
     *   $value      as xs:string?,
     *   $substring  as xs:string?,
     *   $collation  as xs:string? := fn:default-collation()
     * ) as xs:string
     */
    public AntlrQueryValue substringAfter(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args) {

        // must have 2 or 3 args
        if (args.size() < 2 || args.size() > 3) {
            return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        }

        // normalize $value and $substring
        final String value = args.get(0).isEmptySequence ? "" : args.get(0).stringValue;
        final String substring = args.get(1).isEmptySequence ? "" : args.get(1).stringValue;

        // determine collation URI (default if omitted or empty)
        String collationUri = Collations.CODEPOINT_URI;
        if (args.size() == 3 && !args.get(2).isEmptySequence) {
            collationUri = args.get(2).stringValue;
        }

        // if substring is empty → return value
        if (substring.isEmpty()) {
            return valueFactory.string(value);
        }

        // if value is empty → always return ""
        if (value.isEmpty()) {
            return valueFactory.string("");
        }

        // for now, only support codepoint collation
        if (!Collations.CODEPOINT_URI.equals(collationUri)) {
            return valueFactory.error(AntlrQueryError.CollationUnitsNotSupported, "");
        }

        // locate first match
        final int idx = value.indexOf(substring);
        if (idx == -1) {
            return valueFactory.string("");
        }

        final String result = value.substring(idx + substring.length());
        return valueFactory.string(result);
    }


}
