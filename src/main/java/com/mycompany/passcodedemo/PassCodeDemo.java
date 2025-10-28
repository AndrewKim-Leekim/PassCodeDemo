package com.mycompany.passcodedemo;

import javax.swing.SwingUtilities;

/**
 * Application entry point that launches the PassCode demonstration UI.
 */
public final class PassCodeDemo {

    private PassCodeDemo() {
        // Utility class
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
