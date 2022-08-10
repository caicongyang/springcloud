package com.caicongyang.cloud.app.conf;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ThreadLocalFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        String branch = httpRequest.getHeader("branch");
        if(StringUtils.isNotBlank(branch)) {
            RequestContextHolder.put("branch", branch);
        }
        filterChain.doFilter(httpRequest,httpResponse);
    }

    @Override
    public void destroy() {
        RequestContextHolder.clean();
        Filter.super.destroy();
    }
}
