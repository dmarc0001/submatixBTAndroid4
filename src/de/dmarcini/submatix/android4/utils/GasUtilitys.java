package de.dmarcini.submatix.android4.utils;

import android.annotation.SuppressLint;

public class GasUtilitys
{
  /**
   * 
   * Gib den Namen des Gaases zurück
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.01.2013
   * @param o2
   * @param he
   * @return Gasname
   * 
   */
  @SuppressLint( "DefaultLocale" )
  public static String getNameForGas( final int o2, final int he )
  {
    //
    // Wieviel Stickstoff?
    //
    int n2 = 100 - o2 - he;
    //
    // Mal sondieren
    //
    if( n2 == 0 )
    {
      //
      // heliox oder O2
      //
      if( o2 == 100 )
      {
        return( "O2" );
      }
      // Es gibt Helium und O2.... == Heliox
      return( String.format( "HX%d/%d", o2, he ) );
    }
    if( he == 0 )
    {
      // eindeutig Nitrox
      if( o2 == 21 )
      {
        return( "AIR" );
      }
      return( String.format( "NX%02d", o2 ) );
    }
    else
    {
      // das ist dan wohl Trimix/Triox
      return( String.format( "TX%d/%d", o2, he ) );
    }
  }
}
