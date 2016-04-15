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
package org.opentravel.examplehelper;

import java.io.Reader;
import java.io.StringReader;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.lexers.XmlLexer;

/**
 * Read-only editor pane that displays a preview of an XML document with syntax highlighting.
 */
public class ExamplePane extends JEditorPane {
	
	private static final long serialVersionUID = -2828075659125761018L;
	
	public static final String XML_FORMAT  = "text/xhtml";
	public static final String JSON_FORMAT = "text/json";
	
	private String exampleFormat = XML_FORMAT;
	private String exampleContent;
	private boolean initialized = false;
	
	/**
	 * Returns the contents of example document that is currently being displayed.
	 * 
	 * @return String
	 */
	public String getExampleContent() {
		return exampleContent;
	}
	
	/**
	 * Assigns the example format that should be used for syntax highlighting of
	 * of the example document.
	 *
	 * @param exampleFormat  the example format content type to assign
	 */
	public void setExampleFormat(String exampleFormat) {
		this.exampleFormat = exampleFormat;
		setContentType( exampleFormat );
	}

	/**
	 * Assigns the example content that will be displayed with syntax highlighting.
	 * 
	 * @param exampleContent  the example content to be displayed
	 */
	public void setExampleContent(String exampleContent) {
		if (!initialized) {
			DefaultSyntaxKit.initKit();
			setContentType( exampleFormat );
			initialized = true;
		}
		setText( exampleContent );
		this.exampleContent = exampleContent;
	}

	/**
	 * @see javax.swing.text.JTextComponent#setDocument(javax.swing.text.Document)
	 */
	@Override
	public void setDocument(Document doc) {
		if (!(doc instanceof SyntaxDocument)) {
			try {
				Reader reader = new StringReader( doc.getText( 0, doc.getLength() ) );
				doc = new SyntaxDocument( new XmlLexer( reader ) );
				
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		super.setDocument( doc );
	}

}
