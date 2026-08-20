package com.github.akruk.antlrquery.typesystem.typeoperations.stringify;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.typesystem.RecordField;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Ranges;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@DefaultQualifier(NonNull.class)
public final class Stringify {
    private Stringify(){}


    public static String stringify(final ConcreteItemType item) {
        return switch(item) {
            case AtomicType c -> stringify(c);
            case ArrayLikeType.ArrayType e -> stringify(e);
            case MapLikeType.MapType e -> stringify(e);
            case MapLikeType.RecordType e -> Stringify.stringify(e);
            case MapLikeType.ExtensibleRecordType e -> Stringify.stringify(e);
            case ArrayLikeType.TupleType e -> Stringify.stringify(e);
            case GrammarEntityType c2 -> Stringify.stringify(c2);
            case TreeLike c2 -> Stringify.stringify(c2);
            case FunctionType.AnyFunction anyFunction -> Stringify.stringify(anyFunction);
            case FunctionType.ConstrainedFunction functionType -> Stringify.stringify(functionType);
        };
    }

    public static String stringify(final FunctionType.AnyFunction ignoredAnyFunction) {
        return "function(*)";
    }
    public static String stringify(final FunctionType.ConstrainedFunction function) {
        final List<AntlrQuerySequenceType> args = function.argumentTypes();
        final AntlrQuerySequenceType returnedType = function.returnType();
        final String argsString = args.stream()
                .map(Stringify::stringify)
                .collect(Collectors.joining(", "));
        return "fn(" + argsString + ") as " + stringify(returnedType);
    }

    private static String stringify(TreeLike c2) {
        return switch(c2) {
            case TreeNodeType.NodeType(String _, Set<NamespaceResolver.QualifiedName> elementNames) ->
                    elementNames.stream()
                            .map(NamespaceResolver.QualifiedName::toString)
                            .sorted()
                            .collect(Collectors.joining(" | ", "<", ">"));
            case TreeRuleType.RuleType(String _, Set<NamespaceResolver.QualifiedName> elementNames) ->
                    elementNames.stream()
                            .map(NamespaceResolver.QualifiedName::toString)
                            .sorted()
                            .collect(Collectors.joining(" | ", "rule(", ")"));

            case TreeTokenType.TokenType(String _, Set<NamespaceResolver.QualifiedName> elementNames) ->
                    elementNames.stream()
                            .map(NamespaceResolver.QualifiedName::toString)
                            .sorted()
                            .collect(Collectors.joining(" | ", "token(", ")"));
            case TreeNodeType.AnyNode() -> "<*>";
            case TreeNodeType.AnyNodeFromGrammar(String grammar) -> "<" + grammar + ":*>";
            case TreeRuleType.AnyRule() -> "rule(*)";
            case TreeRuleType.AnyRuleFromGrammar(String grammar) -> "rule(" + grammar + ":*)";
            case TreeTokenType.AnyToken() -> "token(*)";
            case TreeTokenType.AnyTokenFromGrammar(String grammar) -> "token(" + grammar + ":*)";
        };
    }

    public static String stringify(final ArrayLikeType.TupleType e) {
        return Arrays.stream(e.members())
                .map(Stringify::stringify)
                .collect(Collectors.joining(", ", "[", "]"))
                ;
    }


    public static String stringify(final MapLikeType.ExtensibleRecordType e) {
        final boolean isAny = e.additionalFieldType().itemType().equals(AntlrQueryItemType.ANY_TYPE);
        final String fieldType = isAny? "": stringify(e.additionalFieldType());
        final String extensibleEnding = ", " +fieldType  + "*}";
        return e.fields().values().stream()
                .map(Stringify::stringifyRecordField)
                .collect(Collectors.joining(", ", "{", extensibleEnding))
                ;
    }

    public static String stringify(final MapLikeType.RecordType e) {
        return e.fields().values().stream()
                .map(Stringify::stringifyRecordField)
                .collect(Collectors.joining(", ", "{", "}"))
                ;
    }

    public static String stringify(MapLikeType.MapType e) {
        return "{" + e.keyType() + " : " + e.valueType() + "}";
    }

    public static String stringify(ArrayLikeType.ArrayType e) {
        final String typeString = stringify(e.memberType());
        if (e.cardinality().equals(Cardinality.ZERO_OR_MORE)) {
            return typeString + "[]";
        }
        return typeString + "[" + Cardinalities.stringify(e.cardinality()) + "]";
    }

    public static String stringify(GrammarEntityType c2) {
        throw new IllegalStateException();
    }


    public static String stringifyRecordField(final RecordField field) {
        final RecordField.TypeOrReference typeOrRef = field.typeOrReference();
        final String fieldSuffix = field.isRequired()? "":"?";
        return switch(typeOrRef) {
            case RecordField.TypeOrReference.Reference(NamespaceResolver.QualifiedName reference, Cardinality cardinality) ->
                    field.name() + fieldSuffix + " as " + reference + Cardinalities.stringifyWithPrefix(cardinality);
            case RecordField.TypeOrReference.Type(AntlrQuerySequenceType type) ->
                    field.name() + fieldSuffix + " as " + stringify(type);
        };
    }
    public static String stringify(AtomicType c) {
        return switch(c) {
            case NumberType n -> stringify(n);
            case StringType s -> switch(s) {
                case StringType.StringEnum(Set<String> enumValues, Cardinality _) when enumValues.size() == 1 ->
                        enumValues.stream().findFirst().get();
                case StringType.StringEnum(Set<String> enumValues, Cardinality _) ->
                        enumValues.stream()
                                .sorted()
                                .map(member->"'" + member + "'")
                                .collect(Collectors.joining(" | ", "(", ")"));
                case StringType.StringNonEnum(Cardinality length) when length.equals(Cardinality.ZERO_OR_MORE) ->
                        "string";
                case StringType.StringNonEnum(Cardinality length) ->
                        "string(" + Cardinalities.stringifyWithoutParentheses(length) + ")";
            };
            case BooleanType c2 -> switch(c2) {
                case BooleanType.True _ -> "true";
                case BooleanType.False _ -> "false";
                case BooleanType.Boolean _ -> "boolean";
            };
            case RegexType(Pattern pattern) ->
                    "regex("+pattern.pattern()+")";
        };
    }

    private static String stringify(NumberType t) {
        if (t.range().equals(NumericRange.FULL)) {
            return "number";
        }
        return Ranges.stringify(t.range());
    }

    public static String stringify(final  AntlrQueryItemType item) {
        return switch(item) {
            case ConcreteItemType c -> Stringify.stringify(c);
            case ChoiceItemType(ConcreteItemType[] itemTypes) ->
                    Arrays.stream(itemTypes)
                            .map(Stringify::stringify)
                            .sorted()
                            .collect(Collectors.joining(" | ", "(", ")"));
            case AnyItemType() -> "item()";
            case NothingType() -> "∅";
            case NeverType() -> "⊥";
            case NamedItemType namedItemType -> namedItemType.reference().toString();
        };
    }

    public static String stringifyWithoutParentheses(final  AntlrQueryItemType item) {
        return switch(item) {
            case ConcreteItemType c -> Stringify.stringify(c);
            case ChoiceItemType(ConcreteItemType[] itemTypes) ->
                    Arrays.stream(itemTypes)
                            .map(Stringify::stringify)
                            .sorted()
                            .collect(Collectors.joining(" | "));
            case AnyItemType() -> "item()";
            case NothingType() -> "∅";
            case NeverType() -> "⊥";
            case NamedItemType namedItemType -> namedItemType.reference().toString();
        };
    }

    public static String stringify(final AntlrQuerySequenceType type) {
        return switch(type) {
            case AntlrQuerySequenceType.EmptySequence() -> "empty-sequence()";
            case AntlrQuerySequenceType.NonEmptySequence(AntlrQueryItemType itemType, Cardinality cardinality) -> {
                String cardinalityRepr = Cardinalities.stringifyWithPrefix(cardinality);
                if (cardinalityRepr.isEmpty()) {
                    yield stringifyWithoutParentheses(itemType);
                }
                if (itemType instanceof AnyItemType) {
                    yield switch(cardinalityRepr) {
                        case "?", "*", "+" -> cardinalityRepr;
                        default -> "item()" + cardinalityRepr;
                    };
                }
                if (itemType instanceof final FunctionType.ConstrainedFunction cf) {
                    yield "(" + stringify(cf) + ")" + cardinalityRepr;
                }
                yield stringify(itemType) + cardinalityRepr;
            }
        };
    }
}
