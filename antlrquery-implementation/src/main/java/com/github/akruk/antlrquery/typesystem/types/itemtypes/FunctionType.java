package com.github.akruk.antlrquery.typesystem.types.itemtypes;

import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.typeoperations.stringify.Stringify;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public sealed interface FunctionType
        extends ConcreteItemType
        permits FunctionType.ConstrainedFunction, FunctionType.AnyFunction
{
    AntlrQuerySequenceType returnType();
    record ConstrainedFunction(
            List<AntlrQuerySequenceType> argumentTypes,
            AntlrQuerySequenceType returnType
    ) implements com.github.akruk.antlrquery.typesystem.types.itemtypes.FunctionType {
        @Override
        public @NonNull String toString() {
            return Stringify.stringify(this);
        }
    }

    final class AnyFunction implements com.github.akruk.antlrquery.typesystem.types.itemtypes.FunctionType {
        private final AntlrQuerySequenceType returnedType;
        public AnyFunction(AntlrQueryTypeFactory tf) {
            returnedType = tf.zeroOrMore(tf.itemAnyItem());
        }
        @Override
        public AntlrQuerySequenceType returnType() {
            return returnedType;
        }
        @Override
        public @NonNull String toString() {
            return Stringify.stringify(this);
        }
    }

}
