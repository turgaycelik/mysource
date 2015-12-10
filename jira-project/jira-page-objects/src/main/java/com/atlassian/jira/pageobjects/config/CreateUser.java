package com.atlassian.jira.pageobjects.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CreateUser
{
    String username();

    String password();

    String [] groupnames() default {};

    boolean user() default true;

    boolean developer() default false;

    boolean admin() default false;

}
