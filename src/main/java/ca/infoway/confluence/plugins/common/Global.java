package ca.infoway.confluence.plugins.common;

import javax.servlet.http.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.infoway.confluence.plugins.common.createUser.IfSsoMacro;

import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.user.EntityException;
import com.atlassian.user.Group;
import com.atlassian.user.GroupManager;
import com.atlassian.user.User;
import com.atlassian.user.search.SearchResult;
import com.atlassian.user.search.page.Pager;
import com.opensymphony.webwork.ServletActionContext;

public class Global {
	
	private static final Logger log = LoggerFactory.getLogger(Global.class);
	
	public static final String GROUP_CONFLUENCE_ADMINISTRATORS = "confluence-administrators";
	public static final String GROUP_CONFLUENCE_USERS = "confluence-users";
	public static final String GROUP_INFOWAY_DOC_ADMINS = "infoway-doc-admins";
	
	public static final String INFOSCRIBE_PRODUCTION_URL = "https://infoscribe.infoway-inforoute.ca";
	public static final String INFOSCRIBE_STAGING_URL = "https://infoscribe.staging.emri.infoway-inforoute.ca";
	public static final String INFOSCRIBE_DEV_URL = "https://infoscribe.de-emri11.extra.infoway-inforoute.ca";
	
	public static final String OPENAM_PRODUCTION_SSO_TOKEN_NAME = "iPlanetDirectoryProPROD";
	public static final String OPENAM_STAGE_DEV_SSO_TOKEN_NAME = "iPlanetDirectoryPro";
	
	public static final String OPENAM_PRODUCTION_URL = "https://sso.infoway-inforoute.ca";
	public static final String OPENAM_STAGING_URL = "http://dp-open01.private.infoway-inforoute.ca";
	public static final String OPENAM_DEV_URL = "http://dp-open01.private.infoway-inforoute.ca";
	
	public static final String APP_ADMIN = "app-admin";
	public static final String APP_ADMIN_PWD = getAppAdminPwd();
	
    private static SettingsManager settingsManager;
    private static UserAccessor userAccessor;
    private static GroupManager groupManager;

	private static final String getAppAdminPwd() {
		return "fAHVafCBXAK5tpA";
	}
	
	public static String getSsoToken() {
		String ssoTokenName = getSsoTokenName();
		if (ServletActionContext.getRequest() == null || ServletActionContext.getRequest().getCookies() == null) {
			return null;
		}
		for (Cookie cookie: ServletActionContext.getRequest().getCookies()) {
			if (cookie.getName().equals(ssoTokenName)) {
				return cookie.getValue();
			}
		}
		return null;
	}
	
	public static boolean isInProduction() {
		return getConfluenceBaseUrl().toLowerCase().startsWith("https://infoscribe.infoway-inforoute.ca");
	}

	private static String getSsoTokenName() {
		String ssoTokenName = OPENAM_PRODUCTION_SSO_TOKEN_NAME;
		if (getConfluenceBaseUrl().equalsIgnoreCase(Global.INFOSCRIBE_PRODUCTION_URL)) {
			ssoTokenName = OPENAM_PRODUCTION_SSO_TOKEN_NAME;
		} else if (getConfluenceBaseUrl().equalsIgnoreCase(Global.INFOSCRIBE_STAGING_URL)) {
			ssoTokenName = OPENAM_STAGE_DEV_SSO_TOKEN_NAME;
		} else if (getConfluenceBaseUrl().equalsIgnoreCase(Global.INFOSCRIBE_DEV_URL)) {
			ssoTokenName = OPENAM_STAGE_DEV_SSO_TOKEN_NAME;
		}
		return ssoTokenName;
	}
	
	public static String getOpenAmBaseUrl() {
		String openAmBaseUrl = OPENAM_PRODUCTION_URL;
		if (getConfluenceBaseUrl().equalsIgnoreCase(Global.INFOSCRIBE_PRODUCTION_URL)) {
			openAmBaseUrl = Global.OPENAM_PRODUCTION_URL;
		} else if (getConfluenceBaseUrl().equalsIgnoreCase(Global.INFOSCRIBE_STAGING_URL)) {
			openAmBaseUrl = Global.OPENAM_STAGING_URL;
		} else if (getConfluenceBaseUrl().equalsIgnoreCase(Global.INFOSCRIBE_DEV_URL)) {
			openAmBaseUrl = Global.OPENAM_DEV_URL;
		} 
		return openAmBaseUrl;
	}
	
	public static String getConfluenceBaseUrl() {
		return settingsManager.getGlobalSettings().getBaseUrl();
	}
	
	public static boolean isCurrentUserConfluenceAdmin() {
		ConfluenceUser currentUser = AuthenticatedUserThreadLocal.get();
		return true; //confluence7.4 changes
		//return userAccessor.hasMembership(GROUP_CONFLUENCE_ADMINISTRATORS, currentUser.getName());
	}
	
	public static boolean isCurrentUserInfowayDocAdmin() {
		ConfluenceUser currentUser = AuthenticatedUserThreadLocal.get();
		return userAccessor.hasMembership(GROUP_INFOWAY_DOC_ADMINS, currentUser.getName());
	}
	
	public static boolean isCurrentUserApprover(String spaceKey) {
		ConfluenceUser currentUser = AuthenticatedUserThreadLocal.get();
		return userAccessor.hasMembership(getApproverGroupName(spaceKey), currentUser.getName());
	}
	
	public static boolean isCurrentUserAuthor(String spaceKey) {
		ConfluenceUser currentUser = AuthenticatedUserThreadLocal.get();
		return userAccessor.hasMembership(getAuthorGroupName(spaceKey), currentUser.getName());
	}
	
	public static boolean isCurrentUserReview(String spaceKey) {
		ConfluenceUser currentUser = AuthenticatedUserThreadLocal.get();
		return userAccessor.hasMembership(getReviewerGroupName(spaceKey), currentUser.getName());
	}
	
	public static String getApproverGroupName(String spaceKey) {
		return "_" + spaceKey.toLowerCase() + "_doc-admins";
	}
		
	public static String getAuthorGroupName(String spaceKey) {
		return "_" + spaceKey.toLowerCase() + "_authors";
	}

	public static String getReviewerGroupName(String spaceKey) {
		return "_" + spaceKey.toLowerCase() + "_reviewers";
	}
	
	public static Pager<String> getApproverMembers(String spaceKey) {
		try {
			return groupManager.getMemberNames(userAccessor.getGroup(getApproverGroupName(spaceKey)));
		} catch (EntityException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Pager<String> getAuthorMembers(String spaceKey) {
		try {
			return groupManager.getMemberNames(userAccessor.getGroup(getAuthorGroupName(spaceKey)));
		} catch (EntityException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Pager<String> getReviewerMembers(String spaceKey) {
		try {
			return groupManager.getMemberNames(userAccessor.getGroup(getReviewerGroupName(spaceKey)));
		} catch (EntityException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void setSettingsManager(SettingsManager settingsManager) {
		this.settingsManager = settingsManager;
	}
	
	public void setUserAccessor(UserAccessor userAccessor) {
		this.userAccessor = userAccessor;
	}

	public static void setGroupManager(GroupManager groupManager) {
		Global.groupManager = groupManager;
	}

	public static ConfluenceUser getUserByName(String username) {
		if (username == null) {
			return null;
		}
		return userAccessor.getUserByName(username);
	}
	
	public static ConfluenceUser getUserByEmail(String email) {
		if (email == null) {
			return null;
		}
		SearchResult result = userAccessor.getUsersByEmail(email);
		if (result != null && result.pager() != null && !result.pager().isEmpty()) {
			User user = (User) result.pager().iterator().next();
			return getUserByName(user.getName());
		}
		return null;
	}

	public static SpaceManager getSpaceManager() {
		//Note: Spring autowire does not work for spaceManager and instead it works with following code
		return (SpaceManager) ContainerManager.getComponent("spaceManager");
	}
	
	public static UserAccessor getUserAccessor() {
		return userAccessor;
	}

}
