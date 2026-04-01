package dev.ssafy.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Page 객체를 안정적인 JSON 구조로 래핑하는 공통 응답 DTO.
 * PageImpl을 직접 직렬화하면 JSON 구조가 불안정하다는 경고가 발생하므로
 * 필요한 필드만 추출해 반환합니다.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
