package com.example.examplemod.util.RedstoneGraphHelpers.FormulaModel;

import com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects.AbstractActiveCircuitObject;
import com.example.examplemod.util.utility;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class InputFormula extends Formula{
    private AbstractActiveCircuitObject input;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InputFormula that = (InputFormula) o;
        return Objects.equals(getInput(), that.getInput());
    }

    @Override
    public boolean evaluate(Map<Integer, Boolean> vars) {
        return vars.get(input.getId());
    }

    @Override
    public Formula simplifyThis() {
        return this;
    }

    @Override
    public Formula simplifyThisBoolisch() {
        return this;
    }

    @Override
    public Set<Integer> getAllInputs() {
        Set<Integer> set = new HashSet();
        set.add(input.getId());
        return set;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInput());
    }

    @Override
    public String toString() {
        return utility.getStringRepresentationOfInt( input.getId());
    }

    public AbstractActiveCircuitObject getInput() {
        return input;
    }

    public InputFormula(AbstractActiveCircuitObject input) {
        this.input = input;
    }
}
