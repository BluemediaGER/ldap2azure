package de.traber_info.home.ldap2azure.rest.model.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum containing possible permission levels for clients.
 *
 * @author Oliver Traber
 */
public enum Permission {

    READ,
    READ_WRITE;

    /**
     * Map used to convert enum values to and from strings for JSON serialisation and deserialization.
     */
    private static Map<String, Permission> typeMap = new HashMap<>();

    static {
        typeMap.put("read", READ);
        typeMap.put("read_write", READ_WRITE);
    }

    /**
     * Get the enum value that represents the given string. The value is determined by the type map.
     * @param value String value you want to get the enum value for.
     * @return Enum value that represents the given string, or null if the string could not be matched to any value.
     */
    @JsonCreator
    public static Permission forValue(String value) {
        return typeMap.get(value.toLowerCase());
    }

    /**
     * Get the lower case string representation of the enum value.
     * @return Lower case string representation of the enum value
     */
    @JsonValue
    public String toValue() {
        for (Map.Entry<String, Permission> entry : typeMap.entrySet()) {
            if (entry.getValue() == this)
                return entry.getKey();
        }
        return null;
    }

}
