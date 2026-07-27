package com.github.akruk.visitorprocessor;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;

@DefaultQualifier(NonNull.class)
public final class LeafModel {

    public final List<ClassModel> visitedClasses;

    public LeafModel(List<ClassModel> visitedClasses) {
        this.visitedClasses = visitedClasses;
    }
}
