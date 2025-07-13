package com.github.akruk.antlrxquery.values;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public record XQueryMap<KeyType>(Map<KeyType, XQueryValue> value, XQueryValueFactory valueFactory)
        implements XQueryValue
        {

    @Override
    public XQueryValue valueEqual(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue valueLessThan(XQueryValue other) {
        return XQueryError.InvalidArgumentType;
    }

    @Override
    public XQueryValue empty() {
        return valueFactory.bool(false);
    }

    @Override
    public ParseTree node() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'node'");
    }

    @Override
    public BigDecimal numericValue() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'numericValue'");
    }

    @Override
    public String stringValue() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'stringValue'");
    }

    @Override
    public Boolean booleanValue() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'booleanValue'");
    }

    @Override
    public XQueryFunction functionValue() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'functionValue'");
    }

    @Override
    public Boolean effectiveBooleanValue() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'effectiveBooleanValue'");
    }

    @Override
    public List<XQueryValue> sequence() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sequence'");
    }

    @Override
    public boolean isNumericValue() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isNumericValue'");
    }

    @Override
    public boolean isStringValue() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isStringValue'");
    }

    @Override
    public boolean isBooleanValue() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isBooleanValue'");
    }

    @Override
    public boolean isSequence() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isSequence'");
    }

    @Override
    public boolean isAtomic() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isAtomic'");
    }

    @Override
    public boolean isNode() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isNode'");
    }

    @Override
    public boolean isFunction() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isFunction'");
    }

    @Override
    public boolean isError() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isError'");
    }

    @Override
    public List<XQueryValue> atomize() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'atomize'");
    }

    @Override
    public XQueryValue not() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'not'");
    }

    @Override
    public XQueryValue and(XQueryValue other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'and'");
    }

    @Override
    public XQueryValue or(XQueryValue other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'or'");
    }

    @Override
    public XQueryValue add(XQueryValue other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'add'");
    }

    @Override
    public XQueryValue subtract(XQueryValue other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'subtract'");
    }

    @Override
    public XQueryValue multiply(XQueryValue other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'multiply'");
    }

    @Override
    public XQueryValue divide(XQueryValue other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'divide'");
    }

    @Override
    public XQueryValue integerDivide(XQueryValue other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'integerDivide'");
    }

    @Override
    public XQueryValue modulus(XQueryValue other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'modulus'");
    }

    @Override
    public XQueryValue concatenate(XQueryValue other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'concatenate'");
    }

    @Override
    public XQueryValue valueUnequal(XQueryValue other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'valueUnequal'");
    }

    @Override
    public XQueryValue valueLessEqual(XQueryValue other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'valueLessEqual'");
    }

    @Override
    public XQueryValue valueGreaterThan(XQueryValue other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'valueGreaterThan'");
    }

    @Override
    public XQueryValue valueGreaterEqual(XQueryValue other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'valueGreaterEqual'");
    }

    @Override
    public XQueryValue generalEqual(XQueryValue other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generalEqual'");
    }

    @Override
    public XQueryValue generalUnequal(XQueryValue other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generalUnequal'");
    }

    @Override
    public XQueryValue generalLessThan(XQueryValue other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generalLessThan'");
    }

    @Override
    public XQueryValue generalLessEqual(XQueryValue other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generalLessEqual'");
    }

    @Override
    public XQueryValue generalGreaterThan(XQueryValue other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generalGreaterThan'");
    }

    @Override
    public XQueryValue generalGreaterEqual(XQueryValue other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generalGreaterEqual'");
    }

    @Override
    public XQueryValue union(XQueryValue otherSequence) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'union'");
    }

    @Override
    public XQueryValue intersect(XQueryValue otherSequence) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'intersect'");
    }

    @Override
    public XQueryValue except(XQueryValue other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'except'");
    }

    @Override
    public XQueryValue head() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'head'");
    }

    @Override
    public XQueryValue tail() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'tail'");
    }

    @Override
    public XQueryValue insertBefore(XQueryValue position, XQueryValue inserted) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertBefore'");
    }

    @Override
    public XQueryValue insertAfter(XQueryValue position, XQueryValue inserted) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertAfter'");
    }

    @Override
    public XQueryValue remove(XQueryValue position) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }

    @Override
    public XQueryValue reverse() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'reverse'");
    }

    @Override
    public XQueryValue subsequence(int startingLoc) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'subsequence'");
    }

    @Override
    public XQueryValue subsequence(int startingLoc, int length) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'subsequence'");
    }

    @Override
    public XQueryValue distinctValues() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'distinctValues'");
    }

    @Override
    public XQueryValue data() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'data'");
    }

    @Override
    public boolean isEmptySequence() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isEmptySequence'");
    }
}

// public class XQueryMap<KeyType> extends XQueryValueBase<Map<KeyType, XQueryValue>> {
//     public XQueryMap(Map<KeyType, XQueryValue> value, XQueryValueFactory valueFactory) {
//         super(value, valueFactory);
//     }

//     @Override
//     public XQueryValue valueEqual(XQueryValue other) {
//         return XQueryError.InvalidArgumentType;
//     }

//     @Override
//     public XQueryValue valueLessThan(XQueryValue other) {
//         return XQueryError.InvalidArgumentType;
//     }

//     @Override
//     public XQueryValue empty() {
//         return valueFactory.bool(false);
//     }
// }
