<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>


<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/troubleshooting_and_support"/>
    <meta name="admin.active.tab" content="instrumentation"/>
    <title><ww:text name="'admin.instrumentation.page.title'"/></title>
</head>

<body>
<table class="aui aui-table-rowhover" id="instrument_table">
    <thead>
    <tr>
        <th><ww:text name="'admin.instrumentation.instrument.name'"/></th>
        <th><ww:text name="'admin.instrumentation.instrument.type'"/></th>
        <th><ww:text name="'admin.instrumentation.instrument.value'"/></th>
        <th><ww:text name="'admin.instrumentation.instrument.invocation'"/></th>
        <th><ww:text name="'admin.instrumentation.instrument.time'"/></th>
        <th><ww:text name="'admin.instrumentation.instrument.cpu'"/></th>
    </tr>
    </thead>
    <tbody>
    <ww:iterator value="/instruments" status="''">
        <tr>
            <td><ww:property value="./name"/></td>
            <td><ww:property value="./type"/></td>
            <td><ww:property value="./value"/></td>

            <td><ww:property value="./invocationCount"/></td>
            <td><ww:property value="./millisecondsTaken"/></td>
            <td><ww:property value="./cpuTime"/></td>
        </tr>
    </ww:iterator>

    </tbody>
</table>

<div>
    <h5><ww:text name="'admin.instrumentation.jmx.info'"/></h5>
    <ul>
        <li>
            <ww:text name="'admin.instrumentation.jmx.info.threadContentionMonitoring'"/>
             - <ww:if test="/jmxStateInfo/threadContentionMonitoringSupported == true"><ww:text name="'admin.instrumentation.jmx.info.supported'"/></ww:if><ww:else><ww:text name="'admin.instrumentation.jmx.info.notsupported'"/></ww:else>
             - <ww:if test="/jmxStateInfo/threadContentionMonitoringEnabled == true"><ww:text name="'admin.instrumentation.jmx.info.enabled'"/></ww:if><ww:else><ww:text name="'admin.instrumentation.jmx.info.notenabled'"/></ww:else>
        </li>
        <li>
            <ww:text name="'admin.instrumentation.jmx.info.threadCpuTime'"/>
             - <ww:if test="/jmxStateInfo/threadCpuTimeSupported == true"><ww:text name="'admin.instrumentation.jmx.info.supported'"/></ww:if><ww:else><ww:text name="'admin.instrumentation.jmx.info.notsupported'"/></ww:else>
             - <ww:if test="/jmxStateInfo/threadCpuTimeEnabled == true"><ww:text name="'admin.instrumentation.jmx.info.enabled'"/></ww:if><ww:else><ww:text name="'admin.instrumentation.jmx.info.notenabled'"/></ww:else>
        </li>
    </ul>
    <ww:if test="/jmxStateInfo/threadContentionMonitoringSupported == true">
        <div class="buttons-container aui-toolbar form-buttons noprint">
            <div class="toolbar-group">
            <span class="toolbar-item">
                <ww:if test="/jmxStateInfo/threadContentionMonitoringEnabled == true">
                    <a class="toolbar-trigger" id="threadmonitoring" href="ViewInstrumentation.jspa?threadContentionMonitoring=false"><ww:text name="'admin.instrumentation.jmx.info.setThreadContentionMonitoringEnabledOff'"/></a>
                </ww:if>
                <ww:else>
                    <a class="toolbar-trigger" id="threadmonitoring" href="ViewInstrumentation.jspa?threadContentionMonitoring=true"><ww:text name="'admin.instrumentation.jmx.info.setThreadContentionMonitoringEnabledOn'"/></a>
                </ww:else>
            </span>
            </div>
        </div>
    </ww:if>
</div>
</body>
</html>