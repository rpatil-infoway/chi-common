package ca.infoway.confluence.plugins.common.spaceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.StringBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import ca.infoway.confluence.plugins.common.Global;

//import com.atlassian.confluence.api.model.people.User;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.PropertyRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
//import com.atlassian.user.GroupManager;

public class SpaceUserManagerMacro implements Macro {

	private static final Logger log = LoggerFactory.getLogger(SpaceUserManagerMacro.class);
	private CrowdService crowdService;
	private UserAccessor userAccessor;
//	private GroupManager groupManager;
	
	private String dropdownCreate(String user, String selectedRole) {
		String basicTemplate = "<form class='aui'><div class='field-group' style='padding: 4px 0px 4px 0px'>"
				+ "<select class='select role-select' id='" + user + "-select-dropdown' name='" + user + "-select-dropdown' data-username='" + user + "'>"
				+ "<option>none</option>"
				+ "<option>approver</option>"
				+ "<option>author</option>"
				+ "<option>reviewer</option>"
				+ "</select>"
				+ "</div></form>";
		StringBuilder newStr = new StringBuilder(basicTemplate);
		int replaceIndex;
		int startIndex = basicTemplate.indexOf("<option>none</option>");
		if (selectedRole.equalsIgnoreCase("approver")) {
			replaceIndex = basicTemplate.indexOf("approver", startIndex) - 1;
		} else if (selectedRole.equalsIgnoreCase("author")) {
			replaceIndex = basicTemplate.indexOf("author", startIndex) - 1;
		} else if (selectedRole.equalsIgnoreCase("reviewer")) {
			replaceIndex = basicTemplate.indexOf("reviewer", startIndex) - 1;
		} else {
			return basicTemplate;
		}
		
		newStr = newStr.insert(replaceIndex, " selected='selected'");
		return newStr.toString();
	}
	
	public SpaceUserManagerMacro(CrowdService crowdService, UserAccessor userAccessor) {
		this.crowdService = crowdService;
		this.userAccessor = userAccessor;
	}

	@Override
	public String execute(Map<String, String> parameters, String body,
			ConversionContext context) throws MacroExecutionException {
		//String spaceGroupPrefix = "_" + context.getSpaceKey().toLowerCase() + "_";
		String spaceKey = context.getSpaceKey().toLowerCase();
		ConfluenceUser currentUser = AuthenticatedUserThreadLocal.get();
		String currentUsername = currentUser.getName();
		 log.trace("currentUsername: " + currentUsername);
		 if ( !(userAccessor.hasMembership(Global.getApproverGroupName(spaceKey), currentUsername) 
				 || userAccessor.hasMembership("confluence-administrators", currentUsername)
				 || userAccessor.hasMembership("infoway-doc-admins", currentUsername)) ) {
			 log.trace("The current user does not have the required privileges to view this macro.");
			 return "";
		 }
		
		PropertyRestriction<Boolean> restriction = Restriction.on(UserTermKeys.ACTIVE).containing(true);
		EntityQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(restriction)
		    .returningAtMost(EntityQuery.ALL_RESULTS);
		
		Map<String, Object> ctx = MacroUtils.defaultVelocityContext();
		Iterable<String> usernames = crowdService.search(query);

		Map<String, String> userToRoleMap = getUserToRoleMap(spaceKey, usernames);
		
		Map<String, String> userToDropdownMap = new HashMap<String, String>();
		for (String user : userToRoleMap.keySet()) {
			userToDropdownMap.put(user, dropdownCreate(user, userToRoleMap.get(user)));
		}
		
		Map<String, String> updatedUserToRoleMap = new HashMap<String, String>(userToRoleMap);
		//TODO: apparently userAccessor is already accessible by default, so try removing this line
		ctx.put("spaceGroupPrefix", spaceKey);
		ctx.put("userAccessor", userAccessor);
		ctx.put("usernameList", new ArrayList<String>(userToRoleMap.keySet()));
		ctx.put("userToRoleMap", userToRoleMap);
		ctx.put("updatedUserToRoleMap", updatedUserToRoleMap);
		ctx.put("userToDropdownMap", userToDropdownMap);
		String renderedTemplate = VelocityUtils.getRenderedTemplate("/templates/space-user-manager-dialog-template.vm", ctx);
		return renderedTemplate;
	}
	
	private Map<String, String> getUserToRoleMap(String spaceKey, Iterable<String> usernames) {
		Map<String, String> userToRoleMap = new HashMap<String, String>();
		for (String user : usernames) {
			String role;
			
			if (userAccessor.hasMembership(Global.getApproverGroupName(spaceKey), user)) {
				role = "approver";
			} else if (userAccessor.hasMembership(Global.getAuthorGroupName(spaceKey), user)) {
				role = "author";
			} else if (userAccessor.hasMembership(Global.getReviewerGroupName(spaceKey), user)) {
				role = "reviewer";
			} else {
				role = "none";
			}
			userToRoleMap.put(user, role);
		}
		return userToRoleMap;
	}

	@Override
	public BodyType getBodyType() {
		return BodyType.NONE;
	}

	@Override
	public OutputType getOutputType() {
		return OutputType.BLOCK;
	}

}
