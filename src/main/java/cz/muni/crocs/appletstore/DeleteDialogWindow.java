package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.KeysPresence;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import net.miginfocom.swing.MigLayout;
import pro.javacard.gp.GPRegistryEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class DeleteDialogWindow extends JPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private JCheckBox forceUninstall = new JCheckBox();
    private JCheckBox advanced = new JCheckBox();

    private KeysPresence keys;
    private GPRegistryEntry.Kind kind;


    public DeleteDialogWindow(String aid, GPRegistryEntry.Kind kind, KeysPresence hasKeys) {

        this.keys = hasKeys;
        this.kind = kind;

        setLayout(new MigLayout("width 250px"));
        add(new JLabel("<html><p width=\"600\">" + textSrc.getString("advanced_settings") + "</p></html>"),
                "wrap, span 5, gapbottom 10");

        add(new JLabel("<html><p width=\"600\">" + textSrc.getString("pkg_id") + aid +
               "</p></html>"), "wrap, span 5, gapbottom 20");

        JLabel more = new JLabel(textSrc.getString("advanced_settings"));
        more.setFont(OptionsFactory.getOptions().getDefaultFont().deriveFont(Font.BOLD, 12f));
        add(more, "span 2");

        advanced.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                forceUninstall.setEnabled(advanced.isSelected());
            }
        });
        add(advanced, "wrap");

        add(forceUninstall);
        forceUninstall.setEnabled(false);
        add(new JLabel(textSrc.getString("chbox_force_delete")), "span 4, wrap");
        add(getHint("chbox_force_delete_expl"), "span 5, wrap");
    }

    private JLabel getHint(String key) {
        JLabel hint = new JLabel("<html><p width=\"600\">" + textSrc.getString(key) + "</p></html>");
        hint.setForeground(Color.DARK_GRAY);
        return hint;
    }

    /**
     * Get confirm dialog msg
     * @return null if ok, otherwise msg warning
     */
    public String confirm() {
        if (keys == KeysPresence.PRESENT) {
            return textSrc.getString("applet") + textSrc.getString("contains") + textSrc.getString("W_personal_data");
        }
        if (keys == KeysPresence.UNKNOWN) {
            return textSrc.getString("applet") + textSrc.getString("may_contain") + textSrc.getString("W_personal_data");
        }
        if (kind == GPRegistryEntry.Kind.SecurityDomain || kind == GPRegistryEntry.Kind.IssuerSecurityDomain) {
            return textSrc.getString("E_delete_sd"); //todo allow deleting SD?
        }
        return null;
    }

    public boolean willForce() {
        return advanced.isSelected() && forceUninstall.isSelected();
    }
}
