package be.recettopia.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Forces UTF-8 for every request and response of the application.
 *
 * <p>Why: browsers send form data in UTF-8, but Tomcat reads request bodies in
 * ISO-8859-1 by default. Without this filter, "crème brûlée" typed in a form
 * would arrive in the servlet as "crÃ¨me brÃ»lÃ©e".</p>
 *
 * <p>A filter runs BEFORE every servlet. urlPatterns = "/*" means it applies to
 * every URL of the application, so no servlet has to deal with encoding itself.</p>
 */
@WebFilter(filterName = "EncodingFilter", urlPatterns = "/*")
public class EncodingFilter implements Filter {

    /** Encoding used for every request and response. */
    private static final String ENCODING = StandardCharsets.UTF_8.name();

    /**
     * Called by Tomcat for each request, before the servlet.
     *
     * @param request  the incoming HTTP request
     * @param response the HTTP response that will be sent back
     * @param chain    the rest of the processing (other filters, then the servlet)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Read form parameters as UTF-8.
        // Must be done BEFORE anything reads a parameter, which is why it is in a filter.
        request.setCharacterEncoding(ENCODING);

        // Write the HTML pages sent back to the browser in UTF-8
        response.setCharacterEncoding(ENCODING);

        // Pass the request on to the next filter, or to the servlet
        chain.doFilter(request, response);
    }
}