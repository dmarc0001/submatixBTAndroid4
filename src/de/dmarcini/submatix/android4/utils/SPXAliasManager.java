package de.dmarcini.submatix.android4.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import de.dmarcini.submatix.android4.BuildConfig;

public class SPXAliasManager
{
  private static final String TAG   = SPXAliasManager.class.getSimpleName();
  private SQLiteDatabase      dBase = null;

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
   */
  public SPXAliasManager( SQLiteDatabase db )
  {
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
   * @param defautAlias
   * @return
   */
  public String getAliasForMac( String mac, String defautAlias )
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
      return( alias );
    }
    return( defautAlias );
  }
}
