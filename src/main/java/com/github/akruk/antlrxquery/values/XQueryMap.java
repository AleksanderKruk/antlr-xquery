// package com.github.akruk.antlrxquery.values;

// import java.util.Map;

// public class XQueryMap extends XQueryValueBase<Map<XQueryValue, XQueryValue>> {
//     public XQueryMap(Map<String, XQueryValue> value) {
//         this.value = value;
//     }

//     @Override
//     public XQueryValue valueEqual(XQueryValue other) {
//         return XQueryBoolean.FALSE;
//     }

//     @Override
//     public XQueryValue valueLessThan(XQueryValue other) {
//         return XQueryBoolean.FALSE;
//     }

//     @Override
//     public XQueryValue copy() {
//         return new XQueryMap(Map.copyOf(value));
//     }

//     @Override
//     public XQueryValue empty() {
//         return XQueryBoolean.of(value.isEmpty());
//     }


//     @Override
//     public XQueryValue size() {
//         return new XQueryNumber(value.size());
//     }


// }
