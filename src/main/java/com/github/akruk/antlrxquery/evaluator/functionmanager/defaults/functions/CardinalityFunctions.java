
package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.util.List;
import java.util.Map;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.values.XQueryError;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class CardinalityFunctions {
    public CardinalityFunctions(final XQueryValueFactory valueFactory) {
    }

    /**
     * fn:zero-or-one($input as item()*) as item()?
     * Returns $input unchanged if it contains zero or one items;
     * otherwise raises FORG0003.
     */
    public XQueryValue zeroOrOne(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String,XQueryValue> kwargs) {

        XQueryValue input = args.get(0);
        List<XQueryValue> seq = input.atomize();
        if (seq.size() > 1) {
            return XQueryError.ZeroOrOneWrongArity;
        }
        // zero-or-one returns the sequence unchanged
        return input;
    }

    /**
     * fn:one-or-more($input as item()*) as item()+
     * Returns $input unchanged if it contains one or more items;
     * otherwise raises FORG0004.
     */
    public XQueryValue oneOrMore(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String,XQueryValue> kwargs) {

        XQueryValue input = args.get(0);
        List<XQueryValue> seq = input.atomize();
        if (seq.isEmpty()) {
            return XQueryError.OneOrMoreEmpty;
        }
        // one-or-more returns the sequence unchanged
        return input;
    }

    /**
     * fn:exactly-one($input as item()*) as item()
     * Returns the single item in $input if there is exactly one;
     * otherwise raises FORG0005.
     */
    public XQueryValue exactlyOne(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String,XQueryValue> kwargs) {

        XQueryValue input = args.get(0);
        List<XQueryValue> seq = input.atomize();
        if (seq.size() != 1) {
            return XQueryError.ExactlyOneWrongArity;
        }
        // exactly-one returns that single item
        return seq.get(0);
    }
}


