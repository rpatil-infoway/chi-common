//package ca.infoway.confluence.plugins.common.listeners;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.DisposableBean;
//
//import ca.infoway.confluence.plugins.common.Global;
//
//import com.atlassian.bandana.BandanaManager;
//import com.atlassian.confluence.event.events.security.LoginEvent;
//import com.atlassian.confluence.spaces.SpaceManager;
//import com.atlassian.confluence.user.UserAccessor;
//import com.atlassian.event.api.EventListener;
//import com.atlassian.event.api.EventPublisher;
//import com.atlassian.sal.api.net.Request;
//import com.atlassian.sal.api.net.RequestFactory;
//import com.atlassian.sal.api.net.Response;
//import com.atlassian.sal.api.net.ResponseException;
//import com.atlassian.sal.api.net.ResponseHandler;
//
//public class LoginListener implements DisposableBean {
//	private static final Logger log = LoggerFactory.getLogger(LoginListener.class);
//
//	private EventPublisher eventPublisher;
//	private BandanaManager bandanaManager;
//	private SpaceManager spaceManager;
//	private UserAccessor userAccessor;
//	private RequestFactory<?> requestFactory;
//
//	public LoginListener(
//			EventPublisher eventPublisher, 
//			SpaceManager spaceManager, 
//			BandanaManager bandanaManager, 
//			UserAccessor userAccessor,
//			RequestFactory<?> requestFactory) {
//
//		this.spaceManager = spaceManager;
//		this.bandanaManager = bandanaManager;
//		this.userAccessor = userAccessor;
//		this.requestFactory = requestFactory;
//		
//		this.eventPublisher = eventPublisher;
//		this.eventPublisher.register(this);
//	}
//
//	@EventListener
//	public void loginEvent(LoginEvent event) {
//
//		final String debugIndex = "**11** ";
//		log.trace(debugIndex + "start");
//		
//		String url = Global.getConfluenceBaseUrl() + "/plugins/servlet/infoway/notify-invite-listener?username=" + event.getUsername();
//
//		Request request = requestFactory.createRequest(Request.MethodType.GET, url);
//
//		try {
//			request.execute(new ResponseHandler() {
//				
//				@Override
//				public void handle(Response response) throws ResponseException {
//					log.trace("Sent message to notify-invite-listener with status code=" + response.getStatusCode());
//				}
//			});
//		} catch (ResponseException e) {
//			log.error("Error in sending message to notify-invite-listener", e);
//		}
//		
//		log.trace(debugIndex + "done");
//
//	}
//
//	@Override
//	public void destroy() throws Exception {
//		this.eventPublisher.unregister(this);
//	}
//}
