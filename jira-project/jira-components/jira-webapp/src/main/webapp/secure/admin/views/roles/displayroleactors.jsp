<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<script type="text/javascript">
function toggle(mode, elementId)
{
    var hideElement;
    var showElement;

    if (mode == "hide")
    {
        hideElement = document.getElementById('long_' + elementId);
        showElement = document.getElementById('short_' + elementId);
    }
    else
    {
        hideElement = document.getElementById('short_' + elementId);
        showElement = document.getElementById('long_' + elementId);
    }

    if (hideElement && showElement)
    {
        hideElement.style.display = 'none';
        showElement.style.display = '';
    }
}
</script>

<ww:iterator value="roleActorTypes">
    <td>
        <ww:if test="/roleActorTypes(.., ./type, -1)/size() != 0">
            <span id="short_<ww:property value="../name"/>_<ww:property value="./type"/>"  onclick="toggle('expand', '<ww:property value="../name"/>_<ww:property value="./type"/>');">
                <ww:iterator value="/roleActorTypes(.., ./type, 3)" status="'liststatus'">
                    <ww:property value="descriptor" /><ww:if test="@liststatus/last == false">, </ww:if>
                </ww:iterator>
                <ww:if test="/roleActorTypes(.., ./type, -1)/size() > 3"><span style="cursor:pointer;" class="smallgrey" >[<ww:text name="'common.concepts.more'" />]</span></ww:if>
            </span>
            <ww:if test="/roleActorTypes(.., ./type, -1)/size() > 3">
                <span style="display:none; cursor:pointer;" id="long_<ww:property value="../name"/>_<ww:property value="./type"/>" onclick="toggle('hide', '<ww:property value="../name"/>_<ww:property value="./type"/>');">
                    <ww:iterator value="/roleActorTypes(.., ./type, -1)" status="'liststatus'">
                        <ww:property value="descriptor" /><ww:if test="@liststatus/last == false">, </ww:if>
                    </ww:iterator>
                <span class="smallgrey">[<ww:text name="'admin.deleteuser.hide'" />]</span>
                </span>
            </ww:if>
        </ww:if>
        <ww:else>
            <i><ww:text name="'admin.projectroles.view.none.selected'"/></i>
        </ww:else>
        <!-- get projectRoleModuleDescriptor for the type then get projectRoleModuleDescriptor.getParams().get("ConfigurationURL") -->
        <ww:if test="/configurationUrl(./type) != null">
            <a id="edit_<ww:property value="../id"/>_<ww:property value="./type"/>" href="<ww:property value="/configurationUrl(./type)"/>.jspa?projectRoleId=<ww:property value="../id"/><ww:if test="projectId != null" >&projectId=<ww:property value="projectId"/></ww:if>"><ww:text name="'common.words.edit'"/></a>
        </ww:if>
    </td>
</ww:iterator>
