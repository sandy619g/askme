
package main.java.com.askme.dao.mongo;

import java.util.UUID;

import org.json.JSONArray;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.google.gson.Gson;

import lombok.Data;

@Document(collection = "answers")
@Data
public class AnswersDAO
{
	//	@Id
	//	private Object		id;

	@Field("ansUUID")
    @Indexed(unique = true)
	private String		ansUUID;

	private String		ansText				= null;
	private long		createdTime			= -1L;

	@Indexed
	private int			answeredBy			= -1;

	private int			answeredRoleId		= -1;

	@Indexed
	private String		qsUUID				= null;

	private String		qsText				= null;
	private JSONArray	qsTopicIds			= new JSONArray();

	@Indexed
	private int			qsAskedBy			= -1;
	private int			qsAskerRoleId		= -1;

	private long		modifiedTime		= -1L;
	private boolean		isDeleted			= false;

	private int			upvotes				= 0;
	private JSONArray	ansUpvotedBy		= new JSONArray();

	public AnswersDAO()
	{
		super();
	}

	public AnswersDAO(int qsAskedBy, int answeredBy)
	{
		this.ansUUID = UUID.randomUUID().toString();
		this.createdTime = System.currentTimeMillis();
		this.answeredBy = answeredBy;
		this.qsAskedBy = qsAskedBy;
	}

	
}
