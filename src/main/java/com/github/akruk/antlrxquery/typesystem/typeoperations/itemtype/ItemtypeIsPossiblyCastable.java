package com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype;

import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;

public class ItemtypeIsPossiblyCastable extends ItemtypeUnaryDirectPredicate
{
    public boolean test(XQueryItemType x) {
        return automaton[x.getType().ordinal()];
    }

    @Override
    public boolean errorValue() {
        return false;
    }

    @Override
    public boolean anyItemValue() {
        return false;
    }

    @Override
    public boolean anyNodeValue() {
        return false;
    }

    @Override
    public boolean elementValue() {
        return true;
    }

    @Override
    public boolean enumValue() {
        return true;
    }

    @Override
    public boolean booleanValue() {
        return true;
    }

    @Override
    public boolean numberValue() {
        return true;
    }

    @Override
    public boolean stringValue() {
        return true;
    }

    @Override
    public boolean anyMapValue() {
        return false;
    }

    @Override
    public boolean mapValue() {
        return false;
    }

    @Override
    public boolean choiceValue() {
        return true;
    }

    @Override
    public boolean anyArrayValue() {
        return false;
    }

    @Override
    public boolean arrayValue() {
        return false;
    }

    @Override
    public boolean anyFunctionValue() {
        return false;
    }

    @Override
    public boolean functionValue() {
        return false;
    }

    @Override
    public boolean recordValue() {
        return false;
    }

    @Override
    public boolean extensibleRecordValue() {
        return false;
    }


}
