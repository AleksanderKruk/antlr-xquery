package com.github.akruk.antlrxquery.values;

import java.math.BigDecimal;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;

public abstract class XQueryValueBase<T> implements XQueryValue {
    T value;
    @Override
    public XQueryValue empty() {
        return XQueryBoolean.FALSE;
    }

    @Override
    public ParseTree node() {
        return null;
    }

    public BigDecimal numericValue() {
        return null;
    };

    public String stringValue() {
        return null;
    };

    public Boolean booleanValue() {
        return null;
    };

    public Boolean effectiveBooleanValue() {
        return null;
    };

    public List<XQueryValue> sequence() {
        return null;
    }

    public XQueryValue reference() {
        return null;
    }

    @Override
    public XQueryFunction functionValue() {
        return null;
    }

    @Override
    public boolean isFunction() {
        return functionValue() != null;
    }

    public boolean isNumericValue() {
        return numericValue() != null;
    }

    public boolean isStringValue() {
        return stringValue() != null;
    }

    public boolean isBooleanValue() {
        return booleanValue() != null;
    }

    public boolean isSequence() {
        return sequence() != null;
    }

    public boolean isReference() {
        return reference() != null;
    }

    public boolean isAtomic() {
        return sequence() == null;
    }

    public boolean isNode() {
        return node() != null;
    }

    public List<XQueryValue> atomize() {
        return List.of(this);
    }


    public XQueryValue not() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
	}

    public XQueryValue and(XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
	}

    public XQueryValue or(XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
	}


    public XQueryValue add(XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
	}

    public XQueryValue subtract(XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();

	}

  public XQueryValue multiply(XQueryValue other) throws XQueryUnsupportedOperation {
    throw new XQueryUnsupportedOperation();

	}

  public XQueryValue divide(XQueryValue other) throws XQueryUnsupportedOperation {
    throw new XQueryUnsupportedOperation();

	}

  public XQueryValue integerDivide(XQueryValue other) throws XQueryUnsupportedOperation {
    throw new XQueryUnsupportedOperation();

	}

  public XQueryValue modulus(XQueryValue other) throws XQueryUnsupportedOperation {
    throw new XQueryUnsupportedOperation();

	}

    public XQueryValue concatenate(XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();

	}

    public abstract XQueryValue valueEqual(XQueryValue other);

    public XQueryValue valueUnequal(XQueryValue other) {
        final var isUnequal = !valueEqual(other).booleanValue();
        return XQueryBoolean.of(isUnequal);
	}

    public abstract XQueryValue valueLessThan(XQueryValue other);

    public XQueryValue valueLessEqual(XQueryValue other) {
        final var isLessEqual = valueEqual(other).booleanValue() || valueLessThan(other).booleanValue();
        return XQueryBoolean.of(isLessEqual);
	}

    public XQueryValue valueGreaterThan(XQueryValue other) {
        final var isGreaterThan = !valueLessEqual(other).booleanValue();
        return XQueryBoolean.of(isGreaterThan);
	}

    public XQueryValue valueGreaterEqual(XQueryValue other) {
        final var isGreaterEqual = !valueLessThan(other).booleanValue();
        return XQueryBoolean.of(isGreaterEqual);
    }

    public XQueryValue generalEqual(XQueryValue other) {
        var thisAtomized = atomize();
        var otherAtomized = other.atomize();
        for (var thisElement : thisAtomized) {
            for (var otherElement : otherAtomized) {
                if (thisElement.valueEqual(otherElement).booleanValue()) {
                    return XQueryBoolean.TRUE;
                }
            }
        }
        return XQueryBoolean.of(thisAtomized.size() == 0 && otherAtomized.size() == 0);
	}

    public XQueryValue generalUnequal(XQueryValue other) {
        var thisAtomized = atomize();
        var otherAtomized = other.atomize();
        for (var thisElement : thisAtomized) {
            for (var otherElement : otherAtomized) {
                if (thisElement.valueUnequal(otherElement).booleanValue()) {
                    return XQueryBoolean.TRUE;
                }
            }
        }
        return XQueryBoolean.of(thisAtomized.size() == 0 && otherAtomized.size() == 0);
	}

    public XQueryValue generalLessThan(XQueryValue other) {
        var thisAtomized = atomize();
        var otherAtomized = other.atomize();
        for (var thisElement : thisAtomized) {
            for (var otherElement : otherAtomized) {
                if (thisElement.valueLessThan(otherElement).booleanValue()) {
                    return XQueryBoolean.TRUE;
                }
            }
        }
        return XQueryBoolean.of(thisAtomized.size() == 0 && otherAtomized.size() == 0);

	}

    public XQueryValue generalLessEqual(XQueryValue other) {
        var thisAtomized = atomize();
        var otherAtomized = other.atomize();
        for (var thisElement : thisAtomized) {
            for (var otherElement : otherAtomized) {
                if (thisElement.valueLessEqual(otherElement).booleanValue()) {
                    return XQueryBoolean.TRUE;
                }
            }
        }
        return XQueryBoolean.of(thisAtomized.size() == 0 && otherAtomized.size() == 0);

	}

    public XQueryValue generalGreaterThan(XQueryValue other) {
        var thisAtomized = atomize();
        var otherAtomized = other.atomize();
        for (var thisElement : thisAtomized) {
            for (var otherElement : otherAtomized) {
                if (thisElement.valueGreaterThan(otherElement).booleanValue()) {
                    return XQueryBoolean.TRUE;
                }
            }
        }
        return XQueryBoolean.of(thisAtomized.size() == 0 && otherAtomized.size() == 0);
	}

    public XQueryValue generalGreaterEqual(XQueryValue other) {
        var thisAtomized = atomize();
        var otherAtomized = other.atomize();
        for (var thisElement : thisAtomized) {
            for (var otherElement : otherAtomized) {
                if (thisElement.valueGreaterEqual(otherElement).booleanValue()) {
                    return XQueryBoolean.TRUE;
                }
            }
        }
        return XQueryBoolean.of(thisAtomized.size() == 0 && otherAtomized.size() == 0);
	}

    public XQueryValue union(XQueryValue otherSequence) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    public XQueryValue intersect(XQueryValue otherSequence) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    public XQueryValue except(XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    public XQueryValue head() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    public XQueryValue tail() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    public XQueryValue insertBefore(XQueryValue position, XQueryValue inserted) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    public XQueryValue insertAfter(XQueryValue position, XQueryValue inserted) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    public XQueryValue remove(XQueryValue position) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    public XQueryValue reverse() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    public XQueryValue subsequence(int startingLoc) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    public XQueryValue subsequence(int startingLoc, int length) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    public XQueryValue distinctValues() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    public XQueryValue zeroOrOne() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    public XQueryValue oneOrMore() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    public XQueryValue exactlyOne() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    public XQueryValue data() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    public XQueryValue substring(int other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    public XQueryValue substring(int startingLoc, int length) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    public XQueryValue contains(XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    public XQueryValue startsWith(XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    public XQueryValue endsWith(XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    public XQueryValue lowercase() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    public XQueryValue uppercase() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    public XQueryValue substringBefore(XQueryValue splitstring) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    public XQueryValue substringAfter(XQueryValue splitstring) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }
}
