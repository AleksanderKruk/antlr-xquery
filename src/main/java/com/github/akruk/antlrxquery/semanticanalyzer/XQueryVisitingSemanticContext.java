package com.github.akruk.antlrxquery.semanticanalyzer;

import com.github.akruk.antlrxquery.typesystem.defaults.TypeInContext;

public class XQueryVisitingSemanticContext {
    private TypeInContext itemType;
    private TypeInContext positionType;
    private TypeInContext sizeType;

    public void setSizeType(TypeInContext sizeType) {
		this.sizeType = sizeType;
	}

	public TypeInContext getType() {
        return itemType;
    }

    public void setType(TypeInContext item) {
        this.itemType = item;
    }

    public TypeInContext getPositionType() {
        return positionType;
    }

    public TypeInContext getSizeType() {
        return sizeType;
    }

    public void setPositionType(TypeInContext positionType) {
        this.positionType = positionType;
    }
}
