
package main.java.com.askme.repos.mysql;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import main.java.com.askme.dao.mysql.UserTopicsDAO;

public interface UserTopicsRepo extends JpaRepository<UserTopicsDAO, Integer>
{
	List<UserTopicsDAO> findByUserId(int userId);

	List<UserTopicsDAO> findByUserIdAndIsSubscribed(int userId, boolean isSubscribed);

	List<UserTopicsDAO> findByUserIdAndTopicId(int userId, int topicId);

	List<UserTopicsDAO> findByUserIdAndTopicIdIn(int userId, List<Integer> topicIds);

	@Query(value = "select id from user_topics where user_id=?1", nativeQuery = true)
	List<Integer> findAllByUserId(int userId);
}
