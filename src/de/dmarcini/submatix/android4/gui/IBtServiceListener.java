package de.dmarcini.submatix.android4.gui;

import de.dmarcini.submatix.android4.comm.BtServiceMessage;

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
