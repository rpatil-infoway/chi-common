package ca.infoway.confluence.plugins.common.spaceInfo;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;

public class StoreWorkingVersionServlet extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7616613279116316665L;

	private static final Logger log = LoggerFactory.getLogger(StoreWorkingVersionServlet.class);

	private BandanaManager bandanaManager;
	
	public StoreWorkingVersionServlet(BandanaManager bandanaManager) {
		this.bandanaManager = bandanaManager;
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
        super.init(config);
        getServletContext().log("init() called");
        
    }

	@Override
	 protected void service(HttpServletRequest request, HttpServletResponse response) 
			 throws ServletException, IOException {
		String workingVersionId = request.getParameter("workingVersionId");
		String workingVersionName = request.getParameter("workingVersionName");
		String spaceKey = request.getParameter("spaceKey");
		log.trace("Setting working version info to space [" + spaceKey + "].\n");
		log.trace("workingVersionId: " + workingVersionId);
		log.trace("workingVersionName: " + workingVersionName);
		log.trace("spaceKey: " + spaceKey);
		ConfluenceBandanaContext spaceBandanaContext = new ConfluenceBandanaContext(spaceKey);
		if (bandanaManager.getValue(spaceBandanaContext, "ca.infoway.confluence.blueprint.workingversion." + workingVersionId) == null) {
			log.trace("Setting working version data for this space...");
			bandanaManager.setValue(spaceBandanaContext, "ca.infoway.confluence.blueprint.workingversion." + workingVersionId, workingVersionName);
		} else {
			log.trace("bandana values already stored.");
		}
		
	}
	
	@Override
    public void destroy() {
        getServletContext().log("destroy() called");
    }

}
