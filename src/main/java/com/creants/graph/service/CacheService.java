package com.creants.graph.service;

import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.RawJsonDocument;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.creants.graph.util.Security;

import rx.Observable;
import rx.functions.Func1;

/**
 * @author LamHa
 *
 */
@Service
public class CacheService implements InitializingBean {
	private static final Logger LOG = LoggerFactory.getLogger(CacheService.class);
	private Cluster cluster;
	private Bucket bucket;

	@Value("${cache.hosts}")
	private String couchbaseHosts;

	@Value("${cache.bucket}")
	private String couchbaseBucket;

	@Value("${cache.pass}")
	private String couchbasePass;

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			LOG.info("---------------- Start CacheService -----------");
			CouchbaseEnvironment env = DefaultCouchbaseEnvironment.builder()
					.connectTimeout((int) TimeUnit.SECONDS.toMillis(45)).kvTimeout(TimeUnit.SECONDS.toMillis(60))
					.computationPoolSize(3).ioPoolSize(3).build();

			cluster = CouchbaseCluster.create(env, couchbaseHosts);
			bucket = cluster.openBucket(couchbaseBucket, couchbasePass);
			if (bucket == null) {
				LOG.error("[ERROR] Cache service can't get bucket");
			}

			LOG.info("---------------- CacheService Started -----------");
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

	}

	public void upsert(String key, String jsonString) {
		upsert(key, 0, jsonString);
	}

	public void login(String token, String data) {
		String encryptMD5 = null;
		try {
			encryptMD5 = Security.encryptMD5(token);
			upsert(encryptMD5, data);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			LOG.error("[ERROR] login fail! token:" + encryptMD5, e);
		}
	}

	public void upsert(String key, int expireSecond, String jsonString) {
		bucket.upsert(RawJsonDocument.create(key, expireSecond, jsonString));
	}

	public String get(String key) {
		RawJsonDocument json = bucket.get(key, RawJsonDocument.class);
		if (json != null) {
			return json.content();
		}

		return null;
	}

	public void delete(String key) {
		bucket.remove(key);
	}

	public List<RawJsonDocument> getBulk(final Collection<String> keys) {
		return Observable.from(keys).flatMap(new Func1<String, Observable<RawJsonDocument>>() {
			@Override
			public Observable<RawJsonDocument> call(String id) {
				return bucket.async().get(id, RawJsonDocument.class);
			}
		}).toList().toBlocking().single();
	}

	public void shutdown() {
		LOG.info("Destroy extension - Shutdown Couchbase");
		if (cluster != null) {
			bucket.close();
			cluster.disconnect();
		}
	}
}
