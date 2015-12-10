<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>

<html>
<head>
    <meta content="none" name="decorator"/>
</head>
<body>
    <!--
        These are just HTML snippits that editIssueTypeScheme uses to update the UI after an issue type is added
        from Edit/Create Issue Type Scheme page.
    -->
    <div id="add-issue-type-template">
        <ww:property value="/newIssueType">
        <ol>
            <li id="selectedOptions_<ww:property value="./id" />">
                <span class="icon icon-vgrabber"></span>
                <img class="icon jira-icon-image" src="<ww:url value="./imagePath" />" alt="" />
                <span class="issue-type-name"><ww:property value="./name" /></span><ww:if test="./subTask == true"> <span class="smallgrey">(<ww:text name="'admin.issuesettings.sub.task'"/>)</span></ww:if>
            </li>
        </ol>
        </ww:property>
        <aui:select theme="'aui'" label="text('admin.issuesettings.default.issue.type')" name="'defaultOption'" list="/allOptions"
            listKey="'id'" listValue="'name'" id="'default-issue-type-select-template'">
            <aui:param name="'defaultOptionText'"><ww:text name="'common.words.none'"/></aui:param>
            <aui:param name="'defaultOptionValue'" value="''"/>
        </aui:select>
    </div>
</body>
</html>