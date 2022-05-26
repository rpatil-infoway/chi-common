package ca.infoway.confluence.plugins.common.spaceInfo;

import static com.atlassian.confluence.mail.template.ConfluenceMailQueueItem.MIME_TYPE_HTML;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.tools.generic.DateTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.infoway.confluence.plugins.common.Global;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.mail.template.ConfluenceMailQueueItem;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.actions.PageAware;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.MailQueueItem;

public class InviteUserAction extends ConfluenceActionSupport implements PageAware {

	private static final Logger log = LoggerFactory.getLogger(InviteUserAction.class);

	private static List<Invite> invites;

	private AbstractPage page;

	private String inviteId;
	private String action;
	private String inviteeName;
	private String inviteeEmail;
	private String inviteeRole;
	private BandanaManager bandanaManager;

	public InviteUserAction() {
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

	public void setInviteeName(String inviteName) {
		this.inviteeName = inviteName;
	}

	public void setInviteeEmail(String inviteEmail) {
		this.inviteeEmail = inviteEmail;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public void setInviteId(String inviteId) {
		this.inviteId = inviteId;
	}

	public void setInviteeRole(String inviteeRole) {
		this.inviteeRole = inviteeRole;
	}

	public void setBandanaManager(BandanaManager bandanaManager) {
		this.bandanaManager = bandanaManager;
	}

	public String execute() {
		log.trace("start");

		ConfluenceUser currentUser = AuthenticatedUserThreadLocal.get();

		if (action.equals("new-invite")) {

			// validate input parameters
			if (!validateParameters()) {
				return "error";
			}

			Invite newInvite = new Invite(inviteeName, inviteeEmail, getPage().getSpaceKey());
			newInvite.setRole(inviteeRole);
			newInvite.setStartDate(new Date());
			newInvite.setLastSendDate(new Date());
			newInvite.setAndCalculateExpiredDate();
			newInvite.setApproverName(currentUser.getName());

			Object invitesObject = bandanaManager.getValue(new ConfluenceBandanaContext(getPage().getSpaceKey()), Invite.BANDANA_KEY);

			invites = invitesObject == null ? new ArrayList<Invite>() : (List<Invite>) invitesObject;
			if (isUserAlreadyExists(newInvite)) {
				ConfluenceUser user = Global.getUserByEmail(newInvite.getInviteeEmail());
				addActionError("Another user with the same email, " + user.getFullName() + " - " + user.getEmail() + ", already exists in InfoScribe. Please use Manage Users to give access to this user.");
				return "error";
			}
			
			if (isInviteAlreadyExists(invites, newInvite)) {
				addActionError("Another invite with the same email address already exists.");
				return "error";
			}
			
			try {
				sendInviteEmail(newInvite);
			} catch (MailException e) {
				addActionError("Unable to send an email to " + inviteeEmail + ". Please verify the email address is valid or try again later.");
				return "error";
			}

			invites.add(newInvite);

			bandanaManager.setValue(new ConfluenceBandanaContext(getPage().getSpaceKey()), Invite.BANDANA_KEY, invites);

			log.trace("invites size: " + invites.size());

			log.trace(newInvite.toString());
		} else if (action.equals("invite-again")) {
			invites = (List<Invite>) bandanaManager.getValue(new ConfluenceBandanaContext(getPage().getSpaceKey()), Invite.BANDANA_KEY);
			Invite invite = findInvite(inviteId);
			try {
				sendInviteEmail(invite);
			} catch (MailException e) {
				addActionError("Unable to send an email to " + inviteeEmail + ". Please verify the email address is valid or try again later.");
				return "error";
			}
			
			invite.setLastSendDate(new Date());
			bandanaManager.setValue(new ConfluenceBandanaContext(getPage().getSpaceKey()), Invite.BANDANA_KEY, invites);
		} else if (action.equals("cancel-invite")) {
			invites = (List<Invite>) bandanaManager.getValue(new ConfluenceBandanaContext(getPage().getSpaceKey()), Invite.BANDANA_KEY);
			removeInvite(inviteId);
			bandanaManager.setValue(new ConfluenceBandanaContext(getPage().getSpaceKey()), Invite.BANDANA_KEY, invites);
		} else {
			addActionError("Unkown action = " + action);
			return "error";
		}

		return "success";

	}

	private boolean validateParameters() {

		inviteeName = inviteeName.trim();
		inviteeEmail = inviteeEmail.trim();

		if (inviteeName.isEmpty()) {
			addActionError("Name is missing.");
		}

		if (inviteeEmail.isEmpty()) {
			addActionError("Email is missing.");
		} else if (!isValidEmailAddress(inviteeEmail)) {
			addActionError("Email address is invalid.");
		}
		
		return getActionErrors().size() > 0 ? false : true;
	}

	private static boolean isValidEmailAddress(String email) {
		boolean result = true;
		try {
			InternetAddress emailAddr = new InternetAddress(email);
			emailAddr.validate();
		} catch (AddressException ex) {
			result = false;
		}
		return result;
	}

	private boolean isInviteAlreadyExists(List<Invite> invites, Invite newInvite) {
		for (Invite invite : invites) {
			if (invite.getInviteeEmail().equalsIgnoreCase(newInvite.getInviteeEmail())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isUserAlreadyExists(Invite newInvite) {
		return Global.getUserByEmail(newInvite.getInviteeEmail()) != null;
	}

	private Invite findInvite(String id) {
		for (Invite invite : invites) {
			if (invite.getId().equals(id)) {
				return invite;
			}
		}
		return null;
	}

	private void removeInvite(String id) {
		for (Invite invite : invites) {
			if (invite.getId().equals(id)) {
				invites.remove(invite);
				break;
			}
		}
	}

	private void sendInviteEmail(Invite invite) throws MailException {
		Map<String, Object> velocityCtx = MacroUtils.defaultVelocityContext();

		velocityCtx.put("invite", invite);
		velocityCtx.put("confluenceBaseUrl", Global.getConfluenceBaseUrl());
		
		//Velocity Tools
		velocityCtx.put("date", new DateTool());
		
		String renderedTemplate = VelocityUtils.getRenderedTemplate("/templates/invite-user-email-template.vm", velocityCtx);

		String emailTo = invite.getInviteeEmail();
		
		String ccTo = invite.getApprover() != null ? invite.getApprover().getEmail() : "";
		
		MailQueueItem mailQueueItem = new ConfluenceMailQueueItem(emailTo, ccTo, "Invite to InfoScribe space " + invite.getSpaceName(), renderedTemplate, MIME_TYPE_HTML);
//		log.trace(renderedTemplate);
		mailQueueItem.send();
	}
	
}
