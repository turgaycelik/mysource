package com.atlassian.jira;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.Exception;
import java.net.URL;
import java.net.URLConnection;

public class RevisionInfo
{

    public static void main(String[] args)
    {
        BufferedReader in = null;
        BufferedWriter output = null;
        try
        {
            URL url = new URL("http://localhost:8080/rest/api/2/serverInfo");
            URLConnection connection = url.openConnection();
            output = new BufferedWriter(new FileWriter("serverInfo.txt"));
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                output.write(inputLine);
            in.close();
        }
        catch (Exception ignore)
        {
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
                if (output != null)
                {
                    output.close();
                }
            }
            catch (Exception ignore)
            {
            }

        }

    }

}