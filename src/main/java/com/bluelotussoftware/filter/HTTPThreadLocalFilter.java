package com.bluelotussoftware.filter;

import com.bluelotussoftware.http.HTTPUtils;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author John Yeary <jyeary@bluelotussoftware.com>
 * @version 1.0
 */
@WebFilter(filterName = "HTTPThreadLocalFilter", urlPatterns = {"/*"})
public class HTTPThreadLocalFilter implements Filter {

    private FilterConfig filterConfig = null;

    /**
     * Default Constructor
     */
    public HTTPThreadLocalFilter() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            getFilterConfig().getServletContext().log("Binding Request and Response to ThreadLocal");
            HTTPUtils.getInstance().setHTTP(httpServletRequest, httpServletResponse);
            chain.doFilter(httpServletRequest, httpServletResponse);
        } catch (IOException | ServletException t) {
            getFilterConfig().getServletContext().log("Filter Processing Exception", t);
        } finally {
            getFilterConfig().getServletContext().log("Clearing Request and Response from ThreadLocal");
            HTTPUtils.getInstance().clearHTTP();
        }

    }

    /**
     * Getter for {@link FilterConfig}.
     *
     * @return the current {@link FilterConfig}.
     */
    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

    /**
     * Setter for {@link FilterConfig}.
     *
     * @param filterConfig Sets the {@link FilterConfig}.
     */
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /**
     * {@inheritDoc}
     * <p>
     * NO-OP
     * </p>
     */
    @Override
    public void destroy() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

}