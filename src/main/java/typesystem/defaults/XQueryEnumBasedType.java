package typesystem.defaults;

import typesystem.XQueryType;

public class XQueryEnumBasedType implements XQueryType {
    private XQueryTypes type;
    private XQueryEnumBasedType containedType;
    private XQueryOccurence occurence;

    private XQueryEnumBasedType(XQueryTypes type, XQueryEnumBasedType containedType, XQueryOccurence occurence) {
        this.type = type;
        this.containedType = containedType;
        this.occurence = occurence;
    }

    public static XQueryEnumBasedType containerType(XQueryTypes type, XQueryEnumBasedType containedType, XQueryOccurence occurence) {
        return new XQueryEnumBasedType(type, containedType, occurence);
    }

    public static XQueryEnumBasedType atomicType(XQueryTypes type) {
        return new XQueryEnumBasedType(type, null, XQueryOccurence.ATOMIC);
    }


    public XQueryTypes getType() {
        return type;
    }
    public XQueryEnumBasedType getSubType() {
        return containedType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof XQueryEnumBasedType))
            return false;
        XQueryEnumBasedType other = (XQueryEnumBasedType) obj;
        XQueryTypes otherType = other.getType();
        if (type.isAtomic() && type == otherType)
            return true;
        return !otherType.isAtomic()
                && containedType.equals(other.getSubType());
    }
    @Override
    public boolean isSubtypeOf(XQueryType obj) {
        if (!(obj instanceof XQueryEnumBasedType))
            return false;
        XQueryEnumBasedType other = (XQueryEnumBasedType) obj;
        // TODO: Switch to bitmask
        XQueryOccurence otherOccurence = other.getOccurence();
        return switch (this.occurence) {
            case ZERO -> switch (otherOccurence) {
                case ZERO -> true;
                case ZERO_OR_ONE -> true;
                case ZERO_OR_MORE -> true;
                default -> false;
            };
            case ZERO_OR_ONE -> switch (otherOccurence) {
                case ZERO_OR_ONE -> containedType.isSubtypeItemtypeOf(other.getSubType());
                case ZERO_OR_MORE -> containedType.isSubtypeItemtypeOf(other.getSubType());
                default -> false;
            };
            case ZERO_OR_MORE -> switch (otherOccurence) {
                case ZERO_OR_MORE -> containedType.isSubtypeItemtypeOf(other.getSubType());
                default -> false;
            };
            case ATOMIC -> switch (otherOccurence) {
                case ZERO -> false;
                default -> containedType.isSubtypeItemtypeOf(other.getSubType());
            };
            case ONE_OR_MORE -> switch (otherOccurence) {
                case ZERO_OR_MORE -> containedType.isSubtypeItemtypeOf(other.getSubType());
                case ONE_OR_MORE -> containedType.isSubtypeItemtypeOf(other.getSubType());
                default -> false;
            };
            default -> false;
        };
    }

    @Override
    public boolean isSubtypeItemtypeOf(XQueryType  obj) {
        if (isAtomicOrUnionTypes(obj) && derivesFrom(obj))
            return true;
        if (isPureUnionType() && derivesFrom(obj))
            return true;
    }

    public XQueryOccurence getOccurence() {
        return occurence;
    }

    public void setOccurence(XQueryOccurence occurence) {
        this.occurence = occurence;
    }



}
