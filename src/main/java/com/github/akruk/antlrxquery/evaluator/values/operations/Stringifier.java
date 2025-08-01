package com.github.akruk.antlrxquery.evaluator.values.operations;

import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

public class Stringifier {

    private XQueryValueFactory valueFactory;

    public Stringifier(final XQueryValueFactory valueFactory, final EffectiveBooleanValue ebv)
    {
        this.valueFactory = valueFactory;
    }

    public XQueryValue stringify(XQueryValue target) {
        if (target.isError)
            return target;
        return valueFactory.string(stringify_(target));
    }

    public String stringify_(XQueryValue target) {
        if (target.isEmptySequence)
            return "";
        if (target.isString)
            return target.stringValue;
        if (target.isBoolean)
            return target.booleanValue? "true" : "false";
        if (target.isNumeric)
            return target.numericValue.toString();
        return target.toString();
    }


}
