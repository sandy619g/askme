
package main.java.com.askme.repos.mongo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import main.java.com.askme.dao.mongo.AnswersDAO;

public interface AnswersRepo extends MongoRepository<AnswersDAO, String>
{
	List<AnswersDAO> findAllByAnsweredBy(int userId);

	AnswersDAO findByAnsUUID(String ansUUID);
}
