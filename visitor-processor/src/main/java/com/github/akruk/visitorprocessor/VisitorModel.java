package com.github.akruk.visitorprocessor;

import java.util.List;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class VisitorModel {

    public final String packageName;
    public final String visibility;
    public final String modifiers;
    public final String className;

    public final List<ClassModel> visitedClasses;

    public final List<SwitchModel> dispatchers;

    public final List<LeafModel> leaves;

    public final List<String> allImportedNames;

    public VisitorModel(
            String packageName,
            String visibility,
            String modifiers,
            String className,
            List<ClassModel> visitedClasses,
            List<SwitchModel> dispatchers,
            List<LeafModel> leaves,
            List<String> allImportedNames)
    {
        this.packageName = packageName;
        this.visibility = visibility;
        this.modifiers = modifiers;
        this.className = className;
        this.visitedClasses = visitedClasses;
        this.dispatchers = dispatchers;
        this.leaves = leaves;
        this.allImportedNames = allImportedNames;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof VisitorModel that)) return false;

        return Objects.equals(packageName, that.packageName)
                && Objects.equals(visibility, that.visibility)
                && Objects.equals(modifiers, that.modifiers)
                && Objects.equals(className, that.className)
                && Objects.equals(visitedClasses, that.visitedClasses)
                && Objects.equals(dispatchers, that.dispatchers)
                && Objects.equals(leaves, that.leaves)
                && Objects.equals(allImportedNames, that.allImportedNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                packageName,
                visibility,
                modifiers,
                className,
                visitedClasses,
                dispatchers,
                leaves,
                allImportedNames);
    }

    @Override
    public String toString() {
        return "VisitorModel[" +
                "packageName=" + packageName +
                ", visibility=" + visibility +
                ", modifiers=" + modifiers +
                ", className=" + className +
                ", visitedClasses=" + visitedClasses +
                ", dispatchers=" + dispatchers +
                ", leaves=" + leaves +
                ", allImportedNames=" + allImportedNames +
                ']';
    }
}
