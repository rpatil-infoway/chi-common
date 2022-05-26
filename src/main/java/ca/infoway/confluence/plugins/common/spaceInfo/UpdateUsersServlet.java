package ca.infoway.confluence.plugins.common.spaceInfo;

import static com.atlassian.confluence.mail.template.ConfluenceMailQueueItem.MIME_TYPE_HTML;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.tools.generic.DateTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.infoway.confluence.plugins.common.Global;

import com.atlassian.confluence.mail.template.ConfluenceMailQueueItem;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.PropertyRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.atlassian.confluence.core.InsufficientPrivilegeException;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.MailQueueItem;

public class UpdateUsersServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 726843062962161304L;
	private static final Logger log = LoggerFactory.getLogger(UpdateUsersServlet.class);

	private static final String NONE = "none";
	private static final String APPROVER = "approver";
	private static final String AUTHOR = "author";
	private static final String REVIEWER = "reviewer";

	private CrowdService crowdService;
	private UserAccessor userAccessor;

	public UpdateUsersServlet(UserAccessor userAccessor, CrowdService crowdService) {
		this.userAccessor = userAccessor;
		this.crowdService = crowdService;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		getServletContext().log("init() called");

	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		getServletContext().log("service() called");

		String spaceKey = request.getParameter("spaceKey");
		String approversGroup = Global.getApproverGroupName(spaceKey);
		String authorsGroup = Global.getAuthorGroupName(spaceKey);
		String reviewersGroup = Global.getReviewerGroupName(spaceKey);

		ConfluenceUser currentUser = AuthenticatedUserThreadLocal.get();

		try {
			String currentUsername = currentUser.getName();
			log.trace("currentUsername: " + currentUsername);
			if (Global.isCurrentUserConfluenceAdmin() || Global.isCurrentUserInfowayDocAdmin() || Global.isCurrentUserApprover(spaceKey)) {
				AuthenticatedUserThreadLocal.set(userAccessor.getUserByName(Global.APP_ADMIN));
			} else {
				throw new InsufficientPrivilegeException(currentUsername);
			}
			PropertyRestriction<Boolean> restriction = Restriction.on(UserTermKeys.ACTIVE).containing(true);
			EntityQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(restriction).returningAtMost(EntityQuery.ALL_RESULTS);
			
			Map<String, String> userToRole = getUserToRoleMap(spaceKey, crowdService.search(query));
			
			Gson gson = new Gson();
			String updateData = request.getParameter("updateData");
			Map<String, String> updateDataMap = gson.fromJson(updateData, new TypeToken<HashMap<String, String>>() {
			}.getType());

			for (String username : updateDataMap.keySet()) {
				String oldRole = userToRole.get(username);
				String newRole = updateDataMap.get(username);

				String oldGroup = "";
				if (oldRole.equalsIgnoreCase(APPROVER)) {
					oldGroup = approversGroup;
				} else if (oldRole.equalsIgnoreCase(AUTHOR)) {
					oldGroup = authorsGroup;
				} else if (oldRole.equalsIgnoreCase(REVIEWER)) {
					oldGroup = reviewersGroup;
				}

				if (!oldRole.equalsIgnoreCase(newRole)) {

					if (newRole.equalsIgnoreCase(NONE)) {
						userAccessor.removeMembership(oldGroup, username);
					} else if (newRole.equalsIgnoreCase(APPROVER)) {
						if (!oldRole.equalsIgnoreCase(NONE)) {
							userAccessor.removeMembership(oldGroup, username);
						}
						userAccessor.addMembership(approversGroup, username);
					} else if (newRole.equalsIgnoreCase(AUTHOR)) {
						if (!oldRole.equalsIgnoreCase(NONE)) {
							userAccessor.removeMembership(oldGroup, username);
						}
						userAccessor.addMembership(authorsGroup, username);
					} else if (newRole.equalsIgnoreCase(REVIEWER)) {
						if (!oldRole.equalsIgnoreCase(NONE)) {
							userAccessor.removeMembership(oldGroup, username);
						}
						userAccessor.addMembership(reviewersGroup, username);
					} else {
						log.trace("newRole: " + newRole);
						throw new ServletException("Attempted to change a user's role to an invalid one.");
					}

					if (!newRole.equalsIgnoreCase(NONE)) {
						sendInviteEmail(username, currentUsername, spaceKey);
					}
				}
			}

//			log.trace("\noldDataMap: " + oldDataMap.toString());
//			log.trace("\nupdateDataMap: " + updateDataMap.toString());
		} catch (RuntimeException e) {
			throw e;
		} catch (ServletException se) {
			throw se;
		} catch (MailException e) {
			e.printStackTrace();
		} finally {
			AuthenticatedUserThreadLocal.set(currentUser);
		}

	}

	private void sendInviteEmail(String inviteeUsername, String approverUsername, String spaceKey) throws MailException {
		Map<String, Object> velocityCtx = MacroUtils.defaultVelocityContext();

		ConfluenceUser user = Global.getUserByName(inviteeUsername);
		ConfluenceUser approver = Global.getUserByName(approverUsername);

		Space space = Global.getSpaceManager().getSpace(spaceKey);

		Invite invite = new Invite(user.getFullName(), user.getEmail(), spaceKey);
		invite.setApproverName(approverUsername);

		velocityCtx.put("invite", invite);
		velocityCtx.put("confluenceBaseUrl", Global.getConfluenceBaseUrl());

		// Velocity Tools
		velocityCtx.put("date", new DateTool());

		String renderedTemplate = VelocityUtils.getRenderedTemplate("/templates/invite-existing-user-email-template.vm", velocityCtx);

		String emailTo = user.getEmail();

		String ccTo = approver.getEmail();

		MailQueueItem mailQueueItem = new ConfluenceMailQueueItem(emailTo, ccTo, "Invite to InfoScribe space " + space.getName(), renderedTemplate, MIME_TYPE_HTML);
		// log.trace(renderedTemplate);
		mailQueueItem.send();
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
	public void destroy() {
		getServletContext().log("destroy() called");
	}

}
