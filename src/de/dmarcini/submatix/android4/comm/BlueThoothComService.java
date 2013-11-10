package de.dmarcini.submatix.android4.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.regex.Pattern;

import org.joda.time.DateTime;

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
import de.dmarcini.submatix.android4.exceptions.FirmwareNotSupportetException;
import de.dmarcini.submatix.android4.gui.AreaListActivity;
import de.dmarcini.submatix.android4.utils.GasUpdateEntity;
import de.dmarcini.submatix.android4.utils.ProjectConst;
import de.dmarcini.submatix.android4.utils.SPX42Config;
import de.dmarcini.submatix.android4.utils.SPX42GasParms;

// @formatter:off
public class BlueThoothComService extends Service
{
  // @formatter:on
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
      if( BuildConfig.DEBUG )
      {
        Log.d( TAGCON, "createRfCommSocketToServiceRecord(" + ProjectConst.SERIAL_DEVICE_UUID + ")" );
      }
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
      if( BuildConfig.DEBUG )
      {
        Log.d( TAGCON, "run connected()" );
      }
      deviceConnected( mmSocket, mmDevice );
    }
  }

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

    public void registerServiceHandler( Handler mHandler )
    {
      Log.i( TAG, "Client register" );
      mClientHandler.add( mHandler );
      // isRunning = true;
      timeToStopService = 0L;
      // gibt der Activity gleich den Status
      setState( mConnectionState );
    }

    public void unregisterServiceHandler( Handler mHandler )
    {
      // if( mIsBusy ) return( null );
      Log.i( TAG, "Client unregister" );
      mClientHandler.remove( mHandler );
      if( mClientHandler.isEmpty() )
      {
        Log.i( TAG, "last Client ist removed..." );
        // isRunning = false;
        // zeit bis zum Ende des Service setzen
        timeToStopService = System.currentTimeMillis() + msToEndService;
      }
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
    private Boolean               cancelThread = false;
    private final byte[]          buffer       = new byte[1024];
    private final StringBuffer    mInStrBuffer = new StringBuffer( 1024 );

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
      if( BuildConfig.DEBUG )
      {
        Log.d( TAGREADER, "create ReaderThread" );
      }
      mmSocket = socket;
      InputStream tmpIn = null;
      cancelThread = false;
      // die BluetoothSocket input and output streams erstellen
      try
      {
        tmpIn = socket.getInputStream();
      }
      catch( IOException e )
      {
        Log.e( TAGREADER, "temp sockets not created", e );
      }
      mmInStream = tmpIn;
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
        if( mmSocket != null && mmSocket.isConnected() )
        {
          mmSocket.close();
        }
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
      String[] fields;
      //
      if( BuildConfig.DEBUG ) Log.d( TAGREADER, "execLogentryCmd..." );
      lstart = mInStrBuffer.indexOf( ProjectConst.STX );
      lend = mInStrBuffer.indexOf( ProjectConst.ETX );
      if( lstart > -1 && lend > lstart )
      {
        // ups, hier ist ein "normales" Kommando verpackt
        if( BuildConfig.DEBUG ) Log.d( TAGREADER, "oops, normalCmd found.... change to execNormalCmd..." );
        isLogentryMode = false;
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
      // lese das Ding ohne den Schmandzius der Füller
      readMessage = mInStrBuffer.substring( ProjectConst.FILLER.length(), end );
      // lösche das schon mal raus...
      mInStrBuffer = mInStrBuffer.delete( 0, end );
      readMessage = readMessage.replaceAll( ProjectConst.FILLERCHAR, "" );
      // Splitte das auf in Felder
      fields = fieldPatternTab.split( readMessage );
      // Sende an Activity
      if( BuildConfig.DEBUG ) Log.d( TAGREADER, "Logline Recived <" + readMessage.substring( 10 ).replaceAll( "\t", " " ) + "...>" );
      BtServiceMessage msg = new BtServiceMessage( ProjectConst.MESSAGE_LOGENTRY_LINE, fields );
      sendMessageToApp( msg );
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
      String[] fields;
      int command;
      BtServiceMessage msg;
      if( BuildConfig.DEBUG )
      {
        Log.d( TAGREADER, "execNormalCmd..." );
      }
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
      // lösche das schon mal aus dem Puffer raus!
      mInStrBuffer = mInStrBuffer.delete( 0, end + 1 );
      if( BuildConfig.DEBUG )
      {
        Log.d( TAGREADER, "normal Message Recived <" + readMessage + ">" );
      }
      // Trenne die Parameter voneinander, fields[0] ist dann das Kommando
      fields = fieldPatternDp.split( readMessage );
      //
      //
      //
      if( 0 == readMessage.indexOf( ProjectConst.IS_END_LOGLISTENTRY ) )
      {
        // Logbucheinträge fertig gelesen
        msg = new BtServiceMessage( ProjectConst.MESSAGE_DIRENTRY_END );
        Log.v( TAGREADER, "SPX Logdir end readet!" );
        sendMessageToApp( msg );
        return;
      }
      //
      // Messages für die Weiterverarbeitung präparieren
      //
      fields[0] = fields[0].replaceFirst( "~", "" );
      try
      {
        command = Integer.parseInt( fields[0], 16 );
      }
      catch( NumberFormatException ex )
      {
        Log.e( TAGREADER, "Convert String to Int (" + ex.getLocalizedMessage() + ")" );
        return;
      }
      //
      // bekomme heraus, welcher Art die ankommende Message ist
      //
      switch ( command )
      {
        case ProjectConst.SPX_MANUFACTURERS:
          // Sende Nachricht Gerätename empfangen!
          connectedDeviceManufacturer = fields[1];
          msg = new BtServiceMessage( ProjectConst.MESSAGE_MANUFACTURER_READ, new String( fields[1] ) );
          sendMessageToApp( msg );
          if( BuildConfig.DEBUG ) Log.d( TAGREADER, "SPX Devicename recived! <" + fields[1] + ">" );
          break;
        case ProjectConst.SPX_ALIVE:
          // Ackuspannung übertragen
          msg = new BtServiceMessage( ProjectConst.MESSAGE_SPXALIVE, new String( fields[1] ) );
          sendMessageToApp( msg );
          if( BuildConfig.DEBUG ) Log.d( TAGREADER, "SPX is Alive, Acku value recived." );
          break;
        case ProjectConst.SPX_APPLICATION_ID:
          // Sende Nachricht Firmwareversion empfangen!
          msg = new BtServiceMessage( ProjectConst.MESSAGE_FWVERSION_READ, new String( fields[1] ) );
          sendMessageToApp( msg );
          if( BuildConfig.DEBUG ) Log.d( TAGREADER, "Application ID (Firmware Version)  recived! <" + fields[1] + ">" );
          break;
        case ProjectConst.SPX_SERIAL_NUMBER:
          // Sende Nachricht Seriennummer empfangen!
          connectedDeviceSerialNumber = new String( fields[1] );
          msg = new BtServiceMessage( ProjectConst.MESSAGE_SERIAL_READ, new String( fields[1] ) );
          sendMessageToApp( msg );
          if( BuildConfig.DEBUG ) Log.d( TAGREADER, "Serial Number recived! <" + fields[1] + ">" );
          break;
        case ProjectConst.SPX_SET_SETUP_DEKO:
          // Quittung für Setze DECO
          msg = new BtServiceMessage( ProjectConst.MESSAGE_DECO_ACK );
          sendMessageToApp( msg );
          if( BuildConfig.DEBUG ) Log.d( TAGREADER, "SPX_SET_SETUP_DEKO Acknoweledge recived" );
          break;
        case ProjectConst.SPX_SET_SETUP_SETPOINT:
          // Quittung für Setzen der Auto-Setpointeinstelungen
          msg = new BtServiceMessage( ProjectConst.MESSAGE_SETPOINT_ACK );
          sendMessageToApp( msg );
          if( BuildConfig.DEBUG ) Log.d( TAGREADER, "SPX_SET_SETUP_SETPOINT Acknoweledge recived " );
          break;
        case ProjectConst.SPX_SET_SETUP_DISPLAYSETTINGS:
          // Quittung für Setzen der Displayeinstellungen
          msg = new BtServiceMessage( ProjectConst.MESSAGE_DISPLAY_ACK );
          sendMessageToApp( msg );
          if( BuildConfig.DEBUG ) Log.d( TAGREADER, "MESSAGE_DISPLAY_ACK recived " );
          break;
        case ProjectConst.SPX_SET_SETUP_UNITS:
          // Quittung für das Setzen der Masseinheiten
          msg = new BtServiceMessage( ProjectConst.MESSAGE_UNITS_ACK );
          sendMessageToApp( msg );
          if( BuildConfig.DEBUG ) Log.d( TAGREADER, "MESSAGE_UNITS_ACK recived " );
          break;
        case ProjectConst.SPX_SET_SETUP_INDIVIDUAL:
          // Quittung für Individualeinstellungen
          msg = new BtServiceMessage( ProjectConst.MESSAGE_INDIVID_ACK );
          sendMessageToApp( msg );
          if( BuildConfig.DEBUG ) Log.d( TAGREADER, "MESSAGE_INDIVID_ACK recived " );
          break;
        case ProjectConst.SPX_GET_SETUP_DEKO:
          // Kommando DEC liefert zurück:
          // ~34:LL:HH:D:Y:C
          // LL=GF-Low, HH=GF-High,
          // D=Deepstops (0/1)
          // Y=Dynamische Gradienten (0/1)
          // C=Last Decostop (0=3 Meter/1=6 Meter)
          msg = new BtServiceMessage( ProjectConst.MESSAGE_DECO_READ, new String[]
          { fields[1], fields[2], fields[3], fields[4], fields[5] } );
          sendMessageToApp( msg );
          if( BuildConfig.DEBUG ) Log.d( TAGREADER, "MESSAGE_DECO_READ recived " );
          break;
        case ProjectConst.SPX_GET_SETUP_SETPOINT:
          // Kommando GET_SETUP_SETPOINT liefert
          // ~35:A:P
          // A = Setpoint bei (0,1,2,3) = (0,5,15,20)
          // P = Partialdruck (0..4) 1.0 .. 1.4
          msg = new BtServiceMessage( ProjectConst.MESSAGE_SETPOINT_READ, new String[]
          { fields[1], fields[2] } );
          sendMessageToApp( msg );
          if( BuildConfig.DEBUG ) Log.d( TAGREADER, "Setpoint recived!" );
          break;
        case ProjectConst.SPX_GET_SETUP_DISPLAYSETTINGS:
          // Kommando GET_SETUP_DISPLAYSETTINGS liefert
          // ~36:D:A
          // D= 0->10&, 1->50%, 2->100%
          // A= 0->Landscape 1->180Grad
          msg = new BtServiceMessage( ProjectConst.MESSAGE_DISPLAY_READ, new String[]
          { fields[1], fields[2] } );
          sendMessageToApp( msg );
          if( BuildConfig.DEBUG ) Log.d( TAGREADER, "Display settings recived!" );
          break;
        case ProjectConst.SPX_GET_SETUP_UNITS:
          // Kommando GET_SETUP_UNITS
          // ~37:UD:UL:UW
          // UD= Fahrenheit/Celsius => immer 0 in der Firmware 2.6.7.7_U
          // UL= 0=metrisch 1=imperial
          // UW= 0->Salzwasser 1->Süßwasser
          msg = new BtServiceMessage( ProjectConst.MESSAGE_UNITS_READ, new String[]
          { fields[1], fields[2], fields[3] } );
          sendMessageToApp( msg );
          if( BuildConfig.DEBUG ) Log.d( TAGREADER, "Device Units recived!" );
          break;
        case ProjectConst.SPX_GET_SETUP_INDIVIDUAL:
          // Kommando GET_SETUP_INDIVIDUAL liefert
          // ~38:SE:PS:SC:SN:LI
          // SE: Sensors 0->ON 1->OFF
          // PS: PSCRMODE 0->OFF 1->ON
          // SC: SensorCount
          // SN: Sound 0->OFF 1->ON
          // LI: Loginterval 0->10sec 1->30Sec 2->60 Sec
          if( fields.length == 6 )
          {
            msg = new BtServiceMessage( ProjectConst.MESSAGE_INDIVID_READ, new String[]
            { fields[1], fields[2], fields[3], fields[4], fields[5] } );
            sendMessageToApp( msg );
            if( BuildConfig.DEBUG ) Log.d( TAGREADER, "Device individual settings recived!" );
          }
          else
          {
            if( BuildConfig.DEBUG ) Log.d( TAGREADER, "no individual license...!" );
          }
          break;
        case ProjectConst.SPX_GET_SETUP_GASLIST:
          // Kommando GET_SETUP_GASLIST
          // ~39:NR:ST:HE:BA:AA:CG
          // NR: Numer des Gases
          // ST Stickstoff in Prozent (hex)
          // HELIUM
          // Bailout
          // AA Diluent 1 oder 2 oder keins
          // CG curent Gas
          msg = new BtServiceMessage( ProjectConst.MESSAGE_GAS_READ, new String[]
          { fields[1], fields[2], fields[3], fields[4], fields[5], fields[6] } );
          sendMessageToApp( msg );
          if( BuildConfig.DEBUG ) Log.d( TAGREADER, "Gas setup recived!" );
          break;
        case ProjectConst.SPX_SET_SETUP_GASLIST:
          // Besaetigung fuer Gas setzen bekommen
          if( BuildConfig.DEBUG ) Log.d( TAGREADER, "MESSAGE_GAS_ACK recived " );
          msg = new BtServiceMessage( ProjectConst.MESSAGE_GAS_ACK );
          sendMessageToApp( msg );
          break;
        case ProjectConst.SPX_GET_LOG_INDEX:
          // Ein Logbuch Verzeichniseintrag gefunden
          // ~41:21:9_4_10_20_44_55.txt:22
          // ~41:NR:FN:MAX
          // NR: Nummer des Eintrages
          // FN: Filename
          // MAX: letzter Eintrag
          if( BuildConfig.DEBUG ) Log.d( TAGREADER, "MESSAGE_GET_LOG_INDEX recived " );
          msg = new BtServiceMessage( ProjectConst.MESSAGE_DIRENTRY_READ, new String[]
          { fields[1], fields[2], fields[3] } );
          sendMessageToApp( msg );
          //
          // war das der lezte Eintrag?
          //
          if( fields[1].equals( fields[3] ) )
          {
            if( BuildConfig.DEBUG ) Log.d( TAGREADER, "END OF DIRINDEX recived " );
            msg = new BtServiceMessage( ProjectConst.MESSAGE_DIRENTRY_END );
            sendMessageToApp( msg );
          }
          break;
        case ProjectConst.SPX_GET_LOG_NUMBER_SE:
          if( 0 == fields[1].indexOf( "1" ) )
          {
            // Übertragung Logfile gestartet
            if( BuildConfig.DEBUG ) Log.d( TAGREADER, "start of logfile recived " );
            msg = new BtServiceMessage( ProjectConst.MESSAGE_LOGENTRY_START, new String( fields[2] ) );
            sendMessageToApp( msg );
            isLogentryMode = true;
          }
          else if( 0 == fields[1].indexOf( "0" ) )
          {
            {
              // Übertragung beendet
              if( BuildConfig.DEBUG ) Log.d( TAGREADER, "stop of logfile recived " );
              msg = new BtServiceMessage( ProjectConst.MESSAGE_LOGENTRY_STOP, new String( fields[2] ) );
              sendMessageToApp( msg );
              isLogentryMode = false;
            }
          }
          break;
        case ProjectConst.SPX_GET_DEVICE_OFF:
          // SPX meldet, er geht aus dem Sync-Mode
          msg = new BtServiceMessage( ProjectConst.MESSAGE_DISCONNECTED );
          sendMessageToApp( msg );
          if( BuildConfig.DEBUG ) Log.d( TAGREADER, "SPX42 switch syncmode OFF! Connection will failure!" );
          break;
        case ProjectConst.SPX_LICENSE_STATE:
          // Kommando SPX_LICENSE_STATE
          // <~45:LS:CE>
          // LS : License State 0=Nitrox,1=Normoxic Trimix,2=Full Trimix
          // CE : Custom Enabled 0= disabled, 1=enabled
          connectedDeviceLicense = new String[]
          { fields[1], fields[2] };
          msg = new BtServiceMessage( ProjectConst.MESSAGE_LICENSE_STATE_READ, connectedDeviceLicense );
          sendMessageToApp( msg );
          if( BuildConfig.DEBUG ) Log.d( TAGREADER, "SPX42 license state recived!" );
          break;
        default:
          Log.w( TAGREADER, "unknown Messagetype recived <" + readMessage + ">" );
      }
    }

    /**
     * Hier nimmt der Thread seinen Lauf
     */
    @Override
    public void run()
    {
      Log.i( TAGREADER, "BEGIN ReaderThread" );
      String readMessage;
      int bytes, start, end, lstart, lend;
      boolean logCmd, normalCmd;
      // den Inputstream solange lesen, wie die Verbindung besteht
      cancelThread = false;
      while( !cancelThread )
      {
        // lese von InputStream, maximal buffer.length bytes lesen
        try
        {
          bytes = mmInStream.read( buffer );
          if( bytes == -1 )
          {
            // Verbindung beendet/verloren
            cancelThread = true;
            Log.e( TAGREADER, "reader connection lost..." );
            connectionLost();
            cancel();
            break;
          }
          readMessage = new String( buffer, 0, bytes );
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
        //
        // was mach ich jetzt mit dem empfangenen Zeuch?
        //
        // reicht der Platz noch?
        if( ( mInStrBuffer.capacity() + readMessage.length() ) > ProjectConst.MAXINBUFFER )
        {
          Log.e( TAGREADER, "INPUT BUFFER OVERFLOW!" );
          connectionLost();
          cancel();
          break;
        }
        // Die empfangene Nachricht an den Puffer anhängen
        mInStrBuffer.append( readMessage );
        // den Puffer in einen String überführen
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
          {
            normalCmd = true;
          }
          else
          {
            normalCmd = false;
          }
          if( ( lstart > -1 ) && ( lend > lstart ) )
          {
            logCmd = true;
          }
          else
          {
            logCmd = false;
          }
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
      Log.i( TAGREADER, "END ReaderThread" );
      connectionLost();
    }
  }

  /**
   * 
   * Thread zum schreiben von Kommandos und daten an den angeschlossenen SPX
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 10.11.2013
   */
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
      if( BuildConfig.DEBUG )
      {
        Log.d( TAGWRITER, "create WriterThread" );
      }
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
        if( mmSocket != null && mmSocket.isConnected() )
        {
          mmSocket.close();
        }
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
      BtServiceMessage msg;
      //
      Log.i( TAGWRITER, "BEGIN WriterThread" );
      // den Inputstram solange schreiben, wie die Verbindung besteht
      cancelThread = false;
      while( !cancelThread )
      {
        // syncronisiete Methode aufrufen, damit wait und notify machbar sind
        synchronized( this )
        {
          if( writeList.isEmpty() )
          {
            try
            {
              wait( 15 );
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
              writeWatchDog = ProjectConst.WATCHDOG_FOR_WRITEOPS;
              // den String für den Wachhund zwischenspeichern
              sendStr = writeList.remove( 0 );
              // also den String Eintrag in den Outstream...
              mmOutStream.write( sendStr.getBytes() );
              // kommt das an, den Watchog wieder AUS
              sendStr = null;
              writeWatchDog = -1;
              // zwischen den Kommandos etwas warten, der SPX braucht etwas bis er wieder zuhört...
              Thread.sleep( 500 );
            }
            catch( IndexOutOfBoundsException ex )
            {
              Log.e( TAG, "WriterThread IndexOutOfBoundsException: <" + ex.getLocalizedMessage() + ">" );
              msg = new BtServiceMessage( ProjectConst.MESSAGE_DISCONNECTED );
              sendMessageToApp( msg );
              cancelThread = true;
              writeList.clear();
              return;
            }
            catch( IOException ex )
            {
              Log.e( TAG, "WriterThread Exception: <" + ex.getLocalizedMessage() + ">" );
              msg = new BtServiceMessage( ProjectConst.MESSAGE_DISCONNECTED );
              sendMessageToApp( msg );
              cancelThread = true;
              writeList.clear();
              return;
            }
            catch( InterruptedException ex )
            {}
          }
        }
      }
      Log.i( TAGWRITER, "END WriterThread" );
      connectionLost();
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

  private static final String  TAG                         = BlueThoothComService.class.getSimpleName();
  private static final long    msToEndService              = 6000L;
  // private static final Pattern fieldPattern0x09 = Pattern.compile( ProjectConst.LOGSELECTOR );
  private static final Pattern fieldPatternDp              = Pattern.compile( ":" );
  private static final Pattern fieldPatternTab             = Pattern.compile( "\t" );
  private long                 tickToCounter               = 0L;
  private long                 timeToStopService           = 0L;
  private NotificationManager  nm;
  static int                   NOTIFICATION                = 815;
  private final Timer          timerThread                 = new Timer();
  private long                 timerCounter                = 0;
  private int                  writeWatchDog               = -1;
  private String               sendStr                     = null;                                      // Zwischenspeicher für zu sendendes Kommando (für watchdog)
  ArrayList<Handler>           mClientHandler              = new ArrayList<Handler>();                  // Messagehandler für Clienten
  int                          mValue                      = 0;                                         // Holds last value set by a client.
  private final IBinder        mBinder                     = new LocalBinder();
  private BluetoothAdapter     mAdapter                    = null;
  private static ConnectThread mConnectThread              = null;
  private static ReaderThread  mReaderThread               = null;
  private static WriterThread  mWriterThread               = null;
  private static volatile int  mConnectionState;
  private volatile boolean     isLogentryMode              = false;
  private String               connectedDevice             = null;
  private String               connectedDeviceSerialNumber = null;
  private String               connectedDeviceManufacturer = null;
  private String               connectedDeviceFWVersion    = null;
  private String[]             connectedDeviceLicense      = null;

  /**
   * 
   * Lese die Konfiguration vom SPX42
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.06.2013
   */
  public void askForConfigFromSPX42()
  {
    String kdoString;
    kdoString = String.format( "%s~%x~%x~%x~%x~%x~%x~%x%s", ProjectConst.STX, ProjectConst.SPX_GET_SETUP_DEKO, ProjectConst.SPX_GET_SETUP_SETPOINT,
            ProjectConst.SPX_GET_SETUP_DISPLAYSETTINGS, ProjectConst.SPX_GET_SETUP_UNITS, ProjectConst.SPX_GET_SETUP_INDIVIDUAL, ProjectConst.SPX_LICENSE_STATE,
            ProjectConst.SPX_ALIVE, ProjectConst.ETX );
    if( BuildConfig.DEBUG )
    {
      Log.d( TAG, "askForConfigFromSPX42()...send <" + kdoString + ">" );
    }
    this.writeToDevice( kdoString );
  }

  /**
   * 
   * Frage nach den Masseinheiten
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 15.08.2013
   */
  public void aksForUnitsFromSPX42()
  {
    String kdoString;
    kdoString = String.format( "%s~%x%s", ProjectConst.STX, ProjectConst.SPX_GET_SETUP_UNITS, ProjectConst.ETX );
    if( BuildConfig.DEBUG )
    {
      Log.d( TAG, "aksForUnitsFromSPX42()...send <" + kdoString + ">" );
    }
    this.writeToDevice( kdoString );
  }

  /**
   * 
   * Frage nach der Konfiguration der DECO-Parameter
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 13.07.2013
   */
  public void askForDecoConfig()
  {
    String kdoString;
    kdoString = String.format( "%s~%x~%x%s", ProjectConst.STX, ProjectConst.SPX_GET_SETUP_DEKO, ProjectConst.SPX_ALIVE, ProjectConst.ETX );
    if( BuildConfig.DEBUG )
    {
      Log.d( TAG, "askForDecoConfig()...send <" + kdoString + ">" );
    }
    this.writeToDevice( kdoString );
  }

  /**
   * 
   * frag den SPX nach seiener Firmware
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.06.2013
   * 
   */
  public void askForFirmwareVersion()
  {
    if( BuildConfig.DEBUG )
    {
      Log.d( TAG, "askForSerialNumber..." );
    }
    if( connectedDeviceFWVersion != null )
    {
      BtServiceMessage msg = new BtServiceMessage( ProjectConst.MESSAGE_FWVERSION_READ, new String( connectedDeviceFWVersion ) );
      sendMessageToApp( msg );
    }
    else
    {
      this.writeSPXMsgToDevice( String.format( "~%x", ProjectConst.SPX_APPLICATION_ID ) );
    }
  }

  /**
   * 
   * Erfrage beim SPX die Gaslisten
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 18.07.2013 TODO
   */
  public void askForGasFromSPX()
  {
    String kdoString;
    kdoString = String.format( "%s~%x~%x%s", ProjectConst.STX, ProjectConst.SPX_GET_SETUP_GASLIST, ProjectConst.SPX_ALIVE, ProjectConst.ETX );
    if( BuildConfig.DEBUG ) Log.d( TAG, "askForGasFromSPX: sending <" + kdoString + ">" );
    this.writeSPXMsgToDevice( kdoString );
  }

  /**
   * 
   * Erfrage die Lizenz vom SPX
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 04.06.2013
   */
  public void askForLicenseFromSPX()
  {
    if( BuildConfig.DEBUG )
    {
      Log.d( TAG, "askForLicenseFromSPX..." );
    }
    if( connectedDeviceLicense != null )
    {
      BtServiceMessage msg = new BtServiceMessage( ProjectConst.MESSAGE_LICENSE_STATE_READ, connectedDeviceLicense );
      sendMessageToApp( msg );
    }
    else
    {
      this.writeSPXMsgToDevice( String.format( "~%x", ProjectConst.SPX_LICENSE_STATE ) );
    }
  }

  /**
   * 
   * Erfrage den Herstellerstring
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 04.06.2013
   */
  public void askForManufacturer()
  {
    if( BuildConfig.DEBUG )
    {
      Log.d( TAG, "askForLicenseFromSPX..." );
    }
    //
    // wenn der schon mal abgefragt wurde, kann ich das "cachen"
    //
    if( connectedDeviceManufacturer != null )
    {
      BtServiceMessage msg = new BtServiceMessage( ProjectConst.MESSAGE_MANUFACTURER_READ, new String( connectedDeviceManufacturer ) );
      sendMessageToApp( msg );
    }
    else
    {
      this.writeSPXMsgToDevice( String.format( "~%x", ProjectConst.SPX_MANUFACTURERS ) );
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
    if( BuildConfig.DEBUG )
    {
      Log.d( TAG, "askForSerialNumber..." );
    }
    if( connectedDeviceSerialNumber != null )
    {
      BtServiceMessage msg = new BtServiceMessage( ProjectConst.MESSAGE_SERIAL_READ, new String( connectedDeviceSerialNumber ) );
      sendMessageToApp( msg );
    }
    else
    {
      this.writeSPXMsgToDevice( String.format( "~%x", ProjectConst.SPX_SERIAL_NUMBER ) );
    }
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
    if( BuildConfig.DEBUG )
    {
      Log.d( TAG, "askForSPXAlive..." );
    }
    this.writeSPXMsgToDevice( String.format( "~%x", ProjectConst.SPX_ALIVE ) );
  }

  /**
   * 
   * Rufe vom SPX das Logverzeichnis auf
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 06.08.2013
   */
  public void askForLogDirectoryFromSPX()
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "askForLogDirectoryFromSPX..." );
    this.writeSPXMsgToDevice( String.format( "~%x", ProjectConst.SPX_GET_LOG_INDEX ) );
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
    connectedDeviceSerialNumber = null;
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
    // connectedDeviceAlias = null;
    setState( ProjectConst.CONN_STATE_NONE );
    BtServiceMessage msg = new BtServiceMessage( ProjectConst.MESSAGE_DISCONNECTED );
    // Melde des Status an die Clienten
    sendMessageToApp( msg );
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
      if( BuildConfig.DEBUG ) Log.d( TAG, "stop old mConnectThread..." );
      // mConnectThread.cancel();
      mConnectThread = null;
    }
    // Falls da noch verbundene Thread sind, stoppe diese
    if( mReaderThread != null )
    {
      if( BuildConfig.DEBUG ) Log.d( TAG, "stop old mReaderThread..." );
      mReaderThread.cancel();
      mReaderThread = null;
    }
    if( mWriterThread != null )
    {
      if( BuildConfig.DEBUG ) Log.d( TAG, "stop old mWriterThread..." );
      mWriterThread.cancel();
      mWriterThread = null;
    }
    // starte den Lesethread zur Bearbeitung der Daten vom SPX
    if( BuildConfig.DEBUG ) Log.d( TAG, "create mReaderThread..." );
    mReaderThread = new ReaderThread( socket );
    if( BuildConfig.DEBUG ) Log.d( TAG, "start mReaderThread..." );
    mReaderThread.start();
    // starte den Schreibhread zur Bearbeitung der Kommandos zum SPX
    if( BuildConfig.DEBUG ) Log.d( TAG, "create mWriterThread..." );
    mWriterThread = new WriterThread( socket );
    if( BuildConfig.DEBUG ) Log.d( TAG, "start mWriterThread..." );
    mWriterThread.start();
    if( BuildConfig.DEBUG ) Log.d( TAG, "call setState" );
    setState( ProjectConst.CONN_STATE_CONNECTED );
    timerCounter = System.currentTimeMillis() + 100;
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
    setState( ProjectConst.CONN_STATE_NONE );
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
   * @return status
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
   * Gib die Seriennummer des Gerätes zurück (wenn vorhanden)
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.06.2013
   * @return geräte Seriennummer
   */
  public synchronized String getConnectedDeviceSerialNumber()
  {
    return( connectedDeviceSerialNumber );
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
   * Wird immer beim Binden eines Clienten aufgerufen
   */
  @Override
  public IBinder onBind( Intent intent )
  {
    if( BuildConfig.DEBUG )
    {
      Log.d( TAG, "onBind..." );
    }
    showNotification( getText( R.string.notify_service ), getText( R.string.notify_service_connected ) );
    return mBinder;
  }

  @Override
  public void onCreate()
  {
    super.onCreate();
    timeToStopService = 0L;
    nm = ( NotificationManager )getSystemService( NOTIFICATION_SERVICE );
    Log.i( TAG, "Service Started." );
    // isRunning = true;
    // der Überwachungsthread läuft solange der Service aktiv ist aller 1 Sekunde
    timerThread.scheduleAtFixedRate( new TimerTask() {
      @Override
      public void run()
      {
        onTimerTick();
      }
    }, 100, 1000L );
    // Service ist Erzeugt!
    showNotification( getText( R.string.notify_service ), getText( R.string.notify_service_started ) );
    mAdapter = BluetoothAdapter.getDefaultAdapter();
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
    if( timerThread != null )
    {
      timerThread.cancel();
    }
    nm.cancel( NOTIFICATION ); // Cancel the persistent notification.
    Log.i( TAG, "Service Stopped." );
    // isRunning = false;
  }

  @Override
  public int onStartCommand( Intent intent, int flags, int startId )
  {
    Log.i( TAG, "Received start id " + startId + ": " + intent );
    return START_STICKY;
  }

  /**
   * 
   * Wenn der Timer ein Ereignis hat (alle 1000 ms)
   * 
   * Project: BtServiceVersuch Package: de.dmarcini.android.btservive
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 21.02.2013
   */
  private void onTimerTick()
  {
    BtServiceMessage msg;
    //
    // Timeout zum beenden des Service runterzählen
    //
    if( timeToStopService != 0L )
    {
      if( System.currentTimeMillis() > timeToStopService )
      {
        Log.i( TAG, "Service stopping time to stop after last client..." );
        stopSelf();
      }
    }
    //
    // eine Tickernachricht in die gegend senden
    //
    if( System.currentTimeMillis() > tickToCounter )
    {
      tickToCounter = System.currentTimeMillis() + 2000L;
      try
      {
        msg = new BtServiceMessage( ProjectConst.MESSAGE_TICK );
        sendMessageToApp( msg );
      }
      catch( Throwable t )
      {
        // you should always ultimately catch all exceptions in timerThread tasks.
        Log.e( TAG, "Timer Tick Failed.", t );
      }
    }
    //
    // wenn ein Gerät verbunden ist einige Sachen durchführen
    //
    if( mConnectionState == ProjectConst.CONN_STATE_CONNECTED )
    {
      //
      // ist ein 90 Sekunden Intervall vergangen?
      //
      if( timerCounter <= System.currentTimeMillis() )
      {
        askForSPXAlive();
        timerCounter = System.currentTimeMillis() + 90000L;
      }
      //
      // soll der Soft-Watchdog aktiv sein?
      //
      if( writeWatchDog > -1 )
      {
        // ist der Timout aubgelaufen?
        if( writeWatchDog == 0 )
        {
          // ein Wachhund ist abgelaufen, benachrichtige den User!
          if( sendStr != null )
          {
            msg = new BtServiceMessage( ProjectConst.MESSAGE_COMMTIMEOUT, sendStr );
          }
          else
          {
            msg = new BtServiceMessage( ProjectConst.MESSAGE_COMMTIMEOUT );
          }
          sendMessageToApp( msg );
          writeWatchDog = -1;
        }
        // runterzählen, bei -1 ist eh schluss
        writeWatchDog--;
      }
    }
  }

  @Override
  public boolean onUnbind( Intent intent )
  {
    if( BuildConfig.DEBUG )
    {
      Log.d( TAG, "onUnbind..." );
    }
    showNotification( getText( R.string.notify_service ), getText( R.string.notify_service_disconnected ) );
    return( super.onUnbind( intent ) );
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
        // die Daten löschen
        connectedDevice = null;
        connectedDeviceSerialNumber = null;
        connectedDeviceManufacturer = null;
        connectedDeviceFWVersion = null;
        connectedDeviceLicense = null;
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
    PendingIntent contentIntent = PendingIntent.getActivity( getApplicationContext(), 0, new Intent( getApplicationContext(), AreaListActivity.class ),
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

  /**
   * 
   * Schreibe in den SPX Autosetpoint und Setpoint als Indexwerte 0..3 oder 0..4
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.06.2013
   * @param cf
   * @param auto
   * @param pressure
   * @throws FirmwareNotSupportetException
   */
  public void writeAutoSetpoint( SPX42Config cf, int auto, int pressure ) throws FirmwareNotSupportetException
  {
    String kdoString;
    String debugString;
    //
    if( BuildConfig.DEBUG )
    {
      Log.d( TAG, "write setpoint propertys" );
    }
    if( !cf.isFirmwareSupported() )
    {
      Log.e( TAG, "firmware not supportet for write settings!" );
      throw new FirmwareNotSupportetException( "write Auto Setpoint" );
    }
    if( cf.isOldParamSorting() )
    {
      //
      // Kommando SPX_SET_SETUP_SETPOINT
      // ~30:P:A
      // P = Partialdruck (0..4) 1.0 .. 1.4
      // A = Setpoint bei (0,1,2,3,4) = (0,5,15,20,25)
      kdoString = String.format( "~%x:%x:%x", ProjectConst.SPX_SET_SETUP_SETPOINT, pressure, auto );
      if( BuildConfig.DEBUG )
      {
        debugString = String.format( "OLD-FIRMWARE <~%x:P%x:A%x>", ProjectConst.SPX_SET_SETUP_SETPOINT, pressure, auto );
        Log.d( TAG, "writeAutoSetpoint: sending <" + debugString + ">" );
      }
    }
    else
    {
      // ~30:A:P
      // A = Setpoint bei (0,1,2,3,4) = (0,5,15,20,25)
      // P = Partialdruck (0..4) 1.0 .. 1.4
      kdoString = String.format( "~%x:%x:%x", ProjectConst.SPX_SET_SETUP_SETPOINT, auto, pressure );
      if( BuildConfig.DEBUG )
      {
        debugString = String.format( "NEWER-FIRMWARE <~%x:A%x:P%x>", ProjectConst.SPX_SET_SETUP_SETPOINT, auto, pressure );
        Log.d( TAG, "writeAutoSetpoint: sending <" + debugString + ">" );
      }
    }
    this.writeSPXMsgToDevice( kdoString );
  }

  /**
   * 
   * schreibe DECO Parameter in den SPX
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 13.07.2013
   * @param cf
   * @param lowG
   *          Low gradient
   * @param highG
   *          High Gradient
   * @param deepSt
   *          Deppsstop Enable?
   * @param dynGr
   *          Dyn gradients enable?
   * @param lastStop
   *          last stop 3 oder 6 Meter?
   * @throws FirmwareNotSupportetException
   */
  public void writeDecoPrefs( SPX42Config cf, int lowG, int highG, int deepSt, int dynGr, int lastStop ) throws FirmwareNotSupportetException
  {
    String kdoString;
    //
    if( !cf.isFirmwareSupported() )
    {
      Log.e( TAG, "firmware not supportet for write settings!" );
      throw new FirmwareNotSupportetException( "write deco prefs" );
    }
    if( cf.isOldParamSorting() )
    {
      // ~29:GH:GL:LS:DY:DS
      // GH = Gradient HIGH
      // GL = Gradient LOW
      // LS = Last Stop 0=>3m 1=>6m
      // DY = Dynamische gradienten 0->off 1->on
      // DS = Deepstops 0=> enabled, 1=>disabled
      // kdoString = String.format( "~%x:%x:%x:%x:%x:%x", ProjectConst.SPX_SET_SETUP_DEKO, highG, lowG, ( ( lastStop == 1 ) ? 0 : 1 ), dynGr, deepSt );
      kdoString = String.format( "~%x:%x:%x:%x:%x:%x", ProjectConst.SPX_SET_SETUP_DEKO, highG, lowG, lastStop, dynGr, deepSt );
      if( BuildConfig.DEBUG ) Log.d( TAG, "writeDecoPrefs: sending <OLDER-FIRMWARE <" + kdoString + ">>" );
    }
    else
    {
      // Kommando SPX_SET_SETUP_DEKO
      // ~29:GL:GH:DS:DY:LS
      // GL=GF-Low, GH=GF-High,
      // DS=Deepstops (0/1)
      // DY=Dynamische Gradienten (0/1)
      // LS=Last Decostop (0=3 Meter/1=6 Meter)
      kdoString = String.format( "~%x:%x:%x:%x:%x:%x", ProjectConst.SPX_SET_SETUP_DEKO, lowG, highG, deepSt, dynGr, lastStop );
      if( BuildConfig.DEBUG ) Log.d( TAG, "writeDecoPrefs: sending <NEWER-FIRMWARE <" + kdoString + ">>" );
    }
    this.writeSPXMsgToDevice( kdoString );
  }

  /**
   * 
   * Schreibe Display Eigenschaften zum SPX42
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 14.07.2013
   * @param cf
   * @param lumin
   * @param orient
   * @throws FirmwareNotSupportetException
   */
  public void writeDisplayPrefs( SPX42Config cf, int lumin, int orient ) throws FirmwareNotSupportetException
  {
    String kdoString;
    if( !cf.isFirmwareSupported() )
    {
      Log.e( TAG, "firmware not supportet for write settings!" );
      throw new FirmwareNotSupportetException( "write display prefs" );
    }
    kdoString = String.format( "~%x:%x:%x", ProjectConst.SPX_SET_SETUP_DISPLAYSETTINGS, lumin, orient );
    if( BuildConfig.DEBUG ) Log.d( TAG, "writeDisplayPrefs: sending <" + kdoString + ">" );
    this.writeSPXMsgToDevice( kdoString );
  }

  /**
   * 
   * Schreibe ein Gas in den SPX
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 18.07.2013
   * @param cf
   * @param gasNr
   * @param gasParms
   * @throws FirmwareNotSupportetException
   */
  public void writeGasSetup( SPX42Config cf, int gasNr, SPX42GasParms gasParms ) throws FirmwareNotSupportetException
  {
    String kdoString;
    int diluent;
    //
    if( gasParms.d1 )
    {
      diluent = 1;
    }
    else if( gasParms.d2 )
    {
      diluent = 2;
    }
    else
    {
      diluent = 0;
    }
    if( !cf.isFirmwareSupported() )
    {
      Log.e( TAG, "firmware not supportet for write settings!" );
      throw new FirmwareNotSupportetException( "write gas setup" );
    }
    if( cf.isOldParamSorting() )
    {
      // Kommando SPX_SET_SETUP_GASLIST
      // ~40:NR:HE:N2:BO:DI:CU
      // NR -> Gas Nummer
      // HE -> Heliumanteil
      // N2 -> Stickstoffanteil
      // BO -> Bailoutgas? (3?)
      // DI -> Diluent ( 0, 1 oder 2 )
      // CU Current Gas (0 oder 1)
      kdoString = String.format( "~%x:%x:%x:%x:%x:%x:%x", ProjectConst.SPX_SET_SETUP_GASLIST, gasNr, gasParms.he, gasParms.n2, ( gasParms.bo ? 3 : 0 ), diluent,
              ( gasParms.isCurr ? 1 : 0 ) );
      if( BuildConfig.DEBUG ) Log.d( TAG, "writeDecoPrefs: sending <OLDER-FIRMWARE <" + kdoString + ">>" );
    }
    else
    {
      // Kommando SPX_SET_SETUP_GASLIST
      // ~40:NR:N2:HE:BO:DI:CU
      // NR: Nummer des Gases 0..7
      // N2: Sticksoff in %
      // HE: Heluim in %
      // BO: Bailout (Werte 0,1 und 3 gefunden, 0 kein BO, 3 BO Wert 1 unbekannt?)
      // DI: Diluent 1 oder 2
      // CU: Current Gas
      kdoString = String.format( "~%x:%x:%x:%x:%x:%x:%x", ProjectConst.SPX_SET_SETUP_GASLIST, gasNr, gasParms.n2, gasParms.he, ( gasParms.bo ? 1 : 0 ), diluent,
              ( gasParms.isCurr ? 1 : 0 ) );
      if( BuildConfig.DEBUG ) Log.d( TAG, "writeDecoPrefs: sending <NEWER-FIRMWARE <" + kdoString + ">>" );
    }
    //
    this.writeSPXMsgToDevice( kdoString );
  }

  /**
   * 
   * Schreibe eine Anzahl von Gasen in den SPX
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 23.07.2013
   * @param cf
   * @param gasUpdates
   * @throws FirmwareNotSupportetException
   */
  public void writeGasSetup( SPX42Config cf, Vector<GasUpdateEntity> gasUpdates ) throws FirmwareNotSupportetException
  {
    String kdoString = "";
    SPX42GasParms gasParms;
    int diluent, gasNr;
    //
    if( gasUpdates.isEmpty() ) return;
    //
    if( !cf.isFirmwareSupported() )
    {
      Log.e( TAG, "firmware not supportet for write settings!" );
      throw new FirmwareNotSupportetException( "write gas setup" );
    }
    //
    // einen iterator für die Durchforstung des Vectors erzeugen
    //
    Iterator<GasUpdateEntity> it = gasUpdates.iterator();
    // und nun durchforste mal bitte
    while( it.hasNext() )
    {
      GasUpdateEntity ent = it.next();
      gasParms = ent.getGasParms();
      gasNr = ent.getGasNr();
      //
      if( gasParms.d1 )
      {
        diluent = 1;
      }
      else if( gasParms.d2 )
      {
        diluent = 2;
      }
      else
      {
        diluent = 0;
      }
      if( cf.isOldParamSorting() )
      {
        // Kommando SPX_SET_SETUP_GASLIST
        // ~40:NR:HE:N2:BO:DI:CU
        // NR -> Gas Nummer
        // HE -> Heliumanteil
        // N2 -> Stickstoffanteil
        // BO -> Bailoutgas? (3?)
        // DI -> Diluent ( 0, 1 oder 2 )
        // CU Current Gas (0 oder 1)
        kdoString = String.format( "~%x:%x:%x:%x:%x:%x:%x", ProjectConst.SPX_SET_SETUP_GASLIST, gasNr, gasParms.he, gasParms.n2, ( gasParms.bo ? 1 : 0 ), diluent,
                ( gasParms.isCurr ? 1 : 0 ) );
        if( BuildConfig.DEBUG ) Log.d( TAG, "writeDecoPrefs: sending <OLDER-FIRMWARE <" + kdoString + ">>" );
        this.writeSPXMsgToDevice( kdoString );
      }
      else
      {
        // Kommando SPX_SET_SETUP_GASLIST
        // ~40:NR:N2:HE:BO:DI:CU
        // NR: Nummer des Gases 0..7
        // N2: Sticksoff in %
        // HE: Heluim in %
        // BO: Bailout (Werte 0,1 und 3 gefunden, 0 kein BO, 3 BO Wert 1 unbekannt?)
        // DI: Diluent 1 oder 2
        // CU: Current Gas
        kdoString += String.format( "~%x:%x:%x:%x:%x:%x:%x", ProjectConst.SPX_SET_SETUP_GASLIST, gasNr, gasParms.n2, gasParms.he, ( gasParms.bo ? 1 : 0 ), diluent,
                ( gasParms.isCurr ? 1 : 0 ) );
        if( BuildConfig.DEBUG ) Log.d( TAG, "writeDecoPrefs: sending <NEWER-FIRMWARE <" + kdoString + ">>" );
        this.writeSPXMsgToDevice( kdoString );
      }
    }
  }

  /**
   * 
   * Schreibe individual Einstellungen in den SPX42
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 14.07.2013
   * @param cf
   * @param sensorsOff
   * @param pscrOff
   * @param sensorsCount
   * @param soundOn
   * @param logInterval
   * @throws FirmwareNotSupportetException
   */
  public void writeIndividualPrefs( SPX42Config cf, int sensorsOff, int pscrOff, int sensorsCount, int soundOn, int logInterval ) throws FirmwareNotSupportetException
  {
    String kdoString;
    //
    if( !cf.isFirmwareSupported() )
    {
      Log.e( TAG, "firmware not supportet for write settings!" );
      throw new FirmwareNotSupportetException( "write individual prefs" );
    }
    kdoString = String.format( "~%x:%x:%x:%x:%x:%x", ProjectConst.SPX_SET_SETUP_INDIVIDUAL, sensorsOff, pscrOff, sensorsCount, soundOn, logInterval );
    if( BuildConfig.DEBUG ) Log.d( TAG, "writeIndividualPrefs: sending <" + kdoString + ">" );
    this.writeSPXMsgToDevice( kdoString );
  }

  /**
   * 
   * Screibe Kommando zum SPX, füge protokoll Start/Ende an
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.06.2013
   * @param msg
   */
  public synchronized void writeSPXMsgToDevice( String msg )
  {
    this.writeToDevice( ProjectConst.STX + msg + ProjectConst.ETX );
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
   * Schreibe die Masseinheiten in den SPX42
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 14.07.2013
   * @param cf
   * @param isTempMetric
   * @param isDepthMetric
   * @param isFreshwater
   * @throws FirmwareNotSupportetException
   */
  public void writeUnitPrefs( SPX42Config cf, int isTempMetric, int isDepthMetric, int isFreshwater ) throws FirmwareNotSupportetException
  {
    String kdoString;
    //
    if( !cf.isFirmwareSupported() )
    {
      Log.e( TAG, "firmware not supportet for write settings!" );
      throw new FirmwareNotSupportetException( "write unit prefs" );
    }
    kdoString = String.format( "~%x:%x:%x:%x", ProjectConst.SPX_SET_SETUP_UNITS, isTempMetric, isDepthMetric, isFreshwater );
    if( BuildConfig.DEBUG ) Log.d( TAG, "writeUnitPrefs: sending <" + kdoString + ">" );
    this.writeSPXMsgToDevice( kdoString );
  }

  /**
   * 
   * schreibe Datum und Zeit in das Gerät
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 27.10.2013
   * @param cf
   * @param dTime
   * @throws FirmwareNotSupportetException
   * 
   */
  public void writeDateTimeToDevice( SPX42Config cf, DateTime dTime ) throws FirmwareNotSupportetException
  {
    String kdoString;
    //
    //
    if( !cf.isFirmwareSupported() )
    {
      Log.e( TAG, "firmware not supportet for write settings!" );
      throw new FirmwareNotSupportetException( "write datetime to device" );
    }
    if( cf.canSetDate() )
    {
      //
      // Setze das Zeit und Datum als Kommandostring zusammen
      //
      kdoString = String.format( "%s~%x:%02x:%02x:%02x:%02x:%02x%s", ProjectConst.STX, ProjectConst.SPX_DATETIME, dTime.getHourOfDay(), dTime.getMinuteOfHour(),
              dTime.getDayOfMonth(), dTime.getMonthOfYear(), dTime.getYearOfCentury(), ProjectConst.ETX );
      {
        if( BuildConfig.DEBUG ) Log.d( TAG, "writeDateTimeToDevice()...send <" + kdoString + "> (DATETIME)" );
      }
      this.writeToDevice( kdoString );
    }
    else
    {
      if( BuildConfig.DEBUG ) Log.d( TAG, "writeDateTimeToDevice()...Firmware not support <set datetime> yet" );
    }
  }

  /**
   * 
   * Frage nach den schmutzigen Details des Logs
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.08.2013
   * @param numberOnSPX
   */
  public void askForLogDetail( int numberOnSPX )
  {
    String kdoString;
    kdoString = String.format( "~%x:%x", ProjectConst.SPX_GET_LOG_NUMBER, numberOnSPX );
    if( BuildConfig.DEBUG ) Log.d( TAG, "askForLogDetail: sending <" + kdoString + ">" );
    this.writeSPXMsgToDevice( kdoString );
  }
}
