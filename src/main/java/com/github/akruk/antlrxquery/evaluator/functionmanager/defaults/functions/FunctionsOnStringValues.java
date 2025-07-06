package com.github.akruk.antlrxquery.evaluator.functionmanager.defaults.functions;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.akruk.antlrxquery.evaluator.XQueryVisitingContext;
import com.github.akruk.antlrxquery.values.XQueryError;
import com.github.akruk.antlrxquery.values.XQueryValue;
import com.github.akruk.antlrxquery.values.factories.XQueryValueFactory;

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



    public XQueryValue char_(XQueryVisitingContext context, List<XQueryValue> args, Map<String, XQueryValue> kwargs) {
        return null;
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
