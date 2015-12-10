package com.atlassian.jira.config.managedconfiguration;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.MapBuilder;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The default implementation of {@link ManagedConfigurationItemStore}. Could perhaps use a caching layer.
 *
 * @since v5.2
 */
public class DefaultManagedConfigurationItemStore implements ManagedConfigurationItemStore
{
    public static final String ENTITY_NAME = "ManagedConfigurationItem";
    private static final Logger log = Logger.getLogger(DefaultManagedConfigurationItemStore.class);
    
    private final OfBizDelegator ofBizDelegator;

    public DefaultManagedConfigurationItemStore(OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    @Override
    @Nonnull
    public ManagedConfigurationItem updateManagedConfigurationItem(@Nonnull ManagedConfigurationItem item)
    {
        notNull("item", item);

        ManagedConfigurationItem existing = getManagedConfigurationItem(item.getItemId(), item.getItemType());
        GenericValue registered;
        if (existing != null)
        {
            // registration already exists -- need to update the existing one
            item = new ManagedConfigurationItemBuilder()
                    .setId(existing.getId())
                    .setItemId(item.getItemId())
                    .setItemType(item.getItemType())
                    .setManaged(item.isManaged())
                    .setConfigurationItemAccessLevel(item.getConfigurationItemAccessLevel())
                    .setSource(item.getSourceId())
                    .setDescriptionI18nKey(item.getDescriptionI18nKey())
                    .build();
            registered = ofBizDelegator.makeValue(ENTITY_NAME, toGV(item));
            ofBizDelegator.store(registered);
        }
        else
        {
            registered = ofBizDelegator.createValue(ENTITY_NAME, toGV(item));
        }

        ManagedConfigurationItem managedConfigurationItem = fromGV(registered);

        log.info("Managed Configuration Item registered: " + managedConfigurationItem.toString());

        return managedConfigurationItem;
    }

    @Override
    public boolean deleteManagedConfigurationItem(@Nonnull ManagedConfigurationItem item)
    {
        notNull("item", item);

        int count = ofBizDelegator.removeByAnd(ENTITY_NAME, MapBuilder.build("itemId", item.getItemId(), "itemType", item.getItemType().toString()));
        if (count > 0)
        {
            log.info(String.format("Managed item %s successfully unregistered", item));
            return true;
        }
        else
        {
            log.info(String.format("Managed item %s was not unregistered", item));
            return false;
        }
    }

    @Override
    @Nullable
    public ManagedConfigurationItem getManagedConfigurationItem(@Nonnull String itemId, @Nonnull ManagedConfigurationItemType type)
    {
        notNull("itemId", itemId);
        notNull("type", type);

        List<GenericValue> existing = ofBizDelegator.findByAnd(ENTITY_NAME, MapBuilder.build("itemId", itemId, "itemType", type.toString()));
        if (!existing.isEmpty())
        {
            return fromGV(existing.iterator().next());
        }
        return null;
    }

    @Override
    @Nonnull
    public Collection<ManagedConfigurationItem> getManagedConfigurationItems(@Nonnull ManagedConfigurationItemType type)
    {
        notNull("type", type);

        List<GenericValue> existing = ofBizDelegator.findByAnd(ENTITY_NAME, MapBuilder.build("itemType", type.toString()));
        return CollectionUtil.transform(existing, new Function<GenericValue, ManagedConfigurationItem>()
        {
            @Override
            public ManagedConfigurationItem get(GenericValue input)
            {
                return fromGV(input);
            }
        });
    }

    static Map<String, Object> toGV(ManagedConfigurationItem item)
    {
        MapBuilder<String, Object> builder = MapBuilder.newBuilder();
        if (item.getId() != null)
        {
            builder.add("id", item.getId());
        }
        builder
                .add("itemId", item.getItemId())
                .add("itemType", item.getItemType().toString())
                .add("managed", item.isManaged() ? "true" : null)
                .add("accessLevel", item.getConfigurationItemAccessLevel().toString())
                .add("source", item.getSourceId())
                .add("descriptionKey", item.getDescriptionI18nKey());

        return builder.toMap();
    }

    static ManagedConfigurationItem fromGV(GenericValue gv)
    {
        return new ManagedConfigurationItemBuilder()
                .setId(gv.getLong("id"))
                .setItemId(gv.getString("itemId"))
                .setItemType(ManagedConfigurationItemType.valueOf(gv.getString("itemType")))
                .setManaged("true".equals(gv.getString("managed")))
                .setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.valueOf(gv.getString("accessLevel")))
                .setSource(gv.getString("source"))
                .setDescriptionI18nKey(gv.getString("descriptionKey"))
                .build();
    }
}
