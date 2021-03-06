/**
MIT License

Copyright (c) 2017 Frank Kopp

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package fko.pong;

import fko.pong.Sounds.Clips;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * The PongPane handles the playing and screen output.<br>
 * It builds a board with two paddles, a ball a two scores.<br>
 * It adds controls by keyboard and mouse and also adds sound events.<br>
 * @author Frank Kopp
 */
public class PongPane extends Pane implements InvalidationListener {

	private static final int BALL_MOVE_INCREMENTS = 2;
	private static final int BALL_SIZE = 5;
	private static final int INITIAL_PADDLE_SIZE = 60;

	private static final double INITIAL_BALL_SPEED = 60.0;
	private static final double INITIAL_PADDLE_SPEED = 60.0;
	private static final double ACCELARATION = 1.05; // factor

	private double _ballSpeed = INITIAL_BALL_SPEED;
	private double _paddleSpeed = INITIAL_PADDLE_SPEED;

	private double _speedX = BALL_MOVE_INCREMENTS;
	private double _speedY = BALL_MOVE_INCREMENTS;

	private double _paddleSize = INITIAL_PADDLE_SIZE;

	private Timeline _ballAnimation;
	private Timeline _paddleAnimation;

	protected boolean leftPaddleUp = false;
	protected boolean leftPaddleDown = false;
	protected boolean rightPaddleUp = false;
	protected boolean rightPaddleDown = false;

	private Sounds _sounds = new Sounds();
	private Rectangle _leftPaddle;
	private Rectangle _rightPaddle;
	private Circle _ball;

	// The center points of the moving ball
	private DoubleProperty _ballCenterX = new SimpleDoubleProperty();
	private DoubleProperty _ballCenterY = new SimpleDoubleProperty();

	// The position of the paddles
	private DoubleProperty _leftPaddleY = new SimpleDoubleProperty();
	private DoubleProperty _rightPaddleY = new SimpleDoubleProperty();

	// helper for dragging of paddles
	protected double _initialTranslateY;
	protected double _initialDragAnchor;

	// points per player
	Player _playerLeft = new Player("Left");
	Player _playerRight = new Player("Right");

	// status of game
	private boolean _gamePaused = false;
	private boolean _gameRunning = false;

	// the text for the points
	private StringProperty _leftPlayerPoints = new SimpleStringProperty("0");
	private StringProperty _rightPlayerPoints = new SimpleStringProperty("0");

	// text to display options which can be turned on and off
	private StringProperty _optionsTextString = new SimpleStringProperty("Options: ");

	// Options
	private BooleanProperty _soundOn 	= new SimpleBooleanProperty(false);
	private BooleanProperty _anglePaddle = new SimpleBooleanProperty(true);

	/**
	 * The pane where the playing takes place.
	 */
	public PongPane() {
		super();
		this.setBackground(new Background(
				new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

		_sounds = new Sounds();

		// initialize options listener - any time an option property is changed this.invalidated() is called
		_soundOn.addListener(this);
		_anglePaddle.addListener(this);
		updateOptions();
	}

	/**
	 * Initializes the screen by adding a ball, two paddles and two scores.<br>
	 * Also adds the key handler for movements. 
	 * @param optionsText 
	 */
	public void initialize(Text optionsText) {

		optionsText.textProperty().bind(_optionsTextString);

		addBall();
		addPaddles();
		addScore();

		updateOptions();

		// set key event to control game and move flags
		this.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				switch (event.getCode()) {
				// game control
				case SPACE: startGame();	 break;
				case ESCAPE: stopGame(); break;
				case P: {
					if (_gameRunning) {
						if (_gamePaused) resumeGame();
						else pauseGame();
					}
					break;
				}
				// options control
				case DIGIT1: _soundOn.set(!_soundOn.get());; break;
				case DIGIT2: _anglePaddle.set(!_anglePaddle.get()); break;
				// paddle control
				case Q: 		leftPaddleUp = true; break;
				case A:		leftPaddleDown = true; break;
				case UP:	 	rightPaddleUp = true; break;
				case DOWN:  	rightPaddleDown = true; break;
				default:
				}
			}
		});
		this.getScene().setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				switch (event.getCode()) {
				case Q: 		leftPaddleUp = false; break;
				case A:		leftPaddleDown = false; break;
				case UP:	 	rightPaddleUp = false; break;
				case DOWN:  	rightPaddleDown = false; break;
				default:
				}
			}
		}); 
	}

	/**
	 * Is called by the option property objects whenever they change 
	 * @see javafx.beans.InvalidationListener#invalidated(javafx.beans.Observable)
	 */
	@Override
	public void invalidated(Observable observable) {
		updateOptions();
	}

	/**
	 * Updates the text showing which options are active
	 */
	private void updateOptions() {
		// sound
		if (_soundOn.get()) {
			_sounds.soundOn();
		} else {
			_sounds.soundOff();
		}		

		// Options Text
		StringBuilder sb = new StringBuilder("Options: ");
		sb.append("Sound (1) ").append(_soundOn.get() ? "ON" : "OFF").append("  ");
		sb.append("Angling Paddle (2) ").append(_anglePaddle.get() ? "ON" : "OFF").append("  ");
		_optionsTextString.set(sb.toString());
	}

	/**
	 * Adds the display of the score for each player.
	 */
	private void addScore() {
		// create Text for each score 
		Text leftScore = new Text();
		Text rightScore = new Text();
		this.getChildren().add(leftScore);
		this.getChildren().add(rightScore);

		// positioning helpers
		double middle = this.getWidth() / 2;
		final int offsetFromMiddle = 150;

		// layout helpers
		final Font font = Font.font("OCR A Std", FontWeight.BOLD, FontPosture.REGULAR, 40.0);
		final int locationY = 50;
		final Color color = Color.WHITE;

		// left
		leftScore.setFont(font);
		leftScore.setY(locationY);
		leftScore.setFill(color);
		// right
		rightScore.setFont(font);
		rightScore.setY(locationY);
		rightScore.setFill(color);

		// position score text
		leftScore.setX(middle - offsetFromMiddle - leftScore.getBoundsInParent().getWidth());
		rightScore.setX(middle + offsetFromMiddle);

		// bind text to score property
		leftScore.textProperty().bind(_leftPlayerPoints);
		rightScore.textProperty().bind(_rightPlayerPoints);
	}

	/**
	 * Starts the game with the ball from either of the two sides.
	 * The side and start position is chosen randomly.
	 */
	public void startGame() {
		// if game is running do nothing
		if (_gameRunning) return;
		// new players
		_playerLeft = new Player("Left");
		_playerRight = new Player("Right");

		_leftPlayerPoints.setValue(String.valueOf(_playerLeft._points));
		_rightPlayerPoints.setValue(String.valueOf(_playerRight._points));

		// start from either side of the board
		if (Math.random() < 0.5) {
			_ballCenterX.setValue(0.0+_ball.getBoundsInParent().getWidth());	
			_speedX = BALL_MOVE_INCREMENTS;
		} else {
			_ballCenterX.setValue(this.getWidth()-_ball.getBoundsInParent().getWidth());
			_speedX = -BALL_MOVE_INCREMENTS;
		}
		// random y
		_ballCenterY.setValue(Math.random() * this.getHeight());
		// random direction
		_speedY = BALL_MOVE_INCREMENTS * (Math.random() < 0.5 ? 1 : -1);
		_ball.setVisible(true); 
		_ballAnimation.play();
		_gamePaused = false;
		_gameRunning = true;
	}

	/**
	 * Stops the game. Ignored if game not running.
	 */
	public void stopGame() {
		_ball.setVisible(false); 
		_ballAnimation.stop();
		_gamePaused = false;
		_gameRunning = false;
	}

	/**
	 * Pause the game. Ignored if game not running or already paused.
	 */
	public void pauseGame() {
		if (_gameRunning && _gamePaused) return;
		_gamePaused = true;
		_ballAnimation.stop();
	}

	/**
	 * Resume a paused game. Ignored if game not running or game not paused.
	 */
	public void resumeGame() {
		if (_gameRunning && !_gamePaused) return;
		_gamePaused = false;
		_ballAnimation.play();
	}

	/**
	 * Adding the two player paddles to the screen.<br>
	 * Also adding a mouse handler to the paddles.
	 */
	private void addPaddles() {
		double minX = this.getBoundsInLocal().getMinX();
		double maxX = this.getBoundsInLocal().getMaxX();
		double minY = this.getBoundsInLocal().getMinY();
		double maxY = this.getBoundsInLocal().getMaxY();
		final int paddleWidth = 10;
		double left  = minX + 20;
		double right = maxX - 20 - paddleWidth;
		double startPos = (maxY-minY)/2 - _paddleSize/2;

		// enable dragging of paddles with the mouse
		EventHandler<MouseEvent> mouseDragHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				final Rectangle source = (Rectangle) event.getSource();
				final EventType<? extends MouseEvent> eventType = event.getEventType();
				// only the two paddles have these handler so we can do the simple if
				DoubleProperty yProp = source.equals(_leftPaddle) ? _leftPaddleY : _rightPaddleY;
				// handle the three different mouse events
				if (eventType.equals(MouseEvent.MOUSE_PRESSED) ) {
					source.setCursor(Cursor.CLOSED_HAND);
					_initialTranslateY = source.getTranslateY();
					_initialDragAnchor = event.getSceneY();
				} else if (eventType.equals(MouseEvent.MOUSE_DRAGGED) ) {
					double dragY = event.getSceneY() - _initialDragAnchor;
					// don't leave area
					if (_initialTranslateY + dragY > source.getParent().getBoundsInLocal().getMinY() 
							&& _initialTranslateY + dragY + _paddleSize < source.getParent().getBoundsInLocal().getMaxY() ) {
						yProp.setValue(_initialTranslateY + dragY);
					}
				} else if (eventType.equals(MouseEvent.MOUSE_RELEASED) ) {
					source.setCursor(Cursor.OPEN_HAND);
				}
			};
		};

		_leftPaddle = new Rectangle(paddleWidth,_paddleSize, Color.WHITE);
		_leftPaddle.setTranslateX(left);
		_leftPaddleY.set(startPos);
		_leftPaddle.setCursor(Cursor.OPEN_HAND);
		_leftPaddle.translateYProperty().bind(_leftPaddleY);
		_leftPaddle.setOnMousePressed(mouseDragHandler); 
		_leftPaddle.setOnMouseDragged(mouseDragHandler); 
		_leftPaddle.setOnMouseReleased(mouseDragHandler);
		this.getChildren().add(_leftPaddle);

		_rightPaddle = new Rectangle(paddleWidth,_paddleSize, Color.WHITE);
		_rightPaddle.setTranslateX(right);
		_rightPaddleY.set(startPos);
		_rightPaddle.setCursor(Cursor.OPEN_HAND);
		_rightPaddle.translateYProperty().bind(_rightPaddleY);
		_rightPaddle.setOnMousePressed(mouseDragHandler);
		_rightPaddle.setOnMouseDragged(mouseDragHandler);
		_rightPaddle.setOnMouseReleased(mouseDragHandler);
		this.getChildren().add(_rightPaddle);

		_paddleAnimation = new Timeline();
		_paddleAnimation.setCycleCount(Timeline.INDEFINITE);
		KeyFrame movePaddle = 
				new KeyFrame(Duration.seconds(1/_paddleSpeed), e -> { movePaddles();	});
		_paddleAnimation.getKeyFrames().add(movePaddle);
		_paddleAnimation.play();
	}

	/**
	 * Called by the Timeline animation event to move the paddles.
	 */
	private void movePaddles() {
		if (leftPaddleUp 
				&& _leftPaddleY.getValue() > this.getBoundsInLocal().getMinY()) {
			_leftPaddleY.setValue(_leftPaddleY.getValue() - 2);
		}
		if (leftPaddleDown  
				&& _leftPaddleY.getValue() + _paddleSize < this.getBoundsInLocal().getMaxY()) {
			_leftPaddleY.setValue(_leftPaddleY.getValue() + 2);
		}
		if (rightPaddleUp
				&& _rightPaddleY.getValue() > this.getBoundsInLocal().getMinY()) {
			_rightPaddleY.setValue(_rightPaddleY.getValue() - 2);
		}
		if (rightPaddleDown
				&& _rightPaddleY.getValue() + _paddleSize < this.getBoundsInLocal().getMaxY()) {
			_rightPaddleY.setValue(_rightPaddleY.getValue() + 2);
		}
	}

	/**
	 * Adds a ball to the screen. Not visible when game not running. 
	 */
	private void addBall() {
		_ball = new Circle(BALL_SIZE,  Color.WHITE);
		_ballCenterX.setValue(this.getWidth()/2);
		_ballCenterY.setValue(this.getHeight()/2);
		_ball.centerXProperty().bind(_ballCenterX);
		_ball.centerYProperty().bind(_ballCenterY);
		this.getChildren().add(_ball);

		_ballAnimation = new Timeline();
		_ballAnimation.setCycleCount(Timeline.INDEFINITE);
		KeyFrame moveBall = 
				new KeyFrame(Duration.seconds(1/_ballSpeed), e -> {	moveBall();	});
		_ballAnimation.getKeyFrames().add(moveBall);
		_ball.setVisible(false); 
	}

	/**
	 * Called by the Timeline animation event to move the ball.
	 */
	private void moveBall() {
		_ballCenterX.setValue(_ballCenterX.getValue() + _speedX);
		_ballCenterY.setValue(_ballCenterY.getValue() + _speedY);
		checkCollision();
	}

	/**
	 * Checks if the ball has hit a wall, a paddle or has left through left or right wall.<br>
	 * If left through left or right wall we have a goal and the score is increased and the ball resetted on the
	 * scorer's side.   
	 */
	private void checkCollision() {
		double xMin = _ball.getBoundsInParent().getMinX();
		double yMin = _ball.getBoundsInParent().getMinY();
		double xMax = _ball.getBoundsInParent().getMaxX();
		double yMax = _ball.getBoundsInParent().getMaxY();

		// hit left or right wall
		if (xMax < 0 || xMin > this.getWidth()) {
			_sounds.playClip(Clips.GOAL);
			_ballSpeed *= INITIAL_BALL_SPEED;
			_paddleSpeed *= INITIAL_PADDLE_SPEED;
			_ballAnimation.setRate(1.0);
			_paddleAnimation.setRate(1.0);
			_speedX *= -1;
			goal(xMin < 0 ? _playerRight : _playerLeft);
		}

		// hit top or bottom wall
		if (yMin < 0 || yMax > this.getHeight()) {
			_sounds.playClip(Clips.WALL);
			_speedY *= -1;
		}

		// hit on a paddle - left
		if (_speedX < 0 && _ball.intersects(_leftPaddle.getBoundsInParent())) {
			_sounds.playClip(Clips.LEFT);
			_ballSpeed *= ACCELARATION;
			_paddleSpeed *= ACCELARATION;
			_ballAnimation.setRate(_ballAnimation.getRate()*ACCELARATION);
			_paddleAnimation.setRate(_paddleAnimation.getRate()*ACCELARATION);
			// new direction
			
			if (_anglePaddle.get()) {
				DoubleProperty paddle = _leftPaddleY;
				newVector(paddle);
			} else {
				// just changed direction - angle is always constant
				_speedX *= -1;
			}
		// hit on a paddle - right
		} else if (_speedX > 0 && _ball.intersects(_rightPaddle.getBoundsInParent())) {
			_sounds.playClip(Clips.RIGHT);
			_ballSpeed *= ACCELARATION;
			_paddleSpeed *= ACCELARATION;
			_ballAnimation.setRate(_ballAnimation.getRate()*ACCELARATION);
			_paddleAnimation.setRate(_paddleAnimation.getRate()*ACCELARATION);
			// new direction
			if (_anglePaddle.get()) {
				DoubleProperty paddle = _rightPaddleY;
				newVector(paddle);
			} else {
				// just changed direction - angle is always constant
				_speedX *= -1;
			}

		} 

	}

	/**
	 * @param paddle
	 */
	public void newVector(DoubleProperty paddle) {
		
		System.out.println("Old SpeedY: "+_speedY);
		System.out.println("Old SpeedX: "+_speedX);
		
		// calculate where the ball hit the paddle
		// center = 0.0, top=-1-0, bottom=+1.0
		double hitPos = (_ballCenterY.doubleValue() - paddle.doubleValue()) / _paddleSize;
		hitPos = (hitPos-0.5) * 2 * Math.signum(_speedY);
		System.out.println("HitPos: "+hitPos);

		/*
		 * This leads to either convergence to zero or convergence to bigger angles depending on 
		 * the influence of the hitPos. 
		 */
		
		// determine new vector (angle and speed)
		double speed = Math.sqrt(_speedX*_speedX+_speedY*_speedY); // Pythagoras c=speed
		System.out.println("Old Speed: "+speed);
		double angle = Math.atan(_speedY/Math.abs(_speedX)); // current angle in RAD
		System.out.println(String.format("Old Angle: %.2f ",Math.toDegrees(angle)));
		double newAngle = angle * (1+(hitPos)); // influence of the hit position
		System.out.println(String.format("New Angle: %.2f",Math.toDegrees(newAngle)));
		
		// adapt speeds for constant total speed
		_speedY = speed * Math.sin(newAngle);
		System.out.println("New SpeedY: "+_speedY);
		_speedX = Math.signum(_speedX) * speed * Math.cos(newAngle);
		_speedX *= -1; // turn direction
		System.out.println("New SpeedX: "+_speedX);
		System.out.println();
		//pauseGame();
	}

	/**
	 * Increases score for the player who scored and resets the ball to the scorer's side. 
	 * @param playerScored
	 */
	private void goal(Player playerScored) {
		// hide ball
		_ballAnimation.pause();
		_ball.setVisible(false);

		// start from either side of the board
		if (playerScored.equals(_playerLeft)) {
			_ballCenterX.setValue(0.0+_ball.getBoundsInParent().getWidth());	
			_speedX = BALL_MOVE_INCREMENTS;
			_playerLeft._points++;
			_leftPlayerPoints.setValue(String.valueOf(_playerLeft._points));
		} else {
			_ballCenterX.setValue(this.getWidth()-_ball.getBoundsInParent().getWidth());
			_speedX = -BALL_MOVE_INCREMENTS;
			_playerRight._points++;
			_rightPlayerPoints.setValue(String.valueOf(_playerRight._points));
		}
		// random y
		_ballCenterY.setValue(Math.random() * this.getHeight());
		// random direction
		_speedY = BALL_MOVE_INCREMENTS * (Math.random() < 0.5 ? 1 : -1);

		// short break
		try { Thread.sleep(500);
		} catch (InterruptedException e) {}
		_ball.setVisible(true);
		_ballAnimation.play();
	}




}