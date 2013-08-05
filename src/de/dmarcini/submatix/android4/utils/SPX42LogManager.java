package de.dmarcini.submatix.android4.utils;

import android.database.sqlite.SQLiteDatabase;

public class SPX42LogManager extends SPX42AliasManager
{
  public SPX42LogManager( SQLiteDatabase db ) throws NoDatabaseException
  {
    super( db );
  }
}
