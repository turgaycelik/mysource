package com.atlassian.jira.web.action.setup;

import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.util.json.JsonUtil;

import java.util.Map;

/**
 * Contains the results of a call to the license validation REST endpoint.
 *
 * @since v5.2
 */
public class LicenseValidationResults
{
    /**
     * A map of key value pairs indicating any license validation erros.
     */
    private Map<String, String> errors;

    /**
     * The license string.
     */
    private String licenseString;

    /**
     * Default constructor.
     */
    public LicenseValidationResults()
    {
    }

    /**
     * Create this from some existing validation results.
     * @param validationResults
     */
    public LicenseValidationResults(JiraLicenseService.ValidationResult validationResults)
    {
        this.setErrors(validationResults.getErrorCollection().getErrors());
        this.setLicenseString(validationResults.getLicenseString());
    }

    /**
     * Convert this to json.
     * @return A JSON formatted string.
     */
    public String toJson()
    {
        JSONObject object = new JSONObject();
        try
        {
            object.put("license", this.getLicenseString());
            if (this.getErrors() != null)
            {
                object.put("errors", this.getErrors());
            }
        }
        catch (JSONException e)
        {
            object = new JSONObject();
        }
        return JsonUtil.toJsonString(object);
    }

    /**
     * Return any errors associated with the license validation.
     * @return
     */
    public Map<String, String> getErrors()
    {
        return this.errors;
    }

    /**
     * Set the errors associated with this validation result.
     * @param errors
     */
    public void setErrors(Map<String, String> errors)
    {
        this.errors = errors;
    }

    /**
     * Return the license string.
     * @return
     */
    public String getLicenseString()
    {
        return this.licenseString;
    }

    /**
     * Set the license string.
     * @param licenseString
     */
    public void setLicenseString(String licenseString)
    {
        this.licenseString = licenseString;
    }
}

