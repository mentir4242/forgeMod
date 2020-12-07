package com.example.examplemod.util.RedstoneGraphHelpers;

import com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects.AbstractActiveCircuitObject;

import java.util.List;

public class InputModel {
    List<AbstractActiveCircuitObject> path;
    List<AbstractActiveCircuitObject> inputItself;

    public List<AbstractActiveCircuitObject> getPath() {
        return path;
    }

    public List<AbstractActiveCircuitObject> getInputItself() {
        return inputItself;
    }

    public InputModel(List<AbstractActiveCircuitObject> path, List<AbstractActiveCircuitObject> inputItself) {
        this.path = path;
        this.inputItself = inputItself;
    }

    public boolean isInput(){
        return (inputItself.size()==1);
    }

    public String toString(){
        StringBuilder out = new StringBuilder();
        if(isInput()){
            out.append("Input-Input named ").append(inputItself.get(0).getCircuitObject().toString()).append(" with path \n");
        } else {
            out.append("Loop-Input with following parts: \n");
            out.append("--Loops parts start:\n");
            for (AbstractActiveCircuitObject inputPart : inputItself){
                out.append(inputPart.getCircuitObject().toString()).append("\n");
            }
            out.append("--Loops parts end----\n");
        }

        out.append("--------Path start: \n");
        for (AbstractActiveCircuitObject pathPart : path){
            out.append(pathPart.getCircuitObject().toString()).append("\n");
        }
        out.append("--------Path end\n");

        return out.toString();
    }
}
