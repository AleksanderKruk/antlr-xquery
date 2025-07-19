package com.github.akruk.antlrxquery.typesystem.defaults;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class XQueryItemTypeAnyArray extends XQueryItemType {
<<<<<<< HEAD

  public XQueryItemTypeAnyArray(XQueryTypeFactory factory) {
    super(XQueryTypes.ANY_ARRAY, null, null, null, null, null, null, factory, null);
  }

  @Override
  public String toString() {
      return "array(*)";
  }
=======
    final private XQuerySequenceType anyItems;

    public XQueryItemTypeAnyArray(XQueryTypeFactory factory) {
        super(XQueryTypes.ANY_ARRAY, null, null, null, null, null, null, factory, null);
        anyItems = new XQuerySequenceType(factory, new XQueryItemTypeAnyItem(factory), XQueryOccurence.ZERO_OR_MORE);
    }

    @Override
    public String toString() {
        return "array(*)";
    }

    @Override
    public XQuerySequenceType getArrayMemberType() {
        return anyItems;
    }
>>>>>>> language-features/lookup-expression

}
