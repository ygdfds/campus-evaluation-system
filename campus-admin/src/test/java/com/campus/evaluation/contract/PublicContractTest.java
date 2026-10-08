package com.campus.evaluation.contract;

import com.campus.evaluation.common.core.domain.PageQuery;
import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.web.GlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PublicContractTest {

    private static final Pattern TIMESTAMP =
            Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");

    @Test
    void responseUsesFrozenTimestampAndErrorKeyShape() {
        R<String> success = R.ok("value");
        R<Void> failure = R.fail(409, "duplicate", "DUPLICATE_SUBMISSION");

        assertThat(success.getCode()).isEqualTo(200);
        assertThat(success.getTimestamp()).matches(TIMESTAMP);
        assertThat(success.getErrKey()).isNull();
        assertThat(failure.getCode()).isEqualTo(409);
        assertThat(failure.getTimestamp()).matches(TIMESTAMP);
        assertThat(failure.getErrKey()).isEqualTo("DUPLICATE_SUBMISSION");
    }

    @Test
    void pageQueryPrefersPageAndDefaultsToOneAndTwenty() {
        PageQuery defaults = new PageQuery();
        PageQuery legacy = new PageQuery();
        legacy.setPageNum(3);
        PageQuery both = new PageQuery();
        both.setPageNum(3);
        both.setPage(2);

        assertThat(defaults.getPage()).isEqualTo(1);
        assertThat(defaults.getPageSize()).isEqualTo(20);
        assertThat(legacy.getPage()).isEqualTo(3);
        assertThat(both.getPage()).isEqualTo(2);
    }

    @Test
    void pageResultKeepsPageAndLegacyPageNumInSync() {
        PageResult<String> result = new PageResult<>(41, List.of("record"), 3, 20);

        assertThat(result.getPage()).isEqualTo(3);
        assertThat(result.getPageNum()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(3);

        result.setPage(4);
        assertThat(result.getPageNum()).isEqualTo(4);
        result.setPageNum(5);
        assertThat(result.getPage()).isEqualTo(5);
    }

    @Test
    void businessExceptionCarriesStableErrorKey() {
        BusinessException exception = new BusinessException(409, "duplicate", "DUPLICATE_SUBMISSION");

        assertThat(exception.getCode()).isEqualTo(409);
        assertThat(exception.getMessage()).isEqualTo("duplicate");
        assertThat(exception.getErrKey()).isEqualTo("DUPLICATE_SUBMISSION");
    }

    @Test
    void globalHandlerMapsBusinessCodeToHttpStatus() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");

        var response = handler.handleBusinessException(
                new BusinessException(422, "invalid state", "STATE_INVALID"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(422);
        assertThat(response.getBody().getErrKey()).isEqualTo("STATE_INVALID");
    }

    @Test
    void localDateTimeContractPatternIsDocumentedByResponseTimestamp() {
        assertThat(LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm:ss"))).matches(TIMESTAMP);
    }
}
