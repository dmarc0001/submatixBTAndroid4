//@formatter:off
/*
    programm: SubmatixBTLoggerAndroid
    purpose:  configuration and read logs from SUBMATIX SPX42 divecomputer via Bluethooth    
    Copyright (C) 2012  Dirk Marciniak

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/
*/
//@formatter:on
package de.dmarcini.submatix.android4.full.utils;

/**
 * 
 * Klasse als Container für ein Gas-Update
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class GasUpdateEntity
{
  private int           gasNr    = 0;
  private SPX42GasParms gasParms = null;

  /**
   * 
   * Konstruktor mit Parametern
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * Stand: 03.12.2013
   * 
   * @param nr
   * @param parms
   */
  public GasUpdateEntity( int nr, SPX42GasParms parms )
  {
    gasNr = nr;
    gasParms = parms;
  }

  /**
   * @return gasNr
   */
  public int getGasNr()
  {
    return gasNr;
  }

  /**
   * @return gasParms
   */
  public SPX42GasParms getGasParms()
  {
    return gasParms;
  }
}
