package ch.epfl.blchatel.leveleditor;

import ch.epfl.blchatel.leveleditor.io.*;
import ch.epfl.blchatel.leveleditor.swing.ContentPane;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * LevelEditor Application
 * Copyright 2018 Bastien Chatelain
 *
 */
public class LevelEditor {

    public final static String appName = "Level Editor";
    public final static String versionId = "v1.0.1";
    public final static int version = 1;


    // Windows Dimension
    private final static Dimension WINDOW_DIMENSION = new Dimension(1200, 800);

    /**
     * Main Entry Point
     * @param args (Array of String) : Arguments
     *             [0] - absolute path a .lve file to open with the editor (optional)
     */
    public static void main(String[] args){

        if(args.length > 1)
            throw new IllegalArgumentException(appName + " " + versionId + " is waiting either none or one single '.lve' argument");
        if(args.length == 1 && !args[0].toLowerCase().endsWith(".lve"))
            throw new IllegalArgumentException(appName + " " + versionId + " is waiting either none or one single '.lve' argument");

        File file = null;
        if(args.length == 1)
            file = new File(args[0]);

        // Init the application window
        final JFrame frame = new JFrame("LevelEditor");
        frame.setMinimumSize(WINDOW_DIMENSION);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Set content and visible
        ContentPane contentPane = new ContentPane(WINDOW_DIMENSION);
        contentPane.initArgsDisplay(file);
        frame.setContentPane(contentPane);
        frame.setVisible(true);
    }
}
