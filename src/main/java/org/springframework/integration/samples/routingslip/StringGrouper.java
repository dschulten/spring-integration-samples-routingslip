package org.springframework.integration.samples.routingslip;

import java.util.List;

public interface StringGrouper {
    List<List<String>> groupSame(Grouping grouping);
}
