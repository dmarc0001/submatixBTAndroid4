package de.dmarcini.submatix.android4.full.utils;

import java.io.File;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import de.dmarcini.submatix.android4.full.ApplicationDEBUG;
import de.dmarcini.submatix.android4.full.exceptions.NoDatabaseException;

/**
 * 
 * Kapselt Logeinträge und Aliase in der Datenbank
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class SPX42LogManager extends SPX42AliasManager
{
  private static final String TAG = SPX42LogManager.class.getSimpleName();

  /**
   * 
   * Konstriuktor
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * Stand: 18.11.2013
   * 
   * @param db
   * @throws NoDatabaseException
   */
  public SPX42LogManager( SQLiteDatabase db ) throws NoDatabaseException
  {
    super( db );
  }

  /**
   * 
   * Ist dieses Protokoll schon in der Datenbank enthalten?
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 09.08.2013
   * 
   * @param devSerial
   * @param fileOnSPX
   * @return Schon da oder nicht
   */
  public boolean isLogInDatabase( final String devSerial, final String fileOnSPX )
  {
    String sql;
    int count = 0;
    Cursor cu;
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "isLogInDatabase..." );
    sql = String.format( "select count(*) from %s where %s like '%s' and %s like '%s';", ProjectConst.H_TABLE_DIVELOGS, ProjectConst.H_DEVICESERIAL, devSerial,
            ProjectConst.H_FILEONSPX, fileOnSPX );
    cu = dBase.rawQuery( sql, null );
    // formatter:on
    if( cu.moveToFirst() )
    {
      count = cu.getInt( 0 );
      //
      // Cursor schliessen
      //
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "isLogInDatabase: found <" + count + ">" );
    }
    cu.close();
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "isLogInDatabase... datasets is <" + count + ">" );
    return( ( count == 0 ) ? false : true );
  }

  /**
   * 
   * Den Tauchgang nach erfolgter Erstellung der XML-Datei in die DB eintragen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 13.08.2013
   * 
   * @param diveHeader
   * @return Hat geklappt oder nicht
   */
  public boolean saveDive( SPX42DiveHeadData diveHeader )
  {
    ContentValues values;
    //
    // nein, das existiert noch nicht
    //
    values = new ContentValues();
    values.put( ProjectConst.H_FILEONMOBILE, diveHeader.xmlFile.getAbsolutePath() );
    values.put( ProjectConst.H_DEVICEID, diveHeader.deviceId );
    values.put( ProjectConst.H_DIVENUMBERONSPX, String.format( "%d", diveHeader.diveNumberOnSPX ) );
    values.put( ProjectConst.H_DEVICESERIAL, diveHeader.deviceSerialNumber );
    values.put( ProjectConst.H_STARTTIME, diveHeader.startTime ); // TODO: prüfe, ob das so hinkommt
    values.put( ProjectConst.H_FILEONSPX, diveHeader.fileNameOnSpx );
    values.put( ProjectConst.H_LOWTEMP, diveHeader.lowestTemp );
    values.put( ProjectConst.H_MAXDEPTH, diveHeader.maxDepth );
    values.put( ProjectConst.H_SAMPLES, diveHeader.countSamples );
    values.put( ProjectConst.H_UNITS, diveHeader.units );
    values.put( ProjectConst.H_GEO_LON, diveHeader.longgitude );
    values.put( ProjectConst.H_GEO_LAT, diveHeader.latitude );
    values.put( ProjectConst.H_FIRSTTEMP, diveHeader.airTemp );
    values.put( ProjectConst.H_DIVELENGTH, diveHeader.diveLength );
    values.put( ProjectConst.H_HADSEND, 0 );
    return( -1 < dBase.insertOrThrow( ProjectConst.H_TABLE_DIVELOGS, null, values ) );
  }

  /**
   * 
   * Gib die Tauch-Kopfdaten anhand der Seriennummer und des Filenamens zurück
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 15.08.2013
   * 
   * @param devSerial
   * @param fileOnSPX
   * @return SPX42DiveHeadData Headerdaten zum Tauchgang
   */
  public SPX42DiveHeadData getDiveHeader( final String devSerial, final String fileOnSPX )
  {
    String sql;
    Cursor cu;
    SPX42DiveHeadData diveHeader = new SPX42DiveHeadData();
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "getDiveHeader..." );
    // @formatter:off
    sql = String.format( "select %s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s from %s where %s like '%s' and %s like '%s';", 
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
            ProjectConst.H_FILEONSPX, fileOnSPX );     // Dateiname
    // @formatter:on
    cu = dBase.rawQuery( sql, null );
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "getDiveHeader had <" + cu.getCount() + "> results." );
    if( cu.moveToFirst() )
    {
      diveHeader.diveId = cu.getInt( 0 );
      diveHeader.deviceId = cu.getInt( 1 );
      diveHeader.xmlFile = new File( cu.getString( 2 ) );
      diveHeader.diveNumberOnSPX = cu.getInt( 3 );
      diveHeader.fileNameOnSpx = cu.getString( 4 );
      diveHeader.deviceSerialNumber = cu.getString( 5 );
      diveHeader.startTime = cu.getLong( 6 );
      diveHeader.airTemp = cu.getDouble( 8 );
      diveHeader.lowestTemp = cu.getDouble( 9 );
      diveHeader.maxDepth = cu.getInt( 10 );
      diveHeader.countSamples = cu.getInt( 11 );
      diveHeader.diveLength = cu.getInt( 12 );
      diveHeader.units = cu.getString( 13 );
      diveHeader.notes = cu.getString( 14 );
      diveHeader.longgitude = cu.getString( 15 );
      diveHeader.latitude = cu.getString( 16 );
      //
      // Cursor schliessen
      //
      cu.close();
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "getDiveHeader: OK" );
      return( diveHeader );
    }
    return( null );
  }

  /**
   * 
   * Gib eine Liste mit Logs für ein Gerät zurück
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 28.08.2013
   * 
   * @param _deviceId
   * @return Vector mit Werten tauchid,Startzeit,Länge,spx-Nummer,max-Tiefe
   */
  @SuppressLint( "DefaultLocale" )
  public Vector<Long[]> getDiveListForDevice( int _deviceId )
  {
    String sql;
    Cursor cu;
    Vector<Long[]> diveHeadList = new Vector<Long[]>();
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "getDiveListForDevice..." );
    if( _deviceId < 0 )
    {
      Vector<Integer> lst = getDeviceIdList();
      if( lst != null && lst.size() > 0 )
      {
        _deviceId = lst.get( 0 );
      }
      else
      {
        return( null );
      }
    }
    // @formatter:off
    sql = String.format( "select %s,%s,%s,%s,%s from %s where %s=%d order by %s;", 
            ProjectConst.H_DIVEID,                     // 
            ProjectConst.H_STARTTIME,                  // 
            ProjectConst.H_DIVELENGTH,                 // 
            ProjectConst.H_DIVENUMBERONSPX,
            ProjectConst.H_MAXDEPTH,
            ProjectConst.H_TABLE_DIVELOGS,             // Tabelle
            ProjectConst.H_DEVICEID, _deviceId,        // nur Gerätenummer
            ProjectConst.H_DIVEID);                    // Ordne nach Tauchlog-Nummer
    // @formatter:on
    cu = dBase.rawQuery( sql, null );
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "getDiveListForDevice had <" + cu.getCount() + "> results." );
    if( cu.moveToFirst() )
    {
      while( cu.moveToNext() )
      {
        Long[] entry = new Long[]
        { cu.getLong( 0 ), cu.getLong( 1 ), cu.getLong( 2 ), cu.getLong( 3 ), cu.getLong( 4 ) };
        diveHeadList.add( entry );
      }
      //
      // Cursor schliessen
      //
      cu.close();
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "getDiveListForDevice: OK" );
      return( diveHeadList );
    }
    return( null );
  }
}
