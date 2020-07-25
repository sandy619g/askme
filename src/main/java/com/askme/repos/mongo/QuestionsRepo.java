
package main.java.com.askme.repos.mongo;

import java.util.List;

import org.json.JSONArray;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import main.java.com.askme.dao.mongo.QuestionsDAO;

public interface QuestionsRepo extends MongoRepository<QuestionsDAO, String>
{
	List<QuestionsDAO> findByQsAskedBy(int userId);

	QuestionsDAO findByQsUUID(String qsUUID);

	List<QuestionsDAO> findAllByQsTopicIdsIn(List<Integer> topicIds, Pageable page);

	List<QuestionsDAO> findAllByQsTopicIdsLike(JSONArray topicIds, Pageable page);

}
