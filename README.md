
# 🦖 Java Swing Chrome Dinosaur Game

> A faithful desktop recreation of the classic Google Chrome offline dinosaur game, built entirely with Java and the Swing library.

This project brings the beloved endless runner from your browser to your desktop. It includes sprite animations, dynamic obstacles, a persistent high score, and progressively increasing difficulty, just like the original\!

*(**Pro-tip:** You can create your own GIF for the screenshot above using a free tool like [ScreenToGif](https://www.screentogif.com/) and replace the link.)*

-----

## ✨ Features

  * **Endless Runner Gameplay:** Jump and duck to avoid obstacles for as long as you can.
  * **Sprite Animations:** Smooth, frame-by-frame animations for the dinosaur's run, duck, and jump, plus flapping animations for the bird obstacles.
  * **Dynamic Obstacles:** Encounter a variety of cacti and flying birds that spawn randomly.
  * **Increasing Difficulty:** The game speed gradually increases as your score gets higher, keeping the challenge fresh.
  * **Persistent High Score:** Your highest score is saved locally and displayed, so you always have a record to beat\!
  * **Authentic Visuals:** Features the iconic scrolling ground, parallax clouds, and a "Game Over" screen with a clickable restart button.
  * **Clean Game States:** A simple state machine manages the `READY`, `PLAYING`, and `GAME_OVER` states.

-----

## 🚀 How to Run

To get this project running on your local machine, follow these simple steps.

### Prerequisites

You need to have the Java Development Kit (JDK) installed on your system (version 8 or newer is recommended).

### Steps

1.  **Clone the repository:**

    ```sh
    git clone https://github.com/your-username/your-repository-name.git
    ```

2.  **Navigate to the project directory:**

    ```sh
    cd your-repository-name
    ```

3.  **Compile the Java files:**
    From the root directory, run the `javac` command, pointing it to the source files.

    ```sh
    javac src/App.java src/ChromeDinosaur.java
    ```

4.  **Run the application:**
    Now, run the `java` command, specifying the classpath (`-cp`) as the `src` folder and naming the main class (`App`).

    ```sh
    java -cp src App
    ```

> **Alternatively (Using an IDE):**
> You can also open the project folder in an IDE like VS Code, Eclipse, or IntelliJ IDEA and simply run the `App.java` file.

-----

## 🎮 Controls

  * **Jump / Start Game:** `Space` or `Up Arrow`
  * **Duck:** `Down Arrow` (hold)
  * **Restart Game (from Game Over screen):** `Space`, `Up Arrow`, or click the **Reset Button**.

-----

## 🛠️ Built With

  * **Java**: The core programming language.
  * **Java Swing**: The GUI toolkit used for creating the window, drawing graphics, and handling events.

-----

## 📂 Project Structure

A brief overview of the key files in this project.

```
CHROME-DINO-JAVA/
├── src/
│   ├── App.java              # The main entry point (launches the JFrame)
│   ├── ChromeDinosaur.java   # The core game logic and JPanel component
│   └── img/                  # Contains all game assets (images and sprites)
└── README.md                 # This file
```