// package com.github.akruk.antlrxquery.evaluator.values;


// import java.math.BigDecimal;
// import java.util.List;
// import java.util.Map;

// import org.antlr.v4.runtime.tree.ParseTree;

// import com.github.akruk.antlrxquery.evaluator.values.factories.XQueryValueFactory;

// public sealed class XQueryTreeNode<T extends ParseTree>
//     implements XQueryValue<ParseTree>

// {
//     public XQueryTreeNode(ParseTree node, XQueryValueFactory valueFactory) {
//         super(node, valueFactory);
//     }

//     @Override
//     public ParseTree node() {
//         return value;
//     }

//     @Override
//     public String stringValue() {
//         return value.getText();
//     }

//     @Override
//     public String toString() {
//         StringBuilder sb = new StringBuilder("<");
//         sb.append(super.toString());
//         sb.append(":");
//         sb.append(value.getClass().getSimpleName());
//         sb.append(":");
//         sb.append(value.getText());
//         sb.append("/>");
//         return sb.toString();
//     }

//     @Override
//     public XQueryValue valueEqual(XQueryValue other) {
//         return valueFactory.bool(this == other);
//     }

//     @Override
//     public XQueryValue valueLessThan(XQueryValue other) {
//         return null;
//     }

//     @Override
//     public XQueryValue data() {
//         var atomized = atomize();
//         return valueFactory.sequence(atomized);
//     }

//     @Override
//     public XQueryValue empty() {
//         return null;
//     }


//     @Override
//     public List<XQueryValue> arrayMembers() {
//         return null;
//     }

//     @Override
//     public Map<XQueryValue, XQueryValue> mapEntries() {
//         return null;
//     }

//     @Override
//     public BigDecimal numericValue() {
//         // TODO Auto-generated method stub
//         throw new UnsupportedOperationException("Unimplemented method 'numericValue'");
//     }

//     @Override
//     public XQueryFunction functionValue() {
//         // TODO Auto-generated method stub
//         throw new UnsupportedOperationException("Unimplemented method 'functionValue'");
//     }

//     @Override
//     public Boolean effectiveBooleanValue() {
//         // TODO Auto-generated method stub
//         throw new UnsupportedOperationException("Unimplemented method 'effectiveBooleanValue'");
//     }

//     @Override
//     public List<XQueryValue<?>> sequence() {
//         // TODO Auto-generated method stub
//         throw new UnsupportedOperationException("Unimplemented method 'sequence'");
//     }

//     @Override
//     public boolean isNumericValue() {
//         // TODO Auto-generated method stub
//         throw new UnsupportedOperationException("Unimplemented method 'isNumericValue'");
//     }

//     @Override
//     public boolean isStringValue() {
//         // TODO Auto-generated method stub
//         throw new UnsupportedOperationException("Unimplemented method 'isStringValue'");
//     }

//     @Override
//     public boolean isBooleanValue() {
//         // TODO Auto-generated method stub
//         throw new UnsupportedOperationException("Unimplemented method 'isBooleanValue'");
//     }

//     @Override
//     public boolean isSequence() {
//         // TODO Auto-generated method stub
//         throw new UnsupportedOperationException("Unimplemented method 'isSequence'");
//     }

//     @Override
//     public boolean isAtomic() {
//         // TODO Auto-generated method stub
//         throw new UnsupportedOperationException("Unimplemented method 'isAtomic'");
//     }

//     @Override
//     public boolean isNode() {
//         // TODO Auto-generated method stub
//         throw new UnsupportedOperationException("Unimplemented method 'isNode'");
//     }

//     @Override
//     public boolean isFunction() {
//         // TODO Auto-generated method stub
//         throw new UnsupportedOperationException("Unimplemented method 'isFunction'");
//     }

//     @Override
//     public boolean isError() {
//         // TODO Auto-generated method stub
//         throw new UnsupportedOperationException("Unimplemented method 'isError'");
//     }

//     @Override
//     public List<XQueryValue<?>> atomize() {
//         // TODO Auto-generated method stub
//         throw new UnsupportedOperationException("Unimplemented method 'atomize'");
//     }

//     @Override
//     public boolean isEmptySequence() {
//         // TODO Auto-generated method stub
//         throw new UnsupportedOperationException("Unimplemented method 'isEmptySequence'");
//     }
// }
