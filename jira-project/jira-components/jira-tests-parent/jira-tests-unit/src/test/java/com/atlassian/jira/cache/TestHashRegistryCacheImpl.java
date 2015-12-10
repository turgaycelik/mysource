package com.atlassian.jira.cache;

import java.lang.ref.SoftReference;

import com.atlassian.modzdetector.Modifications;
import com.atlassian.modzdetector.ModzDetector;
import com.atlassian.modzdetector.ModzRegistryException;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

import static org.junit.Assert.assertSame;

/**
 *
 * @since v3.13
 */
public class TestHashRegistryCacheImpl
{
    @Test
    public void testGetModifications() throws ModzRegistryException
    {
        Modifications mods = new Modifications();

        final MockControl mockModzDetectorControl = MockClassControl.createControl(ModzDetector.class);
        final ModzDetector mockModzDetector = (ModzDetector) mockModzDetectorControl.getMock();
        mockModzDetector.getModifiedFiles();
        mockModzDetectorControl.setDefaultReturnValue(mods);

        mockModzDetectorControl.replay();
        HashRegistryCacheImpl registry = new HashRegistryCacheImpl(mockModzDetector, new SoftReference(null));

        assertSame(mods, registry.getModifications());
    }
}
