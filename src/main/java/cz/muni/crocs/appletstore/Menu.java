package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.Terminals;
import cz.muni.crocs.appletstore.ui.CustomFont;
import cz.muni.crocs.appletstore.ui.CustomJmenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Menu extends JMenuBar implements ActionListener, ItemListener {

    private JMenu submenu;
    private AppletStore context;

    private JMenu readers;
    private ButtonGroup readersPresent = new ButtonGroup();

    public Menu(AppletStore parent) {
        context = parent;

        //black background with margin and no border bar
        setBackground(new Color(0, 0, 0));
        setMargin(new Insets(10, 100, 5, 5));
        setBorder(null);

        buildMenu();

        //set action events
//TODO: for each menu
//        for (int pos = 0; pos < menu.getItemCount(); ++pos) {
//            menu.getItem(pos).addActionListener(this);
//        }

//        //for each JRadioButtonMenuItem:
//        rbMenuItem.addActionListener(this);
//        ...
//        //for each JCheckBoxMenuItem:
//        cbMenuItem.addItemListener(this);
    }


    /**
     * @param action         action to perform
     * @param keyEvent       KeyEvent key constant
     * @param inputEventMask InputEvent constant - mask for accelerated access
     * @return
     */
    private JMenuItem menuItemWithKeyShortcutAndIcon(AbstractAction action, String imagePath,
                                                     String descripton, int keyEvent, int inputEventMask) {
        JMenuItem menuItem = menuItemWithKeyShortcut(action, descripton, keyEvent,inputEventMask);
        menuItem.setIcon(new ImageIcon(imagePath));

        return menuItem;
    }

    /**
     * @param action         action to perform
     * @param keyEvent       KeyEvent key constant
     * @param inputEventMask InputEvent constant - mask for accelerated access
     * @return
     */
    private JMenuItem menuItemWithKeyShortcut(AbstractAction action, String descripton,
                                               int keyEvent, int inputEventMask) {
        JMenuItem menuItem = menuItemNoShortcut(action, descripton);

        menuItem.setAccelerator(KeyStroke.getKeyStroke(keyEvent, inputEventMask));
        return menuItem;
    }

    private JMenuItem menuItemDisabled(String title, String descripton) {
        JMenuItem menuItem = menuItemNoShortcut(null, descripton);
        menuItem.setText(title);
        return menuItem;
    }

    private JMenuItem menuItemNoShortcut(AbstractAction action, String descripton) {
        JMenuItem menuItem = new JMenuItem(action);
        setItemLook(menuItem, descripton);
        return menuItem;
    }

    private void setItemLook(AbstractButton component, String descripton) {
        component.setForeground(new Color(0x000000));
        component.setBackground(new Color(0xffffff));
        component.setFont(CustomFont.plain.deriveFont(10f));
        component.getAccessibleContext().setAccessibleDescription(descripton);
        component.setMargin(new Insets(4, 4, 4, 4));
        Dimension preferred = component.getPreferredSize();
        component.setPreferredSize(new Dimension(200, (int)preferred.getHeight()));
    }

    private JRadioButtonMenuItem selectableMenuItem(String title, String descripton) {
        JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(title);
        setItemLook(rbMenuItem, descripton);
        return rbMenuItem;
    }

    //TODO menu into func and return menu
    private void buildMenu() {

        CustomJmenu menu = new CustomJmenu(Config.translation.get(93), "", KeyEvent.VK_A);
        add(menu);

        menu.add(menuItemWithKeyShortcutAndIcon(new AbstractAction(Config.translation.get(114)) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Settings settings = new Settings(context);
                Object[] options = { Config.translation.get(115), Config.translation.get(116) };
                int result = JOptionPane.showOptionDialog(null, settings, Config.translation.get(114),
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                        null, options, null);
                if (result == JOptionPane.YES_OPTION){
                    settings.apply();
                }
            }
        }, Config.IMAGE_DIR + "settings.png", "", KeyEvent.VK_S, InputEvent.ALT_MASK));

//        settings.setFont(CustomFont.plain);
//        settings.setForeground(Color.WHITE);
//        settings.setMargin(new Insets(0,0 ,0 ,0 ));


        //a group of JMenuItems
//        menu.add(menuItemWithKeyShortcut("First", "Description", KeyEvent.VK_1, InputEvent.ALT_MASK));
//        menu.add(menuItemNoShortcut("Second", "Description"));
//        menu.add(menuItemNoShortcut("Third", "Description"));
        //a group of radio button menu items
       // menu.addSeparator();

//
//
//        rbMenuItem = new JRadioButtonMenuItem("Another one");
//        rbMenuItem.setMnemonic(KeyEvent.VK_O);
//        group.add(rbMenuItem);
//        menu.add(rbMenuItem);
//
////a group of check box menu items
//        menu.addSeparator();
//        cbMenuItem = new JCheckBoxMenuItem("A check box menu item");
//        cbMenuItem.setMnemonic(KeyEvent.VK_C);
//        menu.add(cbMenuItem);
//
//        cbMenuItem = new JCheckBoxMenuItem("Another one");
//        cbMenuItem.setMnemonic(KeyEvent.VK_H);
//        menu.add(cbMenuItem);

//a submenu

        submenu = new JMenu("A submenu");
        submenu.setMnemonic(KeyEvent.VK_S);

//        submenu.add(menuItemWithKeyShortcut("Item submenu", "Description",
//                KeyEvent.VK_2, InputEvent.ALT_MASK));


        add(new CustomJmenu("Another Menu", "This menu does nothing", KeyEvent.VK_N));

        //BUILD READERS MENU
        readers = new CustomJmenu(Config.translation.get(90), "", KeyEvent.VK_R);

        resetTerminalButtonGroup(); //possible to call multiple times in order to refresh readers in a menu
        add(readers);


//        add(Box.createGlue());
//
//        menu = new CustomJmenu("", "Minimize", KeyEvent.VK_UNDEFINED);
//        menu.setIcon(new ImageIcon("src/main/resources/img/minimize.png"));
//        menu.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (e.getClickCount() >= 1) {
//                    context.setState(JFrame.ICONIFIED);
//                }
//            }
//        });
//        menu.setHorizontalAlignment(SwingConstants.RIGHT);
//        add(menu);
//
//        menu = new CustomJmenu("", "Exit", KeyEvent.VK_UNDEFINED);
//        menu.setIcon(new ImageIcon("src/main/resources/img/close_white.png"));
//
//        menu.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (e.getClickCount() >= 1) {
//                    System.exit(0);
//                }
//            }
//        });
//        menu.setHorizontalAlignment(SwingConstants.RIGHT);
//        add(menu);
    }


    public void resetTerminalButtonGroup() {
        readers.removeAll(); //TODO dont recreate refresh button

        if (context.terminals().getState() != Terminals.TerminalState.NO_READER) {
            readersPresent = new ButtonGroup();
            for (String name : context.terminals().getTerminals().keySet()) {
                //todo set selected
                JRadioButtonMenuItem item = selectableMenuItem(name, Config.translation.get(56));
                readersPresent.add(item);
                readers.add(item);
            }
            readersPresent.setSelected(((JRadioButtonMenuItem)readers.getMenuComponent(2)).getModel(), true);
        } else {
            JMenuItem item = menuItemDisabled(Config.translation.get(2), "");
            item.setIcon(new ImageIcon(Config.IMAGE_DIR + "no-reader-small.png"));
            item.setEnabled(false);
            readers.add(item);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //TODO
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        //TODO
    }
}
