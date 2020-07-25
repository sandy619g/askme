
package main.java.com.askme.repos.mysql;

import org.springframework.data.jpa.repository.JpaRepository;

import main.java.com.askme.dao.mysql.TopicsDAO;

public interface TopicsRepo extends JpaRepository<TopicsDAO, Integer>
{

}
