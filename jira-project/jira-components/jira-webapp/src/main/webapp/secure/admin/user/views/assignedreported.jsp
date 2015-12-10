<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<p class="aui" id="user-data-summary">
    <ww:if test="linkableErrors/size() > 0">
        <span id="user-cannot-delete-explain">
            <ww:text name="'admin.deleteuser.users.cannot.be.deleted.explain'"/>
        </span>
    </ww:if>
    <ww:elseIf test="linkableWarnings/size() > 0">
        <span>
            <ww:text name="'admin.deleteuser.users.may.be.deleted.explain'"/>
        </span>
    </ww:elseIf>
</p>

<div>
    <ul>
        <ww:iterator value="linkableErrors">
            <li class="user-errors">
                <ww:if test="./value != null">
                    <a href="<ww:property value="./value"/>"><ww:property value="./key"/></a>
                </ww:if>
                <ww:else><span><ww:property value="./key"/></span></ww:else>
            </li>
        </ww:iterator>
        <ww:iterator value="linkableWarnings">
            <li class="user-warnings">
                <ww:if test="./value != null">
                    <a href="<ww:property value="./value"/>"><ww:property value="./key"/></a>
                </ww:if>
                <ww:else><span><ww:property value="./key"/></span></ww:else>
            </li>
        </ww:iterator>
    </ul>
</div>
<ww:if test="projectsUserLeadsError/size() > 0">
    <p id="user-projects-summary">
        <ww:text name="'admin.deleteuser.project.lead.title'"/>
        <ww:property value="projectsUserLeadsError">
            <ww:iterator value="." status="'status'">
                <a href="<ww:property value="./value"/>"><ww:property value="./key"/></a><ww:if test="@status/last == false">, </ww:if>
            </ww:iterator>
        </ww:property>
    </p>
</ww:if>
<ww:if test="componentsUserLeadsWarning/size() > 0">
    <div id="user-components-summary">
        <ww:text name="'admin.deleteuser.component.lead.title'"/>
        <ww:property value="componentsUserLeadsWarning">
            <ww:iterator value="." status="'status'">
                <a href="<ww:property value="./value"/>"><ww:property value="./key"/></a><ww:if test="@status/last == false">, </ww:if>
            </ww:iterator>
        </ww:property>
    </div>
</ww:if>