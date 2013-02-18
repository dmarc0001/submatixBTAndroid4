package de.dmarcini.submatix.android4.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import de.dmarcini.submatix.android4.utils.ProjectConst;

public class BlueThoothCommThread extends Thread
{
  private final static String    TAG              = BlueThoothCommThread.class.getSimpleName();
  private volatile boolean       run              = true;
  // soll nach Timeout der Thread enden?
  private volatile boolean       shouldTimeoutEnd = false;
  private long                   timeForTimeout   = 0L;
  // private final BluetoothAdapter mBluetoothAdapter = null;
  @SuppressWarnings( "unused" )
  private Activity               act              = null;
  private String                 connectedDevice  = null;
  private final BluetoothAdapter mBtAdapter       = null;
  private volatile int           mConnectionState;
  private static ConnectThread   mConnectThread   = null;
  private static ConnectedThread mConnectedThread = null;
  private volatile boolean       isLogentryMode   = false;

  /**
   * This thread runs during a connection with a remote device. It handles all incoming and outgoing transmissions.
   */
  private class ConnectedThread extends Thread
  {
    private final BluetoothSocket mmSocket;
    private final InputStream     mmInStream;
    private final OutputStream    mmOutStream;

    /**
     * 
     * Konstruktor des Threads
     * 
     * Project: SubmatixBluethoothLogger Package: de.dmarcini.submatix.logger.service
     * 
     * @author Dirk Marciniak (dirk_marciniak@arcor.de)
     * 
     *         Stand: 24.02.2012
     * @param socket
     */
    public ConnectedThread( BluetoothSocket socket )
    {
      Log.d( TAG, "create ConnectedThread" );
      mmSocket = socket;
      InputStream tmpIn = null;
      OutputStream tmpOut = null;
      // Get the BluetoothSocket input and output streams
      try
      {
        tmpIn = socket.getInputStream();
        tmpOut = socket.getOutputStream();
      }
      catch( IOException e )
      {
        Log.e( TAG, "temp sockets not created", e );
      }
      mmInStream = tmpIn;
      mmOutStream = tmpOut;
    }

    /**
     * 
     * Thread abbrechen
     * 
     * Project: SubmatixBluethoothLogger Package: de.dmarcini.submatix.logger.service
     * 
     * @author Dirk Marciniak (dirk_marciniak@arcor.de)
     * 
     *         Stand: 24.02.2012
     */
    public void cancel()
    {
      try
      {
        mmSocket.close();
      }
      catch( IOException e )
      {
        Log.e( TAG, "close() of connect socket failed", e );
      }
    }

    /**
     * 
     * Bearbeite einen Logeintrag (eine Zeile des Profils)
     * 
     * Project: SubmatixBluethoothLogger Package: de.dmarcini.submatix.logger.service
     * 
     * @author Dirk Marciniak (dirk_marciniak@arcor.de)
     * 
     *         Stand: 24.02.2012
     * @param start
     * @param end
     * @param mInStrBuffer
     */
    private void execLogentryCmd( int start, int end, StringBuffer mInStrBuffer )
    {
      String readMessage;
      int lstart, lend;
      Log.v( TAG, "execLogentryCmd..." );
      lstart = mInStrBuffer.indexOf( ProjectConst.STX );
      lend = mInStrBuffer.indexOf( ProjectConst.ETX );
      if( lstart > -1 && lend > lstart )
      {
        // ups, hier ist ein "normales" Kommando verpackt
        Log.i( TAG, "oops, normalCmd found.... change to execNormalCmd..." );
        isLogentryMode = false;
        // den anfang wegputzen
        // mInStrBuffer = mInStrBuffer.delete( 0, lstart );
        execNormalCmd( lstart, lend, mInStrBuffer );
        return;
      }
      // muss der anfang weg?
      if( start > 0 )
      {
        // das davor kann dann weg...
        mInStrBuffer = mInStrBuffer.delete( 0, start );
        readMessage = mInStrBuffer.toString();
        // Indizies korrigieren
        end = mInStrBuffer.indexOf( ProjectConst.FILLER, start + ProjectConst.FILLER.length() );
        start = 0;
      }
      // lese das Ding ohne den Schmandzius der Fï¿½ller
      readMessage = mInStrBuffer.substring( ProjectConst.FILLER.length(), end );
      // lï¿½sche das schon mal raus...
      mInStrBuffer = mInStrBuffer.delete( 0, end );
      readMessage = readMessage.replaceAll( ProjectConst.FILLERCHAR, "" );
      // Sende an aufrufende Activity
      Log.v( TAG, "Logline Recived <" + readMessage + ">" );
      // TODO: mHandler.obtainMessage( ProjectConst.MESSAGE_LOGENTRY_LINE, -1, -1, readMessage ).sendToTarget();
    }

    /**
     * 
     * Bearbeite eine normale Zeile vom SPX kommend
     * 
     * Project: SubmatixBluethoothLogger Package: de.dmarcini.submatix.logger.service
     * 
     * @author Dirk Marciniak (dirk_marciniak@arcor.de)
     * 
     *         Stand: 24.02.2012
     * @param start
     * @param end
     * @param mInStrBuffer
     */
    private void execNormalCmd( int start, int end, StringBuffer mInStrBuffer )
    {
      String readMessage;
      Log.v( TAG, "execNormalCmd..." );
      // muss der anfang weg?
      if( start > 0 )
      {
        // das davor kann dann weg...
        mInStrBuffer = mInStrBuffer.delete( 0, start );
        readMessage = mInStrBuffer.toString();
        // Indizies korrigieren
        end = mInStrBuffer.indexOf( ProjectConst.ETX );
        start = 0;
      }
      // jetz beginnt der String immer bei 0, lese das Ding
      readMessage = mInStrBuffer.substring( 1, end );
      // lösche das schon mal raus...
      mInStrBuffer = mInStrBuffer.delete( 0, end + 1 );
      // Send the obtained bytes to the UI Activity
      Log.v( TAG, "normal Message Recived <" + readMessage + ">" );
      // TODO: intHandler.obtainMessage( ProjectConst.INT_MESSAGE_READ, -1, -1, readMessage ).sendToTarget();
    }

    /**
     * Hier nimmt der Thread seinen Lauf
     */
    @Override
    public void run()
    {
      Log.i( TAG, "BEGIN mConnectedThread" );
      StringBuffer mInStrBuffer = new StringBuffer( 1024 );
      String readMessage;
      byte[] buffer = new byte[1024];
      int bytes, start, end, lstart, lend;
      boolean logCmd, normalCmd;
      // Keep listening to the InputStream while connected
      while( true )
      {
        // lese von InputStream
        try
        {
          bytes = mmInStream.read( buffer );
        }
        catch( IOException e )
        {
          Log.e( TAG, "disconnected", e );
          // TODO: connectionLost();
          cancel();
          break;
        }
        readMessage = new String( buffer, 0, bytes );
        // reicht der Platz noch?
        if( ( mInStrBuffer.capacity() - mInStrBuffer.length() ) < bytes )
        {
          if( ( mInStrBuffer.capacity() + 1024 ) > ProjectConst.MAXINBUFFER )
          {
            Log.e( TAG, "INPUT BUFFER OVERFLOW!" );
            cancel();
            break;
          }
          // Buffer vergrößern
          mInStrBuffer.setLength( mInStrBuffer.capacity() + 1024 );
        }
        // Puffer auffüllen
        mInStrBuffer.append( readMessage );
        readMessage = mInStrBuffer.toString();
        // die Nachricht abarbeitern, solange komplette MSG da sind
        start = mInStrBuffer.indexOf( ProjectConst.STX );
        end = mInStrBuffer.indexOf( ProjectConst.ETX );
        if( isLogentryMode )
        {
          lstart = mInStrBuffer.indexOf( ProjectConst.FILLER );
          lend = mInStrBuffer.indexOf( ProjectConst.FILLER, start + ProjectConst.FILLER.length() );
        }
        else
        {
          lstart = -1;
          lend = -1;
        }
        // solange etwas gefunden wird
        while( ( ( start > -1 ) && ( end > start ) ) || ( ( lstart > -1 ) && ( lend > lstart ) ) )
        {
          if( ( start > -1 ) && ( end > start ) )
            normalCmd = true;
          else
            normalCmd = false;
          if( ( lstart > -1 ) && ( lend > lstart ) )
            logCmd = true;
          else
            logCmd = false;
          // womit anfangen?
          // sind beide zu finden?
          if( normalCmd == true && logCmd == true )
          {
            // entscheidung, wer zuerst
            if( start < lstart )
            {
              execNormalCmd( start, end, mInStrBuffer );
            }
            else
            {
              execLogentryCmd( lstart, lend, mInStrBuffer );
            }
          }
          else
          {
            // nein, nur ein Typ. Welcher?
            if( normalCmd == true )
            {
              execNormalCmd( start, end, mInStrBuffer );
            }
            else if( logCmd == true )
            {
              execLogentryCmd( lstart, lend, mInStrBuffer );
            }
          }
          start = mInStrBuffer.indexOf( ProjectConst.STX );
          end = mInStrBuffer.indexOf( ProjectConst.ETX );
          if( isLogentryMode )
          {
            lstart = mInStrBuffer.indexOf( ProjectConst.FILLER );
            lend = mInStrBuffer.indexOf( ProjectConst.FILLER, start + ProjectConst.FILLER.length() );
          }
          else
          {
            lstart = -1;
            lend = -1;
          }
        }
      }
    }

    /**
     * Write to the connected OutStream.
     * 
     * @param buffer
     *          The bytes to write
     */
    public void write( byte[] buffer )
    {
      try
      {
        mmOutStream.write( buffer );
        // Share the sent message back to the UI Activity
        // mHandler.obtainMessage( MESSAGE_WRITE, -1, -1, new String( buffer ) ).sendToTarget();
      }
      catch( IOException e )
      {
        Log.e( TAG, "Exception during write", e );
      }
    }
  }

  /**
   * This thread runs while attempting to make an outgoing connection with a device. It runs straight through; the connection either succeeds or fails.
   */
  private class ConnectThread extends Thread
  {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    /**
     * 
     * Konstruktor des Threads, der die Verbindung aufbauen soll.
     * 
     * Project: SubmatixBluethoothLogger Package: de.dmarcini.submatix.logger.service
     * 
     * @author Dirk Marciniak (dirk_marciniak@arcor.de)
     * 
     *         Stand: 24.02.2012
     * @param device
     */
    public ConnectThread( BluetoothDevice device )
    {
      mmDevice = device;
      BluetoothSocket tmp = null;
      Log.v( TAG, "createRfCommSocketToServiceRecord(" + ProjectConst.SERIAL_DEVICE_UUID + ")" );
      // Get a BluetoothSocket for a connection with the
      // given BluetoothDevice
      try
      {
        tmp = device.createRfcommSocketToServiceRecord( ProjectConst.SERIAL_DEVICE_UUID );
      }
      catch( IOException e )
      {
        Log.e( TAG, "create() failed", e );
      }
      mmSocket = tmp;
    }

    /**
     * 
     * Den Verbindungsthread abbrechen.
     * 
     * Project: SubmatixBluethoothLogger Package: de.dmarcini.submatix.logger.service
     * 
     * @author Dirk Marciniak (dirk_marciniak@arcor.de)
     * 
     *         Stand: 24.02.2012
     */
    public void cancel()
    {
      try
      {
        mmSocket.close();
      }
      catch( IOException e )
      {
        Log.e( TAG, "close() of connect socket failed", e );
      }
    }

    /**
     * Hier nummt der Thread seinen Lauf.
     */
    @Override
    public void run()
    {
      Log.i( TAG, "BEGIN mConnectThread" );
      setName( "ConnectThread" );
      // Always cancel discovery because it will slow down a connection
      if( null == mBtAdapter )
      {
        Log.e( TAG, "ConnectThread->run-> Kein BT-Adapter verfï¿½gbar!" );
        return;
      }
      mBtAdapter.cancelDiscovery();
      // Make a connection to the BluetoothSocket
      try
      {
        // This is a blocking call and will only return on a
        // successful connection or an exception
        Log.v( TAG, "Connect Thread -> Socket connecting (blocking)..." );
        mmSocket.connect();
        Log.v( TAG, "Connect Thread -> connected..." );
      }
      catch( IOException e )
      {
        // TODO: connectionFailed();
        // Close the socket
        try
        {
          mmSocket.close();
        }
        catch( IOException e2 )
        {
          Log.e( TAG, "unable to close() socket during connection failure", e2 );
        }
        // Start the service over to restart listening mode
        // TODO: BluethoothComService.this.start();
        return;
      }
      // Reset the ConnectThread because we're done
      synchronized( BlueThoothCommThread.this )
      {
        mConnectThread = null;
      }
      // Start the connected thread
      Log.v( TAG, "Connect Thread -> run connected()" );
      // TODO: connected( mmSocket, mmDevice );
    }
  }

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

  public synchronized void connect( String addr )
  {
    BluetoothDevice device = null;
    Log.v( TAG, "connect to: " + addr );
    connectedDevice = null;
    if( mBtAdapter == null )
    {
      Log.e( TAG, "connect(): None BT-Adapter found!" );
      return;
    }
    device = mBtAdapter.getRemoteDevice( addr );
    // Cancel any thread attempting to make a connection
    if( mConnectionState == ProjectConst.STATE_CONNECTING )
    {
      if( mConnectThread != null )
      {
        mConnectThread.cancel();
        mConnectThread = null;
      }
    }
    // Cancel any thread currently running a connection
    if( mConnectedThread != null )
    {
      mConnectedThread.cancel();
      mConnectedThread = null;
    }
    // Start the thread to connect with the given device
    mConnectThread = new ConnectThread( device );
    connectedDevice = addr;
    mConnectThread.start();
    // TODO: setState( ProjectConst.STATE_CONNECTING );
  }
}
