package com.github.akruk.antlrxquery.errors;

/**
 * Represents a comprehensive list of XQuery errors, including static, type, and dynamic errors.
 * The error codes and messages are based on the W3C XQuery specifications.
 */
public enum XQueryError {

    //================================================================================
    // XPST: Static Errors (General)
    //================================================================================
    StaticContextComponentAbsent("err:XPST0001", "Component of static context is absent"),
    GrammarViolation("err:XPST0003", "Expression violates grammar rules"),
    UndefinedName("err:XPST0008", "Undefined element, attribute, type, or variable name"),
    FunctionCallMismatch("err:XPST0017", "Function call does not match a known function signature"),
    DuplicateFieldName("err:XPST0021", "Duplicate field name in a record declaration"),
    InvalidRecursiveRecord("err:XPST0023", "A recursive record type cannot be instantiated"),
    UndefinedQNameInType("err:XPST0051", "ItemType references a QName that is not defined in the static context"),
    InvalidCastTarget("err:XPST0080", "Target type of a cast or castable expression cannot be xs:NOTATION, xs:anySimpleType, or xs:anyAtomicType"),
    UnresolvablePrefix("err:XPST0081", "A QName contains a namespace prefix that cannot be resolved"),
    InvalidMethodAnnotation("err:XPST0107", "The %method annotation is used on a non-focus function or a function in the query prolog"),
    NonAtomicKeyType("err:XPST0152", "A key type named in a record type is not a generalized atomic type"),

    //================================================================================
    // XPTY/XQTY: Type Errors (Often Statically Detectable)
    //================================================================================
    InappropriateStaticType("err:XPTY0004", "Expression has a static type that is not appropriate for its context"),
    ImplausibleExpressionType("err:XPTY0006", "The inferred static type of an expression is substantively disjoint from the required type"),
    MixedPathResult("err:XPTY0018", "The result of a path operator contains both nodes and non-nodes"),
    PathStepNotNodeSequence("err:XPTY0019", "A step in a path expression does not evaluate to a sequence of nodes"),
    AxisStepContextNotNode("err:XPTY0020", "In an axis step, the context item is not a node"),
    AttributeAfterNonAttribute("err:XQTY0024", "An attribute node follows a non-attribute node in an element constructor's content sequence"),
    InvalidValidateTarget("err:XQTY0030", "The argument of a validate expression does not evaluate to exactly one document or element node"),
    NamespaceSensitiveCopyError("err:XQTY0086", "A namespace-sensitive value is copied with construction mode 'preserve' and copy-namespaces mode 'no-preserve'"),
    FunctionInElementContent("err:XQTY0105", "The content sequence in an element constructor contains a function"),
    UntypedAtomicNamespaceSensitiveCoercion("err:XPTY0117", "An item of type xs:untypedAtomic is coerced to a namespace-sensitive type"),
    ForClauseTypeMismatch("err:XPTY0141", "In a for clause, the binding collection type does not match the keywords (member, key, value)"),
    ImplausibleAxisStep("err:XPTY0144", "An axis step will always return an empty sequence based on static type analysis"),
    ImplausibleLookup("err:XPTY0145", "A lookup expression will always return an empty sequence based on static type analysis"),
    FinallyClauseNotEmpty("err:XQTY0153", "The expression of a finally clause must return an empty sequence"),

    //================================================================================
    // XQST: Static Errors (Query-specific)
    //================================================================================
    SchemaImportUnsupported("err:XQST0009", "Schema import is not supported by an implementation that does not support the Schema Aware Feature"),
    InvalidImportedSchemas("err:XQST0012", "The set of definitions in imported schemas is not valid"),
    InvalidPragmaContent("err:XQST0013", "The content of a recognized pragma is invalid"),
    NamespaceDeclWithExpr("err:XQST0022", "A namespace declaration attribute contains an enclosed expression"),
    UnsupportedVersion("err:XQST0031", "The version number specified in a version declaration is not supported"),
    MultipleBaseURIs("err:XQST0032", "A Prolog contains more than one base URI declaration"),
    DuplicateNamespacePrefix("err:XQST0033", "A module contains multiple bindings for the same namespace prefix"),
    FunctionNameArityConflict("err:XQST0034", "Multiple declared or imported functions have the same expanded QName and overlapping arity ranges"),
    DuplicateSchemaComponent("err:XQST0035", "Two imported schema components define the same name in the same symbol space and scope"),
    InvalidDefaultCollation("err:XQST0038", "A Prolog contains more than one default collation declaration or an unknown collation"),
    DuplicateFunctionParam("err:XQST0039", "A function declaration has more than one parameter with the same name"),
    DuplicateAttributeName("err:XQST0040", "Attributes specified by a direct element constructor do not have distinct expanded QNames"),
    ReservedNamespaceName("err:XQST0045", "The name of a variable, function, or annotation is in a reserved namespace"),
    InvalidURILiteral("err:XQST0046", "A URI literal is not a valid absolute or relative URI"),
    DuplicateModuleImport("err:XQST0047", "Multiple module imports specify the same target namespace"),
    ComponentNotInTargetNamespace("err:XQST0048", "A function, variable, or item type in a library module is not in the module's target namespace"),
    DuplicateVariableName("err:XQST0049", "Two or more declared or imported variables have equal expanded QNames"),
    UndefinedOrNonSimpleCastType("err:XQST0052", "Type in a cast or castable expression must be a simple type defined in the in-scope schema types"),
    MultipleCopyNamespaces("err:XQST0055", "A Prolog contains more than one copy-namespaces declaration"),
    InvalidSchemaImport("err:XQST0057", "A schema import binds a namespace prefix but does not specify a non-empty target namespace"),
    DuplicateSchemaImport("err:XQST0058", "Multiple schema imports specify the same target namespace"),
    UnresolvableImport("err:XQST0059", "An implementation is unable to process a schema or module import"),
    FunctionInNoNamespace("err:XQST0060", "The name of a function in a function declaration is in no namespace"),
    MultipleOrderingModes("err:XQST0065", "A Prolog contains more than one ordering mode declaration"),
    MultipleNamespaceDecls("err:XQST0066", "A Prolog contains more than one default element/type or function namespace declaration"),
    MultipleConstructionDecls("err:XQST0067", "A Prolog contains more than one construction declaration"),
    MultipleBoundarySpaces("err:XQST0068", "A Prolog contains more than one boundary-space declaration"),
    MultipleEmptyOrders("err:XQST0069", "A Prolog contains more than one empty order declaration"),
    InvalidXMLNamespace("err:XQST0070", "A binding for the 'xml' or 'xmlns' prefix is invalid"),
    DuplicateNamespaceDecl("err:XQST0071", "The namespace declaration attributes of a direct element constructor do not have distinct names"),
    ValidateUnsupported("err:XQST0075", "A validate expression is used in an implementation that does not support the Schema Aware Feature"),
    UnknownCollation("err:XQST0076", "A collation used in a FLWOR expression is not present in statically known collations"),
    InvalidExtensionExpr("err:XQST0079", "An extension expression contains neither a recognized pragma nor an enclosed expression"),
    ZeroLengthNamespaceURI("err:XQST0085", "The namespace URI in a namespace declaration is a zero-length string, and the implementation does not support XML Names 1.1"),
    InvalidEncoding("err:XQST0087", "The encoding specified in a Version Declaration is not valid"),
    ZeroLengthTargetNamespace("err:XQST0088", "The target namespace in a module import or declaration is a zero-length string"),
    DuplicateFLWORVariable("err:XQST0089", "A variable and its positional variable in a for or window clause do not have distinct names"),
    InvalidCharacterRef("err:XQST0090", "A character reference does not identify a valid character in the XML version in use"),
    UnknownGroupingVariable("err:XQST0094", "A grouping variable in a group by clause does not match an in-scope variable"),
    InvalidDecimalFormat("err:XQST0097", "A decimal-format declaration specifies an invalid property value"),
    DuplicateDecimalFormatChar("err:XQST0098", "Properties representing characters in a decimal-format picture string do not have distinct values"),
    MultipleContextItems("err:XQST0099", "A module contains more than one context item declaration"),
    DuplicateWindowVariable("err:XQST0103", "All variables in a window clause must have distinct names"),
    UndefinedValidateType("err:XQST0104", "A TypeName specified in a validate expression is not found in the in-scope schema definitions"),
    ConflictingAnnotations("err:XQST0106", "A function declaration contains both %private and %public annotations"),
    OutputInLibraryModule("err:XQST0108", "An output declaration occurs in a library module"),
    InvalidSerializationParam("err:XQST0109", "The local name of an output declaration is not a valid serialization parameter"),
    DuplicateSerializationParam("err:XQST0110", "The same serialization parameter is used more than once in an output declaration"),
    DuplicateDecimalFormat("err:XQST0111", "A query prolog contains two decimal formats with the same name or two default formats"),
    ContextItemValueInLibrary("err:XQST0113", "A context item declaration in a library module specifies a value or default value"),
    DuplicateDecimalFormatProp("err:XQST0114", "A decimal format declaration defines the same property more than once"),
    InvalidParameterDocument("err:XQST0115", "The document specified by an output:parameter-document option raises a serialization error"),
    ConflictingVariableAnnotations("err:XQST0116", "A variable declaration has conflicting or duplicate %private/%public annotations"),
    MismatchedTags("err:XQST0118", "In a direct element constructor, the name in the end tag does not match the start tag"),
    InvalidOutputParamDoc("err:XQST0119", "The implementation cannot process the value of an output:parameter-document declaration"),
    InvalidInlineFunctionAnnotation("err:XQST0125", "An inline function expression is annotated as %public or %private"),
    NamespaceAxisUnsupported("err:XQST0134", "The namespace axis is not supported"),
    InvalidRecursiveItemType("err:XQST0140", "A named item type declaration is illegally recursive"),
    DuplicateItemTypeName("err:XQST0146", "Two or more declared or imported item types have equal expanded QNames"),
    InvalidParameterOrder("err:XQST0148", "An optional parameter in a function declaration is followed by a non-optional parameter"),
    InconsistentImportedSchemas("err:XQST0149", "Schemas imported by different modules of a query are not consistent"),
    InvalidComputedConstructorName("err:XQST0151", "A node name supplied as a string literal in a computed constructor is not a valid EQName"),

    //================================================================================
    // FO: Functions and Operators Errors (Dynamic)
    //================================================================================
    WrongArgumentCountApply("err:FOAP0001", "Wrong number of arguments for fn:apply"),
    DivisionByZero("err:FOAR0001", "Division by zero"),
    NumericOperationOverflow("err:FOAR0002", "Numeric operation overflow/underflow"),
    ArrayIndexOutOfBounds("err:FOAY0001", "Array index out of bounds"),
    NegativeArrayLength("err:FOAY0002", "Negative array length"),
    InputValueTooLargeForDecimal("err:FOCA0001", "Input value too large for decimal"),
    InvalidLexicalValue("err:FOCA0002", "Invalid lexical value"),
    InputValueTooLargeForInteger("err:FOCA0003", "Input value too large for integer"),
    NaNAsFloatDouble("err:FOCA0005", "NaN supplied as float/double value"),
    DecimalCastTooManyDigits("err:FOCA0006", "String to be cast to decimal has too many digits of precision"),
    InvalidCodepoint("err:FOCH0001", "Codepoint not valid"),
    UnsupportedCollation("err:FOCH0002", "Unsupported collation"),
    UnsupportedNormalizationForm("err:FOCH0003", "Unsupported normalization form"),
    CollationNoUnitSupport("err:FOCH0004", "Collation does not support collation units"),
    UnrecognizedCharacterName("err:FOCH0005", "Unrecognized or invalid character name"),
    CsvFieldQuotingError("err:FOCV0001", "CSV field quoting error"),
    InvalidCsvDelimiter("err:FOCV0002", "Invalid CSV delimiter error"),
    DuplicateCsvDelimiter("err:FOCV0003", "Duplicate CSV delimiter error"),
    UnknownCsvColumnName("err:FOCV0004", "Argument supplied is not a known column name"),
    NoContextDocument("err:FODC0001", "No context document"),
    ErrorRetrievingResource("err:FODC0002", "Error retrieving resource"),
    FunctionNotDeterministic("err:FODC0003", "Function not defined as deterministic"),
    InvalidCollectionUri("err:FODC0004", "Invalid collection URI"),
    InvalidUriReference("err:FODC0005", "Invalid URI reference"),
    ParseXmlNotWellFormed("err:FODC0006", "String passed to fn:parse-xml is not a well-formed XML document"),
    ParseXmlNotDtdValid("err:FODC0007", "String passed to fn:parse-xml is not a DTD-valid XML document"),
    ParseXmlInvalidXsdValidationOption("err:FODC0008", "Invalid value for the xsd-validation option of fn:parse-xml"),
    ProcessorNotSchemaAware("err:FODC0009", "Processor is not schema-aware"),
    SerializationNotSupported("err:FODC0010", "The processor does not support serialization"),
    ParseHtmlNotWellFormed("err:FODC0011", "String passed to fn:parse-html is not a well-formed HTML document"),
    UnsupportedHtmlParserOption("err:FODC0012", "Unsupported HTML parser option"),
    NoValidatingXmlParser("err:FODC0013", "No validating XML parser available"),
    ParseXmlNotSchemaValid("err:FODC0014", "String passed to fn:parse-xml is not a schema-valid XML document"),
    CannotCompileSchemaForValidator("err:FODC0015", "Unable to compile schema for fn:xsd-validator"),
    InvalidDecimalFormatName("err:FODF1280", "Invalid decimal format name"),
    InvalidDecimalFormatProperty("err:FODF1290", "Invalid decimal format property"),
    InvalidDecimalFormatPicture("err:FODF1310", "Invalid decimal format picture string"),
    DateTimeOverflow("err:FODT0001", "Overflow/underflow in date/time operation"),
    DurationOverflow("err:FODT0002", "Overflow/underflow in duration operation"),
    InvalidTimezone("err:FODT0003", "Invalid timezone value"),
    NoTimezoneData("err:FODT0004", "No timezone data available"),
    UnidentifiedError("err:FOER0000", "Unidentified error"),
    InvalidDateTimeFormatParams("err:FOFD1340", "Invalid date/time formatting parameters"),
    InvalidDateTimeFormatComponent("err:FOFD1350", "Invalid date/time formatting component"),
    InvalidHashAlgorithm("err:FOHA0001", "Invalid algorithm"),
    JsonSyntaxError("err:FOJS0001", "JSON syntax error"),
    JsonDuplicateKeys("err:FOJS0003", "JSON duplicate keys"),
    JsonNotSchemaAware("err:FOJS0004", "JSON: not schema-aware"),
    JsonInvalidOptions("err:FOJS0005", "Invalid options"),
    InvalidXmlForJson("err:FOJS0006", "Invalid XML representation of JSON"),
    BadJsonEscape("err:FOJS0007", "Bad JSON escape sequence"),
    CannotConvertElementToMap("err:FOJS0008", "Cannot convert element to map"),
    NoNamespaceForPrefix("err:FONS0004", "No namespace found for prefix"),
    BaseUriNotDefined("err:FONS0005", "Base-uri not defined in the static context"),
    OriginNotAncestor("err:FOPA0001", "Origin node is not an ancestor of the target node"),
    ModuleUriIsZeroLength("err:FOQM0001", "Module URI is a zero-length string"),
    ModuleUriNotFound("err:FOQM0002", "Module URI not found"),
    StaticErrorInDynamicModule("err:FOQM0003", "Static error in dynamically loaded XQuery module"),
    DynamicModuleParamIncorrectType("err:FOQM0005", "Parameter for dynamically loaded XQuery module has incorrect type"),
    NoSuitableXQueryProcessor("err:FOQM0006", "No suitable XQuery processor available"),
    InvalidValueForCast("err:FORG0001", "Invalid value for cast/constructor"),
    InvalidArgumentResolveUri("err:FORG0002", "Invalid argument to fn:resolve-uri"),
    ZeroOrOneCalledWithMany("err:FORG0003", "fn:zero-or-one called with a sequence containing more than one item"),
    OneOrMoreCalledWithEmpty("err:FORG0004", "fn:one-or-more called with a sequence containing no items"),
    ExactlyOneCalledWithWrongCardinality("err:FORG0005", "fn:exactly-one called with a sequence containing zero or more than one item"),
    InvalidArgumentType("err:FORG0006", "Invalid argument type"),
    InconsistentTimezones("err:FORG0008", "The two arguments to fn:dateTime have inconsistent timezones"),
    ErrorResolvingRelativeUri("err:FORG0009", "Error in resolving a relative URI against a base URI in fn:resolve-uri"),
    InvalidIetfDate("err:FORG0010", "Invalid date/time"),
    InvalidRadix("err:FORG0011", "Invalid radix"),
    InvalidDigitsForRadix("err:FORG0012", "Invalid digits"),
    InvalidRegexFlags("err:FORX0001", "Invalid regular expression flags"),
    InvalidRegex("err:FORX0002", "Invalid regular expression"),
    RegexMatchesZeroLength("err:FORX0003", "Regular expression matches zero-length string"),
    InvalidReplacementString("err:FORX0004", "Invalid replacement string"),
    IncompatibleReplaceArgs("err:FORX0005", "Incompatible arguments for fn:replace"),
    AtomizeNodeNoTypedValue("err:FOTY0012", "Argument to fn:data contains a node that does not have a typed value"),
    AtomizeFunctionItem("err:FOTY0013", "The argument to fn:data contains a function item"),
    StringFunctionItem("err:FOTY0014", "The argument to fn:string is a function item"),
    InvalidIPv6Authority("err:FOUR0001", "Invalid IPv6/IPvFuture authority"),
    UnparsedTextInvalidUri("err:FOUT1170", "Invalid URI reference for unparsed-text"),
    CannotDecodeResource("err:FOUT1190", "Cannot decode external resource"),
    CannotInferEncoding("err:FOUT1200", "Cannot infer encoding of external resource"),
    NoSuitableXsltProcessor("err:FOXT0001", "No suitable XSLT processor available"),
    InvalidXsltParams("err:FOXT0002", "Invalid parameters to XSLT transformation"),
    XsltTransformationFailed("err:FOXT0003", "XSLT transformation failed"),
    XsltTransformationDisabled("err:FOXT0004", "XSLT transformation has been disabled"),
    XsltOutputNonAcceptedChars("err:FOXT0006", "XSLT output contains non-accepted characters");

    private final String errorCode;
    private final String defaultMessage;

    XQueryError(String errorCode, String defaultMessage) {
        this.errorCode = errorCode;
        this.defaultMessage = defaultMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    @Override
    public String toString() {
        return errorCode + ": " + defaultMessage;
    }
}
