<%--

A component that draws an AUI cog drop down menu.

--%>
<%@ taglib uri="webwork" prefix="ww" %>
<ww:declare id="varId" value="parameters['id']"><ww:if test="."> id="<ww:property value="."/>"</ww:if></ww:declare>
<ww:if test="parameters['model']/totalItems > 0">
    <div class="aui-dd-parent">
        <a class="aui-dd-link cog-dd js-default-dropdown" href="#" <ww:property value="@varId" escape="false"/> >
        <span>
            <ww:property value="parameters['model']/topText"/> <%-- for accessiblity only --%>
        </span>
        </a>
        <div class="aui-list hidden">
            <ww:iterator value="parameters['model']/sections" status="'status'">
                <ul class="aui-list-section <ww:if test="@status/first == true">aui-first</ww:if> <ww:if test="@status/last == true">aui-last</ww:if>">
                    <ww:iterator value="./items">
                        <li class="aui-list-item">
                            <a class="aui-list-item-link <ww:property value="./attrAndRemove('class')"/>" href="<ww:url value="./attrAndRemove('href')"/>" <%-- The rest of the attrs --%>
                            <ww:iterator value="./attrs"><ww:property value="."/>="<ww:property value="../attr(.)"/>"</ww:iterator>>
                                <ww:property value="./text"/>
                            </a>
                        </li>
                    </ww:iterator>
                </ul>
            </ww:iterator>
        </div>
    </div>
</ww:if>
<ww:property id="varId" value="''"/>