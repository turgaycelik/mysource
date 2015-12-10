package com.atlassian.jira.action.admin;

import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.Executor;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.license.LicenseStringFactory;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.xml.sax.Attributes;

import static com.atlassian.jira.action.admin.OfbizImportHandler.OSPROPERTY_ENTRY;
import static com.atlassian.jira.action.admin.OfbizImportHandler.OSPROPERTY_STRING;
import static com.atlassian.jira.action.admin.OfbizImportHandler.OSPROPERTY_TEXT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Copyright All Rights Reserved. Created: christo 16/10/2006 16:39:14
 */
@RunWith ( MockitoJUnitRunner.class )
public class TestOfbizImportHandler
{
    @Mock
    private OfBizDelegator ofBizDelegator;
    @Mock
    private Executor executor;
    @Mock
    private LicenseStringFactory licenseStringFactory;
    @Mock
    private IndexPathManager indexPathManager;
    @Mock
    private AttachmentPathManager attachmentPathManager;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private OfbizImportHandler handler;


    private static final String ENTITY_ENGINE_XML = "entity-engine-xml";
    private static final String PROPERTY_KEY = "propertyKey";
    private static final String VALUE_NAME = "value";
    private static final String LIC_MESSAGE = "LIC_MESSAGE";
    private static final String LIC_HASH = "LIC_HASH";
    private static final String LIC_HASH_NEVER = "EVER";
    private static final String LIC_MESSAGE_NEVER = "NEVER";
    private static final String LIC_STRING = "LIC_STRING";

    private static final String ID = "666";
    private static final String ID2 = "664";
    private static final String ID3 = "665";
    private static final String ID4 = "667";
    private static final String ID5 = "999";
    private static final String BUILD_NUMBER = "12345";
    private static final String INDEX_PATH = "67789";
    private static final String ATTACHMENT_PATH = "88333";

    @Before
    public void setUp() throws Exception
    {
        handler = new OfbizImportHandler(ofBizDelegator, executor, licenseStringFactory, indexPathManager, attachmentPathManager);
    }

    @Test
    public void testBuildNumberNotPresentInXml() throws Exception
    {
        handler.createBuildNumber();
        assertNull(handler.getBuildNumber());
    }

    @Test
    public void testBuildPresentInXml()
    {
        // faking SAX events...
        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID, PROPERTY_KEY, APKeys.JIRA_PATCHED_VERSION));
        handler.recordElementsInfo(OSPROPERTY_STRING, saxAttr(ID, VALUE_NAME, BUILD_NUMBER));

        handler.createBuildNumber();
        assertEquals(BUILD_NUMBER, handler.getBuildNumber());
    }

    @Test
    public void testIndexPathInXml()
    {
        // faking SAX events...
        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID, PROPERTY_KEY, APKeys.JIRA_PATH_INDEX));
        handler.recordElementsInfo(OSPROPERTY_STRING, saxAttr(ID, VALUE_NAME, INDEX_PATH));

        handler.createIndexPath();

        assertEquals(INDEX_PATH, handler.getIndexPath());
    }

    @Test
    public void testIndexPathNotInXml()
    {
        handler.createIndexPath();

        assertNull(handler.getIndexPath());
    }

    @Test
    public void testAttachmentPathInXml()
    {
        // faking SAX events...
        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID, PROPERTY_KEY, APKeys.JIRA_PATH_ATTACHMENTS));
        handler.recordElementsInfo(OSPROPERTY_STRING, saxAttr(ID, VALUE_NAME, ATTACHMENT_PATH));

        handler.createAttachmentPath();

        assertEquals(ATTACHMENT_PATH, handler.getAttachmentPath());
    }

    @Test
    public void testAttachmentPathNotInXml()
    {
        handler.createAttachmentPath();

        assertNull(handler.getAttachmentPath());
    }

    @Test
    public void testNewLicenseKeyIsDetected() throws Exception
    {
        // faking SAX events...
        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID, PROPERTY_KEY, APKeys.JIRA_LICENSE));
        handler.recordElementsInfo(OSPROPERTY_TEXT, saxAttr(ID, VALUE_NAME, LIC_STRING));

        handler.createLicenseString();
        assertEquals(LIC_STRING, handler.getLicenseString());
    }

    @Test
    public void testOldLicenseKeyIsDetected() throws Exception
    {
        when(licenseStringFactory.create(LIC_MESSAGE, LIC_HASH)).thenReturn(LIC_STRING);

        // faking SAX events...
        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID, PROPERTY_KEY, APKeys.JIRA_LICENSE_V1_HASH));
        handler.recordElementsInfo(OSPROPERTY_TEXT, saxAttr(ID, VALUE_NAME, LIC_HASH));

        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID2, PROPERTY_KEY, APKeys.JIRA_LICENSE_V1_MESSAGE));
        handler.recordElementsInfo(OSPROPERTY_TEXT, saxAttr(ID2, VALUE_NAME, LIC_MESSAGE));


        handler.createLicenseString();
        assertEquals(LIC_STRING, handler.getLicenseString());
    }

    @Test
    public void testReallyOldLicenseKeyIsDetected() throws Exception
    {
        when(licenseStringFactory.create(LIC_MESSAGE, LIC_HASH)).thenReturn(LIC_STRING);

        // faking SAX events...
        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID, PROPERTY_KEY, APKeys.JIRA_OLD_LICENSE_V1_HASH));
        handler.recordElementsInfo(OSPROPERTY_STRING, saxAttr(ID, VALUE_NAME, LIC_HASH));

        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID2, PROPERTY_KEY, APKeys.JIRA_OLD_LICENSE_V1_MESSAGE));
        handler.recordElementsInfo(OSPROPERTY_STRING, saxAttr(ID2, VALUE_NAME, LIC_MESSAGE));


        handler.createLicenseString();
        assertEquals(LIC_STRING, handler.getLicenseString());
    }

    @Test
    public void testThatOldLicensesTakePrecedenceOverRealyOld() throws Exception
    {
        when(licenseStringFactory.create(LIC_MESSAGE, LIC_HASH)).thenReturn(LIC_STRING);

        // faking SAX events...
        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID, PROPERTY_KEY, APKeys.JIRA_OLD_LICENSE_V1_HASH));
        handler.recordElementsInfo(OSPROPERTY_STRING, saxAttr(ID, VALUE_NAME, LIC_HASH_NEVER));

        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID2, PROPERTY_KEY, APKeys.JIRA_OLD_LICENSE_V1_MESSAGE));
        handler.recordElementsInfo(OSPROPERTY_STRING, saxAttr(ID2, VALUE_NAME, LIC_MESSAGE_NEVER));


        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID3, PROPERTY_KEY, APKeys.JIRA_LICENSE_V1_HASH));
        handler.recordElementsInfo(OSPROPERTY_TEXT, saxAttr(ID3, VALUE_NAME, LIC_HASH));

        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID4, PROPERTY_KEY, APKeys.JIRA_LICENSE_V1_MESSAGE));
        handler.recordElementsInfo(OSPROPERTY_TEXT, saxAttr(ID4, VALUE_NAME, LIC_MESSAGE));

        handler.createLicenseString();
        assertEquals(LIC_STRING, handler.getLicenseString());
    }

    @Test
    public void testThatNewLicensesTakePrecedence() throws Exception
    {
        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID, PROPERTY_KEY, APKeys.JIRA_OLD_LICENSE_V1_HASH));
        handler.recordElementsInfo(OSPROPERTY_STRING, saxAttr(ID, VALUE_NAME, LIC_HASH_NEVER));

        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID2, PROPERTY_KEY, APKeys.JIRA_OLD_LICENSE_V1_MESSAGE));
        handler.recordElementsInfo(OSPROPERTY_STRING, saxAttr(ID2, VALUE_NAME, LIC_MESSAGE_NEVER));


        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID3, PROPERTY_KEY, APKeys.JIRA_LICENSE_V1_HASH));
        handler.recordElementsInfo(OSPROPERTY_TEXT, saxAttr(ID3, VALUE_NAME, LIC_HASH));

        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID4, PROPERTY_KEY, APKeys.JIRA_LICENSE_V1_MESSAGE));
        handler.recordElementsInfo(OSPROPERTY_TEXT, saxAttr(ID4, VALUE_NAME, LIC_MESSAGE));

        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID5, PROPERTY_KEY, APKeys.JIRA_LICENSE));
        handler.recordElementsInfo(OSPROPERTY_TEXT, saxAttr(ID5, VALUE_NAME, LIC_STRING));

        handler.createLicenseString();
        assertEquals(LIC_STRING, handler.getLicenseString());
    }

    @Test
    public void testWhenThereIsNoLicensePresentAtAll() throws Exception
    {
        handler.createLicenseString();
        assertNull(handler.getLicenseString());
    }

    @Test
    public void testCreateZeroDeadlock() throws Exception
    {
        GenericValue gv = mock(GenericValue.class);
        when(gv.create()).thenReturn(gv);

        handler.createWithDeadlockRetry(Collections.singletonList(gv));
        Mockito.verify(gv, times(1)).create();
    }

    @Test
    public void testCreateOneDeadlock() throws Exception
    {
        GenericValue gv = mock(GenericValue.class);
        when(gv.create()).thenThrow(getSQLDeadlockException()).thenReturn(gv);

        handler.createWithDeadlockRetry(Collections.singletonList(gv));
        Mockito.verify(gv, times(2)).create();
    }

    @Test
    public void testCreateThreeDeadlocks() throws Exception
    {
        GenericValue gv = mock(GenericValue.class);
        when(gv.create())
                .thenThrow(getSQLDeadlockException())
                .thenThrow(getSQLDeadlockException())
                .thenThrow(getSQLDeadlockException())
                .thenReturn(gv);

        handler.createWithDeadlockRetry(Collections.singletonList(gv));
        Mockito.verify(gv, times(4)).create();
    }

    @Test
    public void testCreateFourDeadlocks() throws Exception
    {
        GenericValue gv = mock(GenericValue.class);
        when(gv.create())
                .thenThrow(getSQLDeadlockException())
                .thenThrow(getSQLDeadlockException())
                .thenThrow(getSQLDeadlockException())
                .thenThrow(getSQLDeadlockException());

        expectedException.expect(GenericEntityException.class);
        handler.createWithDeadlockRetry(Collections.singletonList(gv));
    }

    @Test
    public void testCreateDeeplyNestedDeadlocks() throws Exception
    {
        GenericValue gv = mock(GenericValue.class);
        when(gv.create())
                .thenThrow(getSQLDeadlockException())
                .thenThrow(getSQLDeadlockNestedException())
                .thenThrow(getSQLDeadlockNestedException())
                .thenReturn(gv);

        handler.createWithDeadlockRetry(Collections.singletonList(gv));
        Mockito.verify(gv, times(4)).create();
    }

    @Test
    public void testStartElement() throws Exception
    {
        handler.startElement("", "", ENTITY_ENGINE_XML, saxAttr(ID3, PROPERTY_KEY, APKeys.JIRA_LICENSE_V1_HASH));

        when(ofBizDelegator.makeValue(Mockito.anyString())).thenReturn(new MockGenericValue("Action"));
        handler.startElement("", "", "Action", saxAttr(ID3, PROPERTY_KEY, APKeys.JIRA_LICENSE_V1_HASH));
        Mockito.verify(ofBizDelegator, times(1)).makeValue(Mockito.anyString());
    }

    @Test
    public void testStartElementThrowsDuringValidation() throws Exception
    {
        handler.setCreateEntities(false);
        handler.startDocument();
        handler.startElement("", "", ENTITY_ENGINE_XML, saxAttr(ID3, PROPERTY_KEY, APKeys.JIRA_LICENSE_V1_HASH));

        when(ofBizDelegator.makeValue(Mockito.anyString())).thenThrow(new RuntimeException());
        handler.startElement("", "", "Action", saxAttr(ID3, PROPERTY_KEY, APKeys.JIRA_LICENSE_V1_HASH));
        assertTrue("Exception should be muted and recorded in error collection", handler.getErrorCollection().hasAnyErrors());
        Mockito.verify(ofBizDelegator, times(1)).makeValue(Mockito.anyString());
    }

    @Test
    public void testStartElementThrowsDuringImport() throws Exception
    {
        handler.setCreateEntities(true);
        handler.startDocument();
        handler.startElement("", "", ENTITY_ENGINE_XML, saxAttr(ID3, PROPERTY_KEY, APKeys.JIRA_LICENSE_V1_HASH));

        when(ofBizDelegator.makeValue(Mockito.anyString())).thenThrow(new RuntimeException());
        expectedException.expect(RuntimeException.class);
        handler.startElement("", "", "Action", saxAttr(ID3, PROPERTY_KEY, APKeys.JIRA_LICENSE_V1_HASH));
    }

    private Exception getSQLDeadlockException()
    {
        SQLException sqlEx = new SQLException("Dummy Deadlock", "40001");
        return new GenericEntityException(sqlEx.getMessage(), sqlEx);
    }

    private Exception getSQLDeadlockNestedException()
    {
        SQLException sqlEx = new SQLException("Dummy Deadlock", "40001");
        Exception ex2 = new GenericEntityException(sqlEx.getMessage(), sqlEx);
        Exception ex3 = new GenericEntityException(sqlEx.getMessage(), ex2);
        return new GenericEntityException(sqlEx.getMessage(), ex3);
    }

    private Attributes saxAttr(final String id, final String key, final String value)
    {
        return new MockAttributesBuilder().id(id).attr(key, value).build();
    }

}
