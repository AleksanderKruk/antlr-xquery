// package com.github.akruk.antlrxquery;
// import static org.junit.jupiter.api.Assertions.*;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;

// import com.github.akruk.antlrxquery.typesystem.XQueryItemType;
// import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
// import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryEnumTypeFactory;

// import java.util.Map;

// public class RecordSubtypingTest {
//     private XQueryEnumTypeFactory typeFactory;
//     private XQueryItemType anyItem;
//     private XQueryItemType anyMap;

//     @BeforeEach
//     void init() {
//         typeFactory = new XQueryEnumTypeFactory();
//         anyItem     = typeFactory.itemAnyItem();
//         // map(*) is modeled as an extensible record with no fixed fields
//         anyMap      = typeFactory.extensibleRecord(Map.of());
//     }

//     // 3.3.2.8 Subtyping Records
//     // 1) A is map(*) and B is record(*): map(*) ⊆ record(*)  ⇒ true
//     @Test void anyMapIsSubtypeOfItself() {
//         assertTrue(anyMap.isSubtypeOf(anyMap));
//     }

//     // 2) A is record(...)  and B is map(*) or record(*)
//     @Test void fixedRecordIsSubtypeOfMapStar() {
//         var recFixed = typeFactory.record(
//             Map.of("longitude", anyItem, "latitude", anyItem));
//         assertTrue(recFixed.isSubtypeOf(anyMap),
//                    "Non‐extensible record ⊆ map(*) should hold");
//     }

//     @Test void extensibleRecordIsSubtypeOfMapStar() {
//         var recExt = typeFactory.extensibleRecord(
//             Map.of("a", typeFactory.number()));
//         assertTrue(recExt.isSubtypeOf(anyMap),
//                    "Extensible record ⊆ map(*) should hold");
//     }

//     // 3) A is extensible record type, B is extensible record type
//     //    mandatory fields in B ⊆ mandatory in A; shared fields A⊑B;
//     //    extra fields in B must be item()*.
//     @Test void dropExtraSharedFieldInExtensible() {
//         var A = typeFactory.extensibleRecord(
//             Map.of("x", anyItem, "y", anyItem, "z", anyItem));
//         var B = typeFactory.extensibleRecord(
//             Map.of("x", anyItem, "y", anyItem));
//         assertTrue(A.isSubtypeOf(B),
//                    "extensible {x,y,z,*} ⊆ extensible {x,y,*}");
//     }

//     @Test void extensibleFieldTypeWidening() {
//         var A = typeFactory.extensibleRecord(
//             Map.of("x", typeFactory.one(typeFactory.itemInteger()),
//                    "y", typeFactory.one(typeFactory.itemInteger())));
//         var B = typeFactory.extensibleRecord(
//             Map.of("x", typeFactory.one(typeFactory.itemDecimal()),
//                    "y", typeFactory.zeroOrMore(typeFactory.itemInteger())));
//         assertTrue(A.isSubtypeOf(B),
//                    "integer fields ⊑ decimal and multiplicity match");
//     }

//     @Test void optionalMissingFailsMandatoryInExtensible() {
//         var A = typeFactory.extensibleRecord(
//             Map.of("x", typeFactory.zeroOrOne(anyItem),
//                    "y", typeFactory.zeroOrOne(anyItem),
//                    "z", typeFactory.zeroOrOne(anyItem)));
//         var B = typeFactory.extensibleRecord(
//             Map.of("x", anyItem, "y", anyItem));
//         assertFalse(A.isSubtypeOf(B),
//                     "optional missing fields cannot satisfy mandatory in B");
//     }

//     // 4) A is non‐extensible record, B is non‐extensible record
//     //    fields(A) ⊆ fields(B); mandatory(B) ⊆ mandatory(A); A⊑B on shared.
//     @Test void nonExtensibleFieldSubtypeAndMandatory() {
//         var A = typeFactory.record(
//             Map.of("x", anyItem, "y", typeFactory.one(typeFactory.itemInteger())));
//         var B = typeFactory.record(
//             Map.of("x", anyItem, "y", typeFactory.one(typeFactory.itemDecimal())));
//         assertTrue(A.isSubtypeOf(B),
//                    "xs:integer ⊑ xs:decimal and same mandatory fields");
//     }

//     @Test void addOptionalFieldInNonExtensible() {
//         var A = typeFactory.record(
//             Map.of("x", anyItem, "y", anyItem));
//         var B = typeFactory.record(
//             Map.of("x", anyItem, "y", anyItem,
//                    "z", typeFactory.zeroOrOne(anyItem)));
//         assertTrue(A.isSubtypeOf(B),
//                    "non‐extensible {x,y} ⊆ {x,y,z?}");
//     }

//     // 5) A non‐extensible record ⊆ B extensible record
//     //    mandatory(B) ⊆ mandatory(A); shared A⊑B
//     @Test void nonExtensibleToExtensible() {
//         var A = typeFactory.record(
//             Map.of("x", anyItem, "y", typeFactory.one(typeFactory.itemInteger())));
//         var B = typeFactory.extensibleRecord(
//             Map.of("x", anyItem, "y", typeFactory.one(typeFactory.itemDecimal())));
//         assertTrue(A.isSubtypeOf(B),
//                    "non‐extensible ⊆ extensible with widened types");
//     }

//     @Test void missingOptionalFieldAllowedInExtensible() {
//         var A = typeFactory.record(
//             Map.of("y", typeFactory.one(typeFactory.itemInteger())));
//         var B = typeFactory.extensibleRecord(
//             Map.of("x", typeFactory.zeroOrOne(anyItem),
//                    "y", typeFactory.one(typeFactory.itemDecimal())));
//         assertTrue(A.isSubtypeOf(B),
//                    "non‐extensible can omit optional fields from B");
//     }

//     // 6) Records do not subtype if mandatory field missing or narrowing fails
//     @Test void nonExtensibleMissingMandatoryFails() {

//         var A = typeFactory.record(Map.of("x", anyItem));
//         var B = typeFactory.record(Map.of("x", anyItem, "y", anyItem));
//         assertFalse(A.isSubtypeOf(B),
//                     "missing mandatory 'y' should fail");
//     }

//     @Test void fieldTypeNarrowingFails() {
//         var A = typeFactory.record(
//             Map.of("x", anyItem, "y", typeFactory.one(typeFactory.itemDecimal())));
//         var B = typeFactory.record(
//             Map.of("x", anyItem, "y", typeFactory.one(typeFactory.itemInteger())));
//         assertFalse(A.isSubtypeOf(B),
//                     "decimal ⊑ integer does not hold");
//     }
// }

