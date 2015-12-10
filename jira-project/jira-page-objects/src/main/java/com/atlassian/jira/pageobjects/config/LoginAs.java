package com.atlassian.jira.pageobjects.config;

import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.pageobjects.Page;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark your test with this annotation if you want a particular user to be logged-in automatically before
 * the test executes.
 */
@Target( { ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginAs
{
    String user() default "";

    String password() default "";

    boolean sysadmin() default false;

    boolean admin() default false;

    boolean anonymous() default false;

    Class<? extends Page> targetPage() default DashboardPage.class;
}
