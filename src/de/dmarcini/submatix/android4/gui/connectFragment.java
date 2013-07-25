package de.dmarcini.submatix.android4.gui;

import java.io.File;
import java.util.Set;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import de.dmarcini.submatix.android4.BuildConfig;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.utils.BluetoothDeviceArrayAdapter;
import de.dmarcini.submatix.android4.utils.LogDataSQLHelper;
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
public class connectFragment extends Fragment implements IBtServiceListener, OnItemSelectedListener, OnClickListener
{
  public static final String          TAG             = connectFragment.class.getSimpleName();
  private BluetoothDeviceArrayAdapter btArrayAdapter  = null;
  private Button                      discoverButton  = null;
  private Spinner                     devSpinner      = null;
  private ImageButton                 connButton      = null;
  private TextView                    connectTextView = null;
  private SQLiteDatabase              dBase           = null;
  protected ProgressDialog            progressDialog  = null;
  // private FragmentCommonActivity myActivity = null;
  private boolean                     runDiscovering  = false;
  private Activity                    runningActivity = null;
  //
  // der Broadcast Empfänger der Nachrichten über gefundene BT Geräte findet
  //
  private final BroadcastReceiver     mReceiver       = new BroadcastReceiver() {
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
                                                              if( BuildConfig.DEBUG ) Log.i( TAG, "device add to progressDialog..." );
                                                              // den Gerätenamen in den String aus der Resource (lokalisiert) einbauen und in die waitbox setzen
                                                              String dispStr = ( ( device.getName() == null ) ? device.getAddress() : device.getName() );
                                                              progressDialog.setMessage( String.format(
                                                                      runningActivity.getResources().getString( R.string.progress_wait_for_discover_message_continue ), dispStr ) );
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
                                                              if( BuildConfig.DEBUG ) Log.d( TAG, String.format( "Add <%s> to adapter...", device.getName() ) );
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
                                                            stopDiscoverBt();
                                                            setItemsEnabledwhileDiscover( true );
                                                          }
                                                        }
                                                      };

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
    if( BuildConfig.DEBUG ) Log.d( TAG, "fillNewAdapterWithPairedDevices: fill an ArrayAdapter with paired devices..." );
    if( FragmentCommonActivity.mBtAdapter == null ) return;
    //
    // die Liste leeren
    //
    ( ( BluetoothDeviceArrayAdapter )devSpinner.getAdapter() ).clear();
    //
    // eine Liste der bereits gepaarten Devices
    //
    Set<BluetoothDevice> pairedDevices = FragmentCommonActivity.mBtAdapter.getBondedDevices();
    //
    // Sind dort einige vorhanden, dann ab in den adapter...
    //
    if( BuildConfig.DEBUG ) Log.d( TAG, "fillNewAdapterWithPairedDevices: fill List with devices..." );
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
          if( BuildConfig.DEBUG ) Log.d( TAG, "paired Device: " + device.getName() );
          entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ALIAS] = device.getName();
          entr[BluetoothDeviceArrayAdapter.BT_DEVAR_MAC] = device.getAddress();
          entr[BluetoothDeviceArrayAdapter.BT_DEVAR_NAME] = device.getName();
          entr[BluetoothDeviceArrayAdapter.BT_DEVAR_DBID] = "0";
          entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ISPAIRED] = "true";
          entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ISONLINE] = "false";
          ( ( BluetoothDeviceArrayAdapter )devSpinner.getAdapter() ).add( entr );
        }
      }
    }
    else
    {
      if( BuildConfig.DEBUG ) Log.d( TAG, "fillNewAdapterWithPairedDevices: paired Device: " + runningActivity.getString( R.string.no_device ) );
      String[] entr = new String[BluetoothDeviceArrayAdapter.BT_DEVAR_COUNT];
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ALIAS] = runningActivity.getString( R.string.no_device );
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_MAC] = "";
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_NAME] = runningActivity.getString( R.string.no_device );
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_DBID] = "0";
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ISPAIRED] = "false";
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ISONLINE] = "false";
      ( ( BluetoothDeviceArrayAdapter )devSpinner.getAdapter() ).add( entr );
    }
  }

  @Override
  public void handleMessages( int what, BtServiceMessage smsg )
  {
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
        msgConnected( smsg );
        break;
      // ################################################################
      // Computer wurde getrennt
      // ################################################################
      case ProjectConst.MESSAGE_DISCONNECTED:
        msgDisconnected( smsg );
        break;
      // ################################################################
      // Computer wurde getrennt
      // ################################################################
      case ProjectConst.MESSAGE_CONNECTERROR:
        msgConnectError( smsg );
        break;
      // ################################################################
      // SPX sendet "ALIVE" und Ackuspannung
      // ################################################################
      case ProjectConst.MESSAGE_SPXALIVE:
        msgRecivedAlive( smsg );
        break;
      // ################################################################
      // ignoriere...
      // ################################################################
      case ProjectConst.MESSAGE_SERIAL_READ:
      case ProjectConst.MESSAGE_MANUFACTURER_READ:
      case ProjectConst.MESSAGE_FWVERSION_READ:
      case ProjectConst.MESSAGE_LICENSE_STATE_READ:
        break;
      // ################################################################
      // Sonst....
      // ################################################################
      default:
        if( BuildConfig.DEBUG ) Log.i( TAG, "handleMessages: unhadled message message with id <" + smsg.getId() + "> recived!" );
    }
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
    if( BuildConfig.DEBUG ) Log.d( TAG, "makeConnectionView..." );
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
    connectTextView = ( TextView )rootView.findViewById( R.id.connectStatusText );
    if( discoverButton == null || devSpinner == null || connButton == null )
    {
      throw new NullPointerException( "makeConnectionView: can't init GUI (not found an Element)" );
    }
    return( rootView );
  }

  @Override
  public void msgConnected( BtServiceMessage msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "msgConnected..." );
    setSpinnerToConnectedDevice();
    setToggleButtonTextAndStat( ProjectConst.CONN_STATE_CONNECTED );
  }

  @Override
  public void msgConnectError( BtServiceMessage msg )
  {
    Toast.makeText(
            runningActivity.getApplicationContext(),
            runningActivity.getString( R.string.toast_cant_bt_connect )
                    + ( ( BluetoothDeviceArrayAdapter )devSpinner.getAdapter() ).getAlias( devSpinner.getSelectedItemPosition() ), Toast.LENGTH_LONG ).show();
  }

  @Override
  public void msgConnecting( BtServiceMessage msg )
  {
    setToggleButtonTextAndStat( ProjectConst.CONN_STATE_CONNECTING );
  }

  @Override
  public void msgDisconnected( BtServiceMessage msg )
  {
    int index;
    setToggleButtonTextAndStat( ProjectConst.CONN_STATE_NONE );
    index = devSpinner.getSelectedItemPosition();
    btArrayAdapter = ( BluetoothDeviceArrayAdapter )devSpinner.getAdapter();
    btArrayAdapter.setDevicesOffline();
    // Update erzwingen
    devSpinner.setAdapter( btArrayAdapter );
    devSpinner.setSelection( index, true );
  }

  @Override
  public void msgRecivedAlive( BtServiceMessage msg )
  {
    //
  }

  @Override
  public void msgRecivedTick( BtServiceMessage msg )
  {
    // if( BuildConfig.DEBUG ) Log.d( TAG, String.format( "recived Tick <%x08x>", msg.getTimeStamp() ) );
  }

  @Override
  public void msgReciveWriteTmeout( BtServiceMessage msg )
  {
    //
  }

  @Override
  public void onActivityCreated( Bundle bundle )
  {
    super.onActivityCreated( bundle );
    runningActivity = getActivity();
    if( BuildConfig.DEBUG ) Log.i( TAG, "onActivityCreated: ACTIVITY ATTACH" );
    try
    {
      discoverButton = ( Button )runningActivity.findViewById( R.id.connectDiscoverButton );
      devSpinner = ( Spinner )runningActivity.findViewById( R.id.connectBlueToothDeviceSpinner );
      connButton = ( ImageButton )runningActivity.findViewById( R.id.connectButton );
      connectTextView = ( TextView )runningActivity.findViewById( R.id.connectStatusText );
      if( FragmentCommonActivity.mBtAdapter != null && FragmentCommonActivity.mBtAdapter.isEnabled() )
      {
        if( BuildConfig.DEBUG ) Log.d( TAG, "onActivityCreated: set connectFragment eventhandler..." );
        devSpinner.setOnItemSelectedListener( this );
        discoverButton.setOnClickListener( this );
        connButton.setOnClickListener( this );
        //
        // den eigenen ArrayAdapter machen
        //
        btArrayAdapter = new BluetoothDeviceArrayAdapter( runningActivity, R.layout.bt_array_with_pic_adapter_view, FragmentCommonActivity.getAppStyle() );
        devSpinner.setAdapter( btArrayAdapter );
        //
        setToggleButtonTextAndStat( ProjectConst.CONN_STATE_NONE );
      }
      else
      {
        if( BuildConfig.DEBUG ) Log.d( TAG, "onActivityCreated: NOT set onClick eventhandler..." );
      }
    }
    catch( NullPointerException ex )
    {
      Log.e( TAG, "onActivityCreated: gui objects not allocated!" );
    }
  }

  @Override
  public void onAttach( Activity activity )
  {
    super.onAttach( activity );
    runningActivity = activity;
    if( BuildConfig.DEBUG ) Log.i( TAG, "onAttach: ATTACH" );
    //
    // die Datenbank öffnen
    //
    if( BuildConfig.DEBUG ) Log.d( TAG, "onAttach: create SQLite helper..." );
    LogDataSQLHelper sqlHelper = new LogDataSQLHelper( getActivity().getApplicationContext(), FragmentCommonActivity.databaseDir.getAbsolutePath() + File.separator
            + ProjectConst.DATABASE_NAME );
    if( BuildConfig.DEBUG ) Log.d( TAG, "onAttach: open Database..." );
    dBase = sqlHelper.getWritableDatabase();
  }

  @Override
  public void onClick( View cView )
  {
    int connState = ( ( FragmentCommonActivity )runningActivity ).getConnectionStatus();
    if( BuildConfig.DEBUG ) Log.d( TAG, "onClick: ON CLICK!" );
    // btArrayAdapter = ( BluetoothDeviceArrayAdapter )devSpinner.getAdapter();
    //
    if( cView instanceof ImageButton )
    {
      ImageButton tb = ( ImageButton )cView;
      if( devSpinner.getSelectedItemPosition() == -1 )
      {
        Log.w( TAG, "onClick: not devices selected yet..." );
        setToggleButtonTextAndStat( connState );
        return;
      }
      if( btArrayAdapter.isEmpty() || btArrayAdapter.getAlias( 0 ).startsWith( runningActivity.getString( R.string.no_device ).substring( 0, 5 ) ) )
      {
        Log.w( TAG, "onClick: not devices in Adapter yet..." );
        setToggleButtonTextAndStat( connState );
        return;
      }
      switch ( connState )
      {
        case ProjectConst.CONN_STATE_NONE:
        default:
          if( BuildConfig.DEBUG ) Log.d( TAG, "onClick: switch connect to ON" );
          //
          // wenn da noch einer werkelt, anhalten und kompostieren
          //
          tb.setImageResource( R.drawable.bluetooth_icon_color );
          String device = ( ( BluetoothDeviceArrayAdapter )devSpinner.getAdapter() ).getMAC( devSpinner.getSelectedItemPosition() );
          ( ( FragmentCommonActivity )runningActivity ).doConnectBtDevice( device );
          break;
        case ProjectConst.CONN_STATE_CONNECTING:
          Log.i( TAG, "cancel connecting.." );
          ( ( FragmentCommonActivity )runningActivity ).doDisconnectBtDevice();
        case ProjectConst.CONN_STATE_CONNECTED:
          if( BuildConfig.DEBUG ) Log.d( TAG, "onClick: switch connect to OFF" );
          //
          // wenn da noch einer werkelt, anhalten und kompostieren
          //
          ( ( FragmentCommonActivity )runningActivity ).doDisconnectBtDevice();
          break;
      }
    }
    //
    else if( cView instanceof Button )
    {
      Log.i( TAG, "onClick: start discovering for BT Devices..." );
      // ist da nur die Kennzeichnung für LEER?
      if( btArrayAdapter.isEmpty() || btArrayAdapter.getAlias( 0 ).startsWith( runningActivity.getString( R.string.no_device ).substring( 0, 5 ) ) )
      {
        Log.w( TAG, "onClick: not devices in Adapter yet..." );
        btArrayAdapter.clear();
      }
      if( BuildConfig.DEBUG ) Log.d( TAG, "onClick: start discovering for BT Devices...OK" );
      startDiscoverBt();
      return;
    }
  }

  /**
   * Nach dem Erzeugen des Objektes noch Einstellungen....
   */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    if( BuildConfig.DEBUG ) Log.d( TAG, "onCreate..." );
    // Funktionen der Activity nutzen
  }

  /**
   * Wenn das View erzeugt wird (nach onCreate), noch ein paar Sachen erledigen
   */
  @Override
  public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
  {
    View rootView;
    if( BuildConfig.DEBUG ) Log.d( TAG, "onCreateView..." );
    //
    // wenn kein Container vorhanden ist, dann gibts auch keinen View
    //
    if( container == null )
    {
      Log.e( TAG, "onCreateView: container is NULL ..." );
      return( null );
    }
    //
    // wenn die laufende Activity eine areaDetailActivity ist, dann gibts das View schon
    //
    if( runningActivity instanceof areaDetailActivity )
    {
      if( BuildConfig.DEBUG ) Log.d( TAG, "onCreateView: running from areaDetailActivity ..." );
      return( null );
    }
    //
    // Verbindungsseite via twoPane ausgewählt
    //
    rootView = makeConnectionView( inflater, container );
    return rootView;
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
    ( ( FragmentCommonActivity )runningActivity ).removeServiceListener( this );
    //
    // Datenbank wieder schliessen
    //
    if( BuildConfig.DEBUG ) Log.d( TAG, "onDestroy: close Database..." );
    if( dBase != null )
    {
      dBase.close();
      dBase = null;
    }
  }

  @Override
  public void onItemSelected( AdapterView<?> arg0, View arg1, int arg2, long arg3 )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "onItemSelected: ITEM Selected!" );
  }

  @Override
  public void onNothingSelected( AdapterView<?> arg0 )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "onItemSelected: ITEM NOT Selected!" );
  }

  @Override
  public void onPause()
  {
    super.onPause();
    if( BuildConfig.DEBUG ) Log.d( TAG, "onPause..." );
    //
    // die abgeleiteten Objekte führen das auch aus
    //
    if( BuildConfig.DEBUG ) Log.d( TAG, "onPause: clear service listener for preferences fragment..." );
    ( ( FragmentCommonActivity )runningActivity ).removeServiceListener( this );
  }

  /**
   * Wenn das View wieder nach vorn kommt / reaktiviert wurde
   */
  @Override
  public synchronized void onResume()
  {
    super.onResume();
    if( BuildConfig.DEBUG ) Log.d( TAG, "onResume..." );
    ( ( FragmentCommonActivity )runningActivity ).addServiceListener( this );
    // wenn zu diesem Zeitpunkt das Array noch nicht gefüllt ist, dann mach das nun
    if( 0 == btArrayAdapter.getCount() )
    {
      fillNewAdapterWithPairedDevices();
    }
    // setze den verbindungsstatus visuell
    setToggleButtonTextAndStat( ( ( FragmentCommonActivity )runningActivity ).getConnectionStatus() );
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
      if( BuildConfig.DEBUG ) Log.d( TAG, "setItemsEnabledwhileDiscover: dialog dismiss...." );
      progressDialog.dismiss();
      progressDialog = null;
    }
    if( !enabled )
    {
      if( BuildConfig.DEBUG ) Log.d( TAG, "setItemsEnabledwhileDiscover: dialog show...." );
      progressDialog = new ProgressDialog( runningActivity );
      progressDialog.setTitle( R.string.progress_wait_for_discover_title );
      progressDialog.setIndeterminate( true );
      progressDialog.setMessage( runningActivity.getResources().getString( R.string.progress_wait_for_discover_message_start ) );
      progressDialog.show();
    }
    setToggleButtonTextAndStat( ProjectConst.CONN_STATE_NONE );
  }

  /**
   * 
   * Setze den Spinner auf den Eintrag mir dem verbundenen Gerät
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 28.05.2013
   */
  private void setSpinnerToConnectedDevice()
  {
    String deviceAddr = null;
    int deviceIndex = -1;
    if( BuildConfig.DEBUG ) Log.d( TAG, "setSpinnerToConnectedDevice..." );
    try
    {
      // wenn zu diesem Zeitpunkt das Array noch nicht gefüllt ist, dann mach das nun
      if( 0 == btArrayAdapter.getCount() )
      {
        fillNewAdapterWithPairedDevices();
      }
      // ArrayAdapter erfragen
      // mit welchem Gerät bin ich verbunden?
      deviceAddr = ( ( FragmentCommonActivity )runningActivity ).getConnectedDevice();
      Log.v( TAG, "setSpinnerToConnectedDevice connected Device: <" + deviceAddr + ">" );
      // welcher index gehört zu dem Gerät?
      deviceIndex = btArrayAdapter.getIndexForMac( deviceAddr );
      Log.v( TAG, "setSpinnerToConnectedDevice index in Adapter: <" + deviceIndex + ">" );
      // Online Markieren
      btArrayAdapter.setDeviceIsOnline( deviceIndex );
      // Update erzwingen
      devSpinner.setAdapter( btArrayAdapter );
      // Selektieren
      devSpinner.setSelection( deviceIndex, true );
      Log.v( TAG, "setSpinnerToConnectedDevice set Spinner to index <" + deviceIndex + ">" );
    }
    catch( Exception ex )
    {
      Log.e( TAG, "setSpinnerToConnectedDevice: <" + ex.getLocalizedMessage() + ">" );
      Log.w( TAG, "setSpinnerToConnectedDevice exception: Spinner to 0" );
      devSpinner.setSelection( 0, false );
    }
  }

  /**
   * 
   * Oberfläche anpassen, je nach gemeldetem Verbindungsstatus
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 26.04.2013
   * @param connState
   *          welcher Verbindungsstatus
   */
  private void setToggleButtonTextAndStat( int connState )
  {
    Resources res = runningActivity.getResources();
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
          connectTextView.setText( R.string.connect_disconnect_device );
          connectTextView.setTextColor( res.getColor( R.color.connectFragment_disconnectText ) );
          break;
        case ProjectConst.CONN_STATE_CONNECTING:
          connButton.setImageResource( R.drawable.bluetooth_icon_connecting );
          connButton.setAlpha( 0.5F );
          connButton.setEnabled( true );
          discoverButton.setEnabled( false );
          devSpinner.setEnabled( false );
          connectTextView.setText( R.string.connect_connecting_device );
          connectTextView.setTextColor( res.getColor( R.color.connectFragment_connectingText ) );
          break;
        case ProjectConst.CONN_STATE_CONNECTED:
          connButton.setImageResource( R.drawable.bluetooth_icon_color );
          connButton.setAlpha( 1.0F );
          connButton.setEnabled( true );
          discoverButton.setEnabled( false );
          devSpinner.setEnabled( false );
          connectTextView.setText( R.string.connect_connected_device );
          connectTextView.setTextColor( res.getColor( R.color.connectFragment_connectText ) );
          break;
      }
    }
    catch( NullPointerException ex )
    {
      Log.e( TAG, "setToggleButtonTextAndStat: Nullpointer while setToggleButtonTextAndStat() : " + ex.getLocalizedMessage() );
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
    Log.i( TAG, "startDiscoverBt: start discovering..." );
    setItemsEnabledwhileDiscover( false );
    if( FragmentCommonActivity.mBtAdapter == null ) return;
    if( FragmentCommonActivity.mBtAdapter.isDiscovering() )
    {
      stopDiscoverBt();
    }
    //
    // Register broadcasts während Geräte gesucht werden
    //
    IntentFilter filter = new IntentFilter( BluetoothDevice.ACTION_FOUND );
    runningActivity.registerReceiver( mReceiver, filter );
    //
    // Register broadcasts wenn die Suche beendet wurde
    //
    filter = new IntentFilter( BluetoothAdapter.ACTION_DISCOVERY_FINISHED );
    runningActivity.registerReceiver( mReceiver, filter );
    //
    // Discovering Marker setzen
    this.runDiscovering = true;
    // Adapter frisch befüllen
    fillNewAdapterWithPairedDevices();
    // Discovering starten
    FragmentCommonActivity.mBtAdapter.startDiscovery();
  }

  /**
   * 
   * Das Discovering beenden
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 06.05.2013
   */
  private void stopDiscoverBt()
  {
    FragmentCommonActivity.mBtAdapter.cancelDiscovery();
    // Unregister broadcast listeners
    runningActivity.unregisterReceiver( mReceiver );
  }
}
