<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ include file="/template/standard/controlheader.jsp" %>

<div class="formOne">
    <ww:if test="/showCustom == 'true'">
        <fieldset class="hidden parameters">
            <input type="hidden" id="admin.indexing.custompath.migration.confirmation" value="<ww:text name="'admin.indexing.custompath.migration.confirmation'"/>">
        </fieldset>
        <script type="text/javascript">
            jQuery(function()
            {
                jQuery('#indexPathOption_DEFAULT').bind('click', function(e)
                {
                    var msg = AJS.params['admin.indexing.custompath.migration.confirmation'];
                    if (! confirm(msg))
                    {
                        e.preventDefault();
                        e.stopPropagation();
                    }
                    else
                    {
                        jQuery('#indexPathOption_DEFAULT').unbind('click', arguments.callee);
                    }
                });
            });
        </script>

        <div>
            <input class="radio" type="radio" id="indexPathOption_CUSTOM" name="indexPathOption" value="CUSTOM" checked="checked" disabled="disabled"/>
            <label for="indexPathOption_CUSTOM"><ww:text name="'admin.indexing.usecustompath'"/></label>

            <div>
                <div class="field-description">
                    <p><ww:text name="'admin.indexing.custompath.msg'"/> :
                        <em><ww:property value="/indexPath"/></em>
                    </p>

                    <p>
                        <ww:text name="'admin.import.index.warning'">
                            <ww:param name="'value0'"><span class="warning"></ww:param>
                            <ww:param name="'value1'"></span></ww:param>
                        </ww:text>
                    </p>
                </div>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'helpKey'">JRA21004</aui:param>
                    <aui:param name="'messageHtml'">
                        <p><ww:text name="'admin.indexing.custompath.migration.msg'"/></p>
                    </aui:param>
                </aui:component>
            </div>
        </div>

        <div>
            <input class="radio" type="radio" id="indexPathOption_DEFAULT" name="indexPathOption" value="DEFAULT"
                   <ww:if test="/indexPathOption == 'DEFAULT'">CHECKED</ww:if> />
            <label for="indexPathOption_DEFAULT"><ww:text name="'admin.indexing.usedefaultpath'"/></label>
            <div class="field-description">
                <p><ww:property value="/defaultIndexPath"/></p>
            </div>
        </div>

    </ww:if>
    <ww:else>
        <div>
            <div>
                <span id="default-index-path"><ww:property value="/defaultIndexPath"/></span>
            </div>
        </div>
    </ww:else>
</div>

<%@ include file="/template/standard/controlfooter.jsp" %>
