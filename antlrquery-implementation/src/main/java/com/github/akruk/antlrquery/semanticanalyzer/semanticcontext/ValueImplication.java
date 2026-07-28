package com.github.akruk.antlrquery.semanticanalyzer.semanticcontext;

import com.github.akruk.antlrquery.typesystem.types.TypeInContext;

public abstract class ValueImplication<T> implements Implication
{
    private Assumption matchingAssumption;

    public ValueImplication(TypeInContext target, T value) {
        this.matchingAssumption = new Assumption(target, value);
    }

    @Override
    public boolean isApplicable(AntlrQuerySemanticContext context)
    {
        return context.currentScope().existsAssumption(matchingAssumption);
    }
}
