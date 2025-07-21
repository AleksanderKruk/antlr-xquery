// package com.github.akruk.antlrxquery.evaluator.values;

// import java.math.BigDecimal;
// import java.util.List;
// import java.util.Map;

// import org.antlr.v4.runtime.tree.ParseTree;

// import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

// public class XQueryArray extends XQueryValue
// {
// (List<XQueryValue<?>> value, XQueryValueFactory valueFactory)


    // public XQueryValue valueEqual(XQueryValue other) {
    //     if (!(other instanceof XQueryArray otherArray)) {
    //         return XQueryError.InvalidArgumentType;
    //     }
    //     if (this.value.size() != otherArray.value.size()) {
    //         return valueFactory.bool(false);
    //     }

    //     for (int i = 0; i < this.value.size(); i++) {
    //         XQueryValue thisValue = this.value.get(i);
    //         XQueryValue otherValue = otherArray.value.get(i);
    //         if (!thisValue.valueEqual(otherValue).effectiveBooleanValue()) {
    //             return valueFactory.bool(false);
    //         }
    //     }
    //     return valueFactory.bool(true);
    // }



    // public String stringValue() {
    //     StringBuilder sb = new StringBuilder("[");
    //     for (int i = 0; i < value.size(); i++) {
    //         sb.append(value.get(i).stringValue());
    //         if (i < value.size() - 1) {
    //             sb.append(", ");
    //         }
    //     }
    //     sb.append("]");
    //     return sb.toString();
    // }


    // public XQueryFunction functionValue() {
    //     return (_, args) -> {
    //         if (!args.get(0).isNumericValue())
    //             return XQueryError.InvalidArgumentType;
    //         return value.get(args.get(0).numericValue().intValue());
    //     };
    // }

// }
