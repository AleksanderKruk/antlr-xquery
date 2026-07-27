package com.github.akruk.antlrxquery.typesystem.types.itemtypes;

sealed public interface TreeLike
        extends ConcreteItemType
        permits TreeNodeType, TreeRuleType, TreeTokenType
{
}

