package com.github.akruk.antlrquery.typesystem.typeoperations.itemtype;

import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;
import com.github.akruk.visitorannotations.Visitor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@Visitor(name = "TreeNodeTypeVisitor", classes= {TreeLike.class, TreeLike.class})
@DefaultQualifier(NonNull.class)
public class TreeNodeIsSubtype implements TreeNodeTypeVisitor<Boolean> {


    private final AntlrQueryTypeFactory typeFactory;

    public TreeNodeIsSubtype(AntlrQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }

    @Override
    public Boolean visit(TreeTokenType.TokenType tokenType, TreeTokenType.TokenType tokenType2) {
        return tokenType.grammar().equals(tokenType2.grammar())
                && tokenType2.elementNames().containsAll(tokenType.elementNames());
    }

    @Override
    public Boolean visit(TreeTokenType.TokenType tokenType, TreeTokenType.AnyToken anyToken) {
        return true;
    }

    @Override
    public Boolean visit(TreeTokenType.TokenType tokenType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return tokenType.grammar().equals(anyTokenFromGrammar.grammar());
    }

    @Override
    public Boolean visit(TreeTokenType.TokenType tokenType, TreeRuleType.RuleType ruleType) {
        return false;
    }

    @Override
    public Boolean visit(TreeTokenType.TokenType tokenType, TreeRuleType.AnyRule anyRule) {
        return true;
    }

    @Override
    public Boolean visit(TreeTokenType.TokenType tokenType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return tokenType.grammar().equals(anyRuleFromGrammar.grammar());
    }

    @Override
    public Boolean visit(TreeTokenType.AnyToken anyToken, TreeNodeType.NodeType elementType) {
        return false;
    }

    @Override
    public Boolean visit(TreeTokenType.AnyToken anyToken, TreeNodeType.AnyNode anyNode) {
        return true;
    }

    @Override
    public Boolean visit(TreeTokenType.AnyToken anyToken, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return false;
    }

    @Override
    public Boolean visit(TreeTokenType.AnyToken anyToken, TreeTokenType.TokenType tokenType) {
        return false;
    }

    @Override
    public Boolean visit(TreeTokenType.AnyToken anyToken, TreeTokenType.AnyToken anyToken2) {
        return true;
    }

    @Override
    public Boolean visit(TreeTokenType.AnyToken anyToken, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return false;
    }

    @Override
    public Boolean visit(TreeTokenType.AnyToken anyToken, TreeRuleType.RuleType ruleType) {
        return false;
    }

    @Override
    public Boolean visit(TreeTokenType.AnyToken anyToken, TreeRuleType.AnyRule anyRule) {
        return false;
    }

    @Override
    public Boolean visit(TreeTokenType.AnyToken anyToken, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return false;
    }

    @Override
    public Boolean visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeNodeType.NodeType elementType) {
        return anyTokenFromGrammar.grammar().equals(elementType.grammar())
                && elementType.elementNames().containsAll(typeFactory.grammarTokens(anyTokenFromGrammar.grammar()));
    }

    @Override
    public Boolean visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeNodeType.AnyNode anyNode) {
        return true;
    }

    @Override
    public Boolean visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return anyNodeFromGrammar.grammar().equals(anyTokenFromGrammar.grammar());
    }

    @Override
    public Boolean visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeTokenType.TokenType tokenType) {
        return false;
    }

    @Override
    public Boolean visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeTokenType.AnyToken anyToken) {
        return true;
    }

    @Override
    public Boolean visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar2) {
        return anyTokenFromGrammar.grammar().equals(anyTokenFromGrammar2.grammar());
    }

    @Override
    public Boolean visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeRuleType.RuleType ruleType) {
        return false;
    }

    @Override
    public Boolean visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeRuleType.AnyRule anyRule) {
        return true;
    }

    @Override
    public Boolean visit(TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return anyTokenFromGrammar.grammar().equals(anyRuleFromGrammar.grammar());
    }

    @Override
    public Boolean visit(TreeRuleType.RuleType ruleType, TreeNodeType.NodeType nodeType) {
        return ruleType.grammar().equals(nodeType.grammar())
                && nodeType.elementNames().containsAll(ruleType.elementNames());
    }

    @Override
    public Boolean visit(TreeRuleType.RuleType ruleType, TreeNodeType.AnyNode anyNode) {
        return true;
    }

    @Override
    public Boolean visit(TreeRuleType.RuleType ruleType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return ruleType.grammar().equals(anyNodeFromGrammar.grammar());
    }

    @Override
    public Boolean visit(TreeRuleType.RuleType ruleType, TreeTokenType.TokenType tokenType) {
        return false;
    }

    @Override
    public Boolean visit(TreeRuleType.RuleType ruleType, TreeTokenType.AnyToken anyToken) {
        return false;
    }

    @Override
    public Boolean visit(TreeRuleType.RuleType ruleType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return false;
    }

    @Override
    public Boolean visit(TreeNodeType.NodeType elementType, TreeNodeType.NodeType elementType2) {
        return elementType.grammar().equals(elementType2.grammar())
                && elementType2.elementNames().containsAll(elementType.elementNames());
    }

    @Override
    public Boolean visit(TreeNodeType.NodeType elementType, TreeNodeType.AnyNode anyNode) {
        return true;
    }

    @Override
    public Boolean visit(TreeNodeType.NodeType elementType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return elementType.grammar().equals(anyNodeFromGrammar.grammar());
    }

    @Override
    public Boolean visit(TreeNodeType.NodeType elementType, TreeTokenType.TokenType tokenType) {
        return false;
    }

    @Override
    public Boolean visit(TreeNodeType.NodeType elementType, TreeTokenType.AnyToken anyToken) {
        return false;
    }

    @Override
    public Boolean visit(TreeNodeType.NodeType elementType, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return false;
    }

    @Override
    public Boolean visit(TreeNodeType.NodeType elementType, TreeRuleType.RuleType ruleType) {
        return false;
    }

    @Override
    public Boolean visit(TreeNodeType.NodeType elementType, TreeRuleType.AnyRule anyRule) {
        return false;
    }

    @Override
    public Boolean visit(TreeNodeType.NodeType elementType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return false;
    }

    @Override
    public Boolean visit(TreeNodeType.AnyNode anyNode, TreeNodeType.NodeType elementType) {
        return false;
    }

    @Override
    public Boolean visit(TreeNodeType.AnyNode anyNode, TreeNodeType.AnyNode anyNode2) {
        return true;
    }

    @Override
    public Boolean visit(TreeNodeType.AnyNode anyNode, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return false;
    }

    @Override
    public Boolean visit(TreeNodeType.AnyNode anyNode, TreeTokenType.TokenType tokenType) {
        return false;
    }

    @Override
    public Boolean visit(TreeNodeType.AnyNode anyNode, TreeTokenType.AnyToken anyToken) {
        return false;
    }

    @Override
    public Boolean visit(TreeNodeType.AnyNode anyNode, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return false;
    }

    @Override
    public Boolean visit(TreeNodeType.AnyNode anyNode, TreeRuleType.RuleType ruleType) {
        return false;
    }

    @Override
    public Boolean visit(TreeNodeType.AnyNode anyNode, TreeRuleType.AnyRule anyRule) {
        return false;
    }

    @Override
    public Boolean visit(TreeNodeType.AnyNode anyNode, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return false;
    }

    @Override
    public Boolean visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeNodeType.NodeType elementType) {
        return anyNodeFromGrammar.grammar().equals(elementType.grammar())
                &&elementType.elementNames().containsAll(typeFactory.grammarNodes(anyNodeFromGrammar.grammar()));
    }

    @Override
    public Boolean visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeNodeType.AnyNode anyNode) {
        return true;
    }

    @Override
    public Boolean visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar2) {
        return anyNodeFromGrammar.grammar().equals(anyNodeFromGrammar2.grammar());
    }

    @Override
    public Boolean visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeTokenType.TokenType tokenType) {
        return false;
    }

    @Override
    public Boolean visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeTokenType.AnyToken anyToken) {
        return false;
    }

    @Override
    public Boolean visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return false;
    }

    @Override
    public Boolean visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeRuleType.RuleType ruleType) {
        return false;
    }

    @Override
    public Boolean visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeRuleType.AnyRule anyRule) {
        return false;
    }

    @Override
    public Boolean visit(TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return false;
    }

    @Override
    public Boolean visit(TreeTokenType.TokenType tokenType, TreeNodeType.NodeType elementType) {
        return tokenType.grammar().equals(elementType.grammar())
                && elementType.elementNames().containsAll(tokenType.elementNames());
    }

    @Override
    public Boolean visit(TreeTokenType.TokenType tokenType, TreeNodeType.AnyNode anyNode) {
        return true;
    }

    @Override
    public Boolean visit(TreeTokenType.TokenType tokenType, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return tokenType.grammar().equals(anyNodeFromGrammar.grammar());
    }

    @Override
    public Boolean visit(TreeRuleType.RuleType ruleType, TreeRuleType.RuleType ruleType2) {
        return ruleType.grammar().equals(ruleType2.grammar())
                && ruleType2.elementNames().containsAll(ruleType.elementNames());
    }

    @Override
    public Boolean visit(TreeRuleType.RuleType ruleType, TreeRuleType.AnyRule anyRule) {
        return true;
    }

    @Override
    public Boolean visit(TreeRuleType.RuleType ruleType, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return false;
    }

    @Override
    public Boolean visit(TreeRuleType.AnyRule anyRule, TreeNodeType.NodeType elementType) {
        return false;
    }

    @Override
    public Boolean visit(TreeRuleType.AnyRule anyRule, TreeNodeType.AnyNode anyNode) {
        return true;
    }

    @Override
    public Boolean visit(TreeRuleType.AnyRule anyRule, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return false;
    }

    @Override
    public Boolean visit(TreeRuleType.AnyRule anyRule, TreeTokenType.TokenType tokenType) {
        return false;
    }

    @Override
    public Boolean visit(TreeRuleType.AnyRule anyRule, TreeTokenType.AnyToken anyToken) {
        return false;
    }

    @Override
    public Boolean visit(TreeRuleType.AnyRule anyRule, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return false;
    }

    @Override
    public Boolean visit(TreeRuleType.AnyRule anyRule, TreeRuleType.RuleType ruleType) {
        return false;
    }

    @Override
    public Boolean visit(TreeRuleType.AnyRule anyRule, TreeRuleType.AnyRule anyRule2) {
        return true;
    }

    @Override
    public Boolean visit(TreeRuleType.AnyRule anyRule, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar) {
        return false;
    }

    @Override
    public Boolean visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeNodeType.NodeType elementType) {
        return anyRuleFromGrammar.grammar().equals(elementType.grammar())
                && elementType.elementNames().containsAll(typeFactory.grammarRules(anyRuleFromGrammar.grammar()));
    }

    @Override
    public Boolean visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeNodeType.AnyNode anyNode) {
        return true;
    }

    @Override
    public Boolean visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeNodeType.AnyNodeFromGrammar anyNodeFromGrammar) {
        return anyRuleFromGrammar.grammar().equals(anyNodeFromGrammar.grammar());
    }

    @Override
    public Boolean visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeTokenType.TokenType tokenType) {
        return false;
    }

    @Override
    public Boolean visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeTokenType.AnyToken anyToken) {
        return false;
    }

    @Override
    public Boolean visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeTokenType.AnyTokenFromGrammar anyTokenFromGrammar) {
        return false;
    }

    @Override
    public Boolean visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeRuleType.RuleType ruleType) {
        return false;
    }

    @Override
    public Boolean visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeRuleType.AnyRule anyRule) {
        return true;
    }

    @Override
    public Boolean visit(TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar, TreeRuleType.AnyRuleFromGrammar anyRuleFromGrammar2) {
        return anyRuleFromGrammar.grammar().equals(anyRuleFromGrammar2.grammar());
    }
}
