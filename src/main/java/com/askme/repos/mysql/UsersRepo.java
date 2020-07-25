
package main.java.com.askme.repos.mysql;

import org.springframework.data.jpa.repository.JpaRepository;

import main.java.com.askme.dao.mysql.UsersDAO;

public interface UsersRepo extends JpaRepository<UsersDAO, Integer>
{

	UsersDAO findById(int id);

	UsersDAO findByUserId(int userId);

	UsersDAO findByPhoneNo(long phone);

}
