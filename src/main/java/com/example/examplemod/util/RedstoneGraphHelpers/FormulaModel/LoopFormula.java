package com.example.examplemod.util.RedstoneGraphHelpers.FormulaModel;

import com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects.AbstractActiveCircuitObject;
import com.example.examplemod.util.utility;

import java.util.*;

public class LoopFormula extends Formula{
    private List<AbstractActiveCircuitObject> loopParts;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoopFormula that = (LoopFormula) o;
        return Objects.equals(getLoopParts(), that.getLoopParts());
    }

    @Override
    public boolean evaluate(Map<Integer, Boolean> vars) {
        //a loop in itself can never be on. non of its parts are inputs.
        //all connections that is may have to the output will be modeled by different Formulas
        return false;
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
        return new HashSet<>();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLoopParts());
    }

    @Override
    public String toString() {
        StringBuilder string= new StringBuilder();
        string.append("loop[");
        for (AbstractActiveCircuitObject abstractActiveCircuitObject : loopParts){
            string.append(utility.getStringRepresentationOfInt( abstractActiveCircuitObject.getId()));
            string.append(",");
        }
        string.deleteCharAt(string.length()-1);
        string.append("]");
        return string.toString();
    }

    public List<AbstractActiveCircuitObject> getLoopParts() {
        return loopParts;
    }

    public LoopFormula(List<AbstractActiveCircuitObject> loopParts) {
        this.loopParts = loopParts;
    }
}
