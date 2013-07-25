package de.dmarcini.submatix.android4.utils;

import android.app.DialogFragment;

/**
 * 
 * Die aufrufende App muss das Interface implementieren
 * 
 * Project: Android_4_BlueThoothTest Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 02.11.2012
 */
public interface NoticeDialogListener
{
  public void onDialogPositiveClick( DialogFragment dialog );

  public void onDialogNegativeClick( DialogFragment dialog );
}

