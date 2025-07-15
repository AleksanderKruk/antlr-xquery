package com.github.akruk.antlrxquery.values;

import java.util.List;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;


public interface XQueryFunction {
    public XQueryValue call(final XQueryVisitingContext context,
                            final List<XQueryValue> positionalArguments);
}
