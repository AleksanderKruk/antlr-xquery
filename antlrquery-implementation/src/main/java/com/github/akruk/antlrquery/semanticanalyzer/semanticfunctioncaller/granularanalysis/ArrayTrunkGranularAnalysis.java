package com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.granularanalysis;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.semanticanalyzer.VisitingSemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticSymbolManager;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.TypeInContext;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.ArrayLikeType.ArrayType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.ArrayLikeType.TupleType;
import org.antlr.v4.runtime.tree.ParseTree;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class ArrayTrunkGranularAnalysis
        implements SemanticSymbolManager.GrainedAnalysis {

    private final AntlrQueryTypeFactory typeFactory;

    public ArrayTrunkGranularAnalysis(
            final AntlrQueryTypeFactory typeFactory) {

        this.typeFactory = typeFactory;
    }

    @Override
    public TypeInContext analyze(
            final List<SemanticSymbolManager.UsedArg> args,
            final @Nullable VisitingSemanticContext context,
            final @Nullable ParseTree functionBody,
            final AntlrQuerySemanticContext typeContext) {

        final AntlrQuerySequenceType array =
                args.get(0).type().type;

        return typeContext.typeInContext(
                analyze(array));
    }

    private AntlrQuerySequenceType analyze(
            final AntlrQuerySequenceType array) {

        return typeFactory.one(
                arrayTrunk(array.itemType()));
    }

    private AntlrQueryItemType arrayTrunk(
            final AntlrQueryItemType array) {

        return switch (array) {
            case ChoiceItemType choice ->
                    arrayTrunk(choice);

            case ConcreteItemType concrete ->
                    arrayTrunk(concrete);

            case NamedItemType(
                    NamespaceResolver.QualifiedName reference) ->
                    arrayTrunk(
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

    private AntlrQueryItemType arrayTrunk(
            final ChoiceItemType choice) {

        final ConcreteItemType[] itemTypes =
                choice.itemTypes();

        final ConcreteItemType[] result =
                new ConcreteItemType[itemTypes.length];

        for (int i = 0; i < itemTypes.length; i++) {
            final AntlrQueryItemType trunk =
                    arrayTrunk(itemTypes[i]);

            if (trunk instanceof NeverType) {
                return trunk;
            }

            result[i] = (ConcreteItemType) trunk;
        }

        return typeFactory.itemChoice(result);
    }

    private AntlrQueryItemType arrayTrunk(
            final ConcreteItemType array) {

        return switch (array) {
            case ArrayLikeType arrayLike ->
                    arrayTrunk(arrayLike);

            default ->
                    throw new IllegalStateException(
                            "Expected array type, got: " + array);
        };
    }

    private AntlrQueryItemType arrayTrunk(
            final ArrayLikeType array) {

        return switch (array) {
            case ArrayType arrayType ->
                    arrayTrunk(arrayType);

            case TupleType tupleType ->
                    arrayTrunk(tupleType);
        };
    }

    private AntlrQueryItemType arrayTrunk(
            final ArrayType array) {

        /*
         * The array contains at least one member.
         *
         * Removing the last member therefore decreases the
         * cardinality by exactly one while preserving the
         * member type.
         *
         * For example:
         *
         *   array(number, 1..∞)
         *
         * becomes:
         *
         *   array(number, 0..∞)
         */
        return typeFactory.itemArray(
                array.memberType(),
                Objects.requireNonNull(Cardinalities.remove(array.cardinality(), Cardinality.ONE)));
    }

    private AntlrQueryItemType arrayTrunk(
            final TupleType tuple) {

        final AntlrQuerySequenceType[] members =
                tuple.members();

        /*
         * A one-member tuple becomes an empty array.
         */
        if (members.length == 1) {
            return typeFactory.itemTuple(
                    List.of());
        }

        return typeFactory.itemTuple(
                Arrays.asList(
                        Arrays.copyOf(
                                members,
                                members.length - 1)));
    }
}
