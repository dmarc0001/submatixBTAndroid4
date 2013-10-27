/**
 * SQL-Helper fuer meine SQL-Datenbank fuer Logdaten
 */
package de.dmarcini.submatix.android4.utils;

import java.io.File;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;
import de.dmarcini.submatix.android4.BuildConfig;

/**
 * Helferklasse für den Umgang mit SQLite Datenbanken.
 * 
 * Die Klasse nimmt die Arbeit des �ffnens, erzeugens und updaten der Datenbanken ab.
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
   * @author Dirk Marciniak
   * @see android.database.sqlite.SQLiteOpenHelper
   * @param context
   *          Der Anwendungskontext hier
   * @param dbn
   *          (Database Name)
   */
  public DataSQLHelper( Context context, String dbn )
  {
    super( context, ProjectConst.DATABASE_NAME, null, ProjectConst.DATABASE_VERSION );
    if( BuildConfig.DEBUG ) Log.d( TAG, "DataSQLHelper..." );
    cx = context;
    dbName = dbn;
  }

  /**
   * Erzeuge Tabellen in einer bereits geöffneten Datenbank
   * 
   * @author Dirk Marciniak
   * @param db
   *          Datenbankobjekt
   */
  private void createTables( final SQLiteDatabase db )
  {
    String sql;
    Log.i( TAG, "createTables..." );
    // Alias Tabelle
    if( BuildConfig.DEBUG ) Log.d( TAG, "create table " + ProjectConst.A_TABLE_ALIASES + "..." );
    sql = "create table  " + ProjectConst.A_TABLE_ALIASES + " ";
    sql += "(";
    sql += ProjectConst.A_DEVICEID + " integer primary key autoincrement, \n";
    sql += ProjectConst.A_DEVNAME + " text not null, \n";
    sql += ProjectConst.A_ALIAS + " text not null, \n";
    sql += ProjectConst.A_MAC + " text not null, \n";
    sql += ProjectConst.A_SERIAL + " text\n";
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
    // Main-Tabelle
    if( BuildConfig.DEBUG ) Log.d( TAG, "create table " + ProjectConst.H_TABLE_DIVELOGS + "..." );
    sql = "create table  " + ProjectConst.H_TABLE_DIVELOGS + " ";
    sql += "(";
    sql += ProjectConst.H_DIVEID + " integer primary key autoincrement, \n";
    sql += ProjectConst.H_DEVICEID + " integer not null, \n";
    sql += ProjectConst.H_FILEONMOBILE + " text not null, \n";
    sql += ProjectConst.H_DIVENUMBERONSPX + " text not null, \n";
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
    if( BuildConfig.DEBUG ) Log.d( TAG, "create INDEX  on Table " + ProjectConst.H_TABLE_DIVELOGS + "..." );
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
    Log.i( TAG, "createTables...OK" );
  }

  /**
   * Lösche Tabellen aus einer bestehenden geöffneten Datenbank zur Erzeugung einer neuen Version.
   * 
   * @author Dirk Marciniak
   * @param db
   *          geöffnete Datenbank
   */
  private void dropTables( final SQLiteDatabase db )
  {
    String sql = null;
    Log.i( TAG, "dropTables..." );
    // Aliase
    sql = "drop table " + ProjectConst.A_TABLE_ALIASES + ";";
    if( BuildConfig.DEBUG ) Log.d( TAG, "SQL: <" + sql + ">" );
    try
    {
      db.execSQL( sql );
      if( BuildConfig.DEBUG ) Log.d( TAG, "SQL: <" + sql + ">.... OK" );
    }
    catch( SQLException ex )
    {
      Log.e( TAG, ex.getLocalizedMessage() );
      Toast.makeText( cx, ex.getLocalizedMessage(), Toast.LENGTH_SHORT ).show();
      return;
    }
    // Maindaten
    sql = "drop table " + ProjectConst.H_TABLE_DIVELOGS + ";";
    if( BuildConfig.DEBUG ) Log.d( TAG, "SQL: <" + sql + ">" );
    try
    {
      db.execSQL( sql );
      if( BuildConfig.DEBUG ) Log.d( TAG, "SQL: <" + sql + ">.... OK" );
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
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 30.10.2011
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
   * @author Dirk Marciniak
   * @param writable
   * @return Objekt einer Datenbank als SQLiteDatabase
   */
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
        return( null );
      }
    }
    catch( SQLiteException ex )
    {
      Log.e( TAG, "can't open Database. Got an exception: <" + ex.getLocalizedMessage() + ">" );
      return( null );
    }
    return( dbObj );
  }

  /**
   * Gib eine nur lesbare Datenbank zurück
   * 
   * @author Dirk Marciniak
   * @return eine nur lesbare Datenbank oder null
   */
  @Override
  public SQLiteDatabase getReadableDatabase()
  {
    SQLiteDatabase dbObj;
    if( BuildConfig.DEBUG )
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
   * @author Dirk Marciniak
   * @return eine bescheibbare Datenbank oder null
   */
  @Override
  public SQLiteDatabase getWritableDatabase()
  {
    SQLiteDatabase dbObj;
    if( BuildConfig.DEBUG )
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
   * @author Dirk Marciniak
   * @param db
   *          geöffnete Datenbank
   */
  @Override
  public void onCreate( SQLiteDatabase db )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "onCreate..." );
    createTables( db );
    if( BuildConfig.DEBUG ) Log.d( TAG, "onCreate...OK" );
  }

  /**
   * Wird beim öffnen einer Datenbank aufgerufen um zu checken, ob ein Update der Datenbankstrukturen nowendig ist.
   * 
   * @author Dirk Marciniak
   * @param db
   *          geöffnete Datenbank
   * @param newVersion
   *          eventuell neue Version Der Datenbank
   */
  @Override
  public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "onUpgrade Old: <" + oldVersion + "> New: <" + newVersion + ">..." );
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
    else
    {
      dropTables( db );
      createTables( db );
      db.setVersion( newVersion );
      db.close();
      Log.i( TAG, "onUpgrade...OK" );
    }
  }

  /**
   * 
   * Tauchlog Tabelle erstellen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.08.2013
   * @param db
   */
  private void createMainTable( SQLiteDatabase db )
  {
    String sql;
    // Main-Tabelle
    if( BuildConfig.DEBUG ) Log.d( TAG, "create table " + ProjectConst.H_TABLE_DIVELOGS + "..." );
    sql = "create table  " + ProjectConst.H_TABLE_DIVELOGS + " ";
    sql += "(";
    sql += ProjectConst.H_DIVEID + " integer primary key autoincrement, \n";
    sql += ProjectConst.H_DIVENUMBERONSPX + " text not null, \n";
    sql += ProjectConst.H_DEVICESERIAL + " text not null, \n";
    sql += ProjectConst.H_STARTTIME + " text not null, \n";
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
    if( BuildConfig.DEBUG ) Log.d( TAG, "create INDEX  on Table " + ProjectConst.H_TABLE_DIVELOGS + "..." );
    sql = "create INDEX  idx_" + ProjectConst.H_TABLE_DIVELOGS + "_" + ProjectConst.H_STARTTIME;
    sql += " ON " + ProjectConst.H_TABLE_DIVELOGS + "(" + ProjectConst.H_STARTTIME + " ASC);";
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
    Log.i( TAG, "createMainTable...OK" );
  }
}
