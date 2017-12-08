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
package org.nmrfx.processor.datasets.peaks;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bruce Johnson
 */
public class SimpleResonance implements Resonance {

    String atomName = "";
    List<String> names = new ArrayList<>();
    List<PeakDim> peakDims = new ArrayList<>();
    final long id;

    public SimpleResonance(long id) {
        this.id = id;
    }

    @Override
    public void setName(List<String> names) {
        this.names.clear();
        this.names.addAll(names);
    }

    @Override
    public void remove(PeakDim peakDim) {
        peakDims.remove(peakDim);
    }

    @Override
    public String getName() {
        String result = "";
        if (names.size() == 1) {
            result = names.get(0);
        } else if (names.size() > 1) {
            StringBuilder builder = new StringBuilder();
            for (String name : names) {
                if (builder.length() > 0) {
                    builder.append(" ");
                }
                builder.append(name);
            }
            result = builder.toString();
        }
        return result;
    }

    @Override
    public void setName(String name) {
        names.clear();
        names.add(name);
    }

    @Override
    public String getAtomName() {
        return atomName;
    }

    @Override
    public String getIDString() {
        return String.valueOf(id);

    }

    @Override
    public long getID() {
        return id;
    }

    @Override
    public void merge(Resonance resB) {
        List<PeakDim> peakDimsB = resB.getPeakDims();
        System.out.println("merge " + peakDims.size() + " " + peakDimsB.size());
        int sizeA = peakDims.size();
        int sizeB = peakDimsB.size();
        for (PeakDim peakDim : peakDimsB) {
            peakDim.setResonance(this);
            peakDims.add(peakDim);
        }
        peakDimsB.clear();
        System.out.println("mergd " + peakDims.size() + " " + peakDimsB.size());

    }

    public List<PeakDim> getPeakDims() {
        // fixme should be unmodifiable or copy
        return peakDims;
    }

    @Override
    public void add(PeakDim peakDim) {
        peakDim.setResonance(this);
        peakDims.add(peakDim);
    }
}
