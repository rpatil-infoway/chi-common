package ca.infoway.confluence.plugins.common.template;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.Renderer;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.spaces.SpaceManager;

public class HomepageMacro implements Macro {

	private static final Logger log = LoggerFactory.getLogger(HomepageMacro.class);
	private Renderer viewRenderer;
	private SpaceManager spaceManager;
	
	public HomepageMacro(Renderer viewRenderer, SpaceManager spaceManager) {
		this.viewRenderer = viewRenderer;
		this.spaceManager = spaceManager;
	}
	
	static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}

	@Override
	public String execute(Map<String, String> parameters, String body,
			ConversionContext context) throws MacroExecutionException {

		InputStream in = com.atlassian.core.util.ClassLoaderUtils.getResourceAsStream("/templates/homepage-macro-template.xml", this.getClass());
		
		String source = convertStreamToString(in);
		
		source = source.replace("***SPACE_NAME***", spaceManager.getSpace(context.getSpaceKey()).getName()); 
		
		String renderedTemplate = viewRenderer.render(source, context);
		
		
//log.trace("renderedTemplate: \n" + renderedTemplate);
		
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
