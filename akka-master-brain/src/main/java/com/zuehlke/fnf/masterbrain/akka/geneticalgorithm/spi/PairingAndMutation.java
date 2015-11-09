package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi;

import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;

import java.io.Serializable;
import java.util.Collection;

/**
 * Stateless and default constructor mandatory
 */
public interface PairingAndMutation<T> extends Serializable {
    Collection<T> pairAndMutate(T adam, T eva, Configuration configuration);
}
