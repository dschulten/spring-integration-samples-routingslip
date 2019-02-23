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

import java.util.concurrent.atomic.AtomicInteger;


@SpringBootApplication
public class RoutingSlipApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoutingSlipApplication.class, args);
    }


    public MessageChannel result() {
        return MessageChannels.direct()
            .get();
    }

    /**
     * Defines transformer flow which takes a {@code routing-slip} parameter.
     * @see <a href="https://docs.spring.io/spring-integration/reference/html/#uri-template-variables-and-expressions">
     * Path variables and request params</a>
     */
    @Bean
    public IntegrationFlow transformerChainWithRouteStrategy(RoutingSlipRouteStrategy routeStrategy) {
        return IntegrationFlows.from(
            Http.inboundGateway("/transform")
                .headerExpression(ExternalRoutingSlipRouteStrategy.ROUTING_SLIP_HEADER,
                    "#requestParams['routing-slip']")
                .requestPayloadType(String.class))
            .enrichHeaders(spec -> spec
                    .headerFunction(ExternalRoutingSlipRouteStrategy.ROUTING_SLIP_INDEX_HEADER,
                        h -> new AtomicInteger())
                    .header(IntegrationMessageHeaderAccessor.ROUTING_SLIP,
                        new RoutingSlipHeaderValueMessageProcessor(routeStrategy)
                    )
            )
            .logAndReply();
    }


    @Bean
    public IntegrationFlow transformerChainWithRoutePojo(RoutingSlipRouteStrategy routeStrategy) {
        return IntegrationFlows.from(
            Http.inboundGateway("/transform-with-pojo")
                .headerExpression(ExternalRoutingSlipRouteStrategy.ROUTING_SLIP_HEADER,
                    "#requestParams['routing-slip']")
                .requestPayloadType(String.class))
            .enrichHeaders(spec -> spec
                .headerFunction(ExternalRoutingSlipRouteStrategy.ROUTING_SLIP_INDEX_HEADER,
                    h -> new AtomicInteger())
                .header(IntegrationMessageHeaderAccessor.ROUTING_SLIP,
                    new RoutingSlipHeaderValueMessageProcessor("@routePojo.next(request, reply)")
                )
            )
            .logAndReply();
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
        return IntegrationFlows.from("capitalize").<String, String>transform(source -> source + " capitalize").get();
    }

    @Bean
    RoutingSlipRouteStrategy routeStrategy() {
        return new ExternalRoutingSlipRouteStrategy();
    }

    @Bean
    ExternalRoutingSlipRoutePojo routePojo() {
        return new ExternalRoutingSlipRoutePojo();
    }

}
