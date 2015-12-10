<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>


<ul>
    <ww:iterator value="@fieldlist">
        <ww:if test="./validated == false">
            <li class="unprocessed">
                <img src="<%=request.getContextPath()%>/images/icons/control_stop.png" alt="" title="Not validated"><ww:property value="./displayName"/>
                <div class="notification"><ww:text name="'admin.project.import.summary.not.checked'"/></div>
            </li>
        </ww:if>
        <ww:elseIf test="./messageSet/hasAnyMessages == true">
            <ww:if test="./messageSet/hasAnyErrors == true">
                <li class="error">
                    <img src="<%=request.getContextPath()%>/images/icons/cancel.png" alt="Error" title="Error"><ww:property value="./displayName"/>
                    <ww:iterator value="./messageSet/errorMessages">
                        <div class="description"><ww:property value="escapeValuePreserveSpaces(.)" escape="false"/>
                            <ww:if test="../messageSet/linkForError(.) != null">
                                <br/>
                                <ww:if test="../messageSet/linkForError(.)/absolutePath == false" >
                                    <a href="<%= request.getContextPath() %><ww:property value="../messageSet/linkForError(.)/linkUrl"/>"><ww:property value="../messageSet/linkForError(.)/linkText"/></a>
                                </ww:if>
                                <ww:else>
                                    <a href="<ww:property value="../messageSet/linkForError(.)/linkUrl"/>"><ww:property value="../messageSet/linkForError(.)/linkText"/></a>
                                </ww:else>
                            </ww:if>
                        </div>
                    </ww:iterator>
                </li>
            </ww:if>
            <ww:if test="./messageSet/hasAnyWarnings == true">
                <li class="warning">
                    <img src="<%=request.getContextPath()%>/images/icons/error.png" alt="Warning" title="Warning"><ww:property value="./displayName"/>
                    <ww:iterator value="./messageSet/warningMessages">
                        <div class="description"><ww:property value="escapeValuePreserveSpaces(.)" escape="false"/>
                            <ww:if test="../messageSet/linkForWarning(.) != null">
                                <br/>
                                <ww:if test="../messageSet/linkForWarning(.)/absolutePath == false" >
                                    <a href="<%= request.getContextPath() %><ww:property value="../messageSet/linkForWarning(.)/linkUrl"/>"><ww:property value="../messageSet/linkForWarning(.)/linkText"/></a>
                                </ww:if>
                                <ww:else>
                                    <a href="<ww:property value="../messageSet/linkForWarning(.)/linkUrl"/>"><ww:property value="../messageSet/linkForWarning(.)/linkText"/></a>
                                </ww:else>
                            </ww:if>
                        </div>
                    </ww:iterator>
                </li>
            </ww:if>
        </ww:elseIf>
        <ww:else>
            <li>
                <img src="<%=request.getContextPath()%>/images/icons/accept.png" alt="OK" title="OK"><ww:property value="./displayName"/>
            </li>
        </ww:else>
    </ww:iterator>
</ul>
