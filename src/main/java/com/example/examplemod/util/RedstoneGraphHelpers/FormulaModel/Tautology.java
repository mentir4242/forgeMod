package com.example.examplemod.util.RedstoneGraphHelpers.FormulaModel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Tautology extends Formula{
    @Override
    public Formula simplifyThis() {
        return this;
    }

    @Override
    public String toString() {
        return "1";
    }

    @Override
    public Formula simplifyThisBoolisch() {
        return this;
    }

    @Override
    public boolean evaluate(Map<Integer, Boolean> vars) {
        return true;
    }

    @Override
    public Set<Integer> getAllInputs() {
        return new HashSet<>();
    }
}
