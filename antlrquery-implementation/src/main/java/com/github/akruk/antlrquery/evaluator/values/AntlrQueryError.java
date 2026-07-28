package com.github.akruk.antlrquery.evaluator.values;

public final class AntlrQueryError {

    public static final AntlrQueryError MissingDynamicContextComponent = new AntlrQueryError("XPDY0002",
            "Evaluation relies on a missing component of the dynamic context.");
    public static final AntlrQueryError TreatAsTypeMismatch = new AntlrQueryError("XPDY0050",
            "Dynamic type does not match required sequence type in 'treat as'.");
    public static final AntlrQueryError ImplementationLimitExceeded = new AntlrQueryError("XPDY0130",
            "Implementation-dependent limit exceeded.");
    public static final AntlrQueryError DuplicateAttributeNames = new AntlrQueryError("XQDY0025",
            "Constructed element has attributes with non-distinct names.");
    public static final AntlrQueryError InvalidProcessingInstructionContent = new AntlrQueryError("XQDY0026",
            "Content of computed processing instruction contains '?>'.");
    public static final AntlrQueryError UnexpectedValidationResult = new AntlrQueryError("XQDY0027",
            "Validation result root element does not have the expected validity property.");
    public static final AntlrQueryError InvalidProcessingInstructionNameCast = new AntlrQueryError("XQDY0041",
            "The name expression in a computed processing instruction cannot be cast to xs:NCName.");
    public static final AntlrQueryError InvalidAttributeNodeName = new AntlrQueryError("XQDY0044",
            "Invalid node-name in a computed attribute constructor due to namespace rules.");
    public static final AntlrQueryError InvalidValidateOperand = new AntlrQueryError("XQDY0061",
            "validate expression operand must have exactly one element child.");
    public static final AntlrQueryError XmlProcessingInstructionDisallowed = new AntlrQueryError("XQDY0064",
            "Computed processing instruction name must not be 'XML' (case insensitive).");
    public static final AntlrQueryError InvalidCommentContent = new AntlrQueryError("XQDY0072",
            "Computed comment contains '--' or ends with '-'.");
    public static final AntlrQueryError InvalidQNameConversion = new AntlrQueryError("XQDY0074",
            "Name expression cannot be converted to an expanded QName.");
    public static final AntlrQueryError MissingElementDeclaration = new AntlrQueryError("XQDY0084",
            "Validated element lacks top-level declaration in strict mode.");
    public static final AntlrQueryError XmlIdConstraintViolation = new AntlrQueryError("XQDY0091",
            "xml:id attribute construction encountered an XML ID error.");
    public static final AntlrQueryError InvalidXmlSpaceValue = new AntlrQueryError("XQDY0092",
            "Constructed xml:space attribute has invalid value.");
    public static final AntlrQueryError InvalidElementNodeName = new AntlrQueryError("XQDY0096",
            "Invalid node-name in computed element constructor due to namespace rules.");
    public static final AntlrQueryError InvalidNamespaceBinding = new AntlrQueryError("XQDY0101",
            "Invalid computed namespace constructor bindings.");
    public static final AntlrQueryError ConflictingNamespaceBindings = new AntlrQueryError("XQDY0102",
            "Conflicting or duplicate namespace bindings in element constructor.");
    public static final AntlrQueryError DuplicateMapKeys = new AntlrQueryError("XQDY0137", "Duplicate keys in a map.");
    public static final AntlrQueryError MixedNodesAndAtomicInPath = new AntlrQueryError("XPTY0018",
            "Path operator result contains both nodes and non-nodes.");
    public static final AntlrQueryError PathLhsNotNodes = new AntlrQueryError("XPTY0019",
            "Path expression left-hand side does not evaluate to sequence of nodes.");
    public static final AntlrQueryError AxisStepContextItemNotNode = new AntlrQueryError("XPTY0020",
            "Context item is not a node in an axis step.");
    public static final AntlrQueryError NamespaceSensitiveCastOnUntyped = new AntlrQueryError("XPTY0117",
            "Namespace-sensitive coercion on untypedAtomic.");
    public static final AntlrQueryError InvalidTypeInForMember = new AntlrQueryError("XPTY0141",
            "Incorrect type for collection in 'for member' or 'for key/value' clause.");
    public static final AntlrQueryError AxisStepAlwaysEmpty = new AntlrQueryError("XPTY0144",
            "Axis step will always return an empty sequence due to implausible type.");
    public static final AntlrQueryError LookupAlwaysEmpty = new AntlrQueryError("XPTY0145",
            "Lookup expression will always return an empty sequence due to implausible type.");

    public static final AntlrQueryError WrongNumberOfArguments = new AntlrQueryError("FOAP0001",
            "fn:apply called with wrong number of arguments.");
    public static final AntlrQueryError DivisionByZero = new AntlrQueryError("FOAR0001", "Division by zero.");
    public static final AntlrQueryError NumericOverflowUnderflow = new AntlrQueryError("FOAR0002",
            "Numeric operation overflow or underflow.");
    public static final AntlrQueryError ArrayIndexOutOfBounds = new AntlrQueryError("FOAY0001", "Array index out of bounds.");
    public static final AntlrQueryError NegativeArrayLength = new AntlrQueryError("FOAY0002",
            "Negative array cardinality in array:subarray.");
    public static final AntlrQueryError DecimalInputTooLarge = new AntlrQueryError("FOCA0001",
            "Input value too large for xs:decimal.");
    public static final AntlrQueryError InvalidLexicalValue = new AntlrQueryError("FOCA0002",
            "Invalid lexical value for casting or QName resolution.");
    public static final AntlrQueryError IntegerInputTooLarge = new AntlrQueryError("FOCA0003",
            "Input value too large for xs:integer.");
    public static final AntlrQueryError NaNSupplied = new AntlrQueryError("FOCA0005", "NaN supplied as float/double value.");
    public static final AntlrQueryError DecimalPrecisionTooHigh = new AntlrQueryError("FOCA0006",
            "String has too many digits for xs:decimal precision.");
    public static final AntlrQueryError InvalidCodepoint = new AntlrQueryError("FOCH0001",
            "Invalid codepoint in codepoints-to-string.");
    public static final AntlrQueryError UnsupportedCollation = new AntlrQueryError("FOCH0002", "Unsupported collation.");
    public static final AntlrQueryError UnsupportedNormalizationForm = new AntlrQueryError("FOCH0003",
            "Unsupported normalization form.");
    public static final AntlrQueryError CollationUnitsNotSupported = new AntlrQueryError("FOCH0004",
            "Collation does not support collation units.");
    public static final AntlrQueryError UnrecognizedOrInvalidCharacterName = new AntlrQueryError("FOCH0005",
            "Unrecognized or invalid character name.");
    public static final AntlrQueryError CsvFieldQuotingError = new AntlrQueryError("FOCV0001", "CSV field quoting error.");
    public static final AntlrQueryError InvalidCsvDelimiter = new AntlrQueryError("FOCV0002", "Invalid CSV delimiter.");
    public static final AntlrQueryError DuplicateCsvDelimiter = new AntlrQueryError("FOCV0003",
            "Duplicate CSV delimiter roles.");
    public static final AntlrQueryError UnknownCsvColumnName = new AntlrQueryError("FOCV0004", "Unknown CSV column name.");
    public static final AntlrQueryError NoContextDocument = new AntlrQueryError("FODC0001", "No context document.");
    public static final AntlrQueryError ResourceRetrievalError = new AntlrQueryError("FODC0002",
            "Error retrieving resource or non-XML returned.");
    public static final AntlrQueryError NonDeterministicFunction = new AntlrQueryError("FODC0003",
            "Function result not deterministic.");
    public static final AntlrQueryError InvalidCollectionUri = new AntlrQueryError("FODC0004", "Invalid collection URI.");
    public static final AntlrQueryError InvalidUriReference = new AntlrQueryError("FODC0005", "Invalid URI reference.");
    public static final AntlrQueryError ParseXmlNotWellFormed = new AntlrQueryError("FODC0006",
            "String is not well-formed XML.");
    public static final AntlrQueryError ParseXmlNotDtdValid = new AntlrQueryError("FODC0007", "String is not DTD-valid XML.");
    public static final AntlrQueryError InvalidXsdValidationOption = new AntlrQueryError("FODC0008",
            "Invalid xsd-validation option.");
    public static final AntlrQueryError NotSchemaAwareProcessor = new AntlrQueryError("FODC0009",
            "Processor not schema-aware.");
    public static final AntlrQueryError SerializationNotSupported = new AntlrQueryError("FODC0010",
            "Processor does not support serialization.");
    public static final AntlrQueryError ParseHtmlNotWellFormed = new AntlrQueryError("FODC0011",
            "String is not well-formed HTML.");
    public static final AntlrQueryError UnsupportedHtmlOption = new AntlrQueryError("FODC0012",
            "Unsupported HTML parser option.");
    public static final AntlrQueryError NoValidatingXmlParser = new AntlrQueryError("FODC0013",
            "No validating XML parser available.");
    public static final AntlrQueryError XsdValidationFailed = new AntlrQueryError("FODC0014", "XSD validation failed.");
    public static final AntlrQueryError SchemaCompilationError = new AntlrQueryError("FODC0015",
            "Cannot compile schema for xsd-validator.");
    public static final AntlrQueryError InvalidDecimalFormatName = new AntlrQueryError("FODF1280",
            "Invalid decimal format name.");
    public static final AntlrQueryError InvalidDecimalFormatProperty = new AntlrQueryError("FODF1290",
            "Invalid decimal format property.");
    public static final AntlrQueryError InvalidDecimalFormatPicture = new AntlrQueryError("FODF1310",
            "Invalid picture string for decimal format.");
    public static final AntlrQueryError DateTimeOverflow = new AntlrQueryError("FODT0001",
            "Overflow/underflow in date/time operation.");
    public static final AntlrQueryError DurationOverflow = new AntlrQueryError("FODT0002",
            "Overflow/underflow in duration operation.");
    public static final AntlrQueryError InvalidTimezoneValue = new AntlrQueryError("FODT0003", "Invalid timezone value.");
    public static final AntlrQueryError NoTimezoneData = new AntlrQueryError("FODT0004", "No timezone data available.");
    public static final AntlrQueryError UnidentifiedError = new AntlrQueryError("FOER0000", "Unidentified error.");
    public static final AntlrQueryError InvalidDateTimeFormatParams = new AntlrQueryError("FOFD1340",
            "Invalid date/time formatting parameters.");
    public static final AntlrQueryError InvalidDateTimeFormatComponent = new AntlrQueryError("FOFD1350",
            "Invalid formatting component.");
    public static final AntlrQueryError InvalidHashAlgorithm = new AntlrQueryError("FOHA0001", "Invalid hash algorithm.");
    public static final AntlrQueryError JsonSyntaxError = new AntlrQueryError("FOJS0001", "JSON syntax error.");
    public static final AntlrQueryError JsonDuplicateKeys = new AntlrQueryError("FOJS0003", "Duplicate keys in JSON.");
    public static final AntlrQueryError JsonSchemaNotSupported = new AntlrQueryError("FOJS0004",
            "JSON validation requested but not supported.");
    public static final AntlrQueryError JsonInvalidOptions = new AntlrQueryError("FOJS0005",
            "Invalid JSON processing options.");
    public static final AntlrQueryError JsonInvalidXmlRepresentation = new AntlrQueryError("FOJS0006",
            "Invalid XML representation of JSON.");
    public static final AntlrQueryError JsonBadEscapeSequence = new AntlrQueryError("FOJS0007",
            "Invalid JSON escape sequence.");
    public static final AntlrQueryError ElementToMapConversionError = new AntlrQueryError("FOJS0008",
            "Cannot convert element to map.");
    public static final AntlrQueryError NamespaceNotFound = new AntlrQueryError("FONS0004", "No namespace found for prefix.");
    public static final AntlrQueryError BaseUriNotDefined = new AntlrQueryError("FONS0005", "Base URI not defined.");
    public static final AntlrQueryError OriginNotAncestor = new AntlrQueryError("FOPA0001",
            "Origin node is not ancestor of target.");
    public static final AntlrQueryError ModuleUriEmpty = new AntlrQueryError("FOQM0001", "Module URI is empty.");
    public static final AntlrQueryError ModuleUriNotFound = new AntlrQueryError("FOQM0002", "Module URI not found.");
    public static final AntlrQueryError StaticErrorInLoadedModule = new AntlrQueryError("FOQM0003",
            "Static error in dynamically loaded XQuery module.");
    public static final AntlrQueryError ModuleParameterTypeMismatch = new AntlrQueryError("FOQM0005",
            "Incorrect parameter type in XQuery module.");
    public static final AntlrQueryError NoXQueryProcessor = new AntlrQueryError("FOQM0006",
            "No suitable XQuery processor available.");
    public static final AntlrQueryError InvalidCastValue = new AntlrQueryError("FORG0001",
            "Invalid value for cast or constructor.");
    public static final AntlrQueryError InvalidResolveUriArg = new AntlrQueryError("FORG0002",
            "Invalid argument to fn:resolve-uri.");
    public static final AntlrQueryError ZeroOrOneWrongArity = new AntlrQueryError("FORG0003",
            "fn:zero-or-one called with multiple items.");
    public static final AntlrQueryError OneOrMoreEmpty = new AntlrQueryError("FORG0004",
            "fn:one-or-more called with empty sequence.");
    public static final AntlrQueryError ExactlyOneWrongArity = new AntlrQueryError("FORG0005",
            "fn:exactly-one called with wrong item count.");
    public static final AntlrQueryError InvalidArgumentType = new AntlrQueryError("FORG0006",
            "Invalid argument type");
    public static final AntlrQueryError InconsistentTimezones = new AntlrQueryError("FORG0008",
            "Inconsistent timezones in fn:dateTime.");
    public static final AntlrQueryError ResolveUriError = new AntlrQueryError("FORG0009", "Error resolving relative URI.");
    public static final AntlrQueryError InvalidDateTime = new AntlrQueryError("FORG0010", "Invalid date/time value.");
    public static final AntlrQueryError InvalidRadix = new AntlrQueryError("FORG0011", "Invalid radix for fn:parse-integer.");
    public static final AntlrQueryError InvalidDigitsForRadix = new AntlrQueryError("FORG0012",
            "Invalid digits for specified radix.");
    public static final AntlrQueryError InvalidRegexFlags = new AntlrQueryError("FORX0001",
            "Invalid regular expression flags.");
    public static final AntlrQueryError InvalidRegex = new AntlrQueryError("FORX0002", "Invalid regular expression.");
    public static final AntlrQueryError RegexMatchesZeroLength = new AntlrQueryError("FORX0003",
            "Regular expression matches zero-cardinality string.");
    public static final AntlrQueryError InvalidReplacementString = new AntlrQueryError("FORX0004",
            "Invalid replacement string.");
    public static final AntlrQueryError IncompatibleReplaceArgs = new AntlrQueryError("FORX0005",
            "Incompatible arguments for fn:replace.");
    public static final AntlrQueryError DataNodeWithoutTypedValue = new AntlrQueryError("FOTY0012",
            "Node without typed value in fn:data.");
    public static final AntlrQueryError DataFunctionItemFound = new AntlrQueryError("FOTY0013", "Function item in fn:data.");
    public static final AntlrQueryError StringFunctionItemFound = new AntlrQueryError("FOTY0014",
            "Function item in fn:string.");
    public static final AntlrQueryError InvalidUriAuthority = new AntlrQueryError("FOUR0001",
            "Invalid IPv6/IPvFuture authority in URI.");
    public static final AntlrQueryError UnparsedTextUriRefError = new AntlrQueryError("FOUT1170",
            "Invalid URI reference in fn:unparsed-text.");
    public static final AntlrQueryError CannotDecodeExternalResource = new AntlrQueryError("FOUT1190",
            "Cannot decode external resource.");
    public static final AntlrQueryError CannotInferExternalEncoding = new AntlrQueryError("FOUT1200",
            "Cannot infer encoding of external resource.");
    public static final AntlrQueryError NoXsltProcessor = new AntlrQueryError("FOXT0001",
            "No suitable XSLT processor available.");
    public static final AntlrQueryError InvalidXsltTransformParams = new AntlrQueryError("FOXT0002",
            "Invalid parameters to fn:transform.");
    public static final AntlrQueryError XsltTransformationFailed = new AntlrQueryError("FOXT0003",
            "XSLT transformation failed.");
    public static final AntlrQueryError XsltTransformationDisabled = new AntlrQueryError("FOXT0004",
            "XSLT transformation has been disabled.");
    public static final AntlrQueryError XsltInvalidOutputCharacters = new AntlrQueryError("FOXT0006",
            "XSLT output contains non-accepted characters.");
    public static final AntlrQueryError UnknownFunctionName = new AntlrQueryError("AXQY0001", "Called function that is not registered");
    public static final AntlrQueryError TooManyArguments = new AntlrQueryError("AXQY0002", "Tried to register a function with too many arguments");

    private final String code;
    private final String description;

    AntlrQueryError(final String code, final String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }


}
