package ca.infoway.confluence.plugins.common.micros;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.spaces.SpaceManager;

public class PageSorterMacro implements Macro {
	
	private PageManager pageManager;
	
	private static final Logger log = LoggerFactory
			.getLogger(PageSorterMacro.class);

	public PageSorterMacro(PageManager pageManager, SpaceManager spaceManager) {
		this.pageManager = pageManager;
	}

	@Override
	public String execute(Map<String, String> parameters, String body,
			ConversionContext ctx) throws MacroExecutionException {
		log.trace(parameters.toString());
		String selectedOrder;
		if (parameters.get("order") == null) {
			selectedOrder = "ascending";
		} else {
			selectedOrder = parameters.get("order");
		}
		
		String currentSpaceKey = ctx.getSpaceKey();
		String currentPageTitle = ctx.getPageContext().getPageTitle();
		Page currentPage = pageManager.getPage(currentSpaceKey, currentPageTitle);
		List<Long> childIds = new ArrayList<Long>();
		
		log.trace(currentPage.getDescendants().toString());
		
		for (Page childPage : currentPage.getDescendants()) {
			childIds.add(childPage.getId());
		}
		Collections.sort(childIds);
		if (selectedOrder.equalsIgnoreCase("ascending")) {
			//pages already in ascending order
			log.trace("ascending");
			log.trace(childIds.toString());
			pageManager.setChildPageOrder(currentPage, childIds);
		} else if (selectedOrder.equalsIgnoreCase("descending")) {
			log.trace("descending");
			//pages in ascending order, so put them into an array and just reverse it, then set it as the new order		
			Collections.reverse(childIds);
			log.trace(childIds.toString());
			pageManager.setChildPageOrder(currentPage, childIds);
			
		} else {
			//default to ascending
			pageManager.setChildPageOrder(currentPage, childIds);
		}
		
		return "";
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
