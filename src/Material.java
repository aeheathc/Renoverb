/* Renoverb is a program aimed at theater engineers that predicts the effect
   of sound absorbing materials on the reverberation of a room.
   Copyright 2008: Anthony Heathcoat, Nicholas Roth, Jim Simon, Yusuke Hasegawa

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.LinkedList;

public class Material {

	public String name = "", manufacturer = "";
	public double _32Hz, _65Hz, _125Hz, _250Hz, _500Hz, _1kHz, _2kHz, _4kHz, _8kHz;
	public int db_key = -1;
	
	public Material(String name, String manufacturer, double hz, double hz2,
			double hz3, double hz4, double hz5, double hz6, double hz7,
			double hz8, double hz9) {
		this(name, manufacturer, hz, hz2, hz3, hz4, hz5, hz6, hz7, hz8, hz9, -1);
	}
	
	public Material(String name, String manufacturer, double hz, double hz2,
			double hz3, double hz4, double hz5, double hz6, double hz7,
			double hz8, double hz9, int key) {
		this.name = name;
		this.manufacturer = manufacturer;
		_32Hz = hz;
		_65Hz = hz2;
		_125Hz = hz3;
		_250Hz = hz4;
		_500Hz = hz5;
		_1kHz = hz6;
		_2kHz = hz7;
		_4kHz = hz8;
		_8kHz = hz9;
		db_key = key;
	}

	public LinkedList<Double> getCoefficients() {
		LinkedList<Double> c = new LinkedList<Double>();
		c.add(new Double(_32Hz));
		c.add(new Double(_65Hz));
		c.add(new Double(_125Hz));
		c.add(new Double(_250Hz));
		c.add(new Double(_500Hz));
		c.add(new Double(_1kHz));
		c.add(new Double(_2kHz));
		c.add(new Double(_4kHz));
		c.add(new Double(_8kHz));
		return c;
	}
	
}
