
package main.java.com.askme.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import main.java.com.askme.dao.mongo.AnswersDAO;
import main.java.com.askme.dao.mongo.QuestionsDAO;
import main.java.com.askme.dao.mysql.NotificationsDAO;
import main.java.com.askme.dao.mysql.UsersDAO;
import main.java.com.askme.repos.mongo.AnswersRepo;
import main.java.com.askme.repos.mongo.QuestionsRepo;
import main.java.com.askme.repos.mysql.NotificationsRepo;
import main.java.com.askme.repos.mysql.UsersRepo;
import main.java.com.askme.utils.NotificationType;
import main.java.com.askme.utils.ResponseCode;
import main.java.com.askme.utils.RestResponse;
import main.java.com.askme.utils.AskMeConstants;
import main.java.com.askme.utils.AskMeUtils;

@Controller
public class AnswersController
{
	private static Logger	log	= Logger.getLogger(AnswersController.class);

	@Autowired
	AnswersRepo				aRepo;

	@Autowired
	QuestionsRepo			qRepo;

	@Autowired
	UsersRepo				uRepo;

	@Autowired
	NotificationsRepo		nRepo;

	//	@Autowired
	//	MongoTemplate			mTemp;

	@Autowired
	MongoOperations			mOps;

	/**
	 * Return all answers asked by an user
	 * 
	 * @param userId
	 * @param httpReq
	 * @param httpRes
	 * @return
	 */
	@RequestMapping(value = "/users/answers", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<RestResponse> getAnswers(@RequestParam String type, @RequestParam String id, HttpServletRequest httpReq, HttpServletResponse httpRes)
	{
		RestResponse response = new RestResponse();
		try
		{
			Map<String, Object> respObj = new HashMap<String, Object>();

			if(AskMeConstants.ANSWER.equals(type))
			{
				AnswersDAO answer = aRepo.findByAnsUUID(id);
				if(answer == null)
				{
					response.setResponseCode(ResponseCode.ERROR);
					response.setResponseMessage("No such answer exist");
					response.setData(respObj);
					return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(response);
				}
				respObj.put(AskMeConstants.ANSWERS, new ObjectMapper().readValue(answer.toString(), HashMap.class));
				response.setResponseCode("success");
				response.setResponseMessage("success");
				response.setData(respObj);
				return ResponseEntity.ok(response);
			}
			else if(AskMeConstants.USER.equals(type))
			{
				List<AnswersDAO> listAns = aRepo.findAllByAnsweredBy(Integer.valueOf(id));
				respObj.put(AskMeConstants.ANSWERS, new ObjectMapper().readValue(listAns.toString(), List.class));

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
	 * Save an answer asked by user
	 * 
	 * @param data
	 *            {}
	 * @param httpReq
	 * @param httpRes
	 * @return
	 */
	@RequestMapping(value = "/users/answers", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<RestResponse> saveAnswer(@RequestBody Map<String, Object> data, HttpServletRequest httpReq, HttpServletResponse httpRes)
	{
		RestResponse response = new RestResponse();
		try
		{
			if(AskMeUtils.isValid(data))
			{
				String ansStr = saveAnswerData(data);
				response.setResponseCode("success");
				response.setResponseMessage("success");
				response.setData(new ObjectMapper().readValue(ansStr, HashMap.class));
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
			response.setResponseCode("error");
			response.setResponseMessage("server error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(response);
		}

		return ResponseEntity.ok(response);
	}

	public String saveAnswerData(Map<String, Object> data) throws Exception
	{
		try
		{
			String qsUUID = (String) data.get("qsUUID");
			int answeredBy = (int) data.get("userId");
			String text = (String) data.get("text");

			UsersDAO answerBy = uRepo.findById(answeredBy);

			QuestionsDAO question = qRepo.findByQsUUID(qsUUID);

			if(question == null)
				throw new Exception("Question does not exist");

			int qsAskedBy = question.getQsAskedBy();

			AnswersDAO answer = new AnswersDAO(qsAskedBy, answeredBy);
			answer.setQsUUID(qsUUID);
			answer.setAnsText(text);

			answer.setAnsweredRoleId(answerBy.getRoleId());

			answer.setQsText(question.getQsText());
			answer.setQsTopicIds(question.getQsTopicIds());
			answer.setQsAskerRoleId(question.getQsAskedRoleId());

			aRepo.save(answer);

			//Add notification for qsaskedby
			NotificationsDAO notif = new NotificationsDAO(qsAskedBy, NotificationType.NOTIF_ANS);
			notif.setAnsUUID(answer.getAnsUUID());
			notif.setQsUUID(answer.getQsUUID());
			nRepo.save(notif);

			updateQuestion(answer, question);

			return answer.toString();
		}
		catch(Exception e)
		{
			log.error(e.getMessage(), e);
			throw e;
		}
	}

	public void updateQuestion(AnswersDAO answer, QuestionsDAO question)
	{
		//		QuestionsDAO question = qRepo.findByQsUUID(answer.getQsUUID());
		question.setRecentAnsBy(answer.getAnsweredBy());
		question.setRecentAnsUUID(answer.getAnsUUID());

		JSONObject qobj = new JSONObject();

		qobj.put("ansUUID", answer.getAnsUUID());
		qobj.put("qsAnsweredBy", answer.getAnsweredBy());
		qobj.put("qsAnsweredRoleId", answer.getAnsweredRoleId());

		JSONArray answers = question.getAnswers();

		answers.put(qobj);

		int ansCount = question.getAnsCount();
		question.setAnsCount(++ansCount);
		question.setModifiedTime(System.currentTimeMillis());

		mOps.upsert(new Query(Criteria.where("qsUUID").is(answer.getQsUUID())),
				new Update().set("recentAnsBy", answer.getAnsweredBy()).set("recentAnsUUID", answer.getAnsUUID()).set("answers", answers).set("ansCount", ++ansCount).set("modifiedTime", System.currentTimeMillis()), "questions");
		//		qRepo.save(question);
	}

	/**
	 * update an answer asked by an user -- upvotes
	 * 
	 * @param data
	 *            {ansUUID,upvoterId}
	 * @param httpReq
	 * @param httpRes
	 * @return
	 */
	@RequestMapping(value = "/users/answers", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<RestResponse> updateAnswers(@RequestParam String ansUUID, @RequestParam int userId, HttpServletRequest httpReq, HttpServletResponse httpRes)
	{
		RestResponse response = new RestResponse();
		try
		{
			AnswersDAO answer = aRepo.findByAnsUUID(ansUUID);

			if(!AskMeUtils.isValid(answer))
			{
				response.setResponseCode(ResponseCode.ERROR);
				response.setResponseMessage("Answer not found");
				response.setData(new HashMap<>());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(response);
			}

			if(answer.getAnsweredBy() == userId)
			{
				response.setResponseCode(ResponseCode.ERROR);
				response.setResponseMessage("Cannot upvote own answer");
				response.setData(new HashMap<>());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(response);
			}

			UsersDAO upvotedUser = uRepo.findById(answer.getAnsweredBy());

			int usrUpvotes = upvotedUser.getUpvotes();

			boolean flag = true;
			JSONArray ansUpvotedBy = answer.getAnsUpvotedBy();
			int upvotes = answer.getUpvotes();

			if(ansUpvotedBy != null && ansUpvotedBy.length() > 1)
			{
				for(int i = 0; i < ansUpvotedBy.length(); i++)
				{
					if(ansUpvotedBy.optInt(i) == userId)
					{
						flag = false;
						break;
					}
				}
			}

			if(flag)
			{
				ansUpvotedBy.put(userId);
				answer.setAnsUpvotedBy(ansUpvotedBy);
				answer.setUpvotes(++upvotes);
				answer.setModifiedTime(System.currentTimeMillis());

				mOps.upsert(new Query(Criteria.where("ansUUID").is(answer.getAnsUUID())), new Update().set("ansUpvotedBy", ansUpvotedBy).set("upvotes", upvotes).set("modifiedTime", System.currentTimeMillis()), "answers");
				//				aRepo.save(answer);

				//update user upvotes
				upvotedUser.setUpvotes(++usrUpvotes);
				upvotedUser.setModifiedTime(System.currentTimeMillis());
				uRepo.save(upvotedUser);

				//add notification for upvoted user
				NotificationsDAO notif = new NotificationsDAO(upvotedUser.getId(), NotificationType.NOTIF_UPVOTE);
				notif.setAnsUUID(answer.getAnsUUID());
				notif.setQsUUID(answer.getQsUUID());
				nRepo.save(notif);
			}

			response.setResponseCode("success");
			response.setResponseMessage("success");
			response.setData(new ObjectMapper().readValue(answer.toString(), HashMap.class));
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
