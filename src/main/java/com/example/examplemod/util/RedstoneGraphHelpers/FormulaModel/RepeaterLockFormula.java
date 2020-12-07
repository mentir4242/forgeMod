package com.example.examplemod.util.RedstoneGraphHelpers.FormulaModel;

import java.util.*;

public class RepeaterLockFormula extends Formula {
    Formula inputFormula;
    Formula lockFormula;

    public RepeaterLockFormula(Formula inputFormula, Formula lockFormula) {
        this.inputFormula = inputFormula;
        this.lockFormula = lockFormula;
    }

    @Override
    public boolean evaluate(Map<Integer, Boolean> vars) {
        //we dont know what the state is, so we just assume its out if lock is on.
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
        RepeaterLockFormula that = (RepeaterLockFormula) o;
        return Objects.equals(getInputFormula(), that.getInputFormula()) &&
                Objects.equals(getLockFormula(), that.getLockFormula());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInputFormula(), getLockFormula());
    }

    @Override
    public String toString(){
        return "["+inputFormula.toString()+" locked by "+lockFormula+"]";
    }

    public Formula getInputFormula() {
        return inputFormula;
    }

    public Formula getLockFormula() {
        return lockFormula;
    }
}
