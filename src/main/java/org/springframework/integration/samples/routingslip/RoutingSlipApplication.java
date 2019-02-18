package org.springframework.integration.samples.routingslip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.http.dsl.Http;
import org.springframework.integration.routingslip.RoutingSlipRouteStrategy;
import org.springframework.integration.transformer.support.RoutingSlipHeaderValueMessageProcessor;
import org.springframework.messaging.MessageChannel;

@SpringBootApplication
public class RoutingSlipApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoutingSlipApplication.class, args);
    }


    public MessageChannel result() {
        return MessageChannels.direct().get();
    }
    /**
     * Defines transformer chain.
     * <ul>
     * <li>defining the RoutingSlipHeaderValueMessage in a headerFunction does not work since the routingSlip
     * must be a Map and we cannot execute processMap in the headerFunction because at that time the processor
     * has no bean context to work with.</li>
     * <li>we also cannot use an expression like <code>request.headers['routingSlipParam']</code> because
     * expressions given to RoutingSlipHeaderValueMessageProcessor must return exactly one String</li>
     * <li>the challenge is to create a RoutingSlipRouteStrategy which holds the routing slip list as internal state
     * and returns a different item from the list each time</li>
     * </ul>
     *
     * @return
     * @see <a href="https://docs.spring.io/spring-integration/reference/html/#uri-template-variables-and-expressions">
     * Path variables and request params</a>
     */
    @Bean
    public IntegrationFlow transformerChain(RoutingSlipRouteStrategy routeStrategy) {
        return IntegrationFlows.from(
            Http.inboundGateway("/transform")
                .headerExpression("routingSlipParam", "#requestParams['routing-slip']")
                .requestPayloadType(String.class))
            .enrichHeaders(spec -> spec.header(IntegrationMessageHeaderAccessor.ROUTING_SLIP,
//                new RoutingSlipHeaderValueMessageProcessor(routeStrategy, "result")
                new RoutingSlipHeaderValueMessageProcessor("@routePojo.get(request, reply)")
                )
            )
            .logAndReply();
    }

    @Bean
    public IntegrationFlow resultFlow() {
        return IntegrationFlows.from("result").logAndReply();
    }

    @Bean
    public ExternalRoutingSlipRoutePojo externalRoutingSlipRoutePojo() {
        return new ExternalRoutingSlipRoutePojo();
    }

    @Bean
    IntegrationFlow toUppercaseFlow() {
        return IntegrationFlows.from("uppercase").<String, String>transform(source -> source + " upper").get();
    }

    @Bean
    IntegrationFlow toLowercaseFlow() {
        return IntegrationFlows.from("lowercase").<String, String>transform(source -> source + " lower").get();
    }

    @Bean
    IntegrationFlow capitalizeFlow() {
        return IntegrationFlows.from("capitalize").<String, String>transform(source->source + " capitalize").get();
    }

    @Bean
    RoutingSlipRouteStrategy routeStrategy() {
        return new ExternalRoutingSlipRouteStrategy();
    }

    @Bean
    ExternalRoutingSlipRoutePojo routePojo() {
        return new ExternalRoutingSlipRoutePojo();
    }


    @Bean
    TestRoutingSlipRoutePojo testRoutePojo() {
        return new TestRoutingSlipRoutePojo();
    }

}
