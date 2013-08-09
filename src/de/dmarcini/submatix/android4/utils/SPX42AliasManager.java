package de.dmarcini.submatix.android4.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import de.dmarcini.submatix.android4.BuildConfig;
import de.dmarcini.submatix.android4.exceptions.NoDatabaseException;

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
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 26.07.2013
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
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 26.07.2013
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
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 26.07.2013
   * @param mac
   * @param defaultAlias
   * @return Den Alias des Gerätes zurück
   */
  public String getAliasForMac( String mac, String defaultAlias )
  {
    String sql, alias;
    Cursor cu;
    //
    if( BuildConfig.DEBUG ) Log.i( TAG, "getAliasForMac..." );
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
      if( BuildConfig.DEBUG ) Log.i( TAG, "getAliasForMac: found <" + alias + ">" );
      return( alias );
    }
    if( BuildConfig.DEBUG ) Log.i( TAG, "getAliasForMac: not found, use default <" + defaultAlias + ">" );
    return( defaultAlias );
  }

  /**
   * 
   * Erzeuge oder update einen Alias für ein Gerät
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 26.07.2013
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
    if( BuildConfig.DEBUG ) Log.i( TAG, "setAliasForMac..." );
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
}
