package com.wx.invoicefx.google;

import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.util.Throwables;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This receiver has been extracted from the Google API and slightly adapted
 * in order to retrieve the activation url
 * Created on 16/07/2015
 */
class CustomCodeReceiver implements VerificationCodeReceiver {

    private static final String CALLBACK_PATH = "/Callback";

    /**
     * Server or {@code null} before {@link #getRedirectUri()}.
     */
    private Server server;

    /**
     * Verification code or {@code null} for none.
     */
    String code;

    /**
     * Error code or {@code null} for none.
     */
    String error;

    /**
     * Lock on the code and error.
     */
    final Lock lock = new ReentrantLock();

    /**
     * Condition for receiving an authorization response.
     */
    final Condition gotAuthorizationResponse = lock.newCondition();

    /**
     * Port to use or {@code -1} to select an unused port in {@link #getRedirectUri()}.
     */
    private int port;

    /**
     * Host name to use.
     */
    private final String host;

    /**
     * Constructor that starts the server on {@code "localhost"} selects an unused port.
     * <p>
     */
    public CustomCodeReceiver() {
        this("localhost", -1);
    }

    /**
     * Constructor.
     *
     * @param host Host name to use
     * @param port Port to use or {@code -1} to select an unused port
     */
    CustomCodeReceiver(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String getRedirectUri() throws IOException {
        if (port == -1) {
            port = getUnusedPort();
        }
        server = new Server(port);
        for (Connector c : server.getConnectors()) {
            c.setHost(host);
        }
        server.addHandler(new CallbackHandler());
        try {
            server.start();
        } catch (Exception e) {
            Throwables.propagateIfPossible(e);
            throw new IOException(e);
        }
        return "http://" + host + ":" + port + CALLBACK_PATH;
    }

    @Override
    public String waitForCode() throws IOException {
        lock.lock();
        try {
            while (code == null && error == null) {
                try {
                    gotAuthorizationResponse.await();
                } catch (InterruptedException e) {
                    error = "Cancelled";
                }
            }
            if (error != null) {
                throw new IOException("User authorization failed (" + error + ")");
            }
            return code;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void stop() throws IOException {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                Throwables.propagateIfPossible(e);
                throw new IOException(e);
            }
            server = null;
        }
    }

    /**
     * Returns the host name to use.
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the port to use or {@code -1} to select an unused port in {@link #getRedirectUri()}.
     */
    public int getPort() {
        return port;
    }

    private static int getUnusedPort() throws IOException {
        Socket s = new Socket();
        s.bind(null);
        try {
            return s.getLocalPort();
        } finally {
            s.close();
        }
    }

    /**
     * Jetty handler that takes the verifier token passed over from the OAuth provider and stashes it where {@link
     * #waitForCode} will find it.
     */
    class CallbackHandler extends AbstractHandler {

        @Override
        public void handle(
                String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
                throws IOException {
            if (!CALLBACK_PATH.equals(target)) {
                return;
            }
            writeLandingHtml(response);
            response.flushBuffer();
            ((Request) request).setHandled(true);
            lock.lock();
            try {
                error = request.getParameter("error");
                code = request.getParameter("code");
                gotAuthorizationResponse.signal();
            } finally {
                lock.unlock();
            }
        }

        private void writeLandingHtml(HttpServletResponse response) throws IOException {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/html");

            PrintWriter doc = response.getWriter();
            doc.println("<html>");
            doc.println("<head><title>OAuth 2.0 Authentication Token Received</title></head>");
            doc.println("<body>");
            doc.println("Received verification code. You may now close this window...");
            doc.println("</body>");
            doc.println("</HTML>");
            doc.flush();
        }
    }
}
