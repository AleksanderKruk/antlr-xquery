package typesystem.defaults;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import typesystem.XQueryType;

public class XQueryEnumBasedType implements XQueryType {
    private final XQueryTypes type;
    private final XQueryEnumBasedType containedType;
    private final List<XQueryType> argumentTypes;
    private final XQueryEnumBasedType returnedType;
    private final XQueryOccurence occurence;

    public XQueryEnumBasedType getContainedType() {
        return containedType;
    }

    public List<XQueryType> getArgumentTypes() {
        return argumentTypes;
    }

    public XQueryType getReturnedType() {
        return returnedType;
    }

    private final String name;

    public static XQueryEnumBasedType string() {
        return new XQueryEnumBasedType(XQueryTypes.STRING, null, null,
                null, XQueryOccurence.ONE, null);
    }

    public static XQueryEnumBasedType number() {
        return new XQueryEnumBasedType(XQueryTypes.NUMBER, null, null,
                null, XQueryOccurence.ONE, null);
    }

    public static XQueryEnumBasedType integer() {
        return new XQueryEnumBasedType(XQueryTypes.INTEGER, null, null,
                null, XQueryOccurence.ONE, null);
    }

    public static XQueryEnumBasedType anyNode() {
        return new XQueryEnumBasedType(XQueryTypes.ANY_NODE, null, null,
                null, XQueryOccurence.ONE, null);
    }

    public static XQueryEnumBasedType anyArray() {
        return new XQueryEnumBasedType(XQueryTypes.ANY_ARRAY, null, null,
                null, XQueryOccurence.ONE, null);
    }

    public static XQueryEnumBasedType anyMap() {
        return new XQueryEnumBasedType(XQueryTypes.ANY_MAP, null, null,
                null, XQueryOccurence.ONE, null);
    }

    public static XQueryEnumBasedType anyElement() {
        return new XQueryEnumBasedType(XQueryTypes.ANY_ELEMENT, null, null,
                null, XQueryOccurence.ONE, null);
    }

    public static XQueryEnumBasedType anyFunction() {
        return new XQueryEnumBasedType(XQueryTypes.ANY_FUNCTION, null, null,
                null, XQueryOccurence.ONE, null);
    }

    public static XQueryEnumBasedType anyItem() {
        return new XQueryEnumBasedType(XQueryTypes.ANY_ITEM, null, null,
                null, XQueryOccurence.ONE, null);
    }

    public static XQueryEnumBasedType boolean_() {
        return new XQueryEnumBasedType(XQueryTypes.BOOLEAN, null, null,
                null, XQueryOccurence.ONE, null);
    }

    // public static XQueryEnumBasedType element() {
    //     return new XQueryEnumBasedType(XQueryTypes.ELEMENT, null, null,
    //             null, XQueryOccurence.ONE, null);
    // }

    // public static XQueryEnumBasedType error() {
    //     return new XQueryEnumBasedType(XQueryTypes.ERROR, null, null,
    //             null, XQueryOccurence.ONE, null);
    // }

    // public static XQueryEnumBasedType error() {
    //     return new XQueryEnumBasedType(XQueryTypes.NODE, null, null,
    //             null, XQueryOccurence.ONE, null);
    // }

    // public static XQueryEnumBasedType array() {
    //     return new XQueryEnumBasedType(XQueryTypes.ARRAY, null, null,
    //             null, XQueryOccurence.ONE, null);
    // }

    public static XQueryEnumBasedType sequence(XQueryEnumBasedType containedType, XQueryOccurence occurence) {
        return new XQueryEnumBasedType(XQueryTypes.SEQUENCE, containedType, null,
                null, occurence, null);
    }

    public static XQueryEnumBasedType emptySequence() {
        return new XQueryEnumBasedType(XQueryTypes.EMPTY_SEQUENCE, null, null,
                null, XQueryOccurence.ZERO, null);
    }

    public XQueryEnumBasedType(XQueryTypes type, XQueryEnumBasedType containedType,
            List<XQueryType> argumentTypes, XQueryEnumBasedType returnedType, XQueryOccurence occurence,
            String name) {
        this.type = type;
        this.containedType = containedType;
        this.argumentTypes = argumentTypes;
        this.returnedType = returnedType;
        this.occurence = occurence;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public XQueryTypes getType() {
        return type;
    }

    public XQueryEnumBasedType getSubType() {
        return containedType;
    }

    private static boolean isNullableEquals(Object one, Object other) {
        if (one != null)
            return one.equals(other);
        return one == other;
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
        if (type != other.getType())
            return false;
        if (!isNullableEquals(this.containedType, other.getContainedType()))
            return false;
        if (occurence != other.getOccurence())
            return false;
        List<XQueryType> otherArgumentTypes = other.getArgumentTypes();
        if (this.argumentTypes == null && otherArgumentTypes != null)
            return false;
        if (this.argumentTypes != null && otherArgumentTypes == null)
            return false;
        if (this.argumentTypes != null) {
            if (this.argumentTypes.size() != otherArgumentTypes.size())
                return false;
            if (IntStream.range(0, this.argumentTypes.size())
                        .allMatch(i -> this.argumentTypes.get(i).equals(otherArgumentTypes.get(i))))
                return false;
        }
        XQueryType otherReturnedType = other.getReturnedType();
        return isNullableEquals(this.returnedType, otherReturnedType);
    }

    // private static final BiPredicate<XQueryEnumBasedType, XQueryEnumBasedType> alwaysTrue = (t1, t2) -> true;
    // private static final BiPredicate<XQueryEnumBasedType, XQueryEnumBasedType> alwaysFalse = (t1, t2) -> false;
    // private static final BiPredicate<XQueryEnumBasedType, XQueryEnumBasedType>[][] isSubtypeOf;
    // static {
    //     int occurenceCount = XQueryOccurence.values().length;
    //     var _isSubtypeOf = new Object[][] {
    //         // ONE
    //         //    + ONE       + ZERO      + ZERO_OR_ONE  + ONE_OR_MORE  +ZERO_OR_MORE
    //             { alwaysTrue, alwaysTrue, alwaysTrue,    alwaysFalse,   alwaysFalse}
    //         // ZERO,
    //         //    + ONE       + ZERO      + ZERO_OR_ONE  + ONE_OR_MORE  +ZERO_OR_MORE
    //             { alwaysTrue, alwaysTrue, alwaysTrue,    alwaysFalse,   alwaysFalse}
    //         // ZERO_OR_ONE,
    //         //    + ONE       + ZERO      + ZERO_OR_ONE  + ONE_OR_MORE  +ZERO_OR_MORE
    //         // ONE_OR_MORE,
    //         //    + ONE       + ZERO      + ZERO_OR_ONE  + ONE_OR_MORE  +ZERO_OR_MORE
    //         // ZERO_OR_MORE
    //         //    + ONE       + ZERO      + ZERO_OR_ONE  + ONE_OR_MORE  +ZERO_OR_MORE

    //     };
    // }

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
            case ONE -> switch (otherOccurence) {
                case ZERO -> false;
                default -> containedType.isSubtypeItemtypeOf(other.getSubType());
            };
            case ONE_OR_MORE -> switch (otherOccurence) {
                case ZERO_OR_MORE -> containedType.isSubtypeItemtypeOf(other.getSubType());
                case ONE_OR_MORE -> containedType.isSubtypeItemtypeOf(other.getSubType());
                default -> false;
            };
        };
    }

    @Override
    public boolean isSubtypeItemtypeOf(XQueryType obj) {
        if (!(obj instanceof XQueryEnumBasedType))
            return false;
        XQueryEnumBasedType other = (XQueryEnumBasedType) obj;
        if (isAtomic() && type == other.getType())
            return true;
        return switch (other.getType()) {
            case ERROR -> this.isAtomic();
            case ANY_ITEM -> true;
            case ANY_NODE -> this.isNode();
            case ANY_ELEMENT -> this.isElement();
            case ELEMENT -> this.isElement(other.getName());
            case ANY_FUNCTION -> this.isFunction();
            case FUNCTION -> this.isFunction(other.getName(), other.getReturnedType(), other.getArgumentTypes());
            case ANY_MAP -> this.isMap();
            // case MAP -> this.isMap();
            case ANY_ARRAY -> this.isArray();
            // case ARRAY -> this.isArray();
            default -> false;
        };
    }

    private static final boolean[] isAtomic = falseEnumArray(
            XQueryTypes.SEQUENCE,
            XQueryTypes.EMPTY_SEQUENCE);

    @Override
    public boolean isAtomic() {
        return isAtomic[type.ordinal()];
    }

    public XQueryOccurence getOccurence() {
        return occurence;
    }

    private static boolean[] booleanEnumArray(boolean value, XQueryTypes... values) {
        var array = new boolean[XQueryTypes.values().length];
        for (var v : values) {
            array[v.ordinal()] = value;
        }
        return array;
    }

    private static boolean[] falseEnumArray(XQueryTypes... falsevalues) {
        return booleanEnumArray(false, falsevalues);
    }

    private static boolean[] trueEnumArray(XQueryTypes... truevalues) {
        return booleanEnumArray(true, truevalues);
    }

    private static final boolean[] isNode = trueEnumArray(XQueryTypes.ANY_NODE, XQueryTypes.NODE);

    @Override
    public boolean isNode() {
        return isNode[type.ordinal()];
    }

    private static final boolean[] isElement = trueEnumArray(XQueryTypes.ANY_ELEMENT, XQueryTypes.ELEMENT);

    @Override
    public boolean isElement() {
        return isElement[type.ordinal()];
    }

    @Override
    public boolean isElement(String otherName) {
        return isElement() && name.equals(otherName);
    }

    private static final boolean[] isFunction = trueEnumArray(XQueryTypes.ANY_FUNCTION, XQueryTypes.FUNCTION);

    @Override
    public boolean isFunction() {
        return isFunction[type.ordinal()];
    }

    @Override
    public boolean isFunction(String otherName, XQueryType otherReturnedType, List<XQueryType> otherArgumentTypes) {
        return isFunction[type.ordinal()]
                && name.equals(otherName)
                && this.returnedType.equals(otherReturnedType)
                && this.argumentTypes.size() == otherArgumentTypes.size()
                && IntStream.range(0, this.argumentTypes.size())
                        .allMatch(i -> this.argumentTypes.get(i).equals(otherArgumentTypes.get(i)));
    }

    private static final boolean[] isMap = trueEnumArray(XQueryTypes.MAP, XQueryTypes.ANY_MAP);

    @Override
    public boolean isMap() {
        return isMap[type.ordinal()];
    }

    private static final boolean[] isArray = trueEnumArray(XQueryTypes.ARRAY, XQueryTypes.ANY_ARRAY);

    @Override
    public boolean isArray() {
        return isArray[type.ordinal()];
    }

}
