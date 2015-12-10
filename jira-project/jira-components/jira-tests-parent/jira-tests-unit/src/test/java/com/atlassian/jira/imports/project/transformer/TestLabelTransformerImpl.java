package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalLabel;
import com.atlassian.jira.imports.project.mapper.CustomFieldMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;

import org.junit.Test;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;

public class TestLabelTransformerImpl
{
    @Test
    public void testTransform()
    {
        LabelTransformerImpl transformer = new LabelTransformerImpl();

        ExternalLabel oldLabel = new ExternalLabel();
        oldLabel.setId("10000");
        oldLabel.setIssueId("10001");
        oldLabel.setCustomFieldId("12000");
        oldLabel.setLabel("alabel");

        final SimpleProjectImportIdMapper mockIdMapper = createMock(SimpleProjectImportIdMapper.class);
        expect(mockIdMapper.getMappedId("10001")).andReturn("20001");
        final CustomFieldMapper mockCustomFieldMapper = createMock(CustomFieldMapper.class);
        expect(mockCustomFieldMapper.getMappedId("12000")).andReturn("14000");
        final ProjectImportMapper mockProjectImportMapper = createMock(ProjectImportMapper.class);
        expect(mockProjectImportMapper.getIssueMapper()).andReturn(mockIdMapper);
        expect(mockProjectImportMapper.getCustomFieldMapper()).andReturn(mockCustomFieldMapper);

        replay(mockProjectImportMapper, mockIdMapper, mockCustomFieldMapper);
        final ExternalLabel externalLabel = transformer.transform(mockProjectImportMapper, oldLabel);
        ExternalLabel expected = new ExternalLabel();
        expected.setIssueId("20001");
        expected.setCustomFieldId("14000");
        expected.setLabel("alabel");
        assertEquals(expected, externalLabel);

        verify(mockProjectImportMapper, mockIdMapper, mockCustomFieldMapper);
    }

}
