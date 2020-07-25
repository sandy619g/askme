
package main.java.com.askme.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import main.java.com.askme.dao.mysql.TopicsDAO;
import main.java.com.askme.dao.mysql.UserTopicsDAO;
import main.java.com.askme.dao.mysql.UsersDAO;
import main.java.com.askme.repos.mysql.TopicsRepo;
import main.java.com.askme.repos.mysql.UserTopicsRepo;
import main.java.com.askme.repos.mysql.UsersRepo;
import main.java.com.askme.utils.ResponseCode;
import main.java.com.askme.utils.RestResponse;
import main.java.com.askme.utils.AskMeConstants;
import main.java.com.askme.utils.AskMeUtils;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class UserTopicController
{
	private static Logger					log			= Logger.getLogger(UserTopicController.class);
	public static Map<Integer, TopicsDAO>	topicsMap	= new HashMap<Integer, TopicsDAO>();

	@Autowired
	UserTopicsRepo							utRepo;

	@Autowired
	TopicsRepo								tRepo;

	@Autowired
	UsersRepo								uRepo;

	@PostConstruct
	public void init()
	{
		if(topicsMap.isEmpty())
			loadTopics();
	}

	/**
	 * This method returns the topics subscribed by the user
	 * 
	 * @param userId
	 * @return {@link ResponseEntity}
	 */
	@RequestMapping(value = "/users/topics/{userId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<RestResponse> getUserTopics(@PathVariable int userId)
	{
		RestResponse response = new RestResponse();

		try
		{
			Map<String, Object> respObj = new HashMap<String, Object>();

			List<UserTopicsDAO> userTopics = utRepo.findByUserIdAndIsSubscribed(userId, true);
			if(!AskMeUtils.isValid(userTopics))
			{
				userTopics = new ArrayList<UserTopicsDAO>();
			}

			respObj.put(AskMeConstants.SUBSCRIBED_TOPICS, userTopics);

			response.setResponseCode(ResponseCode.SUCCESS);
			response.setResponseMessage("success");
			response.setData(respObj);
		}
		catch(Exception e)
		{
			log.error(e.getMessage(), e);
			response.setResponseCode(ResponseCode.ERROR);
			response.setResponseMessage("server error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(response);
		}

		return ResponseEntity.ok(response);
	}

	/**
	 * This method subscribes/unsubscribes the topics for the user
	 * 
	 * @param userId
	 * @param data
	 * @param httpReq
	 * @param httpRes
	 * @return {@link ResponseEntity}
	 */
	@RequestMapping(value = "/users/topics/{userId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<RestResponse> subscribeUnsubscribeTopics(@PathVariable int userId, @RequestBody Map<String, Object> data, HttpServletRequest httpReq, HttpServletResponse httpRes)
	{
		RestResponse response = new RestResponse();

		try
		{
			Map<String, Object> respObj = new HashMap<String, Object>();
			UsersDAO userDao = uRepo.findById(userId);

			//array of topics to subscribe and unsubscribe
			List<Integer> subscribeList = (List<Integer>) data.get(AskMeConstants.SUBSCRIBE_TOPICS);
			List<Integer> unsubscribeList = (List<Integer>) data.get(AskMeConstants.UNSUBSCRIBE_TOPICS);
			subscribeList = subscribeList == null ? new ArrayList<Integer>() : subscribeList;
			unsubscribeList = unsubscribeList == null ? new ArrayList<Integer>() : unsubscribeList;

			//array of subscribed and unsubscribed topics for response
			List<Integer> subscribedTopics = new ArrayList<Integer>();
			List<Integer> unsubscribedTopics = new ArrayList<Integer>();

			//return response if nothing to subscribe and unsubscribe
			if(subscribeList.size() <= 0 && unsubscribeList.size() <= 0)
			{
				respObj.put(AskMeConstants.SUBSCRIBED_TOPICS, subscribedTopics);
				respObj.put(AskMeConstants.UNSUBSCRIBED_TOPICS, unsubscribedTopics);

				response.setResponseCode(ResponseCode.SUCCESS);
				response.setResponseMessage("success");
				response.setData(respObj);
			}

			//gets all users topics
			List<UserTopicsDAO> usersTopics = utRepo.findByUserId(userId);

			//toggle subscription for already created userTopics
			for(UserTopicsDAO userTopic : usersTopics)
			{
				int topicId = userTopic.getTopicId();
				boolean isSubscriptionToggled = false;
				if(subscribeList.contains(topicId))
				{
					userTopic.setSubscribed(true);
					subscribedTopics.add(topicId);
					isSubscriptionToggled = true;
				}
				if(unsubscribeList.contains(topicId))
				{
					userTopic.setSubscribed(false);
					unsubscribedTopics.add(topicId);
					isSubscriptionToggled = true;
				}
				if(isSubscriptionToggled)
				{
					userTopic.setModifiedTime(System.currentTimeMillis());
					utRepo.save(userTopic);
					subscribeList.remove(Integer.valueOf(topicId));
					unsubscribeList.remove(Integer.valueOf(topicId));
				}
			}

			//create new userTopics if not created yet
			for(Integer topicId : subscribeList)
			{
				TopicsDAO topicDao = topicsMap.get(topicId);

				int subscribeTopic = subscribeTopic(userDao, topicDao);
				subscribedTopics.add(subscribeTopic);
			}

			respObj.put(AskMeConstants.SUBSCRIBED_TOPICS, subscribedTopics);
			respObj.put(AskMeConstants.UNSUBSCRIBED_TOPICS, unsubscribedTopics);

			response.setResponseCode(ResponseCode.SUCCESS);
			response.setResponseMessage("success");
			response.setData(respObj);
		}
		catch(Exception e)
		{
			log.error(e.getMessage(), e);
			response.setResponseCode(ResponseCode.ERROR);
			response.setResponseMessage("server error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(response);
		}

		return ResponseEntity.ok(response);
	}

	public int subscribeTopic(UsersDAO userDao, TopicsDAO topicDao)
	{

		try
		{
			int topicId = topicDao.getId();
			String topicName = topicDao.getTopicName();
			String topicCategory = topicDao.getTopicCategory();
			int userId = userDao.getId();
			String userName = userDao.getName();
			long createdTime = System.currentTimeMillis();
			long modifiedTime = System.currentTimeMillis();
			boolean isSubscribed = true;
			int roleId = userDao.getRoleId();

			UserTopicsDAO userTopic = new UserTopicsDAO(topicId, topicName, topicCategory, userId, userName, createdTime, modifiedTime, isSubscribed, roleId);
			utRepo.save(userTopic);

			return topicId;
		}
		catch(Exception e)
		{
			log.error(e.getMessage(), e);
			throw e;
		}

	}

	private void loadTopics()
	{
		List<TopicsDAO> topics = tRepo.findAll();
		if(!AskMeUtils.isValid(topics))
			return;

		for(TopicsDAO topic : topics)
		{
			int topicId = topic.getId();
			topicsMap.put(topicId, topic);
		}
	}

}
