package com.atlassian.jira.webtests;

import com.atlassian.jira.functest.framework.Administration;
import com.atlassian.jira.functest.framework.FuncTestHelperFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractTestIssueNavigatorView extends JIRAWebTest
{
    protected static final String XML_TITLE = "Your Company JIRA";
    protected static final String XML_LINK_COMMENT = "/secure/IssueNavigator.jspa?reset=true&amp;jqlQuery=";
    protected static final String XML_LINK_MULTIPLE = "/secure/IssueNavigator.jspa?reset=true&amp;jqlQuery=project+%3D+HSP";
    protected static final String XML_DESCRIPTION_MULTIPLE = "An XML representation of a search request";
    protected static final String XML_DESCRIPTION_SINGLE = "This file is an XML representation of an issue";
    protected static final String XML_LANGUAGE = "en-uk";

    protected static final String ATT_DATE_CREATED = "created";
    protected static final String ATT_LAST_VIEWED = "lastViewed";
    protected static final String ATT_DATE_UPDATED = "updated";
    protected static final String ATT_DATE_DUE = "due";
    protected static final String ATT_DATE_RESOLVED = "resolved";
    protected static final String ATT_TITLE = "title";
    protected static final String ATT_LINK = "link";
    protected static final String ATT_DESCRIPTION = "description";
    protected static final String ATT_ENVIRONMENT = "environment";
    protected static final String ATT_KEY = "key";
    protected static final String ATT_SUMMARY = "summary";
    protected static final String ATT_TYPE = "type";
    protected static final String ATT_PRIORITY = "priority";
    protected static final String ATT_STATUS = "status";
    protected static final String ATT_RESOLUTION = "resolution";
    protected static final String ATT_ASSIGNEE = "assignee";
    protected static final String ATT_REPORTER = "reporter";
    protected static final String ATT_CREATOR = "creator";
    protected static final String ATT_VERSION = "version";
    protected static final String ATT_FIX_VERSION = "fixVersion";
    protected static final String ATT_REMAINING_ESTIMATE = "timeestimate";
    protected static final String ATT_REMAINING_ESTIMATE_HOURS = "timeestimateHours";
    protected static final String ATT_REMAINING_ESTIMATE_DAYS = "timeestimateDays";
    protected static final String ATT_TIMEORIGINALESTIMATE = "timeoriginalestimate";
    protected static final String ATT_TIMEORIGINALESTIMATE_HOURS = "timeoriginalestimateHours";
    protected static final String ATT_TIMEORIGINALESTIMATE_DAYS = "timeoriginalestimateDays";
    protected static final String ATT_VOTES = "votes";
    protected static final String ATT_WATCHES = "watches";
    protected static final String ATT_WORK_RATIO = "workRatio";
    protected static final String ATT_SECURITY_LEVEL = "securityLevel";

    protected static final String TYPE_DATETIME = "com.atlassian.jira.plugin.system.customfieldtypes:datetime";
    protected static final String TYPE_DATEPICKER = "com.atlassian.jira.plugin.system.customfieldtypes:datepicker";
    protected static final String TYPE_CASCADINGSELECT = "com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect";

    protected static final String ISSUE_TYPE = "issueType";
    protected static final String ISSUE_KEY = "issueKey";
    protected static final String ISSUE_SUMMARY = "issueSummary";
    protected static final String ISSUE_ASSIGNEE = "issueAssignee";
    protected static final String ISSUE_REPORTER = "issueReporter";
    protected static final String ISSUE_CREATOR = "issueCreator";
    protected static final String ISSUE_PRIORITY = "issuePriority";
    protected static final String ISSUE_STATUS = "issueStatus";
    protected static final String ISSUE_RESOLUTION = "issueResolution";
    protected static final String ISSUE_CREATED = "issueCreated";
    protected static final String ISSUE_LAST_VIEWED = "lastViewed";
    protected static final String ISSUE_UPDATED = "issueUpdated";
    protected static final String LAST_VIEWED = "lastViewed";
    protected static final String ISSUE_DUE = "issueDue";
    protected static final String ISSUE_RESOLVED = "issueResolved";
    protected static final String ISSUE_AFFECTS_VERSIONS = "issueAffectsVersions";
    protected static final String ISSUE_CASCADING_SELECT_FIELD = "issueCascadingSelectField";
    protected static final String ISSUE_SECURITY_LEVEL = "issueSecurityLevel";
    protected static final String ISSUE_COMPONENTS = "issueComponents";
    protected static final String ISSUE_DATE_PICKER_FIELD = "issueDatePickerField";
    protected static final String ISSUE_DATE_TIME_FIELD = "issueDateTimeField";
    protected static final String ISSUE_DESCRIPTION = "issueDescription";
    protected static final String ISSUE_ENVIRONMENT = "issueEnvironment";
    protected static final String ISSUE_FIX_VERSIONS = "issueFixVersions";
    protected static final String ISSUE_FREE_TEXT_FIELD = "issueFreeTextField";
    protected static final String ISSUE_GROUP_PICKER_FIELD = "issueGroupPickerField";
    protected static final String ISSUE_IMAGES = "issueImages";
    protected static final String ISSUE_IMPORT_ID_FIELD = "issueImportIdField";
    protected static final String ISSUE_LINKS = "issueLinks";
    protected static final String ISSUE_MULTI_CHECKBOXES_FIELD = "issueMultiCheckboxesField";
    protected static final String ISSUE_MULTI_GROUP_PICKER_FIELD = "issueMultiGroupPickerField";
    protected static final String ISSUE_MULTI_SELECT_FIELD = "issueMultiSelectField";
    protected static final String ISSUE_MULTI_USER_PICKER_FIELD = "issueMultiUserPickerField";
    protected static final String ISSUE_NUMBER_FIELD = "issueNumberField";
    protected static final String ISSUE_ORIGINAL_ESTIMATE = "issueOriginalEstimate";
    protected static final String ISSUE_PROJECT = "issueProject";
    protected static final String ISSUE_PROJECT_PICKER_FIELD = "issueProjectPickerField";
    protected static final String ISSUE_ROTEXT_FIELD = "issueROTextField";
    protected static final String ISSUE_RADIO_BUTTONS_FIELD = "issueRadioButtonsField";
    protected static final String ISSUE_REMAINING_ESTIMATE = "issueRemainingEstimate";
    protected static final String ISSUE_SELECT_LIST = "issueSelectList";
    protected static final String ISSUE_TEXT_FIELD255 = "issueTextField255";
    protected static final String ISSUE_TIME_SPENT = "issueTimeSpent";
    protected static final String ISSUE_URL_FIELD = "issueURLField";
    protected static final String ISSUE_USER_PICKER_FIELD = "issueUserPickerField";
    protected static final String ISSUE_VOTES = "issueVotes";
    protected static final String ISSUE_WATCHES = "issueWatches";
    protected static final String ISSUE_SINGLE_VERSION_PICKER_FIELD = "issueSingleVersionPickerField";
    protected static final String ISSUE_VERSION_PICKER_FIELD = "issueVersionPickerField";
    protected static final String ISSUE_PROGRESS = "issueTimeTrackingProgress";
    protected static final String ISSUE_LABELS = "issueLabels";

    protected static final String ISSUE_WORK_RATIO = "issueWorkRatio";

    protected static final String NOT_TESTED = "NOT TESTED";
    protected final List<String> issueFieldColumnMap = new ArrayList<String>();

    protected static final String PRIORITY_MAJOR = "Major";
    protected static final String PRIORITY_MINOR = "Minor";
    protected static final String PRIORITY_TRIVIAL = "Trivial";
    protected static final Map<String, String> ISSUE_PRIORITY_IMAGE_MAP = new HashMap<String, String>();

    static
    {
        ISSUE_PRIORITY_IMAGE_MAP.put(PRIORITY_MAJOR, "priorities/major.png");
        ISSUE_PRIORITY_IMAGE_MAP.put(PRIORITY_MINOR, "priorities/minor.png");
        ISSUE_PRIORITY_IMAGE_MAP.put(PRIORITY_TRIVIAL, "priorities/trivial.png");
    }

    protected final List<Item> items = new ArrayList<Item>();
    protected Item item1;
    protected Item item2;
    protected Item item3;
    protected final List<Item> commentItems = new ArrayList<Item>();
    protected Item commentItem1;
    protected Item commentItem2;
    protected Item commentItem3;
    protected Item commentItem4;
    protected String timeFormat;
    protected Administration administration;

    protected static final String CF_SINGLE_VERSION_PICKER_FIELD = "SingleVersionPickerField";
    protected static final String CF_DATE_TIME_FIELD = "DateTimeField";
    protected static final String CF_DATE_PICKER_FIELD = "DatePickerField";
    protected static final String CF_SELECT_LIST = "SelectList";
    protected static final String CF_NUMBER_FIELD = "NumberField";
    protected static final String CF_CASCADING_SELECT_FIELD = "CascadingSelectField";
    protected static final String CF_MULTI_SELECT_FIELD = "MultiSelectField";
    protected static final String CF_PROJECT_PICKER_FIELD = "ProjectPickerField";
    protected static final String CF_VERSION_PICKER_FIELD = "VersionPickerField";
    protected static final String CF_MULTI_CHECKBOXES_FIELD = "MultiCheckboxesField";
    protected static final String CF_GROUP_PICKER_FIELD = "GroupPickerField";
    protected static final String CF_URLFIELD = "URLField";
    protected static final String CF_RADIO_BUTTONS_FIELD = "RadioButtonsField";
    protected static final String CF_MULTI_GROUP_PICKER_FIELD = "MultiGroupPickerField";
    protected static final String CF_MULTI_USER_PICKER_FIELD = "MultiUserPickerField";
    protected static final String CF_USER_PICKER_FIELD = "UserPickerField";
    protected static final String CF_FREE_TEXT_FIELD = "FreeTextField";
    protected static final String CF_TEXT_FIELD255 = "TextField255";
    protected static final String CF_RO_TEXT_FIELD = "ROTextField";
    protected static final String CF_IMPORT_ID_FIELD = "ImportIdField";
    protected static final String ATT_PROJECT = "project";
    protected static final String ATT_TIMESPENT = "timespent";
    protected static final String ATT_TIMESPENT_DAYS = "timespentDays";
    protected static final String ATT_TIMESPENT_HOURS = "timespentHours";
    protected static final String PICTURE_ATTACHMENT_JPG = "picture-attachment.jpg";

    public AbstractTestIssueNavigatorView(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        final FuncTestHelperFactory funcTestHelperFactory = new FuncTestHelperFactory(tester, getEnvironmentData());
        administration = funcTestHelperFactory.getAdministration();
        log("Importing TestSearchRequestViewsAndIssueViews.xml");
        restoreData("TestSearchRequestViewsAndIssueViews.xml");

        administration.generalConfiguration().setJiraLocale("English (UK)");
        //enable attachments.
        administration.attachments().enable();

        initTestData();
    }

    public void reconfigureTimetracking(String format)
    {
        super.reconfigureTimetracking(format);
        timeFormat = format;
    }

    /**
     * Convert the given issue into a subtask of the given parent issue.
     *
     * @param issueKey  issue key
     * @param parentKey parent issue key
     */
    protected void subTaskify(String issueKey, String parentKey)
    {
        gotoIssue(issueKey);
        clickLink("issue-to-subtask");
        setFormElement("parentIssueKey", parentKey);
        submit("Next >>");
        submit("Next >>");
        submit("Finish");
    }

    /**
     * Just a helper bean to keep the data encapsulated for each item
     */
    protected static class Item
    {

        private Map<String, String> attributeMap = new HashMap<String, String>();
        private Map<String, Map<String, String>> attributeAttributesMap = new HashMap<String, Map<String, String>>();
        private AbstractTestIssueNavigatorView.IssueLinks links;
        private List<String> attachments = new ArrayList<String>();
        private List<Comment> comments = new ArrayList<Comment>();
        private List<String> components = new ArrayList<String>();
        private List<CustomField> customFields = new ArrayList<CustomField>();
        private Map<String, CustomField> customFieldByName = new HashMap<String, CustomField>();

        public CustomField getCustomFieldByName(String name)
        {
            return customFieldByName.get(name);
        }

        public String getAttribute(String name)
        {
            return attributeMap.get(name);
        }

        public void setAttribute(String name, String value)
        {
            attributeMap.put(name, value);
        }

        public void setAttribute(String name, String value, Map<String, String> attributes)
        {
            attributeMap.put(name, value);
            Map<String, String> m = attributeAttributesMap.get(name);
            if (m == null)
            {
                m = new HashMap<String, String>();
                attributeAttributesMap.put(name, m);
            }
            m.putAll(attributes);
        }

        public void setIssueLinks(AbstractTestIssueNavigatorView.IssueLinks links)
        {
            this.links = links;
        }

        public IssueLinks getLinks()
        {
            return links;
        }

        public void addAttachment(String attachment)
        {
            attachments.add(attachment);
        }

        public void addComment(AbstractTestIssueNavigatorView.Comment comment)
        {
            comments.add(comment);
        }

        public void addComponent(String component)
        {
            components.add(component);
        }

        public void addCustomField(AbstractTestIssueNavigatorView.CustomField customField)
        {
            customFields.add(customField);
            customFieldByName.put(customField.getName(), customField);
        }

        public List<String> getAttachments()
        {
            return new ArrayList<String>(attachments);
        }

        public List<Comment> getComments()
        {
            return new ArrayList<Comment>(comments);
        }

        public List<String> getComponents()
        {
            return new ArrayList<String>(components);
        }

        public List<CustomField> getCustomFields()
        {
            return new ArrayList<CustomField>(customFields);
        }

        public Map getAttributeMap()
        {
            return Collections.unmodifiableMap(attributeMap);
        }

        public Map<String, String> getAttributeAttributesMap(String attributeName)
        {
            final Map<String, String> map = attributeAttributesMap.get(attributeName);
            return map == null ? Collections.<String, String>emptyMap() : Collections.unmodifiableMap(map);
        }

        /**
         * Returns a map of <String> attribute key to a Map<String,String> attName-attValue
         * @return all attributes map, never null
         */
        public Map getAllAttributeAttributesMap()
        {
            return Collections.unmodifiableMap(attributeAttributesMap);
        }

    }

    public static class CustomField
    {

        public static class Value
        {
            private final String value;
            private final String displayValue;
            private final String link;

            public static Value[] build(String[] values)
            {
                Value[] retValues = new Value[values.length];
                for (int i = 0; i < values.length; i++)
                {
                    retValues[i] = new Value(values[i]);
                }
                return retValues;
            }

            public Value(String value)
            {
                this(value, value, null);
            }

            public Value(String value, String displayValue)
            {
                this(value, displayValue, null);
            }

            public Value(String value, String displayValue, String link)
            {
                this.value = value;
                this.displayValue = displayValue;
                this.link = link;
            }

            public String getValue()
            {
                return value;
            }

            public String getDisplayValue()
            {
                return displayValue;
            }

            public String getLink()
            {
                return link;
            }
        }

        private final String id;
        private final String key;
        private final String name;
        private final List<Value> values = new ArrayList<Value>();

        public CustomField(String id, String key, String name, Value value)
        {
            this.id = id;
            this.key = key;
            this.name = name;
            this.values.add(value);
        }

        public CustomField(String id, String key, String name, Value[] values)
        {
            this.id = id;
            this.key = key;
            this.name = name;
            this.values.addAll(Arrays.asList(values));
        }

        public String getId()
        {
            return id;
        }

        public String getKey()
        {
            return key;
        }

        public String getName()
        {
            return name;
        }

        public List<Value> getValues()
        {
            return new ArrayList<Value>(values);
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder("CustomField[");
            sb.append("\n  id = ");
            sb.append(id);
            sb.append("\n  key = ");
            sb.append(key);
            sb.append("\n  name = ");
            sb.append(name);
            for (Value value : values)
            {
                sb.append("\n  value = ");
                sb.append(value.getValue());
                sb.append("\n  displayValue = ");
                sb.append(value.getDisplayValue());
                sb.append("\n  link = ");
                sb.append(value.getLink());
            }
            sb.append("\n]");
            return sb.toString();
        }
    }

    protected static class Comment
    {
        private String author;
        private String created;
        private String level;
        private String value;

        public Comment(String author, String created, String level, String value)
        {
            this.author = author;
            this.created = created;
            this.level = level;
            this.value = value;
        }

        public String getAuthor()
        {
            return author;
        }

        public String getCreated()
        {
            return created;
        }

        public String getLevel()
        {
            return level;
        }

        public String getValue()
        {
            return value;
        }

    }

    protected static class IssueLinks
    {
        private String id;
        private String name;
        private String inDesc;
        private String outDesc;
        private List<IssueLink> inLinks = new ArrayList<IssueLink>();
        private List<IssueLink> outLinks = new ArrayList<IssueLink>();

        public void addInLink(AbstractTestIssueNavigatorView.IssueLink link)
        {
            inLinks.add(link);
        }

        public void addOutLink(AbstractTestIssueNavigatorView.IssueLink link)
        {
            outLinks.add(link);
        }

        public String getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public String getInDesc()
        {
            return inDesc;
        }

        public String getOutDesc()
        {
            return outDesc;
        }

        public List getInLinks()
        {
            return inLinks;
        }

        public List getOutLinks()
        {
            return outLinks;
        }

    }

    protected static class IssueLink
    {
        private final String id;
        private final String link;
        private final String url;

        public IssueLink(String id, String link, String url)
        {
            this.id = id;
            this.link = link;
            this.url = url;
        }

        public String getId()
        {
            return id;
        }

        public String getLink()
        {
            return link;
        }

        public String getUrl()
        {
            return url;
        }
    }

    private void initTestData()
    {
        // third issue
        items.add(item3 = createItem3());

        // second issue
        items.add(item2 = createItem2());

        // first issue
        items.add(item1 = createItem1());

        {
            URL baseUrl = getEnvironmentData().getBaseUrl();
            // Add a couple of comment items that will be checked for the comment view
            commentItem1 = new Item();
            commentItem1.setAttribute(ATT_TITLE, "RE: [HSP-11] Minor Bug 01");
            commentItem1.setAttribute(ATT_LINK, baseUrl + "/browse/HSP-11?focusedCommentId=10001#comment-10001");
            commentItem1.setAttribute("pubDate", "Wed, 5 Apr 2006 16:03:40");
            commentItem1.setAttribute(ATT_DESCRIPTION, "jira-developers");
            commentItem1.setAttribute("description_link_profile", baseUrl + "/secure/ViewProfile.jspa?name=admin");
            commentItem1.setAttribute("description_link_issue", baseUrl + "/browse/HSP-11");
            commentItems.add(commentItem1);

            commentItem2 = new Item();
            commentItem2.setAttribute(ATT_TITLE, "RE: [HSP-11] Minor Bug 01");
            commentItem2.setAttribute(ATT_LINK, baseUrl + "/browse/HSP-11?focusedCommentId=10010#comment-10010");
            commentItem2.setAttribute("pubDate", "Thu, 13 Apr 2006 18:48:55");
            commentItem2.setAttribute(ATT_DESCRIPTION, "no comment");
            commentItem2.setAttribute("description_link_profile", baseUrl + "/secure/ViewProfile.jspa?name=admin");
            commentItem2.setAttribute("description_link_issue", baseUrl + "/browse/HSP-11");
            commentItems.add(commentItem2);

            commentItem3 = new Item();
            commentItem3.setAttribute(ATT_TITLE, "RE: [HSP-11] Minor Bug 01");
            commentItem3.setAttribute(ATT_LINK, baseUrl + "/browse/HSP-11?focusedCommentId=10000#comment-10000");
            commentItem3.setAttribute("pubDate", "Wed, 5 Apr 2006 16:03:19");
            commentItem3.setAttribute(ATT_DESCRIPTION, "This is my first comment");
            commentItem3.setAttribute("description_link_profile", baseUrl + "/secure/ViewProfile.jspa?name=admin");
            commentItem3.setAttribute("description_link_issue", baseUrl + "/browse/HSP-11");
            commentItems.add(commentItem3);


            // only used for testing the date ranges in the RSS comments feed (TestIssueNavigatorRssCommentsFeed.xml).
            // Therefore this isn't added to the commentItems list.
            commentItem4 = new Item();
            commentItem4.setAttribute(ATT_TITLE, "RE: [HSP-10] Big 01");
            commentItem4.setAttribute(ATT_LINK, baseUrl + "/browse/HSP-10?focusedCommentId=10020#comment-10020");
            commentItem4.setAttribute("pubDate", "Tue, 21 Nov 2006 12:27:15");
            commentItem4.setAttribute(ATT_DESCRIPTION, "New Comment 1");
            commentItem4.setAttribute("description_link_profile", baseUrl + "/secure/ViewProfile.jspa?name=admin");
            commentItem4.setAttribute("description_link_issue", baseUrl + "/browse/HSP-10");
        }

        initFieldColumnMap();
    }

    protected Item createItem1()
    {
        URL baseUrl = getEnvironmentData().getBaseUrl();

        final Map<String, String> typeAttributes = new HashMap<String, String>();
        typeAttributes.put("iconUrl", baseUrl + "/images/icons/issuetypes/newfeature.png");
        final Map<String, String> priorityAttributes = new HashMap<String, String>();
        priorityAttributes.put("iconUrl", baseUrl + "/images/icons/priorities/trivial.png");
        final Map<String, String> statusAttributes = new HashMap<String, String>();
        statusAttributes.put("iconUrl", baseUrl + "/images/icons/statuses/open.png");

        Item item = new Item();
        item.setAttribute(ATT_TITLE, "[HSP-12] Feature 00");
        item.setAttribute(ATT_LINK, baseUrl + "/browse/HSP-12");
        item.setAttribute(ATT_DESCRIPTION, "");
        item.setAttribute(ATT_ENVIRONMENT, "");
        item.setAttribute(ATT_KEY, "HSP-12");
        item.setAttribute(ATT_SUMMARY, "Feature 00");
        item.setAttribute(ATT_TYPE, "New Feature", typeAttributes);
        item.setAttribute(ATT_PRIORITY, PRIORITY_TRIVIAL, priorityAttributes);
        item.setAttribute(ATT_STATUS, "Open", statusAttributes);
        item.setAttribute(ATT_RESOLUTION, "Unresolved");
        item.setAttribute(ATT_ASSIGNEE, "developer");
        item.setAttribute(ATT_REPORTER, ADMIN_FULLNAME);
        item.setAttribute(ATT_CREATOR, "Administrator");
        item.setAttribute(ATT_DATE_CREATED, NOT_TESTED);
        item.setAttribute(ATT_DATE_UPDATED, NOT_TESTED);
        item.setAttribute(ATT_FIX_VERSION, "New Version 5");
        item.setAttribute(ATT_DATE_DUE, "");
        item.setAttribute(ATT_VOTES, "0");
        item.setAttribute(ATT_WATCHES, "0");

        IssueLinks links = new IssueLinks();
        links.id = "10000";
        links.name = "Duplicate";
        links.outDesc = "duplicates";
        links.addOutLink(new IssueLink("10010", "HSP-10", baseUrl + "/browse/HSP-10"));
        item.setIssueLinks(links);

        item.addCustomField(new CustomField("customfield_10017",
            "com.atlassian.jira.plugin.system.customfieldtypes:version",
            CF_SINGLE_VERSION_PICKER_FIELD,
            new CustomField.Value("10000", "New Version 5", baseUrl + "/secure/IssueNavigator.jspa?reset=true&mode=hide&sorter/order=ASC&sorter/field=priority&pid=10000&customfield_10017=10000")));
        item.addCustomField(new CustomField("customfield_10007",
            "com.atlassian.jira.plugin.system.customfieldtypes:select", CF_SELECT_LIST,
            new CustomField.Value("option 01")));
        item.addCustomField(new CustomField("customfield_10000",
            TYPE_CASCADINGSELECT,
            CF_CASCADING_SELECT_FIELD,
            new CustomField.Value[]{new CustomField.Value("10000", "value 01"), new CustomField.Value("10007", "value 013")}));
        item.addCustomField(new CustomField("customfield_10004",
            "com.atlassian.jira.plugin.system.customfieldtypes:multiselect",
            CF_MULTI_SELECT_FIELD,
            CustomField.Value.build(new String[]{"option 00", "option 01", "option 02", "option 03"})));
        item.addCustomField(new CustomField("customfield_10019",
            "com.atlassian.jira.plugin.system.customfieldtypes:multiversion",
            CF_VERSION_PICKER_FIELD, new CustomField.Value[]{
            new CustomField.Value("10003", "New Version 2",
                baseUrl + "/secure/IssueNavigator.jspa?reset=true&mode=hide&sorter/order=ASC&sorter/field=priority&pid=10000&customfield_10019=10003"),
            new CustomField.Value("10001", "New Version 4",
                baseUrl + "/secure/IssueNavigator.jspa?reset=true&mode=hide&sorter/order=ASC&sorter/field=priority&pid=10000&customfield_10019=10001")}));
        item.addCustomField(new CustomField("customfield_10003",
            "com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes",
            CF_MULTI_CHECKBOXES_FIELD,
            CustomField.Value.build(new String[]{"option 00", "option 01", "option 02", "option 03"})));
        return item;
    }

    protected Item createItem2()
    {
        URL baseUrl = getEnvironmentData().getBaseUrl();

        final Map<String, String> typeAttributes = new HashMap<String, String>();
        typeAttributes.put("iconUrl", baseUrl + "/images/icons/issuetypes/bug.png");
        final Map<String, String> priorityAttributes = new HashMap<String, String>();
        priorityAttributes.put("iconUrl", baseUrl + "/images/icons/priorities/minor.png");
        final Map<String, String> statusAttributes = new HashMap<String, String>();
        statusAttributes.put("iconUrl", baseUrl + "/images/icons/statuses/inprogress.png");

        Item item = new Item();
        item.setAttribute(ATT_TITLE, "[HSP-11] Minor Bug 01");
        item.setAttribute(ATT_LINK, baseUrl + "/browse/HSP-11");
        item.setAttribute(ATT_DESCRIPTION, "");
        item.setAttribute(ATT_ENVIRONMENT, "");
        item.setAttribute(ATT_KEY, "HSP-11");
        item.setAttribute(ATT_SUMMARY, "Minor Bug 01");
        item.setAttribute(ATT_TYPE, "Bug", typeAttributes);
        item.setAttribute(ATT_PRIORITY, PRIORITY_MINOR, priorityAttributes);
        item.setAttribute(ATT_STATUS, "In Progress", statusAttributes);
        item.setAttribute(ATT_RESOLUTION, "Unresolved");
        item.setAttribute(ATT_ASSIGNEE, ADMIN_FULLNAME);
        item.setAttribute(ATT_REPORTER, ADMIN_FULLNAME);
        item.setAttribute(ATT_CREATOR, "Administrator");
        item.setAttribute(ATT_DATE_CREATED, NOT_TESTED);
        item.setAttribute(ATT_DATE_UPDATED, NOT_TESTED);
        item.setAttribute(ATT_VERSION, "New Version 2");
        item.setAttribute(ATT_FIX_VERSION, "New Version 4");
        item.setAttribute(ATT_REMAINING_ESTIMATE, "30 minutes");
        item.setAttribute(ATT_REMAINING_ESTIMATE_DAYS, "0.5h");
        item.setAttribute(ATT_REMAINING_ESTIMATE_HOURS, "0.5h");
        item.setAttribute(ATT_TIMESPENT, "3 hours, 20 minutes");
        item.setAttribute(ATT_TIMESPENT_DAYS, "3h 20m");
        item.setAttribute(ATT_TIMESPENT_HOURS, "3h 20m");
        item.setAttribute(ATT_DATE_DUE, "");
        item.setAttribute(ATT_VOTES, "0");

        IssueLinks links = new IssueLinks();
        links.id = "10000";
        links.name = "Duplicate";
        links.outDesc = "duplicates";
        links.addOutLink(new IssueLink("10010", "HSP-10", baseUrl + "/browse/HSP-10"));
        links.inDesc = "is duplicated by";
        links.addInLink(new IssueLink("10010", "HSP-10", baseUrl + "/browse/HSP-10"));
        item.setIssueLinks(links);
        item.addComponent("New Component 1");
        item.addComponent("New Component 2");
        item.addComment(new Comment(ADMIN_USERNAME, NOT_TESTED, "", "This is my first comment"));
        item.addComment(new Comment(ADMIN_USERNAME, NOT_TESTED, "jira-developers", "Developers, developers, developers!"));
        item.addCustomField(new CustomField("customfield_10017",
            "com.atlassian.jira.plugin.system.customfieldtypes:version",
            CF_SINGLE_VERSION_PICKER_FIELD,
            new CustomField.Value("10000", "New Version 5", baseUrl + "/secure/IssueNavigator.jspa?reset=true&mode=hide&sorter/order=ASC&sorter/field=priority&pid=10000&customfield_10017=10000")));
        item.addCustomField(new CustomField("customfield_10007",
            "com.atlassian.jira.plugin.system.customfieldtypes:select", CF_SELECT_LIST,
            new CustomField.Value("option 00")));
        item.addCustomField(new CustomField("customfield_10000",
            "com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect",
            CF_CASCADING_SELECT_FIELD,
            new CustomField.Value("10004", "value 05")));
        item.addCustomField(new CustomField("customfield_10004",
            "com.atlassian.jira.plugin.system.customfieldtypes:multiselect",
            CF_MULTI_SELECT_FIELD,
            new CustomField.Value("option 00")));
        item.addCustomField(new CustomField("customfield_10015",
            "com.atlassian.jira.plugin.system.customfieldtypes:project",
            CF_PROJECT_PICKER_FIELD,
            new CustomField.Value("10000", "homosapien", baseUrl + "/browse/HSP")));
        item.addCustomField(new CustomField("customfield_10019",
            "com.atlassian.jira.plugin.system.customfieldtypes:multiversion",
            CF_VERSION_PICKER_FIELD,
            new CustomField.Value("10000", "New Version 5", baseUrl + "/secure/IssueNavigator.jspa?reset=true&mode=hide&sorter/order=ASC&sorter/field=priority&pid=10000&customfield_10019=10000")));
        item.addCustomField(new CustomField("customfield_10003",
            "com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes",
            CF_MULTI_CHECKBOXES_FIELD,
            new CustomField.Value("option 00")));
        item.addCustomField(new CustomField("customfield_10006",
            "com.atlassian.jira.plugin.system.customfieldtypes:radiobuttons",
            CF_RADIO_BUTTONS_FIELD,
            new CustomField.Value("option 00")));
        return item;
    }

    protected Item createItem3()
    {
        URL baseUrl = getEnvironmentData().getBaseUrl();

        final Map<String, String> typeAttributes = new HashMap<String, String>();
        typeAttributes.put("iconUrl", baseUrl + "/images/icons/issuetypes/bug.png");
        final Map<String, String> priorityAttributes = new HashMap<String, String>();
        priorityAttributes.put("iconUrl", baseUrl + "/images/icons/priorities/major.png");
        final Map<String, String> statusAttributes = new HashMap<String, String>();
        statusAttributes.put("iconUrl", baseUrl + "/images/icons/statuses/resolved.png");

        Item item = new Item();
        item.setAttribute(ATT_TITLE, "[HSP-10] Big 01");
        item.setAttribute(ATT_LINK, baseUrl + "/browse/HSP-10");
        item.setAttribute(ATT_DESCRIPTION, "Some description");
        item.setAttribute(ATT_ENVIRONMENT, "MS Windows XP");
        item.setAttribute(ATT_KEY, "HSP-10");
        item.setAttribute(ATT_SUMMARY, "Big 01");
        item.setAttribute(ATT_TYPE, "Bug", typeAttributes);
        item.setAttribute(ATT_PRIORITY, PRIORITY_MAJOR, priorityAttributes);
        item.setAttribute(ATT_STATUS, "Resolved", statusAttributes);
        item.setAttribute(ATT_RESOLUTION, "Fixed");
        item.setAttribute(ATT_ASSIGNEE, "developer");
        item.setAttribute(ATT_REPORTER, ADMIN_FULLNAME);
        item.setAttribute(ATT_CREATOR, "Administrator");
        item.setAttribute(ATT_DATE_CREATED, NOT_TESTED);
        item.setAttribute(ATT_DATE_UPDATED, NOT_TESTED);
        item.setAttribute(ATT_DATE_RESOLVED, "12/Apr/06 4:18 PM");
        item.setAttribute(ATT_VERSION, "New Version 4");
        item.setAttribute(ATT_FIX_VERSION, "New Version 5");
        item.setAttribute(ATT_DATE_DUE, NOT_TESTED);
        item.setAttribute(ATT_VOTES, "0");
        item.setAttribute(ATT_TIMEORIGINALESTIMATE, "1 day");
        item.setAttribute(ATT_TIMEORIGINALESTIMATE_DAYS, "1d");
        item.setAttribute(ATT_TIMEORIGINALESTIMATE_HOURS, "24h");
        item.setAttribute(ATT_REMAINING_ESTIMATE, "1 day");
        item.setAttribute(ATT_REMAINING_ESTIMATE_DAYS, "1d");
        item.setAttribute(ATT_REMAINING_ESTIMATE_HOURS, "24h");

        IssueLinks links = new IssueLinks();
        links.id = "10000";
        links.name = "Duplicate";
        links.outDesc = "duplicates";
        links.addOutLink(new IssueLink("10011", "HSP-11", baseUrl + "/browse/HSP-11"));
        links.inDesc = "is duplicated by";
        links.addInLink(new IssueLink("10011", "HSP-11", baseUrl + "/browse/HSP-11"));
        links.addInLink(new IssueLink("10012", "HSP-12", baseUrl + "/browse/HSP-12"));
        item.setIssueLinks(links);

        item.addAttachment(PICTURE_ATTACHMENT_JPG);

        item.addCustomField(new CustomField(
            "customfield_10017",
            "com.atlassian.jira.plugin.system.customfieldtypes:version",
            CF_SINGLE_VERSION_PICKER_FIELD,
            new CustomField.Value("10000", "New Version 5", baseUrl + "/secure/IssueNavigator.jspa?reset=true&mode=hide&sorter/order=ASC&sorter/field=priority&pid=10000&customfield_10017=10000")));
        item.addCustomField(new CustomField(
            "customfield_10001",
            TYPE_DATETIME,
            CF_DATE_TIME_FIELD,
            new CustomField.Value(NOT_TESTED, "12/Apr/06 3:55 PM")));
        item.addCustomField(new CustomField(
            "customfield_10010",
            TYPE_DATEPICKER,
            CF_DATE_PICKER_FIELD,
            new CustomField.Value(NOT_TESTED, "24/Oct/06")));
        item.addCustomField(new CustomField(
            "customfield_10007",
            "com.atlassian.jira.plugin.system.customfieldtypes:select",
            CF_SELECT_LIST,
            new CustomField.Value("option 01")));
        item.addCustomField(new CustomField(
            "customfield_10005",
            "com.atlassian.jira.plugin.system.customfieldtypes:float",
            CF_NUMBER_FIELD,
            new CustomField.Value("123.0", "123")));
        item.addCustomField(new CustomField(
            "customfield_10000",
            "com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect",
            CF_CASCADING_SELECT_FIELD,
            new CustomField.Value[]{new CustomField.Value("10000", "value 01"), new CustomField.Value("10007", "value 013")}));
        item.addCustomField(new CustomField(
            "customfield_10004",
            "com.atlassian.jira.plugin.system.customfieldtypes:multiselect",
            CF_MULTI_SELECT_FIELD,
            new CustomField.Value[]{new CustomField.Value("option 00"), new CustomField.Value("option 02")}));
        item.addCustomField(new CustomField(
            "customfield_10015",
            "com.atlassian.jira.plugin.system.customfieldtypes:project",
            CF_PROJECT_PICKER_FIELD,
            new CustomField.Value("10000", "homosapien", baseUrl + "/browse/HSP")));
        item.addCustomField(new CustomField(
            "customfield_10019",
            "com.atlassian.jira.plugin.system.customfieldtypes:multiversion",
            CF_VERSION_PICKER_FIELD,
            new CustomField.Value[]{
                new CustomField.Value("10003", "New Version 2",
                    baseUrl + "/secure/IssueNavigator.jspa?reset=true&mode=hide&sorter/order=ASC&sorter/field=priority&pid=10000&customfield_10019=10003"),
                new CustomField.Value("10001", "New Version 4",
                    baseUrl + "/secure/IssueNavigator.jspa?reset=true&mode=hide&sorter/order=ASC&sorter/field=priority&pid=10000&customfield_10019=10001")}));
        item.addCustomField(new CustomField(
            "customfield_10003",
            "com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes",
            CF_MULTI_CHECKBOXES_FIELD,
            new CustomField.Value[]{new CustomField.Value("option 01"), new CustomField.Value("option 03")}));
        item.addCustomField(new CustomField(
            "customfield_10002",
            "com.atlassian.jira.plugin.system.customfieldtypes:grouppicker",
            CF_GROUP_PICKER_FIELD,
            new CustomField.Value("jira-developers")));
        item.addCustomField(new CustomField(
            "customfield_10018",
            "com.atlassian.jira.plugin.system.customfieldtypes:url",
            CF_URLFIELD,
            new CustomField.Value("http://localhost:8080/", "http://localhost:8080/", "http://localhost:8080/")));
        item.addCustomField(new CustomField(
            "customfield_10006",
            "com.atlassian.jira.plugin.system.customfieldtypes:radiobuttons",
            CF_RADIO_BUTTONS_FIELD,
            new CustomField.Value("option 01")));
        item.addCustomField(new CustomField(
            "customfield_10013",
            "com.atlassian.jira.plugin.system.customfieldtypes:multigrouppicker",
            CF_MULTI_GROUP_PICKER_FIELD,
            new CustomField.Value[]{
                new CustomField.Value("jira-administrators", "jira-administrators", baseUrl + "/secure/admin/user/ViewGroup.jspa?name=jira-administrators"),
                new CustomField.Value("jira-developers", "jira-developers", baseUrl + "/secure/admin/user/ViewGroup.jspa?name=jira-developers")}));
        item.addCustomField(new CustomField(
            "customfield_10014",
            "com.atlassian.jira.plugin.system.customfieldtypes:multiuserpicker",
            CF_MULTI_USER_PICKER_FIELD,
            new CustomField.Value[]{
                new CustomField.Value("dev", "developer", baseUrl + "/secure/ViewProfile.jspa?name=dev"),
                new CustomField.Value("user", "user", baseUrl + "/secure/ViewProfile.jspa?name=user")}));
        item.addCustomField(new CustomField(
            "customfield_10009",
            "com.atlassian.jira.plugin.system.customfieldtypes:userpicker",
            CF_USER_PICKER_FIELD,
            new CustomField.Value[]{new CustomField.Value(ADMIN_USERNAME, ADMIN_FULLNAME, baseUrl + "/secure/ViewProfile.jspa?name=admin")}));
        item.addCustomField(new CustomField(
            "customfield_10011",
            "com.atlassian.jira.plugin.system.customfieldtypes:textarea",
            CF_FREE_TEXT_FIELD,
            new CustomField.Value("Some FreeTextfield", "Some FreeTextfield")));
        item.addCustomField(new CustomField(
            "customfield_10008",
            "com.atlassian.jira.plugin.system.customfieldtypes:textfield",
            CF_TEXT_FIELD255,
            new CustomField.Value("Textfield 255")));
        return item;
    }

    protected void initFieldColumnMap()
    {
        issueFieldColumnMap.add(ISSUE_TYPE);
        issueFieldColumnMap.add(ISSUE_KEY);
        issueFieldColumnMap.add(ISSUE_SUMMARY);
        issueFieldColumnMap.add(ISSUE_ASSIGNEE);
        issueFieldColumnMap.add(ISSUE_REPORTER);
        issueFieldColumnMap.add(ISSUE_PRIORITY);
        issueFieldColumnMap.add(ISSUE_STATUS);
        issueFieldColumnMap.add(ISSUE_RESOLUTION);
        issueFieldColumnMap.add(ISSUE_CREATED);
        issueFieldColumnMap.add(ISSUE_UPDATED);
        issueFieldColumnMap.add(ISSUE_DUE);
        issueFieldColumnMap.add(ISSUE_AFFECTS_VERSIONS);
        issueFieldColumnMap.add(ISSUE_CASCADING_SELECT_FIELD);
        issueFieldColumnMap.add(ISSUE_COMPONENTS);
        issueFieldColumnMap.add(ISSUE_DATE_PICKER_FIELD);
        issueFieldColumnMap.add(ISSUE_DATE_TIME_FIELD);
        issueFieldColumnMap.add(ISSUE_DESCRIPTION);
        issueFieldColumnMap.add(ISSUE_ENVIRONMENT);
        issueFieldColumnMap.add(ISSUE_FIX_VERSIONS);
        issueFieldColumnMap.add(ISSUE_FREE_TEXT_FIELD);
        issueFieldColumnMap.add(ISSUE_GROUP_PICKER_FIELD);
        issueFieldColumnMap.add(ISSUE_IMAGES);
        issueFieldColumnMap.add(ISSUE_IMPORT_ID_FIELD);
        issueFieldColumnMap.add(ISSUE_LINKS);
        issueFieldColumnMap.add(ISSUE_MULTI_CHECKBOXES_FIELD);
        issueFieldColumnMap.add(ISSUE_MULTI_GROUP_PICKER_FIELD);
        issueFieldColumnMap.add(ISSUE_MULTI_SELECT_FIELD);
        issueFieldColumnMap.add(ISSUE_MULTI_USER_PICKER_FIELD);
        issueFieldColumnMap.add(ISSUE_NUMBER_FIELD);
        issueFieldColumnMap.add(ISSUE_ORIGINAL_ESTIMATE);
        issueFieldColumnMap.add(ISSUE_PROJECT);
        issueFieldColumnMap.add(ISSUE_PROJECT_PICKER_FIELD);
        issueFieldColumnMap.add(ISSUE_ROTEXT_FIELD);
        issueFieldColumnMap.add(ISSUE_RADIO_BUTTONS_FIELD);
        issueFieldColumnMap.add(ISSUE_REMAINING_ESTIMATE);
        issueFieldColumnMap.add(ISSUE_SELECT_LIST);
        issueFieldColumnMap.add(ISSUE_SINGLE_VERSION_PICKER_FIELD);
        issueFieldColumnMap.add(ISSUE_TEXT_FIELD255);
        issueFieldColumnMap.add(ISSUE_TIME_SPENT);
        issueFieldColumnMap.add(ISSUE_URL_FIELD);
        issueFieldColumnMap.add(ISSUE_USER_PICKER_FIELD);
        issueFieldColumnMap.add(ISSUE_VERSION_PICKER_FIELD);
        issueFieldColumnMap.add(ISSUE_VOTES);
        issueFieldColumnMap.add(ISSUE_WATCHES);
        issueFieldColumnMap.add(ISSUE_WORK_RATIO);
        issueFieldColumnMap.add(ISSUE_CREATOR);
    }
}
