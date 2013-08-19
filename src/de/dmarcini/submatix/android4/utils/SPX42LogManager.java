package de.dmarcini.submatix.android4.utils;

import java.io.File;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import de.dmarcini.submatix.android4.BuildConfig;
import de.dmarcini.submatix.android4.exceptions.NoDatabaseException;

public class SPX42LogManager extends SPX42AliasManager
{
  private static final String TAG = SPX42LogManager.class.getSimpleName();

  //
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
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 09.08.2013
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
    if( BuildConfig.DEBUG ) Log.d( TAG, "isLogInDatabase..." );
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
      if( BuildConfig.DEBUG ) Log.d( TAG, "isLogInDatabase: found <" + count + ">" );
    }
    cu.close();
    if( BuildConfig.DEBUG ) Log.d( TAG, "isLogInDatabase... datasets is <" + count + ">" );
    return( ( count == 0 ) ? false : true );
  }

  /**
   * 
   * Den Tauchgang nach erfolgter Erstellung der XML-Datei in die DB eintragen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 13.08.2013
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
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 15.08.2013
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
    if( BuildConfig.DEBUG ) Log.d( TAG, "getDiveHeader..." );
    // @formatter:off
    sql = String.format( "select %s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s from %s where %s like '%s' and %s like '%s';", 
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
            ProjectConst.H_DEVICESERIAL, devSerial,    // seriennummer
            ProjectConst.H_FILEONSPX, fileOnSPX );     // Dateiname
    // @formatter:on
    cu = dBase.rawQuery( sql, null );
    if( BuildConfig.DEBUG ) Log.d( TAG, "getDiveHeader had <" + cu.getCount() + "> results." );
    if( cu.moveToFirst() )
    {
      diveHeader.diveId = cu.getInt( 0 );
      diveHeader.xmlFile = new File( cu.getString( 1 ) );
      diveHeader.diveNumberOnSPX = cu.getInt( 2 );
      diveHeader.fileNameOnSpx = cu.getString( 3 );
      diveHeader.deviceSerialNumber = cu.getString( 4 );
      diveHeader.startTime = cu.getLong( 5 );
      diveHeader.airTemp = cu.getDouble( 7 );
      diveHeader.lowestTemp = cu.getDouble( 8 );
      diveHeader.maxDepth = cu.getInt( 9 );
      diveHeader.countSamples = cu.getInt( 10 );
      diveHeader.diveLength = cu.getInt( 11 );
      diveHeader.units = cu.getString( 12 );
      diveHeader.notes = cu.getString( 13 );
      diveHeader.longgitude = cu.getString( 14 );
      diveHeader.latitude = cu.getString( 15 );
      //
      // Cursor schliessen
      //
      cu.close();
      if( BuildConfig.DEBUG ) Log.d( TAG, "getDiveHeader: OK" );
      return( diveHeader );
    }
    return( null );
  }
}
