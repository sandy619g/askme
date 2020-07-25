
package main.java.com.askme.dao.mysql;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data
@Getter
@Setter
@Table(name = "user_topics", uniqueConstraints = {@UniqueConstraint(columnNames = {"topic_id", "user_id"})})
public class UserTopicsDAO
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int		id				= -1;

	@Column(name = "topic_id")
	private int		topicId			= -1;

	@Column(name = "topic_name")
	private String	topicName		= null;

	@Column(name = "topic_category")
	private String	topicCategory	= null;

	@Column(name = "user_id")
	private int		userId			= -1;

	@Column(name = "user_name")
	private String	userName		= null;

	@Column(name = "created_time")
	private long	createdTime		= -1L;

	@Column(name = "modified_time")
	private long	modifiedTime	= -1L;

	@Column(name = "is_subscribed")
	private boolean	isSubscribed	= false;

	@Column(name = "role_id")
	private int		roleId		= -1;

	public UserTopicsDAO()
	{
		super();
	}

	public UserTopicsDAO(int topicId, String topicName, String topicCategory, int userId, String userName, long createdTime, long modifiedTime, boolean isSubscribed, int roleId)
	{
		this.topicId = topicId;
		this.topicName = topicName;
		this.topicCategory = topicCategory;
		this.userId = userId;
		this.userName = userName;
		this.createdTime = createdTime;
		this.modifiedTime = modifiedTime;
		this.isSubscribed = isSubscribed;
		this.roleId = roleId;
	}

}
