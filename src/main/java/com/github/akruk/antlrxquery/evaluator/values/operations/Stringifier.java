package com.github.akruk.antlrxquery.evaluator.values.operations;

import com.github.akruk.antlrxquery.evaluator.values.XQueryValue;
import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

public class Stringifier {

    private final XQueryValueFactory valueFactory;

    public Stringifier(final XQueryValueFactory valueFactory, final EffectiveBooleanValue ebv)
    {
        this.valueFactory = valueFactory;
    }

    public XQueryValue stringify(final XQueryValue target) {
        if (target.isError || target.isString)
            return target;
        return valueFactory.string(stringify_(target));
    }

    public String stringify_(final XQueryValue target) {
        if (target.isEmptySequence)
            return "";
        if (target.isString)
            return target.stringValue;
        if (target.isBoolean)
            return target.booleanValue? "true" : "false";
        if (target.isNumeric)
            return target.numericValue.toString();
        if (target.isNode)
            return target.node.getText();
        return target.toString();
    }


}
