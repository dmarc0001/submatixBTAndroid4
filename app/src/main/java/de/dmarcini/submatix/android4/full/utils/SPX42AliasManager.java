//@formatter:off
/*
    programm: SubmatixBTLoggerAndroid
    purpose:  configuration and read logs from SUBMATIX SPX42 divecomputer via Bluethooth    
    Copyright (C) 2012  Dirk Marciniak

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/
*/
//@formatter:on
package de.dmarcini.submatix.android4.full.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;

import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import de.dmarcini.submatix.android4.full.ApplicationDEBUG;
import de.dmarcini.submatix.android4.full.exceptions.NoDatabaseException;

/**
 * Klasse kapselt den Zugriff auf Gerätealiase in der Datenbank
 * <p/>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p/>
 *         Stand: 10.11.2013
 */
public class SPX42AliasManager
{
  private static final String TAG = SPX42AliasManager.class.getSimpleName();
  protected SQLiteDatabase dBase = null;

  /**
   * Konstruktor mit Übergabe einer geöffneten Datenbank
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 26.07.2013
   *
   * @param _db
   * @throws NoDatabaseException
   */
  public SPX42AliasManager(SQLiteDatabase _db) throws NoDatabaseException
  {
    if( _db == null || !_db.isOpen() )
    {
      throw new NoDatabaseException("no database or dadabase is not open!");
    }
    dBase = _db;
  }

  /**
   * Datenbank schliessen, wenn geöffnet
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
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
   * Lösche einen Aliaseintrag für ein Gerät
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.utils
   * <p/>
   * Stand: 21.01.2014
   *
   * @param _deviceId
   */
  public void deleteAlias(int _deviceId)
  {
    int count = 0;
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "deleteAlias...");
    }
    if( _deviceId < 0 )
    {
      return;
    }
    count = dBase.delete(ProjectConst.A_TABLE_ALIASES, String.format("%s=%d", ProjectConst.A_DEVICEID, _deviceId), null);
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "deleteAlias: <" + count + "> aliases deleted: OK");
    }
    return;
  }

  /**
   * Existiert ein Device mit der ID
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.utils
   * <p/>
   * Stand: 21.01.2014
   *
   * @param _deviceId
   * @return Existiert das Gerät in der Datenbank?
   */
  public boolean existDeviceId(int _deviceId)
  {
    String sql;
    Cursor cu;
    boolean retVal = false;
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.i(TAG, "getDeviceIdList...");
    }
    sql = String.format(Locale.ENGLISH, "select count(*) from %s where %s=%d;", ProjectConst.A_TABLE_ALIASES, ProjectConst.A_DEVICEID, _deviceId);
    cu = dBase.rawQuery(sql, null);
    //
    try
    {
      if( cu.moveToFirst() )
      {
        if( cu.getInt(0) > 0 )
        {
          retVal = true;
        }
      }
      cu.close();
      Log.d(TAG, "exist Devive <" + _deviceId + "> : " + retVal);
      return (retVal);
    }
    catch( SQLException ex )
    {
      Log.e(TAG, "Error while existDeviceId: <" + ex.getLocalizedMessage() + ">");
      cu.close();
      return (false);
    }
  }

  /**
   * Gib den Gerätelias für eine GeräteID zurück
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.utils
   * <p/>
   * Stand: 10.01.2014
   *
   * @param _deviceId
   * @return Alias für Geräteid
   */
  public String getAliasForId(int _deviceId)
  {
    String sql;
    Cursor cu;
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "getAliasForId...");
    }
    if( _deviceId < 0 )
    {
      return (null);
    }
    // @formatter:off
    sql = String.format(Locale.ENGLISH, "select %s from %s where %s=%d;",
        ProjectConst.A_ALIAS,
        ProjectConst.A_TABLE_ALIASES,
        ProjectConst.A_DEVICEID,
        _deviceId);
    // @formatter:on
    cu = dBase.rawQuery(sql, null);
    if( cu.moveToFirst() )
    {
      sql = cu.getString(0);
    }
    else
    {
      sql = null;
    }
    //
    // Cursor schliessen
    //
    cu.close();
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "getAliasForId: OK");
    }
    return (sql);
  }

  /**
   * Gib die PIN aus der DB zurück, falls vorhanden
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.utils
   * <p/>
   * Stand: 15.11.2014
   *
   * @param _deviceId
   * @return PIN oder null
   */
  public String getPINForId(int _deviceId)
  {
    String sql;
    String pin = null;
    Cursor cu;
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "getPINForId...");
    }
    if( _deviceId < 0 )
    {
      return (null);
    }
    // @formatter:off
    sql = String.format(Locale.ENGLISH, "select %s from %s where %s=%d;",
        ProjectConst.A_PIN,
        ProjectConst.A_TABLE_ALIASES,
        ProjectConst.A_DEVICEID,
        _deviceId);
    // @formatter:on
    cu = dBase.rawQuery(sql, null);
    if( cu.moveToFirst() )
    {
      try
      {
        pin = cu.getString(0);
      }
      finally
      {
        cu.close();
      }
    }
    else
    {
      cu.close();
      pin = null;
    }
    //
    // Cursor schliessen
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "getPINForId: OK");
    }
    return (pin);
  }

  /**
   * Gib den Alias oder den Default zurück
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 26.07.2013
   *
   * @param _mac
   * @param _defaultAlias
   * @return Den Alias des Gerätes zurück
   */
  public String getAliasForMac(String _mac, String _defaultAlias)
  {
    String sql, alias;
    Cursor cu;
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.i(TAG, "getAliasForMac...");
    }
    sql = String.format("select %s from %s where %s like '%s';", ProjectConst.A_ALIAS, ProjectConst.A_TABLE_ALIASES, ProjectConst.A_MAC, _mac);
    cu = dBase.rawQuery(sql, null);
    // formatter:on
    if( cu.moveToFirst() )
    {
      alias = cu.getString(0);
      //
      // Cursor schliessen
      //
      cu.close();
      if( ApplicationDEBUG.DEBUG )
      {
        Log.i(TAG, "getAliasForMac: found <" + alias + ">");
      }
      return (alias);
    }
    cu.close();
    if( ApplicationDEBUG.DEBUG )
    {
      Log.i(TAG, "getAliasForMac: not found, use default <" + _defaultAlias + ">");
    }
    return (_defaultAlias);
  }

  /**
   * Gib die PIN für einen BT MAC zurück
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.utils
   * <p/>
   * Stand: 15.11.2014
   *
   * @param _mac
   * @return PIN oder null
   */
  public String getPINForMac(String _mac)
  {
    String sql;
    String pin = null;
    Cursor cu;
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.i(TAG, "getPINForMac...");
    }
    sql = String.format("select %s from %s where %s like '%s';", ProjectConst.A_PIN, ProjectConst.A_TABLE_ALIASES, ProjectConst.A_MAC, _mac);
    cu = dBase.rawQuery(sql, null);
    // formatter:on
    if( cu.moveToFirst() )
    {
      try
      {
        pin = cu.getString(0);
      }
      finally
      {
        cu.close();
      }
      //
      // Cursor schliessen
      //
      cu.close();
      if( ApplicationDEBUG.DEBUG )
      {
        Log.i(TAG, "getPINForMac: found <" + pin + ">");
      }
      return (pin);
    }
    cu.close();
    if( ApplicationDEBUG.DEBUG )
    {
      Log.i(TAG, "getPINForMac: not found");
    }
    return (null);
  }

  /**
   * Gib eine Liste der Deviceids zurück
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 21.01.2014
   *
   * @return Liste mit Deviceids
   */
  public Vector<Integer> getDeviceIdList()
  {
    String sql;
    Cursor cu;
    Vector<Integer> lst = new Vector<Integer>();
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.i(TAG, "getDeviceIdList...");
    }
    sql = String.format("select %s from %s order by %s;", ProjectConst.A_DEVICEID, ProjectConst.A_TABLE_ALIASES, ProjectConst.A_DEVICEID);
    cu = dBase.rawQuery(sql, null);
    //
    try
    {
      if( cu.moveToFirst() )
      {
        do
        {
          lst.add(cu.getInt(0));
        }
        while( cu.moveToNext() );
      }
      cu.close();
      Log.d(TAG, "read <" + lst.size() + "> entrys...");
      return (lst);
    }
    catch( SQLException ex )
    {
      Log.e(TAG, "Error while getDeviceIdList: <" + ex.getLocalizedMessage() + ">");
      cu.close();
      return (null);
    }
  }

  /**
   * Funktion liest eine Liste der gespeicherten Devices aus der Datenbank
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
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
    if( ApplicationDEBUG.DEBUG )
    {
      Log.i(TAG, "getDeviceNameIdList...");
    }
    sql = String.format("select %s,%s from %s order by %s", ProjectConst.A_DEVICEID, ProjectConst.A_ALIAS, ProjectConst.A_TABLE_ALIASES, ProjectConst.A_DEVICEID);
    cu = dBase.rawQuery(sql, null);
    //
    try
    {
      if( cu.moveToFirst() )
      {
        do
        {
          devList.add(new Pair<Integer, String>(cu.getInt(0), cu.getString(1)));
        }
        while( cu.moveToNext() );
      }
      Log.d(TAG, "getDeviceNameIdList: read <" + devList.size() + "> entrys...");
      cu.close();
      return (devList);
    }
    catch( SQLException ex )
    {
      cu.close();
      Log.e(TAG, "Error while getDeviceNameIdList: <" + ex.getLocalizedMessage() + ">");
      return (null);
    }
  }

  /**
   * Gib eine Liste mit Beschreibungen für alle gespeicherten Geräte zurück
   * <p/>
   * Project: SubmatixBTAndroid4 Package: de.dmarcini.submatix.android4.full.utils
   * <p/>
   * Stand: 24.11.2014
   *
   * @return Vector mit Parametern aller gespeicherten Geräte
   */
  public Vector<HashMap<String, String>> getDeviceAdressesList()
  {
    String sql;
    Cursor cu;
    Vector<HashMap<String, String>> devLists = new Vector<HashMap<String, String>>();
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.i(TAG, "getDeviceAdressesList...");
    }
    sql = String.format("select %s,%s,%s from %s order by %s", ProjectConst.A_MAC, ProjectConst.A_DEVNAME, ProjectConst.A_ALIAS, ProjectConst.A_TABLE_ALIASES,
        ProjectConst.A_DEVICEID);
    cu = dBase.rawQuery(sql, null);
    //
    try
    {
      if( cu.moveToFirst() )
      {
        do
        {
          HashMap<String, String> props = new HashMap<String, String>();
          props.put(ProjectConst.A_MAC, cu.getString(0));
          props.put(ProjectConst.A_DEVNAME, cu.getString(1));
          props.put(ProjectConst.A_ALIAS, cu.getString(2));
          devLists.add(props);
        }
        while( cu.moveToNext() );
      }
      Log.d(TAG, "getDeviceAdressesList: read <" + devLists.size() + "> entrys...");
      cu.close();
      return (devLists);
    }
    catch( SQLException ex )
    {
      cu.close();
      Log.e(TAG, "Error while getDeviceAdressesList: <" + ex.getLocalizedMessage() + ">");
      return (null);
    }
  }

  /**
   * Giv die Deviceid zurück
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 28.08.2013
   *
   * @param _mac
   * @return Deviceid
   */
  public int getIdForDeviceFromMac(String _mac)
  {
    String sql;
    int deviceId = -1;
    Cursor cu;
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.i(TAG, "getIdForDevice...");
    }
    sql = String.format("select %s from %s where %s like '%s';", ProjectConst.A_DEVICEID, ProjectConst.A_TABLE_ALIASES, ProjectConst.A_MAC, _mac);
    cu = dBase.rawQuery(sql, null);
    // formatter:on
    if( cu.moveToFirst() )
    {
      deviceId = cu.getInt(0);
      //
      // Cursor schliessen
      //
      cu.close();
      if( ApplicationDEBUG.DEBUG )
      {
        Log.i(TAG, "getIdForDevice: found <" + deviceId + ">");
      }
      return (deviceId);
    }
    cu.close();
    if( ApplicationDEBUG.DEBUG )
    {
      Log.i(TAG, "getIdForDevice: not found, use default <-1>");
    }
    return (-1);
  }

  /**
   * Gib die DB-Id für ein Gerät nach Seriennummer
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * Stand: 01.12.2013
   *
   * @param _serial
   * @return Datenbank-Id des gesuchten Gerätes
   */
  public int getIdForDeviceFromSerial(String _serial)
  {
    String sql;
    int deviceId = -1;
    Cursor cu;
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.i(TAG, "getIdForDeviceFromSerial...");
    }
    sql = String.format("select %s from %s where %s like '%s';", ProjectConst.A_DEVICEID, ProjectConst.A_TABLE_ALIASES, ProjectConst.A_SERIAL, _serial);
    // if( ApplicationDEBUG.DEBUG ) Log.e( TAG, "getIdForDeviceFromSerial: sql <" + sql + ">" );
    cu = dBase.rawQuery(sql, null);
    // formatter:on
    if( cu.moveToFirst() )
    {
      deviceId = cu.getInt(0);
      //
      // Cursor schliessen
      //
      cu.close();
      if( ApplicationDEBUG.DEBUG )
      {
        Log.d(TAG, "getIdForDeviceFromSerial: found <" + deviceId + ">");
      }
      return (deviceId);
    }
    cu.close();
    if( ApplicationDEBUG.DEBUG )
    {
      Log.i(TAG, "getIdForDeviceFromSerial: not found, use default <-1>");
    }
    return (-1);
  }

  /**
   * Erzeuge oder update einen Alias für ein Gerät
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 26.07.2013
   *
   * @param _mac
   * @param _devName
   * @param _alias
   * @param _serial  Kann null sein, wenn nicht bekannt, wird dann ignoriert
   * @return War das Setzen des Alias für Gerät erfolgreich?
   */
  public boolean setAliasForMac(String _mac, String _devName, String _alias, String _serial)
  {
    String sql;
    Cursor cu;
    ContentValues values;
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.i(TAG, "setAliasForMac...");
    }
    sql = String.format("select %s from %s where %s like '%s';", ProjectConst.A_ALIAS, ProjectConst.A_TABLE_ALIASES, ProjectConst.A_MAC, _mac);
    cu = dBase.rawQuery(sql, null);
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
        values.put(ProjectConst.A_ALIAS, _alias);
        values.put(ProjectConst.A_DEVNAME, _devName);
        if( _serial != null )
        {
          values.put(ProjectConst.A_SERIAL, _serial);
        }
        // Ausführen mit on-the-fly whereklausel
        if( 0 < dBase.update(ProjectConst.A_TABLE_ALIASES, values, String.format("%s like '%s'", ProjectConst.A_MAC, _mac), null) )
        {
          return (true);
        }
        return (false);
      }
      //
      // nein, das existiert noch nicht
      //
      values = new ContentValues();
      values.put(ProjectConst.A_MAC, _mac);
      values.put(ProjectConst.A_DEVNAME, _devName);
      values.put(ProjectConst.A_ALIAS, _alias);
      if( _serial != null )
      {
        values.put(ProjectConst.A_SERIAL, _serial);
      }
      cu.close();
      if( -1 == dBase.insertOrThrow(ProjectConst.A_TABLE_ALIASES, null, values) )
      {
        return (false);
      }
      return (true);
    }
    catch( SQLException ex )
    {
      cu.close();
      Log.e(TAG, "Error while setAliasForMac: <" + ex.getLocalizedMessage() + ">");
      return (false);
    }
  }

  /**
   * Erzeuge einen Eintrag in der Aliasdatenbank, wenn keiner vorhanden ist
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 28.08.2013
   *
   * @param _mac
   * @param _deviceName
   */
  public void setAliasForMacIfNotExist(String _mac, String _deviceName)
  {
    String sql;
    Cursor cu;
    ContentValues values;
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.i(TAG, "setAliasForMacIfNotExist...");
    }
    sql = String.format("select %s from %s where %s like '%s';", ProjectConst.A_ALIAS, ProjectConst.A_TABLE_ALIASES, ProjectConst.A_MAC, _mac);
    cu = dBase.rawQuery(sql, null);
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
      cu.close();
      //
      // nein, das existiert noch nicht
      //
      values = new ContentValues();
      values.put(ProjectConst.A_MAC, _mac);
      values.put(ProjectConst.A_DEVNAME, _deviceName);
      values.put(ProjectConst.A_ALIAS, _deviceName);
      dBase.insertOrThrow(ProjectConst.A_TABLE_ALIASES, null, values);
    }
    catch( SQLException ex )
    {
      Log.e(TAG, "Error while setAliasForMacIfNotExist: <" + ex.getLocalizedMessage() + ">");
      cu.close();
      return;
    }
  }

  /**
   * Setze die Seriennummer für ein Gerät, wenn die noch nicht geschehen ist
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * Stand: 03.12.2013
   *
   * @param _mac
   * @param _serial
   */
  public void setSerialIfNotExist(String _mac, String _serial)
  {
    String sql;
    Cursor cu;
    ContentValues values;
    String whereString;
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.i(TAG, "setSerialIfNotExist...");
    }
    sql = String.format("select %s from %s where %s like '%s';", ProjectConst.A_SERIAL, ProjectConst.A_TABLE_ALIASES, ProjectConst.A_MAC, _mac);
    cu = dBase.rawQuery(sql, null);
    //
    try
    {
      if( cu.moveToFirst() )
      {
        if( cu.isNull(0) || cu.getString(0).isEmpty() )
        {
          cu.close();
          //
          // nein, das existiert noch nicht
          //
          values = new ContentValues();
          values.put(ProjectConst.A_SERIAL, _serial);
          whereString = String.format("%s='%s'", ProjectConst.A_MAC, _mac);
          dBase.update(ProjectConst.A_TABLE_ALIASES, values, whereString, null);
          return;
        }
      }
      cu.close();
    }
    catch( SQLException ex )
    {
      cu.close();
      Log.e(TAG, "Error while setSerialIfNotExist: <" + ex.getLocalizedMessage() + ">");
      return;
    }
  }

  /**
   * PIN für eine MAC setzen
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.utils
   * <p/>
   * Stand: 15.11.2014
   *
   * @param _mac
   * @param _pin
   */
  public void setPinForMac(String _mac, String _pin)
  {
    ContentValues values;
    String whereString;
    //
    try
    {
      if( ApplicationDEBUG.DEBUG )
      {
        Log.i(TAG, "setPinForMac...");
      }
      values = new ContentValues();
      values.put(ProjectConst.A_PIN, _pin);
      whereString = String.format("%s='%s'", ProjectConst.A_MAC, _mac);
      dBase.update(ProjectConst.A_TABLE_ALIASES, values, whereString, null);
      return;
    }
    catch( SQLException ex )
    {
      Log.e(TAG, "Error while setPinForMac: <" + ex.getLocalizedMessage() + ">");
      return;
    }
  }
}
