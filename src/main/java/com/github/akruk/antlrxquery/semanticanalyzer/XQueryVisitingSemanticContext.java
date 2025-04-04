package com.github.akruk.antlrxquery.semanticanalyzer;

import com.github.akruk.antlrxquery.typesystem.XQueryType;

public class XQueryVisitingSemanticContext {
    private XQueryType itemType;
    private XQueryType positionType;

    public XQueryType getItemType() {
        return itemType;
    }

    public void setItemType(XQueryType item) {
        this.itemType = item;
    }

    public XQueryType getPositionType() {
        return positionType;
    }

    public XQueryType  getSizeType() {
        return positionType;
    }

    public void setPositionType(XQueryType positionType) {
        this.positionType = positionType;
    }
}
