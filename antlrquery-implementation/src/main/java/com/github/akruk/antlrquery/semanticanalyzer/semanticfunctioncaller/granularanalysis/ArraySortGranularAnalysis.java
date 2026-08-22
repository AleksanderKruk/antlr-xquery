package com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.granularanalysis;

import java.util.List;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.semanticanalyzer.VisitingSemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticSymbolManager;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.ArrayLikeType.ArrayType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.ArrayLikeType.TupleType;
import org.antlr.v4.runtime.tree.ParseTree;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;


@DefaultQualifier(NonNull.class)
public class ArraySortGranularAnalysis
        implements SemanticSymbolManager.GrainedFunctionCallAnalysis {

    private final AntlrQueryTypeFactory typeFactory;

    public ArraySortGranularAnalysis(
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

        return typeFactory.one(
                arraySort(array.itemType()));
    }

    private AntlrQueryItemType arraySort(
            final AntlrQueryItemType array) {

        return switch (array) {
            case ChoiceItemType choice -> arraySort(choice);
            case ConcreteItemType concrete -> arraySort(concrete);
            case NamedItemType(NamespaceResolver.QualifiedName reference) ->
                    arraySort(
                            typeFactory.guaranteedItemNamedType(
                                    reference,
                                    new IllegalStateException()));

            case NeverType _, NothingType _, AnyItemType _ ->
                    throw new IllegalStateException(
                            "Analysis should have prevented type: " +
                                    array);
        };
    }

    private AntlrQueryItemType arraySort(
            final ChoiceItemType choice) {

        final ConcreteItemType[] itemTypes =
                choice.itemTypes();

        final ConcreteItemType[] result =
                new ConcreteItemType[itemTypes.length];

        for (int i = 0; i < itemTypes.length; i++) {
            result[i] = arraySort(
                    itemTypes[i]);
        }

        return typeFactory.itemChoice(result);
    }

    private ConcreteItemType arraySort(
            final ConcreteItemType array) {

        return switch (array) {
            case ArrayLikeType arrayLike ->
                    arraySort(arrayLike);

            default ->
                    throw new IllegalStateException(
                            "Expected array type, got: " + array);
        };
    }

    private ConcreteItemType arraySort(
            final ArrayLikeType array) {

        return switch (array) {
            case ArrayType arrayType ->
                    arraySort(arrayType);

            case TupleType tupleType ->
                    arraySort(tupleType);
        };
    }

    private ConcreteItemType arraySort(
            final ArrayType array) {

        /*
         * Sorting changes the order of members, but does not
         * change their type or cardinality.
         *
         * An ArrayType already has no positional information,
         * therefore its type is unchanged.
         */
        return array;
    }

    private ConcreteItemType arraySort(
            final TupleType tuple) {

        final AntlrQuerySequenceType[] members =
                tuple.members();

        /*
         * Sorting destroys the positional information of a
         * concrete tuple.
         *
         * Every member can occur at every position, therefore
         * the resulting array has the union of all member types.
         */
        AntlrQuerySequenceType memberType =
                typeFactory.neverType();

        for (AntlrQuerySequenceType member : members) {
            memberType = Types.union(
                    typeFactory,
                    memberType,
                    member);
        }

        return (ConcreteItemType) typeFactory.itemArray(
                memberType,
                Cardinality.of(members.length));
    }
}
