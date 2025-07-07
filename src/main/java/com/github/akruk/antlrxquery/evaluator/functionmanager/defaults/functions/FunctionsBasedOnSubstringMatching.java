
package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.util.List;
import java.util.Map;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.values.XQueryError;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class FunctionsBasedOnSubstringMatching {
    private final XQueryValueFactory valueFactory;
    public FunctionsBasedOnSubstringMatching(XQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    public XQueryValue contains(XQueryVisitingContext ctx, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        return args.get(0).contains(args.get(1));
    }

    public XQueryValue startsWith(XQueryVisitingContext ctx, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        return args.get(0).startsWith(args.get(1));
    }

    public XQueryValue endsWith(XQueryVisitingContext ctx, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        return args.get(0).endsWith(args.get(1));
    }

    public XQueryValue substringAfter(XQueryVisitingContext ctx, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        return args.get(0).substringAfter(args.get(1));
    }

    public XQueryValue substringBefore(XQueryVisitingContext ctx, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        return args.get(0).substringBefore(args.get(1));
    }
}
