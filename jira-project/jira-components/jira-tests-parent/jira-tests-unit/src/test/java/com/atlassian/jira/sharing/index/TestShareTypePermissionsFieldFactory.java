package com.atlassian.jira.sharing.index;

import java.io.StringReader;
import java.util.Collection;

import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import com.atlassian.jira.sharing.type.GlobalShareType;
import com.atlassian.jira.sharing.type.ShareQueryFactory;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.user.MockApplicationUser;

import org.apache.lucene.document.Field;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * A test case for ShareTypePermissionsFieldFactory
 *
 * @since v3.13
 */
public class TestShareTypePermissionsFieldFactory extends MockControllerTestCase
{
    private static final String THIS_IS_THE_RESULT = "ThisIsTheResult";

    /**
     * This always returns null.  Its only do as part of the interface implementation
     */
    @Test
    public void test_getFieldName()
    {
        final ShareTypePermissionsFieldFactory fieldFactory = new ShareTypePermissionsFieldFactory(null);
        assertNull(fieldFactory.getFieldName());
    }

    @Test
    public void test_getField_PrivatePermission()
    {
        final PortalPage portalPage = PortalPage.id(123L).name("name").description("desc").owner(new MockApplicationUser("ownerName")).
                favouriteCount(5L).layout(Layout.AA).version(0L).permissions(SharedEntity.SharePermissions.PRIVATE).build();

        final ShareTypePermissionsFieldFactory fieldFactory = mockController.instantiate(ShareTypePermissionsFieldFactory.class);
        final Collection<Field> fields = fieldFactory.getField(portalPage);
        assertNotNull(fields);
        assertEquals(1, fields.size());
        final Field field = fields.iterator().next();
        assertEquals("owner", field.name());
    }

    @Test
    public void test_getField_GlobalPermission()
    {
        final PortalPage portalPage = PortalPage.id(123L).name("name").description("desc").owner(new MockApplicationUser("ownerName")).
                favouriteCount(5L).layout(Layout.AA).version(0L).permissions(SharedEntity.SharePermissions.GLOBAL).build();

        final ShareType.Name name = GlobalShareType.TYPE;
        final Field expectedField = new Field(THIS_IS_THE_RESULT, new StringReader(""));

        @SuppressWarnings("unchecked")
        final ShareQueryFactory<ShareTypeSearchParameter> shareQueryFactory = mockController.getMock(ShareQueryFactory.class);
        shareQueryFactory.getField(portalPage, new SharePermissionImpl(name, null, null));
        mockController.setReturnValue(expectedField);

        final ShareType shareType = mockController.getMock(ShareType.class);
        shareType.getQueryFactory();
        mockController.setReturnValue(shareQueryFactory);

        final ShareTypeFactory shareTypeFactory = mockController.getMock(ShareTypeFactory.class);
        shareTypeFactory.getShareType(name);
        mockController.setReturnValue(shareType);

        final ShareTypePermissionsFieldFactory fieldFactory = mockController.instantiate(ShareTypePermissionsFieldFactory.class);
        final Collection<Field> fields = fieldFactory.getField(portalPage);
        assertNotNull(fields);
        assertEquals(1, fields.size());
        final Field field = fields.iterator().next();
        assertEquals(THIS_IS_THE_RESULT, field.name());
    }

}
