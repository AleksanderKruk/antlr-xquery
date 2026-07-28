package com.github.akruk.antlrquery.semanticanalyzer.semanticcontext;

import java.util.Map;

import com.github.akruk.antlrquery.typesystem.types.TypeInContext;

public interface Implication
{
    public Implication remapTypes(Map<TypeInContext, TypeInContext> typeMapping);
    public boolean isApplicable(AntlrQuerySemanticContext context);
    public void transform(AntlrQuerySemanticContext context);
}
