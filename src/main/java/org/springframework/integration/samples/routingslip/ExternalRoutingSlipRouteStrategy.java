package org.springframework.integration.samples.routingslip;

import org.springframework.integration.routingslip.RoutingSlipRouteStrategy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.*;

public class ExternalRoutingSlipRouteStrategy implements RoutingSlipRouteStrategy {

    private Map<UUID, LinkedList<String>> routingSlips = new WeakHashMap<>();
    private static final LinkedList EMPTY_ROUTINGSLIP = new LinkedList<>();

    @Override
    public Object getNextPath(Message<?> requestMessage, Object reply) {
        MessageHeaders headers = requestMessage.getHeaders();

        UUID id = headers.getId();
        if (!routingSlips.containsKey(id)) {
            @SuppressWarnings("unchecked")
            List<String> routingSlipParam = headers.get("routingSlipParam", List.class);
            if (routingSlipParam != null) {
                routingSlips.put(id, new LinkedList<>(routingSlipParam));
            }
        }
        LinkedList<String> routingSlip = routingSlips.getOrDefault(id, EMPTY_ROUTINGSLIP);

        String nextPath = routingSlip.poll();
        if (nextPath == null) {
            routingSlips.remove(id);
        }

        return nextPath;

    }
}
