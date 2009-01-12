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

import java.util.Vector;

public class Calculator {
	private static final double coefficient = .049;
	//the number of frequencies used for modeling a room
	private static final int numberOfFrequencies = 9;
	//modifiers for desired RT values. Begins with 32Hz up 8kHz.
	private static final double[] desiredModifiers = {1.44,1.32,1.2,1.08,1,1,1,1,1};
	
	/**
	 * Calculates the absorption coefficient of a room at a certain frequency.
	 * @param volume The volume of the room.
	 * @param surfaceArea The surface area of the room.
	 * @param measuredRT The response time of the frequency in the room.
	 * @return The absorption coefficient of the room.
	 */
	public static double absorptionCoefficient(double volume, double surfaceArea, double measuredRT) {
		return ((coefficient*volume)/(surfaceArea*measuredRT));
	}
	
	/**
	 * Calculates and returns the required absorption for a room based on the desired response time.
	 * @param volume The volume of the room.
	 * @param desiredRT The desired response time.
	 * @return The required absorption.
	 */
	public static double requiredAbsorption(double volume, double desiredRT) {
		return (coefficient*volume)/desiredRT;
	}
	
	/**
	 * 
	 * @param absorptionCoefficient
	 * @param materialSurfaceArea
	 * @return
	 */
	public static double materialAbsoprtion(double absorptionCoefficient, double materialSurfaceArea) {
		return absorptionCoefficient*materialSurfaceArea;
	}
	
	/**
	 * 
	 * @param desiredRT
	 * @return
	 */
	public static double[] desiredRTCurve(double desiredRT) {
		double[] curve = new double[desiredModifiers.length	];
		
		for (int i = 0; i < curve.length; i++) {
			curve[i] = desiredRT*desiredModifiers[i];
		}
		return curve;
	}
	
	/**
	 * Calculates and returns the expected RT values of a room based on the existing RT and the absorption that new material(s) will add. 
	 * @param room_volume The volume of the room.
	 * @param room_surfaceArea The surface area of the room.
	 * @param measured_RTs The measured RT values at each of the frequencies.
	 * @param material_absorption_coefficients A Vector containing arrays that hold each material's absorption coefficients.
	 * @param material_surfaceArea The surface of each material being added.
	 * @return An array containing the calculated RT values of the room with the given material(s) added.
	 */
	public static double[] newRTcurve(double room_volume, double room_surfaceArea, double[] measured_RTs, Vector<double[]> material_absorption_coefficients, Vector<Double> material_surfaceArea) { 
		double[] results = new double[numberOfFrequencies];
		
		//for each frequency find the new RT value
		for (int f = 0; f < results.length; f++) {
			double existing_absorption = room_surfaceArea*(absorptionCoefficient(room_volume, room_surfaceArea, measured_RTs[f]));
//			double existing_absorption = room_surfaceArea*((coefficient*room_volume)/(room_surfaceArea*measured_RTs[f]));
			double material_absorption = 0;
			
			//for each material find the total absorption at the current frequency
			for(int m=0;m<material_absorption_coefficients.size();m++) {
				material_absorption += materialAbsoprtion(material_surfaceArea.elementAt(m), material_absorption_coefficients.elementAt(m)[f]);
//				material_absorption += material_surfaceArea.elementAt(m)*material_absorption_coefficients.elementAt(m)[f];
			}
			
			results[f] = (coefficient*room_volume)/(existing_absorption*material_absorption);
			if (results[f] == Double.POSITIVE_INFINITY || results[f] == Double.NEGATIVE_INFINITY) {
				results[f] = measured_RTs[f];
			}
		}
		
		
		return results;
	}
}
