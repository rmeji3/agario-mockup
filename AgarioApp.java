import javafx.animation.AnimationTimer;
import javafx.application.Application;


import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;



import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import javafx.stage.Stage;



import java.util.*;


class DoublePair {
	private Double first;
	private Double second;

	public DoublePair(Double first, Double second) {
		this.first = first;
		this.second = second;
	}

	public void setFirst(double first) {
		this.first = first;
	}

	public void setSecond(double second) {
		this.second = second;
	}
	public Double getFirst() {
		return first;
	}

	public Double getSecond() {
		return second;
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}
}

public class AgarioApp extends Application {

	Circle player;

	double scaleFactor;

	Integer startRadius = 20;
	Integer currRadius = 20;
	DoublePair currCoords = new DoublePair(0.0,0.0);

	private final double worldWidth = 10000; // Larger than scene size
	private final double worldHeight = 10000; // Larger than scene size

	HashMap<DoublePair, Circle> foodMap = new HashMap<DoublePair, Circle>();



	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	private Circle spawn(){
		int red = (int) (Math.random() * 256);
		int green = (int) (Math.random() * 256);
		int blue = (int) (Math.random() * 256);
		Circle circle = new Circle(startRadius);
		circle.setFill(Color.rgb(red,green,blue) );
		circle.setStroke(Color.BLACK);
		return circle;
	}
	private Circle generateCircle(double x, double y){
		int red = (int) (Math.random() * 256);
		int green = (int) (Math.random() * 256);
		int blue = (int) (Math.random() * 256);
		Circle circle = new Circle(10);
		circle.setCenterX(x);
		circle.setCenterY(y);
		circle.setFill(Color.rgb(red,green,blue) );
		circle.setStroke(Color.BLACK);
		return circle;
	}

	private void fillFood(){
		double x = Math.random()* worldWidth;
		double y = Math.random()* worldHeight;
		DoublePair pair = new DoublePair(x,y);
		foodMap.put(pair, generateCircle(x,y));
		world.getChildren().add(foodMap.get(pair));
	}
	private void checkForCollision() {
		// Assume scaleFactor is available and represents the current scale of the world/viewport
		scaleFactor = world.getScaleX(); // Assuming uniform scaling for X and Y

		// Adjust the proximity threshold based on the scale
		double adjustedThreshold = currRadius / scaleFactor; // Use inverse of scale for threshold adjustment

		List<DoublePair> toRemove = new ArrayList<>();
		for (Map.Entry<DoublePair, Circle> entry : foodMap.entrySet()) {
			DoublePair key = entry.getKey();
			// Calculate distance considering the world's scale
			double distance = Math.sqrt(Math.pow((currCoords.getFirst() - key.getFirst()) / scaleFactor, 2)
					+ Math.pow((currCoords.getSecond() - key.getSecond()) / scaleFactor, 2));
			if (distance < adjustedThreshold) {
				// Collision detected, mark for removal
				toRemove.add(key);
				// Increase player size or perform other game logic here
				growPlayer(); // Make sure this method adjusts the player size and potentially the zoom level
			}
		}

		// Remove the consumed food items from the map and the pane
		for (DoublePair key : toRemove) {
			Circle foodCircle = foodMap.remove(key);
			world.getChildren().remove(foodCircle);
		}
	}


	private void updateWorld() {
		// Adjust world's position based on movement
		double newTranslateX = Math.min(Math.max(world.getTranslateX() - moveX, -worldWidth + sceneWidth), 0);
		double newTranslateY = Math.min(Math.max(world.getTranslateY() - moveY, -worldHeight + sceneHeight), 0);
		world.setTranslateX(newTranslateX);
		world.setTranslateY(newTranslateY);


		currCoords.setFirst(sceneWidth / 2 - world.getTranslateX());
		currCoords.setSecond(sceneHeight / 2 - world.getTranslateY());


		if (foodMap.size() < foodCount) {
			fillFood();
		}
		checkForCollision();
	}
	private void updateWorldPosition(double distanceX, double distanceY) {
		double newTranslateX = Math.max(Math.min(world.getTranslateX() + distanceX, 0), -worldWidth + sceneWidth);
		double newTranslateY = Math.max(Math.min(world.getTranslateY() + distanceY, 0), -worldHeight + sceneHeight);

		world.setTranslateX(newTranslateX);
		world.setTranslateY(newTranslateY);
	}
	private void handleKeyInput() {
		double tempLastMoveX = 0;
		double tempLastMoveY = 0;

		if (pressedKeys.contains(KeyCode.W)) {
			tempLastMoveY = speed; // Move up
		}
		if (pressedKeys.contains(KeyCode.S)) {
			tempLastMoveY = -speed; // Move down
		}
		if (pressedKeys.contains(KeyCode.A)) {
			tempLastMoveX = speed; // Move left
		}
		if (pressedKeys.contains(KeyCode.D)) {
			tempLastMoveX = -speed; // Move right
		}

		// Only update lastMoveX/Y if a direction key is pressed to avoid stopping the momentum immediately
		if (!pressedKeys.isEmpty()) {
			lastMoveX = tempLastMoveX;
			lastMoveY = tempLastMoveY;
		}
		updateWorld();
	}

	private void updateZoom() {
		// Trigger zoom adjustment when the player reaches or exceeds multiples of 100
		if (currRadius >= 100) { // Starts zooming out once the player size is 100 or more
			double scaleFactor = Math.max(100.0 / currRadius, 0.5); // Calculate the scale factor

			// Apply the scale transformation to the world or viewport
			world.setScaleX(scaleFactor);
			world.setScaleY(scaleFactor);
		}
	}

	private void growPlayer() {
		currRadius += 2; // Grow the player
		player.setRadius(currRadius); // Update the player's visual size
		System.out.println(currRadius);
		scaleFactor = world.getScaleX(); // Update this if your zoom level changes with player size
		// Check if we need to update the zoom based on the new size
		updateZoom();
	}
	private final double sceneWidth = 800;
	private final double sceneHeight = 600;

	double lastMoveX = 0;
	double lastMoveY = 0;

	double speed = 5;
	double moveX = 0;
	double moveY = 0;
	private Set<KeyCode> pressedKeys = new HashSet<>();
	Pane viewport;
	Pane world;
	long lastFrameTime = System.nanoTime();
	
	double damping = 0.98; // Ensure this value is less than 1 to reduce speed over time
	double pixelsPerSecond = 100; // Adjust for desired speed

	int foodCount = 4000;
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		primaryStage.setTitle("Agar.io");

		world = new Pane();
		world.setPrefSize(worldWidth, worldHeight);

		viewport = new Pane(world);

		player = spawn();
		player.setCenterX(sceneWidth / 2);
		player.setCenterY(sceneHeight / 2);

		Scene scene = new Scene(viewport, sceneWidth, sceneHeight);
		scene.setOnKeyPressed(event -> {
			pressedKeys.add(event.getCode());

			handleKeyInput(); // Update movement based on the current pressed keys
		});

		scene.setOnKeyReleased(event -> {
			pressedKeys.remove(event.getCode());
			checkForCollision();
			handleKeyInput(); // Update movement based on the current pressed keys
		});


		viewport.getChildren().add(player);

		viewport.setStyle(
				"-fx-background-color: #e1e1e1; "
		);



		AnimationTimer timer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				double elapsedTimeInSeconds = (now - lastFrameTime) / 1_000_000_000.0;
				lastFrameTime = now;
				checkForCollision();
				// Apply damping when no keys are pressed to gradually slow down
				if (pressedKeys.isEmpty()) {
					lastMoveX *= damping;
					lastMoveY *= damping;
					checkForCollision();
					// Stop completely if movement is very slow to prevent endless minor movement
					if (Math.abs(lastMoveX) < 0.1) lastMoveX = 0;
					if (Math.abs(lastMoveY) < 0.1) lastMoveY = 0;
				}

				double distanceX = lastMoveX * pixelsPerSecond * elapsedTimeInSeconds;
				double distanceY = lastMoveY * pixelsPerSecond * elapsedTimeInSeconds;
				checkForCollision();
				updateWorldPosition(distanceX, distanceY);
			}
		};
		timer.start();


		primaryStage.setScene(scene);
		primaryStage.show();
	}

}