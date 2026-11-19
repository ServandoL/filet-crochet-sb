package com.filet_crochet.filet_crochet.config;

import java.io.IOException;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * The browser sends the Access cookie with any request to the hostname, so a page on another
 * site could post a form here. Browsers label each request's origin in Sec-Fetch-Site; writes
 * must come from this origin. Unlike a JSON-only rule, this lets multipart uploads through.
 * Off in local, where the dev server is a different origin.
 */
@Component
@Profile("!local")
public class SameSiteWritesFilter extends OncePerRequestFilter {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    // "none" is a request the person started themselves, such as typing the URL.
    private static final Set<String> ALLOWED_SITES = Set.of("same-origin", "none");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String site = request.getHeader("Sec-Fetch-Site");
        if (!SAFE_METHODS.contains(request.getMethod()) && site != null && !ALLOWED_SITES.contains(site)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("text/plain");
            response.getWriter().write("Cross-site writes are not allowed.");
            return;
        }
        chain.doFilter(request, response);
    }
}
