# The Level Editor Project

### Author
Bastien Chatelain 

`bastien.chatelain@epfl.ch`


### Description

This program allows you to create some new Grid Levels textures for Mini-Projet 2.

In this program, a level is defined by three images: 
    
* the **Behavior** image has one pixel for each cell of the grid. It has `w x h px` size. The behavior allows you to 
indicate cell content or specific behavior using a defined [color map]() 

* the **Backgrounds** image has `64px` resolution which means each cell of the grid is `64 x 64 px`. The final image
has size `W x D = 64w x 64h px`. We assume the background image is static and will be painted at first for each frame. 

* the **Foregrounds** _(optional)_ image has the exact same dimensions and uses as the background image. 
We assume the foreground image will be the last painted for each frame.
The foreground image is generally a subset of the background completed with transparent pixels which will be painted 
after the dynamic elements (i.e. player).
This simulate structures closer to the camera. In other words, the dynamic elements are moving between the background
and foreground layer.   

### Get Started

* Level Editor is a Java Application which means it can run on any compatible OS. 
* Level Editor works only with `.png` images and specific `.lve` files

In order to use the projet, you have two choices :
1. Run directly the "`.jar` file" with Java
2. Run the program in an IDE (recommended for beginners)\
Recommended IDE : Intellij IDEA

### First method, not recommended for beginners
#### Prerequisites

* Java 8 (JDK)
* (Optional) git 

#### Download the source code 

You can either download it from https://github.com/veski-dev/LevelEditor/tree/master (easier)\
Then click on `Code > Download ZIP`

Or if you have `git` installed:\
Go to your desired directory :\
```cd path/to/parent/directory/```\
And clone the project :\
```git clone https://github.com/blchatel/LevelEditor.git```

#### Execute `.jar` file

* Ensure the "Allow executing file as program" option is enable
* Run the program either by double clicking on it or using one of the following commands: 
    * `java -jar levelEditor-v1-0-1.jar`
    * `java -jar levelEditor-v1-0-1.jar level.lve`
    
    Notice the level editor version may be different

### Second method, recommended for begginers

#### IntelliJ IDEA

0. If you have a project still opened, close it : `File > Close Project` (Top-left of  the window)
1. If you have already downloaded the project
- Open it with `Open` (Top-right)
- Select the project folder
2. If you have not already downloaded it : `Get from VCS` (Top-right)\
- Select `git` in `Version control`\
- Put `https://github.com/blchatel/LevelEditor.git` in `URL`\
- Select your desired directory
- Click on `Clone`
3. Set `Java 8` SDK
- Click on `"Setting icon" (Top-right) > Project Structure`
- Set SDK version to `1.8` (recommended vendor : `Eclipse Temurin AdoptOpenJDK HotSpot`)
4. Verify if `resources/` folder icon is marked as `Resources Root`\
To check simply `right-click` on `resources/` folder,\
Then at the bottom `Mark Directory as > Resources Root`

#### Eclipse (very unprecise)

* Create a new Java Project.
* Uncheck the "default location" and indicate the LevelEditor folder location
* The project name should automatically take the value "LevelEditor"
* Be sure java jre 1.8 is used
* Click on finish and you can know run the project as Java Application
  
### Use LevelEditor

1. You can either create a new Level (File / new) or open and edit an existing `.lve` file
2. Use the proposed brushes to create the texture. The brush's background, foreground and behavior are 
by default directly applied on the corresponding images. 
3. You can switch tab to see intermediate results
4. You can control which part of the brush is applied playing with checkboxes.   
5. Save the Level as a `.lve` file

Assume you save "Level1" into "levels" directory, you get:
* `levels/Level1.lve`
* `levels/Backgrounds/Level1.png`
* `levels/Foregrounds/Level1.png`
* `levels/Behaviors/Level1.png`

The Content of `Level1.lve` file is typically:
```
Level1.lve 
Backgrounds/Level1.png 
Foregrounds/Level1.png 
Behaviors/Level1.png 
Info: 
Wall: (x1, y1) (x2, y2) 
Door: (x3, y3) (x4, y4) 
...
```
Which are respectively:
* the filename 
* the relative path to background, foreground and behavior png images
* some informations about the color map. Assuming the `(0, 0)` cell is the grid's bottom-left corner in 
above example, we know a wall in `(x1, y1)`, a door in cell `(x3, y3)`, etc.  
Notice the information mapping is not used by the program.  

#### Add new Brushes

* Add your new brushes backgrounds, (foregrounds) and behaviors into the corresponding
source `res` directory.  
Notice: 
    * The brushes are not limited to one single cell size. 
    * A brush behavior can be any cell size (1x1, 1x2, 2x1, 2x2, 3x1, etc.)
    * The program assumes your brushes backgrounds (foregrounds) have 64px 
    resolution as explained above.
* Run the `ComputeBrushesRes` program indicating as `args` where is the Brushes resource
directory. This will compute for you the enumeration content you need to replace.
* Copy the output enumeration body and replace the equivalent it into `BrushesRes` enum file. 
* Build and run again `LevelEditor`       

### The Color Map)
 
LABEL   | HTML     | Integer  
------- | -------- | ---------:
Wall    | 0x000000 | -16777216 
Interact| 0xffff00 |      -256      
Door    | 0xff0000 | -65536     
Indoor  | 0xffffff | -1     
Outdoor | 0x28a745 | -14112955      
Water   | 0x0000ff | -16776961      

### Samples

Find some samples into this repertory. Feel free to use and/or edit any of them

### Reference

* [Java 8 JDK](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Eclipse](http://www.eclipse.org)
* [IntelliJ IDEA](https://www.jetbrains.com/idea/)
* [Git](https://git-scm.com/)
* [Kenney Assets](https://kenney.nl/assets/rpg-base)

