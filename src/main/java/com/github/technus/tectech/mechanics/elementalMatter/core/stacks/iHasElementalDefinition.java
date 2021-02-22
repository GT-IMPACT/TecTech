package com.github.technus.tectech.mechanics.elementalMatter.core.stacks;

import com.github.technus.tectech.mechanics.elementalMatter.core.templates.iElementalDefinition;

/**
 * Created by danie_000 on 30.01.2017.
 */
public interface iHasElementalDefinition extends Comparable<iHasElementalDefinition>,Cloneable {
    iElementalDefinition getDefinition();

    double  getAmount();

    double  getCharge();

    double  getMass();

    iHasElementalDefinition clone();
}
