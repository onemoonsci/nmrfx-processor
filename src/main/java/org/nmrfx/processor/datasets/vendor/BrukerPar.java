/*
 * NMRFx Processor : A Program for Processing NMR Data 
 * Copyright (C) 2004-2017 One Moon Scientific, Inc., Westfield, N.J., USA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.nmrfx.processor.datasets.vendor;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrukerPar {

    static Map parGroups = new HashMap();
    static long nParGroups = 0;
    private String name = null;
    private String value = null;

    static Logger logger = Logger.getLogger("org.nmrfx.processor.datasets.Dataset");

    public BrukerPar(String handle, String newName, String newValue) {
        name = newName.intern();
        value = newValue.intern();

        Map parMap = (Map) parGroups.get(handle);

        if (parMap == null) {
            parMap = new HashMap();
            parGroups.put(handle, parMap);
        }

        parMap.put(newName, this);
    }

    public static boolean handleExists(String handleName) {
        return parGroups.containsKey(handleName);
    }

    public static String getNextHandle() {
        nParGroups++;

        return "bpar" + nParGroups;
    }

    public static void removeParGroup(String handle) {
        parGroups.remove(handle);
    }

    public static BrukerPar getPar(String handle, String name) {
        Map parMap = (Map) parGroups.get(handle);

        if (parMap == null) {
            return null;
        }

        return (BrukerPar) parMap.get(name);
    }

    /**
     * parse a Bruker parameter file
     *
     * @param pmap : HashMap to store parameters
     * @param filename : parameter file to read
     * @param iDim : data dimension
     * @param strict: convert parameter names to JCAMP standard (strip space etc.)
     */
    static void processBrukerParFile(final HashMap<String, String> pmap, final String filename, final int iDim, final boolean strict)
            throws NMRParException {

        Pattern brukerPattern2 = Pattern.compile("\\s*(##)([^=]*)(=\\s*)(.*)");
        Pattern brukerPattern1 = Pattern.compile("\\s*(##\\$)([^=]*)(=\\s*)(.*)");
        Pattern[] patterns = {brukerPattern1, brukerPattern2};
        boolean haveParameter = false;
        String parName = "";
        String value = "";
        int pageNum = 0;
        // fixme what about NTUPLES
        ArrayList<String> values = new ArrayList<String>();
        try (LineNumberReader lineReader = new LineNumberReader(new FileReader(filename))) {
            // multi-line arrays need a little work
            while (lineReader.ready()) {
                String attrLine = lineReader.readLine();

                if (attrLine == null) {
                    break;
                }
                int commentStart = attrLine.indexOf("$$");
                if (commentStart != -1) {
                    attrLine = attrLine.substring(0, commentStart);
                }
                attrLine = attrLine.trim();
                boolean gotPar = false;
                for (int i = 0; i < patterns.length; i++) {
                    Matcher m = patterns[i].matcher(attrLine);
                    if (m.matches() && (m.groupCount() == 4)) {
                        if (haveParameter) {
                            storeParameter(pmap, parName + "," + iDim, values);
                        }
                        values.clear();
                        parName = m.group(2);
                        parName = parName.replace(" ", "");
                        if (strict) {
                            parName = parName.replace("/", "");
                            parName = parName.replace("_", "");
                            parName = parName.toUpperCase();
                        }
                        value = m.group(4).trim();
                        gotPar = true;
                        haveParameter = true;
                        if (parName.equals("PAGE")) {
                            pageNum = Integer.parseInt(value.substring(2));
                        } else if (parName.equals("DATATABLE")) {
                            parName = parName + pageNum;
                        }
                        break;
                    }
                }
                if (!gotPar) {
                    value = attrLine.trim();
                }
                int vlen = value.length();
                if ((vlen > 1) && (value.charAt(0) == '<') && (value.charAt(vlen - 1) == '>')) {
                    value = value.substring(1, vlen - 1);
                }
                if (!gotPar) {
                    value = '\n' + value;
                }
                values.add(value);
            }
            // misses last parameter value, but last param should be END anyway
        } catch (IOException ioE) {
            throw new NMRParException("error reading bruker par string");
        }
    }

    /**
     * store parameter name and value in a hashmap
     *
     * @param pmap : HashMap to store parameters
     * @param parName : parameter name
     * @param values : parameter value list
     */
    static void storeParameter(final HashMap<String, String> pmap, final String parName, ArrayList<String> values) {
        int nValues = values.size();
        String value = "";
        if (nValues == 1) {
            value = values.get(0);
        } else {
            StringBuffer sBuf = new StringBuffer();
            for (int i = 0; i < nValues; i++) {
                if (i > 0) {
                    sBuf.append(" ");
                }
                sBuf.append(values.get(i));

            }
            value = sBuf.toString();
        }
        pmap.put(parName, value);
//        pmap.put(parName, new BrukerPar(parName, value));
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }
}
