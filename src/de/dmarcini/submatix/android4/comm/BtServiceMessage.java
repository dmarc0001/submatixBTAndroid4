package de.dmarcini.submatix.android4.comm;

import java.util.Calendar;

/**
 * 
 * Objekt zur Übergabe von Nachrichten vom Service an die App
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 23.02.2013
 */
public class BtServiceMessage
{
  private final int    id;
  private final Object container;
  private final long   timestamp;

  @SuppressWarnings( "unused" )
  private BtServiceMessage()
  {
    this.id = -1;
    this.container = null;
    this.timestamp = -1L;
  }

  public BtServiceMessage( int id )
  {
    this.id = id;
    this.container = null;
    this.timestamp = Calendar.getInstance().getTimeInMillis();
  }

  public BtServiceMessage( int id, Object container )
  {
    this.id = id;
    this.container = container;
    this.timestamp = Calendar.getInstance().getTimeInMillis();
  }

  public BtServiceMessage( int id, Object container, long time )
  {
    this.id = id;
    this.container = container;
    this.timestamp = time;
  }

  public int getId()
  {
    return( id );
  }

  public Object getContainer()
  {
    return( container );
  }

  public long getTimeStamp()
  {
    return( timestamp );
  }
}
