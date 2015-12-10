package com.atlassian.jira.functest.unittests.config.sharing;

import com.atlassian.jira.functest.config.ConfigXmlUtils;
import com.atlassian.jira.functest.config.sharing.ConfigSharedEntityId;
import com.atlassian.jira.functest.config.sharing.FavouritesCleaner;
import junit.framework.TestCase;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test for {@link com.atlassian.jira.functest.config.sharing.FavouritesCleaner}.
 *
 * @since v4.2
 */
public class TestFavouritesCleaner extends TestCase
{
    private static final String ELEMENT_FAVOURITE = "FavouriteAssociations";

    public void testClean() throws Exception
    {
        MockFavourite one = new MockFavourite().setId(1L).setOwner("brenden").setEntityType("type1").setEntityId(1L);
        MockFavourite two = new MockFavourite().setId(2L).setOwner("brenden").setEntityType("type1").setEntityId(2L).setSequence(2);
        MockFavourite three = new MockFavourite().setId(3L).setOwner("brenden").setEntityType("type1").setEntityId(3L).setSequence(1);
        MockFavourite four = new MockFavourite().setId(4L).setOwner("jack").setEntityType("type1").setEntityId(2L).setSequence(1);
        MockFavourite five = new MockFavourite().setId(5L).setOwner("jack").setEntityType("type1").setEntityId(4575758L);
        MockFavourite six = new MockFavourite().setId(6L).setOwner("brenden").setEntityType("type2").setEntityId(2L);
        MockFavourite seven = new MockFavourite().setId(7L).setOwner("brenden").setEntityType("type3").setEntityId(2L);
        MockFavourite eight = new MockFavourite().setId(8L).setOwner("brenden").setEntityType("type1").setEntityId(48484L).setSequence(3939393);
        MockFavourite nine = new MockFavourite().setId(9L).setOwner("other").setEntityType("type574").setEntityId(4588484L);
        MockFavourite ten = new MockFavourite().setId(9L).setOwner("other").setEntityType("type1").setEntityId(2L);

        Document doc = createDocument(one, two, three, four, five, six, seven, eight, nine, ten);
        FavouritesCleaner cleaner = new FavouritesCleaner(doc);

        //Let change the sequence order for ("brenden", "type1")
        one.setSequence(2);
        three.setSequence(0);
        eight.setSequence(1);

        //Lets change the sequence for ("jack", "type1")
        five.setSequence(0);

        assertTrue(cleaner.clean(new ConfigSharedEntityId(2L, "type1")));
        assertEquals(Arrays.asList(one, three, five, six, seven, eight, nine), parseDocument(doc));
        assertFalse(cleaner.clean(new ConfigSharedEntityId(2L, "type1")));
    }

    public void testCleanBadSharedEntity() throws Exception
    {
        MockFavourite one = new MockFavourite().setId(1L).setOwner("brenden").setEntityType("type1").setEntityId(1L);
        MockFavourite two = new MockFavourite().setId(2L).setOwner("brenden").setEntityType("type1").setEntityId(2L).setSequence(2);

        Document doc = createDocument(one, two);
        FavouritesCleaner cleaner = new FavouritesCleaner(doc);

        assertFalse(cleaner.clean(new ConfigSharedEntityId(2L, null)));
        assertEquals(Arrays.asList(one, two), parseDocument(doc));

        assertFalse(cleaner.clean(new ConfigSharedEntityId(null, "type1")));
        assertEquals(Arrays.asList(one, two), parseDocument(doc));

        assertFalse(cleaner.clean(new ConfigSharedEntityId(null, null)));
        assertEquals(Arrays.asList(one, two), parseDocument(doc));
    }

    public void testCleanBadFavourite() throws Exception
    {
        MockFavourite one = new MockFavourite().setId(1L).setEntityType("type1").setEntityId(2L);
        MockFavourite two = new MockFavourite().setId(2L).setEntityType("type1").setEntityId(1L);

        Document doc = createDocument(one, two);
        FavouritesCleaner cleaner = new FavouritesCleaner(doc);

        //Normally deleting this ID would reorder the favourites of "type1" associated with the favourite owner. In this
        //case there is no owner so no reorder should be taking place.
        assertTrue(cleaner.clean(new ConfigSharedEntityId(2L, "type1")));
        assertEquals(Arrays.asList(two), parseDocument(doc));
    }

    private Document createDocument(MockFavourite... permissions)
    {
        final Document document = DocumentFactory.getInstance().createDocument();
        final Element element = document.addElement("entity-engine-xml");
        for (MockFavourite permission : permissions)
        {
            permission.save(element.addElement(ELEMENT_FAVOURITE));
        }
        return document;
    }

    private List<MockFavourite> parseDocument(Document document)
    {
        final List<Element> elementList = ConfigXmlUtils.getTopElementsByName(document, ELEMENT_FAVOURITE);
        final List<MockFavourite> perms = new ArrayList<MockFavourite>(elementList.size());
        for (Element element : elementList)
        {
            perms.add(new MockFavourite(element));
        }
        return perms;
    }

    private static class MockFavourite
    {
        private static final String ATTRIBUTE_ID = "id";
        private static final String ATTRIBUTE_ENTITY_ID = "entityId";
        private static final String ATTRIBUTE_ENTITY_TYPE = "entityType";
        private static final String ATTRIBUTE_SEQUENCE = "sequence";
        private static final String ATTRIBUTE_OWNER = "username";

        private Long id;
        private Long entityId;
        private String entityType;
        private Integer sequence;
        private String owner;

        private MockFavourite()
        {
        }

        private MockFavourite(Element element)
        {
            this.id = ConfigXmlUtils.getLongValue(element, ATTRIBUTE_ID);
            this.entityId = ConfigXmlUtils.getLongValue(element, ATTRIBUTE_ENTITY_ID);
            this.entityType = ConfigXmlUtils.getTextValue(element, ATTRIBUTE_ENTITY_TYPE);
            this.sequence = ConfigXmlUtils.getIntegerValue(element, ATTRIBUTE_SEQUENCE);
            this.owner = ConfigXmlUtils.getTextValue(element, ATTRIBUTE_OWNER);
        }

        private void save(Element element)
        {
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ID, id);
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ENTITY_ID, entityId);
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ENTITY_TYPE, entityType);
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_SEQUENCE, sequence);
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_OWNER, owner);
        }

        private MockFavourite setEntityType(final String entityType)
        {
            this.entityType = entityType;
            return this;
        }

        private String getEntityType()
        {
            return entityType;
        }

        private Long getEntityId()
        {
            return entityId;
        }

        private MockFavourite setEntityId(Long entityId)
        {
            this.entityId = entityId;
            return this;
        }

        private Long getId()
        {
            return id;
        }

        private MockFavourite setId(Long id)
        {
            this.id = id;
            return this;
        }

        private Integer getSequence()
        {
            return this.sequence;
        }

        private MockFavourite setSequence(Integer sequence)
        {
            this.sequence = sequence;
            return this;
        }

        private String getOwner()
        {
            return owner;
        }

        private MockFavourite setOwner(final String owner)
        {
            this.owner = owner;
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

            final MockFavourite that = (MockFavourite) o;

            if (entityId != null ? !entityId.equals(that.entityId) : that.entityId != null)
            {
                return false;
            }
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
            result = 31 * result + (entityId != null ? entityId.hashCode() : 0);
            result = 31 * result + (entityType != null ? entityType.hashCode() : 0);
            result = 31 * result + (sequence != null ? sequence.hashCode() : 0);
            result = 31 * result + (owner != null ? owner.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
