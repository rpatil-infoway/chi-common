package ca.infoway.confluence.plugins.common.spaceInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;

public class EditSpaceInfoServlet extends HttpServlet {

	private static final long serialVersionUID = -7496886952767793242L;

	private static final Logger log = LoggerFactory.getLogger(EditSpaceInfoServlet.class);
	private BandanaManager bandanaManager;
	private SpaceManager spaceManager;
	private UserAccessor userAccessor;
	private PermissionManager permissionManager;
	
	public EditSpaceInfoServlet(BandanaManager bandanaManager, SpaceManager spaceManager,
			UserAccessor userAccessor, PermissionManager permissionManager) {
		this.bandanaManager = bandanaManager;
		this.spaceManager = spaceManager;
		this.userAccessor = userAccessor;
		this.permissionManager = permissionManager;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
        super.init(config);
        getServletContext().log("init() called");
        
    }
	
	@Override
	 protected void service(HttpServletRequest request, HttpServletResponse response) 
			 throws ServletException, IOException {
		String currentSpaceKey = request.getParameter("spaceKey");
		Space currentSpace = spaceManager.getSpace(currentSpaceKey);
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
		log.trace("hasAccess: " + hasAccess);
		
		if (hasAccess) {
			ConfluenceBandanaContext currentSpaceBandanaContext = new ConfluenceBandanaContext(currentSpaceKey);
			SpaceInfo newSpaceInfo = new SpaceInfoImpl();
			String[] jurisdictionsArray = request.getParameterValues("jurisdictions");
			String domain = request.getParameter("domain");
			String clinArea = request.getParameter("clinicalArea");
			String[] standardsArray  = request.getParameterValues("standards");
			String standardsVersion = request.getParameter("standardsVersion");
			String[] organizationsArray = request.getParameterValues("organizations");
			String[] metadataArray = request.getParameterValues("metadata");

			List<String> jurisdictions = new ArrayList<String>();
			List<String> standards = new ArrayList<String>();
			List<String> organizations = new ArrayList<String>();
			List<String> metadata = new ArrayList<String>();

			Collections.addAll(jurisdictions, jurisdictionsArray);
			Collections.addAll(standards, standardsArray);
			Collections.addAll(metadata, metadataArray);
			Collections.addAll(organizations, organizationsArray);
			
			log.trace("jurisdictions: " + jurisdictions);
			log.trace("domain: " + domain);
			log.trace("clinArea: " + clinArea);
			log.trace("standards: " + standards);
			log.trace("standardsVersion: " + standardsVersion);
			log.trace("organizations: " + organizations);
			log.trace("metadata: " + metadata);

			if (domain.isEmpty() || clinArea.isEmpty() || organizations.size() <= 0 || (organizations.size() == 1 && organizations.get(0).isEmpty())) {
				response.sendError(422, "One or more of the required fields are missing.");
				log.error("One or more of the required fields are missing.");
			} else {
				newSpaceInfo.setJurisdictions(jurisdictions);
				newSpaceInfo.setDomain(domain);
				newSpaceInfo.setClinicalArea(clinArea);
				newSpaceInfo.setStandards(standards);
				newSpaceInfo.setStandardsVersion(standardsVersion);
				newSpaceInfo.setOrganizations(organizations);
				newSpaceInfo.setMetadata(metadata);
				bandanaManager.setValue(currentSpaceBandanaContext, "ca.infoway.confluence.blueprint.info", newSpaceInfo);
			}
		} else {
			response.sendError(response.SC_FORBIDDEN, "User "+ confluenceUser.getName() + " does not the required privileges.");
			log.error( "User "+ confluenceUser.getName() + " does not the required privileges.");
		}
	}
	
	@Override
    public void destroy() {
        getServletContext().log("destroy() called");
    }
	
}
