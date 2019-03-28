package org.springframework.integration.samples.routingslip;

import java.util.List;

public class Grouping {
    final List<String> routingSlip;
    final List<String> strings;

    public Grouping(List<String> strings, List<String> routingSlip) {
        this.routingSlip = routingSlip;
        this.strings = strings;
    }

    public List<String> getRoutingSlip() {
        return routingSlip;
    }

    public List<String> getStrings() {
        return strings;
    }
}
