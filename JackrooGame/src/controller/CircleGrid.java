
package controller;

import java.util.ArrayList;

import engine.board.Cell;
import engine.board.CellType;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class CircleGrid extends GridPane {

    // Screen dimensions and scaling factors
    private DoubleProperty screenWidth = new SimpleDoubleProperty();
    private DoubleProperty screenHeight = new SimpleDoubleProperty();
    private static final double REFERENCE_WIDTH = 1869.0;
    private static final double REFERENCE_HEIGHT = 954.0;
    
    // Custom object type to pair Circle with its grid cell
    public static class CircleCellPair {
        private final Circle circle;
        private final int column;
        private final int row;
        private Cell cell;

        public CircleCellPair(Circle circle, int column, int row, Cell cell) {
            this.circle = circle;
            this.column = column;
            this.row = row;
            this.cell=null;
        }

        public Circle getCircle() {
            return circle;
        }
        
        public Cell getCell() {
            return cell;
        }
        
        public void setCell(Cell cell) {
            this.cell=cell;
        }

        public int getColumn() {
            return column;
        }

        public int getRow() {
            return row;
        }
    }

    // Array to store all circle-cell pairs
    ArrayList<CircleCellPair> circleCellPairs = new ArrayList<>();
    ArrayList<CircleCellPair> safeCellPairs = new ArrayList<>();
    ArrayList<CircleCellPair> homeCellPairs = new ArrayList<>();
     // Adjust size as needed
    private int pairCount = 0;

    // Properties to track selected marbles
    private ArrayList<CircleCellPair> selectedMarbles = new ArrayList<>();
    private Color playerColor = Color.BLUE; // Default color, will be set from Main
    
    public void setPlayerColor(Color color) {
        this.playerColor = color;
    }
    
    // Add buttons for special actions
    private javafx.scene.control.Button swapButton;
    private javafx.scene.control.Button splitButton;
    private javafx.scene.control.Button burnButton;
    private javafx.scene.control.Button discardButton;
    private javafx.scene.control.Button queenDiscardButton;
    private javafx.scene.layout.VBox actionButtonsBox;

    // Add reference to MainScene
    private MainScene mainScene;

    public void setMainScene(MainScene scene) {
        this.mainScene = scene;
    }

    public CircleGrid() {
        // Initialize action buttons with larger size
        swapButton = new javafx.scene.control.Button("Swap Selected Marbles");
        splitButton = new javafx.scene.control.Button("Split Movement");
        burnButton = new javafx.scene.control.Button("Burn Selected Marble");
        discardButton = new javafx.scene.control.Button("Discard Ten");
        queenDiscardButton = new javafx.scene.control.Button("Use Queen Power");
        
        // Style buttons with larger size and blue color scheme
        String buttonStyle = "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; " +
                           "-fx-min-width: 180px; -fx-min-height: 40px; -fx-font-size: 14px;";
        swapButton.setStyle(buttonStyle);
        splitButton.setStyle(buttonStyle);
        burnButton.setStyle(buttonStyle);
        discardButton.setStyle(buttonStyle);
        queenDiscardButton.setStyle(buttonStyle);
        
        // Add hover effect
        String hoverStyle = buttonStyle + "; -fx-background-color: #1976D2;";
        swapButton.setOnMouseEntered(e -> swapButton.setStyle(hoverStyle));
        swapButton.setOnMouseExited(e -> swapButton.setStyle(buttonStyle));
        splitButton.setOnMouseEntered(e -> splitButton.setStyle(hoverStyle));
        splitButton.setOnMouseExited(e -> splitButton.setStyle(buttonStyle));
        burnButton.setOnMouseEntered(e -> burnButton.setStyle(hoverStyle));
        burnButton.setOnMouseExited(e -> burnButton.setStyle(buttonStyle));
        discardButton.setOnMouseEntered(e -> discardButton.setStyle(hoverStyle));
        discardButton.setOnMouseExited(e -> discardButton.setStyle(buttonStyle));
        queenDiscardButton.setOnMouseEntered(e -> queenDiscardButton.setStyle(hoverStyle));
        queenDiscardButton.setOnMouseExited(e -> queenDiscardButton.setStyle(buttonStyle));
        
        // Create VBox for buttons with more spacing
        actionButtonsBox = new javafx.scene.layout.VBox(15); // Increased spacing to 15
        actionButtonsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        actionButtonsBox.setPadding(new javafx.geometry.Insets(20, 0, 0, 20)); // Add padding
        actionButtonsBox.getChildren().addAll(swapButton, splitButton, burnButton, discardButton, queenDiscardButton);
        actionButtonsBox.setVisible(false);
        
        // Position buttons beside the deck with more space
        add(actionButtonsBox, 27, 0, 2, 10); // Increased column span and row span
        
        initialize();
    }

    public void showActionButtons(String cardType) {
        // Show/hide appropriate buttons based on card type
        swapButton.setVisible(cardType.equals("JACK"));
        splitButton.setVisible(cardType.equals("SEVEN"));
        burnButton.setVisible(cardType.equals("BURNER"));
        discardButton.setVisible(cardType.equals("TEN"));
        queenDiscardButton.setVisible(cardType.equals("QUEEN"));
        actionButtonsBox.setVisible(true);
    }

    public void hideActionButtons() {
        actionButtonsBox.setVisible(false);
    }

    public javafx.scene.control.Button getSwapButton() {
        return swapButton;
    }

    public javafx.scene.control.Button getSplitButton() {
        return splitButton;
    }

    public javafx.scene.control.Button getBurnButton() {
        return burnButton;
    }

    public javafx.scene.control.Button getDiscardButton() {
        return discardButton;
    }

    public javafx.scene.control.Button getQueenDiscardButton() {
        return queenDiscardButton;
    }

    // Method to check if a position is a player's entry zone
    private boolean isEntryZone(int position, Color marbleColor) {
        // Map colors to their entry positions
        int entryPosition = -1;
        if (marbleColor.equals(Color.BLUE)) {
            entryPosition = 23; // Adjust these positions based on your board layout
        } else if (marbleColor.equals(Color.GREEN)) {
            entryPosition = 48;
        } else if (marbleColor.equals(Color.YELLOW)) {
            entryPosition = 73;
        } else if (marbleColor.equals(Color.RED)) {
            entryPosition = 98;
        }
        return position == entryPosition;
    }

    // Method to get the safe zone for a color
    private ArrayList<CircleCellPair> getSafeZoneForColor(Color color) {
        // Find the safe zone cells for the given color
        ArrayList<CircleCellPair> safeZone = new ArrayList<>();
        int startIndex = -1;
        
        if (color.equals(Color.BLUE)) {
            startIndex = 0;
        } else if (color.equals(Color.GREEN)) {
            startIndex = 4;
        } else if (color.equals(Color.YELLOW)) {
            startIndex = 8;
        } else if (color.equals(Color.RED)) {
            startIndex = 12;
        }
        
        if (startIndex != -1) {
            for (int i = 0; i < 4; i++) {
                safeZone.add(safeCellPairs.get(startIndex + i));
            }
        }
        
        return safeZone;
    }

    // Method to check if safe zone entry is possible
    private boolean canEnterSafeZone(Color marbleColor) {
        ArrayList<CircleCellPair> safeZone = getSafeZoneForColor(marbleColor);
        for (CircleCellPair pair : safeZone) {
            if (pair.getCircle().getFill().equals(Color.web("#fad082"))) {
                return true;
            }
        }
        return false;
    }

    // Method to move marble to safe zone
    private void moveToSafeZone(CircleCellPair marble, Color marbleColor) {
        ArrayList<CircleCellPair> safeZone = getSafeZoneForColor(marbleColor);
        for (CircleCellPair pair : safeZone) {
            if (pair.getCircle().getFill().equals(Color.web("#fad082"))) {
                // Move marble to first available safe zone spot
                pair.getCircle().setFill(marbleColor);
                marble.getCircle().setFill(Color.web("#fad082"));
                
                // Update cell data
                if (marble.getCell() != null) {
                    pair.setCell(marble.getCell());
                    marble.setCell(null);
                }
                
                // Deselect the marble
                if (selectedMarbles.contains(marble)) {
                    selectedMarbles.remove(marble);
                    marble.getCircle().setStroke(Color.BLACK); marble.getCircle().setStrokeWidth(1);
                }
                break;
            }
        }
    }

    // Method to return a marble to its home zone
    private void returnMarbleToHome(CircleCellPair marble, Color marbleColor) {
        // Find the home zone for this color
        int startIndex = -1;
        if (marbleColor.equals(Color.BLUE)) {
            startIndex = 0;  // First 4 home cells (0-3) are for Blue
        } else if (marbleColor.equals(Color.GREEN)) {
            startIndex = 4;  // Next 4 home cells (4-7) are for Green
        } else if (marbleColor.equals(Color.YELLOW)) {
            startIndex = 8;  // Next 4 home cells (8-11) are for Yellow
        } else if (marbleColor.equals(Color.RED)) {
            startIndex = 12; // Last 4 home cells (12-15) are for Red
        }
        
        // Find an empty spot in the correct home zone
        if (startIndex != -1) {
            boolean foundSpot = false;
            for (int i = 0; i < 4; i++) {
                CircleCellPair homePair = homeCellPairs.get(startIndex + i);
                if (homePair.getCircle().getFill().equals(Color.web("#fad082"))) {
                    // Move marble to home
                    homePair.getCircle().setFill(marbleColor);
                    marble.getCircle().setFill(Color.web("#fad082"));
                    
                    // Update cell data
                    if (marble.getCell() != null) {
                        homePair.setCell(marble.getCell());
                        marble.setCell(null);
                    }
                    
                    // Deselect the marble if it was selected
                    if (selectedMarbles.contains(marble)) {
                        selectedMarbles.remove(marble);
                        marble.getCircle().setStroke(Color.BLACK); marble.getCircle().setStrokeWidth(1);
                    }
                    
                    foundSpot = true;
                    break;
                }
            }
            
            if (!foundSpot) {
                // If no empty spot was found in home zone, show warning
                Platform.runLater(() -> {
                    try {
                        Main.displayAlert2("Home Zone Full", 
                            "No empty spots in home zone for " + getColorName(marbleColor) + " marble!");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    // Helper method to get color name for messages
    private String getColorName(Color color) {
        if (color.equals(Color.BLUE)) return "Blue";
        if (color.equals(Color.GREEN)) return "Green";
        if (color.equals(Color.YELLOW)) return "Yellow";
        if (color.equals(Color.RED)) return "Red";
        return "Unknown";
    }

    // Method to handle marble movement based on card action
    public void moveMarble(CircleCellPair marble, int targetIndex) {
        // Get the target circle from the track
        if (targetIndex >= 0 && targetIndex < circleCellPairs.size()) {
            CircleCellPair targetPair = circleCellPairs.get(targetIndex);
            Color marbleColor = (Color) marble.getCircle().getFill();
            
            // Check if target is entry zone and marble can enter safe zone
            if (isEntryZone(targetIndex, marbleColor)) {
                if (canEnterSafeZone(marbleColor)) {
                    moveToSafeZone(marble, marbleColor);
                    System.out.println("Marble entered safe zone");
                    return;
                } else {
                    // Display safe zone warning using Main's displayAlert2
                    Platform.runLater(() -> {
                        try {
                            Main.displayAlert2("Safe Zone Entry", "Cannot enter safe zone - no available spaces!");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    return;
                }
            }
            
            // Check if target position has another marble
            if (!targetPair.getCircle().getFill().equals(Color.web("#fad082"))) {
                Color targetColor = (Color) targetPair.getCircle().getFill();
                if (!targetColor.equals(marbleColor)) {
                    // Return the target marble to its home
                    returnMarbleToHome(targetPair, targetColor);
                    Platform.runLater(() -> {
                        try {
                            Main.displayAlert2("Marble Collision", 
                                getColorName(marbleColor) + " marble sent " + 
                                getColorName(targetColor) + " marble back to home!");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
            
            // Move the marble
            targetPair.getCircle().setFill(marbleColor);
            
            // Update cell data
            if(marble.getCell() != null) {
                targetPair.setCell(marble.getCell());
                marble.setCell(null);
            }
            
            // Reset the original marble's color
            marble.getCircle().setFill(Color.web("#fad082"));
            
            // Deselect the marble
            if (selectedMarbles.contains(marble)) {
                selectedMarbles.remove(marble);
                marble.getCircle().setStroke(Color.BLACK); marble.getCircle().setStrokeWidth(1);
            }
            
            // Make the newly moved marble selectable if it's the player's color
            if (marbleColor.equals(playerColor)) {
                makeCircleSelectable(targetPair);
            }
        }
    }

    // Method to handle backward movement
    public void moveMarbleBackward(CircleCellPair marble, int steps) {
        int currentIndex = circleCellPairs.indexOf(marble);
        int targetIndex = (currentIndex - steps + circleCellPairs.size()) % circleCellPairs.size();
        moveMarble(marble, targetIndex);
    }
    
    // Helper method to calculate position based on reference size and current size
    private double calcSize(double reference) {
        double scaleFactorX = screenWidth.get() / REFERENCE_WIDTH;
        double scaleFactorY = screenHeight.get() / REFERENCE_HEIGHT;
        // Use a consistent scaling factor to maintain aspect ratio
        return reference * Math.min(scaleFactorX, scaleFactorY);
    }
    
    // Method to update the grid based on new screen dimensions
    public void updateDimensions(double width, double height) {
        screenWidth.set(width);
        screenHeight.set(height);
        
        // Calculate scale factors
        double scaleX = screenWidth.get() / REFERENCE_WIDTH;
        double scaleY = screenHeight.get() / REFERENCE_HEIGHT;
        
        // Use the smaller scale factor to maintain aspect ratio
        double scaleFactor = Math.min(scaleX, scaleY);
        
        // Update circle sizes
        for (CircleCellPair pair : circleCellPairs) {
            pair.getCircle().setRadius(12.0 * scaleFactor);
        }
        
        for (CircleCellPair pair : safeCellPairs) {
            pair.getCircle().setRadius(12.0 * scaleFactor);
        }
        
        for (CircleCellPair pair : homeCellPairs) {
            pair.getCircle().setRadius(12.0 * scaleFactor);
        }
        
        // Update layout properties
        setPrefHeight(640.0 * scaleFactor);
        setPrefWidth(630.0 * scaleFactor);
    }

    private void initialize() {
        setAlignment(javafx.geometry.Pos.CENTER);
        setGridLinesVisible(false);
        setMaxHeight(Double.NEGATIVE_INFINITY);
        setMaxWidth(Double.NEGATIVE_INFINITY);
        setMinHeight(Double.NEGATIVE_INFINITY);
        setMinWidth(Double.NEGATIVE_INFINITY);
        setPrefHeight(640.0);
        setPrefWidth(630.0);
        
        setScaleX(1.4);
        setScaleY(1.3);
        setTranslateX(120.0);
        setLayoutX(300.0);
        setLayoutY(170.0);
        
        setSnapToPixel(false);

        // Create column constraints
        for (int i = 0; i < 26; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setHgrow(javafx.scene.layout.Priority.SOMETIMES);
            column.setMinWidth(10.0);
            column.setPrefWidth(100.0);
            getColumnConstraints().add(column);
        }

        // Create row constraints
        for (int i = 0; i < 26; i++) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(10.0);
            row.setPrefHeight(30.0);
            row.setVgrow(javafx.scene.layout.Priority.SOMETIMES);
            getRowConstraints().add(row);
        }

        // Add all circles
        addCircle(10, 25);
        addCircle(10, 23);
        addCircle(10, 24);
        addCircle(10, 22);
        addCircle(10, 21);
        addCircle(10, 20);
        
        addCircle(9, 20);
        addCircle(8, 20);
        addCircle(7, 20);
        addCircle(6, 20);
        addCircle(5, 20);
        addCircle(4, 20);
        
        addCircle(4, 19);
        addCircle(5,18);
        addCircle(6,17);
        addCircle(7,16);
        
        addCircle(7,15);
        addCircle(6,15);
        addCircle(5,15);
        addCircle(4,15);
        addCircle(3,15);
        addCircle(2,15);
        
        addCircle(2,14);
        addCircle(2,13);
        addCircle(2,12);
        addCircle(2,11);
        
        addCircle(3,11);
        addCircle(4,11);
        addCircle(5,11);
        addCircle(6,11);
        addCircle(7,11);
        
        addCircle(7,10);
        addCircle(6,9);
        addCircle(5,8);
        addCircle(4,7);
        
        addCircle(4,6);
        addCircle(5,6);
        addCircle(6,6);
        addCircle(7,6);
        addCircle(8,6);
        addCircle(9,6);
        addCircle(10,6);
        
        addCircle(10,5);
        addCircle(10,4);
        addCircle(10,3);
        addCircle(10,2);
        addCircle(10,1);
        
        addCircle(11,1);
        addCircle(12,1);
        addCircle(13,1);
        addCircle(14,1);
        
        addCircle(14,2);
        addCircle(14,3);
        addCircle(14,4);
        addCircle(14,5);
        addCircle(14,6);
        
        addCircle(15 ,6);
        addCircle(16 ,6);
        addCircle(17 ,6);
        addCircle(18 ,6);
        addCircle(19 ,6);
        addCircle(20 ,6);
        
        addCircle(20,7);
        
        addCircle(19,8 );
        addCircle(18,9);
        addCircle(17,10);
        addCircle(17,11);
        
        addCircle(18,11);
        addCircle(19,11);
        addCircle(20,11);
        addCircle(21,11);
        addCircle(22,11);
        
        addCircle(22,12);
        addCircle(22,13);
        addCircle(22,14);
        addCircle(22,15);
        
        addCircle(21,15);
        addCircle(20,15);
        addCircle(19,15);
        addCircle(18,15);
        addCircle(17,15);
        
        addCircle(17,16);
        addCircle(18,17);
        addCircle(19,18);
        addCircle(20,19);
        addCircle(20,20);
        
        addCircle(19,20);
        addCircle(18,20);
        addCircle(17,20);
        addCircle(16,20);
        addCircle(15,20);
        addCircle(14,20);
        
        addCircle(14,21);
        addCircle(14,22);
        addCircle(14,23);
        addCircle(14,24);
        addCircle(14,25);
        
        addCircle(13,25);
        addCircle(12,25);
        addCircle(11,25);
        
        //safezone Player
        addSafeZone(12,24);
        addSafeZone(12,23);
        addSafeZone(12,22);
        addSafeZone(12,21);
        
        // homezone Player
        addHomeZone(16,23);
        addHomeZone(17,23);
        addHomeZone(16,24);
        addHomeZone(17,24);
        
        //safezone CPU 1
        addSafeZone(3,13);
        addSafeZone(4,13);
        addSafeZone(5,13);
        addSafeZone(6,13);
        
        // homezone CPU 1
        addHomeZone(2,17);
        addHomeZone(2,18);
        addHomeZone(1,17);
        addHomeZone(1,18);
        
        //safezone CPU 2
        addSafeZone(12,2);
        addSafeZone(12,3);
        addSafeZone(12,4);
        addSafeZone(12,5);
        
        // homezone CPU 2
        addHomeZone(8,3);
        addHomeZone(7,3);
        addHomeZone(8,2);
        addHomeZone(7,2);
       
        //safezone CPU 3
        addSafeZone(21,13);
        addSafeZone(20,13);
        addSafeZone(19,13);
        addSafeZone(18,13);
        
        // homezone CPU 3
        addHomeZone(22,9);
        addHomeZone(22,8);
        addHomeZone(23,9);
        addHomeZone(23,8);
    }

    private void addCircle(int col, int row) {
        Circle circle = new Circle(12.0, javafx.scene.paint.Color.web("#fad082"));
        circle.setStroke(javafx.scene.paint.Color.BLACK);
        circle.setStrokeType(javafx.scene.shape.StrokeType.INSIDE);
        add(circle, col, row);
        
        // Add to the circle-cell pairs array
        circleCellPairs.add(new CircleCellPair(circle, col, row, null));
        pairCount++;
    }
    
    private void addSafeZone(int col, int row) {
        Circle circle = new Circle(12.0, javafx.scene.paint.Color.web("#fad082"));
        circle.setStroke(javafx.scene.paint.Color.BLACK);
        circle.setStrokeType(javafx.scene.shape.StrokeType.INSIDE);
        add(circle, col, row);
        
        // Add to the circle-cell pairs array
        safeCellPairs.add(new CircleCellPair(circle, col, row, null));
        pairCount++;
    }
    
    private void addHomeZone(int col, int row) {
        Circle circle = new Circle(12.0, javafx.scene.paint.Color.web("#fad082"));
        circle.setStroke(javafx.scene.paint.Color.BLACK);
        circle.setStrokeType(javafx.scene.shape.StrokeType.INSIDE);
        add(circle, col, row);
        
        // Add to the circle-cell pairs array
        homeCellPairs.add(new CircleCellPair(circle, col, row, null));
        pairCount++;
    }
    
    public Circle getCircleAt(int col, int row) {
        for (CircleCellPair pair : circleCellPairs) {
            if (pair != null && pair.getColumn() == col && pair.getRow() == row) {
                return pair.getCircle();
            }
        }
        return null;
    }
    
    public Circle getSafeAt(int col, int row) {
        for (CircleCellPair pair : safeCellPairs) {
            if (pair != null && pair.getColumn() == col && pair.getRow() == row) {
                return pair.getCircle();
            }
        }
        return null;
    }
    
    public Circle getHomeAt(int col, int row) {
        for (CircleCellPair pair : homeCellPairs) {
            if (pair != null && pair.getColumn() == col && pair.getRow() == row) {
                return pair.getCircle();
            }
        }
        return null;
    }
    
    public ArrayList<CircleCellPair> getCircleCellPairs() {
        return circleCellPairs;
    }
    
    public ArrayList<CircleCellPair> getSafeCellPairs() {
        return safeCellPairs;
    }
    
    public ArrayList<CircleCellPair> getHomeCellPairs() {
        return homeCellPairs;
    }
    
    public int getPairCount() {
        return pairCount;
    }

    // Method to make marbles selectable for the human player
    public void makeMarbleSelectable() {
        // First clear all selections
        clearMarbleSelections();
        
        // Make marbles of player's color selectable on the main track
        for (CircleCellPair pair : circleCellPairs) {
            if (pair.getCircle().getFill() instanceof Color && 
                ((Color)pair.getCircle().getFill()).equals(playerColor)) {
                makeCircleSelectable(pair);
            }
        }
        
        // Make home zone marbles selectable
        for (CircleCellPair pair : homeCellPairs) {
            if (pair.getCircle().getFill() instanceof Color && 
                ((Color)pair.getCircle().getFill()).equals(playerColor)) {
                makeCircleSelectable(pair);
            }
        }

        // Make safe zone marbles of player's color selectable
        for (CircleCellPair pair : safeCellPairs) {
            if (pair.getCircle().getFill() instanceof Color &&
                ((Color)pair.getCircle().getFill()).equals(playerColor)) {
                makeCircleSelectable(pair);
            }
        }
    }

    /**
     * Makes all marbles on the board selectable, regardless of color
     * Used for special cards like Jack and Seven
     */
    public void makeAllMarblesSelectable() {
        // First clear all selections
        clearMarbleSelections();
        
        // Set special card mode
        setSpecialCardActive(true);
        
        // Make all marbles on track selectable
        for (CircleCellPair pair : circleCellPairs) {
            if (pair.getCircle().getFill() instanceof Color && 
                !((Color)pair.getCircle().getFill()).equals(Color.web("#fad082"))) {
                makeCircleSelectable(pair);
            }
        }
        
        // Make all marbles in home zones selectable
        for (CircleCellPair pair : homeCellPairs) {
            if (pair.getCircle().getFill() instanceof Color && 
                !((Color)pair.getCircle().getFill()).equals(Color.web("#fad082"))) {
                makeCircleSelectable(pair);
            }
        }
    }
    
    /**
     * Makes only OPPONENT marbles on the TRACK selectable (excludes player's own color, safe zone, home zone).
     * Used for the Burner card which can only target opponent marbles on track (not base, not safe, not home).
     */
    public void makeOpponentTrackMarblesSelectable() {
        clearMarbleSelections();
        setSpecialCardActive(true);
        for (CircleCellPair pair : circleCellPairs) {
            javafx.scene.paint.Paint fill = pair.getCircle().getFill();
            if (fill instanceof Color) {
                Color c = (Color) fill;
                // Must be a marble (not empty #fad082), not the player's own color, and solid (not faded safe-zone)
                if (!c.equals(Color.web("#fad082")) && !c.equals(playerColor) && c.getOpacity() > 0.5) {
                    makeCircleSelectable(pair);
                }
            }
        }
    }

    /**
     * Makes all marbles on the TRACK selectable (any colour), excluding safe zone and home zone.
     * Used for the Five card which can move any marble currently on the main track.
     */
    public void makeTrackMarblesSelectable() {
        clearMarbleSelections();
        setSpecialCardActive(true);
        // Only circleCellPairs = track cells. Empty cells have #fad082 fill.
        for (CircleCellPair pair : circleCellPairs) {
            javafx.scene.paint.Paint fill = pair.getCircle().getFill();
            if (fill instanceof Color) {
                Color c = (Color) fill;
                // Not empty (#fad082) and not a faded safe-zone colour — just any solid marble
                if (!c.equals(Color.web("#fad082")) && c.getOpacity() > 0.5) {
                    makeCircleSelectable(pair);
                }
            }
        }
    }

    // Method to get the currently selected marbles
    public ArrayList<CircleCellPair> getSelectedMarbles() {
        return selectedMarbles;
    }
    
    // Method to clear all marble selections
    public void clearMarbleSelections() {
        for (CircleCellPair pair : selectedMarbles) {
            pair.getCircle().setStroke(Color.BLACK); pair.getCircle().setStrokeWidth(1);
        }
        selectedMarbles.clear();
        setSpecialCardActive(false);
    }
    
    // Check if a cell is a trap (for displaying warnings)
    public boolean isTrapCell(int cellIndex) {
        if (cellIndex >= 0 && cellIndex < circleCellPairs.size()) {
            CircleCellPair pair = circleCellPairs.get(cellIndex);
            return pair.getCell() != null && pair.getCell().isTrap();
        }
        return false;
    }

    // Method to handle marble movement from home to track
    public boolean moveMarbleFromHome(CircleCellPair homeMarble, int targetIndex) {
        try {
            // Safety checks
            if (targetIndex < 0 || targetIndex >= circleCellPairs.size()) {
                System.out.println("Invalid target position: " + targetIndex + " (Max: " + (circleCellPairs.size()-1) + ")");
                return false;
            }
            
            // Get the marble color
            Color marbleColor = (Color) homeMarble.getCircle().getFill();
            
            // Get the target circle
            CircleCellPair targetPair = circleCellPairs.get(targetIndex);
            
            // Log target details
            System.out.println("Target circle position: col=" + targetPair.getColumn() + ", row=" + targetPair.getRow());
            
            // Move color from home to target
            Color oldColor = (Color) targetPair.getCircle().getFill();
            targetPair.getCircle().setFill(marbleColor);
            
            System.out.println("Changed circle color from " + oldColor + " to " + marbleColor);
            
            // Clear home marble color
            homeMarble.getCircle().setFill(Color.web("#fad082"));
            
            // Make the newly moved marble selectable if it's the player's color
            if (marbleColor.equals(playerColor)) {
                Circle circle = targetPair.getCircle();
                
                // Add hover effect
                circle.setOnMouseEntered(e -> {
                    if (!selectedMarbles.contains(targetPair)) {
                        circle.setStroke(Color.YELLOW);
                        circle.setStrokeWidth(2);
                    }
                });
                
                circle.setOnMouseExited(e -> {
                    if (!selectedMarbles.contains(targetPair)) {
                        circle.setStroke(Color.BLACK);
                        circle.setStrokeWidth(1);
                    }
                });
                
                // Add click handler for selection
                circle.setOnMouseClicked(e -> {
                    if (selectedMarbles.contains(targetPair)) {
                        // Deselect
                        selectedMarbles.remove(targetPair);
                        circle.setStroke(Color.BLACK);
                        circle.setStrokeWidth(1);
                    } else {
                        // Select
                        selectedMarbles.add(targetPair);
                        circle.setStroke(Color.WHITE);
                        circle.setStrokeWidth(3);
                    }
                });
            }
            
            System.out.println("Successfully moved marble from home to position " + targetIndex);
            return true;
        } catch (Exception e) {
            System.out.println("Error moving marble from home: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Helper method to make a single circle/marble selectable
     */
    private void makeCircleSelectable(CircleCellPair pair) {
        Circle circle = pair.getCircle();
        
        // Add hover effect
        circle.setOnMouseEntered(e -> {
            if (!selectedMarbles.contains(pair)) {
                circle.setStroke(Color.WHITE);
                circle.setStrokeWidth(2);
            }
        });
        
        circle.setOnMouseExited(e -> {
            if (!selectedMarbles.contains(pair)) {
                circle.setStroke(Color.BLACK);
                circle.setStrokeWidth(1);
            }
        });
        
        // Add click handler
        circle.setOnMouseClicked(e -> {
            if (selectedMarbles.contains(pair)) {
                // Deselect marble
                selectedMarbles.remove(pair);
                circle.setStroke(Color.BLACK);
                circle.setStrokeWidth(1);
            } else {
                // For regular cards, only allow one marble selection
                if (!isSpecialCardActive && !selectedMarbles.isEmpty()) {
                    return;
                }
                
                // For Jack card, allow selecting two marbles for swapping
                if (isSpecialCardActive && selectedMarbles.size() >= 2) {
                    return;
                }
                
                // Select marble
                selectedMarbles.add(pair);
                circle.setStroke(Color.WHITE);
                circle.setStrokeWidth(3);
            }
        });
    }

    // Add a flag to track if a special card is active
    private boolean isSpecialCardActive = false;

    public void setSpecialCardActive(boolean active) {
        this.isSpecialCardActive = active;
    }
}