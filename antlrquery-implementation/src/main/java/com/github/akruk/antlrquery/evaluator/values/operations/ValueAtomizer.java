package com.github.akruk.antlrquery.evaluator.values.operations;

import java.util.*;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;

public class ValueAtomizer {

    public List<AntlrQueryValue> atomize(
            final AntlrQueryValue value) {

        final List<AntlrQueryValue> result = new ArrayList<>();
        final LinkedList<AntlrQueryValue> queue = new LinkedList<>();
        queue.add(value);

        while (!queue.isEmpty()) {
            final AntlrQueryValue current = queue.poll();

            if (current.isEmptySequence) {
                continue;
            }

            if (current.isArray) {
                for (int i = current.arrayMembers.size() - 1; i >= 0; i--) {
                    queue.addFirst(current.arrayMembers.get(i));
                }
            } else if (current.size == 1) {
                result.add(current.sequence.getFirst());
            } else {
                for (int i = current.sequence.size() - 1; i >= 0; i--) {
                    queue.addFirst(current.sequence.get(i));
                }
            }
        }

        return result;
    }
}
