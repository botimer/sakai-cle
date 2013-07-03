<ul class="navIntraTool">
	<li><span><h:commandLink id="editProfile" title ="#{msgs.profile_edit}" action="#{ProfileTool.processActionEdit}" immediate="true"   value="#{msgs.profile_edit}" rendered="#{!ProfileTool.profile.locked}" /></span></li>
	<li><span><h:commandLink  id="searchProfile" title ="#{msgs.profile_show}" immediate="true" action="#{SearchTool.processCancel}"  value="#{msgs.profile_show}" /></span></li>
</ul>