package de.dmarcini.submatix.android4.full.utils;

import android.os.Bundle;

/**
 * ein Proghramminterner Stackeintrag über Aufrufe der Fragmente
 *
 * Project: SubmatixBTAndroid4 Package: de.dmarcini.submatix.android4.full.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *
 *         Stand: 24.11.2014
 */
public class FragmentCallStackEntry
{
  private int    itemNumber = -1;
  private Bundle itemBundle = null;

  @SuppressWarnings( "unused" )
  private FragmentCallStackEntry()
  {};

  /**
   * Der Kosntruktor mit den Inhalten
   *
   * Project: SubmatixBTAndroid4 Package: de.dmarcini.submatix.android4.full.utils
   * 
   * Stand: 24.11.2014
   * 
   * @param itemNumber
   * @param paramBundle
   */
  public FragmentCallStackEntry( int itemNumber, Bundle paramBundle )
  {
    this.itemNumber = itemNumber;
    this.itemBundle = paramBundle;
  }

  /**
   * Gib die ID des Programmpunktes zurück
   *
   * Project: SubmatixBTAndroid4 Package: de.dmarcini.submatix.android4.full.utils
   * 
   * Stand: 24.11.2014
   * 
   * @return Programmpunkt-ID
   */
  public int getId()
  {
    return( itemNumber );
  }

  /**
   * Gib die Argumente (Bundle) des Eintrages zurück
   *
   * Project: SubmatixBTAndroid4 Package: de.dmarcini.submatix.android4.full.utils
   * 
   * Stand: 24.11.2014
   * 
   * @return eine argumentesammlung (Bundle)
   */
  public Bundle getBundle()
  {
    return( itemBundle );
  }
}
