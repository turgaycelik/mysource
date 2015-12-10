package com.atlassian.jira.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes elements in JIRA code base, whose change will affect the On Demand code in terms of compilation or runtime
 * behaviour.
 *
 * @since 4.4
 */
@Retention(RetentionPolicy.SOURCE)
@Target ({ ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.TYPE})
public @interface OnDemand
{
    String value() default "";
}
