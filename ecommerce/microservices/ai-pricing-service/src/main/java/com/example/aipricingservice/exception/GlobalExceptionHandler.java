package com.example.aipricingservice.exception;

import com.example.aipricingservice.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 🛡️ Catch-All: Handles unexpected code crashes (like NullPointerExceptions)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        log.error("🛑 [CRITICAL ERROR]: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("AI Pricing Engine encountered a problem: " + ex.getMessage()));
    }

    /**
     * 🛡️ Bad Input: Handles logic errors (like a negative ID or invalid demand score)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        log.warn("⚠️ [BAD REQUEST]: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Invalid Input: " + ex.getMessage()));
    }

    /**
     * 🛡️ URL Error: Handles cases where someone sends text instead of a number
     * Example: Calling /calculate/abc instead of /calculate/2
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        // ✅ FIX: Guard against getRequiredType() returning null to prevent NPE
        String requiredType = (ex.getRequiredType() != null)
                ? ex.getRequiredType().getSimpleName()
                : "unknown";

        String error = String.format("The parameter '%s' should be of type %s",
                ex.getName(), requiredType);

        log.warn("⚠️ [TYPE MISMATCH]: {}", error);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(error));
    }
}