/**
 * File BluethoothComService.java
 */
package de.dmarcini.android4.comm;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
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
  protected volatile boolean                                    mIsBusy;
  protected static BluetoothAdapter                          mAdapter = null;
  protected static Handler                                   mHandler = null;
  private BlueThoothCommThread commThread = null;

  
  //@formatter:on
  @Override
  public IBinder onBind( Intent intent )
  {
    Log.v( TAG, "onBind..." );
    IBinder mBluetoothServiceBinder = new BluethoothComServiceBinder();
    ( ( BluethoothComServiceBinder )mBluetoothServiceBinder ).setService( this );
    return( mBluetoothServiceBinder );
  }

  @Override
  public void onCreate()
  {
    Log.v( TAG, "onCreate..." );
  }

  @Override
  public int onStartCommand( Intent intent, int flags, int startId )
  {
    Log.v( TAG, "onStartCommand..." );
    commThread = new BlueThoothCommThread();
    commThread.start();
    // We want this service to continue running until it is explicitly
    // stopped, so return sticky.
    return START_STICKY;
  }

  @Override
  public void onDestroy()
  {
    Log.v( TAG, "onDestroy..." );
    if( commThread != null )
    {
      commThread.stopThread();
      commThread = null;
    }
  }
}
