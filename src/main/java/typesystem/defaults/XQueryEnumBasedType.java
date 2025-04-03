package typesystem.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import javax.print.DocFlavor.STRING;

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

    public static final XQueryEnumBasedType string = new XQueryEnumBasedType(XQueryTypes.STRING, null, null, null, null, null);
    public static XQueryEnumBasedType string() {
        return sequence(string, XQueryOccurence.ONE);
    }

    public static final XQueryEnumBasedType number = new XQueryEnumBasedType(XQueryTypes.NUMBER, null, null, null, null, null);
    public static XQueryEnumBasedType number() {
        return sequence(number, XQueryOccurence.ONE);
    }

    public static final XQueryEnumBasedType integer = new XQueryEnumBasedType(XQueryTypes.INTEGER, null, null, null, null, null);
    public static XQueryEnumBasedType integer() {
        return sequence(integer, XQueryOccurence.ONE);
    }

    public static final XQueryEnumBasedType anynode = new XQueryEnumBasedType(XQueryTypes.ANY_NODE, null, null, null, null, null);
    public static XQueryEnumBasedType anyNode() {
        return sequence(anynode, XQueryOccurence.ONE);
    }

    public static final XQueryEnumBasedType anyarray = new XQueryEnumBasedType(XQueryTypes.ANY_NODE, null, null, null, null, null);
    public static XQueryEnumBasedType anyArray() {
        return sequence(anyarray, XQueryOccurence.ONE);
    }

    public static final XQueryEnumBasedType anymap = new XQueryEnumBasedType(XQueryTypes.ANY_MAP, null, null, null, null, null);
    public static XQueryEnumBasedType anyMap() {
        return sequence(anymap, XQueryOccurence.ONE);
    }

    public static final XQueryEnumBasedType anyelement = new XQueryEnumBasedType(XQueryTypes.ANY_ELEMENT, null, null, null, null, null);
    public static XQueryEnumBasedType anyElement() {
        return sequence(anyelement, XQueryOccurence.ONE);
    }

    public static final XQueryEnumBasedType anyfunction = new XQueryEnumBasedType(XQueryTypes.ANY_ELEMENT, null, null, null, null, null);
    public static XQueryEnumBasedType anyFunction() {
        return sequence(anyfunction, XQueryOccurence.ONE);
    }

    public static final XQueryEnumBasedType anyitem = new XQueryEnumBasedType(XQueryTypes.ANY_ITEM, null, null, null, null, null);
    public static XQueryEnumBasedType anyItem() {
        return sequence(anyitem, XQueryOccurence.ONE);
    }

    public static final XQueryEnumBasedType boolean_ = new XQueryEnumBasedType(XQueryTypes.BOOLEAN, null, null, null, null, null);
    public static XQueryEnumBasedType boolean_() {
        return sequence(boolean_, XQueryOccurence.ONE);
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

    private static final BiPredicate<XQueryEnumBasedType, XQueryEnumBasedType> alwaysTrue = (t1, t2) -> true;
    private static final BiPredicate<XQueryEnumBasedType, XQueryEnumBasedType> alwaysFalse = (t1, t2) -> false;
    private static final int occurenceCount = XQueryOccurence.values().length;
    private static final BiPredicate<XQueryEnumBasedType, XQueryEnumBasedType>[][] isSubtypeOf = new BiPredicate[occurenceCount][occurenceCount];
    static {
        for (int i = 0; i < occurenceCount; i++) {
            for (int j = 0; j < occurenceCount; j++) {
                isSubtypeOf[i][j] = alwaysFalse;
            }
        }
        int one = XQueryOccurence.ONE.ordinal();
        for (int i = 0; i < occurenceCount; i++) {
            // isSubtypeOf[one][i] = ;
        }
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

    private static final boolean[] isAtomic = booleanEnumArray(t -> t != XQueryTypes.SEQUENCE || t!= XQueryTypes.SEQUENCE);

    @Override
    public boolean isAtomic() {
        return isAtomic[type.ordinal()];
    }

    public XQueryOccurence getOccurence() {
        return occurence;
    }

    private static boolean[] booleanEnumArray(XQueryTypes... values) {
        var array = new boolean[XQueryTypes.values().length];
        for (var v : values) {
            array[v.ordinal()] = true;
        }
        return array;
    }

    private static boolean[] booleanEnumArray(Predicate<XQueryTypes> predicateForTrueValues) {
        var array = new boolean[XQueryTypes.values().length];
        for (int j = 0; j < array.length; j++) {
            XQueryTypes tested = XQueryTypes.values()[j];
            array[j] = predicateForTrueValues.test(tested);
        }
        return array;
    }

    private static final boolean[] isNode = booleanEnumArray(XQueryTypes.ANY_NODE, XQueryTypes.NODE);

    @Override
    public boolean isNode() {
        return isNode[type.ordinal()];
    }

    private static final boolean[] isElement = booleanEnumArray(XQueryTypes.ANY_ELEMENT, XQueryTypes.ELEMENT);

    @Override
    public boolean isElement() {
        return isElement[type.ordinal()];
    }

    @Override
    public boolean isElement(String otherName) {
        return isElement() && name.equals(otherName);
    }

    private static final boolean[] isFunction = booleanEnumArray(XQueryTypes.ANY_FUNCTION, XQueryTypes.FUNCTION);

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

    private static final boolean[] isMap = booleanEnumArray(XQueryTypes.MAP, XQueryTypes.ANY_MAP);

    @Override
    public boolean isMap() {
        return isMap[type.ordinal()];
    }

    private static final boolean[] isArray = booleanEnumArray(XQueryTypes.ARRAY, XQueryTypes.ANY_ARRAY);

    @Override
    public boolean isArray() {
        return isArray[type.ordinal()];
    }

}
