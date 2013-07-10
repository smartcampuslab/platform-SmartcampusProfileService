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
import it.unitn.disi.sweb.webapi.model.entity.Entity;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.common.SemanticHelper;
import eu.trentorise.smartcampus.exceptions.AlreadyExistException;
import eu.trentorise.smartcampus.exceptions.SmartCampusException;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.profileservice.model.ExtendedProfile;
import eu.trentorise.smartcampus.profileservice.storage.ProfileStorage;
import eu.trentorise.smartcampus.resourceprovider.model.User;
import eu.trentorise.smartcampus.social.SocialEngineConnector;

/**
 * <i>ProfileManager</i> manages the
 * eu.trentorise.smartcampus.profileservice.model informations of all the user
 * in the system
 * 
 * @author mirko perillo
 * 
 */
@Component
public class ProfileManager extends SocialEngineConnector {

	private static final Logger logger = Logger.getLogger(ProfileManager.class);

	@Autowired
	private ProfileStorage storage;

	/**
	 * persists a custom profile for the given user
	 * 
	 * @param userId
	 *            user owner of extended profile
	 * @param extProfile
	 *            custom profile to persist
	 * 
	 * @return the stored custom profile
	 * @throws AlreadyExistException
	 * @throws SmartCampusException
	 */
	public ExtendedProfile create(User user, ExtendedProfile extProfile)
			throws ProfileServiceException, AlreadyExistException,
			SmartCampusException {
		// check if user and extProfile informations are consistent
		if (extProfile.getUserId() == null
				|| !user.getId().toString().equals(extProfile.getUserId())) {
			String msg = "Extended profile and user "+user.getId()+" mismatch";
			logger.error(msg);
			throw new SmartCampusException(msg);
		}
		ExtendedProfile present = storage.findExtendedProfile(
				extProfile.getUserId(), extProfile.getAppId(),
				extProfile.getProfileId());
		if (present != null) {
			String msg = String
					.format("extended profile exists userId:%s, appId:%s, profileId:%s",
							extProfile.getUserId(), extProfile.getAppId(),
							extProfile.getProfileId());
			logger.error(msg);
			throw new AlreadyExistException(msg);
		}

		try {
			Entity entity = SemanticHelper.createEntity(
					socialEngineClient,
					user.getSocialId(),
					"profile",
					"profileId:" + extProfile.getProfileId(),
					"appId:" + extProfile.getAppId() + ",userId:"
							+ extProfile.getUserId(), null, null);
			extProfile.setSocialId(entity.getId());
		} catch (WebApiException e1) {
			logger.error("Exception creating profile entity", e1);
			throw new SmartCampusException(
					"Exception creating social entity for profile");
		}
		try {
			storage.storeObject(extProfile);
			return extProfile;
		} catch (DataException e) {
			logger.error("Exception storing extended profile", e);
			throw new SmartCampusException(
					"Exceptions storing extended profile");
		}
	}

	/**
	 * Deletes an extended profile
	 * 
	 * @param extProfileId
	 *            id of extended profile to delete
	 * @return true if operation gone fine, a SmartCampusException is threw
	 *         otherwise
	 * @throws SmartCampusException
	 * @throws DataException
	 * @throws NotFoundException
	 */
	public boolean deleteExtendedProfile(String extProfileId)
			throws SmartCampusException, NotFoundException {
		try {
			return deleteExtendedProfile(storage.getObjectById(extProfileId,
					ExtendedProfile.class));
		} catch (DataException e) {
			String msg = String.format("Exception getting extended profile %s",
					extProfileId);
			logger.error(msg, e);
			throw new SmartCampusException(msg);
		}
	}

	/**
	 * 
	 * @param extProfile
	 * @return
	 * @throws SmartCampusException
	 */
	public boolean deleteExtendedProfile(ExtendedProfile extProfile)
			throws SmartCampusException {
		try {
			try {
				if (!SemanticHelper.deleteEntity(socialEngineClient,
						extProfile.getSocialId())) {
					logger.warn(String
							.format("Error deleting entity %s binded to extended profile %s",
									extProfile.getSocialId(),
									extProfile.getId()));
				}
			} catch (WebApiException e) {
				logger.warn(String
						.format("Error deleting entity %s binded to extended profile %s",
								extProfile.getSocialId(), extProfile.getId()));
			}

			storage.deleteExtendedProfile(extProfile.getUserId(),
					extProfile.getAppId(), extProfile.getProfileId());
			return true;
		} catch (DataException e) {
			String msg = String.format(
					"Exception deleting extended profile %s",
					extProfile.getId());
			logger.error(msg, e);
			throw new SmartCampusException(msg);
		}
	}

}
