/*
 VARNA is a tool for the automated drawing, visualization and annotation of the secondary structure of RNA, designed as a companion software for web servers and databases.
 Copyright (C) 2008  Kevin Darty, Alain Denise and Yann Ponty.
 electronic mail : Yann.Ponty@lri.fr
 paper mail : LRI, bat 490 Université Paris-Sud 91405 Orsay Cedex France

 This file is part of VARNA version 3.1.
 VARNA version 3.1 is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

 VARNA version 3.1 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with VARNA version 3.1.
 If not, see http://www.gnu.org/licenses.
 */
package fr.orsay.lri.varna.controlers;

import fr.orsay.lri.varna.VARNAPanel;

public class ControleurBlinkingThread extends Thread {
	public static final long DEFAULT_FREQUENCY = 50;
	private long _period;
	private VARNAPanel _parent;
	private double _minVal, _maxVal, _val, _incr;
	private boolean _increasing = true;
	private boolean _active = false;

	public ControleurBlinkingThread(VARNAPanel vp) {
		this(vp, DEFAULT_FREQUENCY, 0, 1.0, 0.0, 0.2);
	}

	public ControleurBlinkingThread(VARNAPanel vp, long period, double minVal,
			double maxVal, double val, double incr) {
		_parent = vp;
		_period = period;
		_minVal = minVal;
		_maxVal = maxVal;
		_incr = incr;
	}

	public void setActive(boolean b) {
		if (_active == b)
		{}
		else
		{
		_active = b;
		if (_active) {
			interrupt();
		}
		}
	}

	public boolean getActive() {
		return _active;
	}
	
	
	public double getVal() {
		return _val;
	}

	public void run() {
		while (true) {
			try {
				if (_active) {
					sleep(_period);
					if (_increasing) {
						_val = Math.min(_val + _incr, _maxVal);
						if (_val == _maxVal) {
							_increasing = false;
						}
					} else {
						_val = Math.max(_val - _incr, _minVal);
						if (_val == _minVal) {
							_increasing = true;
						}
					}
					_parent.repaint();
				} else {
					sleep(10000);
				}
			} catch (InterruptedException e) {
			}
		}
	}
}
