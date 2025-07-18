package com.github.akruk.antlrxquery.typesystem;

import com.github.akruk.antlrxquery.typesystem.defaults.XQuerySequenceType;

public record XQueryRecordField(XQuerySequenceType type, boolean isRequired) {}
