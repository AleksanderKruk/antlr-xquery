package com.github.akruk.antlrxquery.values;

import java.util.ArrayList;
import java.util.List;
import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

public class XQuerySequence extends XQueryValueBase<List<XQueryValue>> {
    @Override
    public List<XQueryValue> sequence() {
        return value;
    }

    public XQuerySequence(List<XQueryValue> list, XQueryValueFactory valueFactory) {
        super(list, valueFactory);
    }

    public XQuerySequence(XQueryValueFactory valueFactory) {
        super(List.of(), valueFactory);
    }

    @Override
    public Boolean effectiveBooleanValue() {
        return value.isEmpty();
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
    public XQueryValue union(XQueryValue otherSequence) throws XQueryUnsupportedOperation {
        var newSequence = new ArrayList<XQueryValue>();
        newSequence.addAll(value);
        newSequence.addAll(otherSequence.sequence());
        return valueFactory.sequence(newSequence);
    }

    @Override
    public XQueryValue intersect(XQueryValue otherSequence) throws XQueryUnsupportedOperation {
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
    public XQueryValue except(XQueryValue otherSequence) throws XQueryUnsupportedOperation {
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
    public XQueryValue copy() {
        return valueFactory.sequence(List.copyOf(value));
    }


    @Override
    public XQueryValue empty() throws XQueryUnsupportedOperation {
        return valueFactory.bool(value.isEmpty());
    }


    @Override
    public XQueryValue head() throws XQueryUnsupportedOperation {
        if (value.isEmpty())
            return valueFactory.emptySequence();
        return value.get(0);
    }


    @Override
    public XQueryValue tail() throws XQueryUnsupportedOperation {
        if (value.isEmpty())
            return valueFactory.emptySequence();
        return valueFactory.sequence(value.subList(1, value.size()));
    }

    @Override
    public XQueryValue insertBefore(XQueryValue position, XQueryValue inserted)
    throws XQueryUnsupportedOperation
    {
        var newSequence = new ArrayList<XQueryValue>(value.size());
        newSequence.addAll(value);
        if (!position.isNumericValue())
            throw new XQueryUnsupportedOperation();
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
    public XQueryValue remove(XQueryValue position) throws XQueryUnsupportedOperation
    {
        var newSequence = new ArrayList<XQueryValue>(value.size());
        newSequence.addAll(value);
        if (!position.isNumericValue())
            throw new XQueryUnsupportedOperation();
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
    public XQueryValue reverse() throws XQueryUnsupportedOperation
    {
        var newSequence = List.copyOf(value);
        return valueFactory.sequence(newSequence.reversed());
    }



    @Override
    public XQueryValue subsequence(int startingLoc) throws XQueryUnsupportedOperation {
        return subsequence(startingLoc, value.size()-startingLoc+1);
    }

    @Override
    public XQueryValue subsequence(int startingLoc, int length) throws XQueryUnsupportedOperation {
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
    public XQueryValue distinctValues() throws XQueryUnsupportedOperation {
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
    public XQueryValue zeroOrOne() throws XQueryUnsupportedOperation {
        return switch (value.size()) {
            case 0, 1 -> this;
            default -> null;
        };
    }

    @Override
    public XQueryValue oneOrMore() throws XQueryUnsupportedOperation {
        return switch (value.size()) {
            case 0 -> null;
            default -> this;
        };
    }

    @Override
    public XQueryValue exactlyOne() throws XQueryUnsupportedOperation {
        return switch (value.size()) {
            case 1 -> this;
            default -> null;
        };
    }

    @Override
    public XQueryValue data() throws XQueryUnsupportedOperation {
        var atomized = atomize();
        return valueFactory.sequence(atomized);
    }

    @Override
    public XQueryValue concatenate(XQueryValue other) throws XQueryUnsupportedOperation {
        StringBuilder builder = new StringBuilder();
        for (var e : this.value) {
            builder.append(e.stringValue());
        }
        builder.append(other.stringValue());
        return valueFactory.string(builder.toString());
    }
}
