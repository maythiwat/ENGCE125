package com.maythiwat.engce125.lab3;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class PerformanceChart {

    private final int[] sizes;
    private final long[][] results;

    private PerformanceChart(int[] sizes, long[][] results) {
        this.sizes = sizes;
        this.results = results;
    }

    // Entry point called from Sequential
    public static void show(int[] sizes, long[][] results) {
        PerformanceChart pc = new PerformanceChart(sizes, results);
        pc.buildFrame();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String fmtSize(int sz) {
        return sz >= 1_000_000 ? sz / 1_000_000 + "M"
                : sz >= 1_000     ? sz / 1_000 + "K"
                : String.valueOf(sz);
    }

    private String fmtTime(long us) {
        return us >= 1000 ? String.format("%.2f ms", us / 1000.0) : us + " μs";
    }

    // ── Frame ─────────────────────────────────────────────────────────────────

    private void buildFrame() {
        JFrame frame = new JFrame("Sequential vs Parallel Performance");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1050, 600);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));

        tabs.addTab("Sort Chart", new LineChartPanel(
                "Sort Time — Sequential vs Parallel",
                new String[]{"Sequential Sort", "Parallel Sort"},
                new Color[]{new Color(80, 140, 220), new Color(220, 80, 80)},
                new int[]{0, 2}
        ));
        tabs.addTab("Stats Chart", new LineChartPanel(
                "Stats Time — Sequential vs Parallel",
                new String[]{"Sequential Stats", "Parallel Stats"},
                new Color[]{new Color(60, 190, 110), new Color(240, 165, 40)},
                new int[]{1, 3}
        ));
        tabs.addTab("All Metrics", new LineChartPanel(
                "All Metrics — Sequential & Parallel Sort & Stats",
                new String[]{"Sequential Sort", "Sequential Stats", "Parallel Sort", "Parallel Stats"},
                new Color[]{
                        new Color(80, 140, 220),
                        new Color(60, 190, 110),
                        new Color(220, 80, 80),
                        new Color(240, 165, 40)
                },
                new int[]{0, 1, 2, 3}
        ));
        tabs.addTab("Results Table", buildTablePanel());

        frame.add(tabs);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ── Line Chart Panel ──────────────────────────────────────────────────────

    private class LineChartPanel extends JPanel {
        String title;
        String[] seriesLabels;
        Color[] colors;
        int[] resultIdx;

        LineChartPanel(String title, String[] seriesLabels, Color[] colors, int[] resultIdx) {
            this.title = title;
            this.seriesLabels = seriesLabels;
            this.colors = colors;
            this.resultIdx = resultIdx;
            setBackground(new Color(28, 28, 38));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int padL = 100, padR = 40, padT = 60, padB = 90;
            int chartW = w - padL - padR;
            int chartH = h - padT - padB;

            // Chart area background
            g2.setColor(new Color(38, 38, 52));
            g2.fillRoundRect(padL, padT, chartW, chartH, 10, 10);

            // Title
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            FontMetrics tfm = g2.getFontMetrics();
            g2.drawString(title, (w - tfm.stringWidth(title)) / 2, 38);

            // Y-axis label (rotated)
            Graphics2D g2r = (Graphics2D) g2.create();
            g2r.setColor(new Color(170, 170, 195));
            g2r.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2r.rotate(-Math.PI / 2, 14, padT + chartH / 2);
            g2r.drawString("Time (μs / ms)", 14 - 45, padT + chartH / 2 + 4);
            g2r.dispose();

            // Max value
            long maxVal = 1;
            for (int s = 0; s < sizes.length; s++)
                for (int idx : resultIdx)
                    if (results[s][idx] > maxVal) maxVal = results[s][idx];
            maxVal = (long)(maxVal * 1.15);

            // Grid lines + Y labels
            int gridLines = 6;
            for (int i = 0; i <= gridLines; i++) {
                int y = padT + chartH - (int)((double) i / gridLines * chartH);
                g2.setColor(new Color(55, 55, 75));
                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 0));
                g2.drawLine(padL + 1, y, padL + chartW - 1, y);
                g2.setStroke(new BasicStroke(1));

                g2.setColor(new Color(160, 160, 185));
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                long yVal = maxVal * i / gridLines;
                String yLabel = fmtTime(yVal);
                FontMetrics yfm = g2.getFontMetrics();
                g2.drawString(yLabel, padL - yfm.stringWidth(yLabel) - 6, y + 4);
            }

            // X positions for each data point
            int n = sizes.length;
            int[] xPos = new int[n];
            for (int s = 0; s < n; s++) {
                xPos[s] = padL + (int)((double) s / (n - 1) * chartW);
            }

            // X labels
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            for (int s = 0; s < n; s++) {
                String lbl = "N=" + fmtSize(sizes[s]);
                FontMetrics sfm = g2.getFontMetrics();
                g2.drawString(lbl, xPos[s] - sfm.stringWidth(lbl) / 2, padT + chartH + 22);
            }

            // Draw each series
            for (int b = 0; b < resultIdx.length; b++) {
                int[] yPos = new int[n];
                for (int s = 0; s < n; s++) {
                    long val = results[s][resultIdx[b]];
                    yPos[s] = padT + chartH - (int)((double) val / maxVal * chartH);
                }

                // Shaded area under line
                Polygon area = new Polygon();
                area.addPoint(xPos[0], padT + chartH);
                for (int s = 0; s < n; s++) area.addPoint(xPos[s], yPos[s]);
                area.addPoint(xPos[n - 1], padT + chartH);
                g2.setColor(new Color(colors[b].getRed(), colors[b].getGreen(), colors[b].getBlue(), 35));
                g2.fillPolygon(area);

                // Line
                g2.setColor(colors[b]);
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int s = 0; s < n - 1; s++) {
                    g2.drawLine(xPos[s], yPos[s], xPos[s + 1], yPos[s + 1]);
                }
                g2.setStroke(new BasicStroke(1));

                // Dots + value labels
                for (int s = 0; s < n; s++) {
                    long val = results[s][resultIdx[b]];

                    // Dot glow
                    g2.setColor(new Color(colors[b].getRed(), colors[b].getGreen(), colors[b].getBlue(), 60));
                    g2.fillOval(xPos[s] - 8, yPos[s] - 8, 16, 16);
                    // Dot fill
                    g2.setColor(colors[b]);
                    g2.fillOval(xPos[s] - 5, yPos[s] - 5, 10, 10);
                    // Dot border
                    g2.setColor(Color.WHITE);
                    g2.drawOval(xPos[s] - 5, yPos[s] - 5, 10, 10);

                    // Value label — offset each series differently to reduce overlap
                    String valStr = fmtTime(val);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                    FontMetrics vfm = g2.getFontMetrics();
                    int vx = xPos[s] - vfm.stringWidth(valStr) / 2;
                    int labelOffset = switch (b % 4) {
                        case 0 -> -14;
                        case 1 -> -25;
                        case 2 -> 18;
                        default -> 29;
                    };
                    int vy = yPos[s] + labelOffset;
                    g2.setColor(colors[b].brighter());
                    g2.drawString(valStr, vx, vy);
                }
            }

            // Axes
            g2.setColor(new Color(100, 100, 130));
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(padL, padT, padL, padT + chartH);
            g2.drawLine(padL, padT + chartH, padL + chartW, padT + chartH);

            // Legend — dynamic width based on number of series
            int legY = padT + chartH + 50;
            int itemW = 180;
            int totalLegW = resultIdx.length * itemW;
            int legStartX = (w - totalLegW) / 2;
            for (int b = 0; b < resultIdx.length; b++) {
                int lx = legStartX + b * itemW;
                g2.setColor(colors[b]);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawLine(lx, legY + 7, lx + 20, legY + 7);
                g2.fillOval(lx + 7, legY + 3, 8, 8);
                g2.setStroke(new BasicStroke(1));
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g2.drawString(seriesLabels[b], lx + 26, legY + 12);
            }
        }
    }

    // ── Results Table Panel ───────────────────────────────────────────────────

    private JPanel buildTablePanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(28, 28, 38));

        JLabel header = new JLabel("Performance Results (Microseconds)", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 15));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(18, 0, 12, 0));
        outer.add(header, BorderLayout.NORTH);

        String[] cols = {
                "Data Size",
                "Seq Sort (μs)", "Para Sort (μs)", "Sort Speedup",
                "Seq Stats (μs)", "Para Stats (μs)", "Stats Speedup"
        };

        Object[][] data = new Object[sizes.length][cols.length];
        for (int s = 0; s < sizes.length; s++) {
            long ss = results[s][0], ps = results[s][2];
            long st = results[s][1], pt = results[s][3];
            double sortSpeedup  = ps > 0 ? (double) ss / ps : 0;
            double statsSpeedup = pt > 0 ? (double) st / pt : 0;

            data[s][0] = fmtSize(sizes[s]);
            data[s][1] = ss;
            data[s][2] = ps;
            data[s][3] = String.format("%.2fx", sortSpeedup);
            data[s][4] = st;
            data[s][5] = pt;
            data[s][6] = String.format("%.2fx", statsSpeedup);
        }

        DefaultTableModel model = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Monospaced", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.setBackground(new Color(38, 38, 52));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(60, 60, 85));
        table.setSelectionBackground(new Color(70, 100, 160));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("SansSerif", Font.BOLD, 12));
        th.setBackground(new Color(50, 55, 80));
        th.setForeground(new Color(200, 210, 255));
        th.setReorderingAllowed(false);

        DefaultTableCellRenderer speedupRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                           boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                double v = Double.parseDouble(val.toString().replace("x", ""));
                if (v > 1.05)       setForeground(new Color(80, 220, 120));
                else if (v < 0.95)  setForeground(new Color(255, 100, 100));
                else                setForeground(new Color(200, 200, 200));
                return this;
            }
        };

        DefaultTableCellRenderer stripedRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                           boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setForeground(Color.WHITE);
                if (!sel) {
                    setBackground(row % 2 == 0 ? new Color(38, 38, 52) : new Color(48, 48, 65));
                }
                return this;
            }
        };

        for (int c = 0; c < cols.length; c++) {
            TableColumn tc = table.getColumnModel().getColumn(c);
            tc.setPreferredWidth(c == 0 ? 90 : 130);
            tc.setCellRenderer((c == 3 || c == 6) ? speedupRenderer : stripedRenderer);
        }

        JLabel note = new JLabel(
                "Speedup = Seq Time / Para Time   |   Green >1× = Parallel faster   |   Red <1× = Sequential faster",
                SwingConstants.CENTER);
        note.setFont(new Font("SansSerif", Font.ITALIC, 11));
        note.setForeground(new Color(160, 160, 190));
        note.setBorder(BorderFactory.createEmptyBorder(10, 0, 12, 0));

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(38, 38, 52));
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        outer.add(scroll, BorderLayout.CENTER);
        outer.add(note, BorderLayout.SOUTH);
        return outer;
    }
}