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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Parser {



	public static LinkedList<Material> parse(String filepath) throws NumberFormatException, IOException{
		File file = new File(filepath);
		return parse(file);
	}

	public static LinkedList<Material> parse(File file) throws NumberFormatException, IOException{
		LinkedList<Material> mats = new LinkedList<Material>();
		BufferedReader sc = new BufferedReader(new FileReader(file));
		String name = null, manufacturer = null;
		String _32Hz;
		String _65Hz;
		String _125Hz;
		String _250Hz;
		String _500Hz;
		String _1kHz;
		String _2kHz;
		String _4kHz;
		String _8kHz;

		while(sc.ready()) {
			name = sc.readLine();	
			manufacturer = sc.readLine();
			_32Hz  = sc.readLine();
			_65Hz  = sc.readLine();
			_125Hz = sc.readLine();
			_250Hz = sc.readLine();
			_500Hz = sc.readLine();
			_1kHz  = sc.readLine();
			_2kHz  = sc.readLine();
			_4kHz  = sc.readLine();
			_8kHz  = sc.readLine();
			if (_32Hz.equals("?"))	{_32Hz  = "0.0";}
			if (_65Hz.equals("?"))	{_65Hz  = "0.0";}
			if (_125Hz.equals("?"))	{_125Hz = "0.0";}
			if (_250Hz.equals("?"))	{_250Hz = "0.0";}
			if (_500Hz.equals("?"))	{_500Hz = "0.0";}
			if (_1kHz.equals("?"))	{_1kHz  = "0.0";}
			if (_2kHz.equals("?"))	{_2kHz  = "0.0";}
			if (_4kHz.equals("?"))	{_4kHz  = "0.0";}
			if (_8kHz.equals("?"))	{_8kHz  = "0.0";}
			mats.add(new Material(name,manufacturer,
					Double.parseDouble(_32Hz),
					Double.parseDouble(_65Hz),
					Double.parseDouble(_125Hz),
					Double.parseDouble(_250Hz),
					Double.parseDouble(_500Hz),
					Double.parseDouble(_1kHz),
					Double.parseDouble(_2kHz),
					Double.parseDouble(_4kHz),
					Double.parseDouble(_8kHz)));
			sc.readLine();
		}
		return mats;
	}
}
