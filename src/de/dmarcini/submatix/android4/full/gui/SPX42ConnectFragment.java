//@formatter:off
/*
    programm: SubmatixBTLoggerAndroid
    purpose:  configuration and read logs from SUBMATIX SPX42 divecomputer via Bluethooth    
    Copyright (C) 2012  Dirk Marciniak

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/
*/
//@formatter:on
package de.dmarcini.submatix.android4.full.gui;

import java.io.File;
import java.util.Set;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import de.dmarcini.submatix.android4.full.ApplicationDEBUG;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.full.dialogs.EditAliasDialogFragment;
import de.dmarcini.submatix.android4.full.dialogs.UserAlertDialogFragment;
import de.dmarcini.submatix.android4.full.exceptions.NoDatabaseException;
import de.dmarcini.submatix.android4.full.interfaces.IBtServiceListener;
import de.dmarcini.submatix.android4.full.utils.BluetoothDeviceArrayAdapter;
import de.dmarcini.submatix.android4.full.utils.CommToast;
import de.dmarcini.submatix.android4.full.utils.DataSQLHelper;
import de.dmarcini.submatix.android4.full.utils.ProjectConst;
import de.dmarcini.submatix.android4.full.utils.SPX42AliasManager;

/**
 * 
 * Ein Detsailfragment, welches die Verbindung mit dem SPX Managed
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class SPX42ConnectFragment extends Fragment implements IBtServiceListener, OnItemSelectedListener, OnClickListener
{
  @SuppressWarnings( "javadoc" )
  public static final String          TAG                       = SPX42ConnectFragment.class.getSimpleName();
  private static final String         LAST_CONNECTED_DEVICE_KEY = "keyLastConnectedDevice";
  private String                      lastConnectedDeviceMac    = null;
  private BluetoothDeviceArrayAdapter btArrayAdapter            = null;
  private Button                      discoverButton            = null;
  private Button                      aliasEditButton           = null;
  private Spinner                     devSpinner                = null;
  private ImageButton                 connButton                = null;
  private TextView                    connectTextView           = null;
  protected ProgressDialog            progressDialog            = null;
  private boolean                     runDiscovering            = false;
  private Activity                    runningActivity           = null;
  private CommToast                   theToast                  = null;
  private boolean                     showCommToast             = false;
  //
  // der Broadcast Empfänger der Nachrichten über gefundene BT Geräte findet
  //
  private final BroadcastReceiver     mReceiver                 = new BroadcastReceiver() {
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
                                                                        if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "device add to progressDialog..." );
                                                                        // den Gerätenamen in den String aus der Resource (lokalisiert) einbauen und in die waitbox setzen
                                                                        String dispStr = ( ( device.getName() == null ) ? device.getAddress() : device.getName() );
                                                                        progressDialog.setMessage( String.format(
                                                                                runningActivity.getResources().getString( R.string.progress_wait_for_discover_message_continue ),
                                                                                dispStr ) );
                                                                      }
                                                                      //
                                                                      // Ist das gerät bereits gepaart, überspringe es, da es bereits gelistet ist
                                                                      //
                                                                      if( ( device.getBondState() != BluetoothDevice.BOND_BONDED ) && ( device.getName() != null )
                                                                              && ( device.getBluetoothClass().getMajorDeviceClass() == ProjectConst.SPX_BTDEVICE_CLASS ) )
                                                                      {
                                                                        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, String.format( "<%s> is an RFCOMM, Add...", device.getName() ) );
                                                                        // BluetoothClass btClass = device.getBluetoothClass();
                                                                        // btClass.hasService( service );
                                                                        // Feld 0 = Geräte Alias / Gerätename
                                                                        // Feld 1 = Geräte-MAC
                                                                        // Feld 2 = Geräte-Name
                                                                        // Feld 3 = Datenbank-Id (wenn vorhanden) sonst 0
                                                                        // Feld 4 = Gerät gepart?
                                                                        String devAlias = MainActivity.aliasManager.getAliasForMac( device.getAddress(), device.getName() );
                                                                        String[] entr = new String[BluetoothDeviceArrayAdapter.BT_DEVAR_COUNT];
                                                                        entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ALIAS] = devAlias;
                                                                        entr[BluetoothDeviceArrayAdapter.BT_DEVAR_MAC] = device.getAddress();
                                                                        entr[BluetoothDeviceArrayAdapter.BT_DEVAR_NAME] = device.getName();
                                                                        entr[BluetoothDeviceArrayAdapter.BT_DEVAR_DBID] = "0";
                                                                        entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ISPAIRED] = "false";
                                                                        entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ISONLINE] = "true";
                                                                        // add oder Update Datensatz, wenn nicht schon vorhanden
                                                                        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, String.format( "Add <%s> to adapter...", devAlias ) );
                                                                        ( ( BluetoothDeviceArrayAdapter )devSpinner.getAdapter() ).addOrUpdate( entr );
                                                                      }
                                                                      else
                                                                      {
                                                                        // kein RFCOMM-Gerät
                                                                        if( ApplicationDEBUG.DEBUG )
                                                                          Log.d( TAG, String.format( "<%s> is not RFCOMM, Ignore...", device.getName() ) );
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
   * 
   * Stand: 13.03.2013
   */
  private void fillNewAdapterWithPairedDevices()
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "fillNewAdapterWithPairedDevices: fill an ArrayAdapter with paired devices..." );
    if( MainActivity.mBtAdapter == null ) return;
    //
    // die Liste leeren
    //
    if( btArrayAdapter == null )
    {
      btArrayAdapter = ( BluetoothDeviceArrayAdapter )devSpinner.getAdapter();
    }
    btArrayAdapter.clear();
    //
    // eine Liste der bereits gepaarten Devices
    //
    Set<BluetoothDevice> pairedDevices = MainActivity.mBtAdapter.getBondedDevices();
    //
    // Sind dort einige vorhanden, dann ab in den adapter...
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "fillNewAdapterWithPairedDevices: fill List with devices..." );
    if( pairedDevices.size() > 0 )
    {
      // Alle gepaarten Geräte durch
      for( BluetoothDevice device : pairedDevices )
      {
        // Ist es ein Gerät vom gewünschten Typ?
        if( ( device.getBluetoothClass().getDeviceClass() == ProjectConst.SPX_BTDEVICE_CLASS ) && ( device.getName() != null ) )
        {
          try
          {
            String[] entr = new String[BluetoothDeviceArrayAdapter.BT_DEVAR_COUNT];
            if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "paired Device: " + device.getName() );
            entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ALIAS] = MainActivity.aliasManager.getAliasForMac( device.getAddress(), device.getName() );
            entr[BluetoothDeviceArrayAdapter.BT_DEVAR_MAC] = device.getAddress();
            entr[BluetoothDeviceArrayAdapter.BT_DEVAR_NAME] = device.getName();
            entr[BluetoothDeviceArrayAdapter.BT_DEVAR_DBID] = "0";
            entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ISPAIRED] = "true";
            entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ISONLINE] = "false";
            btArrayAdapter.add( entr );
          }
          catch( NullPointerException ex )
          {
            Log.e( TAG, "Nullpointer (alias manager not initialized? <" + ex.getLocalizedMessage() + ">" );
            continue;
          }
        }
      }
    }
    else
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "fillNewAdapterWithPairedDevices: paired Device: " + runningActivity.getString( R.string.no_device ) );
      String[] entr = new String[BluetoothDeviceArrayAdapter.BT_DEVAR_COUNT];
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ALIAS] = runningActivity.getString( R.string.no_device );
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_MAC] = "";
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_NAME] = runningActivity.getString( R.string.no_device );
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_DBID] = "0";
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ISPAIRED] = "false";
      entr[BluetoothDeviceArrayAdapter.BT_DEVAR_ISONLINE] = "false";
      btArrayAdapter.add( entr );
    }
    setSpinnerToLastConnected();
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
      // Alias editiert
      // ################################################################
      case ProjectConst.MESSAGE_DEVALIAS_SET:
        msgReciveDeviceAliasSet( smsg );
        break;
      // ################################################################
      // Wenn Serial dann in die DB
      // ################################################################
      case ProjectConst.MESSAGE_SERIAL_READ:
        setSerialIfNotExist( MainActivity.spxConfig.getConnectedDeviceMac(), MainActivity.spxConfig.getSerial() );
        // ################################################################
        // ignoriere...
        // ################################################################
        break;
      case ProjectConst.MESSAGE_MANUFACTURER_READ:
      case ProjectConst.MESSAGE_FWVERSION_READ:
      case ProjectConst.MESSAGE_LICENSE_STATE_READ:
        break;
      // ################################################################
      // Sonst....
      // ################################################################
      default:
        if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "handleMessages: unhadled message message with id <" + smsg.getId() + "> recived!" );
    }
  }

  /**
   * 
   * Die Anzeige der Verbunden/trennen Seite
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 04.11.2012
   * 
   * @param inflater
   * @param container
   * @return
   */
  private View makeConnectionView( LayoutInflater inflater, ViewGroup container )
  {
    View rootView;
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "makeConnectionView..." );
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
    aliasEditButton = ( Button )rootView.findViewById( R.id.connectAliasEditButton );
    if( discoverButton == null || devSpinner == null || connButton == null )
    {
      throw new NullPointerException( "makeConnectionView: can't init GUI (not found an Element)" );
    }
    return( rootView );
  }

  @Override
  public void msgConnected( BtServiceMessage msg )
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "msgConnected..." );
    if( showCommToast )
    {
      String deviceName = ( ( BluetoothDeviceArrayAdapter )devSpinner.getAdapter() ).getAlias( devSpinner.getSelectedItemPosition() );
      String connStringFormat = getResources().getString( R.string.toast_connect_connected );
      theToast.showConnectionToast( String.format( connStringFormat, deviceName ), false );
      showCommToast = false;
    }
    setSpinnerToConnectedDevice();
    setToggleButtonTextAndStat( ProjectConst.CONN_STATE_CONNECTED );
    writePreferences();
  }

  @Override
  public void msgConnectError( BtServiceMessage msg )
  {
    String deviceName = ( ( BluetoothDeviceArrayAdapter )devSpinner.getAdapter() ).getAlias( devSpinner.getSelectedItemPosition() );
    String connStringFormat = getResources().getString( R.string.toast_connect_cant_bt_connect );
    theToast.showConnectionToastAlert( String.format( connStringFormat, deviceName ) );
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
    MainActivity.spxConfig.clear();
    setToggleButtonTextAndStat( ProjectConst.CONN_STATE_NONE );
    index = devSpinner.getSelectedItemPosition();
    btArrayAdapter = ( BluetoothDeviceArrayAdapter )devSpinner.getAdapter();
    btArrayAdapter.setDevicesOffline();
    // Update erzwingen
    devSpinner.setAdapter( btArrayAdapter );
    devSpinner.setSelection( index, true );
    if( showCommToast )
    {
      theToast.showConnectionToast( getResources().getString( R.string.toast_connect_disconnected ), false );
      showCommToast = false;
    }
  }

  @Override
  public void msgRecivedAlive( BtServiceMessage msg )
  {
    //
  }

  /**
   * 
   * Empfange Nachricht, dass der Device Alias geändert werden soll
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 26.07.2013
   * 
   * @param msg
   */
  private void msgReciveDeviceAliasSet( BtServiceMessage msg )
  {
    int dIndex;
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "msgReciveDeviceAliasSet..." );
    // ist das das Stringarray?
    if( msg.getContainer() instanceof String[] )
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "msgReciveDeviceAliasSet: is String[]..." );
      // genau 3 Paraqmeter (devicename,alias,mac)
      String[] parm = ( String[] )msg.getContainer();
      if( parm.length == 3 )
      {
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "msgReciveDeviceAliasSet: is 3 params" );
        dIndex = btArrayAdapter.getIndexForMac( parm[2] );
        // hat er's gefunden?
        if( dIndex == -1 )
        {
          Log.e( TAG, "msgReciveDeviceAliasSet: can't find device mac addr in device array adapter!" );
          return;
        }
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "msgReciveDeviceAliasSet: Set alias to <" + parm[1] + ">" );
        //
        // in der Datenbank verramschen. Seriennummer ist nicht bekannt (bin offline)
        //
        if( MainActivity.aliasManager.setAliasForMac( parm[2], parm[0], parm[1], null ) )
        {
          if( 0 == btArrayAdapter.getCount() )
          {
            fillNewAdapterWithPairedDevices();
          }
          btArrayAdapter.setDevAlias( dIndex, parm[1] );
          // Update erzwingen
          devSpinner.setAdapter( btArrayAdapter );
          // Selektieren
          devSpinner.setSelection( dIndex, true );
          btArrayAdapter = ( BluetoothDeviceArrayAdapter )devSpinner.getAdapter();
        }
        else
        {
          Log.w( TAG, "can't set alias in database!" );
        }
      }
    }
  }

  @Override
  public void msgRecivedTick( BtServiceMessage msg )
  {
    // if( ApplicationDEBUG.DEBUG ) Log.d( TAG, String.format( "recived Tick <%x08x>", msg.getTimeStamp() ) );
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
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onActivityCreated: ..." );
    try
    {
      discoverButton = ( Button )runningActivity.findViewById( R.id.connectDiscoverButton );
      devSpinner = ( Spinner )runningActivity.findViewById( R.id.connectBlueToothDeviceSpinner );
      connButton = ( ImageButton )runningActivity.findViewById( R.id.connectButton );
      aliasEditButton = ( Button )runningActivity.findViewById( R.id.connectAliasEditButton );
      connectTextView = ( TextView )runningActivity.findViewById( R.id.connectStatusText );
      if( ( MainActivity.mBtAdapter != null ) && ( MainActivity.mBtAdapter.isEnabled() ) )
      {
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onActivityCreated: set SPX42ConnectFragment eventhandler..." );
        devSpinner.setOnItemSelectedListener( this );
        discoverButton.setOnClickListener( this );
        aliasEditButton.setOnClickListener( this );
        connButton.setOnClickListener( this );
        //
        // den eigenen ArrayAdapter machen
        //
        btArrayAdapter = new BluetoothDeviceArrayAdapter( runningActivity, R.layout.bt_array_with_pic_adapter_view, MainActivity.getAppStyle() );
        devSpinner.setAdapter( btArrayAdapter );
        //
        setToggleButtonTextAndStat( ProjectConst.CONN_STATE_NONE );
      }
      else
      {
        if( ApplicationDEBUG.DEBUG ) Log.e( TAG, "onActivityCreated: NOT BT Adapter/not enabled => NOT set onClick eventhandler..." );
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
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onAttach: ATTACH" );
    //
    // die Datenbank öffnen
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onAttach: create SQLite helper..." );
    DataSQLHelper sqlHelper = new DataSQLHelper( getActivity().getApplicationContext(), MainActivity.databaseDir.getAbsolutePath() + File.separator + ProjectConst.DATABASE_NAME );
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onAttach: open Database..." );
    try
    {
      if( MainActivity.aliasManager == null )
      {
        MainActivity.aliasManager = new SPX42AliasManager( sqlHelper.getWritableDatabase() );
      }
    }
    catch( NoDatabaseException ex )
    {
      Log.e( TAG, "NoDatabaseException: <" + ex.getLocalizedMessage() + ">" );
      UserAlertDialogFragment uad = new UserAlertDialogFragment( runningActivity.getResources().getString( R.string.dialog_sqlite_error_header ), runningActivity.getResources()
              .getString( R.string.dialog_sqlite_nodatabase_error ) );
      uad.show( getFragmentManager(), "abortProgram" );
    }
    lastConnectedDeviceMac = null;
  }

  @Override
  public void onClick( View cView )
  {
    int connState = ( ( MainActivity )runningActivity ).getConnectionStatus();
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onClick: ON CLICK!" );
    //
    // Wenn das der CONNECT Button war
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
          if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onClick: switch connect to ON" );
          //
          // da soll verbunden werden!
          //
          showCommToast = true;
          tb.setImageResource( R.drawable.bluetooth_icon_color );
          String device = ( ( BluetoothDeviceArrayAdapter )devSpinner.getAdapter() ).getMAC( devSpinner.getSelectedItemPosition() );
          String deviceName = ( ( BluetoothDeviceArrayAdapter )devSpinner.getAdapter() ).getDevName( devSpinner.getSelectedItemPosition() );
          setAliasForDeviceIfNotExist( device, deviceName );
          ( ( MainActivity )runningActivity ).doConnectBtDevice( device );
          break;
        case ProjectConst.CONN_STATE_CONNECTING:
          Log.i( TAG, "cancel connecting.." );
          ( ( MainActivity )runningActivity ).doDisconnectBtDevice();
        case ProjectConst.CONN_STATE_CONNECTED:
          if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onClick: switch connect to OFF" );
          //
          // wenn da noch einer werkelt, anhalten und kompostieren
          //
          showCommToast = true;
          ( ( MainActivity )runningActivity ).doDisconnectBtDevice();
          break;
      }
    }
    //
    // es war ein anderer Button?
    //
    else if( cView instanceof Button )
    {
      //
      // es war der Geräte-Such-Button
      //
      if( ( Button )cView == discoverButton )
      {
        Log.i( TAG, "onClick: start discovering for BT Devices..." );
        // ist da nur die Kennzeichnung für LEER?
        if( btArrayAdapter.isEmpty() || btArrayAdapter.getAlias( 0 ).startsWith( runningActivity.getString( R.string.no_device ).substring( 0, 5 ) ) )
        {
          Log.w( TAG, "onClick: not devices in Adapter yet..." );
          btArrayAdapter.clear();
        }
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onClick: start discovering for BT Devices...OK" );
        startDiscoverBt();
        return;
      }
      //
      // es war der EDIT-Alias Button
      //
      else if( ( Button )cView == aliasEditButton )
      {
        Log.i( TAG, "onClick: start edit current alias..." );
        if( btArrayAdapter.isEmpty() || btArrayAdapter.getAlias( 0 ).startsWith( runningActivity.getString( R.string.no_device ).substring( 0, 5 ) ) )
        {
          Log.w( TAG, "onClick: not devices in Adapter yet..." );
          return;
        }
        //
        // erzeuge den Dialog zum Bearbeiten des Alias
        //
        int pos = devSpinner.getSelectedItemPosition();
        DialogFragment dialog = new EditAliasDialogFragment( btArrayAdapter.getDevName( pos ), btArrayAdapter.getAlias( pos ), btArrayAdapter.getMAC( pos ) );
        dialog.show( getFragmentManager(), "EditAliasDialogFragment" );
      }
    }
  }

  /**
   * Nach dem Erzeugen des Objektes noch Einstellungen....
   */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreate..." );
    theToast = new CommToast( getActivity() );
    showCommToast = false;
  }

  /**
   * Wenn das View erzeugt wird (nach onCreate), noch ein paar Sachen erledigen
   */
  @Override
  public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
  {
    View rootView;
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreateView..." );
    //
    // wenn kein Container vorhanden ist, dann gibts auch keinen View
    //
    if( container == null )
    {
      Log.e( TAG, "onCreateView: container is NULL ..." );
      return( null );
    }
    rootView = makeConnectionView( inflater, container );
    return rootView;
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
    // Make sure we're not doing discovery anymore
    if( MainActivity.mBtAdapter != null )
    {
      MainActivity.mBtAdapter.cancelDiscovery();
    }
    ( ( MainActivity )runningActivity ).removeServiceListener( this );
  }

  @Override
  public void onItemSelected( AdapterView<?> arg0, View arg1, int arg2, long arg3 )
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onItemSelected: ITEM Selected!" );
  }

  @Override
  public void onNothingSelected( AdapterView<?> arg0 )
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onItemSelected: ITEM NOT Selected!" );
  }

  @Override
  public void onPause()
  {
    super.onPause();
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onPause..." );
    //
    // die abgeleiteten Objekte führen das auch aus
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onPause: clear service listener for preferences fragment..." );
    ( ( MainActivity )runningActivity ).removeServiceListener( this );
  }

  /**
   * Wenn das View wieder nach vorn kommt / reaktiviert wurde
   */
  @Override
  public synchronized void onResume()
  {
    super.onResume();
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onResume..." );
    ( ( MainActivity )runningActivity ).addServiceListener( this );
    // wenn zu diesem Zeitpunkt das Array noch nicht gefüllt ist, dann mach das nun
    if( 0 == btArrayAdapter.getCount() )
    {
      fillNewAdapterWithPairedDevices();
    }
    // setze den verbindungsstatus visuell
    setToggleButtonTextAndStat( ( ( MainActivity )runningActivity ).getConnectionStatus() );
  }

  /**
   * 
   * Setze einen Alias für das Gerät, wenn noch keiner gesetzt wurde
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 03.12.2013
   * 
   * @param _mac
   * @param _deviceName
   */
  private void setAliasForDeviceIfNotExist( String _mac, String _deviceName )
  {
    if( MainActivity.aliasManager != null )
    {
      MainActivity.aliasManager.setAliasForMacIfNotExist( _mac, _deviceName );
    }
  }

  /**
   * 
   * Lasse die Butons nicht mehr bedienen während des Discovering
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 17.02.2013
   * 
   * @param enabled
   */
  private void setItemsEnabledwhileDiscover( boolean enabled )
  {
    if( progressDialog != null )
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "setItemsEnabledwhileDiscover: dialog dismiss...." );
      progressDialog.dismiss();
      progressDialog = null;
    }
    if( !enabled )
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "setItemsEnabledwhileDiscover: dialog show...." );
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
   * Wenn noch keine Seriennummer für das Gerät eingetragen ist, hole das nach
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 03.12.2013
   * 
   * @param _mac
   * @param _serial
   */
  private void setSerialIfNotExist( String _mac, String _serial )
  {
    if( MainActivity.aliasManager != null )
    {
      MainActivity.aliasManager.setSerialIfNotExist( _mac, _serial );
    }
  }

  /**
   * 
   * Setze den Spinner auf den Eintrag mir dem verbundenen Gerät
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 28.05.2013
   */
  private void setSpinnerToConnectedDevice()
  {
    // String deviceAddr = null;
    int deviceIndex = -1;
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "setSpinnerToConnectedDevice..." );
    try
    {
      // wenn zu diesem Zeitpunkt das Array noch nicht gefüllt ist, dann mach das nun
      if( 0 == btArrayAdapter.getCount() )
      {
        fillNewAdapterWithPairedDevices();
      }
      // ArrayAdapter erfragen
      // mit welchem Gerät bin ich verbunden?
      lastConnectedDeviceMac = ( ( MainActivity )runningActivity ).getConnectedDevice();
      Log.v( TAG, "setSpinnerToConnectedDevice connected Device: <" + lastConnectedDeviceMac + ">" );
      // welcher index gehört zu dem Gerät?
      deviceIndex = btArrayAdapter.getIndexForMac( lastConnectedDeviceMac );
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

  private void setSpinnerToLastConnected()
  {
    //
    // guck mal, ob da was gespeichert war...
    //
    if( ApplicationDEBUG.DEBUG )
      Log.d( TAG, "fillNewAdapterWithPairedDevices: try set last connected device (" + ( ( lastConnectedDeviceMac == null ) ? "none" : lastConnectedDeviceMac ) + ")" );
    if( lastConnectedDeviceMac == null )
    {
      SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences( runningActivity );
      if( sPref.contains( LAST_CONNECTED_DEVICE_KEY ) )
      {
        lastConnectedDeviceMac = sPref.getString( LAST_CONNECTED_DEVICE_KEY, null );
        if( ApplicationDEBUG.DEBUG )
          Log.d( TAG, "fillNewAdapterWithPairedDevices: try from preference (" + ( ( lastConnectedDeviceMac == null ) ? "none" : lastConnectedDeviceMac ) + ")" );
      }
    }
    //
    // so, war da jetzt irgendwie was?
    //
    if( lastConnectedDeviceMac != null )
    {
      // welcher Index gehört zu dem Gerät?
      int deviceIndex = btArrayAdapter.getIndexForMac( lastConnectedDeviceMac );
      if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "set to last connected device MAC:<" + lastConnectedDeviceMac + "> on Index <" + deviceIndex + ">" );
      if( deviceIndex > -1 )
      {
        devSpinner.setSelection( deviceIndex, true );
      }
    }
  }

  /**
   * 
   * Oberfläche anpassen, je nach gemeldetem Verbindungsstatus
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 26.04.2013
   * 
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
          aliasEditButton.setEnabled( true );
          devSpinner.setEnabled( true );
          connectTextView.setText( R.string.connect_disconnect_device );
          connectTextView.setTextColor( res.getColor( R.color.connectFragment_disconnectText ) );
          setSpinnerToLastConnected();
          break;
        case ProjectConst.CONN_STATE_CONNECTING:
          connButton.setImageResource( R.drawable.bluetooth_icon_connecting );
          connButton.setAlpha( 0.5F );
          connButton.setEnabled( true );
          discoverButton.setEnabled( false );
          aliasEditButton.setEnabled( false );
          devSpinner.setEnabled( false );
          connectTextView.setText( R.string.connect_connecting_device );
          connectTextView.setTextColor( res.getColor( R.color.connectFragment_connectingText ) );
          break;
        case ProjectConst.CONN_STATE_CONNECTED:
          connButton.setImageResource( R.drawable.bluetooth_icon_color );
          connButton.setAlpha( 1.0F );
          connButton.setEnabled( true );
          discoverButton.setEnabled( false );
          aliasEditButton.setEnabled( false );
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
   * 
   * Stand: 17.02.2013
   */
  private void startDiscoverBt()
  {
    // If we're already discovering, stop it
    Log.i( TAG, "startDiscoverBt: start discovering..." );
    setItemsEnabledwhileDiscover( false );
    if( MainActivity.mBtAdapter == null ) return;
    if( MainActivity.mBtAdapter.isDiscovering() )
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
    MainActivity.mBtAdapter.startDiscovery();
  }

  /**
   * 
   * Das Discovering beenden
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 06.05.2013
   */
  private void stopDiscoverBt()
  {
    MainActivity.mBtAdapter.cancelDiscovery();
    // Unregister broadcast listeners
    runningActivity.unregisterReceiver( mReceiver );
  }

  /**
   * 
   * Schreibe das letzte verbiundene Gerät in die Preferences
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 26.07.2013
   */
  private void writePreferences()
  {
    if( lastConnectedDeviceMac == null || lastConnectedDeviceMac.isEmpty() ) return;
    SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences( runningActivity );
    SharedPreferences.Editor editor = sPref.edit();
    editor.putString( LAST_CONNECTED_DEVICE_KEY, lastConnectedDeviceMac );
    //
    // alles in die Propertys
    //
    if( editor.commit() )
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "writePreferences: wrote preference to storeage." );
    }
    else
    {
      Log.e( TAG, "writePreferences: CAN'T wrote preference to storage." );
    }
  }
}
