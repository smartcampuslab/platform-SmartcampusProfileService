/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.profileservice.model;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import eu.trentorise.smartcampus.presentation.data.BasicObject;

@XmlRootElement(name = "ExtendedProfile")
public class ExtendedProfile extends BasicObject {

	private static final long serialVersionUID = -6213454306656243304L;

	private String appId;
	/**
	 * profile label
	 */
	private String profileId;

	private Map<String, Object> content;

	private String userId;
	/**
	 * profile social id
	 */
	private long socialId;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getProfileId() {
		return profileId;
	}

	public void setProfileId(String profileId) {
		this.profileId = profileId;
	}

	public Map<String, Object> getContent() {
		return content;
	}

	public void setContent(Map<String, Object> content) {
		this.content = content;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public long getSocialId() {
		return socialId;
	}

	public void setSocialId(long socialId) {
		this.socialId = socialId;
	}

}
