package com.atlassian.jira.webtests.ztests.email;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.atlassian.jira.functest.framework.admin.MailServerAdministration;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import com.atlassian.jira.webtests.JIRAServerSetup;

import org.apache.commons.io.IOUtils;

@WebTest ({ Category.FUNC_TEST, Category.EMAIL })
public class TestSendTestEmail extends EmailFuncTestCase
{
    public static final int PORT = 25 + JIRAServerSetup.PORT_OFFSET;
    private SmtpServerNotRespondingToQuit testMail = new SmtpServerNotRespondingToQuit();

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
        testMail.startServer(PORT);
    }

    @Override
    public void tearDownTest()
    {
        testMail.stop();
        super.tearDownTest();
    }

    public void testJiraConsidersTestMailSuccessfulWhenTheMailServerDoesNotRespondToQuitCommand() throws InterruptedException
    {
        assertSendingMailIsEnabled();
        log("Setting SMTP server to 'localhost:" + PORT + "'");
        backdoor.mailServers().addSmtpServer("admin@example.com", "PRE", PORT);

        final MailServerAdministration.SendTestEmail sendTestEmail = administration.mailServers().Smtp().goTo().sendTestEmail();

        sendTestEmail.send();
        sendTestEmail.assertMessageSentInformationDisplayed();
    }

    class SmtpServerNotRespondingToQuit
    {
        ServerSocket serverSocket;
        Thread serverThread;

        public void stop()
        {
            closeQuietly(serverSocket);
            serverThread.interrupt();
        }

        // because in java 1.6 ServerSocket didn't implement Closeable
        private void closeQuietly(ServerSocket serverSocket)
        {
            try
            {
                if (serverSocket != null)
                {
                    serverSocket.close();
                }
            }
            catch (IOException ioe)
            {
                // ignore
            }
        }

        public void startServer(final int port)
        {
            serverThread = new Thread()
            {
                public void run()
                {
                    log("SmtpServerNotRespondingToQuit starting up");
                    try
                    {
                        serverSocket = new ServerSocket(port);
                    }
                    catch (IOException ex)
                    {
                        throw new RuntimeException(ex);
                    }

                    boolean data = false;
                    InputStream inputStream = null;
                    OutputStream outputStream = null;

                    Socket socket = null;
                    try
                    {
                        socket = serverSocket.accept();
                        inputStream = socket.getInputStream();
                        outputStream = socket.getOutputStream();
                        PrintWriter pw = new PrintWriter(outputStream);
                        pw.println("220 SmtpServerNotRespondingToQuit Simple Mail Transfer Service Ready");
                        pw.flush();

                        BufferedReader bufferedReader =
                                new BufferedReader(new InputStreamReader(inputStream));

                        while (true)
                        {
                            String line = bufferedReader.readLine();

                            if (line == null)
                            {
                                break;
                            }
                            if (line.startsWith("EHLO"))
                            {
                                pw.println("250-SmtpServerNotRespondingToQuit greets localhost");
                                pw.println("250 HELP");
                            }
                            else if (line.startsWith("DATA"))
                            {
                                pw.println("354 Start mail input; end with <CRLF>.<CRLF>");
                                data = true;
                            }
                            else if (line.trim().equals("."))
                            {
                                data = false;
                                pw.println("250 OK");
                            }
                            else if (line.startsWith("QUIT"))
                            {
                                // (troll) do not respond with 221 fakemailserver Service closing transmission channel
                            }
                            else if (!data)
                            {
                                pw.println("250 OK");
                            }
                            pw.flush();
                        }
                    }
                    catch (Throwable ex)
                    {
                        log(ex);
                    }
                    finally
                    {
                        IOUtils.closeQuietly(outputStream);
                        IOUtils.closeQuietly(inputStream);
                        IOUtils.closeQuietly(socket);
                        closeQuietly(serverSocket);
                        log("SmtpServerNotRespondingToQuit shutting down");
                    }
                }
            };

            serverThread.start();
        }
    }

}
