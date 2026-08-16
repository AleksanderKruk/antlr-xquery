package com.github.akruk.visitorprocessor;

import java.util.List;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CaseModel {

    public final String className;
    public final String variableName;
    public final List<String> pathVariables;

    public CaseModel(
            String className,
            String variableName,
            List<String> pathVariables)
    {
        this.className = className;
        this.variableName = variableName;
        this.pathVariables = pathVariables;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof CaseModel that)) return false;

        return Objects.equals(className, that.className)
                && Objects.equals(variableName, that.variableName)
                && Objects.equals(pathVariables, that.pathVariables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, variableName, pathVariables);
    }

    @Override
    public String toString() {
        return "CaseModel[" +
                "className=" + className +
                ", variableName=" + variableName +
                ", pathVariables=" + pathVariables +
                ']';
    }
}
