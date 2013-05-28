package de.dmarcini.submatix.android4.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import de.dmarcini.submatix.android4.BuildConfig;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.gui.areaListActivity;
import de.dmarcini.submatix.android4.utils.ProjectConst;

public class BlueThoothComService extends Service
{
  private static final String  TAG               = BlueThoothComService.class.getSimpleName();
  private static final long    msToEndService    = 6000L;
  private long                 tickToCounter     = 0L;
  private long                 timeToStopService = 0L;
  private NotificationManager  nm;
  static int                   NOTIFICATION      = 815;
  private final Timer          timer             = new Timer();
  private int                  counter           = 0;
  private final int            incrementby       = 1;
  private boolean              isRunning         = false;
  ArrayList<Handler>           mClientHandler    = new ArrayList<Handler>();                  // Messagehandler für Clienten
  int                          mValue            = 0;                                         // Holds last value set by a client.
  private final IBinder        mBinder           = new LocalBinder();
  private BluetoothAdapter     mAdapter          = null;
  private static ConnectThread mConnectThread    = null;
  private static ReaderThread  mReaderThread     = null;
  private static WriterThread  mWriterThread     = null;
  private static volatile int  mConnectionState;
  private volatile boolean     isLogentryMode    = false;
  private String               connectedDevice   = null;

  /**
   * 
   * Der Binder sorgt später für die Übergabe der referenz zum Service (Lokaler Service!)
   * 
   * Project: BtServiceVersuch Package: de.dmarcini.android.btservive
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 21.02.2013
   */
  public class LocalBinder extends Binder
  {
    public BlueThoothComService getService()
    {
      return BlueThoothComService.this;
    }

    public void unregisterServiceHandler( Handler mHandler )
    {
      // if( mIsBusy ) return( null );
      Log.i( TAG, "Client unregister" );
      mClientHandler.remove( mHandler );
      if( mClientHandler.isEmpty() )
      {
        Log.i( TAG, "last Client ist removed..." );
        isRunning = false;
        // zeit bis zum Ende des Service setzen
        timeToStopService = System.currentTimeMillis() + msToEndService;
      }
    }

    public void registerServiceHandler( Handler mHandler )
    {
      Log.i( TAG, "Client register" );
      mClientHandler.add( mHandler );
      isRunning = true;
      timeToStopService = 0L;
      // gibt der Activity gleich den Status
      setState( mConnectionState );
    }
  }

  /**
   * 
   * Der Thread zum Verbinden eines BT Devices
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.03.2013
   */
  private class ConnectThread extends Thread
  {
    private final String          TAGCON = ConnectThread.class.getSimpleName();
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
      if( BuildConfig.DEBUG ) Log.d( TAGCON, "createRfCommSocketToServiceRecord(" + ProjectConst.SERIAL_DEVICE_UUID + ")" );
      //
      // Einen Socket für das Gerät erzeugen
      //
      try
      {
        tmp = device.createRfcommSocketToServiceRecord( ProjectConst.SERIAL_DEVICE_UUID );
      }
      catch( IOException e )
      {
        Log.e( TAGCON, "create() failed", e );
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
        Log.e( TAGCON, "close() of connect socket failed", e );
      }
    }

    /**
     * Hier nimmt der Thread seinen Lauf.
     */
    @Override
    public void run()
    {
      Log.i( TAGCON, "BEGIN mConnectThread" );
      setName( "connect_thread" );
      // Teste mal ob ein Adapter da ist
      if( null == mAdapter )
      {
        Log.e( TAGCON, "not bt-adaper exist!" );
        return;
      }
      // immer discovering beenden, Verbindungsaufbau sonst schleppend
      mAdapter.cancelDiscovery();
      // Eine Verbindung zum BluetoothSocket
      try
      {
        //
        // Dies ist ein blockierender Call
        // er endet mit Verbindung oder Exception
        //
        Log.v( TAGCON, "Socket connecting (blocking)..." );
        mmSocket.connect();
        Log.v( TAGCON, "connected..." );
      }
      catch( IOException e )
      {
        connectionFailed();
        // Socket schließen
        try
        {
          mmSocket.close();
        }
        catch( IOException ex )
        {
          Log.e( TAG, "unable to close() socket during connection failure", ex );
        }
        return;
      }
      // Ich lösch mich selber irgendwie...
      synchronized( BlueThoothComService.this )
      {
        mConnectThread = null;
      }
      // Start the connected thread
      Log.v( TAGCON, "run connected()" );
      deviceConnected( mmSocket, mmDevice );
    }
  }

  private class WriterThread extends Thread
  {
    private final String            TAGWRITER    = WriterThread.class.getSimpleName();
    private final BluetoothSocket   mmSocket;
    private final OutputStream      mmOutStream;
    private Boolean                 cancelThread = false;
    private final ArrayList<String> writeList    = new ArrayList<String>();

    /**
     * 
     * Konstruktor des Writer Thread
     * 
     * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
     * 
     * @author Dirk Marciniak (dirk_marciniak@arcor.de)
     * 
     *         Stand: 28.05.2013
     * @param socket
     */
    public WriterThread( BluetoothSocket socket )
    {
      if( BuildConfig.DEBUG ) Log.d( TAGWRITER, "create WriterThread" );
      mmSocket = socket;
      OutputStream tmpOut = null;
      cancelThread = false;
      // die BluetoothSocket input and output streams erstellen
      try
      {
        tmpOut = socket.getOutputStream();
      }
      catch( IOException e )
      {
        Log.e( TAGWRITER, "temp sockets not created", e );
      }
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
     *         Stand: 28.05.2013
     */
    public void cancel()
    {
      try
      {
        cancelThread = true;
        mmSocket.close();
      }
      catch( IOException ex )
      {
        Log.e( TAGWRITER, "close() of connect socket failed", ex );
      }
    }

    /**
     * Hier läuft der Thread
     */
    @Override
    public void run()
    {
      Log.i( TAGWRITER, "BEGIN WriterThread" );
      // den Inputstram solange schreiben, wie die Verbindung besteht
      writeList.clear();
      cancelThread = false;
      while( !cancelThread )
      {
        if( writeList.isEmpty() )
        {
          try
          {
            Thread.yield();
            wait( 10 );
          }
          catch( InterruptedException ex )
          {}
        }
        else
        {
          // ich gebe einen Eintrag aus...
          try
          {
            // Watchdog für Schreiben aktivieren
            // writeWatchDog = ProjectConst.WATCHDOG_FOR_WRITEOPS;
            // also den String Eintrag in den Outstream...
            mmOutStream.write( ( writeList.remove( 0 ) ).getBytes() );
            // kommt das an, den Watchog wieder AUS
            // writeWatchDog = -1;
            // zwischen den Kommandos etwas warten, der SPX braucht etwas bis er wieder zuhört...
            // das gibt dem Swing-Thread etwas Gelegenheit zum Zeichnen oder irgendwas anderem
            for( int factor = 0; factor < 5; factor++ )
            {
              Thread.yield();
              Thread.sleep( 80 );
            }
          }
          catch( IndexOutOfBoundsException ex )
          {
            // TODO
            // isConnected = false;
            // if( aListener != null )
            // {
            // ActionEvent ev = new ActionEvent( this, ProjectConst.MESSAGE_DISCONNECTED, null );
            // aListener.actionPerformed( ev );
            // }
            cancelThread = true;
            return;
          }
          catch( IOException ex )
          {
            // isConnected = false;
            // if( aListener != null )
            // {
            // ActionEvent ev = new ActionEvent( this, ProjectConst.MESSAGE_DISCONNECTED, null );
            // aListener.actionPerformed( ev );
            // }
            cancelThread = true;
            return;
          }
          catch( InterruptedException ex )
          {}
        }
      }
      Log.i( TAGWRITER, "END WriterThread" );
    }

    /**
     * 
     * Schreibe Daten zum SPX
     * 
     * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
     * 
     * @author Dirk Marciniak (dirk_marciniak@arcor.de)
     * 
     *         Stand: 28.05.2013
     * @param msg
     */
    public synchronized void writeToDevice( String msg )
    {
      writeList.add( msg );
      notifyAll();
    }
  }

  /**
   * 
   * Der Thread, der während der Verbindung alle Datenströme vom Gerät bearbeitet
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.03.2013
   */
  private class ReaderThread extends Thread
  {
    private final String          TAGREADER    = ReaderThread.class.getSimpleName();
    private final BluetoothSocket mmSocket;
    private final InputStream     mmInStream;
    // private final OutputStream mmOutStream;
    private Boolean               cancelThread = false;

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
    public ReaderThread( BluetoothSocket socket )
    {
      if( BuildConfig.DEBUG ) Log.d( TAGREADER, "create ReaderThread" );
      mmSocket = socket;
      InputStream tmpIn = null;
      // OutputStream tmpOut = null;
      cancelThread = false;
      // die BluetoothSocket input and output streams erstellen
      try
      {
        tmpIn = socket.getInputStream();
        // tmpOut = socket.getOutputStream();
      }
      catch( IOException e )
      {
        Log.e( TAGREADER, "temp sockets not created", e );
      }
      mmInStream = tmpIn;
      // mmOutStream = tmpOut;
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
        cancelThread = true;
        mmSocket.close();
      }
      catch( IOException ex )
      {
        Log.e( TAGREADER, "close() of connect socket failed", ex );
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
      Log.v( TAGREADER, "execLogentryCmd..." );
      // TODO: hier Code unterbringen
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
      Log.v( TAGREADER, "execNormalCmd..." );
      // TODO: hier Code unterbringen
    }

    /**
     * Hier nimmt der Thread seinen Lauf
     */
    @Override
    public void run()
    {
      Log.i( TAGREADER, "BEGIN WriterThread" );
      StringBuffer mInStrBuffer = new StringBuffer( 1024 );
      String readMessage;
      byte[] buffer = new byte[1024];
      int bytes, start, end, lstart, lend;
      boolean logCmd, normalCmd;
      // den Inputstram solange lesen, wie die Verbindung besteht
      while( true )
      {
        // lese von InputStream
        try
        {
          bytes = mmInStream.read( buffer );
        }
        catch( IOException e )
        {
          if( cancelThread )
          {
            Log.i( TAGREADER, "while cancel thread: disconnected " + e.getLocalizedMessage() );
          }
          else
          {
            Log.e( TAGREADER, "disconnected " + e.getLocalizedMessage() );
          }
          connectionLost();
          cancel();
          break;
        }
        readMessage = new String( buffer, 0, bytes );
        // reicht der Platz noch?
        if( ( mInStrBuffer.capacity() - mInStrBuffer.length() ) < bytes )
        {
          if( ( mInStrBuffer.capacity() + 1024 ) > ProjectConst.MAXINBUFFER )
          {
            Log.e( TAGREADER, "INPUT BUFFER OVERFLOW!" );
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
  }

  /**
   * Wird immer beim Binden eines Clienten aufgerufen
   */
  @Override
  public IBinder onBind( Intent intent )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "onBind..." );
    showNotification( getText( R.string.notify_service ), getText( R.string.notify_service_connected ) );
    return mBinder;
  }

  @Override
  public boolean onUnbind( Intent intent )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "onUnbind..." );
    showNotification( getText( R.string.notify_service ), getText( R.string.notify_service_disconnected ) );
    return( super.onUnbind( intent ) );
  }

  @Override
  public void onCreate()
  {
    super.onCreate();
    timeToStopService = 0L;
    nm = ( NotificationManager )getSystemService( NOTIFICATION_SERVICE );
    Log.i( TAG, "Service Started." );
    isRunning = true;
    // der Überwachungsthread läuft solange der Service aktiv ist
    timer.scheduleAtFixedRate( new TimerTask() {
      @Override
      public void run()
      {
        onTimerTick();
      }
    }, 10, 100L );
    // Service ist Erzeugt!
    showNotification( getText( R.string.notify_service ), getText( R.string.notify_service_started ) );
    mAdapter = BluetoothAdapter.getDefaultAdapter();
  }

  /**
   * 
   * Nachricht senden, dass ein Gerät verbunden wurde
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.03.2013
   * @param mmSocket2
   * @param mmDevice2
   */
  private void deviceConnected( BluetoothSocket socket, BluetoothDevice device )
  {
    Log.v( TAG, "connected()..." );
    // den Verbindunsthread stoppen, seine Aufgabe ist erfüllt
    if( mConnectThread != null )
    {
      Log.v( TAG, "stop old mConnectThread..." );
      // mConnectThread.cancel();
      mConnectThread = null;
    }
    // Falls da noch verbundene Thread sind, stoppe diese
    if( mReaderThread != null )
    {
      Log.v( TAG, "stop old mReaderThread..." );
      mReaderThread.cancel();
      mReaderThread = null;
    }
    if( mWriterThread != null )
    {
      Log.v( TAG, "stop old mWriterThread..." );
      mWriterThread.cancel();
      mWriterThread = null;
    }
    // starte den Lesethread zur Bearbeitung der Daten vom SPX
    Log.v( TAG, "create mReaderThread..." );
    mReaderThread = new ReaderThread( socket );
    Log.v( TAG, "start mReaderThread..." );
    mReaderThread.start();
    // starte den Schreibhread zur Bearbeitung der Kommandos zum SPX
    Log.v( TAG, "create mWriterThread..." );
    mWriterThread = new WriterThread( socket );
    Log.v( TAG, "start mWriterThread..." );
    mWriterThread.start();
    Log.v( TAG, "call setState" );
    setState( ProjectConst.CONN_STATE_CONNECTED );
  }

  /**
   * 
   * Zeigt Notification in der Statuszeile an!
   * 
   * Project: BtServiceVersuch Package: de.dmarcini.android.btservive
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 21.02.2013
   */
  private void showNotification( CharSequence head, CharSequence msg )
  {
    // Icon Titel und Inhalt anzeigen, Intent beim Anckickcne setzen
    PendingIntent contentIntent = PendingIntent.getActivity( getApplicationContext(), 0, new Intent( getApplicationContext(), areaListActivity.class ),
            PendingIntent.FLAG_UPDATE_CURRENT );
    //@formatter:off
    Notification notification = new Notification.Builder( getBaseContext() )
                                  .setContentTitle( head )
                                  .setContentText( msg )
                                  .setSmallIcon( R.drawable.bluetooth_icon_color )
                                  .setTicker( getText( R.string.notify_service_ticker ) )
                                  .setContentIntent(contentIntent)
                                  .getNotification();
    //@formatter:on
    // Send the notification.
    nm.notify( NOTIFICATION, notification );
  }

  @Override
  public int onStartCommand( Intent intent, int flags, int startId )
  {
    Log.i( TAG, "Received start id " + startId + ": " + intent );
    return START_STICKY;
  }

  /**
   * 
   * Wenn der Timer ein Ereignis hat
   * 
   * Project: BtServiceVersuch Package: de.dmarcini.android.btservive
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 21.02.2013
   */
  private void onTimerTick()
  {
    if( timeToStopService != 0L )
    {
      if( System.currentTimeMillis() > timeToStopService )
      {
        Log.i( TAG, "Service stopping time to stop after last client..." );
        stopSelf();
      }
    }
    if( System.currentTimeMillis() > tickToCounter )
    {
      // if( BuildConfig.DEBUG ) Log.d( TAG, "Timer :<" + counter + ">" );
      tickToCounter = System.currentTimeMillis() + 2000L;
      try
      {
        counter += incrementby;
        BtServiceMessage msg = new BtServiceMessage( ProjectConst.MESSAGE_TICK );
        sendMessageToApp( msg );
      }
      catch( Throwable t )
      {
        // you should always ultimately catch all exceptions in timer tasks.
        Log.e( TAG, "Timer Tick Failed.", t );
      }
    }
  }

  /**
   * 
   * Eine Nachricht (entkoppelt) zu den Empfängern schicken
   * 
   * Project: BtServiceVersuch Package: de.dmarcini.android.btservive
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 21.02.2013
   * @param msg
   */
  private void sendMessageToApp( BtServiceMessage msg )
  {
    for( int i = mClientHandler.size() - 1; i >= 0; i-- )
    {
      try
      {
        mClientHandler.get( i ).obtainMessage( msg.getId(), msg ).sendToTarget();
      }
      catch( NullPointerException ex )
      {
        // das ging schief, den Clienten NICHT mehr benutzen
        mClientHandler.remove( i );
      }
      catch( Exception ex )
      {
        Log.e( TAG, "error while sendMessageToApp: " + ex.getLocalizedMessage() );
      }
    }
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
    if( timer != null )
    {
      timer.cancel();
    }
    counter = 0;
    nm.cancel( NOTIFICATION ); // Cancel the persistent notification.
    Log.i( TAG, "Service Stopped." );
    isRunning = false;
  }

  /**
   * 
   * Wenn die Verbindung verloren geht
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 13.03.2013
   */
  private void connectionLost()
  {
    connectedDevice = null;
    // connectedDeviceAlias = null;
    setState( ProjectConst.CONN_STATE_NONE );
    BtServiceMessage msg = new BtServiceMessage( ProjectConst.MESSAGE_DISCONNECTED );
    // Melde des Status an die Clienten
    sendMessageToApp( msg );
  }

  /**
   * 
   * Setze den Verbindungsstatus
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.03.2013
   * @param state
   */
  private void setState( int state )
  {
    BtServiceMessage msg;
    //
    mConnectionState = state;
    Log.v( TAG, "setState to <" + mConnectionState + ">  " + state );
    switch ( state )
    {
      default:
      case ProjectConst.CONN_STATE_NONE:
        msg = new BtServiceMessage( ProjectConst.MESSAGE_DISCONNECTED );
        break;
      case ProjectConst.CONN_STATE_CONNECTING:
        msg = new BtServiceMessage( ProjectConst.MESSAGE_CONNECTING );
        break;
      case ProjectConst.CONN_STATE_CONNECTED:
        msg = new BtServiceMessage( ProjectConst.MESSAGE_CONNECTED );
        break;
    }
    // Melde des Status an die Clienten
    sendMessageToApp( msg );
  }

  /**
   * 
   * Wenn die Verbindung nicht geklappt hat
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 04.03.2013
   */
  private void connectionFailed()
  {
    connectedDevice = null;
    // connectedDeviceAlias = null;
    setState( ProjectConst.CONN_STATE_NONE );
    BtServiceMessage msg = new BtServiceMessage( ProjectConst.MESSAGE_DISCONNECTED );
    // Melde des Status an die Clienten
    sendMessageToApp( msg );
    BtServiceMessage msg1 = new BtServiceMessage( ProjectConst.MESSAGE_CONNECTERROR );
    // Melde des Status an die Clienten
    sendMessageToApp( msg1 );
  }

  /**
   * 
   * Verbinde mit einem Gerät
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.03.2013
   * @param addr
   */
  public synchronized void connect( String addr )
  {
    BluetoothDevice device = null;
    Log.v( TAG, "connect to: " + addr );
    connectedDevice = null;
    // connectedDeviceAlias = null;
    if( mAdapter == null )
    {
      Log.e( TAG, "None bt-adapter found!" );
      return;
    }
    device = mAdapter.getRemoteDevice( addr );
    // Thread stoppen, bevor eine Verbindung aufgebaut werden kann
    if( mConnectionState == ProjectConst.CONN_STATE_CONNECTING )
    {
      if( mConnectThread != null )
      {
        mConnectThread.cancel();
        mConnectThread = null;
      }
    }
    // Thread stoppen, bevor eine Verbindung aufgebaut werden kann
    if( mReaderThread != null )
    {
      mReaderThread.cancel();
      mReaderThread = null;
    }
    // Start the thread to connect with the given device
    mConnectThread = new ConnectThread( device );
    connectedDevice = addr;
    mConnectThread.start();
    setState( ProjectConst.CONN_STATE_CONNECTING );
  }

  /**
   * 
   * Verbindung zum Gerät trennen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.03.2013
   */
  public void disconnect()
  {
    Log.v( TAG, "stopping bt-connection" );
    if( mConnectThread != null )
    {
      mConnectThread.cancel();
      mConnectThread = null;
    }
    if( mReaderThread != null )
    {
      mReaderThread.cancel();
      mReaderThread = null;
    }
    if( mWriterThread != null )
    {
      mWriterThread.cancel();
      mWriterThread = null;
    }
    connectedDevice = null;
    // connectedDeviceAlias = null;
    setState( ProjectConst.CONN_STATE_NONE );
  }

  /**
   * 
   * Welcher Verbindunsstatus hat die BT Schnittstelle?
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 04.03.2013
   * @return verbindungsstatus
   */
  public int getConnectionState()
  {
    return( mConnectionState );
  }

  /**
   * 
   * Mit welchem Gerät (Addrese) bin ich verbunden?
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 28.05.2013
   * @return TODO
   */
  public String getConnectedDevice()
  {
    if( mConnectionState == ProjectConst.CONN_STATE_CONNECTED )
    {
      if( connectedDevice != null )
      {
        return( connectedDevice );
      }
    }
    return( null );
  }

  /**
   * 
   * Schreibe Daten zum SPX
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 28.05.2013
   * @param msg
   */
  public synchronized void writeToDevice( String msg )
  {
    if( mConnectionState == ProjectConst.CONN_STATE_CONNECTED && mWriterThread != null )
    {
      mWriterThread.writeToDevice( msg );
    }
  }

  /**
   * 
   * Frage den SPX nach der Seriennummer
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 28.05.2013
   */
  public void askForSerialNumber()
  {
    this.writeToDevice( String.format( "%s~%x%s", ProjectConst.STX, ProjectConst.SPX_SERIAL_NUMBER, ProjectConst.ETX ) );
  }

  /**
   * 
   * Frag den SPX, ob er noch da ist
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 28.05.2013
   */
  public void askForSPXAlive()
  {
    this.writeToDevice( String.format( "%s~%x%s", ProjectConst.STX, ProjectConst.SPX_ALIVE, ProjectConst.ETX ) );
  }
}
