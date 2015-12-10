package com.atlassian.jira.service.services.mail;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.service.util.handler.MessageHandlerExecutionMonitor;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Queue;
import javax.annotation.Nullable;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * Internal-use class only
 *
 * @since v5.0
 */
@Internal
class ErrorAccumulatingMessageHandlerExecutionMonitor implements MessageHandlerExecutionMonitor
{
    private final MessageHandlerExecutionMonitor delgate;
    private volatile boolean hasErrors = false;
    private final Queue<String> errors = Lists.newLinkedList();
    private String lastReportedExceptionStackTrace;
    private boolean markedForDeletion = false;
    private String markedForDeletionMessage;
    private boolean markedToForward = false;

    public ErrorAccumulatingMessageHandlerExecutionMonitor(MessageHandlerExecutionMonitor delgate)
    {
        this.delgate = delgate;
    }

    @Override
    public void setNumMessages(int count)
    {
        delgate.setNumMessages(count);
    }

    @Override
    public void messageRejected(Message message, String reason)
    {
        delgate.messageRejected(message, reason);
    }

    @Override
    public void nextMessage(Message message)
    {
        delgate.nextMessage(message);
    }

    // Cluster-safe because it's guarding internal state
    @Override
    public synchronized void markMessageForDeletion(final String reason)
    {
        delgate.markMessageForDeletion(reason);
        markedForDeletion = true;
        markedForDeletionMessage = reason;
    }

    // Cluster-safe because it's guarding internal state
    public synchronized boolean isMessagedMarkedForDeletion()
    {
        return markedForDeletion;
    }

    @Override
    public void error(String error)
    {
        delgate.error(error);
        addErrorImpl(error);

    }

    @Override
    public void warning(String warning)
    {
        delgate.warning(warning);
        addErrorImpl(warning);
    }

    @Override
    public void warning(String warning, @Nullable Throwable e)
    {
        delgate.warning(warning, e);
        addErrorImpl(warning);
    }

    @Override
    public void info(String info)
    {
        delgate.info(info);
    }

    @Override
    public void info(String info, @Nullable Throwable e)
    {
        delgate.info(info, e);
    }

    // Cluster-safe because it's guarding internal state
    private synchronized void addErrorImpl(String error)
    {
        hasErrors = true;
        if (error != null)
        {
            if (errors.size() > 5)
            {
                errors.remove();
            }
            errors.add(error);
        }
    }


    @Override
    public void error(String error, @Nullable Throwable e)
    {
        delgate.error(error, e);
        addErrorImpl(error);
        if (e != null)
        {
            // Cluster-safe because it's guarding internal state
            synchronized (this) {
                lastReportedExceptionStackTrace = getStackTraceAsString(e);
            }
        }
        else
        {
            // Cluster-safe because it's guarding internal state
            synchronized (this) {
                lastReportedExceptionStackTrace = null;
            }
        }
    }

    // This is marked if a forward message should be sent
    public void markMessageForForwarding(boolean markedToForward) {
        this.markedToForward = markedToForward;
    }

    public boolean isMarkedToForward() {
        return this.markedToForward;
    }

    // Cluster-safe because it's guarding internal state
    public synchronized boolean hasErrors()
    {
        return hasErrors;
    }

    // Cluster-safe because it's guarding internal state
    public synchronized String getErrorsAsString()
    {
        String errorString = "";
        if (isMessagedMarkedForDeletion()) {
            errorString = Strings.nullToEmpty(markedForDeletionMessage) + "\n";
        }
        return errorString + StringUtils.join(errors, "\n");
    }

    // Cluster-safe because it's guarding internal state
    public synchronized String getExceptionsAsString()
    {
        return lastReportedExceptionStackTrace;
    }

    String getStackTraceAsString(Throwable e)
    {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter p = new PrintWriter(stringWriter);
        e.printStackTrace(p);
        p.flush();
        p.close();
        return stringWriter.toString();
    }
}
