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
/**
 * SQL-Helper fuer meine SQL-Datenbank fuer Logdaten
 */
package de.dmarcini.submatix.android4.full.utils;

import java.io.File;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;
import de.dmarcini.submatix.android4.full.ApplicationDEBUG;

/**
 * 
 * Helferklasse für den Umgang mit SQLite Datenbanken. Die Klasse nimmt die Arbeit des Öffnens, Erzeugens und Updaten der Datenbanken ab.
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class DataSQLHelper extends SQLiteOpenHelper
{
  private static final String TAG    = DataSQLHelper.class.getSimpleName();
  // Tabellenpfad und -Name
  private String              dbName = null;
  private Context             cx     = null;

  /**
   * Der Konstruktor der Helferklasse.
   * 
   * @see android.database.sqlite.SQLiteOpenHelper
   * @param context
   *          Der Anwendungskontext hier
   * @param dbn
   *          (Database Name)
   */
  public DataSQLHelper( Context context, String dbn )
  {
    super( context, ProjectConst.DATABASE_NAME, null, ProjectConst.DATABASE_VERSION );
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "DataSQLHelper..." );
    cx = context;
    dbName = dbn;
  }

  private void createAliasTable( final SQLiteDatabase db )
  {
    String sql;
    //
    Log.i( TAG, "createAliasTable..." );
    // Alias Tabelle
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "create table " + ProjectConst.A_TABLE_ALIASES + "..." );
    sql = "create table  " + ProjectConst.A_TABLE_ALIASES + " ";
    sql += "(";
    sql += ProjectConst.A_DEVICEID + " integer primary key autoincrement, \n";
    sql += ProjectConst.A_DEVNAME + " text not null, \n";
    sql += ProjectConst.A_ALIAS + " text not null, \n";
    sql += ProjectConst.A_MAC + " text not null, \n";
    sql += ProjectConst.A_SERIAL + " text\n,";
    sql += ProjectConst.A_PIN + " text\n";
    sql += ");";
    try
    {
      db.execSQL( sql );
    }
    catch( SQLException ex )
    {
      Log.e( TAG, ex.getLocalizedMessage() );
      Toast.makeText( cx, ex.getLocalizedMessage(), Toast.LENGTH_SHORT ).show();
      return;
    }
  }

  /**
   * 
   * Tauchlog Tabelle erstellen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 07.08.2013
   * 
   * @param db
   */
  private void createMainTable( SQLiteDatabase db )
  {
    String sql;
    // Main-Tabelle
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "create table " + ProjectConst.H_TABLE_DIVELOGS + "..." );
    sql = "create table  " + ProjectConst.H_TABLE_DIVELOGS + " ";
    sql += "(";
    sql += ProjectConst.H_DIVEID + " integer primary key autoincrement, \n";
    sql += ProjectConst.H_DEVICEID + " integer not null, \n";
    sql += ProjectConst.H_FILEONMOBILE + " text not null, \n";
    sql += ProjectConst.H_DIVENUMBERONSPX + " integer not null, \n";
    sql += ProjectConst.H_DEVICESERIAL + " text not null, \n";
    sql += ProjectConst.H_STARTTIME + " integer not null, \n";
    sql += ProjectConst.H_HADSEND + " interger, \n";
    sql += ProjectConst.H_FILEONSPX + " text not null, \n";
    sql += ProjectConst.H_FIRSTTEMP + " real, \n";
    sql += ProjectConst.H_LOWTEMP + " real, \n";
    sql += ProjectConst.H_MAXDEPTH + " integer, \n";
    sql += ProjectConst.H_SAMPLES + " integer, \n";
    sql += ProjectConst.H_DIVELENGTH + " integer, \n";
    sql += ProjectConst.H_UNITS + " text not null, \n";
    sql += ProjectConst.H_NOTES + " text, \n";
    sql += ProjectConst.H_GEO_LON + " text, \n";
    sql += ProjectConst.H_GEO_LAT + " text \n";
    sql += ");";
    try
    {
      db.execSQL( sql );
    }
    catch( SQLException ex )
    {
      Log.e( TAG, ex.getLocalizedMessage() );
      Toast.makeText( cx, ex.getLocalizedMessage(), Toast.LENGTH_SHORT ).show();
      return;
    }
    // Erzeuge index
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "create INDEX  on Table " + ProjectConst.H_TABLE_DIVELOGS + "..." );
    try
    {
      sql = "create INDEX  idx_" + ProjectConst.H_TABLE_DIVELOGS + "_" + ProjectConst.H_STARTTIME;
      sql += " ON " + ProjectConst.H_TABLE_DIVELOGS + "(" + ProjectConst.H_STARTTIME + " ASC);";
      db.execSQL( sql );
      sql = "create INDEX  idx_" + ProjectConst.H_DEVICEID;
      sql += " ON " + ProjectConst.H_TABLE_DIVELOGS + "(" + ProjectConst.H_DEVICEID + " ASC);";
      db.execSQL( sql );
    }
    catch( SQLException ex )
    {
      Log.e( TAG, ex.getLocalizedMessage() );
      Toast.makeText( cx, ex.getLocalizedMessage(), Toast.LENGTH_SHORT ).show();
      return;
    }
    Log.i( TAG, "createMainTable...OK" );
  }

  /**
   * Erzeuge Tabellen in einer bereits geöffneten Datenbank
   * 
   * @param db
   *          Datenbankobjekt
   */
  private void createTables( final SQLiteDatabase db )
  {
    Log.i( TAG, "createTables..." );
    // Alias Tabelle
    createAliasTable( db );
    // Main-Tabelle
    createMainTable( db );
    Log.i( TAG, "createTables...OK" );
  }

  /**
   * Lösche Tabellen aus einer bestehenden geöffneten Datenbank zur Erzeugung einer neuen Version.
   * 
   * @param db
   *          geöffnete Datenbank
   */
  private void dropTables( final SQLiteDatabase db )
  {
    String sql = null;
    Log.i( TAG, "dropTables..." );
    // Aliase
    sql = "drop table " + ProjectConst.A_TABLE_ALIASES + ";";
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SQL: <" + sql + ">" );
    try
    {
      db.execSQL( sql );
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SQL: <" + sql + ">.... OK" );
    }
    catch( SQLException ex )
    {
      Log.e( TAG, ex.getLocalizedMessage() );
      Toast.makeText( cx, ex.getLocalizedMessage(), Toast.LENGTH_SHORT ).show();
      return;
    }
    // Maindaten
    sql = "drop table " + ProjectConst.H_TABLE_DIVELOGS + ";";
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SQL: <" + sql + ">" );
    try
    {
      db.execSQL( sql );
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SQL: <" + sql + ">.... OK" );
    }
    catch( SQLException ex )
    {
      Log.e( TAG, ex.getLocalizedMessage() );
      Toast.makeText( cx, ex.getLocalizedMessage(), Toast.LENGTH_SHORT ).show();
      return;
    }
    Log.i( TAG, "dropTables...OK" );
  }

  /**
   * 
   * Gib das Verzeichnis der DB als File Objekt zurück
   * 
   * Project: SubmatixBTLog-Service Package: de.dmarcini.bluethooth.submatix.btService
   * 
   * 
   * Stand: 30.10.2011
   * 
   * @return Verzeichnis der DB als File Objekt
   */
  public File getDbDir()
  {
    File dbFile;
    dbFile = new File( dbName );
    return( new File( dbFile.getParent() ) );
  }

  /**
   * Gib eine geöffnete Datenbank zurück.
   * 
   * @param writable
   * @return Objekt einer Datenbank als SQLiteDatabase
   */
  @SuppressWarnings( "resource" )
  private SQLiteDatabase getOpenDatabase( boolean writable )
  {
    SQLiteDatabase dbObj;
    int flags;
    // Datenbank versuchen zu oeffnen
    if( writable )
      flags = SQLiteDatabase.OPEN_READWRITE;
    else
      flags = SQLiteDatabase.OPEN_READONLY;
    try
    {
      dbObj = SQLiteDatabase.openDatabase( dbName, null, flags );
    }
    catch( SQLiteCantOpenDatabaseException ex )
    {
      Log.w( TAG, "can't open Database, try create one... <" + ex.getLocalizedMessage() + ">" );
      try
      {
        // Geht nicht, probier mal mit erzeugen
        dbObj = SQLiteDatabase.openDatabase( dbName, null, SQLiteDatabase.OPEN_READWRITE + SQLiteDatabase.CREATE_IF_NECESSARY );
        // wennes hier eine exception git, bitte sehr....
        dbObj.setVersion( ProjectConst.DATABASE_VERSION );
        onCreate( dbObj );
      }
      catch( SQLiteException ex1 )
      {
        Log.e( TAG, "can't open Database. Got an exception: <" + ex.getLocalizedMessage() + ">" );
        dbObj = null;
        return( null );
      }
    }
    catch( SQLiteException ex )
    {
      Log.e( TAG, "can't open Database. Got an exception: <" + ex.getLocalizedMessage() + ">" );
      dbObj = null;
      return( null );
    }
    return( dbObj );
  }

  /**
   * Gib eine nur lesbare Datenbank zurück
   * 
   * @return eine nur lesbare Datenbank oder null
   */
  @Override
  public SQLiteDatabase getReadableDatabase()
  {
    SQLiteDatabase dbObj;
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d( TAG, "getReadableDatabase()... " );
      Log.d( TAG, "Datepath: " + dbName );
    }
    dbObj = getOpenDatabase( false );
    // OK, offen Upgrade abfragen
    onUpgrade( dbObj, dbObj.getVersion(), ProjectConst.DATABASE_VERSION );
    if( dbObj.isOpen() && dbObj.isReadOnly() )
      return( dbObj );
    else
      return( getOpenDatabase( false ) );
  }

  /**
   * Gib eine beschreibbare Datenbank zurück
   * 
   * @return eine bescheibbare Datenbank oder null
   */
  @Override
  public SQLiteDatabase getWritableDatabase()
  {
    SQLiteDatabase dbObj;
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d( TAG, "getWritableDatabase... " );
      Log.d( TAG, "Datepath: " + dbName );
    }
    dbObj = getOpenDatabase( true );
    // OK, offen Upgrade abfragen
    onUpgrade( dbObj, dbObj.getVersion(), ProjectConst.DATABASE_VERSION );
    if( dbObj.isOpen() )
      return( dbObj );
    else
      return( getOpenDatabase( true ) );
  }

  /**
   * Ereignis beim Erzeugen des Objektes, nach Konstruktor...
   * 
   * @param db
   *          geöffnete Datenbank
   */
  @Override
  public void onCreate( SQLiteDatabase db )
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreate..." );
    createTables( db );
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreate...OK" );
  }

  /**
   * Wird beim öffnen einer Datenbank aufgerufen um zu checken, ob ein Update der Datenbankstrukturen nowendig ist.
   * 
   * @param db
   *          geöffnete Datenbank
   * @param newVersion
   *          eventuell neue Version Der Datenbank
   */
  @SuppressWarnings( "resource" )
  @Override
  public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onUpgrade Old: <" + oldVersion + "> New: <" + newVersion + ">..." );
    if( oldVersion >= newVersion ) return;
    Log.i( TAG, "onUpgrade: update Version from Old: <" + oldVersion + "> to New: <" + newVersion + ">..." );
    if( db.isReadOnly() )
    {
      // neu RW oeffnen
      Log.v( TAG, "Database ist readonly. close and open RW..." );
      db.close();
      db = getOpenDatabase( true );
    }
    if( oldVersion == 1 && newVersion == 2 )
    {
      createMainTable( db );
      db.setVersion( newVersion );
    }
    else if( oldVersion == 4 )
    {
      if( newVersion == 5 )
      {
        upgradeV4ToV5( db );
        db.setVersion( newVersion );
      }
      else if( newVersion == 6 )
      {
        upgradeV4ToV5( db );
        upgradeV5ToV6( db );
        db.setVersion( newVersion );
      }
    }
    else if( oldVersion == 5 )
    {
      upgradeV5ToV6( db );
      db.setVersion( newVersion );
    }
    else
    {
      dropTables( db );
      createTables( db );
      db.setVersion( newVersion );
    }
    db.close();
    Log.i( TAG, "onUpgrade...OK" );
  }

  /**
   * 
   * Tabelle konvertieren (Feld für Nummer auf SPX nach INT konvertieren)
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.utils
   * 
   * Stand: 08.01.2014
   * 
   * @param db
   */
  private void upgradeV4ToV5( SQLiteDatabase db )
  {
    Log.i( TAG, "upgrade db from version 4 to 5..." );
    String sql;
    try
    {
      // indizi loeschen
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "drop old indizies..." );
      sql = "drop index idx_" + ProjectConst.H_TABLE_DIVELOGS + "_" + ProjectConst.H_STARTTIME + ";";
      db.execSQL( sql );
      sql = "drop index idx_" + ProjectConst.H_DEVICEID + ";";
      db.execSQL( sql );
      // Tabelle umbenennen
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "rename old table..." );
      sql = "alter table " + ProjectConst.H_TABLE_DIVELOGS + " rename to " + ProjectConst.H_TABLE_DIVELOGS + "_old;";
      db.execSQL( sql );
      // tabelle neu anlegen
      createMainTable( db );
      // Daten in neue Tablelle übernehmen
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "convert data..." );
      sql = "insert into " + ProjectConst.H_TABLE_DIVELOGS + " select * from " + ProjectConst.H_TABLE_DIVELOGS + "_old;";
      db.execSQL( sql );
      // alte Tabelle entfernen
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "drop old table..." );
      sql = "drop table " + ProjectConst.H_TABLE_DIVELOGS + "_old;";
      db.execSQL( sql );
    }
    catch( SQLException ex )
    {
      Log.e( TAG, ex.getLocalizedMessage() );
      Toast.makeText( cx, ex.getLocalizedMessage(), Toast.LENGTH_SHORT ).show();
      return;
    }
  }

  /**
   * 
   * Upgrade vin Version 5 nach Version 6
   *
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.utils
   * 
   * Stand: 15.11.2014
   * 
   * @param db
   */
  private void upgradeV5ToV6( SQLiteDatabase db )
  {
    Log.i( TAG, "upgrade db from version 5 to 6..." );
    String sql;
    try
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "rename old table..." );
      sql = "alter table " + ProjectConst.A_TABLE_ALIASES + " rename to " + ProjectConst.A_TABLE_ALIASES + "_old;";
      db.execSQL( sql );
      // tabelle neu anlegen
      createAliasTable( db );
      // Daten in neue Tablelle übernehmen
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "convert data..." );
      //@formatter:off 
      sql = String.format( "insert into %s (%s,%s,%s,%s,%s) select %s,%s,%s,%s,%s from %s_old;",
              ProjectConst.A_TABLE_ALIASES,
              // fields to
              ProjectConst.A_DEVICEID,
              ProjectConst.A_DEVNAME,
              ProjectConst.A_ALIAS,
              ProjectConst.A_MAC,
              ProjectConst.A_SERIAL,
              // fields from
              ProjectConst.A_DEVICEID,
              ProjectConst.A_DEVNAME,
              ProjectConst.A_ALIAS,
              ProjectConst.A_MAC,
              ProjectConst.A_SERIAL,
              // table from
              ProjectConst.A_TABLE_ALIASES );              
      //@formatter:on 
      db.execSQL( sql );
      // alte Tabelle entfernen
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "drop old table..." );
      sql = "drop table " + ProjectConst.A_TABLE_ALIASES + "_old;";
      db.execSQL( sql );
    }
    catch( SQLException ex )
    {
      Log.e( TAG, ex.getLocalizedMessage() );
      Toast.makeText( cx, ex.getLocalizedMessage(), Toast.LENGTH_SHORT ).show();
      return;
    }
  }
}
