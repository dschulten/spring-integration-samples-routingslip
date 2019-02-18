package org.springframework.integration.samples.routingslip;

import java.util.List;

public class Transform {
    private String toTransform;
    private List<String> routingSlip;

    public String getToTransform() {
        return toTransform;
    }

    public void setToTransform(String toTransform) {
        this.toTransform = toTransform;
    }

    public List<String> getRoutingSlip() {
        return routingSlip;
    }

    public void setRoutingSlip(List<String> routingSlip) {
        this.routingSlip = routingSlip;
    }
}
