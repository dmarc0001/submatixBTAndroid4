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
package de.dmarcini.submatix.android4.full.comm;

import java.util.Calendar;

/**
 * Objekt zur Übergabe von Nachrichten vom Service an die App
 * <p>
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
   */ private BtServiceMessage()
  {
    this.id = - 1;
    this.container = null;
    this.timestamp = - 1L;
  }

  /**
   * Konstruktor mit ID
   * <p>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.comm
   * <p>
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
   * Konstruktor mit Messageid und Objekt
   * <p>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.comm
   * <p>
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
   * Konstruktor mit Messageid und Objekt und Zeitpunkt
   * <p>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.comm
   * <p>
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
   * Gib die ID her
   * <p>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.comm
   * <p>
   * Stand: 03.12.2013
   *
   * @return die ID
   */
  public int getId()
  {
    return ( id );
  }

  /**
   * Gib dfen Container zurück
   * <p>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.comm
   * <p>
   * Stand: 03.12.2013
   *
   * @return Contaionerobjekt
   */
  public Object getContainer()
  {
    return ( container );
  }

  /**
   * Gib den Zeitstempel zurück
   * <p>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.comm
   * <p>
   * Stand: 03.12.2013
   *
   * @return Ereigniszeit
   */
  public long getTimeStamp()
  {
    return ( timestamp );
  }
}
