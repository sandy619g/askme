
package main.java.com.askme.dao.mysql;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.mongodb.core.index.Indexed;

import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class UsersDAO
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer	id;

	@Column(name = "user_id")
	private int		userId		= -1;

	@Column(name = "name")
	private String	name			= null;

	@Column(name = "role_id")
	private int		roleId		= -1;

	@Column(name = "role_name")
	private String	roleName	= null;

	@Indexed(unique = true)
	@Column(name = "phone_number")
	private long	phoneNo			= -1l;

	@Column(name = "email_id")
	private String	emailId			= null;

	@Column(name = "pincode")
	private int		pincode			= -1;

	@Column(name = "upvotes")
	private int		upvotes			= 0;

	@Column(name = "rank")
	private String	rank			= null;

	@Column(name = "created_time")
	private long	createdTime		= -1;

	@Column(name = "standard")
	private String	standard		= null;

	@Column(name = "section")
	private String	section			= null;

	@Column(name = "date_of_birth")
	private String	dateOfBirth		= null;

	@Column(name = "modified_time")
	private long	modifiedTime	= -1l;

	public UsersDAO()
	{
		this.createdTime = System.currentTimeMillis();
	}

	
}
