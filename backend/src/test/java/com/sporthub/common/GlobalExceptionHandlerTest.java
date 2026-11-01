package com.sporthub.common;

import jakarta.servlet.http.HttpServletRequest;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** Focused unit checks for the API's shared error response formatting. */
public class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test(groups = "unit")
    public void unexpectedFailureReturnsGeneric500AndRequestPath() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/checkout");

        ResponseEntity<ApiErrorResponse> response = handler.unexpected(
                new IllegalStateException("private database detail"), request);

        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals(response.getBody().status(), 500);
        assertEquals(response.getBody().code(), "INTERNAL_ERROR");
        assertEquals(response.getBody().path(), "/api/v1/checkout");
        assertTrue(response.getBody().timestamp() != null);
        assertTrue(!response.getBody().message().contains("private database detail"));
    }
}
