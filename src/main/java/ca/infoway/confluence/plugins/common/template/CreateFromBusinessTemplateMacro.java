package ca.infoway.confluence.plugins.common.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.infoway.confluence.plugins.common.Global;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.pages.templates.PageTemplate;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.content.render.xhtml.Renderer;
import com.atlassian.json.jsonorg.JSONArray;
import com.atlassian.json.jsonorg.JSONObject;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseHandler;

public class CreateFromBusinessTemplateMacro implements Macro {

	private static final Logger log = LoggerFactory
			.getLogger(CreateFromBusinessTemplateMacro.class);
	
	private RequestFactory<?> requestFactory;
	private Renderer renderer;
	private SpaceManager spaceManager;
	private SettingsManager settingsManager;
	
	private ArrayList<String> pageTitles;
	
	private static Map<String, String> codeToTemplateMap;
	private static Map<String, String> codeToLabelMap;
	
	static
    {
		codeToTemplateMap = new HashMap<String, String>();
		codeToTemplateMap.put("BR", "ca.infoway.confluence.plugins.chi-templates:chi-business-rule-template-bp");
		codeToTemplateMap.put("GU", "ca.infoway.confluence.plugins.chi-templates:chi-guidance-template-bp");
		codeToTemplateMap.put("UC", "ca.infoway.confluence.plugins.chi-templates:chi-use-case-template-bp");
		
		codeToLabelMap = new HashMap<String, String>();
		codeToLabelMap.put("BR", "Add a Business Rule");
		codeToLabelMap.put("GU", "Add Guidance");
		codeToLabelMap.put("UC", "Add a Use Case");
		
    }
	
	private static String MACRO_TEMPLATE = "<ac:structured-macro ac:name=\"create-from-template\">"
			+ "<ac:parameter ac:name=\"blueprintModuleCompleteKey\">TEMPLATEKEY_PLACEHOLDER</ac:parameter>"
			+ "<ac:parameter ac:name=\"title\">TITLE_PLACEHOLDER</ac:parameter>"
			+"<ac:parameter ac:name=\"buttonLabel\">BUTTONLABEL_PLACEHOLDER</ac:parameter>"
			+ "</ac:structured-macro>";
	
	private static String MACRO_TEMPLATE_WITH_TEMPLATEID = "<ac:structured-macro ac:name=\"create-from-template\">"
			+ "<ac:parameter ac:name=\"templateId\">TEMPLATEKEY_PLACEHOLDER</ac:parameter>"
			+ "<ac:parameter ac:name=\"title\">TITLE_PLACEHOLDER</ac:parameter>"
			+"<ac:parameter ac:name=\"buttonLabel\">BUTTONLABEL_PLACEHOLDER</ac:parameter>"
			+ "</ac:structured-macro>";

	private static String SV_PAGE_TREE_HTML = "<ac:structured-macro ac:name=\"sv-pagetree\">"
        + "<ac:parameter ac:name=\"hideUnavailable\">true</ac:parameter>"
        + "<ac:parameter ac:name=\"startDepth\">100</ac:parameter>"
        + "</ac:structured-macro>";
	
	public CreateFromBusinessTemplateMacro(
			PageManager pageManager, 
			SettingsManager settingsManager,
			SpaceManager spaceManager,
			RequestFactory<?> requestFactory, 
			Renderer renderer) {
		
		this.renderer = renderer;
		this.requestFactory = requestFactory;
		this.spaceManager = spaceManager;
		
		pageTitles = new ArrayList<String>();
	}

	@Override
	public String execute(Map<String, String> params, String body,
			ConversionContext ctx) throws MacroExecutionException {
		
		String currentSpaceKey = ctx.getSpaceKey();
		Space currentSpace = spaceManager.getSpace(currentSpaceKey);
		
		String renderContent = renderer.render(SV_PAGE_TREE_HTML, ctx);
		log.trace("renderContent=" + renderContent);
		
		if (renderContent == null || renderContent.length() == 0) {
			return null;
		}
		
		String paramParentPageId = getRegularExpressionValue(renderContent,"data-root-page-id=\"(.*)\"");
		log.trace("paramParentPageId=" + paramParentPageId);
		
		String paramSpaceKey = getRegularExpressionValue(renderContent,"data-space-key=\"(.*)\"");
		log.trace("paramSpaceKey=" + paramSpaceKey);
		
		String paramVersionId = getRegularExpressionValue(renderContent,"data-version-id=\"(.*)\"");
		log.trace("paramVersionId=" + paramVersionId);
		
		ArrayList<String> svPageTitles = retrievePageTitles(paramSpaceKey, paramVersionId, paramParentPageId);
		String pageType = params.get("pageType");
		
//		for (Object ptAsObject: currentSpace.getPageTemplates()) {
//			if (ptAsObject instanceof PageTemplate) {
//				PageTemplate pt = (PageTemplate) ptAsObject;
//				log.error("SAM: pageTemplate: " +pt.getName());
//			}
//		}

		String templateID = params.get("templateID");

		Integer maxNumber = 0;
		
		for(String pageTitle : pageTitles) {
			if (pageTitle.toLowerCase().startsWith(pageType.toLowerCase() + "-")) {
				String pageSuffix = pageTitle.substring(3);
				
				if (pageSuffix.length() > 0 && pageSuffix.matches("^\\d+$")) {
					Integer pageItemNumber = Integer.parseInt(pageSuffix);
					if (pageItemNumber > maxNumber) {
						maxNumber = pageItemNumber;
					}
				}
			}
		}
		
		String newPageTemplateKey = codeToTemplateMap.get(pageType);
		String newPageButtonLabel = codeToLabelMap.get(pageType);
		String newPageTitle = pageType + "-" + String.format("%02d", maxNumber + 1);
		
		String newPageMacroTemplateRaw;
		
		if (templateID != null) {
			newPageMacroTemplateRaw = MACRO_TEMPLATE_WITH_TEMPLATEID
				.replace("TEMPLATEKEY_PLACEHOLDER", templateID);
		} else {
			newPageMacroTemplateRaw = MACRO_TEMPLATE
					.replace("TEMPLATEKEY_PLACEHOLDER", newPageTemplateKey);
		}
		
		newPageMacroTemplateRaw = newPageMacroTemplateRaw
			.replace("TITLE_PLACEHOLDER", newPageTitle)
			.replace("BUTTONLABEL_PLACEHOLDER", newPageButtonLabel);
		
		//log.trace(newPageMacroTemplateRaw);
		String newPageMacroTemplateRendered = renderer.render(newPageMacroTemplateRaw, ctx);
		//log.trace(newPageMacroTemplateRendered);
		return newPageMacroTemplateRendered;
	}
	
	private ArrayList<String> retrievePageTitles(String spaceKey, String versionId, String parentPageId) {
		
		pageTitles.clear();

		String url = Global.getConfluenceBaseUrl() + "/rest/scroll-versions/latest/pagetree/" + spaceKey 
				+ "?parentPageId=" + parentPageId
				+ "&expandedPageId=" + parentPageId
				+ "&versionId=" + versionId
				+ "&isShowUnavailablePages=false"
				+ "&startDepth=100"
				+ "&isShowToplevelPages=false";
		
//log.warn("URL: " + url);
		
		Request request = requestFactory.createRequest(Request.MethodType.GET, url);
		String baseUrl = settingsManager.getGlobalSettings().getBaseUrl();
		request.addBasicAuthentication(baseUrl,Global.APP_ADMIN, Global.APP_ADMIN_PWD); //confluence7.4 changes
		request.setHeader("Content-Type", "application/json");

		try {
			request.execute(new ResponseHandler() {
				@Override
				public void handle(Response response) throws ResponseException {
//log.warn(" statusCode=" + response.getStatusCode());
//log.warn(" response=" + response.getResponseBodyAsString());
					 
					 JSONObject json = new JSONObject(response.getResponseBodyAsString());
					 
					 log.trace("children=" + json.getJSONArray("children").length());
					 
					 parseChildren(json);
				}
				
				private void parseChildren(JSONObject json) {
					if (!json.isNull("children") && json.getJSONArray("children").length() > 0) {
						JSONArray children = json.getJSONArray("children");
						for (JSONObject child : children.objects()) {
							String pageTitle = child.getString("scrollPageTitle");
							log.trace("pageTitle=" +  pageTitle);
							pageTitles.add(pageTitle);
							
							//parse children of this child (recursive loop)
							parseChildren(child);
						} 
					}
				}
			});
			
		} catch (ResponseException e) {
			log.error("Error in restful message", e);
		}
		return pageTitles;
	}
	
	private String getRegularExpressionValue(String content, String expression) {
		Matcher m = Pattern.compile(expression).matcher(content);

		if (m.find()) {
			// log.trace("m=" + m);
			return m.group(1);
		}
		return null;
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
