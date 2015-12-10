package com.atlassian.jira.scheme;

import java.util.Arrays;
import java.util.Collection;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.security.Permissions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.permission.ProjectPermissions.ADMINISTER_PROJECTS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @since v6.3
 */
@RunWith (MockitoJUnitRunner.class)
public class TestDefaultSchemeFactory
{
    private DefaultSchemeFactory defaultSchemeFactory = new DefaultSchemeFactory();

    @Test
    public void permissionSchemeEntityWithSystemPermissionKeyGetsSystemPermissionId()
    {
        MockGenericValue entityGv = new MockGenericValue("SchemePermissions");
        entityGv.set("id", 1L);
        entityGv.set("permissionKey", ADMINISTER_PROJECTS.permissionKey());
        entityGv.set("permission", 1L);
        entityGv.set("type", "group");
        entityGv.set("parameter", "test");

        Collection<SchemeEntity> entities = defaultSchemeFactory.convertToSchemeEntities(Arrays.<GenericValue>asList(entityGv), 100L, true);
        assertThat(entities.size(), equalTo(1));

        SchemeEntity entity = entities.iterator().next();
        assertThat(entity.getId(), equalTo(1L));
        assertThat(entity.getType(), equalTo("group"));
        assertThat(entity.getParameter(), equalTo("test"));
        assertThat(entity.getEntityTypeId(), equalTo((Object) (long) Permissions.PROJECT_ADMIN));
    }

    @Test
    public void permissionSchemeEntityWithUnexpectedPermissionKeyGetsPermissionFieldValue()
    {
        MockGenericValue entityGv = new MockGenericValue("SchemePermissions");
        entityGv.set("id", 1L);
        entityGv.set("permissionKey", "unexpected");
        entityGv.set("permission", 1L);
        entityGv.set("type", "group");
        entityGv.set("parameter", "test");

        Collection<SchemeEntity> entities = defaultSchemeFactory.convertToSchemeEntities(Arrays.<GenericValue>asList(entityGv), 100L, true);
        assertThat(entities.size(), equalTo(1));

        SchemeEntity entity = entities.iterator().next();
        assertThat(entity.getId(), equalTo(1L));
        assertThat(entity.getType(), equalTo("group"));
        assertThat(entity.getParameter(), equalTo("test"));
        assertThat(entity.getEntityTypeId(), equalTo((Object) 1L));
    }

    @Test
    public void permissionSchemeEntityWithMissingPermissionKeyGetsPermissionFieldValue()
    {
        MockGenericValue entityGv = new MockGenericValue("SchemePermissions");
        entityGv.set("id", 1L);
        entityGv.set("permission", 1L);
        entityGv.set("type", "group");
        entityGv.set("parameter", "test");

        Collection<SchemeEntity> entities = defaultSchemeFactory.convertToSchemeEntities(Arrays.<GenericValue>asList(entityGv), 100L, true);
        assertThat(entities.size(), equalTo(1));

        SchemeEntity entity = entities.iterator().next();
        assertThat(entity.getId(), equalTo(1L));
        assertThat(entity.getType(), equalTo("group"));
        assertThat(entity.getParameter(), equalTo("test"));
        assertThat(entity.getEntityTypeId(), equalTo((Object) 1L));
    }

    @Test
    public void permissionSchemeEntityWithMissingPermissionKeyAndPermissionIdGetsIgnored()
    {
        MockGenericValue entityGv = new MockGenericValue("SchemePermissions");
        entityGv.set("id", 1L);
        entityGv.set("type", "group");
        entityGv.set("parameter", "test");

        Collection<SchemeEntity> entities = defaultSchemeFactory.convertToSchemeEntities(Arrays.<GenericValue>asList(entityGv), 100L, true);
        assertThat(entities.isEmpty(), is(true));
    }
}
