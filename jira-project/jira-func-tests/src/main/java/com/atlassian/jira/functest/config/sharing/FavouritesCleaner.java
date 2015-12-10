package com.atlassian.jira.functest.config.sharing;

import com.atlassian.jira.functest.config.ConfigXmlUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class that cleans up share permissions associated with a {@link ConfigSharedEntity}.
 *
 * @since v4.2
 */
public class FavouritesCleaner implements ConfigSharedEntityCleaner
{
    private final Document document;

    public FavouritesCleaner(final Document document)
    {
        this.document = document;
    }

    public boolean clean(final ConfigSharedEntity entity)
    {
        final List<FavouriteAssociation> favourites = getFavouritesForEntity(entity);
        if (favourites.isEmpty())
        {
            return false;
        }

        for (FavouriteAssociation favourite : favourites)
        {
            favourite.delete();
            final List<FavouriteAssociation> userFavourites = getUserFavourites(favourite);
            Collections.sort(userFavourites);
            int count = 0;
            for (FavouriteAssociation userFavourite : userFavourites)
            {
                userFavourite.setSequence(count++);
                userFavourite.save();
            }
        }
        return true;
    }

    private List<FavouriteAssociation> getFavouritesForEntity(final ConfigSharedEntity entity)
    {
        if (entity.getId() == null || StringUtils.isBlank(entity.getEntityType()))
        {
            return Collections.emptyList();
        }

        return getFavouritesFromXpath(String.format("/entity-engine-xml/FavouriteAssociations[@entityId='%d' and @entityType='%s']",
                entity.getId(), entity.getEntityType()));
    }

    private List<FavouriteAssociation> getUserFavourites(final FavouriteAssociation association)
    {
        if (StringUtils.isBlank(association.getOwner()) || StringUtils.isBlank(association.getEntityType()))
        {
            return Collections.emptyList();
        }

        return getFavouritesFromXpath(String.format("/entity-engine-xml/FavouriteAssociations[@username='%s' and @entityType='%s']",
                association.getOwner(), association.getEntityType()));
    }

    private List<FavouriteAssociation> getFavouritesFromXpath(final String xpath)
    {
        final List<Element> list = ConfigXmlUtils.getElementsByXpath(document, xpath);
        final List<FavouriteAssociation> associations = new ArrayList<FavouriteAssociation>(list.size());
        for (Element element : list)
        {
            associations.add(new FavouriteAssociation(element));
        }
        return associations;
    }

    private static class FavouriteAssociation implements Comparable<FavouriteAssociation>
    {
        private static final String ATTRIBUTE_ID = "id";
        private static final String ATTRIBUTE_USERNAME = "username";
        private static final String ATTRIBUTE_ENTITY_TYPE = "entityType";
        private static final String ATTRIBUTE_ENTITY_ID = "entityId";
        private static final String ATTRIBUTE_SEQUENCE = "sequence";

        private Long id;
        private String owner;
        private String entityType;
        private Long entityId;
        private Integer sequence;

        private Element element;

        FavouriteAssociation(Element element)
        {
            //<FavouriteAssociations id="10005" username="admin" entityType="SearchRequest" entityId="10005" sequence="0"/>
            this.id = ConfigXmlUtils.getLongValue(element, ATTRIBUTE_ID);
            this.owner = ConfigXmlUtils.getTextValue(element, ATTRIBUTE_USERNAME);
            this.entityType = ConfigXmlUtils.getTextValue(element, ATTRIBUTE_ENTITY_TYPE);
            this.entityId = ConfigXmlUtils.getLongValue(element, ATTRIBUTE_ENTITY_ID);
            this.sequence = ConfigXmlUtils.getIntegerValue(element, ATTRIBUTE_SEQUENCE);

            this.element = element;
        }

        void delete()
        {
            ConfigXmlUtils.removeElement(element);
        }

        void save()
        {
            //<FavouriteAssociations id="10005" username="admin" entityType="SearchRequest" entityId="10005" sequence="0"/>
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ID, id);
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_USERNAME, owner);
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ENTITY_TYPE, entityType);
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ENTITY_ID, entityId);
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_SEQUENCE, sequence);
        }

        Element getElement()
        {
            return element;
        }

        Long getEntityId()
        {
            return entityId;
        }

        FavouriteAssociation setEntityId(final Long entityId)
        {
            this.entityId = entityId;
            return this;
        }

        Long getId()
        {
            return id;
        }

        FavouriteAssociation setId(final Long id)
        {
            this.id = id;
            return this;
        }

        String getOwner()
        {
            return owner;
        }

        FavouriteAssociation setOwner(final String owner)
        {
            this.owner = owner;
            return this;
        }

        String getEntityType()
        {
            return entityType;
        }

        FavouriteAssociation setEntityType(final String entityType)
        {
            this.entityType = entityType;
            return this;
        }

        Integer getSequence()
        {
            return sequence;
        }

        FavouriteAssociation setSequence(final Integer sequence)
        {
            this.sequence = sequence;
            return this;
        }

        public int compareTo(final FavouriteAssociation o)
        {
            if (this.sequence == null)
            {
                if (o.sequence == null)
                {
                    return 0;
                }
                else
                {
                    return 1;
                }
            }
            else if (o.sequence == null)
            {
                return -1;
            }
            else
            {
                return this.sequence.compareTo(o.sequence);
            }
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

            final FavouriteAssociation that = (FavouriteAssociation) o;

            if (entityType != null ? !entityType.equals(that.entityType) : that.entityType != null)
            {
                return false;
            }
            if (id != null ? !id.equals(that.id) : that.id != null)
            {
                return false;
            }
            if (owner != null ? !owner.equals(that.owner) : that.owner != null)
            {
                return false;
            }
            if (sequence != null ? !sequence.equals(that.sequence) : that.sequence != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (owner != null ? owner.hashCode() : 0);
            result = 31 * result + (entityType != null ? entityType.hashCode() : 0);
            result = 31 * result + (sequence != null ? sequence.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
