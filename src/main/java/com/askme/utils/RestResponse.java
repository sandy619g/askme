
package main.java.com.askme.utils;

public class RestResponse
{
	//	private int		statusCode	= -1;
	private Object	data;
	private String	responseCode;
	private String	responseMessage;
	private String	timeStamp;
	//private String  path;

	public RestResponse()//Object status, Object data
	{
		this.timeStamp = System.currentTimeMillis() + "";
	}

	/*public int getStatusCode()
	{
		return statusCode;
	}
	
	public void setStatusCode(int statusCode)
	{
		this.statusCode = statusCode;
	}*/

	public Object getData()
	{
		return data;
	}

	public void setData(Object data)
	{
		this.data = data;
	}

	public String getResponseCode()
	{
		return responseCode;
	}

	public void setResponseCode(String responseCode)
	{
		this.responseCode = responseCode;
	}

	public String getResponseMessage()
	{
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage)
	{
		this.responseMessage = responseMessage;
	}

	public String getTimeStamp()
	{
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp)
	{
		this.timeStamp = timeStamp;
	}

	@Override
	public String toString()
	{
		return "RestResponse [data=" + data + ", responseCode=" + responseCode + ", responseMessage=" + responseMessage + ", timeStamp=" + timeStamp + "]";
	}

}