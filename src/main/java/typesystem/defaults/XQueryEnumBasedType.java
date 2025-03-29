package typesystem.defaults;

import typesystem.XQueryType;

public class XQueryEnumBasedType implements XQueryType {
    private XQueryTypes type;
    private XQueryEnumBasedType containedType;
    private XQueryOccurence occurence;

    public XQueryTypes getType() {
        return type;
    }
    public void setType(XQueryTypes type) {
        this.type = type;
    }
    public XQueryEnumBasedType getContainedType() {
        return containedType;
    }
    public void setContainedType(XQueryEnumBasedType containedType) {
        this.containedType = containedType;
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
                && containedType.equals(other.getContainedType());
    }



}
