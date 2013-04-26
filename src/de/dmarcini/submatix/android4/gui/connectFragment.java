package de.dmarcini.submatix.android4.gui;

import java.util.Set;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import de.dmarcini.submatix.android4.BuildConfig;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.utils.BluetoothDeviceArrayAdapter;
import de.dmarcini.submatix.android4.utils.ProjectConst;

/**
 * 
 * Ein Detsailfragment, welches die Verbindung mit dem SPX Managed
 * 
 * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 04.11.2012
 */
public class connectFragment extends Fragment implements OnClickListener, IBtServiceListener
{
  public static final String          TAG            = connectFragment.class.getSimpleName();
  private View                        rootView       = null;
  private BluetoothDeviceArrayAdapter btArrayAdapter = null;
  private Button                      discoverButton = null;
  private Spinner                     devSpinner     = null;
  private ImageButton                 connButton     = null;
  protected ProgressDialog            progressDialog = null;
  private FragmentCommonActivity      myActivity     = null;
  private boolean                     runDiscovering = false;

  /**
   * Nach dem Erzeugen des Objektes noch Einstellungen....
   */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate()..." );
    // Funktionen der Activity nutzen
    myActivity = ( ( FragmentCommonActivity )getActivity() );
    // damit die Activity Nachrichten schicken kann
    // ( ( FragmentCommonActivity )getActivity() ).setServiceListener( this );
    myActivity.setServiceListener( this );
  }

  /**
   * Wenn das View erzeugt wird (nach onCreate), noch ein paar Sachen erledigen
   */
  @Override
  public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
  {
    Log.v( TAG, "onCreateView()..." );
    // Verbindungsseite ausgewählt
    rootView = makeConnectionView( inflater, container );
    //
    // Register broadcasts während Geräte gesucht werden
    //
    IntentFilter filter = new IntentFilter( BluetoothDevice.ACTION_FOUND );
    getActivity().registerReceiver( mReceiver, filter );
    //
    // Register broadcasts wenn die Suche beendet wurde
    //
    filter = new IntentFilter( BluetoothAdapter.ACTION_DISCOVERY_FINISHED );
    getActivity().registerReceiver( mReceiver, filter );
    return rootView;
  }

  @Override
  public void onPause()
  {
    super.onPause();
    Log.v( TAG, "onPause()..." );
    discoverButton.setOnClickListener( null );
    connButton.setOnClickListener( null );
  }

  /**
   * 
   * Einen neuen Adapter mit bereits gepaarten Geräten befüllen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 13.03.2013
   */
  private void fillNewAdapterWithPairedDevices()
  {
    Log.v( TAG, "fill a new ArrayAdapter with paired devices..." );
    btArrayAdapter = new BluetoothDeviceArrayAdapter( getActivity(), R.layout.device_name_textview, FragmentCommonActivity.getAppStyle() );
    if( FragmentCommonActivity.mBtAdapter == null ) return;
    //
    // eine Liste der bereits gepaarten Devices
    //
    Set<BluetoothDevice> pairedDevices = FragmentCommonActivity.mBtAdapter.getBondedDevices();
    //
    // Sind dort einige vorhanden, dann ab in den adapter...
    //
    if( pairedDevices.size() > 0 )
    {
      // Alle gepaarten Geräte durch
      for( BluetoothDevice device : pairedDevices )
      {
        // Ist es ein Gerät vom gewünschten Typ?
        if( ( device.getBluetoothClass().getDeviceClass() == ProjectConst.SPX_BTDEVICE_CLASS ) && ( device.getName() != null ) )
        {
          String[] entr = new String[BluetoothDeviceArrayAdapter.BT_DEVAR_COUNT];
          // TODO: ALIAS TESTEN und eintragen!
          entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ALIAS] = device.getName();
          entr[BluetoothDeviceArrayAdapter.BT_DEVAR_MAC] = device.getAddress();
          entr[BluetoothDeviceArrayAdapter.BT_DEVAR_NAME] = device.getName();
          entr[BluetoothDeviceArrayAdapter.BT_DEVAR_DBID] = "0";
          entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ISPAIRED] = "true";
          entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ISONLINE] = "false";
          btArrayAdapter.add( entr );
        }
      }
    }
    else
    {
      String[] entr = new String[BluetoothDeviceArrayAdapter.BT_DEVAR_COUNT];
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ALIAS] = getActivity().getString( R.string.no_device );
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_MAC] = "";
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_NAME] = getActivity().getString( R.string.no_device );
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_DBID] = "0";
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ISPAIRED] = "false";
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ISONLINE] = "false";
      btArrayAdapter.add( entr );
    }
    Log.v( TAG, "fill List with devices..." );
    devSpinner.setAdapter( btArrayAdapter );
  }

  /**
   * Wenn das View wieder nach vorn kommt / reaktiviert wurde
   */
  @Override
  public synchronized void onResume()
  {
    super.onResume();
    Log.v( TAG, "onResume()..." );
    fillNewAdapterWithPairedDevices();
    // setze den verbindungsstatus visuell
    setToggleButtonTextAndStat( myActivity.getConnectionStatus() );
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
    // Make sure we're not doing discovery anymore
    if( FragmentCommonActivity.mBtAdapter != null )
    {
      FragmentCommonActivity.mBtAdapter.cancelDiscovery();
    }
    ( ( FragmentCommonActivity )getActivity() ).clearServiceListener();
    // Unregister broadcast listeners
    getActivity().unregisterReceiver( mReceiver );
  }

  /**
   * 
   * Die Anzeige der Verbunden/trennen Seite
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 04.11.2012
   * @param inflater
   * @param container
   * @return
   */
  private View makeConnectionView( LayoutInflater inflater, ViewGroup container )
  {
    View rootView;
    //
    Log.v( TAG, "makeConnectionView()..." );
    //
    // View aus Resource laden
    //
    rootView = inflater.inflate( R.layout.fragment_connect, container, false );
    //
    // Objekte lokalisieren
    //
    discoverButton = ( Button )rootView.findViewById( R.id.connectDiscoverButton );
    devSpinner = ( Spinner )rootView.findViewById( R.id.connectBlueToothDeviceSpinner );
    connButton = ( ImageButton )rootView.findViewById( R.id.connectButton );
    if( discoverButton == null || devSpinner == null || connButton == null )
    {
      throw new NullPointerException( "can init GUI (not found an Element)" );
    }
    //
    // den Discoverbutton mit Funktion versehen, wenn ein Adapter da und eingeschaltet ist
    //
    if( FragmentCommonActivity.mBtAdapter != null && FragmentCommonActivity.mBtAdapter.isEnabled() )
    {
      discoverButton.setOnClickListener( this );
      connButton.setOnClickListener( this );
    }
    setToggleButtonTextAndStat( ProjectConst.CONN_STATE_NONE );
    return( rootView );
  }

  private void setToggleButtonTextAndStat( int connState )
  {
    try
    {
      switch ( connState )
      {
        case ProjectConst.CONN_STATE_NONE:
        default:
          connButton.setImageResource( R.drawable.bluetooth_icon_bw );
          connButton.setAlpha( 1.0F );
          connButton.setEnabled( true );
          discoverButton.setEnabled( true );
          devSpinner.setEnabled( true );
          break;
        case ProjectConst.CONN_STATE_CONNECTING:
          connButton.setImageResource( R.drawable.bluetooth_icon_color );
          connButton.setAlpha( 0.5F );
          connButton.setEnabled( false );
          discoverButton.setEnabled( false );
          devSpinner.setEnabled( false );
          break;
        case ProjectConst.CONN_STATE_CONNECTED:
          connButton.setImageResource( R.drawable.bluetooth_icon_color );
          connButton.setAlpha( 1.0F );
          connButton.setEnabled( true );
          discoverButton.setEnabled( true );
          devSpinner.setEnabled( true );
          break;
      }
    }
    catch( NullPointerException ex )
    {
      Log.e( TAG, "Nullpointer while setToggleButtonTextAndStat() : " + ex.getLocalizedMessage() );
    }
  }

  /**
   * 
   * Starte das Suchen nach BT-Geräten
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 17.02.2013
   */
  private void startDiscoverBt()
  {
    // If we're already discovering, stop it
    Log.v( TAG, "start discovering..." );
    setItemsEnabledwhileDiscover( false );
    if( FragmentCommonActivity.mBtAdapter == null ) return;
    if( FragmentCommonActivity.mBtAdapter.isDiscovering() )
    {
      FragmentCommonActivity.mBtAdapter.cancelDiscovery();
    }
    // Discovering Marker setzen
    this.runDiscovering = true;
    // Adapter frisch befüllen
    fillNewAdapterWithPairedDevices();
    // Discovering starten
    FragmentCommonActivity.mBtAdapter.startDiscovery();
  }

  /**
   * 
   * Lasse die Butons nicht mehr bedienen während des Discovering
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 17.02.2013
   * @param enabled
   */
  private void setItemsEnabledwhileDiscover( boolean enabled )
  {
    if( progressDialog != null )
    {
      Log.v( TAG, "dialog dismiss...." );
      progressDialog.dismiss();
      progressDialog = null;
    }
    if( !enabled )
    {
      Log.v( TAG, "dialog show...." );
      progressDialog = new ProgressDialog( getActivity() );
      progressDialog.setTitle( R.string.progress_wait_for_discover_title );
      progressDialog.setIndeterminate( true );
      progressDialog.setMessage( getActivity().getResources().getString( R.string.progress_wait_for_discover_message_start ) );
      progressDialog.show();
      // .show( getActivity().getApplicationContext(), "search", "Loading...", true );
    }
    discoverButton.setEnabled( enabled );
    devSpinner.setEnabled( enabled );
    connButton.setEnabled( enabled );
  }

  //
  // der Broadcast Empfänger der Nachrichten über gefundene BT Geräte findet
  //
  private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
                                              @Override
                                              public void onReceive( Context context, Intent intent )
                                              {
                                                String action = intent.getAction();
                                                //
                                                // wenn ein Gerät gefunden wurde
                                                //
                                                if( BluetoothDevice.ACTION_FOUND.equals( action ) )
                                                {
                                                  //
                                                  // ignoriere das, wenn ich das nicht selber angeschubst hab
                                                  //
                                                  if( !runDiscovering ) return;
                                                  //
                                                  // Das Gerät extraieren
                                                  //
                                                  BluetoothDevice device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );
                                                  if( progressDialog != null )
                                                  {
                                                    Log.v( TAG, "device add to progressDialog..." );
                                                    // den Gerätenamen in den String aus der Resource (lokalisiert) einbauen und in die waitbox setzen
                                                    String dispStr = ( ( device.getName() == null ) ? device.getAddress() : device.getName() );
                                                    progressDialog.setMessage( String.format(
                                                            getActivity().getResources().getString( R.string.progress_wait_for_discover_message_continue ), dispStr ) );
                                                  }
                                                  // If it's already paired, skip it, because it's been listed already
                                                  if( ( device.getBondState() != BluetoothDevice.BOND_BONDED ) && ( device.getName() != null )
                                                          && ( device.getBluetoothClass().getMajorDeviceClass() == ProjectConst.SPX_BTDEVICE_CLASS ) )
                                                  {
                                                    if( BuildConfig.DEBUG ) Log.d( TAG, String.format( "<%s> is an RFCOMM, Add...", device.getName() ) );
                                                    // BluetoothClass btClass = device.getBluetoothClass();
                                                    // btClass.hasService( service );
                                                    // Feld 0 = Geräte Alias / Gerätename
                                                    // Feld 1 = Geräte-MAC
                                                    // Feld 2 = Geräte-Name
                                                    // Feld 3 = Datenbank-Id (wenn vorhanden) sonst 0
                                                    // Feld 4 = Gerät gepart?
                                                    String[] entr = new String[BluetoothDeviceArrayAdapter.BT_DEVAR_COUNT];
                                                    entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ALIAS] = device.getName();
                                                    entr[BluetoothDeviceArrayAdapter.BT_DEVAR_MAC] = device.getAddress();
                                                    entr[BluetoothDeviceArrayAdapter.BT_DEVAR_NAME] = device.getName();
                                                    entr[BluetoothDeviceArrayAdapter.BT_DEVAR_DBID] = "0";
                                                    entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ISPAIRED] = "false";
                                                    entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ISONLINE] = "true";
                                                    // add oder Update Datensatz, wenn nicht schon vorhanden
                                                    ( ( BluetoothDeviceArrayAdapter )devSpinner.getAdapter() ).addOrUpdate( entr );
                                                  }
                                                  else
                                                  {
                                                    // kein RFCOMM-Gerät
                                                    if( BuildConfig.DEBUG ) Log.d( TAG, String.format( "<%s> is not RFCOMM, Ignore...", device.getName() ) );
                                                  }
                                                  //
                                                  // When discovery is finished, change the Activity title
                                                }
                                                else if( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals( action ) )
                                                {
                                                  Log.v( TAG, "discover finished, enable button." );
                                                  runDiscovering = false;
                                                  setItemsEnabledwhileDiscover( true );
                                                  devSpinner.setAdapter( btArrayAdapter );
                                                }
                                              }
                                            };

  @Override
  public void onClick( View cView )
  {
    int connState = ( ( FragmentCommonActivity )getActivity() ).getConnectionStatus();
    if( BuildConfig.DEBUG ) Log.d( TAG, "ON CLICK!" );
    //
    if( cView instanceof ImageButton )
    {
      ImageButton tb = ( ImageButton )cView;
      if( btArrayAdapter.isEmpty() || btArrayAdapter.getAlias( 0 ).startsWith( getActivity().getString( R.string.no_device ).substring( 0, 5 ) ) )
      {
        Log.v( TAG, "not devices in Adapter yet..." );
        // TODO: setToggleButtonTextAndStat( tb );
        return;
      }
      switch ( connState )
      {
        case ProjectConst.CONN_STATE_NONE:
        default:
          Log.v( TAG, "switch connect to ON" );
          //
          // wenn da noch einer werkelt, anhalten und kompostieren
          //
          tb.setImageResource( R.drawable.bluetooth_icon_color );
          String device = ( ( BluetoothDeviceArrayAdapter )devSpinner.getAdapter() ).getMAC( devSpinner.getSelectedItemPosition() );
          myActivity.doConnectBtDevice( device );
          break;
        case ProjectConst.CONN_STATE_CONNECTING:
          Log.v( TAG, "cancel connecting.." );
          myActivity.doDisconnectBtDevice();
        case ProjectConst.CONN_STATE_CONNECTED:
          Log.v( TAG, "switch connect to OFF" );
          //
          // wenn da noch einer werkelt, anhalten und kompostieren
          //
          myActivity.doDisconnectBtDevice();
          break;
      }
    }
    //
    if( cView instanceof Button )
    {
      Log.v( TAG, "start discovering for BT Devices..." );
      // ist da nur die Kennzeichnung für LEER?
      if( btArrayAdapter.isEmpty() || btArrayAdapter.getAlias( 0 ).startsWith( getActivity().getString( R.string.no_device ).substring( 0, 5 ) ) )
      {
        Log.v( TAG, "not devices in Adapter yet..." );
        btArrayAdapter.clear();
        // connButton.setChecked( false );
      }
      Log.v( TAG, "start discovering for BT Devices...ArrayAdapter created" );
      startDiscoverBt();
      return;
    }
  }

  //
  // ENDE
  //
  @Override
  public void msgConnecting( BtServiceMessage msg )
  {
    setToggleButtonTextAndStat( ProjectConst.CONN_STATE_CONNECTING );
  }

  @Override
  public void msgConnected( BtServiceMessage msg )
  {
    setToggleButtonTextAndStat( ProjectConst.CONN_STATE_CONNECTED );
  }

  @Override
  public void msgDisconnected( BtServiceMessage msg )
  {
    setToggleButtonTextAndStat( ProjectConst.CONN_STATE_NONE );
  }

  @Override
  public void msgRecivedTick( BtServiceMessage msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, String.format( "recived Tick <%x08x>", msg.getTimeStamp() ) );
  }

  @Override
  public void msgConnectError( BtServiceMessage msg )
  {
    Toast.makeText(
            getActivity().getApplicationContext(),
            getActivity().getString( R.string.toast_cant_bt_connect ) + ( ( BluetoothDeviceArrayAdapter )devSpinner.getAdapter() ).getAlias( devSpinner.getSelectedItemPosition() ),
            Toast.LENGTH_LONG ).show();
  }
}
