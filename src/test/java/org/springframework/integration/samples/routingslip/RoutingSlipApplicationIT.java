package org.springframework.integration.samples.routingslip;


import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@SpringIntegrationTest
public class RoutingSlipApplicationIT {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private StringGrouper grouper;

    @Test
    public void groupsEqualItems() {
        List<List<String>> result = grouper.groupSame(new Grouping(Arrays.asList("a", "b", "b", "b", "c"),
            Collections.singletonList("joinStrings"))); // routing slip for group
        assertEquals(3, result.size());
        assertThat(result, Matchers.contains("a", "b,b,b", "c"));
    }

    @Test
    public void keepsSingleMessagesIfNotJoined() {
        List<List<String>> result = grouper.groupSame(new Grouping(Arrays.asList("a", "b", "b", "b", "c"),
            Collections.singletonList("stringsToUpper"))); // routing slip for group
        assertEquals(5, result.size());
        assertThat(result, Matchers.contains("a", "B", "B", "B", "c"));
    }

}
