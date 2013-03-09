package eu.trentorise.smartcampus.profileservice.controllers.rest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.ac.provider.model.User;
import eu.trentorise.smartcampus.profileservice.managers.CommunityManagerException;
import eu.trentorise.smartcampus.profileservice.model.ExtendedProfile;
import eu.trentorise.smartcampus.profileservice.model.ExtendedProfiles;
import eu.trentorise.smartcampus.profileservice.storage.ProfileStorage;

@Controller("extendedProfileController")
public class ExtendedProfileController extends RestController {

	// @Autowired
	// private ProfileManager profileManager;

	@Autowired
	private ProfileStorage storage;

	@RequestMapping(method = RequestMethod.POST, value = "/eu.trentorise.smartcampus.profileservice.model.ExtendedProfile/{userId}/{appId}/{profileId}")
	public void createExtendedProfile(HttpServletRequest request, HttpServletResponse response, HttpSession session, @PathVariable("userId") String userId, @PathVariable("appId") String appId, @PathVariable("profileId") String profileId, @RequestBody Map<String, Object> content) throws IOException, CommunityManagerException {
		try {
			User user = retrieveUser(request, response);
			// User should not be null
			if (user == null) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			String id = Long.toString(user.getId());

			if (!id.equals(userId)) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			ExtendedProfile old = storage.findExtendedProfile(userId, appId, profileId);

			if (old != null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			ExtendedProfile extProfile = new ExtendedProfile();
			extProfile.setAppId(appId);
			extProfile.setProfileId(profileId);
			extProfile.setUserId(id);
			extProfile.setContent(content);
			extProfile.setUser(id);
			extProfile.setUpdateTime(System.currentTimeMillis());

			storage.storeObject(extProfile);

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.profileservice.model.ExtendedProfile/{userId}/{appId}/{profileId}")
	public @ResponseBody
	ExtendedProfile getExtendedProfile(HttpServletRequest request, HttpServletResponse response, HttpSession session, @PathVariable("userId") String userId, @PathVariable("appId") String appId, @PathVariable("profileId") String profileId) throws IOException, CommunityManagerException {
		try {
			User user = retrieveUser(request, response);
			// User should not be null
			if (user == null) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return null;
			}
			String id = Long.toString(user.getId());

			if (!id.equals(userId)) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return null;
			}

			return storage.findExtendedProfile(userId, appId, profileId);

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.profileservice.model.ExtendedProfile/{userId}/{appId}")
	public @ResponseBody
	ExtendedProfiles getExtendedProfiles(HttpServletRequest request, HttpServletResponse response, HttpSession session, @PathVariable("userId") String userId, @PathVariable("appId") String appId) throws IOException, CommunityManagerException {
		try {
			User user = retrieveUser(request, response);
			// User should not be null
			if (user == null) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return null;
			}

			String id = Long.toString(user.getId());

			if (!id.equals(userId)) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return null;
			}

			List<ExtendedProfile> profiles = storage.findExtendedProfiles(userId, appId);
			ExtendedProfiles ext = new ExtendedProfiles();
			ext.setProfiles(profiles);
			
			return ext;

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
	}	

	@RequestMapping(method = RequestMethod.PUT, value = "/eu.trentorise.smartcampus.profileservice.model.ExtendedProfile/{userId}/{appId}/{profileId}")
	public void updateExtendedProfile(HttpServletRequest request, HttpServletResponse response, HttpSession session, @PathVariable("userId") String userId, @PathVariable("appId") String appId, @PathVariable("profileId") String profileId, @RequestBody Map<String, Object> content) throws IOException, CommunityManagerException {
		try {
			User user = retrieveUser(request, response);
			// User should not be null
			if (user == null) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			String id = Long.toString(user.getId());

			if (!id.equals(userId)) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			ExtendedProfile profile = storage.findExtendedProfile(userId, appId, profileId);

			if (profile == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			profile.setUpdateTime(System.currentTimeMillis());
			profile.setContent(content);
			profile.setUpdateTime(System.currentTimeMillis());
			storage.updateObject(profile);
			

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/eu.trentorise.smartcampus.profileservice.model.ExtendedProfile/{userId}/{appId}/{profileId}")
	public void deleteExtendedProfile(HttpServletRequest request, HttpServletResponse response, HttpSession session, @PathVariable("userId") String userId, @PathVariable("appId") String appId, @PathVariable("profileId") String profileId) throws IOException, CommunityManagerException {
		try {
			User user = retrieveUser(request, response);
			// User should not be null
			if (user == null) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			String id = Long.toString(user.getId());

			if (!id.equals(userId)) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			storage.deleteExtendedProfile(userId, appId, profileId);

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

}
