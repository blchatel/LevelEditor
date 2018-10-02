package ch.epfl.blchatel.leveleditor;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;


public class ComputeBrushesRes {


    /**
     * Compute the BrushesRes Enumeration values as a String
     * @see ch.epfl.blchatel.leveleditor.io.BrushesRes
     * @param args (Array of String): Assume of length 1 or 4
     *             [0] - absolute path to brushes directory
     *             [1] - backgrounds sub-directory (optional)(default="Backgrounds")
     *             [2] - foregrounds sub-directory (optional)(default="Foregrounds")
     *             [3] - behaviors sub-directory (optional)(default="Behaviors")
     */
    public static void main(String[] args){

        if(args.length < 1){
            throw new IllegalArgumentException("one argument ('Brushes path')is expected");
        }

        String brushesPath = args[0];
        if(!brushesPath.endsWith(File.separator))
            brushesPath += File.separator;

        System.out.println("Start computing BrushesRes Enum");
        System.out.println("one "+ brushesPath);

        String backgroundsSubPath = "Backgrounds";
        String foregroundsSubPath = "Foregrounds";
        String behaviorsSubPath = "Behaviors";

        if(args.length == 4){
            backgroundsSubPath = args[1];
            foregroundsSubPath = args[2];
            behaviorsSubPath = args[3];
        }

        File backgroundFolder = new File(brushesPath + backgroundsSubPath);
        File foregroundFolder = new File( brushesPath + foregroundsSubPath);
        File behaviorFolder = new File(brushesPath + behaviorsSubPath);
        File[] backgroundFiles = backgroundFolder.listFiles();
        File[] foregroundFiles = foregroundFolder.listFiles();
        File[] behaviorFiles = behaviorFolder.listFiles();

        if(backgroundFiles == null || foregroundFiles == null || behaviorFiles == null){
            throw new IllegalArgumentException("At least one the following folders is not found: \n   Backgrounds:" + (brushesPath + backgroundsSubPath)
                    + "\n   Foregrounds:" + (brushesPath + foregroundsSubPath)
                    + "\n   Behaviors:" + (brushesPath + behaviorsSubPath) + "\n"
            );
        }

        if(backgroundFiles.length != behaviorFiles.length){
            throw new IllegalArgumentException("Please check brushes, some are missing : \n" +
                    "#Background: " + backgroundFiles.length + ", #behavior: " + behaviorFiles.length);
        }

        System.out.println("with: \n   Backgrounds:" + (brushesPath + backgroundsSubPath)
                + "\n   Foregrounds:" + (brushesPath + foregroundsSubPath)
                + "\n   Behaviors:" + (brushesPath + behaviorsSubPath) + "\n"
        );

        Comparator<File> fileComparator = Comparator.comparing(File::getName);
        Arrays.sort(backgroundFiles, fileComparator);
        Arrays.sort(behaviorFiles, fileComparator);
        Arrays.sort(foregroundFiles, fileComparator);

        System.out.println("    // Please Copy the following output to the enum BrushesRes ");

        for(int i = 0; i < backgroundFiles.length; i++){

            File backgroundFile = backgroundFiles[i];
            File behaviorFile = behaviorFiles[i];
            String name = backgroundFile.getName();

            if((!backgroundFile.isFile() || !name.toLowerCase().endsWith(".png"))){
                throw new IllegalStateException("Brush must be png files :" + name);
            }

            if(!name.equals(behaviorFile.getName())){
                throw new IllegalStateException("Brush name must have the sames names in Background and Behavior folders");
            }

            String enumEntry = "\t" + name.toUpperCase().substring(0, name.lastIndexOf('.')).replace('.', '_');
            enumEntry += "(\""+name+"\")";
            enumEntry += (i == backgroundFiles.length-1) ? ";" : ",";
            System.out.println(enumEntry);
        }
    }
}
