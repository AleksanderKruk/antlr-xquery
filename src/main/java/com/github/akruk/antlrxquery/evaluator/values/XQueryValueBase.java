package com.github.akruk.antlrxquery.evaluator.values;

import java.math.BigDecimal;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

public abstract class XQueryValueBase<T> implements XQueryValue {
    final T value;
    final XQueryValueFactory valueFactory;

    public XQueryValueBase(final T value, final XQueryValueFactory valueFactory) {
        this.value = value;
        this.valueFactory = valueFactory;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
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
    public Boolean effectiveBooleanValue() {
        return null;
    };

    @Override
    public List<XQueryValue> sequence() {
        return List.of(this);
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
    public boolean isError() {
        return false;
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
        return effectiveBooleanValue() != null;
    }

    @Override
    public boolean isSequence() {
        return false;
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
    public XQueryValue not() {
        return null;
	}

    @Override
    public XQueryValue and(final XQueryValue other) {
        return null;
	}

    @Override
    public XQueryValue or(final XQueryValue other) {
        return null;
	}


    @Override
    public XQueryValue add(final XQueryValue other) {
        return null;
	}

    @Override
    public XQueryValue subtract(final XQueryValue other) {
        return null;

	}

  @Override
  public XQueryValue multiply(final XQueryValue other) {
    return null;

	}

  @Override
  public XQueryValue divide(final XQueryValue other) {
    return null;

	}

  @Override
  public XQueryValue integerDivide(final XQueryValue other) {
    return null;

	}

  @Override
  public XQueryValue modulus(final XQueryValue other) {
    return null;

	}

    @Override
    public XQueryValue concatenate(final XQueryValue other) {
        return null;

	}

    @Override
    public abstract XQueryValue valueEqual(XQueryValue other);

    @Override
    public XQueryValue valueUnequal(final XQueryValue other) {
        final var isUnequal = !valueEqual(other).effectiveBooleanValue();
        return valueFactory.bool(isUnequal);
	}

    @Override
    public abstract XQueryValue valueLessThan(XQueryValue other);

    @Override
    public XQueryValue valueLessEqual(final XQueryValue other) {
        final var isLessEqual = valueEqual(other).effectiveBooleanValue() || valueLessThan(other).effectiveBooleanValue();
        return valueFactory.bool(isLessEqual);
	}

    @Override
    public XQueryValue valueGreaterThan(final XQueryValue other) {
        final var isGreaterThan = !valueLessEqual(other).effectiveBooleanValue();
        return valueFactory.bool(isGreaterThan);
	}

    @Override
    public XQueryValue valueGreaterEqual(final XQueryValue other) {
        final var isGreaterEqual = !valueLessThan(other).effectiveBooleanValue();
        return valueFactory.bool(isGreaterEqual);
    }

    @Override
    public XQueryValue generalEqual(final XQueryValue other) {
        final var thisAtomized = atomize();
        final var otherAtomized = other.atomize();
        for (final var thisElement : thisAtomized) {
            for (final var otherElement : otherAtomized) {
                if (thisElement.valueEqual(otherElement).effectiveBooleanValue()) {
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
                if (thisElement.valueUnequal(otherElement).effectiveBooleanValue()) {
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
                if (thisElement.valueLessThan(otherElement).effectiveBooleanValue()) {
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
                if (thisElement.valueLessEqual(otherElement).effectiveBooleanValue()) {
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
                if (thisElement.valueGreaterThan(otherElement).effectiveBooleanValue()) {
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
                if (thisElement.valueGreaterEqual(otherElement).effectiveBooleanValue()) {
                    return valueFactory.bool(true);
                }
            }
        }
        return valueFactory.bool(thisAtomized.size() == 0 && otherAtomized.size() == 0);
	}

    @Override
    public XQueryValue union(final XQueryValue otherSequence) {
        return null;
    }


    @Override
    public XQueryValue intersect(final XQueryValue otherSequence) {
        return null;
    }

    @Override
    public XQueryValue except(final XQueryValue other) {
        return null;
    }


    @Override
    public XQueryValue remove(final XQueryValue position) {
        return null;
    }

    @Override
    public XQueryValue data() {
        return null;
    }


    @Override
    public boolean isEmptySequence() {
        return false;
    }

}
