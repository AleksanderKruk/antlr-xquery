package com.github.akruk.antlrquery.typesystem.types;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.typeoperations.itemtype.ItemTypeIntersection;
import com.github.akruk.antlrquery.typesystem.typeoperations.itemtype.ItemTypeIsSubtype;
import com.github.akruk.antlrquery.typesystem.typeoperations.itemtype.ItemTypeUnion;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.FunctionType;
import com.github.akruk.visitorannotations.Visitor;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrquery.typesystem.RecordField;
import com.github.akruk.antlrquery.typesystem.RecordField.TypeOrReference;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Ranges;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.ArrayLikeType.ArrayType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.MapLikeType.ExtensibleRecordType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.MapLikeType.MapType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.MapLikeType.RecordType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.ArrayLikeType.TupleType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.ArrayLenRange;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@Visitor(name = "AreValueComparable", classes = {AtomicType.class, AtomicType.class})
public final class ItemTypes {
    private ItemTypes(){}

    public static AntlrQueryItemType union(AntlrQueryTypeFactory typeFactory, AntlrQueryItemType@ArrayLenRange(from = 1)... itemTypes) {
        assert itemTypes.length != 0;
        var merger = new ItemTypeUnion(typeFactory);
        return Arrays.stream(itemTypes).reduce(merger::union).get();
    }

    public static String stringifyConcreteItemType(final ConcreteItemType item) {
        return switch(item) {
            case AtomicType c -> ItemTypes.stringifyAtomicType(c);
            case ArrayLikeType.ArrayType e -> ItemTypes.stringifyArrayType(e);
            case MapLikeType.MapType e -> ItemTypes.stringifyMapType(e);
            case MapLikeType.RecordType e -> ItemTypes.stringifyRecordType(e);
            case MapLikeType.ExtensibleRecordType e -> ItemTypes.stringifyExtensibleRecordType(e);
            case ArrayLikeType.TupleType e -> ItemTypes.stringifyTupleType(e);
            case GrammarEntityType c2 -> ItemTypes.stringifyGrammarEntity(c2);
            case TreeLike c2 -> ItemTypes.stringifyTreeNodeType(c2);
            case FunctionType.AnyFunction anyFunction -> ItemTypes.stringify(anyFunction);
            case FunctionType.ConstrainedFunction functionType -> ItemTypes.stringify(functionType);
        };
    }
    public static String stringify(final FunctionType.AnyFunction ignoredAnyFunction) {
        return "function(*)";
    }
    public static String stringify(final FunctionType.ConstrainedFunction function) {
        final List<AntlrQuerySequenceType> args = function.argumentTypes();
        final AntlrQuerySequenceType returnedType = function.returnType();
        final String argsString = args.stream()
                .map(Types::stringify)
                .collect(Collectors.joining(", "));
        return "fn(" + argsString + ") as " + Types.stringify(returnedType);
    }

    private static String stringifyTreeNodeType(TreeLike c2) {
        return switch(c2) {
            case TreeNodeType.NodeType(String _, Set<QualifiedName> elementNames) ->
                elementNames.stream()
                    .sorted()
                    .map(QualifiedName::toString)
                    .collect(Collectors.joining(" | ", "element(", ")"));
            case TreeRuleType.RuleType(String _, Set<QualifiedName> elementNames) ->
                elementNames.stream()
                    .sorted()
                    .map(QualifiedName::toString)
                    .collect(Collectors.joining(" | ", "rule(", ")"));

            case TreeTokenType.TokenType(String _, Set<QualifiedName> elementNames) ->
                elementNames.stream()
                    .sorted()
                    .map(QualifiedName::toString)
                    .collect(Collectors.joining(" | ", "token(", ")"));
            case TreeNodeType.AnyNode anyNode -> null;
            case TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar -> null;
            case TreeRuleType.AnyRule anyRule -> null;
            case TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar -> null;
            case TreeTokenType.AnyToken anyToken -> null;
            case TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar -> null;
        };
    }

    public static String stringifyTupleType(final TupleType e) {
        return Arrays.stream(e.members())
            .map(Types::stringify)
            .collect(Collectors.joining(", ", "[", "]"))
            ;
    }


    public static String stringifyExtensibleRecordType(final ExtensibleRecordType e) {
        final boolean isAny = e.additionalFieldType().itemType().equals(AntlrQueryItemType.ANY_TYPE); 
        final String fieldType = isAny? "": Types.stringify(e.additionalFieldType());
        final String extensibleEnding = ", " +fieldType  + "*}";
        return e.fields().values().stream()
            .map(ItemTypes::stringifyRecordField)
            .collect(Collectors.joining(", ", "{", extensibleEnding))
            ;
    }

    public static String stringifyRecordType(final RecordType e) {
        return e.fields().values().stream()
            .map(ItemTypes::stringifyRecordField)
            .collect(Collectors.joining(", ", "{", "}"))
            ;
    }

    public static String stringifyMapType(MapType e) {
        return "{" + e.keyType() + " : " + e.valueType() + "}";
    }

    public static String stringifyArrayType(ArrayType e) {
        final String typeString = Types.stringify(e.memberType());
        if (e.cardinality().equals(Cardinality.ZERO_OR_MORE)) {
            return typeString + "[]";
        }
        return typeString + "[" + Cardinalities.stringify(e.cardinality()) + "]";
    }

    public static String stringifyGrammarEntity(GrammarEntityType c2) {
        throw new IllegalStateException();
    }


    public static String stringifyRecordField(final  RecordField field) {
        final TypeOrReference typeOrRef = field.typeOrReference();
        final String fieldSuffix = field.isRequired()? "":"?"; 
        return switch(typeOrRef) {
            case TypeOrReference.Reference(QualifiedName reference, Cardinality cardinality) ->
                    field.name() + fieldSuffix + " as " + reference + Cardinalities.stringifyWithPrefix(cardinality);
            case TypeOrReference.Type(AntlrQuerySequenceType type) ->
                    field.name() + fieldSuffix + " as " + Types.stringify(type);
        };
    }
    public static String stringifyAtomicType(AtomicType c) {
        return switch(c) {
            case AtomicType.NumberType(NumericRange range) -> "number"+Ranges.stringify(range);
            case StringType s -> switch(s) {
                case StringType.StringEnum(Set<String> enumValues, Cardinality _) ->
                    "string" + enumValues.stream().sorted().collect(Collectors.joining(" | ", "(", ")"));
                case StringType.StringNonEnum(Cardinality length) -> 
                    "string" + Cardinalities.stringifyWithoutParentheses(length);
            };
            case BooleanType c2 -> switch(c2) {
                case BooleanType.True _ -> "true";
                case BooleanType.False _ -> "false";
                case BooleanType.Boolean _ -> "boolean";
            };
            case AtomicType.RegexType(Pattern pattern) ->
                "regex("+pattern.pattern()+")";
        };
    }

    public static String stringify(final  AntlrQueryItemType item) {
        return switch(item) {
            case ConcreteItemType c -> ItemTypes.stringifyConcreteItemType(c);
            case ChoiceItemType(ConcreteItemType[] itemTypes) ->
                Arrays.stream(itemTypes)
                    .map(ItemTypes::stringify)
                    .sorted()
                    .collect(Collectors.joining(" | ", "(", ")"));
            case AnyItemType() -> "item()";
            case NothingType() -> "∅";
            case NeverType() -> "⊥";
        };
    }

    public static boolean isSubtype(AntlrQueryTypeFactory typeFactory, AntlrQueryItemType tested, AntlrQueryItemType itemAnyItem) {
        var merger = new ItemTypeIsSubtype(typeFactory);
        return merger.isSubtype(tested, itemAnyItem);
    }

    public static @Nullable AntlrQueryItemType intersection(AntlrQueryTypeFactory typeFactory, AntlrQueryItemType@ArrayLenRange(from = 1)... array) {
        return ItemTypeIntersection.intersection(typeFactory, array);
    }

    public static boolean areValueComparable(AntlrQueryItemType type, AntlrQueryItemType type2) {
        if (!(type instanceof final AtomicType a1) || !(type2 instanceof AtomicType a2)) {
            return false;
        }
        return switch (a1) {
            case AtomicType.NumberType numberType when a2 instanceof AtomicType.NumberType -> true;
            case StringType stringType when a2 instanceof StringType -> true;
            case BooleanType.Boolean aBoolean when a2 instanceof BooleanType.Boolean -> true;
            case AtomicType.RegexType r when a2 instanceof AtomicType.RegexType -> true;
            default -> false;
        };
    }
}

