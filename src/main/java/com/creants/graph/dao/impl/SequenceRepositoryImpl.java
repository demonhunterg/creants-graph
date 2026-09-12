package com.creants.graph.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.creants.graph.dao.SequenceRepository;
import com.creants.graph.om.SequenceId;

/**
 * 
 * @author LamHM
 *
 */
@Repository
public class SequenceRepositoryImpl implements SequenceRepository {
	@Autowired
	private MongoOperations mongoOperations;


	@Override
	public void createSequenceDocument(String documentName, long defaultValue) {
		Query query = new Query(Criteria.where("_id").is(documentName));
		SequenceId sequenceId = mongoOperations.findOne(query, SequenceId.class);
		if (sequenceId == null) {
			SequenceId sequence = new SequenceId();
			sequence.setId(documentName);
			sequence.setSeq(defaultValue);
			mongoOperations.save(sequence);
		}

	}


	@Override
	public long getNextSequenceId(String key) {
		Query query = new Query(Criteria.where("_id").is(key));
		Update update = new Update().inc("seq", 1);
		FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
		SequenceId sequenceId = mongoOperations.findAndModify(query, update, options, SequenceId.class);
		return sequenceId.getSeq();
	}


	public long setDefaultValue(String documentName, long value) {
		Query query = new Query(Criteria.where("_id").is(documentName));
		Update update = new Update().set("seq", value);
		FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
		SequenceId sequenceId = mongoOperations.findAndModify(query, update, options, SequenceId.class);
		return sequenceId.getSeq();
	}

}
