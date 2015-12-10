package com.atlassian.jira.crowd.embedded.ofbiz;

import com.atlassian.crowd.search.query.entity.restriction.constants.GroupTermKeys;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @since v6.0
 */
public class GroupEntityTest
{
    @Test
    public void testCrowdGroupTermKeyName() throws Exception
    {
        assertThat(GroupEntity.isSystemField(GroupTermKeys.NAME.getPropertyName()), is(true));
        assertThat(GroupEntity.getLowercaseFieldNameFor(GroupTermKeys.NAME.getPropertyName()), is(GroupEntity.LOWER_NAME));
    }
}
