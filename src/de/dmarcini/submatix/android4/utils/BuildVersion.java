/**
 * Klasse beinhaltet die Buildnummer und das Erstellungsdatum
 * 
 * BuildVersion.java de.dmarcini.netutils.dsl cmdLineSequenzialDslChecker
 * 
 * @author Dirk Marciniak 31.07.2012
 */
package de.dmarcini.submatix.android4.utils;

import java.util.Locale;

import org.joda.time.DateTime;

import de.dmarcini.submatix.android4.gui.FragmentCommonActivity;

/**
 * @author dmarc
 */
public class BuildVersion
{
  private static final long buildNumber = 2709L;
  private static final long buildDate = 1384091570729L;
  private static final String buildNumberString = String.format( Locale.ENGLISH, "%d", buildNumber );

  /**
   * Gib die Buildnummer zurück
   * 
   * @author Dirk Marciniak 31.07.2012
   * @return long
   */
  public static long getBuild()
  {
    return( buildNumber );
  }

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
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 22.08.2013
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
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 31.07.2012
   * @return Versionsstring
   */
  public static String getVersion()
  {
    return( ProjectConst.MANUFACTVERS );
  }
}
