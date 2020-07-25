
package main.java.com.askme.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import main.java.com.askme.dao.mysql.TopicsDAO;
import main.java.com.askme.repos.mysql.TopicsRepo;
import main.java.com.askme.utils.RestResponse;
import main.java.com.askme.utils.AskMeConstants;

@Controller
public class TopicsController
{
	private static Logger	log	= Logger.getLogger(TopicsController.class);

	@Autowired
	TopicsRepo				topics;

	@RequestMapping(value = "/topics", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<RestResponse> getTopics()
	{
		RestResponse response = new RestResponse();
		try
		{
			List<TopicsDAO> topicsList = topics.findAll();

			Map<String, Object> respObj = new HashMap<String, Object>();
			respObj.put(AskMeConstants.TOPICS, topicsList);

			response.setResponseCode("success");
			response.setResponseMessage("success");
			response.setData(respObj);
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
