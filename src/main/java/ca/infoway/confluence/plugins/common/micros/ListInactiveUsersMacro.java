package ca.infoway.confluence.plugins.common.micros;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.Renderer;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.security.login.LoginManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.PropertyRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.user.Group;
import com.atlassian.user.search.page.Pager;
//import com.atlassian.user.User;
//import com.atlassian.user.search.page.Pager;

public class ListInactiveUsersMacro implements Macro {

	private static final Logger log = LoggerFactory.getLogger(ListInactiveUsersMacro.class);
	
	private String recentlyUpdatedMacro = "<p>"
  + "<ac:structured-macro ac:name='recently-updated'>"
  + "<ac:parameter ac:name='spaces'>"
  +    "<ri:space ri:space-key='@all'/>"
  +  "</ac:parameter>"
  +  "<ac:parameter ac:name='author'>"
  +    "<ri:user ri:userkey='USERKEYPLACEHOLDER'/>"
  +  "</ac:parameter>"
  + "<ac:parameter ac:name='max'>1</ac:parameter>"
  + "</ac:structured-macro>"
  + "</p>";
	
	private String blankExperienceString = "macro-blank-experience";
	private LoginManager loginManager;
	private UserAccessor userAccessor;
	private Renderer renderer;
	private CrowdService crowdService;
	private SpaceManager spaceManager;
	
	public ListInactiveUsersMacro(UserAccessor userAccessor, Renderer renderer, 
			LoginManager loginManager, CrowdService crowdService, SpaceManager spaceManager) {
		this.userAccessor = userAccessor;
		this.renderer = renderer;
		this.loginManager = loginManager;
		this.crowdService = crowdService;
		this.spaceManager = spaceManager;
	}

	@Override
	public String execute(Map<String, String> parameters, String body,
			ConversionContext context) throws MacroExecutionException {
		
		ConfluenceUser currentUser = AuthenticatedUserThreadLocal.get();
		PropertyRestriction<Boolean> restriction = Restriction.on(UserTermKeys.ACTIVE).containing(true);
		EntityQuery<User> query = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(restriction)
		    .returningAtMost(EntityQuery.ALL_RESULTS);
		Iterable<User> allUserList = crowdService.search(query);
		List<InactiveUserDetail> inactiveUserList = new ArrayList<InactiveUserDetail>();
		Map<String, Object> velocityCtx = MacroUtils.defaultVelocityContext();
		int numberOfInactives;
		boolean isAdmin = userAccessor.hasMembership(UserAccessor.GROUP_CONFLUENCE_ADMINS, currentUser.getName());
		velocityCtx.put("isAdmin", isAdmin);
		for (User user : allUserList) {
			ConfluenceUser cUser = userAccessor.getUserByName(user.getName());
			UserKey userKey = cUser.getKey();
			String userName = cUser.getName();
			log.trace("userName: " + userName);
			String userKeyString = userKey.getStringValue();
			String thisUsersHistory = recentlyUpdatedMacro.replace("USERKEYPLACEHOLDER", userKeyString);
			String renderedHistory = renderer.render(thisUsersHistory, context);
//			log.trace("renderedHistory: " + renderedHistory);
			// after the recently updated macro is rendered, if the user has no updates,
			// the macro will contain a "macro-blank-experience" class
			if (renderedHistory.contains(blankExperienceString)) {
				String userEmail = cUser.getEmail();
				String userFullName = cUser.getFullName();
				Date lastLogin = loginManager.getLoginInfo(cUser).getLastSuccessfulLoginDate();
				String lastLoginString = lastLogin != null ? lastLogin.toString() : "No previous login date";
				List<String> userGroups = userAccessor.getGroupNames(cUser);
				List<Map<String, String>> userGroupDetails = new ArrayList<Map<String, String>>();
				for (String gr : userGroups) {
					String[] splitName = gr.split("_");
					if (splitName.length == 3) {
						Map<String, String> userGroupDetail = new HashMap<String, String>();
						userGroupDetail.put("space", splitName[1]);
						userGroupDetail.put("role", getUserRole(splitName[2]));
						log.trace("userGroupDetail: " + userGroupDetail);
						userGroupDetails.add(userGroupDetail);
					}
				}
				log.trace("userGroupDetails: " + userGroupDetails);
				Pager<Group> cGroups = userAccessor.getGroups(cUser);
				String groups = "";
				for (Group cGroup: cGroups) {
					if (!cGroup.getName().equals("confluence-users")) {
						groups += cGroup.getName() + ", ";
					}
				}
				// simple object to store these values
				InactiveUserDetail iud = new InactiveUserDetail(userName, userFullName, userEmail, lastLoginString, userGroupDetails, groups);
				inactiveUserList.add(iud);
				log.trace("iud.getUserRoleDetails: " + iud.getUserGroupDetails());
			}
		}
		numberOfInactives = inactiveUserList.size();
		velocityCtx.put("spaceManager", spaceManager);
		velocityCtx.put("numberOfInactives", numberOfInactives);
		velocityCtx.put("inactiveUserList", inactiveUserList);
		String renderedTable = VelocityUtils.getRenderedTemplate("/templates/inactive-user-list.vm", velocityCtx);
		return renderedTable;
	}

	@Override
	public BodyType getBodyType() {
		return BodyType.NONE;
	}

	@Override
	public OutputType getOutputType() {
		return OutputType.BLOCK;
	}
	
	private String getUserRole(String s) {
		if (s.equalsIgnoreCase("doc-admins")) {
			return "Approver";
		} else if (s.equalsIgnoreCase("authors")) {
			return "Author";
		} else if (s.equalsIgnoreCase("reviewers")) {
			return "Reviewer";
		}
		return null;
	}

}
