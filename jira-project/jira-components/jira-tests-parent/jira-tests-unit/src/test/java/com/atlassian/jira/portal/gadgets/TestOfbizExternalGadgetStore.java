package com.atlassian.jira.portal.gadgets;

import com.atlassian.gadgets.directory.spi.ExternalGadgetSpec;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecId;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;


public class TestOfbizExternalGadgetStore
{
    private OfbizExternalGadgetStore externalGadgetStore;

    private static final String TABLE_EXTERNALGADGET = "ExternalGadget";
    private static final String COLUMN_ID = "id";

    @Test
    public void testStore()
    {
        initializeWith(new MockOfBizDelegator());

        final URI google = URI.create("http://www.google.com.au/");
        final URI msn = URI.create("http://www.msn.com.au/");
        final URI atlassian = URI.create("http://www.atlassian.com/");

        assertFalse(externalGadgetStore.containsSpecUri(google));
        assertFalse(externalGadgetStore.containsSpecUri(msn));
        assertFalse(externalGadgetStore.containsSpecUri(atlassian));

        ExternalGadgetSpec googleSpec = externalGadgetStore.addGadgetSpecUri(google);
        Assert.assertTrue(externalGadgetStore.containsSpecUri(google));
        Assert.assertFalse(externalGadgetStore.containsSpecUri(msn));
        Assert.assertFalse(externalGadgetStore.containsSpecUri(atlassian));

        Set<ExternalGadgetSpec> gadgetSpecUris = externalGadgetStore.getAllGadgetSpecUris();
        Assert.assertEquals(1, gadgetSpecUris.size());
        Assert.assertEquals(google, gadgetSpecUris.iterator().next().getSpecUri());

        //duplicates are not allowed
        try
        {
            googleSpec = externalGadgetStore.addGadgetSpecUri(google);
            Assert.fail("Should have complained about duplicates!");
        }
        catch (IllegalStateException e)
        {
            //yay
        }

        final ExternalGadgetSpec msnSpec = externalGadgetStore.addGadgetSpecUri(msn);
        final ExternalGadgetSpec atlassianSpec = externalGadgetStore.addGadgetSpecUri(atlassian);

        gadgetSpecUris = externalGadgetStore.getAllGadgetSpecUris();
        Assert.assertEquals(3, gadgetSpecUris.size());
        Assert.assertTrue(gadgetSpecUris.contains(googleSpec));
        Assert.assertTrue(gadgetSpecUris.contains(msnSpec));
        Assert.assertTrue(gadgetSpecUris.contains(atlassianSpec));

        externalGadgetStore.removeGadgetSpecUri(googleSpec.getId());
        gadgetSpecUris = externalGadgetStore.getAllGadgetSpecUris();
        Assert.assertEquals(2, gadgetSpecUris.size());
        Assert.assertTrue(gadgetSpecUris.contains(msnSpec));
        Assert.assertTrue(gadgetSpecUris.contains(atlassianSpec));

        //try removing a URI that doesn't exist. Should just work (tm)
        externalGadgetStore.removeGadgetSpecUri(ExternalGadgetSpecId.valueOf(Long.toString(-999)));
        gadgetSpecUris = externalGadgetStore.getAllGadgetSpecUris();
        Assert.assertEquals(2, gadgetSpecUris.size());
        Assert.assertTrue(gadgetSpecUris.contains(msnSpec));
        Assert.assertTrue(gadgetSpecUris.contains(atlassianSpec));
    }


    //JRA-20554: The call to removeByAnd must contain a Long for the ID to match that database column definition
    //as otherwise PostgreSQL83+  will throw an "operator does not exist" exception.
    @Test
    public void testRemoveGadgetSpecUri() throws Exception
    {
        final OfBizDelegator ofBizDelegator = Mockito.mock(OfBizDelegator.class);
        initializeWith(ofBizDelegator);

        final long gadgetId = 10;
        externalGadgetStore.removeGadgetSpecUri(ExternalGadgetSpecId.valueOf("" + gadgetId));
        verify(ofBizDelegator).removeByAnd(TABLE_EXTERNALGADGET, ImmutableMap.of(COLUMN_ID, gadgetId));
    }

    private void initializeWith(final OfBizDelegator ofBizDelegator)
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker().addMock(OfBizDelegator.class, ofBizDelegator));
        externalGadgetStore = new OfbizExternalGadgetStore(ofBizDelegator);
    }
}
