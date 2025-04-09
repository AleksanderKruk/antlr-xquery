package com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller;

import java.util.List;

import com.github.akruk.antlrxquery.semanticanalyzer.XQueryVisitingSemanticContext;
import com.github.akruk.antlrxquery.typesystem.XQueryType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public interface XQuerySemanticFunctionCaller {
    public XQueryType call(String functionName, XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, List<XQueryType> args);
    public XQueryType getFunctionReference(String functionName, XQueryTypeFactory valueFactory);
    public XQueryType not(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQueryType> args);
    // fn:abs($arg as xs:numeric?) as xs:numeric?
    public XQueryType abs(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType ceiling(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType floor(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType round(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQueryType> args);
    // public XQueryType roundHaftToEven(final List<XQueryType> args);
    public XQueryType numericAdd(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType numericSubtract(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType numericMultiply(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType numericDivide(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType numericIntegerDivide(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType numericMod(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType numericUnaryPlus(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType numericUnaryMinus(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType true_(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType false_(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType pi(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType empty(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType exists(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType head(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType tail(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType insertBefore(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType remove(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType reverse(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType subsequence(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType substring(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType distinctValues(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType zeroOrOne(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType oneOrMore(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType exactlyOne(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType data(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType contains(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType startsWith(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType endsWith(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType substringAfter(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType substringBefore(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType uppercase(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType lowercase(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType string(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType concat(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType stringJoin(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType position(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType last(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType stringLength(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType normalizeSpace(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
    public XQueryType replace(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQueryType> args);
}
