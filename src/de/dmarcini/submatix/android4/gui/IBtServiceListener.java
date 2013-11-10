package de.dmarcini.submatix.android4.gui;

import de.dmarcini.submatix.android4.comm.BtServiceMessage;

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
  public void handleMessages( final int what, final BtServiceMessage msg );

  public void msgConnecting( final BtServiceMessage msg );

  public void msgConnected( final BtServiceMessage msg );

  public void msgDisconnected( final BtServiceMessage msg );

  public void msgRecivedTick( final BtServiceMessage msg );

  public void msgRecivedAlive( final BtServiceMessage msg );

  public void msgConnectError( final BtServiceMessage msg );

  public void msgReciveWriteTmeout( final BtServiceMessage msg );
}
