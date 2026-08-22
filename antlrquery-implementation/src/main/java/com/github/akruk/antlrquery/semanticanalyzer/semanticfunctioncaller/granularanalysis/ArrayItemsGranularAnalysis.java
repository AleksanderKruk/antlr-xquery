package com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.granularanalysis;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.semanticanalyzer.VisitingSemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticSymbolManager;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.ItemTypes;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;

@DefaultQualifier(NonNull.class)
public class ArrayItemsGranularAnalysis
        implements SemanticSymbolManager.GrainedFunctionCallAnalysis
{

    private final AntlrQueryTypeFactory typeFactory;

    public ArrayItemsGranularAnalysis(
            final AntlrQueryTypeFactory typeFactory) {

        this.typeFactory = typeFactory;
    }

    @Override
    public SemanticSymbolManager.FunctionCallAnalysis analyze(
            final List<SemanticSymbolManager.UsedArg> args,
            final @Nullable VisitingSemanticContext context,
            final @Nullable ParseTree functionBody,
            final AntlrQuerySemanticContext typeContext) {

        final AntlrQuerySequenceType array =
                args.getFirst().type().type;

        return SemanticSymbolManager.FunctionCallAnalysis.typeOnly(typeContext.typeInContext(analyze(array)));
    }

    private AntlrQuerySequenceType analyze(
            final AntlrQuerySequenceType array) {

        return arrayItems(array.itemType());
    }

    private AntlrQuerySequenceType arrayItems(
            final AntlrQueryItemType array) {

        return switch (array) {
            case ChoiceItemType choice ->
                    arrayItems(choice);

            case ConcreteItemType concrete ->
                    arrayItems(concrete);

            case NamedItemType(
                    NamespaceResolver.QualifiedName reference) ->
                    arrayItems(
                            typeFactory.guaranteedItemNamedType(
                                    reference,
                                    new IllegalStateException()));

            case NeverType _, NothingType _, AnyItemType _ ->
                    throw new IllegalStateException(
                            "Analysis should have prevented type: " +
                                    array +
                                    " from reaching analysis");
        };
    }

    private AntlrQuerySequenceType arrayItems(
            final ChoiceItemType choice) {

        AntlrQuerySequenceType result =
                typeFactory.emptySequence();

        for (final ConcreteItemType itemType : choice.itemTypes()) {
            result = Types.union(
                    typeFactory,
                    result,
                    arrayItems(itemType));
        }

        return result;
    }

    private AntlrQuerySequenceType arrayItems(
            final ConcreteItemType array) {

        return switch (array) {
            case ArrayLikeType arrayLike ->
                    arrayItems(arrayLike);

            default ->
                    throw new IllegalStateException(
                            "Expected array type, got: " + array);
        };
    }

    private AntlrQuerySequenceType arrayItems(
            final ArrayLikeType array) {

        return switch (array) {
            case ArrayLikeType.ArrayType arrayType ->
                    arrayItems(arrayType);

            case ArrayLikeType.TupleType tupleType ->
                    arrayItems(tupleType);
        };
    }

    private AntlrQuerySequenceType arrayItems(
            final ArrayLikeType.ArrayType array) {

        final AntlrQuerySequenceType member =
                array.memberType();

        final Cardinality cardinality =
                Cardinalities.multiply(
                        array.cardinality(),
                        member.cardinality());

        return typeFactory.sequence(
                member.itemType(),
                cardinality);
    }

    private AntlrQuerySequenceType arrayItems(
            final ArrayLikeType.TupleType tuple) {

        AntlrQueryItemType itemType =
                AntlrQueryItemType.NOTHING;

        Cardinality cardinality =
                Cardinality.ZERO;

        for (final AntlrQuerySequenceType member :
                tuple.members()) {

            itemType = ItemTypes.union(
                    typeFactory,
                    itemType,
                    member.itemType());

            cardinality =
                    Cardinalities.add(
                            cardinality,
                            member.cardinality());
        }

        return typeFactory.sequence(
                itemType,
                cardinality);
    }
}
