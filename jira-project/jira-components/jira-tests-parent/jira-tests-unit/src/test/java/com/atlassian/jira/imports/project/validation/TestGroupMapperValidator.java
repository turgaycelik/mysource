package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapperImpl;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.junit.Test;

import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestGroupMapperValidator
{
    @Test
    public void testGroupMapperValidator()
    {
        GroupMapperValidator groupMapperValidator = new GroupMapperValidator();
        SimpleProjectImportIdMapper groupMapper = new SimpleProjectImportIdMapperImpl();
        groupMapper.registerOldValue("dudes", "dudes");
        groupMapper.registerOldValue("dudettes", "dudettes");
        groupMapper.flagValueAsRequired("dudes");
        groupMapper.flagValueAsRequired("dudettes");
        groupMapper.mapValue("dudes", "dudes");

        MessageSet messageSet = groupMapperValidator.validateMappings(new MockI18nBean(), groupMapper);
        assert1ErrorNoWarnings(messageSet, "The group 'dudettes' is required for the import but does not exist in the current JIRA instance.");
    }
}
