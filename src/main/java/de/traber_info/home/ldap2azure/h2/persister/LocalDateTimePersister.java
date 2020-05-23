package de.traber_info.home.ldap2azure.h2.persister;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom H2 persister used to convert {@link LocalDateTime} to an database friendly format.
 *
 * @author Oliver Traber
 */
public class LocalDateTimePersister extends StringType {

    private static final LocalDateTimePersister singleton = new LocalDateTimePersister();

    private LocalDateTimePersister() {
        super(SqlType.STRING, new Class<?>[] { LocalDateTime.class });
    }

    public static LocalDateTimePersister getSingleton() {
        return singleton;
    }

    /**
     * Convert an {@link LocalDateTime} to it's iso_local_date_time representation for persistence.
     * @param fieldType Type of the SQL field where the object should be persisted.
     * @param javaObject Object that should be persisted.
     * @return String representation of the given {@link LocalDateTime}, or null if the given object was null.
     */
    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        LocalDateTime dateTime = (LocalDateTime) javaObject;
        if (dateTime == null) {
            return null;
        } else {
            return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }

    /**
     * Convert an {@link String} back to it's {@link LocalDateTime} representation.
     * @param fieldType Type of the SQL field where the object was persisted.
     * @param sqlArg Object that was persisted.
     * @return Java object representing the given sql type.
     */
    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        return LocalDateTime.parse((String) sqlArg, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

}
