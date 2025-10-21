package com.github.akruk.antlrxquery.semanticanalyzer;

import java.util.Map;

import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.Implication;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.ValueImplication;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContext;
import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;

final class InstanceOfSuccessImplication extends ValueImplication<Boolean> {
    private final TypeInContext target;
    private final Boolean value;
    private final TypeInContext expression;
    private final TypeInContext testedType;

    InstanceOfSuccessImplication(
        TypeInContext target, Boolean value, TypeInContext expression, TypeInContext testedType) {
        super(target, value);
        this.target = target;
        this.value = value;
        this.expression = expression;
        this.testedType = testedType;
    }

    @Override
    public void transform(XQuerySemanticContext context)
    {
        expression.type = testedType.type;
    }

    @Override
    public Implication remapTypes(Map<TypeInContext, TypeInContext> typeMapping)
    {
        return new InstanceOfSuccessImplication(
            typeMapping.getOrDefault(target, target),
            value,
            typeMapping.getOrDefault(expression, expression),
            typeMapping.getOrDefault(testedType, testedType)
        );
    }

}
