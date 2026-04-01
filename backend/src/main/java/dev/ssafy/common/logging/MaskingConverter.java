package dev.ssafy.common.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * %mask 키워드로 등록되는 커스텀 컨버터.
 * %msg 대신 사용하며, 로그 메시지 내의 이메일/전화번호를 자동으로 마스킹합니다.
 * CompositeConverter가 아닌 ClassicConverter를 직접 상속해야 단독 키워드로 동작합니다.
 */
public class MaskingConverter extends ClassicConverter {

    // 이메일: 앞 3글자 노출, 이후 @ 앞까지 마스킹 (예: tes***@gmail.com)
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("([a-zA-Z0-9._%+-]{3})[a-zA-Z0-9._%+-]*(@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");

    // 휴대전화번호: 010-1234-5678 → 010-****-5678
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("\\b(010)-\\d{4}-(\\d{4})\\b");

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (message == null) {
            return "";
        }
        message = EMAIL_PATTERN.matcher(message).replaceAll("$1***$2");
        message = PHONE_PATTERN.matcher(message).replaceAll("$1-****-$2");
        return message;
    }
}
