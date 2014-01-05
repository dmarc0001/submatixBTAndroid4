/**
 * Klasse beinhaltet die Buildnummer und das Erstellungsdatum
 * 
 * BuildVersion.java de.dmarcini.netutils.dsl cmdLineSequenzialDslChecker
 * 
 */
package de.dmarcini.submatix.android4.full.utils;

import java.util.Locale;

import org.joda.time.DateTime;

import de.dmarcini.submatix.android4.full.gui.FragmentCommonActivity;

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
  private static final long buildNumber = 3040L;
  private static final long buildDate = 1388942812266L;
  @SuppressWarnings( "javadoc" )
  public static final boolean isLightVersion = false;
  /**
   * Kennzeichnung für eine Light-Version
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
    return( new DateTime( buildDate ).toString( FragmentCommonActivity.localTimeFormatter ) );
  }

  /**
   * 
   * Version aus den Projektdefinitionen zurückgeben
   * 
   * Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.utils
   * 
   * 
   * Stand: 31.07.2012
   * 
   * @return Versionsstring
   */
  public static String getVersion()
  {
    if( isLightVersion )
    {
      return( ProjectConst.MANUFACTVERS_LT );
    }
    return( ProjectConst.MANUFACTVERS );
  }
}
