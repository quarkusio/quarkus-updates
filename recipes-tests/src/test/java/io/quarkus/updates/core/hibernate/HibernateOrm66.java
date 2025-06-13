package io.quarkus.updates.core.hibernate;

import org.intellij.lang.annotations.Language;

public class HibernateOrm66 {
	private HibernateOrm66() {
	}

	public static String[] api() {
		@Language("java")
		String[] apis = new String[] { """
			package jakarta.persistence;
			@Documented
			@Target(TYPE)
			@Retention(RUNTIME)
			public @interface Entity {
			}
			""", """
			package jakarta.persistence;
			@Target({METHOD, FIELD})
			@Retention(RUNTIME)
			public @interface Id {}
			""", """
			package jakarta.persistence;
			@Target({METHOD, FIELD})
			@Retention(RUNTIME)
			public @interface ManyToOne {
			}
			""", """
			package jakarta.persistence;
			import jakarta.persistence.LockModeType;
			public interface EntityManager extends AutoCloseable {
				void persist(Object entity);
				<T> T merge(T entity);
				void refresh(Object object);
				void refresh(Object entity, Map<String, Object> properties);
				void refresh(Object entity, LockModeType lockMode);
				void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties);
				void remove(Object entity);
				<T> T find(Class<T> entityClass, Object primaryKey);
				<T> T find(Class<T> entityClass, Object primaryKey,
						Map<String, Object> properties);
				<T> T find(Class<T> entityClass, Object primaryKey,
						LockModeType lockMode);
				<T> T find(Class<T> entityClass, Object primaryKey,
						LockModeType lockMode,
						Map<String, Object> properties);
				<T> T getReference(Class<T> entityClass, Object primaryKey);
				<T> T getReference(T entity);
			}
			""", """
			package jakarta.persistence;
			public enum LockModeType implements FindOption, RefreshOption {
				READ,
				WRITE,
				OPTIMISTIC,
				OPTIMISTIC_FORCE_INCREMENT,
				PESSIMISTIC_READ,
				PESSIMISTIC_WRITE,
				PESSIMISTIC_FORCE_INCREMENT,
				NONE
			}
			""", """
			package org.hibernate;
			import jakarta.persistence.EntityManager;
			import org.hibernate.LockMode;
			import org.hibernate.LockOptions;
			public interface Session extends EntityManager {
				@Deprecated(since = "6.0")
				<T> T load(Class<T> theClass, Object id);
				@Deprecated(since = "6.0")
				Object load(String entityName, Object id);
				@Deprecated(since = "6.0")
				<T> T load(Class<T> theClass, Object id, LockMode lockMode);
				@Deprecated(since = "6.0")
				Object load(String entityName, Object id, LockOptions lockOptions);
				@Deprecated(since = "6.0")
				<T> T load(Class<T> theClass, Object id, LockOptions lockOptions);
				@Deprecated(since = "6.0")
				Object load(String entityName, Object id, LockMode lockMode);
				@Override
				<T> T getReference(Class<T> entityType, Object id);
				Object getReference(String entityName, Object id);
				@Override
				<T> T getReference(T object);
				void refresh(Object object, LockOptions lockOptions);
				void refresh(Object object, LockMode lockMode);
				void refresh(Object object, LockOptions lockOptions);
				@Deprecated(since = "6.0")
				void refresh(String entityName, Object object);
				@Deprecated(since = "6.0")
				void refresh(String entityName, Object object, LockOptions lockOptions);
				<T> T get(Class<T> entityType, Object id);
				<T> T get(Class<T> entityType, Object id, LockMode lockMode);
				Object get(String entityName, Object id);
				Object get(String entityName, Object id, LockMode lockMode);
				<T> T get(Class<T> entityType, Object id, LockOptions lockOptions);
				Object get(String entityName, Object id, LockOptions lockOptions);
				Object save(Object object);
				Object save(String entityName, Object object);
				void saveOrUpdate(Object object);
				void saveOrUpdate(String entityName, Object object);
				void update(Object object);
				void update(String entityName, Object object);
				@Deprecated(since = "6.0")
				void delete(Object object);
				@Deprecated(since = "6.0")
				void delete(String entityName, Object object);
			}
			""", """
			package org.hibernate;
			public enum LockMode implements FindOption, RefreshOption {
				NONE,
				READ,
				OPTIMISTIC,
				OPTIMISTIC_FORCE_INCREMENT,
				WRITE,
				PESSIMISTIC_READ,
				PESSIMISTIC_WRITE,
				PESSIMISTIC_FORCE_INCREMENT,
				UPGRADE_NOWAIT,
				UPGRADE_SKIPLOCKED
			}
			""", """
			package org.hibernate;
			public class LockOptions implements Serializable {
				public LockOptions() {
				}
				public LockOptions(LockMode lockMode) {
				}
				public LockOptions(LockMode lockMode, Timeout timeout) {
				}
				public LockOptions(LockMode lockMode, Timeout timeout, PessimisticLockScope scope) {
				}
				protected LockOptions(boolean immutable, LockMode lockMode) {
				}
				public LockOptions(LockMode lockMode, int timeout) {
					this( lockMode, Timeouts.interpretMilliSeconds( timeout ) );
				}
				public LockOptions(LockMode lockMode, int timeout, PessimisticLockScope scope) {
					this( lockMode, Timeouts.interpretMilliSeconds( timeout ), scope );
				}
				public static final LockOptions NONE = new LockOptions( true, LockMode.NONE );
				public static final LockOptions READ = new LockOptions( true, LockMode.READ );
				static final LockOptions OPTIMISTIC = new LockOptions( true, LockMode.OPTIMISTIC );
				static final LockOptions OPTIMISTIC_FORCE_INCREMENT = new LockOptions( true, LockMode.OPTIMISTIC_FORCE_INCREMENT );
				static final LockOptions PESSIMISTIC_READ = new LockOptions( true, LockMode.PESSIMISTIC_READ );
				static final LockOptions PESSIMISTIC_WRITE = new LockOptions( true, LockMode.PESSIMISTIC_WRITE );
				static final LockOptions PESSIMISTIC_FORCE_INCREMENT = new LockOptions( true, LockMode.PESSIMISTIC_FORCE_INCREMENT );
				static final LockOptions UPGRADE_NOWAIT = new LockOptions( true, LockMode.UPGRADE_NOWAIT );
				static final LockOptions UPGRADE_SKIPLOCKED = new LockOptions( true, LockMode.UPGRADE_SKIPLOCKED );
				public static final LockOptions UPGRADE = PESSIMISTIC_WRITE;
				public static final int NO_WAIT = Timeouts.NO_WAIT_MILLI;
				public static final int WAIT_FOREVER = Timeouts.WAIT_FOREVER_MILLI;
				@Deprecated(since = "6.2", forRemoval = true)
				public static final int SKIP_LOCKED = -2;
			}
			""", """
			package org.hibernate.annotations;
			@Target({METHOD, FIELD})
			@Retention(RUNTIME)
			public @interface Cascade {
				CascadeType[] value();
			}
			""", """
			package org.hibernate.annotations;
			public enum CascadeType {
				ALL,
				PERSIST,
				MERGE,
				REMOVE,
				REFRESH,
				DETACH,
				LOCK,
				DELETE,
				SAVE_UPDATE,
				REPLICATE,
				DELETE_ORPHAN
			}
			"""
		};
		return apis;
	}
}
