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

import eu.trentorise.smartcampus.exceptions.AlreadyExistException;
import eu.trentorise.smartcampus.exceptions.SmartCampusException;
import eu.trentorise.smartcampus.profileservice.managers.CommunityManagerException;
import eu.trentorise.smartcampus.profileservice.managers.PermissionManager;
import eu.trentorise.smartcampus.profileservice.managers.ProfileManager;
import eu.trentorise.smartcampus.profileservice.model.ExtendedProfile;
import eu.trentorise.smartcampus.profileservice.model.ExtendedProfiles;
import eu.trentorise.smartcampus.profileservice.storage.ProfileStorage;
import eu.trentorise.smartcampus.resourceprovider.controller.SCController;
import eu.trentorise.smartcampus.resourceprovider.model.AuthServices;
import eu.trentorise.smartcampus.resourceprovider.model.User;

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

	@Autowired
	private AuthServices services;
	@Override
	protected AuthServices getAuthServices() {
		return services;
	}

	/**
	 * Creates a extended profile for a user given application and profileId
	 * Valid only if userId is the authenticated user
	 * 
	 * @param response
	 * @param userId
	 * @param appId
	 * @param profileId
	 * @param content
	 * @throws IOException
	 * @throws CommunityManagerException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/extprofile/{userId}/{appId}/{profileId}")
	public void createExtendedProfile(HttpServletResponse response,
			@PathVariable("userId") String userId,
			@PathVariable("appId") String appId,
			@PathVariable("profileId") String profileId,
			@RequestBody Map<String, Object> content) throws IOException,
			CommunityManagerException {

		ExtendedProfile extProfile = new ExtendedProfile();
		extProfile.setAppId(appId);
		extProfile.setProfileId(profileId);
		extProfile.setUserId(userId);
		extProfile.setContent(content);
		extProfile.setUser(userId);
		extProfile.setUpdateTime(System.currentTimeMillis());

		try {
			User user = getUserObject(userId);
			if (user == null) {
				throw new SmartCampusException("No user found for id "+userId);
			}
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
							+ userId, e);
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
	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/{userId}/{appId}/{profileId}")
	public @ResponseBody
	ExtendedProfile getExtendedProfile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("userId") String userId,
			@PathVariable("appId") String appId,
			@PathVariable("profileId") String profileId) throws IOException,
			CommunityManagerException {
		try {
			ExtendedProfile profile = storage.findExtendedProfile(userId, appId, profileId);
			return profile;

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
	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/me/{appId}/{profileId}")
	public @ResponseBody
	ExtendedProfile getMyExtendedProfile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("appId") String appId,
			@PathVariable("profileId") String profileId) throws IOException,
			CommunityManagerException {
		try {
			String userId = getUserId();

			return storage.findExtendedProfile(userId, appId, profileId);

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
	}

	/**
	 * Returns extended profiles of an authenticate user given application
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @param appId
	 * @return
	 * @throws IOException
	 * @throws CommunityManagerException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/me/{appId}")
	public @ResponseBody
	ExtendedProfiles getMyAppExtendedProfiles(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("appId") String appId) throws IOException,
			CommunityManagerException {
		try {
			String userId = getUserId();
			ExtendedProfiles ext = new ExtendedProfiles();
			ext.setProfiles(storage.findExtendedProfiles(userId, appId));
			return ext;

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
	}

	/**
	 * Returns extended profiles of an authenticate user
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 * @throws IOException
	 * @throws CommunityManagerException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/me")
	public @ResponseBody
	ExtendedProfiles getMyExtendedProfiles(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) throws IOException,
			CommunityManagerException {
		try {
			String userId = getUserId();
			ExtendedProfiles ext = new ExtendedProfiles();
			ext.setProfiles(storage.findExtendedProfiles(""+userId));
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
	@RequestMapping(method = RequestMethod.POST, value = "/extprofile/all/{appId}/{profileId}")
	public @ResponseBody
	ExtendedProfiles getUsersExtendedProfilesByAttributes(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable String profileId, @PathVariable String appId,
			@RequestBody Map<String, Object> profileAttrs) throws IOException,
			CommunityManagerException {

		try {
			List<ExtendedProfile> profiles = storage.findExtendedProfiles(appId, profileId, profileAttrs);

			ExtendedProfiles ext = new ExtendedProfiles();
			ext.setProfiles(profiles);
			return ext;

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}

	}

	/**
	 * Returns the list of extended profiles of a list of userIds.
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @param userIds
	 * @return
	 * @throws IOException
	 * @throws CommunityManagerException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/all")
	public @ResponseBody
	ExtendedProfiles getUsersExtendedProfiles(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestParam List<String> userIds) throws IOException,
			CommunityManagerException {
		return getAllProfiles(response, userIds, null, null);
	}
	/**
	 * Returns the list of extended profiles of a list of userIds given an
	 * application.
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
	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/all/{appId}")
	public @ResponseBody
	ExtendedProfiles getUsersAppExtendedProfiles(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestParam List<String> userIds,
			@PathVariable("appId") String appId) throws IOException,
			CommunityManagerException {
		return getAllProfiles(response, userIds, appId, null);
	}
	/**
	 * Returns the list of extended profiles of a list of userIds given an
	 * application and profile.
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @param userIds
	 * @param userId
	 * @param appId
	 * @param profileId
	 * @return
	 * @throws IOException
	 * @throws CommunityManagerException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/extprofile/all/{appId}/{profileId}")
	public @ResponseBody
	ExtendedProfiles getUsersAppProfileExtendedProfiles(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestParam List<String> userIds,
			@PathVariable("appId") String appId, @PathVariable String profileId) throws IOException,
			CommunityManagerException {
		return getAllProfiles(response, userIds, appId, profileId);
	}

	protected ExtendedProfiles getAllProfiles(HttpServletResponse response, List<String> userIds, String appId, String profileId) {
		try {
			List<ExtendedProfile> profiles = new ArrayList<ExtendedProfile>();
			for (String userId : userIds) {
				if (profileId != null && appId != null) {
					profiles.add(storage.findExtendedProfile(userId, appId, profileId));
				} else if (appId != null) {
					profiles.addAll(storage.findExtendedProfiles(userId, appId));
				} else {
					profiles.addAll(storage.findExtendedProfiles(userId));
				}
			}

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
	@RequestMapping(method = RequestMethod.PUT, value = "/extprofile/{userId}/{appId}/{profileId}")
	public void updateExtendedProfile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("userId") String userId,
			@PathVariable("appId") String appId,
			@PathVariable("profileId") String profileId,
			@RequestBody Map<String, Object> content) throws IOException,
			CommunityManagerException {
		try {
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
	@RequestMapping(method = RequestMethod.DELETE, value = "/extprofile/{userId}/{appId}/{profileId}")
	public void deleteExtendedProfile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("userId") String userId,
			@PathVariable("appId") String appId,
			@PathVariable("profileId") String profileId) throws IOException,
			CommunityManagerException {
		try {
			storage.deleteExtendedProfile(userId, appId, profileId);

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

}
