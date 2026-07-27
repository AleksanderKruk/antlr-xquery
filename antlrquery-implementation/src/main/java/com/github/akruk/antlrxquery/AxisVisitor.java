package com.github.akruk.antlrxquery;

public class AxisVisitor 
    extends AntlrXqueryParserBaseVisitor<XQueryAxis> 
{
    @Override
    public XQueryAxis visitChildAxis(AntlrXqueryParser.ChildAxisContext ctx) {
        return XQueryAxis.CHILD;
    }

    @Override
    public XQueryAxis visitDescendantAxis(AntlrXqueryParser.DescendantAxisContext ctx) {
        return XQueryAxis.DESCENDANT;
    }

    @Override
    public XQueryAxis visitSelfAxis(AntlrXqueryParser.SelfAxisContext ctx) {
        return XQueryAxis.SELF;
    }

    @Override
    public XQueryAxis visitDescendantOrSelfAxis(AntlrXqueryParser.DescendantOrSelfAxisContext ctx) {
        return XQueryAxis.DESCENDANT_OR_SELF;
    }

    @Override
    public XQueryAxis visitFollowingSiblingAxis(AntlrXqueryParser.FollowingSiblingAxisContext ctx) {
        return XQueryAxis.FOLLOWING_SIBLING;
    }

    @Override
    public XQueryAxis visitFollowingAxis(AntlrXqueryParser.FollowingAxisContext ctx) {
        return XQueryAxis.FOLLOWING;
    }

    @Override
    public XQueryAxis visitFollowingSiblingOrSelfAxis(AntlrXqueryParser.FollowingSiblingOrSelfAxisContext ctx) {
        return XQueryAxis.FOLLOWING_SIBLING_OR_SELF;
    }

    @Override
    public XQueryAxis visitFollowingOrSelfAxis(AntlrXqueryParser.FollowingOrSelfAxisContext ctx) {
        return XQueryAxis.FOLLOWING_OR_SELF;
    }

    @Override
    public XQueryAxis visitParentAxis(AntlrXqueryParser.ParentAxisContext ctx) {
        return XQueryAxis.PARENT;
    }

    @Override
    public XQueryAxis visitPrecedingSiblingOrSelfAxis(AntlrXqueryParser.PrecedingSiblingOrSelfAxisContext ctx) {
        return XQueryAxis.PRECEDING_SIBLING_OR_SELF;
    }

    @Override
    public XQueryAxis visitPrecedingOrSelfAxis(AntlrXqueryParser.PrecedingOrSelfAxisContext ctx) {
        return XQueryAxis.PRECEDING_OR_SELF;
    }

    @Override
    public XQueryAxis visitAncestorAxis(AntlrXqueryParser.AncestorAxisContext ctx) {
        return XQueryAxis.ANCESTOR;
    }

    @Override
    public XQueryAxis visitPrecedingSiblingAxis(AntlrXqueryParser.PrecedingSiblingAxisContext ctx) {
        return XQueryAxis.PRECEDING_SIBLING;
    }

    @Override
    public XQueryAxis visitPrecedingAxis(AntlrXqueryParser.PrecedingAxisContext ctx) {
        return XQueryAxis.PRECEDING;
    }

    @Override
    public XQueryAxis visitAncestorOrSelfAxis(AntlrXqueryParser.AncestorOrSelfAxisContext ctx) {
        return XQueryAxis.ANCESTOR_OR_SELF;
    }
}
