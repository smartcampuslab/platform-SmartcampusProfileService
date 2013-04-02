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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.ac.provider.AcServiceException;
import eu.trentorise.smartcampus.ac.provider.filters.AcClient;
import eu.trentorise.smartcampus.ac.provider.model.User;
import eu.trentorise.smartcampus.exceptions.SmartCampusException;
import eu.trentorise.smartcampus.profileservice.model.ExtendedProfile;

/**
 * @author mirko perillo
 * 
 */
@Component
public class PermissionManager {

	private static final Logger logger = Logger
			.getLogger(PermissionManager.class);

	@Autowired
	private AcClient acClient;

	public boolean checkExtendedProfilePermission(User user,
			ExtendedProfile profile) throws SmartCampusException {
		try {

			return acClient.canReadResource(user.getAuthToken(),
					"" + profile.getSocialId());
		} catch (AcServiceException e) {
			String msg = String.format(
					"Error checking if user %s can access profile %s",
					user.getId(), profile.getId());
			logger.error(msg, e);
			throw new SmartCampusException(msg);
		}
	}
}
