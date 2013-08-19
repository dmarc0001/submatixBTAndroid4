/**
 * Klasse beinhaltet die Buildnummer und das Erstellungsdatum
 * 
 * BuildVersion.java de.dmarcini.netutils.dsl cmdLineSequenzialDslChecker
 * 
 * @author Dirk Marciniak 31.07.2012
 */
package de.dmarcini.submatix.android4.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author dmarc
 */
public class BuildVersion
{
  private static final long buildNumber = 2601L;
  private static final long buildDate = 1376945676548L;
  private static final String buildNumberString = String.format( "%d", buildNumber );
  private static final String buildDateString   = new Date( buildDate ).toString();

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
   * Gib das Builddatum als String zurück
   * 
   * @author Dirk Marciniak 31.07.2012
   * @return String
   */
  public static String getBuildDate()
  {
    return( buildDateString );
  }

  /**
   * 
   * Das Builddatum als lokalisiertes Format
   * 
   * Project: SubmatixBTForPC Package: de.dmarcini.submatix.pclogger.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 31.07.2012
   * @param fmt
   * @return Datum als String
   */
  public static String getLocaleDate( String fmt )
  {
    Date date = new Date( buildDate );
    SimpleDateFormat sdf = new SimpleDateFormat( fmt );
    return( sdf.format( date ) );
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
