package com.github.akruk.antlrxquery.typesystem.types.itemtypes.constraints;

import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.github.akruk.antlrxquery.namespaceresolver.NamespaceResolver.QualifiedName;
import com.github.akruk.antlrxquery.typesystem.types.Cardinality;
import com.github.akruk.antlrxquery.typesystem.types.itemtypes.StringType;
import com.github.akruk.antlrxquery.typesystem.types.itemtypes.constraints.grammarexpressions.GrammarExpression;

public sealed interface Constraint
    permits Constraint.AtomicConstraint, 
            Constraint.NodeConstraint, 
            Constraint.CollectionConstraint 
{

    public sealed interface AtomicConstraint extends Constraint
        permits AtomicConstraint.NumberConstraint, 
                AtomicConstraint.StringConstraint, 
                AtomicConstraint.BinaryConstraint 
    {

        sealed interface NumberConstraint
            extends AtomicConstraint
            permits NumberConstraint.NumberIntervalConstraint 
        {

            record NumberIntervalConstraint(
                @NonNull Cardinality values
            ) implements NumberConstraint {}
        }

        sealed interface StringConstraint
            extends AtomicConstraint
            permits StringConstraint.LengthConstraint,
                    StringConstraint.EnumConstraint,
                    StringConstraint.ParseableConstraint 
        {

            record LengthConstraint(
                @NonNull Cardinality length
            ) implements StringConstraint {}

            record EnumConstraint(
                @NonNull Set<
                    StringType
                > values
            ) implements StringConstraint {}

            record ParseableConstraint(
                @NonNull QualifiedName rule
            ) implements StringConstraint {}
        }

        public sealed interface BinaryConstraint
            extends AtomicConstraint
            permits BinaryConstraint.BinaryLengthConstraint 
        {

            public record BinaryLengthConstraint(
                @NonNull Cardinality length
            ) implements BinaryConstraint {}
        }
    }

    public sealed interface NodeConstraint
        extends Constraint
        permits NodeConstraint.GrammarConstraint, 
                NodeConstraint.RootConstraint 
    {

        public record GrammarConstraint(
            @NonNull GrammarExpression selector
        ) implements NodeConstraint {}

        public record RootConstraint()
            implements NodeConstraint {}
    }

    public sealed interface CollectionConstraint
        extends Constraint
        permits CollectionConstraint.ArrayConstraint, 
                CollectionConstraint.RecordConstraint 
    {

        public sealed interface ArrayConstraint
            extends CollectionConstraint
            permits ArrayConstraint.ArrayLengthConstraint 
        {

            record ArrayLengthConstraint(
                @NonNull Cardinality length
            ) implements ArrayConstraint {}
        }

        public sealed interface RecordConstraint
            extends CollectionConstraint
            permits RecordConstraint.ExtensibleRecordConstraint 
        {

            public record ExtensibleRecordConstraint()
                implements RecordConstraint {}
        }
    }
}