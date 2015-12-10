<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<ww:property value="importer" >

<table class="grid" style="width: 80%; margin: 10px auto;">
    <tr>
        <th><ww:text name="'admin.importlogs.statistic'"/></th>
        <th><ww:text name="'admin.importlogs.data'"/></th>
    </tr>
    <tr>
        <td><ww:text name="'admin.importlogs.elapsed.time'"/></td>
        <td><ww:property value="./stats/formattedTime" /> <ww:property value="./stats/estimateRemaining" /></td>
    </tr>
    <tr>
        <td><ww:text name="'admin.importlogs.import.rate'"/></td>
        <td><ww:property value="./stats/importRate" /></td>
    </tr>
    <ww:property value="./stats/failures" >
    <tr <ww:if test=". > 0">class="red-highlight"</ww:if>>
        <td><ww:text name="'admin.importlogs.failures'"/></td>
        <td><ww:property value="." /></td>
    </tr>
    </ww:property>
    <ww:if test="./stats/usersImported > 0">
    <tr>
        <td><ww:text name="'admin.importlogs.users.imported'"/></td>
        <td><ww:property value="./stats/usersImported" /></td>
    </tr>
    </ww:if>
    <ww:if test="./stats/projectsImported > 0">
    <tr>
        <td><ww:text name="'admin.importlogs.projects.imported'"/></td>
        <td><ww:property value="./stats/projectsImported" /></td>
    </tr>
    </ww:if>
    <ww:if test="./stats/versionsImported > 0">
    <tr>
        <td><ww:text name="'admin.importlogs.versions.imported'"/></td>
        <td><ww:property value="./stats/versionsImported" /></td>
    </tr>
    </ww:if>
    <ww:if test="./stats/componentsImported > 0">
    <tr>
        <td><ww:text name="'admin.importlogs.components.imported'"/></td>
        <td><ww:property value="./stats/componentsImported" /></td>
    </tr>
    </ww:if>

    <tr>
        <td><ww:text name="'admin.importlogs.issues.imported'"/></td>
        <td><ww:property value="./stats/issuesImported" /></td>
    </tr>
    <ww:if test="./stats/customfieldsImported > 0">
    <tr>
        <td><ww:text name="'admin.importlogs.new.custom.fields'"/></td>
        <td><ww:property value="./stats/customfieldsImported" /></td>
    </tr>
    </ww:if>

    <ww:if test="./stats/issuetypesImported > 0">
    <tr>
        <td><ww:text name="'admin.importlogs.new.issue.types'"/></td>
        <td><ww:property value="./stats/issuetypesImported" /></td>
    </tr>
    </ww:if>
    <ww:if test="./stats/resolutionsImported > 0">
    <tr>
        <td><ww:text name="'admin.importlogs.new.resolutions'"/></td>
        <td><ww:property value="./stats/resolutionsImported" /></td>
    </tr>
    </ww:if>
    <ww:if test="./stats/prioritiesImported > 0">
    <tr>
        <td><ww:text name="'admin.importlogs.new.priorities'"/></td>
        <td><ww:property value="./stats/prioritiesImported" /></td>
    </tr>
    </ww:if>



</table>


<pre class="codearea">
<h4><ww:text name="'admin.importlogs.import.logs'"/></h4>
<ww:if test="./finished == true"><ww:property value="./log/importLog" /></ww:if><ww:else><textarea readonly="readonly" id="importLogs" name="importLogs"><ww:property value="./log/shortenedLog" /></textarea></ww:else>
</pre>
    <ww:if test="./log/logShortened == true && ./finished == false">
    <div class="informationbox">
        <ww:text name="'admin.importlogs.please.note'"/>
    </div>
</ww:if>
<script language="JavaScript" type="text/javascript">
<!--
    function scrollDown()
    {
        try
        {
            var importLogs = document.getElementById("importLogs");
            if (importLogs.scrollHeight)
                importLogs.scrollTop = importLogs.scrollHeight;
        }
        catch (e)
        {
            // do nothing;
        }
    }

    setTimeout('scrollDown();', 1);
//-->
</script>

</ww:property>


