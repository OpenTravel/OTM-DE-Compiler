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

package org.opentravel.schemacompiler.xml;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Encapsulates the logic required to format an XML output stream using the DOM load-and-save (LS) utilities. In
 * addition to the line-spacing logic that is implemented by this class, all XML output is also converted to UTF-8
 * encoding when content is written to the file system.
 * 
 * @author S. Livezey
 */
public class XMLPrettyPrinter {

    public static final String LINE_BREAK_TOKEN = "__LINE_BREAK__";
    public static final String LINE_BREAK_COMMENT = "<!--" + LINE_BREAK_TOKEN + "-->";

    private static final Logger log = LogManager.getLogger( XMLPrettyPrinter.class );

    private static final DocumentBuilder docBuilder;
    private static final DOMImplementationLS domLS;

    private PrettyPrintLineBreakProcessor lineBreakProcessor;

    /**
     * Default constructor.
     */
    public XMLPrettyPrinter() {}

    /**
     * Constructor that provides a <code>PrettyPrintLineBreakProcessor</code> to be utilized during XML formatting.
     * 
     * @param lineBreakProcessor the line break processor instance
     */
    public XMLPrettyPrinter(PrettyPrintLineBreakProcessor lineBreakProcessor) {
        this.lineBreakProcessor = lineBreakProcessor;
    }

    /**
     * Constructs a new DOM document using the default <code>DocumentBuilder</code> implementation.
     * 
     * @return Document
     */
    public static Document newDocument() {
        return docBuilder.newDocument();
    }

    /**
     * Produces formatted XML output using the given document content, sending it to the specified output stream.
     * 
     * @param document the DOM document that defines the XML document's content
     * @param out the output stream that will receive the formatted content
     */
    public void formatDocument(Document document, OutputStream out) {
        try {
            LSSerializer serializer = domLS.createLSSerializer();
            LSOutput lsOut = domLS.createLSOutput();
            Writer writer = new LineBreakTokenWriter( out );

            if (lineBreakProcessor != null) {
                lineBreakProcessor.insertLineBreakTokens( document );
            }
            serializer.getDomConfig().setParameter( "format-pretty-print", true );
            lsOut.setCharacterStream( writer );

            serializer.write( document, lsOut );
            writer.flush();

        } catch (Exception e) {
            log.warn( "Error formatting XML document.", e );
        }
    }

    /**
     * Writer that intercepts the XML output produced by the pretty-printer class, replacing line-break tokens with
     * actual line breaks in the underlying output stream.
     * 
     * @author S. Livezey
     */
    private class LineBreakTokenWriter extends Writer {

        private StringBuilder buffer = new StringBuilder();
        private BufferedWriter outputWriter;

        /**
         * Constructor that provides the underlying writer to which output should be directed.
         * 
         * @param outputStream the underlying output stream
         */
        public LineBreakTokenWriter(OutputStream outputStream) {
            this.outputWriter = new BufferedWriter( new OutputStreamWriter( outputStream, StandardCharsets.UTF_8 ) );
        }

        /**
         * @see java.io.Writer#write(char[], int, int)
         */
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            for (int i = 0; i < len; i++) {
                char ch = cbuf[i];

                buffer.append( ch );

                if (ch == '\n') {
                    outputWriter.write( buffer.toString().replaceAll( LINE_BREAK_COMMENT, "" ) );
                    buffer.setLength( 0 );
                }
            }
        }

        /**
         * @see java.io.Writer#flush()
         */
        @Override
        public void flush() throws IOException {
            outputWriter.write( buffer.toString().replaceAll( LINE_BREAK_COMMENT, "" ) );
            outputWriter.flush();
            buffer.setLength( 0 );
        }

        /**
         * @see java.io.Writer#close()
         */
        @Override
        public void close() throws IOException {
            flush();
            outputWriter.close();
        }

    }

    /**
     * Initializes the document builder instance used to construct documents and DOM serializer components.
     */
    static {
        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            domLS = (DOMImplementationLS) docBuilder.getDOMImplementation().getFeature( "LS", "3.0" );

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
