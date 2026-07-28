package com.github.akruk.antlrquery.evaluator.values.operations;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;

public class ValueAtomizer {

    public List<AntlrQueryValue> atomize(final AntlrQueryValue value) {
        final List<AntlrQueryValue> result = new ArrayList<>();
        final Queue<AntlrQueryValue> queue = new LinkedList<>();
        queue.add(value);

        while (!queue.isEmpty()) {
            final AntlrQueryValue current = queue.poll();

            if (current.isEmptySequence) continue;

            if (current.isArray) {
                queue.addAll(current.arrayMembers);
            } else if (current.isMap) {
                queue.addAll(current.mapEntries.values());
            } else if (current.size == 1) {
                result.add(current.sequence.getFirst());
            } else {
                queue.addAll(current.sequence);
            }
        }

        return result;
    }

}
