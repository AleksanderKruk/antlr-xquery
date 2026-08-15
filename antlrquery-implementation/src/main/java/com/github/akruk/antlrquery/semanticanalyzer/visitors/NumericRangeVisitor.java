package com.github.akruk.antlrquery.semanticanalyzer.visitors;

import com.github.akruk.antlrquery.AntlrQueryParser.*;
import com.github.akruk.antlrquery.AntlrQueryParserBaseVisitor;
import com.github.akruk.antlrquery.typesystem.typeoperations.cardinality.Ranges;
import com.github.akruk.antlrquery.typesystem.types.NumericRange;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.math.BigDecimal;


/**
 * CardinalityVisitor visits AntlrQuery parse tree to determine the cardinality of type
 */
public class NumericRangeVisitor
    extends AntlrQueryParserBaseVisitor<NumericRange>
{
    public NumericRangeVisitor() {
    }

    @Override
    public NumericRange visitNumericRangeSet(NumericRangeSetContext ctx) {
        var ranges = ctx.numericRangeTerm().stream().map(this::visitNumericRangeTerm).toArray(NumericRange[]::new);
        return Ranges.union(ranges);
    }

    @Override
    public NumericRange visitSingleNumberNumericRange(SingleNumberNumericRangeContext ctx) {
        return NumericRange.of(Integer.parseInt(ctx.IntegerLiteral().getText()));
    }

    @Override
    public NumericRange visitInclusiveRangeNumericRange(InclusiveRangeNumericRangeContext ctx) {
        return getNumericRange(ctx.IntegerLiteral(0), ctx.IntegerLiteral(1), true, true);
    }

    @Override
    public NumericRange visitMaximumCardinality(MaximumCardinalityContext ctx) {
        return getLessThanNumericRange(ctx.IntegerLiteral(), true);
    }

    @Override
    public NumericRange visitMinimumCardinality(MinimumCardinalityContext ctx) {
        return getGreaterThanNumericRange(ctx.IntegerLiteral(), true);
    }

    @Override
    public NumericRange visitGreaterThanNumericRange(GreaterThanNumericRangeContext ctx) {
        return getGreaterThanNumericRange(ctx.IntegerLiteral(), false);
    }

    @Override
    public NumericRange visitGreaterOrEqualNumericRange(GreaterOrEqualNumericRangeContext ctx) {
        return getGreaterThanNumericRange(ctx.IntegerLiteral(), true);
    }

    @Override
    public NumericRange visitLessThanNumericRange(LessThanNumericRangeContext ctx) {
        return getLessThanNumericRange(ctx.IntegerLiteral(), false);
    }

    @Override
    public NumericRange visitLessOrEqualNumericRange(LessOrEqualNumericRangeContext ctx) {
        return getLessThanNumericRange(ctx.IntegerLiteral(), true);
    }

    @Override
    public NumericRange visitLeftOpenRangeNumericRange(LeftOpenRangeNumericRangeContext ctx) {
        return getNumericRange(ctx.IntegerLiteral(0), ctx.IntegerLiteral(1), false, true);
    }

    @Override
    public NumericRange visitRightOpenRangeNumericRange(RightOpenRangeNumericRangeContext ctx) {
        return getNumericRange(ctx.IntegerLiteral(0), ctx.IntegerLiteral(1), true, false);
    }

    @Override
    public NumericRange visitClosedRangeLessEqualNumericRange(ClosedRangeLessEqualNumericRangeContext ctx) {
        return getNumericRange(ctx.IntegerLiteral(0), ctx.IntegerLiteral(1), true, true);
    }

    @Override
    public NumericRange visitClosedRangeGreaterEqualNumericRange(ClosedRangeGreaterEqualNumericRangeContext ctx) {
        return getNumericRange(ctx.IntegerLiteral(1), ctx.IntegerLiteral(0), true, true);
    }


    @Override
    public NumericRange visitOpenRangeLessThanNumericRange(OpenRangeLessThanNumericRangeContext ctx) {
        return getNumericRange(ctx.IntegerLiteral(0), ctx.IntegerLiteral(1), false, false);
    }

    @Override
    public NumericRange visitOpenRangeGreaterThanNumericRange(OpenRangeGreaterThanNumericRangeContext ctx) {
        return getNumericRange(ctx.IntegerLiteral(1), ctx.IntegerLiteral(0), false, false);
    }


    private static @NonNull NumericRange getNumericRange(TerminalNode ctx, TerminalNode ctx1, boolean fromInclusive, boolean toInclusive) {
        var from = Long.parseLong(ctx.getText());
        var to = Long.parseLong(ctx1.getText());
        return NumericRange.of(
                new NumericRange.Event(new NumericRange.FiniteBound(BigDecimal.valueOf(from), fromInclusive), NumericRange.Type.START),
                new NumericRange.Event(new NumericRange.FiniteBound(BigDecimal.valueOf(to), toInclusive), NumericRange.Type.END)
        );
    }

    private static @NonNull NumericRange getGreaterThanNumericRange(TerminalNode ctx, boolean inclusive) {
        var from = Long.parseLong(ctx.getText());
        return NumericRange.of(
                new NumericRange.Event(new NumericRange.FiniteBound(BigDecimal.valueOf(from), inclusive), NumericRange.Type.START),
                new NumericRange.Event(NumericRange.NegativeInfinity.POSITIVE_INFINITY, NumericRange.Type.END)
        );
    }

    private static @NonNull NumericRange getLessThanNumericRange(TerminalNode ctx, boolean inclusive) {
        var to = Long.parseLong(ctx.getText());
        return NumericRange.of(
                new NumericRange.Event(NumericRange.NegativeInfinity.NEGATIVE_INFINITY, NumericRange.Type.START),
                new NumericRange.Event(new NumericRange.FiniteBound(BigDecimal.valueOf(to), inclusive), NumericRange.Type.END)
        );
    }


}
