package com.github.akruk.antlrquery.semanticanalyzer.visitors;

import com.github.akruk.antlrquery.AntlrQueryParser.*;
import com.github.akruk.antlrquery.AntlrQueryParserBaseVisitor;
import com.github.akruk.antlrquery.typesystem.factories.CardinalityFactory;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Cardinalities;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;

import java.math.BigInteger;


/**
 * CardinalityVisitor visits AntlrQuery parse tree to determine the cardinality of type
 */
public class NumericRangeVisitor
    extends AntlrQueryParserBaseVisitor<NumericRange>
{
    private CardinalityFactory cardinalityFactory;

    public NumericRangeVisitor(CardinalityFactory factory) {
        this.cardinalityFactory = factory;
    }

}
