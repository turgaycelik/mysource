<%@ taglib prefix="ww" uri="webwork" %>
<div class="aui-page-header-actions" id="navigator-options">
    <ul class="operations">
    <ww:iterator value="/sectionsForMenu">
        <ww:property value="/sectionLinks(./id)">
            <ww:if test="./empty == false">
                <li class="aui-dd-parent">
                    <a id="<ww:property value="../id"/>" class="lnk aui-dd-link standard <ww:property value="../styleClass"/>" href="#" hidefocus title="<ww:property value="../title" />"><span><ww:property value="../label"/></span></a>

                    <div class="aui-list hidden">
                        <ul id="<ww:property value="../id"/>-dropdown">
                            <ww:iterator value=".">
                                <li class="aui-list-item">
                                    <a class="aui-list-item-link" id="<ww:property value="./id"/>" title="<ww:property value="./title"/>" href="<ww:property value="./url"/>"><ww:property value="./label"/></a>
                                 </li>
                            </ww:iterator>
                         </ul>
                    </div>
                 </li>
            </ww:if>
        </ww:property>
    </ww:iterator>
    </ul>
</div>