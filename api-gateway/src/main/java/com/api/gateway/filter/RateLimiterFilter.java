package com.api.gateway.filter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
public class RateLimiterFilter implements GlobalFilter, Ordered {
    private final RateLimiter rateLimiter;

    public RateLimiterFilter(
            @Value("${rate-limiter.orders.limit-for-period:20}") int limitForPeriod,
            @Value("${rate-limiter.orders.refresh-period-seconds:1}") int refreshPeriodSeconds
    ) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(limitForPeriod)
                .limitRefreshPeriod(Duration.ofSeconds(refreshPeriodSeconds))
                .timeoutDuration(Duration.ZERO)
                .build();
        this.rateLimiter = RateLimiter.of("orderService", config);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (path.startsWith("/api/orders")) {
            if (!rateLimiter.acquirePermission()) {
                log.warn("Rate limit exceeded: path={}", path);
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                exchange.getResponse().getHeaders().add("Retry-After",
                        String.valueOf(rateLimiter.getRateLimiterConfig().getLimitRefreshPeriod().toSeconds()));
                return exchange.getResponse().setComplete();
            }
            log.debug("Request allowed: path={}", path);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
