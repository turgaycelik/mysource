package com.atlassian.jira.imports.project.core;

import java.util.Collections;
import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.util.MessageSetImpl;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestMappingResult
{
    @Test
    public void testCanImportNullMessageList()
    {
        MappingResult mappingResult = new MappingResult();

        // Can't import with a null message List
        mappingResult.setSystemFieldsMessageList(null);
        mappingResult.setCustomFieldsMessageList(Collections.EMPTY_LIST);
        assertFalse(mappingResult.canImport());
    }

    @Test
    public void testCanImportNullCFMessageList()
    {
        MappingResult mappingResult = new MappingResult();

        // Can't import with a null message List
        mappingResult.setSystemFieldsMessageList(Collections.EMPTY_LIST);
        mappingResult.setCustomFieldsMessageList(null);
        assertFalse(mappingResult.canImport());
    }

    @Test
    public void testCanImportNotValidated()
    {
        MappingResult mappingResult = new MappingResult();
        // Can't import with any "not validated"
        List messageList = EasyList.build(new MappingResult.ValidationMessage("Thomas", null));
        mappingResult.setSystemFieldsMessageList(messageList);
        mappingResult.setCustomFieldsMessageList(Collections.EMPTY_LIST);
        assertFalse(mappingResult.canImport());
    }

    @Test
    public void testCanImportError()
    {
        MappingResult mappingResult = new MappingResult();
        // Can't import with any Errors
        final MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("You lose.");
        List messageList = EasyList.build(new MappingResult.ValidationMessage("Thomas", messageSet));
        mappingResult.setSystemFieldsMessageList(messageList);
        mappingResult.setCustomFieldsMessageList(Collections.EMPTY_LIST);
        assertFalse(mappingResult.canImport());
    }

    @Test
    public void testCanImportErrorCF()
    {
        MappingResult mappingResult = new MappingResult();
        // Can't import with any Errors
        final MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("You lose.");
        List messageList = EasyList.build(new MappingResult.ValidationMessage("Thomas", messageSet));
        mappingResult.setSystemFieldsMessageList(Collections.EMPTY_LIST);
        mappingResult.setCustomFieldsMessageList(messageList);
        assertFalse(mappingResult.canImport());
    }

    @Test
    public void testCanImportWarning()
    {
        MappingResult mappingResult = new MappingResult();
        // Can't import with any Errors
        final MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addWarningMessage("You lose.");
        List messageList = EasyList.build(new MappingResult.ValidationMessage("Thomas", messageSet));
        mappingResult.setSystemFieldsMessageList(messageList);
        mappingResult.setCustomFieldsMessageList(Collections.EMPTY_LIST);
        assertTrue(mappingResult.canImport());
    }

}
