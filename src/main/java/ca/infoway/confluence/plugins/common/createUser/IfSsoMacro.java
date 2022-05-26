package ca.infoway.confluence.plugins.common.createUser;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.infoway.confluence.plugins.common.Global;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.Renderer;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserDetailsManager;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.user.User;
import com.opensymphony.webwork.ServletActionContext;

public class IfSsoMacro implements Macro {
	
	private static final Logger log = LoggerFactory.getLogger(IfSsoMacro.class);
	
	private static final String PARAM_SSO_ACTIVE = "ssoActive";
	private static final String PARAM_SSO_EXPIRED = "ssoExpired";
	private static final String PARAM_SSO_MISSING = "ssoMissing";
	
    private RequestFactory<?> requestFactory;
    private SettingsManager settingsManager;
	
    public IfSsoMacro(RequestFactory<?> requestFactory, SettingsManager settingsManager) {
		this.requestFactory = requestFactory;
		this.settingsManager = settingsManager;
	}

	@Override
	public String execute(Map<String, String> parameters, String body,
			ConversionContext context) throws MacroExecutionException {
		
		String paramCondition = parameters.get("condition");
		log.trace("paramCondition: " + paramCondition);
		
		if (paramCondition.equals(PARAM_SSO_MISSING) && Global.getSsoToken() == null) {
			return body;
		} else if (Global.getSsoToken() == null) {
			//return empty for all other conditions since SSO token is missing
			return "";
		}

		String openAmBaseUrl = Global.getOpenAmBaseUrl();
				
		Request request = requestFactory.createRequest(Request.MethodType.POST, openAmBaseUrl + "/openam/identity/attributes");

		request.setHeader("Content-Type", "application/x-www-form-urlencoded");
		request.setRequestBody("subjectid=" + Global.getSsoToken());

		CreateUserResponseHandler response = new CreateUserResponseHandler();

		try {
			// send message to openAM and obtain username, fullname, and email
			request.execute(response);
		} catch (ResponseException e) {
			log.error("Error in sending message to openAM", e);
			return "ERROR: Unable to communicate with openAM";
		}

		if (paramCondition.equals(PARAM_SSO_ACTIVE) && response.isSucccess()) {
			return body;
		} else if (paramCondition.equals(PARAM_SSO_EXPIRED) && response.isSsoExpired()) { 
			return body;
		} else {
			return "";
		}
	}
	
	@Override
	public BodyType getBodyType() {
		return BodyType.RICH_TEXT;
	}

	@Override
	public OutputType getOutputType() {
		return OutputType.INLINE;
	}
}
