package ca.infoway.confluence.plugins.common.micros;

import java.util.List;
import java.util.Map;

public class InactiveUserDetail {
	
	private String userName;
	private String userFullName;
	private String email;
	private String lastLogin;
	private List<Map<String, String>> userGroupDetails;
	private String groups;
	
	public InactiveUserDetail(String userName, String userFullName, String email, String lastLogin, 
			List<Map<String, String>> userGroupDetails, String groups) {
		this.userName = userName;
		this.userFullName = userFullName;
		this.email = email;
		this.lastLogin = lastLogin;
		this.userGroupDetails = userGroupDetails;
		this.setGroups(groups);
	}
	
	public String getUserName() {
		return userName;
	}
	
	public String getUserFullName() {
		return userFullName;
	}

	public String getEmail() {
		return email;
	}

	public String getLastLogin() {
		return lastLogin;
	}
	
	public List<Map<String, String>> getUserGroupDetails() {
		return userGroupDetails;
	}
	
	public void setUserName(String s) {
		this.userName = s;
	}
	
	public void setUserFullName(String s) {
		this.userFullName = s;
	}

	public void setEmail(String s) {
		this.email = s;
	}

	public void setLastLogin(String s) {
		this.lastLogin = s;
	}
	
	public void setUserGroupDetails(List<Map<String, String>> l) {
		this.userGroupDetails = l;
	}

	public String getGroups() {
		return groups;
	}

	public void setGroups(String groups) {
		this.groups = groups;
	}


}
