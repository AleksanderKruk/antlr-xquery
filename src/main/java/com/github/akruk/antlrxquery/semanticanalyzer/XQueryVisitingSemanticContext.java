package com.github.akruk.antlrxquery.semanticanalyzer;

import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;

public class XQueryVisitingSemanticContext {
    private XQuerySequenceType itemType;
    private XQuerySequenceType positionType;

    public XQuerySequenceType getItemType() {
        return itemType;
    }

    public void setItemType(XQuerySequenceType item) {
        this.itemType = item;
    }

    public XQuerySequenceType getPositionType() {
        return positionType;
    }

    public XQuerySequenceType  getSizeType() {
        return positionType;
    }

    public void setPositionType(XQuerySequenceType positionType) {
        this.positionType = positionType;
    }
}
