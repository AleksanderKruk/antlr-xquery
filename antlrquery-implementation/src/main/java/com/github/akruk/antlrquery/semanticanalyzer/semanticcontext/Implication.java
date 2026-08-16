package com.github.akruk.antlrquery.semanticanalyzer.semanticcontext;

import java.util.Map;

import com.github.akruk.antlrquery.typesystem.types.TypeInContext;

public interface Implication
{
    Implication remapTypes(Map<TypeInContext, TypeInContext> typeMapping);
    boolean isApplicable(AntlrQuerySemanticContext context);
    void transform(AntlrQuerySemanticContext context);
}
