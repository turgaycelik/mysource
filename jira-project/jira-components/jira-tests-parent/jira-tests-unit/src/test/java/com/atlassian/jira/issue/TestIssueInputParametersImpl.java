package com.atlassian.jira.issue;

import java.util.Map;

import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.issue.fields.TimeTrackingSystemField;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
public class TestIssueInputParametersImpl
{
    private static final String[] EMPTY_STRING_ARRAY = new String[] {};
    private static final String ORIGINAL_ESTIMATE_DURATION_STRING = "1d 5h";
    public static final String REMAINING_ESTIMATE_DURATION_STRING = "2d 2h";

    @Test
    public void testGetAndSetOriginalEstimateLongInLegacyMode()
    {

        IssueInputParametersImpl legacyIssueInputParameters = createLegacyIssueInputParameters();

        legacyIssueInputParameters.setOriginalEstimate(1000L);

        Map<String,String[]> actionParameters = legacyIssueInputParameters.getActionParameters();

        assertFalse(actionParameters.containsKey(TimeTrackingSystemField.TIMETRACKING_TARGETSUBFIELD));
        assertFalse(actionParameters.containsKey(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE));
        assertFalse(actionParameters.containsKey(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE));
        assertEquals("1000", getSingleStringValue(actionParameters.get(IssueFieldConstants.TIMETRACKING)));

        assertEquals("1000", legacyIssueInputParameters.getOriginalEstimateAsDurationString());
        assertEquals("1000", legacyIssueInputParameters.getRemainingEstimateAsDurationString());

        assertEquals(Long.valueOf(1000L), legacyIssueInputParameters.getOriginalEstimate());
    }

    @Test
    public void testGetAndSetOriginalEstimateLongInModernMode()
    {

        IssueInputParametersImpl modernIssueInputParameters = createModernIssueInputParameters();

        modernIssueInputParameters.setOriginalEstimate(1000L);

        Map<String,String[]> actionParameters = modernIssueInputParameters.getActionParameters();

        assertEquals(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE, getSingleStringValue(actionParameters.get(TimeTrackingSystemField.TIMETRACKING_TARGETSUBFIELD)));
        assertEquals("1000", getSingleStringValue(actionParameters.get(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE)));
        assertNull(actionParameters.get(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE));
        assertArrayEquals(EMPTY_STRING_ARRAY, actionParameters.get(IssueFieldConstants.TIMETRACKING));

        assertEquals("1000", modernIssueInputParameters.getOriginalEstimateAsDurationString());
        assertNull(modernIssueInputParameters.getRemainingEstimateAsDurationString());

        assertEquals(Long.valueOf(1000L), modernIssueInputParameters.getOriginalEstimate());
    }

    @Test
    public void testGetAndSetRemainingEstimateLongInLegacyMode()
    {

        IssueInputParametersImpl legacyIssueInputParameters = createLegacyIssueInputParameters();

        legacyIssueInputParameters.setRemainingEstimate(1000L);

        Map<String,String[]> actionParameters = legacyIssueInputParameters.getActionParameters();

        assertFalse(actionParameters.containsKey(TimeTrackingSystemField.TIMETRACKING_TARGETSUBFIELD));
        assertFalse(actionParameters.containsKey(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE));
        assertFalse(actionParameters.containsKey(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE));
        assertEquals("1000", getSingleStringValue(actionParameters.get(IssueFieldConstants.TIMETRACKING)));

        assertEquals("1000", legacyIssueInputParameters.getOriginalEstimateAsDurationString());
        assertEquals("1000", legacyIssueInputParameters.getRemainingEstimateAsDurationString());

        assertEquals(Long.valueOf(1000L), legacyIssueInputParameters.getRemainingEstimate());
    }

    @Test
    public void testGetAndSetRemainingEstimateLongInModernMode()
    {

        IssueInputParametersImpl modernIssueInputParameters = createModernIssueInputParameters();

        modernIssueInputParameters.setRemainingEstimate(1000L);

        Map<String,String[]> actionParameters = modernIssueInputParameters.getActionParameters();

        assertEquals(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE, getSingleStringValue(actionParameters.get(TimeTrackingSystemField.TIMETRACKING_TARGETSUBFIELD)));
        assertEquals("1000", getSingleStringValue(actionParameters.get(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE)));
        assertNull(actionParameters.get(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE));
        assertArrayEquals(EMPTY_STRING_ARRAY, actionParameters.get(IssueFieldConstants.TIMETRACKING));

        assertEquals("1000", modernIssueInputParameters.getRemainingEstimateAsDurationString());
        assertNull(modernIssueInputParameters.getOriginalEstimateAsDurationString());

        assertEquals(Long.valueOf(1000L), modernIssueInputParameters.getRemainingEstimate());
    }

    @Test
    public void testSetOriginalEstimateInLegacyMode()
    {
        IssueInputParametersImpl legacyIssueInputParameters = createLegacyIssueInputParameters();

        legacyIssueInputParameters.setOriginalEstimate(ORIGINAL_ESTIMATE_DURATION_STRING);

        Map<String,String[]> actionParameters = legacyIssueInputParameters.getActionParameters();

        assertFalse(actionParameters.containsKey(TimeTrackingSystemField.TIMETRACKING_TARGETSUBFIELD));
        assertFalse(actionParameters.containsKey(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE));
        assertFalse(actionParameters.containsKey(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE));
        assertEquals(ORIGINAL_ESTIMATE_DURATION_STRING, getSingleStringValue(actionParameters.get(IssueFieldConstants.TIMETRACKING)));

        assertEquals(ORIGINAL_ESTIMATE_DURATION_STRING, legacyIssueInputParameters.getOriginalEstimateAsDurationString());
        assertEquals(ORIGINAL_ESTIMATE_DURATION_STRING, legacyIssueInputParameters.getRemainingEstimateAsDurationString());
    }

    @Test
    public void testSetOriginalEstimateInModernMode()
    {
        IssueInputParametersImpl legacyIssueInputParameters = createModernIssueInputParameters();

        legacyIssueInputParameters.setOriginalEstimate(ORIGINAL_ESTIMATE_DURATION_STRING);

        Map<String,String[]> actionParameters = legacyIssueInputParameters.getActionParameters();

        assertEquals(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE, getSingleStringValue(actionParameters.get(TimeTrackingSystemField.TIMETRACKING_TARGETSUBFIELD)));
        assertEquals(ORIGINAL_ESTIMATE_DURATION_STRING, getSingleStringValue(actionParameters.get(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE)));
        assertNull(actionParameters.get(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE));
        assertArrayEquals(EMPTY_STRING_ARRAY, actionParameters.get(IssueFieldConstants.TIMETRACKING));

        assertEquals(ORIGINAL_ESTIMATE_DURATION_STRING, legacyIssueInputParameters.getOriginalEstimateAsDurationString());
        assertNull(legacyIssueInputParameters.getRemainingEstimateAsDurationString());
    }

    @Test
    public void testSetRemainingEstimateInLegacyMode()
    {
        IssueInputParametersImpl legacyIssueInputParameters = createLegacyIssueInputParameters();

        legacyIssueInputParameters.setRemainingEstimate(REMAINING_ESTIMATE_DURATION_STRING);

        Map<String,String[]> actionParameters = legacyIssueInputParameters.getActionParameters();

        assertFalse(actionParameters.containsKey(TimeTrackingSystemField.TIMETRACKING_TARGETSUBFIELD));
        assertFalse(actionParameters.containsKey(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE));
        assertFalse(actionParameters.containsKey(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE));
        assertEquals(REMAINING_ESTIMATE_DURATION_STRING, getSingleStringValue(actionParameters.get(IssueFieldConstants.TIMETRACKING)));

        assertEquals(REMAINING_ESTIMATE_DURATION_STRING, legacyIssueInputParameters.getRemainingEstimateAsDurationString());
        assertEquals(REMAINING_ESTIMATE_DURATION_STRING, legacyIssueInputParameters.getOriginalEstimateAsDurationString());
    }

    @Test
    public void testSetRemainingEstimateInModernMode()
    {
        IssueInputParametersImpl legacyIssueInputParameters = createModernIssueInputParameters();

        legacyIssueInputParameters.setRemainingEstimate(REMAINING_ESTIMATE_DURATION_STRING);

        Map<String,String[]> actionParameters = legacyIssueInputParameters.getActionParameters();

        assertEquals(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE, getSingleStringValue(actionParameters.get(TimeTrackingSystemField.TIMETRACKING_TARGETSUBFIELD)));
        assertEquals(REMAINING_ESTIMATE_DURATION_STRING, getSingleStringValue(actionParameters.get(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE)));
        assertNull(actionParameters.get(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE));
        assertArrayEquals(EMPTY_STRING_ARRAY, actionParameters.get(IssueFieldConstants.TIMETRACKING));

        assertEquals(REMAINING_ESTIMATE_DURATION_STRING, legacyIssueInputParameters.getRemainingEstimateAsDurationString());
        assertNull(legacyIssueInputParameters.getOriginalEstimateAsDurationString());
    }

    @Test
    public void testSetOriginalAndRemainingEstimateLongInModernMode()
    {
        IssueInputParametersImpl legacyIssueInputParameters = createModernIssueInputParameters();

        legacyIssueInputParameters.setOriginalAndRemainingEstimate(1000L, 2000L);

        Map<String,String[]> actionParameters = legacyIssueInputParameters.getActionParameters();

        assertFalse(actionParameters.containsKey(TimeTrackingSystemField.TIMETRACKING_TARGETSUBFIELD));
        assertEquals("1000", getSingleStringValue(actionParameters.get(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE)));
        assertEquals("2000", getSingleStringValue(actionParameters.get(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE)));
        assertArrayEquals(EMPTY_STRING_ARRAY, actionParameters.get(IssueFieldConstants.TIMETRACKING));

        assertEquals("1000", legacyIssueInputParameters.getOriginalEstimateAsDurationString());
        assertEquals("2000", legacyIssueInputParameters.getRemainingEstimateAsDurationString());

        legacyIssueInputParameters.setOriginalAndRemainingEstimate("1000", "2000");

        assertEquals(Long.valueOf(1000), legacyIssueInputParameters.getOriginalEstimate());
        assertEquals(Long.valueOf(2000), legacyIssueInputParameters.getRemainingEstimate());
    }

    @Test
    public void testSetOriginalAndRemainingEstimateInModernMode()
    {
        IssueInputParametersImpl legacyIssueInputParameters = createModernIssueInputParameters();

        legacyIssueInputParameters.setOriginalAndRemainingEstimate(ORIGINAL_ESTIMATE_DURATION_STRING, REMAINING_ESTIMATE_DURATION_STRING);

        Map<String,String[]> actionParameters = legacyIssueInputParameters.getActionParameters();

        assertFalse(actionParameters.containsKey(TimeTrackingSystemField.TIMETRACKING_TARGETSUBFIELD));
        assertEquals(ORIGINAL_ESTIMATE_DURATION_STRING, getSingleStringValue(actionParameters.get(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE)));
        assertEquals(REMAINING_ESTIMATE_DURATION_STRING, getSingleStringValue(actionParameters.get(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE)));
        assertArrayEquals(EMPTY_STRING_ARRAY, actionParameters.get(IssueFieldConstants.TIMETRACKING));

        assertEquals(ORIGINAL_ESTIMATE_DURATION_STRING, legacyIssueInputParameters.getOriginalEstimateAsDurationString());
        assertEquals(REMAINING_ESTIMATE_DURATION_STRING, legacyIssueInputParameters.getRemainingEstimateAsDurationString());

        legacyIssueInputParameters.setOriginalAndRemainingEstimate("1000", "2000");

        assertEquals(Long.valueOf(1000), legacyIssueInputParameters.getOriginalEstimate());
        assertEquals(Long.valueOf(2000), legacyIssueInputParameters.getRemainingEstimate());
    }

    @Test
    public void testSetOriginalAndRemainingEstimateInLegacyMode()
    {
        IssueInputParametersImpl legacyIssueInputParameters = createLegacyIssueInputParameters();

        legacyIssueInputParameters.setOriginalAndRemainingEstimate(ORIGINAL_ESTIMATE_DURATION_STRING, REMAINING_ESTIMATE_DURATION_STRING);

        Map<String,String[]> actionParameters = legacyIssueInputParameters.getActionParameters();

        assertFalse(actionParameters.containsKey(TimeTrackingSystemField.TIMETRACKING_TARGETSUBFIELD));
        assertFalse(actionParameters.containsKey(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE));
        assertFalse(actionParameters.containsKey(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE));
        assertFalse(actionParameters.containsKey(IssueFieldConstants.TIMETRACKING));

        assertNull(legacyIssueInputParameters.getOriginalEstimateAsDurationString());
        assertNull(legacyIssueInputParameters.getRemainingEstimateAsDurationString());

        assertNull(legacyIssueInputParameters.getRemainingEstimate());
        assertNull(legacyIssueInputParameters.getOriginalEstimate());
    }

    @Test
    public void testFieldToForcePresentParam()
    {
        IssueInputParametersImpl issueInputParameters = createModernIssueInputParameters();
        assertFalse(issueInputParameters.isFieldPresent(IssueFieldConstants.FIX_FOR_VERSIONS));
        
        issueInputParameters.setFixVersionIds(1000L, 2000L);
        assertTrue(issueInputParameters.isFieldPresent(IssueFieldConstants.FIX_FOR_VERSIONS));

        Map<String, String[]> actionParameters = issueInputParameters.getActionParameters();
        assertNull(actionParameters.get("fieldsToForcePresent"));
        assertFalse(issueInputParameters.isFieldPresent(IssueFieldConstants.COMPONENTS));
        issueInputParameters.addFieldToForcePresent(IssueFieldConstants.COMPONENTS);
        assertTrue(issueInputParameters.isFieldPresent(IssueFieldConstants.COMPONENTS));

        assertArrayEquals(actionParameters.get("fieldsToForcePresent"), new String[] {IssueFieldConstants.COMPONENTS});
    }
    
    private String getSingleStringValue(String[] values)
    {
        return values[0];
    }

    private IssueInputParametersImpl createLegacyIssueInputParameters()
    {
        return new IssueInputParametersImpl()
        {
            @Override
            boolean isInLegacyTimetrackingMode()
            {
                return true;
            }

            @Override
            Long convertDurationToMins(String duration) throws InvalidDurationException
            {
                return Long.valueOf(duration);
            }
        };
    }

    private IssueInputParametersImpl createModernIssueInputParameters()
    {
        return new IssueInputParametersImpl()
        {
            @Override
            boolean isInLegacyTimetrackingMode()
            {
                return false;
            }

            @Override
            Long convertDurationToMins(String duration) throws InvalidDurationException
            {
                return Long.valueOf(duration);
            }
        };
    }

}
