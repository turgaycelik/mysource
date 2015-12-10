package com.atlassian.jira.functest.framework.admin;

/**
 * Time tracking configuration in JIRA's Administration interface.
 *
 * @since v4.0
 */
public interface TimeTracking
{
    String TIME_TRACKING_ADMIN_PAGE = "/secure/admin/jira/TimeTrackingAdmin!default.jspa";

    /**
     * <p>Activate time tracking with default options.</p> <p><em>NOTE</em>: You need to call {@link
     * com.atlassian.jira.functest.framework.admin.TimeTracking#disable()} if Time Tracking is already enabled.
     *
     * @param mode The time tracking mode to be set.
     */
    void enable(Mode mode);

    /**
     * Short-hand for disabling and then enabling Time Tracking into the specified mode.
     *
     * @param mode the mode to enable.
     */
    void switchMode(Mode mode);

    /**
     * <p>Activate time tracking with default options.</p> <p><em>NOTE</em>: You need to call {@link
     * com.atlassian.jira.functest.framework.admin.TimeTracking#disable()} if Time Tracking is already enabled.
     *
     * @param format The time tracking format to be set.
     */
    void enable(Format format);

    /**
     * <p>Disables Time Tracking</p>. <p>NOTE: You need to call this before enabling Time Tracking if it is already
     * enabled.</p>
     */
    void disable();

    /**
     * <p>Activate time tracking with non-default options.</p> <p><em>NOTE</em>: You need to call {@link
     * com.atlassian.jira.functest.framework.admin.TimeTracking#disable()} if Time Tracking is already enabled.
     *
     * @param hoursPerDay the number of hours in a day. specified as a string so you can use "5.5"
     * @param daysPerWeek the number of days in a week. specified as a string so you can use "3.5"
     * @param format the display format used ("pretty", "days", "hours")
     * @param defaultUnit default unit for time tracking entry ("minute", "hour", "day", "week")
     * @param mode The time tracking mode to be set. It can be MODERN (original estimate and remaining estimate can be
     * edited independently), and LEGACY (original estimate can only be edited before logging work and after that you
     * can only edit the remaining estimate).
     */
    void enable(String hoursPerDay, String daysPerWeek, String format, String defaultUnit, final Mode mode);

    /**
     * <p>Activate time tracking with non-default options.</p> <p><em>NOTE</em>: You need to call {@link
     * com.atlassian.jira.functest.framework.admin.TimeTracking#disable()} if Time Tracking is already enabled.
     *
     * @param hoursPerDay the number of hours in a day. specified as a string so you can use "5.5"
     * @param daysPerWeek the number of days in a week. specified as a string so you can use "3.5"
     * @param format the display format used ("pretty", "days", "hours")
     * @param defaultUnit default unit for time tracking entry ("minute", "hour", "day", "week")
     * @param mode The time tracking mode to be set. It can be MODERN (original estimate and remaining estimate can be
     * edited independently), and LEGACY (original estimate can only be edited before logging work and after that you
     * can only edit the remaining estimate).
     */
    void enable(String hoursPerDay, String daysPerWeek, Format format, Unit defaultUnit, final Mode mode);

    /**
     * Whether Time tracking is active in a specified mode. <p><em>NOTE:</em> If time tracking is disabled this method
     * will return false.</p>
     *
     * @param mode The mode to check.
     * @return true if time tracking is on in the specified mode; otherwise, it returns false.
     */
    boolean isIn(Mode mode);

    /**
     * Disables and re-enables time tracking with the "Copying of comments to work description" disabled. This will mean
     * that when logging work on transition, the comment input will only be used for comments and not work descriptions.
     *
     * @since v4.2
     */
    void disableCopyingOfComments();


    /**
     * Short-hand for disabling and then enabling Time Tracking using the specified fromat.
     *
     * @param format the fromat to enable.
     */
    void switchFormat(Format format);

    /**
     * <p>Represents the "format" which Time Tracking information can be displayed in.</p>
     * <dl>
     * <dt>PRETTY</dt>
     * <dd>e.g. "2 days, 4 hours, 30 minutes"</dd>
     * <dt>DAYS</dt>
     * <dd>e.g. "2d 4.5h"</dd>
     * <dt>HOURS</dt>
     * <dd>e.g. "52.5h"</dd>
     * </dl>
     */
    enum Format
    {
        PRETTY, DAYS, HOURS
    }

    /**
     * <p>Represents a "mode" in which the Time Tracking module operates.</p> The current modes are: 
     * <dl>
     * <dt>MODERN</dt>
     * <dd>In this mode, the Original and Remaining estimate can be edited independently.</dd>
     * <dt>LEGACY</dt>
     * <dd>In this mode, the Original Estimate can only be edited before logging work, and after that the user
     * can only edit the Remaining Estimate. This is being kept for backwards compatibility</dd>
     * </dl>
     */
    enum Mode
    {
        MODERN("Modern Mode"),
        LEGACY("Legacy Mode");

        private final String value;

        static final String LEGACY_MODE_TEXT = "Legacy mode is currently <b>ON</b>.";

        Mode(final String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return value;
        }
    }

    /**
     * Represnts the default unit to use for time tracking.
     */
    enum Unit
    {
        MINUTE, HOUR, DAY, WEEK
    }
}