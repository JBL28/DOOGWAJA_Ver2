package dev.ssafy.common.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * 모든 HTTP 요청에 대해 고유한 Trace ID와 요청 메타데이터를 MDC에 삽입하는 필터입니다.
 * 분산 서버/여러 계층의 로깅 시 동일한 요청을 추적하기 위해 사용됩니다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter implements Filter {

    private static final String TRACE_ID = "traceId";
    private static final String REQUEST_URL = "requestUrl";
    private static final String REQUEST_METHOD = "requestMethod";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            // 요청 추적을 위한 8자리 짧은 식별자 부여
            String traceId = UUID.randomUUID().toString().substring(0, 8);
            MDC.put(TRACE_ID, traceId);
            
            // 공통 로깅 포맷팅을 위해 URL 및 Method 보관
            if (request instanceof HttpServletRequest req) {
                MDC.put(REQUEST_URL, req.getRequestURI());
                MDC.put(REQUEST_METHOD, req.getMethod());
            }

            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID);
            MDC.remove(REQUEST_URL);
            MDC.remove(REQUEST_METHOD);
            // 사용자 정보나 기타 MDC 데이터도 함께 지운다고 가정 (메모리 누수 방지)
            MDC.remove("userId"); 
        }
    }
}
