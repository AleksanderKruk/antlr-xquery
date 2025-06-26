package com.github.akruk.antlrxquery.values;

import java.math.BigDecimal;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

public enum XQueryError implements XQueryValue {
    MissingDynamicContextComponent("XPDY0002", "Evaluation relies on a missing component of the dynamic context."),
    TreatAsTypeMismatch("XPDY0050", "Dynamic type does not match required sequence type in 'treat as'."),
    ImplementationLimitExceeded("XPDY0130", "Implementation-dependent limit exceeded."),
    DuplicateAttributeNames("XQDY0025", "Constructed element has attributes with non-distinct names."),
    InvalidProcessingInstructionContent("XQDY0026", "Content of computed processing instruction contains '?>'."),
    UnexpectedValidationResult("XQDY0027", "Validation result root element does not have the expected validity property."),
    InvalidProcessingInstructionNameCast("XQDY0041", "The name expression in a computed processing instruction cannot be cast to xs:NCName."),
    InvalidAttributeNodeName("XQDY0044", "Invalid node-name in a computed attribute constructor due to namespace rules."),
    InvalidValidateOperand("XQDY0061", "validate expression operand must have exactly one element child."),
    XmlProcessingInstructionDisallowed("XQDY0064", "Computed processing instruction name must not be 'XML' (case insensitive)."),
    InvalidCommentContent("XQDY0072", "Computed comment contains '--' or ends with '-'."),
    InvalidQNameConversion("XQDY0074", "Name expression cannot be converted to an expanded QName."),
    MissingElementDeclaration("XQDY0084", "Validated element lacks top-level declaration in strict mode."),
    XmlIdConstraintViolation("XQDY0091", "xml:id attribute construction encountered an XML ID error."),
    InvalidXmlSpaceValue("XQDY0092", "Constructed xml:space attribute has invalid value."),
    InvalidElementNodeName("XQDY0096", "Invalid node-name in computed element constructor due to namespace rules."),
    InvalidNamespaceBinding("XQDY0101", "Invalid computed namespace constructor bindings."),
    ConflictingNamespaceBindings("XQDY0102", "Conflicting or duplicate namespace bindings in element constructor."),
    DuplicateMapKeys("XQDY0137", "Duplicate keys in a map."),
    MixedNodesAndAtomicInPath("XPTY0018", "Path operator result contains both nodes and non-nodes."),
    PathLhsNotNodes("XPTY0019", "Path expression left-hand side does not evaluate to sequence of nodes."),
    AxisStepContextItemNotNode("XPTY0020", "Context item is not a node in an axis step."),
    NamespaceSensitiveCastOnUntyped("XPTY0117", "Namespace-sensitive coercion on untypedAtomic."),
    InvalidTypeInForMember("XPTY0141", "Incorrect type for collection in 'for member' or 'for key/value' clause."),
    AxisStepAlwaysEmpty("XPTY0144", "Axis step will always return an empty sequence due to implausible type."),
    LookupAlwaysEmpty("XPTY0145", "Lookup expression will always return an empty sequence due to implausible type.");
    ;

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
