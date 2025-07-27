package com.github.akruk.antlrxquery.typesystem.typeoperations;



import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.defaults.XQueryTypes;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype.ItemtypeIsValidCastTarget;

public class SequencetypeCastable {
    // private final XQuerySequenceType anyItems;
    private final XQueryTypeFactory typeFactory;

    public SequencetypeCastable(XQueryTypeFactory typeFactory, SequencetypeAtomization atomizer) {
        this.typeFactory = typeFactory;
        // this.anyItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
        this.atomizer = atomizer;
        boolean_ = typeFactory.boolean_();
        wrongTargetType = new IsCastableResult(boolean_, null, Castability.WRONG_TARGET_TYPE, null, null);
    }

    public static record IsCastableResult(
        XQuerySequenceType resultingType,
        XQuerySequenceType atomizedType,
        Castability castability,
        XQueryItemType[] wrongItemtypes,
        Castability[] problems
    ) {}

    public enum Castability {
        POSSIBLE,
        ALWAYS_POSSIBLE_MANY,
        ALWAYS_POSSIBLE_CASTING_TO_SUBTYPE,
        ALWAYS_POSSIBLE_CASTING_TO_SAME,
        ALWAYS_POSSIBLE_CASTING_TO_TARGET,
        IMPOSSIBLE,
        WRONG_TARGET_TYPE,
        TESTED_EXPRESSION_IS_EMPTY_SEQUENCE,
        TESTED_EXPRESSION_CAN_BE_EMPTY_SEQUENCE_WITHOUT_FLAG,
        TESTED_EXPRESSION_IS_ZERO_OR_MORE,
    }

    private final static ItemtypeIsValidCastTarget isValidCastTarget = new ItemtypeIsValidCastTarget();
    private final IsCastableResult wrongTargetType;
    private final SequencetypeAtomization atomizer;

    // private final IsCastableResult zeroOrMore;
    // private final IsCastableResult canBeEmptyWithoutFlag;
    // private final IsCastableResult castingToSubtype;
    private final XQuerySequenceType boolean_;

    public IsCastableResult isCastable(XQuerySequenceType targetType, XQuerySequenceType tested, boolean emptyAllowed) {
        if (!isValidCastTarget.test(targetType.getItemType())) {
            return wrongTargetType;
        }
        final var atomized = atomizer.atomize(tested);
        return switch (atomized.getOccurence()) {
            case ZERO -> new IsCastableResult(targetType, atomized, Castability.TESTED_EXPRESSION_IS_EMPTY_SEQUENCE, null, null);
            case ZERO_OR_ONE -> {
                if (!emptyAllowed) {
                    yield new IsCastableResult(targetType, atomized, Castability.TESTED_EXPRESSION_CAN_BE_EMPTY_SEQUENCE_WITHOUT_FLAG, null, null);
                }
                yield handleCastable(atomized, targetType, typeFactory.zeroOrOne(atomized.getItemType()));
            }
            case ONE -> handleCastable(atomized, targetType, typeFactory.one(atomized.getItemType()));
            default -> new IsCastableResult(targetType, atomized, Castability.TESTED_EXPRESSION_IS_ZERO_OR_MORE, null, null);
        };
    }

    IsCastableResult handleCastable(
            XQuerySequenceType atomized,
            XQuerySequenceType type,
            XQuerySequenceType result)
    {
        if (atomized.itemtypeIsSubtypeOf(atomized)) {
            return new IsCastableResult(result, atomized, Castability.ALWAYS_POSSIBLE_CASTING_TO_SUBTYPE, null, null);
        }
        final XQueryTypes targetItemType = type.getItemType().getType();
        final XQueryTypes testedItemType = atomized.getItemType().getType();
        return handleItemTypeCastable(targetItemType, testedItemType, result, atomized);
    }

    IsCastableResult handleItemTypeCastable(XQueryTypes targetItemType, XQueryItemType testedItem, XQuerySequenceType result, XQuerySequenceType atomized) {
        final IsCastableResult impossible = new IsCastableResult(result, atomized, Castability.IMPOSSIBLE, null, null);
        final IsCastableResult castingtosame = new IsCastableResult(result, atomized, Castability.ALWAYS_POSSIBLE_CASTING_TO_SAME, null, null);
        final IsCastableResult possible = new IsCastableResult(result, atomized, Castability.POSSIBLE, null, null);
        switch (targetItemType) {
        case STRING:
            return new IsCastableResult(result, atomized, Castability.ALWAYS_POSSIBLE_CASTING_TO_TARGET, null, null);
        case ENUM:
            switch (testedItem.getType()) {
            case ANY_ARRAY, ARRAY, MAP, ANY_MAP, RECORD, EXTENSIBLE_RECORD, ANY_FUNCTION, FUNCTION, ERROR:
                break;
            case BOOLEAN:
                // if (testedItem.getEnum)
            case NUMBER:

            case ANY_ITEM, ANY_NODE, ELEMENT, STRING:
                return possible;
            case CHOICE:
                return choiceCasting(targetItemType, result, atomized, impossible);
            case ENUM:

                break;
            default:
                break;

            }
        case BOOLEAN:
            switch (testedItem) {
            case BOOLEAN:
                return castingtosame;
            case NUMBER, STRING, ENUM, ANY_ITEM, ELEMENT, ANY_NODE:
                return possible;
            case CHOICE:
                return choiceCasting(targetItemType, result, atomized, impossible);
            case ANY_ARRAY, ANY_FUNCTION, FUNCTION, ANY_MAP, MAP, ARRAY, ERROR, EXTENSIBLE_RECORD, RECORD:
                return impossible;
            }
        case NUMBER:
            switch (testedItem) {
            case BOOLEAN:
                return castingtosame;
            case NUMBER:
            case STRING, ENUM:
            case ANY_ITEM:
                return possible;
            case CHOICE:
                break;
            case ANY_MAP, MAP, EXTENSIBLE_RECORD, RECORD:
            case ANY_FUNCTION, FUNCTION:
            case ANY_NODE, ELEMENT:
            case ERROR:
            case ANY_ARRAY, ARRAY: // Unreachable because of atomization
                return impossible;
            }
            return possible;
        case ANY_ARRAY:
            break;
        case ANY_FUNCTION:
            break;
        case ANY_ITEM:
            break;
        case ANY_MAP:
            break;
        case ANY_NODE:
            break;
        case ARRAY:
            break;
        case CHOICE:
            break;
        case ELEMENT:
            break;
        case ERROR:
            break;
        case EXTENSIBLE_RECORD:
            break;
        case FUNCTION:
            break;
        case MAP:
            break;
        case RECORD:
            break;
        };
        return ok;
    }

    private IsCastableResult choiceCasting(XQueryTypes targetItemType, XQuerySequenceType result, XQuerySequenceType atomized,
            final IsCastableResult impossible) {
        final Collection<XQueryItemType> itemTypes = atomized.getItemType().getItemTypes();
        final int itemtypecount = itemTypes.size();
        final Set<Castability> castabilityVariants = new HashSet<>(itemtypecount);
        boolean hasPossible = false;
        final Castability[] wrong = new Castability[itemtypecount];
        final XQueryItemType[] wrongTypes = new XQueryItemType[itemtypecount];
        int i = 0;
        for (final XQueryItemType itemtype : itemTypes) {
            IsCastableResult r = handleItemTypeCastable(targetItemType, itemtype.getType(), result, atomized);
            if (r.castability == Castability.IMPOSSIBLE)
                return impossible;
            wrong[i] = r.castability;
            wrongTypes[i] = itemtype;
            i++;
        }
        if (castabilityVariants.size() == 1)
            return new IsCastableResult(result, atomized, castabilityVariants.stream().findFirst().get(), null, null);
        for (final var castability : castabilityVariants) {
            if (castability == Castability.POSSIBLE) {
                return new IsCastableResult(result, atomized, Castability.POSSIBLE, null, null);
            }
        }
        return new IsCastableResult(result, atomized, Castability.ALWAYS_POSSIBLE_MANY, wrongTypes, wrong);
    }

}
