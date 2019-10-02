package cz.muni.crocs.appletstore.card;

import apdu4j.APDUBIBO;
import apdu4j.CardChannelBIBO;
import apdu4j.TerminalManager;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.command.*;
import cz.muni.crocs.appletstore.util.LogOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import pro.javacard.AID;
import pro.javacard.CAPFile;
import pro.javacard.gp.GPException;
import pro.javacard.gp.GPRegistryEntry;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

/**
 * Manager providing all functionality over card
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CardManagerImpl implements CardManager {

    private static final Logger logger = LoggerFactory.getLogger(CardManagerImpl.class);
    private static final LogOutputStream loggerStream = new LogOutputStream(logger, Level.INFO);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private Terminals terminals = new Terminals();

    private CardInstance card;
    private String lastCardId = textSrc.getString("no_last_card");
    private AID selectedAID = null;
    private AID lastInstalled = null;
    private volatile boolean busy = false;

    @Override
    public void switchApplet(AID aid) {
        if (card == null) {
            selectedAID = null;
            return;
        }

        if (aid.equals(selectedAID)) {
            selectedAID = null;
            aid = null;
        }

        if (card.getApplets() == null)
            return;
        this.selectedAID = aid;
    }

    @Override
    public boolean isAppletSelected() {
        return selectedAID != null;
    }

    @Override
    public boolean isAppletSelected(AID applet) {
        return applet != null && applet.equals(selectedAID);
    }

    @Override
    public Terminals.TerminalState getTerminalState() {
        return terminals.getState();
    }

    @Override
    public Set<String> getTerminals() {
        return terminals.getTerminals().keySet();
    }

    @Override
    public CardTerminal getSelectedTerminal() {
        return terminals.getTerminal();
    }

    @Override
    public String getSelectedTerminalName() {
        return terminals.getSelectedReaderName();
    }

    @Override
    public void setSelectedTerminal(String name) {
        terminals.selectTerminal(name);
    }

    @Override
    public List<AppletInfo> getInstalledApplets() {
        return card == null ? null : Collections.unmodifiableList(card.getApplets());
    }

    @Override
    public String getCardId() {
        return card == null ? "" : card.getId();
    }

    @Override
    public String getCardDescriptor() {
        return card == null ? "" : card.getName() + "  " + card.getId();
    }

    @Override
    public String getLastCardDescriptor() {
        return lastCardId;
    }

    @Override
    public synchronized int needsCardRefresh() {
        while (busy) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.info("The card was busy when needsCardRefresh() called, waiting interrupted.");
                Thread.currentThread().interrupt();
            }
        }
        busy = true;

        try {
            return terminals.checkTerminals();
        } finally {
            busy = false;
            notifyAll();
        }
    }

    @Override
    public synchronized void loadCard() throws LocalizedCardException {
        while (busy) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.info("The card was busy when loadCard() called, waiting interrupted.");
                Thread.currentThread().interrupt();
            }
        }
        busy = true;
        lastInstalled = null;
        try {
            if (terminals.getState() == Terminals.TerminalState.OK) {
                CardDetails details = getCardDetails(terminals.getTerminal());
                lastCardId = CardDetails.getId(details);
                card = new CardInstance(details, terminals.getTerminal());
            } else {
                card = null;
            }
        } catch (LocalizedCardException ex) {
            card = null;
            throw ex;
        } catch (Exception e) {
            card = null;
            throw new LocalizedCardException(e.getMessage(), "unable_to_translate", e);
        } finally {
            busy = false;
            notifyAll();
        }
    }

    @Override
    public Integer getCardLifeCycle() {
        if (card == null)
            return 0;
        List<AppletInfo> infoList = card.getApplets();
        if (infoList == null)
            return 0;

        for (AppletInfo info : infoList) {
            if (info.getKind() == GPRegistryEntry.Kind.IssuerSecurityDomain) {
                return info.getLifecycle();
            }
        }
        throw new Error("Should not end here.");
    }

    @Override
    public void setLastAppletInstalled(AID aid) {
        lastInstalled = aid;
    }

    @Override
    public AID getLastAppletInstalledAid() {
        return lastInstalled;
    }

    @Override
    public synchronized void install(File file, InstallOpts data) throws LocalizedCardException, IOException {
        if (!file.exists()) throw new LocalizedCardException(textSrc.getString("E_install_no_file_1") +
                file.getAbsolutePath() + textSrc.getString("E_install_no_file_2"));

        CAPFile capFile;
        try (FileInputStream fin = new FileInputStream(file)) {
            capFile = CAPFile.fromStream(fin);
        }

        try {
            installImpl(capFile, data);
        } catch (CardException e) {
            e.printStackTrace();
            refreshCard();
            throw new LocalizedCardException(e.getMessage(), "unable_to_translate", e);
        } catch (LocalizedCardException e) {
            refreshCard();
            throw e;
        }
    }

    @Override
    public synchronized void install(final CAPFile file, InstallOpts data) throws LocalizedCardException {
        try {
            installImpl(file, data);
        } catch (CardException e) {
            e.printStackTrace();
            refreshCard();
            throw new LocalizedCardException(e.getMessage(), "unable_to_translate", e);
        } catch (LocalizedCardException e) {
            refreshCard();
            throw e;
        }
    }

    private void saveData(final CAPFile file, final InstallOpts data) throws LocalizedCardException {
        AppletInfo info = data.getInfo();
        //now we rewrite the default aid as custom aid that was used
        info.setAID(data.getCustomAID());
        List<AppletInfo> appletInfoList = card.getApplets();
        //add applet
        appletInfoList.add(info);
        //add package instance, donst save image as the package wont be distinguishable from applet
        appletInfoList.add(new AppletInfo(info.getName(), null, info.getVersion(), info.getAuthor(),
                info.getSdk(), file.getPackageAID().toString(), KeysPresence.NO_KEYS));
        AppletSerializer<List<AppletInfo>> toSave = new AppletSerializerImpl();
        toSave.serialize(appletInfoList, new File(Config.APP_DATA_DIR + Config.SEP + card.getId()));
    }

    @Override
    public synchronized void uninstall(AppletInfo nfo, boolean force) throws LocalizedCardException {
        if (card == null) {
            throw new LocalizedCardException("No card recognized.", "no_card");
        }

        while (busy) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.info("The card was busy when uninstall() called, waiting interrupted.");
                Thread.currentThread().interrupt();
            }
        }
        busy = true;

        try {
            Delete delete = new Delete(nfo, force);
            ListContents contents = new ListContents();
            card.executeCommands(delete, contents);
            card.setApplets(contents.getResult());
            selectedAID = null;
        } catch (CardException e) {
            refreshCard();
            throw new LocalizedCardException(e.getMessage(), "unable_to_translate", e);
        } finally {
            busy = false;
            notifyAll();
        }
    }

    @Override
    public synchronized void sendApdu(String AID) throws LocalizedCardException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Performs the only card insecure-channel use (e.g. GET)
     * to get data from inserted card
     */
    private CardDetails getCardDetails(CardTerminal terminal) throws CardException, LocalizedCardException, IOException {
        Card card = null;
        APDUBIBO channel = null;

        try {
            card = terminal.connect("*");
            card.beginExclusive();
            channel = CardChannelBIBO.getBIBO(card.getBasicChannel());
        } catch (CardException e) {
            if (card != null) card.endExclusive();
            throw new LocalizedCardException("Could not connect to selected reader: " +
                    TerminalManager.getExceptionMessage(e), "E_connect_fail");
        }

        GetDetails command = new GetDetails(channel);
        command.execute();
        card.endExclusive();
        card.disconnect(false);

        CardDetails details = command.getOuput();
        details.setAtr(card.getATR());
        return details;
    }

    private synchronized void installImpl(final CAPFile file, InstallOpts data) throws CardException, LocalizedCardException {
        if (card == null) {
            throw new LocalizedCardException("No card recognized.", "no_card");
        }

        while (busy) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.info("The card was busy when install() called, waiting interrupted.");
                Thread.currentThread().interrupt();
            }
        }
        busy = true;

        try (PrintStream print = new PrintStream(loggerStream)) {
            file.dump(print);
            Install install = new Install(file, data);
            ListContents contents = new ListContents();
            card.executeCommands(install, new GPCommand() {
                @Override
                public boolean execute() throws LocalizedCardException {
                    saveData(file, data);
                    return true;
                }
            }, contents);
            selectedAID = null;
            card.setApplets(contents.getResult());
        } finally {
            busy = false;
            notifyAll();
        }
    }

    private void refreshCard() throws LocalizedCardException {
        //todo maybe ugly, instead of calling the refresh card, just refresh terminals and let routine do the job
        terminals.refresh();
        loadCard();
        card = null;
        selectedAID = null;
        lastInstalled = null;

        logger.info("Card successfully refreshed.");
    }
}
