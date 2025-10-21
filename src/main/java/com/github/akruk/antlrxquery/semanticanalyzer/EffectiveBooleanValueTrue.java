package com.github.akruk.antlrxquery.semanticanalyzer;

import java.util.Map;
import java.util.Set;

import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.Implication;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.ValueImplication;
import com.github.akruk.antlrxquery.semanticanalyzer.semanticcontext.XQuerySemanticContext;
import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class EffectiveBooleanValueTrue extends ValueImplication<Boolean> {

    private final TypeInContext ebv;
    private final TypeInContext changedType;
    private final XQueryTypeFactory typeFactory;

    public EffectiveBooleanValueTrue(TypeInContext ebv, TypeInContext changedType, XQueryTypeFactory typeFactory) {
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
    public void transform(XQuerySemanticContext context) {
        var variantSingleton = typeFactory.zeroOrOne(typeFactory.itemChoice(Set.of(
            typeFactory.itemString(),
            typeFactory.itemBoolean(),
            typeFactory.itemNumber()
        )));
        if (changedType.isSubtypeOf(variantSingleton)) {
            changedType.type = typeFactory.one(changedType.type.itemType);
            return;
        }
        var variantNodes = typeFactory.zeroOrMore(typeFactory.itemAnyNode());
        if (changedType.isSubtypeOf(variantNodes)) {
            changedType.type = switch(changedType.type.occurence) {
                case ZERO_OR_MORE -> typeFactory.oneOrMore(changedType.type.itemType);
                case ZERO_OR_ONE -> typeFactory.one(changedType.type.itemType);
                default -> changedType.type;
            };
            return;
        }
        return;
    }

}
