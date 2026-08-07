package com.github.akruk.antlrquery.semanticanalyzer.visitors;

import com.github.akruk.antlrquery.AntlrQueryParser;
import com.github.akruk.antlrquery.AntlrQueryParserBaseVisitor;
import com.github.akruk.antlrquery.AntlrQueryParser.EmptySequenceTypeContext;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.Types;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.AntlrQueryItemType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;


@DefaultQualifier(NonNull.class)
public class TypeVisitor
    extends AntlrQueryParserBaseVisitor<AntlrQuerySequenceType>
{


    private final AntlrQueryTypeFactory typeFactory;
    private final CardinalityVisitor cardinalityVisitor;
    private final ItemTypeVisitor itemTypeVisitor;

    public TypeVisitor(
        AntlrQueryTypeFactory factory, 
        CardinalityVisitor cardinalityVisitor,
        ItemTypeVisitor itemTypeVisitor
    ) 
    {
        this.typeFactory = factory;
        this.cardinalityVisitor = cardinalityVisitor;
        this.itemTypeVisitor = itemTypeVisitor;
    }


    @Override
    public AntlrQuerySequenceType visitEmptySequenceType(EmptySequenceTypeContext ctx) {
        return typeFactory.emptySequence();
    }

    @Override
    public AntlrQuerySequenceType visitConstrainedSequenceType(AntlrQueryParser.ConstrainedSequenceTypeContext ctx) {
        final AntlrQueryItemType it = ctx.itemType().accept(itemTypeVisitor);
        final Cardinality c = ctx.cardinality().accept(cardinalityVisitor);
        return typeFactory.sequence(it, c);
    }

    @Override
    public AntlrQuerySequenceType visitExtensibleType(AntlrQueryParser.ExtensibleTypeContext ctx) {
        if (ctx.itemType() == null) {
            return typeFactory.any();
        }
        var it = itemTypeVisitor.visitItemType(ctx.itemType());
        return typeFactory.zeroOrMore(it);
    }


    @Override
    public AntlrQuerySequenceType visitSequenceTypeUnion(AntlrQueryParser.SequenceTypeUnionContext ctx) {
        AntlrQuerySequenceType[] types = ctx.sequenceTypeIntersection()
                .stream()
                .map(this::visitSequenceTypeIntersection)
                .toArray(AntlrQuerySequenceType[]::new);
        return Types.union(typeFactory, types);
    }

    @Override
    public AntlrQuerySequenceType visitSequenceTypeIntersection(AntlrQueryParser.SequenceTypeIntersectionContext ctx) {
        AntlrQuerySequenceType[] types = ctx.sequenceTypeSubtraction()
                .stream()
                .map(this::visitSequenceTypeSubtraction)
                .toArray(AntlrQuerySequenceType[]::new);
        return Types.intersect(typeFactory, types);
    }

    @Override
    public AntlrQuerySequenceType visitSequenceTypeSubtraction(AntlrQueryParser.SequenceTypeSubtractionContext ctx) {
        AntlrQuerySequenceType[] types = ctx.arrayTypeOperator()
                .stream()
                .map(this::visitArrayTypeOperator)
                .toArray(AntlrQuerySequenceType[]::new);
        return Types.subtract(typeFactory, types);
    }

    @Override
    public AntlrQuerySequenceType visitNeverType(AntlrQueryParser.NeverTypeContext ctx) {
        return typeFactory.neverType();
    }
}
