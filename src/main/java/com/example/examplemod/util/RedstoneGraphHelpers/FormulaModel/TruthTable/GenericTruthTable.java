package com.example.examplemod.util.RedstoneGraphHelpers.FormulaModel.TruthTable;

import com.example.examplemod.util.RedstoneGraphHelpers.FormulaModel.Formula;
import com.example.examplemod.util.utility;

import java.util.*;

public class GenericTruthTable implements TruthTable {
    Formula formula;
    @Override
    public boolean getValueFor(Map<Integer, Boolean> vars) {
        return formula.evaluate(vars);
    }

    public GenericTruthTable(Formula formula) {
        this.formula = formula;
    }

    @Override
    public String toString() {
        List<Integer> inputIds = new ArrayList<>(formula.getAllInputs());
        StringBuilder string= new StringBuilder();
        String umbruch="\n";
        String tab = "\t";
        string.append("GenericTruthTable for ").append(formula.toString());
        string.append(umbruch);

        for(Integer integer : inputIds){
            string.append(utility.getStringRepresentationOfInt(integer)).append(tab);
        }
        string.append("Values:").append(tab);
        string.append(umbruch);

        Map<Integer, Boolean> vars = new HashMap<>();
        for (int j = 0; j < inputIds.size(); j++) {
            vars.put(inputIds.get(j), false);
        }

        while(hasNext(vars)){
            string.append(getVarsString(vars, inputIds)).append(formula.evaluate(vars) ? "1, true" : "0, false");
            string.append(umbruch);
            getNext(vars, inputIds);
        }
        string.append(getVarsString(vars, inputIds)).append(formula.evaluate(vars) ? "1, true" : "0, false");
        string.append(umbruch);

        string.append("finish fuer heute");

        return string.toString();
    }


    public String lautToString() {
        List<Integer> inputIds = new ArrayList<>(formula.getAllInputs());
        String umbruch="\n";
        String tab = "\t";
        System.out.println("GenericTruthTable for ");
        System.out.println(formula.toString());
        System.out.println(umbruch);
        System.out.println("Values:");
        System.out.println(tab);
        for(Integer integer : inputIds){
            System.out.println(utility.getStringRepresentationOfInt(integer));
            System.out.println(tab);
        }
        System.out.println(umbruch);

        Map<Integer, Boolean> vars = new HashMap<>();
        for (int j = 0; j < inputIds.size(); j++) {
            vars.put(inputIds.get(j), false);
        }

        while(hasNext(vars)){
            System.out.println(getVarsString(vars, inputIds));
            System.out.println("is:\t");
            System.out.println(formula.evaluate(vars) ? "1, true" : "0, false");
            System.out.println(umbruch);
            getNext(vars, inputIds);
        }

        System.out.println("finish fuer heute");

        return "";
    }

    public static String getVarsString(Map<Integer, Boolean> vars, List<Integer> integers){
        StringBuilder string= new StringBuilder();
        for(int i : integers){
            string.append(vars.get(i) ? 1 : 0);
            string.append("\t");
        }
        return string.toString();
    }

    //if all are one, we return false, otherwise true.
    public boolean hasNext(Map<Integer, Boolean> vars){
        for(Boolean bool : vars.values()){
            if(bool==false){
                return true;
            }
        }
        return false;
    }

    //add 1 to the "number" by flipping from left to right until we hit a 0 (false)
    public void getNext(Map<Integer, Boolean> vars, List<Integer> integers){
        int i=0;
        while(vars.get(integers.get(i)))
        {
            Boolean value = vars.get(integers.get(i));
            value = !value;
            vars.put(integers.get(i), value);
            i++;
        }
        vars.put(integers.get(i), true);
    }
}
