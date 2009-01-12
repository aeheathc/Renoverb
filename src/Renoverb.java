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

import javax.swing.JTextField;

public class Renoverb
{
	public static final int[] FREQUENCIES = {32,65,125,250,500,1000,2000,4000,8000};
	
	public static void main(String[] args)
	{
		(new MainWindow()).setVisible(true);
	}
	
	public static double fieldToDouble(JTextField field)
	{
		try{return Double.parseDouble(field.getText());}
		catch(NumberFormatException e) {return 0;}
	}
}