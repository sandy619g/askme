
package main.java.com.askme.dao.mongo;

import java.util.UUID;

import org.json.JSONArray;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.google.gson.Gson;

import lombok.Data;

@Document(collection = "questions")
@Data
public class QuestionsDAO
{
	//	@Id
	//	private Object		id;

	@Indexed(unique = true)
	@Field("qsUUID")
	private String		qsUUID;

	private String		qsText				= null;
	private JSONArray	qsTopicIds			= new JSONArray();
	private JSONArray	qsTopicNames		= new JSONArray();
	private JSONArray	qsTopicCategories	= new JSONArray();
	private long		createdTime			= -1L;
	private long		modifiedTime		= -1L;

	@Indexed
	private int			qsAskedBy			= -1;
	private int			qsAskedRoleId		= -1;

	@Indexed
	private String		recentAnsUUID		= null;

	@Indexed
	private int			recentAnsBy			= -1;

	private int			ansCount			= 0;
	private JSONArray	answers				= new JSONArray();	//JSONObjects Array

	private boolean		isDeleted			= false;

	public QuestionsDAO()
	{
		super();
	}

	public QuestionsDAO(String qsText, int qsAskedBy)
	{
		this.qsUUID = UUID.randomUUID().toString();
		this.createdTime = this.modifiedTime = System.currentTimeMillis();
		this.qsText = qsText;
		this.qsAskedBy = qsAskedBy;
	}

}
