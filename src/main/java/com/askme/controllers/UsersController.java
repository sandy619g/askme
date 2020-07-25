
package main.java.com.askme.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import main.java.com.askme.dao.mongo.AnswersDAO;
import main.java.com.askme.dao.mongo.QuestionsDAO;
import main.java.com.askme.dao.mysql.NotificationsDAO;
import main.java.com.askme.dao.mysql.TopicsDAO;
import main.java.com.askme.dao.mysql.UserTopicsDAO;
import main.java.com.askme.dao.mysql.UsersDAO;
import main.java.com.askme.repos.mongo.AnswersRepo;
import main.java.com.askme.repos.mongo.QuestionsRepo;
import main.java.com.askme.repos.mysql.NotificationsRepo;
import main.java.com.askme.repos.mysql.UserTopicsRepo;
import main.java.com.askme.repos.mysql.UsersRepo;
import main.java.com.askme.utils.ResponseCode;
import main.java.com.askme.utils.RestResponse;
import main.java.com.askme.utils.AskMeConstants;
import main.java.com.askme.utils.AskMeUtils;

@Controller
public class UsersController
{
	private static Logger	log	= Logger.getLogger(UsersController.class);

	@Autowired
	UsersRepo				uRepo;

	@Autowired
	NotificationsRepo		notiRepo;

	@Autowired
	UserTopicController		utCtrl;

	@Autowired
	AnswersRepo				aRepo;

	@Autowired
	QuestionsRepo			qRepo;

	@Autowired
	UserTopicsRepo			utRepo;

	@Autowired
	MongoOperations			mOps;

	@Autowired
	JdbcOperations			jOps;

	/**
	 * Get a user profile
	 * 
	 * @param id
	 * @param httpReq
	 * @param httpRes
	 * @return
	 */
	@RequestMapping(value = "/users/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<RestResponse> getUser(@PathVariable int id, HttpServletRequest httpReq, HttpServletResponse httpRes)
	{
		RestResponse response = new RestResponse();
		try
		{
			UsersDAO userObj = uRepo.findById(id);
			response.setResponseCode(ResponseCode.SUCCESS);
			response.setResponseMessage("success");
			response.setData(new ObjectMapper().readValue(userObj.toString(), HashMap.class));
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
	 * Save a user profile
	 * 
	 * @param data
	 *            {}
	 * @param httpReq
	 * @param httpRes
	 * @return
	 */
	@RequestMapping(value = "/users", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<RestResponse> saveUsers(@RequestBody Map<String, Object> data, HttpServletRequest httpReq, HttpServletResponse httpRes)
	{
		RestResponse response = new RestResponse();
		try
		{
			long phone = data.get("phoneNo") == null ? -1l : Long.valueOf(String.valueOf(data.get("phoneNo")));
			UsersDAO findByPhone = uRepo.findByPhoneNo(phone);

			if(findByPhone != null)
			{
				response.setResponseCode(ResponseCode.ERROR);
				response.setResponseMessage("User already exists");
				response.setData(new ObjectMapper().readValue(findByPhone.toString(), HashMap.class));
				return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(response);
			}

			if(AskMeUtils.isValid(data))
			{
				UsersDAO newUser = new UsersDAO();
				String userData = saveUserData(data, newUser);

				//subscribe to default topics
				defaultSubscriptions(newUser);

				response.setResponseCode(ResponseCode.SUCCESS);
				response.setResponseMessage("success");
				response.setData(new ObjectMapper().readValue(userData, HashMap.class));
			}
			else
			{
				response.setResponseCode(ResponseCode.ERROR);
				response.setResponseMessage("Post data null or blank");
				response.setData(new HashMap<>());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(response);
			}
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

	public String saveUserData(Map data, UsersDAO newUser)
	{
		Iterator iterator = data.keySet().iterator();

		try
		{
			while(iterator.hasNext())
			{
				String key = (String) iterator.next();
				String value = String.valueOf(data.get(key));

				switch(key)
				{
					case "userId":
						newUser.setUserId(Integer.valueOf(value));
					break;
					case "name":
						newUser.setName(value);
					break;
					case "roleId":
						newUser.setRoleId(Integer.valueOf(value));
					break;
					case "RoleName":
						newUser.setRoleName(value);
					break;
					case "phoneNo":
						newUser.setPhoneNo(Long.valueOf(value));
					break;
					case "emailId":
						newUser.setEmailId(value);
					break;
					case "pincode":
						newUser.setPincode(Integer.valueOf(value));
					break;
					case "standard":
						newUser.setStandard(value);
					break;
					case "section":
						newUser.setSection(value);
					break;
					case "dateOfBirth":
						newUser.setDateOfBirth(value);
					break;
				}
			}
			uRepo.save(newUser);
		}
		catch(Exception e)
		{
			log.error(e.getMessage(), e);
			throw e;
		}
		return newUser.toString();
	}

	public void defaultSubscriptions(UsersDAO user)
	{
		int[] topicsArr = {1, 25, 40, 51, 57};

		for(int i = 0; i < topicsArr.length; i++)
		{
			TopicsDAO topicDao = UserTopicController.topicsMap.get(topicsArr[i]);
			utCtrl.subscribeTopic(user, topicDao);
		}
	}

	/**
	 * Save a user profile
	 * 
	 * @param data
	 *            {}
	 * @param httpReq
	 * @param httpRes
	 * @return
	 */
	@RequestMapping(value = "/users/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<RestResponse> updateUsers(@PathVariable int id, @RequestBody Map<String, Object> data, HttpServletRequest httpReq, HttpServletResponse httpRes)
	{
		RestResponse response = new RestResponse();
		try
		{
			int phone = data.get("phoneNo") == null ? -1 : (int) data.get("phoneNo");
			UsersDAO findByPhone = uRepo.findById(id);

			if(findByPhone == null)
			{
				response.setResponseCode(ResponseCode.ERROR);
				response.setResponseMessage("User doesn't exists!");
				response.setData(new HashMap<>());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(response);
			}

			if(AskMeUtils.isValid(data))
			{
				String userData = saveUserData(data, findByPhone);
				response.setResponseCode(ResponseCode.SUCCESS);
				response.setResponseMessage("success");
				response.setData(new ObjectMapper().readValue(userData, HashMap.class));
			}
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
	 * returns feeds based on type i.e; if the type is 'user' then id will act as userId and if the type is 'question' then id will act as questionId return the result based on the type
	 * 
	 * @param type
	 *            type may be 'user' or 'question'
	 * @param id
	 *            based on type this will act as 'userId' or 'questionId'
	 * 
	 * @return {@link ResponseEntity}
	 */
	@RequestMapping(value = "/users/feeds", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<RestResponse> getUserFeeds(@RequestParam String type, @RequestParam String id, @RequestParam(required = false, defaultValue = "0") int page, @RequestParam(required = false, defaultValue = "10") int size)
	{
		RestResponse response = new RestResponse();
		Map<String, Object> respObj = new HashMap<String, Object>();

		try
		{
			if(type.equalsIgnoreCase("user"))
			{
				/*
				 * Find all topics subscribed by user
				 * Find all questions per topic order by modifiedTime desc limit page offset size 
				 * Find most recent answer for all questions
				 * Add to Map Qs<->Ans 
				 */
				Pageable pageObj = new PageRequest(page, size, Direction.DESC, "modifiedTime");
				List<Integer> userTopics = jOps.queryForList("select id from user_topics where user_id=" + id, Integer.class);

				/*				
	  				List<UserTopicsDAO> userTopics = utRepo.findByUserId(Integer.valueOf(id));
					List<Integer> topicsList = new ArrayList<>();
					if(userTopics != null || userTopics.size() < 0)
					{
						for(UserTopicsDAO user : userTopics)
							topicsList.add(user.getId());
					}
				*/
				if(userTopics != null && !userTopics.isEmpty())
				{

					List<QuestionsDAO> allQuestions = mOps.find(new Query().addCriteria(Criteria.where("qsTopicIds.myArrayList").in(userTopics)
																		   .andOperator(Criteria.where("recentAnsUUID").ne(null))).with(pageObj), QuestionsDAO.class);
					JSONObject qsAs = null;
					JSONArray feeds = new JSONArray();
					for(QuestionsDAO qs : allQuestions)
					{
						qsAs = new JSONObject();
						qsAs.put("question", qs);
						qsAs.put("answer", aRepo.findByAnsUUID(qs.getRecentAnsUUID()));
						feeds.put(qsAs);
					}
					respObj.put("feeds", new ObjectMapper().readValue(feeds.toString(), ArrayList.class));

				}
			}
			else if(type.equalsIgnoreCase("question"))
			{
				QuestionsDAO question = qRepo.findByQsUUID(id);
				//if question doesn't exist
				if(question == null)
				{
					response.setResponseCode(ResponseCode.ERROR);
					response.setResponseMessage("Question doesn't exists!");
					response.setData(respObj);
					return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(response);
				}
				JSONArray answers = question.getAnswers();
				List<AnswersDAO> listAns = new ArrayList<>();

				if(answers != null && answers.length() > 0)
					for(int i = 0; i < answers.length(); i++)
						listAns.add(aRepo.findByAnsUUID(answers.optJSONObject(i).optString("ansUUID")));

				respObj.put(AskMeConstants.ANSWERS, listAns.toString());
			}

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
	 * fetches users notifications based on the userId provided in the url
	 * 
	 * @param userId
	 * @return {@link ResponseEntity}
	 */
	@RequestMapping(value = "/users/notifications/{userId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<RestResponse> getNotifications(@PathVariable int userId)
	{
		RestResponse response = new RestResponse();

		try
		{
			Map<String, Object> respObj = new HashMap();
			List<NotificationsDAO> notifications = notiRepo.findAllByUserIdOrderByCreatedTimeDesc(userId);
			if(!AskMeUtils.isValid(notifications))
			{
				notifications = new ArrayList();
			}
			respObj.put("notifications", notifications);

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
	 * updates user's notification as read
	 * 
	 * @param userId
	 * @param notificationId
	 * @param data
	 * @param httpReq
	 * @param httpRes
	 * @return {@link ResponseEntity}
	 */
	@RequestMapping(value = "/users/notifications/{userId}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<RestResponse> updateUserNotificationsAsRead(@PathVariable int userId, @RequestBody Map<String, Object> data, HttpServletRequest httpReq, HttpServletResponse httpRes)
	{
		RestResponse response = new RestResponse();
		try
		{
			Map<String, Object> respObj = new HashMap<String, Object>();
			List<Integer> notificationIds = (List<Integer>) data.get(AskMeConstants.NOTIFICATION_IDS);
			JSONArray readNotifications = new JSONArray();

			if(AskMeUtils.isValid(notificationIds))
			{
				List<Integer> idList = new ArrayList<Integer>();
				/*for(int i = 0; i < notificationIds.length(); i++)
				{
					int notificationId = notificationIds.optInt(i, -1);
					if(notificationId > 0)
						idList.add(notificationId);
				}*/

				for(Integer notificationId : notificationIds)
				{
					if(notificationId > 0)
						idList.add(notificationId);
				}

				List<NotificationsDAO> notifications = notiRepo.findByIdIn(idList);
				if(AskMeUtils.isValid(notifications))
				{
					for(NotificationsDAO notification : notifications)
					{
						notification.setRead(true);
						notiRepo.save(notification);
						readNotifications.put(notification.getId());
					}
				}
			}

			respObj.put(AskMeConstants.NOTIFICATION_IDS, new ObjectMapper().readValue(readNotifications.toString(), List.class));

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

}
