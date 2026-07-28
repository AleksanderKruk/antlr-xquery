package com.github.akruk.antlrquery.typesystem.types.itemtypes;

sealed public interface TreeLike
        extends ConcreteItemType
        permits TreeNodeType, TreeRuleType, TreeTokenType
{
}

