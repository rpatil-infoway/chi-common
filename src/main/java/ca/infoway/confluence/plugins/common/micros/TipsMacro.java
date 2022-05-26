package ca.infoway.confluence.plugins.common.micros;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.confluence.util.velocity.VelocityUtils;

public class TipsMacro implements Macro {

	private static final Logger log = LoggerFactory.getLogger(TipsMacro.class);
	private final I18NBeanFactory i18NBeanFactory;
	
	public TipsMacro(I18NBeanFactory i18NBeanFactory) {
		this.i18NBeanFactory = i18NBeanFactory;
	}

	@Override
	public String execute(Map<String, String> parameters, String body,
			ConversionContext context) throws MacroExecutionException {
		String paramKey = parameters.get("key");
		Map<String, Object> ctx = MacroUtils.defaultVelocityContext();
		String selectedKey = paramKey.toLowerCase();
		log.trace(selectedKey);
		if (selectedKey.equalsIgnoreCase("specification status")) {
			String tooltipText = i18NBeanFactory.getI18NBean().getText("ca.infoway.confluence.plugins.chi-common.chi-tips.spec-status.text");
			ctx.put("tooltipText", tooltipText);
			ctx.put("tipId", "spec-status" + "-" + System.currentTimeMillis());
			ctx.put("iconStyle", "color: #14892c;");
		} else if (selectedKey.equalsIgnoreCase("authors") || selectedKey.equalsIgnoreCase("reviewers") || selectedKey.equalsIgnoreCase("approvers")) {
			String tooltipText = i18NBeanFactory.getI18NBean().getText("ca.infoway.confluence.plugins.chi-common.chi-tips.spaceinfo." + selectedKey + ".text");
			ctx.put("tooltipText", tooltipText);
			ctx.put("tipId", selectedKey + "-" + System.currentTimeMillis());
			ctx.put("iconStyle", "color: #14892c;");
		} else {
			return "something went wrong....";
		}
		
		String renderedTemplate = VelocityUtils.getRenderedTemplate("/templates/tips-template.vm", ctx);
		
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
