package ca.infoway.confluence.plugins.common.micros;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;

public class InactiveUserRemoveServlet extends HttpServlet {

	private static final long serialVersionUID = -6567871786687992018L;
	private static final Logger log = LoggerFactory.getLogger(InactiveUserRemoveServlet.class);
	
	private UserAccessor userAccessor;
	
	public InactiveUserRemoveServlet(UserAccessor userAccessor) {
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
		ConfluenceUser currentUser = AuthenticatedUserThreadLocal.get();
		String userToRemove = request.getParameter("username");
		boolean hasAccess = false;
		List<String> userGroups = userAccessor.getGroupNames(currentUser);
		
		for (String group : userGroups) {
			log.trace("group:" + group);
			if (group.equalsIgnoreCase(UserAccessor.GROUP_CONFLUENCE_ADMINS)) {
				hasAccess = true;
				break;
			}
		}
		
		if (hasAccess) {
			userAccessor.removeUser(userAccessor.getUserByName(userToRemove));
			log.trace("User " + userToRemove + " removed.");
//			response.sendRedirect(location);
		} else {
			log.trace("This user does not have the required privileges.");
			response.sendError(403, "This user does not have the required privileges.");
		}
	}
	
	@Override
    public void destroy() {
        getServletContext().log("destroy() called");
    }
}
