package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.antlr.v4.runtime.Parser;
import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.evaluator.values.XQueryError;
import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

public class ProcessingSequencesFunctions {

    private final XQueryValueFactory valueFactory;
    public ProcessingSequencesFunctions(final XQueryValueFactory valueFactory, final Parser targetParser) {
        this.valueFactory = valueFactory;
    }


    public XQueryValue empty(XQueryVisitingContext ctx, List<XQueryValue> args) {
        var input = args.get(0);

        return valueFactory.bool(input.isEmptySequence());
    }

    public XQueryValue exists(XQueryVisitingContext ctx, List<XQueryValue> args) {
        var input = args.get(0);

        return valueFactory.bool(!input.isEmptySequence());
    }

    public XQueryValue foot(XQueryVisitingContext context, List<XQueryValue> args) {
        var input = args.get(0);

        var sequence = input.atomize();
        return sequence.isEmpty() ? valueFactory.emptySequence() : sequence.get(sequence.size() - 1);
    }

    public XQueryValue head(XQueryVisitingContext ctx, List<XQueryValue> args) {
        var input = args.get(0);

        var sequence = input.atomize();
        return sequence.isEmpty() ? valueFactory.emptySequence() : sequence.get(0);
    }

    public XQueryValue identity(XQueryVisitingContext context, List<XQueryValue> args) {
        return args.get(0);
    }

    public XQueryValue insertBefore(XQueryVisitingContext ctx, List<XQueryValue> args) {
        var input = args.get(0);
        var positionArg = args.get(1);
        var insertArg = args.get(2);

        if (!positionArg.isNumericValue()) return XQueryError.InvalidArgumentType;

        var sequence = input.sequence();
        var insertSequence = insertArg.sequence();
        int position = positionArg.numericValue().intValue() - 1; // Convert to 0-based

        var result = new ArrayList<XQueryValue>();

        // Add elements before position
        for (int i = 0; i < Math.min(position, sequence.size()); i++) {
            result.add(sequence.get(i));
        }

        // Add inserted elements
        result.addAll(insertSequence);

        // Add remaining elements
        for (int i = Math.max(0, position); i < sequence.size(); i++) {
            result.add(sequence.get(i));
        }

        return valueFactory.sequence(result);
    }

    public XQueryValue itemsAt(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        var input = args.get(0);
        var positions = args.get(1);

        var sequence = input.sequence();
        var result = new ArrayList<XQueryValue>();

        for (var pos : positions.sequence()) {
            if (!pos.isNumericValue()) return XQueryError.InvalidArgumentType;
            int index = pos.numericValue().intValue() - 1; // XQuery uses 1-based indexing
            if (index >= 0 && index < sequence.size()) {
                result.add(sequence.get(index));
            }
        }

        return valueFactory.sequence(result);
    }

    public XQueryValue remove(XQueryVisitingContext ctx, List<XQueryValue> args) {
        if (args.size() != 2) return XQueryError.WrongNumberOfArguments;
        var input = args.get(0);
        var positionsArg = args.get(1);

        var sequence = input.sequence();
        var positionsToRemove = new HashSet<Integer>();

        for (var pos : positionsArg.atomize()) {
            if (!pos.isNumericValue()) return XQueryError.InvalidArgumentType;
            int index = pos.numericValue().intValue() - 1; // Convert to 0-based
            if (index >= 0 && index < sequence.size()) {
                positionsToRemove.add(index);
            }
        }

        var result = new ArrayList<XQueryValue>();
        for (int i = 0; i < sequence.size(); i++) {
            if (!positionsToRemove.contains(i)) {
                result.add(sequence.get(i));
            }
        }

        return valueFactory.sequence(result);
    }

    public XQueryValue replicate(XQueryVisitingContext context, List<XQueryValue> args) {
        var input = args.get(0);
        var countArg = args.get(1);

        if (!countArg.isNumericValue()) return XQueryError.InvalidArgumentType;
        int count = countArg.numericValue().intValue();
        if (count < 0) return XQueryError.InvalidArgumentType;
        if (count == 0) return valueFactory.emptySequence();

        var result = new ArrayList<XQueryValue>();
        for (int i = 0; i < count; i++) {
            result.addAll(input.sequence());
        }

        return valueFactory.sequence(result);
    }

    public XQueryValue reverse(XQueryVisitingContext ctx, List<XQueryValue> args) {
        var input = args.get(0);

        var sequence = input.sequence();
        return valueFactory.sequence(sequence.reversed());
    }

    public XQueryValue sequenceJoin(XQueryVisitingContext context, List<XQueryValue> args) {
        var input = args.get(0);
        var separator = args.get(1);

        var sequence = input.sequence();
        if (sequence.isEmpty()) return valueFactory.emptySequence();
        if (sequence.size() == 1) return sequence.get(0);

        var result = new ArrayList<XQueryValue>();
        for (int i = 0; i < sequence.size(); i++) {
            if (i > 0) {
                result.addAll(separator.sequence());
            }
            result.add(sequence.get(i));
        }

        return valueFactory.sequence(result);
    }

    public XQueryValue slice(
            XQueryVisitingContext context,
            List<XQueryValue> args) {

        XQueryValue input = args.get(0);
        XQueryValue startArg = args.get(1);
        XQueryValue endArg = args.get(2);
        XQueryValue stepArg = args.get(3);

        List<XQueryValue> sequence = input.sequence();
        int count = sequence.size();

        if (count == 0) {
            return valueFactory.emptySequence();
        }

        // Validate types
        if (!startArg.isEmptySequence() && !startArg.isNumericValue())
            return XQueryError.InvalidArgumentType;

        if (!endArg.isEmptySequence() && !endArg.isNumericValue())
            return XQueryError.InvalidArgumentType;

        if (!stepArg.isEmptySequence() && !stepArg.isNumericValue())
            return XQueryError.InvalidArgumentType;

        // Compute S
        int S;
        if (startArg.isEmptySequence() || startArg.numericValue().intValue() == 0) {
            S = 1;
        } else if (startArg.numericValue().intValue() < 0) {
            S = count + startArg.numericValue().intValue() + 1;
        } else {
            S = startArg.numericValue().intValue();
        }

        // Compute E
        int E;
        if (endArg.isEmptySequence() || endArg.numericValue().intValue() == 0) {
            E = count;
        } else if (endArg.numericValue().intValue() < 0) {
            E = count + endArg.numericValue().intValue() + 1;
        } else {
            E = endArg.numericValue().intValue();
        }

        // Compute STEP
        int STEP;
        if (stepArg.isEmptySequence() || stepArg.numericValue().intValue() == 0) {
            STEP = (E >= S) ? 1 : -1;
        } else {
            STEP = stepArg.numericValue().intValue();
        }

        // If negative STEP → reverse + slice(-S, -E, -STEP)
        if (STEP < 0) {
            List<XQueryValue> reversed = new ArrayList<>(sequence);
            Collections.reverse(reversed);
            XQueryValue reversedInput = valueFactory.sequence(reversed);

            List<XQueryValue> reversedArgs = new ArrayList<>();
            reversedArgs.add(reversedInput);
            reversedArgs.add(valueFactory.number(-S));
            reversedArgs.add(valueFactory.number(-E));
            reversedArgs.add(valueFactory.number(-STEP));

            return slice(context, reversedArgs);
        }

        // Apply selection based on position
        List<XQueryValue> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int pos = i + 1;
            if (pos >= S && pos <= E && ((pos - S) % STEP == 0)) {
                result.add(sequence.get(i));
            }
        }

        return valueFactory.sequence(result);
    }

    public XQueryValue subsequence(XQueryVisitingContext ctx, List<XQueryValue> args) {
        var input = args.get(0);
        var startArg = args.get(1);
        var lengthArg = args.get(2);

        if (!startArg.isNumericValue()) return XQueryError.InvalidArgumentType;

        var sequence = input.sequence();
        int start = startArg.numericValue().intValue() - 1; // Convert to 0-based
        if (lengthArg.isEmptySequence()) {
            // No length specified, take from start to end
            if (start >= sequence.size() || start < 0) {
                return valueFactory.emptySequence();
            }
            return valueFactory.sequence(sequence.subList(start, sequence.size()));

        }
        if (!lengthArg.isNumericValue()) return XQueryError.InvalidArgumentType;

        int length = lengthArg.numericValue().intValue();
        if (length <= 0 || start >= sequence.size() || start < 0) {
            return valueFactory.emptySequence();
        }

        int end = Math.min(start + length, sequence.size());
        return valueFactory.sequence(sequence.subList(start, end));
    }

    public XQueryValue tail(XQueryVisitingContext ctx, List<XQueryValue> args) {
        var input = args.get(0);

        var sequence = input.atomize();
        if (sequence.isEmpty()) return valueFactory.emptySequence();

        return valueFactory.sequence(sequence.subList(1, sequence.size()));
    }

    public XQueryValue trunk(XQueryVisitingContext context, List<XQueryValue> args) {
        var input = args.get(0);

        var sequence = input.atomize();
        if (sequence.isEmpty()) return valueFactory.emptySequence();
        if (sequence.size() == 1) return valueFactory.emptySequence();

        return valueFactory.sequence(sequence.subList(0, sequence.size() - 1));
    }

    public XQueryValue unordered(XQueryVisitingContext context, List<XQueryValue> args) {

        // fn:unordered simply returns the input sequence as-is
        // It's a hint to the processor that order doesn't matter
        return args.get(0);
    }

    public XQueryValue voidFunction(XQueryVisitingContext context, List<XQueryValue> args) {
        if (args.size() > 1) return XQueryError.WrongNumberOfArguments;

        // fn:void always returns empty sequence regardless of input
        return valueFactory.emptySequence();
    }

}
