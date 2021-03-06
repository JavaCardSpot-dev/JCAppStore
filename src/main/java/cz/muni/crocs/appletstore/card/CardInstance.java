package cz.muni.crocs.appletstore.card;

import cz.muni.crocs.appletstore.iface.CallableParam;
import cz.muni.crocs.appletstore.iface.ProcessTrackable;
import pro.javacard.AID;
import pro.javacard.gp.GPRegistryEntry;

import javax.smartcardio.ATR;

/**
 * Card instance definition, also visible from outisde of the package, but through manager only
 */
public interface CardInstance {

    /**
     * Get applets on card
     * @return applets info list
     */
    CardInstanceMetaData getCardMetadata();

    /**
     * Return applet info associated with AID given
     * @param aid aid to search for
     * @return AppletInfo or null
     */
    AppletInfo getInfoOf(AID aid);

    /**
     * Get card identifier
     * @return card id
     */
    String getId();

    /**
     * Get card name and id
     * @return card descriptor
     */
    String getDescriptor();

    /**
     * Return card ATR
     * @return card ATR
     */
    ATR getCardATR();

    /**
     * Get life cycle of the card
     * @return int, where value determines card state - OP_READY, LOCKED...
     * as designed by GlobalPlatform specification
     */
    Integer getLifeCycle();

    /**
     * Get the default selected applet AID
     * @return null if not default selected applet, AID otherwise
     */
    AID getDefaultSelected();

    /**
     * Get card name (found in card database or provided by user)
     */
    String getName();

    /**
     * Set custom card name
     */
    void setName(String name) throws LocalizedCardException;

    /**
     * Executes the call for each applet
     * @param kind kind to call the callback for
     * @param call to execute, returns true if continue in loop
     */
    void foreachAppletOf(GPRegistryEntry.Kind kind, CallableParam<Boolean, AppletInfo> call);

    /**
     * Adds and executes given task. The task cannot be assigned
     * if another task is currently active
     * @param task task to execute on background
     * @return true when task assigned
     */
    boolean addTask(ProcessTrackable task);

    /**
     * Check whether a task is running
     * @return true if task running
     */
    boolean isTask();

    /**
     * Check whether the card can use secure-channel
     * @return true if card was authenticated to
     */
    boolean isAuthenticated();
}
