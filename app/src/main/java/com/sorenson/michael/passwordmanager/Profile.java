package com.sorenson.michael.passwordmanager;

import java.io.Serializable;
import java.util.UUID;
import java.util.Date;
import java.security.GeneralSecurityException;
import java.math.BigInteger;
import com.lambdaworks.crypto.SCrypt;

import org.json.JSONObject;

public class Profile implements Serializable {
    public static final String SCHEME_SCRYPT_16384_8_1 =
        "scrypt(master\\turl\\tusername,generation,16384,8,1,length)";
    public static final int MIN_MASTER_LENGTH = 1;
    public static final int MAX_MASTER_LENGTH = 128;
    public static final int MIN_URL_LENGTH = 0;
    public static final int MAX_URL_LENGTH = 256;
    public static final int MIN_USERNAME_LENGTH = 0;
    public static final int MAX_USERNAME_LENGTH = 256;
    public static final int MIN_LENGTH = 1;
    public static final int MAX_LENGTH = 32;
    public static final int DEFAULT_LENGTH = 16;
    public static final char MIN_CHAR = 32;
    public static final char MAX_CHAR = 126;

    // main data fields
    public UUID uuid;

    public String scheme;
    public int generation;

    public String title;
    public String url;
    public String username;
    public int length;

    public boolean lower;
    public boolean upper;
    public boolean digits;
    public boolean punctuation;
    public boolean spaces;
    public String include;
    public String exclude;
    public Date modifiedAt;

    // make an empty profile
    public Profile() {
        uuid = UUID.randomUUID();
        scheme = SCHEME_SCRYPT_16384_8_1;
        generation = 1;
        title = "";
        url = "";
        username = "";
        length = DEFAULT_LENGTH;
        lower = true;
        upper = true;
        digits = true;
        punctuation = true;
        spaces = false;
        include = "";
        exclude = "";
        modifiedAt = new Date();
    }

    // generate a password from this profile
    public String generate(String master) throws GeneralSecurityException {
        // make sure this is a recognized scheme
        if (!scheme.equals(SCHEME_SCRYPT_16384_8_1)) {
            throw new IllegalArgumentException("Unknown scheme: I only recognize " + SCHEME_SCRYPT_16384_8_1);
        }

        // compute the password
        // validate the parts
        if (master.length() < MIN_MASTER_LENGTH || master.length() > MAX_MASTER_LENGTH)
            throw new IllegalArgumentException("master password too short or too long");
        for (int i = 0; i < master.length(); i++)
            if (master.charAt(i) < MIN_CHAR || master.charAt(i) > MAX_CHAR)
                throw new IllegalArgumentException("master password contains an illegal character");
        if (url.length() < MIN_URL_LENGTH || url.length() > MAX_URL_LENGTH)
            throw new IllegalArgumentException("url too short or too long");
        for (int i = 0; i < url.length(); i++)
            if (url.charAt(i) < MIN_CHAR || url.charAt(i) > MAX_CHAR)
                throw new IllegalArgumentException("url contains an illegal character");
        url = url.toLowerCase();
        if (username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH)
            throw new IllegalArgumentException("username too short or too long");
        for (int i = 0; i < username.length(); i++)
            if (username.charAt(i) < MIN_CHAR || username.charAt(i) > MAX_CHAR)
                throw new IllegalArgumentException("username/email contains an illegal character");
        username = username.toLowerCase();

        String passwordPart = master + "\t" + url + "\t" + username;
        String saltPart = String.valueOf(generation);
        byte[] raw = SCrypt.scrypt(passwordPart.getBytes(), saltPart.getBytes(), 16384, 8, 1, length);

        // gather the character set
        StringBuilder chars = new StringBuilder();
        for (char ch = MIN_CHAR; ch <= MAX_CHAR; ch++) {
            boolean use = false;

            // is this character one the profile calls for?
            if (Character.isWhitespace(ch))
                use = spaces;
            else if (Character.isLowerCase(ch))
                use = lower;
            else if (Character.isUpperCase(ch))
                use = upper;
            else if (Character.isDigit(ch))
                use = digits;
            else
                use = punctuation;

            // is this a special case?
            if (include.indexOf(ch) >= 0)
                use = true;
            if (exclude.indexOf(ch) >= 0)
                use = false;

            if (use)
                chars.append(ch);
        }

        // generate the requested number of characters
        RandomChar source = new RandomChar(raw, chars);
        StringBuilder out = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            out.append(source.nextChar());

        return out.toString();
    }

    private class RandomChar {
        private BigInteger pool;
        private BigInteger poolSize;
        private BigInteger alphabetSize;
        private StringBuilder alphabet;

        public RandomChar(byte[] raw, StringBuilder chars) {
            pool = new BigInteger(1, raw);
            poolSize = BigInteger.ZERO.setBit(raw.length * 8);
            alphabet = chars;
            alphabetSize = BigInteger.valueOf(alphabet.length());
        }

        public char nextChar() {
            BigInteger base = pool.multiply(alphabetSize);
            BigInteger[] qr = base.divideAndRemainder(poolSize);
            pool = qr[1];
            int which = qr[0].intValue();
            return alphabet.charAt(which);
        }
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        try {
            if (generation != 1) {
                result.put("generation", String.valueOf(generation));
            }
            if(!title.equals("")) {
                result.put("title", title);
            }
            if(!url.equals("")) {
                result.put("url", url);
            }
            if(!username.equals("")) {
                result.put("username", username);
            }
            if(length != DEFAULT_LENGTH) {
                result.put("length", String.valueOf(length));
            }
            if(!lower) {
                result.put("lower", String.valueOf(false));
            }
            if(!upper) {
                result.put("upper", String.valueOf(false));
            }
            if(!digits) {
                result.put("digits", String.valueOf(false));
            }
            if(!punctuation) {
                result.put("punctuation", String.valueOf(false));
            }
            if(spaces) {
                result.put("spaces", String.valueOf(true));
            }
            if(!include.equals("")) {
                result.put("include", include);
            }
            if(!exclude.equals("")) {
                result.put("exclude", exclude);
            }
        } catch(Exception ex) {
            System.out.println("Your JSON is bad and you should feel bad");
        }
        return result;
    }

    public void fromJson(JSONObject input) {
        uuid = UUID.fromString(input.optString("uuid"));
        generation = input.optInt("generation", 1);
        title = input.optString("title", "");
        url = input.optString("url", "");
        username = input.optString("username", "");
        length = input.optInt("generation", DEFAULT_LENGTH);
        lower = input.optBoolean("lower", true);
        upper = input.optBoolean("upper", true);
        digits = input.optBoolean("digits", true);
        punctuation = input.optBoolean("punctuation", true);
        spaces = input.optBoolean("punctuation", false);
        include = input.optString("include", "");
        exclude = input.optString("exclude", "");
        try {
            String modified = input.optString("modified_at");
            modifiedAt = Util.parseRFC3339Date(modified);
        } catch (Exception ex) {
            modifiedAt = new Date();
        }
    }
}
