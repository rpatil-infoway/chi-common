package ca.infoway.confluence.plugins.common.spaceInfo;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.confluence.util.velocity.VelocityUtils;

public class EditSpaceInfoMacro implements Macro {

	private static final Logger log = LoggerFactory.getLogger(EditSpaceInfoMacro.class);
	private BandanaManager bandanaManager;
	private PermissionManager permissionManager;
	private SpaceManager spaceManager;
	private UserAccessor userAccessor;
	private I18NBeanFactory i18NBeanFactory;
	
	public EditSpaceInfoMacro(BandanaManager bandanaManager, PermissionManager permissionManager,
			SpaceManager spaceManager, UserAccessor userAccessor, I18NBeanFactory i18NBeanFactory) {
			this.bandanaManager = bandanaManager;
			this.permissionManager = permissionManager;
			this.spaceManager = spaceManager;
			this.userAccessor = userAccessor;
			this.i18NBeanFactory = i18NBeanFactory;
	}

	@Override
	public String execute(Map<String, String> parameters, String body,
			ConversionContext context) throws MacroExecutionException {
		String spaceKey = context.getSpaceKey();
		Space currentSpace = spaceManager.getSpace(spaceKey);
		Map<String, Object> velocityCtx = MacroUtils.defaultVelocityContext();
		ConfluenceBandanaContext currentSpaceBandanaContext = new ConfluenceBandanaContext(currentSpace);
		SpaceInfo spaceInfo = (SpaceInfo) bandanaManager.getValue(currentSpaceBandanaContext, "ca.infoway.confluence.blueprint.info");
		ConfluenceUser confluenceUser = AuthenticatedUserThreadLocal.get();
		List<SpacePermission> spacePermissions = currentSpace.getPermissions();
		List<String> userGroupNames = userAccessor.getGroupNames(confluenceUser);
		boolean hasAccess = false;
		
		// only allow space info edits if the user is:
		// 	a doc-admin of the space, in infoway-doc-admins or is a confluence admin
		if (permissionManager.hasPermission(confluenceUser, Permission.ADMINISTER, PermissionManager.TARGET_APPLICATION)) {
			hasAccess = true;
		} else {
			for (SpacePermission sp : spacePermissions) {
				String groupName = sp.getGroup();
				if (groupName != null && (groupName.contains("_doc-admins") || groupName.equalsIgnoreCase("infoway-doc-admins"))) {
					if (userGroupNames.contains(groupName)) {
						hasAccess = true;
						break;
					}
				}
			}
		}
		
		// Only show it for space created either using the Clinical Req or Specification blueprint
		String blueprintId = (String) bandanaManager.getValue(new ConfluenceBandanaContext(currentSpace), "ca.infoway.confluence.blueprint.id");
		if (blueprintId != null && (blueprintId.equals(i18NBeanFactory.getI18NBean().getText("confluence.clinicalreq.space.blueprint.id")) || 
				blueprintId.equals(i18NBeanFactory.getI18NBean().getText("confluence.samplespec.space.blueprint.id")))) {
			velocityCtx.put("isVisible", true);	
		}else {
			velocityCtx.put("isVisible", false);
		}
		
		log.trace("hasAccess: " + hasAccess);
		if (spaceInfo != null) {
			if (hasAccess) {
				List<String> jurisdictions = spaceInfo.getJurisdictions();
				String domain = spaceInfo.getDomain();
				String clinArea = spaceInfo.getClinicalArea();
				List<String> standards = spaceInfo.getStandards();
				String standardsVersion = spaceInfo.getStandardsVersion();
				List<String> metadata = spaceInfo.getMetadata();
				List<String> organizations = spaceInfo.getOrganizations();
				
				velocityCtx.put("jurisdictions", jurisdictions);
				velocityCtx.put("domain", domain);
				velocityCtx.put("clinArea", clinArea);
				velocityCtx.put("standards", standards);
				velocityCtx.put("standardsVersion", standardsVersion);
				velocityCtx.put("metadata", metadata);
				velocityCtx.put("organizations", organizations);
				velocityCtx.put("spaceKey", spaceKey);
				String renderedDialog = VelocityUtils.getRenderedTemplate("/templates/edit-space-info-dialog.vm", velocityCtx);
				return renderedDialog;
			}
		}
		return "";
	}

	@Override
	public BodyType getBodyType() {
		return BodyType.NONE;
	}

	@Override
	public OutputType getOutputType() {
		// TODO Auto-generated method stub
		return OutputType.BLOCK;
	}

}
