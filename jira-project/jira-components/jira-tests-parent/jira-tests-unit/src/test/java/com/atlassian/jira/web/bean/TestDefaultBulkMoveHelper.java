package com.atlassian.jira.web.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.Function;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.1
 */
public class TestDefaultBulkMoveHelper extends MockControllerTestCase
{
    BulkMoveHelper helper = new DefaultBulkMoveHelper() {
        protected BulkEditBean getBulkEditBeanFromSession() {
            return new BulkEditBeanImpl(null);
        }
    };
    BulkEditBean bulkEditBean = createMock(BulkEditBean.class);
    OrderableField orderableField = createMock(OrderableField.class);
    Function issueValueResolution = createMock(Function.class);
    Function nameResolution = createMock(Function.class);
    FieldLayout fieldLayout = createMock(FieldLayout.class);
    FieldLayoutItem targetFieldLayoutItem = createMock(FieldLayoutItem.class);
    Map targetIssueObjects = createMock(Map.class);
    Project project = createMock(Project.class);

    private void replay()
    {
        replay(bulkEditBean, orderableField, issueValueResolution, nameResolution,fieldLayout,targetFieldLayoutItem,
                targetIssueObjects, project);
    }

    private void verify()
    {
        verify(bulkEditBean, orderableField, issueValueResolution, nameResolution,fieldLayout,targetFieldLayoutItem,
                targetIssueObjects, project);
    }

    private List getList(Object ...objs) 
    {
        final List list = new ArrayList();
        for(Object obj : objs)
        {
            list.add(obj);
        }
        return list;
    }

    private void expectSelectedIssues(Issue... selectedIssues)
    {
        expect(orderableField.getId()).andReturn("1");
        expect(bulkEditBean.getTargetFieldLayout()).andReturn(fieldLayout);
        expect(fieldLayout.getFieldLayoutItem(orderableField)).andReturn(targetFieldLayoutItem);
        expect(bulkEditBean.getSelectedIssues()).andReturn(getList(selectedIssues));
    }

    @Test
    public void testGetDistinctValuesForMoveEmptyList()
    {
        expectSelectedIssues();
        replay();

        Map<Long, BulkMoveHelper.DistinctValueResult> result = helper.getDistinctValuesForMove(bulkEditBean, orderableField, issueValueResolution, nameResolution);
        assertEquals(0, result.size());

        verify();
    }

    private void expectProject(Issue issue, Long id)
    {
        expect(issue.getProjectObject()).andReturn(project);
        expect(project.getId()).andReturn(id);
    }

    private void expectTargetIssue(Issue selectedIssue, Issue targetIssue, Long selectedIssueId, Long targetIssueId)
    {
        expect(bulkEditBean.getTargetIssueObjects()).andReturn(targetIssueObjects);
        expect(targetIssueObjects.get(selectedIssue)).andReturn(targetIssue);
        expectProject(targetIssue, targetIssueId);
        expectProject(selectedIssue, selectedIssueId);
    }


    @Test
    public void testGetDistinctValuesForMoveSameProject()
    {
        Issue selectedIssue = createMock(Issue.class);
        Issue targetIssue = createMock(Issue.class);
        expectSelectedIssues(selectedIssue);
        expectTargetIssue(selectedIssue, targetIssue, 1L, 1L);

        replay();

        Map<Long, BulkMoveHelper.DistinctValueResult> result = helper.getDistinctValuesForMove(bulkEditBean, orderableField, issueValueResolution, nameResolution);
        assertEquals(0, result.size());

        verify();
    }

    @Test
    public void testGetDistinctValuesEmptyValuesForIssueNotRequired()
    {
        Issue selectedIssue = createMock(Issue.class);
        Issue targetIssue = createMock(Issue.class);
        expectSelectedIssues(selectedIssue);
        expectTargetIssue(selectedIssue, targetIssue, 1L, 2L);

        expect(issueValueResolution.get(selectedIssue)).andReturn(new ArrayList());
        expect(targetFieldLayoutItem.isRequired()).andReturn(false);

        replay();

        Map<Long, BulkMoveHelper.DistinctValueResult> result = helper.getDistinctValuesForMove(bulkEditBean, orderableField, issueValueResolution, nameResolution);
        assertEquals(0, result.size());

        verify();
    }

    @Test
    public void testGetDistinctValuesEmptyValuesForIssueRequired()
    {
        Issue selectedIssue = createMock(Issue.class);
        Issue targetIssue = createMock(Issue.class);
        expectSelectedIssues(selectedIssue);
        expectTargetIssue(selectedIssue, targetIssue, 1L, 2L);

        expect(issueValueResolution.get(selectedIssue)).andReturn(new ArrayList());
        expect(targetFieldLayoutItem.isRequired()).andReturn(true);

        replay();

        Map<Long, BulkMoveHelper.DistinctValueResult> result = helper.getDistinctValuesForMove(bulkEditBean, orderableField, issueValueResolution, nameResolution);
        assertEquals(1, result.size());
        assertEquals(result.get(-1L), new BulkMoveHelper.DistinctValueResult());

        verify();
    }

    @Test
    public void testGetDistinctValuesWithValueForIssueResolvedToNull()
    {
        Issue selectedIssue = createMock(Issue.class);
        Issue targetIssue = createMock(Issue.class);
        expectSelectedIssues(selectedIssue);
        expectTargetIssue(selectedIssue, targetIssue, 1L, 2L);

        expect(issueValueResolution.get(selectedIssue)).andReturn(getList("55"));
        expect(nameResolution.get("55")).andReturn(null);

        replay();

        Map<Long, BulkMoveHelper.DistinctValueResult> result = helper.getDistinctValuesForMove(bulkEditBean, orderableField, issueValueResolution, nameResolution);
        assertEquals(0, result.size());

        verify();
    }

    private void expectProjectName(Issue issue, String projectName)
    {
        expect(issue.getProjectObject()).andReturn(project);
        expect(project.getName()).andReturn(projectName);
    }

    @Test
    public void testGetDistinctValuesWithValueForIssueResolved()
    {
        Issue selectedIssue = createMock(Issue.class);
        Issue targetIssue = createMock(Issue.class);
        expectSelectedIssues(selectedIssue);
        expectTargetIssue(selectedIssue, targetIssue, 1L, 2L);

        expect(issueValueResolution.get(selectedIssue)).andReturn(getList("-1"));
        expect(nameResolution.get("-1")).andReturn(" resolvedName ");
        expectProjectName(selectedIssue, " myProjectName ");

        replay();

        Map<Long, BulkMoveHelper.DistinctValueResult> result = helper.getDistinctValuesForMove(bulkEditBean, orderableField, issueValueResolution, nameResolution);
        assertEquals(1, result.size());
        assertEquals(result.get(-1L), new BulkMoveHelper.DistinctValueResult("resolvedName", " myProjectName ", null, false));

        verify();
    }

    @Test
    public void testGetDistinctValuesWithValueForIssueResolvedWithDuplicates()
    {
        Issue selectedIssue = createMock(Issue.class);
        Issue targetIssue = createMock(Issue.class);
        expectSelectedIssues(selectedIssue);
        expectTargetIssue(selectedIssue, targetIssue, 1L, 2L);

        expect(issueValueResolution.get(selectedIssue)).andReturn(getList("10", "10"));
        expect(nameResolution.get("10")).andReturn(" resolvedName ");
        expectProjectName(selectedIssue, " myProjectName ");

        replay();

        Map<Long, BulkMoveHelper.DistinctValueResult> result = helper.getDistinctValuesForMove(bulkEditBean, orderableField, issueValueResolution, nameResolution);
        assertEquals(1, result.size());
        assertEquals(result.get(10L), new BulkMoveHelper.DistinctValueResult("resolvedName", " myProjectName ", null, false));

        verify();
    }

    @Test
    public void testNeedsSelection()
    {
        replay();
        assertFalse(helper.needsSelection(new BulkMoveHelper.DistinctValueResult(), 1L, "value"));
        assertTrue(helper.needsSelection(new BulkMoveHelper.DistinctValueResult("valueName", "projectName", 1L, true), 1L, ""));
        assertFalse(helper.needsSelection(new BulkMoveHelper.DistinctValueResult("valueName", "projectName", 1L, true), 2L, ""));
        assertTrue(helper.needsSelection(new BulkMoveHelper.DistinctValueResult("valueName", "projectName", null, false), 2L, "valueName"));
        assertFalse(helper.needsSelection(new BulkMoveHelper.DistinctValueResult("valueName", "projectName", null, true), 2L, "valueName"));
        assertFalse(helper.needsSelection(new BulkMoveHelper.DistinctValueResult("valueName", "projectName", null, false), 2L, "VALUENAME"));
        assertFalse(helper.needsSelection(new BulkMoveHelper.DistinctValueResult("valueName", "projectName", null, false), 2L, "ANOTHER NAME"));
        verify();
    }
}
