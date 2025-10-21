package com.github.akruk.antlrxquery.semanticanalyzer;

import java.util.List;
import java.util.Map;

import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.Assumption;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.Implication;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.ValueImplication;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContext;
import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;

final class AndTrueImplication extends ValueImplication<Boolean> {
    private final TypeInContext andResult;
    private final List<TypeInContext> andExpressions;

    AndTrueImplication(TypeInContext andResult, List<TypeInContext> andExpressions) {
        super(andResult, true);
        this.andResult = andResult;
        this.andExpressions = andExpressions;
    }

    @Override
    public void transform(XQuerySemanticContext context)
    {
        for (var andExpr : andExpressions) {
            context.currentScope().assume(andExpr, new Assumption(andExpr, true));
        }
    }

    @Override
    public Implication remapTypes(Map<TypeInContext, TypeInContext> typeMapping)
    {
        TypeInContext remappedAndResult = typeMapping.getOrDefault(andResult, andResult);
        List<TypeInContext> remappedAndExpressions = new java.util.ArrayList<>(andExpressions.size());
        for (var expr : andExpressions) {
            remappedAndExpressions.add(typeMapping.getOrDefault(expr, expr));
        }
        return new AndTrueImplication(remappedAndResult, remappedAndExpressions);
    }

}
