package com.github.akruk.antlrquery.evaluator.values.operations;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;

public class Data {

    private final AntlrQueryValueFactory valueFactory;
    private final ValueAtomizer atomizer;

    public Data(final AntlrQueryValueFactory valueFactory, final ValueAtomizer atomizer)
    {
        this.valueFactory = valueFactory;
        this.atomizer = atomizer;
    }

    public AntlrQueryValue data(final AntlrQueryValue target) {
        return valueFactory.sequence(atomizer.atomize(target));
    }


}
