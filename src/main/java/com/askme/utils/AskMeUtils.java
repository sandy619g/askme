
package main.java.com.askme.utils;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class AskMeUtils
{
	private static final Logger LOGGER = Logger.getLogger(AskMeUtils.class);

	/**
	 * Validates parameters for default behavior if it's null or blank it returns false
	 * 
	 * @param obj
	 * @return {@link Boolean} result on the basis of validation
	 */
	public static boolean isValid(final Object... obj)
	{
		for(final Object object : obj)
		{
			if(object == null)
			{
				return false;
			}
			else if(object instanceof String)
			{
				final String strObject = (String) object;
				if(strObject == null || "".equals(strObject.toString()))
				{
					return false;
				}
			}
			else if(object instanceof JSONArray)
			{
				final JSONArray jsonArrayObject = (JSONArray) object;
				try
				{
					if(jsonArrayObject == null || jsonArrayObject.isNull(0) || jsonArrayObject.get(0).equals(""))
					{
						return false;
					}
				}
				catch(final JSONException e)
				{
					return false;
				}
			}
			else if(object instanceof JSONObject)
			{
				final JSONObject jsonObject = (JSONObject) object;
				if(jsonObject.length() < 1)
					return false;
			}
			else if(object instanceof Map)
			{
				final Map mapObj = (Map) object;
				if(mapObj.isEmpty() || mapObj.size() < 1)
					return false;

			}
			else if(object instanceof List)
			{
				final List listObj = (List) object;
				if(listObj.isEmpty() || listObj.size() < 1)
					return false;
			}
			else
			{
				Class<? extends Object> classInstance = object.getClass();
				try
				{
					if(classInstance.newInstance().equals(object))
					{
						return false;
					}
				}
				catch(Exception e)
				{
					LOGGER.error(e.getMessage(), e);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Validates whether given parameter is integer or not
	 * 
	 * @param number
	 *            parmeter to be validated
	 * @return {@link Boolean} result on the basis of validation
	 */
	public static boolean isInteger(Object number)
	{
		boolean isValid = true;
		if(!isValid(number))
			isValid = false;
		try
		{
			int amount;
			if(number instanceof String)
				amount = Integer.parseInt((String) number);
		}
		catch(Exception e)
		{
			isValid = false;
		}

		return isValid;
	}

	/**
	 * Validates whether given parameter is integer or not
	 * 
	 * @param number
	 *            parmeter to be validated
	 * @return {@link Boolean} result on the basis of validation
	 */
	public static boolean isFloat(Object number)
	{
		try
		{
			float parseFloat = Float.parseFloat((number.toString()));
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}

	public static Object postRequestWithPayloadAndQueryString(HashMap<String, Object> restApiData) throws JSONException
	{
		HttpHeaders headers = new HttpHeaders();

		if(restApiData.get("httpmethod").equals(HttpMethod.POST))
			headers.setContentType((MediaType) restApiData.get("ContentType"));

		if(restApiData.containsKey("Authorization"))
			headers.set("Authorization", (String) restApiData.get("Authorization"));

		if(restApiData.containsKey("accessToken"))
			headers.set("accessToken", (String) restApiData.get("accessToken"));

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl((String) restApiData.get("url"));

		if(restApiData.containsKey("urlParam"))
		{
			Map<String, Object> urlParam = (Map<String, Object>) restApiData.get("urlParam");
			Set<Entry<String, Object>> dataSet = urlParam.entrySet();
			for(Entry<String, Object> entry : dataSet)
			{
				String key = entry.getKey();
				Object value = entry.getValue();
				builder.queryParam(key, value);
			}
		}

		HttpEntity<?> entity;
		if(!restApiData.containsKey("payload"))
			entity = new HttpEntity<>(headers);
		else
		{
			entity = new HttpEntity<>(restApiData.get("payload"), headers);// postPayload
		}

		URI re = builder.build().encode().toUri();
		//		System.out.println(re);
		RestTemplate restTemplate = new RestTemplate();

		HttpMethod method = (HttpMethod) restApiData.get("httpmethod");

		LOGGER.error("Resp Entity : " + re + " Method : " + method + " Req Entity : " + entity);
		HttpEntity<String> response = restTemplate.exchange(re, method, entity, String.class);
		LOGGER.error(response.toString());

		return response.getBody();
	}
}
