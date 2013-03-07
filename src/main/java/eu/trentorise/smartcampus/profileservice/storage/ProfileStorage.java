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
package eu.trentorise.smartcampus.profileservice.storage;

import java.util.List;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.storage.sync.mongo.BasicObjectSyncMongoStorage;
import eu.trentorise.smartcampus.profileservice.model.ExtendedProfile;

public class ProfileStorage extends BasicObjectSyncMongoStorage {

	public ProfileStorage(MongoOperations mongoTemplate) {
		super(mongoTemplate);
	}

	public List<StoreProfile> findByName(String fullname) {
		Criteria criteria = new Criteria();
		criteria = Criteria.where("content.fullname").regex(fullname, "i");
		criteria.and("type").is(StoreProfile.class.getCanonicalName());
		criteria.and("deleted").is(false);
		return find(Query.query(criteria), StoreProfile.class);
	}
	
	public ExtendedProfile findExtendedProfile(String userId, String appId, String profileId) {
		Criteria criteria = new Criteria();
		criteria = Criteria.where("content.userId").is(userId).and("content.appId").is(appId).and("content.profileId").is(profileId);
		criteria.and("type").is(ExtendedProfile.class.getCanonicalName());
		criteria.and("deleted").is(false);
		
		List<ExtendedProfile> profiles = find(Query.query(criteria), ExtendedProfile.class);
		if (!profiles.isEmpty()) {
			return profiles.get(0);
		} else {
			return null;
		}
	}	
	
	public List<ExtendedProfile> findExtendedProfiles(String userId, String appId) {
		Criteria criteria = new Criteria();
		criteria = Criteria.where("content.userId").is(userId).and("content.appId").is(appId);
		criteria.and("type").is(ExtendedProfile.class.getCanonicalName());
		criteria.and("deleted").is(false);
		
		List<ExtendedProfile> profiles = find(Query.query(criteria), ExtendedProfile.class);
		return profiles;
	}		
	
	public void deleteExtendedProfile(String userId, String appId, String profileId) throws DataException {
		Criteria criteria = new Criteria();
		criteria = Criteria.where("content.userId").is(userId).and("content.appId").is(appId).and("content.profileId").is(profileId);
		criteria.and("type").is(ExtendedProfile.class.getCanonicalName());
		criteria.and("deleted").is(false);

		List<ExtendedProfile> profiles = find(Query.query(criteria), ExtendedProfile.class);
		if (!profiles.isEmpty()) {
			deleteObject(profiles.get(0));
		}
	}		
	
	
}

