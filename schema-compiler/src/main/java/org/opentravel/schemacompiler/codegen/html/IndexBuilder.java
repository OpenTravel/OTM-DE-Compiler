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

package org.opentravel.schemacompiler.codegen.html;

import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Build the mapping of each Unicode character with it's member lists containing members names starting with it. Also
 * build a list for all the Unicode characters which start a member name. Member name is classkind or field or method or
 * constructor name.
 *
 * <p>
 * This code is not part of an API. It is implementation that is subject to change. Do not use it as an API
 *
 * @since 1.2
 * @see java.lang.Character
 * @author Atul M Dambalkar
 */
public class IndexBuilder {

    /**
     * Mapping of each Unicode Character with the member list containing members with names starting with it.
     */
    private Map<Character,List<LibraryMember>> indexmap = new HashMap<>();

    protected final Object[] elements;

    /**
     * A comparator used to sort classes and members. Note: Maybe this compare code belongs in the tool?
     */
    private class MemberComparator implements Comparator<LibraryMember> {
        public int compare(LibraryMember d1, LibraryMember d2) {
            String n1 = d1.getLocalName();
            String n2 = d2.getLocalName();
            return n1.compareToIgnoreCase( n2 );
        }
    }

    /**
     * Constructor. Build the index map.
     *
     * @param configuration the current configuration of the doclet.
     * @param noDeprecated true if -nodeprecated option is used, false otherwise.
     */
    public IndexBuilder(Configuration configuration, boolean noDeprecated) {
        this( configuration, noDeprecated, false );
    }

    /**
     * Constructor. Build the index map.
     *
     * @param configuration the current configuration of the doclet.
     * @param noDeprecated true if -nodeprecated option is used, false otherwise.
     * @param classesOnly Include only classes in index.
     */
    public IndexBuilder(Configuration configuration, boolean noDeprecated, boolean classesOnly) {
        if (classesOnly) {
            configuration.message.notice( "doclet.Building_Index_For_All_Library_Members" );
        } else {
            configuration.message.notice( "doclet.Building_Index" );
        }
        buildIndexMap( configuration.getModel() );
        Set<Character> set = indexmap.keySet();
        elements = set.toArray();
        Arrays.sort( elements );
    }

    /**
     * Sort the index map. Traverse the index map for all it's elements and sort each element which is a list.
     */
    protected void sortIndexMap() {
        for (Iterator<List<LibraryMember>> it = indexmap.values().iterator(); it.hasNext();) {
            Collections.sort( it.next(), new MemberComparator() );
        }
    }

    /**
     * Get all the members in all the Packages and all the Classes given on the command line. Form separate list of
     * those members depending upon their names.
     *
     * @param model the model for which the documemt is being generated
     */
    protected void buildIndexMap(TLModel model) {
        List<LibraryMember> objects = new ArrayList<>();
        for (TLLibrary lib : model.getUserDefinedLibraries()) {
            objects.addAll( lib.getNamedMembers() );
        }
        adjustIndexMap( objects );
        sortIndexMap();
    }

    /**
     * Adjust list of members according to their names. Check the first character in a member name, and then add the
     * member to a list of members for that particular unicode character.
     *
     * @param objects Array of members.
     */
    protected void adjustIndexMap(Collection<LibraryMember> objects) {
        for (LibraryMember member : objects) {
            String name = member.getLocalName();
            char ch = (name.length() == 0) ? '*' : Character.toUpperCase( name.charAt( 0 ) );
            Character unicode = Character.valueOf( ch );
            indexmap.computeIfAbsent( unicode, u -> new ArrayList<>() ).add( member );
        }
    }

    /**
     * Return a map of all the individual member lists with Unicode character.
     *
     * @return Map index map.
     */
    public Map<Character,List<LibraryMember>> getIndexMap() {
        return indexmap;
    }

    /**
     * Return the sorted list of members, for passed Unicode Character.
     *
     * @param index index Unicode character.
     * @return List member list for specific Unicode character.
     */
    public List<LibraryMember> getMemberList(Character index) {
        return indexmap.get( index );
    }

    /**
     * Array of IndexMap keys, Unicode characters.
     * 
     * @return Object[]
     */
    public Object[] elements() {
        return elements;
    }
}
