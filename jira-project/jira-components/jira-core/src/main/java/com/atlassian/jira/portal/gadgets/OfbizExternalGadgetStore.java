package com.atlassian.jira.portal.gadgets;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.atlassian.gadgets.directory.spi.ExternalGadgetSpec;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecId;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.dbc.Assertions;

import org.ofbiz.core.entity.GenericValue;

/**
 * Ofbiz implementation of the ExternalGadgetStore.
 *
 * @since v4.0
 */
public class OfbizExternalGadgetStore implements ExternalGadgetStore
{
    public static final String TABLE = "ExternalGadget";

    private final OfBizDelegator ofBizDelegator;

    public static final class Columns
    {
        public static final String ID = "id";
        public static final String GADGET_XML = "gadgetXml";
    }

    public OfbizExternalGadgetStore(final OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    public Set<ExternalGadgetSpec> getAllGadgetSpecUris()
    {
        final Set<ExternalGadgetSpec> gadgetUris = new HashSet<ExternalGadgetSpec>();
        final List<GenericValue> list = ofBizDelegator.findAll(TABLE);
        for (final GenericValue genericValue : list)
        {
            final String uriString = genericValue.getString(Columns.GADGET_XML);
            try
            {
                gadgetUris.add(new ExternalGadgetSpec(ExternalGadgetSpecId.valueOf(genericValue.getLong(Columns.ID).toString()), new URI(uriString)));
            }
            catch (final URISyntaxException e)
            {
                throw new DataAccessException(
                    "External gadget with id '" + genericValue.getLong(Columns.ID) + "' does not have a valid URI: '" + uriString + "'.", e);
            }
        }

        return Collections.unmodifiableSet(gadgetUris);
    }

    public ExternalGadgetSpec addGadgetSpecUri(final URI uri)
    {
        Assertions.notNull("uri", uri);

        if (containsSpecUri(uri))
        {
            throw new IllegalStateException("External Gadget Store already contains URI '" + uri + "'");
        }
        final GenericValue specGV = EntityUtils.createValue(TABLE, MapBuilder.<String, Object>build(Columns.GADGET_XML, uri.toASCIIString()));
        return new ExternalGadgetSpec(ExternalGadgetSpecId.valueOf(specGV.getLong(Columns.ID).toString()),
            URI.create(specGV.getString(Columns.GADGET_XML)));
    }

    public void removeGadgetSpecUri(final ExternalGadgetSpecId id)
    {
        Assertions.notNull("id", id);

        ofBizDelegator.removeByAnd(TABLE, MapBuilder.newBuilder(Columns.ID, Long.parseLong(id.value())).toMap());
    }

    public boolean containsSpecUri(final URI uri)
    {
        Assertions.notNull("uri", uri);

        final List<GenericValue> list = ofBizDelegator.findByLike(TABLE, MapBuilder.<String, Object> newBuilder().add(Columns.GADGET_XML,
            uri.toASCIIString()).toMap(), Collections.<String> emptyList());
        return (list != null) && !list.isEmpty();
    }
}
