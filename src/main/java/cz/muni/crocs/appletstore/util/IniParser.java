package cz.muni.crocs.appletstore.util;
import java.io.IOException;

/**
 * Parser class that focuses only on one header inside INI file:
 * simplified, as we are concerted about one card only at time.
 * The header is implicitly used, given in constructor.
 * @author Jiří Horák
 * @version 1.0
 */
public interface IniParser {

    /**
     * Get value of INI under header specified in constructor
     * @param key key to get
     * @return value assigned to the key given
     */
    String getValue(String key);

    /**
     * Add value of INI under header specified in constructor
     * @param key key to modify
     * @param value value to insert
     * @return this instance for builder pattern
     */
    IniParser addValue(String key, String value);

    /**
     * Add value of INI under header specified in constructor
     * @param key key to modify
     * @param value value to insert
     * @return this instance for builder pattern
     */
    IniParser addValue(String key, byte[] value);

    /**
     * Save all changes. Without calling this method, all changes
     * made using methods above are discarded.
     */
    void store() throws IOException;

    /**
     * Check header presence
     * @return true if header, given in constructor, present
     */
    boolean isHeaderPresent() ;
}
