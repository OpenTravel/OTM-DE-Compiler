/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.diffutil;

import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * JavaFX application for the OTM-Diff Utility.
 */
public class Application extends javafx.application.Application {
	
	/**
	 * Main method invoked from the command-line.
	 * 
	 * @param args the command-line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}
	
	/**
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader( Application.class.getResource(
					DiffUtilityController.FXML_FILE ) );
			Parent root = loader.load();
			DiffUtilityController controller = loader.getController();
			UserSettings userSettings = UserSettings.load();
			
			primaryStage.setTitle("OTM-Diff Utility");
			primaryStage.setScene( new Scene( root, userSettings.getWindowSize().getWidth(),
					userSettings.getWindowSize().getHeight() ) );
			primaryStage.setX( userSettings.getWindowPosition().getX() );
			primaryStage.setY( userSettings.getWindowPosition().getY() );
			
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					UserSettings settings = UserSettings.load();
					Scene scene = primaryStage.getScene();
					
					settings.setWindowPosition( new Point(
							primaryStage.xProperty().intValue(), primaryStage.yProperty().intValue() ) );
					settings.setWindowSize( new Dimension(
							scene.widthProperty().intValue(), scene.heightProperty().intValue() ) );
					settings.save();
				}
			});
			controller.setPrimaryStage( primaryStage );
			primaryStage.show();
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
			throw new RuntimeException("Unable to initialize JavaFX application.", e);
		}
	}
}
