package de.dmarcini.submatix.android4.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import android.os.Environment;
import android.util.Log;

public class ExtSdCardFinder
{
  private static final String TAG = ExtSdCardFinder.class.getSimpleName();

  /**
   * 
   * finde eine externe SD-Karte
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 25.07.2013
   * @return
   * @throws FileNotFoundException
   */
  public static File findSdCard() throws FileNotFoundException
  {
    File extSdCard;
    //
    // scanne das proc-System nach Anzeichen für eine SD-Card
    // nach Artikel c't 23/12, S. 172
    //
    Scanner scanner = new Scanner( new File( "/proc/mounts" ) );
    while( scanner.hasNext() )
    {
      // hole eine Zeile
      String strZeile = scanner.nextLine();
      // ist der Eintrag "vold"
      if( strZeile.startsWith( "/dev/block/vold/" ) )
      {
        // Teile das Ergebnis
        String[] strSplit = strZeile.split( " " );
        // ist in der Zeile das Dateisystem vfat?
        if( strSplit[2].contains( "vfat" ) )
        {
          // ist das gemountete Verzeichnis da und beschreibbar?
          extSdCard = new File( strSplit[1] );
          if( extSdCard.exists() && extSdCard.isDirectory() && extSdCard.canWrite() )
          {
            Log.i( TAG, "1st extern medium is on path <" + extSdCard.getAbsolutePath() + ">" );
            return( extSdCard );
          }
        }
      }
    }
    //
    // finde ich nicht.....
    //
    return( null );
  }

  /**
   * 
   * finde eine externe SD-Karte oder den als extern gekennzeichneten Speicher
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 25.07.2013
   * @return
   * @throws FileNotFoundException
   */
  public static File findExternStorage() throws FileNotFoundException
  {
    File extSdCard;
    //
    extSdCard = findSdCard();
    if( extSdCard != null )
    {
      return( extSdCard );
    }
    // nach: https://gist.github.com/MobileTuts/4546296
    // Status des externen Speichers
    String status = Environment.getExternalStorageState();
    Log.i( TAG, "extern storage status: " + status );
    // Medium ist eingelegt und gemountet
    if( Environment.MEDIA_MOUNTED.equals( status ) )
    {
      Log.i( TAG, "medium inserted and mounted" );
      // Sd-Card
      extSdCard = Environment.getExternalStorageDirectory();
      Log.i( TAG, "path sd-card: " + extSdCard.getAbsolutePath() + " media is removable: " + Environment.isExternalStorageRemovable() );
      return( extSdCard );
    }
    //
    // Medium wurde entfernt
    //
    else if( Environment.MEDIA_REMOVED.equals( status ) )
    {
      Log.w( TAG, "medium removed" );
      return( null );
    }
    //
    // Medium ist eingelegt kann aber vom System nicht genutzt werden (nicht gemountet)
    //
    else if( Environment.MEDIA_UNMOUNTED.equals( status ) )
    {
      Log.w( TAG, "medium inserted but not mounted" );
      return( null );
    }
    return( null );
  }
}