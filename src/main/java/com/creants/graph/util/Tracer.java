package com.creants.graph.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author LamHa
 *
 */
public class Tracer {
	private static final Logger CREANTS_LOG = LoggerFactory.getLogger("CreantsLogger");

	public static void debug(Class<?> clazz, Object... msgs) {
		if (CREANTS_LOG.isDebugEnabled()) {
			CREANTS_LOG.debug(getTraceMessage(clazz, msgs));
		}
	}

	public static void info(Class<?> clazz, Object... msgs) {
		CREANTS_LOG.info(getTraceMessage(clazz, msgs));
	}

	/**
	 * Log thông tin lỗi
	 * 
	 * @param clazz
	 *            class nào xảy ra lỗi
	 * @param msgs
	 *            thông tin kèm theo lỗi - nên kèm theo tên hàm
	 */
	public static void error(Class<?> clazz, Object... msgs) {
		CREANTS_LOG.error(getTraceMessage(clazz, msgs));
	}

	/**
	 * Log thông tin cảnh báo
	 * 
	 * @param clazz
	 *            class nào xảy ra lỗi
	 * @param msgs
	 *            thông tin kèm theo lỗi - nên kèm theo tên hàm
	 */
	public static void warn(Class<?> clazz, Object... msgs) {
		CREANTS_LOG.warn(getTraceMessage(clazz, msgs));
	}

	private static String getTraceMessage(Class<?> clazz, Object[] msgs) {
		StringBuilder traceMsg = new StringBuilder().append("{").append(clazz.getSimpleName()).append("}: ");
		Object[] arrayOfObject;
		int j = (arrayOfObject = msgs).length;
		for (int i = 0; i < j; i++) {
			traceMsg.append(arrayOfObject[i].toString()).append(" ");
		}

		return traceMsg.toString();
	}
}
