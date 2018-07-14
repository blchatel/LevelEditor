package ch.epfl.blchatel.leveleditor;

import ch.epfl.blchatel.leveleditor.swing.ContentPane;

import javax.swing.*;
import java.awt.*;

/**
 * LevelEditor Application
 * Copyright 2018 Bastien Chatelain
 *
 */
public class LevelEditor {

    // Windows Dimension
    private final static Dimension WINDOW_DIMENSION = new Dimension(1200, 800);

    /**
     * Main Entry Point
     * @param args (Array of String) : Arguments (not used)
     */
    public static void main(String[] args){

        // Init the application window
        final JFrame frame = new JFrame("LevelEditor");
        frame.setMinimumSize(WINDOW_DIMENSION);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Set content and visible
        frame.setContentPane(new ContentPane(WINDOW_DIMENSION));
        frame.setVisible(true);
    }
}
