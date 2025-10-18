package com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext;

import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;

public abstract class ValueImplication<T> implements Implication
{
    private Assumption matchingAssumption;

    public ValueImplication(TypeInContext target, T value) {
        this.matchingAssumption = new Assumption(target, value);
    }

    @Override
    public boolean isApplicable(XQuerySemanticContext context)
    {
        return context.currentScope().existsAssumption(matchingAssumption);
    }
}
