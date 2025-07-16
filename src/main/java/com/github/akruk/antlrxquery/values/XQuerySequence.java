package com.github.akruk.antlrxquery.values;

import java.util.ArrayList;
import java.util.List;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class XQuerySequence extends XQueryValueBase<List<XQueryValue>> {
    @Override
    public List<XQueryValue> sequence() {
        return value;
    }

    @Override
    public boolean isSequence() {
        return true;
    }

    final boolean isEmptySequence;
    public XQuerySequence(List<XQueryValue> list, XQueryValueFactory valueFactory) {
        super(list, valueFactory);
        isEmptySequence = value.size() == 0;
    }

    public XQuerySequence(XQueryValueFactory valueFactory) {
        this(List.of(), valueFactory);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<");
        sb.append(super.toString());
        sb.append(":");
        int lastIndex = value.size() - 1;
        for (int i = 0; i < value.size()-1; i++) {
            sb.append(value.get(i).toString());
            sb.append(", ");
        }
        sb.append(value.get(lastIndex).toString());
        sb.append("/>");
        return sb.toString();
    }

    @Override
    public Boolean effectiveBooleanValue() {
        return !value.isEmpty();
    }

    @Override
    public List<XQueryValue> atomize() {
        List<XQueryValue> result = new ArrayList<>();
        for (XQueryValue element : value) {
            if (element.isAtomic()) {
                result.add(element);
                continue;
            }
            // If the result is not atomic we atomize it
            // and extend the result list
            var atomizedValues = element.atomize();
            result.addAll(atomizedValues);
        }
        return result;
    }


    @Override
    public XQueryValue valueEqual(XQueryValue other) {
        return valueFactory.bool(false);
    }

    @Override
    public XQueryValue valueLessThan(XQueryValue other) {
        return valueFactory.bool(false);
    }

    @Override
    public XQueryValue union(XQueryValue otherSequence) {
        var newSequence = new ArrayList<XQueryValue>();
        newSequence.addAll(value);
        newSequence.addAll(otherSequence.sequence());
        return valueFactory.sequence(newSequence);
    }

    @Override
    public XQueryValue intersect(XQueryValue otherSequence) {
        var otherSequenceValue = otherSequence.sequence();
        var newSequence = new ArrayList<XQueryValue>(otherSequenceValue.size());
        for (var element : value) {
            for (var otherElement : otherSequenceValue) {
                if (element.valueEqual(otherElement).booleanValue()) {
                    newSequence.add(element);
                }
            }
        }
        return valueFactory.sequence(newSequence);
    }


    @Override
    public XQueryValue except(XQueryValue otherSequence) {
        var otherSequenceValue = otherSequence.sequence();
        var newSequence = new ArrayList<XQueryValue>(otherSequenceValue.size());
        NEXT_ELEMENT:
        for (var element : value) {
            for (var otherElement : otherSequenceValue) {
                if (element.valueEqual(otherElement).booleanValue()) {
                    continue NEXT_ELEMENT;
                }
            }
            newSequence.add(element);
        }
        return valueFactory.sequence(newSequence);
    }


    @Override
    public XQueryValue empty() {
        return valueFactory.bool(value.isEmpty());
    }


    @Override
    public XQueryValue head() {
        if (value.isEmpty())
            return valueFactory.emptySequence();
        return value.get(0);
    }


    @Override
    public XQueryValue tail() {
        if (value.isEmpty())
            return valueFactory.emptySequence();
        return valueFactory.sequence(value.subList(1, value.size()));
    }

    @Override
    public XQueryValue insertBefore(XQueryValue position, XQueryValue inserted)

    {
        var newSequence = new ArrayList<XQueryValue>(value.size());
        newSequence.addAll(value);
        if (!position.isNumericValue())
            return XQueryError.InvalidArgumentType;
        int positionIndex = position.numericValue().intValue();
        if (positionIndex > value.size()) {
            newSequence.addAll(inserted.atomize());
            return valueFactory.sequence(newSequence);
        }
        if (positionIndex <= 0) {
            newSequence.addAll(0, inserted.atomize());
            return valueFactory.sequence(newSequence);
        }
        newSequence.addAll(positionIndex - 1, inserted.atomize());
        return valueFactory.sequence(newSequence);
    }

    @Override
    public XQueryValue remove(XQueryValue position)
    {
        var newSequence = new ArrayList<XQueryValue>(value.size());
        newSequence.addAll(value);
        if (!position.isNumericValue())
            return XQueryError.InvalidArgumentType;
        int positionIndex = position.numericValue().intValue();
        if (positionIndex > value.size()) {
            return valueFactory.sequence(newSequence);
        }
        if (positionIndex <= 0) {
            return valueFactory.sequence(newSequence);
        }
        newSequence.remove(positionIndex-1);
        return valueFactory.sequence(newSequence);
    }

    @Override
    public XQueryValue reverse()
    {
        var newSequence = List.copyOf(value);
        return valueFactory.sequence(newSequence.reversed());
    }



    @Override
    public XQueryValue subsequence(int startingLoc) {
        return subsequence(startingLoc, value.size()-startingLoc+1);
    }

    @Override
    public XQueryValue subsequence(int startingLoc, int length) {
        int currentLength = value.size();
        if (startingLoc > currentLength) {
            return valueFactory.emptySequence();
        }
        int startIndexIncluded = Math.max(startingLoc - 1, 0);
        int endIndexExcluded = Math.min(startingLoc + length - 1, currentLength);
        var newSequence = value.subList(startIndexIncluded, endIndexExcluded);
        return valueFactory.sequence(newSequence);
    }

    @Override
    public XQueryValue distinctValues() {
        int currentLength = value.size();
        if (currentLength == 0) {
            return valueFactory.emptySequence();
        }
        var newSequence = new ArrayList<XQueryValue>(value.size());
        for (var element : value) {
            var exists = newSequence.stream().filter(
                    v -> v == element || v.valueEqual(element).booleanValue()).findFirst().isPresent();
            if (!exists) {
                newSequence.add(element);
            }
        }
        return valueFactory.sequence(newSequence);
    }

    @Override
    public XQueryValue data() {
        var atomized = atomize();
        return valueFactory.sequence(atomized);
    }

    @Override
    public XQueryValue concatenate(XQueryValue other) {
        StringBuilder builder = new StringBuilder();
        for (var e : this.value) {
            builder.append(e.stringValue());
        }
        builder.append(other.stringValue());
        return valueFactory.string(builder.toString());
    }

    @Override
    public boolean isEmptySequence() {
        return isEmptySequence;
    }

}
