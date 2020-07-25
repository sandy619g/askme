
package main.java.com.askme.dao.mysql;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import main.java.com.askme.utils.NotificationType;

@Entity
@Table(name = "notifications")
@Data
public class NotificationsDAO
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int					id;

	@Column(name = "title")
	private String				title			= null;

	@Column(name = "type")
	private NotificationType	type			= null;

	@Column(name = "user_id")
	private int					userId			= -1;

	@Column(name = "qs_uuid")
	private String				qsUUID			= null;

	@Column(name = "ans_uuid")
	private String				ansUUID			= null;

	@Column(name = "created_time")
	private long				createdTime		= -1L;

	@Column(name = "modified_time")
	private long				modifiedTime	= -1L;

	@Column(name = "is_read")
	private boolean				isRead			= false;

	public NotificationsDAO()
	{
		super();
	}

	public NotificationsDAO(int userId, NotificationType type)
	{
		this.createdTime = this.modifiedTime = System.currentTimeMillis();
		this.userId = userId;
		this.type = type;
		this.title = NotificationType.getNotificationTitle(type);
	}
}
