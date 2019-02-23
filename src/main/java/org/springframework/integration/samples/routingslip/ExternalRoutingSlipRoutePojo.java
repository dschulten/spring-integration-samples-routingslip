package org.springframework.integration.samples.routingslip;

import org.springframework.messaging.Message;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ExternalRoutingSlipRoutePojo {

    public String get(Message<?> requestMessage, Object reply) {

        //noinspection unchecked
        List<String> routingSlip = (List<String>) requestMessage.getHeaders()
            .get("routingSlipParam");

        //noinspection ConstantConditions
        int routingSlipIndex = requestMessage.getHeaders()
            .get("counter", AtomicInteger.class)
            .getAndIncrement();

        String routingSlipEntry;
        if (routingSlip != null && routingSlipIndex < routingSlip.size()) {
            routingSlipEntry = routingSlip.get(routingSlipIndex);
        } else {
            routingSlipEntry = null;
        }
        return routingSlipEntry;
    }

}