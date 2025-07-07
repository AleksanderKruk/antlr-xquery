package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.values.XQueryError;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;
import com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions.htmlentities.HTMLEntities;

public class FunctionsOnStringValues {
    private final XQueryValueFactory valueFactory;
    public FunctionsOnStringValues(final XQueryValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    public XQueryValue concat(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() >= 2) return XQueryError.WrongNumberOfArguments;
        if (args.size() == 0) return valueFactory.string(context.getItem().stringValue());
        String joined = args.get(0).atomize().stream().map(XQueryValue::stringValue).collect(Collectors.joining());
        return valueFactory.string(joined);
    }

    public XQueryValue stringJoin(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() == 1) {
            var sequence = args.get(0).sequence();
            String joined = sequence.stream().map(XQueryValue::stringValue).collect(Collectors.joining());
            return valueFactory.string(joined);
        } else if (args.size() == 2) {
            var sequence = args.get(0).sequence();
            var delimiter = args.get(1).stringValue();
            String joined = sequence.stream().map(XQueryValue::stringValue).collect(Collectors.joining(delimiter));
            return valueFactory.string(joined);
        } else {
            return XQueryError.WrongNumberOfArguments;
        }
    }

    public XQueryValue substring(XQueryVisitingContext ctx, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() == 2 || args.size() == 3) {
            var target = args.get(0);
            if (!args.get(1).isNumericValue()) return XQueryError.InvalidArgumentType;
            int position = args.get(1).numericValue().intValue();
            if (args.size() == 2) {
                return target.substring(position);
            } else {
                if (!args.get(2).isNumericValue()) return XQueryError.InvalidArgumentType;
                int length = args.get(2).numericValue().intValue();
                return target.substring(position, length);
            }
        }
        return XQueryError.WrongNumberOfArguments;
    }


    public XQueryValue stringLength(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() == 0) {
            var str = context.getItem().stringValue();
            return valueFactory.number(str.length());
        } else if (args.size() == 1) {
            return valueFactory.number(args.get(0).stringValue().length());
        } else {
            return XQueryError.WrongNumberOfArguments;
        }
    }

    public Pattern whitespace = Pattern.compile("\\s+");
    public UnaryOperator<String> normalize = (String s) -> {
        var trimmed = s.trim();
        return whitespace.matcher(trimmed).replaceAll(" ");
    };
    public XQueryValue normalizeSpace(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() == 0) {
            String s = context.getItem().stringValue();
            return valueFactory.string(normalize.apply(s));
        } else if (args.size() == 1) {
            return valueFactory.string(normalize.apply(args.get(0).stringValue()));
        } else {
            return XQueryError.WrongNumberOfArguments;
        }
    }

    public XQueryValue uppercase(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        return args.get(0).uppercase();
    }

    public XQueryValue lowercase(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        if (args.size() != 1) return XQueryError.WrongNumberOfArguments;
        return args.get(0).lowercase();
    }



    // private static final Map<String,String> HTML5_ENTITIES = Map.ofEntries(
    //     Map.entry("nbsp", "\u00A0"),
    //     Map.entry("lt",   "\u003C"),
    //     Map.entry("gt",   "\u003E"),
    //     Map.entry("amp",  "\u0026"),
    //     Map.entry("quot", "\""),
    //     Map.entry("apos", "\u0027"),
    //     Map.entry("pi",   "\u03C0"),
    //     Map.entry("NotEqualTilde", "\u2242\u0338")
    // );

    private Map<String, String> HTML5_ENTITIES;
    Map<String, String> getEntities() {
        if (HTML5_ENTITIES == null) {
            HTML5_ENTITIES = (new HTMLEntities()).HTML5_ENTITIES;
        }
        return HTML5_ENTITIES;
    }

    /**
     *  fn:char($value as xs:string|xs:positiveInteger) as xs:string
     *  The function returns a string, generally containing a single character or glyph, identified by $value.
     *  The supplied value of $value must be one of the following:
     */
    public XQueryValue char_(
            XQueryVisitingContext context,
            List<XQueryValue> args,
            Map<String, XQueryValue> kwargs)
    {
        XQueryValue arg = args.get(0);
        // A Unicode codepoint, supplied as an integer. For example fn:char(9) returns the tab character.
        if (arg.isNumericValue()) {
            BigDecimal dec = arg.numericValue();
            try {
                int cp = dec.intValueExact();
                // Unicode range and surrogates
                if (cp < 0
                    || cp > Character.MAX_CODE_POINT
                    || (cp >= 0xD800 && cp <= 0xDFFF))
                {
                    return XQueryError.UnrecognizedOrInvalidCharacterName;
                }
                String s = new String(Character.toChars(cp));
                return valueFactory.string(s);

            } catch (ArithmeticException ex) {
                return XQueryError.InvalidArgumentType;
            }
        }

        // A backslash-escape sequence from the set \n (U+000A (NEWLINE) ), \r (U+000D (CARRIAGE RETURN) ), or \t (U+0009 (TAB) ).
        String s = arg.stringValue();
        switch (s) {
            case "\\n":
                return valueFactory.string("\n");
            case "\\r":
                return valueFactory.string("\r");
            case "\\t":
                return valueFactory.string("\t");
        }

        // An HTML5 character reference name (often referred to as an entity name) as defined at https://html.spec.whatwg.org/multipage/named-characters.html. The name is written with no leading ampersand and no trailing semicolon. For example fn:char("pi") represents the character U+03C0 (GREEK SMALL LETTER PI, π) and fn:char("nbsp") returns U+00A0 (NON-BREAKING SPACE, NBSP) .
        // A processor may recognize additional character reference names defined in other versions of HTML. Character reference names are case-sensitive.
        // In the event that the HTML5 character reference name identifies a string comprising multiple codepoints, that string is returned.

        if (getEntities().containsKey(s)) {
            return valueFactory.string(HTML5_ENTITIES.get(s));
        }

        return XQueryError.UnrecognizedOrInvalidCharacterName;
    }
    public XQueryValue characters(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        return null;
    }

    public XQueryValue graphemes(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        return null;
    }


    public XQueryValue normalizeUnicode(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        return null;
    }

    public XQueryValue translate(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        return null;
    }


}
