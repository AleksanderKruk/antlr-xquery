package com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller;

import java.util.List;

import com.github.akruk.antlrxquery.semanticanalyzer.XQueryVisitingSemanticContext;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public interface XQuerySemanticFunctionCaller {
    public XQuerySequenceType call(String functionName, XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, List<XQuerySequenceType> args);
    public XQuerySequenceType getFunctionReference(String functionName, XQueryTypeFactory valueFactory);
    public XQuerySequenceType not(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    // fn:abs($arg as xs:numeric?) as xs:numeric?
    public XQuerySequenceType abs(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType ceiling(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType floor(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType round(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    // public XQuerySequenceType roundHaftToEven(final List<XQuerySequenceType> args);
    public XQuerySequenceType numericAdd(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType numericSubtract(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType numericMultiply(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType numericDivide(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType numericIntegerDivide(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType numericMod(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType numericUnaryPlus(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType numericUnaryMinus(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType true_(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType false_(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType pi(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType empty(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType exists(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType head(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType tail(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType insertBefore(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType remove(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType reverse(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType subsequence(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType substring(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType distinctValues(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType zeroOrOne(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType oneOrMore(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType exactlyOne(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType data(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType contains(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType startsWith(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType endsWith(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType substringAfter(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType substringBefore(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType uppercase(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType lowercase(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType string(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType concat(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType stringJoin(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType position(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType last(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType stringLength(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType normalizeSpace(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public XQuerySequenceType replace(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
}
