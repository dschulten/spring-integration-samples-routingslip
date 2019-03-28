package org.springframework.integration.samples.routingslip;

import org.apache.tomcat.util.buf.StringUtils;
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
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;


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
     *
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
    public IntegrationFlow groupMessageFlow() {
        return flow -> flow
            .split()
            .aggregate(agg -> agg.correlationStrategy(message -> message.getPayload())
                .releaseStrategy(group -> 5 == group.getSequenceSize()))
            .log();
    }

    /**
     * Groups messages, routes groups by routing slip, but treats single items separately.
     *
     * @return
     */
    @Bean
    public IntegrationFlow groupStringsFlow(RoutingSlipRouteStrategy routeStrategy) {
        return IntegrationFlows.from(StringGrouper.class)
            .enrichHeaders(h -> h.<Grouping>headerFunction(ExternalRoutingSlipRouteStrategy.ROUTING_SLIP_HEADER,
                message -> message.getPayload()
                    .getRoutingSlip()))
            .<Grouping, List<List<String>>>transform(grouping -> grouping.getStrings()
                .stream()
                .collect(Collectors.collectingAndThen(
                    Collectors.groupingBy(Function.identity()),
                    grouped ->
                        new ArrayList<>(grouped.values()))
                ))
            .split()
            .<List<?>, Boolean>route(items -> items.size() == 1,
                route -> route
                    .subFlowMapping(true, flow -> flow // single items
                        .<List<String>, String>transform(source -> source.get(0))
                        .log() // do sth with message
                        .transform(Collections::singletonList)
                        .channel(finalizeFlow().getInputChannel()) // route into channel, or stranger things happen
                    )
                    .subFlowMapping(false, flow -> flow // groups
                        .transform(source -> source)
                        .enrichHeaders(spec -> spec
                            .headerFunction(ExternalRoutingSlipRouteStrategy.ROUTING_SLIP_INDEX_HEADER,
                                h -> new AtomicInteger())
                            .header(IntegrationMessageHeaderAccessor.ROUTING_SLIP,
                                new RoutingSlipHeaderValueMessageProcessor(routeStrategy)
                            )
                        )
                    ))
            .get();

    }

    @Bean
    IntegrationFlow finalizeFlow() {
        return f -> f
            .aggregate()
            .<List<List<String>>, List<String>>transform(source -> source.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));
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
    IntegrationFlow joinStringsFlow() {
        return IntegrationFlows.from(joinStrings())
            .<List<String>, List<String>>transform(source -> Collections.singletonList(StringUtils.join(source)))
            .channel(finalizeFlow().getInputChannel()) // route into specific channel, or stranger things happen
            .get();
    }

    @Bean
    IntegrationFlow stringsToUpperFlow() {
        return IntegrationFlows.from(stringsToUpper())
            .<List<String>, List<String>>transform(source -> source.stream()
                .map(s -> s.toUpperCase())
                .collect(Collectors.toList())
            )
            .channel(finalizeFlow().getInputChannel()) // route into specific channel, or stranger things happen
            .get();
    }


    @Bean
    public MessageChannel stringsToUpper() {
        return MessageChannels.direct()
            .get();
    }

    @Bean
    public MessageChannel joinStrings() {
        return MessageChannels.direct()
            .get();
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
