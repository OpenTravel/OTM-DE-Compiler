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

package org.opentravel.messagevalidate;

import java.io.File;

import org.junit.Test;

public class TestMessageValidation {
	
	@Test
	public void testJsonValidation() throws Exception {
		File codegenFolder = new File( "C:/Software/model-jar-projects/acs-models/src/main/resources/ACS-snapshot_ValidatorOutput" );
		File messageFile = new File( "C:/Software/temp/de-test/CrcTest_CompilerOutput/json/examples/ReservationDomain_8_0_0/Reservation.json" );
		
		new MessageValidator( codegenFolder, System.out ).validate( messageFile );
	}
	
}
