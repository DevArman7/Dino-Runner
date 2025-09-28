import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import java.util.prefs.Preferences; // NEW: For high score
import javax.imageio.ImageIO;
import javax.swing.*;

public class ChromeDinosaur extends JPanel implements ActionListener, KeyListener {
    // --- CONSTANTS ---
    private static final int BOARD_WIDTH = 750;
    private static final int BOARD_HEIGHT = 250;
    private static final int GROUND_Y = BOARD_HEIGHT - 40; // Y position of the track

    // --- GAME STATE ---
    // NEW: Game state management
    private enum GameState {
        READY, PLAYING, GAME_OVER
    }
    private GameState gameState = GameState.READY;

    // --- ASSETS ---
    // REFINED: Using BufferedImage for better control
    private BufferedImage dinosaurRun1Img, dinosaurRun2Img, dinosaurJumpImg, dinosaurDeadImg, dinosaurDuck1Img, dinosaurDuck2Img;
    private BufferedImage cactus1Img, cactus2Img, cactus3Img;
    private BufferedImage bird1Img, bird2Img;
    private BufferedImage gameOverImg, resetImg, cloudImg, trackImg;

    // --- GAME OBJECTS ---
    private Dinosaur dinosaur;
    private ArrayList<Obstacle> obstacles;
    private Ground ground; // NEW
    private ArrayList<Cloud> clouds;
    private Random random = new Random();
    private Rectangle restartButton; // NEW

    // --- GAME LOGIC ---
    private double gameSpeed = 8.0;
    private int score = 0;
    private int highScore; // NEW
    private Preferences prefs; // NEW

    // --- TIMERS ---
    private Timer gameLoop;
    private Timer placeObstaclesTimer;

    // --- INNER CLASS: Animation ---
    // NEW: Handles sprite animations
    class Animation {
        BufferedImage[] frames;
        private int frameIndex = 0;
        private int delay;
        private long lastFrameTime;

        Animation(int delay, BufferedImage... frames) {
            this.frames = frames;
            this.delay = delay;
            this.lastFrameTime = 0;
        }

        public void update() {
            if (System.currentTimeMillis() - lastFrameTime > delay) {
                frameIndex = (frameIndex + 1) % frames.length;
                lastFrameTime = System.currentTimeMillis();
            }
        }

        public BufferedImage getFrame() {
            return frames[frameIndex];
        }
    }

    // --- INNER CLASS: Dinosaur ---
    class Dinosaur {
        int x = 50;
        int y, startY;
        int width, height;
        double velocityY = 0;
        double gravity = 0.8;
        boolean isDucking = false;

        Animation runAnim, duckAnim;
        BufferedImage jumpImg, deadImg;
        Rectangle hitbox;

        Dinosaur() {
            runAnim = new Animation(100, dinosaurRun1Img, dinosaurRun2Img);
            duckAnim = new Animation(100, dinosaurDuck1Img, dinosaurDuck2Img);
            jumpImg = dinosaurJumpImg;
            deadImg = dinosaurDeadImg;

            this.startY = GROUND_Y - runAnim.getFrame().getHeight();
            this.y = startY;
            this.width = runAnim.getFrame().getWidth();
            this.height = runAnim.getFrame().getHeight();
            this.hitbox = new Rectangle(x, y, width, height);
        }

        void update() {
            if (gameState == GameState.PLAYING) {
                // Apply gravity
                velocityY += gravity;
                y += velocityY;

                // Ground check
                if (y >= startY) {
                    y = startY;
                    velocityY = 0;
                }

                if (isDucking) {
                    duckAnim.update();
                } else {
                    runAnim.update();
                }
                updateHitbox();
            }
        }

        void jump() {
            if (y == startY) {
                velocityY = -17;
            }
        }

        void duck(boolean shouldDuck) {
            if (shouldDuck && y == startY) {
                isDucking = true;
            } else {
                isDucking = false;
            }
        }

        private void updateHitbox() {
            BufferedImage currentFrame = getCurrentImage();
            width = currentFrame.getWidth();
            height = currentFrame.getHeight();
            // Adjust Y when ducking to stay on the ground
            int currentY = isDucking ? startY + (dinosaurRun1Img.getHeight() - height) : y;
            hitbox.setBounds(x, currentY, width, height);
        }

        public BufferedImage getCurrentImage() {
            if (gameState == GameState.GAME_OVER) return deadImg;
            if (y < startY) return jumpImg;
            return isDucking ? duckAnim.getFrame() : runAnim.getFrame();
        }
    }

    // --- INNER CLASS: Obstacle (and its subclasses) ---
    abstract class Obstacle {
        BufferedImage image;
        int x, y, width, height;
        Rectangle hitbox;
        
        abstract void update();
        public abstract BufferedImage getImage();
    }

    class Cactus extends Obstacle {
        Cactus(int x, BufferedImage img) {
            this.image = img;
            this.width = img.getWidth();
            this.height = img.getHeight();
            this.x = x;
            this.y = GROUND_Y - height;
            this.hitbox = new Rectangle(x, y, width, height);
        }
        @Override
        public void update() {
            x -= gameSpeed;
            hitbox.x = x;
        }
        @Override
        public BufferedImage getImage() { return image; }
    }

    class Bird extends Obstacle {
        Animation flyAnim;
        Bird(int x, int y) {
            flyAnim = new Animation(150, bird1Img, bird2Img);
            this.width = flyAnim.getFrame().getWidth();
            this.height = flyAnim.getFrame().getHeight();
            this.x = x;
            this.y = y;
            this.hitbox = new Rectangle(x, y, width, height);
        }
        @Override
        public void update() {
            x -= (gameSpeed + 2); // Birds can be slightly faster
            flyAnim.update();
            hitbox.x = x;
        }
        @Override
        public BufferedImage getImage() { return flyAnim.getFrame(); }
    }
    
    // --- INNER CLASS: Ground ---
    // NEW: Manages the scrolling ground
    class Ground {
        int x1 = 0, x2;
        int width;

        Ground() {
            this.width = trackImg.getWidth();
            this.x2 = width;
        }

        void update() {
            x1 -= gameSpeed;
            x2 -= gameSpeed;

            // When one image moves off-screen, reset it to the end of the other
            if (x1 < -width) {
                x1 = x2 + width;
            }
            if (x2 < -width) {
                x2 = x1 + width;
            }
        }

        void draw(Graphics g) {
            g.drawImage(trackImg, x1, GROUND_Y, width, trackImg.getHeight(), null);
            g.drawImage(trackImg, x2, GROUND_Y, width, trackImg.getHeight(), null);
        }
    }

    // --- INNER CLASS: Cloud ---
    class Cloud {
        int x, y, width, height;
        Cloud(int x, int y) {
            this.x = x;
            this.y = y;
            this.width = cloudImg.getWidth();
            this.height = cloudImg.getHeight();
        }
        void update() {
            x -= (gameSpeed / 4); // Clouds move slower for parallax effect
        }
    }

    // --- CONSTRUCTOR ---
    public ChromeDinosaur() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);
        
        // NEW: Add mouse listener for the restart button
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameState == GameState.GAME_OVER && restartButton.contains(e.getPoint())) {
                    restartGame();
                }
            }
        });

        loadAssets();
        
        // NEW: Load high score
        prefs = Preferences.userNodeForPackage(ChromeDinosaur.class);
        highScore = prefs.getInt("highscore", 0);

        gameLoop = new Timer(1000 / 60, this);
        placeObstaclesTimer = new Timer(1500, e -> placeObstacle());

        restartGame(); // Initial setup
    }

    private void restartGame() {
        dinosaur = new Dinosaur();
        ground = new Ground();
        obstacles = new ArrayList<>();
        clouds = new ArrayList<>();
        placeCloud(); // Start with one cloud
        
        gameSpeed = 8.0;
        score = 0;
        gameState = GameState.READY;
        
        gameLoop.start();
        placeObstaclesTimer.start();
    }

    private void loadAssets() {
        try {
            // Use ImageIO.read for BufferedImages
            dinosaurRun1Img = ImageIO.read(getClass().getResource("./img/dino-run1.png"));
            dinosaurRun2Img = ImageIO.read(getClass().getResource("./img/dino-run2.png"));
            dinosaurJumpImg = ImageIO.read(getClass().getResource("./img/dino-jump.png"));
            dinosaurDeadImg = ImageIO.read(getClass().getResource("./img/dino-dead.png"));
            dinosaurDuck1Img = ImageIO.read(getClass().getResource("./img/dino-duck1.png"));
            dinosaurDuck2Img = ImageIO.read(getClass().getResource("./img/dino-duck2.png"));

            cactus1Img = ImageIO.read(getClass().getResource("./img/cactus1.png"));
            cactus2Img = ImageIO.read(getClass().getResource("./img/big-cactus1.png")); // Using big cacti for variety
            cactus3Img = ImageIO.read(getClass().getResource("./img/big-cactus3.png"));

            bird1Img = ImageIO.read(getClass().getResource("./img/bird1.png"));
            bird2Img = ImageIO.read(getClass().getResource("./img/bird2.png"));
            
            trackImg = ImageIO.read(getClass().getResource("./img/track.png"));
            gameOverImg = ImageIO.read(getClass().getResource("./img/game-over.png"));
            resetImg = ImageIO.read(getClass().getResource("./img/reset.png"));
            cloudImg = ImageIO.read(getClass().getResource("./img/cloud.png"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    void placeObstacle() {
        if (gameState != GameState.PLAYING) return;

        double chance = random.nextDouble();
        if (chance > 0.7 && score > 80) { // Cacti
            obstacles.add(new Cactus(BOARD_WIDTH + 50, cactus1Img));
        } else if (chance > 0.5 && score > 200) {
            obstacles.add(new Cactus(BOARD_WIDTH + 50, cactus2Img));
        } else if (chance > 0.3 && score > 400) {
             obstacles.add(new Cactus(BOARD_WIDTH + 50, cactus3Img));
        } else if (chance > 0.15 && score > 500) { // Birds appear later
            int birdY = GROUND_Y - bird1Img.getHeight() - random.nextInt(40);
            obstacles.add(new Bird(BOARD_WIDTH + 50, birdY));
        }
    }
    
    void placeCloud() {
        int cloudY = 20 + random.nextInt(80);
        clouds.add(new Cloud(BOARD_WIDTH + random.nextInt(200), cloudY));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        // Draw Clouds
        for (Cloud cloud : clouds) {
            g.drawImage(cloudImg, cloud.x, cloud.y, null);
        }

        // Draw Ground
        ground.draw(g);

        // Draw Dinosaur
        BufferedImage dinoImg = dinosaur.getCurrentImage();
        g.drawImage(dinoImg, dinosaur.x, dinosaur.hitbox.y, null);

        // Draw Obstacles
        for (Obstacle obstacle : obstacles) {
            g.drawImage(obstacle.getImage(), obstacle.x, obstacle.y, null);
        }

        // Draw Score and UI
        g.setColor(Color.decode("#535353")); // Original game's text color
        g.setFont(new Font("monospaced", Font.BOLD, 20));
        
        // Format score to be 5 digits with leading zeros
        String formattedScore = String.format("%05d", score);
        String formattedHighScore = String.format("HI %05d", highScore);
        g.drawString(formattedScore, BOARD_WIDTH - 100, 30);
        g.drawString(formattedHighScore, BOARD_WIDTH - 220, 30);

        if (gameState == GameState.GAME_OVER) {
            // Draw Game Over text and Restart button
            int goX = BOARD_WIDTH / 2 - gameOverImg.getWidth() / 2;
            int goY = BOARD_HEIGHT / 2 - 50;
            g.drawImage(gameOverImg, goX, goY, null);

            int restartX = BOARD_WIDTH / 2 - resetImg.getWidth() / 2;
            int restartY = goY + gameOverImg.getHeight() + 20;
            g.drawImage(resetImg, restartX, restartY, null);
            // Define the button bounds for the mouse listener
            restartButton = new Rectangle(restartX, restartY, resetImg.getWidth(), resetImg.getHeight());
        }
    }
    
    private void move() {
        ground.update();
        dinosaur.update();
        
        // Randomly add clouds
        if (random.nextInt(200) == 1) {
            placeCloud();
        }

        for (Obstacle obstacle : obstacles) {
            obstacle.update();
            if (dinosaur.hitbox.intersects(obstacle.hitbox)) {
                gameState = GameState.GAME_OVER;
                dinosaur.y = dinosaur.startY; // Put dino back on ground when dead
                placeObstaclesTimer.stop();
                
                // NEW: Check and save high score
                if (score > highScore) {
                    highScore = score;
                    prefs.putInt("highscore", highScore);
                }
            }
        }
        
        // Clean up off-screen objects
        obstacles.removeIf(o -> o.x < -o.width);
        clouds.removeIf(c -> c.x < -c.width);

        // Update score and speed
        score++;
        if (score % 100 == 0) {
            gameSpeed += 0.5;
            // Shorten obstacle placement timer as game speeds up
            int newDelay = Math.max(700, placeObstaclesTimer.getDelay() - 50);
            placeObstaclesTimer.setDelay(newDelay);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == GameState.PLAYING) {
            move();
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (gameState == GameState.READY) {
            if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_UP) {
                gameState = GameState.PLAYING;
                dinosaur.jump();
            }
        } else if (gameState == GameState.PLAYING) {
            if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_UP) {
                dinosaur.jump();
            } else if (keyCode == KeyEvent.VK_DOWN) {
                dinosaur.duck(true);
            }
        } else if (gameState == GameState.GAME_OVER) {
            if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_UP) {
                restartGame();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameState == GameState.PLAYING && e.getKeyCode() == KeyEvent.VK_DOWN) {
            dinosaur.duck(false);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}