package com.github.akruk.antlrxquery.evaluator.functioncaller;

import java.util.List;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public interface XQueryFunctionCaller {
    public XQueryValue call(String functionName, XQueryVisitingContext context, List<XQueryValue> args);
    public XQueryValue getFunctionReference(String functionName);
    public XQueryValue not(final XQueryVisitingContext context, final List<XQueryValue> args);
    // fn:abs($arg as xs:numeric?) as xs:numeric?
    public XQueryValue abs(final XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue ceiling(final XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue floor(final XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue round(final XQueryVisitingContext context, final List<XQueryValue> args);
    // public XQueryValue roundHaftToEven(final List<XQueryValue> args);
    public XQueryValue numericAdd(final XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue numericSubtract(final XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue numericMultiply(final XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue numericDivide(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue numericIntegerDivide(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue numericMod(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue numericUnaryPlus(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue numericUnaryMinus(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue true_(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue false_(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue pi(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue empty(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue exists(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue head(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue tail(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue insertBefore(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue remove(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue reverse(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue subsequence(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue substring(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue distinctValues(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue zeroOrOne(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue oneOrMore(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue exactlyOne(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue data(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue contains(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue startsWith(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue endsWith(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue substringAfter(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue substringBefore(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue uppercase(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue lowercase(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue string(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue concat(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue stringJoin(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue position(final XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue last(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue stringLength(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue normalizeSpace(XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue replace(XQueryVisitingContext context, final List<XQueryValue> args);

}
