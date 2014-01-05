package de.dmarcini.submatix.android4.full.utils;

import java.io.File;

/**
 * 
 * Objekt zum Speichern der Headerdaten eines Tauchgangs beim Auslesen eines Logs
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 13.08.2013
 */
@SuppressWarnings( "javadoc" )
public class SPX42DiveHeadData
{
  public int    diveId             = -1;
  public int    deviceId           = -1;
  public int    diveNumberOnSPX    = -1;
  public String fileNameOnSpx      = "";
  public File   xmlFile            = null;
  public String deviceSerialNumber = "";
  public long   startTime          = 0L;
  public double airTemp            = -1;
  public double lowestTemp         = -1;
  public int    maxDepth           = 0;
  public int    countSamples       = 0;
  public int    diveLength         = 0;
  public String units              = "m";
  public String longgitude         = "";
  public String latitude           = "";
  public String notes              = null;

  /**
   * 
   * Ist das die niedrigste Temperatur?
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * Stand: 03.12.2013
   * 
   * @param temp
   * @return geringste Temperatur
   * 
   */
  public double checkLowestTemp( final double temp )
  {
    // Beim ersten Mal ist DAS die tieste Temperatur!
    if( airTemp == -1 ) airTemp = temp;
    // ansonsten prüf das einfach immer wieder
    if( ( lowestTemp == -1 ) || ( temp < lowestTemp ) )
    {
      lowestTemp = temp;
    }
    return( lowestTemp );
  }

  /**
   * 
   * Ist das die Max Tiefe?
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * Stand: 03.12.2013
   * 
   * @param depth
   * @return
   */
  public double checkMaxDepth( final int depth )
  {
    // prüf das einfach immer wieder
    if( ( maxDepth == 0 ) || ( maxDepth < depth ) )
    {
      maxDepth = depth;
    }
    return( maxDepth );
  }
}
