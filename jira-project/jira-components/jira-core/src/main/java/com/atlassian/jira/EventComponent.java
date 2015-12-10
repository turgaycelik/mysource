package com.atlassian.jira;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A class annotated with this will be registered automatically with JIRA's {@link com.atlassian.event.api.EventPublisher}
 * to listen for events.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventComponent
{
}
