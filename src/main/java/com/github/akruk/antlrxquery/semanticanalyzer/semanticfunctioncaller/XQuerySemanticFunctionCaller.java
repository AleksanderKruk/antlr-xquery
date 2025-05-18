package com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller;

import java.util.List;

import com.github.akruk.antlrxquery.semanticanalyzer.XQueryVisitingSemanticContext;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public interface XQuerySemanticFunctionCaller {
    public static record CallAnalysisResult(XQuerySequenceType result, List<String> errors) {}
    public CallAnalysisResult call(String functionName, XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, List<XQuerySequenceType> args);
    public CallAnalysisResult getFunctionReference(String functionName, XQueryTypeFactory valueFactory);
    public CallAnalysisResult not(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    // fn:abs($arg as xs:numeric?) as xs:numeric?
    public CallAnalysisResult abs(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult ceiling(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult floor(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult round(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    // public CallAnalysisResult roundHaftToEven(final List<XQuerySequenceType> args);
    public CallAnalysisResult numericAdd(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult numericSubtract(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult numericMultiply(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult numericDivide(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult numericIntegerDivide(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult numericMod(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult numericUnaryPlus(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult numericUnaryMinus(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult true_(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult false_(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult pi(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult empty(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult exists(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult head(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult tail(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult insertBefore(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult remove(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult reverse(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult subsequence(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult substring(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult distinctValues(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult zeroOrOne(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult oneOrMore(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult exactlyOne(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult data(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult contains(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult startsWith(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult endsWith(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult substringAfter(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult substringBefore(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult uppercase(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult lowercase(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult string(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult concat(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult stringJoin(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult position(final XQueryTypeFactory valueFactory, final XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult last(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult stringLength(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult normalizeSpace(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
    public CallAnalysisResult replace(final XQueryTypeFactory valueFactory, XQueryVisitingSemanticContext context, final List<XQuerySequenceType> args);
}
