package com.github.akruk.antlrxquery.values;

import java.math.BigDecimal;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

public class XQueryError implements XQueryValue {

    public static final XQueryError MissingDynamicContextComponent = new XQueryError("XPDY0002",
            "Evaluation relies on a missing component of the dynamic context.");
    public static final XQueryError TreatAsTypeMismatch = new XQueryError("XPDY0050",
            "Dynamic type does not match required sequence type in 'treat as'.");
    public static final XQueryError ImplementationLimitExceeded = new XQueryError("XPDY0130",
            "Implementation-dependent limit exceeded.");
    public static final XQueryError DuplicateAttributeNames = new XQueryError("XQDY0025",
            "Constructed element has attributes with non-distinct names.");
    public static final XQueryError InvalidProcessingInstructionContent = new XQueryError("XQDY0026",
            "Content of computed processing instruction contains '?>'.");
    public static final XQueryError UnexpectedValidationResult = new XQueryError("XQDY0027",
            "Validation result root element does not have the expected validity property.");
    public static final XQueryError InvalidProcessingInstructionNameCast = new XQueryError("XQDY0041",
            "The name expression in a computed processing instruction cannot be cast to xs:NCName.");
    public static final XQueryError InvalidAttributeNodeName = new XQueryError("XQDY0044",
            "Invalid node-name in a computed attribute constructor due to namespace rules.");
    public static final XQueryError InvalidValidateOperand = new XQueryError("XQDY0061",
            "validate expression operand must have exactly one element child.");
    public static final XQueryError XmlProcessingInstructionDisallowed = new XQueryError("XQDY0064",
            "Computed processing instruction name must not be 'XML' (case insensitive).");
    public static final XQueryError InvalidCommentContent = new XQueryError("XQDY0072",
            "Computed comment contains '--' or ends with '-'.");
    public static final XQueryError InvalidQNameConversion = new XQueryError("XQDY0074",
            "Name expression cannot be converted to an expanded QName.");
    public static final XQueryError MissingElementDeclaration = new XQueryError("XQDY0084",
            "Validated element lacks top-level declaration in strict mode.");
    public static final XQueryError XmlIdConstraintViolation = new XQueryError("XQDY0091",
            "xml:id attribute construction encountered an XML ID error.");
    public static final XQueryError InvalidXmlSpaceValue = new XQueryError("XQDY0092",
            "Constructed xml:space attribute has invalid value.");
    public static final XQueryError InvalidElementNodeName = new XQueryError("XQDY0096",
            "Invalid node-name in computed element constructor due to namespace rules.");
    public static final XQueryError InvalidNamespaceBinding = new XQueryError("XQDY0101",
            "Invalid computed namespace constructor bindings.");
    public static final XQueryError ConflictingNamespaceBindings = new XQueryError("XQDY0102",
            "Conflicting or duplicate namespace bindings in element constructor.");
    public static final XQueryError DuplicateMapKeys = new XQueryError("XQDY0137", "Duplicate keys in a map.");
    public static final XQueryError MixedNodesAndAtomicInPath = new XQueryError("XPTY0018",
            "Path operator result contains both nodes and non-nodes.");
    public static final XQueryError PathLhsNotNodes = new XQueryError("XPTY0019",
            "Path expression left-hand side does not evaluate to sequence of nodes.");
    public static final XQueryError AxisStepContextItemNotNode = new XQueryError("XPTY0020",
            "Context item is not a node in an axis step.");
    public static final XQueryError NamespaceSensitiveCastOnUntyped = new XQueryError("XPTY0117",
            "Namespace-sensitive coercion on untypedAtomic.");
    public static final XQueryError InvalidTypeInForMember = new XQueryError("XPTY0141",
            "Incorrect type for collection in 'for member' or 'for key/value' clause.");
    public static final XQueryError AxisStepAlwaysEmpty = new XQueryError("XPTY0144",
            "Axis step will always return an empty sequence due to implausible type.");
    public static final XQueryError LookupAlwaysEmpty = new XQueryError("XPTY0145",
            "Lookup expression will always return an empty sequence due to implausible type.");

    public static final XQueryError WrongNumberOfArguments = new XQueryError("FOAP0001",
            "fn:apply called with wrong number of arguments.");
    public static final XQueryError DivisionByZero = new XQueryError("FOAR0001", "Division by zero.");
    public static final XQueryError NumericOverflowUnderflow = new XQueryError("FOAR0002",
            "Numeric operation overflow or underflow.");
    public static final XQueryError ArrayIndexOutOfBounds = new XQueryError("FOAY0001", "Array index out of bounds.");
    public static final XQueryError NegativeArrayLength = new XQueryError("FOAY0002",
            "Negative array length in array:subarray.");
    public static final XQueryError DecimalInputTooLarge = new XQueryError("FOCA0001",
            "Input value too large for xs:decimal.");
    public static final XQueryError InvalidLexicalValue = new XQueryError("FOCA0002",
            "Invalid lexical value for casting or QName resolution.");
    public static final XQueryError IntegerInputTooLarge = new XQueryError("FOCA0003",
            "Input value too large for xs:integer.");
    public static final XQueryError NaNSupplied = new XQueryError("FOCA0005", "NaN supplied as float/double value.");
    public static final XQueryError DecimalPrecisionTooHigh = new XQueryError("FOCA0006",
            "String has too many digits for xs:decimal precision.");
    public static final XQueryError InvalidCodepoint = new XQueryError("FOCH0001",
            "Invalid codepoint in codepoints-to-string.");
    public static final XQueryError UnsupportedCollation = new XQueryError("FOCH0002", "Unsupported collation.");
    public static final XQueryError UnsupportedNormalizationForm = new XQueryError("FOCH0003",
            "Unsupported normalization form.");
    public static final XQueryError CollationUnitsNotSupported = new XQueryError("FOCH0004",
            "Collation does not support collation units.");
    public static final XQueryError UnrecognizedOrInvalidCharacterName = new XQueryError("FOCH0005",
            "Unrecognized or invalid character name.");
    public static final XQueryError CsvFieldQuotingError = new XQueryError("FOCV0001", "CSV field quoting error.");
    public static final XQueryError InvalidCsvDelimiter = new XQueryError("FOCV0002", "Invalid CSV delimiter.");
    public static final XQueryError DuplicateCsvDelimiter = new XQueryError("FOCV0003",
            "Duplicate CSV delimiter roles.");
    public static final XQueryError UnknownCsvColumnName = new XQueryError("FOCV0004", "Unknown CSV column name.");
    public static final XQueryError NoContextDocument = new XQueryError("FODC0001", "No context document.");
    public static final XQueryError ResourceRetrievalError = new XQueryError("FODC0002",
            "Error retrieving resource or non-XML returned.");
    public static final XQueryError NonDeterministicFunction = new XQueryError("FODC0003",
            "Function result not deterministic.");
    public static final XQueryError InvalidCollectionUri = new XQueryError("FODC0004", "Invalid collection URI.");
    public static final XQueryError InvalidUriReference = new XQueryError("FODC0005", "Invalid URI reference.");
    public static final XQueryError ParseXmlNotWellFormed = new XQueryError("FODC0006",
            "String is not well-formed XML.");
    public static final XQueryError ParseXmlNotDtdValid = new XQueryError("FODC0007", "String is not DTD-valid XML.");
    public static final XQueryError InvalidXsdValidationOption = new XQueryError("FODC0008",
            "Invalid xsd-validation option.");
    public static final XQueryError NotSchemaAwareProcessor = new XQueryError("FODC0009",
            "Processor not schema-aware.");
    public static final XQueryError SerializationNotSupported = new XQueryError("FODC0010",
            "Processor does not support serialization.");
    public static final XQueryError ParseHtmlNotWellFormed = new XQueryError("FODC0011",
            "String is not well-formed HTML.");
    public static final XQueryError UnsupportedHtmlOption = new XQueryError("FODC0012",
            "Unsupported HTML parser option.");
    public static final XQueryError NoValidatingXmlParser = new XQueryError("FODC0013",
            "No validating XML parser available.");
    public static final XQueryError XsdValidationFailed = new XQueryError("FODC0014", "XSD validation failed.");
    public static final XQueryError SchemaCompilationError = new XQueryError("FODC0015",
            "Cannot compile schema for xsd-validator.");
    public static final XQueryError InvalidDecimalFormatName = new XQueryError("FODF1280",
            "Invalid decimal format name.");
    public static final XQueryError InvalidDecimalFormatProperty = new XQueryError("FODF1290",
            "Invalid decimal format property.");
    public static final XQueryError InvalidDecimalFormatPicture = new XQueryError("FODF1310",
            "Invalid picture string for decimal format.");
    public static final XQueryError DateTimeOverflow = new XQueryError("FODT0001",
            "Overflow/underflow in date/time operation.");
    public static final XQueryError DurationOverflow = new XQueryError("FODT0002",
            "Overflow/underflow in duration operation.");
    public static final XQueryError InvalidTimezoneValue = new XQueryError("FODT0003", "Invalid timezone value.");
    public static final XQueryError NoTimezoneData = new XQueryError("FODT0004", "No timezone data available.");
    public static final XQueryError UnidentifiedError = new XQueryError("FOER0000", "Unidentified error.");
    public static final XQueryError InvalidDateTimeFormatParams = new XQueryError("FOFD1340",
            "Invalid date/time formatting parameters.");
    public static final XQueryError InvalidDateTimeFormatComponent = new XQueryError("FOFD1350",
            "Invalid formatting component.");
    public static final XQueryError InvalidHashAlgorithm = new XQueryError("FOHA0001", "Invalid hash algorithm.");
    public static final XQueryError JsonSyntaxError = new XQueryError("FOJS0001", "JSON syntax error.");
    public static final XQueryError JsonDuplicateKeys = new XQueryError("FOJS0003", "Duplicate keys in JSON.");
    public static final XQueryError JsonSchemaNotSupported = new XQueryError("FOJS0004",
            "JSON validation requested but not supported.");
    public static final XQueryError JsonInvalidOptions = new XQueryError("FOJS0005",
            "Invalid JSON processing options.");
    public static final XQueryError JsonInvalidXmlRepresentation = new XQueryError("FOJS0006",
            "Invalid XML representation of JSON.");
    public static final XQueryError JsonBadEscapeSequence = new XQueryError("FOJS0007",
            "Invalid JSON escape sequence.");
    public static final XQueryError ElementToMapConversionError = new XQueryError("FOJS0008",
            "Cannot convert element to map.");
    public static final XQueryError NamespaceNotFound = new XQueryError("FONS0004", "No namespace found for prefix.");
    public static final XQueryError BaseUriNotDefined = new XQueryError("FONS0005", "Base URI not defined.");
    public static final XQueryError OriginNotAncestor = new XQueryError("FOPA0001",
            "Origin node is not ancestor of target.");
    public static final XQueryError ModuleUriEmpty = new XQueryError("FOQM0001", "Module URI is empty.");
    public static final XQueryError ModuleUriNotFound = new XQueryError("FOQM0002", "Module URI not found.");
    public static final XQueryError StaticErrorInLoadedModule = new XQueryError("FOQM0003",
            "Static error in dynamically loaded XQuery module.");
    public static final XQueryError ModuleParameterTypeMismatch = new XQueryError("FOQM0005",
            "Incorrect parameter type in XQuery module.");
    public static final XQueryError NoXQueryProcessor = new XQueryError("FOQM0006",
            "No suitable XQuery processor available.");
    public static final XQueryError InvalidCastValue = new XQueryError("FORG0001",
            "Invalid value for cast or constructor.");
    public static final XQueryError InvalidResolveUriArg = new XQueryError("FORG0002",
            "Invalid argument to fn:resolve-uri.");
    public static final XQueryError ZeroOrOneWrongArity = new XQueryError("FORG0003",
            "fn:zero-or-one called with multiple items.");
    public static final XQueryError OneOrMoreEmpty = new XQueryError("FORG0004",
            "fn:one-or-more called with empty sequence.");
    public static final XQueryError ExactlyOneWrongArity = new XQueryError("FORG0005",
            "fn:exactly-one called with wrong item count.");
    public static final XQueryError InvalidArgumentType = new XQueryError("FORG0006",
            "Invalid argument type");
    public static final XQueryError InconsistentTimezones = new XQueryError("FORG0008",
            "Inconsistent timezones in fn:dateTime.");
    public static final XQueryError ResolveUriError = new XQueryError("FORG0009", "Error resolving relative URI.");
    public static final XQueryError InvalidDateTime = new XQueryError("FORG0010", "Invalid date/time value.");
    public static final XQueryError InvalidRadix = new XQueryError("FORG0011", "Invalid radix for fn:parse-integer.");
    public static final XQueryError InvalidDigitsForRadix = new XQueryError("FORG0012",
            "Invalid digits for specified radix.");
    public static final XQueryError InvalidRegexFlags = new XQueryError("FORX0001",
            "Invalid regular expression flags.");
    public static final XQueryError InvalidRegex = new XQueryError("FORX0002", "Invalid regular expression.");
    public static final XQueryError RegexMatchesZeroLength = new XQueryError("FORX0003",
            "Regular expression matches zero-length string.");
    public static final XQueryError InvalidReplacementString = new XQueryError("FORX0004",
            "Invalid replacement string.");
    public static final XQueryError IncompatibleReplaceArgs = new XQueryError("FORX0005",
            "Incompatible arguments for fn:replace.");
    public static final XQueryError DataNodeWithoutTypedValue = new XQueryError("FOTY0012",
            "Node without typed value in fn:data.");
    public static final XQueryError DataFunctionItemFound = new XQueryError("FOTY0013", "Function item in fn:data.");
    public static final XQueryError StringFunctionItemFound = new XQueryError("FOTY0014",
            "Function item in fn:string.");
    public static final XQueryError InvalidUriAuthority = new XQueryError("FOUR0001",
            "Invalid IPv6/IPvFuture authority in URI.");
    public static final XQueryError UnparsedTextUriRefError = new XQueryError("FOUT1170",
            "Invalid URI reference in fn:unparsed-text.");
    public static final XQueryError CannotDecodeExternalResource = new XQueryError("FOUT1190",
            "Cannot decode external resource.");
    public static final XQueryError CannotInferExternalEncoding = new XQueryError("FOUT1200",
            "Cannot infer encoding of external resource.");
    public static final XQueryError NoXsltProcessor = new XQueryError("FOXT0001",
            "No suitable XSLT processor available.");
    public static final XQueryError InvalidXsltTransformParams = new XQueryError("FOXT0002",
            "Invalid parameters to fn:transform.");
    public static final XQueryError XsltTransformationFailed = new XQueryError("FOXT0003",
            "XSLT transformation failed.");
    public static final XQueryError XsltTransformationDisabled = new XQueryError("FOXT0004",
            "XSLT transformation has been disabled.");
    public static final XQueryError XsltInvalidOutputCharacters = new XQueryError("FOXT0006",
            "XSLT output contains non-accepted characters.");
    public static final XQueryValue UnknownFunctionName = new XQueryError("AXQY0001", "Called function that is not registered");

    private final String code;
    private final String description;

    XQueryError(final String code, final String description) {
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
    public XQueryValue and(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue or(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue add(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue subtract(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue multiply(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue divide(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue integerDivide(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue modulus(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue concatenate(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue valueEqual(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue valueUnequal(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue valueLessThan(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue valueLessEqual(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue valueGreaterThan(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue valueGreaterEqual(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue generalEqual(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue generalUnequal(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue generalLessThan(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue generalLessEqual(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue generalGreaterThan(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue generalGreaterEqual(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue union(final XQueryValue otherSequence) {
        return null;
    }

    @Override
    public XQueryValue intersect(final XQueryValue otherSequence) {
        return null;
    }

    @Override
    public XQueryValue except(final XQueryValue other) {
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
    public XQueryValue insertBefore(final XQueryValue position, final XQueryValue inserted) {
        return null;
    }

    @Override
    public XQueryValue insertAfter(final XQueryValue position, final XQueryValue inserted) {
        return null;
    }

    @Override
    public XQueryValue remove(final XQueryValue position) {
        return null;
    }

    @Override
    public XQueryValue reverse() {
        return null;
    }

    @Override
    public XQueryValue subsequence(final int startingLoc) {
        return null;
    }

    @Override
    public XQueryValue subsequence(final int startingLoc, final int length) {
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
    public XQueryValue contains(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue startsWith(final XQueryValue other) {
        return null;
    }

    @Override
    public XQueryValue endsWith(final XQueryValue other) {
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
    public XQueryValue substring(final int startingLoc) {
        return null;
    }

    @Override
    public XQueryValue substring(final int startingLoc, final int length) {
        return null;
    }

    @Override
    public XQueryValue substringBefore(final XQueryValue splitstring) {
        return null;
    }

    @Override
    public XQueryValue substringAfter(final XQueryValue splitstring) {
        return null;
    }

    @Override
    public boolean isError() {
        return true;
    }

    @Override
    public boolean isEmptySequence() {
        return false;
    }

}
