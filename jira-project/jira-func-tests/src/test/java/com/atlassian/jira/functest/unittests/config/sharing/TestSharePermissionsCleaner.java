package com.atlassian.jira.functest.unittests.config.sharing;

import com.atlassian.jira.functest.config.ConfigXmlUtils;
import com.atlassian.jira.functest.config.sharing.ConfigSharedEntityId;
import com.atlassian.jira.functest.config.sharing.SharePermissionsCleaner;
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
 * Test {@link com.atlassian.jira.functest.config.sharing.SharePermissionsCleaner}.
 *
 * @since v4.2
 */
public class TestSharePermissionsCleaner extends TestCase
{
    private static final String ELEMENT_SHARE = "SharePermissions";

    public void testClean() throws Exception
    {
        MockSharePermission perm1 = new MockSharePermission().setId(17L).setEntityType("type1").setEntityId(10L);
        MockSharePermission perm2 = new MockSharePermission().setId(18L).setEntityType("type1").setEntityId(11L);
        MockSharePermission perm3 = new MockSharePermission().setId(19L).setEntityType("type2").setEntityId(12L);
        MockSharePermission perm4 = new MockSharePermission().setId(20L).setEntityType("type1").setEntityId(10L);

        Document doc = createDocument(perm1, perm2, perm3, perm4);

        final SharePermissionsCleaner cleaner = new SharePermissionsCleaner(doc);
        assertTrue(cleaner.clean(new ConfigSharedEntityId(perm1.getEntityId(), perm1.getEntityType())));
        assertEquals(Arrays.asList(perm2, perm3), parseDocument(doc));
        assertFalse(cleaner.clean(new ConfigSharedEntityId(perm1.getEntityId(), perm1.getEntityType())));
        assertEquals(Arrays.asList(perm2, perm3), parseDocument(doc));
        assertTrue(cleaner.clean(new ConfigSharedEntityId(perm3.getEntityId(), perm3.getEntityType())));
        assertEquals(Arrays.asList(perm2), parseDocument(doc));
    }

    public void testCleanConfigSharedEntity() throws Exception
    {
        MockSharePermission perm1 = new MockSharePermission().setId(17L).setEntityType("type1").setEntityId(10L);
        MockSharePermission perm2 = new MockSharePermission().setId(18L).setEntityType("type1").setEntityId(11L);
        MockSharePermission perm3 = new MockSharePermission().setId(19L).setEntityType("type2").setEntityId(12L);
        MockSharePermission perm4 = new MockSharePermission().setId(20L).setEntityType("type1").setEntityId(10L);

        Document doc = createDocument(perm1, perm2, perm3, perm4);

        final SharePermissionsCleaner cleaner = new SharePermissionsCleaner(doc);
        assertFalse(cleaner.clean(new ConfigSharedEntityId(perm1.getEntityId(), null)));
        assertEquals(Arrays.asList(perm1, perm2, perm3, perm4), parseDocument(doc));

        assertFalse(cleaner.clean(new ConfigSharedEntityId(null, perm1.getEntityType())));
        assertEquals(Arrays.asList(perm1, perm2, perm3, perm4), parseDocument(doc));

        assertFalse(cleaner.clean(new ConfigSharedEntityId(null, null)));
        assertEquals(Arrays.asList(perm1, perm2, perm3, perm4), parseDocument(doc));
    }

    private Document createDocument(MockSharePermission...permissions)
    {
        final Document document = DocumentFactory.getInstance().createDocument();
        final Element element = document.addElement("entity-engine-xml");
        for (MockSharePermission permission : permissions)
        {
            permission.save(element.addElement(ELEMENT_SHARE));
        }
        return document;
    }

    private List<MockSharePermission> parseDocument(Document document)
    {
        final List<Element> elementList = ConfigXmlUtils.getTopElementsByName(document, ELEMENT_SHARE);
        final List<MockSharePermission> perms = new ArrayList<MockSharePermission>(elementList.size());
        for (Element element : elementList)
        {
            perms.add(new MockSharePermission(element));
        }
        return perms;
    }

    private static class MockSharePermission
    {
        private static final String ATTRIBUTE_ID = "id";
        private static final String ATTRIBUTE_ENTITY_ID = "entityId";
        private static final String ATTRIBUTE_ENTITY_TYPE = "entityType";

        private Long id;
        private Long entityId;
        private String entityType;

        private MockSharePermission()
        {
        }

        private MockSharePermission(Element element)
        {
            this.id = ConfigXmlUtils.getLongValue(element, ATTRIBUTE_ID);
            this.entityId = ConfigXmlUtils.getLongValue(element, ATTRIBUTE_ENTITY_ID);
            this.entityType = ConfigXmlUtils.getTextValue(element, ATTRIBUTE_ENTITY_TYPE);
        }

        private void save(Element element)
        {
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ID, id);
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ENTITY_ID, entityId);
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ENTITY_TYPE, entityType);
        }

        private MockSharePermission setEntityType(final String entityType)
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

        private MockSharePermission setEntityId(Long entityId)
        {
            this.entityId = entityId;
            return this;
        }

        private Long getId()
        {
            return id;
        }

        private MockSharePermission setId(Long id)
        {
            this.id = id;
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

            final MockSharePermission that = (MockSharePermission) o;

            if (id != null ? !id.equals(that.id) : that.id != null)
            {
                return false;
            }
            if (entityType != null ? !entityType.equals(that.entityType) : that.entityType != null)
            {
                return false;
            }
            if (entityId != null ? !entityId.equals(that.entityId) : that.entityId != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = entityId != null ? entityId.hashCode() : 0;
            result = 31 * result + (entityType != null ? entityType.hashCode() : 0);
            result = 31 * result + (id != null ? id.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
