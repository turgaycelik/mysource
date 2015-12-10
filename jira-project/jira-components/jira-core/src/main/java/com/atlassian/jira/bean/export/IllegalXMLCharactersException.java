package com.atlassian.jira.bean.export;

public class IllegalXMLCharactersException extends Exception
{
    public IllegalXMLCharactersException()
    {
    }

    public IllegalXMLCharactersException(String message)
    {
        super(message);
    }
}
