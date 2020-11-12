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

 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nmrfx.processor.math.units;

import org.nmrfx.processor.math.Vec;

/**
 * An Index is an Integer index of a Vec.
 *
 * @author johnsonb
 */
public class Index extends Unit<Integer> {

    public Index(Integer i) {
        field = i;
    }

    public Index(String frac) {
        field = Integer.parseInt(frac);
    }

    public String toString() {
        return field + "";
    }

    public double getDoublePosition(Vec vec) {
        return vec.getDoublePosition(this);
    }
}
