package com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.granularanalysis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.semanticanalyzer.VisitingSemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticSymbolManager;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Ranges;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.ArrayLikeType.ArrayType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.ArrayLikeType.TupleType;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class ArrayPutGranularAnalysis
        implements SemanticSymbolManager.GrainedFunctionCallAnalysis {

    private final AntlrQueryTypeFactory typeFactory;

    public ArrayPutGranularAnalysis(AntlrQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }

    @Override
    public SemanticSymbolManager.FunctionCallAnalysis analyze(
            final List<SemanticSymbolManager.UsedArg> args,
            final @Nullable VisitingSemanticContext context,
            final @Nullable ParseTree functionBody,
            final AntlrQuerySemanticContext typeContext) {

        final AntlrQuerySequenceType array =
                args.get(0).type().type;

        final AntlrQuerySequenceType position =
                args.get(1).type().type;

        final AntlrQuerySequenceType member =
                args.get(2).type().type;

        return SemanticSymbolManager.FunctionCallAnalysis.typeOnly(typeContext.typeInContext(
                analyze(array, position, member)));
    }

    private AntlrQuerySequenceType analyze(
            final AntlrQuerySequenceType array,
            final AntlrQuerySequenceType position,
            final AntlrQuerySequenceType member) {

        return typeFactory.one(
                arrayPut(
                        array.itemType(),
                        position,
                        member));
    }

    private AntlrQueryItemType arrayPut(
            final AntlrQueryItemType array,
            final AntlrQuerySequenceType position,
            final AntlrQuerySequenceType member) {

        return switch (array) {
            case ChoiceItemType choice ->
                    arrayPut(choice, position, member);

            case ConcreteItemType concrete ->
                    arrayPut(concrete, position, member);

            case NamedItemType(NamespaceResolver.QualifiedName reference) ->
                    arrayPut(
                            typeFactory.guaranteedItemNamedType(
                                    reference,
                                    new IllegalStateException()),
                            position,
                            member);

            case NeverType _, NothingType _, AnyItemType _ ->
                    throw new IllegalStateException(
                            "Analysis should have prevented type: " + array +
                                    " from reaching analysis");
        };
    }

    private AntlrQueryItemType arrayPut(
            final ChoiceItemType choice,
            final AntlrQuerySequenceType position,
            final AntlrQuerySequenceType member) {

        final ConcreteItemType[] itemTypes =
                choice.itemTypes();

        final List<ConcreteItemType> result =
                new ArrayList<>(itemTypes.length);

        for (final ConcreteItemType itemType : itemTypes) {
            final AntlrQueryItemType analyzed =
                    arrayPut(
                            itemType,
                            position,
                            member);

            /*
             * This particular array alternative cannot satisfy
             * the requested position. It must be removed from
             * the choice, but must not make the whole result NEVER.
             */
            if (analyzed instanceof NeverType) {
                continue;
            }

            result.add((ConcreteItemType) analyzed);
        }

        /*
         * None of the possible array types can satisfy the
         * requested position.
         */
        if (result.isEmpty()) {
            return AntlrQueryItemType.NEVER;
        }

        /*
         * Only one alternative remains, so there is no need
         * to construct a ChoiceItemType.
         */
        if (result.size() == 1) {
            return result.getFirst();
        }

        return typeFactory.itemChoice(
                result.toArray(ConcreteItemType[]::new));
    }

    private AntlrQueryItemType arrayPut(
            final ConcreteItemType array,
            final AntlrQuerySequenceType position,
            final AntlrQuerySequenceType member) {

        return switch (array) {
            case ArrayLikeType arrayLike ->
                    arrayPut(arrayLike, position, member);

            default ->
                    throw new IllegalStateException(
                            "Expected array type, got: " + array);
        };
    }

    private AntlrQueryItemType arrayPut(
            final ArrayLikeType array,
            final AntlrQuerySequenceType position,
            final AntlrQuerySequenceType member) {

        return switch (array) {
            case ArrayType arrayType ->
                    arrayPut(arrayType, member);

            case TupleType tupleType ->
                    arrayPut(tupleType, position, member);
        };
    }

    private ConcreteItemType arrayPut(
            final ArrayType array,
            final AntlrQuerySequenceType member) {

        /*
         * array:put replaces an existing member.
         *
         * For a general array the number of members is unknown,
         * therefore the position cannot be proven to be outside
         * the array bounds.
         *
         * Replacing a member does not change the array cardinality.
         */
        final AntlrQuerySequenceType resultMember =
                Types.union(
                        typeFactory,
                        array.memberType(),
                        member);

        return (ConcreteItemType) typeFactory.itemArray(
                resultMember,
                array.cardinality());
    }

    private AntlrQueryItemType arrayPut(
            final TupleType tuple,
            final AntlrQuerySequenceType position,
            final AntlrQuerySequenceType member) {

        final NumericRange positionRange =
                positionRange(position);

        /*
         * Array indexing is 1-based.
         *
         * Ranges.integers(start, stop) uses an exclusive upper bound,
         * therefore a tuple with N members has valid positions:
         *
         *     1..N
         *
         * and must be represented as integers(1, N + 1).
         */
        final NumericRange validRange =
                Ranges.integers(
                        1,
                        tuple.members().length + 1);

        final @Nullable NumericRange validPositions =
                Ranges.intersection(
                        positionRange,
                        validRange);

        if (validPositions == null) {
            return AntlrQueryItemType.NEVER;
        }

        final @Nullable BigInteger knownPosition =
                knownPosition(validPositions);

        if (knownPosition != null) {
            return arrayPut(
                    tuple,
                    knownPosition,
                    member);
        }

        return arrayPutUnknownPosition(
                tuple,
                validPositions,
                member);
    }

    private ConcreteItemType arrayPut(
            final TupleType tuple,
            final BigInteger position,
            final AntlrQuerySequenceType member) {

        final AntlrQuerySequenceType[] members =
                tuple.members();

        final AntlrQuerySequenceType[] result =
                members.clone();

        /*
         * XQuery array positions are 1-based,
         * Java array positions are 0-based.
         */
        result[position.intValueExact() - 1] =
                member;

        return (ConcreteItemType) typeFactory.itemTuple(
                Arrays.asList(result));
    }

    private ConcreteItemType arrayPutUnknownPosition(
            final TupleType tuple,
            final NumericRange positions,
            final AntlrQuerySequenceType member) {

        final AntlrQuerySequenceType[] members =
                tuple.members();

        final AntlrQuerySequenceType[] result =
                members.clone();

        for (int i = 0; i < members.length; i++) {
            final BigInteger position =
                    BigInteger.valueOf(i + 1L);

            if (Ranges.contains(
                    positions,
                    new BigDecimal(position))) {

                result[i] = Types.union(
                        typeFactory,
                        result[i],
                        member);
            }
        }

        return (ConcreteItemType) typeFactory.itemTuple(
                Arrays.asList(result));
    }

    private NumericRange positionRange(
            final AntlrQuerySequenceType position) {

        if (!(position.itemType() instanceof NumberType(NumericRange range))) {
            throw new IllegalStateException(
                    "array:put position must be integer");
        }

        return Ranges.integers(range);
    }

    private @Nullable BigInteger knownPosition(
            final NumericRange positions) {

        final NumericRange.@Nullable Event min =
                Ranges.min(positions);

        if (min == null) {
            return null;
        }

        final NumericRange.@Nullable Event max =
                Ranges.max(positions);

        if (max == null) {
            return null;
        }

        if (!(min.value() instanceof NumericRange.FiniteBound minBound) ||
                !(max.value() instanceof NumericRange.FiniteBound maxBound)) {
            return null;
        }

        if (minBound.value().compareTo(maxBound.value()) != 0) {
            return null;
        }

        try {
            return minBound.value().toBigIntegerExact();
        } catch (ArithmeticException e) {
            return null;
        }
    }
}
