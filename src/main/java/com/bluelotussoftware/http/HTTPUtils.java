package com.bluelotussoftware.http;

import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author John Yeary <jyeary@bluelotussoftware.com>
 * @version 1.0
 */
public class HTTPUtils {

    private static volatile HTTPUtils instance = null;
    private final ThreadLocalRequest threadLocalRequest = new ThreadLocalRequest();
    private final ThreadLocalResponse threadLocalResponse = new ThreadLocalResponse();

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

    public void setHTTP(final HttpServletRequest request, final HttpServletResponse response) {
        threadLocalRequest.setRequest(request);
        threadLocalResponse.setResponse(response);
    }

    public HttpServletRequest getCurrentRequest() {
        return threadLocalRequest.getRequest();
    }

    public HttpServletResponse getCurrentResponse() {
        return threadLocalResponse.getResponse();
    }

    public HttpSession changeSessionIdentifier() {
        return changeSessionIdentifier(getCurrentRequest());
    }

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

    public void clearHTTP() {
        threadLocalRequest.set(null);
        threadLocalResponse.set(null);
    }

    public void addHeader(String name, String value) {
        addHeader(getCurrentResponse(), name, value);
    }

    public void addHeader(HttpServletResponse response, String name, String value) {
        response.addHeader(name, value);
    }

}
