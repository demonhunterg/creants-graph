package com.creants.graph.dao;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.creants.graph.om.User;

/**
 * @author LamHM
 *
 */
@Repository
public interface AccountRepository extends MongoRepository<User, Long> {
	@Query("{'provider' : ?0, 'clientId' : ?1}")
	User getByProviderAndClientId(String provider, String clientId);


	@Query("{'id' : ?0}")
	User getUserInfo(long userId);


	@Query("{username:?0, password:?1}")
	User getUserInfo(String username, String password);
}
