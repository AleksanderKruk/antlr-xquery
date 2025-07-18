package com.github.akruk.antlrxquery.values;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;


public interface XQueryValue {
    public ParseTree node();
    public BigDecimal numericValue();
    public String stringValue();
    public XQueryFunction functionValue();
    public Boolean effectiveBooleanValue();
    public List<XQueryValue> sequence();
    public List<XQueryValue> arrayMembers();
    public Map<XQueryValue, XQueryValue> mapEntries();
    public boolean isNumericValue();
    public boolean isStringValue();
    public boolean isBooleanValue();
    public boolean isSequence();
    public boolean isAtomic();
    public boolean isNode();
    public boolean isFunction();
    public boolean isError();
    public List<XQueryValue> atomize();
    public XQueryValue empty();
    public XQueryValue and(XQueryValue other);
    public XQueryValue or(XQueryValue other);
    public XQueryValue add(XQueryValue other);
    public XQueryValue subtract(XQueryValue other);
    public XQueryValue multiply(XQueryValue other);
    public XQueryValue divide(XQueryValue other);
    public XQueryValue integerDivide(XQueryValue other);
    public XQueryValue modulus(XQueryValue other);
    public XQueryValue concatenate(XQueryValue other);
    public XQueryValue valueEqual(XQueryValue other);
    public XQueryValue valueUnequal(XQueryValue other);
    public XQueryValue valueLessThan(XQueryValue other);
    public XQueryValue valueLessEqual(XQueryValue other);
    public XQueryValue valueGreaterThan(XQueryValue other);
    public XQueryValue valueGreaterEqual(XQueryValue other);
    public XQueryValue generalEqual(XQueryValue other);
    public XQueryValue generalUnequal(XQueryValue other);
    public XQueryValue generalLessThan(XQueryValue other);
    public XQueryValue generalLessEqual(XQueryValue other);
    public XQueryValue generalGreaterThan(XQueryValue other);
    public XQueryValue generalGreaterEqual(XQueryValue other);
    public XQueryValue union(XQueryValue otherSequence);
    public XQueryValue intersect(XQueryValue otherSequence);
    public XQueryValue except(XQueryValue other);
    public XQueryValue remove(XQueryValue position);
    public XQueryValue data();
    public boolean isEmptySequence();
}
