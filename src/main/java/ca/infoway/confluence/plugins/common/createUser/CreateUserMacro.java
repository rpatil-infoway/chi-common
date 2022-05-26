package ca.infoway.confluence.plugins.common.createUser;

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

public class CreateUserMacro implements Macro {
	
	private Renderer renderer;
	private UserDetailsManager userDetailsManager;

	public CreateUserMacro(Renderer renderer, UserDetailsManager userDetailsManager) {
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
		String username = currentUser != null ? currentUser.getName() : "";
		velocityCtx.put("username", username);
	
		String renderedTemplate = VelocityUtils.getRenderedTemplate("/templates/create-user-template.vm", velocityCtx);
		
		return renderedTemplate;
	}

	@Override
	public BodyType getBodyType() {
		return BodyType.NONE;
	}

	@Override
	public OutputType getOutputType() {
		return OutputType.INLINE;
	}
}
