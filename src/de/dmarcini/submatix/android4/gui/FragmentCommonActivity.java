/**
 * gemeinsamer Code der List- und der Detailactivity
 * 
 * FragmentCommonActivity.java de.dmarcini.submatix.android4.gui SubmatixBTLoggerAndroid_4
 * 
 * @author Dirk Marciniak 28.12.2012
 */
package de.dmarcini.submatix.android4.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import de.dmarcini.submatix.android4.BuildConfig;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.comm.BlueThoothComService;
import de.dmarcini.submatix.android4.comm.BlueThoothComService.LocalBinder;
import de.dmarcini.submatix.android4.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.content.ContentSwitcher;
import de.dmarcini.submatix.android4.utils.GasUpdateEntity;
import de.dmarcini.submatix.android4.utils.NoticeDialogListener;
import de.dmarcini.submatix.android4.utils.ProjectConst;

/**
 * Der gemeinsame Code der List- und Detailactivity
 * 
 * @author dmarc
 */
public class FragmentCommonActivity extends Activity implements NoticeDialogListener, IBtServiceListener
{
  private static final String                 TAG             = FragmentCommonActivity.class.getSimpleName();
  private static final String                 SERVICENAME     = BlueThoothComService.class.getCanonicalName();
  private static final String                 PACKAGENAME     = "de.dmarcini.submatix.android4";
  protected static File                       databaseDir     = null;
  protected static boolean                    mTwoPane        = false;
  protected static boolean                    isIndividual    = false;
  protected static int                        mixLicense      = ProjectConst.SPX_LICENSE_NOT_SET;             // License State 0=Nitrox,1=Normoxic Trimix,2=Full Trimix
  protected static String                     serialNumber    = null;
  protected static String                     manufacturer    = null;
  protected static BluetoothAdapter           mBtAdapter      = null;
  private BlueThoothComService                mService        = null;
  private LocalBinder                         binder          = null;
  private final ArrayList<IBtServiceListener> serviceListener = new ArrayList<IBtServiceListener>();
  private volatile boolean                    mIsBound        = false;
  private static int                          currentStyleId  = R.style.AppDarkTheme;

  public static final int getAppStyle()
  {
    return( currentStyleId );
  }

  //
  //@formatter:off
  //
  // wird beim binden / unbinden benutzt
  //
  private final ServiceConnection mConnection = new ServiceConnection() 
  {
    @Override
    public void onServiceConnected( ComponentName name, IBinder service )
    {
      if( BuildConfig.DEBUG ) Log.d(TAG,"onServiceConnected()...");
      binder = ( LocalBinder )service;
      mService = binder.getService();
      binder.registerServiceHandler( mHandler );
    }
  
    @Override
    public void onServiceDisconnected( ComponentName name )
    {
      if( BuildConfig.DEBUG ) Log.d(TAG,"onServiceDisconnected...");
      if( mService != null && binder != null )
      {
        if( BuildConfig.DEBUG ) Log.d(TAG,"onServiceDisconnected...unregister Handler...");
        binder.unregisterServiceHandler( mHandler );
      }
      mService = null;
      binder = null;
    }
  };
  //
  // Ein Messagehandler, der vom Service kommende Messages bearbeitet
  //
  private final Handler mHandler = new Handler() 
  {
    @Override
    public void handleMessage( Message msg )
    {
      if( !( msg.obj instanceof BtServiceMessage ) )
      {
        Log.e(TAG,"Handler::handleMessage: Recived Message is NOT type of BtServiceMessage!");
        return;
      }
      BtServiceMessage smsg = (BtServiceMessage)msg.obj;
      
      // an alle Listener versenden!
      Iterator<IBtServiceListener> it = serviceListener.iterator();
      while( it.hasNext() )
      {
        it.next().handleMessages( msg.what, smsg );
      }
    }
  };

  /**
   * 
   * Wenn ein Fragment die Nachrichten erhalten soll, muß es den listener übergben...
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 24.02.2013
   * @param listener
   */
  public void addServiceListener( IBtServiceListener listener )
  {
    Log.v( TAG, "setServiceListener..." );
    if( !serviceListener.contains( listener ) )
    {
      serviceListener.add( listener );
    }
    //
    // wenn ich im "Tablettmodus" bin, wird natürlich keine
    // neue Activity gestartet, d.h. es wird dann auch kein onConnect erzeugt.
    // Somit muss ich da etwas nachhelfen.
    //
    if( mTwoPane )
    {
      if( mService != null )
      {
        BtServiceMessage msg;
        int state = mService.getConnectionState();
        // welche Message muss ich machen?
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
        // an alle Listener versenden!
        Iterator<IBtServiceListener> it = serviceListener.iterator();
        while( it.hasNext() )
        {
          it.next().msgConnected( msg );
        }
      }
    }
  }

  //
  //@formatter:on
  //
  /**
   * Frage, ob BR erlaubt werden sollte Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 20.12.2012
   */
  private void askEnableBT()
  {
    Log.v( TAG, "askEnableBT..." );
    Intent enableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
    startActivityForResult( enableIntent, ProjectConst.REQUEST_ENABLE_BT );
    Log.v( TAG, "askEnableBT...OK" );
  }

  /**
   * 
   * Frage den SPX nach der Konfiguration
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.06.2013
   */
  public void askForConfigFromSPX42()
  {
    if( mService != null )
    {
      mService.askForConfigFromSPX42();
    }
  }

  /**
   * 
   * Frage nach der DECO-Konfiguration
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 13.07.2013
   */
  public void askForDecoConfig()
  {
    if( mService != null )
    {
      mService.askForDecoConfig();
    }
  }

  /**
   * 
   * Grad beim SPX nach der Firmwareversion
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.06.2013
   */
  public void askForFirmwareVersion()
  {
    if( mService != null )
    {
      mService.askForFirmwareVersion();
    }
  }

  /**
   * 
   * frage den SPX nach den Gaseinstellungen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 18.07.2013
   */
  public void askForGasFromSPX()
  {
    if( mService != null )
    {
      mService.askForGasFromSPX();
    }
  }

  /**
   * 
   * Frag nach der Lizenz des SPX
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 04.06.2013
   */
  public void askForLicenseFromSPX()
  {
    if( mService != null )
    {
      mService.askForLicenseFromSPX();
    }
  }

  /**
   * 
   * Frage den SPX nach seinem Hersteller
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 04.06.2013
   */
  public void askForManufacturer()
  {
    if( mService != null )
    {
      mService.askForManufacturer();
    }
  }

  /**
   * 
   * Frage nach der Seriennummer
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 28.05.2013
   */
  public void askForSerialNumber()
  {
    if( mService != null )
    {
      mService.askForSerialNumber();
    }
  }

  /**
   * 
   * Frage den SPX ob er am Leben ist und wie seine Ackuspannung ist
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.06.2013
   */
  public void askForSPXAlive()
  {
    if( mService != null )
    {
      mService.askForSPXAlive();
    }
  }

  /**
   * 
   * Service binden, ggf starten
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 23.02.2013
   */
  private void doBindService()
  {
    //
    // Service starten, wenn er nicht schon läuft
    //
    Log.v( TAG, "doBindService()..." );
    final ActivityManager activityManager = ( ActivityManager )getApplicationContext().getSystemService( ACTIVITY_SERVICE );
    final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices( Integer.MAX_VALUE );
    boolean isServiceFound = false;
    //
    for( int i = 0; i < services.size(); i++ )
    {
      // if( BuildConfig.DEBUG ) Log.d( TAG, "Service Nr." + i + ":" + services.get( i ).service + "<" + services.get( i ).service.getPackageName() + ">" );
      if( ( services.get( i ).service.getPackageName() ).matches( PACKAGENAME ) )
      {
        // if( BuildConfig.DEBUG ) Log.d( TAG, "Service class name <" + services.get( i ).service.getClassName() + ">" );
        if( SERVICENAME.equals( services.get( i ).service.getClassName() ) )
        {
          if( BuildConfig.DEBUG ) Log.d( TAG, "Service is running, need not start..." );
          isServiceFound = true;
        }
      }
    }
    if( !isServiceFound )
    {
      if( BuildConfig.DEBUG ) Log.d( TAG, "Starting Service..." );
      Intent service = new Intent( this, BlueThoothComService.class );
      startService( service );
    }
    //
    // binde Service
    //
    Log.i( TAG, "bind  BT service..." );
    Intent intent = new Intent( this, BlueThoothComService.class );
    bindService( intent, mConnection, Context.BIND_AUTO_CREATE );
    mIsBound = true;
  }

  /**
   * 
   * Verbinde Blutethooth Gerät
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 04.03.2013
   * @param device
   */
  public void doConnectBtDevice( String device )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, String.format( "recived do connect device <%s>", device ) );
    if( mIsBound )
    {
      mService.connect( device );
    }
  }

  /**
   * 
   * Trenne Bluethooth Gerät
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 04.03.2013
   */
  public void doDisconnectBtDevice()
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "recived do disconnect device " );
    if( mIsBound )
    {
      mService.disconnect();
    }
  }

  /**
   * 
   * Service unbinden
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 23.02.2013
   */
  private void doUnbindService()
  {
    if( mIsBound )
    {
      // If we have received the service, and hence registered with it, then now is the time to unregister.
      if( mService != null )
      {
        Log.v( TAG, "doUnbindService..." );
        if( mService != null )
        {
          if( mService != null && binder != null )
          {
            if( BuildConfig.DEBUG ) Log.d( TAG, "doUnbindService...unregister Handler..." );
            binder.unregisterServiceHandler( mHandler );
          }
          unbindService( mConnection );
          mService = null;
          binder = null;
        }
        mIsBound = false;
        Log.v( TAG, "doUnbindService...OK" );
      }
    }
  }

  @Override
  public void finishFromChild( Activity child )
  {
    Log.i( TAG, "child process called finish()..." );
    //
    // wenn eine Clientactivity mit finish() beendet
    // wurde, ist hier auch schluss
    //
    finish();
  }

  /**
   * 
   * Mit welchem Gerät (Addr) bin ich verbunden?
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 28.05.2013
   * @return MAC-Addr des Gerätes
   */
  public String getConnectedDevice()
  {
    if( mService != null )
    {
      return( mService.getConnectedDevice() );
    }
    return( null );
  }

  /**
   * 
   * Verbindungsstatus erfragen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 13.03.2013
   * @return der Verbindungsstatus
   */
  public int getConnectionStatus()
  {
    if( mService != null )
    {
      return( mService.getConnectionState() );
    }
    return( ProjectConst.CONN_STATE_NONE );
  }

  @Override
  public void handleMessages( int what, BtServiceMessage smsg )
  {
    areaListFragment frag;
    //
    // versuche mal das Fragment mit der Liste zu finden
    //
    frag = ( areaListFragment )getFragmentManager().findFragmentById( R.id.area_list );
    if( frag == null )
    {
      frag = ( areaListFragment )getFragmentManager().findFragmentById( R.id.item_list );
    }
    // was war denn los? Welche Nachricht kam rein?
    switch ( what )
    {
    //
    // ################################################################
    // Service TICK empfangen
    // ################################################################
      case ProjectConst.MESSAGE_TICK:
        msgRecivedTick( smsg );
        break;
      // ################################################################
      // Computer wird gerade verbunden
      // ################################################################
      case ProjectConst.MESSAGE_CONNECTING:
        msgConnecting( smsg );
        break;
      // ################################################################
      // Computer wurde getrennt
      // ################################################################
      case ProjectConst.MESSAGE_CONNECTED:
        // die Menüs anpassen
        if( frag != null )
        {
          Log.v( TAG, "ICONS auf CONNECTED stellen..." );
          frag.setListAdapterForOnlinestatus( true );
        }
        else
        {
          Log.v( TAG, "no fragment found: ICONS auf CONNECTED... " );
        }
        msgConnected( smsg );
        break;
      // ################################################################
      // Computer wurde getrennt
      // ################################################################
      case ProjectConst.MESSAGE_DISCONNECTED:
        // die Menüs anpassen
        if( frag != null )
        {
          Log.v( TAG, "ICONS auf DISCONNECTED stellen" );
          frag.setListAdapterForOnlinestatus( false );
        }
        else
        {
          Log.v( TAG, "no fragment found: ICONS auf DISCONNECTED... " );
        }
        msgDisconnected( smsg );
        break;
      // ################################################################
      // Computer wurde getrennt
      // ################################################################
      case ProjectConst.MESSAGE_CONNECTERROR:
        // die Menüs anpassen
        if( frag != null )
        {
          Log.v( TAG, "ICONS auf DISCONNECTED stellen" );
          frag.setListAdapterForOnlinestatus( false );
        }
        else
        {
          Log.v( TAG, "no fragment found: ICONS auf DISCONNECTED... " );
        }
        msgConnectError( smsg );
        break;
      // ################################################################
      // Seriennummer des ccomputers wurde gelesen
      // ################################################################
      case ProjectConst.MESSAGE_SERIAL_READ:
        msgRecivedSerial( smsg );
        break;
      // ################################################################
      // SPX sendet "ALIVE" und Ackuspannung
      // ################################################################
      case ProjectConst.MESSAGE_SPXALIVE:
        msgRecivedAlive( smsg );
        break;
      // ################################################################
      // SPX sendet Herstellerkennung
      // ################################################################
      case ProjectConst.MESSAGE_MANUFACTURER_READ:
        msgReciveManufacturer( smsg );
        break;
      // ################################################################
      // SPX Lizenz lesen
      // ################################################################
      case ProjectConst.MESSAGE_LICENSE_STATE_READ:
        msgReciveLicenseState( smsg );
        break;
      // ################################################################
      // Sonst....
      // ################################################################
      default:
        if( BuildConfig.DEBUG ) Log.i( TAG, "unknown message with id <" + smsg.getId() + "> recived!" );
    }
  }

  /**
   * 
   * Ist die Activity mit zwei Anzeigeflächen? (Tablett)
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.01.2013
   * @return ist zweigeteilt oder nicht
   */
  public boolean istActivityTwoPane()
  {
    return( mTwoPane );
  }

  /**
   * 
   * Das Gerät wurde verbunden
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 23.02.2013
   * @param msg
   */
  @Override
  public void msgConnected( BtServiceMessage msg )
  {
    Log.v( TAG, "connected..." );
    //
    if( BuildConfig.DEBUG ) Log.d( TAG, "msgConnected(): ask for manufacturer number..." );
    askForManufacturer();
    if( BuildConfig.DEBUG ) Log.d( TAG, "msgConnected(): ask for serial number..." );
    askForSerialNumber();
    if( BuildConfig.DEBUG ) Log.d( TAG, "msgConnected(): ask for Firmware version..." );
    askForFirmwareVersion();
    if( BuildConfig.DEBUG ) Log.d( TAG, "msgConnected(): ask for SPX license..." );
    askForLicenseFromSPX();
  }

  @Override
  public void msgConnectError( BtServiceMessage msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "connection error (device not online?)" );
  }

  /**
   * 
   * Wenn das Gerät verbunden wird (beim Verbinden)
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 23.02.2013
   * @param msg
   */
  @Override
  public void msgConnecting( BtServiceMessage msg )
  {
    Log.v( TAG, "connecting..." );
  }

  /**
   * 
   * Das Gerät wurde getrennt
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 23.02.2013
   * @param msg
   * 
   */
  @Override
  public void msgDisconnected( BtServiceMessage msg )
  {
    Log.v( TAG, "disconnected..." );
    serialNumber = null;
    mixLicense = ProjectConst.SPX_LICENSE_NOT_SET;
    isIndividual = false;
    serialNumber = null;
    manufacturer = null;
  }

  @Override
  public void msgRecivedAlive( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  /**
   * 
   * Nachricht mit der Seriennummer des SPX empfangen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 17.07.2013
   * @param msg
   */
  public void msgRecivedSerial( BtServiceMessage msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "serial <" + ( String )msg.getContainer() + "> recived" );
    serialNumber = new String( ( String )msg.getContainer() );
  }

  @Override
  public void msgRecivedTick( BtServiceMessage msg )
  {
    // if( BuildConfig.DEBUG ) Log.d( TAG, String.format( "recived Tick <%x08x>", msg.getTimeStamp() ) );
  }

  /**
   * 
   * Nachricht mit dem Lizenzstatus des SPX empfangen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 17.07.2013
   * @param msg
   */
  public void msgReciveLicenseState( BtServiceMessage msg )
  {
    // LS : License State 0=Nitrox,1=Normoxic Trimix,2=Full Trimix
    // CE : Custom Enabled 0= disabled, 1=enabled
    String[] lic = ( String[] )msg.getContainer();
    if( BuildConfig.DEBUG ) Log.d( TAG, "SPX License state <" + lic[0] + "," + lic[1] + "> recived" );
    try
    {
      // wie ist der Status?
      mixLicense = Integer.parseInt( lic[0] );
    }
    catch( NumberFormatException ex )
    {
      mixLicense = ProjectConst.SPX_LICENSE_NITROX;
      Log.e( TAG, "license status read exception: <" + ex.getLocalizedMessage() + ">" );
    }
    // ist individual AN?
    isIndividual = ( lic[1].matches( "1" ) );
    if( BuildConfig.DEBUG )
    {
      Log.d( TAG, "SPX License INDIVIDUAL: <" + isIndividual + ">" );
      switch ( mixLicense )
      {
        case 0:
          Log.d( TAG, "SPX MIX License : NITROX" );
          break;
        case 1:
          Log.d( TAG, "SPX MIX License : NORMOXIX TRIMIX" );
          break;
        case 2:
          Log.d( TAG, "SPX MIX License : FULL TRIMIX" );
          break;
        default:
          Log.d( TAG, "SPX MIX License : UNKNOWN" );
      }
    }
  }

  /**
   * 
   * Nachricht über den Hersteller des Gerätes empfangen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 17.07.2013
   * @param msg
   */
  public void msgReciveManufacturer( BtServiceMessage msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "SPX Manufacturer <" + ( String )msg.getContainer() + "> recived" );
    manufacturer = ( String )msg.getContainer();
  }

  @Override
  public void msgReciveWriteTmeout( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void onActivityResult( int requestCode, int resultCode, Intent data )
  {
    Log.v( TAG, "onActivityResult()... " );
    switch ( requestCode )
    {
    //
    // Bluethooth erlauben
    //
      case ProjectConst.REQUEST_ENABLE_BT:
        // Wenn BT eingeschaltet wurde
        if( resultCode == Activity.RESULT_OK )
        {
          Log.v( TAG, "REQUEST_ENABLE_BT => BT Device ENABLED" );
          Toast.makeText( this, R.string.toast_bt_enabled, Toast.LENGTH_SHORT ).show();
          // Service starten und binden (bei onResume())
        }
        else
        {
          // User did not enable Bluetooth or an error occured
          Log.v( TAG, "REQUEST_ENABLE_BT => BT Device NOT ENABLED" );
          Toast.makeText( this, R.string.toast_exit_nobt, Toast.LENGTH_LONG ).show();
          finish();
        }
        break;
      case ProjectConst.REQUEST_SPX_PREFS:
        //
        // wenn die Activity der SPX-Einstellungen zurückkehrt...
        //
        Log.v( TAG, "spx42 preferences activity returns..." );
        // finishActivity( ProjectConst.REQUEST_SPX_PREFS_F );
        setContentView( R.layout.activity_area_list );
        break;
      default:
        Log.w( TAG, "unknown Request code for activity result" );
    }
  }

  /**
   * Wenn die Activity erzeugt wird, u.A. herausfinden ob ein- oder zwei-Flächen Mode
   * 
   * @see android.app.Activity#onCreate(android.os.Bundle)
   * @author Dirk Marciniak 28.12.2012
   * @param savedInstanceState
   */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate..." );
    serviceListener.clear();
    serviceListener.add( this );
    // den defaultadapter lesen
    mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    Log.v( TAG, "onCreate: setContentView..." );
    //
    // Das gewünschte Thema setzen
    //
    SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences( this );
    if( sPref.contains( "keyProgOthersThemeIsDark" ) )
    {
      boolean whishedTheme = sPref.getBoolean( "keyProgOthersThemeIsDark", false );
      if( whishedTheme )
      {
        if( BuildConfig.DEBUG ) Log.d( TAG, "onCreate: select DARK theme while preference was set" );
        currentStyleId = R.style.AppDarkTheme;
        setTheme( R.style.AppDarkTheme );
      }
      else
      {
        if( BuildConfig.DEBUG ) Log.d( TAG, "onCreate: select Blue theme while preference was set" );
        currentStyleId = R.style.AppLightTheme;
        setTheme( R.style.AppLightTheme );
      }
    }
    //
    setContentView( R.layout.activity_area_list );
    //
    // finde raus, ob es ein Restart für ein neues Theme war
    //
    if( getIntent().getExtras() != null && getIntent().getExtras().containsKey( ProjectConst.ARG_ITEM_ID ) )
    {
      Log.v( TAG, "onCreate: it was an bundle there..." );
      if( getIntent().getExtras().getInt( ProjectConst.ARG_ITEM_ID, 0 ) == R.string.progitem_progpref )
      {
        // ja, jetzt muss ich auch drauf reagieren und die Preferenzen aufbauen
        Log.i( TAG, "onCreate: set program preferences after switch theme..." );
        Bundle arg = new Bundle();
        arg.putString( ProjectConst.ARG_ITEM_ID, getResources().getString( R.string.progitem_progpref ) );
        ProgramPreferencesFragment ppFragment = new ProgramPreferencesFragment();
        ppFragment.setArguments( arg );
        getActionBar().setTitle( R.string.conf_prog_headline );
        getActionBar().setLogo( R.drawable.properties );
        getFragmentManager().beginTransaction().replace( R.id.area_detail_container, ppFragment ).commit();
      }
    }
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
  }

  /**
   * Wird ein Dialog negativ beendet (nein oder Abbruch)
   * 
   * @see de.dmarcini.submatix.android4.gui.AreYouSureDialogFragment.NoticeDialogListener#onDialogNegativeClick(android.app.DialogFragment)
   * @author Dirk Marciniak 28.12.2012
   * @param dialog
   */
  @Override
  public void onDialogNegativeClick( DialogFragment dialog )
  {
    Log.v( TAG, "Negative dialog click!" );
  }

  /**
   * Wird ein dialog Positiv beendet (ja oder Ok...)
   * 
   * @see de.dmarcini.submatix.android4.gui.AreYouSureDialogFragment.NoticeDialogListener#onDialogPositiveClick(android.app.DialogFragment)
   * @author Dirk Marciniak 28.12.2012
   * @param dialog
   */
  @Override
  public void onDialogPositiveClick( DialogFragment dialog )
  {
    Log.v( TAG, "Positive dialog click!" );
    //
    // war es ein AreYouSureDialogFragment Dialog?
    //
    if( dialog instanceof AreYouSureDialogFragment )
    {
      AreYouSureDialogFragment aDial = ( AreYouSureDialogFragment )dialog;
      //
      // War der Tag für den Dialog zum Exit des Programmes?
      //
      if( aDial.getTag().equals( "programexit" ) )
      {
        Log.i( TAG, "User will close app..." );
        Toast.makeText( this, R.string.toast_exit, Toast.LENGTH_SHORT ).show();
        if( BluetoothAdapter.getDefaultAdapter() != null )
        {
          // TODO: Preferences -> Programmeinstellungen soll das automatisch passieren?
          BluetoothAdapter.getDefaultAdapter().disable();
        }
        finish();
      }
    }
    else if( dialog instanceof EditAliasDialogFragment )
    {
      Log.i( TAG, "User will edit alias..." );
    }
  }

  /**
   * Wird ein Eintrag der Auswahlliste angeklickt Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 22.12.2012
   * @param listView
   * @param view
   * @param position
   * @param id
   */
  public void onListItemClick( ListView listView, View view, int position, long id )
  {
    ContentSwitcher.ProgItem mItem = null;
    Bundle arguments = new Bundle();
    boolean isOnline = false;
    //
    //
    // zunächst will ich mal wissen, was das werden soll!
    //
    Log.v( TAG, "onListItemClick()..." );
    Log.v( TAG, "onListItemClick: ID was: <" + position + ">" );
    mItem = ( ContentSwitcher.ProgItem )listView.getItemAtPosition( position );
    if( mItem == null )
    {
      Log.e( TAG, "onListItemClick: program menu item was NOT explored!" );
      return;
    }
    arguments.putString( ProjectConst.ARG_ITEM_CONTENT, mItem.content );
    arguments.putInt( ProjectConst.ARG_ITEM_ID, mItem.nId );
    Log.v( TAG, "onListItemClick: item content was: " + mItem.content );
    Log.v( TAG, "onListItemClick: item id was: " + mItem.nId );
    //
    // wenn EXIT angeordnet wurde
    //
    switch ( mItem.nId )
    {
      case R.string.progitem_exit:
        // ENDE
        Log.v( TAG, "onListItemClick: make dialog for USER..." );
        AreYouSureDialogFragment sureDial = new AreYouSureDialogFragment( getString( R.string.dialog_sure_exit ) );
        sureDial.show( getFragmentManager().beginTransaction(), "programexit" );
        return;
    }
    //
    // sind wir online?
    //
    if( getConnectionStatus() == ProjectConst.CONN_STATE_CONNECTED )
    {
      isOnline = true;
    }
    // ////////////////////////////////////////////////////////////////////////
    // jetzt noch zwischen Tablett mit Schirmsplitt und Smartphone unterscheiden
    // ////////////////////////////////////////////////////////////////////////
    if( mTwoPane )
    {
      //
      // zweischirmbetrieb, die Activity bleibt die areaListActivity
      //
      Log.i( TAG, "onListItemClick: towPane mode!" );
      //
      // Abhängig vom Onlinestatus
      //
      if( isOnline )
      {
        //
        // wenn der SPX online ist, Funktionen freischalten
        //
        getActionBar().setLogo( mItem.resIdOnline );
        switch ( mItem.nId )
        {
          case R.string.progitem_config:
            //
            // Der Benutzer wählt den Konfigurationseintrag für den SPX
            //
            Log.v( TAG, "onListItemClick: create SPX42PreferencesFragment..." );
            SPX42PreferencesFragment cFragment = new SPX42PreferencesFragment();
            cFragment.setArguments( arguments );
            getActionBar().setTitle( R.string.conf_headline );
            getFragmentManager().beginTransaction().replace( R.id.area_detail_container, cFragment ).setTransition( FragmentTransaction.TRANSIT_FRAGMENT_FADE ).commit();
            break;
          //
          case R.string.progitem_progpref:
            //
            // der Benutzer will Programmeinstellungen setzen
            //
            Log.v( TAG, "onListItemClick: set program preferences..." );
            ProgramPreferencesFragment ppFragment = new ProgramPreferencesFragment();
            ppFragment.setArguments( arguments );
            getActionBar().setTitle( R.string.conf_prog_headline );
            getFragmentManager().beginTransaction().replace( R.id.area_detail_container, ppFragment ).setTransition( FragmentTransaction.TRANSIT_FRAGMENT_FADE ).commit();
            break;
          case R.string.progitem_gaslist:
            //
            // der Benutzer wählt den Gaslisten Editmode
            //
            Log.v( TAG, "onListItemClick: set gas preferences..." );
            SPX42GaslistPreferencesFragment glFragment = new SPX42GaslistPreferencesFragment();
            glFragment.setArguments( arguments );
            getActionBar().setTitle( R.string.gaslist_headline );
            getFragmentManager().beginTransaction().replace( R.id.area_detail_container, glFragment ).setTransition( FragmentTransaction.TRANSIT_FRAGMENT_FADE ).commit();
            break;
          //
          default:
            Log.w( TAG, "Not programitem found for <" + mItem.nId + ">" );
          case R.string.progitem_connect:
            //
            // keine passende ID gefunden oder
            // der Benutzer wählt den Verbindungseintrag
            //
            connectFragment connFragment = ( connectFragment )getFragmentManager().findFragmentById( R.id.connectLinearLayout );
            if( connFragment == null )
            {
              connFragment = new connectFragment();
            }
            getActionBar().setTitle( R.string.connect_headline );
            connFragment.setArguments( arguments );
            getFragmentManager().beginTransaction().replace( R.id.area_detail_container, connFragment ).setTransition( FragmentTransaction.TRANSIT_FRAGMENT_FADE ).commit();
            //
        }
      }
      else
      {
        //
        // wenn der SPX OFFLINE ist, nur OFFLINE Funktionen freigeben
        getActionBar().setLogo( mItem.resIdOffline );
        switch ( mItem.nId )
        {
          case R.string.progitem_progpref:
            //
            // der Benutzer will Programmeinstellungen setzen
            //
            Log.v( TAG, "onListItemClick: set program preferences..." );
            ProgramPreferencesFragment ppFragment = new ProgramPreferencesFragment();
            ppFragment.setArguments( arguments );
            getActionBar().setTitle( R.string.conf_prog_headline );
            getFragmentManager().beginTransaction().replace( R.id.area_detail_container, ppFragment ).setTransition( FragmentTransaction.TRANSIT_FRAGMENT_FADE ).commit();
            break;
          //
          default:
            //
            // keine passende ID gefunden oder
            // der Benutzer wählt den Verbindungseintrag
            //
            connectFragment connFragment = ( connectFragment )getFragmentManager().findFragmentById( R.id.connectLinearLayout );
            if( connFragment == null )
            {
              connFragment = new connectFragment();
            }
            getActionBar().setTitle( R.string.connect_headline );
            connFragment.setArguments( arguments );
            getFragmentManager().beginTransaction().replace( R.id.area_detail_container, connFragment ).setTransition( FragmentTransaction.TRANSIT_FRAGMENT_FADE ).commit();
            //
        }
      }
    }
    else
    {
      //
      // kleiner Schirm
      // da wird jeder Eintrag als einzelne activity ausgeführt
      //
      Log.i( TAG, "onListItemClick: onePane modus! Call intent DetailActivity fur itenid<" + mItem.nId + ">" );
      Intent detailIntent = new Intent( this, areaDetailActivity.class );
      detailIntent.putExtras( arguments );
      // die neue Activity starten
      startActivity( detailIntent );
    }
  }

  @Override
  public boolean onOptionsItemSelected( MenuItem item )
  {
    switch ( item.getItemId() )
    {
      case android.R.id.home:
        if( BuildConfig.DEBUG ) Log.d( TAG, "onOptionsItemSelected: navigate UP!" );
        // This is called when the Home (Up) button is pressed
        // in the Action Bar.
        Intent parentActivityIntent = new Intent( this, areaListActivity.class );
        parentActivityIntent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity( parentActivityIntent );
        finish();
        return true;
    }
    return super.onOptionsItemSelected( item );
  }

  @Override
  public void onPause()
  {
    super.onPause();
    doUnbindService();
  }

  @Override
  public void onResume()
  {
    Log.v( TAG, "onResume..." );
    super.onResume();
    //
    if( mBtAdapter == null )
    {
      if( ProjectConst.CHECK_PHYSICAL_BT )
      {
        // es gibt gar keinen adapter!
        Toast.makeText( this, R.string.toast_exit_nobt, Toast.LENGTH_LONG ).show();
        finish();
        return;
      }
      else
      {
        Toast.makeText( this, R.string.toast_exit_nobt, Toast.LENGTH_LONG ).show();
        return;
      }
    }
    if( !mBtAdapter.isEnabled() )
    {
      // Eh, kein BT erlaubt!
      askEnableBT();
    }
    else
    {
      // Service wieder anbinden / starten
      doBindService();
    }
  }

  /**
   * 
   * Den Listener löschen, d.h. die Activity macht das wieder selber
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 24.02.2013
   * @param listener
   */
  public void removeServiceListener( IBtServiceListener listener )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "clearServiceListener()..." );
    //
    // wenn der listener vorhanden ist, entferne ihn aus der Liste
    //
    int index = serviceListener.indexOf( listener );
    if( index > -1 )
    {
      serviceListener.remove( index );
    }
  }

  /**
   * 
   * schreibe den Setpoint ins Gerät
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.06.2013
   * @param auto
   * @param pressure
   */
  public void writeAutoSetpoint( int auto, int pressure )
  {
    if( mService != null )
    {
      mService.writeAutoSetpoint( auto, pressure );
    }
  }

  /**
   * 
   * schreibe DECO-Preferenzen auf das Gerät
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 13.07.2013
   */
  public void writeDecoPrefs( int logG, int highG, int deepSt, int dynGr, int lastStop )
  {
    if( mService != null )
    {
      mService.writeDecoPrefs( logG, highG, deepSt, dynGr, lastStop );
    }
  }

  /**
   * 
   * schreibe Display Preferenzen in den SPX
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 14.07.2013
   * @param lumin
   *          Helligkeit
   * @param orient
   *          Orientierung
   */
  public void writeDisplayPrefs( int lumin, int orient )
  {
    if( mService != null )
    {
      mService.writeDisplayPrefs( lumin, orient );
    }
  }

  /**
   * 
   * schreibe INDIVIDUAL Einstellungen in den SPX42
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 14.07.2013
   * @param sensorsOff
   * @param pscrOff
   * @param sensorsCount
   * @param soundOn
   * @param logInterval
   */
  public void writeIndividualPrefs( int sensorsOff, int pscrOff, int sensorsCount, int soundOn, int logInterval )
  {
    if( mService != null )
    {
      mService.writeIndividualPrefs( sensorsOff, pscrOff, sensorsCount, soundOn, logInterval );
    }
  }

  /**
   * 
   * schreibe die Einstellungen für Masseinheiten in den SPX42
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 14.07.2013
   * @param isTempMetric
   * @param isDepthMetric
   * @param isFreshwater
   */
  public void writeUnitPrefs( int isTempMetric, int isDepthMetric, int isFreshwater )
  {
    if( mService != null )
    {
      mService.writeUnitPrefs( isTempMetric, isDepthMetric, isFreshwater );
    }
  }

  /**
   * 
   * schreibe ein Gas setup in den SPX42
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 18.07.2013
   * @param gasUpdates
   */
  public void writeGasSetup( Vector<GasUpdateEntity> gasUpdates )
  {
    if( mService != null )
    {
      mService.writeGasSetup( gasUpdates );
    }
  }
}
