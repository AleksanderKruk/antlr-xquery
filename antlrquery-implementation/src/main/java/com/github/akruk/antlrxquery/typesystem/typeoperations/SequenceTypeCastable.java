package com.github.akruk.antlrxquery.typesystem.typeoperations;


import com.github.akruk.antlrxquery.typesystem.RecordField;
import com.github.akruk.antlrxquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrxquery.typesystem.typeoperations.cardinality.Ranges;
import com.github.akruk.antlrxquery.typesystem.types.*;
import com.github.akruk.antlrxquery.typesystem.types.itemtypes.*;
import com.github.akruk.antlrxquery.typesystem.types.itemtypes.AtomicType;
import com.github.akruk.antlrxquery.typesystem.types.itemtypes.AtomicType.NumberType;
import com.github.akruk.visitorannotations.Visitor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@DefaultQualifier(NonNull.class)
@Visitor(name="SequenceTypeCastableVisitor", classes = {ConcreteItemType.class, ConcreteItemType.class})
public class SequenceTypeCastable
    implements SequenceTypeCastableVisitor<IsCastable>
{
    private final AntlrQueryTypeFactory typeFactory;
    private final IsCastable.Possible possible;
    private final IsCastable.Impossible impossible;
    private final IsCastable.AlwaysPossible.TestedTypeIsSubtypeOfTargetType testedTypeIsSubtypeOfTargetType;

    public SequenceTypeCastable(AntlrQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
        this.impossible = new IsCastable.Impossible();
        this.possible = new IsCastable.Possible();
        this.testedTypeIsSubtypeOfTargetType = new IsCastable.AlwaysPossible.TestedTypeIsSubtypeOfTargetType();
    }

    public IsCastable isCastable(AntlrQuerySequenceType targetType, AntlrQuerySequenceType tested, boolean emptyAllowed) {
        if (tested.cardinality().isZero()) {
            return new IsCastable.TestedExpressionIsEmptySequence();
        }
        if (tested.cardinality().isZeroOrOne()) {
            if (!emptyAllowed) {
                return new IsCastable.TestedExpressionCanBeEmptySequenceWithoutFlag();
            }
            return isCastable_(tested, targetType);
        }
        if (tested.cardinality().isOne()) {
            return isCastable_(tested, targetType);
        }
        return new IsCastable.TestedExpressionIsZeroOrMore();
    }

    IsCastable isCastable_(
            final AntlrQuerySequenceType tested,
            final AntlrQuerySequenceType target)
    {
        if (tested.equals(target)) {
            return new IsCastable.AlwaysPossible.CastingToSame();
        }
        if (Types.isSubtype(typeFactory, tested, target)) {
            return testedTypeIsSubtypeOfTargetType;
        }
        if (Types.isSubtype(typeFactory, target, tested)) {
            return possible;
        }
        return handleItemTypeCastable(tested.itemType(), target.itemType());
    }

    private IsCastable handleItemTypeCastable(AntlrQueryItemType testedItem, AntlrQueryItemType targetItem) {
        return switch (testedItem) {
            case AnyItemType _ -> possible;
            case ChoiceItemType choiceItemType -> isCastableFromChoice(choiceItemType, targetItem);
            case ConcreteItemType concreteTested -> switch (targetItem) {
                case ChoiceItemType choiceTarget -> isCastableToChoice(concreteTested, choiceTarget);
                case ConcreteItemType concreteTarget -> visit(concreteTested, concreteTarget);
                case AnyItemType _ -> new IsCastable.AlwaysPossible.TestedTypeIsSubtypeOfTargetType();
                case NeverType _, NothingType _ -> impossible;
            };
            case NothingType _, NeverType _ -> testedTypeIsSubtypeOfTargetType;
        };
    }

    private IsCastable isCastableToChoice(ConcreteItemType testedType, ChoiceItemType target) {
        boolean atLeastOneAlwaysPossible = false;
        boolean atLeastOnePossible = false;

        for (AntlrQueryItemType targetMember : target.itemTypes()) {
            IsCastable castable = handleItemTypeCastable(testedType, targetMember);
            switch (castable) {
                case IsCastable.AlwaysPossible _ -> atLeastOneAlwaysPossible = true;
                case IsCastable.Possible _ -> atLeastOnePossible = true;
                case IsCastable.Impossible _ -> {}
                case IsCastable.TestedExpressionCanBeEmptySequenceWithoutFlag _,
                     IsCastable.TestedExpressionIsEmptySequence _,
                     IsCastable.TestedExpressionIsZeroOrMore _,
                     IsCastable.WrongTargetType _ -> throw new IllegalStateException();
            }
        }

        if (atLeastOneAlwaysPossible) {
            return new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget();
        } else if (atLeastOnePossible) {
            return new IsCastable.Possible();
        } else {
            return impossible;
        }
    }

    private IsCastable isCastableFromChoice(ChoiceItemType testedType, AntlrQueryItemType target) {
        boolean atLeastOneAlwaysPossible = false;
        boolean atLeastOnePossible = false;

        for (AntlrQueryItemType testedMember : testedType.itemTypes()) {
            IsCastable castable = handleItemTypeCastable(testedMember, target);
            switch (castable) {
                case IsCastable.AlwaysPossible _ -> atLeastOneAlwaysPossible = true;
                case IsCastable.Possible _ -> atLeastOnePossible = true;
                case IsCastable.Impossible _ -> {}
                case IsCastable.TestedExpressionCanBeEmptySequenceWithoutFlag _,
                     IsCastable.TestedExpressionIsEmptySequence _,
                     IsCastable.TestedExpressionIsZeroOrMore _,
                     IsCastable.WrongTargetType _ -> throw new IllegalStateException();
            }
        }

        if (atLeastOneAlwaysPossible) {
            return new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget();
        } else if (atLeastOnePossible) {
            return new IsCastable.Possible();
        } else {
            return impossible;
        }
    }

    private IsCastable keyAndValueCastable(
            AntlrQueryItemType testedKeyType,
            AntlrQuerySequenceType testedValueType,
            AntlrQueryItemType keyCastTarget,
            AntlrQuerySequenceType valueCastTarget)
    {
        final IsCastable keyCasting = handleItemTypeCastable(testedKeyType, keyCastTarget);
        boolean keyAlwaysPossible = false;
        switch (keyCasting) {
            case IsCastable.Possible _ -> {}
            case IsCastable.AlwaysPossible _ -> keyAlwaysPossible = true;
            case IsCastable.Impossible _ -> {
                return impossible;
            }
            case IsCastable.TestedExpressionCanBeEmptySequenceWithoutFlag _,
                 IsCastable.TestedExpressionIsEmptySequence _, IsCastable.TestedExpressionIsZeroOrMore _,
                 IsCastable.WrongTargetType _ -> throw new IllegalStateException();
        }
        final IsCastable valueCasting = isCastable_(testedValueType, valueCastTarget);
        boolean valueAlwaysPossible = false;
        switch (valueCasting) {
            case IsCastable.Possible _ -> { }
            case IsCastable.AlwaysPossible _ -> valueAlwaysPossible = true;
            case IsCastable.Impossible _ -> {
                return impossible;
            }
            case IsCastable.TestedExpressionCanBeEmptySequenceWithoutFlag _,
                 IsCastable.TestedExpressionIsEmptySequence _, IsCastable.TestedExpressionIsZeroOrMore _,
                 IsCastable.WrongTargetType _ -> throw new IllegalStateException();
        }
        return (keyAlwaysPossible && valueAlwaysPossible)
                ? new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget()
                : new IsCastable.Possible();
    }


    @Override
    public IsCastable visit(TreeRuleType.RuleType source, TreeRuleType.RuleType target) {
        return isCastableUsingGrammarAndNames(source, target);
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, TreeRuleType.AnyRule anyRule) {
        return testedTypeIsSubtypeOfTargetType;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return isCastableUsingGrammar(ruleType, anyRuleFromGrammar);
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, TreeTokenType.AnyToken anyToken) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, TreeTokenType.TokenType tokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, TreeNodeType.AnyNode anyNode) {
        return testedTypeIsSubtypeOfTargetType;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return isCastableUsingGrammar(ruleType, anyNodeFromGrammar);
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, TreeNodeType.NodeType nodeType) {
        return isCastableUsingGrammarAndNames(ruleType, nodeType);
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, NumberType numberType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, StringType.StringEnum stringEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, StringType.StringNonEnum stringNonEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, BooleanType.Boolean boolean_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, BooleanType.False false_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType r, GrammarEntityType.GrammarType g) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType r, MapLikeType.RecordType rec) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.RuleType ruleType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }


    private <T1 extends GrammarConstrained, T2 extends GrammarConstrained>
    IsCastable isCastableUsingGrammar(T1 t1, T2 t2) {
        if (t1.grammar().equals(t2.grammar())) {
            return testedTypeIsSubtypeOfTargetType;
        }
        return impossible;
    }

    private <T1 extends NamesConstrained & GrammarConstrained, T2 extends NamesConstrained & GrammarConstrained>
    IsCastable isCastableUsingGrammarAndNames(T1 t1, T2 t2) {
        if (!t1.grammar().equals(t2.grammar())) {
            return impossible;
        }
        if (java.util.Collections.disjoint(t1.elementNames(), t2.elementNames())) {
            return impossible;
        }
        if (t2.elementNames().containsAll(t1.elementNames())) {
            return testedTypeIsSubtypeOfTargetType;
        }
        return possible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, TreeRuleType.RuleType ruleType) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, TreeRuleType.AnyRule anyRule2) {
        return testedTypeIsSubtypeOfTargetType;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, TreeTokenType.AnyToken anyToken) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, TreeTokenType.TokenType tokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, TreeNodeType.AnyNode anyNode) {
        return testedTypeIsSubtypeOfTargetType;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, TreeNodeType.NodeType nodeType) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, NumberType numberType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, StringType.StringEnum stringEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, StringType.StringNonEnum stringNonEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, BooleanType.Boolean boolean_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, BooleanType.False false_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRule anyRule, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeRuleType.RuleType ruleType) {
        if (anyRuleFromGrammar.grammar().equals(ruleType.grammar())) {
            return possible;
        }
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeRuleType.AnyRule anyRule) {
        return testedTypeIsSubtypeOfTargetType;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar2) {
        return isCastableUsingGrammar(anyRuleFromGrammar, anyRuleFromGrammar2);
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeTokenType.AnyToken anyToken) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeTokenType.TokenType tokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeNodeType.AnyNode anyNode) {
        return testedTypeIsSubtypeOfTargetType;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return isCastableUsingGrammar(anyRuleFromGrammar, anyNodeFromGrammar);
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeNodeType.NodeType nodeType) {
        if (anyRuleFromGrammar.grammar().equals(nodeType.grammar())) {
            return possible;
        }
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, NumberType numberType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, StringType.StringEnum stringEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, StringType.StringNonEnum stringNonEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, BooleanType.Boolean boolean_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, BooleanType.False false_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }
    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, TreeRuleType.RuleType ruleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, TreeRuleType.AnyRule anyRule) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, TreeTokenType.AnyToken anyToken2) {
        return testedTypeIsSubtypeOfTargetType;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, TreeTokenType.TokenType tokenType) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, TreeNodeType.AnyNode anyNode) {
        return testedTypeIsSubtypeOfTargetType;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, TreeNodeType.NodeType nodeType) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, NumberType numberType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, StringType.StringEnum stringEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, StringType.StringNonEnum stringNonEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, BooleanType.Boolean boolean_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, BooleanType.False false_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyToken anyToken, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }


    @Override
    public IsCastable visit(MapLikeType.MapType mapType, MapLikeType.MapType mapType2) {
        return keyAndValueCastable(mapType.keyType(), mapType.valueType(), mapType2.keyType(), mapType2.valueType());
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, MapLikeType.RecordType recordType) {
        final Set<String> fieldNames = new HashSet<>(recordType.fields().keySet());
        final AntlrQueryItemType fieldEnum = typeFactory.itemEnum(fieldNames);
        final AntlrQuerySequenceType mergedValueType = Types.getMapValue(typeFactory, recordType);
        return keyAndValueCastable(mapType.keyType(), mapType.valueType(), fieldEnum, mergedValueType);
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, TreeNodeType.NodeType elementType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, TreeRuleType.RuleType ruleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, TreeRuleType.AnyRule anyRule) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, TreeTokenType.AnyToken anyToken) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, TreeTokenType.TokenType tokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, TreeNodeType.AnyNode anyNode) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, ArrayLikeType.TupleType tupleType) {
        var tupleValueType = Types.union(typeFactory, tupleType.members());
        return keyAndValueCastable(
                mapType.keyType(), mapType.valueType(), typeFactory.itemNumber(), tupleValueType);
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, ArrayLikeType.ArrayType arrayType) {
        return keyAndValueCastable(
                mapType.keyType(), mapType.valueType(), typeFactory.itemNumber(), arrayType.memberType());
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, TreeRuleType.RuleType ruleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, TreeRuleType.AnyRule anyRule) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, TreeTokenType.AnyToken anyToken) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, TreeTokenType.TokenType tokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, TreeNodeType.AnyNode anyNode) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, TreeNodeType.NodeType nodeType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, NumberType numberType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, StringType.StringEnum stringEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, StringType.StringNonEnum stringNonEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, BooleanType.Boolean boolean_) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, BooleanType.False false_) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType recordType, MapLikeType.MapType mapType) {
        final Set<String> fieldNames = new HashSet<>(recordType.fields().keySet());
        final AntlrQueryItemType fieldEnum = typeFactory.itemEnum(fieldNames);
        final AntlrQuerySequenceType mergedValueType = Types.getMapValue(typeFactory, recordType);
        return keyAndValueCastable(fieldEnum, mergedValueType, mapType.keyType(), mapType.valueType());
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, StringType.StringEnum stringEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, StringType.StringNonEnum stringNonEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, NumberType numberType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, BooleanType.False false_) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, BooleanType.Boolean boolean_) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.MapType mapType, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, TreeNodeType.NodeType elementType) {
        return isCastableUsingGrammarAndNames(tokenType, elementType);
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, TreeRuleType.RuleType ruleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, TreeRuleType.AnyRule anyRule) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, TreeTokenType.AnyToken anyToken) {
        return testedTypeIsSubtypeOfTargetType;
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, TreeTokenType.TokenType tokenType2) {
        return isCastableUsingGrammarAndNames(tokenType, tokenType2);
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return isCastableUsingGrammar(tokenType, anyTokenFromGrammar);
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, TreeNodeType.AnyNode anyNode) {
        return testedTypeIsSubtypeOfTargetType;
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return isCastableUsingGrammar(tokenType, anyNodeFromGrammar);
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, TreeRuleType.RuleType ruleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, TreeRuleType.AnyRule anyRule) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, TreeTokenType.AnyToken anyToken) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, TreeTokenType.TokenType tokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, TreeNodeType.AnyNode anyNode) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, TreeNodeType.NodeType nodeType) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, AtomicType.RegexType regexType2) {
        return testedTypeIsSubtypeOfTargetType;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, NumberType numberType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType elementType, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType nodeType, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType elementType, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType elementType, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType elementType, BooleanType.Boolean boolean_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType elementType, BooleanType.False false_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType elementType, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType elementType, NumberType numberType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType elementType, StringType.StringNonEnum stringNonEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType elementType, StringType.StringEnum stringEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType elementType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType elementType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType nodeType, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType nodeType, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType nodeType, TreeRuleType.RuleType ruleType) {
        return isCastableUsingGrammarAndNames(nodeType, ruleType);
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType nodeType, TreeRuleType.AnyRule anyRule) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType nodeType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return isCastableUsingGrammar(nodeType, anyRuleFromGrammar);
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType nodeType, TreeTokenType.AnyToken anyToken) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType nodeType, TreeTokenType.TokenType tokenType) {
        return isCastableUsingGrammarAndNames(nodeType, tokenType);
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType nodeType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return isCastableUsingGrammar(nodeType, anyTokenFromGrammar);
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType nodeType, TreeNodeType.AnyNode anyNode) {
        return testedTypeIsSubtypeOfTargetType;
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType nodeType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return isCastableUsingGrammar(nodeType, anyNodeFromGrammar);
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType nodeType, TreeNodeType.NodeType nodeType2) {
        return isCastableUsingGrammarAndNames(nodeType, nodeType2);
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType elementType, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType elementType, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.NodeType nodeType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeRuleType.RuleType ruleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeRuleType.AnyRule anyRule) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeTokenType.AnyToken anyToken) {
        return testedTypeIsSubtypeOfTargetType;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeTokenType.TokenType tokenType) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar2) {
        return isCastableUsingGrammar(anyTokenFromGrammar, anyTokenFromGrammar2);
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeNodeType.AnyNode anyNode) {
        return testedTypeIsSubtypeOfTargetType;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return isCastableUsingGrammar(anyTokenFromGrammar, anyNodeFromGrammar);
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeNodeType.NodeType nodeType) {
        return isCastableUsingGrammar(anyTokenFromGrammar, nodeType);
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, NumberType numberType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, StringType.StringEnum stringEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, StringType.StringNonEnum stringNonEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, BooleanType.Boolean boolean_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, BooleanType.False false_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, TreeRuleType.RuleType ruleType) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, TreeRuleType.AnyRule anyRule) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, TreeTokenType.AnyToken anyToken) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, TreeTokenType.TokenType tokenType) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, TreeNodeType.AnyNode anyNode2) {
        return testedTypeIsSubtypeOfTargetType;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, TreeNodeType.NodeType nodeType) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, NumberType numberType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, StringType.StringEnum stringEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, StringType.StringNonEnum stringNonEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, BooleanType.Boolean boolean_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, BooleanType.False false_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNode anyNode, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeRuleType.RuleType ruleType) {
        return isCastableUsingGrammar(anyNodeFromGrammar, ruleType);
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeRuleType.AnyRule anyRule) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return isCastableUsingGrammar(anyNodeFromGrammar, anyRuleFromGrammar);
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeTokenType.AnyToken anyToken) {
        return possible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeTokenType.TokenType tokenType) {
        return isCastableUsingGrammar(anyNodeFromGrammar, tokenType);
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return isCastableUsingGrammar(anyNodeFromGrammar, anyTokenFromGrammar);
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeNodeType.AnyNode anyNode) {
        return testedTypeIsSubtypeOfTargetType;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar2) {
        return isCastableUsingGrammar(anyNodeFromGrammar, anyNodeFromGrammar2);
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeNodeType.NodeType nodeType) {
        return isCastableUsingGrammar(anyNodeFromGrammar, nodeType);
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, NumberType numberType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, StringType.StringEnum stringEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, StringType.StringNonEnum stringNonEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, BooleanType.Boolean boolean_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, BooleanType.False false_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType source, MapLikeType.RecordType target) {
        for (String sourceKey : source.fields().keySet()) {
            if (!target.fields().containsKey(sourceKey)) {
                return impossible;
            }
        }

        for (Map.Entry<String, RecordField> sourceEntry : source.fields().entrySet()) {
            RecordField targetField = target.fields().get(sourceEntry.getKey());

            AntlrQuerySequenceType sourceType = sourceEntry.getValue().resolveFieldType(typeFactory);
            AntlrQuerySequenceType targetSeqType = targetField.resolveFieldType(typeFactory);

            IsCastable fieldCastable = isCastable_(sourceType, targetSeqType);
            if (fieldCastable instanceof IsCastable.Impossible) {
                return impossible;
            }
        }

        return new IsCastable.Possible();
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType source, MapLikeType.ExtensibleRecordType target) {
        boolean alwaysPossible = true;

        for (Map.Entry<String, RecordField> targetEntry : target.fields().entrySet()) {
            String fieldName = targetEntry.getKey();
            RecordField targetField = targetEntry.getValue();
            AntlrQuerySequenceType targetSeqType = targetField.resolveFieldType(typeFactory);

            if (source.fields().containsKey(fieldName)) {
                AntlrQuerySequenceType sourceType = source.fields().get(fieldName).resolveFieldType(typeFactory);
                IsCastable fieldCastable = isCastable_(sourceType, targetSeqType);

                if (fieldCastable instanceof IsCastable.Impossible) {
                    return impossible;
                } else if (fieldCastable instanceof IsCastable.Possible) {
                    alwaysPossible = false;
                }
            } else {
                alwaysPossible = false;

                IsCastable addCastable = isCastable_(source.additionalFieldType(), targetSeqType);
                if (targetField.isRequired() && addCastable instanceof IsCastable.Impossible) {
                    return impossible;
                }
            }
        }

        for (Map.Entry<String, RecordField> sourceEntry : source.fields().entrySet()) {
            String fieldName = sourceEntry.getKey();
            if (!target.fields().containsKey(fieldName)) {
                AntlrQuerySequenceType sourceType = sourceEntry.getValue().resolveFieldType(typeFactory);
                IsCastable fieldCastable = isCastable_(sourceType, target.additionalFieldType());

                if (fieldCastable instanceof IsCastable.Impossible) {
                    return impossible;
                } else if (fieldCastable instanceof IsCastable.Possible) {
                    alwaysPossible = false;
                }
            }
        }

        IsCastable additionalCastable = isCastable_(source.additionalFieldType(), target.additionalFieldType());
        if (additionalCastable instanceof IsCastable.Impossible) {
            alwaysPossible = false;
        } else if (additionalCastable instanceof IsCastable.Possible) {
            alwaysPossible = false;
        }

        return alwaysPossible ? new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget() : new IsCastable.Possible();
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType source, MapLikeType.MapType target) {
        boolean alwaysPossible = true;

        IsCastable keyCastable = handleItemTypeCastable(typeFactory.itemString(), target.keyType());
        if (keyCastable instanceof IsCastable.Impossible) {
            return impossible;
        } else if (keyCastable instanceof IsCastable.Possible) {
            alwaysPossible = false;
        }

        for (RecordField field : source.fields().values()) {
            AntlrQuerySequenceType fieldType = field.resolveFieldType(typeFactory);
            IsCastable valCastable = isCastable_(fieldType, target.valueType());
            if (valCastable instanceof IsCastable.Impossible) {
                return impossible;
            } else if (valCastable instanceof IsCastable.Possible) {
                alwaysPossible = false;
            }
        }

        IsCastable additionalCastable = isCastable_(source.additionalFieldType(), target.valueType());
        if (additionalCastable instanceof IsCastable.Impossible) {
            alwaysPossible = false;
        } else if (additionalCastable instanceof IsCastable.Possible) {
            alwaysPossible = false;
        }

        return alwaysPossible ? new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget() : new IsCastable.Possible();
    }


    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, TreeRuleType.RuleType ruleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, TreeRuleType.AnyRule anyRule) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, TreeTokenType.AnyToken anyToken) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, TreeTokenType.TokenType tokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, TreeNodeType.AnyNode anyNode) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, TreeNodeType.NodeType nodeType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, NumberType numberType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, StringType.StringEnum stringEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, StringType.StringNonEnum stringNonEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, BooleanType.Boolean boolean_) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, BooleanType.False false_) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(MapLikeType.ExtensibleRecordType extensibleRecordType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }


    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, StringType.StringEnum stringEnum) {
        return new IsCastable.Possible();
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, StringType.StringNonEnum stringNonEnum) {
        return new IsCastable.Possible();
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, NumberType numberType) {
        return new IsCastable.Possible();
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, BooleanType.False false_) {
        return new IsCastable.Possible();
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, BooleanType.Boolean boolean_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(TreeTokenType.TokenType tokenType, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, MapLikeType.MapType mapType) {
        throw new IllegalStateException("array type should never appear here due to atomization");
    }

    @Override
    public IsCastable visit(MapLikeType.RecordType source, MapLikeType.RecordType target) {
        if (!target.fields().keySet().containsAll(source.fields().keySet())) {
            return impossible;
        }

        for (Map.Entry<String, RecordField> targetEntry : target.fields().entrySet()) {
            if (targetEntry.getValue().isRequired() && !source.fields().containsKey(targetEntry.getKey())) {
                return impossible;
            }
        }

        boolean alwaysPossible = true;
        for (Map.Entry<String, RecordField> sourceEntry : source.fields().entrySet()) {
            String fieldName = sourceEntry.getKey();
            RecordField targetField = target.fields().get(fieldName);

            AntlrQuerySequenceType sourceType = sourceEntry.getValue().resolveFieldType(typeFactory);
            AntlrQuerySequenceType targetSeqType = targetField.resolveFieldType(typeFactory);

            IsCastable fieldCastable = isCastable_(sourceType, targetSeqType);

            switch (fieldCastable) {
                case IsCastable.Possible _ -> alwaysPossible = false;
                case IsCastable.AlwaysPossible _ -> {} // Ok
                case IsCastable.Impossible _ -> { return impossible; }
                case IsCastable.TestedExpressionCanBeEmptySequenceWithoutFlag _,
                     IsCastable.TestedExpressionIsEmptySequence _,
                     IsCastable.TestedExpressionIsZeroOrMore _,
                     IsCastable.WrongTargetType _ -> throw new IllegalStateException();
            }
        }

        return alwaysPossible ? new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget() : new IsCastable.Possible();
    }


    @Override
    public IsCastable visit(MapLikeType.RecordType source, MapLikeType.ExtensibleRecordType target) {
        for (Map.Entry<String, RecordField> targetEntry : target.fields().entrySet()) {
            if (targetEntry.getValue().isRequired() && !source.fields().containsKey(targetEntry.getKey())) {
                return impossible;
            }
        }

        boolean alwaysPossible = true;
        for (Map.Entry<String, RecordField> sourceEntry : source.fields().entrySet()) {
            String fieldName = sourceEntry.getKey();
            @Nullable RecordField targetField = target.fields().get(fieldName);

            AntlrQuerySequenceType sourceType = sourceEntry.getValue().resolveFieldType(typeFactory);
            if (targetField != null) {
                AntlrQuerySequenceType targetSeqType = targetField.resolveFieldType(typeFactory);

                IsCastable fieldCastable = isCastable_(sourceType, targetSeqType);

                switch (fieldCastable) {
                    case IsCastable.Possible _ -> alwaysPossible = false;
                    case IsCastable.AlwaysPossible _ -> {}
                    case IsCastable.Impossible _ -> { return impossible; }
                    case IsCastable.TestedExpressionCanBeEmptySequenceWithoutFlag _,
                         IsCastable.TestedExpressionIsEmptySequence _,
                         IsCastable.TestedExpressionIsZeroOrMore _,
                         IsCastable.WrongTargetType _ -> throw new IllegalStateException();
                }
            } else {
                switch(isCastable_(sourceType, target.additionalFieldType())) {
                    case IsCastable.AlwaysPossible _ -> alwaysPossible = false;
                    case IsCastable.Impossible _ -> { return impossible; }
                    case IsCastable.Possible _, IsCastable.TestedExpressionCanBeEmptySequenceWithoutFlag _,
                         IsCastable.TestedExpressionIsEmptySequence _, IsCastable.TestedExpressionIsZeroOrMore _,
                         IsCastable.WrongTargetType _ -> { }
                }
            }
        }

        return alwaysPossible ? new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget() : new IsCastable.Possible();
    }


    @Override
    public IsCastable visit(GrammarEntityType.GrammarType source, GrammarEntityType.GrammarType target) {
        return new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget();
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, TreeRuleType.RuleType ruleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, TreeRuleType.AnyRule anyRule) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, TreeTokenType.AnyToken anyToken) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, TreeTokenType.TokenType tokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, TreeNodeType.AnyNode anyNode) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, TreeNodeType.NodeType nodeType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, NumberType numberType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, StringType.StringEnum stringEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, StringType.StringNonEnum stringNonEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, BooleanType.Boolean boolean_) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, BooleanType.False false_) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType source, GrammarEntityType.GrammarRuleType target) {
        return new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget();
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, TreeRuleType.RuleType ruleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, TreeRuleType.AnyRule anyRule) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, TreeTokenType.AnyToken anyToken) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, TreeTokenType.TokenType tokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, TreeNodeType.AnyNode anyNode) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, TreeNodeType.NodeType nodeType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, NumberType numberType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, StringType.StringEnum stringEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, StringType.StringNonEnum stringNonEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, BooleanType.Boolean boolean_) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, BooleanType.False false_) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarRuleType grammarRuleType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarType grammarType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType source, GrammarEntityType.GrammarTokenType target) {
        return new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget();
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, TreeRuleType.RuleType ruleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, TreeRuleType.AnyRule anyRule) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, TreeTokenType.AnyToken anyToken) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, TreeTokenType.TokenType tokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, TreeNodeType.AnyNode anyNode) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, TreeNodeType.NodeType nodeType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, NumberType numberType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, StringType.StringEnum stringEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, StringType.StringNonEnum stringNonEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, BooleanType.Boolean boolean_) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, BooleanType.False false_) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(GrammarEntityType.GrammarTokenType grammarTokenType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction source, FunctionType.ConstrainedFunction target) {
        if (source.argumentTypes().size() != target.argumentTypes().size()) {
            return impossible;
        }

        boolean alwaysPossible = true;

        IsCastable returnCastable = isCastable_(source.returnType(), target.returnType());
        if (returnCastable instanceof IsCastable.Impossible) {
            return impossible;
        } else if (returnCastable instanceof IsCastable.Possible) {
            alwaysPossible = false;
        }

        for (int i = 0; i < source.argumentTypes().size(); i++) {
            // Function parameters are contravariant
            IsCastable paramCastable = isCastable_(target.argumentTypes().get(i), source.argumentTypes().get(i));
            if (paramCastable instanceof IsCastable.Impossible) {
                return impossible;
            } else if (paramCastable instanceof IsCastable.Possible) {
                alwaysPossible = false;
            }
        }

        return alwaysPossible ? testedTypeIsSubtypeOfTargetType : possible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, FunctionType.AnyFunction anyFunction) {
        return testedTypeIsSubtypeOfTargetType;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, TreeRuleType.RuleType ruleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, TreeRuleType.AnyRule anyRule) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, TreeTokenType.AnyToken anyToken) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, TreeTokenType.TokenType tokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, TreeNodeType.AnyNode anyNode) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, TreeNodeType.NodeType nodeType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, NumberType numberType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, StringType.StringEnum stringEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, StringType.StringNonEnum stringNonEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, BooleanType.Boolean boolean_) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, BooleanType.False false_) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.ConstrainedFunction constrainedFunction, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, FunctionType.ConstrainedFunction constrainedFunction) {
        return possible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, FunctionType.AnyFunction anyFunction2) {
        return testedTypeIsSubtypeOfTargetType;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, TreeRuleType.RuleType ruleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, TreeRuleType.AnyRule anyRule) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, TreeTokenType.AnyToken anyToken) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, TreeTokenType.TokenType tokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, TreeNodeType.AnyNode anyNode) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, TreeNodeType.NodeType nodeType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, NumberType numberType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, StringType.StringEnum stringEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, StringType.StringNonEnum stringNonEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, BooleanType.Boolean boolean_) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, BooleanType.False false_) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(FunctionType.AnyFunction anyFunction, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, MapLikeType.RecordType recordType) {
        throw new IllegalStateException("array type should never appear here due to atomization");
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, GrammarEntityType.GrammarType grammarType) {
        throw new IllegalStateException("array type should never appear here due to atomization");
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        throw new IllegalStateException("array type should never appear here due to atomization");
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        throw new IllegalStateException("array type should never appear here due to atomization");
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, TreeNodeType.NodeType elementType) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, TreeRuleType.RuleType ruleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, TreeRuleType.AnyRule anyRule) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, TreeTokenType.AnyToken anyToken) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, TreeTokenType.TokenType tokenType) {
        throw new IllegalStateException("atomization prevents array");
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, TreeNodeType.AnyNode anyNode) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return impossible;
    }


    @Override
    public IsCastable visit(ArrayLikeType.TupleType source, ArrayLikeType.TupleType target) {
        if (source.members().length != target.members().length) {
            return impossible;
        }

        boolean alwaysPossible = true;
        for (int i = 0; i < source.members().length; i++) {
            IsCastable castable = isCastable_(source.members()[i], target.members()[i]);
            if (castable instanceof IsCastable.Impossible) {
                return impossible;
            }
            if (!(castable instanceof IsCastable.AlwaysPossible)) {
                alwaysPossible = false;
            }
        }
        return alwaysPossible ? new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget() : possible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType source, ArrayLikeType.TupleType target) {
        for (AntlrQuerySequenceType targetElementType : target.members()) {
            IsCastable elementCastable = isCastable_(source.memberType(), targetElementType);
            if (elementCastable instanceof IsCastable.Impossible) {
                return impossible;
            }
        }
        // Array length is dynamic, so casting to a fixed-size tuple requires runtime verification
        return possible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType source, ArrayLikeType.ArrayType target) {
        IsCastable itemCastable = isCastable_(source.memberType(), target.memberType());

        if (itemCastable instanceof IsCastable.Impossible) {
            return impossible;
        } else if (itemCastable instanceof IsCastable.Possible) {
            return possible;
        }

        return testedTypeIsSubtypeOfTargetType;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType source, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType source, ArrayLikeType.ArrayType target) {
        boolean alwaysPossible = true;
        for (AntlrQuerySequenceType memberType : source.members()) {
            IsCastable castable = isCastable_(memberType, target.memberType());
            if (castable instanceof IsCastable.Impossible) {
                return impossible;
            }
            if (!(castable instanceof IsCastable.AlwaysPossible)) {
                alwaysPossible = false;
            }
        }
        return alwaysPossible ? new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget() : new IsCastable.Possible();
    }
    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, TreeRuleType.RuleType ruleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, TreeRuleType.AnyRule anyRule) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, TreeTokenType.AnyToken anyToken) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, TreeTokenType.TokenType tokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, TreeNodeType.AnyNode anyNode) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, TreeNodeType.NodeType nodeType) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, NumberType numberType) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, StringType.StringEnum stringEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, StringType.StringNonEnum stringNonEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, BooleanType.Boolean boolean_) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.TupleType tupleType, BooleanType.False false_) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, StringType.StringEnum stringEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, StringType.StringNonEnum stringNonEnum) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, NumberType numberType) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, BooleanType.False false_) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, BooleanType.Boolean boolean_) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(ArrayLikeType.ArrayType arrayType, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, TreeNodeType.NodeType elementType) {
        return possible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, StringType.StringEnum stringEnum2) {
        if (stringEnum.members().equals(stringEnum2.members())) {
            return new IsCastable.AlwaysPossible.CastingToSame();
        }

        if (stringEnum2.members().containsAll(stringEnum.members())) {
            return new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget();
        }

        return possible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, StringType.StringNonEnum stringNonEnum) {
        return new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget();
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, NumberType numberType) {
        return possible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, BooleanType.False false_) {
        return stringEnum.members().contains("false")
                ? possible
                : impossible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, BooleanType.Boolean boolean_) {
        boolean f = stringEnum.members().contains("false");
        boolean t = stringEnum.members().contains("true");
        return (f || t)
                ? possible
                : impossible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, BooleanType.True true_) {
        return stringEnum.members().contains("true")
                ? possible
                : impossible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, AtomicType.RegexType regexType) {
        return possible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, TreeRuleType.RuleType ruleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, TreeRuleType.AnyRule anyRule) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, TreeTokenType.AnyToken anyToken) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, TreeTokenType.TokenType tokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, TreeNodeType.AnyNode anyNode) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, TreeNodeType.NodeType elementType) {
        return possible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, StringType.StringEnum stringEnum) {
        return possible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, StringType.StringNonEnum stringNonEnum2) {
        if (stringNonEnum.cardinality().equals(stringNonEnum2.cardinality())) {
            return new IsCastable.AlwaysPossible.CastingToSame();
        }

        if (Cardinalities.isSubtype(stringNonEnum.cardinality(), stringNonEnum2.cardinality())) {
            return new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget();
        }

        return possible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, NumberType numberType) {
        return possible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, BooleanType.False false_) {
        if (!Cardinalities.contains(stringNonEnum.cardinality(), BigInteger.valueOf(5))) {
            return impossible;
        }

        return possible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, TreeRuleType.RuleType ruleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, TreeRuleType.AnyRule anyRule) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, TreeTokenType.AnyToken anyToken) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, TreeTokenType.TokenType tokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, TreeNodeType.AnyNode anyNode) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, TreeNodeType.NodeType nodeType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, BooleanType.True true_) {
        if (!Cardinalities.contains(stringNonEnum.cardinality(), BigInteger.valueOf(4)))
            return impossible;

        return new IsCastable.Possible( );
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, BooleanType.Boolean boolean_) {
        boolean canBeTrue  = Cardinalities.contains(stringNonEnum.cardinality(), BigInteger.valueOf(4));
        boolean canBeFalse = Cardinalities.contains(stringNonEnum.cardinality(), BigInteger.valueOf(5));

        if (!canBeTrue && !canBeFalse)
            return impossible;

        return new IsCastable.Possible( );
    }

    @Override
    public IsCastable visit(StringType.StringNonEnum stringNonEnum, AtomicType.RegexType regexType) {
        return new IsCastable.Possible( );
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, TreeRuleType.RuleType ruleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, TreeRuleType.AnyRule anyRule) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, TreeTokenType.AnyToken anyToken) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, TreeTokenType.TokenType tokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, TreeNodeType.AnyNode anyNode) {
        return impossible;
    }

    @Override
    public IsCastable visit(StringType.StringEnum stringEnum, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, TreeRuleType.AnyRule anyRule) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, TreeTokenType.AnyToken anyToken) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, TreeTokenType.TokenType tokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, TreeNodeType.AnyNode anyNode) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, TreeNodeType.NodeType nodeType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, StringType.StringEnum stringEnum) {
        return stringEnum.members().contains("false")
                ? new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget( )
                : impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, StringType.StringNonEnum stringNonEnum) {
        return new IsCastable.Possible( );
    }

    @Override
    public IsCastable visit(BooleanType.False false_, NumberType numberType) {
        return new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget();
    }

    @Override
    public IsCastable visit(BooleanType.False false_, BooleanType.False false_2) {
        return new IsCastable.AlwaysPossible.CastingToSame();
    }

    @Override
    public IsCastable visit(BooleanType.False false_, BooleanType.Boolean boolean_) {
        return new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget();
    }

    @Override
    public IsCastable visit(BooleanType.False false_, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.False false_, TreeRuleType.RuleType ruleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, TreeRuleType.RuleType ruleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, TreeRuleType.AnyRule anyRule) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, TreeTokenType.AnyToken anyToken) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, TreeTokenType.TokenType tokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, TreeNodeType.AnyNode anyNode) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, TreeNodeType.NodeType nodeType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, AtomicType.RegexType regexType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, StringType.StringEnum stringEnum) {
        if (stringEnum.members().containsAll(Set.of("true", "false"))) {
            if (stringEnum.members().size() == 2) {
                return new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget( );
            }
            return new IsCastable.Possible( );
        }
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, StringType.StringNonEnum stringNonEnum) {
        if (Cardinalities.contains(stringNonEnum.cardinality(), "true".length())
                && Cardinalities.contains(stringNonEnum.cardinality(), "false".length()))
        {
            return new IsCastable.Possible();
        }
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, NumberType numberType) {
        return new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget( );
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, BooleanType.False false_) {
        return new IsCastable.Possible( );
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, BooleanType.Boolean boolean_2) {
        return new IsCastable.AlwaysPossible.CastingToSame( );
    }

    @Override
    public IsCastable visit(BooleanType.Boolean boolean_, BooleanType.True true_) {
        return new IsCastable.Possible( );
    }

    @Override
    public IsCastable visit(BooleanType.True true_, StringType.StringEnum stringEnum) {
        return stringEnum.members().contains("true")
                ? new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget( )
                : impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, StringType.StringNonEnum stringNonEnum) {
        return new IsCastable.Possible( );
    }

    @Override
    public IsCastable visit(BooleanType.True true_, NumberType numberType) {
        return new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget( );
    }
    @Override
    public IsCastable visit(BooleanType.True true_, BooleanType.Boolean boolean_) {
        return new IsCastable.AlwaysPossible.TypeCanAlwaysBeCastToTarget();
    }

    @Override
    public IsCastable visit(BooleanType.True true_, BooleanType.False false_) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(BooleanType.True true_, BooleanType.True true_2) {
        return new IsCastable.AlwaysPossible.CastingToSame();
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, StringType.StringEnum stringEnum) {
        return new IsCastable.Possible();
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, StringType.StringNonEnum stringNonEnum) {
        return new IsCastable.Possible();
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, BooleanType.True true_) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, BooleanType.Boolean boolean_) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, BooleanType.False false_) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(AtomicType.RegexType regexType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }


    @Override
    public IsCastable visit(NumberType numberType, StringType.StringEnum stringEnum) {
        return new IsCastable.Possible();
    }

    @Override
    public IsCastable visit(NumberType numberType, StringType.StringNonEnum stringNonEnum) {
        return new IsCastable.Possible();
    }

    @Override
    public IsCastable visit(NumberType numberType, NumberType numberType2) {
        if (Ranges.isSubtype(numberType.range(), numberType2.range())) {
            return new IsCastable.AlwaysPossible.CastingToSame();
        }

        return new IsCastable.Possible();
    }

    @Override
    public IsCastable visit(NumberType numberType, BooleanType.False false_) {
        return Ranges.contains(numberType.range(), BigDecimal.ZERO)
                ? new IsCastable.Possible()
                : impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, BooleanType.Boolean boolean_) {
        boolean zero = Ranges.contains(numberType.range(), BigDecimal.ZERO);
        boolean one  = Ranges.contains(numberType.range(), BigDecimal.ONE);

        if (zero || one) {
            return new IsCastable.Possible();
        }

        return impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, BooleanType.True true_) {
        return Ranges.contains(numberType.range(), BigDecimal.ONE)
                ? new IsCastable.Possible()
                : impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, ArrayLikeType.ArrayType arrayType) {
        return impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, ArrayLikeType.TupleType tupleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, MapLikeType.MapType mapType) {
        return impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, MapLikeType.RecordType recordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, GrammarEntityType.GrammarType grammarType) {
        return impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, FunctionType.ConstrainedFunction constrainedFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, FunctionType.AnyFunction anyFunction) {
        return impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, TreeRuleType.RuleType ruleType) {
        return impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, TreeRuleType.AnyRule anyRule) {
        return impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, TreeTokenType.AnyToken anyToken) {
        return impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, TreeTokenType.TokenType tokenType) {
        return impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, TreeNodeType.AnyNode anyNode) {
        return impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, TreeNodeType.NodeType nodeType) {
        return impossible;
    }

    @Override
    public IsCastable visit(NumberType numberType, AtomicType.RegexType regexType) {
        return impossible;
    }

}
