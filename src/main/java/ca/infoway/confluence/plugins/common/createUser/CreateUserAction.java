package ca.infoway.confluence.plugins.common.createUser;

import static com.atlassian.confluence.mail.template.ConfluenceMailQueueItem.MIME_TYPE_HTML;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.velocity.tools.generic.DateTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.infoway.confluence.plugins.common.Global;
import ca.infoway.confluence.plugins.common.spaceInfo.Invite;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.mail.template.ConfluenceMailQueueItem;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.actions.PageAware;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.spaces.SpaceStatus;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.MailQueueItem;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseHandler;
import com.atlassian.user.impl.DefaultUser;
import com.atlassian.user.security.password.Credential;

public class CreateUserAction extends ConfluenceActionSupport implements PageAware {

	private static final Logger log = LoggerFactory.getLogger(CreateUserAction.class);

	private AbstractPage page;
	private String userName;
//	private String ssoToken;

	private RequestFactory<?> requestFactory;
	private UserAccessor userAccessor;
	private SpaceManager spaceManager;
	private BandanaManager bandanaManager;

	public CreateUserAction() {
	}

	@Override
	public AbstractPage getPage() {
		return page;
	}

	@Override
	public void setPage(AbstractPage page) {
		this.page = page;

	}

	@Override
	public boolean isPageRequired() {
		return true;
	}

	@Override
	public boolean isLatestVersionRequired() {
		return true;
	}

	@Override
	public boolean isViewPermissionRequired() {
		return true;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

//	public String getSsoToken() {
//		return ssoToken;
//	}
//
//	public void setSsoToken(String ssoToken) {
//		this.ssoToken = ssoToken;
//	}

	public RequestFactory<?> getRequestFactory() {
		return requestFactory;
	}

	public void setRequestFactory(RequestFactory<?> requestFactory) {
		this.requestFactory = requestFactory;
	}

	public SpaceManager getSpaceManager() {
		return spaceManager;
	}

	public void setSpaceManager(SpaceManager spaceManager) {
		this.spaceManager = spaceManager;
	}

	public UserAccessor getUserAccessor() {
		return userAccessor;
	}

	public void setUserAccessor(UserAccessor userAccessor) {
		this.userAccessor = userAccessor;
	}

	public BandanaManager getBandanaManager() {
		return bandanaManager;
	}

	public void setBandanaManager(BandanaManager bandanaManager) {
		this.bandanaManager = bandanaManager;
	}

	@SuppressWarnings("unchecked")
	public String execute() {
		log.trace("start");

		log.trace("username: " + userName);
		
		String ssoToken = Global.getSsoToken();

		// For testing only
		// ssoToken =
		// "AQIC5wM2LY4SfczfmeUkYVzdKthjzIhVNqjUwIW8KPNawhQ.*AAJTSQACMDEAAlNLABM4NjUxNTUyMjI5NDcxNDM3NDM4*";

		if (ssoToken == null || ssoToken.isEmpty()) {
			log.trace("ssoToken is missing");
			return "error";
		}
		
		String openAmBaseUrl = Global.getOpenAmBaseUrl();
		
		Request request = requestFactory.createRequest(Request.MethodType.POST, openAmBaseUrl + "/openam/identity/attributes");

		request.setHeader("Content-Type", "application/x-www-form-urlencoded");
		request.setRequestBody("subjectid=" + ssoToken);

		CreateUserResponseHandler response = new CreateUserResponseHandler();

		try {
			// send message to openAM and obtain username, fullname, and email
			request.execute(response);
		} catch (ResponseException e) {
			log.error("Error in sending message to openAM", e);
			return "error";
		}

		if (!response.isSucccess()) {
			return "error";
		}
		
		String ldapUsername = response.getLdapUsername();
		String ldapFullname = response.getLdapFullname();
		String ldapEmail = response.getLdapEmail();

		log.trace("ldapUsername=" + ldapUsername);
		log.trace("ldapFullname=" + ldapFullname);
		log.trace("ldapEmail=" + ldapEmail);

		if (userAccessor.getUserByName(ldapUsername) != null) {
			log.trace("user already exists in Confluence=" + ldapUsername);
			return "error";
		}

		AuthenticatedUserThreadLocal.set(userAccessor.getUserByName("app-admin"));

		DefaultUser userTemplate = new DefaultUser();
		userTemplate.setFullName(ldapFullname);
		userTemplate.setName(ldapUsername);
		userTemplate.setEmail(ldapEmail);

		String randomPassword = UUID.randomUUID().toString();

		ConfluenceUser confluenceUser = userAccessor.createUser(userTemplate, Credential.unencrypted(randomPassword));
		userAccessor.addMembership("confluence-users", ldapUsername);

		log.trace("created account for " + ldapFullname + " (" + ldapUsername + ")");
		
		AuthenticatedUserThreadLocal.set(confluenceUser);
		
		checkInvites(ldapUsername, ldapEmail);

		return "success";

	}

	private void checkInvites(String username, String email) {
		
		ConfluenceUser currentUser = AuthenticatedUserThreadLocal.get();

		try {
			AuthenticatedUserThreadLocal.set(userAccessor.getUserByName(Global.APP_ADMIN));

			for (String spaceKey : getSpaceManager().getAllSpaceKeys(SpaceStatus.CURRENT)) {

				Object invitesObject = getBandanaManager().getValue(new ConfluenceBandanaContext(spaceKey), Invite.BANDANA_KEY);

				if (invitesObject == null || !(invitesObject instanceof List)) {
					continue;
				}

				List<Invite> invites = (List<Invite>) invitesObject;

				List<Invite> invitesToBeRemoved = new ArrayList<Invite>(); 
				
				for (Invite invite : invites) {
					
					if (invite.getInviteeEmail().equalsIgnoreCase(email)) {

						String groupName = null;
						if (invite.isRoleApprover()) {
							groupName = Global.getApproverGroupName(spaceKey);
						} else if (invite.isRoleAuthor()) {
							groupName = Global.getAuthorGroupName(spaceKey);
						} else if (invite.isRoleReviewer()) {
							groupName = Global.getReviewerGroupName(spaceKey);
						}
						
						userAccessor.addMembership(groupName, username);

						invitesToBeRemoved.add(invite);
						
						try {
							sendInviteJoinedEmail(invite);
						} catch (MailException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				 if (!invitesToBeRemoved.isEmpty()) {
					for (Invite invite: invitesToBeRemoved) {
						invites.remove(invite);
					}
					getBandanaManager().setValue(new ConfluenceBandanaContext(spaceKey), Invite.BANDANA_KEY, invites);
				}

			}
		} finally {
			AuthenticatedUserThreadLocal.set(currentUser);
		}

		
//		String url = Global.getConfluenceBaseUrl() + "/plugins/servlet/infoway/notify-invite-listener?username=" + username + "&email=" + email;
//
//		Request request = requestFactory.createRequest(Request.MethodType.GET, url);
//
//		try {
//			request.execute(new ResponseHandler() {
//				
//				@Override
//				public void handle(Response response) throws ResponseException {
//					log.trace("Sent message to notify-invite-listener with status code=" + response.getStatusCode());
//				}
//			});
//		} catch (ResponseException e) {
//			log.error("Error in sending message to notify-invite-listener", e);
//		}
		
	}
	
	private void sendInviteJoinedEmail(Invite invite) throws MailException {
		Map<String, Object> velocityCtx = MacroUtils.defaultVelocityContext();

		velocityCtx.put("invite", invite);
		velocityCtx.put("confluenceBaseUrl", Global.getConfluenceBaseUrl());
		
		//Velocity Tools
		velocityCtx.put("date", new DateTool());
		
		String renderedTemplate = VelocityUtils.getRenderedTemplate("/templates/invite-user-joined-email-template.vm", velocityCtx);

		String emailTo = invite.getApprover().getEmail();
		
		String ccTo = "";
		
		MailQueueItem mailQueueItem = new ConfluenceMailQueueItem(emailTo, ccTo, "Invite accepted by " + invite.getInviteeName() + " for InfoScribe space " + invite.getSpaceName(), renderedTemplate, MIME_TYPE_HTML);
//		log.trace(renderedTemplate);
		mailQueueItem.send();
	}

}
