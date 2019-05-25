//@formatter:off
/*
    programm: SubmatixBTLoggerAimport java.util.Locale;
iguration and read logs from SUBMATIX SPX42 divecomputer via Bluethooth    
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

import java.util.Locale;

/**
 * Helferklasse speichert einen Log-Eintrag fuer UDDF beim Scannen der XMl-Dateien
 * <p/>
 * Project: SubmatixBTLogger Package: de.dmarcini.bluethooth.support
 *
 * @author Dirk Marciniak (dirk_marcinia@arcor.de)
 * <p/>
 * Stand: 13.12.2011
 */
@SuppressWarnings( "javadoc" )
//@formatter:off
public class SPX42LogEntryObj
{
  public int presure = 0;
  public double depth = -1000;
  public double temp = -1000;
  public double acku = -1000;
  public double ppo2 = -1000;
  public double setpoint = -1000;
  public double n2 = -1;
  public double he = -1;
  public double o2 = -1;
  public double ar = 0;
  public int zerotime = 999;
  public int time = 0;
  public String gasSample = null;
  public boolean gasswitch = false;
  public boolean ppo2switch = false;
  //@formatter:on

  /**
   * Alles zurücksetzen
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * Stand: 03.12.2013
   */
  public void clean()
  {
    presure = 0;
    depth = - 1000;
    temp = - 1000;
    acku = - 1000;
    ppo2 = - 1000;
    setpoint = - 1000;
    n2 = - 1;
    he = - 1;
    o2 = - 1;
    ar = 0;
    zerotime = 999;
    time = 0;
    gasswitch = false;
  }

  /**
   * Sind alle Daten vollständig?
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * Stand: 03.12.2013
   *
   * @return
   */
  public boolean whereAlDataThere()
  {
    if( presure == 0 )
    {
      return ( false );
    }
    else if( depth == - 1000 )
    {
      return ( false );
    }
    else if( temp == - 1000 )
    {
      return ( false );
    }
    else if( acku == - 1000 )
    {
      return ( false );
    }
    else if( ppo2 == - 1000 )
    {
      return ( false );
    }
    else if( setpoint == - 1000 )
    {
      return ( false );
    }
    else if( n2 == - 1 )
    {
      return ( false );
    }
    else if( he == - 1 )
    {
      return ( false );
    }
    o2 = ( 1 - n2 - he );
    if( o2 < 0 )
    {
      return ( false );
    }
    gasSample = String.format( Locale.ENGLISH, "%.3f:%.3f:%.3f:%.3f:%.3f", o2, n2, he, 0.0, 0.0 );
    return ( true );
  }
}
