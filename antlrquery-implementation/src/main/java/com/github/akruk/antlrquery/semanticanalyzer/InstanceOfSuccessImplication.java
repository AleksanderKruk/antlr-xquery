package com.github.akruk.antlrquery.semanticanalyzer;

import java.util.Map;

import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.Implication;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.ValueImplication;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContext;
import com.github.akruk.antlrquery.typesystem.types.TypeInContext;

public final class InstanceOfSuccessImplication extends ValueImplication<Boolean> {
    private final TypeInContext target;
    private final Boolean value;
    private final TypeInContext expression;
    private final TypeInContext testedType;

    public InstanceOfSuccessImplication(
        TypeInContext target, Boolean value, TypeInContext expression, TypeInContext testedType) {
        super(target, value);
        this.target = target;
        this.value = value;
        this.expression = expression;
        this.testedType = testedType;
    }

    @Override
    public void transform(AntlrQuerySemanticContext context)
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
