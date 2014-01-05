package de.dmarcini.submatix.android4.full.gui;

import de.dmarcini.submatix.android4.full.comm.BtServiceMessage;

/**
 * 
 * Interfacebeschreibung für Bluethooth Service Listener
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public interface IBtServiceListener
{
  /**
   * 
   * Behandle alle ankommenden Nachrichten
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 16.11.2013
   * 
   * @param what
   * @param msg
   */
  public void handleMessages( final int what, final BtServiceMessage msg );

  /**
   * 
   * Behandle ankommende Nachricht über den Versuch eine Verbindung aufzubauen
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 16.11.2013
   * 
   * @param msg
   */
  public void msgConnecting( final BtServiceMessage msg );

  /**
   * 
   * Behandle Nachricht über den erfolgreichen Aufbau einer Verbindung zum BT Gerät
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 16.11.2013
   * 
   * @param msg
   */
  public void msgConnected( final BtServiceMessage msg );

  /**
   * 
   * Behandle Nachricht über den Verlust der BT-Verbindung
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 16.11.2013
   * 
   * @param msg
   */
  public void msgDisconnected( final BtServiceMessage msg );

  /**
   * 
   * Behandle TICK-Nachricht vom Service
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 16.11.2013
   * 
   * @param msg
   */
  public void msgRecivedTick( final BtServiceMessage msg );

  /**
   * 
   * Behandle die Nachricht vom Service, dass er nich arbei
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 16.11.2013
   * 
   * @param msg
   */
  public void msgRecivedAlive( final BtServiceMessage msg );

  /**
   * 
   * Behandle die Nachricht vom Service, dass der Verbindungsversuch erfolglos war
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 16.11.2013
   * 
   * @param msg
   */
  public void msgConnectError( final BtServiceMessage msg );

  /**
   * 
   * Behandle die _Nachricht, dass es einen Timeout beim schreiben zum BT-Gerät gab
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 16.11.2013
   * 
   * @param msg
   */
  public void msgReciveWriteTmeout( final BtServiceMessage msg );
}
