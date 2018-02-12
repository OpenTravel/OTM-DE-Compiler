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

package org.opentravel.application.common;

import javafx.stage.Stage;

/**
 * Base controller class for all JavaFX main window controllers.
 */
public class AbstractMainWindowController {
	
	private Stage primaryStage;
	
	/**
	 * Returns the primary stage for the window associated with this controller.
	 * 
	 * @return Stage
	 */
	protected Stage getPrimaryStage() {
		return primaryStage;
	}
	
	/**
	 * Assigns the primary stage for the window associated with this controller.
	 *
	 * @param primaryStage  the primary stage for this controller
	 */
	protected void initialize(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
	
}
