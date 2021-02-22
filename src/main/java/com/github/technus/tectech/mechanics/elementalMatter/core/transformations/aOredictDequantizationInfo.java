package com.github.technus.tectech.mechanics.elementalMatter.core.transformations;

import com.github.technus.tectech.mechanics.elementalMatter.core.stacks.iHasElementalDefinition;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;

/**
 * Created by Tec on 23.05.2017.
 */
public class aOredictDequantizationInfo implements iExchangeInfo<iHasElementalDefinition,String> {
    private final iHasElementalDefinition in;
    public final String out;
    public final int amount;

    public aOredictDequantizationInfo(iHasElementalDefinition emIn, String name, int qty) {
        in = emIn;
        out =name;
        amount = qty;
    }

    public aOredictDequantizationInfo(iHasElementalDefinition emIn, OrePrefixes prefix, Materials material, int qty) {
        in = emIn;
        out = prefix.name() + material.mName;
        amount = qty;
    }

    public aOredictDequantizationInfo(iHasElementalDefinition emIn, OrePrefixes prefix, String materialName, int qty) {
        in = emIn;
        out = prefix.name() + materialName;
        amount = qty;
    }

    @Override
    public iHasElementalDefinition input() {
        return in.clone();//MEH!
    }

    @Override
    public String output() {
        return out;
    }

    @Override
    public int hashCode() {
        return in.getDefinition().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof aOredictDequantizationInfo && hashCode() == obj.hashCode();
    }
}