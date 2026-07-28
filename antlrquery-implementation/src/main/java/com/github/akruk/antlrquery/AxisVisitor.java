package com.github.akruk.antlrquery;

public class AxisVisitor 
    extends AntlrXqueryParserBaseVisitor<AntlrQueryAxis>
{
    @Override
    public AntlrQueryAxis visitChildAxis(AntlrXqueryParser.ChildAxisContext ctx) {
        return AntlrQueryAxis.CHILD;
    }

    @Override
    public AntlrQueryAxis visitDescendantAxis(AntlrXqueryParser.DescendantAxisContext ctx) {
        return AntlrQueryAxis.DESCENDANT;
    }

    @Override
    public AntlrQueryAxis visitSelfAxis(AntlrXqueryParser.SelfAxisContext ctx) {
        return AntlrQueryAxis.SELF;
    }

    @Override
    public AntlrQueryAxis visitDescendantOrSelfAxis(AntlrXqueryParser.DescendantOrSelfAxisContext ctx) {
        return AntlrQueryAxis.DESCENDANT_OR_SELF;
    }

    @Override
    public AntlrQueryAxis visitFollowingSiblingAxis(AntlrXqueryParser.FollowingSiblingAxisContext ctx) {
        return AntlrQueryAxis.FOLLOWING_SIBLING;
    }

    @Override
    public AntlrQueryAxis visitFollowingAxis(AntlrXqueryParser.FollowingAxisContext ctx) {
        return AntlrQueryAxis.FOLLOWING;
    }

    @Override
    public AntlrQueryAxis visitFollowingSiblingOrSelfAxis(AntlrXqueryParser.FollowingSiblingOrSelfAxisContext ctx) {
        return AntlrQueryAxis.FOLLOWING_SIBLING_OR_SELF;
    }

    @Override
    public AntlrQueryAxis visitFollowingOrSelfAxis(AntlrXqueryParser.FollowingOrSelfAxisContext ctx) {
        return AntlrQueryAxis.FOLLOWING_OR_SELF;
    }

    @Override
    public AntlrQueryAxis visitParentAxis(AntlrXqueryParser.ParentAxisContext ctx) {
        return AntlrQueryAxis.PARENT;
    }

    @Override
    public AntlrQueryAxis visitPrecedingSiblingOrSelfAxis(AntlrXqueryParser.PrecedingSiblingOrSelfAxisContext ctx) {
        return AntlrQueryAxis.PRECEDING_SIBLING_OR_SELF;
    }

    @Override
    public AntlrQueryAxis visitPrecedingOrSelfAxis(AntlrXqueryParser.PrecedingOrSelfAxisContext ctx) {
        return AntlrQueryAxis.PRECEDING_OR_SELF;
    }

    @Override
    public AntlrQueryAxis visitAncestorAxis(AntlrXqueryParser.AncestorAxisContext ctx) {
        return AntlrQueryAxis.ANCESTOR;
    }

    @Override
    public AntlrQueryAxis visitPrecedingSiblingAxis(AntlrXqueryParser.PrecedingSiblingAxisContext ctx) {
        return AntlrQueryAxis.PRECEDING_SIBLING;
    }

    @Override
    public AntlrQueryAxis visitPrecedingAxis(AntlrXqueryParser.PrecedingAxisContext ctx) {
        return AntlrQueryAxis.PRECEDING;
    }

    @Override
    public AntlrQueryAxis visitAncestorOrSelfAxis(AntlrXqueryParser.AncestorOrSelfAxisContext ctx) {
        return AntlrQueryAxis.ANCESTOR_OR_SELF;
    }
}
