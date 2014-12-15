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

import android.annotation.SuppressLint;
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import de.dmarcini.submatix.android4.full.ApplicationDEBUG;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.full.dialogs.EditAliasDialogFragment;
import de.dmarcini.submatix.android4.full.dialogs.PinErrorDialog;
import de.dmarcini.submatix.android4.full.interfaces.IBtServiceListener;
import de.dmarcini.submatix.android4.full.utils.BluetoothDeviceArrayAdapter;
import de.dmarcini.submatix.android4.full.utils.CommToast;
import de.dmarcini.submatix.android4.full.utils.ProjectConst;

/**
 * Ein Detsailfragment, welches die Verbindung mit dem SPX Managed
 * <p/>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p/>
 *         Stand: 10.11.2013
 */
public class SPX42ConnectFragment extends Fragment implements IBtServiceListener, OnItemSelectedListener, OnClickListener
{
  @SuppressWarnings("javadoc")
  public static final String TAG = SPX42ConnectFragment.class.getSimpleName();
  private static final String LAST_CONNECTED_DEVICE_KEY = "keyLastConnectedDevice";
  private static final String DIAL_GET_PIN = "get_pin_dial";
  private static final String DIAL_NO_PIN_ERR = "no_pin_was_there_dial";
  private final Vector<BluetoothDevice> discoveredDevices = new Vector<BluetoothDevice>();
  protected ProgressDialog progressDialog = null;
  private String lastConnectedDeviceMac = null;
  private BluetoothDeviceArrayAdapter btArrayAdapter = null;
  private Button discoverButton = null;
  private Button aliasEditButton = null;
  private Spinner devSpinner = null;
  private ImageButton connButton = null;
  private TextView connectTextView = null;
  private boolean runDiscovering = false;
  //
  // der Broadcast Empfänger der Nachrichten über gefundene BT Geräte findet
  //
  //
  // @formatter:off
  //
  @SuppressLint("NewApi")
  private final BroadcastReceiver mReceiver = new BroadcastReceiver()
  {
    public final String TAG = BroadcastReceiver.class.getSimpleName();

    //
    @Override
    public void onReceive(Context context, Intent intent)
    {
      String action = intent.getAction();
      //
      // wenn ein Gerät gefunden wurde
      //
      if( BluetoothDevice.ACTION_FOUND.equals(action) )
      {
        //
        // ignoriere das, wenn ich das nicht selber angeschubst hab
        //
        if( !runDiscovering )
        {
          return;
        }
        //
        // Das Gerät extraieren
        //
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if( progressDialog != null )
        {
          if( ApplicationDEBUG.DEBUG )
          {
            Log.i(TAG, "device add to progressDialog...");
          }
          // den Gerätenamen in den String aus der Resource (lokalisiert) einbauen und in die waitbox setzen
          String dispStr = ((device.getName() == null) ? device.getAddress() : device.getName());
          progressDialog.setMessage(String.format(
              runningActivity.getResources().getString(R.string.progress_wait_for_discover_message_continue),
              dispStr));
        }
        //
        // Ist das gerät noch nicht gepaart oder gelistet trage es ein
        //
        if( (device.getBondState() != BluetoothDevice.BOND_BONDED) && (device.getName() != null)
            && (device.getBluetoothClass().getMajorDeviceClass() == ProjectConst.SPX_BTDEVICE_CLASS) )
        {
          if( ApplicationDEBUG.DEBUG )
          {
            Log.d(TAG, String.format("<%s> is an RFCOMM, Add...", device.getName()));
          }
          discoveredDevices.add(device);
        }
        else
        {
          // kein RFCOMM-Gerät
          if( ApplicationDEBUG.DEBUG )
          {
            Log.d(TAG, String.format("<%s> is not RFCOMM, Ignore...", device.getName()));
          }
        }
        //
        // discovering ist zuende -> Zeit das in die Liste zu übernehmen
        //
      }
      else if( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) )
      {
        Log.v(TAG, "discover finished, enable button.");
        runDiscovering = false;
        stopDiscoverBt();
        // und nun die Liste frisch befüllen....
        fillNewAdapterWithKnownDevices();
        setItemsEnabledwhileDiscover(true);
      }
      //
      // Das Ereignis, wenn ein Gerät gepaart werden muss
      //
      else if( BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action) )
      {
        if( (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) && (MainActivity.aliasManager != null) )
        {
          //
          // das klappt nur bei Android >= 4.4, vorher gibt es die API nicht
          //
          if( ApplicationDEBUG.DEBUG )
          {
            Log.d(TAG, "device pairing request");
          }
          //
          // Das Gerät extraieren
          //
          BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
          theToast.showConnectionToast(String.format(getResources().getString(R.string.toast_connect_device_pairing_request), device.getName()), false);
//            if( device.getBondState() == BluetoothDevice.BOND_BONDING )
//            {
//              if( ApplicationDEBUG.DEBUG )Log.d(TAG, "device is bonding... wait...");
//            }
//            else
          {
            String devicePin = MainActivity.aliasManager.getPINForMac(device.getAddress());
            if( devicePin != null )
            {
              if( ApplicationDEBUG.DEBUG )
              {
                Log.d(TAG, String.format("device pin from databese is <%s>", devicePin));
              }
              // Das Gerät automatisch paaren, PIN Speichern
              device.setPin(devicePin.getBytes());
              device.setPairingConfirmation(true);
              device.createBond();
            }
            else
            {
              if( ApplicationDEBUG.DEBUG )
              {
                Log.d(TAG, "device pairing request: database has no pin for device...");
              }
            }
          }
        }
      }
      //
      // wenn der Pairing-Status geändert wurde
      // TODO: die Liste anpassen
      //
      else if( BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action) )
      {
        final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
        final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
        //
        // Das Gerät extraieren
        //
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        //
        if( state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING )
        {
          Log.i(TAG, String.format("device <%s> pairing status changed: device paired...", device.getName()));
          // Das Gerät wurde gepaart
          theToast.showConnectionToast(String.format(getResources().getString(R.string.toast_connect_device_paired), device.getName()), false);
          // das gepaarte Gerät aus der Liste der discoverten löschen, ist ja jezt in der Liste der gepaarten vom System
          Iterator<BluetoothDevice> it = discoveredDevices.iterator();
          while( it.hasNext() )
          {
            BluetoothDevice lDevice = it.next();
            if( lDevice.getAddress().equals(device.getAddress()) )
            {
              it.remove();
            }
          }
          // und nun die Liste frisch befüllen....
          fillNewAdapterWithKnownDevices();
        }
        else if( state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED )
        {
          if( ApplicationDEBUG.DEBUG )
          {
            Log.d(TAG, "device pairing changed: device unpaired...");
          }
        }
      }
    }

  };
  private MainActivity runningActivity = null;
  private CommToast theToast = null;
  private boolean showCommToast = false;
  private String fragmentTitle = "unknown";

//    private void pairDevice(BluetoothDevice device) {
//      try {
//          Method method = device.getClass().getMethod("createBond", (Class[]) null);
//          method.invoke(device, (Object[]) null);
//      } catch (Exception e) {
//          e.printStackTrace();
//      }
//  }
//    
//    private void unpairDevice(BluetoothDevice device) {
//      try {
//          Method method = device.getClass().getMethod("removeBond", (Class[]) null);
//          method.invoke(device, (Object[]) null);
//
//      } catch (Exception e) {
//          e.printStackTrace();
//      }
//  } 

//
// @formatter:on
  //

  /**
   * Einen neuen Adapter mit bereits gepaarten Geräten befüllen
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 24.11.2014
   */
  private void fillNewAdapterWithKnownDevices()
  {
    Iterator<BluetoothDevice> it;
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "fillNewAdapterWithKnownDevices: fill an ArrayAdapter with devices...");
    }
    if( MainActivity.mBtAdapter == null )
    {
      return;
    }
    //
    // die Liste leeren
    //
    if( btArrayAdapter == null )
    {
      btArrayAdapter = ( BluetoothDeviceArrayAdapter ) devSpinner.getAdapter();
    }
    btArrayAdapter.clear();
    //
    // eine Liste der bereits gepaarten Devices
    //
    Vector<BluetoothDevice> pairedDevices = new Vector<BluetoothDevice>(MainActivity.mBtAdapter.getBondedDevices());
    //
    // Liste der Geräte aus der Datenbank, Abgleich mit gepaarten und discoverten Geräten
    //
    Vector<HashMap<String, String>> devListStored = MainActivity.aliasManager.getDeviceAdressesList();
    Iterator<HashMap<String, String>> itStored = devListStored.iterator();
    while( itStored.hasNext() )
    {
      HashMap<String, String> deviceMap = itStored.next();
      if( ApplicationDEBUG.DEBUG )
      {
        Log.d(TAG, "fillNewAdapterWithKnownDevices: check stored Device <" + deviceMap.get(ProjectConst.A_DEVNAME) + ">...");
      }
      // eintrag in Arrayadapter vorbereiten
      String[] entr = new String[ BluetoothDeviceArrayAdapter.BT_DEVAR_COUNT ];
      if( ApplicationDEBUG.DEBUG )
      {
        Log.d(TAG, "stored device: " + deviceMap.get(ProjectConst.A_DEVNAME));
      }
      entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_ALIAS ] = deviceMap.get(ProjectConst.A_ALIAS);
      entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_MAC ] = deviceMap.get(ProjectConst.A_MAC);
      entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_NAME ] = deviceMap.get(ProjectConst.A_DEVNAME);
      entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_ISPAIRED ] = "false";
      entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_ISONLINE ] = "false";
      // zuerst die geparten Geräte vergleichen
      it = pairedDevices.iterator();
      while( it.hasNext() )
      {
        BluetoothDevice device = it.next();
        if( device.getAddress().equals(deviceMap.get(ProjectConst.A_MAC)) )
        {
          if( ApplicationDEBUG.DEBUG )
          {
            Log.d(TAG, "fillNewAdapterWithKnownDevices: Device <" + deviceMap.get(ProjectConst.A_DEVNAME) + "> was in paired devices, remove from list...");
          }
          entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_ISPAIRED ] = "true";
          try
          {
            it.remove();
          }
          catch( UnsupportedOperationException ex )
          {
            pairedDevices.remove(device);
          }
          break;
        }
      }
      // und noch gegen die discoverten Geräte testen
      it = discoveredDevices.iterator();
      while( it.hasNext() )
      {
        BluetoothDevice device = it.next();
        if( device.getAddress().equals(deviceMap.get(ProjectConst.A_MAC)) )
        {
          if( ApplicationDEBUG.DEBUG )
          {
            Log.d(TAG, "fillNewAdapterWithKnownDevices: Device <" + deviceMap.get(ProjectConst.A_DEVNAME) + "> was in discovered devices, remove from list...");
          }
          entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_ISONLINE ] = "true";
          try
          {
            it.remove();
          }
          catch( UnsupportedOperationException ex )
          {
            pairedDevices.remove(device);
          }
          break;
        }
      }
      //
      // Den Eintrag aus der Datenbank eintragen
      //
      btArrayAdapter.add(entr);
    }
    //
    // Sind noch unbearbeitete Geräte vorhanden, oder in der Liste der discoverten Devices dann ab in den adapter...
    //
    if( pairedDevices.size() > 0 || discoveredDevices.size() > 0 )
    {
      if( ApplicationDEBUG.DEBUG )
      {
        Log.d(TAG, "fillNewAdapterWithKnownDevices: fill List with paired devices...");
      }
      // Alle gepaarten Geräte durch
      it = pairedDevices.iterator();
      while( it.hasNext() )
      {
        BluetoothDevice device = it.next();
        // Ist es ein Gerät vom gewünschten Typ?
        if( (device.getBluetoothClass().getDeviceClass() == ProjectConst.SPX_BTDEVICE_CLASS) && (device.getName() != null) )
        {
          try
          {
            // Feld 0 = Geräte Alias / Gerätename
            // Feld 1 = Geräte-MAC
            // Feld 2 = Geräte-Name
            // Feld 3 = Gerät gepart?
            // Feld 4 = Gerät online?
            String[] entr = new String[ BluetoothDeviceArrayAdapter.BT_DEVAR_COUNT ];
            if( ApplicationDEBUG.DEBUG )
            {
              Log.d(TAG, "paired Device: " + device.getName());
            }
            entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_ALIAS ] = MainActivity.aliasManager.getAliasForMac(device.getAddress(), device.getName());
            entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_MAC ] = device.getAddress();
            entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_NAME ] = device.getName();
            entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_ISPAIRED ] = "true";
            entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_ISONLINE ] = "false";
            btArrayAdapter.addOrUpdate(entr);
          }
          catch( NullPointerException ex )
          {
            Log.e(TAG, "Nullpointer (alias manager not initialized? <" + ex.getLocalizedMessage() + ">");
            continue;
          }
        }
      }
      //
      // hinterher die discoverten Geräte
      //
      if( ApplicationDEBUG.DEBUG )
      {
        Log.d(TAG, "fillNewAdapterWithKnownDevices: fill List with discovered devices...");
      }
      it = discoveredDevices.iterator();
      while( it.hasNext() )
      {
        BluetoothDevice device = it.next();
        String devAlias = MainActivity.aliasManager.getAliasForMac(device.getAddress(), device.getName());
        // Feld 0 = Geräte Alias / Gerätename
        // Feld 1 = Geräte-MAC
        // Feld 2 = Geräte-Name
        // Feld 3 = Gerät gepart?
        // Feld 4 = Gerät online?
        String[] entr = new String[ BluetoothDeviceArrayAdapter.BT_DEVAR_COUNT ];
        entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_ALIAS ] = devAlias;
        entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_MAC ] = device.getAddress();
        entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_NAME ] = device.getName();
        entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_ISPAIRED ] = "false";
        entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_ISONLINE ] = "true";
        btArrayAdapter.addOrUpdate(entr);
      }
    }
    else
    {
      if( ApplicationDEBUG.DEBUG )
      {
        Log.d(TAG, "fillNewAdapterWithKnownDevices: no Devices : " + runningActivity.getString(R.string.no_device));
      }
      String[] entr = new String[ BluetoothDeviceArrayAdapter.BT_DEVAR_COUNT ];
      entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_ALIAS ] = runningActivity.getString(R.string.no_device);
      entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_MAC ] = "";
      entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_NAME ] = runningActivity.getString(R.string.no_device);
      entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_ISPAIRED ] = "false";
      entr[ BluetoothDeviceArrayAdapter.BT_DEVAR_ISONLINE ] = "false";
      btArrayAdapter.add(entr);
    }
    setSpinnerToLastConnected();
  }

  @Override
  public void handleMessages(int what, BtServiceMessage smsg)
  {
    // was war denn los? Welche Nachricht kam rein?
    switch( what )
    {
      //
      // ################################################################
      // Service TICK empfangen
      // ################################################################
      case ProjectConst.MESSAGE_TICK:
        msgRecivedTick(smsg);
        break;
      // ################################################################
      // Dialog positiv beendet
      // ################################################################
      case ProjectConst.MESSAGE_DIALOG_POSITIVE:
        msgRecivedDialogPositive(smsg);
        break;
      // ################################################################
      // Dialog negativ beendet
      // ################################################################
      case ProjectConst.MESSAGE_DIALOG_NEGATIVE:
        if( ApplicationDEBUG.DEBUG )
        {
          Log.d(TAG, "dialog negative closed");
        }
        break;
      // ################################################################
      // Computer wird gerade verbunden
      // ################################################################
      case ProjectConst.MESSAGE_CONNECTING:
        msgConnecting(smsg);
        break;
      // ################################################################
      // Computer wurde getrennt
      // ################################################################
      case ProjectConst.MESSAGE_CONNECTED:
        msgConnected(smsg);
        break;
      // ################################################################
      // Computer wurde getrennt
      // ################################################################
      case ProjectConst.MESSAGE_DISCONNECTED:
        msgDisconnected(smsg);
        break;
      // ################################################################
      // Computer wurde getrennt
      // ################################################################
      case ProjectConst.MESSAGE_CONNECTERROR:
        msgConnectError(smsg);
        break;
      // ################################################################
      // Verbindungsversuch mit einem Gerät, welches nicht gepaart ist
      // ################################################################
      case ProjectConst.MESSAGE_CONNECT_NOTBOUND:
        msgConnectNotbound(smsg);
        break;
      // ################################################################
      // SPX sendet "ALIVE" und Ackuspannung
      // ################################################################
      case ProjectConst.MESSAGE_SPXALIVE:
        msgRecivedAlive(smsg);
        break;
      // ################################################################
      // Alias editiert
      // ################################################################
      case ProjectConst.MESSAGE_DEVALIAS_SET:
        msgReciveDeviceAliasSet(smsg);
        break;
      // ################################################################
      // Wenn Serial dann in die DB
      // ################################################################
      case ProjectConst.MESSAGE_SERIAL_READ:
        setSerialIfNotExist(MainActivity.spxConfig.getConnectedDeviceMac(), MainActivity.spxConfig.getSerial());
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
        if( ApplicationDEBUG.DEBUG )
        {
          Log.i(TAG, "handleMessages: unhadled message message with id <" + smsg.getId() + "> recived!");
        }
    }
  }

  /**
   * Die Anzeige der Verbunden/trennen Seite
   * <p/>
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 04.11.2012
   *
   * @param inflater
   * @param container
   * @return
   */
  private View makeConnectionView(LayoutInflater inflater, ViewGroup container)
  {
    View rootView;
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "makeConnectionView...");
    }
    //
    // View aus Resource laden
    //
    rootView = inflater.inflate(R.layout.fragment_connect, container, false);
    //
    // Objekte lokalisieren
    //
    discoverButton = ( Button ) rootView.findViewById(R.id.connectDiscoverButton);
    devSpinner = ( Spinner ) rootView.findViewById(R.id.connectBlueToothDeviceSpinner);
    connButton = ( ImageButton ) rootView.findViewById(R.id.connectButton);
    connectTextView = ( TextView ) rootView.findViewById(R.id.connectStatusText);
    aliasEditButton = ( Button ) rootView.findViewById(R.id.connectAliasEditButton);
    if( discoverButton == null || devSpinner == null || connButton == null )
    {
      throw new NullPointerException("makeConnectionView: can't init GUI (not found an Element)");
    }
    return (rootView);
  }

  @Override
  public void msgConnected(BtServiceMessage msg)
  {
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "msgConnected...");
    }
    if( showCommToast )
    {
      String deviceName = (( BluetoothDeviceArrayAdapter ) devSpinner.getAdapter()).getAlias(devSpinner.getSelectedItemPosition());
      String connStringFormat = getResources().getString(R.string.toast_connect_connected);
      theToast.showConnectionToast(String.format(connStringFormat, deviceName), false);
      showCommToast = false;
    }
    setSpinnerToConnectedDevice();
    setToggleButtonTextAndStat(ProjectConst.CONN_STATE_CONNECTED);
    writePreferences();
  }

  @Override
  public void msgConnectError(BtServiceMessage msg)
  {
    String deviceName = (( BluetoothDeviceArrayAdapter ) devSpinner.getAdapter()).getAlias(devSpinner.getSelectedItemPosition());
    String connStringFormat = getResources().getString(R.string.toast_connect_cant_bt_connect);
    theToast.showConnectionToastAlert(String.format(connStringFormat, deviceName));
  }

  @Override
  public void msgConnecting(BtServiceMessage msg)
  {
    setToggleButtonTextAndStat(ProjectConst.CONN_STATE_CONNECTING);
  }

  /**
   * Nachricht, wenn beim Verbindungsversuch ein nicht gepaartes Gerät verucht wurde
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * <p/>
   * Stand: 16.11.2014
   *
   * @param smsg Der Container smsg.getContainer() ist vom Typ BluetoothDevice
   */
  private void msgConnectNotbound(BtServiceMessage smsg)
  {
    String msg;
    BluetoothDevice device;
    String dName;
    //
    if( MainActivity.aliasManager == null )
    {
      //
      // die Datenbank öffnen, wenn erforderlich
      //
      if( !runningActivity.openAliasManager() )
      {
        //
        // Geht nicht, Die Funktion gibt dann eine ABORT Box aus
        //
        Log.e(TAG, "msgConnectedNotbound: can't open AliasManager!");
        return;
      }
    }
    device = ( BluetoothDevice ) smsg.getContainer();
    dName = String.format("%s/%s", MainActivity.aliasManager.getAliasForMac(device.getAddress(), device.getName()), device.getName());
    //
    if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT )
    {
      // Bei KITKAT kann ich hier die PIN eintragen lassen, in die DB schreiben und den Pairungprozess starten
      msg = String.format(getResources().getString(R.string.dialog_get_pin_msg), dName);
      PinErrorDialog eDial = new PinErrorDialog(getResources().getString(R.string.dialog_no_pin_headline), msg, device);
      eDial.show(getFragmentManager().beginTransaction(), DIAL_GET_PIN);
    }
    else
    {
      // bei App < 4.4 gebe ich nur den Hinweis an den User, er muss das Gerät paaren
      // Message ist der container des Messageobjektes == String
      msg = String.format(getResources().getString(R.string.dialog_no_pin_errmsg), dName);
      PinErrorDialog eDial = new PinErrorDialog(getResources().getString(R.string.dialog_no_pin_headline), msg);
      eDial.show(getFragmentManager().beginTransaction(), DIAL_NO_PIN_ERR);
    }
  }

  @Override
  public void msgDisconnected(BtServiceMessage msg)
  {
    int index;
    MainActivity.spxConfig.clear();
    setToggleButtonTextAndStat(ProjectConst.CONN_STATE_NONE);
    index = devSpinner.getSelectedItemPosition();
    btArrayAdapter = ( BluetoothDeviceArrayAdapter ) devSpinner.getAdapter();
    btArrayAdapter.setDevicesOffline();
    // Update erzwingen
    devSpinner.setAdapter(btArrayAdapter);
    devSpinner.setSelection(index, true);
    if( showCommToast )
    {
      theToast.showConnectionToast(getResources().getString(R.string.toast_connect_disconnected), false);
      showCommToast = false;
    }
  }

  @Override
  public void msgRecivedAlive(BtServiceMessage msg)
  {
    //
  }

  /**
   * NAchricht, ein Dialog ist Positiv beendet worden
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * <p/>
   * Stand: 18.11.2014
   *
   * @param smsg die Nachricht
   */
  @SuppressLint("NewApi")
  private void msgRecivedDialogPositive(BtServiceMessage smsg)
  {
    //
    // hier ist nur der PIN-Dialog interessant, und nur bei API >= 19
    //
    if( (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) && (MainActivity.aliasManager != null) )
    {
      if( smsg.getContainer() instanceof PinErrorDialog )
      {
        PinErrorDialog pinDial = ( PinErrorDialog ) smsg.getContainer();
        if( pinDial.getTag().equals(DIAL_GET_PIN) )
        {
          if( ApplicationDEBUG.DEBUG )
          {
            Log.d(TAG, "get-pin-dialog ends positive");
          }
          BluetoothDevice device = pinDial.getDevice();
          String newPin = pinDial.getPin();
          // kleine Prüfung, es müssen 4 Ziffern sein
          if( newPin.matches("\\d{4}") )
          {
            //
            // Die PIN könnte passen, das Format stimmt schon mal
            //
            if( ApplicationDEBUG.DEBUG )
            {
              Log.d(TAG, "pin in an valid format!");
            }
            // ab in die Database, falls noch nicht vorhanden
            MainActivity.aliasManager.setAliasForMacIfNotExist(device.getAddress(), device.getName());
            MainActivity.aliasManager.setPinForMac(device.getAddress(), newPin);
            runningActivity.doConnectBtDevice(device.getAddress());
            //
            if( ApplicationDEBUG.DEBUG )
            {
              Log.d(TAG, "pin saved");
            }
          }
          else
          {
            //
            // PIN war falsch formatiert :-(
            // nochmal oder Abbruch
            //
            Log.e(TAG, "NOT valid PIN Format <" + newPin + ">");
            pinDial.dismiss();
            // Bei KITKAT kann ich hier die PIN eintragen lassen, in die DB schreiben und den Pairungprozess starten
            String msg = String.format(getResources().getString(R.string.dialog_pin_wrong_format), device.getName());
            pinDial = new PinErrorDialog(getResources().getString(R.string.dialog_no_pin_headline), msg, device);
            pinDial.show(getFragmentManager().beginTransaction(), DIAL_GET_PIN);
          }
        }
      }
    }
  }

  /**
   * Empfange Nachricht, dass der Device Alias geändert werden soll
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 26.07.2013
   *
   * @param msg
   */
  private void msgReciveDeviceAliasSet(BtServiceMessage msg)
  {
    int dIndex;
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "msgReciveDeviceAliasSet...");
    }
    // ist das das Stringarray?
    if( msg.getContainer() instanceof String[] )
    {
      if( ApplicationDEBUG.DEBUG )
      {
        Log.d(TAG, "msgReciveDeviceAliasSet: is String[]...");
      }
      // genau 4 Parameter (devicename,alias,mac,pin)
      String[] parm = ( String[] ) msg.getContainer();
      if( parm.length == 4 )
      {
        if( ApplicationDEBUG.DEBUG )
        {
          Log.d(TAG, "msgReciveDeviceAliasSet: is 4 params");
        }
        dIndex = btArrayAdapter.getIndexForMac(parm[ 2 ]);
        // hat er's gefunden?
        if( dIndex == -1 )
        {
          Log.e(TAG, "msgReciveDeviceAliasSet: can't find device mac addr in device array adapter!");
          return;
        }
        if( ApplicationDEBUG.DEBUG )
        {
          Log.d(TAG, "msgReciveDeviceAliasSet: Set alias to <" + parm[ 1 ] + ">");
        }
        //
        // in der Datenbank verramschen. Seriennummer ist nicht bekannt (bin offline)
        //
        if( MainActivity.aliasManager.setAliasForMac(parm[ 2 ], parm[ 0 ], parm[ 1 ], null) )
        {
          if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT )
          {
            //
            // wenn Android >= API 19 (4.4) läuft
            //
            MainActivity.aliasManager.setPinForMac(parm[ 2 ], parm[ 3 ]);
          }
          if( 0 == btArrayAdapter.getCount() )
          {
            fillNewAdapterWithKnownDevices();
          }
          btArrayAdapter.setDevAlias(dIndex, parm[ 1 ]);
          // Update erzwingen
          devSpinner.setAdapter(btArrayAdapter);
          // Selektieren
          devSpinner.setSelection(dIndex, true);
          btArrayAdapter = ( BluetoothDeviceArrayAdapter ) devSpinner.getAdapter();
        }
        else
        {
          Log.w(TAG, "can't set alias in database!");
        }
      }
    }
  }

  @Override
  public void msgRecivedTick(BtServiceMessage msg)
  {
    // if( ApplicationDEBUG.DEBUG ) Log.d( TAG, String.format( "recived Tick <%x08x>", msg.getTimeStamp() ) );
  }

  @Override
  public void msgReciveWriteTmeout(BtServiceMessage msg)
  {
    //
  }

  @SuppressLint("InlinedApi")
  @Override
  public void onActivityCreated(Bundle savedInstanceState)
  {
    super.onActivityCreated(savedInstanceState);
    runningActivity = ( MainActivity ) getActivity();
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "onActivityCreated: ...");
    }
    try
    {
      discoverButton = ( Button ) runningActivity.findViewById(R.id.connectDiscoverButton);
      devSpinner = ( Spinner ) runningActivity.findViewById(R.id.connectBlueToothDeviceSpinner);
      connButton = ( ImageButton ) runningActivity.findViewById(R.id.connectButton);
      aliasEditButton = ( Button ) runningActivity.findViewById(R.id.connectAliasEditButton);
      connectTextView = ( TextView ) runningActivity.findViewById(R.id.connectStatusText);
      if( (MainActivity.mBtAdapter != null) && (MainActivity.mBtAdapter.isEnabled()) )
      {
        if( ApplicationDEBUG.DEBUG )
        {
          Log.d(TAG, "onActivityCreated: set SPX42ConnectFragment eventhandler...");
        }
        devSpinner.setOnItemSelectedListener(this);
        discoverButton.setOnClickListener(this);
        aliasEditButton.setOnClickListener(this);
        connButton.setOnClickListener(this);
        //
        // den eigenen ArrayAdapter machen
        //
        btArrayAdapter = new BluetoothDeviceArrayAdapter(runningActivity, R.layout.bt_array_with_pic_adapter_view, MainActivity.getAppStyle());
        devSpinner.setAdapter(btArrayAdapter);
        //
        setToggleButtonTextAndStat(ProjectConst.CONN_STATE_NONE);
      }
      else
      {
        if( ApplicationDEBUG.DEBUG )
        {
          Log.e(TAG, "onActivityCreated: NOT BT Adapter/not enabled => NOT set onClick eventhandler...");
        }
      }
    }
    catch( NullPointerException ex )
    {
      Log.e(TAG, "onActivityCreated: gui objects not allocated!");
    }
    //
    // den Titel in der Actionbar setzten
    // Aufruf via create
    //
    Bundle arguments = getArguments();
    if( arguments != null && arguments.containsKey(ProjectConst.ARG_ITEM_CONTENT) )
    {
      fragmentTitle = arguments.getString(ProjectConst.ARG_ITEM_CONTENT);
      runningActivity.onSectionAttached(fragmentTitle);
    }
    else
    {
      Log.w(TAG, "onActivityCreated: TITLE NOT SET!");
    }
    //
    // im Falle eines restaurierten Frames
    //
    if( savedInstanceState != null && savedInstanceState.containsKey(ProjectConst.ARG_ITEM_CONTENT) )
    {
      fragmentTitle = savedInstanceState.getString(ProjectConst.ARG_ITEM_CONTENT);
      runningActivity.onSectionAttached(fragmentTitle);
    }
    //
    // Intend-Filter und Intend für Pairing Anfragen
    //
    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
    if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT )
    {
      // Nur bei Android Versionen größer oder gleich 4.4
      filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
    }
    runningActivity.registerReceiver(mReceiver, filter);
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState)
  {
    super.onSaveInstanceState(savedInstanceState);
    fragmentTitle = savedInstanceState.getString(ProjectConst.ARG_ITEM_CONTENT);
    savedInstanceState.putString(ProjectConst.ARG_ITEM_CONTENT, fragmentTitle);
  }

  @Override
  public void onAttach(Activity activity)
  {
    super.onAttach(activity);
    runningActivity = ( MainActivity ) activity;
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "onAttach: ATTACH");
    }
    if( MainActivity.aliasManager == null )
    {
      //
      // die Datenbank öffnen, wenn erforderlich
      //
      if( !runningActivity.openAliasManager() )
      {
        //
        // Geht nicht, Die Funktion gibt dann eine ABORT Box aus
        //
        Log.e(TAG, "onAttach: can't open AliasManager!");
        return;
      }
    }
    lastConnectedDeviceMac = null;
  }

  @Override
  public void onClick(View cView)
  {
    int connState = runningActivity.getConnectionStatus();
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "onClick: ON CLICK!");
    }
    //
    // Wenn das der CONNECT Button war
    //
    if( cView instanceof ImageButton )
    {
      ImageButton tb = ( ImageButton ) cView;
      if( devSpinner.getSelectedItemPosition() == -1 )
      {
        Log.w(TAG, "onClick: not devices selected yet...");
        setToggleButtonTextAndStat(connState);
        return;
      }
      if( btArrayAdapter.isEmpty() || btArrayAdapter.getAlias(0).startsWith(runningActivity.getString(R.string.no_device).substring(0, 5)) )
      {
        Log.w(TAG, "onClick: not devices in Adapter yet...");
        setToggleButtonTextAndStat(connState);
        return;
      }
      switch( connState )
      {
        case ProjectConst.CONN_STATE_NONE:
        default:
          if( ApplicationDEBUG.DEBUG )
          {
            Log.d(TAG, "onClick: switch connect to ON");
          }
          //
          // da soll verbunden werden!
          //
          showCommToast = true;
          tb.setImageResource(R.drawable.bluetooth_icon_color);
          String deviceMac = (( BluetoothDeviceArrayAdapter ) devSpinner.getAdapter()).getMAC(devSpinner.getSelectedItemPosition());
          String deviceName = (( BluetoothDeviceArrayAdapter ) devSpinner.getAdapter()).getDevName(devSpinner.getSelectedItemPosition());
          setAliasForDeviceIfNotExist(deviceMac, deviceName);
          runningActivity.doConnectBtDevice(deviceMac);
          break;
        case ProjectConst.CONN_STATE_CONNECTING:
          Log.i(TAG, "cancel connecting..");
          runningActivity.doDisconnectBtDevice();
        case ProjectConst.CONN_STATE_CONNECTED:
          if( ApplicationDEBUG.DEBUG )
          {
            Log.d(TAG, "onClick: switch connect to OFF");
          }
          //
          // wenn da noch einer werkelt, anhalten und kompostieren
          //
          showCommToast = true;
          runningActivity.doDisconnectBtDevice();
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
      if( ( Button ) cView == discoverButton )
      {
        Log.i(TAG, "onClick: start discovering for BT Devices...");
        // ist da nur die Kennzeichnung für LEER?
        if( btArrayAdapter.isEmpty() || btArrayAdapter.getAlias(0).startsWith(runningActivity.getString(R.string.no_device).substring(0, 5)) )
        {
          Log.w(TAG, "onClick: not devices in Adapter yet...");
          btArrayAdapter.clear();
        }
        if( ApplicationDEBUG.DEBUG )
        {
          Log.d(TAG, "onClick: start discovering for BT Devices...OK");
        }
        startDiscoverBt();
        return;
      }
      //
      // es war der EDIT-Alias Button
      //
      else if( ( Button ) cView == aliasEditButton )
      {
        Log.i(TAG, "onClick: start edit current alias...");
        if( btArrayAdapter.isEmpty() || btArrayAdapter.getAlias(0).startsWith(runningActivity.getString(R.string.no_device).substring(0, 5)) )
        {
          Log.w(TAG, "onClick: not devices in Adapter yet...");
          return;
        }
        //
        // erzeuge den Dialog zum Bearbeiten des Alias
        //
        int pos = devSpinner.getSelectedItemPosition();
        DialogFragment dialog = null;
        if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT )
        {
          String oldPin = MainActivity.aliasManager.getPINForMac(btArrayAdapter.getMAC(pos));
          if( oldPin == null )
          {
            oldPin = "0000";
          }
          dialog = new EditAliasDialogFragment(btArrayAdapter.getDevName(pos), btArrayAdapter.getAlias(pos), btArrayAdapter.getMAC(pos), oldPin);
        }
        else
        {
          dialog = new EditAliasDialogFragment(btArrayAdapter.getDevName(pos), btArrayAdapter.getAlias(pos), btArrayAdapter.getMAC(pos));
        }
        dialog.show(getFragmentManager(), "EditAliasDialogFragment");
      }
    }
  }

  /**
   * Nach dem Erzeugen des Objektes noch Einstellungen....
   */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "onCreate...");
    }
    theToast = new CommToast(getActivity());
    showCommToast = false;
  }

  /**
   * Wenn das View erzeugt wird (nach onCreate), noch ein paar Sachen erledigen
   */
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View rootView;
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "onCreateView...");
    }
    //
    // wenn kein Container vorhanden ist, dann gibts auch keinen View
    //
    if( container == null )
    {
      Log.e(TAG, "onCreateView: container is NULL ...");
      return (null);
    }
    rootView = makeConnectionView(inflater, container);
    if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT )
    {
      try
      {
        // Falls PIN editierbar (ab Version 4.4)
        Button aliasButton = ( Button ) rootView.findViewById(R.id.connectAliasEditButton);
        aliasButton.setText(R.string.connect_editaliasv4_button_title);
      }
      catch( NullPointerException ex )
      {
        Log.e(TAG, "cant find Button resource for edit alias!");
      }
    }
    return rootView;
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
    // Unregister broadcast listeners
    runningActivity.unregisterReceiver(mReceiver);
    // Stelle sicher, dass hier nix mehr discovered wird
    if( MainActivity.mBtAdapter != null )
    {
      MainActivity.mBtAdapter.cancelDiscovery();
    }
    runningActivity.removeServiceListener(this);
  }

  @Override
  public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
  {
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "onItemSelected: ITEM Selected!");
    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> arg0)
  {
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "onItemSelected: ITEM NOT Selected!");
    }
  }

  @Override
  public void onPause()
  {
    super.onPause();
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "onPause...");
    }
    //
    // die abgeleiteten Objekte führen das auch aus
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "onPause: clear service listener for preferences fragment...");
    }
    runningActivity.removeServiceListener(this);
  }

  /**
   * Wenn das View wieder nach vorn kommt / reaktiviert wurde
   */
  @Override
  public synchronized void onResume()
  {
    super.onResume();
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "onResume...");
    }
    if( MainActivity.aliasManager == null )
    {
      //
      // die Datenbank öffnen, wenn erforderlich
      //
      if( !runningActivity.openAliasManager() )
      {
        //
        // Geht nicht, Die Funktion gibt dann eine ABORT Box aus
        //
        Log.e(TAG, "onResume: can't open AliasManager!");
        return;
      }
    }
    runningActivity.addServiceListener(this);
    // wenn zu diesem Zeitpunkt das Array noch nicht gefüllt ist, dann mach das nun
    if( 0 == btArrayAdapter.getCount() )
    {
      fillNewAdapterWithKnownDevices();
    }
    // setze den verbindungsstatus visuell
    setToggleButtonTextAndStat(runningActivity.getConnectionStatus());
  }

  /**
   * Setze einen Alias für das Gerät, wenn noch keiner gesetzt wurde
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * Stand: 03.12.2013
   *
   * @param _mac
   * @param _deviceName
   */
  private void setAliasForDeviceIfNotExist(String _mac, String _deviceName)
  {
    if( MainActivity.aliasManager != null )
    {
      MainActivity.aliasManager.setAliasForMacIfNotExist(_mac, _deviceName);
    }
  }

  /**
   * Lasse die Butons nicht mehr bedienen während des Discovering
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 17.02.2013
   *
   * @param enabled
   */
  private void setItemsEnabledwhileDiscover(boolean enabled)
  {
    if( progressDialog != null )
    {
      if( ApplicationDEBUG.DEBUG )
      {
        Log.d(TAG, "setItemsEnabledwhileDiscover: dialog dismiss....");
      }
      progressDialog.dismiss();
      progressDialog = null;
    }
    if( !enabled )
    {
      if( ApplicationDEBUG.DEBUG )
      {
        Log.d(TAG, "setItemsEnabledwhileDiscover: dialog show....");
      }
      progressDialog = new ProgressDialog(runningActivity);
      progressDialog.setTitle(R.string.progress_wait_for_discover_title);
      progressDialog.setIndeterminate(true);
      progressDialog.setMessage(runningActivity.getResources().getString(R.string.progress_wait_for_discover_message_start));
      progressDialog.show();
    }
    setToggleButtonTextAndStat(ProjectConst.CONN_STATE_NONE);
  }

  /**
   * Wenn noch keine Seriennummer für das Gerät eingetragen ist, hole das nach
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * Stand: 03.12.2013
   *
   * @param _mac
   * @param _serial
   */
  private void setSerialIfNotExist(String _mac, String _serial)
  {
    if( MainActivity.aliasManager != null )
    {
      MainActivity.aliasManager.setSerialIfNotExist(_mac, _serial);
    }
  }

  /**
   * Setze den Spinner auf den Eintrag mir dem verbundenen Gerät
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 28.05.2013
   */
  private void setSpinnerToConnectedDevice()
  {
    // String deviceAddr = null;
    int deviceIndex = -1;
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "setSpinnerToConnectedDevice...");
    }
    try
    {
      // wenn zu diesem Zeitpunkt das Array noch nicht gefüllt ist, dann mach das nun
      if( 0 == btArrayAdapter.getCount() )
      {
        fillNewAdapterWithKnownDevices();
      }
      // ArrayAdapter erfragen
      // mit welchem Gerät bin ich verbunden?
      lastConnectedDeviceMac = runningActivity.getConnectedDevice();
      Log.v(TAG, "setSpinnerToConnectedDevice connected Device: <" + lastConnectedDeviceMac + ">");
      if( lastConnectedDeviceMac != null )
      {
        // welcher index gehört zu dem Gerät?
        deviceIndex = btArrayAdapter.getIndexForMac(lastConnectedDeviceMac);
        Log.v(TAG, "setSpinnerToConnectedDevice index in Adapter: <" + deviceIndex + ">");
        // Online Markieren
        btArrayAdapter.setDeviceIsOnline(deviceIndex);
        // Update erzwingen
        devSpinner.setAdapter(btArrayAdapter);
        // Selektieren
        devSpinner.setSelection(deviceIndex, true);
        Log.v(TAG, "setSpinnerToConnectedDevice set Spinner to index <" + deviceIndex + ">");
      }
    }
    catch( Exception ex )
    {
      Log.e(TAG, "setSpinnerToConnectedDevice: <" + ex.getLocalizedMessage() + ">");
      Log.w(TAG, "setSpinnerToConnectedDevice exception: Spinner to 0");
      devSpinner.setSelection(0, false);
    }
  }

  private void setSpinnerToLastConnected()
  {
    //
    // guck mal, ob da was gespeichert war...
    //
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "fillNewAdapterWithPairedDevices: try set last connected device (" + ((lastConnectedDeviceMac == null) ? "none" : lastConnectedDeviceMac) + ")");
    }
    if( lastConnectedDeviceMac == null )
    {
      SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(runningActivity);
      if( sPref.contains(LAST_CONNECTED_DEVICE_KEY) )
      {
        lastConnectedDeviceMac = sPref.getString(LAST_CONNECTED_DEVICE_KEY, null);
        if( ApplicationDEBUG.DEBUG )
        {
          Log.d(TAG, "fillNewAdapterWithPairedDevices: try from preference (" + ((lastConnectedDeviceMac == null) ? "none" : lastConnectedDeviceMac) + ")");
        }
      }
    }
    //
    // so, war da jetzt irgendwie was?
    //
    if( lastConnectedDeviceMac != null )
    {
      // welcher Index gehört zu dem Gerät?
      int deviceIndex = btArrayAdapter.getIndexForMac(lastConnectedDeviceMac);
      if( ApplicationDEBUG.DEBUG )
      {
        Log.i(TAG, "set to last connected device MAC:<" + lastConnectedDeviceMac + "> on Index <" + deviceIndex + ">");
      }
      if( deviceIndex > -1 )
      {
        devSpinner.setSelection(deviceIndex, true);
      }
    }
  }

  /**
   * Oberfläche anpassen, je nach gemeldetem Verbindungsstatus
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 26.04.2013
   *
   * @param connState welcher Verbindungsstatus
   */
  private void setToggleButtonTextAndStat(int connState)
  {
    Resources res = runningActivity.getResources();
    try
    {
      switch( connState )
      {
        case ProjectConst.CONN_STATE_NONE:
        default:
          connButton.setImageResource(R.drawable.bluetooth_icon_bw);
          connButton.setAlpha(1.0F);
          connButton.setEnabled(true);
          discoverButton.setEnabled(true);
          aliasEditButton.setEnabled(true);
          devSpinner.setEnabled(true);
          connectTextView.setText(R.string.connect_disconnect_device);
          connectTextView.setTextColor(res.getColor(R.color.connectFragment_disconnectText));
          setSpinnerToLastConnected();
          break;
        case ProjectConst.CONN_STATE_CONNECTING:
          connButton.setImageResource(R.drawable.bluetooth_icon_connecting);
          connButton.setAlpha(0.5F);
          connButton.setEnabled(true);
          discoverButton.setEnabled(false);
          aliasEditButton.setEnabled(false);
          devSpinner.setEnabled(false);
          connectTextView.setText(R.string.connect_connecting_device);
          connectTextView.setTextColor(res.getColor(R.color.connectFragment_connectingText));
          break;
        case ProjectConst.CONN_STATE_CONNECTED:
          connButton.setImageResource(R.drawable.bluetooth_icon_color);
          connButton.setAlpha(1.0F);
          connButton.setEnabled(true);
          discoverButton.setEnabled(false);
          aliasEditButton.setEnabled(false);
          devSpinner.setEnabled(false);
          connectTextView.setText(R.string.connect_connected_device);
          connectTextView.setTextColor(res.getColor(R.color.connectFragment_connectText));
          break;
      }
    }
    catch( NullPointerException ex )
    {
      Log.e(TAG, "setToggleButtonTextAndStat: Nullpointer while setToggleButtonTextAndStat() : " + ex.getLocalizedMessage());
    }
  }

  /**
   * Starte das Suchen nach BT-Geräten
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 17.02.2013
   */
  private void startDiscoverBt()
  {
    // If we're already discovering, stop it
    Log.i(TAG, "startDiscoverBt: start discovering...");
    discoveredDevices.clear();
    setItemsEnabledwhileDiscover(false);
    if( MainActivity.mBtAdapter == null )
    {
      return;
    }
    if( MainActivity.mBtAdapter.isDiscovering() )
    {
      stopDiscoverBt();
    }
    //
    // Register broadcasts während Geräte gesucht werden
    //
    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    runningActivity.registerReceiver(mReceiver, filter);
    //
    // Register broadcasts wenn die Suche beendet wurde
    //
    filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    runningActivity.registerReceiver(mReceiver, filter);
    //
    // Discovering Marker setzen
    this.runDiscovering = true;
    // Adapter frisch befüllen
    fillNewAdapterWithKnownDevices();
    // Discovering starten
    MainActivity.mBtAdapter.startDiscovery();
  }

  /**
   * Das Discovering beenden
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 06.05.2013
   */
  @SuppressLint("InlinedApi")
  private void stopDiscoverBt()
  {
    MainActivity.mBtAdapter.cancelDiscovery();
    // Unregister broadcast listeners
    runningActivity.unregisterReceiver(mReceiver);
    //
    // Intend-Filter und Intend für Pairing Anfragen wieder aktivieren
    //
    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
    if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT )
    {
      // Nur bei Android Versionen größer oder gleich 4.4
      filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
    }
    runningActivity.registerReceiver(mReceiver, filter);
  }

  /**
   * Schreibe das letzte verbundene Gerät in die Preferences
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 26.07.2013
   */
  private void writePreferences()
  {
    if( lastConnectedDeviceMac == null || lastConnectedDeviceMac.isEmpty() )
    {
      return;
    }
    SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(runningActivity);
    SharedPreferences.Editor editor = sPref.edit();
    editor.putString(LAST_CONNECTED_DEVICE_KEY, lastConnectedDeviceMac);
    //
    // alles in die Propertys
    //
    if( editor.commit() )
    {
      if( ApplicationDEBUG.DEBUG )
      {
        Log.d(TAG, "writePreferences: wrote preference to storeage.");
      }
    }
    else
    {
      Log.e(TAG, "writePreferences: CAN'T wrote preference to storage.");
    }
  }
}
