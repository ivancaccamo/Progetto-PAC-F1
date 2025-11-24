package com.ivancaccamo.progetto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Arrays;

public class SimulatorApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SimulatorApp::createAndShowGui);
    }

    private static void createAndShowGui() {
        JFrame frame = new JFrame("F1 Circuit Simulator - Monza");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        TrackPanel track = new TrackPanel();

        JPanel controls = new JPanel();
        JButton startBtn = new JButton("Start");
        JButton stopBtn = new JButton("Stop");
        JButton resetBtn = new JButton("Reset");

        JSlider speedSlider = new JSlider(1, 200, 40);
        speedSlider.setPreferredSize(new Dimension(200, 40));
        speedSlider.setMajorTickSpacing(50);
        speedSlider.setMinorTickSpacing(10);
        speedSlider.setPaintTicks(true);

        controls.add(startBtn);
        controls.add(stopBtn);
        controls.add(resetBtn);
        controls.add(new JLabel("Speed"));
        controls.add(speedSlider);

        startBtn.addActionListener(e -> track.start());
        stopBtn.addActionListener(e -> track.stop());
        resetBtn.addActionListener(e -> track.reset());
        speedSlider.addChangeListener(e -> track.setSpeed(speedSlider.getValue()));

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(track, BorderLayout.CENTER);
        frame.getContentPane().add(controls, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static class TrackPanel extends JPanel implements ActionListener {
        /*
         * Punti normalizzati (x,y in [0,1]) che approssimano la pianta del circuito di Monza.
         * Sono in senso orario, partendo circa dall’uscita della Parabolica / inizio del rettilineo principale.
         */
        // Punti normalizzati (x,y in [0,1]) che approssimano Monza
// senso orario, partendo dalla zona di Curva Grande / 03
    // Punti normalizzati (x,y in [0,1]) che approssimano Monza
// senso orario, partenza circa sul rettilineo di partenza/arrivo
private final double[][] normPoints = new double[][]{
        // Rettilineo di partenza / settore 1 (basso, da destra verso sinistra)
        {0.60, 0.80},
        {0.56, 0.80},
        {0.52, 0.80},
        {0.48, 0.80},
        {0.44, 0.80},
        {0.40, 0.80},
        {0.36, 0.80},
        {0.32, 0.80},
        {0.28, 0.80},
        {0.24, 0.80},
        {0.22, 0.90},

        // Curva Grande (03): grande arco verso sinistra che sale
        {0.21, 0.77},
        {0.20, 0.74},
        {0.20, 0.70},
        {0.20, 0.66},

        // Lato sinistro quasi verticale (verso 04–05–06–07)
        {0.19, 0.62},
        {0.19, 0.58},
        {0.19, 0.54},
        {0.19, 0.50},
        {0.20, 0.46},
        {0.21, 0.42},
        {0.22, 0.38},

        // Zona in alto a sinistra prima della diagonale (Lesmo / inizio Serraglio)
        {0.24, 0.34},
        {0.26, 0.30},
        {0.29, 0.26},
        {0.32, 0.22},
        {0.36, 0.20},

        // Diagonale verso destra (Serraglio → Variante Ascari)
        {0.40, 0.20},
        {0.44, 0.23},
        {0.48, 0.28},
        {0.51, 0.34},
        {0.54, 0.40},
        {0.56, 0.46},
        {0.58, 0.51},
        {0.60, 0.54},

        // Piccola chicane 08–09–10 + inizio rettilineo settore 3
        {0.64, 0.53},
        {0.68, 0.51},
        {0.72, 0.50},
        {0.76, 0.50},
        {0.80, 0.50},
        {0.84, 0.51},
        {0.88, 0.53},

        // Curva Parabolica (ultima curva, grande arco a destra)
        {0.90, 0.56},
        {0.92, 0.60},
        {0.93, 0.64},
        {0.94, 0.68},
        {0.94, 0.72},
        {0.93, 0.75},
        {0.91, 0.78},
        {0.88, 0.80},
        {0.84, 0.81},
        {0.80, 0.81},
        {0.76, 0.81},
        {0.72, 0.81},
        {0.66, 0.81}
        // l'ultimo punto verrà collegato al primo da closePath()
};

        private Point[] scaledPoints;
        private double[] segLengths;
        private double[] cumLengths;
        private double totalLength = 0.0;

        private double pos = 0.0;  // distanza lungo il tracciato in px
        private double speed = 6.0; // px per tick
        private final Timer timer;

        TrackPanel() {
            setPreferredSize(new Dimension(1000, 700));
            setBackground(new Color(34, 139, 34)); // prato
            timer = new Timer(16, this); // ~60 FPS

            addComponentListener(new java.awt.event.ComponentAdapter() {
                public void componentResized(java.awt.event.ComponentEvent evt) {
                    recomputePath();
                }
            });
            recomputePath();
        }

        private void recomputePath() {
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) {
                w = getPreferredSize().width;
                h = getPreferredSize().height;
            }

            int margin = 60;
            int pw = w - margin * 2;
            int ph = h - margin * 2;

            int n = normPoints.length;
            scaledPoints = new Point[n];
            for (int i = 0; i < n; i++) {
                int x = margin + (int) Math.round(normPoints[i][0] * pw);
                int y = margin + (int) Math.round(normPoints[i][1] * ph);
                scaledPoints[i] = new Point(x, y);
            }

            segLengths = new double[n];
            cumLengths = new double[n + 1];
            totalLength = 0.0;
            for (int i = 0; i < n; i++) {
                Point p1 = scaledPoints[i];
                Point p2 = scaledPoints[(i + 1) % n];
                double dx = p2.x - p1.x;
                double dy = p2.y - p1.y;
                double len = Math.hypot(dx, dy);
                segLengths[i] = len;
                cumLengths[i] = totalLength;
                totalLength += len;
            }
            cumLengths[n] = totalLength;

            pos = pos % totalLength;
            if (pos < 0) pos += totalLength;
        }

        void start() { if (!timer.isRunning()) timer.start(); }
        void stop() { if (timer.isRunning()) timer.stop(); }
        void reset() { stop(); pos = 0.0; repaint(); }
        void setSpeed(int sliderValue) {
            this.speed = 0.5 + sliderValue * 0.15;
        }

        private Point2D getPointAtDistance(double d) {
            if (totalLength <= 0) return new Point2D.Double(0, 0);

            double dd = d % totalLength;
            if (dd < 0) dd += totalLength;

            int idx = Arrays.binarySearch(cumLengths, 0, cumLengths.length, dd);
            if (idx >= 0) {
                int i = Math.min(idx, scaledPoints.length - 1);
                return new Point2D.Double(scaledPoints[i].x, scaledPoints[i].y);
            } else {
                int insertion = -idx - 1;
                int seg = Math.max(0, insertion - 1);
                double segStart = cumLengths[seg];
                double segLen = segLengths[seg];
                double t = (dd - segStart) / segLen;
                Point p1 = scaledPoints[seg];
                Point p2 = scaledPoints[(seg + 1) % scaledPoints.length];
                double x = p1.x + (p2.x - p1.x) * t;
                double y = p1.y + (p2.y - p1.y) * t;
                return new Point2D.Double(x, y);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // pista
            g2.setColor(new Color(80, 80, 80));
            Stroke trackStroke = new BasicStroke(48f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            g2.setStroke(trackStroke);
            Path2D trackPath = new Path2D.Double();
            if (scaledPoints != null && scaledPoints.length > 0) {
                trackPath.moveTo(scaledPoints[0].x, scaledPoints[0].y);
                for (int i = 1; i < scaledPoints.length; i++) {
                    trackPath.lineTo(scaledPoints[i].x, scaledPoints[i].y);
                }
                trackPath.closePath();
                g2.draw(trackPath);
            }

            // bordo interno
            g2.setColor(Color.LIGHT_GRAY);
            g2.setStroke(new BasicStroke(2f));
            if (scaledPoints != null && scaledPoints.length > 0) {
                g2.draw(trackPath);
            }

            // linea tratteggiata centrale
            if (scaledPoints != null && scaledPoints.length > 0) {
                Stroke dash = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                        0f, new float[]{8f, 8f}, 0f);
                g2.setStroke(dash);
                g2.setColor(Color.WHITE);
                Path2D center = new Path2D.Double();
                center.moveTo(scaledPoints[0].x, scaledPoints[0].y);
                for (int i = 1; i < scaledPoints.length; i++) center.lineTo(scaledPoints[i].x, scaledPoints[i].y);
                center.closePath();
                g2.draw(center);
            }

            // “macchina”
            Point2D p = getPointAtDistance(pos);
            double x = p.getX();
            double y = p.getY();
            int carSize = 14;
            g2.setColor(new Color(0, 0, 0, 100));
            g2.fillOval((int) x - carSize / 2 + 3, (int) y - carSize / 2 + 3, carSize, carSize);
            g2.setColor(Color.RED);
            g2.fillOval((int) x - carSize / 2, (int) y - carSize / 2, carSize, carSize);

            g2.dispose();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            pos += speed;
            if (totalLength > 0 && pos > totalLength) pos -= totalLength;
            repaint();
        }
    }
}
