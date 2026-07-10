package com.github.akruk.antlrxquery.typesystem.typeoperations;

import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.types.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.types.AntlrQuerySequenceType;

public class SequencetypeAtomization {
    private final AntlrQuerySequenceType anyItems;
    private final XQueryTypeFactory typeFactory;

    public SequencetypeAtomization(XQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
        this.anyItems = typeFactory.zeroOrMore(typeFactory.itemAnyItem());
    }

    public AntlrQuerySequenceType atomize(AntlrQuerySequenceType type) {
        if (type.isZero)
            return type;
        final XQueryItemType itemType = type.itemType;
        return switch(itemType.type) {
            case ANY_ARRAY -> anyItems;
            case ANY_ITEM -> anyItems;
            case ARRAY -> typeFactory.zeroOrMore(itemType.arrayMemberType.itemType);
            case CHOICE ->{
                AntlrQuerySequenceType result = null;
                for (var membertype : itemType.itemTypes) {
                    result = switch(membertype.type) {
                        case ARRAY -> {
                            var atomized = typeFactory.zeroOrMore(itemType.arrayMemberType.itemType);
                            yield result == null? atomized : result.alternativeMerge(atomized);
                        }
                        case ANY_ITEM -> anyItems;
                        case ANY_ARRAY -> anyItems;
                        default -> null;
                    };
                }
                yield type;
            }
            default -> type;
        };
    }


}
