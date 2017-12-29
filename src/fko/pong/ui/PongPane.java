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

import fko.pong.ui.Sounds.Clips;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.util.Duration;

/**
 * @author Frank Kopp
 */
public class PongPane extends Pane {

	private static final int BALL_SIZE = 5;
	private static final int INITIAL_PADDLE_SIZE = 60;

	private static final double INITIAL_BALL_SPEED = 60.0;
	private static final double INITIAL_PADDLE_SPEED = 60.0;
	private static final double ACCELARATION = 1.05; // factor
	
	private double _ballSpeed = INITIAL_BALL_SPEED;
	private double _paddleSpeed = INITIAL_PADDLE_SPEED;

	private int _dx = 2;
	private int _dy = 2;

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
	private DoubleProperty _centerX = new SimpleDoubleProperty();
	private DoubleProperty _centerY = new SimpleDoubleProperty();

	// The position of the paddles
	private DoubleProperty _leftPaddleY = new SimpleDoubleProperty();
	private DoubleProperty _rightPaddleY = new SimpleDoubleProperty();

	// helper for dragging of paddles
	protected double _initialTranslateY;
	protected double _initialDragAnchor;

	/**
	 * The pane where the playing takes place
	 */
	public PongPane() {
		super();
		this.setBackground(new Background(
				new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
		_sounds = new Sounds();
		//		_sounds.soundOff();
	}

	/**
	 * Creates a ball  and two paddles and starts moving the ball
	 */
	public void startGame() {
		addBall();
		addPaddles();
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
		EventHandler<MouseEvent> eventHandler = new EventHandler<MouseEvent>() {
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
		_leftPaddle.addEventHandler(MouseEvent.MOUSE_PRESSED, eventHandler);
		_leftPaddle.addEventHandler(MouseEvent.MOUSE_DRAGGED, eventHandler);
		_leftPaddle.addEventHandler(MouseEvent.MOUSE_RELEASED, eventHandler);
		this.getChildren().add(_leftPaddle);

		_rightPaddle = new Rectangle(paddleWidth,_paddleSize, Color.WHITE);
		_rightPaddle.setTranslateX(right);
		_rightPaddleY.set(startPos);
		_rightPaddle.setCursor(Cursor.OPEN_HAND);
		_rightPaddle.translateYProperty().bind(_rightPaddleY);
		_rightPaddle.addEventHandler(MouseEvent.MOUSE_PRESSED, eventHandler);
		_rightPaddle.addEventHandler(MouseEvent.MOUSE_DRAGGED, eventHandler);
		_rightPaddle.addEventHandler(MouseEvent.MOUSE_RELEASED, eventHandler);
		this.getChildren().add(_rightPaddle);

		// set key event to set move flag
		this.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				switch (event.getCode()) {
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
		_centerX.setValue(this.getWidth()/2);
		_centerY.setValue(this.getHeight()/2);
		_ball.centerXProperty().bind(_centerX);
		_ball.centerYProperty().bind(_centerY);
		this.getChildren().add(_ball);

		_ballAnimation = new Timeline();
		_ballAnimation.setCycleCount(Timeline.INDEFINITE);
		KeyFrame moveBall = 
				new KeyFrame(Duration.seconds(1/_ballSpeed), e -> {	moveBall();	});
		_ballAnimation.getKeyFrames().add(moveBall);
	}

	/**
	 * The move per frame
	 */
	private void moveBall() {
		_centerX.setValue(_centerX.getValue() + _dx);
		_centerY.setValue(_centerY.getValue() + _dy);
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
		if (xMin < 0 || xMax > this.getWidth()) {
			_sounds.playClip(Clips.WALL);
			_ballSpeed *= INITIAL_BALL_SPEED;
			_paddleSpeed *= INITIAL_PADDLE_SPEED;
			_ballAnimation.setRate(1.0);
			_paddleAnimation.setRate(1.0);
			_dx *= -1;
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




}