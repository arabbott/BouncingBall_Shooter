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

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import java.util.Random;

public final class BallTargets {

    private BulletAppState bulletAppState;    
    private Node rootNode;
    private Node target_balls;
    private AssetManager assetManager;
    public Geometry hugeBall;
    public Geometry largeBall;
    public Geometry medBall;
    public Geometry smallBall;
    public RigidBodyControl hugeBall_phy;
    public RigidBodyControl largeBall_phy;
    public RigidBodyControl medBall_phy;
    public RigidBodyControl smallBall_phy;
    
    Random r1 = new Random();
        int low1 = 1;
        int high1 = 10;
        int randCoord1 = r1.nextInt(high1-low1);
        
    Random r2 = new Random();
        int low2 = 1;
        int high2 = 9;
        int randCoord2 = r2.nextInt(high2-low2);

    Random r3 = new Random();
        int low3 = 1;
        int high3 = 8;
        int randCoord3 = r3.nextInt(high3-low3);
                
    Random r4 = new Random();
        int low4 = 100;
        int high4 = 200;
        int randVelocity = r4.nextInt(high4-low4);
    
    
    public BallTargets (AssetManager assetManager, Node rootNode, Node target_balls, BulletAppState bulletAppState) {
        
        this.bulletAppState = bulletAppState;
        this.rootNode = rootNode;
        this.target_balls = target_balls;
        this.assetManager = assetManager;
        rootNode.attachChild(target_balls);

        // Creates the target balls
        createHugeBall();
        createLargeBall();
        createMediumBall();
        createSmallBall();
    }

    public void createHugeBall() {
        Sphere createBall = new Sphere(32, 32, 5f);
        hugeBall = new Geometry("Huge Ball", createBall);
        createBall.setTextureMode(Sphere.TextureMode.Projected);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Red);
        hugeBall.setMaterial(mat);                
        hugeBall.setLocalTranslation(randCoord2,10f,randCoord1);
        target_balls.attachChild(hugeBall);
        hugeBall_phy = new RigidBodyControl(1f);
        hugeBall.addControl(hugeBall_phy);
        bulletAppState.getPhysicsSpace().add(hugeBall_phy);
        hugeBall_phy.setMass(1);
        hugeBall_phy.setFriction(0);
        hugeBall_phy.setRestitution(1.0f);
        hugeBall_phy.setGravity(new Vector3f(0f,-9.81f,0f));
        hugeBall_phy.setLinearVelocity(new Vector3f(randVelocity,randVelocity,randVelocity));
    }

    public void createLargeBall() {
        Sphere createBall = new Sphere(32, 32, 2f);
        largeBall = new Geometry("Large Ball", createBall);
        createBall.setTextureMode(Sphere.TextureMode.Projected);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        largeBall.setMaterial(mat);
        largeBall.setLocalTranslation(randCoord1,10f,randCoord2);
        target_balls.attachChild(largeBall);
        largeBall_phy = new RigidBodyControl(1f);
        largeBall.addControl(largeBall_phy);
        bulletAppState.getPhysicsSpace().add(largeBall_phy);
        largeBall_phy.setMass(1);
        largeBall_phy.setFriction(0);
        largeBall_phy.setRestitution(1.0f);
        largeBall_phy.setGravity(new Vector3f(0f,-9.81f,0f));
        largeBall_phy.setLinearVelocity(new Vector3f(randVelocity,randVelocity,randVelocity));
    }

    public void createMediumBall() {
        Sphere createBall = new Sphere(32, 32, 1f);
        medBall = new Geometry("Medium Ball", createBall);
        createBall.setTextureMode(Sphere.TextureMode.Projected);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Green);
        medBall.setMaterial(mat);
        medBall.setLocalTranslation(randCoord3,10f,randCoord1);
        target_balls.attachChild(medBall);
        medBall_phy = new RigidBodyControl(1f);
        medBall.addControl(medBall_phy);
        bulletAppState.getPhysicsSpace().add(medBall_phy);
        medBall_phy.setMass(1);
        medBall_phy.setFriction(0);
        medBall_phy.setRestitution(1.0f);
        medBall_phy.setGravity(new Vector3f(0f,-9.81f,0f));
        medBall_phy.setLinearVelocity(new Vector3f(randVelocity,randVelocity,randVelocity));
    }

    public void createSmallBall() {
        Sphere createBall = new Sphere(32, 32, 0.5f);
        smallBall = new Geometry("Small Ball", createBall);
        createBall.setTextureMode(Sphere.TextureMode.Projected);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Yellow);
        smallBall.setMaterial(mat);
        smallBall.setLocalTranslation(randCoord1,10f,randCoord3);
        target_balls.attachChild(smallBall);
        smallBall_phy = new RigidBodyControl(1f);
        smallBall.addControl(smallBall_phy);
        bulletAppState.getPhysicsSpace().add(smallBall_phy);
        smallBall_phy.setMass(1);
        smallBall_phy.setFriction(0);
        smallBall_phy.setRestitution(1.0f);
        smallBall_phy.setGravity(new Vector3f(0f,-9.81f,0f));
        smallBall_phy.setLinearVelocity(new Vector3f(randVelocity,randVelocity,randVelocity));
    }          
}