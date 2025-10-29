package com.github.akruk.antlrxquery.evaluator.values.operations;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;

public class ValueAtomizer {

    public List<XQueryValue> atomize(final XQueryValue value) {
        final List<XQueryValue> result = new ArrayList<>();
        final Queue<XQueryValue> queue = new LinkedList<>();
        queue.add(value);

        while (!queue.isEmpty()) {
            final XQueryValue current = queue.poll();

            if (current.isEmptySequence) continue;

            if (current.isArray) {
                queue.addAll(current.arrayMembers);
            } else if (current.isMap) {
                queue.addAll(current.mapEntries.values());
            } else if (current.size == 1) {
                result.add(current.sequence.get(0));
            } else {
                queue.addAll(current.sequence);
            }
        }

        return result;
    }

}
