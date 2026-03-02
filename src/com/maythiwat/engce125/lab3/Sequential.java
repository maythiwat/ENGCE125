package com.maythiwat.engce125.lab3;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Sequential {

    static int[] sizes = {100, 1000, 10000, 100000, 1000000, 10000000, 100000000};
    // [sizeIndex][0=seqSort, 1=seqStats, 2=paraSort, 3=paraStats]
    static long[][] results = new long[sizes.length][4];

    public static void main(String[] args) {
        for (int s = 0; s < sizes.length; s++) {
            int size = sizes[s];
            System.out.println("\n--- Data Size: " + size + " ---");
            int[] numbers = randData(size);

            System.out.println("[Sequential Approach]");
            results[s][0] = seqSort(numbers.clone());
            results[s][1] = seqStats(numbers.clone());

            System.out.println("\n[Parallel Approach]");
            results[s][2] = paraSort(numbers.clone());
            results[s][3] = paraStats(numbers.clone());
        }

        SwingUtilities.invokeLater(Sequential::showChart);
    }

    public static int[] randData(int size) {
        Random rand = new Random();
        int[] numbers = new int[size];
        for (int i = 0; i < size; i++) {
            numbers[i] = rand.nextInt(1, 100);
        }
        return numbers;
    }

    public static long seqSort(int[] numbers) {
        long tStart = System.nanoTime();
        Arrays.sort(numbers);
        long elapsed = (System.nanoTime() - tStart) / 1000;
        System.out.println("Sequential Sort: " + elapsed + " Microseconds");
        return elapsed;
    }

    public static long seqStats(int[] numbers) {
        long tStart = System.nanoTime();

        int min = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i] < min) min = numbers[i];
        }

        int max = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i] > max) max = numbers[i];
        }

        long sum = 0;
        for (int number : numbers) sum += number;
        double avg = (double) sum / numbers.length;

        double median;
        if (numbers.length % 2 == 1) {
            median = numbers[numbers.length / 2];
        } else {
            median = (numbers[(numbers.length / 2) - 1] + numbers[numbers.length / 2]) / 2.0;
        }

        long elapsed = (System.nanoTime() - tStart) / 1000;
        System.out.println("Sequential Stats: " + elapsed + " Microseconds");
        return elapsed;
    }

    public static long paraSort(int[] numbers) {
        int cores = Runtime.getRuntime().availableProcessors();
        int chunkSize = numbers.length / cores;
        int[][] chunks = new int[cores][];

        for (int i = 0; i < cores; i++) {
            int start = i * chunkSize;
            int end = (i == cores - 1) ? numbers.length : (i + 1) * chunkSize;
            chunks[i] = Arrays.copyOfRange(numbers, start, end);
        }

        ExecutorService executor = Executors.newFixedThreadPool(cores);
        long tStart = System.nanoTime();

        for (int i = 0; i < cores; i++) {
            int idx = i;
            executor.submit(() -> Arrays.sort(chunks[idx]));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) { e.printStackTrace(); }

        long elapsed = (System.nanoTime() - tStart) / 1000;
        System.out.println("Data Parallel Sort: " + elapsed + " Microseconds");
        return elapsed;
    }

    public static long paraStats(int[] numbers) {
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cores);
        long tStart = System.nanoTime();

        executor.submit(() -> {
            int min = numbers[0];
            for (int n : numbers) if (n < min) min = n;
        });
        executor.submit(() -> {
            int max = numbers[0];
            for (int n : numbers) if (n > max) max = n;
        });
        executor.submit(() -> {
            long sum = 0;
            for (int n : numbers) sum += n;
            double avg = (double) sum / numbers.length;
        });
        executor.submit(() -> {
            int[] sorted = numbers.clone();
            Arrays.sort(sorted);
            double median = (sorted.length % 2 == 1)
                    ? sorted[sorted.length / 2]
                    : (sorted[(sorted.length / 2) - 1] + sorted[sorted.length / 2]) / 2.0;
        });

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) { e.printStackTrace(); }

        long elapsed = (System.nanoTime() - tStart) / 1000;
        System.out.println("Task Parallel Stats: " + elapsed + " Microseconds");
        return elapsed;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    static String fmtSize(int sz) {
        return sz >= 1_000_000 ? sz / 1_000_000 + "M"
                : sz >= 1_000     ? sz / 1_000 + "K"
                : String.valueOf(sz);
    }

    static String fmtTime(long us) {
        return us >= 1000 ? String.format("%.2f ms", us / 1000.0) : us + " μs";
    }

    // ── Swing UI ──────────────────────────────────────────────────────────────

    static void showChart() {
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
        tabs.addTab("Results Table", buildTablePanel());

        frame.add(tabs);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ── Line Chart Panel ──────────────────────────────────────────────────────

    static class LineChartPanel extends JPanel {
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

                    // Value label
                    String valStr = fmtTime(val);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                    FontMetrics vfm = g2.getFontMetrics();
                    int vx = xPos[s] - vfm.stringWidth(valStr) / 2;
                    int vy = (b == 0) ? yPos[s] - 10 : yPos[s] + 20;
                    g2.setColor(colors[b].brighter());
                    g2.drawString(valStr, vx, vy);
                }
            }

            // Axes
            g2.setColor(new Color(100, 100, 130));
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(padL, padT, padL, padT + chartH);
            g2.drawLine(padL, padT + chartH, padL + chartW, padT + chartH);

            // Legend
            int legY = padT + chartH + 50;
            int totalLegW = resultIdx.length * 180;
            int legStartX = (w - totalLegW) / 2;
            for (int b = 0; b < resultIdx.length; b++) {
                int lx = legStartX + b * 180;
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

    static JPanel buildTablePanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(28, 28, 38));

        // Header label
        JLabel header = new JLabel("Performance Results (Microseconds)", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 15));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(18, 0, 12, 0));
        outer.add(header, BorderLayout.NORTH);

        // Table columns
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

        // Header style
        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("SansSerif", Font.BOLD, 12));
        th.setBackground(new Color(50, 55, 80));
        th.setForeground(new Color(200, 210, 255));
        th.setReorderingAllowed(false);

        // Center all cells
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        // Speedup column renderer — color green if >1, red if <1
        DefaultTableCellRenderer speedupRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                           boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                String s = val.toString();
                double v = Double.parseDouble(s.replace("x", ""));
                if (v > 1.05) {
                    setForeground(new Color(80, 220, 120));   // green = parallel faster
                } else if (v < 0.95) {
                    setForeground(new Color(255, 100, 100));  // red = parallel slower
                } else {
                    setForeground(new Color(200, 200, 200));
                }
                return this;
            }
        };

        // Striped row renderer for numeric cells
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
            if (c == 3 || c == 6) {
                tc.setCellRenderer(speedupRenderer);
            } else {
                tc.setCellRenderer(stripedRenderer);
            }
        }

        // Note label
        JLabel note = new JLabel("Speedup = Seq Time / Para Time   |   Green >1× = Parallel faster   |   Red <1× = Sequential faster",
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