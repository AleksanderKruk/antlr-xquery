package com.github.akruk.antlrxquery.typesystem.typeoperations;



import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;
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
        wrongTargetType = new IsCastableResult(boolean_, Castability.WRONG_TARGET_TYPE);
        emptySequence = new IsCastableResult(boolean_, Castability.TESTED_EXPRESSION_IS_EMPTY_SEQUENCE);
        zeroOrMore = new IsCastableResult(boolean_, Castability.TESTED_EXPRESSION_IS_ZERO_OR_MORE);
        ok = new IsCastableResult(boolean_, Castability.OK);
        canBeEmptyWithoutFlag = new IsCastableResult(boolean_, Castability.TESTED_EXPRESSION_CAN_BE_EMPTY_SEQUENCE_WITHOUT_FLAG);
        unnecessary = new IsCastableResult(boolean_, Castability.UNNECESSARY);
    }

    public static record IsCastableResult(
        XQuerySequenceType resultingType,
        Castability castability
    ) {}

    public enum Castability {
        UNNECESSARY,
        OK,
        WRONG_TARGET_TYPE,
        TESTED_EXPRESSION_IS_EMPTY_SEQUENCE,
        TESTED_EXPRESSION_CAN_BE_EMPTY_SEQUENCE_WITHOUT_FLAG,
        TESTED_EXPRESSION_IS_ZERO_OR_MORE,
    }

    private final static ItemtypeIsValidCastTarget isValidCastTarget = new ItemtypeIsValidCastTarget();
    private final IsCastableResult wrongTargetType;
    private final IsCastableResult emptySequence;
    private final IsCastableResult ok;
    private final SequencetypeAtomization atomizer;

    private final IsCastableResult zeroOrMore;
    private final XQuerySequenceType boolean_;
    private final IsCastableResult canBeEmptyWithoutFlag;
    private final IsCastableResult unnecessary;

    public IsCastableResult isCastable(XQuerySequenceType targetType, XQuerySequenceType tested, boolean emptyAllowed) {
        if (!isValidCastTarget.test(targetType.getItemType())) {
            return wrongTargetType;
        }
        final var atomized = atomizer.atomize(tested);
        return switch (atomized.getOccurence()) {
            case ZERO -> emptySequence;
            case ZERO_OR_ONE -> {
                if (!emptyAllowed) {
                    yield canBeEmptyWithoutFlag;
                }
                yield handleCastable(atomized, tested, targetType, typeFactory.zeroOrOne(atomized.getItemType()));
            }
            case ONE -> handleCastable(atomized, tested, targetType, typeFactory.one(atomized.getItemType()));
            default -> zeroOrMore;
        };
    }

    IsCastableResult handleCastable(
            XQuerySequenceType atomized,
            XQuerySequenceType tested,
            XQuerySequenceType type,
            XQuerySequenceType result)
    {
        if (atomized.itemtypeIsSubtypeOf(tested)) {
            return unnecessary;
        }
        return ok;
    }


}
