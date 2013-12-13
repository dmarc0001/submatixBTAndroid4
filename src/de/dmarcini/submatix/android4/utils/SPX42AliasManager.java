package de.dmarcini.submatix.android4.utils;

import java.util.Vector;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;
import de.dmarcini.submatix.android4.ApplicationDEBUG;
import de.dmarcini.submatix.android4.exceptions.NoDatabaseException;

/**
 * 
 * Klasse kapselt den Zugriff auf Gerätealiase in der Datenbank
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class SPX42AliasManager
{
  private static final String TAG   = SPX42AliasManager.class.getSimpleName();
  protected SQLiteDatabase    dBase = null;

  /**
   * 
   * Konstruktor mit Übergabe einer geöffneten Datenbank
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 26.07.2013
   * 
   * @param db
   * @throws NoDatabaseException
   */
  public SPX42AliasManager( SQLiteDatabase db ) throws NoDatabaseException
  {
    if( db == null || !db.isOpen() )
    {
      throw new NoDatabaseException( "no database or dadabase is not open!" );
    }
    dBase = db;
  }

  /**
   * 
   * Datenbank schliessen, wenn geöffnet
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 26.07.2013
   */
  public void close()
  {
    if( dBase != null && dBase.isOpen() )
    {
      dBase.close();
    }
  }

  /**
   * 
   * Gib den Alias oder den Default zurück
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 26.07.2013
   * 
   * @param mac
   * @param defaultAlias
   * @return Den Alias des Gerätes zurück
   */
  public String getAliasForMac( String mac, String defaultAlias )
  {
    String sql, alias;
    Cursor cu;
    //
    if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "getAliasForMac..." );
    sql = String.format( "select %s from %s where %s like '%s';", ProjectConst.A_ALIAS, ProjectConst.A_TABLE_ALIASES, ProjectConst.A_MAC, mac );
    cu = dBase.rawQuery( sql, null );
    // formatter:on
    if( cu.moveToFirst() )
    {
      alias = cu.getString( 0 );
      //
      // Cursor schliessen
      //
      cu.close();
      if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "getAliasForMac: found <" + alias + ">" );
      return( alias );
    }
    if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "getAliasForMac: not found, use default <" + defaultAlias + ">" );
    return( defaultAlias );
  }

  /**
   * 
   * Gib eine Liste der Deviceids zurück
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 28.08.2013
   * 
   * @return Liste mit Deviceids
   */
  public Vector<Integer> getDeviceIdList()
  {
    String sql;
    Cursor cu;
    Vector<Integer> lst = new Vector<Integer>();
    //
    if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "getDeviceIdList..." );
    sql = String.format( "select %s from %s order by %s;", ProjectConst.A_DEVICEID, ProjectConst.A_TABLE_ALIASES, ProjectConst.A_DEVICEID );
    cu = dBase.rawQuery( sql, null );
    //
    try
    {
      if( cu.moveToFirst() )
      {
        lst.add( cu.getInt( 0 ) );
        while( cu.moveToNext() )
        {
          lst.add( cu.getInt( 0 ) );
        }
      }
      Log.d( TAG, "read <" + lst.size() + "> entrys..." );
      return( lst );
    }
    catch( SQLException ex )
    {
      Log.e( TAG, "Error while getDeviceIdList: <" + ex.getLocalizedMessage() + ">" );
      return( null );
    }
  }

  /**
   * 
   * Funktion liest eine Liste der gespeicherten Devices aus der Datenbank
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * Stand: 01.12.2013
   * 
   * @return Ein Objekt mit Daten über die Devices in der Datenbank
   */
  public Vector<Pair<Integer, String>> getDeviceNameIdList()
  {
    // Pair<Integer,String> = new Pair<Integer,String>(null, null);
    String sql;
    Cursor cu;
    Vector<Pair<Integer, String>> devList = new Vector<Pair<Integer, String>>();
    //
    if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "getDeviceNameIdList..." );
    sql = String.format( "select %s,%s from %s order by %s", ProjectConst.A_DEVICEID, ProjectConst.A_ALIAS, ProjectConst.A_TABLE_ALIASES, ProjectConst.A_DEVICEID );
    cu = dBase.rawQuery( sql, null );
    //
    try
    {
      if( cu.moveToFirst() )
      {
        devList.add( new Pair<Integer, String>( cu.getInt( 0 ), cu.getString( 1 ) ) );
        while( cu.moveToNext() )
        {
          devList.add( new Pair<Integer, String>( cu.getInt( 0 ), cu.getString( 1 ) ) );
        }
      }
      Log.d( TAG, "getDeviceNameIdList: read <" + devList.size() + "> entrys..." );
      return( devList );
    }
    catch( SQLException ex )
    {
      Log.e( TAG, "Error while getDeviceNameIdList: <" + ex.getLocalizedMessage() + ">" );
      return( null );
    }
  }

  /**
   * 
   * Giv die Deviceid zurück
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 28.08.2013
   * 
   * @param mac
   * @return Deviceid
   */
  public int getIdForDeviceFromMac( String mac )
  {
    String sql;
    int deviceId = -1;
    Cursor cu;
    //
    if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "getIdForDevice..." );
    sql = String.format( "select %s from %s where %s like '%s';", ProjectConst.A_DEVICEID, ProjectConst.A_TABLE_ALIASES, ProjectConst.A_MAC, mac );
    cu = dBase.rawQuery( sql, null );
    // formatter:on
    if( cu.moveToFirst() )
    {
      deviceId = cu.getInt( 0 );
      //
      // Cursor schliessen
      //
      cu.close();
      if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "getIdForDevice: found <" + deviceId + ">" );
      return( deviceId );
    }
    if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "getIdForDevice: not found, use default <-1>" );
    return( -1 );
  }

  /**
   * 
   * Gib die DB-Id für ein Gerät nach Seriennummer
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * Stand: 01.12.2013
   * 
   * @param serial
   * @return Datenbank-Id des gesuchten Gerätes
   */
  public int getIdForDeviceFromSerial( String serial )
  {
    String sql;
    int deviceId = -1;
    Cursor cu;
    //
    if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "getIdForDeviceFromSerial..." );
    sql = String.format( "select %s from %s where %s like '%s';", ProjectConst.A_DEVICEID, ProjectConst.A_TABLE_ALIASES, ProjectConst.A_SERIAL, serial );
    if( ApplicationDEBUG.DEBUG ) Log.e( TAG, "getIdForDeviceFromSerial: sql <" + sql + ">" );
    cu = dBase.rawQuery( sql, null );
    // formatter:on
    if( cu.moveToFirst() )
    {
      deviceId = cu.getInt( 0 );
      //
      // Cursor schliessen
      //
      cu.close();
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "getIdForDeviceFromSerial: found <" + deviceId + ">" );
      return( deviceId );
    }
    if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "getIdForDeviceFromSerial: not found, use default <-1>" );
    return( -1 );
  }

  /**
   * 
   * Erzeuge oder update einen Alias für ein Gerät
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 26.07.2013
   * 
   * @param mac
   * @param devName
   * @param alias
   * @param serial
   *          Kann null sein, wenn nicht bekannt, wird dann ignoriert
   * @return War das Setzen des Alias für Gerät erfolgreich?
   */
  public boolean setAliasForMac( String mac, String devName, String alias, String serial )
  {
    String sql;
    Cursor cu;
    ContentValues values;
    //
    if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "setAliasForMac..." );
    sql = String.format( "select %s from %s where %s like '%s';", ProjectConst.A_ALIAS, ProjectConst.A_TABLE_ALIASES, ProjectConst.A_MAC, mac );
    cu = dBase.rawQuery( sql, null );
    //
    try
    {
      if( cu.moveToFirst() )
      {
        //
        // ja, der alias existiert, updaten!
        //
        cu.close();
        // Vorbereiten eines ContentValues Objektes
        values = new ContentValues();
        values.put( ProjectConst.A_ALIAS, alias );
        values.put( ProjectConst.A_DEVNAME, devName );
        if( serial != null ) values.put( ProjectConst.A_SERIAL, serial );
        // Ausführen mit on-the-fly whereklausel
        return( 0 > dBase.update( ProjectConst.A_TABLE_ALIASES, values, String.format( "%s like '%s'", ProjectConst.A_MAC, mac ), null ) );
      }
      //
      // nein, das existiert noch nicht
      //
      values = new ContentValues();
      values.put( ProjectConst.A_MAC, mac );
      values.put( ProjectConst.A_DEVNAME, devName );
      values.put( ProjectConst.A_ALIAS, alias );
      if( serial != null ) values.put( ProjectConst.A_SERIAL, serial );
      return( -1 < dBase.insertOrThrow( ProjectConst.A_TABLE_ALIASES, null, values ) );
    }
    catch( SQLException ex )
    {
      Log.e( TAG, "Error while setAliasForMac: <" + ex.getLocalizedMessage() + ">" );
      return( false );
    }
  }

  /**
   * 
   * Erzeuge einen Eintrag in der Aliasdatenbank, wenn keiner vorhanden ist
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 28.08.2013
   * 
   * @param _mac
   * @param _deviceName
   */
  public void setAliasForMacIfNotExist( String _mac, String _deviceName )
  {
    String sql;
    Cursor cu;
    ContentValues values;
    //
    if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "setAliasForMacIfNotExist..." );
    sql = String.format( "select %s from %s where %s like '%s';", ProjectConst.A_ALIAS, ProjectConst.A_TABLE_ALIASES, ProjectConst.A_MAC, _mac );
    cu = dBase.rawQuery( sql, null );
    //
    try
    {
      if( cu.moveToFirst() )
      {
        //
        // ja, der alias existiert, erledigt!
        //
        cu.close();
        return;
      }
      //
      // nein, das existiert noch nicht
      //
      values = new ContentValues();
      values.put( ProjectConst.A_MAC, _mac );
      values.put( ProjectConst.A_DEVNAME, _deviceName );
      values.put( ProjectConst.A_ALIAS, _deviceName );
      dBase.insertOrThrow( ProjectConst.A_TABLE_ALIASES, null, values );
    }
    catch( SQLException ex )
    {
      Log.e( TAG, "Error while setAliasForMacIfNotExist: <" + ex.getLocalizedMessage() + ">" );
      return;
    }
  }

  /**
   * 
   * Setze die Seriennummer für ein Gerät, wenn die noch nicht geschehen ist
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * Stand: 03.12.2013
   * 
   * @param _mac
   * @param _serial
   */
  public void setSerialIfNotExist( String _mac, String _serial )
  {
    String sql;
    Cursor cu;
    ContentValues values;
    String whereString;
    //
    if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "setSerialIfNotExist..." );
    sql = String.format( "select %s from %s where %s like '%s';", ProjectConst.A_SERIAL, ProjectConst.A_TABLE_ALIASES, ProjectConst.A_MAC, _mac );
    cu = dBase.rawQuery( sql, null );
    //
    try
    {
      if( cu.moveToFirst() )
      {
        if( cu.isNull( 0 ) || cu.getString( 0 ).isEmpty() )
        {
          cu.close();
          //
          // nein, das existiert noch nicht
          //
          values = new ContentValues();
          values.put( ProjectConst.A_SERIAL, _serial );
          whereString = String.format( "%s='%s'", ProjectConst.A_MAC, _mac );
          dBase.update( ProjectConst.A_TABLE_ALIASES, values, whereString, null );
          return;
        }
      }
      cu.close();
    }
    catch( SQLException ex )
    {
      Log.e( TAG, "Error while setSerialIfNotExist: <" + ex.getLocalizedMessage() + ">" );
      return;
    }
  }
}
