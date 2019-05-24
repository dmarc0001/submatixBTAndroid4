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
package de.dmarcini.submatix.android4.full.interfaces;

import de.dmarcini.submatix.android4.full.comm.BtServiceMessage;

/**
 * Interfacebeschreibung für Bluethooth Service Listener
 * <p>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p>
 *         Stand: 10.11.2013
 */
public interface IBtServiceListener
{
  /**
   * Behandle alle ankommenden Nachrichten
   * <p>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * <p>
   * Stand: 16.11.2013
   *
   * @param what
   * @param msg
   */
  public void handleMessages(final int what, final BtServiceMessage msg);

  /**
   * Behandle ankommende Nachricht über den Versuch eine Verbindung aufzubauen
   * <p>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * <p>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  public void msgConnecting(final BtServiceMessage msg);

  /**
   * Behandle Nachricht über den erfolgreichen Aufbau einer Verbindung zum BT Gerät
   * <p>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * <p>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  public void msgConnected(final BtServiceMessage msg);

  /**
   * Behandle Nachricht über den Verlust der BT-Verbindung
   * <p>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * <p>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  public void msgDisconnected(final BtServiceMessage msg);

  /**
   * Behandle TICK-Nachricht vom Service
   * <p>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * <p>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  public void msgRecivedTick(final BtServiceMessage msg);

  /**
   * Behandle die Nachricht vom Service, dass er nich arbei
   * <p>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * <p>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  public void msgRecivedAlive(final BtServiceMessage msg);

  /**
   * Behandle die Nachricht vom Service, dass der Verbindungsversuch erfolglos war
   * <p>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * <p>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  public void msgConnectError(final BtServiceMessage msg);

  /**
   * Behandle die _Nachricht, dass es einen Timeout beim schreiben zum BT-Gerät gab
   * <p>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * <p>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  public void msgReciveWriteTmeout(final BtServiceMessage msg);
}
