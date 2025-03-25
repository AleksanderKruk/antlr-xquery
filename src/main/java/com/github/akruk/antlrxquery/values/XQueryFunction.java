package com.github.akruk.antlrxquery.values;

import java.util.List;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;


public interface XQueryFunction {
    public XQueryValue call(final XQueryValueFactory valueFactory,
                            final XQueryVisitingContext context,
                            final List<XQueryValue> values);
}
