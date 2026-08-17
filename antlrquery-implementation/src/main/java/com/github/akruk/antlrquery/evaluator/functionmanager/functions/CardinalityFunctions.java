
package com.github.akruk.antlrquery.evaluator.functionmanager.functions;

import java.util.List;

import com.github.akruk.antlrquery.evaluator.AntlrQueryVisitingContext;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;
import com.github.akruk.antlrquery.evaluator.values.operations.ValueAtomizer;

public class CardinalityFunctions {

    final ValueAtomizer atomizer;
    private final AntlrQueryValueFactory valueFactory;

    public CardinalityFunctions(final AntlrQueryValueFactory valueFactory, final ValueAtomizer atomizer) {

        this.valueFactory = valueFactory;
        this.atomizer = atomizer;
    }

    /**
     * fn:zero-or-one($input as item()*) as item()?
     * Returns $input unchanged if it contains zero or one items;
     * otherwise raises FORG0003.
     */
    public AntlrQueryValue zeroOrOne(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        AntlrQueryValue input = args.getFirst();
        List<AntlrQueryValue> seq = atomizer.atomize(input);
        if (seq.size() > 1) {
            return valueFactory.error(AntlrQueryError.ZeroOrOneWrongArity, "");
        }
        // zero-or-one returns the sequence unchanged
        return input;
    }

    /**
     * fn:one-or-more($input as item()*) as item()+
     * Returns $input unchanged if it contains one or more items;
     * otherwise raises FORG0004.
     */
    public AntlrQueryValue oneOrMore(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        AntlrQueryValue input = args.getFirst();
        List<AntlrQueryValue> seq = atomizer.atomize(input);
        if (seq.isEmpty()) {
            return valueFactory.error(AntlrQueryError.OneOrMoreEmpty, "");
        }
        // one-or-more returns the sequence unchanged
        return input;
    }

    /**
     * fn:exactly-one($input as item()*) as item()
     * Returns the single item in $input if there is exactly one;
     * otherwise raises FORG0005.
     */
    public AntlrQueryValue exactlyOne(
            AntlrQueryVisitingContext context,
            List<AntlrQueryValue> args) {

        AntlrQueryValue input = args.getFirst();
        List<AntlrQueryValue> seq = atomizer.atomize(input);
        if (seq.size() != 1) {
            return valueFactory.error(AntlrQueryError.ExactlyOneWrongArity, "");
        }
        // exactly-one returns that single item
        return seq.getFirst();
    }
}


