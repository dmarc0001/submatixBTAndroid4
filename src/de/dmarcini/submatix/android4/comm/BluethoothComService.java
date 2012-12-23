/**
 * File BluethoothComService.java
 */
package de.dmarcini.submatix.android4.comm;

import java.util.Set;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * Service Thread der im Hintergrund die Kommunikation mit dem SPX42 und der Datenbank abwickelt.
 * 
 * @see android.app.Service
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 */
//@formatter:off
public class BluethoothComService extends Service
{
  // Debugging
  private static final String                                    TAG  = BluethoothComService.class.getSimpleName();
  // Member fields
  protected volatile boolean                                  mIsBusy = false;
  protected static BluetoothAdapter                          mAdapter = null;
  protected static Handler                                   mHandler = null;
  // der kann nur einmal per Programm da sein, also STATIC
  private static BlueThoothCommThread                      commThread = null;
  //@formatter:on
  //
  @Override
  public IBinder onBind( Intent intent )
  {
    Log.v( TAG, "onBind..." );
    IBinder mBluetoothServiceBinder = new BluethoothComServiceBinder();
    ( ( BluethoothComServiceBinder )mBluetoothServiceBinder ).setService( this );
    testForStartBTThread();
    return( mBluetoothServiceBinder );
  }

  @Override
  public void onCreate()
  {
    Log.v( TAG, "onCreate..." );
    // ist der Thread am ackern?
    testForStartBTThread();
    mAdapter = BluetoothAdapter.getDefaultAdapter();
  }

  @Override
  public void onDestroy()
  {
    Log.v( TAG, "onDestroy..." );
    if( ( commThread != null ) && ( commThread.getState() != Thread.State.TERMINATED ) )
    {
      commThread.prepareStopThread();
    }
  }

  /**
   * 
   * Muß der Thread gestartet werden?
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 20.12.2012
   */
  private void testForStartBTThread()
  {
    //
    // ist der Thread nicht da oder ist der terminiert
    //
    if( ( commThread == null ) || ( commThread.getState() == Thread.State.TERMINATED ) )
    {
      commThread = new BlueThoothCommThread();
      commThread.start();
    }
  }

  /**
   * 
   * Gibt eine Liste mit gepaarten Devices zurück. Gelistet werden Name und Addr.
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 20.12.2012
   * @return Set von gepaarten BT Devices
   */
  protected Set<BluetoothDevice> getPairedBTDevices()
  {
    if( mAdapter != null )
    {
      return( mAdapter.getBondedDevices() );
    }
    return null;
  }
}
