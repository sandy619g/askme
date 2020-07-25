
package main.java.com.askme.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import main.java.com.askme.dao.mongo.QuestionsDAO;
import main.java.com.askme.dao.mysql.UsersDAO;
import main.java.com.askme.repos.mongo.QuestionsRepo;
import main.java.com.askme.repos.mysql.UsersRepo;
import main.java.com.askme.utils.ResponseCode;
import main.java.com.askme.utils.RestResponse;
import main.java.com.askme.utils.AskMeConstants;
import main.java.com.askme.utils.AskMeUtils;

@Controller
public class QuestionsController
{
	private static Logger	log	= Logger.getLogger(QuestionsController.class);

	@Autowired
	QuestionsRepo			qRepo;

	@Autowired
	UsersRepo				uRepo;

	/**
	 * Return all questions asked by an user
	 * 
	 * @param userId
	 * @param httpReq
	 * @param httpRes
	 * @return
	 */
	@RequestMapping(value = "/users/questions", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<RestResponse> getQuestions(@RequestParam String type, @RequestParam String id, HttpServletRequest httpReq, HttpServletResponse httpRes)
	{
		RestResponse response = new RestResponse();
		try
		{
			Map<String, Object> respObj = new HashMap<String, Object>();
			if(AskMeConstants.QUESTION.equals(type))
			{
				QuestionsDAO question = qRepo.findByQsUUID(id);
				if(question == null)
				{
					response.setResponseCode(ResponseCode.ERROR);
					response.setResponseMessage("No such question exist");
					response.setData(respObj);
					return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(response);
				}
				respObj.put(AskMeConstants.QUESTIONS, new ObjectMapper().readValue(question.toString(), HashMap.class));
				response.setResponseCode("success");
				response.setResponseMessage("success");
				response.setData(respObj);
				return ResponseEntity.ok(response);
			}
			else if(AskMeConstants.USER.equals(type))
			{
				List<QuestionsDAO> listQs = qRepo.findByQsAskedBy(Integer.valueOf(id));
				respObj.put(AskMeConstants.QUESTIONS, new ObjectMapper().readValue(listQs.toString(), List.class));

				response.setResponseCode("success");
				response.setResponseMessage("success");
				response.setData(respObj);
				return ResponseEntity.ok(response);
			}
			response.setResponseCode(ResponseCode.ERROR);
			response.setResponseMessage("Invalid type");
			response.setData(respObj);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(response);
		}
		catch(Exception e)
		{
			log.error(e.getMessage(), e);
			response.setResponseCode("error");
			response.setResponseMessage("server error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(response);
		}
	}

	/**
	 * Save a question asked by user
	 * 
	 * @param data
	 *            {}
	 * @param httpReq
	 * @param httpRes
	 * @return
	 */
	@RequestMapping(value = "/users/questions", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<RestResponse> saveQuestion(@RequestBody Map<String, Object> data, HttpServletRequest httpReq, HttpServletResponse httpRes)
	{
		RestResponse response = new RestResponse();
		try
		{
			Map<String, Object> respObj = new HashMap<String, Object>();

			if(!AskMeUtils.isValid(data))
			{
				response.setResponseCode(ResponseCode.ERROR);
				response.setResponseMessage("Post data null or blank");
				response.setData(respObj);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(response);
			}

			String qsString = saveQuestionData(data);

			response.setResponseCode(ResponseCode.SUCCESS);
			response.setResponseMessage("success");
			response.setData(new ObjectMapper().readValue(qsString, HashMap.class));
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

	private String saveQuestionData(Map<String, Object> data)
	{

		try
		{
			int userId = (int) data.get(AskMeConstants.USER_ID);
			UsersDAO userObj = uRepo.findById(userId);
			String text = (String) data.get(AskMeConstants.TEXT);
			JSONArray topicIds = new JSONArray(String.valueOf(data.get(AskMeConstants.TOPIC_IDS)));
			JSONArray topicNames = new JSONArray(String.valueOf(data.get(AskMeConstants.TOPIC_NAMES)));
			JSONArray topicCategories = new JSONArray(String.valueOf(data.get(AskMeConstants.TOPIC_CATEGORIES)));
			int askedRoleId = userObj.getRoleId();

			QuestionsDAO question = new QuestionsDAO(text, userId);
			question.setQsTopicIds(topicIds);
			question.setQsTopicNames(topicNames);
			question.setQsTopicCategories(topicCategories);
			question.setQsAskedRoleId(askedRoleId);

			qRepo.save(question);

			return question.toString();
		}
		catch(Exception e)
		{
			log.error(e.getMessage(), e);
			throw e;
		}

	}

	/**
	 * update a question asked by an user --follows
	 * 
	 * @param data
	 *            {}
	 * @param httpReq
	 * @param httpRes
	 * @return
	 */
	@RequestMapping(value = "/users/questions", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<RestResponse> updateQuestion(@RequestBody Map<String, Object> data, HttpServletRequest httpReq, HttpServletResponse httpRes)
	{
		RestResponse response = new RestResponse();
		try
		{
			response.setResponseCode("success");
			response.setResponseMessage("success");
			response.setData(new HashMap<>());
		}
		catch(Exception e)
		{
			log.error(e.getMessage(), e);
			response.setResponseCode("error");
			response.setResponseMessage("server error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(response);
		}

		return ResponseEntity.ok(response);
	}

}
