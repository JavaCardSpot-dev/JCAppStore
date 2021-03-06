package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

/**
 * Panel intended to work with HintLabel
 * Draws the hint message over the hintLabel if mouse hoovered
 * The line break symbol is "\n"
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class HintPanel extends JPanel {

    private static boolean enabled = true;
    protected String hint;
    protected Font hintFont = OptionsFactory.getOptions().getFont(12f);

    private Point p = null;
    private boolean shown = false;
    private Dimension hintDimen;

    /**
     * Create a hint panel
     * @param enable whether the hints should be displayed, changes the value for every hint panel (static)
     */
    public HintPanel(boolean enable) {
        enabled = enable;

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!enabled) return;

                Point at = e.getPoint();
                Component c = getComponentAt(at);
                if (c instanceof HintLabel) {
                    p = e.getPoint();
                    hint = ((HintLabel) c).getHint();
                    if (hint.isEmpty()) {
                        hint = null;
                        shown = false;
                    } else {
                        shown = true;
                    }

                    revalidate();
                    repaint();
                } else {
                    if (shown)  {
                        shown = false;
                        revalidate();
                        repaint();
                    }
                    hint = null;
                    hintDimen = null;
                }
            }
        });

    }

    /**
     * Enable hint display, enables it globally for all hint panels
     * @param enable true if show hints
     */
    public static void enableHint(boolean enable) {
        enabled = enable;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (enabled && hint != null) {
            Graphics2D g2d = (Graphics2D) g;
            // first call the dimen is not known, after second the dimen is computed
            if (hintDimen != null) {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(p.x + 13, p.y - 13, hintDimen.width, hintDimen.height, 5, 5);
            }
            drawString(g2d, hint, p.x + 15, p.y);
        }
    }

    private void drawString(Graphics2D graphics, String str, int x, int y) {
        FontRenderContext fontRender = graphics.getFontRenderContext();
        graphics.setColor(Color.BLACK);
        graphics.setFont(hintFont);
        int width = 0;
        int height = 0;
        for (String line : str.split("\\n")) {
            if (hintDimen != null)
                graphics.drawString(line, x, y + height);
            Rectangle2D bnds = hintFont.getStringBounds(line, fontRender);
            width = Math.max(width, (int) bnds.getWidth());
            height += bnds.getHeight();
        }
        hintDimen = new Dimension(width + 4, height + 3);
    }
}
