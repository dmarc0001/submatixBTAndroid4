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
/**
 * Klasse beinhaltet die Buildnummer und das Erstellungsdatum
 * 
 * BuildVersion.java de.dmarcini.netutils.dsl cmdLineSequenzialDslChecker
 * 
 */
package de.dmarcini.submatix.android4.full.utils;

import java.util.Locale;

import org.joda.time.DateTime;

import de.dmarcini.submatix.android4.full.gui.MainActivity;

/**
 * 
 * Klasse, die die Build-Version und das Erstellungsdatum darstellt (im Buildprozess per Script aktualisiert)
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class BuildVersion
{
  private static final long buildNumber = 3652L;
  private static final long buildDate = 1416402499318L;
  /**
   * Kennzeichnung für eine Version
   */
  private static final String buildNumberString = String.format( Locale.ENGLISH, "%d", buildNumber );

  /**
   * Gib die Buildnummer zurück
   * 
   * @return long
   */
  public static long getBuild()
  {
    return( buildNumber );
  }

  /**
   * 
   * Gib den Build als String zurück
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * Stand: 03.12.2013
   * 
   * @return Buildstring
   */
  public static String getBuildAsString()
  {
    return( buildNumberString );
  }

  /**
   * 
   * Gib die konfigurierte Zeitdarstellung zurück
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 22.08.2013
   * 
   * @return Zeitstring nach den Programmeinstellungen
   */
  public static String getdefaukltDateString()
  {
    return( new DateTime( buildDate ).toString( MainActivity.localTimeFormatter ) );
  }

  /**
   * 
   * Version aus den Projektdefinitionen zurückgeben
   * 
   * Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.utils
   * 
   * 
   * Stand: 26.02.2014
   * 
   * @return Versionsstring
   */
  public static String getVersion()
  {
    return( ProjectConst.MANUFACTVERS );
  }
}
