package ch.epfl.blchatel.leveleditor.swing;

import ch.epfl.blchatel.leveleditor.LayerImage;

import javax.swing.*;



public enum ColorMap {

    WALL("Wall", "0x000000", -16777216),
    INTERACT("Interact", "0xffff00", -256),
    DOOR("Door", "0xff0000", -65536),
    INDOOR("Indoor", "0xffffff", -1),
    OUTDOOR("Outdoor", "0x28a745", -14112955),
    WATER("Water", "0x0000ff", -16776961);

    final String label;
    final String htmlValue;
    final int intValue;

    ColorMap(String label, String htmlValue, int intValue){
        this.label = label;
        this.htmlValue = htmlValue;
        this.intValue = intValue;
    }

    public static void showMap(){

        StringBuilder colorMap = new StringBuilder("Color Map : \n" +
                "--------------------------------------------------------------------------------\n" +
                "  LABEL\t| HTML\t| Integer\n" +
                "--------------------------------------------------------------------------------\n");

        for(ColorMap cMap : ColorMap.values())
            colorMap.append("  ").append(cMap.label).append(":\t| ").append(cMap.htmlValue).append("\t| ").append(cMap.intValue).append("\n");

        JTextArea textArea = new JTextArea(colorMap.toString());
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(null, textArea, "Color Map", JOptionPane.INFORMATION_MESSAGE);
    }

    public static String[] createMap(LayerImage image){

        if(image == null || image.behavior == null)
            return null;

        ColorMap[] keys = ColorMap.values();
        String[] cellMaps = new String[keys.length];

        for(int i = 0; i < keys.length; i++)
            cellMaps[i] = keys[i].label + ": ";

        for(int r = 0; r < image.cellHeight; r++)
        for(int c = 0; c < image.cellWidth; c++) {
            for(int i = 0; i < keys.length; i++) {
                if(keys[i].intValue == image.behavior.getRGB(c, r))
                    cellMaps[i] += "("+(c)+","+(image.cellHeight-r-1)+") ";

            }
        }
        return cellMaps;
    }
}
