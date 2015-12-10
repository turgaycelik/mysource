package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.mapper.IdKeyPair;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapperImpl;
import com.atlassian.jira.imports.project.parser.ProjectParser;

import com.google.common.collect.ImmutableMap;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * @since v3.13
 */
public class TestProjectMapperHandler
{
    
    @Test
    public void testProject() throws ParseException
    {
        SimpleProjectImportIdMapper projectMapper = new SimpleProjectImportIdMapperImpl();

        ProjectMapperHandler projectMapperHandler = new ProjectMapperHandler(projectMapper);
        projectMapperHandler.handleEntity(ProjectParser.PROJECT_ENTITY_NAME, ImmutableMap.of("id", "123", "key", "TST"));
        projectMapperHandler.handleEntity(ProjectParser.PROJECT_ENTITY_NAME, ImmutableMap.of("id", "321", "key", "ANA"));
        assertThat(projectMapper.getValuesFromImport(), containsInAnyOrder(
                new IdKeyPair("123", "TST"),
                new IdKeyPair("321", "ANA") ));
    }

    @Test
    public void testProjectWrongEntityType() throws ParseException
    {
        SimpleProjectImportIdMapper projectMapper = new SimpleProjectImportIdMapperImpl();

        ProjectMapperHandler projectMapperHandler = new ProjectMapperHandler(projectMapper);
        projectMapperHandler.handleEntity("BSENTITY", ImmutableMap.<String,String>of());
        assertThat(projectMapper.getValuesFromImport(), Matchers.<IdKeyPair>empty());
    }

}
