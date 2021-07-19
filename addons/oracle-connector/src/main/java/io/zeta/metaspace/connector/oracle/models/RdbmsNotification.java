package io.zeta.metaspace.connector.oracle.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

@JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RdbmsNotification extends Notification implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum RdbmsNotificationType {
		/**
		 * create db ,table
		 */
		NEW("n"),
		/**
		 * alter db ,table
		 */
		ALTER("m"),
		/**
		 * drop db ,table
		 */
		DROP("d"),
		/**
		 * insert data
		 */
		CREATE("c"),
		/**
		 * update data
		 */
		UPDATE("u"),
		/**
		 * delete data
		 */
		DELETE("d"),
		/**
		 * ddl
		 */
		DDL("ddl");
		private RdbmsNotificationType(String code) {
			this.code = code;
		}

		private String code;

		private String getCode() {
			return this.code;
		}

		public static RdbmsNotificationType getTypeByCode(String code) {
			if (null == code || "".equals(code)) {
				throw new RuntimeException("rdbms code is empty");
			}
			for (RdbmsNotificationType type : RdbmsNotificationType.values()) {
				if (type.getCode().equalsIgnoreCase(code)) {
					return type;
				}
			}
			throw new RuntimeException("not found RdbmsNotificationType, code = " + code);
		}

	}

	private RdbmsNotificationType type;

	private RdbmsMessage rdbmsMessage;

	public RdbmsNotification() {
		super();
	}

	public RdbmsNotification(RdbmsNotificationType type) {
		super();
		this.type = type;
	}

	public RdbmsNotification(RdbmsNotificationType type, String user) {
		super(user);
		this.type = type;
	}

	public RdbmsNotificationType getType() {
		return type;
	}

	public void setType(RdbmsNotificationType type) {
		this.type = type;
	}

	public RdbmsMessage getRdbmsMessage() {
		return rdbmsMessage;
	}

	public void setRdbmsMessage(RdbmsMessage rdbmsMessage) {
		this.rdbmsMessage = rdbmsMessage;
	}
}
