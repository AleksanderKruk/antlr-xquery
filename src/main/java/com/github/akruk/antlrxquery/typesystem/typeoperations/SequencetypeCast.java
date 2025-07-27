package com.github.akruk.antlrxquery.typesystem.typeoperations;

import java.util.function.Supplier;

import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.typeoperations.SequencetypeCastable.Castability;
import com.github.akruk.antlrxquery.typesystem.typeoperations.SequencetypeCastable.IsCastableResult;
import com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype.ItemtypeIsValidCastTarget;

public class SequencetypeCast {
    private final XQuerySequenceType anyItems;
    private final XQueryTypeFactory typeFactory;
    private final SequencetypeAtomization atomizer;
    private final ItemtypeIsValidCastTarget isValidCastTarget;
    private final XQuerySequenceType boolean_;
    private final CastResult wrongTargetType;
    private final CastResult emptySequence;
    // private final CastResult unnecessary;
    // private final CastResult canBeEmptyWithoutFlag;
    // private final CastResult ok;
    // private final CastResult zeroOrMore;

    public SequencetypeCast(XQueryTypeFactory typeFactory, SequencetypeAtomization atomizer) {
        this.typeFactory = typeFactory;
        this.atomizer = atomizer;
        this.anyItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        this.isValidCastTarget = new ItemtypeIsValidCastTarget();
        this.boolean_ = typeFactory.boolean_();
        this.wrongTargetType = new CastResult(anyItems, null, Castability.WRONG_TARGET_TYPE, null, null);
        this.emptySequence = new CastResult(typeFactory.emptySequence(), Castability.TESTED_EXPRESSION_IS_EMPTY_SEQUENCE, null, null);

        // this.zeroOrMore = new CastResult(boolean_, Castability.TESTED_EXPRESSION_IS_ZERO_OR_MORE, null);
        // this.canBeEmptyWithoutFlag = new CastResult(boolean_, Castability.TESTED_EXPRESSION_CAN_BE_EMPTY_SEQUENCE_WITHOUT_FLAG, null);
        // this.unnecessary = new CastResult(boolean_, Castability.UNNECESSARY, null);
    }

    public enum EmptyFlagState {
        VALID,
        INVALID
    }


    public static record CastResult(XQuerySequenceType type, CastingStatus castability, EmptyFlagState empty, XQueryItemType invalidType) {}

    // error(ctx, "Type: " + type + "is an invalid cast target for type");
    // error(ctx, "Empty sequence is not allowed without adding '?' after target type");
    // warn(ctx, "Empty sequence as input of cast expression");
    // error(ctx, "Empty sequence is not allowed without adding '?' after target type");
    // error(ctx, "Sequences cannot be the target of casting unless they are of type item() or item()? when '?' is specified");
    // warn(ctx, "Unnecessary castability test");
    // error(ctx, errorMessageSupplier.get());
    public CastResult cast(XQuerySequenceType type, XQuerySequenceType tested, boolean isEmptyAllowed)
    {
        if (!isValidCastTarget.test(type.getItemType())) {
            return wrongTargetType;
        }
        final var atomized = atomizer.atomize(tested);
        EmptyFlagState flagstate;
        switch (atomized.getOccurence()) {
            case ZERO:
                return emptySequence;
            case ZERO_OR_ONE:
                flagstate = isEmptyAllowed? EmptyFlagState.VALID: EmptyFlagState.INVALID;
                handleCast(atomized, tested, type, typeFactory.zeroOrOne(atomized.getItemType()), flagstate);
                break;
            case ONE:
                // if (impossibleCasing(null);)
                handleCast(atomized, tested, type, typeFactory.one(atomized.getItemType()));
                break;
            case ZERO_OR_MORE:
                flagstate = isEmptyAllowed? EmptyFlagState.VALID: EmptyFlagState.INVALID;
                return new CastResult(anyItems, Castability.TESTED_EXPRESSION_IS_ZERO_OR_MORE, flagstate, null);
            case ONE_OR_MORE:
                flagstate = EmptyFlagState.VALID;
                return new CastResult(anyItems, Castability.TESTED_EXPRESSION_IS_ZERO_OR_MORE, flagstate, null);
        };
    }

    CastResult handleCast(
            XQuerySequenceType atomized,
            XQuerySequenceType tested,
            XQuerySequenceType type,
            XQuerySequenceType result,
            EmptyFlagState emptyFlagState)
    {
        if (atomized.itemtypeIsSubtypeOf(tested)) {
            return new CastResult(type, Castability.UNNECESSARY, emptyFlagState, null);
        }
        final XQueryItemType atomizedItemtype = atomized.getItemType();
        final XQueryTypes atomizedItemtypeType = atomizedItemtype.getType();
        final XQueryTypes castTargetType = type.getItemType().getType();
        if (atomizedItemtypeType == XQueryTypes.CHOICE)
        {
            final var itemtypes = atomizedItemtype.getItemTypes();
            for (final var itemtype : itemtypes) {
                if (impossibleCasing(castTargetType)) {
                    return new CastResult(anyItems, Castability.ALWAYS_POSSIBLE_MANY , emptyFlagState, atomizedItemtype)
                }
            }
            return result;
        }
        impossibleCasing(castTargetType);
        return result;
    }

    void impossibleCasing(XQueryTypes castTargetType)
    {
        switch (castTargetType) {
            case STRING, NUMBER, ENUM, BOOLEAN:
                break;
            default:
        };
    }


}
