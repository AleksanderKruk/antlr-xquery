package com.github.akruk.antlrxquery.semanticanalyzer;

import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;

public class XQueryVisitingSemanticContext {
    private XQuerySequenceType itemType;
    private XQuerySequenceType positionType;
    private XQuerySequenceType sizeType;

    public void setSizeType(XQuerySequenceType sizeType) {
		this.sizeType = sizeType;
	}

	public XQuerySequenceType getType() {
        return itemType;
    }

    public void setType(XQuerySequenceType item) {
        this.itemType = item;
    }

    public XQuerySequenceType getPositionType() {
        return positionType;
    }

    public XQuerySequenceType getSizeType() {
        return sizeType;
    }

    public void setPositionType(XQuerySequenceType positionType) {
        this.positionType = positionType;
    }
}
