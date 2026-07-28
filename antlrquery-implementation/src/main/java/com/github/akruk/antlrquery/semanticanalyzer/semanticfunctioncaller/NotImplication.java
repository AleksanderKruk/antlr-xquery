package com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller;

import java.util.Map;

import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.Assumption;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.Implication;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.ValueImplication;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContext;
import com.github.akruk.antlrquery.typesystem.types.TypeInContext;

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
    public void transform(AntlrQuerySemanticContext context) {
        context.currentScope().assume(argumentBoolean, new Assumption(argumentBoolean, !value));
    }


}
