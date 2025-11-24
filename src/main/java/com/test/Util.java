package com.test;

import static com.test.Config.EMPTY_VALUE;
import static com.test.Config.REQ_ID_KEY;

import io.micronaut.context.propagation.slf4j.MdcPropagationContext;
import io.micronaut.core.propagation.PropagatedContext;
import java.util.Optional;
import org.slf4j.MDC;

public class Util {

    public static Optional<String> findInMdc() {
        return Optional.ofNullable(MDC.get(REQ_ID_KEY));
    }

    public static Optional<String> findInPc() {
        return PropagatedContext.getOrEmpty()
                .findAll(MdcPropagationContext.class)
                .map(MdcPropagationContext::state)
                .filter(state -> state.containsKey(REQ_ID_KEY))
                .map(state -> state.get(REQ_ID_KEY))
                .findFirst();
    }

}
