package com.github.akruk.antlrquery.semanticanalyzer.visitors;

import java.math.BigInteger;

import com.github.akruk.antlrquery.AntlrXqueryParserBaseVisitor;
import com.github.akruk.antlrquery.AntlrXqueryParser.*;
import com.github.akruk.antlrquery.typesystem.factories.CardinalityFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;


/**
 * CardinalityVisitor visits AntlrQuery parse tree to determine the cardinality of type
 */
public class CardinalityVisitor 
    extends AntlrXqueryParserBaseVisitor<Cardinality> 
{
    private CardinalityFactory cardinalityFactory;

    public CardinalityVisitor(CardinalityFactory factory) {
        this.cardinalityFactory = factory;
    }
    
    @Override
    public Cardinality visitOneOrMoreCardinality(OneOrMoreCardinalityContext ctx) {
        return cardinalityFactory.oneOrMore();
    }
    
    @Override
    public Cardinality visitZeroOrMoreCardinality(ZeroOrMoreCardinalityContext ctx) {
        return cardinalityFactory.zeroOrMore();
    }

    @Override
    public Cardinality visitZeroOrOneCardinality(ZeroOrOneCardinalityContext ctx) {
        return cardinalityFactory.zeroOrOne();
    }

    @Override
    public Cardinality visitExactlyOneCardinality(ExactlyOneCardinalityContext ctx) {
        return cardinalityFactory.exactlyOne();
    }


    @Override
    public Cardinality visitEmptySequenceType(EmptySequenceTypeContext ctx) {
        return cardinalityFactory.empty();
    }

    @Override
    public Cardinality visitSingleNumberCardinality(SingleNumberCardinalityContext ctx) {
        BigInteger v = new BigInteger(ctx.IntegerLiteral().getText());
        return cardinalityFactory.singleNumber(v);
    }

    @Override
    public Cardinality visitInclusiveRangeCardinality(InclusiveRangeCardinalityContext ctx) {
        final BigInteger min = new BigInteger(ctx.IntegerLiteral(0).getText());
        final BigInteger max = new BigInteger(ctx.IntegerLiteral(1).getText());
        return cardinalityFactory.inclusiveRange(min, max);
    }

    @Override
    public Cardinality visitMinimumCardinality(MinimumCardinalityContext ctx) {
        BigInteger min = new BigInteger(ctx.IntegerLiteral().getText());
        return cardinalityFactory.minimum(min);
    }

    @Override
    public Cardinality visitMaximumCardinality(MaximumCardinalityContext ctx) {
        BigInteger max = new BigInteger(ctx.IntegerLiteral().getText());
        return cardinalityFactory.maximum(max);
    }
    
    @Override
    public Cardinality visitCardinalitySet(CardinalitySetContext ctx) {
        Cardinality[] cardinalities = ctx.cardinalityTerm().stream().map(this::visit).toArray(Cardinality[]::new);
        return Cardinalities.union(cardinalities);
    }


}
