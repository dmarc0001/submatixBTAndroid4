package de.dmarcini.submatix.android4.comm;

import android.app.Activity;
import android.util.Log;

public class BlueThoothCommThread extends Thread
{
  private final static String TAG              = BlueThoothCommThread.class.getSimpleName();
  private volatile boolean    run              = true;
  // soll nach Timeout der Thread enden?
  private volatile boolean    shouldTimeoutEnd = false;
  private long                timeForTimeout   = 0L;
  // private final BluetoothAdapter mBluetoothAdapter = null;
  @SuppressWarnings( "unused" )
  private Activity            act              = null;

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
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.gui.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.11.2012
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
      if( shouldTimeoutEnd )
      {
        if( System.currentTimeMillis() >= timeForTimeout )
        {
          run = false;
          Log.w( TAG, "testThread TIMEOUT" );
        }
        else
        {
          threadSleep();
        }
      }
      else
      {
        threadSleep();
        Log.i( TAG, "testThread TICK" );
      }
    }
    Log.i( TAG, "testThread ENDS!" );
  }

  private void threadSleep()
  {
    try
    {
      Thread.sleep( 2000 );
    }
    catch( InterruptedException ex )
    {
      Log.e( TAG, "exception while testThread sleeeeeeeeeps" );
    }
  }

  /**
   * 
   * Wenn nicht Entwarnung kommt, dann stoppe den Thread nach 3 Sekunden
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 16.12.2012
   */
  public void prepareStopThread()
  {
    Log.v( TAG, "thread waits for timeout..." );
    timeForTimeout = System.currentTimeMillis() + 3000;
    shouldTimeoutEnd = true;
  }

  /**
   * 
   * Kein Timeout, der Thread soll weiter laufen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 16.12.2012
   */
  public void threadNotSpopping()
  {
    shouldTimeoutEnd = false;
  }

  public void stopThread()
  {
    Log.v( TAG, "Thread will ending..." );
    run = false;
  }
}
