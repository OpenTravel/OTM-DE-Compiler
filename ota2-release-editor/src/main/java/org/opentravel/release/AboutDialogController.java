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

package org.opentravel.release;

import java.util.Locale;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Controller class for the About appication dialog.
 */
public class AboutDialogController {
	
	private static final ResourceBundle messageBundle = ResourceBundle.getBundle( "ota2-release-messages", Locale.getDefault() );
	
	public static final String FXML_FILE = "/about-dialog.fxml";
	
	private Stage dialogStage;
	
	@FXML private Label buildNumberLabel;
	
	/**
	 * Called when the user clicks the close button of the dialog.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void closeSelected(ActionEvent event) {
		dialogStage.close();
	}
	
	/**
	 * Assigns the stage for the dialog.
	 *
	 * @param dialogStage  the dialog stage to assign
	 */
	public void setDialogStage(Stage dialogStage) {
		buildNumberLabel.setText( messageBundle.getString( "BUILD_NUMBER" ) );
		this.dialogStage = dialogStage;
	}
	
	/**
	 * @see javafx.stage.Stage#showAndWait()
	 */
	public void showAndWait() {
		dialogStage.showAndWait();
	}

}
