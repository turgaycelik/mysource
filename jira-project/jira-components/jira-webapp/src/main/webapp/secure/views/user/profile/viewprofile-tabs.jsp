<%@ taglib prefix="ww" uri="webwork" %>
<ul id="user_profile_tabs" class="vertical tabs">
    <ww:iterator value="/tabDescriptors" status="'status'">
        <li id="up_<ww:property value="./key"/>_li" class="<ww:if test="/selected == completeKey">active</ww:if><ww:if test="@status/first == true"> first</ww:if>">
            <a id="up_<ww:property value="./key"/>_a" href='<%= request.getContextPath() %>/secure/ViewProfile.jspa?<ww:if test="/user != /remoteUser">name=<ww:property value="user/name"/>&</ww:if>selectedTab=<ww:property value="./completeKey"/>' title="<ww:text name="./name"/>"><strong><ww:text name="./name"/></strong></a>
        </li>
    </ww:iterator>
</ul>