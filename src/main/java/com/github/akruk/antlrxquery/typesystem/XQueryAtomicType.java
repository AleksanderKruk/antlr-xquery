package com.github.akruk.antlrxquery.typesystem;


public interface XQueryAtomicType extends XQueryItemType {
  @Override
  default boolean isAtomic() {
    return true;
  }
}
