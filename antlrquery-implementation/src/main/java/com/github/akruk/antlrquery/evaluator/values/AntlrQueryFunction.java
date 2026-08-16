package com.github.akruk.antlrquery.evaluator.values;

import java.util.List;

import com.github.akruk.antlrquery.evaluator.AntlrQueryVisitingContext;


public interface AntlrQueryFunction {
    AntlrQueryValue call(final AntlrQueryVisitingContext context,
                         final List<AntlrQueryValue> positionalArguments);
}
