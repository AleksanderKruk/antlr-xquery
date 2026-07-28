package com.github.akruk.antlrquery.semanticanalyzer;

import java.util.Map;

import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.Implication;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.ValueImplication;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContext;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.TypeInContext;

public class EffectiveBooleanValueTrue extends ValueImplication<Boolean> {

    private final TypeInContext ebv;
    private final TypeInContext changedType;
    private final AntlrQueryTypeFactory typeFactory;

    public EffectiveBooleanValueTrue(TypeInContext ebv, TypeInContext changedType, AntlrQueryTypeFactory typeFactory) {
        super(ebv, true);
        this.ebv = ebv;
        this.changedType = changedType;
        this.typeFactory = typeFactory;
    }

    @Override
    public Implication remapTypes(Map<TypeInContext, TypeInContext> typeMapping) {
        TypeInContext remappedEbv = typeMapping.getOrDefault(ebv, ebv);
        TypeInContext remappedChangedType = typeMapping.getOrDefault(changedType, changedType);
        return new EffectiveBooleanValueTrue(remappedEbv, remappedChangedType, typeFactory);
    }

    @Override
    public void transform(AntlrQuerySemanticContext context) {
        var variantSingleton = typeFactory.zeroOrOne(typeFactory.itemChoice(
            typeFactory.itemString(),
            typeFactory.itemBoolean(),
            typeFactory.itemNumber()
        ));
        if (changedType.isSubtypeOf(variantSingleton)) {
            changedType.type = typeFactory.one(changedType.type.itemType());
            return;
        }
        var variantNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        if (changedType.isSubtypeOf(variantNodes)) {
            if (changedType.type.cardinality() == Cardinality.ZERO_OR_MORE) {
                changedType.type = typeFactory.oneOrMore(changedType.type.itemType());
            } else if (changedType.type.cardinality() == Cardinality.ZERO_OR_ONE) {
                changedType.type = typeFactory.one(changedType.type.itemType());
            } else {
                changedType.type = changedType.type;
            }
            return;
        }
        return;
    }

}
