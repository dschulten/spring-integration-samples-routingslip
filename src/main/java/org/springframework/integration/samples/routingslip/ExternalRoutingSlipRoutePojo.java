package org.springframework.integration.samples.routingslip;

import org.springframework.messaging.Message;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExternalRoutingSlipRoutePojo {

    private List<String> routingSlip;
    private int i = 0;

    public String get(Message<?> requestMessage, Object reply) {
        if (routingSlip == null) {
            routingSlip = (LinkedList)requestMessage.getHeaders()
                .get("routingSlipParam");
        }
        try {
            return this.routingSlip.get(i++);
        } catch (Exception e) {
            return null;
        }
    }

}