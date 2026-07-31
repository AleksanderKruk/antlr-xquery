
package com.github.akruk.antlrquery.typesystem.typeoperations.itemtype;

import java.util.*;
import java.util.function.Predicate;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.typesystem.RecordField;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Ranges;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.ItemTypes;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.BooleanType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.framework.qual.DefaultQualifier;


@DefaultQualifier(NonNull.class)
public class ItemTypeUnion
    implements ItemTypeBinaryOperation<AntlrQueryItemType>
{

    private final AntlrQueryTypeFactory typeFactory;

    /**
     * Merges two item types into one union item type.
     * e.g.
     * number, string -> (number | string)
     * element(a), element(b) -> element(a | b)
     * @param type1 the first item type
     * @param type2 the second item type
     * @return a new item type that is an alternative of the two input types
     */
    public AntlrQueryItemType union(final AntlrQueryItemType type1, final AntlrQueryItemType type2)
    {
        if (ItemTypes.isSubtype(typeFactory, type1, type2)) {
            return type2;
        }
        if (ItemTypes.isSubtype(typeFactory, type2, type1)) {
            return type1;
        }
        return visit(type1, type2);
    }




    public ItemTypeUnion(final AntlrQueryTypeFactory typeFactory)
    {
        this.typeFactory = Objects.requireNonNull(typeFactory);
    }


    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, AtomicType.NumberType numberType2) {
        return typeFactory.itemNumber(Ranges.union(numberType.range(), numberType2.range()));
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(numberType, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(numberType, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(numberType, regexType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, BooleanType.False false_) {
        return typeFactory.itemChoice(numberType, false_);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, BooleanType.True true_) {
        return typeFactory.itemChoice(numberType, true_);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(numberType, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(numberType, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(numberType, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(numberType, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(numberType, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(numberType, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(numberType, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(numberType, mapType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(numberType, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(numberType, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, TreeNodeType.NodeType elementType) {
        return typeFactory.itemChoice(numberType, elementType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(numberType, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(numberType, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, TreeTokenType.TokenType tokenType) {
        return typeFactory.itemChoice(numberType, tokenType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(numberType, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(numberType, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, TreeRuleType.RuleType ruleType) {
        return typeFactory.itemChoice(numberType, ruleType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(numberType, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(numberType, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(numberType, recordType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, ChoiceItemType choiceItemType) {
        final AntlrQueryItemType[] items = choiceItemType.itemTypes();
        int indexOfNumber = findIf(items, i -> i instanceof AtomicType.NumberType);

        if (indexOfNumber != -1) {
            AtomicType.NumberType existingNumber = (AtomicType.NumberType) items[indexOfNumber];
            AntlrQueryItemType mergedNumber = typeFactory.itemNumber(Ranges.union(numberType.range(), existingNumber.range()));

            AntlrQueryItemType[] result = Arrays.copyOf(items, items.length);
            result[indexOfNumber] = mergedNumber;
            return typeFactory.itemChoice(result);
        }

        AntlrQueryItemType[] result = Arrays.copyOf(items, items.length + 1);
        result[items.length] = numberType;
        return typeFactory.itemChoice(result);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.NumberType numberType, NeverType neverType) {
        return numberType;
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(stringEnum, numberType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, StringType.StringEnum stringEnum2) {
        Set<String> mergedValues = new HashSet<>(stringEnum.members());
        mergedValues.addAll(stringEnum2.members());
        return typeFactory.itemEnum(mergedValues);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemString(Cardinalities.union(stringEnum.cardinality(), stringNonEnum.cardinality()));
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(stringEnum, regexType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, BooleanType.False false_) {
        return typeFactory.itemChoice(stringEnum, false_);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, BooleanType.True true_) {
        return typeFactory.itemChoice(stringEnum, true_);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(stringEnum, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(stringEnum, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(stringEnum, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(stringEnum, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(stringEnum, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(stringEnum, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(stringEnum, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(stringEnum, mapType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(stringEnum, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(stringEnum, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, TreeNodeType.NodeType elementType) {
        return typeFactory.itemChoice(stringEnum, elementType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(stringEnum, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(stringEnum, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, TreeTokenType.TokenType tokenType) {
        return typeFactory.itemChoice(stringEnum, tokenType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(stringEnum, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(stringEnum, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, TreeRuleType.RuleType ruleType) {
        return typeFactory.itemChoice(stringEnum, ruleType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(stringEnum, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(stringEnum, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(stringEnum, recordType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, ChoiceItemType choiceItemType) {
        if (ItemTypes.isSubtype(typeFactory, stringEnum, choiceItemType)) {
            return choiceItemType;
        }

        final AntlrQueryItemType[] items = choiceItemType.itemTypes();

        int indexOfNonEnum = findIf(items, i -> i instanceof StringType.StringNonEnum);
        if (indexOfNonEnum != -1) {
            StringType.StringNonEnum existingNonEnum = (StringType.StringNonEnum) items[indexOfNonEnum];
            AntlrQueryItemType mergedString = typeFactory.itemString(Cardinalities.union(stringEnum.cardinality(), existingNonEnum.cardinality()));

            AntlrQueryItemType[] result = Arrays.copyOf(items, items.length);
            result[indexOfNonEnum] = mergedString;
            return typeFactory.itemChoice(result);
        }

        int indexOfEnum = findIf(items, i -> i instanceof StringType.StringEnum);
        if (indexOfEnum != -1) {
            StringType.StringEnum existingEnum = (StringType.StringEnum) items[indexOfEnum];
            Set<String> mergedValues = new HashSet<>(stringEnum.members());
            mergedValues.addAll(existingEnum.members());
            AntlrQueryItemType mergedEnum = typeFactory.itemEnum(mergedValues);

            AntlrQueryItemType[] result = Arrays.copyOf(items, items.length);
            result[indexOfEnum] = mergedEnum;
            return typeFactory.itemChoice(result);
        }

        AntlrQueryItemType[] result = Arrays.copyOf(items, items.length + 1);
        result[items.length] = stringEnum;
        return typeFactory.itemChoice(result);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringEnum stringEnum, NeverType neverType) {
        return stringEnum;
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(stringNonEnum, numberType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, StringType.StringEnum stringEnum) {
        return typeFactory.itemString(Cardinalities.union(stringNonEnum.cardinality(), stringEnum.cardinality()));
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, StringType.StringNonEnum stringNonEnum2) {
        return typeFactory.itemString(Cardinalities.union(stringNonEnum.cardinality(), stringNonEnum2.cardinality()));
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(stringNonEnum, regexType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, BooleanType.False false_) {
        return typeFactory.itemChoice(stringNonEnum, false_);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, BooleanType.True true_) {
        return typeFactory.itemChoice(stringNonEnum, true_);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(stringNonEnum, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(stringNonEnum, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(stringNonEnum, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(stringNonEnum, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(stringNonEnum, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(stringNonEnum, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(stringNonEnum, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(stringNonEnum, mapType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(stringNonEnum, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(stringNonEnum, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, TreeNodeType.NodeType elementType) {
        return typeFactory.itemChoice(stringNonEnum, elementType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(stringNonEnum, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(stringNonEnum, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, TreeTokenType.TokenType tokenType) {
        return typeFactory.itemChoice(stringNonEnum, tokenType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(stringNonEnum, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(stringNonEnum, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, TreeRuleType.RuleType ruleType) {
        return typeFactory.itemChoice(stringNonEnum, ruleType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(stringNonEnum, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(stringNonEnum, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(stringNonEnum, recordType);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, ChoiceItemType choiceItemType) {
        if (ItemTypes.isSubtype(typeFactory, stringNonEnum, choiceItemType)) {
            return choiceItemType;
        }

        final AntlrQueryItemType[] items = choiceItemType.itemTypes();

        int indexOfSubtype = findIf(items, i -> i instanceof StringType.StringNonEnum || i instanceof StringType.StringEnum);

        if (indexOfSubtype != -1) {
            AntlrQueryItemType existingString = items[indexOfSubtype];
            Cardinality existingCardinality = (existingString instanceof StringType.StringNonEnum)
                    ? ((StringType.StringNonEnum) existingString).cardinality()
                    : ((StringType.StringEnum) existingString).cardinality();

            AntlrQueryItemType mergedString = typeFactory.itemString(Cardinalities.union(stringNonEnum.cardinality(), existingCardinality));

            AntlrQueryItemType[] result = Arrays.copyOf(items, items.length);
            result[indexOfSubtype] = mergedString;
            return typeFactory.itemChoice(result);
        }

        AntlrQueryItemType[] result = Arrays.copyOf(items, items.length + 1);
        result[items.length] = stringNonEnum;
        return typeFactory.itemChoice(result);
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(StringType.StringNonEnum stringNonEnum, NeverType neverType) {
        return stringNonEnum;
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(regexType, numberType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(regexType, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(regexType, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, AtomicType.RegexType regexType2) {
        if (regexType.equals(regexType2)) {
            return regexType;
        }
        return typeFactory.itemChoice(regexType, regexType2);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, BooleanType.False false_) {
        return typeFactory.itemChoice(regexType, false_);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, BooleanType.True true_) {
        return typeFactory.itemChoice(regexType, true_);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(regexType, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(regexType, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(regexType, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(regexType, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(regexType, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(regexType, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(regexType, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(regexType, mapType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(regexType, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(regexType, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, TreeNodeType.NodeType elementType) {
        return visit(elementType, regexType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(regexType, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(regexType, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, TreeTokenType.TokenType tokenType) {
        return visit(tokenType, regexType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(regexType, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(regexType, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, TreeRuleType.RuleType ruleType) {
        return visit(ruleType, regexType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(regexType, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(regexType, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(regexType, recordType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, ChoiceItemType choiceItemType) {
        return visit(choiceItemType, regexType);
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AtomicType.RegexType regexType, NeverType neverType) {
        return regexType;
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(false_, numberType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(false_, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(false_, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(false_, regexType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, BooleanType.False false_2) {
        return false_;
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, BooleanType.True true_) {
        return typeFactory.itemBoolean();
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, BooleanType.Boolean boolean_) {
        return boolean_;
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(false_, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(false_, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(false_, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(false_, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(false_, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(false_, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(false_, mapType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(false_, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(false_, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, TreeNodeType.NodeType elementType) {
        return typeFactory.itemChoice(false_, elementType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(false_, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(false_, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, TreeTokenType.TokenType tokenType) {
        return typeFactory.itemChoice(false_, tokenType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(false_, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(false_, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, TreeRuleType.RuleType ruleType) {
        return typeFactory.itemChoice(false_, ruleType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(false_, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(false_, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(false_, recordType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, NothingType nothingType) { return nothingType; }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, ChoiceItemType choiceItemType) {
        final AntlrQueryItemType[] items = choiceItemType.itemTypes();
        int indexOfTrue = findIf(items, i -> i instanceof BooleanType.True);

        if (indexOfTrue != -1) {
            AntlrQueryItemType[] result = Arrays.copyOf(items, items.length);
            result[indexOfTrue] = typeFactory.itemBoolean();
            return typeFactory.itemChoice(result);
        }

        AntlrQueryItemType[] result = Arrays.copyOf(items, items.length + 1);
        result[items.length] = false_;
        return typeFactory.itemChoice(result);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.False false_, NeverType neverType) {
        return false_;
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(true_, numberType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(true_, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(true_, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(true_, regexType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, BooleanType.False false_) {
        return typeFactory.itemBoolean();
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, BooleanType.True true_2) {
        return true_;
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, BooleanType.Boolean boolean_) {
        return boolean_;
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(true_, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(true_, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(true_, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(true_, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(true_, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(true_, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(true_, mapType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(true_, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(true_, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, TreeNodeType.NodeType elementType) {
        return typeFactory.itemChoice(true_, elementType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(true_, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(true_, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, TreeTokenType.TokenType tokenType) {
        return typeFactory.itemChoice(true_, tokenType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(true_, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(true_, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, TreeRuleType.RuleType ruleType) {
        return typeFactory.itemChoice(true_, ruleType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(true_, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(true_, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(true_, recordType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, ChoiceItemType choiceItemType) {
        final AntlrQueryItemType[] items = choiceItemType.itemTypes();
        int indexOfFalse = findIf(items, i -> i instanceof BooleanType.False);

        if (indexOfFalse != -1) {
            AntlrQueryItemType[] result = Arrays.copyOf(items, items.length);
            result[indexOfFalse] = typeFactory.itemBoolean();
            return typeFactory.itemChoice(result);
        }

        AntlrQueryItemType[] result = Arrays.copyOf(items, items.length + 1);
        result[items.length] = true_;
        return typeFactory.itemChoice(result);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.True true_, NeverType neverType) {
        return true_;
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(boolean_, numberType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(boolean_, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(boolean_, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(boolean_, regexType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, BooleanType.False false_) {
        return boolean_;
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, BooleanType.True true_) {
        return boolean_;
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, BooleanType.Boolean boolean_2) {
        return boolean_;
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(boolean_, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(boolean_, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(boolean_, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(boolean_, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(boolean_, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(boolean_, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(boolean_, mapType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(boolean_, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(boolean_, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, TreeNodeType.NodeType elementType) {
        return typeFactory.itemChoice(boolean_, elementType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(boolean_, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(boolean_, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, TreeTokenType.TokenType tokenType) {
        return typeFactory.itemChoice(boolean_, tokenType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(boolean_, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(boolean_, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, TreeRuleType.RuleType ruleType) {
        return typeFactory.itemChoice(boolean_, ruleType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(boolean_, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(boolean_, anyRule);
    }
    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(boolean_, recordType);
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, ChoiceItemType choiceItemType) {
        final AntlrQueryItemType[] items = choiceItemType.itemTypes();

        if (ItemTypes.isSubtype(typeFactory, boolean_, choiceItemType)) {
            return choiceItemType;
        }

        // Remove True/False subtypes and append Boolean
        int indexOfType = findIf(items, i-> i instanceof BooleanType.True || i instanceof BooleanType.False);
        if (indexOfType == -1) {
            return choiceItemType;
        }
        AntlrQueryItemType[] result = Arrays.copyOf(items, items.length);
        result[indexOfType] = boolean_;
        return typeFactory.itemChoice(result);
    }

    private static @IntRange(from = -1, to = Integer.MAX_VALUE) int findIf(AntlrQueryItemType[] items, Predicate<AntlrQueryItemType> p) {
        int found = -1;
        for (int i = 0, itemsLength = items.length; i < itemsLength; i++) {
            AntlrQueryItemType item = items[i];
            if (p.test(item)) {
                found = i;
                break;
            }
        }
        return found;
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(BooleanType.Boolean boolean_, NeverType neverType) {
        return boolean_;
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(tupleType, numberType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(tupleType, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(tupleType, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(tupleType, regexType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, BooleanType.False false_) {
        return typeFactory.itemChoice(tupleType, false_);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, BooleanType.True true_) {
        return typeFactory.itemChoice(tupleType, true_);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(tupleType, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, ArrayLikeType.TupleType tupleType2) {
        if (tupleType.members().length != tupleType2.members().length) {
            return typeFactory.itemChoice(tupleType, tupleType2);
        }

        int len = tupleType.members().length;
        AntlrQuerySequenceType[] mergedMembers = new AntlrQuerySequenceType[len];

        for (int i = 0; i < len; i++) {
            mergedMembers[i] = Types.addition(typeFactory, tupleType.members()[i], tupleType2.members()[i]);
        }

        return new ArrayLikeType.TupleType(mergedMembers);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(tupleType, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(tupleType, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(tupleType, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(tupleType, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(tupleType, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(tupleType, mapType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(tupleType, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(tupleType, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, TreeNodeType.NodeType elementType) {
        return visit(elementType, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(tupleType, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(tupleType, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, TreeTokenType.TokenType tokenType) {
        return visit(tokenType, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(tupleType, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(tupleType, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, TreeRuleType.RuleType ruleType) {
        return visit(ruleType, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(tupleType, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(tupleType, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(tupleType, recordType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, ChoiceItemType choiceItemType) {
        return visit(choiceItemType, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.TupleType tupleType, NeverType neverType) {
        return tupleType;
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(arrayType, numberType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(arrayType, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(arrayType, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(arrayType, regexType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, BooleanType.False false_) {
        return typeFactory.itemChoice(arrayType, false_);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, BooleanType.True true_) {
        return typeFactory.itemChoice(arrayType, true_);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(arrayType, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(arrayType, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(arrayType, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(arrayType, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(arrayType, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(arrayType, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(arrayType, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(arrayType, mapType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, ArrayLikeType.ArrayType arrayType2) {
        AntlrQuerySequenceType mergedMemberType = Types.union(typeFactory, arrayType.memberType(), arrayType2.memberType() );

        Cardinality mergedCardinality = Cardinalities.union( arrayType.cardinality(), arrayType2.cardinality() );

        return new ArrayLikeType.ArrayType(mergedMemberType, mergedCardinality);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(arrayType, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, TreeNodeType.NodeType elementType) {
        return visit(elementType, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(arrayType, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(arrayType, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, TreeTokenType.TokenType tokenType) {
        return visit(tokenType, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(arrayType, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(arrayType, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, TreeRuleType.RuleType ruleType) {
        return visit(ruleType, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(arrayType, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(arrayType, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(arrayType, recordType);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, ChoiceItemType choiceItemType) {
        return visit(choiceItemType, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, NeverType neverType) {
        return grammarType;
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, BooleanType.False false_) {
        return typeFactory.itemChoice(grammarType, false_);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, BooleanType.True true_) {
        return typeFactory.itemChoice(grammarType, true_);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(grammarType, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(grammarType, regexType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(grammarType, numberType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(grammarType, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(grammarType, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(grammarType, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, TreeNodeType.NodeType elementType) {
        return visit(elementType, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(grammarType, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(grammarType, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, TreeRuleType.RuleType ruleType) {
        return visit(ruleType, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(grammarType, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(grammarType, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, TreeTokenType.TokenType tokenType) {
        return visit(tokenType, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(grammarType, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(grammarType, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(grammarType, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(grammarType, recordType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(grammarType, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType x, GrammarEntityType.GrammarType y) {
        return x.equals(y) ? x : typeFactory.itemChoice(x, y);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(grammarType, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(grammarType, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(grammarType, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(grammarType, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(grammarType, mapType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarType grammarType, ChoiceItemType choiceItemType) {
        return visit(choiceItemType, grammarType);
    }

    // =========================================================================
    // GrammarRuleType
    // =========================================================================

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, NeverType neverType) {
        return grammarRuleType;
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, BooleanType.False false_) {
        return typeFactory.itemChoice(grammarRuleType, false_);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, BooleanType.True true_) {
        return typeFactory.itemChoice(grammarRuleType, true_);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(grammarRuleType, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(grammarRuleType, regexType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(grammarRuleType, numberType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(grammarRuleType, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(grammarRuleType, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(grammarRuleType, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, TreeNodeType.NodeType elementType) {
        return visit(elementType, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(grammarRuleType, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(grammarRuleType, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, TreeRuleType.RuleType ruleType) {
        return visit(ruleType, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(grammarRuleType, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(grammarRuleType, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, TreeTokenType.TokenType tokenType) {
        return visit(tokenType, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(grammarRuleType, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(grammarRuleType, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(grammarRuleType, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(grammarRuleType, recordType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(grammarRuleType, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(grammarRuleType, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType x, GrammarEntityType.GrammarRuleType y) {
        return x.equals(y) ? x : typeFactory.itemChoice(x, y);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(grammarRuleType, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(grammarRuleType, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(grammarRuleType, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(grammarRuleType, mapType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarRuleType grammarRuleType, ChoiceItemType choiceItemType) {
        return visit(choiceItemType, grammarRuleType);
    }
    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, NeverType neverType) {
        return grammarTokenType;
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, BooleanType.False false_) {
        return typeFactory.itemChoice(grammarTokenType, false_);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, BooleanType.True true_) {
        return typeFactory.itemChoice(grammarTokenType, true_);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(grammarTokenType, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(grammarTokenType, regexType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(grammarTokenType, numberType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(grammarTokenType, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(grammarTokenType, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(grammarTokenType, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, TreeNodeType.NodeType elementType) {
        return visit(elementType, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(grammarTokenType, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(grammarTokenType, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, TreeRuleType.RuleType ruleType) {
        return visit(ruleType, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(grammarTokenType, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(grammarTokenType, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, TreeTokenType.TokenType tokenType) {
        return visit(tokenType, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(grammarTokenType, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(grammarTokenType, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(grammarTokenType, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(grammarTokenType, recordType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(grammarTokenType, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(grammarTokenType, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(grammarTokenType, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType x, GrammarEntityType.GrammarTokenType y) {
        return x.equals(y) ? x : typeFactory.itemChoice(x, y);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(grammarTokenType, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(grammarTokenType, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(grammarTokenType, mapType);
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(GrammarEntityType.GrammarTokenType grammarTokenType, ChoiceItemType choiceItemType) {
        return visit(choiceItemType, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, NeverType neverType) {
        return anyFunction;
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(anyFunction, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(anyFunction, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(anyFunction, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(anyFunction, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(anyFunction, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, FunctionType.AnyFunction anyFunction2) {
        return typeFactory.itemChoice(anyFunction, anyFunction2);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(anyFunction, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(anyFunction, regexType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, BooleanType.False false_) {
        return typeFactory.itemChoice(anyFunction, false_);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(anyFunction, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, BooleanType.True true_) {
        return typeFactory.itemChoice(anyFunction, true_);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(anyFunction, numberType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(anyFunction, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(anyFunction, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(anyFunction, recordType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(anyFunction, mapType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(anyFunction, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, TreeNodeType.NodeType nodeType) {
        return typeFactory.itemChoice(anyFunction, nodeType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(anyFunction, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(anyFunction, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, TreeRuleType.RuleType ruleType) {
        return typeFactory.itemChoice(anyFunction, ruleType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(anyFunction, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(anyFunction, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, TreeTokenType.TokenType tokenType) {
        return typeFactory.itemChoice(anyFunction, tokenType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(anyFunction, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(anyFunction, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.AnyFunction anyFunction, ChoiceItemType choiceItemType) {
        if (ItemTypes.isSubtype(typeFactory, anyFunction, choiceItemType)) {
            return choiceItemType;
        }

        final AntlrQueryItemType[] items = choiceItemType.itemTypes();
        int index = findIf(items, i -> i instanceof FunctionType.AnyFunction);
        if (index != -1) {
            return choiceItemType;
        }

        AntlrQueryItemType[] result = Arrays.copyOf(items, items.length + 1);
        result[items.length] = anyFunction;
        return typeFactory.itemChoice(result);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, NeverType neverType) {
        return constrainedFunction;
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(constrainedFunction, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(constrainedFunction, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(constrainedFunction, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(constrainedFunction, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(constrainedFunction, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(constrainedFunction, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, FunctionType.ConstrainedFunction constrainedFunction2) {
        return typeFactory.itemChoice(constrainedFunction, constrainedFunction2);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(constrainedFunction, regexType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, BooleanType.False false_) {
        return typeFactory.itemChoice(constrainedFunction, false_);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(constrainedFunction, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, BooleanType.True true_) {
        return typeFactory.itemChoice(constrainedFunction, true_);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(constrainedFunction, numberType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(constrainedFunction, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(constrainedFunction, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(constrainedFunction, recordType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(constrainedFunction, mapType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(constrainedFunction, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, TreeNodeType.NodeType nodeType) {
        return typeFactory.itemChoice(constrainedFunction, nodeType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(constrainedFunction, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(constrainedFunction, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, TreeRuleType.RuleType ruleType) {
        return typeFactory.itemChoice(constrainedFunction, ruleType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(constrainedFunction, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(constrainedFunction, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, TreeTokenType.TokenType tokenType) {
        return typeFactory.itemChoice(constrainedFunction, tokenType);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(constrainedFunction, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(constrainedFunction, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(FunctionType.ConstrainedFunction constrainedFunction, ChoiceItemType choiceItemType) {
        if (ItemTypes.isSubtype(typeFactory, constrainedFunction, choiceItemType)) {
            return choiceItemType;
        }

        final AntlrQueryItemType[] items = choiceItemType.itemTypes();
        int index = findIf(items, i -> i instanceof FunctionType.ConstrainedFunction);
        if (index != -1) {
            return choiceItemType;
        }

        AntlrQueryItemType[] result = Arrays.copyOf(items, items.length + 1);
        result[items.length] = constrainedFunction;
        return typeFactory.itemChoice(result);
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(ArrayLikeType.ArrayType arrayType, NeverType neverType) {
        return arrayType;
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(extensibleRecordType, numberType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(extensibleRecordType, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(extensibleRecordType, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(extensibleRecordType, regexType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, BooleanType.False false_) {
        return typeFactory.itemChoice(extensibleRecordType, false_);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, BooleanType.True true_) {
        return typeFactory.itemChoice(extensibleRecordType, true_);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(extensibleRecordType, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(extensibleRecordType, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(extensibleRecordType, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(extensibleRecordType, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(extensibleRecordType, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(extensibleRecordType, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(extensibleRecordType, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(extensibleRecordType, mapType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(extensibleRecordType, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType x, MapLikeType.ExtensibleRecordType y) {
        final var xFields = x.fields();
        final var yFields = y.fields();

        final Set<String> allKeys = new LinkedHashSet<>(xFields.keySet());
        allKeys.addAll(yFields.keySet());

        final Map<String, RecordField> mergedFields = new HashMap<>();

        for (String key : allKeys) {
            final @Nullable RecordField xField = xFields.get(key);
            final @Nullable RecordField yField = yFields.get(key);

            if (yField != null && xField != null) {
                final AntlrQuerySequenceType resolvedX = xField.resolveFieldType(typeFactory);
                final AntlrQuerySequenceType resolvedY = yField.resolveFieldType(typeFactory);
                final boolean required = xField.isRequired() && yField.isRequired();

                mergedFields.put(key, new RecordField(
                        key,
                        new RecordField.TypeOrReference.Type(
                                Types.addition(typeFactory, resolvedX, resolvedY)
                        ),
                        required
                ));
            } else if (xField != null) {
                final AntlrQuerySequenceType resolvedX = xField.resolveFieldType(typeFactory);
                mergedFields.put(key, new RecordField(
                        key,
                        new RecordField.TypeOrReference.Type(
                                Types.addition(typeFactory, resolvedX, y.additionalFieldType())
                        ),
                        false
                ));
            } else if (yField != null){
                final AntlrQuerySequenceType resolvedY = yField.resolveFieldType(typeFactory);
                mergedFields.put(key, new RecordField(
                        key,
                        new RecordField.TypeOrReference.Type(
                                Types.addition(typeFactory, x.additionalFieldType(), resolvedY)
                        ),
                        false
                ));
            }
        }


        final AntlrQuerySequenceType mergedAdditional = Types.addition(typeFactory, x.additionalFieldType(), y.additionalFieldType());

        return new MapLikeType.ExtensibleRecordType(mergedFields, mergedAdditional);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, TreeNodeType.NodeType elementType) {
        return visit(elementType, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(extensibleRecordType, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(extensibleRecordType, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, TreeTokenType.TokenType tokenType) {
        return visit(tokenType, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(extensibleRecordType, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(extensibleRecordType, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, TreeRuleType.RuleType ruleType) {
        return visit(ruleType, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(extensibleRecordType, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(extensibleRecordType, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(extensibleRecordType, recordType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, ChoiceItemType choiceItemType) {
        return visit(choiceItemType, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.ExtensibleRecordType extensibleRecordType, NeverType neverType) {
        return extensibleRecordType;
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(mapType, numberType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(mapType, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(mapType, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(mapType, regexType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, BooleanType.False false_) {
        return typeFactory.itemChoice(mapType, false_);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, BooleanType.True true_) {
        return typeFactory.itemChoice(mapType, true_);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(mapType, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(mapType, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(mapType, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(mapType, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(mapType, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(mapType, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(mapType, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType x, MapLikeType.MapType y) {
        final AntlrQueryItemType mergedKey = ItemTypes.union(typeFactory, x.keyType(), y.keyType());
        final AntlrQuerySequenceType mergedValue = Types.union(typeFactory, x.valueType(), y.valueType());
        return new MapLikeType.MapType(mergedKey, mergedValue);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(mapType, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(mapType, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, TreeNodeType.NodeType elementType) {
        return visit(elementType, mapType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(mapType, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(mapType, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, TreeTokenType.TokenType tokenType) {
        return visit(tokenType, mapType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(mapType, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(mapType, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, TreeRuleType.RuleType ruleType) {
        return visit(ruleType, mapType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(mapType, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(mapType, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(mapType, recordType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, ChoiceItemType choiceItemType) {
        return visit(choiceItemType, mapType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.MapType mapType, NeverType neverType) {
        return mapType;
    }
    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(recordType, numberType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(recordType, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(recordType, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(recordType, regexType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, BooleanType.False false_) {
        return typeFactory.itemChoice(recordType, false_);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, BooleanType.True true_) {
        return typeFactory.itemChoice(recordType, true_);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(recordType, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(recordType, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(recordType, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(recordType, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(recordType, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(recordType, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(recordType, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(recordType, mapType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(recordType, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(recordType, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, TreeNodeType.NodeType elementType) {
        return visit(elementType, recordType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(recordType, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(recordType, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, TreeTokenType.TokenType tokenType) {
        return visit(tokenType, recordType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(recordType, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(recordType, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, TreeRuleType.RuleType ruleType) {
        return visit(ruleType, recordType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(recordType, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(recordType, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType x, MapLikeType.RecordType y) {
        final var xFields = x.fields();
        final var yFields = y.fields();

        final Set<String> commonKeys = new HashSet<>(xFields.keySet());
        commonKeys.retainAll(yFields.keySet());
        if (commonKeys.isEmpty()) {
            return typeFactory.itemChoice(x, y);
        }

        final LinkedHashMap<String, RecordField> newFields = new LinkedHashMap<>();

        for (Map.Entry<String, RecordField> entry : xFields.entrySet()) {
            final String key = entry.getKey();
            final RecordField xField = entry.getValue();
            final RecordField yField = yFields.get(key);

            if (yField != null) {
                final AntlrQuerySequenceType resolvedX = xField.resolveFieldType(typeFactory);
                final AntlrQuerySequenceType resolvedY = yField.resolveFieldType(typeFactory);
                final boolean required = xField.isRequired() && yField.isRequired();

                newFields.put(key, new RecordField(
                        key,
                        new RecordField.TypeOrReference.Type(Types.addition(typeFactory, resolvedX, resolvedY)
                        ),
                        required
                ));
            } else {
                newFields.put(key, new RecordField(
                        key,
                        xField.typeOrReference(),
                        false
                ));
            }
        }

        for (Map.Entry<String, RecordField> entry : yFields.entrySet()) {
            final String key = entry.getKey();
            if (!newFields.containsKey(key)) {
                final RecordField yField = entry.getValue();
                newFields.put(key, new RecordField(
                        key,
                        yField.typeOrReference(),
                        false
                ));
            }
        }

        return new MapLikeType.RecordType(newFields);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, ChoiceItemType choiceItemType) {
        return visit(choiceItemType, recordType);
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(MapLikeType.RecordType recordType, NeverType neverType) {
        return recordType;
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(elementType, numberType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(elementType, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(elementType, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(elementType, regexType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, BooleanType.False false_) {
        return typeFactory.itemChoice(elementType, false_);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, BooleanType.True true_) {
        return typeFactory.itemChoice(elementType, true_);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(elementType, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(elementType, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(elementType, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(elementType, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType nodeType, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(nodeType, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType nodeType, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(nodeType, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(elementType, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(elementType, mapType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(elementType, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(elementType, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, TreeNodeType.NodeType elementType2) {
        if (Objects.equals(elementType.grammar(), elementType2.grammar())) {
            Set<NamespaceResolver.QualifiedName> mergedNames = new HashSet<>(elementType.elementNames());
            mergedNames.addAll(elementType2.elementNames());
            return typeFactory.itemNodesFromGrammar(elementType.grammar(), mergedNames);
        }
        return typeFactory.itemChoice(elementType, elementType2);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType nodeType, TreeNodeType.AnyNode anyNode) {
        return anyNode;
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType nodeType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        if (nodeType.grammar().equals(anyNodeFromGrammar.grammar())) {
            return anyNodeFromGrammar;
        }
        return typeFactory.itemChoice(nodeType, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, TreeTokenType.TokenType tokenType) {
        if (Objects.equals(elementType.grammar(), tokenType.grammar())) {
            Set<NamespaceResolver.QualifiedName> mergedNames = new HashSet<>(elementType.elementNames());
            mergedNames.addAll(tokenType.elementNames());
            return typeFactory.itemNodesFromGrammar(elementType.grammar(), mergedNames);
        }
        return typeFactory.itemChoice(elementType, tokenType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType nodeType, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(nodeType, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType nodeType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(nodeType, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType nodeType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(nodeType, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType nodeType, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(nodeType, anyRule);
    }
    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, TreeRuleType.RuleType ruleType) {
        if (Objects.equals(elementType.grammar(), ruleType.grammar())) {
            Set<NamespaceResolver.QualifiedName> mergedNames = new HashSet<>(elementType.elementNames());
            mergedNames.addAll(ruleType.elementNames());
            return typeFactory.itemNodesFromGrammar(elementType.grammar(), mergedNames);
        }
        return typeFactory.itemChoice(elementType, ruleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(elementType, recordType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, ChoiceItemType choiceItemType) {
        return visit(choiceItemType, elementType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, NeverType neverType) {
        return anyNode;
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(anyNode, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(anyNode, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(anyNode, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(anyNode, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(anyNode, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(anyNode, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(anyNode, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(anyNode, regexType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, BooleanType.False false_) {
        return typeFactory.itemChoice(anyNode, false_);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(anyNode, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, BooleanType.True true_) {
        return typeFactory.itemChoice(anyNode, true_);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(anyNode, numberType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(anyNode, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(anyNode, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(anyNode, recordType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(anyNode, mapType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(anyNode, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, TreeNodeType.NodeType nodeType) {
        return typeFactory.itemChoice(anyNode, nodeType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, TreeNodeType.AnyNode anyNode2) {
        return typeFactory.itemChoice(anyNode, anyNode2);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(anyNode, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, TreeRuleType.RuleType ruleType) {
        return typeFactory.itemChoice(anyNode, ruleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(anyNode, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(anyNode, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, TreeTokenType.TokenType tokenType) {
        return typeFactory.itemChoice(anyNode, tokenType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(anyNode, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(anyNode, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNode anyNode, ChoiceItemType choiceItemType) {
        if (ItemTypes.isSubtype(typeFactory, anyNode, choiceItemType)) {
            return choiceItemType;
        }

        final AntlrQueryItemType[] items = choiceItemType.itemTypes();
        int index = findIf(items, i -> i instanceof TreeNodeType.AnyNode);
        if (index != -1) {
            return choiceItemType;
        }

        AntlrQueryItemType[] result = Arrays.copyOf(items, items.length + 1);
        result[items.length] = anyNode;
        return typeFactory.itemChoice(result);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, NeverType neverType) {
        return anyNodeFromGrammar;
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(anyNodeFromGrammar, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(anyNodeFromGrammar, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(anyNodeFromGrammar, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(anyNodeFromGrammar, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(anyNodeFromGrammar, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(anyNodeFromGrammar, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(anyNodeFromGrammar, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(anyNodeFromGrammar, regexType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, BooleanType.False false_) {
        return typeFactory.itemChoice(anyNodeFromGrammar, false_);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(anyNodeFromGrammar, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, BooleanType.True true_) {
        return typeFactory.itemChoice(anyNodeFromGrammar, true_);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(anyNodeFromGrammar, numberType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(anyNodeFromGrammar, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(anyNodeFromGrammar, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(anyNodeFromGrammar, recordType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(anyNodeFromGrammar, mapType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(anyNodeFromGrammar, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeNodeType.NodeType nodeType) {
        if (Objects.equals(anyNodeFromGrammar.grammar(), nodeType.grammar())) {
            return anyNodeFromGrammar;
        }
        return typeFactory.itemChoice(anyNodeFromGrammar, nodeType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(anyNodeFromGrammar, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar x, TreeNodeType.AnyNodeFromGrammar y) {
        if (Objects.equals(x.grammar(), y.grammar())) {
            return x;
        }
        return typeFactory.itemChoice(x, y);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeRuleType.RuleType ruleType) {
        if (Objects.equals(anyNodeFromGrammar.grammar(), ruleType.grammar())) {
            return anyNodeFromGrammar;
        }
        return typeFactory.itemChoice(anyNodeFromGrammar, ruleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        if (Objects.equals(anyNodeFromGrammar.grammar(), anyRuleFromGrammar.grammar())) {
            return anyNodeFromGrammar;
        }
        return typeFactory.itemChoice(anyNodeFromGrammar, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(anyNodeFromGrammar, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeTokenType.TokenType tokenType) {
        if (Objects.equals(anyNodeFromGrammar.grammar(), tokenType.grammar())) {
            return anyNodeFromGrammar;
        }
        return typeFactory.itemChoice(anyNodeFromGrammar, tokenType);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(anyNodeFromGrammar, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar2) {
        if (Objects.equals(anyNodeFromGrammar.grammar(), anyTokenFromGrammar2.grammar())) {
            return anyNodeFromGrammar;
        }
        return typeFactory.itemChoice(anyNodeFromGrammar, anyTokenFromGrammar2);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, ChoiceItemType choiceItemType) {
        if (ItemTypes.isSubtype(typeFactory, anyNodeFromGrammar, choiceItemType)) {
            return choiceItemType;
        }

        final AntlrQueryItemType[] items = choiceItemType.itemTypes();
        int index = findIf(items, i -> i instanceof TreeNodeType.AnyNodeFromGrammar);
        if (index != -1) {
            return choiceItemType;
        }

        AntlrQueryItemType[] result = Arrays.copyOf(items, items.length + 1);
        result[items.length] = anyNodeFromGrammar;
        return typeFactory.itemChoice(result);
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(TreeNodeType.NodeType elementType, NeverType neverType) {
        return elementType;
    }


    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(tokenType, numberType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(tokenType, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(tokenType, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(tokenType, regexType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, BooleanType.False false_) {
        return typeFactory.itemChoice(tokenType, false_);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, BooleanType.True true_) {
        return typeFactory.itemChoice(tokenType, true_);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(tokenType, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(tokenType, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(tokenType, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(tokenType, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(tokenType, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(tokenType, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(tokenType, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(tokenType, mapType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(tokenType, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(tokenType, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, TreeNodeType.NodeType elementType) {
        return visit(elementType, tokenType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, TreeNodeType.AnyNode anyNode) {
        return anyNode;
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        if (tokenType.grammar().equals(anyRuleFromGrammar.grammar())) {
            final var rules = typeFactory.grammarRules(anyRuleFromGrammar.grammar());
            final HashSet<NamespaceResolver.QualifiedName> nodes = new HashSet<>(tokenType.elementNames().size() + rules.size());
            nodes.addAll(rules);
            nodes.addAll(tokenType.elementNames());
            return typeFactory.itemRulesFromGrammar(tokenType.grammar(), nodes);
        }
        return typeFactory.itemChoice(tokenType, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(tokenType, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(tokenType, recordType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, ChoiceItemType choiceItemType) {
        return visit(choiceItemType, tokenType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(tokenType, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, TreeTokenType.TokenType tokenType2) {
        if (Objects.equals(tokenType.grammar(), tokenType2.grammar())) {
            Set<NamespaceResolver.QualifiedName> mergedNames = new HashSet<>(tokenType.elementNames());
            mergedNames.addAll(tokenType2.elementNames());
            return typeFactory.itemTokensFromGrammar(tokenType.grammar(), mergedNames);
        }
        return typeFactory.itemChoice(tokenType, tokenType2);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(tokenType, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(tokenType, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, TreeRuleType.RuleType ruleType) {
        if (Objects.equals(tokenType.grammar(), ruleType.grammar())) {
            Set<NamespaceResolver.QualifiedName> mergedNames = new HashSet<>(tokenType.elementNames());
            mergedNames.addAll(ruleType.elementNames());
            return typeFactory.itemNodesFromGrammar(tokenType.grammar(), mergedNames);
        }
        return typeFactory.itemChoice(tokenType, ruleType);
    }


    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, NeverType neverType) {
        return anyToken;
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(anyToken, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(anyToken, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(anyToken, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(anyToken, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(anyToken, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(anyToken, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(anyToken, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(anyToken, regexType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, BooleanType.False false_) {
        return typeFactory.itemChoice(anyToken, false_);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(anyToken, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, BooleanType.True true_) {
        return typeFactory.itemChoice(anyToken, true_);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(anyToken, numberType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(anyToken, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(anyToken, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(anyToken, recordType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(anyToken, mapType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(anyToken, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, TreeNodeType.NodeType nodeType) {
        return typeFactory.itemChoice(anyToken, nodeType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(anyToken, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(anyToken, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, TreeRuleType.RuleType ruleType) {
        return typeFactory.itemChoice(anyToken, ruleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(anyToken, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(anyToken, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, TreeTokenType.TokenType tokenType) {
        return typeFactory.itemChoice(anyToken, tokenType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, TreeTokenType.AnyToken anyToken2) {
        return typeFactory.itemChoice(anyToken, anyToken2);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(anyToken, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyToken anyToken, ChoiceItemType choiceItemType) {
        if (ItemTypes.isSubtype(typeFactory, anyToken, choiceItemType)) {
            return choiceItemType;
        }

        final AntlrQueryItemType[] items = choiceItemType.itemTypes();
        int index = findIf(items, i -> i instanceof TreeTokenType.AnyToken);
        if (index != -1) {
            return choiceItemType;
        }

        AntlrQueryItemType[] result = Arrays.copyOf(items, items.length + 1);
        result[items.length] = anyToken;
        return typeFactory.itemChoice(result);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, NeverType neverType) {
        return anyTokenFromGrammar;
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(anyTokenFromGrammar, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(anyTokenFromGrammar, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(anyTokenFromGrammar, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(anyTokenFromGrammar, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(anyTokenFromGrammar, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(anyTokenFromGrammar, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(anyTokenFromGrammar, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(anyTokenFromGrammar, regexType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, BooleanType.False false_) {
        return typeFactory.itemChoice(anyTokenFromGrammar, false_);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(anyTokenFromGrammar, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, BooleanType.True true_) {
        return typeFactory.itemChoice(anyTokenFromGrammar, true_);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(anyTokenFromGrammar, numberType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(anyTokenFromGrammar, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(anyTokenFromGrammar, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(anyTokenFromGrammar, recordType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(anyTokenFromGrammar, mapType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(anyTokenFromGrammar, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeNodeType.NodeType nodeType) {
        return typeFactory.itemChoice(anyTokenFromGrammar, nodeType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(anyTokenFromGrammar, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(anyTokenFromGrammar, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeRuleType.RuleType ruleType) {
        return typeFactory.itemChoice(anyTokenFromGrammar, ruleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(anyTokenFromGrammar, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(anyTokenFromGrammar, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeTokenType.TokenType tokenType) {
        return typeFactory.itemChoice(anyTokenFromGrammar, tokenType);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(anyTokenFromGrammar, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar2) {
        return typeFactory.itemChoice(anyTokenFromGrammar, anyTokenFromGrammar2);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, ChoiceItemType choiceItemType) {
        if (ItemTypes.isSubtype(typeFactory, anyTokenFromGrammar, choiceItemType)) {
            return choiceItemType;
        }

        final AntlrQueryItemType[] items = choiceItemType.itemTypes();
        int index = findIf(items, i -> i instanceof TreeTokenType.AnyTokenFromGrammar);
        if (index != -1) {
            return choiceItemType;
        }

        AntlrQueryItemType[] result = Arrays.copyOf(items, items.length + 1);
        result[items.length] = anyTokenFromGrammar;
        return typeFactory.itemChoice(result);
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(TreeTokenType.TokenType tokenType, NeverType neverType) {
        return tokenType;
    }

    // =========================================================================
    // TreeRuleType.RuleType
    // =========================================================================

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(ruleType, numberType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(ruleType, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(ruleType, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(ruleType, regexType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, BooleanType.False false_) {
        return typeFactory.itemChoice(ruleType, false_);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, BooleanType.True true_) {
        return typeFactory.itemChoice(ruleType, true_);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(ruleType, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(ruleType, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(ruleType, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(ruleType, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(ruleType, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(ruleType, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(ruleType, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(ruleType, mapType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(ruleType, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(ruleType, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, TreeNodeType.NodeType elementType) {
        return visit(elementType, ruleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(ruleType, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(ruleType, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(ruleType, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(ruleType, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, TreeTokenType.TokenType tokenType) {
        return visit(tokenType, ruleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, TreeRuleType.RuleType ruleType2) {
        if (Objects.equals(ruleType.grammar(), ruleType2.grammar())) {
            Set<NamespaceResolver.QualifiedName> mergedNames = new HashSet<>(ruleType.elementNames());
            mergedNames.addAll(ruleType2.elementNames());
            return typeFactory.itemRulesFromGrammar(ruleType.grammar(), mergedNames);
        }
        return typeFactory.itemChoice(ruleType, ruleType2);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        if (ruleType.grammar().equals(anyRuleFromGrammar.grammar())) {
            return anyRuleFromGrammar;
        } else {
            return typeFactory.itemChoice(ruleType, anyRuleFromGrammar);
        }
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, TreeRuleType.AnyRule anyRule) {
        return anyRule;
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(ruleType, recordType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, ChoiceItemType choiceItemType) {
        return visit(choiceItemType, ruleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.RuleType ruleType, NeverType neverType) {
        return ruleType;
    }


    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, NeverType neverType) {
        return anyRuleFromGrammar;
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(anyRuleFromGrammar, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(anyRuleFromGrammar, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(anyRuleFromGrammar, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(anyRuleFromGrammar, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(anyRuleFromGrammar, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(anyRuleFromGrammar, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(anyRuleFromGrammar, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(anyRuleFromGrammar, regexType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, BooleanType.False false_) {
        return typeFactory.itemChoice(anyRuleFromGrammar, false_);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(anyRuleFromGrammar, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, BooleanType.True true_) {
        return typeFactory.itemChoice(anyRuleFromGrammar, true_);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(anyRuleFromGrammar, numberType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(anyRuleFromGrammar, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(anyRuleFromGrammar, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(anyRuleFromGrammar, recordType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(anyRuleFromGrammar, mapType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(anyRuleFromGrammar, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeNodeType.NodeType nodeType) {
        return typeFactory.itemChoice(anyRuleFromGrammar, nodeType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(anyRuleFromGrammar, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar2) {
        return typeFactory.itemChoice(anyRuleFromGrammar, anyNodeFromGrammar2);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeRuleType.RuleType ruleType) {
        return typeFactory.itemChoice(anyRuleFromGrammar, ruleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar2) {
        return typeFactory.itemChoice(anyRuleFromGrammar, anyRuleFromGrammar2);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeRuleType.AnyRule anyRule) {
        return typeFactory.itemChoice(anyRuleFromGrammar, anyRule);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeTokenType.TokenType tokenType) {
        return typeFactory.itemChoice(anyRuleFromGrammar, tokenType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(anyRuleFromGrammar, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(anyRuleFromGrammar, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, ChoiceItemType choiceItemType) {
        if (ItemTypes.isSubtype(typeFactory, anyRuleFromGrammar, choiceItemType)) {
            return choiceItemType;
        }

        final AntlrQueryItemType[] items = choiceItemType.itemTypes();
        int index = findIf(items, i -> i instanceof TreeRuleType.AnyRuleFromGrammar);
        if (index != -1) {
            return choiceItemType;
        }

        AntlrQueryItemType[] result = Arrays.copyOf(items, items.length + 1);
        result[items.length] = anyRuleFromGrammar;
        return typeFactory.itemChoice(result);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, NeverType neverType) {
        return anyRule;
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, ArrayLikeType.ArrayType arrayType) {
        return typeFactory.itemChoice(anyRule, arrayType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, ArrayLikeType.TupleType tupleType) {
        return typeFactory.itemChoice(anyRule, tupleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, GrammarEntityType.GrammarType grammarType) {
        return typeFactory.itemChoice(anyRule, grammarType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return typeFactory.itemChoice(anyRule, grammarRuleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return typeFactory.itemChoice(anyRule, grammarTokenType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, FunctionType.AnyFunction anyFunction) {
        return typeFactory.itemChoice(anyRule, anyFunction);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, FunctionType.ConstrainedFunction constrainedFunction) {
        return typeFactory.itemChoice(anyRule, constrainedFunction);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, AtomicType.RegexType regexType) {
        return typeFactory.itemChoice(anyRule, regexType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, BooleanType.False false_) {
        return typeFactory.itemChoice(anyRule, false_);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, BooleanType.Boolean boolean_) {
        return typeFactory.itemChoice(anyRule, boolean_);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, BooleanType.True true_) {
        return typeFactory.itemChoice(anyRule, true_);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, AtomicType.NumberType numberType) {
        return typeFactory.itemChoice(anyRule, numberType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, StringType.StringEnum stringEnum) {
        return typeFactory.itemChoice(anyRule, stringEnum);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, StringType.StringNonEnum stringNonEnum) {
        return typeFactory.itemChoice(anyRule, stringNonEnum);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, MapLikeType.RecordType recordType) {
        return typeFactory.itemChoice(anyRule, recordType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, MapLikeType.MapType mapType) {
        return typeFactory.itemChoice(anyRule, mapType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return typeFactory.itemChoice(anyRule, extensibleRecordType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, TreeNodeType.NodeType nodeType) {
        return typeFactory.itemChoice(anyRule, nodeType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, TreeNodeType.AnyNode anyNode) {
        return typeFactory.itemChoice(anyRule, anyNode);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return typeFactory.itemChoice(anyRule, anyNodeFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, TreeRuleType.RuleType ruleType) {
        return typeFactory.itemChoice(anyRule, ruleType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return typeFactory.itemChoice(anyRule, anyRuleFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, TreeRuleType.AnyRule anyRule2) {
        return typeFactory.itemChoice(anyRule, anyRule2);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, TreeTokenType.TokenType tokenType) {
        return typeFactory.itemChoice(anyRule, tokenType);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, TreeTokenType.AnyToken anyToken) {
        return typeFactory.itemChoice(anyRule, anyToken);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return typeFactory.itemChoice(anyRule, anyTokenFromGrammar);
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(TreeRuleType.AnyRule anyRule, ChoiceItemType choiceItemType) {
        if (ItemTypes.isSubtype(typeFactory, anyRule, choiceItemType)) {
            return choiceItemType;
        }

        final AntlrQueryItemType[] items = choiceItemType.itemTypes();
        int index = findIf(items, i -> i instanceof TreeRuleType.AnyRule);
        if (index != -1) {
            return choiceItemType;
        }

        AntlrQueryItemType[] result = Arrays.copyOf(items, items.length + 1);
        result[items.length] = anyRule;
        return typeFactory.itemChoice(result);
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, AtomicType.NumberType numberType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, StringType.StringEnum stringEnum) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, StringType.StringNonEnum stringNonEnum) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, AtomicType.RegexType regexType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, BooleanType.False false_) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, BooleanType.True true_) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, BooleanType.Boolean boolean_) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, ArrayLikeType.TupleType tupleType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, FunctionType.AnyFunction anyFunction) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, FunctionType.ConstrainedFunction constrainedFunction) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, GrammarEntityType.GrammarType grammarType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, MapLikeType.MapType mapType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, ArrayLikeType.ArrayType arrayType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, TreeNodeType.NodeType elementType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, TreeNodeType.AnyNode anyNode) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, TreeTokenType.TokenType tokenType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, TreeTokenType.AnyToken anyToken) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, TreeRuleType.RuleType ruleType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, TreeRuleType.AnyRule anyRule) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, MapLikeType.RecordType recordType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, NothingType nothingType2) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, ChoiceItemType choiceItemType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, AnyItemType anyItemType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NothingType nothingType, NeverType neverType) {
        return nothingType;
    }
    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, AtomicType.NumberType numberType) {
        return visit(numberType, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, StringType.StringEnum stringEnum) {
        return visit(stringEnum, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, StringType.StringNonEnum stringNonEnum) {
        return visit(stringNonEnum, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, AtomicType.RegexType regexType) {
        return visit(regexType, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, BooleanType.False false_) {
        return visit(false_, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, BooleanType.True true_) {
        return visit(true_, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, BooleanType.Boolean boolean_) {
        return visit(boolean_, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, ArrayLikeType.TupleType tupleType) {
        return visit(tupleType, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return visit(grammarRuleType, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return visit(grammarTokenType, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, FunctionType.AnyFunction anyFunction) {
        return visit(anyFunction, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, FunctionType.ConstrainedFunction constrainedFunction) {
        return visit(constrainedFunction, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, GrammarEntityType.GrammarType grammarType) {
        return visit(grammarType, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, MapLikeType.MapType mapType) {
        return visit(mapType, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, ArrayLikeType.ArrayType arrayType) {
        return visit(arrayType, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return visit(extensibleRecordType, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, TreeNodeType.NodeType elementType) {
        return visit(elementType, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, TreeNodeType.AnyNode anyNode) {
        return visit(anyNode, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return visit(anyNodeFromGrammar, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, TreeTokenType.TokenType tokenType) {
        return visit(tokenType, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, TreeTokenType.AnyToken anyToken) {
        return visit(anyToken, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return visit(anyTokenFromGrammar, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, TreeRuleType.RuleType ruleType) {
        return visit(ruleType, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return visit(anyRuleFromGrammar, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, TreeRuleType.AnyRule anyRule) {
        return visit(anyRule, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, MapLikeType.RecordType recordType) {
        return visit(recordType, choiceItemType);
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, ChoiceItemType choiceItemType2) {
        AntlrQueryItemType result = choiceItemType;
        for (AntlrQueryItemType item : choiceItemType2.itemTypes()) {
            result = union(item, result);
        }
        return result;
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(ChoiceItemType choiceItemType, NeverType neverType) {
        return choiceItemType;
    }
    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, AtomicType.NumberType numberType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, StringType.StringEnum stringEnum) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, StringType.StringNonEnum stringNonEnum) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, AtomicType.RegexType regexType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, BooleanType.False false_) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, BooleanType.True true_) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, BooleanType.Boolean boolean_) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, ArrayLikeType.TupleType tupleType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, FunctionType.AnyFunction anyFunction) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, FunctionType.ConstrainedFunction constrainedFunction) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, GrammarEntityType.GrammarType grammarType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, MapLikeType.MapType mapType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, ArrayLikeType.ArrayType arrayType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, TreeNodeType.NodeType elementType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, TreeNodeType.AnyNode anyNode) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, TreeTokenType.TokenType tokenType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, TreeTokenType.AnyToken anyToken) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, TreeRuleType.RuleType ruleType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, TreeRuleType.AnyRule anyRule) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, MapLikeType.RecordType recordType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, NothingType nothingType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, ChoiceItemType choiceItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, AnyItemType anyItemType2) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(AnyItemType anyItemType, NeverType neverType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, AtomicType.NumberType numberType) {
        return numberType;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, StringType.StringEnum stringEnum) {
        return stringEnum;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, StringType.StringNonEnum stringNonEnum) {
        return stringNonEnum;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, AtomicType.RegexType regexType) {
        return regexType;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, BooleanType.False false_) {
        return false_;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, BooleanType.True true_) {
        return true_;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, BooleanType.Boolean boolean_) {
        return boolean_;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, ArrayLikeType.TupleType tupleType) {
        return tupleType;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, GrammarEntityType.GrammarRuleType grammarRuleType) {
        return grammarRuleType;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, GrammarEntityType.GrammarTokenType grammarTokenType) {
        return grammarTokenType;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, FunctionType.AnyFunction anyFunction) {
        return anyFunction;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, FunctionType.ConstrainedFunction constrainedFunction) {
        return constrainedFunction;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, GrammarEntityType.GrammarType grammarType) {
        return grammarType;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, MapLikeType.MapType mapType) {
        return mapType;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, ArrayLikeType.ArrayType arrayType) {
        return arrayType;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, MapLikeType.ExtensibleRecordType extensibleRecordType) {
        return extensibleRecordType;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, TreeNodeType.NodeType elementType) {
        return elementType;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, TreeNodeType.AnyNode anyNode) {
        return anyNode;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return anyNodeFromGrammar;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, TreeTokenType.TokenType tokenType) {
        return tokenType;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, TreeTokenType.AnyToken anyToken) {
        return anyToken;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return anyTokenFromGrammar;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, TreeRuleType.RuleType ruleType) {
        return ruleType;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return anyRuleFromGrammar;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, TreeRuleType.AnyRule anyRule) {
        return anyRule;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, MapLikeType.RecordType recordType) {
        return recordType;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, NothingType nothingType) {
        return nothingType;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, ChoiceItemType choiceItemType) {
        return choiceItemType;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, AnyItemType anyItemType) {
        return anyItemType;
    }

    @Override
    public AntlrQueryItemType visit(NeverType neverType, NeverType neverType2) {
        return neverType2;
    }
}
