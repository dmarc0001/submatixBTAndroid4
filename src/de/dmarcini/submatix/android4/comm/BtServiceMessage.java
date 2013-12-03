package de.dmarcini.submatix.android4.comm;

import java.util.Calendar;

/**
 * 
 * Objekt zur Übergabe von Nachrichten vom Service an die App
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 23.02.2013
 */
public class BtServiceMessage
{
  private final int    id;
  private final Object container;
  private final long   timestamp;

  @SuppressWarnings( "unused" )
  /**
   * 
   * Privater Konstruktor
   *
   * Project: SubmatixBTLoggerAndroid
   * Package: de.dmarcini.submatix.android4.comm
   * 
   * Stand: 03.12.2013
   */
  private BtServiceMessage()
  {
    this.id = -1;
    this.container = null;
    this.timestamp = -1L;
  }

  /**
   * 
   * Konstruktor mit ID
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.comm
   * 
   * Stand: 03.12.2013
   * 
   * @param id
   */
  public BtServiceMessage( int id )
  {
    this.id = id;
    this.container = null;
    this.timestamp = Calendar.getInstance().getTimeInMillis();
  }

  /**
   * 
   * Konstruktor mit Messageid und Objekt
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.comm
   * 
   * Stand: 03.12.2013
   * 
   * @param id
   * @param container
   */
  public BtServiceMessage( int id, Object container )
  {
    this.id = id;
    this.container = container;
    this.timestamp = Calendar.getInstance().getTimeInMillis();
  }

  /**
   * 
   * Konstruktor mit Messageid und Objekt und Zeitpunkt
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.comm
   * 
   * Stand: 03.12.2013
   * 
   * @param id
   * @param container
   * @param time
   */
  public BtServiceMessage( int id, Object container, long time )
  {
    this.id = id;
    this.container = container;
    this.timestamp = time;
  }

  /**
   * 
   * Gib die ID her
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.comm
   * 
   * Stand: 03.12.2013
   * 
   * @return die ID
   */
  public int getId()
  {
    return( id );
  }

  /**
   * 
   * Gib dfen Container zurück
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.comm
   * 
   * Stand: 03.12.2013
   * 
   * @return Contaionerobjekt
   */
  public Object getContainer()
  {
    return( container );
  }

  /**
   * 
   * Gib den Zeitstempel zurück
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.comm
   * 
   * Stand: 03.12.2013
   * 
   * @return Ereigniszeit
   * 
   */
  public long getTimeStamp()
  {
    return( timestamp );
  }
}
