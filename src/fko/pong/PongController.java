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

import javafx.stage.WindowEvent;

/**
 * PongController
 * 31.12.2017
 * @author Frank Kopp
 */
public class PongController {
	
	private PongModel model;

	/**
	 * @param model
	 */
	public PongController(PongModel model) {
		this.model = model;
		
	}

	/**
	 * 
	 */
	public void startGameAction() {
		System.out.println (new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * 
	 */
	public void stopGameAction() {
		System.out.println (new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * 
	 */
	public void pauseGameAction() {
		System.out.println (new Exception().getStackTrace()[0].getMethodName());		
	}

	/**
	 * 
	 */
	public void soundOnOptionAction() {
		System.out.println (new Exception().getStackTrace()[0].getMethodName());
//		// sound
//		if (_soundOn.get()) {
//			_sounds.soundOn();
//		} else {
//			_sounds.soundOff();
//		}	
	}

	/**
	 * 
	 */
	public void anglePaddleOptionAction() {
		System.out.println (new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * 
	 */
	public void onLeftPaddleUpAction() {
		System.out.println (new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * 
	 */
	public void onLeftPaddleDownAction() {
		System.out.println (new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * 
	 */
	public void onRightPaddleUpAction() {
		System.out.println (new Exception().getStackTrace()[0].getMethodName());		
	}

	/**
	 * 
	 */
	public void onRightPaddleDownAction() {
		System.out.println (new Exception().getStackTrace()[0].getMethodName());		
	}

	/**
	 * @param event
	 */
	public void close_action(WindowEvent event) {
		Pong.exit();		
	}

}
