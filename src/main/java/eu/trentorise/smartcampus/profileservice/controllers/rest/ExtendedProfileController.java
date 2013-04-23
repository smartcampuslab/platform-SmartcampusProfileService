package eu.trentorise.smartcampus.profileservice.controllers.rest;

import java.io.IOException;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.ac.provider.model.User;
import eu.trentorise.smartcampus.controllers.SCController;
import eu.trentorise.smartcampus.exceptions.AlreadyExistException;
import eu.trentorise.smartcampus.exceptions.SmartCampusException;
import eu.trentorise.smartcampus.profileservice.managers.CommunityManagerException;
import eu.trentorise.smartcampus.profileservice.managers.PermissionManager;
import eu.trentorise.smartcampus.profileservice.managers.ProfileManager;
import eu.trentorise.smartcampus.profileservice.model.ExtendedProfile;
import eu.trentorise.smartcampus.profileservice.model.ExtendedProfiles;
import eu.trentorise.smartcampus.profileservice.storage.ProfileStorage;

@Controller("extendedProfileController")
public class ExtendedProfileController extends SCController {

	private static final Logger logger = Logger
			.getLogger(ExtendedProfileController.class);

	@Autowired
	private ProfileManager profileManager;

	@Autowired
	private ProfileStorage storage;

	@Autowired
	private PermissionManager permissionManager;

	/**
	 * Creates a extended profile for a user given application and profileId
	 * Valid only if userId is the authenticated user
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @param userId
	 * @param appId
	 * @param profileId
	 * @param content
	 * @throws IOException
	 * @throws CommunityManagerException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/eu.trentorise.smartcampus.profileservice.model.ExtendedProfile/{userId}/{appId}/{profileId}")
	public void createExtendedProfile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("userId") String userId,
			@PathVariable("appId") String appId,
			@PathVariable("profileId") String profileId,
			@RequestBody Map<String, Object> content) throws IOException,
			CommunityManagerException {
		User user = null;
		try {
			user = retrieveUser(request);
			// User should not be null
			if (user == null) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
		} catch (SmartCampusException e1) {
			logger.error("Error getting user", e1);
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}

		// check permissions
		String id = Long.toString(user.getId());

		if (!id.equals(userId)) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		ExtendedProfile extProfile = new ExtendedProfile();
		extProfile.setAppId(appId);
		extProfile.setProfileId(profileId);
		extProfile.setUserId(id);
		extProfile.setContent(content);
		extProfile.setUser(id);
		extProfile.setUpdateTime(System.currentTimeMillis());

		try {
			profileManager.create(user, extProfile);
		} catch (AlreadyExistException e) {
			logger.error(
					String.format(
							"Extended profile already exists userId:%s, appId:%s, profileId:%s",
							userId, appId, profileId), e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		} catch (SmartCampusException e) {
			logger.error(
					"General exception creating extended profile for user "
							+ user.getId(), e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * Creates a extended profile for an authenticated user, given an
	 * application and a profileId
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @param appId
	 * @param profileId
	 * @param content
	 * @throws IOException
	 * @throws CommunityManagerException
	 */

	@RequestMapping(method = RequestMethod.POST, value = "/eu.trentorise.smartcampus.profileservice.model.ExtendedProfile/me/{appId}/{profileId}")
	public void createMyExtendedProfile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("appId") String appId,
			@PathVariable("profileId") String profileId,
			@RequestBody Map<String, Object> content) throws IOException,
			CommunityManagerException {
		User user = null;
		try {
			user = retrieveUser(request);
			// User should not be null
			if (user == null) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
		} catch (SmartCampusException e1) {
			logger.error("Error getting user", e1);
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}

		ExtendedProfile extProfile = new ExtendedProfile();
		extProfile.setAppId(appId);
		extProfile.setProfileId(profileId);
		extProfile.setUserId("" + user.getId());
		extProfile.setContent(content);
		extProfile.setUser("" + user.getId());
		extProfile.setUpdateTime(System.currentTimeMillis());

		try {
			profileManager.create(user, extProfile);
		} catch (AlreadyExistException e) {
			logger.error(
					String.format(
							"Extended profile already exists userId:%s, appId:%s, profileId:%s",
							"" + user.getId(), appId, profileId), e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		} catch (SmartCampusException e) {
			logger.error(
					"General exception creating extended profile for user "
							+ user.getId(), e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * Returns extended profile of a user given application and profileId,
	 * filtered by user visibility permissions
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @param userId
	 * @param appId
	 * @param profileId
	 * @return
	 * @throws IOException
	 * @throws CommunityManagerException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.profileservice.model.ExtendedProfile/{userId}/{appId}/{profileId}")
	public @ResponseBody
	ExtendedProfile getExtendedProfile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("userId") String userId,
			@PathVariable("appId") String appId,
			@PathVariable("profileId") String profileId) throws IOException,
			CommunityManagerException {
		try {
			User user = retrieveUser(request);
			// User should not be null
			if (user == null) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return null;
			}

			ExtendedProfile profile = storage.findExtendedProfile(userId,
					appId, profileId);
			
			if (profile == null) return null;

			if (!permissionManager
					.checkExtendedProfilePermission(user, profile)) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				return null;
			} else {
				return profile;
			}

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
	}

	/**
	 * Returns extended profile of an authenticate user given application and
	 * profileId
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @param appId
	 * @param profileId
	 * @return
	 * @throws IOException
	 * @throws CommunityManagerException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.profileservice.model.ExtendedProfile/me/{appId}/{profileId}")
	public @ResponseBody
	ExtendedProfile getMyExtendedProfile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("appId") String appId,
			@PathVariable("profileId") String profileId) throws IOException,
			CommunityManagerException {
		try {
			User user = retrieveUser(request);
			// User should not be null
			if (user == null) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return null;
			}

			return storage.findExtendedProfile("" + user.getId(), appId,
					profileId);

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
	}

	/**
	 * Returns all profiles of a user for given application, filtered by user
	 * visibility permissions
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @param userId
	 * @param appId
	 * @return
	 * @throws IOException
	 * @throws CommunityManagerException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.profileservice.model.ExtendedProfile/{userId}/{appId}")
	public @ResponseBody
	ExtendedProfiles getExtendedProfiles(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("userId") String userId,
			@PathVariable("appId") String appId) throws IOException,
			CommunityManagerException {
		try {
			User user = retrieveUser(request);
			// User should not be null
			if (user == null) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return null;
			}

			List<ExtendedProfile> profiles = storage.findExtendedProfiles(
					userId, appId);

			// check user permissions
			List<ExtendedProfile> filteredProfiles = new ArrayList<ExtendedProfile>();
			for (ExtendedProfile profile : profiles) {
				if (permissionManager.checkExtendedProfilePermission(user,
						profile)) {
					filteredProfiles.add(profile);
				}
			}

			ExtendedProfiles ext = new ExtendedProfiles();
			ext.setProfiles(filteredProfiles);
			return ext;

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
	}

	/**
	 * Returns all extended profile for given application and profileId,
	 * filtered by user visibility permissions
	 * 
	 * @param request
	 * @param response
	 * @param profileId
	 * @param appId
	 * @param profileAttrs
	 * @return
	 * @throws IOException
	 * @throws CommunityManagerException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/eu.trentorise.smartcampus.profileservice.model.ExtendedProfile/{appId}/{profileId}")
	public @ResponseBody
	ExtendedProfiles getUsersExtendedProfilesByAttributes(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable String profileId, @PathVariable String appId,
			@RequestBody Map<String, Object> profileAttrs) throws IOException,
			CommunityManagerException {

		try {
			User user = retrieveUser(request);
			// User should not be null
			if (user == null) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return null;
			}

			List<ExtendedProfile> profiles = storage.findExtendedProfiles(
					appId, profileId, profileAttrs);

			// check user permissions
			List<ExtendedProfile> filteredProfiles = new ArrayList<ExtendedProfile>();
			for (ExtendedProfile profile : profiles) {
				if (permissionManager.checkExtendedProfilePermission(user,
						profile)) {
					filteredProfiles.add(profile);
				}
			}

			ExtendedProfiles ext = new ExtendedProfiles();
			ext.setProfiles(filteredProfiles);
			return ext;

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}

	}

	/**
	 * Returns the list of extended profiles of a list of userIds given an
	 * application, filtered by user visibility permissions on the extended
	 * profiles.
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @param userIds
	 * @param userId
	 * @param appId
	 * @return
	 * @throws IOException
	 * @throws CommunityManagerException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.profileservice.model.ExtendedProfile/{appId}")
	public @ResponseBody
	ExtendedProfiles getUsersExtendedProfiles(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestParam List<String> userIds,
			@PathVariable("appId") String appId) throws IOException,
			CommunityManagerException {
		try {
			User user = retrieveUser(request);
			// User should not be null
			if (user == null) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return null;
			}

			List<ExtendedProfile> profiles = new ArrayList<ExtendedProfile>();
			for (String userId : userIds) {
				profiles.addAll(storage.findExtendedProfiles(userId, appId));
			}

			// check user permissions
			List<ExtendedProfile> filteredProfiles = new ArrayList<ExtendedProfile>();
			for (ExtendedProfile profile : profiles) {
				if (permissionManager.checkExtendedProfilePermission(user,
						profile)) {
					filteredProfiles.add(profile);
				}
			}

			ExtendedProfiles ext = new ExtendedProfiles();
			ext.setProfiles(filteredProfiles);
			return ext;

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
	}

	/**
	 * Returns the list of extended profiles of authenticated user for a given
	 * application
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @param appId
	 * @return
	 * @throws IOException
	 * @throws CommunityManagerException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.profileservice.model.ExtendedProfile/me/{appId}")
	public @ResponseBody
	ExtendedProfiles getMyExtendedProfiles(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("appId") String appId) throws IOException,
			CommunityManagerException {
		try {
			User user = retrieveUser(request);
			// User should not be null
			if (user == null) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return null;
			}

			List<ExtendedProfile> profiles = storage.findExtendedProfiles(""
					+ user.getId(), appId);
			ExtendedProfiles ext = new ExtendedProfiles();
			ext.setProfiles(profiles);

			return ext;

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
	}

	/**
	 * Updates a extended profile of a user given application and profileId
	 * Valid only if userId is the authenticated user
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @param userId
	 * @param appId
	 * @param profileId
	 * @param content
	 * @throws IOException
	 * @throws CommunityManagerException
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/eu.trentorise.smartcampus.profileservice.model.ExtendedProfile/{userId}/{appId}/{profileId}")
	public void updateExtendedProfile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("userId") String userId,
			@PathVariable("appId") String appId,
			@PathVariable("profileId") String profileId,
			@RequestBody Map<String, Object> content) throws IOException,
			CommunityManagerException {
		try {
			User user = retrieveUser(request);
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

			ExtendedProfile profile = storage.findExtendedProfile(userId,
					appId, profileId);

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

	/**
	 * Updates an extended profile of authenticated user given application and
	 * profileId
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @param appId
	 * @param profileId
	 * @param content
	 * @throws IOException
	 * @throws CommunityManagerException
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/eu.trentorise.smartcampus.profileservice.model.ExtendedProfile/me/{appId}/{profileId}")
	public void updateMyExtendedProfile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("appId") String appId,
			@PathVariable("profileId") String profileId,
			@RequestBody Map<String, Object> content) throws IOException,
			CommunityManagerException {
		try {
			User user = retrieveUser(request);
			// User should not be null
			if (user == null) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			ExtendedProfile profile = storage.findExtendedProfile(
					"" + user.getId(), appId, profileId);

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

	/**
	 * Deletes an extended profile of a user given application and profileId
	 * Valid only if userId is the authenticated user
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @param userId
	 * @param appId
	 * @param profileId
	 * @throws IOException
	 * @throws CommunityManagerException
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/eu.trentorise.smartcampus.profileservice.model.ExtendedProfile/{userId}/{appId}/{profileId}")
	public void deleteExtendedProfile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("userId") String userId,
			@PathVariable("appId") String appId,
			@PathVariable("profileId") String profileId) throws IOException,
			CommunityManagerException {
		try {
			User user = retrieveUser(request);
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

	/**
	 * Deletes an extended profile of an authenticated user given application
	 * and profileId
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @param appId
	 * @param profileId
	 * @throws IOException
	 * @throws CommunityManagerException
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/eu.trentorise.smartcampus.profileservice.model.ExtendedProfile/me/{appId}/{profileId}")
	public void deleteMyExtendedProfile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("appId") String appId,
			@PathVariable("profileId") String profileId) throws IOException,
			CommunityManagerException {
		try {
			User user = retrieveUser(request);
			// User should not be null
			if (user == null) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			storage.deleteExtendedProfile("" + user.getId(), appId, profileId);

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

}
