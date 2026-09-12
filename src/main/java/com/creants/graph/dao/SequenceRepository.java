package com.creants.graph.dao;

/**
 * @author LamHM
 *
 */
public interface SequenceRepository {
	long getNextSequenceId(String key);

	void createSequenceDocument(String documentName, long defaultValue);
}
