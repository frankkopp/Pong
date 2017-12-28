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

import fko.pong.ui.PongUI;

/**
 * 
 * @author Frank Kopp
 */
public class Pong {

	// VERSION
	public static final String VERSION = "0.1"; 
	
	/**
	 * The handle to the user interface class
	 */
	public static PongUI _ui;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// creates and starts ui
		_ui = PongUI.getInstance();
		// exit - ui stays alive and handles further actions
	}
	
	 /**
     * Clean up and exit the application
     */
    public static void exit() {
    		exit(0);
    }

    /**
     * Clean up and exit the application
     */
    private static void exit(int returnCode) {
        // nothing to clean up yet
        System.exit(returnCode);
    }
    
    /**
     * Called when there is an unexpected unrecoverable error.<br/>
     * Prints a stack trace together with a provided message.<br/>
     * Terminates with <tt>exit(1)</tt>.
     * @param message to be displayed with the exception message
     */
    public static void fatalError(String message) {
        Exception e = new Exception(message);
        e.printStackTrace();
        exit(1);
    }

    /**
     * Called when there is an unexpected but recoverable error.<br/>
     * Prints a stack trace together with a provided message.<br/>
     * @param message to be displayed with the exception message
     */
    public static void criticalError(String message) {
        Exception e = new Exception(message);
        e.printStackTrace();
    }
    
    /**
     * Called when there is an unexpected minor error.<br/>
     * Prints a provided message.<br/>
     * @param message to be displayed
     */
    public static void minorError(String message) {
        System.err.println(message);
    }
	
}
