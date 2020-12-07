package com.example.examplemod.util.RedstoneGraphHelpers.FormulaModel;

import java.util.Map;
import java.util.Set;

public abstract class Formula {
    //is destructive!
    public abstract Formula simplifyThis();

    public abstract Formula simplifyThisBoolisch();

    public abstract boolean evaluate(Map<Integer,Boolean> vars);
    public abstract Set<Integer> getAllInputs();

//    public Formula simplifyThisWithTautologyHelp(){}

//    public abstract boolean isTautology();
//    public abstract boolean isReverseTautology();
}
