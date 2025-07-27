package com.github.akruk.antlrxquery.typesystem.typeoperations;



import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


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
        wrongTargetType = new IsCastableResult(boolean_, null, Castability.WRONG_TARGET_TYPE, null, null, null);
    }

    public static record IsCastableResult(
        XQuerySequenceType resultingType,
        XQuerySequenceType atomizedType,
        Castability castability,
        XQueryItemType[] wrongItemtypes,
        XQuerySequenceType[] wrongSequenceTypes,
        Castability[] problems
    ) {}

    public enum Castability {
        POSSIBLE,
        ALWAYS_POSSIBLE_MANY_SEQUENCETYPES,
        ALWAYS_POSSIBLE_MANY_ITEMTYPES,
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
        if (!isValidCastTarget.test(targetType.itemType)) {
            return wrongTargetType;
        }
        final var atomized = atomizer.atomize(tested);
        return switch (atomized.occurence) {
            case ZERO -> new IsCastableResult(targetType, atomized, Castability.TESTED_EXPRESSION_IS_EMPTY_SEQUENCE, null, null, null);
            case ZERO_OR_ONE -> {
                if (!emptyAllowed) {
                    yield new IsCastableResult(targetType, atomized, Castability.TESTED_EXPRESSION_CAN_BE_EMPTY_SEQUENCE_WITHOUT_FLAG, null, null, null);
                }
                yield handleCastable(atomized, targetType, typeFactory.zeroOrOne(atomized.itemType));
            }
            case ONE -> handleCastable(atomized, targetType, typeFactory.one(atomized.itemType));
            default -> new IsCastableResult(targetType, atomized, Castability.TESTED_EXPRESSION_IS_ZERO_OR_MORE, null, null, null);
        };
    }

    IsCastableResult handleCastable(
            XQuerySequenceType atomized,
            XQuerySequenceType type,
            XQuerySequenceType result)
    {
        if (atomized.itemtypeIsSubtypeOf(atomized)) {
            return new IsCastableResult(result, atomized, Castability.ALWAYS_POSSIBLE_CASTING_TO_SUBTYPE, null, null, null);
        }
        return handleItemTypeCastable(type.itemType, atomized.itemType, result, atomized);
    }

    Pattern digit = Pattern.compile("(\\d+)?\\.\\d+");
    IsCastableResult handleItemTypeCastable(XQueryItemType targetItem, XQueryItemType testedItem, XQuerySequenceType result, XQuerySequenceType atomized) {
        final IsCastableResult impossible = new IsCastableResult(result, atomized, Castability.IMPOSSIBLE, null, null, null);
        final IsCastableResult castingtosame = new IsCastableResult(result, atomized, Castability.ALWAYS_POSSIBLE_CASTING_TO_SAME, null, null, null);
        final IsCastableResult possible = new IsCastableResult(result, atomized, Castability.POSSIBLE, null, null, null);
        final IsCastableResult always = new IsCastableResult(result, atomized, Castability.ALWAYS_POSSIBLE_CASTING_TO_TARGET, null, null, null);
        switch (targetItem.type) {
        case STRING:
            return always;
        case ENUM:
            return handleEnumAsTarget(targetItem, testedItem, impossible, possible, always, result, atomized);
        case BOOLEAN:
            return handleBooleanAsTarget(targetItem, testedItem, impossible, possible, castingtosame, result, atomized);
        case NUMBER:
            return handleNumberAsTarget(targetItem, testedItem, impossible, possible, castingtosame, result, atomized, always);
        case RECORD:
            return handleRecordAsTarget(targetItem, testedItem, impossible, possible, castingtosame, result, atomized, always);
        case EXTENSIBLE_RECORD:
            return handleRecordAsTarget(targetItem, testedItem, impossible, possible, castingtosame, result, atomized, always);
        case MAP:
            return handleMapAsTarget(targetItem, testedItem, impossible, possible, castingtosame, result, atomized, always);
        case FUNCTION: // TODO: maybe map -> function ; array -> function not only as coercion but as casting too?
        case ELEMENT: // TODO: possible map -> node and vice versa ??
        case ERROR: // casting to error is forbidden
        case CHOICE: // casting should be precise
        case ANY_ARRAY, ARRAY: // unreachable due to atomization
        case ANY_FUNCTION, ANY_ITEM, ANY_MAP, ANY_NODE: // impossible due to being abstract types
            return wrongTargetType;
        };
        return null; // unreachable
    }

    private IsCastableResult handleMapToRecordCastability(XQueryItemType targetItem, XQueryItemType testedItem, XQuerySequenceType result,
            XQuerySequenceType atomized, final IsCastableResult impossible, final IsCastableResult possible) {
        if (testedItem.mapKeyType.type != XQueryTypes.STRING) {
            if (testedItem.mapKeyType.type != XQueryTypes.ENUM) {
                return impossible;
            }
            Set<String> requiredFieldNames = targetItem.recordFields.entrySet().stream()
                .filter(entry->entry.getValue().isRequired())
                .map(entry->entry.getKey())
                .collect(Collectors.toSet());
            var allRequiredPresent = testedItem.enumMembers.containsAll(requiredFieldNames);
            if (!allRequiredPresent)
                return impossible;
            return recordFieldsMustMatchMapValue(targetItem, testedItem, result, atomized, possible);
        } else {
            return recordFieldsMustMatchMapValue(targetItem, testedItem, result, atomized, possible);
        }
    }

    private IsCastableResult recordFieldsMustMatchMapValue(XQueryItemType targetItem, XQueryItemType testedItem,
            XQuerySequenceType result, XQuerySequenceType atomized, final IsCastableResult possible) {
        final var valueType = testedItem.mapValueType;
        final int recordFieldCount = targetItem.recordFields.size();
        final Castability[] wrong = new Castability[recordFieldCount];
        final XQuerySequenceType[] wrongSequenceTypes = new XQuerySequenceType[recordFieldCount];
        int i = 0;
        boolean hasPossible = false;
        for (String member : targetItem.recordFields.keySet()) {
            final var recordField = targetItem.recordFields.get(member);
            final var expectedType = recordField.type();
            final var isCastable = isCastable(expectedType, valueType, true);
            switch(isCastable.castability) {
                case TESTED_EXPRESSION_CAN_BE_EMPTY_SEQUENCE_WITHOUT_FLAG:
                case TESTED_EXPRESSION_IS_EMPTY_SEQUENCE:
                case TESTED_EXPRESSION_IS_ZERO_OR_MORE:
                case WRONG_TARGET_TYPE:
                case IMPOSSIBLE:
                    return isCastable;
                case ALWAYS_POSSIBLE_CASTING_TO_SAME:
                case ALWAYS_POSSIBLE_CASTING_TO_SUBTYPE:
                case ALWAYS_POSSIBLE_CASTING_TO_TARGET:
                case ALWAYS_POSSIBLE_MANY_ITEMTYPES:
                case ALWAYS_POSSIBLE_MANY_SEQUENCETYPES:
                    break;
                case POSSIBLE:
                    hasPossible = true;
                    break;
            }
            wrongSequenceTypes[i] = expectedType;
            wrong[i] = isCastable.castability;
        }
        if (hasPossible)
            return possible;
        return new IsCastableResult(result, atomized, Castability.ALWAYS_POSSIBLE_MANY_SEQUENCETYPES, null, wrongSequenceTypes, wrong);
    }

    private IsCastableResult choiceCasting(
            final XQueryItemType targetItemType,
            final XQuerySequenceType result,
            final XQuerySequenceType atomized,
            final IsCastableResult impossible)
    {
        final Collection<XQueryItemType> itemTypes = atomized.itemType.itemTypes;
        final int itemtypecount = itemTypes.size();
        final Set<Castability> castabilityVariants = new HashSet<>(itemtypecount);
        final Castability[] wrong = new Castability[itemtypecount];
        final XQueryItemType[] wrongTypes = new XQueryItemType[itemtypecount];
        int i = 0;
        for (final XQueryItemType itemtype : itemTypes) {
            IsCastableResult r = handleItemTypeCastable(targetItemType, itemtype, result, atomized);
            if (r.castability == Castability.IMPOSSIBLE)
                return impossible;
            wrong[i] = r.castability;
            wrongTypes[i] = itemtype;
            i++;
        }
        if (castabilityVariants.size() == 1)
            return new IsCastableResult(result, atomized, castabilityVariants.stream().findFirst().get(), null, null, null);
        for (final var castability : castabilityVariants) {
            if (castability == Castability.POSSIBLE) {
                return new IsCastableResult(result, atomized, Castability.POSSIBLE, null, null, null);
            }
        }
        return new IsCastableResult(result, atomized, Castability.ALWAYS_POSSIBLE_MANY_ITEMTYPES, wrongTypes, null, wrong);
    }


    IsCastableResult handleEnumAsTarget(
        XQueryItemType targetItem,
        XQueryItemType testedItem,
        IsCastableResult impossible,
        IsCastableResult possible,
        IsCastableResult always, XQuerySequenceType result, XQuerySequenceType atomized)
    {
        return switch (testedItem.type) {
            case ANY_ARRAY, ARRAY, MAP, ANY_MAP, RECORD, EXTENSIBLE_RECORD, ANY_FUNCTION, FUNCTION, ERROR ->
                impossible;
            case BOOLEAN-> {
                final boolean containsTrue = targetItem.enumMembers.contains("true");
                final boolean containsFalse = targetItem.enumMembers.contains("false");
                if (containsTrue) {
                    if (containsFalse) {
                        if (targetItem.enumMembers.size() == 2)
                            yield always;
                        yield possible;
                    } else {
                        yield possible;
                    }
                } else {
                    if (containsFalse) {
                        yield possible;
                    }
                    yield impossible;
                }
            }
            case NUMBER ->{
                if (!targetItem.enumMembers.stream().anyMatch(member->digit.matcher(member).matches()))
                    yield impossible;
                yield possible;
            }
            case ANY_ITEM, ANY_NODE, ELEMENT, STRING -> possible;
            case CHOICE -> choiceCasting(targetItem, result, atomized, impossible);
            case ENUM -> {
                if (!testedItem.enumMembers.stream().anyMatch(member->targetItem.enumMembers.contains(member)))
                    yield impossible;
                yield possible;
            }
        };

    }

    IsCastableResult handleBooleanAsTarget(
        XQueryItemType targetItem,
        XQueryItemType testedItem,
        IsCastableResult impossible,
        IsCastableResult possible,
        IsCastableResult castingtosame,
        XQuerySequenceType result,
        XQuerySequenceType atomized)
    {
        return switch (testedItem.type) {
        case BOOLEAN -> castingtosame;
        case NUMBER, STRING, ENUM, ANY_ITEM, ELEMENT, ANY_NODE -> possible;
        case CHOICE -> choiceCasting(targetItem, result, atomized, impossible);
        case ANY_ARRAY, ANY_FUNCTION, FUNCTION,
            ANY_MAP, MAP, ARRAY, ERROR,
            EXTENSIBLE_RECORD, RECORD
            -> impossible;
        };
    }

    IsCastableResult handleNumberAsTarget(
        XQueryItemType targetItem,
        XQueryItemType testedItem,
        IsCastableResult impossible,
        IsCastableResult possible,
        IsCastableResult castingtosame,
        XQuerySequenceType result,
        XQuerySequenceType atomized,
        IsCastableResult always)
    {
        switch (testedItem.type) {
        case BOOLEAN:
            return always;
        case NUMBER:
            return castingtosame;
        case ENUM:
            if(testedItem.enumMembers.parallelStream().noneMatch(member->digit.matcher(member).matches())) {
                return impossible;
            }
            return possible;
        case ANY_ITEM, ANY_NODE, ELEMENT, STRING:
            return possible;
        case CHOICE:
            return choiceCasting(targetItem, result, atomized, impossible);
        case ANY_MAP, MAP, EXTENSIBLE_RECORD, RECORD, ANY_FUNCTION, FUNCTION, ERROR:
        case ANY_ARRAY, ARRAY: // Unreachable because of atomization
            return impossible;
        }
        return possible;
    }



    IsCastableResult handleRecordAsTarget(
        XQueryItemType targetItem,
        XQueryItemType testedItem,
        IsCastableResult impossible,
        IsCastableResult possible,
        IsCastableResult castingtosame,
        XQuerySequenceType result,
        XQuerySequenceType atomized,
        IsCastableResult always)
    {
        return switch(testedItem.type) {
            case ANY_MAP -> possible;
            case MAP -> handleMapToRecordCastability(targetItem, testedItem, result, atomized, impossible, possible);
            // TODO: refine
            case EXTENSIBLE_RECORD -> possible;
            // TODO: refine
            case RECORD -> possible;
            case ANY_ITEM -> possible;
            case STRING -> always;
            case CHOICE -> choiceCasting(targetItem, result, atomized, impossible);
            case ANY_ARRAY, ARRAY, ANY_NODE, ELEMENT, ANY_FUNCTION, FUNCTION, BOOLEAN, ENUM, NUMBER, ERROR
                -> impossible;
        };
    }

    IsCastableResult handleMapAsTarget(
        XQueryItemType targetItem,
        XQueryItemType testedItem,
        IsCastableResult impossible,
        IsCastableResult possible,
        IsCastableResult castingtosame,
        XQuerySequenceType result,
        XQuerySequenceType atomized,
        IsCastableResult always)
    {
        return switch(testedItem.type) {
            case ANY_MAP -> possible;
            case MAP -> {
                boolean hasPossible = false;
                var keyResult = handleItemTypeCastable(targetItem.mapKeyType, testedItem.mapKeyType, result, atomized);
                switch(keyResult.castability) {
                case ALWAYS_POSSIBLE_CASTING_TO_SAME:
                case ALWAYS_POSSIBLE_CASTING_TO_SUBTYPE:
                case ALWAYS_POSSIBLE_CASTING_TO_TARGET:
                case ALWAYS_POSSIBLE_MANY_ITEMTYPES:
                case ALWAYS_POSSIBLE_MANY_SEQUENCETYPES:
                    break;
                case POSSIBLE:
                    hasPossible = true;
                    break;
                case TESTED_EXPRESSION_CAN_BE_EMPTY_SEQUENCE_WITHOUT_FLAG:
                case TESTED_EXPRESSION_IS_EMPTY_SEQUENCE:
                case TESTED_EXPRESSION_IS_ZERO_OR_MORE:
                case WRONG_TARGET_TYPE:
                case IMPOSSIBLE:
                    yield impossible;
                }
                var valueResult = handleCastable(atomized, atomized, result);
                switch(valueResult.castability) {
                    case ALWAYS_POSSIBLE_CASTING_TO_SAME:
                    case ALWAYS_POSSIBLE_CASTING_TO_SUBTYPE:
                    case ALWAYS_POSSIBLE_CASTING_TO_TARGET:
                    case ALWAYS_POSSIBLE_MANY_ITEMTYPES:
                    case ALWAYS_POSSIBLE_MANY_SEQUENCETYPES:
                        break;
                    case POSSIBLE:
                        hasPossible = true;
                        break;
                    case TESTED_EXPRESSION_CAN_BE_EMPTY_SEQUENCE_WITHOUT_FLAG:
                    case TESTED_EXPRESSION_IS_EMPTY_SEQUENCE:
                    case TESTED_EXPRESSION_IS_ZERO_OR_MORE:
                    case WRONG_TARGET_TYPE:
                    case IMPOSSIBLE:
                        yield impossible;
                }
                if (hasPossible)
                    yield possible;
                yield always;
            }
            // TODO: refine
            case EXTENSIBLE_RECORD -> possible;
            // TODO: refine
            case RECORD -> possible;
            case ANY_ITEM -> possible;
            case STRING -> always;
            case CHOICE -> choiceCasting(targetItem, result, atomized, impossible);
            case ANY_ARRAY, ARRAY, ANY_NODE, ELEMENT, ANY_FUNCTION, FUNCTION, BOOLEAN, ENUM, NUMBER, ERROR
                -> impossible;
        };
    }



}
