package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.action.InstallAction;
import cz.muni.crocs.appletstore.action.ReloadAction;
import cz.muni.crocs.appletstore.ui.CustomFlowLayout;
import cz.muni.crocs.appletstore.ui.CustomScrollBarUI;
import cz.muni.crocs.appletstore.ui.DisablePanel;
import cz.muni.crocs.appletstore.ui.ErrorPane;
import cz.muni.crocs.appletstore.ui.LoadingPaneCircle;

import cz.muni.crocs.appletstore.iface.OnEventCallBack;
import cz.muni.crocs.appletstore.util.LocalizedException;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

/**
 * Panel that lists all card contents. Allows the applet
 * installation through special LocalInstallItem object
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalWindowPane extends DisablePanel implements Searchable, Refreshable {

    private static final Logger logger = LoggerFactory.getLogger(LocalWindowPane.class);
    private static final ResourceBundle textSrc =
            ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private final LocalSubMenu submenu;
    private final LocalItemInfo infoLayout;
    private final JPanel windowLayout;
    private final JScrollPane windowScroll;

    private SearchBar searchBar;
    private final TreeSet<LocalItem> items = new TreeSet<>();
    private final LocalInstallItem installCmd = new LocalInstallItem();

    private final GridBagConstraints constraints;

    /**
     * Local panel
     */
    public LocalWindowPane() {
        setOpaque(false);

        submenu = new LocalSubMenu();

        GridBagLayout gb = new GridBagLayout();
        gb.columnWeights = new double[]{1d, 0.1d};
        gb.rowWeights = new double[]{0.01d, 1d};
        this.setLayout(gb);

        constraints = new GridBagConstraints();

        infoLayout = new LocalItemInfo();
        windowLayout = new JPanel();
        windowScroll = new JScrollPane();
        windowScroll.setViewportBorder(null);

        windowScroll.setOpaque(false);
        windowScroll.getViewport().setOpaque(false);
        windowScroll.setOpaque(false);
        windowScroll.setBorder(BorderFactory.createEmptyBorder());
        windowScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        windowScroll.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        windowScroll.getVerticalScrollBar().setUnitIncrement(16);
        windowScroll.getVerticalScrollBar().setOpaque(false);

        windowLayout.setLayout(new CustomFlowLayout(FlowLayout.LEFT, 20, 20, -1));
        windowLayout.setBorder(new EmptyBorder(10, 50, 50, 50));
        windowLayout.setOpaque(false);

        installCmd.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new InstallAction(GUIFactory.Components().defaultActionEventCallback()).mouseClicked(e);
            }
        });
        refresh();

        CardManagerFactory.getManager().setCallbackOnFailure(() -> {
            refresh();
            return null;
        });
    }

    @Override
    public void showItems(String query) {
        if (query == null) {
            query = searchBar.getQuery();
        }
        showItemsInternal(query);
    }

    @Override
    public void refresh() {
        infoLayout.set(null);

        CardManager manager = CardManagerFactory.getManager();
        CardInstance card = manager.getCard();
        logger.debug("Local pane updated: " + manager.getTerminalState().toString());

        if (!verifyTerminalState(manager.getTerminalState()))  return;
        if (card == null) {
            showError("no_card", "H_no_card", "plug-in-out.png");
            return;
        }

        if (!card.isAuthenticated() || verifyCardLifeState(card.getLifeCycle())) {
            Set<AppletInfo> cardApplets = card.getCardMetadata().getApplets();
            if (cardApplets == null) {
                showError("failed_to_list_aps", null, "no-card.png");
                logger.warn("Applet list failed, null as applet array returned.");
                return;
            }
            removeAll();

            loadApplets(cardApplets, manager);
            //skip calling search bar as it might not have been initialized
            showItemsInternal(null);

            constraints.fill = GridBagConstraints.BOTH;

            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.gridwidth = 2;
            submenu.showPackagesButton(manager.getCard().isAuthenticated());
            add(submenu, constraints);

            constraints.gridy = 1;
            constraints.gridwidth = 1;
            add(windowScroll, constraints);
            constraints.gridx = 1;

            add(infoLayout, constraints);

            infoLayout.setBackground(Color.WHITE);
        }
        revalidate();
        repaint();
    }

    @Override
    public void registerSearchBar(SearchBar bar) {
        this.searchBar = bar;
    }

    @Override
    public void showError(JPanel pane) {
        removeAll();
        add(pane);
        revalidate();
    }

    @Override
    public void showError(String keyTitle, String keyText, String imgName) {
        removeAll();
        if (keyText == null)
            add(new ErrorPane(textSrc.getString(keyTitle), imgName));
        else
            add(new ErrorPane(textSrc.getString(keyTitle), textSrc.getString(keyText), imgName));
        revalidate();
        repaint();
    }

    @Override
    public void showError(String keyTitle, String text, String imgName, LocalizedException cause) {
        removeAll();
        if (text == null)
            add(new ErrorPane(textSrc.getString(keyTitle), imgName));
        else
            add(new ErrorPane(textSrc.getString(keyTitle), text + cause.getLocalizedMessage(), imgName));
        revalidate();
        repaint();
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        infoLayout.setVisible(CardManagerFactory.getManager().isAppletStoreSelected());
    }

    private void showItemsInternal(String query) {
        if (query == null || query.isEmpty()) {
            showPanel(items);
        } else {
            TreeSet<LocalItem> sortedIems = new TreeSet<>();
            for (LocalItem item : items) {
                if (item.getSearchQuery().toLowerCase().contains(query.toLowerCase())) {
                    sortedIems.add(item);
                }
            }
            showPanel(sortedIems);
        }
    }

    /**
     * Verify whether the terminal is persent and card inserted
     * @param state state of the terminal
     * @return true if card present
     */
    private boolean verifyTerminalState(Terminals.TerminalState state) {
        switch (state) {
            case OK:
                break;
            case NO_CARD:
                showError("no_card", null, "no-card.png");
                return false;
            case NO_READER:
                showError("no_reader", null, "no-reader.png");
                return false;
            case LOADING:
                add(new LoadingPaneCircle());
                return false;
            case NO_SERVICE:
                showError("E_service", "H_service", "offline.png");
                return false;
            default:
        }
        return true;
    }

    private boolean verifyCardLifeState(Integer isdLifeState) {
        if (isdLifeState == null) {
            if (OptionsFactory.getOptions().is(Options.KEY_VERBOSE_MODE)) {
                showError("E_no_life_state", "H_no_life_state", "plug-in-out.png");
            } else {
                showError("E_communication", "H_communication", "plug-in-out.png");
            }
            return false;
        }
        switch (isdLifeState) {
            case 0x1:
                return true;
            case 0x7:
                //todo possibly allow management
                showError("E_initialized", "H_initialized", "announcement_white.png");
                return false;
            case 0xF:
                showError("E_secure_state", "H_secure_state", "announcement_white.png");
                return false;
            case 0x7F:
                showError("E_locked", "H_locked", "announcement_white.png");
                return false;
            case 0xFF:
                showError("E_terminated", "H_terminated", "announcement_white.png");
                return false;
            default:
                showError("E_no_life_state", "H_no_life_state", "announcement_white.png");
                return false;
        }
    }

    private void resetAll() {
        for (LocalItem item : items) {
            item.resetSelection();
        }
    }

    private void loadApplets(Set<AppletInfo> applets, CardManager manager) {
        items.clear();
        for (AppletInfo appletInfo : applets) {
            LocalItem item = new LocalItem(appletInfo);
            item.setCursor(new Cursor(Cursor.HAND_CURSOR));
            item.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    manager.switchAppletStoreSelected(item.info.getAid());
                    if (manager.isAppletStoreSelected())
                        infoLayout.set(item.info);
                    else
                        infoLayout.unset();
                    resetAll();
                }
            });
            items.add(item);
        }
        windowScroll.setViewportView(windowLayout);
    }

    private void showPanel(Collection<LocalItem> sortedItems) {
        windowLayout.removeAll();
        if (sortedItems.size() == 0) {
            windowLayout.add(new NotFoundItem());
        } else {
            for (LocalItem item : sortedItems) {
                if (submenu.accept(item.info.getKind()))
                    windowLayout.add(item);
            }
        }
        CardInstance card = CardManagerFactory.getManager().getCard();
        if (card != null && card.isAuthenticated()) windowLayout.add(installCmd);
        windowLayout.revalidate();
        windowLayout.repaint();
    }
}
