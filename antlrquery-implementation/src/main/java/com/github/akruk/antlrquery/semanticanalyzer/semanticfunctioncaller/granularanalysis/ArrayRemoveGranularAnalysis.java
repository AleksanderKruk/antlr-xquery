package com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.granularanalysis;

import com.github.akruk.antlrquery.semanticanalyzer.VisitingSemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticSymbolManager;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@DefaultQualifier(NonNull.class)
public class ArrayRemoveGranularAnalysis
        implements SemanticSymbolManager.GrainedFunctionCallAnalysis
{


    private final AntlrQueryTypeFactory typeFactory;

    public ArrayRemoveGranularAnalysis(
            AntlrQueryTypeFactory typeFactory) {

        this.typeFactory = typeFactory;
    }

    @Override
    public SemanticSymbolManager.FunctionCallAnalysis analyze(
            List<SemanticSymbolManager.UsedArg> args,
            VisitingSemanticContext context,
            ParseTree functionBody,
            AntlrQuerySemanticContext typeContext)
    {
        final AntlrQuerySequenceType arrayType = args.get(0).type().type;
        final AntlrQuerySequenceType positionsType = args.get(1).type().type;

        final AntlrQueryItemType itemType =
                arrayRemove(arrayType.itemType(), positionsType);

        return SemanticSymbolManager.FunctionCallAnalysis.typeOnly(
                typeContext.typeInContext(
                    typeFactory.sequence(
                            itemType,
                            Objects.requireNonNull(Cardinalities.remove(
                                    arrayType.cardinality(),
                                    positionsType.cardinality())))));
    }


    private AntlrQueryItemType arrayRemove(
            final AntlrQueryItemType itemType,
            final AntlrQuerySequenceType positionsType) {

        return switch (itemType) {
            case ChoiceItemType choice ->
                    arrayRemove(choice, positionsType);

            case ConcreteItemType concrete ->
                    arrayRemove(concrete, positionsType);

            case NamedItemType named ->
                    arrayRemove(
                            typeFactory.guaranteedItemNamedType(
                                    named.reference(),
                                    new IllegalStateException()),
                            positionsType);

            case NeverType _, NothingType _, AnyItemType _ ->
                    throw new IllegalStateException(
                            "Analysis should have prevented type: " + itemType);
        };
    }

    private AntlrQueryItemType arrayRemove(
            final ChoiceItemType choice,
            final AntlrQuerySequenceType positionsType) {

        return typeFactory.itemChoice(
                Arrays.stream(choice.itemTypes())
                        .map(item -> arrayRemove(item, positionsType))
                        .toArray(AntlrQueryItemType[]::new));
    }

    private AntlrQueryItemType arrayRemove(
            final ConcreteItemType concrete,
            final AntlrQuerySequenceType positionsType) {

        return switch (concrete) {
            case ArrayLikeType array ->
                    arrayRemove(array, positionsType);

            default ->
                    throw new IllegalStateException(
                            "Expected array-like type, got: " + concrete);
        };
    }

    private AntlrQueryItemType arrayRemove(
            final ArrayLikeType array,
            final AntlrQuerySequenceType positionsType) {

        return switch (array) {
            case ArrayLikeType.ArrayType(
                    AntlrQuerySequenceType memberType,
                    Cardinality cardinality) ->

                    typeFactory.itemArray(
                            memberType,
                            arrayRemove(cardinality, positionsType));

            case ArrayLikeType.TupleType tuple ->
                    arrayRemove(tuple, positionsType);
        };
    }

    private AntlrQueryItemType arrayRemove(
            final ArrayLikeType.TupleType tuple,
            final AntlrQuerySequenceType positionsType) {

        final AntlrQuerySequenceType[] members = tuple.members();

        if (members.length == 0)
            return tuple;

        final List<AntlrQuerySequenceType> result =
                new ArrayList<>(members.length);

        for (int i = 0; i < members.length; i++) {
            final BigInteger position = BigInteger.valueOf(i + 1);

            if (!isRemovedPosition(position, positionsType))
                result.add(members[i]);
        }

        return typeFactory.itemTuple(result);
    }

    private boolean isRemovedPosition(
            final BigInteger position,
            final AntlrQuerySequenceType positionsType) {

        return positionsType.itemType() instanceof AtomicType
                && Cardinalities.contains(
                positionsType.cardinality(),
                position);
    }

    private Cardinality arrayRemove(
            final Cardinality cardinality,
            final AntlrQuerySequenceType positionsType) {

        return Objects.requireNonNull(Cardinalities.subtract(
                cardinality,
                positionsType.cardinality()));
    }

}
