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
package eu.trentorise.smartcampus.profileservice.controllers.rest;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.ac.provider.model.User;
import eu.trentorise.smartcampus.profileservice.managers.CommunityManagerException;
import eu.trentorise.smartcampus.profileservice.managers.ProfileManager;
import eu.trentorise.smartcampus.profileservice.model.BasicProfile;
import eu.trentorise.smartcampus.profileservice.model.BasicProfiles;

@Controller("basicProfileController")
public class BasicProfileController extends RestController {

	private static final Logger logger = Logger.getLogger(BasicProfileController.class);

	@Autowired
	private ProfileManager profileManager;

	private Validator validator = ESAPI.validator();

	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.profileservice.model.BasicProfile/{userId}")
	public @ResponseBody
	BasicProfile getUser(HttpServletRequest request, HttpServletResponse response, HttpSession session, @PathVariable("userId") String userId) throws IOException, CommunityManagerException {
		try {
			User user = retrieveUser(request, response);

			return profileManager.getBasicProfileById(userId);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.profileservice.model.BasicProfile")
	public @ResponseBody
	BasicProfiles searchUsers(HttpServletRequest request, HttpServletResponse response, HttpSession session, @RequestParam(value = "filter", required = false) String fullNameFilter) throws CommunityManagerException, IOException {
		try {
			List<BasicProfile> list;
			if (fullNameFilter != null && !fullNameFilter.isEmpty()) {
				list = profileManager.getUsers(fullNameFilter);

			} else {
				list = profileManager.getUsers();
			}
			
			BasicProfiles profiles = new BasicProfiles();
			profiles.setProfiles(list);
			return profiles;
			
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.profileservice.model.BasicProfile/me")
	public @ResponseBody
	BasicProfile findProfile(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException, CommunityManagerException {
		try {
			User user = retrieveUser(request, response);
			return profileManager.getOrCreateProfile(user);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
	}

}
