
package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.util.List;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.evaluator.values.operations.ValueAtomizer;

public class CardinalityFunctions {

    final ValueAtomizer atomizer;
    private final XQueryValueFactory valueFactory;

    public CardinalityFunctions(final XQueryValueFactory valueFactory, final ValueAtomizer atomizer) {

        this.valueFactory = valueFactory;
        this.atomizer = atomizer;
    }

    /**
     * fn:zero-or-one($input as item()*) as item()?
     * Returns $input unchanged if it contains zero or one items;
     * otherwise raises FORG0003.
     */
    public XQueryValue zeroOrOne(
            XQueryVisitingContext context,
            List<XQueryValue> args) {

        XQueryValue input = args.get(0);
        List<XQueryValue> seq = atomizer.atomize(input);
        if (seq.size() > 1) {
            return valueFactory.error(XQueryError.ZeroOrOneWrongArity, "");
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
            List<XQueryValue> args) {

        XQueryValue input = args.get(0);
        List<XQueryValue> seq = atomizer.atomize(input);
        if (seq.isEmpty()) {
            return valueFactory.error(XQueryError.OneOrMoreEmpty, "");
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
            List<XQueryValue> args) {

        XQueryValue input = args.get(0);
        List<XQueryValue> seq = atomizer.atomize(input);
        if (seq.size() != 1) {
            return valueFactory.error(XQueryError.ExactlyOneWrongArity, "");
        }
        // exactly-one returns that single item
        return seq.get(0);
    }
}


