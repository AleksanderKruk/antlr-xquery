package com.github.akruk.antlrxquery.values;

import java.math.BigDecimal;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

public enum XQueryError implements XQueryValue {
    ;

    @Override
    public ParseTree node() {
        return null;
    }

    @Override
    public BigDecimal numericValue() {
        return null;
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public Boolean booleanValue() {
        return null;
    }

    @Override
    public XQueryFunction functionValue() {
        return null;
    }

    @Override
    public Boolean effectiveBooleanValue() {
        return null;
    }

    @Override
    public List<XQueryValue> sequence() {
        return null;
    }

    @Override
    public boolean isNumericValue() {
        return false;
    }

    @Override
    public boolean isStringValue() {
        return false;
    }

    @Override
    public boolean isBooleanValue() {
        return false;
    }

    @Override
    public boolean isSequence() {
        return false;
    }

    @Override
    public boolean isAtomic() {
        return false;
    }

    @Override
    public boolean isNode() {
        return false;
    }

    @Override
    public boolean isFunction() {
        return false;
    }

    @Override
    public List<XQueryValue> atomize() {
        return null;
    }

    @Override
    public XQueryValue copy() {
        return null;
    }

    @Override
    public XQueryValue empty() {
        return null;
    }

    @Override
    public XQueryValue not() {
        return null;
    }

    @Override
    public XQueryValue and(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue or(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue add(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue subtract(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue multiply(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue divide(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue integerDivide(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue modulus(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue concatenate(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue valueEqual(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue valueUnequal(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue valueLessThan(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue valueLessEqual(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue valueGreaterThan(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue valueGreaterEqual(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue generalEqual(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue generalUnequal(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue generalLessThan(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue generalLessEqual(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue generalGreaterThan(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue generalGreaterEqual(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue union(XQueryValue otherSequence) {
        return null;
    }

    @Override
    public XQueryValue intersect(XQueryValue otherSequence) {
        return null;
    }

    @Override
    public XQueryValue except(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue head() {
        return null;
    }

    @Override
    public XQueryValue tail() {
        return null;
    }

    @Override
    public XQueryValue insertBefore(XQueryValue position, XQueryValue inserted) {
        return null;
    }

    @Override
    public XQueryValue insertAfter(XQueryValue position, XQueryValue inserted) {
        return null;
    }

    @Override
    public XQueryValue remove(XQueryValue position) {
        return null;
    }

    @Override
    public XQueryValue reverse() {
        return null;
    }

    @Override
    public XQueryValue subsequence(int startingLoc) {
        return null;
    }

    @Override
    public XQueryValue subsequence(int startingLoc, int length) {
        return null;
    }

    @Override
    public XQueryValue distinctValues() {
        return null;
    }

    @Override
    public XQueryValue zeroOrOne() {
        return null;
    }

    @Override
    public XQueryValue oneOrMore() {
        return null;
    }

    @Override
    public XQueryValue exactlyOne() {
        return null;
    }

    @Override
    public XQueryValue data() {
        return null;
    }

    @Override
    public XQueryValue contains(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue startsWith(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue endsWith(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue uppercase() {
        return null;
    }

    @Override
    public XQueryValue lowercase() {
        return null;
    }

    @Override
    public XQueryValue substring(int startingLoc) {
        return null;
    }

    @Override
    public XQueryValue substring(int startingLoc, int length) {
        return null;
    }

    @Override
    public XQueryValue substringBefore(XQueryValue splitstring) {
        return null;
    }

    @Override
    public XQueryValue substringAfter(XQueryValue splitstring) {
        return null;
    }

}
