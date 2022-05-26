package ca.infoway.confluence.plugins.common.spaceInfo;

import java.util.List;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.core.InsufficientPrivilegeException;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.security.SpacePermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;

public class ToggleSpaceVisibilityServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8377413129995985526L;
	private static final Logger log = LoggerFactory.getLogger(ToggleSpaceVisibilityServlet.class);
	private SpacePermissionManager spacePermissionManager;
	private SpaceManager spaceManager;
	private UserAccessor userAccessor;
	
	public ToggleSpaceVisibilityServlet(SpacePermissionManager spacePermissionManager
			, SpaceManager spaceManager, UserAccessor userAccessor) {
		this.spacePermissionManager = spacePermissionManager;
		this.spaceManager = spaceManager;
		this.userAccessor = userAccessor;
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
        super.init(config);
        getServletContext().log("init() called");
        
    }
	
	@Override
	 protected void service(HttpServletRequest request, HttpServletResponse response) 
			 throws ServletException, IOException {
		/*  
		 * - button will determine whether to make it private or public and send it via parameter 
		 * 	-- but should still check if the permission exists JIC
		 * - no need for button macro like the user manager, since theres nothing to render
		 * 
		 * - groupHasPermission(String permissionType, Space space, String group) 
		 * - removePermission(SpacePermission permission) 
		 * - savePermission(SpacePermission permission) 
		 * 
		 * - declaring a new permission: 
		 * 		new SpacePermission(permission, space, groupName)
		 * 			permission - should most likely be SpacePermission.VIEWSPACE_PERMISSION
		 * 			space - get from parameter
		 * 			groupName - "confluence-users"
		*/ 
		String spaceVisibility = request.getParameter("spaceVisibility");
		String currentSpaceKey = request.getParameter("spaceKey");
		log.trace("spaceVisibility: " + spaceVisibility);
		log.trace("currentSpaceKey: " + currentSpaceKey);
		Space currentSpace = spaceManager.getSpace(currentSpaceKey);
		ConfluenceUser confluenceUser = AuthenticatedUserThreadLocal.get();
		
		//get this space's groups, and get user's groups, and check if they match
		List<SpacePermission> spacePermissions = currentSpace.getPermissions();
		List<String> userGroupNames = userAccessor.getGroupNames(confluenceUser);
		boolean hasAccess = false;

		for (SpacePermission sp : spacePermissions) {
			String groupName = sp.getGroup();
			if (userGroupNames.contains(groupName) && groupName.contains("_doc-admins") && !groupName.equalsIgnoreCase("confluence-users")) {
				hasAccess = true;
				break;
			}
		}
		
		if (hasAccess) 
			AuthenticatedUserThreadLocal.set(userAccessor.getUserByName("app-admin"));

		SpacePermission spPer = new SpacePermission(SpacePermission.VIEWSPACE_PERMISSION, currentSpace, "confluence-users");
		boolean permissionExists = spacePermissionManager.permissionExists(spPer);
		log.trace("permissionExists: " + Boolean.toString(permissionExists));
		// **** PRIVATE PERMISSION ****
		if (spaceVisibility.equalsIgnoreCase("private")) {
			if (permissionExists) {
				log.trace("changing to private...");
				List<SpacePermission> allPermissions = spacePermissionManager.getAllPermissionsForGroup("confluence-users");
				/*
				 * Simply removing spPer does not work, so we have to iterate through all of the 
				 * group's permissions and find the correct one to remove.
				 * REFERENCE: https://answers.atlassian.com/questions/218079/how-to-remove-comment-permission-for-a-group-from-a-space-from-java
				 */
				for (SpacePermission sp : allPermissions) {
					if (sp.equals(spPer)) {
						log.trace("removing...");
						try {
							spacePermissionManager.removePermission(sp);
						} catch (InsufficientPrivilegeException ipe) {
							ipe.printStackTrace();
							throw new ServletException("[INFOSCRIBE] You do not have sufficient privileges.");
						}
					}
				}
			} 
		} 
		// ***** PUBLIC PERMISSION ******
		else if (spaceVisibility.equalsIgnoreCase("public")) {
			if (!permissionExists) {
				log.trace("changing to public...");
				spacePermissionManager.savePermission(spPer);
			}
		}
		AuthenticatedUserThreadLocal.set(confluenceUser);
	}
	
	@Override
    public void destroy() {
        getServletContext().log("destroy() called");
    }

}
