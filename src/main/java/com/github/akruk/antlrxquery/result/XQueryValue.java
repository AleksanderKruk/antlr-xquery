package com.github.akruk.antlrxquery.result;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;

public interface XQueryValue {
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

    public default List<XQueryValue> collection() {
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

    public default boolean isCollection() {
        return collection() != null;
    }

    public default boolean isFilter() {
        return filter() != null;
    }

    public default boolean isReference() {
        return reference() != null;
    }

    public default XQueryValue not(XQueryValue other) throws XQueryUnsupportedOperation {
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
        return new XQueryBoolean(isUnequal);
	}

    public default XQueryValue valueLessThan(XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();

	}

    public default XQueryValue valueLessEqual(XQueryValue other) throws XQueryUnsupportedOperation {
        final var isLessEqual = valueEqual(other).booleanValue() || valueLessThan(other).booleanValue();
        return new XQueryBoolean(isLessEqual);
	}

    public default XQueryValue valueGreaterThan(XQueryValue other) throws XQueryUnsupportedOperation {
        final var isGreaterThan = !valueLessEqual(other).booleanValue();
        return new XQueryBoolean(isGreaterThan);
	}

    public default XQueryValue valueGreaterEqual(XQueryValue other) throws XQueryUnsupportedOperation {
        final var isGreaterEqual = !valueLessThan(other).booleanValue();
        return new XQueryBoolean(isGreaterEqual);
	}

    public default XQueryValue generalEqual(XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();
	}

    public default XQueryValue generalUnequal(XQueryValue other) throws XQueryUnsupportedOperation {
        final var isUnequal = !generalEqual(other).booleanValue();
        return new XQueryBoolean(isUnequal);
	}

    public default XQueryValue generalLessThan(XQueryValue other) throws XQueryUnsupportedOperation {
        throw new XQueryUnsupportedOperation();

	}

    public default XQueryValue generalLessEqual(XQueryValue other) throws XQueryUnsupportedOperation {
        final var isLessEqual = generalEqual(other).booleanValue() || generalLessThan(other).booleanValue();
        return new XQueryBoolean(isLessEqual);
	}

    public default XQueryValue generalGreaterThan(XQueryValue other) throws XQueryUnsupportedOperation {
        final var isGreaterThan = !generalLessEqual(other).booleanValue();
        return new XQueryBoolean(isGreaterThan);
	}

    public default XQueryValue generalGreaterEqual(XQueryValue other) throws XQueryUnsupportedOperation {
        final var isGreaterEqual = !generalLessThan(other).booleanValue();
        return new XQueryBoolean(isGreaterEqual);
	}

}