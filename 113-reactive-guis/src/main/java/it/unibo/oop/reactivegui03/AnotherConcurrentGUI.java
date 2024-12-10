package it.unibo.oop.reactivegui03;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Third experiment with reactive gui.
 */
@SuppressWarnings("PMD.AvoidPrintStackTrace")
public final class AnotherConcurrentGUI extends JFrame {
    private static final long serialVersionUID = 3958307097838615082L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel display = new JLabel("0");
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final JButton stop = new JButton("stop");
    private final Agent agent;

    private void disableCountAndButtons() {
        this.agent.stopCounting();
        this.up.setEnabled(false);
        this.down.setEnabled(false);
        this.stop.setEnabled(false);
    }

    /**
     * Builds a new AnotherConcurrentGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        this.agent = new Agent();
        new Thread(agent).start();
        /*
         * Register listeners
         */
        down.addActionListener(e -> agent.downCounting());
        up.addActionListener(e -> agent.upCounting());
        stop.addActionListener(e -> {
            this.disableCountAndButtons();
        });
    }

    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private final class Agent implements Runnable {
        private static final int SLEEP_TIME = 100;
        private volatile boolean stop;
        private volatile boolean upCount = true;
        private int counter;

        @Override
        public void run() {
            final TimeoutAgent timeoutAgent = new TimeoutAgent();
            new Thread(timeoutAgent).start();
            while (!this.stop) {
                try {
                    if (timeoutAgent.isTimeToQuit()) {
                        SwingUtilities.invokeLater(AnotherConcurrentGUI.this::disableCountAndButtons);
                    } else {
                        this.counter += this.upCount ? 1 : -1;
                        final var nextText = Integer.toString(this.counter);
                        SwingUtilities.invokeLater(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                        Thread.sleep(SLEEP_TIME);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }

        /**
         * External command to set counting down.
         */
        public void downCounting() {
            this.upCount = false;
        }

        /**
         * External command to set counting up.
         */
        public void upCounting() {
            this.upCount = true;
        }
    }

    /*
     * The timeout agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private final class TimeoutAgent implements Runnable {
        private static final int TIMEOUT = 10_000;
        private static final int SLEEP_TIME = 500;
        private volatile boolean isTimeToQuit;

        @Override
        public void run() {
            final long startTime = new Date().getTime();
            while (!this.isTimeToQuit) {
                try {
                    final long now = new Date().getTime();
                    if (now - startTime >= TIMEOUT) {
                        this.isTimeToQuit = true;
                    }
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        /**
         * Get information when tha application should stop to count.
         * 
         * @return true is timeout is expired
         */
        public boolean isTimeToQuit() {
            return this.isTimeToQuit;
        }
    }
}
