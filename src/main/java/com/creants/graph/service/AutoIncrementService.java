package com.creants.graph.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.creants.graph.dao.SequenceRepository;

/**
 * @author LamHM
 *
 */
@Service
public class AutoIncrementService implements InitializingBean {
	public static final String ACCOUNT_ID = "account_id";

	@Autowired
	private SequenceRepository sequenceRepository;


	@Override
	public void afterPropertiesSet() throws Exception {
		sequenceRepository.createSequenceDocument(ACCOUNT_ID, 300);

	}


	public long genAccountId() {
		return sequenceRepository.getNextSequenceId(ACCOUNT_ID);
	}

}
