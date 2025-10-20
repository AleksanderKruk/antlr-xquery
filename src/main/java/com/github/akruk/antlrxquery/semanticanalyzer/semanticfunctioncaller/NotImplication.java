package com.github.akruk.antlrxquery.semanticanalyzer.semanticfunctioncaller;

import java.util.Map;

import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.Assumption;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.Implication;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.ValueImplication;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContext;
import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;

class NotImplication extends ValueImplication<Boolean> {
    private Boolean value;
    private TypeInContext resultBoolean;
    private TypeInContext argumentBoolean;

    public NotImplication(TypeInContext resultBoolean, TypeInContext argumentBoolean, Boolean value) {
        super(resultBoolean, value);
        this.resultBoolean = resultBoolean;
        this.argumentBoolean = argumentBoolean;
        this.value = value;
    }

    @Override
    public Implication remapTypes(Map<TypeInContext, TypeInContext> typeMapping) {
        return new NotImplication(typeMapping.get(resultBoolean), typeMapping.get(argumentBoolean), this.value);
    }

    @Override
    public void transform(XQuerySemanticContext context) {
        context.currentScope().assume(argumentBoolean, new Assumption(argumentBoolean, !value));
    }


}
