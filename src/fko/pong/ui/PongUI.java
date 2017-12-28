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

import java.io.IOException;

import com.sun.javafx.application.PlatformImpl;

import fko.pong.Pong;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * 
 * @author Frank Kopp
 *
 */
public class PongUI extends Application {

	// The singleton instance of this class
	private static PongUI _instance = null; 	

	// The primary stage
	private static Stage _primaryStage;

	/**
	 * UI is a singleton and can't be created via constructor.<br> 
	 * Use this <code>getInstance()</code> instead.
	 */
	public static PongUI getInstance() {
		if (_instance == null) {// singleton pattern 
			_instance = new PongUI();
		}
		return _instance;
	}

	/**
	 * Private constructor to accommodate the Singleton pattern
	 */
	private PongUI() {

		// Startup the JavaFX platform
		Platform.setImplicitExit(false);

		PlatformImpl.startup(() -> {
			_primaryStage = new Stage();
			_primaryStage.setTitle("Pong by Frank Kopp (c)");
			try {
				start(_primaryStage);
			} catch (IOException e) {
				Pong.fatalError("Error while starting UI");
			}
		});

		waitForUI(); // wait until primary stage is shown
	}

	/**
	 * Standard way to start a JavaFX application. Is called in the constructor.
	 * @throws IOException 
	 */
	@Override
	public void start(Stage primaryStage) throws IOException {

		// pong pane
		PongPane pongpane = new PongPane();
		
		// Create root pane for the Scene
		BorderPane _root = new BorderPane(pongpane);

		// Create the Scene
		Scene scene = new Scene(_root, 600, 400);

		// set the minimum size
		_primaryStage.setMinWidth(600);
		_primaryStage.setMinHeight(400);
		_primaryStage.setMaxWidth(600);
		_primaryStage.setMaxHeight(380);

		// pu the scene on the primary stage
		_primaryStage.setScene(scene);
		
		// closeAction - close through close action
		scene.getWindow().setOnCloseRequest(event -> {
			close_action(event);
			event.consume();
		});

		// now show the window
		_primaryStage.show();

		pongpane.startGame();
	}

	/**
	 * Waits for the UI to show.
	 */
	public void waitForUI() {
		// wait for the UI to show before returning
		do {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				//return;
			}
		} while (_primaryStage == null || !_primaryStage.isShowing());
	}

	/**
	 * @return the primary stage which has been stored as a static field
	 */
	public static Stage getPrimaryStage() {
		return _primaryStage;
	}
	
	/* ********************************
	 * ACTIONS
	 * ********************************/
	
	/**
	 * @param event
	 */
	public void close_action(WindowEvent event) {
		Pong.exit();
	}

}
