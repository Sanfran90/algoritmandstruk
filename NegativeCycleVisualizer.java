import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
public class NegativeCycleVisualizer extends JFrame {

    static final int INF = Integer.MAX_VALUE / 2;
    static final String[] V = {"A", "B", "C", "D", "E", "F"};
    static final int N = V.length;

    // {откуда, куда, вес} - веса можно менять
    static final int[][] EDGES = {
            {0, 1, 1},
            {1, 3, -2},
            {2, 1, 3},
            {3, 0, -1},
            {3, 4, 2},
            {4, 5, -1},
            {2, 4, 4}
    };

    static final Point[] POS = {
            new Point(70, 90), new Point(220, 90), new Point(370, 90),
            new Point(70, 290), new Point(220, 290), new Point(370, 290)
    };

    final List<int[][]> snaps = new ArrayList<>();
    final List<boolean[][]> changed = new ArrayList<>();
    int step = 0;

    final GraphPanel graphPanel = new GraphPanel();
    final DefaultTableModel model = new DefaultTableModel(N, N + 1) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    final JTable table = new JTable(model);
    final JLabel status = new JLabel(" ", SwingConstants.CENTER);
    final JButton prev = new JButton("◀ Назад");
    final JButton next = new JButton("Далее ▶");
    final JButton reset = new JButton("Сброс");

    public NegativeCycleVisualizer() {
        super("Поиск отрицательного цикла (Флойд-Уоршелл)");
        compute();
        buildUi();
        refresh();
    }

    void compute() {
        int[][] d = new int[N][N];
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                d[i][j] = (i == j) ? 0 : INF;
        for (int[] e : EDGES) d[e[0]][e[1]] = e[2];

        snaps.add(copy(d));
        changed.add(new boolean[N][N]);

        for (int k = 0; k < N; k++) {
            boolean[][] ch = new boolean[N][N];
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++)
                    if (d[i][k] < INF && d[k][j] < INF && d[i][k] + d[k][j] < d[i][j]) {
                        d[i][j] = d[i][k] + d[k][j];
                        ch[i][j] = true;
                    }
            snaps.add(copy(d));
            changed.add(ch);
        }
    }

    static int[][] copy(int[][] a) {
        int[][] r = new int[a.length][];
        for (int i = 0; i < a.length; i++) r[i] = a[i].clone();
        return r;
    }

    void buildUi() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        String[] cols = new String[N + 1];
        cols[0] = "";
        System.arraycopy(V, 0, cols, 1, N);
        model.setColumnIdentifiers(cols);
        table.setRowHeight(32);
        table.setFont(new Font("SansSerif", Font.PLAIN, 16));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));
        table.setEnabled(false);
        table.setDefaultRenderer(Object.class, new CellRenderer());
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(420, 32 * (N + 1) + 6));
        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 10));
        right.add(sp, BorderLayout.NORTH);

        // Кнопки и статус
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttons.add(prev);
        buttons.add(next);
        buttons.add(reset);
        status.setFont(new Font("SansSerif", Font.BOLD, 15));
        status.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(status, BorderLayout.NORTH);
        bottom.add(buttons, BorderLayout.CENTER);

        prev.addActionListener(e -> { if (step > 0) { step--; refresh(); } });
        next.addActionListener(e -> { if (step < N) { step++; refresh(); } });
        reset.addActionListener(e -> { step = 0; refresh(); });

        add(graphPanel, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        setSize(960, 520);
        setLocationRelativeTo(null);
    }

    void refresh() {
        int[][] d = snaps.get(step);
        for (int i = 0; i < N; i++) {
            model.setValueAt(V[i], i, 0);
            for (int j = 0; j < N; j++)
                model.setValueAt(d[i][j] >= INF ? "∞" : String.valueOf(d[i][j]), i, j + 1);
        }

        if (step == 0) {
            status.setText("Исходная матрица смежности (∞ - ребра нет)");
        } else if (step < N) {
            status.setText("Итерация k = " + V[step - 1] + ": ищем пути через вершину " + V[step - 1]);
        } else {
            status.setText("Итерация k = " + V[N - 1] + ". " + (hasNegativeCycle(d)
                    ? "Вывод: в графе ЕСТЬ отрицательный цикл (на диагонали числа < 0)"
                    : "Вывод: отрицательных циклов нет"));
        }

        prev.setEnabled(step > 0);
        next.setEnabled(step < N);
        graphPanel.repaint();
        table.repaint();
    }

    static boolean hasNegativeCycle(int[][] d) {
        for (int i = 0; i < N; i++) if (d[i][i] < 0) return true;
        return false;
    }

    class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean sel,
                                                       boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, false, false, row, col);
            setHorizontalAlignment(CENTER);
            setForeground(Color.BLACK);
            setBackground(Color.WHITE);
            setFont(getFont().deriveFont(Font.PLAIN));
            if (col == 0) {
                setBackground(new Color(230, 230, 230));
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                int j = col - 1;
                if (changed.get(step)[row][j]) setBackground(new Color(190, 235, 190));
                if (row == j && snaps.get(step)[row][j] < 0) {
                    setBackground(new Color(240, 150, 150));
                    setFont(getFont().deriveFont(Font.BOLD));
                }
            }
            return this;
        }
    }

    class GraphPanel extends JPanel {
        static final int R = 24;

        GraphPanel() {
            setPreferredSize(new Dimension(450, 380));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int[][] d = snaps.get(step);
            boolean[] neg = new boolean[N];
            for (int i = 0; i < N; i++) neg[i] = d[i][i] < 0;
            int curK = (step >= 1) ? step - 1 : -1;

            // рёбра
            for (int[] e : EDGES) {
                boolean red = neg[e[0]] && neg[e[1]];
                drawEdge(g, POS[e[0]], POS[e[1]], e[2], red);
            }

            // вершины
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            for (int i = 0; i < N; i++) {
                Color fill = neg[i] ? new Color(240, 150, 150)
                        : (i == curK ? new Color(255, 210, 120) : new Color(200, 220, 250));
                g.setColor(fill);
                g.fill(new Ellipse2D.Double(POS[i].x - R, POS[i].y - R, 2 * R, 2 * R));
                g.setColor(Color.DARK_GRAY);
                g.setStroke(new BasicStroke(2f));
                g.draw(new Ellipse2D.Double(POS[i].x - R, POS[i].y - R, 2 * R, 2 * R));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(V[i], POS[i].x - fm.stringWidth(V[i]) / 2,
                        POS[i].y + fm.getAscent() / 2 - 2);
            }

            // легенда
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.setColor(Color.GRAY);
            g.drawString("оранжевая - текущая вершина k", 10, getHeight() - 28);
            g.drawString("красная - вершина на отрицательном цикле", 10, getHeight() - 12);
        }

        void drawEdge(Graphics2D g, Point a, Point b, int w, boolean red) {
            double dx = b.x - a.x, dy = b.y - a.y;
            double len = Math.hypot(dx, dy);
            double ux = dx / len, uy = dy / len;

            double x1 = a.x + ux * R, y1 = a.y + uy * R;
            double x2 = b.x - ux * R, y2 = b.y - uy * R;

            g.setColor(red ? new Color(200, 30, 30) : Color.DARK_GRAY);
            g.setStroke(new BasicStroke(2.2f));
            g.draw(new Line2D.Double(x1, y1, x2, y2));

            double ang = Math.atan2(dy, dx);
            int ah = 12;
            Path2D arrow = new Path2D.Double();
            arrow.moveTo(x2, y2);
            arrow.lineTo(x2 - ah * Math.cos(ang - Math.PI / 7), y2 - ah * Math.sin(ang - Math.PI / 7));
            arrow.lineTo(x2 - ah * Math.cos(ang + Math.PI / 7), y2 - ah * Math.sin(ang + Math.PI / 7));
            arrow.closePath();
            g.fill(arrow);

            double mx = (x1 + x2) / 2 - uy * 14, my = (y1 + y2) / 2 + ux * 14;
            g.setFont(new Font("SansSerif", Font.BOLD, 15));
            String s = String.valueOf(w);
            FontMetrics fm = g.getFontMetrics();
            g.setColor(w < 0 ? new Color(190, 40, 40) : new Color(30, 90, 170));
            g.drawString(s, (float) (mx - fm.stringWidth(s) / 2.0), (float) (my + fm.getAscent() / 2.0 - 2));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NegativeCycleVisualizer().setVisible(true));
    }
}