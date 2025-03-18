package com.github.akruk.antlrxquery.values;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;

public interface XQueryValue {
    public XQueryValue copy();

    public default XQueryValue isEmpty() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public default ParseTree node() {
        return null;
    }

    // Function
    public default BigDecimal numericValue() {
        return null;
    };

    public default String stringValue() {
        return null;
    };

    public default Boolean booleanValue() {
        return null;
    };

    public default List<XQueryValue> sequence() {
        return null;
    }

    public default Predicate<ParseTree> filter() {
        return null;
    }

    public default XQueryValue reference() {
        return null;
    }

    public default boolean isNumericValue() {
        return numericValue() != null;
    }

    public default boolean isStringValue() {
        return stringValue() != null;
    }

    public default boolean isBooleanValue() {
        return booleanValue() != null;
    }

    public default boolean isSequence() {
        return sequence() != null;
    }

    public default boolean isFilter() {
        return filter() != null;
    }

    public default boolean isReference() {
        return reference() != null;
    }

    public default boolean isAtomic() {
        return sequence() == null;
    }

    public default boolean isNode() {
        return node() != null;
    }

    public default List<XQueryValue> atomize() {
        return List.of(this);
    }


    public default XQueryValue not() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
	}

    public default XQueryValue and(XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
	}

    public default XQueryValue or(XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
	}


    public default XQueryValue add(XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
	}

    public default XQueryValue subtract(XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();

	}

  public default XQueryValue multiply(XQueryValue other) throws XQueryUnsupportedOperation {
    throw new XQueryUnsupportedOperation();

	}

  public default XQueryValue divide(XQueryValue other) throws XQueryUnsupportedOperation {
    throw new XQueryUnsupportedOperation();

	}

  public default XQueryValue integerDivide(XQueryValue other) throws XQueryUnsupportedOperation {
    throw new XQueryUnsupportedOperation();

	}

  public default XQueryValue modulus(XQueryValue other) throws XQueryUnsupportedOperation {
    throw new XQueryUnsupportedOperation();

	}

    public default XQueryValue concatenate(XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();

	}

    public default XQueryValue valueEqual(XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
	}

    public default XQueryValue valueUnequal(XQueryValue other) throws XQueryUnsupportedOperation {
        final var isUnequal = !valueEqual(other).booleanValue();
        return XQueryBoolean.of(isUnequal);
	}

    public default XQueryValue valueLessThan(XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();

	}

    public default XQueryValue valueLessEqual(XQueryValue other) throws XQueryUnsupportedOperation {
        final var isLessEqual = valueEqual(other).booleanValue() || valueLessThan(other).booleanValue();
        return XQueryBoolean.of(isLessEqual);
	}

    public default XQueryValue valueGreaterThan(XQueryValue other) throws XQueryUnsupportedOperation {
        final var isGreaterThan = !valueLessEqual(other).booleanValue();
        return XQueryBoolean.of(isGreaterThan);
	}

    public default XQueryValue valueGreaterEqual(XQueryValue other) throws XQueryUnsupportedOperation {
        final var isGreaterEqual = !valueLessThan(other).booleanValue();
        return XQueryBoolean.of(isGreaterEqual);
    }

    public default XQueryValue generalEqual(XQueryValue other) throws XQueryUnsupportedOperation {
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

    public default XQueryValue generalUnequal(XQueryValue other) throws XQueryUnsupportedOperation {
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

    public default XQueryValue generalLessThan(XQueryValue other) throws XQueryUnsupportedOperation {
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

    public default XQueryValue generalLessEqual(XQueryValue other) throws XQueryUnsupportedOperation {
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

    public default XQueryValue generalGreaterThan(XQueryValue other) throws XQueryUnsupportedOperation {
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

    public default XQueryValue generalGreaterEqual(XQueryValue other) throws XQueryUnsupportedOperation {
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

    public default XQueryValue union(XQueryValue otherSequence) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    public default XQueryValue intersect(XQueryValue otherSequence) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    public default XQueryValue except(XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    public default XQueryValue head() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }



}
