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

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.joda.time.DateTime;

import java.io.File;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import de.dmarcini.submatix.android4.full.ApplicationDEBUG;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.exceptions.NoDatabaseException;
import de.dmarcini.submatix.android4.full.gui.MainActivity;

/**
 * Kapselt Logeinträge und Aliase in der Datenbank
 * <p/>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p/>
 *         Stand: 10.11.2013
 */
public class SPX42LogManager extends SPX42AliasManager
{
  private static final String TAG = SPX42LogManager.class.getSimpleName();

  /**
   * Konstruktor
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * Stand: 18.11.2013
   *
   * @param db
   * @throws NoDatabaseException
   */
  public SPX42LogManager(SQLiteDatabase db) throws NoDatabaseException
  {
    super(db);
  }

  /**
   * Lösche ALLE Daten eines Gerätes, einschliesslich Alias
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.utils
   * <p/>
   * Stand: 21.01.2014
   *
   * @param deviceId
   * @return Erfolgreich?
   */
  public boolean deleteAllDataForDevice(int deviceId)
  {
    Vector<File> dataFiles;
    //
    // Punkt 1: gibt es das Gerät in der DB?
    //
    if( !existDeviceId(deviceId) )
    {
      return (false);
    }
    //
    // Punkt 2: Alle Dateien finden, die dem Gerät gehören
    //
    dataFiles = getDatafilesForDevice(deviceId);
    if( !dataFiles.isEmpty() )
    {
      //
      // entferne die Datendateien aus dem Verzeichnis
      //
      Iterator<File> it = dataFiles.iterator();
      while( it.hasNext() )
      {
        File toDelFile = it.next();
        if( ApplicationDEBUG.DEBUG )
        {
          Log.d(TAG, "deleteAllDataForDevice: File: <" + toDelFile.getName() + ">...");
        }
        toDelFile.delete();
      }
    }
    dataFiles.clear();
    dataFiles = null;
    //
    // Punkt 3: Daten in der Datentabelle löschen
    //
    deleteDivesForDevice(deviceId);
    //
    // Punkt 4: Alias löschen
    //
    deleteAlias(deviceId);
    return (true);
  }

  /**
   * Daten für einen Logeintrag (Tauchgang) löschen
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.utils
   * <p/>
   * Stand: 22.01.2014
   *
   * @param dbId
   * @return hat es geklappt?
   */
  public boolean deleteDataForDevice(int dbId)
  {
    //
    // Punkt 1: Datei finden, die dem Gerät und dem Tauchgang gehören und löschen
    //
    File dataFile = getDatafileForDbId(dbId);
    if( dataFile == null )
    {
      Log.e(TAG, "can't find the XML-file for dive!");
    }
    else
    {
      if( ApplicationDEBUG.DEBUG )
      {
        Log.d(TAG, "deleteDataForDevice: File: <" + dataFile.getName() + ">...");
      }
      dataFile.delete();
    }
    //
    // Punkt 2: Eintrag u sder Datenbank löschen
    //
    return (deleteOneDiveLog(dbId));
  }

  /**
   * Lösche alle Datern eines Gerätes aus der Datenbank
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.utils
   * <p/>
   * Stand: 21.01.2014
   *
   * @param deviceId
   */
  public void deleteDivesForDevice(int deviceId)
  {
    int count = 0;
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "deleteDivesForDevice...");
    }
    if( deviceId < 0 )
    {
      return;
    }
    count = dBase.delete(ProjectConst.H_TABLE_DIVELOGS, String.format("%s=%d", ProjectConst.H_DEVICEID, deviceId), null);
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "deleteDivesForDevice: <" + count + "> sets deleted: OK");
    }
    return;
  }

  /**
   * Lösche einen Logeintrag aus der Datenbank
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.utils
   * <p/>
   * Stand: 22.01.2014
   *
   * @param dbId
   * @return
   */
  private boolean deleteOneDiveLog(int dbId)
  {
    int count = 0;
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "deleteOneDiveLog...");
    }
    if( dbId < 0 )
    {
      return (false);
    }
    count = dBase.delete(ProjectConst.H_TABLE_DIVELOGS, String.format("%s=%d", ProjectConst.H_DIVEID, dbId), null);
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "deleteOneDiveLog: <" + count + "> sets deleted: OK");
    }
    return (true);
  }

  /**
   * Suche die XML-Datei für einen Logeintrag
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.utils
   * <p/>
   * Stand: 22.01.2014
   *
   * @param dbId
   * @return Die Datei oder null
   */
  private File getDatafileForDbId(int dbId)
  {
    String sql;
    Cursor cu;
    File   dataFile = null;
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "getDatafileForDbId...");
    }
    if( dbId < 0 )
    {
      return (dataFile);
    }
    // @formatter:off
    sql = String.format(Locale.ENGLISH, "select %s from %s where %s=%d;",
        ProjectConst.H_FILEONMOBILE,
        ProjectConst.H_TABLE_DIVELOGS,
        ProjectConst.H_DIVEID,
        dbId);
    // @formatter:on
    cu = dBase.rawQuery(sql, null);
    if( cu.moveToFirst() )
    {
      dataFile = new File(cu.getString(0));
    }
    //
    // Cursor schliessen
    //
    cu.close();
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "getDatafileForDbId: OK");
    }
    return (dataFile);
  }

  /**
   * Gib eine Liste mit den Datendateien zurück
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.utils
   * <p/>
   * Stand: 21.01.2014
   *
   * @param deviceId
   * @return Liste mit Datendateien
   */
  public Vector<File> getDatafilesForDevice(int deviceId)
  {
    String       sql;
    Cursor       cu;
    Vector<File> dataFiles = new Vector<File>();
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "getDatafilesForDevice...");
    }
    if( deviceId < 0 )
    {
      return (dataFiles);
    }
    // @formatter:off
    sql = String.format(Locale.ENGLISH, "select %s from %s where %s=%d;",
        ProjectConst.H_FILEONMOBILE,
        ProjectConst.H_TABLE_DIVELOGS,
        ProjectConst.H_DEVICEID,
        deviceId);
    // @formatter:on
    cu = dBase.rawQuery(sql, null);
    if( cu.moveToFirst() )
    {
      do
      {
        dataFiles.add(new File(cu.getString(0)));
      }
      while( cu.moveToNext() );
    }
    //
    // Cursor schliessen
    //
    cu.close();
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "getDatafilesForDevice: OK");
    }
    return (dataFiles);
  }

  /**
   * Gib die Tauch-Kopfdaten anhand der Seriennummer und des Filenamens zurück
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 15.08.2013
   *
   * @param devSerial
   * @param fileOnSPX
   * @return SPX42DiveHeadData Headerdaten zum Tauchgang
   */
  public SPX42DiveHeadData getDiveHeader(final String devSerial, final String fileOnSPX)
  {
    String            sql;
    Cursor            cu;
    SPX42DiveHeadData diveHeader = new SPX42DiveHeadData();
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "getDiveHeader...");
    }
    // @formatter:off
    sql = String.format("select %s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s from %s where %s like '%s' and %s like '%s';",
        ProjectConst.H_DIVEID,                     // 0
        ProjectConst.H_DEVICEID,                   // 1
        ProjectConst.H_FILEONMOBILE,               // 2
        ProjectConst.H_DIVENUMBERONSPX,            // 3
        ProjectConst.H_FILEONSPX,                  // 4
        ProjectConst.H_DEVICESERIAL,               // 5
        ProjectConst.H_STARTTIME,                  // 6
        ProjectConst.H_HADSEND,                    // 7
        ProjectConst.H_FIRSTTEMP,                  // 8
        ProjectConst.H_LOWTEMP,                    // 9
        ProjectConst.H_MAXDEPTH,                   // 10
        ProjectConst.H_SAMPLES,                    // 11
        ProjectConst.H_DIVELENGTH,                 // 12
        ProjectConst.H_UNITS,                      // 13
        ProjectConst.H_NOTES,                      // 14
        ProjectConst.H_GEO_LON,                    // 15
        ProjectConst.H_GEO_LAT,                    // 16
        ProjectConst.H_TABLE_DIVELOGS,             // Tabelle
        ProjectConst.H_DEVICESERIAL, devSerial,    // seriennummer
        ProjectConst.H_FILEONSPX, fileOnSPX);     // Dateiname
    // @formatter:on
    cu = dBase.rawQuery(sql, null);
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "getDiveHeader had <" + cu.getCount() + "> results.");
    }
    if( cu.moveToFirst() )
    {
      diveHeader.diveId = cu.getInt(0);
      diveHeader.deviceId = cu.getInt(1);
      diveHeader.xmlFile = new File(cu.getString(2));
      diveHeader.diveNumberOnSPX = cu.getInt(3);
      diveHeader.fileNameOnSpx = cu.getString(4);
      diveHeader.deviceSerialNumber = cu.getString(5);
      diveHeader.startTime = cu.getLong(6);
      diveHeader.airTemp = cu.getDouble(8);
      diveHeader.lowestTemp = cu.getDouble(9);
      diveHeader.maxDepth = cu.getInt(10);
      diveHeader.countSamples = cu.getInt(11);
      diveHeader.diveLength = cu.getInt(12);
      diveHeader.units = cu.getString(13);
      diveHeader.notes = cu.getString(14);
      diveHeader.longgitude = cu.getString(15);
      diveHeader.latitude = cu.getString(16);
      //
      // Cursor schliessen
      //
      cu.close();
      if( ApplicationDEBUG.DEBUG )
      {
        Log.d(TAG, "getDiveHeader: OK");
      }
      return (diveHeader);
    }
    cu.close();
    return (null);
  }

  /**
   * Gib eine Liste mit Logs für ein Gerät zurück
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 28.08.2013
   *
   * @param _deviceId
   * @param res
   * @param descOrder Absteigende Sortierung?
   * @return Vector mit ReadLogItemObj
   */
  @SuppressLint( "DefaultLocale" )
  public Vector<ReadLogItemObj> getDiveListForDevice(int _deviceId, Resources res, boolean descOrder)
  {
    String                 sql;
    Cursor                 cu;
    String                 orderPhrase  = " ";
    Vector<ReadLogItemObj> diveHeadList = new Vector<ReadLogItemObj>();
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "getDiveListForDevice...");
    }
    if( _deviceId < 0 )
    {
      Vector<Integer> lst = getDeviceIdList();
      if( lst != null && lst.size() > 0 )
      {
        _deviceId = lst.get(0);
      }
      else
      {
        return (null);
      }
    }
    if( descOrder )
    {
      orderPhrase = "desc";
    }
    // @formatter:off
    sql = String.format(Locale.ENGLISH, "select %s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s from %s where %s=%d order by %s %s;",
        ProjectConst.H_DIVEID,                     // 0
        ProjectConst.H_FILEONMOBILE,               // 1
        ProjectConst.H_DIVENUMBERONSPX,            // 2
        ProjectConst.H_FILEONSPX,                  // 3
        ProjectConst.H_DEVICESERIAL,               // 4
        ProjectConst.H_STARTTIME,                  // 5
        ProjectConst.H_HADSEND,                    // 6
        ProjectConst.H_FIRSTTEMP,                  // 7
        ProjectConst.H_LOWTEMP,                    // 8
        ProjectConst.H_MAXDEPTH,                   // 9
        ProjectConst.H_SAMPLES,                    // 10
        ProjectConst.H_DIVELENGTH,                 // 11
        ProjectConst.H_UNITS,                      // 12
        ProjectConst.H_NOTES,                      // 13
        ProjectConst.H_GEO_LON,                    // 14
        ProjectConst.H_GEO_LAT,                    // 15
        ProjectConst.H_TABLE_DIVELOGS,             // Tabelle
        ProjectConst.H_DEVICEID, _deviceId,        // nur Gerätenummer
        ProjectConst.H_DIVENUMBERONSPX,            // Ordne nach Tauchlog-Nummer auf SPX
        orderPhrase);
    // @formatter:on
    cu = dBase.rawQuery(sql, null);
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "getDiveListForDevice had <" + cu.getCount() + "> results.");
    }
    if( cu.moveToFirst() )
    {
      do
      {
        long     startTm       = cu.getLong(5);
        DateTime startDateTime = new DateTime(startTm);
        String detailText = String.format(res.getString(R.string.logread_saved_format), cu.getInt(9) / 10.0, res.getString(R.string.app_unit_depth_metric), cu.getInt(11) / 60, cu.getInt(11) % 60);
        String         itemName = String.format("#%03d: %s", cu.getInt(2), startDateTime.toString(MainActivity.localTimeFormatter));
        ReadLogItemObj rlo      = new ReadLogItemObj();
        rlo.isSaved = true;
        rlo.itemName = itemName;
        rlo.itemNameOnSPX = cu.getString(3);
        rlo.itemDetail = detailText;
        rlo.dbId = cu.getInt(0);
        rlo.numberOnSPX = cu.getInt(2);
        rlo.startTimeMilis = startTm;
        rlo.isMarked = false;
        rlo.tagId = -1;
        rlo.fileOnMobile = cu.getString(1);
        rlo.firstTemp = cu.getFloat(7);
        rlo.lowTemp = cu.getFloat(8);
        rlo.maxDepth = cu.getInt(9);
        rlo.countSamples = cu.getInt(10);
        rlo.diveLen = cu.getInt(11);
        rlo.units = cu.getString(12);
        rlo.notes = cu.getString(13);
        rlo.geoLon = cu.getString(14);
        rlo.geoLat = cu.getString(15);
        if( rlo.notes == null || rlo.notes.isEmpty() )
        {
          rlo.notes = " ";
        }
        diveHeadList.add(rlo);
      }
      while( cu.moveToNext() );
      //
      // Cursor schliessen
      //
      cu.close();
      if( ApplicationDEBUG.DEBUG )
      {
        Log.d(TAG, "getDiveListForDevice: OK");
      }
      return (diveHeadList);
    }
    cu.close();
    return (null);
  }

  /**
   * Gib ein Headerobjekt für einene bestimmten Tauchgang zurück
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.utils
   * <p/>
   * Stand: 01.02.2014
   *
   * @param dbId
   * @param res
   * @return Headerobjekt
   */
  public ReadLogItemObj getLogObjForDbId(int dbId, Resources res)
  {
    String         sql;
    Cursor         cu;
    ReadLogItemObj rlo = new ReadLogItemObj();
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "getLogObjForDbId...");
    }
    if( dbId < 1 )
    {
      return (null);
    }
    // @formatter:off
    sql = String.format(Locale.ENGLISH, "select %s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s from %s where %s=%d",
        ProjectConst.H_DIVEID,                     // 0
        ProjectConst.H_FILEONMOBILE,               // 1
        ProjectConst.H_DIVENUMBERONSPX,            // 2
        ProjectConst.H_FILEONSPX,                  // 3
        ProjectConst.H_DEVICESERIAL,               // 4
        ProjectConst.H_STARTTIME,                  // 5
        ProjectConst.H_HADSEND,                    // 6
        ProjectConst.H_FIRSTTEMP,                  // 7
        ProjectConst.H_LOWTEMP,                    // 8
        ProjectConst.H_MAXDEPTH,                   // 9
        ProjectConst.H_SAMPLES,                    // 10
        ProjectConst.H_DIVELENGTH,                 // 11
        ProjectConst.H_UNITS,                      // 12
        ProjectConst.H_NOTES,                      // 13
        ProjectConst.H_GEO_LON,                    // 14
        ProjectConst.H_GEO_LAT,                    // 15
        ProjectConst.H_TABLE_DIVELOGS,             // Tabelle
        ProjectConst.H_DIVEID, dbId               // für Dive-ID
    );
    // @formatter:on
    cu = dBase.rawQuery(sql, null);
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "getLogObjForDbId had <" + cu.getCount() + "> results.");
    }
    if( cu.moveToFirst() )
    {
      long     startTm       = cu.getLong(5);
      DateTime startDateTime = new DateTime(startTm);
      String detailText = String.format(res.getString(R.string.logread_saved_format), cu.getInt(9) / 10.0, res.getString(R.string.app_unit_depth_metric), cu.getInt(11) / 60, cu.getInt(11) % 60);
      String itemName = String.format(Locale.ENGLISH, "#%03d: %s", cu.getInt(2), startDateTime.toString(MainActivity.localTimeFormatter));
      rlo.isSaved = true;
      rlo.itemName = itemName;
      rlo.itemNameOnSPX = cu.getString(3);
      rlo.itemDetail = detailText;
      rlo.dbId = cu.getInt(0);
      rlo.numberOnSPX = cu.getInt(2);
      rlo.startTimeMilis = startTm;
      rlo.isMarked = false;
      rlo.tagId = -1;
      rlo.fileOnMobile = cu.getString(1);
      rlo.firstTemp = cu.getFloat(7);
      rlo.lowTemp = cu.getFloat(8);
      rlo.maxDepth = cu.getInt(9);
      rlo.countSamples = cu.getInt(10);
      rlo.diveLen = cu.getInt(11);
      rlo.units = cu.getString(12);
      rlo.notes = cu.getString(13);
      rlo.geoLon = cu.getString(14);
      rlo.geoLat = cu.getString(15);
      if( rlo.notes == null || rlo.notes.isEmpty() )
      {
        rlo.notes = " ";
      }
      //
      // Cursor schliessen
      //
      cu.close();
      if( ApplicationDEBUG.DEBUG )
      {
        Log.d(TAG, "getLogObjForDbId: OK");
      }
      return (rlo);
    }
    cu.close();
    return (null);
  }

  /**
   * Ist dieses Protokoll schon in der Datenbank enthalten?
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 09.08.2013
   *
   * @param devSerial
   * @param fileOnSPX
   * @return Schon da oder nicht
   */
  public boolean isLogInDatabase(final String devSerial, final String fileOnSPX)
  {
    String sql;
    int    count = 0;
    Cursor cu;
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "isLogInDatabase...");
    }
    sql = String.format(Locale.ENGLISH, "select count(*) from %s where %s like '%s' and %s like '%s';", ProjectConst.H_TABLE_DIVELOGS, ProjectConst.H_DEVICESERIAL, devSerial, ProjectConst.H_FILEONSPX, fileOnSPX);
    cu = dBase.rawQuery(sql, null);
    // formatter:on
    if( cu.moveToFirst() )
    {
      count = cu.getInt(0);
      //
      // Cursor schliessen
      //
      if( ApplicationDEBUG.DEBUG )
      {
        Log.d(TAG, "isLogInDatabase: found <" + count + ">");
      }
    }
    cu.close();
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "isLogInDatabase... datasets is <" + count + ">");
    }
    return ((count == 0) ? false : true);
  }

  /**
   * Den Tauchgang nach erfolgter Erstellung der XML-Datei in die DB eintragen
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 13.08.2013
   *
   * @param diveHeader
   * @return Hat geklappt oder nicht
   */
  public boolean saveDive(SPX42DiveHeadData diveHeader)
  {
    ContentValues values;
    //
    // nein, das existiert noch nicht
    //
    values = new ContentValues();
    values.put(ProjectConst.H_FILEONMOBILE, diveHeader.xmlFile.getAbsolutePath());
    values.put(ProjectConst.H_DEVICEID, diveHeader.deviceId);
    values.put(ProjectConst.H_DIVENUMBERONSPX, diveHeader.diveNumberOnSPX);
    values.put(ProjectConst.H_DEVICESERIAL, diveHeader.deviceSerialNumber);
    values.put(ProjectConst.H_STARTTIME, diveHeader.startTime);
    values.put(ProjectConst.H_FILEONSPX, diveHeader.fileNameOnSpx);
    values.put(ProjectConst.H_LOWTEMP, diveHeader.lowestTemp);
    values.put(ProjectConst.H_MAXDEPTH, diveHeader.maxDepth);
    values.put(ProjectConst.H_SAMPLES, diveHeader.countSamples);
    values.put(ProjectConst.H_UNITS, diveHeader.units);
    values.put(ProjectConst.H_GEO_LON, diveHeader.longgitude);
    values.put(ProjectConst.H_GEO_LAT, diveHeader.latitude);
    values.put(ProjectConst.H_FIRSTTEMP, diveHeader.airTemp);
    values.put(ProjectConst.H_DIVELENGTH, diveHeader.diveLength);
    values.put(ProjectConst.H_HADSEND, 0);
    return (-1 < dBase.insertOrThrow(ProjectConst.H_TABLE_DIVELOGS, null, values));
  }
}
