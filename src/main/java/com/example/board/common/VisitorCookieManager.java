package com.example.board.common;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.time.Duration;
import java.util.UUID;

@Component
public class VisitorCookieManager {

    public static final String COOKIE_NAME = "board_visitor";
    private static final Duration MAX_AGE = Duration.ofDays(365);

    public String getOrIssue(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, COOKIE_NAME);
        if (cookie != null && isValidUuid(cookie.getValue())) {
            return cookie.getValue();
        }

        String visitorId = UUID.randomUUID().toString();
        ResponseCookie newCookie = ResponseCookie.from(COOKIE_NAME, visitorId)
                .path("/")
                .maxAge(MAX_AGE)
                .httpOnly(true)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, newCookie.toString());
        return visitorId;
    }

    private boolean isValidUuid(String value) {
        try {
            return UUID.fromString(value).toString().equals(value);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
