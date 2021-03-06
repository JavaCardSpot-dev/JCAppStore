package cz.muni.crocs.appletstore;

import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.ui.HtmlText;
import cz.muni.crocs.appletstore.ui.HtmlTitle;
import cz.muni.crocs.appletstore.util.JsonParser;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Item displayed in store
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreItem extends JPanel implements Item {

    private final static Color BORDER_COLOR = new Color(200,200,200);
    private final static Color BORDER_COLOR_OUTER = new Color(214,214,214);

    private final String searchQuery;
    private final boolean isBydefaultHidden;
    private int position;

    /**
     * Create a store item
     * @param dataSet json object from info_[lang].json file
     * @param category category the applet belongs to
     * @param position position as defined in the file
     */
    public StoreItem(JsonObject dataSet, String category, int position) {
        this(dataSet.get(JsonParser.TAG_TITLE).getAsString(),
                dataSet.get(JsonParser.TAG_AUTHOR).getAsString(),
                category,
                dataSet.get(JsonParser.TAG_LATEST).getAsString(),
                dataSet.get(JsonParser.TAG_ICON).getAsString(),
                dataSet.get(JsonParser.TAG_HIDDEN).getAsBoolean()
        );
        this.position = position;
    }

    @Override
    public String getSearchQuery() {
        return searchQuery;
    }

    @Override
    public boolean byDefaultHidden() {
        return isBydefaultHidden;
    }

    @Override
    public int hashCode() {
        return position;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Item)) return false;
        return obj.hashCode() == hashCode();
    }

    @Override
    public int compareTo(Item o) {
        int thisCode = hashCode();
        int otherCode = o.hashCode();

        if (thisCode == otherCode) {
            return 0;
        }
        return thisCode > otherCode ? 1 : -1;
    }

    private StoreItem(String title, String author, String category,
                      String version, String image, boolean defaultHidden) {
        isBydefaultHidden = defaultHidden;
        searchQuery = title + author + category;
        setLayout(new GridBagLayout());

        setOpaque(false);
       // setBackground(Color.WHITE);
        setAlignmentX(LEFT_ALIGNMENT);

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR_OUTER),
                BorderFactory.createLineBorder(BORDER_COLOR)
        ));

        JPanel container = new JPanel();
        container.setLayout(new GridBagLayout());
        container.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel icon = new JLabel(
                "<html><img src=\"file:///" + getImgAddress(image) + "\" width=\"130\" height=\"130\"/> </html>") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D)g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                super.paintComponent(g);
            }
        };
        add(icon, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        title = adjustLength(title, 25);
        container.add(getLabel(title, "width:100px; height: 60px; margin: 5px", 16f, true), gbc);

        gbc.fill = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        author = adjustLength(author, 15);
        JLabel info = getLabel(author, "width:85px; max-lines:1; margin: 5px", 13f, true);
        info.setHorizontalAlignment(SwingConstants.RIGHT);
        container.add(info, gbc);

        gbc.anchor = GridBagConstraints.LAST_LINE_END;
        version = adjustLength(version, 5);
        container.add(getLabel(version, "width:10px; text-overflow: ellipsis; margin: 5px", 15f, false), gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(container, gbc);
    }

    private JLabel getLabel(String text, String css, Float fontSize, boolean title) {
        return (title) ?
                new HtmlTitle("<div style=\"" + css + "\">" + text + "</div>", fontSize)
                :
                new HtmlText("<div style=\"" + css + "\">" + text + "</div>", fontSize);
    }

    private String getImgAddress(String imgName) {
        if (imgName == null || imgName.isEmpty()) {
            return new File(Config.IMAGE_DIR + "applet_plain.png").getAbsolutePath();
        }
        File img = new File(Config.RESOURCES + imgName);
        img = (img.exists()) ? img : new File(Config.IMAGE_DIR + "applet_plain.png");
        return img.getAbsolutePath();
    }
}
