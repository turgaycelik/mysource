package com.atlassian.jira.functest.unittests.mocks;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.BindException;
import java.util.HashMap;
import java.util.Map;

/**
 * A mini web server based on Jetty that servers name content.
 * <p/>
 * There is only one instance of this object which can be called via the getInstance() static method
 *
 * @since v3.13
 */
public class MockWebServer
{
    private static final Logger log = Logger.getLogger(MockWebServer.class);
    
    private final int port;
    private final Map<String, String> contentMap = new HashMap<String, String>();
    private Server server;

    private static final int HTTP_PORT = 50789;

    private static class LazyHolder
    {
        private static final MockWebServer WEB_SERVER = new MockWebServer(HTTP_PORT);
    }

    public static MockWebServer getInstance()
    {
        MockWebServer mockWebServer = LazyHolder.WEB_SERVER;
        return startWebServer(mockWebServer);
    }

    private static MockWebServer startWebServer(MockWebServer mockWebServer)
    {
        if (!mockWebServer.isStarted())
        {
            try
            {
                mockWebServer.start();
            }
            catch(BindException be)
            {
                final int newPort = mockWebServer.getPort() + 10;
                log.warn("Trying to start mockWebServer failed on port '" + mockWebServer.getPort() +
                          "'.  Retrying on '" + newPort + "'.");
                //Looks like the port is already in use.  Let's try to increment the port by 10
                //and try that.
                return startWebServer(new MockWebServer(newPort));
            }
            catch(Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        return mockWebServer;
    }

    MockWebServer(int port)
    {
        this.port = port;

        server = new Server();
        Connector connector = new SocketConnector();
        connector.setPort(port);
        server.setConnectors(new Connector[] { connector });

        Handler handler = new MockHandler();
        server.setHandler(handler);
        server.setStopAtShutdown(true);
    }

    public int getPort()
    {
        return port;
    }

    public String getHostAndPort()
    {
        return "http://localhost:" + port;
    }

    public void addPage(String pageUrl, String pageContent)
    {
        contentMap.put(pageUrl, pageContent);
    }

    public void start() throws Exception
    {
        server.start();
    }

    public void stop() throws Exception
    {
        server.stop();
        while (!server.isStopped())
        {
            Thread.sleep(100);
        }
    }

    public boolean isRunning()
    {
        return server.isRunning();
    }

    public boolean isStarted()
    {
        return server.isStarted();
    }

    public boolean isStarting()
    {
        return server.isStarting();
    }

    public boolean isStopping()
    {
        return server.isStopping();
    }

    public boolean isStopped()
    {
        return server.isStopped();
    }

    public class MockHandler extends AbstractHandler
    {
        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
                throws IOException, ServletException
        {
            Request base_request = (request instanceof Request) ? (Request) request : HttpConnection.getCurrentConnection().getRequest();
            base_request.setHandled(true);

            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);

            String content = contentMap.get(target);
            PrintWriter pw = response.getWriter();
            pw.println(content);
        }
    }

    protected void finalize() throws Throwable
    {
        getInstance().stop();
        super.finalize();
    }
}
