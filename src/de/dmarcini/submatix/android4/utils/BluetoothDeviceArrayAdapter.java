package de.dmarcini.submatix.android4.utils;

// Eigener Adapter für Geräte
// Es werden String gespeichert, Felder werden durch "\n" getrennt
// Feld 0 = Geräte alias / gerätename
// Feld 1 = Geräte-MAC
// Feld 2 = Geräte-Name
// Feld 3 = Datenbank-Id (wenn vorhanden) sonst 0
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
  private static final String       TAG            = BluetoothDeviceArrayAdapter.class.getSimpleName();
  private final ArrayList<String[]> theList        = new ArrayList<String[]>();
  public static final int           BT_DEVAR_ALIAS = 0;
  public static final int           BT_DEVAR_MAC   = 1;
  public static final int           BT_DEVAR_NAME  = 2;
  public static final int           BT_DEVAR_DBID  = 3;

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

  private BluetoothDeviceArrayAdapter( Context context, int resource, int textViewResourceId )
  {
    super( context, resource, textViewResourceId );
  }

  private BluetoothDeviceArrayAdapter( Context context, int textViewResourceId, String[] objects )
  {
    super( context, textViewResourceId, objects );
  }

  private BluetoothDeviceArrayAdapter( Context context, int resource, int textViewResourceId, String[] objects )
  {
    super( context, resource, textViewResourceId, objects );
  }

  private BluetoothDeviceArrayAdapter( Context context, int textViewResourceId, List<String> objects )
  {
    super( context, textViewResourceId, objects );
  }

  private BluetoothDeviceArrayAdapter( Context context, int resource, int textViewResourceId, List<String> objects )
  {
    super( context, resource, textViewResourceId, objects );
  }

  @Override
  public void add( String item )
  {
    String[] items = item.split( "\n" );
    //
    // guck nach, ob schon MAC Addr feld[1] vorhanden
    //
    addEntr( items );
  }

  /**
   * 
   * Das Stringarray in die Liste!
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 17.02.2013
   * @param items
   */
  public void addEntr( String[] items )
  {
    if( !isMacThere( items ) )
    {
      theList.add( items );
      // super.add( items[0] );
    }
  }

  /**
   * 
   * Ist die MAC-Adresse schon in der Liste?
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 17.02.2013
   * @param item
   * @return
   */
  private boolean isMacThere( String[] item )
  {
    // int count = super.getCount();
    int count = theList.size();
    for( int i = 0; i < count; i++ )
    {
      if( item[BT_DEVAR_MAC].equals( theList.get( i )[BT_DEVAR_MAC] ) )
      {
        return( true );
      }
    }
    return false;
  }

  @Override
  public String getItem( int position )
  {
    //
    // Ziel ist, daß nur der Name in der Liste angezeigt wird
    //
    // return( super.getItem( position ) );
    // Log.i( TAG, "getItem(" + position + ")" );
    return( theList.get( position )[0] );
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
    String[] fields = theList.get( position );
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
   * @return den String
   */
  public String getAlias( int position )
  {
    return( getStringAt( position, BT_DEVAR_ALIAS ) );
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
   * @return den String
   */
  public String getMAC( int position )
  {
    return( getStringAt( position, BT_DEVAR_MAC ) );
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
   * @return den String
   */
  public String getDevName( int position )
  {
    return( getStringAt( position, BT_DEVAR_NAME ) );
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
   * @return den String
   */
  public String getDbIdStr( int position )
  {
    return( getStringAt( position, BT_DEVAR_DBID ) );
  }

  /**
   * 
   * gib die DatenbankID zurück (als INT)
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 17.02.2013
   * @param position
   * @return dbId
   */
  public int getDbIdNum( int position )
  {
    String dbIdStr = getStringAt( position, BT_DEVAR_DBID );
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

  @Override
  public void addAll( Collection<? extends String> collection )
  {
    Log.e( TAG, "addAll(Collection<? extends String> collection) not implemented yet." );
  }

  @Override
  public int getCount()
  {
    return( theList.size() );
  }

  @Override
  public void clear()
  {
    theList.clear();
    super.clear();
  }
}
