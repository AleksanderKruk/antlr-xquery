package com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.granularanalysis;

import java.util.List;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.semanticanalyzer.VisitingSemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticcontext.AntlrQuerySemanticContext;
import com.github.akruk.antlrquery.semanticanalyzer.semanticfunctioncaller.SemanticSymbolManager;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.ArrayLikeType.ArrayType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.ArrayLikeType.TupleType;
import org.antlr.v4.runtime.tree.ParseTree;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class ArrayHeadGranularAnalysis
        implements SemanticSymbolManager.GrainedFunctionCallAnalysis {

    private final AntlrQueryTypeFactory typeFactory;

    public ArrayHeadGranularAnalysis(
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

        return arrayHead(array.itemType());
    }

    private AntlrQuerySequenceType arrayHead(
            final AntlrQueryItemType array) {

        return switch (array) {
            case ChoiceItemType choice ->
                    arrayHead(choice);

            case ConcreteItemType concrete ->
                    arrayHead(concrete);

            case NamedItemType(
                    NamespaceResolver.QualifiedName reference) ->
                    arrayHead(
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

    private AntlrQuerySequenceType arrayHead(
            final ChoiceItemType choice) {

        AntlrQuerySequenceType result =
                typeFactory.emptySequence();

        for (final ConcreteItemType itemType :
                choice.itemTypes()) {

            result = Types.union(
                    typeFactory,
                    result,
                    arrayHead(itemType));
        }

        return result;
    }

    private AntlrQuerySequenceType arrayHead(
            final ConcreteItemType array) {

        return switch (array) {
            case ArrayLikeType arrayLike ->
                    arrayHead(arrayLike);

            default ->
                    throw new IllegalStateException(
                            "Expected array type, got: " + array);
        };
    }

    private AntlrQuerySequenceType arrayHead(
            final ArrayLikeType array) {

        return switch (array) {
            case ArrayType arrayType ->
                    arrayHead(arrayType);

            case TupleType tupleType ->
                    arrayHead(tupleType);
        };
    }

    private AntlrQuerySequenceType arrayHead(
            final ArrayType array) {

        /*
         * For a general array the first member has the same
         * type and cardinality as any other member.
         */
        return array.memberType();
    }

    private AntlrQuerySequenceType arrayHead(
            final TupleType tuple) {

        /*
         * An empty tuple has no head.
         */
        if (tuple.members().length == 0) {
            return typeFactory.emptySequence();
        }

        return tuple.members()[0];
    }
}
