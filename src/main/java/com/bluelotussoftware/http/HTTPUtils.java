/*
 * Copyright 2014-2016 Blue Lotus Software, LLC..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bluelotussoftware.http;

import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * A collection of HTTP Utilities which provide access to the
 * {@link HttpServletRequest} and {@link HttpServletResponse} objects by using
 * {@link ThreadLocal}.
 *
 * @author John Yeary <jyeary@bluelotussoftware.com>
 * @version 1.1
 */
public class HTTPUtils {

    private static volatile HTTPUtils instance;
    private final ThreadLocalRequest threadLocalRequest = new ThreadLocalRequest();
    private final ThreadLocalResponse threadLocalResponse = new ThreadLocalResponse();

    /**
     * Private constructor.
     */
    private HTTPUtils() {
    }

    /**
     * Singleton method for getting an instance of the {@code HTTPUtils} object.
     *
     * @return the singleton instance {@code HTTPUtils} object, or creates a new
     * instance if required.
     */
    public static HTTPUtils getInstance() {
        HTTPUtils local = HTTPUtils.instance;
        if (local == null) {
            synchronized (HTTPUtils.class) {
                local = HTTPUtils.instance;
                if (local == null) {
                    HTTPUtils.instance = local = new HTTPUtils();
                }
            }
        }
        return local;
    }

    /**
     * Defines the ThreadLocalRequest to store the current request for this
     * thread.
     */
    private final class ThreadLocalRequest extends InheritableThreadLocal<HttpServletRequest> {

        public HttpServletRequest getRequest() {
            return super.get();
        }

        @Override
        public HttpServletRequest initialValue() {
            return null;
        }

        public void setRequest(HttpServletRequest request) {
            super.set(request);
        }
    }

    /**
     * Defines the ThreadLocalResponse to store the current response for this
     * thread.
     */
    private final class ThreadLocalResponse extends InheritableThreadLocal<HttpServletResponse> {

        public HttpServletResponse getResponse() {
            return super.get();
        }

        @Override
        public HttpServletResponse initialValue() {
            return null;
        }

        public void setResponse(HttpServletResponse response) {
            super.set(response);
        }
    }

    /**
     * <p>
     * This method is used to set the {@link ThreadLocal} objects to contain the
     * {@link HttpServletRequest} and {@link HttpServletResponse} objects.</p>
     * <p>
     * <strong>Note:</strong> The objects must be removed at the end of
     * processing by using a {@code finally} block.</p>
     * <p>
     * The typical implementation is to use the following arrangement:</p>
     * <pre>
     * try {
     *     HTTPUtils.getInstance().setHTTP(httpServletRequest, httpServletResponse);
     *     // YOUR CODE HERE
     * } catch (IOException | ServletException e) {
     * } finally {
     *     // This is extremely critical.
     *     HTTPUtils.getInstance().clearHTTP();
     * }
     * </pre>
     *
     *
     * @param request The current {@link HttpServletRequest} object to be set.
     * @param response The current {@link HttpServletResponse} object to be set.
     * @see #clearHTTP()
     *
     */
    public void setHTTP(final HttpServletRequest request, final HttpServletResponse response) {
        threadLocalRequest.setRequest(request);
        threadLocalResponse.setResponse(response);
    }

    /**
     * This is a method to get the current {@link HttpServletRequest} of this
     * thread.
     *
     * @return The current {@link HttpServletRequest}.
     */
    public HttpServletRequest getCurrentRequest() {
        return threadLocalRequest.getRequest();
    }

    /**
     * This is a method to get the current {@link HttpServletResponse} of this
     * thread.
     *
     * @return The current {@link HttpServletResponse}.
     */
    public HttpServletResponse getCurrentResponse() {
        return threadLocalResponse.getResponse();
    }

    /**
     * A convenience method to change the {@link HttpSession} of the current
     * thread local {@link HttpServletRequest}.
     *
     * @return A new {@link HttpSession}.
     * @see #changeSessionIdentifier(javax.servlet.http.HttpServletRequest)
     */
    public HttpSession changeSessionIdentifier() {
        return changeSessionIdentifier(getCurrentRequest());
    }

    /**
     * Creates a new {@link HttpSession} and copies the attributes to the new
     * session.
     *
     * @param request The {@link HttpServletRequest} to change the session on.
     * @return A new {@link HttpSession}.
     */
    public HttpSession changeSessionIdentifier(final HttpServletRequest request) {
        HttpSession oldSession = request.getSession();

        // copy attributes to a temporary map
        Map<String, Object> temp = new ConcurrentHashMap<>();
        Enumeration e = oldSession.getAttributeNames();
        while (e != null && e.hasMoreElements()) {
            String name = (String) e.nextElement();
            Object value = oldSession.getAttribute(name);
            temp.put(name, value);
        }

        int maxInactiveInterval = oldSession.getMaxInactiveInterval();

        oldSession.invalidate();
        HttpSession newSession = request.getSession();

        newSession.setMaxInactiveInterval(maxInactiveInterval);

        // Add old session attributes to new session
        for (Map.Entry<String, Object> entry : temp.entrySet()) {
            newSession.setAttribute(entry.getKey(), entry.getValue());
        }
        return newSession;
    }

    /**
     * Clears the {@link ThreadLocal} objects of the {@link HttpServletRequest}
     * and {@link HttpServletResponse} objects.
     *
     * @see #setHTTP(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    public void clearHTTP() {
        threadLocalRequest.set(null);
        threadLocalResponse.set(null);
    }

    /**
     * Adds a header to the {@link HttpServletResponse} thread local object.
     *
     * @param name The name of the header.
     * @param value The value of the header.
     */
    public void addResponseHeader(final String name, final String value) {
        addResponseHeader(getCurrentResponse(), name, value);
    }

    /**
     * Adds a header to the {@link HttpServletResponse} object.
     *
     * @param response The {@link HttpServletResponse} to use.
     * @param name The name of the header.
     * @param value The value of the header.
     */
    public void addResponseHeader(final HttpServletResponse response, final String name, final String value) {
        response.addHeader(name, value);
    }

}
