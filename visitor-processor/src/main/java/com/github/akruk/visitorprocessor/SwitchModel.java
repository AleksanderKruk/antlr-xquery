package com.github.akruk.visitorprocessor;

import java.util.List;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class SwitchModel {

    public final List<ClassModel> parameters;
    public final String variableToSwitchOn;
    public final List<CaseModel> cases;

    public SwitchModel(
            List<ClassModel> parameters,
            String variableToSwitchOn,
            List<CaseModel> cases)
    {
        this.parameters = parameters;
        this.variableToSwitchOn = variableToSwitchOn;
        this.cases = cases;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof SwitchModel that)) return false;

        return Objects.equals(parameters, that.parameters)
                && Objects.equals(variableToSwitchOn, that.variableToSwitchOn)
                && Objects.equals(cases, that.cases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters, variableToSwitchOn, cases);
    }

    @Override
    public String toString() {
        return "SwitchModel[" +
                "parameters=" + parameters +
                ", variableToSwitchOn=" + variableToSwitchOn +
                ", cases=" + cases +
                ']';
    }
}
