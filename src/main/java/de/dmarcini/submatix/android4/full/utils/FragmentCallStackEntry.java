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

import android.os.Bundle;

/**
 * ein Proghramminterner Stackeintrag über Aufrufe der Fragmente
 * <p/>
 * Project: SubmatixBTAndroid4 Package: de.dmarcini.submatix.android4.full.utils
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p/>
 *         Stand: 24.11.2014
 */
public class FragmentCallStackEntry
{
  private int    itemNumber = -1;
  private Bundle itemBundle = null;

  @SuppressWarnings( "unused" )
  private FragmentCallStackEntry()
  {
  }

  ;

  /**
   * Der Kosntruktor mit den Inhalten
   * <p/>
   * Project: SubmatixBTAndroid4 Package: de.dmarcini.submatix.android4.full.utils
   * <p/>
   * Stand: 24.11.2014
   *
   * @param itemNumber
   * @param paramBundle
   */
  public FragmentCallStackEntry(int itemNumber, Bundle paramBundle)
  {
    this.itemNumber = itemNumber;
    this.itemBundle = paramBundle;
  }

  /**
   * Gib die ID des Programmpunktes zurück
   * <p/>
   * Project: SubmatixBTAndroid4 Package: de.dmarcini.submatix.android4.full.utils
   * <p/>
   * Stand: 24.11.2014
   *
   * @return Programmpunkt-ID
   */
  public int getId()
  {
    return (itemNumber);
  }

  /**
   * Gib die Argumente (Bundle) des Eintrages zurück
   * <p/>
   * Project: SubmatixBTAndroid4 Package: de.dmarcini.submatix.android4.full.utils
   * <p/>
   * Stand: 24.11.2014
   *
   * @return eine argumentesammlung (Bundle)
   */
  public Bundle getBundle()
  {
    return (itemBundle);
  }
}
