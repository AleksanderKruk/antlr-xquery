package com.github.akruk.antlrxquery.evaluator.functioncaller;

import java.util.List;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.values.XQueryValue;

public interface XQueryFunctionCaller {
    public XQueryValue call(String functionName, XQueryVisitingContext context, List<XQueryValue> args);
    public XQueryValue getFunctionReference(String functionName);
}
