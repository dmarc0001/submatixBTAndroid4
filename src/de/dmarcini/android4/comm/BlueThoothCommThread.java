package de.dmarcini.android4.comm;

import android.app.Activity;
import android.util.Log;

public class BlueThoothCommThread extends Thread
{
  private final static String TAG = BlueThoothCommThread.class.getSimpleName();
  // Intent request codes
  private boolean             run = true;
  // private final BluetoothAdapter mBluetoothAdapter = null;
  private Activity            act = null;

  public void setActivity( Activity ac )
  {
    act = ac;
  }

  public void clearActivity()
  {
    act = null;
  }

  /**
   * 
   * Ist der BT-Adapter überhaupt vorhanden?
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.11.2012
   * @return
   */
  @Override
  public void run()
  {
    //
    // wie heisse ich?
    //
    setName( TAG );
    run = true;
    while( run )
    {
      Log.i( TAG, "testThread TICK" );
      try
      {
        Thread.sleep( 2000 );
      }
      catch( InterruptedException ex )
      {
        Log.e( TAG, "exception while testThread sleeeeeeeeeps" );
      }
    }
  }

  public void stopThread()
  {
    Log.v( TAG, "Thread will ending..." );
    run = false;
  }
}
