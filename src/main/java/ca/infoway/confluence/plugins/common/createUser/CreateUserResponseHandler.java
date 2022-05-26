package ca.infoway.confluence.plugins.common.createUser;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseHandler;

public class CreateUserResponseHandler implements ResponseHandler<Response> {
	
	private static final Logger log = LoggerFactory.getLogger(CreateUserResponseHandler.class);

	private String ldapUsername;
	private String ldapFullname;
	private String ldapEmail;
	private boolean isSuccess = false;
	private boolean isSsoExpired = false;
	
	@Override
	public void handle(Response response) throws ResponseException {

		if (validateResponse(response)) {
			List<String> multilines = Arrays.asList(response.getResponseBodyAsString().split("\\r?\\n"));

			for (int i = 0; i < multilines.size(); i++) {
				String line = multilines.get(i);
				
				if (line.equals("userdetails.attribute.name=uid")) {
					ldapUsername = getNextlineValue(multilines, i);
				} else if (line.equals("userdetails.attribute.name=mail")) {
					ldapEmail = getNextlineValue(multilines, i);
				} else if (line.equals("userdetails.attribute.name=cn")) {
					ldapFullname = getNextlineValue(multilines, i);
				}
			}
			
			if (ldapUsername != null && ldapEmail != null) {
				isSuccess = true;
			} else {
				log.trace("Unable to find uid and mail");
			}
		}
	}

	private boolean validateResponse(Response response) throws ResponseException {
		String body = response.getResponseBodyAsString();
 		if (!response.isSuccessful() && response.getStatusCode() == 401 && body.contains("com.sun.identity.idsvcs.TokenExpired")) {
			isSsoExpired = true;
			log.trace("SSO token expired.");
		} else if (!response.isSuccessful()) {
			log.warn("response is unsuccessful. HTTP status=" + response.getStatusCode());
		} else if (!body.contains("userdetails.attribute.value")) {
			log.info(" response=" + response.getResponseBodyAsString());
			log.trace("invalid response. Check response message to debug.");
		} else {
			//no error then return true
			return true;
		}
		//has error then return false
		return false;
	}

	private String getNextlineValue(List<String> multilines, int i) {
		String nextline = (i + 1) < multilines.size() ? multilines.get(i + 1) : "";
		nextline = nextline.replaceAll("userdetails.attribute.value=", "");
		return nextline;
	}
	
	public String getLdapUsername() {
		return ldapUsername;
	}


	public String getLdapFullname() {
		return ldapFullname;
	}

	
	public String getLdapEmail() {
		return ldapEmail;
	}
	
	public boolean isSucccess() {
		return isSuccess;
	}

	public boolean isSsoExpired() {
		return isSsoExpired;
	}

	
}
