package com.github.akruk.antlrxquery.evaluator.functionmanager;

import java.util.List;
import java.util.Map;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.values.XQueryValue;

public interface IXQueryEvaluatingFunctionManager {
    public XQueryValue call(String namespace,
                            String functionName,
                            XQueryVisitingContext context,
                            List<XQueryValue> args,
                            Map<String, XQueryValue> kwargs);
    public XQueryValue getFunctionReference(String namespace, String functionName, long arity);
}
