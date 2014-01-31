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
package org.opentravel.schemacompiler.validate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeSet;

/**
 * Encapsulates the findings (errors and warnings) discovered during validation. NOTE: This
 * implementation is not currently thread-safe.
 * 
 * <p>
 * NOTE: By default, the error/warning messages displayed by the compiler are obtained from the
 * 'compiler-messages.properties' resource bundle. If an alternative set of messages is needed
 * (including support for non-english messages), the defaults can be overridden by updating the
 * compiler's application context file.
 * 
 * @author S. Livezey
 */
public class ValidationFindings implements Serializable {

    private Collection<ValidationFinding> allFindings = new TreeSet<ValidationFinding>();
    private Map<Validatable, List<String>> messageKeysBySourceObject = new HashMap<Validatable, List<String>>();

    /**
     * Returns true if no findings have been added to this collection.
     * 
     * @return boolean
     */
    public boolean isEmpty() {
        return allFindings.isEmpty();
    }

    /**
     * Returns true if one or more findings have been added to this collection.
     * 
     * @return boolean
     */
    public boolean hasFinding() {
        return !isEmpty();
    }

    /**
     * Returns true if one or more findings have been added for the given source object.
     * 
     * @param source
     *            the source object to check
     * @return boolean
     */
    public boolean hasFinding(Validatable source) {
        return messageKeysBySourceObject.containsKey(source);
    }

    /**
     * Returns true if one or more findings of the indicated type have been added to this
     * collection.
     * 
     * @param type
     *            the type of finding to check for
     * @return boolean
     */
    public boolean hasFinding(FindingType type) {
        return (count(type) > 0);
    }

    /**
     * Returns true if one or more findings of the indicated type have been added for the given
     * source object.
     * 
     * @param source
     *            the source object to check
     * @param type
     *            the type of finding to check for
     * @return boolean
     */
    public boolean hasFinding(Validatable source, FindingType type) {
        return (count(source, type) > 0);
    }

    /**
     * Returns the total number of findings in this collection, regardless of the type.
     * 
     * @return int
     */
    public int count() {
        return allFindings.size();
    }

    /**
     * Returns the number of findings for the given source object in this collection.
     * 
     * @param source
     *            the source object to check
     * @return int
     */
    public int count(Validatable source) {
        int findingCount = 0;

        for (ValidationFinding message : allFindings) {
            if (message.getSource() == source) {
                findingCount++;
            }
        }
        return findingCount;
    }

    /**
     * Returns the number of findings of the specified type in this collection.
     * 
     * @param type
     *            the type of finding for which to return a count
     * @return int
     */
    public int count(FindingType type) {
        int findingCount = 0;

        if (type != null) {
            for (ValidationFinding finding : allFindings) {
                if (finding.getType() == type) {
                    findingCount++;
                }
            }
        }
        return findingCount;
    }

    /**
     * Returns the number findings of the indicated type in this collection for the given source
     * object.
     * 
     * @param source
     *            the source object to check
     * @param type
     *            the type of finding to check for
     * @return int
     */
    public int count(Validatable source, FindingType type) {
        int findingCount = 0;

        for (ValidationFinding message : allFindings) {
            if ((message.getSource() == source) && (message.getType() == type)) {
                findingCount++;
            }
        }
        return findingCount;
    }

    /**
     * Adds a copy of the given finding to this collection. If a duplicate message key has already
     * been added for the source object (regardless of its type), the new finding will be ignored.
     * 
     * @param finding
     *            the validation finding to add
     */
    public void addFinding(ValidationFinding finding) {
        if (finding != null) {
            addFinding(finding.getType(), finding.getSource(), finding.getMessageKey(),
                    finding.getMessageParams());
        }
    }

    /**
     * Adds a new finding of the specified type to this collection. If a duplicate message key has
     * already been added for the source object (regardless of its type), the new finding will be
     * ignored.
     * 
     * @param type
     *            the type of the finding
     * @param source
     *            the object that is the source of the finding
     * @param messageKey
     *            the message key for the finding (required)
     * @param messageParams
     *            the optional message parameters for the finding
     */
    public void addFinding(FindingType type, Validatable source, String messageKey,
            Object... messageParams) {
        if (type == null) {
            throw new NullPointerException("Finding type cannot be null for validation findings.");
        }
        if (source == null) {
            throw new NullPointerException("Source cannot be null for validation findings.");
        }
        if ((messageKey == null) || (messageKey.length() == 0)) {
            throw new NullPointerException(
                    "The message key for validation findings cannot be a null or empty string.");
        }
        List<String> messageKeys = messageKeysBySourceObject.get(source);

        if ((messageKeys == null) || !messageKeys.contains(messageKey)) {
            ValidationFinding message = new ValidationFinding(source, type, messageKey,
                    messageParams);

            if (messageKeys == null) {
                messageKeys = new ArrayList<String>();
                messageKeysBySourceObject.put(source, messageKeys);
            }
            messageKeys.add(messageKey);
            allFindings.add(message);
        }
    }

    /**
     * Adds all of the findings from the given collection into this one.
     * 
     * @param findings
     *            the collection of findings to add (may be null)
     */
    public void addAll(ValidationFindings findings) {
        if (findings != null) {
            for (ValidationFinding otherMessage : findings.allFindings) {
                List<String> messageKeys = messageKeysBySourceObject.get(otherMessage.getSource());

                if ((messageKeys == null) || !messageKeys.contains(otherMessage.getMessageKey())) {
                    if (messageKeys == null) {
                        messageKeys = new ArrayList<String>();
                        messageKeysBySourceObject.put(otherMessage.getSource(), messageKeys);
                    }
                    messageKeys.add(otherMessage.getMessageKey());
                    allFindings.add(otherMessage);
                }
            }
        }
    }

    /**
     * Returns all of the individual validation findings in a list.
     * 
     * @return List<ValidationFinding>
     */
    public List<ValidationFinding> getAllFindingsAsList() {
        List<ValidationFinding> findings = new ArrayList<ValidationFinding>();

        findings.addAll(allFindings);
        return findings;
    }

    /**
     * Returns the individual validation findings associated with the specified source object in a
     * list.
     * 
     * @param source
     *            the source object to filter on
     * @return List<ValidationFinding>
     */
    public List<ValidationFinding> getFindingsAsList(Validatable source) {
        List<ValidationFinding> findings = new ArrayList<ValidationFinding>();

        for (ValidationFinding finding : allFindings) {
            if (finding.getSource() == source) {
                findings.add(finding);
            }
        }
        return findings;
    }

    /**
     * Returns the individual validation findings associated with the indicated
     * <code>FindingType</code> in a list.
     * 
     * @param type
     *            the type of finding to filter on
     * @return List<ValidationFinding>
     */
    public List<ValidationFinding> getFindingsAsList(FindingType type) {
        List<ValidationFinding> findings = new ArrayList<ValidationFinding>();

        for (ValidationFinding finding : allFindings) {
            if (finding.getType() == type) {
                findings.add(finding);
            }
        }
        return findings;
    }

    /**
     * Returns the individual validation findings the indicated <code>FindingType</code> for the
     * specified source object in a list.
     * 
     * @param source
     *            the source object to filter on
     * @param type
     *            the type of finding to filter on
     * @return List<ValidationFinding>
     */
    public List<ValidationFinding> getFindingsAsList(Validatable source, FindingType type) {
        List<ValidationFinding> findings = new ArrayList<ValidationFinding>();

        for (ValidationFinding finding : allFindings) {
            if ((finding.getSource() == source) && (finding.getType() == type)) {
                findings.add(finding);
            }
        }
        return findings;
    }

    /**
     * Returns human-readable validation messages for all findings in the collection.
     * 
     * <p>
     * If a different message format is desired, the compiler's message resource bundle can override
     * the default format by defining the following keys:
     * <ul>
     * <li><code>ValidationFindings.IdentityMessageFormat</code>: Overrides the "identified" message
     * format.</li>
     * <li><code>ValidationFindings.BareMessageFormat</code>: Overrides the "bare" message format.</li>
     * <li><code>ValidationFindings.MessageOnlyFormat</code>: Overrides the "message-only" message
     * format.</li>
     * </ul>
     * 
     * @param format
     *            the message format to use when rendering text for this finding
     * @return String[]
     */
    public String[] getAllValidationMessages(FindingMessageFormat format) {
        List<String> messageList = new ArrayList<String>();

        for (ValidationFinding finding : allFindings) {
            messageList.add(finding.getFormattedMessage(format));
        }
        return messageList.toArray(new String[messageList.size()]);
    }

    /**
     * Returns human-readable validation messages for the specified source object.
     * 
     * See the
     * {@link org.opentravel.schemacompiler.validate.ValidationFindings#getAllValidationmessages(ResourceBundle, boolean)}
     * method description for details on the message formatting options.
     * 
     * @param source
     *            the source object to filter on
     * @param format
     *            the message format to use when rendering text for this finding
     * @return String[]
     */
    public String[] getValidationMessages(Validatable source, FindingMessageFormat format) {
        List<String> messageList = new ArrayList<String>();

        for (ValidationFinding finding : allFindings) {
            if (finding.getSource() == source) {
                messageList.add(finding.getFormattedMessage(format));
            }
        }
        return messageList.toArray(new String[messageList.size()]);
    }

    /**
     * Returns human-readable validation messages for the indicated <code>FindingType</code>.
     * 
     * See the
     * {@link org.opentravel.schemacompiler.validate.ValidationFindings#getAllValidationmessages(ResourceBundle, boolean)}
     * method description for details on the message formatting options.
     * 
     * @param type
     *            the type of finding to filter on
     * @param bundle
     *            the resource bundle used to obtain user-readable messages
     * @param format
     *            the message format to use when rendering text for this finding
     * @return String[]
     */
    public String[] getValidationMessages(FindingType type, FindingMessageFormat format) {
        List<String> messageList = new ArrayList<String>();

        for (ValidationFinding finding : allFindings) {
            if (finding.getType() == type) {
                messageList.add(finding.getFormattedMessage(format));
            }
        }
        return messageList.toArray(new String[messageList.size()]);
    }

    /**
     * Returns human-readable validation messages of the indicated <code>FindingType</code> for the
     * specified source object.
     * 
     * See the
     * {@link org.opentravel.schemacompiler.validate.ValidationFindings#getAllValidationmessages(ResourceBundle, boolean)}
     * method description for details on the message formatting options.
     * 
     * @param source
     *            the source object to filter on
     * @param type
     *            the type of finding to filter on
     * @param bundle
     *            the resource bundle used to obtain user-readable messages
     * @param format
     *            the message format to use when rendering text for this finding
     * @return String[]
     */
    public String[] getValidationMessages(Validatable source, FindingType type,
            FindingMessageFormat format) {
        List<String> messageList = new ArrayList<String>();

        for (ValidationFinding finding : allFindings) {
            if ((finding.getSource() == source) && (finding.getType() == type)) {
                messageList.add(finding.getFormattedMessage(format));
            }
        }
        return messageList.toArray(new String[messageList.size()]);
    }

}
