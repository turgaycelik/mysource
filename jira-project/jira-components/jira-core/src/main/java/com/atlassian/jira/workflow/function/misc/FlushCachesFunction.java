/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.misc;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * This function flushes the caches for a given key
 * @deprecated This class is here for legacy reasons - it may be contained in other people's
 */
public class FlushCachesFunction implements FunctionProvider
{
    private static final Logger log = Logger.getLogger(FlushCachesFunction.class);

    public void execute(Map transientVars, Map args, PropertySet ps)
    {
        log.info("FlushCachesFunction is deprecated, as it no longer serves any purpose.  You can safely remove it from your workflow.");
    }

    public static FunctionDescriptor makeDescriptor()
    {
        FunctionDescriptor descriptor = DescriptorFactory.getFactory().createFunctionDescriptor();
        descriptor.setType("class");
        descriptor.getArgs().put("class.name", FlushCachesFunction.class.getName());
        return descriptor;
    }



}
