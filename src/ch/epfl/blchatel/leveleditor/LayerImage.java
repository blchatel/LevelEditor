package ch.epfl.blchatel.leveleditor;

import ch.epfl.blchatel.leveleditor.io.DefaultFileSystem;
import ch.epfl.blchatel.leveleditor.io.FileSystem;
import ch.epfl.blchatel.leveleditor.io.ResourceFileSystem;
import ch.epfl.blchatel.leveleditor.swing.ColorMap;
import ch.epfl.blchatel.leveleditor.swing.ComposedIcon;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class LayerImage {

    /// The icon resolution in pixel
    public final static int CELL_RESOLUTION = 64;
    private final static int ICON_RESOLUTION = 64;
    private final static int ICON_GAP = 5;

    private final static ImageIcon NULL_ICON = buildNullIcon();
    public final ImageIcon icon;
    public final ComposedIcon vsIcon;
    public final BufferedImage background, foreground, behavior;

    public final int pixelWidth, pixelHeight, cellWidth, cellHeight;

    private static ImageIcon buildNullIcon(){
        FileSystem fileSystem = new ResourceFileSystem(DefaultFileSystem.INSTANCE);
        return new ImageIcon(fileSystem.readImage("icons/alpha_64.png"));
    }

    /**
     * The default LayerImage constructor
     *
     * @param background (Image) : the background layer (not null)
     * @param foreground (Image) : the foreground layer (may be null)
     * @param behavior   (Image) : the behavior layer (not null)
     */
    public LayerImage(Image background, Image foreground, Image behavior) {

        if(background == null || behavior == null) {
            throw new IllegalArgumentException("background and behavior images should not be null");
        }

        this.background = LayerImage.toBufferedImage(background);
        this.foreground = foreground == null ? null : LayerImage.toBufferedImage(foreground);
        this.behavior = LayerImage.toBufferedImage(behavior);

        if(this.background == null || this.behavior == null) {
            throw new IllegalArgumentException("background or behavior encounter problem while converting into buffered image");
        }

        icon = new ImageIcon(background.getScaledInstance(ICON_RESOLUTION, ICON_RESOLUTION, Image.SCALE_DEFAULT));

        ImageIcon foregroundIcon = NULL_ICON;
        if(foreground != null){
            foregroundIcon =  new ImageIcon(foreground.getScaledInstance(ICON_RESOLUTION, ICON_RESOLUTION, Image.SCALE_DEFAULT));
        }
        ImageIcon behaviorIcon = new ImageIcon(behavior.getScaledInstance(ICON_RESOLUTION, ICON_RESOLUTION, Image.SCALE_DEFAULT));
        vsIcon = new ComposedIcon(ICON_GAP, icon, foregroundIcon, behaviorIcon);

        pixelWidth = this.background.getWidth();
        pixelHeight = this.background.getHeight();
        cellWidth = this.behavior.getWidth();
        cellHeight = this.behavior.getHeight();

        if (!isValid()) {
            System.out.println("The layered image is not valid");
        }
    }

    private boolean isValid() {
        // If background and foreground have not the same dimension
        if (foreground != null && (background.getWidth() != foreground.getWidth() || background.getHeight() != foreground.getHeight()))
            return false;
        // Check the resolution
        if (cellWidth * CELL_RESOLUTION != pixelWidth || cellHeight * CELL_RESOLUTION != pixelHeight)
            return false;

        return true;
    }

    public boolean isValid(int maxCellsX, int maxCellsY) {

        // If the behavior size overflow the available size
        if (cellWidth < 1 || cellWidth > maxCellsX || cellHeight < 1 || cellHeight > maxCellsY)
            return false;

        return isValid() && !(pixelWidth < CELL_RESOLUTION || pixelWidth > maxCellsX * CELL_RESOLUTION
                || pixelHeight < CELL_RESOLUTION || pixelHeight > maxCellsY * CELL_RESOLUTION
                || pixelWidth % CELL_RESOLUTION != 0 || pixelHeight % CELL_RESOLUTION != 0);
    }


    /**
     * Convert an Image to a BufferedImage
     *
     * @param img (Image) : the input Image (not null)
     * @return (BufferedImage): the output Image (may be null)
     */
    private static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        int width = img.getWidth(null);
        int height = img.getHeight(null);

        if(width < 1 || height < 1)
            return null;

        // Create a buffered image with transparency
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bufferedImage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bufferedImage;
    }


    public static LayerImage getFromFile(File file) {

        try {
            if (!file.isFile() || !file.getName().toLowerCase().endsWith(".lve"))
                throw new IOException("Error while reading : file is not .lve format");

            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;

            String[] strings = new String[4];

            for (int i = 0; i < 4; i++) {
                line = bufferedReader.readLine();
                if (line == null) {
                    throw new IOException("Error while reading : file has not enough line");
                }
                strings[i] = line;
            }
            fileReader.close();

            System.out.println("Contents of file:");
            for (String s : strings)
                System.out.println(s);

            String absolutePath = file.getAbsolutePath();
            String filePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator) + 1);

            Image backgroundImage = new ImageIcon(filePath + strings[1]).getImage();
            Image foregroundImage = new ImageIcon(filePath + strings[2]).getImage();
            Image behaviorImage = new ImageIcon(filePath + strings[3]).getImage();

            if (backgroundImage == null || foregroundImage == null || behaviorImage == null)
                throw new IOException("One of the layer is null");

            return new LayerImage(backgroundImage, foregroundImage, behaviorImage);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static LayerImage createImage(int w, int h) {

        Image backgroundImage = new BufferedImage(w * CELL_RESOLUTION, h * CELL_RESOLUTION, BufferedImage.TYPE_INT_ARGB);
        Image foregroundImage = new BufferedImage(w * CELL_RESOLUTION, h * CELL_RESOLUTION, BufferedImage.TYPE_INT_ARGB);
        Image behaviorImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        return new LayerImage(backgroundImage, foregroundImage, behaviorImage);
    }


    public static boolean saveToFile(LayerImage image, File lveFile) {

        if(image == null)
            return false;

        if (lveFile != null && lveFile.getName().toLowerCase().endsWith(".lve")) {
            try {
                String absolutePath = lveFile.getAbsolutePath();
                String filePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator) + 1);
                String fileName = lveFile.getName().replaceFirst("[.][^.]+$", "");

                List<String> lines = new ArrayList<>();
                lines.addAll(Arrays.asList(lveFile.getName(), "backgrounds/" + fileName + ".png", "foregrounds/" + fileName + ".png", "behaviors/" + fileName + ".png", "Info:"));
                lines.addAll(Arrays.asList(ColorMap.createMap(image)));

                Path file = Paths.get(lveFile.getAbsolutePath());
                Files.write(file, lines, Charset.forName("UTF-8"));

                File backgroundFile = new File(filePath + lines.get(1));
                backgroundFile.getParentFile().mkdirs();
                ImageIO.write(image.background, "png", backgroundFile);

                File foregroundFile = new File(filePath + lines.get(2));
                foregroundFile.getParentFile().mkdirs();
                ImageIO.write(image.foreground, "png", foregroundFile);

                File behaviorFile = new File(filePath + lines.get(3));
                behaviorFile.getParentFile().mkdirs();
                ImageIO.write(image.behavior, "png", behaviorFile);

                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}