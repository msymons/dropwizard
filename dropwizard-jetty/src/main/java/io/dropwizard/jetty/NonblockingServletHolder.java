package io.dropwizard.jetty;

import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * A {@link ServletHolder} subclass which removes the synchronization around servlet initialization
 * by requiring a pre-initialized servlet holder.
 *
 * @deprecated If necessary, use {@link ServletHolder} or {@link org.eclipse.jetty.servlet.FilterHolder} directly.
 * This class will be removed in Dropwizard 2.1.0.
 */
@SuppressWarnings("unused")
@Deprecated
public class NonblockingServletHolder extends ServletHolder {
    private final Servlet servlet;

    public NonblockingServletHolder(Servlet servlet) {
        super(servlet);
        setInitOrder(1);
        this.servlet = servlet;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public synchronized Servlet getServlet() throws ServletException {
        return servlet;
    }

    @Override
    public void handle(Request baseRequest,
                       ServletRequest request,
                       ServletResponse response) throws ServletException, IOException {
        final boolean asyncSupported = baseRequest.isAsyncSupported();
        if (!isAsyncSupported()) {
            baseRequest.setAsyncSupported(false, null);
        }
        try {
            servlet.service(request, response);
        } catch (EofException ignored) {
            // Want to ignore the EofException as this signifies the client has disconnected or the
            // response has already been written. The problem with using an ExceptionMapper is that
            // we don't actually want to write a response given that the connection has already been
            // closed
        } finally {
            baseRequest.setAsyncSupported(asyncSupported, null);
        }
    }
}
