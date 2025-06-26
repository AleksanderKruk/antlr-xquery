package com.github.akruk.antlrxquery.values;

import java.math.BigDecimal;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

public class XQueryError implements XQueryValue {

    public static final XQueryError MissingDynamicContextComponent =
        new XQueryError("XPDY0002", "Evaluation relies on a missing component of the dynamic context.");
    public static final XQueryError TreatAsTypeMismatch =
        new XQueryError("XPDY0050", "Dynamic type does not match required sequence type in 'treat as'.");
    public static final XQueryError ImplementationLimitExceeded =
        new XQueryError("XPDY0130", "Implementation-dependent limit exceeded.");
    public static final XQueryError DuplicateAttributeNames =
        new XQueryError("XQDY0025", "Constructed element has attributes with non-distinct names.");
    public static final XQueryError InvalidProcessingInstructionContent =
        new XQueryError("XQDY0026", "Content of computed processing instruction contains '?>'.");
    public static final XQueryError UnexpectedValidationResult =
        new XQueryError("XQDY0027", "Validation result root element does not have the expected validity property.");
    public static final XQueryError InvalidProcessingInstructionNameCast =
        new XQueryError("XQDY0041", "The name expression in a computed processing instruction cannot be cast to xs:NCName.");
    public static final XQueryError InvalidAttributeNodeName =
        new XQueryError("XQDY0044", "Invalid node-name in a computed attribute constructor due to namespace rules.");
    public static final XQueryError InvalidValidateOperand =
        new XQueryError("XQDY0061", "validate expression operand must have exactly one element child.");
    public static final XQueryError XmlProcessingInstructionDisallowed =
        new XQueryError("XQDY0064", "Computed processing instruction name must not be 'XML' (case insensitive).");
    public static final XQueryError InvalidCommentContent =
        new XQueryError("XQDY0072", "Computed comment contains '--' or ends with '-'.");
    public static final XQueryError InvalidQNameConversion =
        new XQueryError("XQDY0074", "Name expression cannot be converted to an expanded QName.");
    public static final XQueryError MissingElementDeclaration =
        new XQueryError("XQDY0084", "Validated element lacks top-level declaration in strict mode.");
    public static final XQueryError XmlIdConstraintViolation =
        new XQueryError("XQDY0091", "xml:id attribute construction encountered an XML ID error.");
    public static final XQueryError InvalidXmlSpaceValue =
        new XQueryError("XQDY0092", "Constructed xml:space attribute has invalid value.");
    public static final XQueryError InvalidElementNodeName =
        new XQueryError("XQDY0096", "Invalid node-name in computed element constructor due to namespace rules.");
    public static final XQueryError InvalidNamespaceBinding =
        new XQueryError("XQDY0101", "Invalid computed namespace constructor bindings.");
    public static final XQueryError ConflictingNamespaceBindings =
        new XQueryError("XQDY0102", "Conflicting or duplicate namespace bindings in element constructor.");
    public static final XQueryError DuplicateMapKeys =
        new XQueryError("XQDY0137", "Duplicate keys in a map.");
    public static final XQueryError MixedNodesAndAtomicInPath =
        new XQueryError("XPTY0018", "Path operator result contains both nodes and non-nodes.");
    public static final XQueryError PathLhsNotNodes =
        new XQueryError("XPTY0019", "Path expression left-hand side does not evaluate to sequence of nodes.");
    public static final XQueryError AxisStepContextItemNotNode =
        new XQueryError("XPTY0020", "Context item is not a node in an axis step.");
    public static final XQueryError NamespaceSensitiveCastOnUntyped =
        new XQueryError("XPTY0117", "Namespace-sensitive coercion on untypedAtomic.");
    public static final XQueryError InvalidTypeInForMember =
        new XQueryError("XPTY0141", "Incorrect type for collection in 'for member' or 'for key/value' clause.");
    public static final XQueryError AxisStepAlwaysEmpty =
        new XQueryError("XPTY0144", "Axis step will always return an empty sequence due to implausible type.");
    public static final XQueryError LookupAlwaysEmpty =
        new XQueryError("XPTY0145", "Lookup expression will always return an empty sequence due to implausible type.");

    private final String code;
    private final String description;

    XQueryError(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public ParseTree node() {
        return null;
    }

    @Override
    public BigDecimal numericValue() {
        return null;
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public Boolean booleanValue() {
        return null;
    }

    @Override
    public XQueryFunction functionValue() {
        return null;
    }

    @Override
    public Boolean effectiveBooleanValue() {
        return null;
    }

    @Override
    public List<XQueryValue> sequence() {
        return null;
    }

    @Override
    public boolean isNumericValue() {
        return false;
    }

    @Override
    public boolean isStringValue() {
        return false;
    }

    @Override
    public boolean isBooleanValue() {
        return false;
    }

    @Override
    public boolean isSequence() {
        return false;
    }

    @Override
    public boolean isAtomic() {
        return false;
    }

    @Override
    public boolean isNode() {
        return false;
    }

    @Override
    public boolean isFunction() {
        return false;
    }

    @Override
    public List<XQueryValue> atomize() {
        return null;
    }

    @Override
    public XQueryValue copy() {
        return null;
    }

    @Override
    public XQueryValue empty() {
        return null;
    }

    @Override
    public XQueryValue not() {
        return null;
    }

    @Override
    public XQueryValue and(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue or(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue add(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue subtract(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue multiply(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue divide(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue integerDivide(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue modulus(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue concatenate(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue valueEqual(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue valueUnequal(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue valueLessThan(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue valueLessEqual(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue valueGreaterThan(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue valueGreaterEqual(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue generalEqual(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue generalUnequal(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue generalLessThan(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue generalLessEqual(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue generalGreaterThan(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue generalGreaterEqual(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue union(XQueryValue otherSequence) {
        return null;
    }

    @Override
    public XQueryValue intersect(XQueryValue otherSequence) {
        return null;
    }

    @Override
    public XQueryValue except(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue head() {
        return null;
    }

    @Override
    public XQueryValue tail() {
        return null;
    }

    @Override
    public XQueryValue insertBefore(XQueryValue position, XQueryValue inserted) {
        return null;
    }

    @Override
    public XQueryValue insertAfter(XQueryValue position, XQueryValue inserted) {
        return null;
    }

    @Override
    public XQueryValue remove(XQueryValue position) {
        return null;
    }

    @Override
    public XQueryValue reverse() {
        return null;
    }

    @Override
    public XQueryValue subsequence(int startingLoc) {
        return null;
    }

    @Override
    public XQueryValue subsequence(int startingLoc, int length) {
        return null;
    }

    @Override
    public XQueryValue distinctValues() {
        return null;
    }

    @Override
    public XQueryValue zeroOrOne() {
        return null;
    }

    @Override
    public XQueryValue oneOrMore() {
        return null;
    }

    @Override
    public XQueryValue exactlyOne() {
        return null;
    }

    @Override
    public XQueryValue data() {
        return null;
    }

    @Override
    public XQueryValue contains(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue startsWith(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue endsWith(XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue uppercase() {
        return null;
    }

    @Override
    public XQueryValue lowercase() {
        return null;
    }

    @Override
    public XQueryValue substring(int startingLoc) {
        return null;
    }

    @Override
    public XQueryValue substring(int startingLoc, int length) {
        return null;
    }

    @Override
    public XQueryValue substringBefore(XQueryValue splitstring) {
        return null;
    }

    @Override
    public XQueryValue substringAfter(XQueryValue splitstring) {
        return null;
    }

}
