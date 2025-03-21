package com.github.akruk.antlrxquery.evaluator;

import com.github.akruk.antlrxquery.values.XQueryValue;

public class XQueryVisitingContext {
    private XQueryValue item;
    private int position;
    private int size;
    public XQueryValue getItem() {
        return item;
    }
    public void setItem(XQueryValue item) {
        this.item = item;
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
