
package main.java.com.askme.repos.mysql;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import main.java.com.askme.dao.mysql.NotificationsDAO;

public interface NotificationsRepo extends JpaRepository<NotificationsDAO, Integer>
{
	List<NotificationsDAO> findByUserId(int userId);

	List<NotificationsDAO> findAllByUserIdOrderByCreatedTimeDesc(int userId);

	List<NotificationsDAO> findByUserIdAndIsRead(int userId, boolean isRead);

	List<NotificationsDAO> findByIdIn(List<Integer> ids);
}
