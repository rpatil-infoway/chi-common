package ca.infoway.confluence.plugins.common.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import ca.infoway.confluence.plugins.common.spaceInfo.SpaceInfo;
import ca.infoway.confluence.plugins.common.spaceInfo.SpaceInfoImpl;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.labels.Label;
import com.atlassian.confluence.labels.LabelManager;
import com.atlassian.confluence.labels.Labelable;
import com.atlassian.confluence.labels.SpaceLabelManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.plugins.createcontent.api.events.SpaceBlueprintHomePageCreateEvent;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.confluence.spaces.Space;
//import com.atlassian.confluence.spaces.SpaceDescription;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;

public class SpaceCreatedEventListener implements DisposableBean {
	
	private static final Logger log = LoggerFactory
			.getLogger(SpaceCreatedEventListener.class);
	
	private PageManager pageManager;
	private SpaceLabelManager spaceLabelManager;
	private LabelManager labelManager;
	private BandanaManager bandanaManager; 
	
	private final I18NBeanFactory i18NBeanFactory;

	protected EventPublisher eventPublisher;

	public SpaceCreatedEventListener(EventPublisher eventPublisher, PageManager pageManager, 
			LabelManager labelManager, SpaceLabelManager spaceLabelManager, 
			I18NBeanFactory i18NBeanFactory, BandanaManager bandanaManager) {
		this.eventPublisher = eventPublisher;
        this.pageManager = pageManager;
        this.spaceLabelManager = spaceLabelManager;
        this.i18NBeanFactory = i18NBeanFactory;
        this.bandanaManager = bandanaManager;
        this.labelManager = labelManager;
        eventPublisher.register(this);
	}
	
	@EventListener
	public void homePageCreatedEvent(SpaceBlueprintHomePageCreateEvent event) {
	
		String spaceBlueprintNameKey = event.getSpaceBlueprint().getI18nNameKey();
		Space currentSpace = event.getSpace();
		Page homepage = currentSpace.getHomePage();
		
		log.trace("spaceBlueprintNameKey: " + spaceBlueprintNameKey);
		
		List<String> jurisdictions = (ArrayList<String>) event.getContext().get("jurisdictions");
   	 	List<String> standards = (ArrayList<String>) event.getContext().get("standards");
   	 	List<String> organizations = (ArrayList<String>) event.getContext().get("organizations");
   	 	List<String> additionalMetadata = (ArrayList<String>) event.getContext().get("metadata");
   	 	String standardsVersion = (String) event.getContext().get("standardsVersion");
   	 	String domain = (String) event.getContext().get("domain");
   	 	String clinicalArea = (String) event.getContext().get("clinicalArea");
   	 	
   	 	
   	 	log.info("creating spaceinfo....");
   	 	SpaceInfo spaceInfo = new SpaceInfoImpl();
   	 	log.info("...spaceinfo created");
   	 	log.info("setting jurisdictions");
	    spaceInfo.setJurisdictions(jurisdictions);
	    log.info("setting domain");
	    spaceInfo.setDomain(domain);
	    log.info("setting clinical area");
	    spaceInfo.setClinicalArea(clinicalArea);
	    log.info("setting standards");
		spaceInfo.setStandards(standards); 
	    log.info("setting standardsversion");
	    spaceInfo.setStandardsVersion(standardsVersion);
	    log.info("settings organizations");
	    spaceInfo.setOrganizations(organizations);
	    log.info("setting metadata");
	    spaceInfo.setMetadata(additionalMetadata);
	    bandanaManager.setValue(new ConfluenceBandanaContext(event.getSpace()), "ca.infoway.confluence.blueprint.info", spaceInfo);
	     
	    log.info("added space info to bandana");
	    log.info("space=" + spaceInfo.toString()); 
//   	log.debug(homepage.getNameForComparison());
	
	    String blueprintId = "";
	    String blueprintVersion = "";
	    
		// ~BEGIN spec case
	    log.trace("spaceBlueprintNameKey: " + spaceBlueprintNameKey);
		if (spaceBlueprintNameKey.equalsIgnoreCase("confluence.samplespec.space.blueprint.name")) {

		    log.info("Setting blueprint id and version for spec");
		    blueprintId = i18NBeanFactory.getI18NBean().getText("confluence.samplespec.space.blueprint.id");
		    blueprintVersion = i18NBeanFactory.getI18NBean().getText("confluence.samplespec.space.blueprint.version");
		     
			// Add categories to space
			log.info("Adding categories: specification, private-space");
			spaceLabelManager.addLabel(currentSpace, "specification");
			spaceLabelManager.addLabel(currentSpace, "private-space");

			log.info("Adding labels to Business RUle (BR) and Guidance (GU) pages");
			// could make this into a function...
			List<Long> childIds = new ArrayList<Long>();
			for (Page childPage : homepage.getDescendants()) {
				childIds.add(childPage.getId());
				// Add labels to Business RUle (BR) and Guidance (GU) pages
				if (childPage.getTitle().startsWith("UC-")) {
					labelManager.addLabel((Labelable) childPage, new Label("use-case"));
				} else if (childPage.getTitle().startsWith("BR-")) {
					labelManager.addLabel((Labelable) childPage, new Label("business-rule"));
				} else if (childPage.getTitle().startsWith("GU-")) {
					labelManager.addLabel((Labelable) childPage, new Label("guidance"));
				}
			}
			 
		// ~END spec case
		// ~BEGIN clin req case
		} else if (spaceBlueprintNameKey.equalsIgnoreCase("confluence.clinicalreq.space.blueprint.name")) {
			
			log.info("Setting blueprint id and version for clin req");
		    blueprintId = i18NBeanFactory.getI18NBean().getText("confluence.clinicalreq.space.blueprint.id");
		    blueprintVersion = i18NBeanFactory.getI18NBean().getText("confluence.clinicalreq.space.blueprint.version");
			
			// Add categories to space
		    log.info("Adding categories: clinical-requirements, work-space");
		    spaceLabelManager.addLabel(currentSpace, "clinical-requirements");
		    spaceLabelManager.addLabel(currentSpace, "private-space");
		    
		    log.info("Adding selected jurisdictions");
		    for (String jurisdictionKey : jurisdictions) {
		    	if (!jurisdictionKey.isEmpty()) {
			    	 spaceLabelManager.addLabel(currentSpace, jurisdictionKey.toLowerCase());	 
			    }
		    }  		    
		  //~END clin req case  
		}else if (spaceBlueprintNameKey.equalsIgnoreCase("confluence.empty.space.blueprint.name")) {
		    blueprintId = i18NBeanFactory.getI18NBean().getText("confluence.empty.space.blueprint.id");
		    blueprintVersion = i18NBeanFactory.getI18NBean().getText("confluence.empty.space.blueprint.version");
			
			// Add categories to space
		    log.info("Adding categories: private-space");
		    spaceLabelManager.addLabel(currentSpace, "private-space");
		}else if (spaceBlueprintNameKey.equalsIgnoreCase("confluence.empty.with.versioning.space.blueprint.name")) {
		    blueprintId = i18NBeanFactory.getI18NBean().getText("confluence.empty.with.versioning.space.blueprint.id");
		    blueprintVersion = i18NBeanFactory.getI18NBean().getText("confluence.empty.with.versioning.space.blueprint.version");
			
			// Add categories to space
		    log.info("Adding categories: private-space");
		    spaceLabelManager.addLabel(currentSpace, "private-space");
		}
		
		setProperPageOrder(homepage);
//    	List<Long> childIds = new ArrayList<Long>();
//    	for (Page childPage : homepage.getChildren()) {
//    		childIds.add(childPage.getId());
//    	}
//    	// Pages are created in the order in which they are appear in
//		// atlassian-plugin.xml - so just sort the ids!
//		log.info("Sorting pages");
//		Collections.sort(childIds);
//		pageManager.setChildPageOrder(homepage, childIds);
//		     
		
		log.info("Setting blueprint id and version in bandana");
		if (StringUtils.isNotEmpty(blueprintId)) {
			bandanaManager.setValue(new ConfluenceBandanaContext(event.getSpace()), "ca.infoway.confluence.blueprint.id", blueprintId);
		}
		if (StringUtils.isNotEmpty(blueprintVersion)) {
			bandanaManager.setValue(new ConfluenceBandanaContext(event.getSpace()), "ca.infoway.confluence.blueprint.version", blueprintVersion);
		}

		// set new description
//		String newDescription = spaceInfo.createDescription();
//		log.trace("newDescription: \n\n" + newDescription);
//		SpaceDescription spaceDescription = currentSpace.getDescription();
//		spaceDescription.setBodyAsString(newDescription);
//		currentSpace.setDescription(spaceDescription);
//		log.info("spaceDescription:\n ");
//		log.info(spaceDescription.getBodyAsString());
		
	}
	
	@Override
	public void destroy() throws Exception {
		eventPublisher.unregister(this);

	}

	private void setProperPageOrder(Page parent) {
		if (parent.hasChildren()) {
			List<Long> childIds = new ArrayList<Long>();
	    	for (Page childPage : parent.getChildren()) {
	    		childIds.add(childPage.getId());
	    		setProperPageOrder(childPage);
	    	}
	    	// Pages are created in the order in which they are appear in
			// atlassian-plugin.xml - so just sort the ids!
	    	if (childIds.size() > 1) {
	    		log.trace("Sorting pages");
	    		Collections.sort(childIds);
	    		pageManager.setChildPageOrder(parent, childIds);
	    	}
		}
	}

}
