package com.github.akruk.antlrquery.typesystem.typeoperations.stringify;

import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import com.github.akruk.antlrquery.typesystem.types.itemtypes.*;

public class TypeRequiresParentheses {

    public static boolean requiresParentheses(final AntlrQuerySequenceType type) {
        return switch(type) {
            case AntlrQuerySequenceType.EmptySequence() -> false;
            case AntlrQuerySequenceType.NonEmptySequence(AntlrQueryItemType itemType, Cardinality cardinality) -> {
                if (cardinality.isOne()) {
                    yield false;
                }
                yield switch(itemType) {
                    case ChoiceItemType _, FunctionType _ -> true;
                    case TreeRuleType _, TreeTokenType _, NamedItemType _, NeverType _,
                         NothingType _, AnyItemType _, AtomicType _, ArrayLikeType _, MapLikeType _,
                         GrammarEntityType _ -> false;
                    case TreeNodeType treeNodeType ->
                            treeNodeType instanceof NamesConstrained nc
                                    && nc.elementNames().size() > 1;
                };
            }
        };
    }
}
