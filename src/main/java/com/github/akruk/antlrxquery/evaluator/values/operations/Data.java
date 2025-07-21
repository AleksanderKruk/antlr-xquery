package com.github.akruk.antlrxquery.evaluator.values.operations;

import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

public class Data {

    private final XQueryValueFactory valueFactory;
    private final ValueAtomizer atomizer;

    public Data(final XQueryValueFactory valueFactory, final ValueAtomizer atomizer)
    {
        this.valueFactory = valueFactory;
        this.atomizer = atomizer;
    }

    public XQueryValue data(XQueryValue target) {
        return valueFactory.sequence(atomizer.atomize(target));
    }


}
