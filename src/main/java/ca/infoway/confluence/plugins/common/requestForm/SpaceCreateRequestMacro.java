package ca.infoway.confluence.plugins.common.requestForm;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.Renderer;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserDetailsManager;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.user.User;

public class SpaceCreateRequestMacro implements Macro {
	
	private Renderer renderer;
	private UserDetailsManager userDetailsManager;

	public SpaceCreateRequestMacro(Renderer renderer, UserDetailsManager userDetailsManager) {
		this.renderer = renderer;
		this.userDetailsManager = userDetailsManager;
	}

	@Override
	public String execute(Map<String, String> parameters, String body,
			ConversionContext context) throws MacroExecutionException {
		Map<String, Object> velocityCtx = MacroUtils.defaultVelocityContext();
		String pageId = context.getEntity().getIdAsString();
		velocityCtx.put("pageId", pageId);
		ConfluenceUser currentUser = AuthenticatedUserThreadLocal.get();
		String userFullName = currentUser.getFullName();
		String userEmail = currentUser.getEmail();
		String userPhoneNumber = userDetailsManager.getStringProperty(currentUser, "phone") != null ? userDetailsManager.getStringProperty(currentUser, "phone") : "";
		velocityCtx.put("userFullName", userFullName);
		velocityCtx.put("userEmail", userEmail);
		velocityCtx.put("userPhoneNumber", userPhoneNumber);

		String renderedTemplate = VelocityUtils.getRenderedTemplate("/templates/space-create-request-template.vm", velocityCtx);
		return renderedTemplate;
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
