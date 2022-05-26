package ca.infoway.confluence.plugins.common.jobs;

import static com.atlassian.confluence.mail.template.ConfluenceMailQueueItem.MIME_TYPE_HTML;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.mail.template.ConfluenceMailQueueItem;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.MailQueueItem;
import com.atlassian.quartz.jobs.AbstractJob;

public class CheckActiveUserCountJob extends AbstractJob {

	private static final Logger log = LoggerFactory.getLogger(CheckActiveUserCountJob.class);
	private UserAccessor userAccessor;
	
	public CheckActiveUserCountJob(UserAccessor userAccessor) {
		this.userAccessor = userAccessor;
	}

	@Override
	public void doExecute(JobExecutionContext jobExecutionContext)
			throws JobExecutionException {
		int userCount = userAccessor.countLicenseConsumingUsers();
		if (userCount > 200) {
			log.trace("user count > 200... sending email...");
			String emailTo = "stse@infoway-inforoute.ca"; 
			String renderedTemplate = "Current number of license-consuming users: " + String.valueOf(userCount);
			MailQueueItem mailQueueItem = new ConfluenceMailQueueItem(emailTo, "Number of license-consuming users nearing limit", renderedTemplate, MIME_TYPE_HTML);
			
			try {
				mailQueueItem.send();
			} catch (MailException e) {
				e.printStackTrace();
			}
		} else {
			log.trace("user count below limit, no email sent");
		}
	}

}
