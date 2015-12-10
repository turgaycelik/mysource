package com.atlassian.jira.portal.gadgets;

import java.net.URI;
import java.util.Set;

import com.atlassian.gadgets.directory.spi.ExternalGadgetSpec;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecId;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestCachingExternalGadgetStore
{
    @Mock ExternalGadgetStore mockExternalGadgetStore;

    CachingExternalGadgetStore externalGadgetStore;


    @After
    public void tearDown()
    {
        mockExternalGadgetStore = null;
        externalGadgetStore = null;
    }

    @Test
    public void testCRUD()
    {
        ExternalGadgetSpec spec1 = new ExternalGadgetSpec(ExternalGadgetSpecId.valueOf("1"), URI.create("http://www.igoogle.com/frogger.xml"));
        ExternalGadgetSpec spec2 = new ExternalGadgetSpec(ExternalGadgetSpecId.valueOf("2"), URI.create("http://jira.atlassian.com/pie.xml"));
        ExternalGadgetSpec spec3 = new ExternalGadgetSpec(ExternalGadgetSpecId.valueOf("3"), URI.create("http://confluence.atlassian.com/stuff.xml"));
        ExternalGadgetSpec addedSpec = new ExternalGadgetSpec(ExternalGadgetSpecId.valueOf("4"), URI.create("http://jira.atlassian.com/timesince.xml"));

        final Set<ExternalGadgetSpec> originalSet = CollectionBuilder.newBuilder(spec1, spec2, spec3).asSet();
        when(mockExternalGadgetStore.getAllGadgetSpecUris()).thenReturn(originalSet);

        externalGadgetStore = new CachingExternalGadgetStore(mockExternalGadgetStore);
        verify(mockExternalGadgetStore).getAllGadgetSpecUris();

        //none of these calls should hit the delegate store.
        assertContains(true, "http://www.igoogle.com/frogger.xml");
        assertContains(false, "http://example.com/invalid.xml");
        assertContains(true, "http://jira.atlassian.com/pie.xml");
        assertContains(true, "http://confluence.atlassian.com/stuff.xml");
        assertContains(false, "http://jira.atlassian.com/timesince.xml");

        when(mockExternalGadgetStore.addGadgetSpecUri(URI.create("http://jira.atlassian.com/timesince.xml"))).thenReturn(addedSpec);
        externalGadgetStore.addGadgetSpecUri(URI.create("http://jira.atlassian.com/timesince.xml"));
        verify(mockExternalGadgetStore).addGadgetSpecUri(URI.create("http://jira.atlassian.com/timesince.xml"));
        assertContains(true, "http://jira.atlassian.com/timesince.xml");

        externalGadgetStore.removeGadgetSpecUri(ExternalGadgetSpecId.valueOf("4"));
        verify(mockExternalGadgetStore).removeGadgetSpecUri(ExternalGadgetSpecId.valueOf("4"));
        assertContains(false, "http://jira.atlassian.com/timesince.xml");

        final Set<ExternalGadgetSpec> gadgetSpecUris = externalGadgetStore.getAllGadgetSpecUris();
        assertEquals(originalSet, gadgetSpecUris);

        verifyNoMoreInteractions(mockExternalGadgetStore);
    }

    void assertContains(boolean expectedValue, String url)
    {
        assertThat(url, externalGadgetStore.containsSpecUri(URI.create(url)), is(expectedValue));
    }
}
