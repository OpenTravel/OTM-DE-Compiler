/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.opentravel.schemacompiler.codegen.html;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

import org.opentravel.schemacompiler.codegen.html.SourcePosition;

/**
 * Utility for integrating with javadoc tools and for localization.
 * Handle Resources. Access to error and warning counts.
 * Message formatting.
 * <br>
 * Also provides implementation for DocErrorReporter.
 *
 * @see java.util.ResourceBundle
 * @see java.text.MessageFormat
 * @author Neal Gafter (rewrite)
 */
public class Messager {//extends Log implements DocErrorReporter {

	//@Deprecated
    public final PrintWriter errWriter;

    //@Deprecated
    public final PrintWriter warnWriter;

    //@Deprecated
    public final PrintWriter noticeWriter;

    /** The maximum number of errors/warnings that are reported.
     */
    public final int MaxErrors;
    public final int MaxWarnings;
    
    /** The number of errors encountered so far.
     */
    public int nerrors = 0;

    /** The number of warnings encountered so far.
     */
    public int nwarnings = 0;
    
    /** Switch: prompt user on each error.
     */
    public boolean promptOnError;

    public class ExitJavadoc extends Error {
        private static final long serialVersionUID = 0;
    }

    final String programName;

    private ResourceBundle messageRB = null;

    /** The default writer for diagnostics
     */
    static final PrintWriter defaultErrWriter = new PrintWriter(System.err);
    static final PrintWriter defaultWarnWriter = new PrintWriter(System.err);
    static final PrintWriter defaultNoticeWriter = new PrintWriter(System.out);

    /**
     * Constructor
     * @param programName  Name of the program (for error messages).
     */
    public Messager(String programName) {
        this(programName, defaultErrWriter, defaultWarnWriter, defaultNoticeWriter);
    }

    /**
     * Constructor
     * @param programName  Name of the program (for error messages).
     * @param errWriter    Stream for error messages
     * @param warnWriter   Stream for warnings
     * @param noticeWriter Stream for other messages
     */
    protected Messager(String programName,
                       PrintWriter errWriter,
                       PrintWriter warnWriter,
                       PrintWriter noticeWriter) {
       // super(context, errWriter, warnWriter, noticeWriter);
    	this.errWriter = errWriter;
    	this.warnWriter = warnWriter;
    	this.noticeWriter = noticeWriter;
        this.programName = programName;
        this.MaxErrors = getDefaultMaxErrors();
        this.MaxWarnings = getDefaultMaxWarnings();
    }

   
    protected int getDefaultMaxErrors() {
        return Integer.MAX_VALUE;
    }

  
    protected int getDefaultMaxWarnings() {
        return Integer.MAX_VALUE;
    }

    /**
     * Reset resource bundle, eg. locale has changed.
     */
    public void reset() {
        messageRB = null;
    }

    /**
     * Get string from ResourceBundle, initialize ResourceBundle
     * if needed.
     */
    private String getString(String key) {
        if (messageRB == null) {
            try {
                messageRB = ResourceBundle.getBundle(
                          "org.opentravel.schemacompiler.codegen.html.resources.javadoc");
            } catch (MissingResourceException e) {
                throw new Error("Fatal: Resource for javadoc is missing");
            }
        }
        return messageRB.getString(key);
    }

    /**
     * get and format message string from resource
     *
     * @param key selects message from resource
     */
    String getText(String key) {
        return getText(key, (String)null);
    }

    /**
     * get and format message string from resource
     *
     * @param key selects message from resource
     * @param a1 first argument
     */
    String getText(String key, String a1) {
        return getText(key, a1, null);
    }

    /**
     * get and format message string from resource
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     */
    String getText(String key, String a1, String a2) {
        return getText(key, a1, a2, null);
    }

    /**
     * get and format message string from resource
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     * @param a3 third argument
     */
    String getText(String key, String a1, String a2, String a3) {
        return getText(key, a1, a2, a3, null);
    }

    /**
     * get and format message string from resource
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     * @param a3 third argument
     * @param a4 fourth argument
     */
    String getText(String key, String a1, String a2, String a3,
                          String a4) {
        try {
            String message = getString(key);
            String[] args = new String[4];
            args[0] = a1;
            args[1] = a2;
            args[2] = a3;
            args[3] = a4;
            return MessageFormat.format(message, (Object[])args);
        } catch (MissingResourceException e) {
            return "********** Resource for javadoc is broken. There is no " +
                key + " key in resource.";
        }
    }

    /**
     * Print error message, increment error count.
     * Part of DocErrorReporter.
     *
     * @param msg message to print
     */
    public void printError(String msg) {
        printError(null, msg);
    }

    /**
     * Print error message, increment error count.
     * Part of DocErrorReporter.
     *
     * @param pos the position where the error occurs
     * @param msg message to print
     */
    public void printError(SourcePosition pos, String msg) {
        if (nerrors < MaxErrors) {
            String prefix = (pos == null) ? programName : pos.toString();
            errWriter.println(prefix + ": " + getText("javadoc.error") + " - " + msg);
            errWriter.flush();
            prompt();
            nerrors++;
        }
    }

    /**
     * Print warning message, increment warning count.
     * Part of DocErrorReporter.
     *
     * @param msg message to print
     */
    public void printWarning(String msg) {
        printWarning(null, msg);
    }

    /**
     * Print warning message, increment warning count.
     * Part of DocErrorReporter.
     *
     * @param pos the position where the error occurs
     * @param msg message to print
     */
    public void printWarning(SourcePosition pos, String msg) {
        if (nwarnings < MaxWarnings) {
            String prefix = (pos == null) ? programName : pos.toString();
            warnWriter.println(prefix +  ": " + getText("javadoc.warning") +" - " + msg);
            warnWriter.flush();
            nwarnings++;
        }
    }

    /**
     * Print a message.
     * Part of DocErrorReporter.
     *
     * @param msg message to print
     */
    public void printNotice(String msg) {
        printNotice(null, msg);
    }

    /**
     * Print a message.
     * Part of DocErrorReporter.
     *
     * @param pos the position where the error occurs
     * @param msg message to print
     */
    public void printNotice(SourcePosition pos, String msg) {
        if (pos == null)
            noticeWriter.println(msg);
        else
            noticeWriter.println(pos + ": " + msg);
        noticeWriter.flush();
    }

    /**
     * Print error message, increment error count.
     *
     * @param key selects message from resource
     */
    public void error(SourcePosition pos, String key) {
        printError(pos, getText(key));
    }

    /**
     * Print error message, increment error count.
     *
     * @param key selects message from resource
     * @param a1 first argument
     */
    public void error(SourcePosition pos, String key, String a1) {
        printError(pos, getText(key, a1));
    }

    /**
     * Print error message, increment error count.
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     */
    public void error(SourcePosition pos, String key, String a1, String a2) {
        printError(pos, getText(key, a1, a2));
    }

    /**
     * Print error message, increment error count.
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     * @param a3 third argument
     */
    public void error(SourcePosition pos, String key, String a1, String a2, String a3) {
        printError(pos, getText(key, a1, a2, a3));
    }

    /**
     * Print warning message, increment warning count.
     *
     * @param key selects message from resource
     */
    public void warning(SourcePosition pos, String key) {
        printWarning(pos, getText(key));
    }

    /**
     * Print warning message, increment warning count.
     *
     * @param key selects message from resource
     * @param a1 first argument
     */
    public void warning(SourcePosition pos, String key, String a1) {
        printWarning(pos, getText(key, a1));
    }

    /**
     * Print warning message, increment warning count.
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     */
    public void warning(SourcePosition pos, String key, String a1, String a2) {
        printWarning(pos, getText(key, a1, a2));
    }

    /**
     * Print warning message, increment warning count.
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     * @param a3 third argument
     */
    public void warning(SourcePosition pos, String key, String a1, String a2, String a3) {
        printWarning(pos, getText(key, a1, a2, a3));
    }

    /**
     * Print warning message, increment warning count.
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     * @param a3 third argument
     */
    public void warning(SourcePosition pos, String key, String a1, String a2, String a3,
                        String a4) {
        printWarning(pos, getText(key, a1, a2, a3, a4));
    }

    /**
     * Print a message.
     *
     * @param key selects message from resource
     */
    public void notice(String key) {
        printNotice(getText(key));
    }

    /**
     * Print a message.
     *
     * @param key selects message from resource
     * @param a1 first argument
     */
    public void notice(String key, String a1) {
        printNotice(getText(key, a1));
    }

    /**
     * Print a message.
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     */
    public void notice(String key, String a1, String a2) {
        printNotice(getText(key, a1, a2));
    }

    /**
     * Print a message.
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     * @param a3 third argument
     */
    public void notice(String key, String a1, String a2, String a3) {
        printNotice(getText(key, a1, a2, a3));
    }

    /**
     * Return total number of errors, including those recorded
     * in the compilation log.
     */
    public int nerrors() { return nerrors; }

    /**
     * Return total number of warnings, including those recorded
     * in the compilation log.
     */
    public int nwarnings() { return nwarnings; }

    /**
     * Print exit message.
     */
    public void exitNotice() {
        if (nerrors > 0) {
            notice((nerrors > 1) ? "main.errors" : "main.error",
                   "" + nerrors);
        }
        if (nwarnings > 0) {
            notice((nwarnings > 1) ?  "main.warnings" : "main.warning",
                   "" + nwarnings);
        }
    }

    /**
     * Force program exit, e.g., from a fatal error.
     * <p>
     * TODO: This method does not really belong here.
     */
    public void exit() {
        throw new ExitJavadoc();
    }

    /** Prompt user after an error.
     */
    public void prompt() {
        if (promptOnError) {
            System.err.println("resume.abort");// localize?
            try {
                while (true) {
                    switch (System.in.read()) {
                    case 'a': case 'A':
                        System.exit(-1);
                        return;
                    case 'r': case 'R':
                        return;
                    case 'x': case 'X':
                        throw new AssertionError("user abort");
                    default:
                    }
                }
            } catch (IOException e) {}
        }
    }
}
