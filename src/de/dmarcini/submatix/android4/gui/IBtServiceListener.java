package de.dmarcini.submatix.android4.gui;

import de.dmarcini.submatix.android4.comm.BtServiceMessage;

public interface IBtServiceListener
{
  public void msgConnecting( BtServiceMessage msg );

  public void msgConnected( BtServiceMessage msg );

  public void msgDisconnected( BtServiceMessage msg );

  public void msgRecivedTick( BtServiceMessage msg );

  public void msgRecivedSerial( BtServiceMessage msg );

  public void msgRecivedAlive( BtServiceMessage msg );

  public void msgConnectError( BtServiceMessage msg );
}
