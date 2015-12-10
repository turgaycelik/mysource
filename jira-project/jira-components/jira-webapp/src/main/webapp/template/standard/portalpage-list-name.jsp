<%@ taglib uri="webwork" prefix="ww" %>
<ww:if test="parameters['name'] != false">
    <ww:if test="parameters['favourite'] != 'false'">
        <div class="favourite-status">
            <ww:component name="'favourite'" template="favourite.jsp">
                <ww:param name="'enabled'"><ww:property value="./favourite"/></ww:param>
                <ww:param name="'entityType'">PortalPage</ww:param>
                <ww:param name="'entityId'"><ww:property value="./id" /></ww:param>
                <ww:param name="'tableId'"><ww:property value="parameters['id']"/></ww:param>
                <ww:param name="'entityName'"><ww:property value="./name"/></ww:param>
                <ww:param name="'relatedDropdown'">home_link</ww:param>
                <ww:if test="parameters['remove'] == true">
                    <ww:param name="'removeId'">pp_<ww:property value="./id" /></ww:param>
                </ww:if>
            </ww:component>
        </div>
        <div class="favourite-item">
            <ww:property value="parameters['linkRenderer']/render(./id, ./name)" escape="false"/>
            <ww:if test="./description != null && ./description/length > 0">
                <div class="description secondary-text"><ww:property value="./description"/></div>
            </ww:if>
        </div>
    </ww:if>
    <ww:else>
        <div>
            <ww:property value="parameters['linkRenderer']/render(./id, ./name)" escape="false"/>
            <ww:if test="./description != null && ./description/length > 0">
                <div class="description secondary-text"><ww:property value="./description"/></div>
            </ww:if>
        </div>
    </ww:else>
</ww:if>
