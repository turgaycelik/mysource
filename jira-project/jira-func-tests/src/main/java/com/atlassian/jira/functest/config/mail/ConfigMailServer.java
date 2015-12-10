package com.atlassian.jira.functest.config.mail;

import com.atlassian.jira.functest.config.ConfigObjectWithId;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents a mail server configuration within JIRA XML backup.
 *
 * <code>
 * &lt;MailServer id="10000" name="SMTP" description="" from="me@example.com" prefix="{jira}" smtpPort="25" type="smtp" servername="smtp@example.com"/&gt;
 * &lt;MailServer id="10001" name="asas" description="asAS" from="bbain@altassan.com" prefix="WHAT" smtpPort="25" type="smtp" jndilocation="rhejw"/&gt;
 * </code>
 *
 * @since v4.0
 */
public class ConfigMailServer implements ConfigObjectWithId
{
    public enum Type
    {
        SMTP, POP;

        public static Type parseString(String str)
        {
            for (Type type : Type.values())
            {
                if (type.name().equalsIgnoreCase(str))
                {
                    return type;
                }
            }
            return null;
        }
    }

    private Long id;
    private String name;
    private String description;
    private String from;
    private String port;
    private Type type;
    private String serverName;
    private String jndiLocation;
    private String userName;
    private String password;
    private String prefix;

    public ConfigMailServer()
    {
    }

    public Long getId()
    {
        return id;
    }

    public ConfigMailServer setId(final Long id)
    {
        this.id = id;
        return this;
    }

    public String getName()
    {
        return name;
    }

    public ConfigMailServer setName(final String name)
    {
        this.name = name;
        return this;
    }

    public String getDescription()
    {
        return description;
    }

    public ConfigMailServer setDescription(final String description)
    {
        this.description = description;
        return this;
    }

    public String getFrom()
    {
        return from;
    }

    public ConfigMailServer setFrom(final String from)
    {
        this.from = from;
        return this;
    }

    public String getPort()
    {
        return port;
    }

    public Integer getPortNumber()
    {
        if (port != null)
        {
            try
            {
                return Integer.parseInt(port);
            }
            catch (NumberFormatException e)
            {
                //ingored.
            }
        }
        return null;
    }

    public ConfigMailServer setPort(final String port)
    {
        this.port = port;
        return this;
    }

    public ConfigMailServer setPort(final Integer port)
    {
        return setPort(port == null ? null : port.toString());
    }

    public Type getType()
    {
        return type;
    }

    public ConfigMailServer setType(final Type type)
    {
        this.type = type;
        return this;
    }

    public ConfigMailServer setType(final String type)
    {
        return setType(Type.parseString(type));
    }

    public String getServerName()
    {
        return serverName;
    }

    public ConfigMailServer setServerName(final String serverName)
    {
        this.serverName = serverName;
        return this;
    }

    public String getJndiLocation()
    {
        return jndiLocation;
    }

    public ConfigMailServer setJndiLocation(final String jndiLocation)
    {
        this.jndiLocation = jndiLocation;
        return this;
    }

    public String getUserName()
    {
        return userName;
    }

    public ConfigMailServer setUserName(final String userName)
    {
        this.userName = userName;
        return this;
    }

    public String getPassword()
    {
        return password;
    }

    public ConfigMailServer setPassword(final String password)
    {
        this.password = password;
        return this;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public ConfigMailServer setPrefix(final String prefix)
    {
        this.prefix = prefix;
        return this;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final ConfigMailServer that = (ConfigMailServer) o;

        if (description != null ? !description.equals(that.description) : that.description != null)
        {
            return false;
        }
        if (from != null ? !from.equals(that.from) : that.from != null)
        {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }
        if (jndiLocation != null ? !jndiLocation.equals(that.jndiLocation) : that.jndiLocation != null)
        {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }
        if (password != null ? !password.equals(that.password) : that.password != null)
        {
            return false;
        }
        if (port != null ? !port.equals(that.port) : that.port != null)
        {
            return false;
        }
        if (prefix != null ? !prefix.equals(that.prefix) : that.prefix != null)
        {
            return false;
        }
        if (serverName != null ? !serverName.equals(that.serverName) : that.serverName != null)
        {
            return false;
        }
        if (type != that.type)
        {
            return false;
        }
        if (userName != null ? !userName.equals(that.userName) : that.userName != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (from != null ? from.hashCode() : 0);
        result = 31 * result + (port != null ? port.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (serverName != null ? serverName.hashCode() : 0);
        result = 31 * result + (jndiLocation != null ? jndiLocation.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
