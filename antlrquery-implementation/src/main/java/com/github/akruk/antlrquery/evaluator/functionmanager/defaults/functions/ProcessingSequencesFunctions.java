package com.github.akruk.antlrquery.evaluator.functionmanager.defaults.functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.antlr.v4.runtime.Parser;
import com.github.akruk.antlrquery.evaluator.AntlrQueryVisitingContext;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;
import com.github.akruk.antlrquery.evaluator.values.operations.ValueAtomizer;

public class ProcessingSequencesFunctions {

    private final AntlrQueryValueFactory valueFactory;
    private final ValueAtomizer atomizer;
    public ProcessingSequencesFunctions(final AntlrQueryValueFactory valueFactory, final Parser targetParser, final ValueAtomizer atomizer) {
        this.valueFactory = valueFactory;
        this.atomizer = atomizer;
    }


    public AntlrQueryValue empty(final AntlrQueryVisitingContext ctx, final List<AntlrQueryValue> args) {
        final var input = args.get(0);

        return valueFactory.bool(input.isEmptySequence);
    }

    public AntlrQueryValue exists(final AntlrQueryVisitingContext ctx, final List<AntlrQueryValue> args) {
        final var input = args.get(0);

        return valueFactory.bool(!input.isEmptySequence);
    }

    public AntlrQueryValue foot(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        final var input = args.get(0);

        final var sequence = atomizer.atomize(input);
        return sequence.isEmpty() ? valueFactory.emptySequence() : sequence.get(sequence.size() - 1);
    }

    public AntlrQueryValue head(final AntlrQueryVisitingContext ctx, final List<AntlrQueryValue> args) {
        final var input = args.get(0);
        final var sequence = input.sequence;
        return sequence.isEmpty() ? valueFactory.emptySequence() : sequence.get(0);
    }

    public AntlrQueryValue identity(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        return args.get(0);
    }

    public AntlrQueryValue insertBefore(final AntlrQueryVisitingContext ctx, final List<AntlrQueryValue> args) {
        final var input = args.get(0);
        final var positionArg = args.get(1);
        final var insertArg = args.get(2);

        if (!positionArg.isNumeric) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");

        final var sequence = input.sequence;
        final var insertSequence = insertArg.sequence;
        final int position = positionArg.numericValue.intValue() - 1; // Convert to 0-based

        final var result = new ArrayList<AntlrQueryValue>();

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

    public AntlrQueryValue itemsAt(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        if (args.size() != 2) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        final var input = args.get(0);
        final var positions = args.get(1);

        final var sequence = input.sequence;
        final var result = new ArrayList<AntlrQueryValue>();

        for (final var pos : positions.sequence) {
            if (!pos.isNumeric) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
            final int index = pos.numericValue.intValue() - 1; // XQuery uses 1-based indexing
            if (index >= 0 && index < sequence.size()) {
                result.add(sequence.get(index));
            }
        }

        return valueFactory.sequence(result);
    }

    public AntlrQueryValue remove(final AntlrQueryVisitingContext ctx, final List<AntlrQueryValue> args) {
        if (args.size() != 2) return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");
        final var input = args.get(0);
        final var positionsArg = args.get(1);

        final var sequence = input.sequence;
        final var positionsToRemove = new HashSet<Integer>();

        final var atomized = atomizer.atomize(positionsArg);
        for (final var pos : atomized) {
            if (!pos.isNumeric) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
            final int index = pos.numericValue.intValue() - 1; // Convert to 0-based
            if (index >= 0 && index < sequence.size()) {
                positionsToRemove.add(index);
            }
        }

        final var result = new ArrayList<AntlrQueryValue>();
        for (int i = 0; i < sequence.size(); i++) {
            if (!positionsToRemove.contains(i)) {
                result.add(sequence.get(i));
            }
        }

        return valueFactory.sequence(result);
    }

    public AntlrQueryValue replicate(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        final var input = args.get(0);
        final var countArg = args.get(1);

        if (!countArg.isNumeric) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        final int count = countArg.numericValue.intValue();
        if (count < 0) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");
        if (count == 0) return valueFactory.emptySequence();

        final var result = new ArrayList<AntlrQueryValue>();
        for (int i = 0; i < count; i++) {
            result.addAll(input.sequence);
        }

        return valueFactory.sequence(result);
    }

    public AntlrQueryValue reverse(final AntlrQueryVisitingContext ctx, final List<AntlrQueryValue> args) {
        final var input = args.get(0);

        final var sequence = input.sequence;
        return valueFactory.sequence(sequence.reversed());
    }

    public AntlrQueryValue sequenceJoin(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        final var input = args.get(0);
        final var separator = args.get(1);

        final var sequence = input.sequence;
        if (sequence.isEmpty()) return valueFactory.emptySequence();
        if (sequence.size() == 1) return sequence.get(0);

        final var result = new ArrayList<AntlrQueryValue>();
        for (int i = 0; i < sequence.size(); i++) {
            if (i > 0) {
                result.addAll(separator.sequence);
            }
            result.add(sequence.get(i));
        }

        return valueFactory.sequence(result);
    }

    public AntlrQueryValue slice(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args) {

        final AntlrQueryValue input = args.get(0);
        final AntlrQueryValue startArg = args.get(1);
        final AntlrQueryValue endArg = args.get(2);
        final AntlrQueryValue stepArg = args.get(3);

        final List<AntlrQueryValue> sequence = input.sequence;
        final int count = sequence.size();

        if (count == 0) {
            return valueFactory.emptySequence();
        }

        // Validate types
        if (!startArg.isEmptySequence && !startArg.isNumeric)
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");

        if (!endArg.isEmptySequence && !endArg.isNumeric)
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");

        if (!stepArg.isEmptySequence && !stepArg.isNumeric)
            return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");

        // Compute S
        int S;
        if (startArg.isEmptySequence || startArg.numericValue.intValue() == 0) {
            S = 1;
        } else if (startArg.numericValue.intValue() < 0) {
            S = count + startArg.numericValue.intValue() + 1;
        } else {
            S = startArg.numericValue.intValue();
        }

        // Compute E
        int E;
        if (endArg.isEmptySequence || endArg.numericValue.intValue() == 0) {
            E = count;
        } else if (endArg.numericValue.intValue() < 0) {
            E = count + endArg.numericValue.intValue() + 1;
        } else {
            E = endArg.numericValue.intValue();
        }

        // Compute STEP
        int STEP;
        if (stepArg.isEmptySequence || stepArg.numericValue.intValue() == 0) {
            STEP = (E >= S) ? 1 : -1;
        } else {
            STEP = stepArg.numericValue.intValue();
        }

        // If negative STEP → reverse + slice(-S, -E, -STEP)
        if (STEP < 0) {
            final List<AntlrQueryValue> reversed = new ArrayList<>(sequence);
            Collections.reverse(reversed);
            final AntlrQueryValue reversedInput = valueFactory.sequence(reversed);

            final List<AntlrQueryValue> reversedArgs = new ArrayList<>();
            reversedArgs.add(reversedInput);
            reversedArgs.add(valueFactory.number(-S));
            reversedArgs.add(valueFactory.number(-E));
            reversedArgs.add(valueFactory.number(-STEP));

            return slice(context, reversedArgs);
        }

        // Apply selection based on position
        final List<AntlrQueryValue> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            final int pos = i + 1;
            if (pos >= S && pos <= E && ((pos - S) % STEP == 0)) {
                result.add(sequence.get(i));
            }
        }

        return valueFactory.sequence(result);
    }

    public AntlrQueryValue subsequence(final AntlrQueryVisitingContext ctx, final List<AntlrQueryValue> args) {
        final var input = args.get(0);
        final var startArg = args.get(1);
        final var lengthArg = args.get(2);

        if (!startArg.isNumeric) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");

        final var sequence = input.sequence;
        final int start = startArg.numericValue.intValue() - 1; // Convert to 0-based
        if (lengthArg.isEmptySequence) {
            // No cardinality specified, take from start to end
            if (start >= sequence.size() || start < 0) {
                return valueFactory.emptySequence();
            }
            return valueFactory.sequence(sequence.subList(start, sequence.size()));

        }
        if (!lengthArg.isNumeric) return valueFactory.error(AntlrQueryError.InvalidArgumentType, "");

        final int length = lengthArg.numericValue.intValue();
        if (length <= 0 || start >= sequence.size() || start < 0) {
            return valueFactory.emptySequence();
        }

        final int end = Math.min(start + length, sequence.size());
        return valueFactory.sequence(sequence.subList(start, end));
    }

    public AntlrQueryValue tail(final AntlrQueryVisitingContext ctx, final List<AntlrQueryValue> args) {
        final var input = args.get(0);

        final var sequence = input.sequence;
        if (sequence.isEmpty()) return valueFactory.emptySequence();

        return valueFactory.sequence(sequence.subList(1, sequence.size()));
    }

    public AntlrQueryValue trunk(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args) {
        final var input = args.get(0);

        final var sequence = input.sequence;
        if (sequence.isEmpty()) return valueFactory.emptySequence();
        if (sequence.size() == 1) return valueFactory.emptySequence();

        return valueFactory.sequence(sequence.subList(0, sequence.size() - 1));
    }

    public AntlrQueryValue voidFunction(final AntlrQueryVisitingContext context, final List<AntlrQueryValue> args)
    {
        if (args.size() > 1)
            return valueFactory.error(AntlrQueryError.WrongNumberOfArguments, "");

        // fn:void always returns empty sequence regardless of input
        return valueFactory.emptySequence();
    }

}
