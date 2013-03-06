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
package eu.trentorise.smartcampus.profileservice.converters;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import eu.trentorise.smartcampus.profileservice.model.BasicProfile;
import eu.trentorise.smartcampus.profileservice.storage.StoreProfile;

/**
 * <i>ProfileConverter</i> converts eu.trentorise.smartcampus.profileservice.model of a user from and to its various
 * typology
 * 
 * @author mirko perillo
 * 
 */
public class ProfileConverter {

	private static final Logger logger = Logger
			.getLogger(ProfileConverter.class);

	public static List<BasicProfile> toBasicProfile(
			List<StoreProfile> storeProfiles) throws Exception {
		List<BasicProfile> minProfiles = new ArrayList<BasicProfile>();
		try {
			for (StoreProfile temp : storeProfiles) {
				minProfiles.add(ProfileConverter.toBasicProfile(temp));
			}
		} catch (Exception e) {
			throw e;
		}

		return minProfiles;
	}

	public static BasicProfile toBasicProfile(StoreProfile storeProfile)
			throws Exception {
		if (storeProfile == null) {
			return null;
		}
		BasicProfile minProfile = new BasicProfile();

		try {
			BeanUtils.copyProperties(minProfile, storeProfile);
		} catch (Exception e) {
			logger.error("Exception converting objects");
			throw e;
		}

		return minProfile;
	}

	public static void copyDifferences(StoreProfile source, StoreProfile target) {
		org.springframework.beans.BeanUtils.copyProperties(source, target,
				new String[] { "id" });
	}

}
