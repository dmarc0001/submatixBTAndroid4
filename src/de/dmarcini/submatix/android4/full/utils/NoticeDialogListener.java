package de.dmarcini.submatix.android4.full.utils;

import android.app.DialogFragment;

/**
 * 
 * Die aufrufende App muss das Interface implementieren
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 02.11.2012
 */
public interface NoticeDialogListener
{
  @SuppressWarnings( "javadoc" )
  public void onDialogPositiveClick( DialogFragment dialog );

  @SuppressWarnings( "javadoc" )
  public void onDialogNegativeClick( DialogFragment dialog );
}
