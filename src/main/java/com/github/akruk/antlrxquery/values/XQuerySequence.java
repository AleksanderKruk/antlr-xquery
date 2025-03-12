package com.github.akruk.antlrxquery.values;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.github.akruk.antlrxquery.exceptions.XQueryUnsupportedOperation;

public class XQuerySequence extends XQueryValueBase<List<XQueryValue>> {
    @Override
    public List<XQueryValue> sequence() {
        return value;
    }

    public XQuerySequence(List<XQueryValue> list) {
        value = list;
    }

    public XQuerySequence(XQueryValue... values) {
        value = Arrays.asList(values);
    }


    public XQuerySequence() {
        value = Collections.emptyList();
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
    public XQueryValue union(XQueryValue otherSequence) throws XQueryUnsupportedOperation {
        var newSequence = new ArrayList<XQueryValue>();
        newSequence.addAll(value);
        newSequence.addAll(otherSequence.sequence());
        return new XQuerySequence(newSequence);
    }

    @Override
    public XQueryValue intersect(XQueryValue otherSequence) throws XQueryUnsupportedOperation {
        var newSequence = new ArrayList<XQueryValue>();
        var otherSequenceValue = otherSequence.sequence();
        for (var element : value) {
            for (var otherElement : otherSequenceValue) {
                if (element.valueEqual(otherElement).booleanValue()) {
                    newSequence.add(element);
                }
            }
        }
        return new XQuerySequence(newSequence);
    }


    @Override
    public XQueryValue except(XQueryValue otherSequence) throws XQueryUnsupportedOperation {
        var newSequence = new ArrayList<XQueryValue>();
        var otherSequenceValue = otherSequence.sequence();
        NEXT_ELEMENT:
        for (var element : value) {
            for (var otherElement : otherSequenceValue) {
                if (element.valueEqual(otherElement).booleanValue()) {
                    continue NEXT_ELEMENT;
                }
            }
            newSequence.add(element);
        }
        return new XQuerySequence(newSequence);
    }



    @Override
    public XQueryValue copy() {
        return new XQuerySequence(List.copyOf(value));
    }




}
