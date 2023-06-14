package application;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MineSweeper extends Application {
    private static final int TILE_SIZE = 30;
    private static final int NUM_MINES_BEGINNER = 10;
    private static final int NUM_MINES_INTERMEDIATE = 40;
    private static final int NUM_MINES_EXPERT = 99;

    private int numRows;
    private int numCols;
    private int numMines;
    private int numUncoveredTiles;
    private int numFlags;

    private Button[][] tiles;
    private int[][] board;
    private boolean[][] revealed;
    private boolean gameOver;

    private Label timeLabel;
    private int elapsedTime;

    private Random random;
    private ScheduledExecutorService timerExecutor;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Minesweeper");
        BorderPane root = new BorderPane();
        createStartScreen(primaryStage, root);
        HBox topBar = createTopBar(primaryStage, root);
        root.setTop(topBar);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm()); // Agregar esta línea
        primaryStage.setScene(scene);
        primaryStage.show();
    }
 

    private void createStartScreen(Stage primaryStage, BorderPane root) {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);

        Label titleLabel = new Label("Minesweeper");
        titleLabel.setFont(Font.font("Arial", 24));

        Button beginnerButton = createDifficultyButton("Beginner (8x8, 10 mines)");
        beginnerButton.setOnAction(event -> {
            startGame(8, 8, NUM_MINES_BEGINNER);
            showGameScreen(primaryStage, root);
        });

        Button intermediateButton = createDifficultyButton("Intermediate (16x16, 40 mines)");
        intermediateButton.setOnAction(event -> {
            startGame(16, 16, NUM_MINES_INTERMEDIATE);
            showGameScreen(primaryStage, root);
        });

        Button expertButton = createDifficultyButton("Expert (16x30, 99 mines)");
        expertButton.setOnAction(event -> {
            startGame(16, 30, NUM_MINES_EXPERT);
            showGameScreen(primaryStage, root);
        });

        vbox.getChildren().addAll(titleLabel, beginnerButton, intermediateButton, expertButton);
        root.setCenter(vbox);
    }


    private Button createDifficultyButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(200);
        return button;
    }

    private void startGame(int numRows, int numCols, int numMines) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.numMines = numMines;
        this.numUncoveredTiles = 0;
        this.numFlags = 0;
        this.board = new int[numRows][numCols];
        this.revealed = new boolean[numRows][numCols];
        this.gameOver = false;
        this.random = new Random();

        generateBoard();
        
    }
   
    private void generateBoard() {
        int count = 0;
        int initialRow = random.nextInt(numRows);
        int initialCol = random.nextInt(numCols);

        // Generar el resto de las minas
        while (count < numMines) {
            int row = random.nextInt(numRows);
            int col = random.nextInt(numCols);

            // Verificar que la posición no sea la inicial, no sea una mina y no esté cerca de la posición inicial
            if ((row != initialRow || col != initialCol) && board[row][col] != -1 && !isAdjacentToInitialPosition(row, col, initialRow, initialCol)) {
                board[row][col] = -1;
                count++;
            }
        }

        // Calcular el número de minas adyacentes para las casillas sin minas
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                if (board[row][col] != -1) {
                    int countMines = countAdjacentMines(row, col);
                    board[row][col] = countMines;
                }
            }
        }
    }

    private boolean isAdjacentToInitialPosition(int row, int col, int initialRow, int initialCol) {
        int rowDiff = Math.abs(row - initialRow);
        int colDiff = Math.abs(col - initialCol);
        return (rowDiff == 1 && colDiff <= 1) || (rowDiff <= 1 && colDiff == 1);
    }



    private void generateEmptyPosition(int row, int col) {
        int[] directions = {-1, 0, 1};
        int[] permutation = generateRandomPermutation(directions);

        for (int i = 0; i < permutation.length; i++) {
            int dr = permutation[i];
            for (int j = 0; j < permutation.length; j++) {
                int dc = permutation[j];
                int newRow = row + dr;
                int newCol = col + dc;
                if (newRow >= 0 && newRow < numRows && newCol >= 0 && newCol < numCols && board[newRow][newCol] != -1) {
                    board[newRow][newCol] = 0;
                    return;
                }
            }
        }
    }

    private int[] generateRandomPermutation(int[] array) {
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
        return array;
    }



    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int newRow = row + dr;
                int newCol = col + dc;
                if (newRow >= 0 && newRow < numRows && newCol >= 0 && newCol < numCols && board[newRow][newCol] == -1) {
                    count++;
                }
            }
        }
        return count;
    }

    private void showGameScreen(Stage primaryStage, BorderPane root) {
        root.getChildren().clear();

        HBox topBar = createTopBar(primaryStage, root);
        root.setTop(topBar);

        GridPane gridPane = createGameBoard();
        root.setCenter(gridPane);

        startTimer(); // Add this line to start the timer
    }


    private HBox createTopBar(Stage primaryStage, BorderPane root) {
        HBox topBar = new HBox();
        topBar.setSpacing(10);

        Button replayButton = new Button("Replay");
        replayButton.setOnAction(event -> {
            stopTimer();
            createStartScreen(primaryStage, root);
        });

        timeLabel = new Label("Time: 0");
        timeLabel.setFont(Font.font("Arial", 18));

        topBar.getChildren().addAll(replayButton, timeLabel);
        return topBar;
    }

    private GridPane createGameBoard() {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(1);
        gridPane.setVgap(1);

        tiles = new Button[numRows][numCols];
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                Button tile = createTile(row, col);
                tiles[row][col] = tile;
                gridPane.add(tile, col, row);
            }
        }

        return gridPane;
    }

    
    
    private Button createTile(int row, int col) {
        Button tile = new Button();
        tile.setPrefSize(TILE_SIZE, TILE_SIZE);
        tile.getStyleClass().addAll("tile", "button");

        // Create an intermediate observable value for board[row][col]
        IntegerProperty boardValue = new SimpleIntegerProperty();
        boardValue.bind(Bindings.createObjectBinding(() -> board[row][col]));

        // Add a CSS pseudo-class based on the number of adjacent mines
        boardValue.addListener((observable, oldValue, newValue) -> {
            tile.pseudoClassStateChanged(PseudoClass.getPseudoClass("revealed-" + oldValue), false);
            tile.pseudoClassStateChanged(PseudoClass.getPseudoClass("revealed-" + newValue), true);
        });

        tile.setOnMouseClicked(event -> {
            if (!gameOver) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    handleLeftClick(row, col);
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    handleRightClick(row, col);
                }
            }
        });
        return tile;
    }

    private void handleLeftClick(int row, int col) {
        if (!gameOver) {
            if (numUncoveredTiles == 0) {
                // Se ha hecho el primer clic, abrir el campo sin minas ni números en la posición (row, col)
                openFieldWithoutMinesAndNumbers(row, col);
            }
            
            if (revealed[row][col]) {
                return;
            }

            if (board[row][col] == -1) {
                gameOver();
                return;
            }

            revealTile1(row, col);

            if (numUncoveredTiles == (numRows * numCols - numMines)) {
                gameWon();
            }
        }
    }

    private void openFieldWithoutMinesAndNumbers(int row, int col) {
        // Verificar que la posición no sea una mina ni un número
        if (board[row][col] == -1 || board[row][col] > 0) {
            // Si la posición es una mina o un número, moverla a otra ubicación
            moveMineOrNumberToRandomPosition(row, col);
        }

        // Abrir el campo sin minas ni números
        revealTile1(row, col);
    }

    private void moveMineOrNumberToRandomPosition(int initialRow, int initialCol) {
        // Buscar una posición aleatoria que no sea una mina ni un número y no esté cerca de la posición inicial
        int newRow, newCol;
        do {
            newRow = random.nextInt(numRows);
            newCol = random.nextInt(numCols);
        } while (board[newRow][newCol] == -1 || board[newRow][newCol] > 0 || isAdjacentToInitialPosition(newRow, newCol, initialRow, initialCol));

        // Mover la mina o el número a la nueva posición
        board[initialRow][initialCol] = 0;
        board[newRow][newCol] = -1;
        countAdjacentMines(newRow, newCol);
    }

    private void revealTile1(int row, int col) {
        if (row < 0 || row >= numRows || col < 0 || col >= numCols || revealed[row][col]) {
            return;
        }

        Button tile = tiles[row][col];
        tile.setStyle("-fx-background-color: #A9A9A9;"); // Color para casillas sin bombas alrededor
        revealed[row][col] = true;
        numUncoveredTiles++;

        int value = board[row][col];
        if (value != 0) {
            tile.setText(String.valueOf(value));
        } else {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    revealTile1(row + dr, col + dc);
                }
            }
        }
    }


    	private void handleRightClick(int row, int col) {
    	    Button tile = tiles[row][col];
    	    if (!revealed[row][col]) {
    	        if (!tile.getStyleClass().contains("flagged") && numFlags < numMines) {
    	            tile.getStyleClass().add("flagged");
    	            tile.setText("\uD83D\uDEA9"); // Emote de bandera (Unicode)
    	            numFlags++;
    	        } else {
    	            tile.getStyleClass().remove("flagged");
    	            tile.setText("");
    	            numFlags--;
    	        }
    	    }
    	}



    private void revealTile(int row, int col) {
        if (row < 0 || row >= numRows || col < 0 || col >= numCols || revealed[row][col]) {
            return;
        }

        Button tile = tiles[row][col];
        tile.setStyle("-fx-base: white;");
        revealed[row][col] = true;
        numUncoveredTiles++;

        int value = board[row][col];
        if (value != 0) {
            tile.setText(String.valueOf(value));
        } else {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    revealTile1(row + dr, col + dc);
                }
            }
        }
    }

    private void gameOver() {
        gameOver = true;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                if (board[row][col] == -1) {
                    Button tile = tiles[row][col];
                    tile.setStyle("-fx-base: red;");
                    tile.setText("X");
                }
            }
        }
        stopTimer();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText("Has perdido la partida.");
        alert.showAndWait();

    }
    
    private void gameWon() {
        gameOver = true;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                if (board[row][col] == -1) {
                    Button tile = tiles[row][col];
                    tile.setText("F");
                }
            }
        }
        stopTimer();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("¡Felicidades!");
        alert.setHeaderText(null);
        alert.setContentText("Has ganado la partida.");
        alert.showAndWait();

    }

    private void startTimer() {
        elapsedTime = 0;
        timeLabel.setText("Time: " + elapsedTime);

        timerExecutor = Executors.newSingleThreadScheduledExecutor();
        timerExecutor.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                elapsedTime++;
                timeLabel.setText("Time: " + elapsedTime);
            });
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void stopTimer() {
        if (timerExecutor != null) {
            timerExecutor.shutdown();
        }
    }
    
    @Override
    public void stop() {
        if (timerExecutor != null) {
            timerExecutor.shutdownNow();
        }
    }
}
