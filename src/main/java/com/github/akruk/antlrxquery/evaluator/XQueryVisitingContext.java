package com.github.akruk.antlrxquery.evaluator;

import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;

public class XQueryVisitingContext {
    private XQueryValue value;
    private int position;
    private int size;
    public XQueryValue getValue() {
        return value;
    }
    public void setValue(XQueryValue item) {
        this.value = item;
    }
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
}
