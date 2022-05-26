package ca.infoway.confluence.plugins.common.spaceInfo;

import java.util.List;

public interface SpaceInfo {
	
	public List<String> getJurisdictions();
	
	public String getDomain();
	
	public String getClinicalArea();
	
	public List<String> getStandards();
	
	public String getStandardsVersion();
	
	public List<String> getOrganizations();
	
	public List<String> getMetadata();

	public void setJurisdictions(List<String> jurisdictions);
	
	public void setDomain(String domain);
	
	public void setClinicalArea(String clinicalArea);
	
	public void setStandards(List<String> standards);
	
	public void setStandardsVersion(String standardVersion);
	
	public void setOrganizations(List<String> organizations);
	
	public void setMetadata(List<String> metadata);
	
	public String createDescription();
}
