package com.github.akruk.antlrquery.typing;

import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver;
import com.github.akruk.antlrquery.typesystem.types.Cardinality;
import org.junit.Test;

import com.github.akruk.antlrquery.typesystem.RecordField;
import com.github.akruk.antlrquery.typesystem.RecordField.TypeOrReference;
import com.github.akruk.antlrquery.typesystem.factories.AntlrQueryTypeFactory;
import com.github.akruk.antlrquery.typesystem.factories.defaults.MemoizedTypeFactory;
import com.github.akruk.antlrquery.typesystem.factories.defaults.AntlrQueryNamedTypeSets;
import com.github.akruk.antlrquery.typesystem.types.AntlrQuerySequenceType;
import com.github.akruk.antlrquery.namespaceresolver.NamespaceResolver.QualifiedName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TypeStringRepresentationTests {
    final AntlrQueryTypeFactory typeFactory = new MemoizedTypeFactory(new AntlrQueryNamedTypeSets().all(), Map.of());
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
            typeFactory.array(typeFactory.oneOrMore(typeFactory.itemNumber()), Cardinality.ZERO_OR_MORE).toString()
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
        LinkedHashMap<String, RecordField> fields = new LinkedHashMap<>(Map.of(
            "id", new RecordField("id", new TypeOrReference.Type(typeFactory.one(typeFactory.itemNumber())), true),
            "name", new RecordField("name", new TypeOrReference.Type(typeFactory.zeroOrOne(typeFactory.itemString())), false)
        ));
        String repr = typeFactory.record(fields).toString();
        assertTrue("record(id as number, name? as string?)".equals(repr)
                || "record(name? as string?, id as number)".equals(repr));
    }


    @Test
    public void testEmptyRecord() {
        LinkedHashMap<String, RecordField> fields = new LinkedHashMap<>(Map.of());
        String repr = typeFactory.record(fields).toString();
        assertEquals("record()", repr);
    }
    @Test
    public void testExtensibleRecordType() {
        LinkedHashMap<String, RecordField> fields = new LinkedHashMap<>(Map.of(
            "name", new RecordField(
                "name", 
                new TypeOrReference.Type(typeFactory.zeroOrOne(typeFactory.itemString())), 
                true
            )
        ));
        assertEquals(
            "record(name as string?, *)",
            typeFactory.extensibleRecord(fields).toString()
        );
    }

    @Test
    public void testSingleElementType() {
        assertEquals(
            "element(title)",
            typeFactory.element("", Set.of(new QualifiedName("", "title"))).toString()
        );
    }

    @Test
    public void testElementTypeWithAlternatives() {
        String repr = typeFactory.element("", Set.of(
                new NamespaceResolver.QualifiedName("", "name"),
                new NamespaceResolver.QualifiedName("", "label")
            )).toString();
        assertTrue(
            "element(name | label)".equals(repr)
            || "element(label | name)".equals(repr)
        );
    }



    @Test
    public void testSimpleChoiceItem() {
        String repr = typeFactory.choice(
            typeFactory.itemNumber(),
            typeFactory.itemString()
        ).toString();
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
            typeFactory.itemChoice(typeFactory.itemNumber(), typeFactory.itemString())
            ).toString();
        assertTrue(variant1.equals(result) || variant2.equals(result));

        variant1 = "(number | string)*";
        variant2 = "(string | number)*";
        result = typeFactory.zeroOrMore(
            typeFactory.itemChoice(
                typeFactory.itemNumber(),
                typeFactory.itemString()
            )
        ).toString();
        assertTrue(variant1.equals(result) || variant2.equals(result));

        variant1 = "(number | string)+";
        variant2 = "(string | number)+";
        result = typeFactory.oneOrMore(
            typeFactory.itemChoice(
                typeFactory.itemNumber(),
                typeFactory.itemString()
            )
        ).toString();
        assertTrue(variant1.equals(result) || variant2.equals(result));
    }

    @Test
    public void testFunctionWithTypedArgumentsAndSuffixRequiresParens() {
        AntlrQuerySequenceType resultType = typeFactory.one(typeFactory.itemNumber());
        List<AntlrQuerySequenceType> argTypes = List.of(
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

