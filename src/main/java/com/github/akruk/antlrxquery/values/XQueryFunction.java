package com.github.akruk.antlrxquery.values;

import java.util.List;
import java.util.Map;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;


public interface XQueryFunction {
    public XQueryValue call(final XQueryVisitingContext context,
                            final List<XQueryValue> positionalArguments,
                            final Map<String, XQueryValue> keywordArgs);
}
