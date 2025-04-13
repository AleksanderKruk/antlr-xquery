package com.github.akruk.antlrxquery.typesystem;

import java.util.List;

public interface XQueryAtomicType extends XQueryItemType {
  @Override
  default boolean isAtomic() {
    return true;
  }
}
