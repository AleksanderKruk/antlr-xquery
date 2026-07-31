package com.github.akruk.antlrquery.typesystem.typeoperations.itemtype;

import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Ranges;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@DefaultQualifier(NonNull.class)
public class ItemTypeIsSubtype {
    private final AntlrQueryTypeFactory typeFactory;
    private final MapTypeIsSubtype mapTypeIsSubtype;
    private final ArrayTypeIsSubtype arrayVisitor;
    private final ArrayToMapIsSubtype arrayToMapVisitor;
    private final TreeNodeIsSubtype nodesVisitor;
    private final StringIsSubtype stringIsSubtype;
    private final MapLikeSubtypeOfFunction mapSubtypeOfFunction;
    private final ArrayLikeSubtypeOfFunction arraySubtypeOfFunction;

    public ItemTypeIsSubtype(AntlrQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
        this.mapTypeIsSubtype = new MapTypeIsSubtype(typeFactory);
        this.arrayVisitor = new ArrayTypeIsSubtype(typeFactory);
        this.arrayToMapVisitor = new ArrayToMapIsSubtype(typeFactory);
        this.nodesVisitor = new TreeNodeIsSubtype(typeFactory);
        this.stringIsSubtype = new StringIsSubtype();
        mapSubtypeOfFunction = new MapLikeSubtypeOfFunction(typeFactory);
        arraySubtypeOfFunction = new ArrayLikeSubtypeOfFunction(typeFactory);
    }

    public boolean isSubtype(AntlrQueryItemType t1, AntlrQueryItemType t2) {
        if (t1 instanceof NeverType) return true;
        if (t1 instanceof NothingType) return true;
        if (t1 instanceof ChoiceItemType c1) return allItemsAreSubtypesOf(c1, t2);
        return switch(t2) {
            case AnyItemType _ -> true;
            case ChoiceItemType choiceItemType -> anyItemsAreSubtypesOf(t1, choiceItemType);
            case ArrayLikeType arrayLikeType ->
                    t1 instanceof final ArrayLikeType a1
                        && this.arrayVisitor.visit(a1, arrayLikeType);
            case AtomicType.NumberType(NumericRange r2) ->
                    t1 instanceof AtomicType.NumberType(NumericRange r1) && Ranges.isSubSet(r1, r2);
            case AtomicType.RegexType(Pattern p2) ->
                    t1 instanceof AtomicType.RegexType(Pattern p1) && p1.equals(p2);
            case BooleanType.Boolean _ -> t1 instanceof BooleanType;
            case BooleanType.False _ -> t1 instanceof BooleanType.False;
            case BooleanType.True _ -> t1 instanceof BooleanType.True;
            case StringType s2 -> t1 instanceof StringType s1 && stringIsSubtype.visit(s1, s2);
            case FunctionType f2 -> switch (t1) {
                case final FunctionType f1 -> {
                    if (f2 instanceof FunctionType.AnyFunction) yield true;
                    if (f1 instanceof FunctionType.AnyFunction) yield false;


                    final List<AntlrQuerySequenceType> aArgs = ((FunctionType.ConstrainedFunction) f1).argumentTypes();
                    final List<AntlrQuerySequenceType> bArgs = ((FunctionType.ConstrainedFunction) f2).argumentTypes();

                    final int aSize = aArgs.size();
                    if (aSize > bArgs.size()) {
                        yield false;
                    }

                    for (int i = 0; i < aSize; i++) {
                        final var aArgType = aArgs.get(i);
                        final var bArgType = bArgs.get(i);
                        if (!Types.isSubtype(typeFactory, bArgType, aArgType)) {
                            yield false;
                        }
                    }

                    yield Types.isSubtype(typeFactory, f1.returnType(), f2.returnType());
                }
                case final MapLikeType m1 -> mapSubtypeOfFunction.visit(m1, f2);
                case final ArrayLikeType m1 -> arraySubtypeOfFunction.visit(m1, f2);
                default -> false;
            };
            case GrammarEntityType _ -> t1 instanceof GrammarEntityType;
            case MapLikeType mapLikeType -> {
                if (t1 instanceof final MapLikeType m1) {
                    yield this.mapTypeIsSubtype.visit(m1, mapLikeType);
                }
                if (t1 instanceof final ArrayLikeType a1) {
                    yield this.arrayToMapVisitor.visit(a1, mapLikeType);
                }

                yield false;
            }
            case TreeLike n2 -> {
                if (t1 instanceof TreeLike n1)  {
                    yield nodesVisitor.visit(n1, n2);
                }
                yield false;
            }
            case NeverType _, NothingType _ -> false; // ( {* - { NeverType, NothingType }} x {NeverType, NothingType})
        };
    }

    private boolean allItemsAreSubtypesOf(ChoiceItemType choice, AntlrQueryItemType superType) {
        return Arrays.stream(choice.itemTypes()).allMatch(i->isSubtype(i, superType));
    }

    private boolean anyItemsAreSubtypesOf(AntlrQueryItemType subType, ChoiceItemType choice) {
        return Arrays.stream(choice.itemTypes()).anyMatch(i->isSubtype(subType, i));
    }



}
