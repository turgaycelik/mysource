package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.util.dbc.Null;
import com.atlassian.security.auth.trustedapps.Application;
import com.atlassian.security.auth.trustedapps.RequestConditions;
import com.atlassian.velocity.htmlsafe.HtmlSafe;
import org.ofbiz.core.entity.GenericValue;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.PublicKey;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder pattern class for constructing and transforming our various data and business objects.
 *
 * @since v3.12
 */
public class TrustedApplicationBuilder
{
    private long id;
    private String applicationId;
    private long timeout;
    private String publicKey;
    private String name;
    private String ipMatch;
    private String urlMatch;
    private String createdBy;
    private String updatedBy;
    private Date created;
    private Date updated;

    TrustedApplicationBuilder set(final GenericValue gv)
    {
        Null.not("genericValue", gv);

        if (!TrustedApplicationStore.ENTITY_NAME.equals(gv.getEntityName()))
        {
            throw new IllegalArgumentException("Cannot build a TrustedApplication from a " + gv.getEntityName());
        }
        final Long idLong = gv.getLong(TrustedApplicationStore.Fields.ID);
        setId((idLong == null) ? 0 : idLong);
        setApplicationId(gv.getString(TrustedApplicationStore.Fields.APPLICATION_ID));
        setCreatedBy(gv.getString(TrustedApplicationStore.Fields.CREATED_BY));
        setCreated(gv.getTimestamp(TrustedApplicationStore.Fields.CREATED));
        setUpdatedBy(gv.getString(TrustedApplicationStore.Fields.UPDATED_BY));
        setUpdated(gv.getTimestamp(TrustedApplicationStore.Fields.UPDATED));
        setName(gv.getString(TrustedApplicationStore.Fields.NAME));
        setPublicKey(gv.getString(TrustedApplicationStore.Fields.PUBLIC_KEY));
        setTimeout(gv.getLong(TrustedApplicationStore.Fields.TIMEOUT));
        setIpMatch(gv.getString(TrustedApplicationStore.Fields.IP_MATCH));
        setUrlMatch(gv.getString(TrustedApplicationStore.Fields.URL_MATCH));

        return this;
    }

    TrustedApplicationBuilder set(final TrustedApplicationData data)
    {
        Null.not("data", data);
        setId(data.getId());
        setApplicationId(data.getApplicationId());
        setTimeout(data.getTimeout());
        setPublicKey(data.getPublicKey());
        setName(data.getName());
        setIpMatch(data.getIpMatch());
        setUrlMatch(data.getUrlMatch());
        setCreated(data.getCreated());
        setUpdated(data.getUpdated());
        return this;
    }

    public TrustedApplicationBuilder set(final TrustedApplicationInfo info)
    {
        Null.not("info", info);
        setId(info.getNumericId());
        setApplicationId(info.getID());
        setTimeout(info.getTimeout());
        setPublicKey(info.getPublicKey());
        setName(info.getName());
        setIpMatch(info.getIpMatch());
        setUrlMatch(info.getUrlMatch());
        return this;
    }

    public TrustedApplicationBuilder set(Application app)
    {
        Null.not("app", app);
        setApplicationId(app.getID());
        setPublicKey(app.getPublicKey());
        return this;
    }

    public TrustedApplicationBuilder set(RequestConditions requestConditions)
    {
        Null.not("requestConditions", requestConditions);
        setTimeout(requestConditions.getCertificateTimeout());
        setUrlMatch(requestConditions.getURLPatterns());
        setIpMatch(requestConditions.getIPPatterns());

        return this;
    }

    public long getId()
    {
        return id;
    }

    public TrustedApplicationBuilder setId(final long id)
    {
        this.id = id;
        return this;
    }

    public String getApplicationId()
    {
        return applicationId;
    }

    public TrustedApplicationBuilder setApplicationId(final String applicationId)
    {
        Null.not("applicationId", applicationId);
        this.applicationId = applicationId;
        return this;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public TrustedApplicationBuilder setTimeout(final long timeout)
    {
        this.timeout = timeout;
        return this;
    }

    public String getPublicKey()
    {
        return publicKey;
    }

    public TrustedApplicationBuilder setPublicKey(final PublicKey publicKey)
    {
        Null.not("publicKey", publicKey);
        this.publicKey = KeyFactory.encode(publicKey);
        return this;
    }

    public TrustedApplicationBuilder setPublicKey(final String publicKey)
    {
        Null.not("publicKey", publicKey);
        this.publicKey = publicKey;
        return this;
    }

    public String getName()
    {
        return name;
    }

    public TrustedApplicationBuilder setName(final String name)
    {
        Null.not("name", name);
        this.name = name;
        return this;
    }

    public String getIpMatch()
    {
        return ipMatch;
    }

    public TrustedApplicationBuilder setIpMatch(final String ipMatch)
    {
        this.ipMatch = TrustedApplicationUtil.canonicalize(ipMatch);
        return this;
    }

    public TrustedApplicationBuilder setIpMatch(final Iterable<String> ipMatch)
    {
        this.ipMatch = TrustedApplicationUtil.canonicalize(ipMatch);
        return this;
    }

    public String getUrlMatch()
    {
        return urlMatch;
    }

    public TrustedApplicationBuilder setUrlMatch(final String urlMatch)
    {
        this.urlMatch = TrustedApplicationUtil.canonicalize(urlMatch);
        return this;
    }

    private TrustedApplicationBuilder setUrlMatch(final Iterable<String> urlPatterns)
    {
        this.urlMatch = TrustedApplicationUtil.canonicalize(urlPatterns);
        return this;
    }

    TrustedApplicationBuilder setCreatedBy(final String createdBy)
    {
        Null.not("createdBy", createdBy);
        this.createdBy = createdBy;
        return this;
    }

    TrustedApplicationBuilder setUpdatedBy(final String updatedBy)
    {
        Null.not("updatedBy", updatedBy);
        this.updatedBy = updatedBy;
        return this;
    }

    public TrustedApplicationBuilder setCreated(final Date created)
    {
        Null.not("created", created);
        this.created = new Date(created.getTime());
        return this;
    }

    TrustedApplicationBuilder setUpdated(final Date updated)
    {
        Null.not("updated", updated);
        this.updated = new Date(updated.getTime());
        return this;
    }

    public TrustedApplicationBuilder setCreated(final AuditLog created)
    {
        Null.not("created", created);
        setCreatedBy(created.getWho());
        setCreated(created.getWhen());
        return this;
    }

    TrustedApplicationBuilder setUpdated(final AuditLog updated)
    {
        Null.not("updated", updated);
        setUpdatedBy(updated.getWho());
        setUpdated(updated.getWhen());
        return this;
    }

    public TrustedApplicationInfo toInfo()
    {
        try
        {
            return new TrustedApplicationInfo(id, applicationId, name, timeout, ipMatch, urlMatch, KeyFactory.getPublicKey(publicKey));
        }
        catch (final IllegalArgumentException e)
        {
            final IllegalStateException exception = new IllegalStateException(e.toString());
            exception.initCause(e);
            throw exception;
        }
    }

    public SimpleTrustedApplication toSimple()
    {
        return new Simple(this);
    }

    public TrustedApplicationData toData()
    {
        try
        {
            return new TrustedApplicationData(id, applicationId, name, publicKey, timeout, new AuditLog(createdBy, created), new AuditLog(updatedBy,
                updated), ipMatch, urlMatch);
        }
        catch (final IllegalArgumentException e)
        {
            final IllegalStateException exception = new IllegalStateException(e.toString());
            exception.initCause(e);
            throw exception;
        }
    }

    Map<String, Object> toMap()
    {
        final Map<String, Object> map = new HashMap<String, Object>();
        if (id > 0)
        {
            map.put(TrustedApplicationStore.Fields.ID, id);
        }
        map.put(TrustedApplicationStore.Fields.APPLICATION_ID, applicationId);
        map.put(TrustedApplicationStore.Fields.NAME, name);
        map.put(TrustedApplicationStore.Fields.PUBLIC_KEY, publicKey);
        map.put(TrustedApplicationStore.Fields.TIMEOUT, timeout);
        map.put(TrustedApplicationStore.Fields.CREATED, new Timestamp(created.getTime()));
        map.put(TrustedApplicationStore.Fields.CREATED_BY, createdBy);
        map.put(TrustedApplicationStore.Fields.UPDATED, new Timestamp(updated.getTime()));
        map.put(TrustedApplicationStore.Fields.UPDATED_BY, updatedBy);
        map.put(TrustedApplicationStore.Fields.IP_MATCH, ipMatch);
        map.put(TrustedApplicationStore.Fields.URL_MATCH, urlMatch);
        return map;
    }

    /**
     * Transforms the data held in the builder into a query string representation for use with URLs.
     * Values of individual fields are URL encoded.
     * Note: the length of the returned String may exceed the maximum length for a GET URL.
     *
     * @return the data as a query string
     */
    @HtmlSafe
    public String toQueryString()
    {
        class QueryBuilder
        {
            StringBuffer buffer = new StringBuffer();

            QueryBuilder add(final String name, final Object value)
            {
                if (value != null)
                {
                    if (buffer.length() > 0)
                    {
                        buffer.append("&");
                    }
                    try
                    {
                        buffer.append(name).append("=").append(URLEncoder.encode(String.valueOf(value), "UTF8"));
                    }
                    ///CLOVER:OFF
                    catch (final UnsupportedEncodingException e)
                    {
                        throw new RuntimeException(e);
                    }
                    ///CLOVER:ON
                }

                return this;
            }
        }
        final QueryBuilder result = new QueryBuilder();
        result.add("id", getId());
        result.add("applicationId", getApplicationId());
        result.add("name", getName());
        result.add("publicKey", getPublicKey());
        result.add("timeout", getTimeout());
        result.add("ipMatch", getIpMatch());
        result.add("urlMatch", getUrlMatch());

        return result.buffer.toString();
    }

    static final class Simple implements SimpleTrustedApplication
    {
        final long id;
        final String applicationId;
        final long timeout;
        final String publicKey;
        final String name;
        final String ipMatch;
        final String urlMatch;

        Simple(final TrustedApplicationBuilder builder)
        {
            id = builder.id;
            applicationId = builder.applicationId;
            timeout = builder.timeout;
            publicKey = builder.publicKey;
            name = builder.name;
            ipMatch = builder.ipMatch;
            urlMatch = builder.urlMatch;
        }

        public long getId()
        {
            return id;
        }

        public String getApplicationId()
        {
            return applicationId;
        }

        public long getTimeout()
        {
            return timeout;
        }

        public String getPublicKey()
        {
            return publicKey;
        }

        public String getName()
        {
            return name;
        }

        public String getIpMatch()
        {
            return ipMatch;
        }

        public String getUrlMatch()
        {
            return urlMatch;
        }
    }
}