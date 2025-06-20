package com.github.akruk.antlrxquery.values;

import java.math.BigDecimal;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public abstract class XQueryValueBase<T> implements XQueryValue {
    final T value;
    final XQueryValueFactory valueFactory;

    public XQueryValueBase(final T value, final XQueryValueFactory valueFactory) {
        this.value = value;
        this.valueFactory = valueFactory;
    }

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
    public XQueryValue not() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
	}

    @Override
    public XQueryValue and(final XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
	}

    @Override
    public XQueryValue or(final XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
	}


    @Override
    public XQueryValue add(final XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
	}

    @Override
    public XQueryValue subtract(final XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();

	}

  @Override
  public XQueryValue multiply(final XQueryValue other) throws XQueryUnsupportedOperation {
    throw new XQueryUnsupportedOperation();

	}

  @Override
  public XQueryValue divide(final XQueryValue other) throws XQueryUnsupportedOperation {
    throw new XQueryUnsupportedOperation();

	}

  @Override
  public XQueryValue integerDivide(final XQueryValue other) throws XQueryUnsupportedOperation {
    throw new XQueryUnsupportedOperation();

	}

  @Override
  public XQueryValue modulus(final XQueryValue other) throws XQueryUnsupportedOperation {
    throw new XQueryUnsupportedOperation();

	}

    @Override
    public XQueryValue concatenate(final XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();

	}

    @Override
    public abstract XQueryValue valueEqual(XQueryValue other);

    @Override
    public XQueryValue valueUnequal(final XQueryValue other) {
        final var isUnequal = !valueEqual(other).booleanValue();
        return valueFactory.bool(isUnequal);
	}

    @Override
    public abstract XQueryValue valueLessThan(XQueryValue other);

    @Override
    public XQueryValue valueLessEqual(final XQueryValue other) {
        final var isLessEqual = valueEqual(other).booleanValue() || valueLessThan(other).booleanValue();
        return valueFactory.bool(isLessEqual);
	}

    @Override
    public XQueryValue valueGreaterThan(final XQueryValue other) {
        final var isGreaterThan = !valueLessEqual(other).booleanValue();
        return valueFactory.bool(isGreaterThan);
	}

    @Override
    public XQueryValue valueGreaterEqual(final XQueryValue other) {
        final var isGreaterEqual = !valueLessThan(other).booleanValue();
        return valueFactory.bool(isGreaterEqual);
    }

    @Override
    public XQueryValue generalEqual(final XQueryValue other) {
        final var thisAtomized = atomize();
        final var otherAtomized = other.atomize();
        for (final var thisElement : thisAtomized) {
            for (final var otherElement : otherAtomized) {
                if (thisElement.valueEqual(otherElement).booleanValue()) {
                    return valueFactory.bool(true);
                }
            }
        }
        return valueFactory.bool(thisAtomized.size() == 0 && otherAtomized.size() == 0);
	}

    @Override
    public XQueryValue generalUnequal(final XQueryValue other) {
        final var thisAtomized = atomize();
        final var otherAtomized = other.atomize();
        for (final var thisElement : thisAtomized) {
            for (final var otherElement : otherAtomized) {
                if (thisElement.valueUnequal(otherElement).booleanValue()) {
                    return valueFactory.bool(true);
                }
            }
        }
        return valueFactory.bool(thisAtomized.size() == 0 && otherAtomized.size() == 0);
	}

    @Override
    public XQueryValue generalLessThan(final XQueryValue other) {
        final var thisAtomized = atomize();
        final var otherAtomized = other.atomize();
        for (final var thisElement : thisAtomized) {
            for (final var otherElement : otherAtomized) {
                if (thisElement.valueLessThan(otherElement).booleanValue()) {
                    return valueFactory.bool(true);
                }
            }
        }
        return valueFactory.bool(thisAtomized.size() == 0 && otherAtomized.size() == 0);

	}

    @Override
    public XQueryValue generalLessEqual(final XQueryValue other) {
        final var thisAtomized = atomize();
        final var otherAtomized = other.atomize();
        for (final var thisElement : thisAtomized) {
            for (final var otherElement : otherAtomized) {
                if (thisElement.valueLessEqual(otherElement).booleanValue()) {
                    return valueFactory.bool(true);
                }
            }
        }
        return valueFactory.bool(thisAtomized.size() == 0 && otherAtomized.size() == 0);

	}

    @Override
    public XQueryValue generalGreaterThan(final XQueryValue other) {
        final var thisAtomized = atomize();
        final var otherAtomized = other.atomize();
        for (final var thisElement : thisAtomized) {
            for (final var otherElement : otherAtomized) {
                if (thisElement.valueGreaterThan(otherElement).booleanValue()) {
                    return valueFactory.bool(true);
                }
            }
        }
        return valueFactory.bool(thisAtomized.size() == 0 && otherAtomized.size() == 0);
	}

    @Override
    public XQueryValue generalGreaterEqual(final XQueryValue other) {
        final var thisAtomized = atomize();
        final var otherAtomized = other.atomize();
        for (final var thisElement : thisAtomized) {
            for (final var otherElement : otherAtomized) {
                if (thisElement.valueGreaterEqual(otherElement).booleanValue()) {
                    return valueFactory.bool(true);
                }
            }
        }
        return valueFactory.bool(thisAtomized.size() == 0 && otherAtomized.size() == 0);
	}

    @Override
    public XQueryValue union(final XQueryValue otherSequence) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    @Override
    public XQueryValue intersect(final XQueryValue otherSequence) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue except(final XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    @Override
    public XQueryValue head() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue tail() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue insertBefore(final XQueryValue position, final XQueryValue inserted) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue insertAfter(final XQueryValue position, final XQueryValue inserted) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue remove(final XQueryValue position) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue reverse() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue subsequence(final int startingLoc) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue subsequence(final int startingLoc, final int length) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue distinctValues() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    @Override
    public XQueryValue zeroOrOne() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    @Override
    public XQueryValue oneOrMore() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue exactlyOne() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    @Override
    public XQueryValue data() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    @Override
    public XQueryValue substring(final int other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue substring(final int startingLoc, final int length) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue contains(final XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }


    @Override
    public XQueryValue startsWith(final XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue endsWith(final XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue lowercase() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue uppercase() throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue substringBefore(final XQueryValue splitstring) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }

    @Override
    public XQueryValue substringAfter(final XQueryValue splitstring) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
    }
}
