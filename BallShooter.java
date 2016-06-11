/*
 * Adam R. Abbott
* CMSC 325
* Professor Karmaker
* Final Project
* Requirements:  Using the 3D graphics and associated Physics engine develop an 
* arcade style game that allows a user to fire at various types of targets and 
* be awarded points when the bullet or missile collides with the target.  
* The following functionality is required:
1. At least 4 types of targets should be displayed in the game. As targets 
* are destroyed they should recreated at another location in the scene automatically.  
2. Each of the targets should be moving in the scene.
3. At least one of the targets should have gravity/physics applied.
4. Bullets or missiles should be unlimited.
5. A Heads-up Display should be provided with a minimum of score totals for 
* each target, total score, high score, and the number of bullets/missiles fired.
6. High score should be stored in an ASCII file. Optionally, scores can be 
* kept in a database with ability to have initials and scores for top 5 stored and displayed.
7. Simple directions for running the game should be included on the screen
8. A game timer should be embedded limiting a game session to 5 minutes or less.
9. Coding conventions should be followed based on the programming language used.
* 
* I utilized the bae source code from my first BouncingBall game (Project 1) and 
* the code for the JMonkey HelloPhysics and HelloPicking tutorial as a foundation 
* for this project.  However, the code is modified from it.
* 
* Sources:
* 
* JMonkeyEngine 3 Tutorial (13) - Hello Physics. (n.d.). 
* Retrieved April 5, 2015, 
* from http://wiki.jmonkeyengine.org/doku.php/jme3:beginner:hello_physics
* 
* JMonkeyEngine 3 Tutorial (13) - Hello Picking. (n.d.). 
* Retrieved April 5, 2015, 
* from http://wiki.jmonkeyengine.org/doku.php/jme3:beginner:hello_picking
 */

package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JOptionPane;
import org.lwjgl.openal.AL;
 

public class BallShooter extends SimpleApplication{
 
  public static void main(String args[]) {
    //sets desired display settings    
    AppSettings settings = new AppSettings(true);
    settings.setResolution(1024,768);
    
    BallShooter app = new BallShooter();
    app.setSettings(settings);
    app.start();
  }
    //private java.io.PrintWriter output;
    private java.io.BufferedWriter output;
    private BulletAppState bulletAppState;
    //Declare all materials
    Material wall_mat, trans_wall, side_wall, back_wall, ceiling_mat, floor_mat;
    //Declare all boxes used for floor and walls
    private static final Box wall, wall1, wall2, wall3, frontWall, floor;
    //Declare all variables to set physics controls
    private RigidBodyControl wall_phy, wall1_phy, wall2_phy, wall3_phy, frontWall_phy,
            floor_phy;
    //Various variables used
    private AudioNode singleAudio, scoreAudio;
    BitmapText ch, gameTime, shotsFired, totalHits, accY, totalScore, ballsDestroyed;
    private static final float wallLength = .5f;
    private static final float wallWidth = 20f;
    private static final float wallHeight = 20f;
    float lastShotSince = 0;
    private int hugeBall_hit = 0;
    private int largeBall_hit = 0;
    private int medBall_hit = 0;
    private int smallBall_hit = 0;
    private int total_shots = 0;
    private int total_hits = 0;
    private int hugeBallsDestroyed = 0;
    private int largeBallsDestroyed = 0;
    private int medBallsDestroyed = 0;
    private int smallBallsDestroyed = 0;
    private int score = 0;
    private float accuracy = 0;
    private int gameTimer = 0;
    private Geometry mark;
    Node target_balls;
    private BallTargets targets;
    boolean autoFire=false;
    //Sets size/dimensions for all shapes
    static {
    wall = new Box(wallLength, wallWidth, wallHeight);
    wall.scaleTextureCoordinates(new Vector2f(1f, .5f));
    wall1 = new Box(wallLength, wallWidth, wallHeight);
    wall1.scaleTextureCoordinates(new Vector2f(1f, .5f));
    wall2 = new Box(wallWidth, wallLength, wallHeight);
    wall2.scaleTextureCoordinates(new Vector2f(1f, .5f));
    wall3 = new Box(wallHeight, wallWidth, wallLength);
    wall3.scaleTextureCoordinates(new Vector2f(1f, .5f));
    frontWall = new Box(wallHeight, wallWidth, wallLength);
    frontWall.scaleTextureCoordinates(new Vector2f(1f, .5f));
    floor = new Box(50f, 0.5f, 20f);
    floor.scaleTextureCoordinates(new Vector2f(3, 3));
    }
    
    @Override
    public void simpleInitApp() {
        //Sets up the physics space and sets gravity globally to earth standard
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().setAccuracy(0.005f);
        bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0f,-9.81f,0f));
        //This sets the camera view
        cam.setLocation(new Vector3f(0f, 0f, 100f));
        cam.lookAt(Vector3f.ZERO, Vector3f.ZERO);

        //Create the Environment, HUD, prepare audio sounds, controls, and shot spotter
        initCube();
        initDisplay();
        initAudio();
        initKeys();
        initSpotter();
        //Create a Node for the target Balls and put the balls in it
        target_balls = new Node("target_balls");
        targets = new BallTargets(assetManager, rootNode, target_balls, bulletAppState);
        //Simple instructions on how to play the game
        JOptionPane.showMessageDialog(null, 
                "Welcome to the 3D Ball Shooter Game.  Time limit is 2min.  \n"
                + "See how many balls you can destroy before time is up.  \n"
                + "Huge Balls (Red) require 4 hits and are worth 2 points. \n"
                + "Large Balls (Blue) require 3 hits and are worth 4 points. \n"
                + "Medium Balls (Green) require 2 hits and are worth 6 points. \n"
                + "Small Balls (Yellow) require 1 hit and are worth 10 points. \n\n"
                + "Single shot (better accuracy) by pressing Left Mouse Button. \n"
                + "Auto fire (lower accuracy) by pressing and holding Right Mouse Button.");
        //Below is used for writing of game stats and score to text file
        String userHome = System.getProperty("user.home");
        File file = new File(userHome + "\\ball_shooter_game.txt");
        try {
            output = new java.io.BufferedWriter(new FileWriter(file,true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Creates the cube
    public void initCube(){
        
        //Materials for walls, floor, and ceiling        
        floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floor_mat.setColor("Color", ColorRGBA.Brown);
        
        wall_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key = new TextureKey("Textures/Terrain/BrickWall/BrickWall.jpg");
        key.setGenerateMips(true);
        Texture tex = assetManager.loadTexture(key);
        wall_mat.setTexture("ColorMap", tex);
        
        trans_wall = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        trans_wall.setColor("Color", new ColorRGBA(255,255,255,0.1f));
        trans_wall.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        
        //Add floor
        Geometry floor_geo = new Geometry("Floor", floor);
        floor_geo.setMaterial(floor_mat);
        floor_geo.setLocalTranslation(2, -20f, 0);
        this.rootNode.attachChild(floor_geo);
        floor_phy = new RigidBodyControl(0.0f);
        floor_geo.addControl(floor_phy);
        floor_geo.getControl(RigidBodyControl.class).setRestitution(-1);
        bulletAppState.getPhysicsSpace().add(floor_phy);
        
        //Add walls
        Geometry rightWall_geo = new Geometry ("Wall", wall);
        Geometry leftWall_geo = new Geometry ("Wall1", wall);
        Geometry ceiling = new Geometry ("Wall2", wall2);
        Geometry backWall_geo = new Geometry ("Wall2", wall3);
        Geometry front_geo = new Geometry ("Front", frontWall);
        rightWall_geo.setMaterial(wall_mat);
        leftWall_geo.setMaterial(wall_mat);
        ceiling.setMaterial(floor_mat);
        backWall_geo.setMaterial(trans_wall);
        backWall_geo.setQueueBucket(RenderQueue.Bucket.Transparent);  //Allows the front wall to be transparent
        front_geo.setMaterial(wall_mat);
        rootNode.attachChild(rightWall_geo);
        rootNode.attachChild(leftWall_geo);
        rootNode.attachChild(ceiling);
        rootNode.attachChild(backWall_geo);
        rootNode.attachChild(front_geo);
        rightWall_geo.setLocalTranslation(-18,0f,0);
        leftWall_geo.setLocalTranslation(22,0f,0);
        ceiling.setLocalTranslation(2,20f,0);
        backWall_geo.setLocalTranslation(2,0f,20);
        front_geo.setLocalTranslation(2,0f,-20);
        wall_phy = new RigidBodyControl(0.0f);
        wall1_phy = new RigidBodyControl(0.0f);
        wall2_phy = new RigidBodyControl(0.0f);
        wall3_phy = new RigidBodyControl(0.0f);
        frontWall_phy = new RigidBodyControl(0.0f);
        rightWall_geo.addControl(wall_phy);
        rightWall_geo.getControl(RigidBodyControl.class).setRestitution(-1);
        leftWall_geo.addControl(wall1_phy);
        leftWall_geo.getControl(RigidBodyControl.class).setRestitution(-1);
        ceiling.addControl(wall2_phy);
        ceiling.getControl(RigidBodyControl.class).setRestitution(-1);
        backWall_geo.addControl(wall3_phy);
        backWall_geo.getControl(RigidBodyControl.class).setRestitution(-1);
        front_geo.addControl(frontWall_phy);
        front_geo.getControl(RigidBodyControl.class).setRestitution(-1);
        bulletAppState.getPhysicsSpace().add(wall_phy);
        bulletAppState.getPhysicsSpace().add(wall1_phy);
        bulletAppState.getPhysicsSpace().add(wall2_phy);
        bulletAppState.getPhysicsSpace().add(wall3_phy);
        bulletAppState.getPhysicsSpace().add(frontWall_phy);
    }
    //Create the HUD
    protected void initDisplay() {
        guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");

        // Creates Crosshair
        ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setColor(ColorRGBA.White);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( // center
                settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
                settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
                
        gameTime = new BitmapText(guiFont, false);
        gameTime.setSize(guiFont.getCharSet().getRenderedSize());
        gameTime.setColor(ColorRGBA.White);
        gameTime.setText("Time: " + gameTimer + " seconds");
        gameTime.setLocalTranslation(0, 600, 0);
        
        shotsFired = new BitmapText(guiFont, false);
        shotsFired.setSize(guiFont.getCharSet().getRenderedSize());
        shotsFired.setColor(ColorRGBA.White);
        shotsFired.setText("Total Shots fired: " + total_shots);
        shotsFired.setLocalTranslation(0, 575, 0);
        
        totalHits = new BitmapText(guiFont, false);
        totalHits.setSize(guiFont.getCharSet().getRenderedSize());
        totalHits.setColor(ColorRGBA.White);
        totalHits.setText("Total Shot hits: " + total_hits);
        totalHits.setLocalTranslation(0, 550, 0);
        
        accY = new BitmapText(guiFont, false);
        accY.setSize(guiFont.getCharSet().getRenderedSize());
        accY.setColor(ColorRGBA.White);
        accY.setText("Accuracy: " + accuracy + "%");
        accY.setLocalTranslation(0, 525, 0);
        
        totalScore = new BitmapText(guiFont, false);
        totalScore.setSize(guiFont.getCharSet().getRenderedSize());
        totalScore.setColor(ColorRGBA.White);
        totalScore.setText("Score: " + score + " points");
        totalScore.setLocalTranslation(0, 500, 0);
        
        ballsDestroyed = new BitmapText(guiFont, false);
        ballsDestroyed.setSize(guiFont.getCharSet().getRenderedSize());
        ballsDestroyed.setColor(ColorRGBA.White);
        ballsDestroyed.setText("Huge Balls Destroyed: " + hugeBallsDestroyed + " \n" +
                "Large Balls Destroyed: " + largeBallsDestroyed + " \n" + 
                "Medium Balls Destroyed: " + medBallsDestroyed + " \n" +
                "Small Balls Destroyed: " + smallBallsDestroyed);
        ballsDestroyed.setLocalTranslation(0, 475, 0);

        guiNode.attachChild(ch);
        guiNode.attachChild(gameTime);
        guiNode.attachChild(shotsFired);
        guiNode.attachChild(totalHits);
        guiNode.attachChild(accY);
        guiNode.attachChild(totalScore);
        guiNode.attachChild(ballsDestroyed);
        }
    
    //Create the audio sounds   
    private void initAudio() {
        singleAudio = new AudioNode(assetManager, "Sounds/single.wav", false);
        singleAudio.setLooping(false);
        singleAudio.setVolume(0.1f);
        rootNode.attachChild(singleAudio);
        
        AudioData ad = singleAudio.getAudioData();
        if(ad.getChannels() > 1){
            ad.setupFormat(1, ad.getBitsPerSample(), ad.getSampleRate());
        }
        
        scoreAudio = new AudioNode(assetManager, "Sounds/woohoo.wav", false);
        scoreAudio.setLooping(false);
        scoreAudio.setVolume(100);
        rootNode.attachChild(scoreAudio);
        
        AudioData ad1 = scoreAudio.getAudioData();
        if(ad1.getChannels() > 1){
            ad1.setupFormat(1, ad1.getBitsPerSample(), ad1.getSampleRate());
        }

    }
    
    //Set up the shooting action buttons for single fire and auto
    private void initKeys() {
        inputManager.addMapping("singleShot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "singleShot");
        inputManager.addMapping("multiShot", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addListener(actionListener, "multiShot");
        inputManager.addListener(analogListener, "multiShot");
 
    }
    //Action Listener for the single shot Left Mouse Button click
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean keyPressed, float tpf) {
          if (name.equals("singleShot") && keyPressed) {
              
              singleAudio.playInstance();
              //Set up the collision to detect shot hits
              CollisionResults results = new CollisionResults();
              //No actual bullets, just a ray
              Ray ray = new Ray(cam.getLocation(), cam.getDirection());
              //Detect when the ray(bullet) hits a target
              target_balls.collideWith(ray, results);
              //This tracks the hits and will destroy and regenerate and calculate points and accuracy
              if (results.size() > 0) {
                    for (int i = 0; i < results.size(); i++) {
                        String hit = results.getCollision(i).getGeometry().getName();
                        regenerateBalls(hit);                                        
                        total_hits++;
                    }
                    //Shows a shot spotter where hit occurred (from "hello picking" tutorial
                    if (results.size() > 0) {
                        CollisionResult closest = results.getClosestCollision();
                        mark.setLocalTranslation(closest.getContactPoint());
                        rootNode.attachChild(mark);
                    } else {
                        rootNode.detachChild(mark);
                        }
                    
                    //Needed to implement this because the ray actually passes through a transparent wall before hitting the balls
                    //thereby totaling two hits per each hit.  This subtracts one (for the wall) so accuracy is correct
                    total_hits = total_hits - 1;
                    System.out.println("Total hits: " + total_hits); //Used for troubleshooting
                }
              //Tracks total shots and updates the HUD
              total_shots++;
              System.out.println("Total Shots: " + total_shots);  //Used for troubleshooting
              accuracy = ((total_hits * 100)/total_shots);  //Calculates accuracy
              System.out.println("Accuracy is:" + accuracy + "%"); //Used for troubleshooting
              System.out.println("Total score is: " + score);  //Used for troubleshooting
              System.out.println("Huge Ball Hits: " + hugeBall_hit);  //Used for troubleshooting
              displayUpdate();

          }
          if (name.equals("multiShot") && keyPressed){
              autoFire = true;
          }
        }
    };
    
    //Analog listener for the auto fire feature
    AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            if (autoFire) {
                //While Right Mouse Button is pressed, shot fired every 1/10th of a second
                if (name.equals("multiShot") && lastShotSince > 0.1f) {
                    singleAudio.playInstance();
                    lastShotSince = 0;
                    //Set up the collision to detect shot hits
                    CollisionResults results = new CollisionResults();
                    //No actual bullets, just a ray
                    Ray ray = new Ray(cam.getLocation(), cam.getDirection());
                    //Detect when the ray(bullet) hits a target
                    target_balls.collideWith(ray, results);
                    //This tracks the hits and will destroy and regenerate and calculate points and accuracy
                    if (results.size() > 0) {
                        for (int i = 0; i < results.size(); i++) {
                            String hit = results.getCollision(i).getGeometry().getName();
                            regenerateBalls(hit);                                        
                            total_hits++;
                            }
                        //Shows a shot spotter where hit occurred (from "hello picking" tutorial
                        if (results.size() > 0) {
                            CollisionResult closest = results.getClosestCollision();
                            mark.setLocalTranslation(closest.getContactPoint());
                            rootNode.attachChild(mark);
                            } else {
                            rootNode.detachChild(mark);
                            }
                        //Needed to implement this because the ray actually passes through a transparent wall before hitting the balls
                        //thereby totaling two hits per each hit.  This subtracts one (for the wall) so accuracy is correct
                        total_hits = total_hits - 1;
                        System.out.println("Total hits: " + total_hits);
                        
                        }
                    //Tracks total shots and updates the HUD
                    total_shots++;
                    System.out.println("Total Shots: " + total_shots);  //Used for troubleshooting
                    accuracy = ((total_hits * 100)/total_shots); //Calculates accuracy
                    System.out.println("Accuracy is:" + accuracy + "%");  //Used for troubleshooting
                    System.out.println("Total score is: " + score);  //Used for troubleshooting
                    displayUpdate();
                    }
                }
            }
        };

    //Displays the ball's location in real-time and records them to a text file every second
    public void simpleUpdate(float tpf) {
        //Counter for the auto fire function
        lastShotSince += tpf;
        
        //Game timer for the 2 minute run time
        if (getTimer().getTimeInSeconds() >= 1) {
            getTimer().reset();
            gameTimer++;
            timerUpdate();

            // Stops game automatically
            if (gameTimer >= 120) {
                endGame();
            }
        }
    }
    
    //This method tracks the hits on different  balls and if destroyed regenerates them and awards points
    private void regenerateBalls(String hit) {

        if (hit.equals("Huge Ball")) {
            hugeBall_hit++;
            if (hugeBall_hit == 8) {
                scoreAudio.playInstance();
                target_balls.detachChildNamed(hit);
                targets.createHugeBall();
                score = score + 2;
                hugeBallsDestroyed++;
                hugeBall_hit = 0;
            }
        } else if (hit.equals("Large Ball")) {
            largeBall_hit++;
            if (largeBall_hit == 6) {
                scoreAudio.playInstance();
                target_balls.detachChildNamed(hit);
                targets.createLargeBall();
                score = score + 4;
                largeBallsDestroyed++;
                largeBall_hit = 0;
            }
        } else if (hit.equals("Medium Ball")) {
            medBall_hit++;
            if (medBall_hit == 4) {
                scoreAudio.playInstance();
                target_balls.detachChildNamed(hit);
                targets.createMediumBall();
                score = score + 6;
                medBallsDestroyed++;
                medBall_hit = 0;
            }
        } else if (hit.equals("Small Ball")) {
            smallBall_hit++;
            if (smallBall_hit == 2) {
                scoreAudio.playInstance();
                target_balls.detachChildNamed(hit);
                targets.createSmallBall();
                score = score + 10;
                smallBallsDestroyed++;
                smallBall_hit = 0;
            }
        }
    }
    //Used to end the game and send score data to file
    public void endGame() {
        //Times up message and display stats
        JOptionPane.showMessageDialog(null, 
                "Time is up!.  \n\n"
                + "Total Shots fired: " + total_shots + "\n"
                + "Total Shot hits: " + total_hits + "\n"
                + "Accuracy: " + accuracy + "%\n\n"
                + "Score: " + score + " points");
        //Below asks for user initials and then writes game stats to file
        Date timeHack = Calendar.getInstance().getTime();
        SimpleDateFormat formattedDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dateTime = formattedDateTime.format(timeHack);
        //Gets users initials for recording in the output file
        String userName = JOptionPane.showInputDialog(null, "Please type your initials below:", JOptionPane.QUESTION_MESSAGE);
        try {
        output.write("Game played on: " + dateTime);
        output.newLine();
        output.write("User initials:  " + userName);
        output.newLine();
        output.write("Total Shots fired: " + total_shots);
        output.newLine();
        output.write("Total Shot hits: " + total_hits);
        output.newLine();
        output.write("Accuracy: " + accuracy + "%");
        output.newLine();
        output.write("Huge Balls Destroyed: " + hugeBallsDestroyed + " | " +
                "Large Balls Destroyed: " + largeBallsDestroyed + " | " + 
                "Medium Balls Destroyed: " + medBallsDestroyed + " | " +
                "Small Balls Destroyed: " + smallBallsDestroyed);
        output.newLine();
        output.write("Score: " + score + " points");
        output.newLine();
        output.newLine();
        output.close();
        }
        catch (IOException exception) {}
        System.exit(0);
    }
    
    //Updates the time in the HUD
    private void timerUpdate() {
        gameTime.setText("Time: " + gameTimer + " seconds");
        }
    //Updates the stats in the HUD
    private void displayUpdate() {
        accuracy = ((total_hits * 100)/total_shots);
        accY.setText("Accuracy: " + accuracy + "%");
        shotsFired.setText("Total Shots fired: " + total_shots);
        totalHits.setText("Total Shot hits: " + total_hits);
        totalScore.setText("Score: " + score + " points");
        ballsDestroyed.setText("Huge Balls Destroyed: " + hugeBallsDestroyed + " \n" +
                "Large Balls Destroyed: " + largeBallsDestroyed + " \n" + 
                "Medium Balls Destroyed: " + medBallsDestroyed + " \n" +
                "Small Balls Destroyed: " + smallBallsDestroyed);
        }
    //The shot spotter which shows where a hit occured if a hit is registered (from hello picking tutorial)
    protected void initSpotter() {
        Sphere sphere = new Sphere(30, 30, 0.2f);
        mark = new Geometry("BOOM!", sphere);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.White);
        mark.setMaterial(mark_mat);
        }
       
}
