<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title>
        <ww:if test="/bulkEditBean/singleMode == false">
            <ww:text name="'bulk.migrate.title'"/>
        </ww:if>
        <ww:else>
            <ww:text name="'moveissue.title'"/>
        </ww:else>
    </title>
	
    <script language="JavaScript" type="text/javascript">
        var bulkEditBeanIds = new Array();
        <ww:iterator value="./multiBulkMoveBean/bulkEditBeans" status="'status'">
        <ww:if test="./value/subTaskCollection == false">
        bulkEditBeanIds[bulkEditBeanIds.length] = '<ww:property value="./value/key" />';
        </ww:if>
        </ww:iterator>

        function toggle(keyToStillShow)
        {
            var sameAsBulkEditBean = document.getElementById("sameAsBulkEditBean");
            var sameAsProject = document.getElementById(sameAsBulkEditBean.value + "project");
            var sameAsIssueType = document.getElementById(sameAsBulkEditBean.value + "issuetype");

            var e;
            for (i = 0; i < bulkEditBeanIds.length; i++)
            {
                if (bulkEditBeanIds[i] != keyToStillShow)
                {
                    e = document.getElementById(bulkEditBeanIds[i]);
                    if (e.style.display != 'none')
                    {
                        e.style.display = 'none';
                        // document.getElementById(bulkEditBeanIds[i] + 'pid');
                    }
                    else
                    {
                        e.style.display = '';
                    }
                }
            }
        }

        var sameAsBulkEditBean = document.getElementById("sameAsBulkEditBean");
        if (sameAsBulkEditBean && sameAsBulkEditBean.checked)
        {
            toggle(sameAsBulkEditBean.value);
        }
    </script>
</head>
<body>
    <page:applyDecorator name="bulkpanel" >
        <page:param name="title">
            <ww:if test="/bulkEditBean/singleMode == false">
                <ww:text name="'bulkedit.step3'"/>: <ww:text name="'bulk.migrate.overview.title'"/>
            </ww:if>
            <ww:else>
            	<ww:text name="'bulkedit.step1.single'"/>: <ww:text name="'bulk.migrate.overview.title.single'"/>
            </ww:else>
        </page:param>
        <page:param name="action">BulkMigrateChooseContext.jspa</page:param>
        <ww:property value="'true'" id="hideSubMenu" />
        <page:param name="instructions">
            <p>
            	<ww:if test="/bulkEditBean/singleMode == true">
                    <ww:text name="'bulkedit.step1.single.dsc'"/>
                </ww:if>
                <ww:else>
                <ww:text name="'bulk.migrate.overview.instructions'">
                    <ww:param name="'value0'"><strong><ww:property value="/currentRootBulkEditBean/selectedIssues/size"/></strong></ww:param>
                    <ww:param name="'value1'"><strong><ww:property value="/currentRootBulkEditBean/projectIds/size"/></strong></ww:param>
                    <ww:param name="'value2'"><strong><ww:property value="/currentRootBulkEditBean/issueTypes/size"/></strong></ww:param>
                    <ww:param name="'value3'"><strong><ww:property value="/multiBulkMoveBean/issuesInContext/size"/></strong></ww:param>
                </ww:text>
                </ww:else>
                <ww:if test="/currentRootBulkEditBean/subTaskCollection == true">
                    <ww:text name="'bulk.migrate.overview.subtask.warning'" />
                </ww:if>
            </p>
            <ww:if test="/bulkEditBean/singleMode == false">
                <p><ww:text name="'bulk.migrate.overview.help'" /></p>
            </ww:if>
            <ww:if test="/currentRootBulkEditBean/relatedMultiBulkMoveBean/subTasksDiscarded > 0">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">warning</aui:param>
                    <aui:param name="'messageHtml'">
                        <p>
                            <ww:text name="'bulk.migrate.overview.subtask.warning.discarded'">
                                <ww:param name="'value0'"><strong><ww:property value="/currentRootBulkEditBean/relatedMultiBulkMoveBean/subTasksDiscarded" /></strong></ww:param>
                            </ww:text>
                        </p>
                    </aui:param>
                </aui:component>
            </ww:if>
        </page:param>

        <ui:component name="'subTaskPhase'" template="hidden.jsp"  />
        <ww:iterator value="./multiBulkMoveBean/bulkEditBeans" status="'status'">
            <%@include file="/secure/views/bulkedit/includes/chooseContext.jsp" %>
        </ww:iterator>
    </page:applyDecorator>
</body>
</html>
