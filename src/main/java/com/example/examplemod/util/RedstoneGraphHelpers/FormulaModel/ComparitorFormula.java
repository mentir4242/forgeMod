package com.example.examplemod.util.RedstoneGraphHelpers.FormulaModel;

import java.util.*;

public class ComparitorFormula extends Formula{
    Formula inputFormula;
    Formula lockFormula;

    public ComparitorFormula(Formula inputFormula, Formula lockFormula) {
        this.inputFormula = inputFormula;
        this.lockFormula = lockFormula;
    }

    @Override
    public boolean evaluate(Map<Integer, Boolean> vars) {
        return inputFormula.evaluate(vars) && (lockFormula.evaluate(vars)==false);
    }

    @Override
    public Set<Integer> getAllInputs() {
        Set<Integer> set = new HashSet<>();
        set.addAll(inputFormula.getAllInputs());
        set.addAll(lockFormula.getAllInputs());
        return set;
    }

    @Override
    public Formula simplifyThis() {
        inputFormula=inputFormula.simplifyThis();
        lockFormula=lockFormula.simplifyThis();
        return this;
    }

    @Override
    public Formula simplifyThisBoolisch() {
        inputFormula=inputFormula.simplifyThisBoolisch();
        lockFormula=lockFormula.simplifyThisBoolisch();
        return new AndFormula(
                new HashSet<>(
                        Arrays.asList(
                                inputFormula,
                                new NotFormula(
                                        lockFormula
                                )
                        )
                )
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComparitorFormula that = (ComparitorFormula) o;
        return getInputFormula().equals(that.getInputFormula()) &&
                getLockFormula().equals(that.getLockFormula());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInputFormula(), getLockFormula());
    }

    @Override
    public String toString(){
        return "["+inputFormula.toString()+" minus "+lockFormula+"]";
    }

    public Formula getInputFormula() {
        return inputFormula;
    }

    public Formula getLockFormula() {
        return lockFormula;
    }
}
