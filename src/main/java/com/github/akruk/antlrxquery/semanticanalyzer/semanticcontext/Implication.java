package com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext;

import java.util.Map;

import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;

public interface Implication
{
    public Implication remapTypes(Map<TypeInContext, TypeInContext> typeMapping);
    public boolean isApplicable(XQuerySemanticContext context);
    public void transform(XQuerySemanticContext context);
}
