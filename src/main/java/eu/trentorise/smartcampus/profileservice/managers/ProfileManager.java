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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.ac.provider.model.Attribute;
import eu.trentorise.smartcampus.ac.provider.model.User;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.profileservice.converters.ProfileConverter;
import eu.trentorise.smartcampus.profileservice.model.BasicProfile;
import eu.trentorise.smartcampus.profileservice.storage.ProfileStorage;
import eu.trentorise.smartcampus.profileservice.storage.StoreProfile;

/**
 * <i>ProfileManager</i> manages the eu.trentorise.smartcampus.profileservice.model informations of all the user in the
 * system
 * 
 * @author mirko perillo
 * 
 */
@Component
public class ProfileManager {

	private static final Logger logger = Logger.getLogger(ProfileManager.class);

	// constants to retrieve name and surname attributest
	private static final String nameAttribute = "eu.trentorise.smartcampus.givenname";
	private static final String surnameAttribute = "eu.trentorise.smartcampus.surname";

	@Autowired
	private ProfileStorage storage;


	/**
	 * returns all minimal eu.trentorise.smartcampus.profileservice.model of users who match part of name
	 * 
	 * @param name
	 *            the string to match with name of user
	 * @return the list of minimal eu.trentorise.smartcampus.profileservice.model of users which name contains
	 *         parameter or an empty list
	 * @throws CommunityManagerException
	 */
	public List<BasicProfile> getUsers(String name)
			throws CommunityManagerException {
		try {
			return ProfileConverter.toBasicProfile(storage.findByName(name));
		} catch (Exception e) {
			throw new CommunityManagerException();
		}
	}

	/**
	 * returns all users eu.trentorise.smartcampus.profileservice.model in the system
	 * 
	 * @return the list of all minimal profiles of users
	 * @throws CommunityManagerException
	 */
	public List<BasicProfile> getUsers() throws CommunityManagerException {
		try {
			return ProfileConverter
					.toBasicProfile((List<StoreProfile>) storage
							.getObjectsByType(StoreProfile.class));
		} catch (Exception e) {
			logger.error("Exception getting system users");
			throw new CommunityManagerException();
		}
	}

	/**
	 * persist a eu.trentorise.smartcampus.profileservice.model
	 * 
	 * @param storeProfile
	 *            the eu.trentorise.smartcampus.profileservice.model to persist
	 * @return the stored eu.trentorise.smartcampus.profileservice.model
	 * @throws CommunityManagerException
	 */
	public StoreProfile create(StoreProfile storeProfile)
			throws CommunityManagerException {
		try {
			storeProfile.setFullname(createFullName(storeProfile));
			StoreProfile present = getStoreProfileByUserId(storeProfile
					.getUserId());
			if (present != null) {
				ProfileConverter.copyDifferences(storeProfile, present);
				storage.updateObject(present);
				storeProfile = present;
			} else {
				storage.storeObject(storeProfile);
				storeProfile = getStoreProfileByUserId(storeProfile.getUserId());
			}
			return storeProfile;
		} catch (Exception e) {
			throw new CommunityManagerException();
		}
	}

	/**
	 * Deletes a eu.trentorise.smartcampus.profileservice.model
	 * 
	 * @param storeProfile
	 *            eu.trentorise.smartcampus.profileservice.model to delete
	 * @return true if operation gone fine. false otherwise
	 * @throws CommunityManagerException
	 */
	public boolean delete(StoreProfile storeProfile)
			throws CommunityManagerException {
		try {
			storage.deleteObject(storeProfile);
			return true;
		} catch (DataException e) {
			throw new CommunityManagerException();
		}
	}

	/**
	 * updates a eu.trentorise.smartcampus.profileservice.model
	 * 
	 * @param storeProfile
	 *            eu.trentorise.smartcampus.profileservice.model to update
	 * @return true if operation gone fine, false otherwise
	 * @throws CommunityManagerException
	 */
	public boolean update(StoreProfile storeProfile)
			throws CommunityManagerException {
		try {
			storeProfile.setFullname(createFullName(storeProfile));
			storage.updateObject(storeProfile);
			return true;
		} catch (Exception e) {
			throw new CommunityManagerException();
		}
	}

	private String createFullName(StoreProfile storeProfile) {
		if (storeProfile != null) {
			return ((storeProfile.getName()==null?"":storeProfile.getName())+" "+(storeProfile.getSurname()==null?"":storeProfile.getSurname())).trim(); 
		}
		return "";
	}

	/**
	 * returns the MinimalProfile of a given user
	 * 
	 * @param userId
	 *            id of user
	 * @return MinimalProfile of user or null if it doesn't exist
	 * @throws CommunityManagerException
	 */
	public BasicProfile getBasicProfileById(String userId)
			throws CommunityManagerException {
		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put("userId", userId);
		BasicProfile profile = null;
		try {
			profile = ProfileConverter.toBasicProfile(storage.searchObjects(
					StoreProfile.class, filter).get(0));
		} catch (IndexOutOfBoundsException e) {
			return null;
		} catch (Exception e) {
			throw new CommunityManagerException();
		}

		return profile;
	}	

	/**
	 * returns a StoreProfile given user id
	 * 
	 * @param userId
	 *            user id
	 * @return the StoreProfile or null if doesn't exist
	 * @throws CommunityManagerException
	 */
	public StoreProfile getStoreProfileByUserId(String userId)
			throws CommunityManagerException {
		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put("userId", userId);
		try {
			return storage.searchObjects(StoreProfile.class, filter).get(0);
		} catch (IndexOutOfBoundsException e) {
			return null;
		} catch (Exception e) {
			throw new CommunityManagerException();
		}

	}

	private String retrieveAttribute(User user, String attributeKey) {
		String attributeValue = null;
		for (Attribute attr : user.getAttributes()) {
			if (attr.getKey().equals(attributeKey)) {
				attributeValue = attr.getValue();
			}
		}
		return attributeValue;
	}
	
	public BasicProfile getProfileByUserId(String userId)
			throws CommunityManagerException {
		BasicProfile model = null;
		try {
			model = ProfileConverter.toBasicProfile(getStoreProfileByUserId(userId));
		} catch (NullPointerException e) {
			return null;
		} catch (Exception e) {
			throw new CommunityManagerException();
		}

		return model;
	}	
	
	public BasicProfile getOrCreateProfile(User user)
			throws CommunityManagerException {
		String userId = Long.toString(user.getId());
		BasicProfile model = getProfileByUserId(userId);
		// if  model is null, system creates one using name and surname
		// from authentication process (if these fields are populated)
		if (model == null) {
			String nameValue = retrieveAttribute(user, nameAttribute);
			String surnameValue = retrieveAttribute(user, surnameAttribute);
			if (nameValue != null && surnameValue != null) {
				StoreProfile storeProfile = new StoreProfile();
				storeProfile.setName(nameValue);
				storeProfile.setSurname(surnameValue);
				storeProfile.setFullname(createFullName(storeProfile));
				storeProfile.setSocialId(user.getSocialId());
				storeProfile.setUserId(userId);
				storeProfile.setUser(userId);
				storeProfile.setUpdateTime(System.currentTimeMillis());
				create(storeProfile);
				model = getProfileByUserId(userId);
			}

		}
		return model;
	}	
	
}
