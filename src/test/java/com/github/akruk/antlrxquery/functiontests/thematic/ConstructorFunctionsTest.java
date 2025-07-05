package com.github.akruk.antlrxquery.functiontests.thematic;

import org.junit.jupiter.api.Test;

import com.github.akruk.antlrxquery.functiontests.FunctionsSemanticTest;

public class ConstructorFunctionsTest extends FunctionsSemanticTest {

    // eg:TYPE($value as xs:anyAtomicType? := .) as eg:TYPE?
    // @Test public void egType_defaultContextItem() {
    //     assertType(
    //         "eg:TYPE()",
    //         typeFactory.zeroOrOne(typeFactory.item("eg:TYPE"))
    //     );
    // }
    // @Test public void egType_withAtomic() {
    //     assertType(
    //         "eg:TYPE('x')",
    //         typeFactory.zeroOrOne(typeFactory.item("eg:TYPE"))
    //     );
    // }
    // @Test public void egType_wrongType() {
    //     assertErrors("eg:TYPE(<a/>)");
    // }

    // // xs:unsignedInt($arg as xs:anyAtomicType? := .) as xs:unsignedInt?
    // @Test public void unsignedInt_default() {
    //     assertType(
    //         "xs:unsignedInt()",
    //         typeFactory.zeroOrOne(typeFactory.itemUnsignedInt())
    //     );
    // }
    // @Test public void unsignedInt_withNumber() {
    //     assertType(
    //         "xs:unsignedInt(123)",
    //         typeFactory.zeroOrOne(typeFactory.itemUnsignedInt())
    //     );
    // }
    // @Test public void unsignedInt_wrongType() {
    //     assertErrors("xs:unsignedInt(<n/>)");
    // }

    // // xs:string($value as xs:anyAtomicType? := .) as xs:string?
    // @Test public void xsString_default() {
    //     assertType("xs:string()", typeFactory.zeroOrOne(typeFactory.itemString()));
    // }
    // @Test public void xsString_withAtomic() {
    //     assertType("xs:string(123)", typeFactory.zeroOrOne(typeFactory.itemString()));
    // }
    // @Test public void xsString_wrong() {
    //     assertErrors("xs:string(<a/>)");
    // }

    // // xs:boolean($value as xs:anyAtomicType? := .) as xs:boolean?
    // @Test public void xsBoolean_default() {
    //     assertType("xs:boolean()", typeFactory.zeroOrOne(typeFactory.itemBoolean()));
    // }
    // @Test public void xsBoolean_withAtomic() {
    //     assertType("xs:boolean('true')", typeFactory.zeroOrOne(typeFactory.itemBoolean()));
    // }
    // @Test public void xsBoolean_wrong() {
    //     assertErrors("xs:boolean(<a/>)");
    // }

    // // xs:decimal($value as xs:anyAtomicType? := .) as xs:decimal?
    // @Test public void xsDecimal_default() {
    //     assertType("xs:decimal()", typeFactory.zeroOrOne(typeFactory.itemDecimal()));
    // }
    // @Test public void xsDecimal_withAtomic() {
    //     assertType("xs:decimal('1.23')", typeFactory.zeroOrOne(typeFactory.itemDecimal()));
    // }
    // @Test public void xsDecimal_wrong() {
    //     assertErrors("xs:decimal(<a/>)");
    // }

    // // xs:float($value as xs:anyAtomicType? := .) as xs:float?
    // @Test public void xsFloat_default() {
    //     assertType("xs:float()", typeFactory.zeroOrOne(typeFactory.itemFloat()));
    // }
    // @Test public void xsFloat_withAtomic() {
    //     assertType("xs:float('1.2E3')", typeFactory.zeroOrOne(typeFactory.itemFloat()));
    // }
    // @Test public void xsFloat_wrong() {
    //     assertErrors("xs:float(<a/>)");
    // }

    // // xs:double($value as xs:anyAtomicType? := .) as xs:double?
    // @Test public void xsDouble_default() {
    //     assertType("xs:double()", typeFactory.zeroOrOne(typeFactory.itemDouble()));
    // }
    // @Test public void xsDouble_withAtomic() {
    //     assertType("xs:double('2.34E-1')", typeFactory.zeroOrOne(typeFactory.itemDouble()));
    // }
    // @Test public void xsDouble_wrong() {
    //     assertErrors("xs:double(<a/>)");
    // }

    // // xs:duration($value as xs:anyAtomicType? := .) as xs:duration?
    // @Test public void xsDuration_default() {
    //     assertType("xs:duration()", typeFactory.zeroOrOne(typeFactory.itemDuration()));
    // }
    // @Test public void xsDuration_withAtomic() {
    //     assertType("xs:duration('P1Y2M')", typeFactory.zeroOrOne(typeFactory.itemDuration()));
    // }
    // @Test public void xsDuration_wrong() {
    //     assertErrors("xs:duration(<a/>)");
    // }

    // // xs:dateTime($value as xs:anyAtomicType? := .) as xs:dateTime?
    // @Test public void xsDateTime_default() {
    //     assertType("xs:dateTime()", typeFactory.zeroOrOne(typeFactory.itemDateTime()));
    // }
    // @Test public void xsDateTime_withAtomic() {
    //     assertType("xs:dateTime('2020-01-01T12:00:00')", typeFactory.zeroOrOne(typeFactory.itemDateTime()));
    // }
    // @Test public void xsDateTime_wrong() {
    //     assertErrors("xs:dateTime(<a/>)");
    // }

    // // xs:time($value as xs:anyAtomicType? := .) as xs:time?
    // @Test public void xsTime_default() {
    //     assertType("xs:time()", typeFactory.zeroOrOne(typeFactory.itemTime()));
    // }
    // @Test public void xsTime_withAtomic() {
    //     assertType("xs:time('23:59:59')", typeFactory.zeroOrOne(typeFactory.itemTime()));
    // }
    // @Test public void xsTime_wrong() {
    //     assertErrors("xs:time(<a/>)");
    // }

    // // xs:date($value as xs:anyAtomicType? := .) as xs:date?
    // @Test public void xsDate_default() {
    //     assertType("xs:date()", typeFactory.zeroOrOne(typeFactory.itemDate()));
    // }
    // @Test public void xsDate_withAtomic() {
    //     assertType("xs:date('2020-12-31')", typeFactory.zeroOrOne(typeFactory.itemDate()));
    // }
    // @Test public void xsDate_wrong() {
    //     assertErrors("xs:date(<a/>)");
    // }

    // // xs:hexBinary($value as xs:anyAtomicType? := .) as xs:hexBinary?
    // @Test public void xsHexBinary_default() {
    //     assertType("xs:hexBinary()", typeFactory.zeroOrOne(typeFactory.itemHexBinary()));
    // }
    // @Test public void xsHexBinary_withAtomic() {
    //     assertType("xs:hexBinary('0A0B')", typeFactory.zeroOrOne(typeFactory.itemHexBinary()));
    // }
    // @Test public void xsHexBinary_wrong() {
    //     assertErrors("xs:hexBinary(<a/>)");
    // }

    // // xs:base64Binary($value as xs:anyAtomicType? := .) as xs:base64Binary?
    // @Test public void xsBase64Binary_default() {
    //     assertType("xs:base64Binary()", typeFactory.zeroOrOne(typeFactory.itemBase64Binary()));
    // }
    // @Test public void xsBase64Binary_withAtomic() {
    //     assertType("xs:base64Binary('AQID')", typeFactory.zeroOrOne(typeFactory.itemBase64Binary()));
    // }
    // @Test public void xsBase64Binary_wrong() {
    //     assertErrors("xs:base64Binary(<a/>)");
    // }

    // // xs:anyURI($value as xs:anyAtomicType? := .) as xs:anyURI?
    // @Test public void xsAnyURI_default() {
    //     assertType("xs:anyURI()", typeFactory.zeroOrOne(typeFactory.anyURI_()));
    // }
    // @Test public void xsAnyURI_withAtomic() {
    //     assertType("xs:anyURI('http://x')", typeFactory.zeroOrOne(typeFactory.anyURI_()));
    // }
    // @Test public void xsAnyURI_wrong() {
    //     assertErrors("xs:anyURI(<a/>)");
    // }

    // // xs:QName($value as xs:anyAtomicType? := .) as xs:QName?
    // @Test public void xsQName_default() {
    //     assertType("xs:QName()", typeFactory.zeroOrOne(typeFactory.itemQName()));
    // }
    // @Test public void xsQName_withAtomic() {
    //     assertType("xs:QName('xs:string')", typeFactory.zeroOrOne(typeFactory.itemQName()));
    // }
    // @Test public void xsQName_wrong() {
    //     assertErrors("xs:QName(<a/>)");
    // }

    // // xs:normalizedString($value as xs:anyAtomicType? := .) as xs:normalizedString?
    // @Test public void normalizedString_default() {
    //     assertType("xs:normalizedString()", typeFactory.zeroOrOne(typeFactory.itemNormalizedString()));
    // }
    // @Test public void normalizedString_withAtomic() {
    //     assertType("xs:normalizedString('a b')", typeFactory.zeroOrOne(typeFactory.itemNormalizedString()));
    // }
    // @Test public void normalizedString_wrong() {
    //     assertErrors("xs:normalizedString(<a/>)");
    // }

    // // xs:token($value as xs:anyAtomicType? := .) as xs:token?
    // @Test public void xsToken_default() {
    //     assertType("xs:token()", typeFactory.zeroOrOne(typeFactory.itemToken()));
    // }
    // @Test public void xsToken_withAtomic() {
    //     assertType("xs:token('t')", typeFactory.zeroOrOne(typeFactory.itemToken()));
    // }
    // @Test public void xsToken_wrong() {
    //     assertErrors("xs:token(<a/>)");
    // }

    // // xs:language($value as xs:anyAtomicType? := .) as xs:language?
    // @Test public void xsLanguage_default() {
    //     assertType("xs:language()", typeFactory.zeroOrOne(typeFactory.itemLanguage()));
    // }
    // @Test public void xsLanguage_withAtomic() {
    //     assertType("xs:language('en')", typeFactory.zeroOrOne(typeFactory.itemLanguage()));
    // }
    // @Test public void xsLanguage_wrong() {
    //     assertErrors("xs:language(<a/>)");
    // }

    // // xs:NMTOKEN($value as xs:anyAtomicType? := .) as xs:NMTOKEN?
    // @Test public void xsNMTOKEN_default() {
    //     assertType("xs:NMTOKEN()", typeFactory.zeroOrOne(typeFactory.itemNMTOKEN()));
    // }
    // @Test public void xsNMTOKEN_withAtomic() {
    //     assertType("xs:NMTOKEN('tok')", typeFactory.zeroOrOne(typeFactory.itemNMTOKEN()));
    // }
    // @Test public void xsNMTOKEN_wrong() {
    //     assertErrors("xs:NMTOKEN(<a/>)");
    // }

    // // xs:Name($value as xs:anyAtomicType? := .) as xs:Name?
    // @Test public void xsName_default() {
    //     assertType("xs:Name()", typeFactory.zeroOrOne(typeFactory.itemName()));
    // }
    // @Test public void xsName_withAtomic() {
    //     assertType("xs:Name('n')", typeFactory.zeroOrOne(typeFactory.itemName()));
    // }
    // @Test public void xsName_wrong() {
    //     assertErrors("xs:Name(<a/>)");
    // }

    // // xs:NCName($value as xs:anyAtomicType? := .) as xs:NCName?
    // @Test public void xsNCName_default() {
    //     assertType("xs:NCName()", typeFactory.zeroOrOne(typeFactory.itemNCName()));
    // }
    // @Test public void xsNCName_withAtomic() {
    //     assertType("xs:NCName('nc')", typeFactory.zeroOrOne(typeFactory.itemNCName()));
    // }
    // @Test public void xsNCName_wrong() {
    //     assertErrors("xs:NCName(<a/>)");
    // }

    // // xs:ID($value as xs:anyAtomicType? := .) as xs:ID?
    // @Test public void xsID_default() {
    //     assertType("xs:ID()", typeFactory.zeroOrOne(typeFactory.itemID()));
    // }
    // @Test public void xsID_withAtomic() {
    //     assertType("xs:ID('id1')", typeFactory.zeroOrOne(typeFactory.itemID()));
    // }
    // @Test public void xsID_wrong() {
    //     assertErrors("xs:ID(<a/>)");
    // }

    // // xs:integer($value as xs:anyAtomicType? := .) as xs:integer?
    // @Test public void xsInteger_default() {
    //     assertType("xs:integer()", typeFactory.zeroOrOne(typeFactory.itemInteger()));
    // }
    // @Test public void xsInteger_withAtomic() {
    //     assertType("xs:integer('42')", typeFactory.zeroOrOne(typeFactory.itemInteger()));
    // }
    // @Test public void xsInteger_wrong() {
    //     assertErrors("xs:integer(<a/>)");
    // }

    // // xs:nonNegativeInteger($value as xs:anyAtomicType? := .) as xs:nonNegativeInteger?
    // @Test public void xsNonNegInteger_default() {
    //     assertType("xs:nonNegativeInteger()", typeFactory.zeroOrOne(typeFactory.itemNonNegativeInteger()));
    // }
    // @Test public void xsNonNegInteger_withAtomic() {
    //     assertType("xs:nonNegativeInteger('0')", typeFactory.zeroOrOne(typeFactory.itemNonNegativeInteger()));
    // }
    // @Test public void xsNonNegInteger_wrong() {
    //     assertErrors("xs:nonNegativeInteger(<a/>)");
    // }

    // // xs:positiveInteger($value as xs:anyAtomicType? := .) as xs:positiveInteger?
    // @Test public void xsPositiveInteger_default() {
    //     assertType("xs:positiveInteger()", typeFactory.zeroOrOne(typeFactory.itemPositiveInteger()));
    // }
    // @Test public void xsPositiveInteger_withAtomic() {
    //     assertType("xs:positiveInteger('1')", typeFactory.zeroOrOne(typeFactory.itemPositiveInteger()));
    // }
    // @Test public void xsPositiveInteger_wrong() {
    //     assertErrors("xs:positiveInteger(<a/>)");
    // }

    // // eg:hatSize($value as xs:anyAtomicType) as my:hatSize?
    // @Test public void egHatSize_withAtomic() {
    //     assertType("eg:hatSize(57)", typeFactory.zeroOrOne(typeFactory.item("my:hatSize")));
    // }
    // @Test public void egHatSize_wrong() {
    //     assertErrors("eg:hatSize(<a/>)");
    // }
}
