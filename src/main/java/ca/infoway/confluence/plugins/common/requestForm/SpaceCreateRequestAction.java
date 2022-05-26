package ca.infoway.confluence.plugins.common.requestForm;

import static com.atlassian.confluence.mail.template.ConfluenceMailQueueItem.MIME_TYPE_HTML;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.infoway.confluence.plugins.common.Global;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.mail.template.ConfluenceMailQueueItem;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.actions.PageAware;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.MailQueueItem;

public class SpaceCreateRequestAction extends ConfluenceActionSupport implements PageAware {
	
	private static final Logger log = LoggerFactory.getLogger(SpaceCreateRequestAction.class);
	
	private AbstractPage page;
	private String userName;
	private String userEmail;
	private String phoneNumber;
	private String companyName;
	private String clinReqType;
	private String specType;
	private String docDescription;
	private String questionsComments;
	private SettingsManager settingsManager;
	
	
	public SpaceCreateRequestAction() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public AbstractPage getPage() {
		return page;
	}

	@Override
	public void setPage(AbstractPage page) {
		this.page = page;

	}

	@Override
	public boolean isPageRequired() {
		return true;
	}

	@Override
	public boolean isLatestVersionRequired() {
		return true;
	}

	@Override
	public boolean isViewPermissionRequired() {
		return true;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getUserEmail() {
		return userEmail;
	}
	
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public String getCompanyName() {
		return companyName;
	}
	
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	
	public String getClinReqType() {
		return clinReqType;
	}
	
	public void setClinReqType(String clinReqType) {
		this.clinReqType = clinReqType;
	}
	
	public String getSpecType() {
		return specType;
	}
	
	public void setSpecType(String specType) {
		this.specType = specType;
	}
	
	public String getDocDescription() {
		return docDescription;
	}
	
	public void setDocDescription(String docDescription) {
		this.docDescription = docDescription;
	}
	
	public String getQuestionsComments() {
		return questionsComments;
	}
	
	public void setQuestionsComments(String questionsComments) {
		this.questionsComments = questionsComments;
	}
	
	public final SettingsManager getSettingsManager() {
		return settingsManager;
	}

	public final void setSettingsManager(SettingsManager settingsManager) {
		this.settingsManager = settingsManager;
	}
	
	public String execute() {
		//NOTE: clinReqType and specType
				//	if checked, returns "on"
				// otherwise, returns null
		log.trace("username: " + userName);
		log.trace("useremail: " + userEmail);
		log.trace("phoneNumber: " + phoneNumber);
		log.trace("companyName: " + companyName);
		log.trace("clinReqType: " + clinReqType);
		log.trace("specType: " + specType);
		log.trace("docDescription: " + docDescription);
		log.trace("questionsComments: " + questionsComments);
		if (!(isValid(userName) || isValid(userEmail) || isValid(phoneNumber)
				|| isValid(companyName) || isAtLeastOneDocTypeValid(clinReqType, specType)
				|| isValid(docDescription))) {
			return "error";
		}
		
		boolean clinReqTypeBool = docTypeToBoolean(clinReqType);
		boolean specTypeBool = docTypeToBoolean(specType);
		
		log.trace("clinReqTypeBool: " + clinReqTypeBool);
		log.trace("specTypeBool: " + specTypeBool);
		
		Map<String, Object> velocityCtx = MacroUtils.defaultVelocityContext();
		velocityCtx.put("userName", userName);
		velocityCtx.put("userEmail", userEmail);
		velocityCtx.put("phoneNumber", phoneNumber);
		velocityCtx.put("companyName", companyName);
		velocityCtx.put("clinReqTypeBool", clinReqTypeBool);
		velocityCtx.put("specTypeBool", specTypeBool);
		velocityCtx.put("docDescription", docDescription);
		if (questionsComments == null) {
			questionsComments = "";
		}
		velocityCtx.put("questionsComments", questionsComments);

		String renderedTemplate = VelocityUtils.getRenderedTemplate("/templates/space-create-request-email-template.vm", velocityCtx);
		
		String emailTo = "stse@infoway-inforoute.ca";
		String ccTo = "";
		
		if (Global.isInProduction()) {
			emailTo = "infocentral@infoway-inforoute.ca";
			ccTo = "stse@infoway-inforoute.ca, tachilles@infoway-inforoute.ca";
		}
		
		// change the recipient email to whatever email for testing -- however, for production this will need to be decided
		MailQueueItem mailQueueItem = new ConfluenceMailQueueItem(emailTo, ccTo, "New Space Request from - " + userName, renderedTemplate, MIME_TYPE_HTML);
		log.trace(renderedTemplate);
		try {
			mailQueueItem.send();
			log.trace("success");
			return "success";
		} catch (MailException e) {
			log.trace("error");
			e.printStackTrace();
			return "error";
		}
    }
	
	private boolean isValid(String s) {
		if (s == null || s.isEmpty()) {
			return false;
		}
		return true;
	}
	
	private boolean isAtLeastOneDocTypeValid(String s1, String s2) {
		return isValid(s1) || isValid(s2);
	}
	
	private boolean docTypeToBoolean(String docType) {
		if (isValid(docType)) {
			return true;
		} 
		return false;
	}

}
