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

import java.io.IOException;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * 
 * @author Frank Kopp
 *
 */
public class PongUI extends Application {

	// The primary stage
	private static Stage _primaryStage;

	/**
	 * Constructor
	 */
	public PongUI() {
		// empty
	}

	/* (non-Javadoc)
	 * @see javafx.application.Application#init()
	 */
	@Override
	public void init() throws Exception {
		super.init();
	}

	/**
	 * Standard way to start a JavaFX application. Is called in the constructor.
	 * @throws IOException 
	 */
	@Override
	public void start(Stage primaryStage) throws IOException {
		
		_primaryStage = primaryStage;
		_primaryStage.setTitle("Pong by Frank Kopp (c)");
		
		// pong pane
		PongPane pongpane = new PongPane();

		// Create root pane for the Scene
		BorderPane _root = new BorderPane(pongpane);
		_root.setBackground(new Background(
				new BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY)));


		// add a two line hbox for how-to and options
		VBox vBox = new VBox();
		vBox.setAlignment(Pos.CENTER);

		// add game how-to
		Text howtoText = new Text("SPACE=Start ESC=Stop P=Pause Q=left up A=left down UP=right up DOWN=right down");
		vBox.getChildren().add(howtoText);
		
		// add game options
		Text optionsText = new Text("Options");
		vBox.getChildren().add(optionsText);

		_root.setBottom(vBox);
		BorderPane.setAlignment(vBox, Pos.CENTER);

		// Create the Scene
		Scene scene = new Scene(_root, 600, 400);

		// set the minimum size
		_primaryStage.setMinWidth(600);
		_primaryStage.setMinHeight(400);
		_primaryStage.setMaxWidth(600);
		_primaryStage.setMaxHeight(380);

		// put the scene on the primary stage
		_primaryStage.setScene(scene);

		// closeAction - close through close action
		scene.getWindow().setOnCloseRequest(event -> {
			close_action(event);
			event.consume();
		});

		// now show the window
		_primaryStage.show();

		// initialize Game
		pongpane.initialize(optionsText);
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
