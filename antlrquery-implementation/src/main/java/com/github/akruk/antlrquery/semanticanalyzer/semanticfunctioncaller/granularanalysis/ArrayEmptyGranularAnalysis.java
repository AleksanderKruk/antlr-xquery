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
import java.util.List;


@DefaultQualifier(NonNull.class)
public class ArrayEmptyGranularAnalysis
        implements SemanticSymbolManager.GrainedFunctionCallAnalysis
{

    private final AntlrQueryTypeFactory typeFactory;

    public ArrayEmptyGranularAnalysis(final AntlrQueryTypeFactory typeFactory)
    {
        this.typeFactory = typeFactory;
    }

    @Override
    public SemanticSymbolManager.FunctionCallAnalysis analyze(
            List<SemanticSymbolManager.UsedArg> args,
            VisitingSemanticContext context,
            ParseTree functionBody,
            AntlrQuerySemanticContext typeContext)
    {
        final AntlrQuerySequenceType arrayType = args.getFirst().type().type;
        final AntlrQueryItemType result = arrayEmpty(arrayType.itemType());
        return SemanticSymbolManager.FunctionCallAnalysis.typeOnly(
                typeContext.typeInContext(
                        typeFactory.sequence(result, Cardinality.ONE)));
    }

    private AntlrQueryItemType arrayEmpty(
            final AntlrQueryItemType itemType) {

        return switch (itemType) {
            case ChoiceItemType choice -> arrayEmpty(choice);
            case ConcreteItemType concrete -> arrayEmpty(concrete);
            case NamedItemType named -> arrayEmpty(
                            typeFactory.guaranteedItemNamedType(
                                    named.reference(),
                                    new IllegalStateException()));

            case NeverType _, NothingType _, AnyItemType _ ->
                    throw new IllegalStateException(
                            "Analysis should have prevented type: " + itemType);
        };
    }

    private AntlrQueryItemType arrayEmpty(
            final ChoiceItemType choice) {

        boolean canBeTrue = false;
        boolean canBeFalse = false;

        for (final ConcreteItemType itemType : choice.itemTypes()) {
            final AntlrQueryItemType result = arrayEmpty(itemType);

            switch (result) {
                case BooleanType.True _ ->
                        canBeTrue = true;

                case BooleanType.False _ ->
                        canBeFalse = true;

                case BooleanType.Boolean _ -> {
                    canBeTrue = true;
                    canBeFalse = true;
                }

                default ->
                        throw new IllegalStateException(
                                "Expected boolean type, got: " + result);
            }

            if (canBeTrue && canBeFalse)
                return typeFactory.itemBoolean();
        }

        return canBeTrue
                ? typeFactory.itemTrue()
                : typeFactory.itemFalse();
    }

    private AntlrQueryItemType arrayEmpty(
            final ConcreteItemType itemType) {

        return switch (itemType) {
            case ArrayLikeType array ->
                    arrayEmpty(array);

            default ->
                    throw new IllegalStateException(
                            "Expected array-like type, got: " + itemType);
        };
    }

    private AntlrQueryItemType arrayEmpty(
            final ArrayLikeType array) {

        return switch (array) {
            case ArrayLikeType.ArrayType(
                    _,
                    Cardinality cardinality) ->

                    Cardinalities.contains(cardinality, BigInteger.ZERO)
                            ? typeFactory.itemTrue()
                            : typeFactory.itemFalse();

            case ArrayLikeType.TupleType(
                    AntlrQuerySequenceType[] members) ->

                    members.length == 0
                            ? typeFactory.itemTrue()
                            : typeFactory.itemFalse();
        };
    }

}
