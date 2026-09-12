package com.creants.graph.om;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author LamHM
 *
 */
@Document(collection = "sequence")
public class SequenceId {
	@Id
	private String id;

	private long seq;


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public long getSeq() {
		return seq;
	}


	public void setSeq(long seq) {
		this.seq = seq;
	}
}
