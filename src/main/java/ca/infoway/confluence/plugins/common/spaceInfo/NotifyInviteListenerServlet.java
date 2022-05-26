//package ca.infoway.confluence.plugins.common.spaceInfo;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.servlet.ServletConfig;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import ca.infoway.confluence.plugins.common.Global;
//
//import com.atlassian.bandana.BandanaManager;
//import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
//import com.atlassian.confluence.spaces.SpaceManager;
//import com.atlassian.confluence.spaces.SpaceStatus;
//import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
//import com.atlassian.confluence.user.ConfluenceUser;
//import com.atlassian.confluence.user.UserAccessor;
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//import com.atlassian.confluence.core.InsufficientPrivilegeException;
//import com.atlassian.event.api.EventPublisher;
//import com.atlassian.sal.api.net.RequestFactory;
//
//public class NotifyInviteListenerServlet extends HttpServlet {
//
//	private static final long serialVersionUID = -5634997286412701441L;
//
//	private static final Logger log = LoggerFactory.getLogger(NotifyInviteListenerServlet.class);
//
//	private UserAccessor userAccessor;
//
//	private SpaceManager spaceManager;
//
//	private BandanaManager bandanaManager;
//
//	public NotifyInviteListenerServlet(
//			SpaceManager spaceManager, 
//			BandanaManager bandanaManager, 
//			UserAccessor userAccessor) {
//
//		this.spaceManager = spaceManager;
//		this.bandanaManager = bandanaManager;
//		this.userAccessor = userAccessor;
//	}
//
//	@Override
//	public void init(ServletConfig config) throws ServletException {
//		super.init(config);
//	}
//
//	@Override
//	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//
//
//		String username = request.getParameter("username");
//		String email = request.getParameter("email");
//		
//		ConfluenceUser currentUser = AuthenticatedUserThreadLocal.get();
//
//		try {
//			AuthenticatedUserThreadLocal.set(userAccessor.getUserByName(Global.APP_ADMIN));
//
//			for (String spaceKey : spaceManager.getAllSpaceKeys(SpaceStatus.CURRENT)) {
//
//				Object invitesObject = bandanaManager.getValue(new ConfluenceBandanaContext(spaceKey), Invite.BANDANA_KEY);
//
//				if (invitesObject == null || !(invitesObject instanceof List)) {
//					continue;
//				}
//
//				List<Invite> invites = (List<Invite>) invitesObject;
//
//				List<Invite> invitesToBeRemoved = new ArrayList<Invite>(); 
//				
//				for (Invite invite : invites) {
//					
//					if (invite.getInviteeEmail().equalsIgnoreCase(email)) {
//
//						String groupName = null;
//						if (invite.isRoleApprover()) {
//							groupName = Global.getApproverGroupName(spaceKey);
//						} else if (invite.isRoleAuthor()) {
//							groupName = Global.getAuthorGroupName(spaceKey);
//						} else if (invite.isRoleReviewer()) {
//							groupName = Global.getReviewerGroupName(spaceKey);
//						}
//						
//						userAccessor.addMembership(groupName, username);
//
//						invitesToBeRemoved.add(invite);
//					}
//				}
//
//				 if (!invitesToBeRemoved.isEmpty()) {
//					for (Invite invite: invitesToBeRemoved) {
//						invites.remove(invite);
//					}
//					bandanaManager.setValue(new ConfluenceBandanaContext(spaceKey), Invite.BANDANA_KEY, invites);
//				}
//
//			}
//		} finally {
//			AuthenticatedUserThreadLocal.set(currentUser);
//		}
//
//	}
//
//	@Override
//	public void destroy() {
//	}
//
//}
