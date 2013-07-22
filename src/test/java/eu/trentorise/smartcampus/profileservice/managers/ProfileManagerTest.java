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
package eu.trentorise.smartcampus.profileservice.managers;

import it.unitn.disi.sweb.webapi.client.WebApiException;
import it.unitn.disi.sweb.webapi.model.smartcampus.social.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.trentorise.smartcampus.exceptions.AlreadyExistException;
import eu.trentorise.smartcampus.exceptions.SmartCampusException;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.profileservice.model.ExtendedProfile;
import eu.trentorise.smartcampus.profileservice.storage.ProfileStorage;
import eu.trentorise.smartcampus.test.SocialEngineOperation;

/**
 * Test Class
 * 
 * @author mirko perillo
 * 
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/applicationContext.xml")
public class ProfileManagerTest {

	@Autowired
	ProfileManager profileManager;

	@Autowired
	ProfileStorage storage;

	@Autowired
	SocialEngineOperation socialOperation;

	@Test
	public void crudExtendedProfile() throws AlreadyExistException, SmartCampusException, WebApiException,
			NotFoundException, ProfileServiceException {
		User socialUser = socialOperation.createUser();

		eu.trentorise.smartcampus.resourceprovider.model.User user = new eu.trentorise.smartcampus.resourceprovider.model.User();
		user.setId("1");
		user.setSocialId(socialUser.getId().toString());
		ExtendedProfile p = new ExtendedProfile();
		p.setAppId("appId");
		p.setProfileId("profileId");
		p.setUserId("1");
		Map<String, Object> content = new HashMap<String, Object>();
		content.put("receiveUpdates", true);
		p.setContent(content);
		p = profileManager.create(user, p);
		socialOperation.deleteUser(socialUser.getId());
		Assert.assertNotNull(p.getId());
		Assert.assertNotNull(p.getSocialId());
		Assert.assertTrue(profileManager.deleteExtendedProfile(p.getId()));
	}


	@Test
	public void extProfileByAttrs() throws AlreadyExistException, SmartCampusException, WebApiException,
			DataException, ProfileServiceException {

		// cleaning
		for (ExtendedProfile extP : storage.findExtendedProfiles("10", "appId")) {
			storage.deleteExtendedProfile(extP.getId());
		}

		for (ExtendedProfile extP : storage.findExtendedProfiles("15", "appId")) {
			storage.deleteExtendedProfile(extP.getId());
		}
		User socialUser = socialOperation.createUser();

		// user1
		eu.trentorise.smartcampus.resourceprovider.model.User u = new eu.trentorise.smartcampus.resourceprovider.model.User();
		u.setId("10");
		u.setSocialId(socialUser.getId().toString());

		// profile user 1
		ExtendedProfile profile = new ExtendedProfile();
		profile.setAppId("appId");
		profile.setUserId("10");
		profile.setProfileId("preferences");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("pref1", "value");
		profile.setContent(m);
		profileManager.create(u, profile);

		// user2
		u = new eu.trentorise.smartcampus.resourceprovider.model.User();
		u.setId("15");
		u.setSocialId(socialUser.getId().toString());

		// profile user2
		profile = new ExtendedProfile();
		profile.setAppId("appId");
		profile.setProfileId("preferences");
		profile.setUserId("15");
		m = new HashMap<String, Object>();
		m.put("pref1", "value");
		profile.setContent(m);
		profileManager.create(u, profile);

		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put("pref1", "value");
		List<ExtendedProfile> list = storage.findExtendedProfiles("appId", "preferences", filter);
		Assert.assertEquals(list.size(), 2);

		socialOperation.deleteUser(socialUser.getId());

	}
	
	@Test
	public void extProfileShare() throws AlreadyExistException, SmartCampusException, WebApiException,
			DataException, ProfileServiceException {

		User socialUser1 = socialOperation.createUser();
		User socialUser2 = socialOperation.createUser();

		try {
			// cleaning
			for (ExtendedProfile extP : storage.findExtendedProfiles("10", "appId")) {
				storage.deleteExtendedProfile(extP.getId());
			}

			// user1
			eu.trentorise.smartcampus.resourceprovider.model.User u = new eu.trentorise.smartcampus.resourceprovider.model.User();
			u.setId("10");
			u.setSocialId(socialUser1.getId().toString());

			// profile user 1
			ExtendedProfile profile = new ExtendedProfile();
			profile.setAppId("appId");
			profile.setUserId("10");
			profile.setProfileId("preferences");
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("pref1", "value");
			profile.setContent(m);
			ExtendedProfile ep = profileManager.create(u, profile);

			// user2
			u = new eu.trentorise.smartcampus.resourceprovider.model.User();
			u.setId("15");
			u.setSocialId(socialUser2.getId().toString());

			socialOperation.shareEntityWith(Long.parseLong(ep.getSocialId()), socialUser1.getId(), socialUser2.getId());

			List<Long> list = profileManager.getShared(socialUser2.getId().toString());
			Assert.assertEquals(list.size(), 1);

		} finally {
			socialOperation.deleteUser(socialUser1.getId());
			socialOperation.deleteUser(socialUser2.getId());
		}
		


	}

}
