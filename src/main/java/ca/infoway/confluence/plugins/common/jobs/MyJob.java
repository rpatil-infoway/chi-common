package ca.infoway.confluence.plugins.common.jobs;

import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.Renderer;
import com.atlassian.confluence.security.login.HistoricalLoginInfo;
import com.atlassian.confluence.security.login.LoginManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.quartz.jobs.AbstractJob;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.user.User;
import com.atlassian.user.search.page.Pager;

public class MyJob extends AbstractJob {
	
	private static final Logger log = LoggerFactory.getLogger(MyJob.class);
	private LoginManager loginManager;
	private UserAccessor userAccessor;
	
    public MyJob(LoginManager loginManager, UserAccessor userAccessor) {
    	this.loginManager = loginManager;
    	this.userAccessor = userAccessor;
    }

	@Override
	public void doExecute(JobExecutionContext jobExecutionContext)
			throws JobExecutionException {
		log.trace("\nHello world Job\n");
		Pager<User> userList = userAccessor.getUsers();
		for (User user : userList) {
			Date lastLogin = loginManager.getLoginInfo(user).getLastSuccessfulLoginDate();
			if (lastLogin != null)
				log.trace(user.getName() + ": " + lastLogin.toLocaleString());
			else
				System.out.println(user.getName() + ": null");
		}
	}
}
