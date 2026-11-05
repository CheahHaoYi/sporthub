package com.sporthub.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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

    @Test(groups = "unit")
    public void validationFailureReturnsBadRequestAndFieldDetails() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        MethodArgumentNotValidException exception = Mockito.mock(MethodArgumentNotValidException.class);
        when(request.getRequestURI()).thenReturn("/api/v1/register");
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(exception.getMessage()).thenReturn("private validation detail");
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(
                new FieldError("registrationRequest", "email", "must be a valid email")));

        ResponseEntity<ApiErrorResponse> response = handler.validation(exception, request);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(response.getBody().status(), 400);
        assertEquals(response.getBody().code(), "VALIDATION_ERROR");
        assertEquals(response.getBody().path(), "/api/v1/register");
        assertEquals(response.getBody().fieldErrors().size(), 1);
        assertEquals(response.getBody().fieldErrors().get(0).field(), "email");
        assertEquals(response.getBody().fieldErrors().get(0).message(), "must be a valid email");
        assertFalse(response.getBody().message().contains("private validation detail"));
    }
}
