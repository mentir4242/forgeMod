package com.example.examplemod.util.RedstoneGraphHelpers.FormulaModel;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class OrFormula extends Formula {
    private Set<Formula> parts;

    public OrFormula(Set<Formula> parts) {

        this.parts = parts;
    }

    public Set<Formula> getParts() {
        return parts;
    }

    @Override
    public Formula simplifyThis() {
        Set<Formula> newParts = new HashSet<>();
        for (Formula formula : parts) {
            formula = formula.simplifyThis();
            if (formula instanceof OrFormula) {
                newParts.addAll(((OrFormula) formula).getParts());
            } else {
                newParts.add(formula);
            }
        }
        parts = newParts;

        //if we only one long, we remove ourself
        if (parts.size() == 1) {
            return parts.iterator().next();
        } else {
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (getParts().size() == 1 && o instanceof AndFormula && ((AndFormula) o).getParts().size() == 1) {
            return getParts().equals(((AndFormula) o).getParts());
        }
        if (o == null || getClass() != o.getClass()) return false;
        OrFormula orFormula = (OrFormula) o;
        return getParts().equals(orFormula.getParts());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getParts());
    }

    @Override
    public boolean evaluate(Map<Integer, Boolean> vars) {
        //if at least one is true, the formula is true. If not, sad times
        for (Formula formula : getParts()) {
            if (formula.evaluate(vars)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<Integer> getAllInputs() {
        Set<Integer> set = new HashSet<>();
        for (Formula formula : parts) {
            set.addAll(formula.getAllInputs());
        }
        return set;
    }

    @Override
    public Formula simplifyThisBoolisch() {
        Set<Formula> newParts = new HashSet<>();
        for (Formula formula : parts) {
            formula = formula.simplifyThisBoolisch();
            if (formula instanceof OrFormula) {
                newParts.addAll(((OrFormula) formula).getParts());
            } else {
                newParts.add(formula);
            }
        }
        parts = newParts;

        for (Formula formula : parts) {
            if (formula instanceof Tautology) {
                //if one is alway true, this is always true
                return new Tautology();
            }
            Formula inverseFormula = new NotFormula(formula).simplifyThisBoolisch();
            if (parts.contains(inverseFormula)) {
                return new Tautology();
            }
        }

        parts.forEach((formula -> {
            if (formula instanceof Contradiction) {
                parts.remove(formula);
            }
        }));


        //if we only one long, we remove ourself
        if (parts.size() == 1) {
            return parts.iterator().next();
        } else if (parts.size() == 0){
            //if there is nothing to turn us on, we stay out.
            return new Contradiction();
        } else {
            return this;
        }
    }

    @Override
    public String toString() {
        if (parts.size() == 1) {
            return parts.iterator().next().toString();
        }

        StringBuilder string = new StringBuilder();
        string.append("(");
        for (Formula formula : parts) {
            string.append(formula.toString());
            string.append("\u2228"); //this is the "or" sign, escaped like uro from the graveyard
            //hopefully this doesnt get banned
        }
        string.deleteCharAt(string.length() - 1);
        string.append(")");
        return string.toString();
    }
}
