package org.springframework.integration.samples.routingslip;

import org.springframework.integration.routingslip.RoutingSlipRouteStrategy;
import org.springframework.messaging.Message;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Maintains external routing slip. Requires two message headers for which it defines default header names.
 * <ol>
 *     <li>External routing slip as {@code List<String>} by the header name {@link #ROUTING_SLIP_HEADER}</li>
 *     <li>External routing slip index as {@link AtomicInteger} by the header name
 *     {@link #ROUTING_SLIP_INDEX_HEADER}</li>
 * </ol>
 * If you need multiple external routing slips in a single flow, you may define custom header names.
 *
 * Example:
 * <pre>
 * POST http://localhost:8080/transform?routing-slip=capitalize&routing-slip=lowercase
 * Content-Type: text/plain
 *
 * foo
 * </pre>
 *
 * <pre>
 * Http.inboundGateway("/transform")
 *     .headerExpression(ExternalRoutingSlipRouteStrategy.ROUTING_SLIP_HEADER,
 *         "#requestParams['routing-slip']")
 *     .requestPayloadType(String.class))
 *   .enrichHeaders(spec -> spec
 *     .headerFunction(ExternalRoutingSlipRouteStrategy.ROUTING_SLIP_INDEX_HEADER,
 *         h -> new AtomicInteger())
 *     .header(IntegrationMessageHeaderAccessor.ROUTING_SLIP,
 *         new RoutingSlipHeaderValueMessageProcessor(routeStrategy)
 *     )
 * )
 * </pre>
 */
public class ExternalRoutingSlipRouteStrategy implements RoutingSlipRouteStrategy {


    public static final String ROUTING_SLIP_HEADER = "externalRoutingSlip";
    public static final String ROUTING_SLIP_INDEX_HEADER = "externalRoutingSlipIndex";

    private String externalRoutingSlipHeader = ROUTING_SLIP_HEADER;
    private String externalRoutingSlipIndexHeader = ROUTING_SLIP_INDEX_HEADER;


    public ExternalRoutingSlipRouteStrategy() {

    }

    @Override
    public Object getNextPath(Message<?> requestMessage, Object reply) {
        //noinspection unchecked
        List<String> routingSlip = (List<String>) requestMessage.getHeaders()
            .get(externalRoutingSlipHeader);

        //noinspection ConstantConditions
        int routingSlipIndex = requestMessage.getHeaders()
            .get(externalRoutingSlipIndexHeader, AtomicInteger.class)
            .getAndIncrement();

        String routingSlipEntry;
        if (routingSlip != null && routingSlipIndex < routingSlip.size()) {
            routingSlipEntry = routingSlip.get(routingSlipIndex);
        } else {
            routingSlipEntry = null;
        }
        return routingSlipEntry;
    }

    public void setExternalRoutingSlipHeader(String externalRoutingSlipHeader) {
        this.externalRoutingSlipHeader = externalRoutingSlipHeader;
    }


    public void setExternalRoutingSlipIndexHeader(String externalRoutingSlipIndexHeader) {
        this.externalRoutingSlipIndexHeader = externalRoutingSlipIndexHeader;
    }

}
