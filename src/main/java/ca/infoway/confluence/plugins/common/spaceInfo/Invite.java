package ca.infoway.confluence.plugins.common.spaceInfo;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.infoway.confluence.plugins.common.Global;

import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.user.search.page.Pager;

public class Invite implements Comparable<Invite> {
	
	private static final Logger log = LoggerFactory.getLogger(Invite.class);

	private String id;
	private String inviteeName;
	private String inviteeEmail;
	private String inviteeRole;
	private Date startDate;
	private Date lastSendDate;
	private Date expiredDate;
	private String approverName;
	private InviteStatus status;
	private String spaceKey;
	private String role;
	
	public static final String ROLE_APPROVER = "approver";
	public static final String ROLE_AUTHOR = "author";
	public static final String ROLE_REVIEWER = "reviewer";
	
	public static final String BANDANA_KEY = "ca.infoway.confluence.plugins.common.spaceInfo.invites"; 
	
	public Invite() {
	}
	
	public Invite(String name, String email, String spaceKey){
		this.id = UUID.randomUUID().toString();
		this.inviteeName = name;
		this.inviteeEmail = email;
		this.spaceKey = spaceKey;
		this.status = InviteStatus.ACTIVE;
	}

	public String getInviteeName() {
		return inviteeName;
	}
	public void setInviteeName(String inviteeName) {
		this.inviteeName = inviteeName;
	}
	public String getInviteeEmail() {
		return inviteeEmail;
	}
	public void setInviteeEmail(String inviteeEmail) {
		this.inviteeEmail = inviteeEmail;
	}
	public String getInviteeRole() {
		return inviteeRole;
	}
	public void setInviteeRole(String inviteeRole) {
		this.inviteeRole = inviteeRole;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getLastSendDate() {
		return lastSendDate;
	}
	public void setLastSendDate(Date lastSendDate) {
		this.lastSendDate = lastSendDate;
	}
	public Date getExpiredDate() {
		return expiredDate;
	}
	public void setExpiredDate(Date expiredDate) {
		this.expiredDate = expiredDate;
	}
	public String getApproverName() {
		return approverName;
	}
	public void setApproverName(String approverName) {
		this.approverName = approverName;
	}
	public InviteStatus getStatus() {
		return status;
	}
	public void setStatus(InviteStatus status) {
		this.status = status;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getSpaceKey() {
		return spaceKey;
	}

	public void setSpaceKey(String spacekey) {
		this.spaceKey = spacekey;
	}
	
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
	public boolean isRoleApprover() {
		return role.equalsIgnoreCase(this.ROLE_APPROVER);
	}
	
	public boolean isRoleAuthor() {
		return role.equalsIgnoreCase(this.ROLE_AUTHOR);
	}
	
	public boolean isRoleReviewer() {
		return role.equalsIgnoreCase(this.ROLE_REVIEWER);
	}

	public void setAndCalculateExpiredDate() {
		Calendar c = Calendar.getInstance(); 
		c.setTime(this.lastSendDate); 
		c.add(Calendar.DATE, 28);
		this.expiredDate = c.getTime();
	}
	
	public ConfluenceUser getApprover() {
		if (Global.getUserByName(this.approverName) != null) {
			return Global.getUserByName(this.approverName);
		} else  {
			Pager<String> approverUsers = Global.getApproverMembers(spaceKey);
			for (String nextApprover : approverUsers) {
				if (Global.getUserByName(nextApprover) != null) {
					return Global.getUserByName(nextApprover);
				}
			}
		}
		return null;
	}

	public String getSpaceName() {
		Space space = Global.getSpaceManager().getSpace(spaceKey);
		return space != null ? space.getName() : null;
	}
	
	public String getSpaceLink() {
		Space space = Global.getSpaceManager().getSpace(spaceKey);
		return space != null ? Global.getConfluenceBaseUrl() + space.getUrlPath() : null;
	}

	@Override
	public String toString() {
		String str = "";
		str += "inviteeName: " + inviteeName + ", ";
		str += "inviteeEmail: " + inviteeEmail + ", ";
		str += "spaceKey: " + spaceKey + ", ";
		return str;
	}

	@Override
	public int compareTo(Invite that) {
		return this.getInviteeName().toLowerCase().compareTo(that.getInviteeName().toLowerCase());
	}

	
}
