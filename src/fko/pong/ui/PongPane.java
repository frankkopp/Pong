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
package fko.pong.ui;

import fko.pong.Player;
import fko.pong.ui.Sounds.Clips;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
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
 * @author Frank Kopp
 */
public class PongPane extends Pane {

	/**
	 * 
	 */
	private static final int BALL_MOVE_INCREMENTS = 2;
	private static final int BALL_SIZE = 5;
	private static final int INITIAL_PADDLE_SIZE = 60;

	private static final double INITIAL_BALL_SPEED = 60.0;
	private static final double INITIAL_PADDLE_SPEED = 60.0;
	private static final double ACCELARATION = 1.05; // factor

	private double _ballSpeed = INITIAL_BALL_SPEED;
	private double _paddleSpeed = INITIAL_PADDLE_SPEED;

	private int _dx = BALL_MOVE_INCREMENTS;
	private int _dy = BALL_MOVE_INCREMENTS;

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
	private StringProperty _leftPlayerPoints = new SimpleStringProperty();
	private StringProperty _rightPlayerPoints = new SimpleStringProperty();

	/**
	 * The pane where the playing takes place
	 */
	public PongPane() {
		super();
		this.setBackground(new Background(
				new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

		_sounds = new Sounds();
		//_sounds.soundOff();
	}

	/**
	 * 
	 */
	public void initialize() {
		addBall();
		addPaddles();
		addScore();

		// set key event to set move flag
		this.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				switch (event.getCode()) {
				case SPACE: startGame();	 break;
				case ESCAPE: stopGame(); break;
				case P: {
					if (_gameRunning) {
						if (_gamePaused) resumeGame();
						else pauseGame();
					}
					break;
				}
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
	 * 
	 */
	private void addScore() {
		Text leftScore = new Text();
		Text rightScore = new Text();
		this.getChildren().add(leftScore);
		this.getChildren().add(rightScore);
		
		System.out.println(Font.getFamilies().toString());
		
		double middle = this.getWidth() / 2;
		final int offsetFromMiddle = 150;
		
		final Font font = Font.font("OCR A Std", FontWeight.BOLD, FontPosture.REGULAR, 40.0);
		final int locationY = 50;
		final Color color = Color.WHITE;
		
		leftScore.setFont(font);
		leftScore.setY(locationY);
		leftScore.setFill(color);

		rightScore.setFont(font);
		rightScore.setY(locationY);
		rightScore.setFill(color);
		
		leftScore.setX(middle - offsetFromMiddle - leftScore.getBoundsInParent().getWidth());
		rightScore.setX(middle + offsetFromMiddle);
		
		leftScore.textProperty().bind(_leftPlayerPoints);
		rightScore.textProperty().bind(_rightPlayerPoints);
	}

	/**
	 * Starts the game with the ball from either of the two sides.
	 * The side is chosen randomly.
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
			_dx = BALL_MOVE_INCREMENTS;
		} else {
			_ballCenterX.setValue(this.getWidth()-_ball.getBoundsInParent().getWidth());
			_dx = -BALL_MOVE_INCREMENTS;
		}
		// random y
		_ballCenterY.setValue(Math.random() * this.getHeight());
		// random direction
		_dy = BALL_MOVE_INCREMENTS * (Math.random() < 0.5 ? 1 : -1);
		_ball.setVisible(true); 
		_ballAnimation.play();
		_gamePaused = false;
		_gameRunning = true;
	}

	/**
	 * 
	 */
	public void stopGame() {
		_ball.setVisible(false); 
		_ballAnimation.stop();
		_gamePaused = false;
		_gameRunning = false;
	}

	/**
	 * 
	 */
	public void pauseGame() {
		if (_gameRunning && _gamePaused) return;
		_gamePaused = true;
		_ballAnimation.stop();
	}

	/**
	 * 
	 */
	public void resumeGame() {
		if (_gameRunning && !_gamePaused) return;
		_gamePaused = false;
		_ballAnimation.play();
	}

	/**
	 * 
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
	 * 
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
	 * 
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
	 * The move per frame
	 */
	private void moveBall() {
		_ballCenterX.setValue(_ballCenterX.getValue() + _dx);
		_ballCenterY.setValue(_ballCenterY.getValue() + _dy);
		checkCollision(); // reverses _dx and/or _dy if collision
	}

	/**
	 * 
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
			_dx *= -1;
			goal(xMin < 0 ? _playerRight : _playerLeft);
		}

		// hit top or bottom wall
		if (yMin < 0 || yMax > this.getHeight()) {
			_sounds.playClip(Clips.WALL);
			_dy *= -1;
		}

		// hit on a paddle 
		if (_dx < 0 && _ball.intersects(_leftPaddle.getBoundsInParent())) { // _dx < 0 && 
			_sounds.playClip(Clips.LEFT);
			_ballSpeed *= ACCELARATION;
			_paddleSpeed *= ACCELARATION;
			_ballAnimation.setRate(_ballAnimation.getRate()*ACCELARATION);
			_paddleAnimation.setRate(_paddleAnimation.getRate()*ACCELARATION);
			_dx *= -1;
		} else if (_dx > 0 && _ball.intersects(_rightPaddle.getBoundsInParent())) { // _dx > 0 && 
			_sounds.playClip(Clips.RIGHT);
			_ballSpeed *= ACCELARATION;
			_paddleSpeed *= ACCELARATION;
			_ballAnimation.setRate(_ballAnimation.getRate()*ACCELARATION);
			_paddleAnimation.setRate(_paddleAnimation.getRate()*ACCELARATION);
			_dx *= -1;
		} 

	}

	/**
	 * 
	 * @param playerScored
	 */
	private void goal(Player playerScored) {
		// hide ball
		_ball.setVisible(false);
		// start from either side of the board
		if (playerScored.equals(_playerLeft)) {
			_ballCenterX.setValue(0.0+_ball.getBoundsInParent().getWidth());	
			_dx = BALL_MOVE_INCREMENTS;
			_playerLeft._points++;
			_leftPlayerPoints.setValue(String.valueOf(_playerLeft._points));
		} else {
			_ballCenterX.setValue(this.getWidth()-_ball.getBoundsInParent().getWidth());
			_dx = -BALL_MOVE_INCREMENTS;
			_playerRight._points++;
			_rightPlayerPoints.setValue(String.valueOf(_playerRight._points));
		}
		// random y
		_ballCenterY.setValue(Math.random() * this.getHeight());
		// random direction
		_dy = BALL_MOVE_INCREMENTS * (Math.random() < 0.5 ? 1 : -1);
		_ballAnimation.pause();
		// short break
		try { Thread.sleep(500);
		} catch (InterruptedException e) {}
		_ball.setVisible(true);
		_ballAnimation.play();
	}




}