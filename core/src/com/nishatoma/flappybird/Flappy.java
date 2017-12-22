package com.nishatoma.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

import sun.rmi.runtime.Log;

public class Flappy extends ApplicationAdapter {
    //Remember we can change our screen orientation in Manifest!!
    private SpriteBatch batch;
    //A texture is really just an image.
    private Texture background;
    //For drawing different kinds of shapes
    private ShapeRenderer shapeRenderer;
    //Store the two bird pictures in an array and use both of
    //Them interchangeably.
    private Texture[] birds;
    //Keeps track of which state are we viewing in the 'render'
    //Method each second
    private int flapState = 0;
    private Texture bird;
    //Bird velocity and speed information
    //Used to manage our gravity system
    //Note that only the Y position changes throughout the game!
    private float birdY = 0;
    //How fast the bird is moving
    private float velocity = 0;
    private Circle birdCircle;
    //-----------------------------
    //Game state, lose or still playing?
    private boolean gameRunning = false;
    //gravity
    private static final float G = 3;
    //Tubes
    private Texture topTube;
    private Texture bottomTube;
    //Alter tubes' gap
    private float gap = 400;
    //Tube position
    private float maxTubeOffSet;
    private Random rng;
    //Tube velocity
    private float tubeVelocity = 5;
    //Number of Tubes
    private int numberOfTubes = 4;
    //Keep track of tubes x position
    private float[] tubeX = new float[numberOfTubes];
    private float[] tubeOffset = new float[numberOfTubes];
    //distance between the tubes
    private float distanceBetweenTubes;
    //------------------------------------
    //Our rectangles
    Rectangle[] topTubeRectangles;
    Rectangle[] bottomTubeRectangles;
    //------------------------------------
    private int score = 0;
    //We need something to keep track of the active tube to count score!
    private int scoringTube = 0;
    //------------------------------------
    //Write on the screen using the default font
    BitmapFont font;
    //Game Over texture
    private Texture gameOver;
    //Game over!
    private int gameState = 0 ;


    /**
     * This method is run when the App itself is run.
     */
    @Override
    public void create() {
        //Game over
        gameOver = new Texture("over.png");
        //Font
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(10);
        //Shape
        shapeRenderer = new ShapeRenderer();
        birdCircle = new Circle();
        /*We're going to add a couple of sprites here.
        * We can add our sprites as a SpriteBatch!*/
        batch = new SpriteBatch();
        //Initialize the picture using the png file 'bg'
        background = new Texture("bg.png");
        //Initialize our bird array with two elements
        birds = new Texture[2];
        birds[0] = new Texture("bird.png");
        birds[1] = new Texture("bird2.png");
        bird = birds[0];
        //Initially the bird is in the middle of the screen (Y position)

        //Tubes
        topTube = new Texture("toptube.png");
        bottomTube = new Texture("bottomtube.png");
        maxTubeOffSet = ((Gdx.graphics.getHeight() / 2) - ((gap / 2) + 100));
        //Rectangles
        topTubeRectangles = new Rectangle[numberOfTubes];
        bottomTubeRectangles = new Rectangle[numberOfTubes];
        //Set up the random generator
        rng = new Random();
        //Initially, we want to set tubeX
        tubeX[0] = (Gdx.graphics.getWidth() / 2) - (topTube.getWidth() / 2);
        //Distance between tubes
        distanceBetweenTubes = Gdx.graphics.getWidth() * 3 / 4;

        startGame();


    }

    public void startGame()
    {
        birdY = Gdx.graphics.getHeight() / 2 - (bird.getHeight() / 2);

        for (int i = 0; i < numberOfTubes; i++) {
            tubeOffset[i] = (rng.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - gap - 200);
            //Reset tubeX each time we tap!
            tubeX[i] = (Gdx.graphics.getWidth() / 2) - (topTube.getWidth() / 2) + Gdx.graphics.getWidth()/2 + i * distanceBetweenTubes;
            //For each of our rectangles, we will define them to be a new rectangle!
            topTubeRectangles[i] = new Rectangle();
            bottomTubeRectangles[i] = new Rectangle();
        }
    }


    public void drawBird() {
        //First thing we need to do is run batch.begin()
        //It tells the render method that we are going to start drawing
        //The sprites now.
        /*We are telling the SpriteBatch object 'batch' to draw our background image
        * Starting from x = 0, y= 0, and we want it to occupy the scree using
        * Gdx.getWidth() and Gdx.getHeight()*/
        bird = birds[flapState];
        //Depending of the flap state, the render method will keep switching between bird 1 & 2
        batch.draw(bird, (Gdx.graphics.getWidth() / 2 - (bird.getWidth() / 2)), (birdY));
    }


    /**
     * The render method happens continuously as the App is running.
     */
    @Override
    public void render() {


        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        if (gameState == 1) {
            //Check score!
            if (tubeX[scoringTube] < Gdx.graphics.getWidth() / 2)
            {
                this.score++;

                Gdx.app.log("Score", String.valueOf(score));

                if (scoringTube < numberOfTubes - 1)
                {
                    scoringTube++;
                } else {
                    scoringTube = 0;
                }
            }
            if (Gdx.input.justTouched()) {
                velocity = -20;
            }
            for (int i = 0; i < numberOfTubes; i++) {
                //If the tubes are off screen!
                if (tubeX[i] < -topTube.getWidth()) {
                    tubeX[i] += numberOfTubes * distanceBetweenTubes;
                    tubeOffset[i] = (rng.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - gap - 200);
                } else {
                    //Each time, decrease tubeX by 4
                    tubeX[i] -= tubeVelocity;

                }
                //We draw the tubes when the game is active
                batch.draw(topTube, tubeX[i],
                        (Gdx.graphics.getHeight() / 2) + (gap / 2) + tubeOffset[i]);
                batch.draw(bottomTube, tubeX[i],
                        (Gdx.graphics.getHeight() / 2) - (gap / 2) - bottomTube.getHeight() + tubeOffset[i]);
                topTubeRectangles[i] = new Rectangle(tubeX[i], (Gdx.graphics.getHeight() / 2) + (gap / 2) + tubeOffset[i],
                        topTube.getWidth(), topTube.getHeight());
                bottomTubeRectangles[i] = new Rectangle(tubeX[i], (Gdx.graphics.getHeight() / 2) - (gap / 2) - bottomTube.getHeight() + tubeOffset[i],
                        bottomTube.getWidth(), bottomTube.getHeight());
            }
            //To prevent the bird from falling down the screen
            //Just increase its Y value after a certain threshold.
            if (birdY > 0) {
                //Increase the velocity each time render loop is called!
                velocity += G;
                birdY -= velocity;
            } else {
                gameState = 2;
            }
        } else if (gameState == 0){
            //This check sees if the User has tapped on the screen
            //If so, execute the code in the if-statement
            if (Gdx.input.justTouched()) {
                gameState = 1;
            }
        } else if (gameState == 2){
            batch.draw(gameOver, Gdx.graphics.getWidth()/ 2 - gameOver.getWidth() / 2,
                    Gdx.graphics.getHeight()/ 2  - gameOver.getHeight()/2);

            //if game over, then allow user to restart by tapping
            if (Gdx.input.justTouched()){
                gameState = 1;
                startGame();
                score = 0;
                scoringTube = 0;
                velocity = 0;
            }
        }
        flapState = (flapState == 0) ? 1 : 0;

        drawBird();
        //Draw the font
        font.draw(batch, String.valueOf(score), 100, 200);

        //Draw the circle overlapping with the bird!
        //shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        //shapeRenderer.setColor(Color.RED);
        //Takes x, y of the point in the middle of the circle and its radius.
        birdCircle.set(Gdx.graphics.getWidth() / 2, birdY + birds[flapState].getHeight() / 2, birds[flapState].getHeight() / 2);
        //shapeRenderer.circle(birdCircle.x, birdCircle.y, birdCircle.radius);

        //Display the rectangles
        for (int i = 0; i < numberOfTubes; i++)
        {
            //shapeRenderer.rect(topTubeRectangles[i].x, topTubeRectangles[i].y, topTubeRectangles[i].width, topTubeRectangles[i].height);
            //shapeRenderer.rect(bottomTubeRectangles[i].x, bottomTubeRectangles[i].y, bottomTubeRectangles[i].width, bottomTubeRectangles[i].height);

            if (Intersector.overlaps(birdCircle, topTubeRectangles[i]) || Intersector.overlaps(birdCircle, bottomTubeRectangles[i]))
            {
                gameState = 2;
            }
        }

        batch.end();
        //shapeRenderer.end();
    }
}
