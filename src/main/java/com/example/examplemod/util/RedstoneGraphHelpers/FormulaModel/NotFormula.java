package com.example.examplemod.util.RedstoneGraphHelpers.FormulaModel;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class NotFormula extends Formula{
    private Formula formula;

    public Formula getFormula() {
        return formula;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotFormula that = (NotFormula) o;
        return Objects.equals(getFormula(), that.getFormula());
    }

    @Override
    public boolean evaluate(Map<Integer, Boolean> vars) {
        return !formula.evaluate(vars);
    }

    @Override
    public Set<Integer> getAllInputs() {
        return formula.getAllInputs();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFormula());
    }

    @Override
    public Formula simplifyThis() {
        //simplify everything underneat us.
        formula=formula.simplifyThis();
        //double NOT is YES
        if(formula instanceof NotFormula){
            return ((NotFormula) formula).getFormula();
        }
        //if not double NOT then we have done our job here, nothing more to do IMO.
        else return this;
    }

    @Override
    public Formula simplifyThisBoolisch() {
        //formula=formula.simplifyThis(); //would have terrible runtime, we try to make it so
        //simplify Boolisch is stricly more powerful. Maybe, lets see
        formula=formula.simplifyThisBoolisch();
        //negation of a disjunktion is a konjunktion or the inverse elements
        //aka: NOT of an OR formula is an And formula with all inverted Elements
        if(formula instanceof OrFormula){
            Set<Formula> newFormulaSet=new HashSet<>();
            for(Formula formula : ((OrFormula)this.formula).getParts()){
                Formula newFormula = new NotFormula(formula);
                newFormula=newFormula.simplifyThisBoolisch();
                newFormulaSet.add(newFormula);
            }
            return new AndFormula(newFormulaSet);
        }
        //a similar thing is true in reverse.
        if(formula instanceof AndFormula){
            Set<Formula> formulas=new HashSet<>();
            for(Formula formula : ((AndFormula)this.formula).getParts()){
                Formula formula1 = new NotFormula(formula);
                formula1=formula1.simplifyThisBoolisch();
                formulas.add(formula1);
            }
            return new OrFormula(formulas);
        }

        if(formula instanceof Tautology){
            return new Contradiction();
        }

        if(formula instanceof Contradiction){
            return new Tautology();
        }

        //double NOT is YES
        if(formula instanceof NotFormula){
            return ((NotFormula) formula).getFormula();
        }
        //if not double NOT then we have done our job here, nothing more to do IMO.
        else return this;
    }

    @Override
    public String toString() {
        return "\u00AC"
                 + formula ;
    }

    public NotFormula(Formula formula) {
        this.formula = formula;
    }
}
