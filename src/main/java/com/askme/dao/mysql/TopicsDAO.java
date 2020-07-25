
package main.java.com.askme.dao.mysql;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "topics")
@Data
public class TopicsDAO
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int		id				= -1;

	@Column(name = "topic_name")
	private String	topicName		= null;

	@Column(name = "topic_category")
	private String	topicCategory	= null;

	@Column(name = "created_time")
	private long	createdTime		= -1l;

	public TopicsDAO()
	{
		this.createdTime = System.currentTimeMillis();
	}

	
}
