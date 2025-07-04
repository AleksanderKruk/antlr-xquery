package com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller;

import java.util.List;
import java.util.Map;

import com.github.akruk.antlrxquery.semanticanalyzer.XQuerySemanticError;
import com.github.akruk.antlrxquery.semanticanalyzer.XQueryVisitingSemanticContext;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

public interface IXQuerySemanticFunctionManager {
    public static record CallAnalysisResult(XQuerySequenceType result, List<String> errors) {}
    public static record ArgumentSpecification(String name, boolean isRequired, XQuerySequenceType type) {}
    public CallAnalysisResult call(String namespace,
                                   String functionName,
                                   List<XQuerySequenceType> positionalargs,
                                   Map<String, XQuerySequenceType> keywordArgs,
                                   XQueryVisitingSemanticContext context);
    public XQuerySemanticError register(String namespace,
                                        String functionName,
                                        List<ArgumentSpecification> args,
                                        XQuerySequenceType returnedType);
    public CallAnalysisResult getFunctionReference(String namespace, String functionName, int arity);
}
