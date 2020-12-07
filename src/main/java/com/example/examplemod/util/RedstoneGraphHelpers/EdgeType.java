package com.example.examplemod.util.RedstoneGraphHelpers;

public enum EdgeType {
    powering,
    updating,
    //having both can be functionally different from having one of each (update order nightmares)
    both
}
