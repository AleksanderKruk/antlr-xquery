package com.github.akruk.antlrxquery.evaluator.functioncaller;

import java.util.List;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public interface XQueryFunctionCaller {
    public XQueryValue call(String functionName, XQueryValueFactory valueFactory, XQueryVisitingContext context, List<XQueryValue> args);
    public XQueryValue getFunctionReference(String functionName, XQueryValueFactory valueFactory);
    public XQueryValue not(final XQueryValueFactory valueFactory, final XQueryVisitingContext context, final List<XQueryValue> args);
    // fn:abs($arg as xs:numeric?) as xs:numeric?
    public XQueryValue abs(final XQueryValueFactory valueFactory, final XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue ceiling(final XQueryValueFactory valueFactory, final XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue floor(final XQueryValueFactory valueFactory, final XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue round(final XQueryValueFactory valueFactory, final XQueryVisitingContext context, final List<XQueryValue> args);
    // public XQueryValue roundHaftToEven(final List<XQueryValue> args);
    public XQueryValue numericAdd(final XQueryValueFactory valueFactory, final XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue numericSubtract(final XQueryValueFactory valueFactory, final XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue numericMultiply(final XQueryValueFactory valueFactory, final XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue numericDivide(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue numericIntegerDivide(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue numericMod(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue numericUnaryPlus(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue numericUnaryMinus(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue true_(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue false_(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue pi(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue empty(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue exists(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue head(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue tail(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue insertBefore(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue remove(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue reverse(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue subsequence(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue substring(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue distinctValues(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue zeroOrOne(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue oneOrMore(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue exactlyOne(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue data(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue contains(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue startsWith(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue endsWith(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue substringAfter(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue substringBefore(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue uppercase(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue lowercase(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue string(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue concat(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue stringJoin(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue position(final XQueryValueFactory valueFactory, final XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue last(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue stringLength(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue normalizeSpace(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);
    public XQueryValue replace(final XQueryValueFactory valueFactory, XQueryVisitingContext context, final List<XQueryValue> args);

}
