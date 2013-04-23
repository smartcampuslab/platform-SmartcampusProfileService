package eu.trentorise.smartcampus.profileservice.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ExtendedProfiles")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExtendedProfiles {

	@XmlElement(name = "ExtendedProfile")
	private List<ExtendedProfile> profiles;

	public List<ExtendedProfile> getProfiles() {
		return profiles;
	}

	public void setProfiles(List<ExtendedProfile> profiles) {
		this.profiles = profiles;
	}
	
	
	
}
