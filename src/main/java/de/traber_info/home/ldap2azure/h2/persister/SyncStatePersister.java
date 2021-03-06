package de.traber_info.home.ldap2azure.h2.persister;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;
import de.traber_info.home.ldap2azure.model.type.SyncState;

/**
 * Custom H2 persister used to convert {@link SyncState} to an database friendly format.
 *
 * @author Oliver Traber
 */
public class SyncStatePersister extends StringType {

    private static final SyncStatePersister singleton = new SyncStatePersister();

    private SyncStatePersister() {
        super(SqlType.STRING, new Class<?>[] { SyncState.class });
    }

    public static SyncStatePersister getSingleton() {
        return singleton;
    }

    /**
     * Convert an {@link SyncState} to it's iso_local_date_time representation for persistence.
     * @param fieldType Type of the SQL field where the object should be persisted.
     * @param javaObject Object that should be persisted.
     * @return String representation of the given {@link SyncState}, or null if the given object was null.
     */
    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        return javaObject.toString().toLowerCase();
    }

    /**
     * Convert an {@link String} back to it's {@link SyncState} representation.
     * @param fieldType Type of the SQL field where the object was persisted.
     * @param sqlArg Object that was persisted.
     * @return Java object representing the given sql type.
     */
    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        return SyncState.forValue((String) sqlArg);
    }

}
