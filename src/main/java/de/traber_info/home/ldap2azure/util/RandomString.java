package de.traber_info.home.ldap2azure.util;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Utility used to generate random strings of an specified length.
 *
 * @author Oliver Traber
 */
public class RandomString {

    /** Upper case letters used in the string */
    public static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /** Lower case letters used in the string */
    public static final String lower = upper.toLowerCase(Locale.ROOT);

    /** Digits used in the string */
    public static final String digits = "0123456789";

    /** Special characters used in the string */
    public static final String special = "!ยง$%&/()=?,.-_;#+*";

    /** All possible charachters used in the string */
    public static final String alphanum = upper + lower + digits + special;

    /** Source of randomness for the generation */
    private final Random random;

    /** Holder for the characters that are used for the generation */
    private final char[] symbols;

    /** Buffer for the generation process */
    private final char[] buf;

    /**
     * Create an random string generator.
     */
    public RandomString(int length, Random random, String symbols) {
        if (length < 1) throw new IllegalArgumentException();
        if (symbols.length() < 2) throw new IllegalArgumentException();
        this.random = Objects.requireNonNull(random);
        this.symbols = symbols.toCharArray();
        this.buf = new char[length];
    }

    /**
     * Create an random string generator.
     */
    public RandomString(int length, Random random) {
        this(length, random, alphanum);
    }

    /**
     * Create an random strings from a secure generator.
     */
    public RandomString(int length) {
        this(length, new SecureRandom());
    }

    /**
     * reate an random strings from a secure generator using the default 21 character length.
     */
    public RandomString() {
        this(21);
    }

    /**
     * Generate a random string.
     */
    public String nextString() {
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }

}
