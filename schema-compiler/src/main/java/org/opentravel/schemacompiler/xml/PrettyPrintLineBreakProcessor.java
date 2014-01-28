package org.opentravel.schemacompiler.xml;

import org.w3c.dom.Document;

/**
 * Line break processor that inserts line-break tokens into a DOM tree, indicating where additional
 * blank spaces should be inserted during the XML formatting process.
 * 
 * @author S. Livezey
 */
public interface PrettyPrintLineBreakProcessor {

    public static final String LINE_BREAK_TOKEN = "__LINE_BREAK__";
    public static final String LINE_BREAK_COMMENT = "<!--" + LINE_BREAK_TOKEN + "-->";

    /**
     * Processes the content of the given DOM document, inserting <code>LINE_BREAK_TOKEN</code>
     * comments at any position where additional line breaks will be required during XML formatting.
     * 
     * @param document
     *            the DOM document to process
     */
    public void insertLineBreakTokens(Document document);

}
