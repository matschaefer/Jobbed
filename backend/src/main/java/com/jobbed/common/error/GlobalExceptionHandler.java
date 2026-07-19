package com.jobbed.common.error;

import com.jobbed.common.error.exception.ApiException;
import com.jobbed.common.error.exception.RateLimitExceededException;
import com.jobbed.common.logging.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * Zentrale Fehlerbehandlung. Übersetzt Ausnahmen in das einheitliche
 * {@link ApiError}-Format und stellt sicher, dass keine internen Details oder
 * Stacktraces an den Client gelangen.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiError> handleRateLimit(RateLimitExceededException ex,
                                                    HttpServletRequest request) {
        ApiError body = ApiError.of(ex.getErrorCode(), ex.getMessage(), request.getRequestURI(), correlationId());
        return ResponseEntity.status(ex.getErrorCode().status())
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()))
                .body(body);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex, HttpServletRequest request) {
        ErrorCode code = ex.getErrorCode();
        // 4xx sind erwartbare Fachfehler -> als WARN/DEBUG, nicht als ERROR loggen.
        log.debug("Fachfehler {} auf {}: {}", code, request.getRequestURI(), ex.getMessage());
        return build(code, ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex,
                                                       HttpServletRequest request) {
        return build(ErrorCode.ACCESS_DENIED, "Zugriff verweigert.", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest request) {
        List<ApiFieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        ApiError body = ApiError.of(ErrorCode.VALIDATION_ERROR,
                "Die Eingabedaten sind ungültig.", request.getRequestURI(), correlationId(), fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex,
                                                              HttpServletRequest request) {
        List<ApiFieldError> fieldErrors = ex.getConstraintViolations().stream()
                .map(v -> new ApiFieldError(lastPathNode(v.getPropertyPath().toString()), v.getMessage()))
                .toList();
        ApiError body = ApiError.of(ErrorCode.VALIDATION_ERROR,
                "Die Eingabedaten sind ungültig.", request.getRequestURI(), correlationId(), fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, MissingServletRequestPartException.class})
    public ResponseEntity<ApiError> handleMalformed(Exception ex, HttpServletRequest request) {
        return build(ErrorCode.MALFORMED_REQUEST,
                "Die Anfrage konnte nicht verarbeitet werden.", request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handlePayloadTooLarge(MaxUploadSizeExceededException ex,
                                                          HttpServletRequest request) {
        return build(ErrorCode.PAYLOAD_TOO_LARGE,
                "Die hochgeladene Datei ist zu groß.", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex,
                                                     HttpServletRequest request) {
        return build(ErrorCode.RESOURCE_NOT_FOUND,
                "Die angeforderte Ressource wurde nicht gefunden.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        // Nur hier auf ERROR-Level loggen, inkl. Stacktrace – jedoch nie an den Client geben.
        log.error("Unerwarteter Fehler auf {} [correlationId={}]", request.getRequestURI(), correlationId(), ex);
        return build(ErrorCode.INTERNAL_ERROR,
                "Ein unerwarteter Fehler ist aufgetreten.", request);
    }

    private ResponseEntity<ApiError> build(ErrorCode code, String message, HttpServletRequest request) {
        ApiError body = ApiError.of(code, message, request.getRequestURI(), correlationId());
        return ResponseEntity.status(code.status()).body(body);
    }

    private ApiFieldError toFieldError(FieldError fe) {
        return new ApiFieldError(fe.getField(),
                fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Ungültiger Wert.");
    }

    private String lastPathNode(String path) {
        int idx = path.lastIndexOf('.');
        return idx >= 0 ? path.substring(idx + 1) : path;
    }

    private String correlationId() {
        return MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY);
    }
}
