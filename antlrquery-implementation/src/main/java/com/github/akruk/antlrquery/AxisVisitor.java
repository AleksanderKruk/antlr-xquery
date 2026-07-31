package com.github.akruk.antlrquery;

public class AxisVisitor 
    extends AntlrQueryParserBaseVisitor<AntlrQueryAxis>
{
    @Override
    public AntlrQueryAxis visitChildAxis(AntlrQueryParser.ChildAxisContext ctx) {
        return AntlrQueryAxis.CHILD;
    }

    @Override
    public AntlrQueryAxis visitDescendantAxis(AntlrQueryParser.DescendantAxisContext ctx) {
        return AntlrQueryAxis.DESCENDANT;
    }

    @Override
    public AntlrQueryAxis visitSelfAxis(AntlrQueryParser.SelfAxisContext ctx) {
        return AntlrQueryAxis.SELF;
    }

    @Override
    public AntlrQueryAxis visitDescendantOrSelfAxis(AntlrQueryParser.DescendantOrSelfAxisContext ctx) {
        return AntlrQueryAxis.DESCENDANT_OR_SELF;
    }

    @Override
    public AntlrQueryAxis visitFollowingSiblingAxis(AntlrQueryParser.FollowingSiblingAxisContext ctx) {
        return AntlrQueryAxis.FOLLOWING_SIBLING;
    }

    @Override
    public AntlrQueryAxis visitFollowingAxis(AntlrQueryParser.FollowingAxisContext ctx) {
        return AntlrQueryAxis.FOLLOWING;
    }

    @Override
    public AntlrQueryAxis visitFollowingSiblingOrSelfAxis(AntlrQueryParser.FollowingSiblingOrSelfAxisContext ctx) {
        return AntlrQueryAxis.FOLLOWING_SIBLING_OR_SELF;
    }

    @Override
    public AntlrQueryAxis visitFollowingOrSelfAxis(AntlrQueryParser.FollowingOrSelfAxisContext ctx) {
        return AntlrQueryAxis.FOLLOWING_OR_SELF;
    }

    @Override
    public AntlrQueryAxis visitParentAxis(AntlrQueryParser.ParentAxisContext ctx) {
        return AntlrQueryAxis.PARENT;
    }

    @Override
    public AntlrQueryAxis visitPrecedingSiblingOrSelfAxis(AntlrQueryParser.PrecedingSiblingOrSelfAxisContext ctx) {
        return AntlrQueryAxis.PRECEDING_SIBLING_OR_SELF;
    }

    @Override
    public AntlrQueryAxis visitPrecedingOrSelfAxis(AntlrQueryParser.PrecedingOrSelfAxisContext ctx) {
        return AntlrQueryAxis.PRECEDING_OR_SELF;
    }

    @Override
    public AntlrQueryAxis visitAncestorAxis(AntlrQueryParser.AncestorAxisContext ctx) {
        return AntlrQueryAxis.ANCESTOR;
    }

    @Override
    public AntlrQueryAxis visitPrecedingSiblingAxis(AntlrQueryParser.PrecedingSiblingAxisContext ctx) {
        return AntlrQueryAxis.PRECEDING_SIBLING;
    }

    @Override
    public AntlrQueryAxis visitPrecedingAxis(AntlrQueryParser.PrecedingAxisContext ctx) {
        return AntlrQueryAxis.PRECEDING;
    }

    @Override
    public AntlrQueryAxis visitAncestorOrSelfAxis(AntlrQueryParser.AncestorOrSelfAxisContext ctx) {
        return AntlrQueryAxis.ANCESTOR_OR_SELF;
    }
}
