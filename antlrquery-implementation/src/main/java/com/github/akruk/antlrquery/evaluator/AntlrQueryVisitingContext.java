package com.github.akruk.antlrquery.evaluator;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;

public class AntlrQueryVisitingContext {
    private AntlrQueryValue value;
    private int position;
    private int size;
    public AntlrQueryValue getValue() {
        return value;
    }
    public void setValue(final AntlrQueryValue item) {
        this.value = item;
    }
    public int getPosition() {
        return position;
    }
    public void setPosition(final int position) {
        this.position = position;
    }
    public int getSize() {
        return size;
    }
    public void setSize(final int size) {
        this.size = size;
    }
}
