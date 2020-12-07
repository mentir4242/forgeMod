package com.example.examplemod.util.RedstoneGraphHelpers.FormulaModel;


import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class AndFormula extends Formula{
    private Set<Formula> parts;

    public Set<Formula> getParts() {
        return parts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(getParts().size()==1 && o instanceof OrFormula && ((OrFormula) o).getParts().size()==1){
            return getParts().equals(((OrFormula) o).getParts());
        }
        if (o == null || getClass() != o.getClass()) return false;
        AndFormula that = (AndFormula) o;
        return getParts().equals(that.getParts());
    }

    @Override
    public boolean evaluate(Map<Integer, Boolean> vars) {
        //if at least one is false, we return false. If not, all seem to be true
        //so we return true;
        for (Formula formula : getParts()){
            if(formula.evaluate(vars)==false){
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<Integer> getAllInputs() {
        Set<Integer> set = new HashSet<>();
        for (Formula formula : parts){
            set.addAll(formula.getAllInputs());
        }
        return set;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getParts());
    }

    @Override
    public Formula simplifyThis() {
        Set<Formula> newParts=new HashSet<>();
        for (Formula formula : parts){
            formula = formula.simplifyThis();
            if(formula instanceof AndFormula){
                newParts.addAll(((AndFormula) formula).getParts());
            } else {
                newParts.add(formula);
            }
        }
        parts=newParts;

        //if we only one long, we remove ourself
        if (parts.size()==1){
            return parts.iterator().next();
        } else  {
            return this;
        }
    }

    @Override
    public Formula simplifyThisBoolisch() {
        Set<Formula> newParts=new HashSet<>();
        for (Formula formula : parts){
            formula = formula.simplifyThisBoolisch();
            if(formula instanceof AndFormula){
                newParts.addAll(((AndFormula) formula).getParts());
            } else {
                newParts.add(formula);
            }
        }
        parts=newParts;

        for (Formula formula : parts){
            if (formula instanceof Contradiction){
                return new Contradiction();
            }
            Formula inverseFormula = new NotFormula(formula).simplifyThisBoolisch();
            if (parts.contains(inverseFormula)){
                return new Contradiction();
            }
        }

        parts.forEach((formula -> {
            if (formula instanceof Tautology) {
                parts.remove(formula);
            }
        }));

        //if we only one long, we remove ourself
        if (parts.size()==1){
            return parts.iterator().next();
        } else if (parts.size()==0){
            //nothing present that could turn this off, so we always true (or so I think?)
            return new Tautology();
        }else{
            return this;
        }
    }

    @Override
    public String toString() {
        if(parts.size()==1){
            return parts.iterator().next().toString();
        }

        StringBuilder string= new StringBuilder();
        string.append("(");
        for (Formula formula : parts){
            string.append(formula.toString());
            string.append("\u2227"); //this is the "and" sign, escaped like uro from the graveyard
            //hopefully this doesnt get banned
        }
        string.deleteCharAt(string.length()-1);
        string.append(")");
        return string.toString();
    }

    public AndFormula(Set<Formula> parts) {
        this.parts = parts;
    }
}
