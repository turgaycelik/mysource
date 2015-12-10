/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.service.services.file;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.mail.MailLoggingManager;
import com.atlassian.jira.service.util.handler.MessageHandler;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.atlassian.jira.util.PathUtils;
import com.google.common.collect.Lists;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class FileService extends AbstractMessageHandlingService
{
    private final Logger log = ComponentAccessor.getComponent(MailLoggingManager.class).getIncomingMailChildLogger("fileservice");
    private static final String KEY_DIRECTORY = "directory";
    private File directory = null;

    public static final String MAIL_DIR = PathUtils.joinPaths(JiraHome.IMPORT, "mail");
    public static final String KEY_SUBDIRECTORY = "subdirectory";

    public void init(PropertySet props) throws ObjectConfigurationException
    {
        super.init(props);

        String dir = "";
        if (hasProperty(KEY_DIRECTORY))
        {
            dir = getProperty(KEY_DIRECTORY);
        }
        if (StringUtils.isNotBlank(dir))
        {
            directory = new File(dir);
        }
        else
        {
            if (hasProperty(KEY_SUBDIRECTORY) && StringUtils.isNotBlank(getProperty(KEY_SUBDIRECTORY)))
            {
                try
                {
                    directory =  new File(getJiraHome().getHome(), PathUtils.joinPaths(FileService.MAIL_DIR, getProperty(KEY_SUBDIRECTORY))).getCanonicalFile();
                }
                catch (IOException e)
                {
                    throw new ObjectConfigurationException(e);
                }
            }
            else
            {
                directory = new File(getJiraHome().getHome(), MAIL_DIR);
            }
        }

        if (!directory.isDirectory())
        {
            log.warn("Directory: " + dir + " setup for FileService is not a directory.");
        }
        else if (!directory.canRead())
        {
            log.warn("Directory: " + dir + " setup for FileService does not allow read.");
        }
        else if (!directory.canWrite())
        {
            log.warn("Directory: " + dir + " setup for FileService does not allow write.");
        }
    }

    protected List<File> getFilesOnly(File directory) {
        final List<File> result = Lists.newArrayList();
        final File[] files = directory.listFiles();
        if (files != null) {
            for(File file : files) {
                if (file.isFile()) {
                    result.add(file);
                }
            }
        }
        return result;
    }

    @Override
    protected void runImpl(MessageHandlerContext context)
    {
        if (directory == null)
        {
            context.getMonitor().warning("Directory is not set for FileService");
            return;
        }
        if (!directory.exists())
        {
            context.getMonitor().warning("Directory " + directory.getPath() + " does not exist");
            return;
        }

        log.debug("Getting files in directory: " + directory);

        final List<File> files = getFilesOnly(directory);
        final int filesSize = files.size();

        log.debug(addHandlerInfo("Found " + filesSize + " message(s)"));
        if (!context.isRealRun())
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("Found ");
            sb.append(files.size());
            sb.append(" message(s) in the ");
            sb.append(directory);
            sb.append(" folder.");
            if (filesSize > MAX_READ_MESSAGES_DRY_RUN)
            {
                sb.append(" Only first " + MAX_READ_MESSAGES_DRY_RUN + " messages will be processed in test mode. ");
            }
            context.getMonitor().info(sb.toString());
        }
        context.getMonitor().setNumMessages(filesSize);

        if (filesSize == 0)
        {
            return;
        }
        final MessageHandler handler = getHandler();
        if (handler == null) {
            context.getMonitor().error("Handler for this service has not been instantiated. Check handler configuration and corresponding plugin/module state. Exiting run().");
            return;
        }
        for (int i = 0; i < filesSize; ++i)
        {
            final File file = files.get(i);
            if (!context.isRealRun() && i >= MAX_READ_MESSAGES_DRY_RUN)
            {
                log.debug("In dry-run mode only first " + MAX_READ_MESSAGES_DRY_RUN + " messages are processed. Skipping the rest");
                break;
            }

            // it needs to be a file, and we need to be able to read it and then delete it
            if (file.isFile() && file.canRead() && file.canWrite())
            {
                log.debug("Trying to parse file: " + file.getAbsolutePath());
                FileInputStream fis = null;
                try
                {
                    fis = new FileInputStream(file);
                    final Message message = new MimeMessage(null, fis);
                    final String msgId = message.getHeader("Message-ID") != null ? message.getHeader("Message-ID")[0] : "null";
                    final boolean delete = handler.handleMessage(message, context);
                    fis.close();
                    fis = null;

                    if (delete)
                    {
                        if (context.isRealRun()) {
                            if (!file.delete())
                            {
                                context.getMonitor().warning("Unable to delete file '" + file + "'.");
                            }
                            else
                            {
                                log.info("Deleted file: " + file.getAbsolutePath());
                            }
                        } else {
                            context.getMonitor().info("Deleting Message '" + message.getSubject() + "'");
                            log.debug("Deleting Message: " + msgId + " (skipped due to dry-run mode)");
                        }
                    }
                }
                catch (FileNotFoundException e)
                {
                    // this shouldn't happen
                    context.getMonitor().error("File not found when it should be, are two FileServices running?", e);
                }
                catch (MessagingException e)
                {
                    context.getMonitor().error("A messaging exception occurred in the FileService.", e);
                }
                catch (Exception unexpected)
                {
                    context.getMonitor().error("Unexpected exception in the FileService", unexpected);
                }
                finally
                {
                    if (fis != null)
                    {
                        try
                        {
                            fis.close();
                        }
                        catch (IOException ignored)
                        {
                        }
                    }
                }
            }
        }
    }

    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return getObjectConfiguration("FILESERVICE", "services/com/atlassian/jira/service/services/file/fileservice.xml", null);
    }

    JiraHome getJiraHome()
    {
        return ComponentAccessor.getComponentOfType(JiraHome.class);
    }

    @Override
    protected Logger getLogger()
    {
        return log;
    }

    protected String addHandlerInfo(String msg)
    {
        return getName() + "[" + directory + "]: " + msg;
    }
}
