<%@ taglib uri="webwork" prefix="ww" %>
<ww:bean id="math" name="'com.atlassian.core.bean.MathBean'"/>

<html>
<head>
	<title><ww:text name="'admin.systeminfo.upgrade.history'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/troubleshooting_and_support"/>
    <meta name="admin.active.tab" content="system_info"/>
</head>

<body>
<h3 class="formtitle"><ww:text name="'admin.systeminfo.upgrade.history'"/></h3>
<p><ww:text name="'admin.systeminfo.upgrade.history.description'"/></p>
<table id="upgradehistory" class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th width="33%"><ww:text name="'admin.systeminfo.upgrade.history.target.version'"/></th>
            <th width="33%"><ww:text name="'admin.systeminfo.upgrade.history.target.build'"/></th>
            <th width="33%"><ww:text name="'admin.systeminfo.upgrade.history.time.performed'"/></th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="/upgradeHistory" status="'status'">
        <tr>
            <td><ww:property value="./targetVersion"/></td>
            <td><ww:property value="./targetBuildNumber"/>
            <ww:if test="./inferred == true">
                <ww:if test="./originalBuildNumber/equals(./targetBuildNumber) == false">
                    <ww:text name="'admin.systeminfo.upgrade.history.inferred.from'"><ww:param name="'value0'"><ww:property value="./originalBuildNumber"/></ww:param></ww:text>
                </ww:if>
                <ww:else>
                    <ww:text name="'admin.systeminfo.upgrade.history.inferred'"/>
                </ww:else>
            </ww:if>
            </td>
            <td><ww:property value="/formattedTimePerformed(./timePerformed)"/></td>
        </tr>
    </ww:iterator>
    </tbody>
</table>
</body>
</html>
