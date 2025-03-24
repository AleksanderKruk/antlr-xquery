package com.github.akruk.antlrxquery.values;

import java.math.BigDecimal;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public abstract class XQueryValueBase<T> implements XQueryValue {
    T value;

    @Override
    public ParseTree node() {
        return null;
    }

    @Override
    public BigDecimal numericValue() {
        return null;
    };

    @Override
    public String stringValue() {
        return null;
    };

    @Override
    public Boolean booleanValue() {
        return null;
    };

    @Override
    public Boolean effectiveBooleanValue() {
        return null;
    };

    @Override
    public List<XQueryValue> sequence() {
        return null;
    }

    @Override
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

    @Override
    public boolean isNumericValue() {
        return numericValue() != null;
    }

    @Override
    public boolean isStringValue() {
        return stringValue() != null;
    }

    @Override
    public boolean isBooleanValue() {
        return booleanValue() != null;
    }

    @Override
    public boolean isSequence() {
        return sequence() != null;
    }

    @Override
    public boolean isReference() {
        return reference() != null;
    }

    @Override
    public boolean isAtomic() {
        return sequence() == null;
    }

    @Override
    public boolean isNode() {
        return node() != null;
    }

    @Override
    public List<XQueryValue> atomize() {
        return List.of(this);
    }


    @Override
    public XQueryValue not(XQueryValueFactory factoryValue) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
	}

    @Override
    public XQueryValue and(XQueryValueFactory factoryValue, XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
	}

    @Override
    public XQueryValue or(XQueryValueFactory factoryValue, XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
	}


    @Override
    public XQueryValue add(XQueryValueFactory factoryValue, XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
	}

    @Override
    public XQueryValue subtract(XQueryValueFactory factoryValue, XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();

	}

  @Override
  public XQueryValue multiply(XQueryValueFactory factoryValue, XQueryValue other) throws XQueryUnsupportedOperation {
    throw new XQueryUnsupportedOperation();

	}

  @Override
  public XQueryValue divide(XQueryValueFactory factoryValue, XQueryValue other) throws XQueryUnsupportedOperation {
    throw new XQueryUnsupportedOperation();

	}

  @Override
  public XQueryValue integerDivide(XQueryValueFactory factoryValue, XQueryValue other) throws XQueryUnsupportedOperation {
    throw new XQueryUnsupportedOperation();

	}

  @Override
  public XQueryValue modulus(XQueryValueFactory factoryValue, XQueryValue other) throws XQueryUnsupportedOperation {
    throw new XQueryUnsupportedOperation();

	}

    @Override
    public XQueryValue concatenate(XQueryValueFactory factoryValue, XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();

	}

    @Override
    public abstract XQueryValue valueEqual(XQueryValueFactory factoryValue, XQueryValue other);

    @Override
    public XQueryValue valueUnequal(XQueryValueFactory factoryValue, XQueryValue other) {
        final var isUnequal = !valueEqual(factoryValue, other).booleanValue();
        return XQueryBoolean.of(isUnequal);
	}

    @Override
    public abstract XQueryValue valueLessThan(XQueryValueFactory factoryValue, XQueryValue other);

    @Override
    public XQueryValue valueLessEqual(XQueryValueFactory factoryValue, XQueryValue other) {
        final var isLessEqual = valueEqual(factoryValue, other).booleanValue() || valueLessThan(factoryValue, other).booleanValue();
        return XQueryBoolean.of(isLessEqual);
	}

    @Override
    public XQueryValue valueGreaterThan(XQueryValueFactory factoryValue, XQueryValue other) {
        final var isGreaterThan = !valueLessEqual(factoryValue, other).booleanValue();
        return XQueryBoolean.of(isGreaterThan);
	}

    @Override
    public XQueryValue valueGreaterEqual(XQueryValueFactory factoryValue, XQueryValue other) {
        final var isGreaterEqual = !valueLessThan(factoryValue, other).booleanValue();
        return XQueryBoolean.of(isGreaterEqual);
    }

    @Override
    public XQueryValue generalEqual(XQueryValueFactory factoryValue, XQueryValue other) {
        var thisAtomized = atomize();
        var otherAtomized = other.atomize();
        for (var thisElement : thisAtomized) {
            for (var otherElement : otherAtomized) {
                if (thisElement.valueEqual(factoryValue, otherElement).booleanValue()) {
                    return XQueryBoolean.TRUE;
                }
            }
        }
        return XQueryBoolean.of(thisAtomized.size() == 0 && otherAtomized.size() == 0);
	}

    @Override
    public XQueryValue generalUnequal(XQueryValueFactory factoryValue, XQueryValue other) {
        var thisAtomized = atomize();
        var otherAtomized = other.atomize();
        for (var thisElement : thisAtomized) {
            for (var otherElement : otherAtomized) {
                if (thisElement.valueUnequal(factoryValue, otherElement).booleanValue()) {
                    return XQueryBoolean.TRUE;
                }
            }
        }
        return XQueryBoolean.of(thisAtomized.size() == 0 && otherAtomized.size() == 0);
	}

    @Override
    public XQueryValue generalLessThan(XQueryValueFactory factoryValue, XQueryValue other) {
        var thisAtomized = atomize();
        var otherAtomized = other.atomize();
        for (var thisElement : thisAtomized) {
            for (var otherElement : otherAtomized) {
                if (thisElement.valueLessThan(factoryValue, otherElement).booleanValue()) {
                    return XQueryBoolean.TRUE;
                }
            }
        }
        return XQueryBoolean.of(thisAtomized.size() == 0 && otherAtomized.size() == 0);

	}

    @Override
    public XQueryValue generalLessEqual(XQueryValueFactory factoryValue, XQueryValue other) {
        var thisAtomized = atomize();
        var otherAtomized = other.atomize();
        for (var thisElement : thisAtomized) {
            for (var otherElement : otherAtomized) {
                if (thisElement.valueLessEqual(factoryValue, otherElement).booleanValue()) {
                    return XQueryBoolean.TRUE;
                }
            }
        }
        return XQueryBoolean.of(thisAtomized.size() == 0 && otherAtomized.size() == 0);

	}

    @Override
    public XQueryValue generalGreaterThan(XQueryValueFactory factoryValue, XQueryValue other) {
        var thisAtomized = atomize();
        var otherAtomized = other.atomize();
        for (var thisElement : thisAtomized) {
            for (var otherElement : otherAtomized) {
                if (thisElement.valueGreaterThan(factoryValue, otherElement).booleanValue()) {
                    return XQueryBoolean.TRUE;
                }
            }
        }
        return XQueryBoolean.of(thisAtomized.size() == 0 && otherAtomized.size() == 0);
	}

    @Override
    public XQueryValue generalGreaterEqual(XQueryValueFactory factoryValue, XQueryValue other) {
        var thisAtomized = atomize();
        var otherAtomized = other.atomize();
        for (var thisElement : thisAtomized) {
            for (var otherElement : otherAtomized) {
                if (thisElement.valueGreaterEqual(factoryValue, otherElement).booleanValue()) {
                    return XQueryBoolean.TRUE;
                }
            }
        }
        return XQueryBoolean.of(thisAtomized.size() == 0 && otherAtomized.size() == 0);
	}

    @Override
    public XQueryValue union(XQueryValueFactory factoryValue, XQueryValue otherSequence) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    @Override
    public XQueryValue intersect(XQueryValueFactory factoryValue, XQueryValue otherSequence) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue except(XQueryValueFactory factoryValue, XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    @Override
    public XQueryValue head(XQueryValueFactory factoryValue) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue tail(XQueryValueFactory factoryValue) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue insertBefore(XQueryValueFactory factoryValue, XQueryValue position, XQueryValue inserted) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue insertAfter(XQueryValueFactory factoryValue, XQueryValue position, XQueryValue inserted) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue remove(XQueryValueFactory factoryValue, XQueryValue position) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue reverse(XQueryValueFactory factoryValue) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue subsequence(XQueryValueFactory factoryValue, int startingLoc) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue subsequence(XQueryValueFactory factoryValue, int startingLoc, int length) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue distinctValues(XQueryValueFactory factoryValue) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    @Override
    public XQueryValue zeroOrOne(XQueryValueFactory factoryValue) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    @Override
    public XQueryValue oneOrMore(XQueryValueFactory factoryValue) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue exactlyOne(XQueryValueFactory factoryValue) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    @Override
    public XQueryValue data(XQueryValueFactory factoryValue) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    @Override
    public XQueryValue substring(XQueryValueFactory factoryValue, int other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue substring(XQueryValueFactory factoryValue, int startingLoc, int length) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue contains(XQueryValueFactory factoryValue, XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    @Override
    public XQueryValue startsWith(XQueryValueFactory factoryValue, XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue endsWith(XQueryValueFactory factoryValue, XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue lowercase(XQueryValueFactory factoryValue) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue uppercase(XQueryValueFactory factoryValue) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue substringBefore(XQueryValueFactory factoryValue, XQueryValue splitstring) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue substringAfter(XQueryValueFactory factoryValue, XQueryValue splitstring) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }
}
