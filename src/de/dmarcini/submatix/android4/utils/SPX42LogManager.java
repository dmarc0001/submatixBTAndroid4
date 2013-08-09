package de.dmarcini.submatix.android4.utils;

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
    if( BuildConfig.DEBUG ) Log.i( TAG, "isLogInDatabase..." );
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
      if( BuildConfig.DEBUG ) Log.i( TAG, "isLogInDatabase: found <" + count + ">" );
    }
    cu.close();
    if( BuildConfig.DEBUG ) Log.i( TAG, "isLogInDatabase... datasets is <" + count + ">" );
    return( ( count == 0 ) ? false : true );
  }
}
