package hms.ui.components;

import hms.Hospital;
import hms.RoutePlanner;
import hms.ui.UITheme;
import hms.ui.demo.DemoDataStore;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.*;

/**
 * Interactive visual 2D Hospital Network Map component.
 * Displays regional hospital nodes, weighted highway route edges, real-time bed capacity,
 * and dynamically illuminates the optimal Dijkstra shortest path.
 */
public class HospitalMapCanvas extends JPanel {

    private final DemoDataStore store;
    private String sourceHospitalId = "H001";
    private String destHospitalId = "H002";
    private RoutePlanner.RouteResult activeRoute = null;

    private String hoveredHospitalId = null;
    private Point mousePoint = null;

    private BiConsumer<String, Boolean> onHospitalSelected;

    // Node layout offsets relative to canvas center
    private static class NodePos {
        final int offsetX;
        final int offsetY;
        NodePos(int x, int y) { this.offsetX = x; this.offsetY = y; }
    }

    private static final Map<String, NodePos> DEFAULT_POSITIONS = new HashMap<>();
    static {
        DEFAULT_POSITIONS.put("H001", new NodePos(0, -10));     // Government General Hospital (Center)
        DEFAULT_POSITIONS.put("H002", new NodePos(0, -165));    // Multi-Specialty (North Zone)
        DEFAULT_POSITIONS.put("H003", new NodePos(230, -35));   // Medical College (East Zone)
        DEFAULT_POSITIONS.put("H004", new NodePos(35, 155));    // Trauma Center (South Zone)
        DEFAULT_POSITIONS.put("H005", new NodePos(-225, 25));   // Community Health (West Zone)
    }

    public HospitalMapCanvas() {
        this.store = DemoDataStore.getInstance();
        setBackground(new Color(0xF8, 0xFA, 0xFC));
        setOpaque(true);
        setPreferredSize(new Dimension(650, 480));
        setMinimumSize(new Dimension(450, 380));

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePoint = e.getPoint();
                String prevHovered = hoveredHospitalId;
                hoveredHospitalId = findHospitalAtPoint(e.getPoint());
                if (!Objects.equals(prevHovered, hoveredHospitalId)) {
                    setCursor(hoveredHospitalId != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoveredHospitalId = null;
                mousePoint = null;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                String clickedHosp = findHospitalAtPoint(e.getPoint());
                if (clickedHosp != null) {
                    boolean isRightClick = SwingUtilities.isRightMouseButton(e) || e.isShiftDown();
                    if (isRightClick) {
                        setSourceHospital(clickedHosp);
                        if (onHospitalSelected != null) {
                            onHospitalSelected.accept(clickedHosp, true);
                        }
                    } else {
                        // Left click sets destination unless clicking on current source
                        if (clickedHosp.equalsIgnoreCase(sourceHospitalId)) {
                            // If clicking on source, toggle to make it destination or prompt
                            return;
                        }
                        setDestinationHospital(clickedHosp);
                        if (onHospitalSelected != null) {
                            onHospitalSelected.accept(clickedHosp, false);
                        }
                    }
                }
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    public void setOnHospitalSelected(BiConsumer<String, Boolean> callback) {
        this.onHospitalSelected = callback;
    }

    public void setSourceHospital(String hospitalId) {
        this.sourceHospitalId = hospitalId;
        recalculateRoute();
        repaint();
    }

    public void setDestinationHospital(String hospitalId) {
        this.destHospitalId = hospitalId;
        recalculateRoute();
        repaint();
    }

    public void setActiveRoute(RoutePlanner.RouteResult route) {
        this.activeRoute = route;
        repaint();
    }

    public void recalculateRoute() {
        if (sourceHospitalId != null && destHospitalId != null && !sourceHospitalId.equalsIgnoreCase(destHospitalId)) {
            this.activeRoute = store.getRoutePlanner().findShortestPath(sourceHospitalId, destHospitalId);
        } else {
            this.activeRoute = null;
        }
    }

    public String getSourceHospitalId() {
        return sourceHospitalId;
    }

    public String getDestHospitalId() {
        return destHospitalId;
    }

    public RoutePlanner.RouteResult getActiveRoute() {
        return activeRoute;
    }

    private Point getNodeCenter(String hospitalId, int cx, int cy) {
        NodePos pos = DEFAULT_POSITIONS.get(hospitalId);
        if (pos != null) {
            return new Point(cx + pos.offsetX, cy + pos.offsetY);
        }
        // Fallback layout in a circle for additional dynamic hospitals
        List<Hospital> all = store.getHospitals();
        int idx = 0;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getHospitalId().equalsIgnoreCase(hospitalId)) {
                idx = i;
                break;
            }
        }
        double angle = 2 * Math.PI * idx / Math.max(1, all.size());
        int r = 180;
        return new Point(cx + (int) (r * Math.cos(angle)), cy + (int) (r * Math.sin(angle)));
    }

    private String findHospitalAtPoint(Point pt) {
        if (pt == null) return null;
        int cx = getWidth() / 2;
        int cy = (getHeight() - 40) / 2;

        int nodeW = 180;
        int nodeH = 72;

        for (Hospital h : store.getHospitals()) {
            Point p = getNodeCenter(h.getHospitalId(), cx, cy);
            Rectangle rect = new Rectangle(p.x - nodeW / 2, p.y - nodeH / 2, nodeW, nodeH);
            if (rect.contains(pt)) {
                return h.getHospitalId();
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();

        // 1. Subtle Background Grid Pattern
        drawGrid(g2, w, h);

        int cx = w / 2;
        int cy = (h - 45) / 2;

        // 2. Build Active Path Edge Set for Dijkstra Route Highlighting
        Set<String> activePathEdges = new HashSet<>();
        List<String> pathNodes = (activeRoute != null && activeRoute.isFound()) ? activeRoute.getPath() : Collections.emptyList();
        for (int i = 0; i < pathNodes.size() - 1; i++) {
            String u = pathNodes.get(i);
            String v = pathNodes.get(i + 1);
            activePathEdges.add(u + "-" + v);
            activePathEdges.add(v + "-" + u);
        }

        // 3. Draw All Connecting Route Edges (Highways)
        List<RoutePlanner.NetworkEdge> edges = store.getRoutePlanner().getAllEdges();
        for (RoutePlanner.NetworkEdge edge : edges) {
            Point p1 = getNodeCenter(edge.getSource(), cx, cy);
            Point p2 = getNodeCenter(edge.getTarget(), cx, cy);
            boolean isActive = activePathEdges.contains(edge.getSource() + "-" + edge.getTarget());
            drawRouteEdge(g2, p1, p2, edge.getTravelMinutes(), isActive);
        }

        // 4. Draw Hospital Facility Nodes
        int nodeW = 180;
        int nodeH = 72;

        for (Hospital hosp : store.getHospitals()) {
            Point pt = getNodeCenter(hosp.getHospitalId(), cx, cy);
            boolean isSource = hosp.getHospitalId().equalsIgnoreCase(sourceHospitalId);
            boolean isDest = hosp.getHospitalId().equalsIgnoreCase(destHospitalId);
            boolean isTransit = !isSource && !isDest && pathNodes.contains(hosp.getHospitalId());
            boolean isHovered = hosp.getHospitalId().equalsIgnoreCase(hoveredHospitalId);

            drawHospitalNode(g2, hosp, pt.x, pt.y, nodeW, nodeH, isSource, isDest, isTransit, isHovered);
        }

        // 5. Draw Legend at bottom
        drawLegend(g2, w, h);

        // 6. Draw Hover Tooltip if applicable
        if (hoveredHospitalId != null && mousePoint != null) {
            Hospital hoveredHosp = store.getHospitalById(hoveredHospitalId);
            if (hoveredHosp != null) {
                drawTooltip(g2, hoveredHosp, mousePoint.x, mousePoint.y);
            }
        }

        g2.dispose();
    }

    private void drawGrid(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(0xEE, 0xF2, 0xF6));
        int step = 28;
        for (int x = 0; x < w; x += step) {
            g2.drawLine(x, 0, x, h - 45);
        }
        for (int y = 0; y < h - 45; y += step) {
            g2.drawLine(0, y, w, y);
        }
    }

    private void drawRouteEdge(Graphics2D g2, Point p1, Point p2, int minutes, boolean isActive) {
        if (isActive) {
            // Glowing Highlight for Dijkstra Shortest Route Path
            g2.setColor(new Color(0x25, 0x63, 0xEB, 90));
            g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);

            g2.setColor(UITheme.PRIMARY_BLUE);
            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        } else {
            // Standard Route Edge
            g2.setColor(new Color(0xCB, 0xD5, 0xE1));
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6, 4}, 0));
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        // Edge Travel Weight Pill in Center of Edge
        int midX = (p1.x + p2.x) / 2;
        int midY = (p1.y + p2.y) / 2;

        String weightText = minutes + " min";
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(weightText);
        int pillW = textW + 14;
        int pillH = 18;

        if (isActive) {
            g2.setColor(UITheme.PRIMARY_BLUE);
            g2.fillRoundRect(midX - pillW / 2, midY - pillH / 2, pillW, pillH, 9, 9);
            g2.setColor(Color.WHITE);
        } else {
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(midX - pillW / 2, midY - pillH / 2, pillW, pillH, 9, 9);
            g2.setColor(new Color(0x94, 0xA3, 0xB8));
            g2.drawRoundRect(midX - pillW / 2, midY - pillH / 2, pillW, pillH, 9, 9);
            g2.setColor(new Color(0x47, 0x55, 0x69));
        }

        g2.drawString(weightText, midX - textW / 2, midY + fm.getAscent() / 2 - 2);
    }

    private void drawHospitalNode(Graphics2D g2, Hospital hosp, int cx, int cy, int w, int h,
                                  boolean isSource, boolean isDest, boolean isTransit, boolean isHovered) {
        int x = cx - w / 2;
        int y = cy - h / 2;

        // Drop Shadow
        g2.setColor(new Color(0, 0, 0, isHovered ? 25 : 12));
        g2.fillRoundRect(x + 2, y + 3, w, h, 14, 14);

        // Card Fill
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, y, w, h, 14, 14);

        // Border & Accent Styling
        if (isSource) {
            g2.setColor(UITheme.SUCCESS_GREEN);
            g2.setStroke(new BasicStroke(3.2f));
        } else if (isDest) {
            g2.setColor(UITheme.PRIMARY_BLUE);
            g2.setStroke(new BasicStroke(3.2f));
        } else if (isTransit) {
            g2.setColor(UITheme.WARNING_AMBER);
            g2.setStroke(new BasicStroke(2.8f));
        } else if (isHovered) {
            g2.setColor(UITheme.PRIMARY_NAVY);
            g2.setStroke(new BasicStroke(2.0f));
        } else {
            g2.setColor(UITheme.BORDER_COLOR);
            g2.setStroke(new BasicStroke(1.2f));
        }
        g2.drawRoundRect(x, y, w, h, 14, 14);

        // Top Status Badge (SOURCE / DESTINATION / TRANSIT)
        if (isSource || isDest || isTransit) {
            String badgeText = isSource ? "SOURCE" : (isDest ? "DESTINATION" : "TRANSIT HOP");
            Color badgeBg = isSource ? UITheme.SUCCESS_GREEN : (isDest ? UITheme.PRIMARY_BLUE : UITheme.WARNING_AMBER);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
            FontMetrics bfm = g2.getFontMetrics();
            int bw = bfm.stringWidth(badgeText) + 10;
            int bh = 14;

            g2.setColor(badgeBg);
            g2.fillRoundRect(x + w - bw - 8, y + 6, bw, bh, 6, 6);
            g2.setColor(Color.WHITE);
            g2.drawString(badgeText, x + w - bw - 8 + 5, y + 6 + bfm.getAscent() - 1);
        }

        // Left Icon Emblem (Medical Cross)
        int emblemSize = 24;
        int emblemX = x + 10;
        int emblemY = y + 10;
        g2.setColor(isSource ? UITheme.SUCCESS_LIGHT : (isDest ? UITheme.INFO_BLUE_BG : new Color(0xF1, 0xF5, 0xF9)));
        g2.fillOval(emblemX, emblemY, emblemSize, emblemSize);
        g2.setColor(isSource ? UITheme.SUCCESS_GREEN : (isDest ? UITheme.PRIMARY_BLUE : UITheme.PRIMARY_NAVY));
        g2.fillRect(emblemX + 10, emblemY + 5, 4, 14);
        g2.fillRect(emblemX + 5, emblemY + 10, 14, 4);

        // Hospital ID & Name
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(UITheme.PRIMARY_NAVY);
        String name = hosp.getHospitalName();
        if (name.length() > 16) {
            name = name.substring(0, 15) + "...";
        }
        g2.drawString(hosp.getHospitalId() + ": " + name, x + 40, y + 20);

        // Location Zone Subtitle
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(UITheme.TEXT_MUTED);
        g2.drawString(hosp.getLocation(), x + 40, y + 33);

        // Bed Capacity Badges Bar (Bottom of card)
        int genAvail = hosp.getAvailableGeneralBedsCount();
        int icuAvail = hosp.getAvailableICUBedsCount();

        int pillY = y + 44;

        // ICU Bed Pill
        drawBedPill(g2, "ICU: " + icuAvail, x + 12, pillY, icuAvail > 0);

        // General Bed Pill
        drawBedPill(g2, "General: " + genAvail, x + 82, pillY, genAvail > 0);
    }

    private void drawBedPill(Graphics2D g2, String text, int px, int py, boolean hasCapacity) {
        g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text);
        int pw = tw + 10;
        int ph = 16;

        Color bg = hasCapacity ? new Color(0xDC, 0xFC, 0xE7) : new Color(0xFE, 0xE2, 0xE2);
        Color fg = hasCapacity ? new Color(0x16, 0x65, 0x34) : new Color(0x99, 0x1B, 0x1B);

        g2.setColor(bg);
        g2.fillRoundRect(px, py, pw, ph, 6, 6);
        g2.setColor(fg);
        g2.drawString(text, px + 5, py + fm.getAscent() + 1);
    }

    private void drawLegend(Graphics2D g2, int w, int h) {
        int barH = 34;
        int barY = h - barH;

        g2.setColor(Color.WHITE);
        g2.fillRect(0, barY, w, barH);
        g2.setColor(UITheme.BORDER_COLOR);
        g2.drawLine(0, barY, w, barY);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();

        int startX = 16;
        int itemY = barY + 21;

        // Item 1: Source
        g2.setColor(UITheme.SUCCESS_GREEN);
        g2.fillOval(startX, itemY - 9, 10, 10);
        g2.setColor(UITheme.TEXT_MAIN);
        g2.drawString("Source Facility", startX + 14, itemY);

        // Item 2: Destination
        startX += 120;
        g2.setColor(UITheme.PRIMARY_BLUE);
        g2.fillOval(startX, itemY - 9, 10, 10);
        g2.setColor(UITheme.TEXT_MAIN);
        g2.drawString("Destination Facility", startX + 14, itemY);

        // Item 3: Dijkstra Path
        startX += 140;
        g2.setColor(UITheme.PRIMARY_BLUE);
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(startX, itemY - 4, startX + 20, itemY - 4);
        g2.setColor(UITheme.TEXT_MAIN);
        g2.drawString("Optimal Dijkstra Path", startX + 26, itemY);

        // Right side Hint
        String hint = "Tip: Click facility to set Destination | Right-Click to set Source";
        int hintW = fm.stringWidth(hint);
        g2.setColor(UITheme.TEXT_MUTED);
        g2.drawString(hint, w - hintW - 16, itemY);
    }

    private void drawTooltip(Graphics2D g2, Hospital h, int mx, int my) {
        int tw = 220;
        int th = 110;
        int tx = Math.min(mx + 15, getWidth() - tw - 10);
        int ty = Math.min(my + 15, getHeight() - th - 45);

        // Tooltip box
        g2.setColor(new Color(0x14, 0x2B, 0x4A, 240));
        g2.fillRoundRect(tx, ty, tw, th, 10, 10);
        g2.setColor(new Color(0x38, 0xBD, 0xF8));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(tx, ty, tw, th, 10, 10);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.drawString(h.getHospitalId() + ": " + h.getHospitalName(), tx + 10, ty + 20);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2.setColor(new Color(0x94, 0xA3, 0xB8));
        g2.drawString("Location: " + h.getLocation(), tx + 10, ty + 38);

        g2.setColor(Color.WHITE);
        g2.drawString("Available ICU Beds: " + h.getAvailableICUBedsCount() + " / " + h.getIcuBedCapacity(), tx + 10, ty + 56);
        g2.drawString("Available General Beds: " + h.getAvailableGeneralBedsCount() + " / " + h.getGeneralBedCapacity(), tx + 10, ty + 74);

        g2.setColor(new Color(0x38, 0xBD, 0xF8));
        g2.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        g2.drawString("Click to Select | Double-Click to Route", tx + 10, ty + 94);
    }
}

