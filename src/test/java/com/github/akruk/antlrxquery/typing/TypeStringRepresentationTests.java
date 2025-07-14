package com.github.akruk.antlrxquery.typing;

import org.junit.Test;

import com.github.akruk.antlrxquery.typesystem.XQueryRecordField;
import com.github.akruk.antlrxquery.typesystem.XQuerySequenceType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryEnumTypeFactory;
import com.github.akruk.antlrxquery.typesystem.factories.defaults.XQueryNamedTypeSets;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TypeStringRepresentationTests {
    final XQueryTypeFactory typeFactory = new XQueryEnumTypeFactory(new XQueryNamedTypeSets().all());
    @Test
    public void testItemNumber() {
        assertEquals("number", typeFactory.one(typeFactory.itemNumber()).toString());
    }

    @Test
    public void testItemBooleanOptional() {
        assertEquals("boolean?", typeFactory.zeroOrOne(typeFactory.itemBoolean()).toString());
    }

    @Test
    public void testItemStringRepeated() {
        assertEquals("string*", typeFactory.zeroOrMore(typeFactory.itemString()).toString());
    }

    @Test
    public void testItemStringRequiredRepeated() {
        assertEquals("string+", typeFactory.oneOrMore(typeFactory.itemString()).toString());
    }

    @Test
    public void testEmptySequence() {
        assertEquals("empty-sequence()", typeFactory.emptySequence().toString());
    }

    @Test
    public void testFunctionTypeSimple() {
        String repr = typeFactory.function(
            typeFactory.one(typeFactory.itemNumber()),
            List.of(
                typeFactory.one(typeFactory.itemNumber()),
                typeFactory.one(typeFactory.itemNumber())
            )
        ).toString();
        assertTrue(
            "fn(number, number) as number".equals(repr)
            || "function(number, number) as number".equals(repr)
        );
    }

    @Test
    public void testMapType() {
        assertEquals(
            "map(string, boolean*)",
            typeFactory.map(
                typeFactory.itemString(),
                typeFactory.zeroOrMore(typeFactory.itemBoolean())
            ).toString()
        );
    }

    @Test
    public void testArrayType() {
        assertEquals(
            "array(number+)",
            typeFactory.array(typeFactory.oneOrMore(typeFactory.itemNumber())).toString()
        );
    }

    @Test
    public void testEnumType() {
        String enumRepr = typeFactory.enum_(Set.of("on", "off")).toString();
        assertTrue( "enum('on', 'off')".equals(enumRepr)
                    || "enum('off', 'on')".equals(enumRepr));
    }

    @Test
    public void testRecordType() {
        Map<String, XQueryRecordField> fields = Map.of(
            "id", new XQueryRecordField(typeFactory.one(typeFactory.itemNumber()), true),
            "name", new XQueryRecordField(typeFactory.zeroOrOne(typeFactory.itemString()), false)
        );
        String repr = typeFactory.record(fields).toString();
        assertTrue("record(id as number, name? as string?)".equals(repr)
                || "record(name? as string?, id as number)".equals(repr));
    }

    @Test
    public void testExtensibleRecordType() {
        Map<String, XQueryRecordField> fields = Map.of(
            "name", new XQueryRecordField(
                typeFactory.zeroOrOne(typeFactory.itemString()), true
            )
        );
        assertEquals(
            "record(name as string?, *)",
            typeFactory.extensibleRecord(fields).toString()
        );
    }

    @Test
    public void testSingleElementType() {
        assertEquals(
            "element(title)",
            typeFactory.element(Set.of("title")).toString()
        );
    }

    @Test
    public void testElementTypeWithAlternatives() {
        String repr = typeFactory.element(Set.of("name", "label")).toString();
        assertTrue(
            "element(name | label)".equals(repr)
            || "element(label | name)".equals(repr)
        );
    }



    @Test
    public void testSimpleChoiceItem() {
        String repr = typeFactory.choice(Set.of(
            typeFactory.itemNumber(),
            typeFactory.itemString()
        )).toString();
        assertTrue(
            "number | string".equals(repr)
            || "string | number".equals(repr)
        );
    }

    @Test
    public void testChoiceWithSuffixRequiresParens() {
        String variant1 = "(number | string)?";
        String variant2 = "(string | number)?";
        String result = typeFactory.zeroOrOne(
            typeFactory.itemChoice(Set.of(
                typeFactory.itemNumber(),
                typeFactory.itemString()
            ))
        ).toString();
        assertTrue(variant1.equals(result) || variant2.equals(result));

        variant1 = "(number | string)*";
        variant2 = "(string | number)*";
        result = typeFactory.zeroOrMore(
            typeFactory.itemChoice(Set.of(
                typeFactory.itemNumber(),
                typeFactory.itemString()
            ))
        ).toString();
        assertTrue(variant1.equals(result) || variant2.equals(result));

        variant1 = "(number | string)+";
        variant2 = "(string | number)+";
        result = typeFactory.oneOrMore(
            typeFactory.itemChoice(Set.of(
                typeFactory.itemNumber(),
                typeFactory.itemString()
            ))
        ).toString();
        assertTrue(variant1.equals(result) || variant2.equals(result));
    }

    @Test
    public void testFunctionWithTypedArgumentsAndSuffixRequiresParens() {
        XQuerySequenceType resultType = typeFactory.one(typeFactory.itemNumber());
        List<XQuerySequenceType> argTypes = List.of(
            typeFactory.one(typeFactory.itemNumber()),
            typeFactory.one(typeFactory.itemString())
        );

        String fnRepr = "fn(number, string) as number";
        String functionRepr = "function(number, string) as number";
        String fnOptional = "(fn(number, string) as number)?";
        String functionOptional = "(function(number, string) as number)?";
        String fnStar = "(fn(number, string) as number)*";
        String functionStar = "(function(number, string) as number)*";

        String result = typeFactory.function(resultType, argTypes).toString();
        assertTrue(fnRepr.equals(result) || functionRepr.equals(result));

        result = typeFactory.zeroOrOne(
            typeFactory.itemFunction(resultType, argTypes)
        ).toString();
        assertTrue(fnOptional.equals(result) || functionOptional.equals(result));

        result = typeFactory.zeroOrMore(
            typeFactory.itemFunction(resultType, argTypes)
        ).toString();
        assertTrue(fnStar.equals(result) || functionStar.equals(result));
    }
}

