package ca.infoway.confluence.plugins.common.spaceInfo;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.tools.generic.DateTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.infoway.confluence.plugins.common.Global;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.Renderer;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.security.SpacePermissionManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.user.EntityException;
import com.atlassian.user.Group;
import com.atlassian.user.GroupManager;
import com.atlassian.user.User;
//import com.k15t.scroll.platform.model.config.SpaceConfig;

public class SpaceInfoMacro implements Macro {

	private static final Logger log = LoggerFactory.getLogger(SpaceInfoMacro.class);
	private BandanaManager bandanaManager;
	private SpaceManager spaceManager;
	private GroupManager groupManager;
	private UserAccessor userAccessor;
	private Renderer viewRenderer;
	private SpacePermissionManager spacePermissionManager;

	public SpaceInfoMacro(BandanaManager bandanaManager, SpaceManager spaceManager, GroupManager groupManager, UserAccessor userAccessor, Renderer viewRenderer, SpacePermissionManager spacePermissionManager) {
		this.bandanaManager = bandanaManager;
		this.spaceManager = spaceManager;
		this.groupManager = groupManager;
		this.userAccessor = userAccessor;
		this.viewRenderer = viewRenderer;
		this.spacePermissionManager = spacePermissionManager;
	}

	@Override
	public String execute(Map<String, String> parameters, String body, ConversionContext context) throws MacroExecutionException {
		log.trace("execute macro!");
		String currentSpaceKey = context.getSpaceKey();
		Space currentSpace = spaceManager.getSpace(currentSpaceKey);
		Map<String, Object> velocityCtx = MacroUtils.defaultVelocityContext();
		velocityCtx.put("spaceKey", currentSpaceKey);
		String tipMacroStorageFormat = "<ac:structured-macro ac:name='chi-tips'>" 
              + "<ac:parameter ac:name='key'>REPLACETHISKEY</ac:parameter>"
              + "</ac:structured-macro>";
		/*
		 * NOTE: MAKE SURE METADATA PLUGIN IS INSTALLED!
		 * https://marketplace.atlassian.com/plugins/org.andya.confluence.plugins.metadata
		 */
		String metadataMacroStorageFormat = "<ac:structured-macro ac:name='metadata'>"
				+ "<ac:parameter ac:name='0'>publishedWorkingVersion</ac:parameter>"
				+ "<ac:parameter ac:name='hidden'>false</ac:parameter>"
				+ "<ac:plain-text-body><![CDATA[REPLACETHISVALUE]]></ac:plain-text-body>"
				+ "</ac:structured-macro>";
		
		String spaceUserManagerMacroStorageFormat = "<ac:structured-macro ac:name='chi-space-user-manager'/>";
		
		ConfluenceBandanaContext currentSpaceBandanaContext = new ConfluenceBandanaContext(currentSpaceKey);
		
		// NOTE: appending the variable with "WithHtml" will prevent it from escaping in Velocity
		String spaceConfigString = (String) bandanaManager.getValue(currentSpaceBandanaContext, "com.k15t.scroll.versions.services.space.config");
		
		boolean isVersioned = false;
		if (spaceConfigString != null) {
			log.trace("SpaceConfig exists.");
			String startTag = "<isContentManagerEnabled>";
			String endTag = "</isContentManagerEnabled>";
			int startIndex = spaceConfigString.indexOf(startTag) + startTag.length();
			int endIndex = spaceConfigString.indexOf(endTag);
			String isVersionedString = spaceConfigString.substring(startIndex, endIndex);
			log.trace("====================\n\n\n\n\nisVersionedString: " + isVersionedString);
			isVersioned = Boolean.valueOf(isVersionedString);
		} else {
			log.trace("SpaceConfig doesn't exist.");
		}
		velocityCtx.put("isVersioned", isVersioned);	
		
		velocityCtx.put("approversTipWithHtml", viewRenderer.render(tipMacroStorageFormat.replace("REPLACETHISKEY", "Approvers"), context));
		velocityCtx.put("authorsTipWithHtml", viewRenderer.render(tipMacroStorageFormat.replace("REPLACETHISKEY", "Authors"), context));
		velocityCtx.put("reviewersTipWithHtml", viewRenderer.render(tipMacroStorageFormat.replace("REPLACETHISKEY", "Reviewers"), context));
		velocityCtx.put("spaceUserManagerWithHtml", viewRenderer.render(spaceUserManagerMacroStorageFormat, context));
		
		String workingVersionName = "";
		if (!isVersioned) {
			workingVersionName = (String) bandanaManager.getValue(currentSpaceBandanaContext, "ca.infoway.confluence.blueprint.workingversion");
			if (workingVersionName == null) {
				workingVersionName = "";
			}
			log.trace("workingVersionName: " + workingVersionName);
		}
		String renderedMetadataTemplate = viewRenderer.render(metadataMacroStorageFormat.replace("REPLACETHISVALUE", workingVersionName), context);
		log.trace(renderedMetadataTemplate);
		velocityCtx.put("metadataTemplateWithHtml", renderedMetadataTemplate);
		// need to put boolean of whether this space is public or private -- check if permission exists
		SpacePermission spPer = new SpacePermission(SpacePermission.VIEWSPACE_PERMISSION , currentSpace , "confluence-users");
		boolean permissionExists = spacePermissionManager.permissionExists(spPer);
		log.trace("permissionExists: " + Boolean.toString(permissionExists));
		velocityCtx.put("permissionExists", permissionExists);
		

		SpaceInfo spaceInfo = (SpaceInfo) bandanaManager.getValue(currentSpaceBandanaContext, "ca.infoway.confluence.blueprint.info");
		
		if (spaceInfo != null) {
			velocityCtx.put("jurisdictions", spaceInfo.getJurisdictions() == null ? null : StringUtils.join(spaceInfo.getJurisdictions(), ", "));
			velocityCtx.put("domain", spaceInfo.getDomain());
			velocityCtx.put("clinical-area", spaceInfo.getClinicalArea());
			velocityCtx.put("standards", StringUtils.join(spaceInfo.getStandards(), ", "));
			velocityCtx.put("standards-version", spaceInfo.getStandardsVersion());
			velocityCtx.put("organizations", StringUtils.join(spaceInfo.getOrganizations(), ", "));
			velocityCtx.put("additional-metadata", StringUtils.join(spaceInfo.getMetadata(), ", "));
		}
		
		List<SpacePermission> permissionList = currentSpace.getPermissions();
		// log.trace(permissionList.toString());
		List<String> uniqueGroupList = new ArrayList<String>();
		List<String> userPermissionsList = new ArrayList<String>();

		for (SpacePermission permission : permissionList) {
			if (permission.isGroupPermission()) {
				String groupName = permission.getGroup();
				if (!uniqueGroupList.contains(groupName)) {
					uniqueGroupList.add(groupName);
				}
			} else if (permission.isUserPermission()) {
				String userName = permission.getUserSubject().getName();
				if (!userPermissionsList.contains(userName)) {
					userPermissionsList.add(userName);
				}
			}
		}

		log.trace("uniqueGroupList: " + uniqueGroupList.toString());
		log.trace("userPermissionsList: " + userPermissionsList.toString());

		Map<String, List<String>> groupNameToUsers = new HashMap<String, List<String>>();
		List<String> usersWithAccessList = new ArrayList<String>();
		try {
			for (String groupName : uniqueGroupList) {

				Group group = groupManager.getGroup(groupName);

				List<String> listUsernames = userAccessor.getMemberNamesAsList(group);
				List<String> formattedDisplayUserNames = new ArrayList<String>();
				for (String username : listUsernames) {
					String userFullName = userAccessor.getUserByName(username).getFullName();
					formattedDisplayUserNames.add(userFullName + " (" + username + ")");
				}

				if (groupName.equalsIgnoreCase(Global.getApproverGroupName(currentSpaceKey))) {
					groupName = "Approvers";
					
				} else if (groupName.equalsIgnoreCase(Global.getAuthorGroupName(currentSpaceKey))) {
					groupName = "Authors";
				} else if (groupName.equalsIgnoreCase(Global.getReviewerGroupName(currentSpaceKey))) {
					groupName = "Reviewers";
				} else {
					log.trace("ignoring group=" + groupName);
					continue;
				}
				Collections.sort(formattedDisplayUserNames, String.CASE_INSENSITIVE_ORDER);
				log.trace(formattedDisplayUserNames.toString());
				groupNameToUsers.put(groupName, formattedDisplayUserNames);

			}
		} catch (EntityException e) {
			log.error("SpaceInfoMacro error", e);
		}
		

		try {
			for (String groupName : uniqueGroupList) {
				Group group = groupManager.getGroup(groupName);
				List<String> listUsernames = userAccessor.getMemberNamesAsList(group);
				if (!(groupName.equalsIgnoreCase(Global.GROUP_INFOWAY_DOC_ADMINS)
						|| groupName.equalsIgnoreCase(Global.GROUP_CONFLUENCE_USERS)
						|| groupName.equalsIgnoreCase(Global.GROUP_CONFLUENCE_ADMINISTRATORS))
					&& groupName.contains("doc-admins")) {
					for (String username : listUsernames) {
						String userFullName = userAccessor.getUserByName(username).getFullName();
						String formattedEntry = userFullName + " (" + username + ")";
						if (!usersWithAccessList.contains(formattedEntry))
							usersWithAccessList.add(formattedEntry);
					}
				}
			}
		} catch (EntityException e) {
			// TODO Auto-generated catch block
			log.error("error in getting groups and users data", e);
		}
		
		velocityCtx.put("usersWithAccessList", usersWithAccessList);
		// a reverse-sorted version of the groupNameToUsers key set
		List<String> groupNames = new ArrayList<String>();
		groupNames.addAll(groupNameToUsers.keySet());
		// it just so happens that the order of increasing privileges of each role is the reverse order of their
		// alphabetical ordering c:
		Collections.sort(groupNames, Collections.reverseOrder());
		velocityCtx.put("groupNames", groupNames);
		velocityCtx.put("group-to-user-map", groupNameToUsers);
		velocityCtx.put("individual-users", userPermissionsList);
		velocityCtx.put("pageId", context.getEntity().getIdAsString());
		
		Object invitesObj = bandanaManager.getValue(new ConfluenceBandanaContext(context.getSpaceKey()), Invite.BANDANA_KEY);
		
		if (invitesObj != null && invitesObj instanceof List) {
			List<Invite> invites = (List<Invite>) invitesObj;
			Collections.sort(invites);
			
			velocityCtx.put("invites", invites);
		}
		velocityCtx.put("isShowInviteModule", true);
		velocityCtx.put("isUserAllowToInvite", Global.isCurrentUserConfluenceAdmin() || Global.isCurrentUserInfowayDocAdmin() || Global.isCurrentUserApprover(currentSpaceKey));
		
		//Velocity Tools
		velocityCtx.put("date", new DateTool());
		velocityCtx.put("stringUtils", new org.apache.velocity.util.StringUtils());

		String renderedTable = VelocityUtils.getRenderedTemplate("/templates/spaceinfo-template.vm", velocityCtx);
		
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

}
