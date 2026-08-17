package com.github.akruk.antlrquery.evaluator.functionmanager.functions;

import com.github.akruk.antlrquery.evaluator.AntlrQueryVisitingContext;
import com.github.akruk.antlrquery.evaluator.functionmanager.EvaluatingFunctionManager;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryError;
import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;

import java.util.*;

public class ArrayFunctions {
    private final AntlrQueryValueFactory valueFactory;
    private final EvaluatingFunctionManager functionManager;

    public ArrayFunctions(
            final AntlrQueryValueFactory valueFactory, EvaluatingFunctionManager functionManager)
    {
        this.valueFactory = valueFactory;
        this.functionManager = functionManager;
    }



    public AntlrQueryValue append(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var array = args.get(0);
        var element = args.get(1);
        if (!array.isArray) {
            return valueFactory.error(
                    AntlrQueryError.InvalidArgumentType,
                    "array:append expects array as argument, received: " + array);
        }
        ArrayList<AntlrQueryValue> newArray = new ArrayList<>(array.arrayMembers.size() + 1);
        newArray.addAll(array.arrayMembers);
        newArray.add(element);
        return valueFactory.array(newArray);
    }



    public AntlrQueryValue build(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        var input = args.get(0);
        var action = args.get(1);

        List<AntlrQueryValue> result = new ArrayList<>(input.sequence.size());
        int pos = 1;
        for (AntlrQueryValue item : input.sequence) {
            var applied = action.functionValue.call(
                    context,
                    List.of(item, valueFactory.number(pos))
            );
            result.add(AntlrQueryValue.sequence(applied.sequence, applied.type));
            pos++;
        }

        return AntlrQueryValue.array(result, input.type);
    }


    public AntlrQueryValue empty(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var input = args.getFirst();
        return valueFactory.bool(input.arrayMembers.isEmpty());
    }

    public AntlrQueryValue filter(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        var array = args.get(0);
        var predicate = args.get(1);

        List<AntlrQueryValue> result = new ArrayList<>(array.arrayMembers.size());
        int pos = 1;

        for (AntlrQueryValue member : array.arrayMembers) {

            var applied = predicate.functionValue.call(
                    context,
                    List.of(member, valueFactory.number(pos))
            );

            if (applied.isError) {
                return applied;
            }

            boolean keep;

            if (applied.isEmptySequence) {
                keep = false;
            } else if (applied.isBoolean) {
                keep = applied.booleanValue;
            } else {
                return valueFactory.error(
                        AntlrQueryError.InvalidArgumentType,
                        "array:filter predicate must return xs:boolean or empty sequence"
                );
            }

            if (keep) {
                result.add(member);
            }

            pos++;
        }

        return valueFactory.array(result);
    }

    public AntlrQueryValue flatten(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var input = args.getFirst();

        List<AntlrQueryValue> work = input.sequence;
        List<AntlrQueryValue> out = new ArrayList<>(work.size());

        boolean hasArray = true;

        while (hasArray) {
            hasArray = false;
            out.clear();

            for (AntlrQueryValue item : work) {
                if (item.isArray) {
                    hasArray = true;
                    out.addAll(item.arrayMembers);
                } else {
                    out.add(item);
                }
            }

            work = out;
        }

        return valueFactory.sequence(work);
    }


    public AntlrQueryValue foldLeft(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        var array = args.get(0);
        var acc = args.get(1);
        var action = args.get(2);

        for (AntlrQueryValue member : array.arrayMembers) {
            acc = action.functionValue.call(
                    context,
                    List.of(acc, member)
            );
            if (acc.isError) {
                return acc;
            }
        }

        return acc;
    }


    public AntlrQueryValue foldRight(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        var array = args.get(0);
        var acc = args.get(1);
        var action = args.get(2);

        for (int i = array.arrayMembers.size() - 1; i >= 0; i--) {
            var member = array.arrayMembers.get(i);
            acc = action.functionValue.call(
                    context,
                    List.of(member, acc)
            );
            if (acc.isError) {
                return acc;
            }
        }

        return acc;
    }


    public AntlrQueryValue foot(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var array = args.getFirst();

        if (array.arrayMembers.isEmpty()) {
            return valueFactory.error(
                    AntlrQueryError.ArrayIndexOutOfBounds,
                    "array:foot called on empty array"
            );
        }

        return array.arrayMembers.getLast();
    }


    public AntlrQueryValue forEach(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        var array = args.get(0);
        var action = args.get(1);

        List<AntlrQueryValue> result = new ArrayList<>(array.arrayMembers.size());
        int pos = 1;

        for (AntlrQueryValue member : array.arrayMembers) {
            var applied = action.functionValue.call(
                    context,
                    List.of(member, valueFactory.number(pos))
            );

            if (applied.isError) {
                return applied;
            }

            result.add(valueFactory.sequence(applied.sequence));

            pos++;
        }

        return valueFactory.array(result);
    }

    public AntlrQueryValue reverse(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var array = args.getFirst();
        int size = array.arrayMembers.size();

        List<AntlrQueryValue> result = new ArrayList<>(size);

        for (int i = size - 1; i >= 0; i--) {
            result.add(array.arrayMembers.get(i));
        }

        return valueFactory.array(result);
    }

    public AntlrQueryValue forEachPair(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        var a1 = args.get(0);
        var a2 = args.get(1);
        var action = args.get(2);

        int size = Math.min(a1.arrayMembers.size(), a2.arrayMembers.size());
        List<AntlrQueryValue> result = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            var m1 = a1.arrayMembers.get(i);
            var m2 = a2.arrayMembers.get(i);

            var applied = action.functionValue.call(
                    context,
                    List.of(m1, m2, valueFactory.number(i + 1))
            );

            if (applied.isError) {
                return applied;
            }

            result.add(valueFactory.sequence(applied.sequence));
        }

        return valueFactory.array(result);
    }


    public AntlrQueryValue get(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var array = args.get(0);
        var pos = args.get(1).numericValue.intValueExact();

        if (args.size() == 2) {
            if (pos < 1 || pos > array.arrayMembers.size()) {
                return valueFactory.error(
                        AntlrQueryError.InvalidArgumentType,
                        "array:get position out of bounds"
                );
            }
            return array.arrayMembers.get(pos - 1);
        }

        var def = args.get(2);

        if (pos < 1 || pos > array.arrayMembers.size()) {
            return def;
        }

        return array.arrayMembers.get(pos - 1);
    }


    public AntlrQueryValue insertBefore(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var array = args.get(0);
        int position = args.get(1).numericValue.intValueExact();
        var member = args.get(2);

        int size = array.arrayMembers.size();

        if (position < 1 || position > size + 1) {
            return valueFactory.error(
                    AntlrQueryError.InvalidArgumentType,
                    "array:insert-before position out of bounds"
            );
        }

        List<AntlrQueryValue> result = new ArrayList<>(size + 1);

        for (int i = 0; i < position - 1; i++) {
            result.add(array.arrayMembers.get(i));
        }

        result.add(member);

        for (int i = position - 1; i < size; i++) {
            result.add(array.arrayMembers.get(i));
        }

        return valueFactory.array(result);
    }

    public AntlrQueryValue subarray(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var array = args.get(0);
        int start = args.get(1).numericValue.intValueExact();
        int length = args.get(2).numericValue.intValueExact();

        int size = array.arrayMembers.size();

        if (length <= 0) {
            return valueFactory.array(List.of());
        }

        if (start < 1 || start > size) {
            return valueFactory.array(List.of());
        }

        int end = Math.min(size, start + length - 1);

        List<AntlrQueryValue> result = new ArrayList<>(end - start + 1);

        for (int i = start - 1; i < end; i++) {
            result.add(array.arrayMembers.get(i));
        }

        return valueFactory.array(result);
    }
    public AntlrQueryValue head(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var array = args.getFirst();

        if (array.arrayMembers.isEmpty()) {
            return valueFactory.error(
                    AntlrQueryError.InvalidArgumentType,
                    "array:head called on empty array"
            );
        }

        return array.arrayMembers.getFirst();
    }



    public AntlrQueryValue indexOf(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var array = args.get(0);
        var target = args.get(1);

        List<AntlrQueryValue> result = new ArrayList<>();

        int pos = 1;
        for (AntlrQueryValue member : array.arrayMembers) {
            if (member.equals(target)) {
                result.add(valueFactory.number(pos));
            }
            pos++;
        }

        return valueFactory.sequence(result);
    }


    public AntlrQueryValue indexWhere(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        var array = args.get(0);
        var predicate = args.get(1);

        List<AntlrQueryValue> result = new ArrayList<>();
        int pos = 1;

        for (AntlrQueryValue member : array.arrayMembers) {
            var applied = predicate.functionValue.call(
                    context,
                    List.of(member, valueFactory.number(pos))
            );

            if (applied.isError) {
                return applied;
            }

            boolean keep;

            if (applied.isEmptySequence) {
                keep = false;
            } else if (applied.isBoolean) {
                keep = applied.booleanValue;
            } else {
                return valueFactory.error(
                        AntlrQueryError.InvalidArgumentType,
                        "array:index-where predicate must return xs:boolean or empty sequence"
                );
            }

            if (keep) {
                result.add(valueFactory.number(pos));
            }

            pos++;
        }

        return valueFactory.sequence(result);
    }

    public AntlrQueryValue items(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var array = args.getFirst();

        List<AntlrQueryValue> out = new ArrayList<>(array.arrayMembers.size());

        for (AntlrQueryValue member : array.arrayMembers) {
            out.addAll(member.sequence);
        }

        return valueFactory.sequence(out);
    }

    public AntlrQueryValue put(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var array = args.get(0);
        var position = args.get(1).numericValue.intValueExact();
        var member = args.get(2);

        int size = array.arrayMembers.size();

        if (position < 1 || position > size) {
            return valueFactory.error(
                    AntlrQueryError.InvalidArgumentType,
                    "array:put position out of bounds"
            );
        }

        List<AntlrQueryValue> result = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            if (i + 1 == position) {
                result.add(member);
            } else {
                result.add(array.arrayMembers.get(i));
            }
        }

        return valueFactory.array(result);
    }

    public AntlrQueryValue join(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var arrays = args.getFirst().sequence;

        List<AntlrQueryValue> out = new ArrayList<>();

        for (AntlrQueryValue a : arrays) {
            out.addAll(a.arrayMembers);
        }

        return valueFactory.array(out);
    }


    public AntlrQueryValue members(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var array = args.getFirst();

        List<AntlrQueryValue> out = new ArrayList<>(array.arrayMembers.size());

        for (AntlrQueryValue member : array.arrayMembers) {
            Map<AntlrQueryValue, AntlrQueryValue> map =
                    Map.of(valueFactory.string("value"), member);
            out.add(valueFactory.map(map));
        }

        return valueFactory.sequence(out);
    }


    public AntlrQueryValue ofMembers(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var input = args.getFirst().sequence;

        List<AntlrQueryValue> out = new ArrayList<>(input.size());

        for (AntlrQueryValue record : input) {
            var value = record.mapEntries.get(valueFactory.string("value"));
            out.add(value);
        }

        return valueFactory.array(out);
    }


    public AntlrQueryValue remove(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var array = args.get(0);
        var positions = args.get(1).sequence;

        int size = array.arrayMembers.size();

        if (!positions.isEmpty()) {
            for (AntlrQueryValue p : positions) {
                int pos = p.numericValue.intValueExact();
                if (pos < 1 || pos > size) {
                    return valueFactory.error(
                            AntlrQueryError.InvalidArgumentType,
                            "array:remove position out of bounds"
                    );
                }
            }
        }

        Set<Integer> remove = new HashSet<>();
        for (AntlrQueryValue p : positions) {
            remove.add(p.numericValue.intValueExact());
        }

        List<AntlrQueryValue> result = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            int pos = i + 1;
            if (!remove.contains(pos)) {
                result.add(array.arrayMembers.get(i));
            }
        }

        return valueFactory.array(result);
    }


    public AntlrQueryValue size(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var array = args.getFirst();
        return valueFactory.number(array.arrayMembers.size());
    }


    public AntlrQueryValue slice(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var array = args.get(0);
        int size = array.arrayMembers.size();

        int start = args.size() > 1 && !args.get(1).isEmptySequence
                ? args.get(1).numericValue.intValueExact()
                : 0;

        int end = args.size() > 2 && !args.get(2).isEmptySequence
                ? args.get(2).numericValue.intValueExact()
                : 0;

        int step = args.size() > 3 && !args.get(3).isEmptySequence
                ? args.get(3).numericValue.intValueExact()
                : 0;

        if (step == 0) {
            step = 1;
        }

        int realStart = start == 0
                ? (step > 0 ? 1 : size)
                : (start > 0 ? start : size + start + 1);

        int realEnd = end == 0
                ? (step > 0 ? size : 1)
                : (end > 0 ? end : size + end + 1);

        List<AntlrQueryValue> result = new ArrayList<>();

        if (step > 0) {
            for (int i = realStart; i <= realEnd; i += step) {
                if (i >= 1 && i <= size) {
                    result.add(array.arrayMembers.get(i - 1));
                }
            }
        } else {
            for (int i = realStart; i >= realEnd; i += step) {
                if (i >= 1 && i <= size) {
                    result.add(array.arrayMembers.get(i - 1));
                }
            }
        }

        return valueFactory.array(result);
    }


    public AntlrQueryValue sort(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        var array = args.get(0);

        AntlrQueryValue collation =
                (args.size() > 1 && !args.get(1).isEmptySequence)
                        ? args.get(1)
                        : valueFactory.string("");

        AntlrQueryValue keyFn =
                (args.size() > 2 && !args.get(2).isEmptySequence)
                        ? args.get(2)
                        : functionManager.getFunctionReference("fn", "data", 1);

        List<AntlrQueryValue> members = array.arrayMembers;
        int size = members.size();

        List<AntlrQueryValue> keys = new ArrayList<>(size);

        for (AntlrQueryValue member : members) {
            var k = keyFn.functionValue.call(
                    context,
                    List.of(member)
            );
            if (k.isError) {
                return k;
            }
            keys.add(k);
        }

        List<Integer> idx = new ArrayList<>(size);
        for (int i = 0; i < size; i++) idx.add(i);

        idx.sort((i, j) -> {
            AntlrQueryValue ki = keys.get(i);
            AntlrQueryValue kj = keys.get(j);

            if (ki.sequence.isEmpty() && kj.sequence.isEmpty()) {
                return 0;
            }

            AntlrQueryValue ai = ki.sequence.getFirst();
            AntlrQueryValue aj = kj.sequence.getFirst();

//            TODO: take collation into account
            return ai.stringValue.compareTo(aj.stringValue);
        });

        List<AntlrQueryValue> result = new ArrayList<>(size);
        for (int i : idx) {
            result.add(members.get(i));
        }

        return valueFactory.array(result);
    }


    public AntlrQueryValue sortBy(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        var array = args.get(0);
        var keySpecs = args.size() > 1 ? args.get(1).sequence : List.<AntlrQueryValue>of();

        List<SortKeySpec> specs = new ArrayList<>();

        if (keySpecs.isEmpty()) {
            specs.add(new SortKeySpec(
                    functionManager.getFunctionReference("fn", "data", 1),
                    valueFactory.string(""),
                    "ascending"
            ));
        } else {
            for (AntlrQueryValue rec : keySpecs) {
                var keyFn = rec.mapEntries.getOrDefault(
                        valueFactory.string("key"),
                        functionManager.getFunctionReference("fn", "data", 1)
                );

                var coll = rec.mapEntries.getOrDefault(
                        valueFactory.string("collation"),
                        valueFactory.string("")
                );

                var order = rec.mapEntries.getOrDefault(
                        valueFactory.string("order"),
                        valueFactory.string("ascending")
                ).stringValue;

                specs.add(new SortKeySpec(keyFn, coll, order));
            }
        }

        List<AntlrQueryValue> members = array.arrayMembers;
        int size = members.size();

        List<List<AntlrQueryValue>> keys = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            keys.add(new ArrayList<>());
        }

        for (int i = 0; i < size; i++) {
            var member = members.get(i);
            for (SortKeySpec spec : specs) {
                var k = spec.keyFn.functionValue.call(
                        context,
                        List.of(member)
                );
                if (k.isError) {
                    return k;
                }
                keys.get(i).addAll(k.sequence);
            }
        }

        List<Integer> idx = new ArrayList<>(size);
        for (int i = 0; i < size; i++) idx.add(i);

        idx.sort((i, j) -> {
            for (int s = 0; s < specs.size(); s++) {
                var spec = specs.get(s);
                var ki = keys.get(i).get(s);
                var kj = keys.get(j).get(s);

                int rel = lexCompare(ki.sequence, kj.sequence, spec.collation.stringValue);

                if (rel != 0) {
                    return spec.order.equals("ascending") ? rel : -rel;
                }
            }
            return Integer.compare(i, j);
        });

        List<AntlrQueryValue> result = new ArrayList<>(size);
        for (int i : idx) {
            result.add(members.get(i));
        }

        return valueFactory.array(result);
    }

    private int lexCompare(List<AntlrQueryValue> a,
                           List<AntlrQueryValue> b,
                           String collation)
    {
        if (a.isEmpty() && b.isEmpty()) return 0;
        if (a.isEmpty()) return -1;
        if (b.isEmpty()) return 1;

        var h1 = a.getFirst();
        var h2 = b.getFirst();

        int rel = simpleCompare(h1, h2, collation);
        if (rel != 0) return rel;

        return lexCompare(a.subList(1, a.size()), b.subList(1, b.size()), collation);
    }

    private int simpleCompare(AntlrQueryValue k1,
                              AntlrQueryValue k2,
                              String collation)
    {
        boolean s1 = k1.isString;
        boolean s2 = k2.isString;

        if (s1 && s2) {
            return k1.stringValue.compareTo(k2.stringValue);
        }

        boolean n1 = k1.isNumeric;
        boolean n2 = k2.isNumeric;

        if (n1 && n2) {
            return k1.numericValue.compareTo(k2.numericValue);
        }

        if (k1.equals(k2)) return 0;

        return k1.hashCode() < k2.hashCode() ? -1 : 1;
    }

    private record SortKeySpec(AntlrQueryValue keyFn, AntlrQueryValue collation, String order) {
    }


    public AntlrQueryValue sortWith(
            final AntlrQueryVisitingContext context,
            final List<AntlrQueryValue> args)
    {
        var array = args.get(0);
        var comparators = args.get(1).sequence;

        List<AntlrQueryValue> members = array.arrayMembers;
        int size = members.size();

        List<Integer> idx = new ArrayList<>(size);
        for (int i = 0; i < size; i++) idx.add(i);

        idx.sort((i, j) -> {
            var a = members.get(i);
            var b = members.get(j);

            for (AntlrQueryValue cmp : comparators) {
                var r = cmp.functionValue.call(
                        context,
                        List.of(a, b)
                );

                if (r.isError) {
                    throw new RuntimeException("Comparator error");
                }

                int rel = r.numericValue.intValueExact();

                if (rel != 0) {
                    return rel;
                }
            }

            return Integer.compare(i, j);
        });

        List<AntlrQueryValue> result = new ArrayList<>(size);
        for (int i : idx) {
            result.add(members.get(i));
        }

        return valueFactory.array(result);
    }

    public AntlrQueryValue split(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var array = args.getFirst();
        int size = array.arrayMembers.size();

        if (size == 0) {
            return valueFactory.sequence(List.of());
        }

        List<AntlrQueryValue> result = new ArrayList<>(size);

        for (AntlrQueryValue member : array.arrayMembers) {
            result.add(valueFactory.array(List.of(member)));
        }

        return valueFactory.sequence(result);
    }


    public AntlrQueryValue tail(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var array = args.getFirst();
        int size = array.arrayMembers.size();

        if (size == 0) {
            return valueFactory.error(
                    AntlrQueryError.InvalidArgumentType,
                    "array:tail on empty array"
            );
        }

        if (size == 1) {
            return valueFactory.array(List.of());
        }

        List<AntlrQueryValue> result = new ArrayList<>(size - 1);

        for (int i = 1; i < size; i++) {
            result.add(array.arrayMembers.get(i));
        }

        return valueFactory.array(result);
    }


    public AntlrQueryValue trunk(
            final AntlrQueryVisitingContext ignoredContext,
            final List<AntlrQueryValue> args)
    {
        var array = args.getFirst();
        int size = array.arrayMembers.size();

        if (size == 0) {
            return valueFactory.error(
                    AntlrQueryError.InvalidArgumentType,
                    "array:trunk on empty array"
            );
        }

        if (size == 1) {
            return valueFactory.array(List.of());
        }

        List<AntlrQueryValue> result = new ArrayList<>(size - 1);

        for (int i = 0; i < size - 1; i++) {
            result.add(array.arrayMembers.get(i));
        }

        return valueFactory.array(result);
    }
}
