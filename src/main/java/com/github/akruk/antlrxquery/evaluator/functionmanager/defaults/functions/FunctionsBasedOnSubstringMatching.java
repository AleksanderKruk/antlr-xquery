
package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.util.List;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.evaluator.collations.Collations;
import com.github.akruk.antlrxquery.values.XQueryError;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class FunctionsBasedOnSubstringMatching {
    private final XQueryValueFactory valueFactory;
    public FunctionsBasedOnSubstringMatching(final XQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    /**
     * fn:contains(
     *   $value      as xs:string?,
     *   $substring  as xs:string?,
     *   $collation  as xs:string? := fn:default-collation()
     * ) as xs:boolean
     */
    public XQueryValue contains(
            final XQueryVisitingContext context,
            final List<XQueryValue> args) {

        // arity check
        if (args.size() < 2 || args.size() > 3) {
            return XQueryError.WrongNumberOfArguments;
        }

        // extract and handle empty-sequence for $value
        final XQueryValue valArg = args.get(0);
        final String value = valArg.isEmptySequence()
            ? ""
            : valArg.stringValue();

        // extract and handle empty-sequence for $substring
        final XQueryValue subArg = args.get(1);
        final String substring = subArg.isEmptySequence()
            ? ""
            : subArg.stringValue();

        // determine collation URI
        String collationUri = Collations.CODEPOINT_URI;
        if (args.size() == 3) {
            final XQueryValue collArg = args.get(2);
            if (!collArg.isEmptySequence()) {
                collationUri = collArg.stringValue();
            }
        }

        // zero-length substring → true
        if (substring.isEmpty()) {
            return valueFactory.bool(true);
        }
        // zero-length value → false
        if (value.isEmpty()) {
            return valueFactory.bool(false);
        }

        // only support Unicode‐codepoint collation for now
        if (!Collations.CODEPOINT_URI.equals(collationUri)) {
            return XQueryError.CollationUnitsNotSupported;
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
    public XQueryValue startsWith(
            final XQueryVisitingContext context,
            final List<XQueryValue> args) {

        // Arity check: only 2 or 3 arguments allowed
        if (args.size() < 2 || args.size() > 3) {
            return XQueryError.WrongNumberOfArguments;
        }

        // Handle $value
        final XQueryValue valArg = args.get(0);
        final String value = valArg.isEmptySequence()
            ? ""
            : valArg.stringValue();

        // Handle $substring
        final XQueryValue subArg = args.get(1);
        final String substring = subArg.isEmptySequence()
            ? ""
            : subArg.stringValue();

        // Determine collation URI (default if omitted or empty)
        String collationUri = Collations.CODEPOINT_URI;
        if (args.size() == 3) {
            final XQueryValue collArg = args.get(2);
            if (!collArg.isEmptySequence()) {
                collationUri = collArg.stringValue();
            }
        }

        // If substring is zero‐length, always true
        if (substring.isEmpty()) {
            return valueFactory.bool(true);
        }
        // If value is zero‐length and substring non‐empty, false
        if (value.isEmpty()) {
            return valueFactory.bool(false);
        }

        // Currently only support codepoint collation
        if (!Collations.CODEPOINT_URI.equals(collationUri)) {
            return XQueryError.CollationUnitsNotSupported;
        }

        // Simple prefix check under codepoint collation
        final boolean result = value.startsWith(substring);
        return valueFactory.bool(result);
    }


    public XQueryValue endsWith(
            final XQueryVisitingContext context,
            final List<XQueryValue> args) {

        // Ensure correct arity: must be 2 or 3 arguments
        if (args.size() < 2 || args.size() > 3) {
            return XQueryError.WrongNumberOfArguments;
        }

        // Normalize $value to zero-length string if empty sequence
        final String value = args.get(0).isEmptySequence()
            ? ""
            : args.get(0).stringValue();

        // Normalize $substring to zero-length string if empty sequence
        final String substring = args.get(1).isEmptySequence()
            ? ""
            : args.get(1).stringValue();

        // Determine collation URI, defaulting if omitted or empty
        String collationUri = Collations.CODEPOINT_URI;
        if (args.size() == 3) {
            final XQueryValue collArg = args.get(2);
            if (!collArg.isEmptySequence()) {
                collationUri = collArg.stringValue();
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
            return XQueryError.CollationUnitsNotSupported;
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
    public XQueryValue substringBefore(
            final XQueryVisitingContext context,
            final List<XQueryValue> args) {

        // Ensure correct arity: must be 2 or 3 args
        if (args.size() < 2 || args.size() > 3) {
            return XQueryError.WrongNumberOfArguments;
        }

        // Normalize $value and $substring to zero-length if empty sequence
        final String value = args.get(0).isEmptySequence()
            ? ""
            : args.get(0).stringValue();
        final String substring = args.get(1).isEmptySequence()
            ? ""
            : args.get(1).stringValue();

        // Determine collation URI (default if omitted or empty)
        String collationUri = Collations.CODEPOINT_URI;
        if (args.size() == 3) {
            final XQueryValue collArg = args.get(2);
            if (!collArg.isEmptySequence()) {
                collationUri = collArg.stringValue();
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
            return XQueryError.CollationUnitsNotSupported;
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
    public XQueryValue substringAfter(
            final XQueryVisitingContext context,
            final List<XQueryValue> args) {

        // must have 2 or 3 args
        if (args.size() < 2 || args.size() > 3) {
            return XQueryError.WrongNumberOfArguments;
        }

        // normalize $value and $substring
        final String value = args.get(0).isEmptySequence() ? "" : args.get(0).stringValue();
        final String substring = args.get(1).isEmptySequence() ? "" : args.get(1).stringValue();

        // determine collation URI (default if omitted or empty)
        String collationUri = Collations.CODEPOINT_URI;
        if (args.size() == 3 && !args.get(2).isEmptySequence()) {
            collationUri = args.get(2).stringValue();
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
            return XQueryError.CollationUnitsNotSupported;
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
