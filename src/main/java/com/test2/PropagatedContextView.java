package com.test2;

import io.micronaut.core.propagation.PropagatedContextElement;
import reactor.util.context.ContextView;

public record PropagatedContextView(ContextView ctx) implements PropagatedContextElement {
}
