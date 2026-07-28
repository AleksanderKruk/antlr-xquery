package com.github.akruk.antlrquery.typesystem.typeoperations;

import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.AntlrQueryItemType;

public sealed interface IsCastable
        permits
        IsCastable.Possible,
        IsCastable.AlwaysPossible,
        IsCastable.Impossible,
        IsCastable.WrongTargetType,
        IsCastable.TestedExpressionIsEmptySequence,
        IsCastable.TestedExpressionCanBeEmptySequenceWithoutFlag,
        IsCastable.TestedExpressionIsZeroOrMore {

    record Possible(
    ) implements IsCastable {
    }

    sealed interface AlwaysPossible extends IsCastable
            permits
            AlwaysPossible.TestedTypeIsSubtypeOfTargetType,
            AlwaysPossible.CastingToSame,
            AlwaysPossible.TypeCanAlwaysBeCastToTarget,
            AlwaysPossible.ManySequenceTypes,
            AlwaysPossible.ManyItemTypes {
        record TestedTypeIsSubtypeOfTargetType(
        ) implements AlwaysPossible {
        }

        record CastingToSame(
        ) implements AlwaysPossible {
        }

        record TypeCanAlwaysBeCastToTarget(
        ) implements AlwaysPossible {
        }

        record ManySequenceTypes(
                AntlrQuerySequenceType[] wrongSequenceTypes,
                IsCastable[] problems
        ) implements AlwaysPossible {
        }

        record ManyItemTypes(
                AntlrQueryItemType[] wrongItemTypes,
                IsCastable[] problems
        ) implements AlwaysPossible {
        }
    }

    record Impossible(
    ) implements IsCastable {
    }

    record WrongTargetType(
    ) implements IsCastable {
    }

    record TestedExpressionIsEmptySequence(
    ) implements IsCastable {
    }

    record TestedExpressionCanBeEmptySequenceWithoutFlag(
    ) implements IsCastable {
    }

    record TestedExpressionIsZeroOrMore(
    ) implements IsCastable {
    }
}
