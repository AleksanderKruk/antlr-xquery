// package com.github.akruk.antlrxquery.evaluator.values;

// import java.math.BigDecimal;
// import java.util.List;
// import java.util.Map;

// import org.antlr.v4.runtime.tree.ParseTree;

// import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

// public record XQueryMap(Map<XQueryValue, XQueryValue> value, XQueryValueFactory valueFactory)
//         implements XQueryValue
// {

//     @Override
//     public XQueryValue valueEqual(XQueryValue other) {
//         if (!(other instanceof XQueryMap otherMap)) {
//             return XQueryError.InvalidArgumentType;
//         }
//         if (this.value.size() != otherMap.value.size()) {
//             return valueFactory.bool(false);
//         }
//         for (Map.Entry<XQueryValue, XQueryValue> entry : this.value.entrySet()) {
//             XQueryValue otherValue = otherMap.value.get(entry.getKey());
//             if (otherValue == null || !entry.getValue().valueEqual(otherValue).effectiveBooleanValue()) {
//                 return valueFactory.bool(false);
//             }
//         }
//         return valueFactory.bool(true);
//     }

//     @Override
//     public XQueryValue valueLessThan(XQueryValue other) {
//         return XQueryError.InvalidArgumentType;
//     }

//     @Override
//     public XQueryValue empty() {
//         return valueFactory.bool(false);
//     }

//     @Override
//     public ParseTree node() {
//         return null;
//     }

//     @Override
//     public BigDecimal numericValue() {
//         return null;
//     }

//     @Override
//     public String stringValue() {
//         StringBuilder sb = new StringBuilder();
//         sb.append("{");
//         boolean first = true;
//         for (Map.Entry<XQueryValue, XQueryValue> entry : value.entrySet()) {
//             if (!first) {
//                 sb.append(", ");
//             }
//             first = false;
//             sb.append(String.valueOf(entry.getKey()))
//               .append(": ")
//               .append(entry.getValue() != null ? entry.getValue().stringValue() : "null");
//         }
//         sb.append("}");
//         return sb.toString();
//     }

//     @Override
//     public XQueryFunction functionValue() {
//         return (_, positionalArguments) -> {
//             return value.get(positionalArguments.get(0));
//         };
//     }

// }
