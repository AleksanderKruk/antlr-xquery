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
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.ArrayLikeType.ArrayType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.ArrayLikeType.TupleType;
import org.checkerframework.framework.qual.DefaultQualifier;


@DefaultQualifier(NonNull.class)
public class ArrayTailGranularAnalysis
        implements SemanticSymbolManager.GrainedFunctionCallAnalysis {

    private final AntlrQueryTypeFactory typeFactory;

    public ArrayTailGranularAnalysis(
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
                arrayTail(array.itemType()));
    }

    private AntlrQueryItemType arrayTail(
            final AntlrQueryItemType array) {

        return switch (array) {
            case ChoiceItemType choice ->
                    arrayTail(choice);

            case ConcreteItemType concrete ->
                    arrayTail(concrete);

            case NamedItemType(
                    NamespaceResolver.QualifiedName reference) ->
                    arrayTail(
                            typeFactory.guaranteedItemNamedType(
                                    reference,
                                    new IllegalStateException()));

            case NeverType _, NothingType _, AnyItemType _ ->
                    throw new IllegalStateException(
                            "Analysis should have prevented type: " +
                                    array);
        };
    }

    private AntlrQueryItemType arrayTail(
            final ChoiceItemType choice) {

        final ConcreteItemType[] itemTypes =
                choice.itemTypes();

        final ConcreteItemType[] result =
                new ConcreteItemType[itemTypes.length];

        for (int i = 0; i < itemTypes.length; i++) {
            result[i] = arrayTail(itemTypes[i]);
        }

        return typeFactory.itemChoice(result);
    }

    private ConcreteItemType arrayTail(
            final ConcreteItemType array) {

        return switch (array) {
            case ArrayLikeType arrayLike ->
                    arrayTail(arrayLike);

            default ->
                    throw new IllegalStateException(
                            "Expected array type, got: " + array);
        };
    }

    private ConcreteItemType arrayTail(
            final ArrayLikeType array) {

        return switch (array) {
            case ArrayType arrayType ->
                    arrayTail(arrayType);

            case TupleType tupleType ->
                    arrayTail(tupleType);
        };
    }

    private ConcreteItemType arrayTail(
            final ArrayType array) {

        /*
         * tail() removes exactly one member.
         *
         * The member type remains unchanged, while the
         * cardinality is reduced by one.
         */
        final Cardinality cardinality =
                Objects.requireNonNull(Cardinalities.subtract(
                        array.cardinality(),
                        Cardinality.ONE));

        return (ConcreteItemType) typeFactory.itemArray(
                array.memberType(),
                cardinality);
    }

    private ConcreteItemType arrayTail(
            final TupleType tuple) {

        final AntlrQuerySequenceType[] members =
                tuple.members();

        /*
         * The argument is guaranteed to be non-empty by the
         * basic semantic analysis, so there is no empty-tuple
         * case to handle here.
         */
        return (ConcreteItemType) typeFactory.itemTuple(
                Arrays.asList(
                        Arrays.copyOfRange(
                                members,
                                1,
                                members.length)));
    }
}
