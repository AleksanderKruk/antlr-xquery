package com.github.akruk.visitorprocessor;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class ClassModel {

    public final String qualifiedName;
    public final String className;
    public final String variableName;

    public ClassModel(
            final String qualifiedName,
            final String className,
            final String variableName
    ) {
        this.qualifiedName = qualifiedName;
        this.className = className;
        this.variableName = variableName;
    }
}
