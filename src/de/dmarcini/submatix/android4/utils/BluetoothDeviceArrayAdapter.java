package de.dmarcini.submatix.android4.utils;

// Eigener Adapter für Geräte
// Es werden String gespeichert, Felder werden durch "\n" getrennt
// Feld 0 = Geräte alias / gerätename
// Feld 1 = Geräte-MAC
// Feld 2 = Geräte-Name
// Feld 3 = Datenbank-Id (wenn vorhanden) sonst 0
import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

/**
 * 
 * Meine spezielle Klasse für Anzeige der BT Geräte
 * 
 * Gespeichert werden Strings, durch Trennzeichen "\n" sind Felder voneinander getrennt
 * 
 * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 05.11.2012
 */
public class BluetoothDeviceArrayAdapter extends ArrayAdapter<String>
{
  private static final String TAG = BluetoothDeviceArrayAdapter.class.getSimpleName();

  /**
   * 
   * Mein Konstruktor
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 05.11.2012
   * @param context
   * @param textViewResourceId
   */
  public BluetoothDeviceArrayAdapter( Context context, int textViewResourceId )
  {
    super( context, textViewResourceId );
  }

  @Override
  public String getItem( int position )
  {
    return( getStringAt( position, 0 ) );
  }

  /**
   * 
   * Die universelle Funktion zur Rückgabe einiger Teilstrings
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 05.11.2012
   * @param position
   * @param index
   * @return
   */
  private String getStringAt( int position, int index )
  {
    String[] fields = super.getItem( position ).split( "(?mu)[\\r\\n]+" );
    if( fields.length >= index )
    {
      return( fields[index] );
    }
    return( null );
  }

  /**
   * 
   * Gib den Aliasnamen des Eintrages zurück
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 05.11.2012
   * @param position
   * @return
   */
  public String getAlias( int position )
  {
    return( getStringAt( position, 0 ) );
  }

  /**
   * 
   * Gib die MAC des BT-Devices als Sting zurück
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 05.11.2012
   * @param position
   * @return
   */
  public String getMAC( int position )
  {
    return( getStringAt( position, 1 ) );
  }

  /**
   * 
   * Gib den vom Gerät zurückgegebenen Namen
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 05.11.2012
   * @param position
   * @return
   */
  public String getDevName( int position )
  {
    return( getStringAt( position, 2 ) );
  }

  /**
   * 
   * Gib die DatenbankId des Eintrages zurück
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 05.11.2012
   * @param position
   * @return
   */
  public String getDbIdStr( int position )
  {
    return( getStringAt( position, 3 ) );
  }

  public int getDbIdNum( int position )
  {
    String dbIdStr = getStringAt( position, 3 );
    int dbId;
    //
    if( dbIdStr != null )
    {
      try
      {
        dbId = Integer.parseInt( dbIdStr );
      }
      catch( NumberFormatException ex )
      {
        Log.e( TAG, "can't NumberString (" + dbIdStr + ") convert to int " + ex.getMessage() );
        dbId = 0;
      }
      return( dbId );
    }
    return( 0 );
  }
}
