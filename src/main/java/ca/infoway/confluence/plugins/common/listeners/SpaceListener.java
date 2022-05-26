package ca.infoway.confluence.plugins.common.listeners;

import java.util.Iterator;
import java.util.List;

import static com.atlassian.confluence.mail.template.ConfluenceMailQueueItem.MIME_TYPE_HTML;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.tools.generic.DateTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import ca.infoway.confluence.plugins.common.Global;
import ca.infoway.confluence.plugins.common.spaceInfo.CustomSpacePermissions;
import ca.infoway.confluence.plugins.common.spaceInfo.Invite;
import ca.infoway.confluence.plugins.common.spaceInfo.SpaceInfo;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.content.ContentProperties;
import com.atlassian.confluence.content.ContentProperty;
import com.atlassian.confluence.event.events.security.LoginEvent;
import com.atlassian.confluence.event.events.space.SpaceCreateEvent;
import com.atlassian.confluence.event.events.space.SpaceDetailsViewEvent;
import com.atlassian.confluence.event.events.space.SpaceRemoveEvent;
import com.atlassian.confluence.event.events.space.SpaceUpdateEvent;
import com.atlassian.confluence.labels.SpaceLabelManager;
import com.atlassian.confluence.mail.template.ConfluenceMailQueueItem;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.templates.PageTemplate;
import com.atlassian.confluence.plugins.createcontent.api.events.SpaceBlueprintHomePageCreateEvent;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.security.SpacePermissionManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.DefaultSpaceManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.json.jsonorg.JSONObject;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.MailQueueItem;
import com.atlassian.json.jsonorg.JSONArray;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseHandler;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.user.EntityException;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.crowd.model.authentication.Session;
import com.atlassian.user.Group;
import com.atlassian.user.GroupManager;
//import com.k15t.scroll.platform.event.space.VersionPublishEvent;  //confluence7.4 changes
import ca.infoway.confluence.plugins.common.listeners.ScrollVersionsPublishEventListener;
import org.springframework.transaction.support.TransactionTemplate;

public class SpaceListener implements DisposableBean {
	private static final Logger log = LoggerFactory
			.getLogger(SpaceListener.class);

	protected EventPublisher eventPublisher;
	private SettingsManager settingsManager;
	private GroupManager groupManager;
	private SpacePermissionManager spacePermissionManager;
	private UserAccessor userAccessor;
	private BandanaManager bandanaManager;
	private SpaceManager spaceManager;
	private SpaceLabelManager spaceLabelManager;
	
	private final RequestFactory<?> requestFactory;
	private final String INFOWAY_USERS_GROUP = "infoway-users";
	private final String ANONYMOUS_GROUP = "Anonymous";
	
	public SpaceListener(EventPublisher eventPublisher, 
			RequestFactory<?> requestFactory, 
			SpacePermissionManager spacePermissionManager, 
			SettingsManager settingsManager, 
			BandanaManager bandanaManager,
			UserAccessor userAccessor,
			GroupManager groupManager,
			SpaceManager spaceManager,
			SpaceLabelManager spaceLabelManager
			){
		this.eventPublisher = eventPublisher;
		this.spacePermissionManager = spacePermissionManager;
		this.settingsManager = settingsManager;
		this.bandanaManager = bandanaManager;
		this.groupManager = groupManager;
		this.userAccessor = userAccessor;
		this.requestFactory = requestFactory;
		this.spaceManager = spaceManager;
		this.spaceLabelManager = spaceLabelManager;
		eventPublisher.register(this);
	}

	@EventListener
	public void homePageCreatedEvent(SpaceBlueprintHomePageCreateEvent event) {
		log.trace("homePageCreatedEvent() starts.");
		
		String spaceBlueprintKey = event.getSpaceBlueprint().getI18nNameKey();
		
		log.debug("spaceBlueprintKey=" + spaceBlueprintKey);

		Space space = event.getSpace();
		String spaceKey = space.getKey();
		ConfluenceUser currentUser = event.getCreator();

		// ********************************************************
		// Create and assign custom groups to the space
		// ********************************************************
		
		try {
			groupManager.addMembership(groupManager.createGroup(Global.getApproverGroupName(spaceKey)), currentUser);
			groupManager.createGroup(Global.getAuthorGroupName(spaceKey));
			groupManager.createGroup(Global.getReviewerGroupName(spaceKey));
			
//			ArrayList<SpacePermission> originalPermissions = new ArrayList(space.getPermissions());
			
			spacePermissionManager.removeAllPermissions(space);
			
			for (String permission: CustomSpacePermissions.CONFLUENCE_ADMINISTRATOR_PERMISSION_TYPES) {
				spacePermissionManager.savePermission(new SpacePermission(permission, space, UserAccessor.GROUP_CONFLUENCE_ADMINS));
			}
			
			for (String permission: CustomSpacePermissions.INFOWAY_DOC_ADMINS_PERMISSION_TYPES) {
				spacePermissionManager.savePermission(new SpacePermission(permission, space, Global.GROUP_INFOWAY_DOC_ADMINS));
			}
			
			for (String permission: CustomSpacePermissions.DOC_ADMINS_PERMISSION_TYPES) {
				spacePermissionManager.savePermission(new SpacePermission(permission, space, Global.getApproverGroupName(spaceKey)));
			}
			
			for (String permission: CustomSpacePermissions.AUTHORS_PERMISSION_TYPES) {
				spacePermissionManager.savePermission(new SpacePermission(permission, space, Global.getAuthorGroupName(spaceKey)));
			}
			
			for (String permission: CustomSpacePermissions.REVIEWERS_PERMISSION_TYPES) {
				spacePermissionManager.savePermission(new SpacePermission(permission, space, Global.getReviewerGroupName(spaceKey)));
			}

			if (groupManager.getGroup(Global.GROUP_INFOWAY_DOC_ADMINS) == null) {
				log.info("The \"infoway-doc-admins\" group does not exist. Creating it now...");
				groupManager.createGroup(Global.GROUP_INFOWAY_DOC_ADMINS);
				log.info("The \"infoway-doc-admins\" group has been created.");
			}
			
			for (String permission: CustomSpacePermissions.INFOWAY_USERS_PERMISSION_TYPES) {
				spacePermissionManager.savePermission(new SpacePermission(permission, space, INFOWAY_USERS_GROUP));
			}
			
//			for (SpacePermission p : event.getSpace().getPermissions()){
//				log.info(" user=" + (p.getUserSubject() != null? p.getUserSubject().getKey() : null) + ", group=" + p.getGroup() + ", type=" + p.getType());
//			}
		} catch (EntityException e) {
			log.error("Error in adding membership", e);
		}

		enableScrollVersionsForSpace(spaceKey);
	}

	private void enableScrollVersionsForSpace(String spaceKey) {

		String baseUrl = settingsManager.getGlobalSettings().getBaseUrl();

		Request request = requestFactory.createRequest(Request.MethodType.POST, baseUrl + "/rest/scroll-versions/latest/config/" + spaceKey);

		request.addBasicAuthentication(baseUrl,Global.APP_ADMIN, Global.APP_ADMIN_PWD); //confluence7.4 upgrade
		request.setHeader("Content-Type", "application/json");
		request.setRequestBody("{" + "\"enableVersionManagement\":true,"
				+ "\"enableTheme\":true," 
				+ "\"enableWorkflow\":true,"
				+ "\"enableTranslation\":false," 
				+ "\"enableVariants\":true,"
				+ "\"enablePermalinks\":true," 
				+ "\"reviewers\":[\"" + Global.getReviewerGroupName(spaceKey) + "\", \"infoway-users\"],"
				+ "\"authors\":[\"" + Global.getAuthorGroupName(spaceKey) + "\"],"
				+ "\"docadmins\":[\"" + Global.getApproverGroupName(spaceKey) + "\", \"" + Global.GROUP_INFOWAY_DOC_ADMINS + "\", \"" + Global.GROUP_CONFLUENCE_ADMINISTRATORS + "\"],"
				+ "\"translators\":[]" + "}");

		try {
			request.execute(new ResponseHandler() {
				@Override
				public void handle(Response response) throws ResponseException {
					// log.info(" response="
					// + response.getResponseBodyAsString());
				}
			});
		} catch (ResponseException e) {
			log.error("Error in sending Scroll Version Restful message", e);
		}
	}

	@EventListener
	public void spaceCreateEvent(SpaceCreateEvent event) {
		log.debug("SAM: Space Create Event=" + event.getSpace().getKey());
	}
	
	@EventListener
	public void spaceUpdateEvent(SpaceUpdateEvent event) {
		log.debug("SAM: Space Update Event=" + event.getSpace().getKey());
	}
	
	/*@EventListener
	public void versionPublishEvent(VersionPublishEvent event) throws EntityException {
		String baseUrl = settingsManager.getGlobalSettings().getBaseUrl();
//		log.debug("SAM: spaceKey=" + event.getSpaceKey() );
//		log.debug("SAM: targetSpaceKey=" + event.getTargetSpaceKey() );
//		log.debug("SAM: publishType=" + event.getPublishType() );
//		log.debug("SAM: versionId=" + event.getVersionId() );
		String sourceSpaceKey = event.getSpaceKey();
		String targetSpaceKey = event.getTargetSpaceKey();
		Space targetSpace = spaceManager.getSpace(targetSpaceKey);
		final ConfluenceBandanaContext targetSpaceBandanaContext = new ConfluenceBandanaContext(targetSpaceKey);
		
		Request request = requestFactory.createRequest(Request.MethodType.GET, baseUrl + "/rest/scroll-versions/1.0/context/"
				+ "?spaceKey=" + sourceSpaceKey 
				);
		
		//TODO test with non admin account
		
		request.setHeader("Content-Type", "application/json");
		final String publishedVersionId = event.getVersionId();
		log.trace("publishedVersionId: " + publishedVersionId);
		try {
			request.execute(new ResponseHandler() {
				@Override
				public void handle(Response response) throws ResponseException {
//					log.trace("spacelistener response="
//							 + response.getResponseBodyAsString());
							 
					JSONObject json = new JSONObject(response.getResponseBodyAsString());
//					log.trace("response = " + json.toString());
					JSONArray versions = json.getJSONArray("versions");
					log.trace("versions=" + versions.toString());
					Iterator<Object> versionIterator = versions.iterator();
					
					while( versionIterator.hasNext() ) {
					    JSONObject obj = (JSONObject) versionIterator.next();
					    String vId = obj.getString("versionId");
					    if (publishedVersionId.equalsIgnoreCase(vId)) {
					    	log.trace("match found: " + vId);
					    	String vName = obj.getString("name");
					    	log.trace("Settings working version data to the published space...");
							bandanaManager.setValue(targetSpaceBandanaContext, "ca.infoway.confluence.blueprint.workingversion", vName);
							log.trace("Setting complete.");
							break;
					    }
					}
					
				}
			});
		} catch (ResponseException e) {
			log.error("Error in restful message", e);
		}
		
		// with event.getVersionId, we can get the correct version from bandana and set it to the targetSpace
		

//		StaticAccessor.getTransactionTemplate().execute(new TransactionCallback()
//		{
//		    @Override
//		    public Object doInTransaction()
//		    {
//		    	spaceLabelManager.addLabel(targetSpace, "published");
//
//			    return null;
//		    }
//		});
//		spaceLabelManager.addLabel(targetSpace, "published");
		
		// get space info from the source space and copy it into the new space
		SpaceInfo sourceSpaceInfo = (SpaceInfo) bandanaManager.getValue(new ConfluenceBandanaContext(sourceSpaceKey), "ca.infoway.confluence.blueprint.info");
		if (sourceSpaceInfo != null) {
			bandanaManager.setValue(targetSpaceBandanaContext, "ca.infoway.confluence.blueprint.info", sourceSpaceInfo);
		} else {
			log.trace("Could not find space info for source space at key [" + sourceSpaceKey + "].");
		}
		
		log.info("Scroll Verions pubished space (" + event.getTargetSpaceKey() + ") - Removed default permissions and gave anonymous view access");
		spacePermissionManager.removeAllPermissions(targetSpace);
		
		String sourceSpaceReviewers = Global.getReviewerGroupName(sourceSpaceKey);
		String sourceSpaceDocAdmins = Global.getApproverGroupName(sourceSpaceKey);
		String sourceSpaceAuthors = Global.getAuthorGroupName(sourceSpaceKey);
		
		log.trace("sourceSpaceReviewers: " + sourceSpaceReviewers);
		log.trace("sourceSpaceDocAdmins: " + sourceSpaceDocAdmins);
		log.trace("sourceSpaceAuthors: " + sourceSpaceAuthors);
		// if the user is an approver or infoway-doc-admin, give authentication
		ConfluenceUser confluenceUser = AuthenticatedUserThreadLocal.get();

		Group approversGroup = groupManager.getGroup(Global.getApproverGroupName(sourceSpaceKey));
		Group infowayDocAdminsGroup = groupManager.getGroup(Global.GROUP_INFOWAY_DOC_ADMINS);
		
		if (userAccessor.hasMembership(approversGroup, confluenceUser) || userAccessor.hasMembership(infowayDocAdminsGroup, confluenceUser)) {
			AuthenticatedUserThreadLocal.set(userAccessor.getUserByName(Global.APP_ADMIN));
		}
		log.trace("Adding permissions...");
//		addPublishedGroupPermission(sourceSpaceReviewers, targetSpace);
		addPublishedGroupPermission(sourceSpaceAuthors, targetSpace,
				SpacePermission.VIEWSPACE_PERMISSION,
				SpacePermission.CREATEEDIT_PAGE_PERMISSION,
				SpacePermission.COMMENT_PERMISSION,				
				SpacePermission.CREATE_ATTACHMENT_PERMISSION,
				SpacePermission.SET_PAGE_PERMISSIONS_PERMISSION,
				SpacePermission.EXPORT_SPACE_PERMISSION
		);
		
		addPublishedGroupPermission(sourceSpaceDocAdmins, targetSpace, 
				SpacePermission.VIEWSPACE_PERMISSION,
				SpacePermission.CREATEEDIT_PAGE_PERMISSION,
				SpacePermission.REMOVE_PAGE_PERMISSION,
				SpacePermission.COMMENT_PERMISSION,
				SpacePermission.REMOVE_COMMENT_PERMISSION,
				SpacePermission.CREATE_ATTACHMENT_PERMISSION,
				SpacePermission.REMOVE_ATTACHMENT_PERMISSION,
				SpacePermission.SET_PAGE_PERMISSIONS_PERMISSION,				
				SpacePermission.EXPORT_SPACE_PERMISSION,
				SpacePermission.ADMINISTER_SPACE_PERMISSION
		);
		
//		addPublishedGroupPermission("infoway-doc-admins", targetSpace, 
//				SpacePermission.VIEWSPACE_PERMISSION,
//				SpacePermission.CREATEEDIT_PAGE_PERMISSION,
//				SpacePermission.REMOVE_PAGE_PERMISSION,
//				SpacePermission.COMMENT_PERMISSION,
//				SpacePermission.REMOVE_COMMENT_PERMISSION,
//				SpacePermission.CREATE_ATTACHMENT_PERMISSION,
//				SpacePermission.REMOVE_ATTACHMENT_PERMISSION,
//				SpacePermission.SET_PAGE_PERMISSIONS_PERMISSION,
//				SpacePermission.EXPORT_SPACE_PERMISSION
//		);
		
		//set user and group to null for anonymous
		//spacePermissionManager.savePermission(new SpacePermission(SpacePermission.VIEWSPACE_PERMISSION, targetSpace, null, (ConfluenceUser)null));
		//set viewable by confluence-users
		spacePermissionManager.savePermission(new SpacePermission(SpacePermission.VIEWSPACE_PERMISSION, targetSpace, UserAccessor.GROUP_CONFLUENCE_USERS));
		
		AuthenticatedUserThreadLocal.set(confluenceUser);
	}*/

//	@EventListener
//	public void spaceDetailsViewEvent(SpaceDetailsViewEvent event) {
////		log.debug("SAM: space.type=" + event.getSpace().getType() );
////		log.debug("SAM: space.spacetype=" + event.getSpace().getSpaceType() );
////		log.debug("SAM: space.id=" + event.getSpace().getId() );
////		log.debug("SAM: space.key=" + event.getSpace().getKey() );
////		for (Object x : event.getSpace().getPageTemplates()) {
////		log.debug("SAM: space.template.name=" + ((PageTemplate)x).getName() );
////		log.debug("SAM: space.template.name=" + ((PageTemplate)x).getVersion() );
////		}
//				
//	}
	
//	@EventListener
//	public void loginEvent(LoginEvent event) {
//		log.debug("enter.");
//		
//		String bandanaKey = "com.k15t.scroll.versions.model.ScrollUserSettings.user." + event.getUsername();
//		log.debug("bandanaKey=" + bandanaKey);
//		Object value = bandanaManager.getValue(new ConfluenceBandanaContext(), bandanaKey);
//
//		// check to see if the login user has Scroll Version User Settings; if not, then insert default settings WITH 
//		if (value == null) {
//			value = "<com.k15t.scroll.versions.model.settings.DefaultScrollUserSettings> <highlightIncludes>true</highlightIncludes> <highlightConditionalContent>true</highlightConditionalContent> <showArchivedVersions>false</showArchivedVersions> <displayPageInfoPanel>true</displayPageInfoPanel> <showTopLevelPages>true</showTopLevelPages> <isShowUnavailablePages>true</isShowUnavailablePages> <rememberTreeState>true</rememberTreeState> <workingVersionIds/> <variantIds/> <languageCodes/> </com.k15t.scroll.versions.model.settings.DefaultScrollUserSettings>";
//			bandanaManager.setValue(new ConfluenceBandanaContext(), bandanaKey, value);
//		}
//	}

	@EventListener
	public void spaceRemoveEvent(SpaceRemoveEvent event) throws MailException {
		log.debug("enter");
		String spaceKey = event.getSpace().getKey();
		
		sendDeleteNoficationEmail(event);
		
		removeGroupIfExist(Global.getReviewerGroupName(spaceKey));
		removeGroupIfExist(Global.getAuthorGroupName(spaceKey));
		removeGroupIfExist(Global.getApproverGroupName(spaceKey));
	}
	 
	private void sendDeleteNoficationEmail(SpaceRemoveEvent event) throws MailException {
		Map<String, Object> velocityCtx = MacroUtils.defaultVelocityContext();
		
		ConfluenceUser confluenceUser = AuthenticatedUserThreadLocal.get();
		Space space = event.getSpace();
		
		velocityCtx.put("toName", confluenceUser.getFullName());
		String emailTo = confluenceUser.getEmail();
		velocityCtx.put("ccName", "Sam Tse");
		String emailCc = "stse@infoway-inforoute.ca";
		velocityCtx.put("spaceName", space.getName());
		velocityCtx.put("spaceKey", space.getKey());
		velocityCtx.put("date", (new DateTool()).getDate().toString());
		
		String renderedTemplate = VelocityUtils.getRenderedTemplate("/templates/delete-space-email-template.vm", velocityCtx);
		
		MailQueueItem mailQueueItem = new ConfluenceMailQueueItem(emailTo, emailCc, "InfoScribe space deleted - " + space.getName(), renderedTemplate, MIME_TYPE_HTML);
//		log.trace(renderedTemplate);
		mailQueueItem.send();
	}
 
	
	private void removeGroupIfExist(String groupName) {
		try {
			Group group = groupManager.getGroup(groupName);			
			if (group != null) {
				groupManager.removeGroup(group);
			}
		} catch (EntityException e) {
			e.printStackTrace();
		}
	}
	
	private void addPublishedGroupPermission(String groupName, Space targetSpace, String... spacePermissions) {
		for (String spacePermission: spacePermissions){
			spacePermissionManager.savePermission(new SpacePermission(spacePermission, targetSpace, groupName));	
		}
	}

	// Unregister the listener if the plugin is uninstalled or disabled.
	public void destroy() throws Exception {
		eventPublisher.unregister(this);
	}
}