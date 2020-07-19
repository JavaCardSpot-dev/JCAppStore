package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.action.applet.Applets;
import cz.muni.crocs.appletstore.action.applet.JCMemory;
import cz.muni.crocs.appletstore.iface.OnEventCallBack;

import java.awt.event.MouseEvent;
import java.util.concurrent.TimeUnit;

/**
 * Getting the free card memory action
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class FreeMemoryAction extends CardAbstractAction<Void, byte[]> {

    public FreeMemoryAction(OnEventCallBack<Void, byte[]> call) {
        super(call);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        execute(() -> Applets.JCMEMORY.performDefault(), "JCMemory.getSystemInfo() failed",
                textSrc.getString("E_could_not_get_memory"), 10, TimeUnit.SECONDS);
    }
}
