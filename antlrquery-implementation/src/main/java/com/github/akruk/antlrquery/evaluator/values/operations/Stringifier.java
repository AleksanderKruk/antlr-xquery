package com.github.akruk.antlrquery.evaluator.values.operations;

import com.github.akruk.antlrquery.evaluator.values.AntlrQueryValue;
import com.github.akruk.antlrquery.evaluator.values.factories.AntlrQueryValueFactory;

public class Stringifier {

    private final AntlrQueryValueFactory valueFactory;

    public Stringifier(final AntlrQueryValueFactory valueFactory, final EffectiveBooleanValue ebv)
    {
        this.valueFactory = valueFactory;
    }

    public AntlrQueryValue stringify(final AntlrQueryValue target) {
        if (target.isError || target.isString)
            return target;
        return valueFactory.string(stringify_(target));
    }

    public String stringify_(final AntlrQueryValue target) {
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
