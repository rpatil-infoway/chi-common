package ca.infoway.confluence.plugins.common.spaceInfo;

import java.util.List;

import org.apache.commons.lang.StringUtils;

public class SpaceInfoImpl implements SpaceInfo {

	private List<String> jurisdictions;
	private String domain;
	private String clinicalArea;
	private List<String> standards;
	private String standardsVersion;
	private List<String> organizations;
	private List<String> metadata;
	
	public SpaceInfoImpl() {
		super();
	}
	
	public final List<String> getJurisdictions() {
		return jurisdictions;
	}
	
	public final String getDomain() {
		return domain;
	}
	
	public final String getClinicalArea() {
		return clinicalArea;
	}
	
	public final List<String> getStandards() {
		return standards;
	}
	
	public final String getStandardsVersion() {
		return standardsVersion;
	}
	
	public final List<String> getOrganizations() {
		return organizations;
	}
	
	public final List<String> getMetadata() {
		return metadata;
	}

	public final void setJurisdictions(List<String> jurisdictions) {
		this.jurisdictions = jurisdictions;
	}
	
	public final void setDomain(String domain) {
		this.domain = domain;
	}
	
	public final void setClinicalArea(String clinicalArea) {
		this.clinicalArea = clinicalArea;
	}
	
	public final void setStandards(List<String> standards) {
		this.standards = standards;
	}
	
	public final void setStandardsVersion(String standardsVersion) {
		this.standardsVersion = standardsVersion;
	}
	
	public final void setOrganizations(List<String> organizations) {
		this.organizations = organizations;
	}
	
	public final void setMetadata(List<String> metadata) {
		this.metadata = metadata;
	}
	
	private String listToString(List<String> list) {
		if (list == null || list.size() == 0) {
			return "";
		}
		return StringUtils.join(list, ", ");
	}
	
	private String replaceEmptyOrNull(String str) {
		if (str.isEmpty() || str == null) {
			return "none";
		} else {
			return str;
		}
 	}

	public String createDescription() {
		String jurisdictionsString = listToString(jurisdictions);
		String standardsString = listToString(standards);
		String organizationsString = listToString(organizations);
		String metadataString = listToString(metadata);
		String newLineChar = System.getProperty("line.separator");
		
//		return "Test 123";
		
		return "Jurisdictions = " + replaceEmptyOrNull(jurisdictionsString) 	+ newLineChar
				+ "Domain = " + replaceEmptyOrNull(domain) 						+ newLineChar
				+ "Clinical Area = " + replaceEmptyOrNull(clinicalArea)  		+ newLineChar
				+ "Standards = " + replaceEmptyOrNull(standardsString) 			+ newLineChar
				+ "Standards Version = " + replaceEmptyOrNull(standardsVersion)	+ newLineChar
				+ "Organizations = " + replaceEmptyOrNull(organizationsString) 	+ newLineChar
				+ "Metadata = " + replaceEmptyOrNull(metadataString)			+ newLineChar
				;
	}
	
}
