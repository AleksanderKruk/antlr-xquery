package com.github.akruk.antlrxquery.evaluator.values.operations;

import java.util.List;

import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

public class ValueArithmeticOperator {

    private final XQueryValueFactory valueFactory;
    private static final ValueAtomizer atomizer = new ValueAtomizer();

    public ValueArithmeticOperator(final XQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    private XQueryValue validateOperand(final XQueryValue operand, final List<XQueryValue> atomized) {
        // If an atomized operand is a sequence of length greater than one, a type error is raised [err:XPTY0004].
        if (atomized.size() > 1) {
            return valueFactory.error(XQueryError.InvalidArgumentType, "Atomized operand" + operand +" is a sequence of length greater than one");
        }
        if (operand.isError) {
            return operand;
        }
        // Tried to compare non atomic values
        if (operand.isNode) {
            return valueFactory.error(
                XQueryError.InvalidArgumentType,
                "Operand: " +operand+ " is a node, which cannot be compared using value comparison"
            );
        }
        if (operand.isMap) {
            return valueFactory.error(
                XQueryError.InvalidArgumentType,
                "Operand: " +operand+ " is a map, which cannot be compared using value comparison"
            );
        }
        if (operand.isArray) {
            return valueFactory.error(
                XQueryError.InvalidArgumentType,
                "Operand: " +operand+ " is an array, which cannot be compared using value comparison"
            );
        }
        return null;
    }


    public XQueryValue add(final XQueryValue o1, final XQueryValue o2) {
        // Atomization is applied to each operand. The result of this operation is called the atomized operand.
        final List<XQueryValue> atomized1 = atomizer.atomize(o1);
        final List<XQueryValue> atomized2 = atomizer.atomize(o2);

        // If an atomized operand is an empty sequence, the result of the value comparison is an empty sequence, and the implementation need not evaluate the other operand or apply the operator.
        // However, an implementation may choose to evaluate the other operand in order to determine whether it raises an error.
        if (atomized1.isEmpty())
            return o1;

        if (atomized2.isEmpty())
            return o2;

        final var err1 = validateOperand(o1, atomized1);
        if (err1 != null)
            return err1;
        final var err2 = validateOperand(o2, atomized2);
        if (err2 != null)
            return err2;

        if (o1.isBoolean && o2.isBoolean) {
            return valueFactory.bool(o1.booleanValue.equals(o2.booleanValue));
        }

        if (o1.isNumeric && o2.isNumeric) {
            return valueFactory.bool(o1.numericValue.equals(o2.numericValue));
        }

        return valueFactory.bool(o1.stringValue.equals(o2.stringValue));
    }

    public XQueryValue subtract(final XQueryValue o1, final XQueryValue o2) {
        // Atomization is applied to each operand. The result of this operation is called the atomized operand.
        final List<XQueryValue> atomized1 = atomizer.atomize(o1);
        final List<XQueryValue> atomized2 = atomizer.atomize(o2);

        // If an atomized operand is an empty sequence, the result of the value comparison is an empty sequence, and the implementation need not evaluate the other operand or apply the operator.
        // However, an implementation may choose to evaluate the other operand in order to determine whether it raises an error.
        if (atomized1.isEmpty())
            return o1;

        if (atomized2.isEmpty())
            return o2;

        final var err1 = validateOperand(o1, atomized1);
        if (err1 != null)
            return err1;
        final var err2 = validateOperand(o2, atomized2);
        if (err2 != null)
            return err2;

        if (o1.isBoolean && o2.isBoolean) {
            return valueFactory.bool(o1.booleanValue.compareTo(o2.booleanValue) != 0);
        }

        if (o1.isNumeric && o2.isNumeric) {
            return valueFactory.bool(o1.numericValue.compareTo(o2.numericValue) != 0);
        }

        return valueFactory.bool(!o1.stringValue.equals(o2.stringValue));
    }



    public XQueryValue multiply(final XQueryValue o1, final XQueryValue o2) {
        // Atomization is applied to each operand. The result of this operation is called the atomized operand.
        final List<XQueryValue> atomized1 = atomizer.atomize(o1);
        final List<XQueryValue> atomized2 = atomizer.atomize(o2);

        // If an atomized operand is an empty sequence, the result of the value comparison is an empty sequence, and the implementation need not evaluate the other operand or apply the operator.
        // However, an implementation may choose to evaluate the other operand in order to determine whether it raises an error.
        if (atomized1.isEmpty())
            return o1;

        if (atomized2.isEmpty())
            return o2;

        final var err1 = validateOperand(o1, atomized1);
        if (err1 != null)
            return err1;
        final var err2 = validateOperand(o2, atomized2);
        if (err2 != null)
            return err2;


        if (o1.isBoolean && o2.isBoolean) {
            return valueFactory.bool(o1.booleanValue == false && o2.booleanValue == true);
        }

        if (o1.isNumeric && o2.isNumeric) {
            return valueFactory.bool(o1.numericValue.compareTo(o2.numericValue) < 0);
        }

        return valueFactory.bool(o1.stringValue.compareTo(o2.stringValue) < 0);
    }


    public XQueryValue divide(final XQueryValue o1, final XQueryValue o2) {
        // Atomization is applied to each operand. The result of this operation is called the atomized operand.
        final List<XQueryValue> atomized1 = atomizer.atomize(o1);
        final List<XQueryValue> atomized2 = atomizer.atomize(o2);

        // If an atomized operand is an empty sequence, the result of the value comparison is an empty sequence, and the implementation need not evaluate the other operand or apply the operator.
        // However, an implementation may choose to evaluate the other operand in order to determine whether it raises an error.
        if (atomized1.isEmpty())
            return o1;

        if (atomized2.isEmpty())
            return o2;

        final var err1 = validateOperand(o1, atomized1);
        if (err1 != null)
            return err1;
        final var err2 = validateOperand(o2, atomized2);
        if (err2 != null)
            return err2;

        if (o1.isBoolean && o2.isBoolean) {
            return valueFactory.bool(o1.booleanValue.compareTo(o2.booleanValue) > 0);
        }

        if (o1.isNumeric && o2.isNumeric) {
            return valueFactory.bool(o1.numericValue.compareTo(o2.numericValue) > 0);
        }

        return valueFactory.bool(o1.stringValue.compareTo(o2.stringValue) > 0);
    }


    public XQueryValue unary(final XQueryValue o1, final XQueryValue o2) {
        // Atomization is applied to each operand. The result of this operation is called the atomized operand.
        final List<XQueryValue> atomized1 = atomizer.atomize(o1);
        final List<XQueryValue> atomized2 = atomizer.atomize(o2);

        // If an atomized operand is an empty sequence, the result of the value comparison is an empty sequence, and the implementation need not evaluate the other operand or apply the operator.
        // However, an implementation may choose to evaluate the other operand in order to determine whether it raises an error.
        if (atomized1.isEmpty())
            return o1;

        if (atomized2.isEmpty())
            return o2;

        final var err1 = validateOperand(o1, atomized1);
        if (err1 != null)
            return err1;
        final var err2 = validateOperand(o2, atomized2);
        if (err2 != null)
            return err2;

        if (o1.isBoolean && o2.isBoolean) {
            return valueFactory.bool(o1.booleanValue.compareTo(o2.booleanValue) <= 0);
        }

        if (o1.isNumeric && o2.isNumeric) {
            return valueFactory.bool(o1.numericValue.compareTo(o2.numericValue) <= 0);
        }

        return valueFactory.bool(o1.stringValue.compareTo(o2.stringValue) <= 0);
    }


    public XQueryValue valueGreaterEqual(final XQueryValue o1, final XQueryValue o2) {
        // Atomization is applied to each operand. The result of this operation is called the atomized operand.
        final List<XQueryValue> atomized1 = atomizer.atomize(o1);
        final List<XQueryValue> atomized2 = atomizer.atomize(o2);

        // If an atomized operand is an empty sequence, the result of the value comparison is an empty sequence, and the implementation need not evaluate the other operand or apply the operator.
        // However, an implementation may choose to evaluate the other operand in order to determine whether it raises an error.
        if (atomized1.isEmpty())
            return o1;

        if (atomized2.isEmpty())
            return o2;

        final var err1 = validateOperand(o1, atomized1);
        if (err1 != null)
            return err1;
        final var err2 = validateOperand(o2, atomized2);
        if (err2 != null)
            return err2;

        if (o1.isBoolean && o2.isBoolean) {
            return valueFactory.bool(o1.booleanValue.compareTo(o2.booleanValue) >= 0);
        }

        if (o1.isNumeric && o2.isNumeric) {
            return valueFactory.bool(o1.numericValue.compareTo(o2.numericValue) >= 0);
        }

        return valueFactory.bool(o1.stringValue.compareTo(o2.stringValue) >= 0);
    }



}
