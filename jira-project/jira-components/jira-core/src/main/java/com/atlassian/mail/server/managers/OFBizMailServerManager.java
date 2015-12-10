package com.atlassian.mail.server.managers;


import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.core.user.preferences.DefaultPreferences;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.mail.MailConstants;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.MailServerConfigurationHandler;
import com.atlassian.mail.server.PopMailServer;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.PopMailServerImpl;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.opensymphony.util.TextUtils;

import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

import static com.atlassian.mail.MailConstants.DEFAULT_POP_PORT;
import static com.atlassian.mail.MailConstants.DEFAULT_POP_PROTOCOL;
import static com.atlassian.mail.MailConstants.DEFAULT_SMTP_PORT;
import static com.atlassian.mail.MailConstants.DEFAULT_SMTP_PROTOCOL;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

/**
 * This was taken from atlassian-mail and placed into its now rightful home of JIRA.
 *
 * @since 4.3 
 */
public class OFBizMailServerManager extends AbstractMailServerManager
{
    private DefaultPreferences defaultPreferences = new DefaultPreferences();

    public MailServer getMailServer(Long id) throws MailException
    {
        try
        {
            GenericValue gv = ComponentAccessor.getOfBizDelegator().findByPrimaryKey("MailServer", UtilMisc.toMap("id", id));
            if (gv == null)
                return null;
            else
                return constructMailServer(gv);
        }
        catch (DataAccessException e)
        {
            throw new MailException(e);
        }
    }

    public MailServer getMailServer(String name) throws MailException
    {
        try
        {
            GenericValue gv = EntityUtil.getOnly(ComponentAccessor.getOfBizDelegator().findByAnd("MailServer", UtilMisc.toMap("name", name)));
            if (gv == null)
                return null;
            else
                return constructMailServer(gv);
        }
        catch (DataAccessException e)
        {
            throw new MailException(e);
        }
    }

    private List<MailServer> getAllServers()
    {
        try
        {
            List<GenericValue> mailServerGVs = ComponentAccessor.getOfBizDelegator().findAll("MailServer", UtilMisc.toList("id asc"));

            return newArrayList(Iterables.transform(mailServerGVs, new Function<GenericValue, MailServer>()
            {
                @Override
                public MailServer apply(GenericValue from)
                {
                    return constructMailServer(from);

                }
            }));
        }
        catch (DataAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    public List<String> getServerNames() throws MailException
    {
        return newArrayList(Iterables.transform(getAllServers(), new Function<MailServer, String>()
        {
            @Override
            public String apply(MailServer from)
            {
                return from.getName();
            }
        }));
    }

    public List<SMTPMailServer> getSmtpMailServers()
    {
        return getMailServersByType(SMTPMailServer.class);
    }

    public List<PopMailServer> getPopMailServers()
    {
        return getMailServersByType(PopMailServer.class);
    }

    public Long create(MailServer mailServer) throws MailException
    {
        try
        {
            GenericValue storedMailServer = EntityUtils.createValue("MailServer", getMapFromColumns(mailServer));
            return storedMailServer.getLong("id");
        }
        catch (DataAccessException e)
        {
            throw new MailException(e);
        }
    }

    public void update(MailServer mailServer) throws MailException
    {
        try
        {
            GenericValue storedMailServer = getMailServerGV(mailServer.getId());
            storedMailServer.setFields(getMapFromColumns(mailServer));
            ComponentAccessor.getOfBizDelegator().store(storedMailServer);
        }
        catch (DataAccessException e)
        {
            throw new MailException(e);
        }
    }

    public void delete(Long mailServerId) throws MailException
    {
        try
        {
            GenericValue storedMailServer = getMailServerGV(mailServerId);
            ComponentAccessor.getOfBizDelegator().removeValue(storedMailServer);
        }
        catch (DataAccessException e)
        {
            throw new MailException(e);
        }
    }


    @Nullable
    public SMTPMailServer getDefaultSMTPMailServer()
    {
        SMTPMailServer smtps;
        try
        {
            smtps = (SMTPMailServer) getMailServer(defaultPreferences.getString("DefaultSmtpServer"));
            if (smtps != null)
                return smtps;
        }
        catch (Exception ignored){}

        final List<SMTPMailServer> smtpMailServers = getSmtpMailServers();
        if (smtpMailServers == null || smtpMailServers.size() == 0)
        {
            return null;
        }
        return smtpMailServers.get(0);
    }

    @Nullable
    public PopMailServer getDefaultPopMailServer()
    {
        PopMailServer pops;
        try
        {
            pops = (PopMailServer) getMailServer(defaultPreferences.getString("DefaultPopServer"));
            if (pops != null)
                return pops;
        }
        catch (Exception ignored){}

        final List<PopMailServer> popMailServers = getPopMailServers();
        if (popMailServers == null || popMailServers.size() == 0)
        {
            return null;
        }
        return popMailServers.get(0);

    }

    protected <T extends MailServer> List<T> getMailServersByType(final Class<T> serverType)
    {
        return newArrayList(filter(getAllServers(), serverType));
    }

    protected GenericValue getMailServerGV(Long id) throws MailException
    {
        try
        {
            return ComponentAccessor.getOfBizDelegator().findByPrimaryKey("MailServer", UtilMisc.toMap("id", id));
        }
        catch (DataAccessException e)
        {
            throw new MailException(e);
        }
    }

    protected MailServer constructMailServer(GenericValue gv)
    {
        final MailServer mailServer = constructMailServerImpl(gv);
        final MailServerConfigurationHandler mailServerConfigurationHandler = getMailServerConfigurationHandler();
        if (mailServerConfigurationHandler != null) {
            mailServerConfigurationHandler.configureMailServer(mailServer);
        }
        return mailServer;
    }

    private MailServer constructMailServerImpl(GenericValue gv)
    {

        final String serverType = gv.getString("type");
        String port = gv.getString("smtpPort");
        // this is to fix upgrade errors when smtp port is null in the DB
        if (port == null)
        {
             port = getDefaultPort(serverType);
        }
        // this is for upgrade errors where protocol is null
        String protocol = gv.getString("protocol");
        MailProtocol mailProtocol = (protocol != null) ? MailProtocol.getMailProtocol(protocol) : getDefaultProtocol(serverType);
        // this is for upgrade errors where timeout is null
        long timeout = gv.getLong("timeout") == null ? MailConstants.DEFAULT_TIMEOUT : gv.getLong("timeout");
        {

        }
        if (SERVER_TYPES[0].equals(serverType))
        {
            return new PopMailServerImpl(gv.getLong("id"), gv.getString("name"), gv.getString("description"),
                    mailProtocol, gv.getString("servername"), port, gv.getString("username"), gv.getString("password"), timeout,
                    gv.getString("socksHost"), gv.getString("socksPort"));
        }
        else if (SERVER_TYPES[1].equals(serverType))
        {

            boolean isTlsRequired = Boolean.valueOf(gv.getString("istlsrequired"));
            if (TextUtils.stringSet(gv.getString("servername")))
            {
                return new SMTPMailServerImpl(gv.getLong("id"), gv.getString("name"), gv.getString("description"),
                        gv.getString("from"), gv.getString("prefix"), false, mailProtocol, gv.getString("servername"),
                        port, isTlsRequired, gv.getString("username"), gv.getString("password"), timeout,
                        gv.getString("socksHost"), gv.getString("socksPort"));
            }
            else
            {
                return new SMTPMailServerImpl(gv.getLong("id"), gv.getString("name"), gv.getString("description"),
                        gv.getString("from"), gv.getString("prefix"), true, mailProtocol, gv.getString("jndilocation"),
                        port, isTlsRequired, gv.getString("username"), gv.getString("password"), timeout,
                        gv.getString("socksHost"), gv.getString("socksPort"));
            }
        }
        else
            return null;
    }

    private MailProtocol getDefaultProtocol(String serverType)
    {
        return SERVER_TYPES[0].equals(serverType) ? DEFAULT_POP_PROTOCOL: DEFAULT_SMTP_PROTOCOL;
    }

    private String getDefaultPort(String serverType)
    {
        return SERVER_TYPES[0].equals(serverType) ? DEFAULT_POP_PORT: DEFAULT_SMTP_PORT;

    }

    protected Map<String, Object> getMapFromColumns(MailServer mailServer) throws MailException
    {
        Map<String, Object> columns = Maps.newHashMap();
        columns.put("name", mailServer.getName());
        columns.put("description", mailServer.getDescription());
        columns.put("username", mailServer.getUsername());
        columns.put("password", mailServer.getPassword());
        columns.put("type", mailServer.getType());
        columns.put("servername", mailServer.getHostname());
        columns.put("smtpPort", mailServer.getPort());
        columns.put("protocol",mailServer.getMailProtocol().getProtocol());
        columns.put("timeout", mailServer.getTimeout());
        columns.put("socksHost", mailServer.getSocksHost());
        columns.put("socksPort", mailServer.getSocksPort());

        if (SERVER_TYPES[0].equals(mailServer.getType()))
        {
            //Do nothing different
        }
        else if (SERVER_TYPES[1].equals(mailServer.getType()))
        {
            SMTPMailServer smtp = (SMTPMailServer) mailServer;
            columns.put("from", smtp.getDefaultFrom());
            columns.put("prefix", smtp.getPrefix());
            columns.put("istlsrequired",Boolean.toString(smtp.isTlsRequired()));

            if (smtp.isSessionServer())
            {
                columns.put("jndilocation", smtp.getJndiLocation());
            }
            else
            {
                columns.put("servername", smtp.getHostname());
            }
        }
        else
        {
            throw new MailException("The Type of Mail Server is not recognised");
        }
        return columns;
    }
}
