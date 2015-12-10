package com.atlassian.jira.web.action;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotation used to indicate that a method is to be used to provide data to an {@link JiraWebActionSupport}
 * action.
 * <p/>
 * If you specify no view value then it defaults to "*" which means this applies to all the action views
 * <p/>
 * By default the name of the method is used as the data key.  If the method is a JavaBean getter then the
 * de-capitalised name will be used otherwise the method name is taken as is.  You can use the key="xxx" attribute to
 * override the key name used
 * <p/>
 * <pre>
 *     getAddress() --> "address"
 *     myAddress()  --> "myAddress
 * </pre>
 *
 * @since v6.0
 */
@Retention (RUNTIME)
@Target (METHOD)
public @interface ActionViewData
{
    /**
     * Names the view that this data item is associated with.
     *
     * @return the view to apply to or it defaults to "*" which means this applies to all the action views
     */
    String value() default "*";

    /**
     * Use this optional parameter to change the name of the key used to store the method value.  This is useful if you
     * cannot shape the method name how you would like.  The general idea is to use bean names or direct method naming.
     *
     * @return the name to use or "*" which means use the method name as is
     */
    String key() default "*";

}
