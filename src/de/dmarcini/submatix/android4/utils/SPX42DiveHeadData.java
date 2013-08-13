package de.dmarcini.submatix.android4.utils;

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
public class SPX42DiveHeadData
{
  public int    diveNumberOnSPX    = -1;
  public String fileNameOnSpx      = "";
  public File   xmlFile            = null;
  public String deviceSerialNumber = "";
  public long   startTime          = 0L;
  public double airTemp            = -1;
  public double lowestTemp         = -1;
  public double maxDepth           = 0;
  public int    countSamples       = 0;
  public int    diveLength         = 0;
  public String units              = "m";
  public String longgitude         = "";
  public String latitude           = "";

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

  public double checkMaxDepth( final double depth )
  {
    // prüf das einfach immer wieder
    if( ( maxDepth == 0 ) || ( maxDepth < depth ) )
    {
      maxDepth = depth;
    }
    return( maxDepth );
  }
}
