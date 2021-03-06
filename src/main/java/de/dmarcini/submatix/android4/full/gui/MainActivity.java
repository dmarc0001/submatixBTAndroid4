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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import de.dmarcini.submatix.android4.full.BuildConfig;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.comm.BlueThoothComService;
import de.dmarcini.submatix.android4.full.comm.BlueThoothComService.LocalBinder;
import de.dmarcini.submatix.android4.full.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.full.content.ContentSwitcher;
import de.dmarcini.submatix.android4.full.content.ContentSwitcher.ProgItem;
import de.dmarcini.submatix.android4.full.dialogs.AreYouSureDialogFragment;
import de.dmarcini.submatix.android4.full.dialogs.EditAliasDialogFragment;
import de.dmarcini.submatix.android4.full.dialogs.UserAlertDialogFragment;
import de.dmarcini.submatix.android4.full.exceptions.FirmwareNotSupportetException;
import de.dmarcini.submatix.android4.full.exceptions.NoDatabaseException;
import de.dmarcini.submatix.android4.full.interfaces.IBtServiceListener;
import de.dmarcini.submatix.android4.full.interfaces.INavigationDrawerCallbacks;
import de.dmarcini.submatix.android4.full.interfaces.INoticeDialogListener;
import de.dmarcini.submatix.android4.full.utils.DataSQLHelper;
import de.dmarcini.submatix.android4.full.utils.GasUpdateEntity;
import de.dmarcini.submatix.android4.full.utils.ProjectConst;
import de.dmarcini.submatix.android4.full.utils.SPX42AliasManager;
import de.dmarcini.submatix.android4.full.utils.SPX42Config;

/**
 * Die Aktivität der Application
 * <p/>
 * Project: NaviTest Package: com.example.navitest
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p/>
 *         Stand: 06.11.2014
 */
public class MainActivity extends Activity implements INavigationDrawerCallbacks, INoticeDialogListener, IBtServiceListener, OnBackStackChangedListener
{
  //
  // @formatter:on
  //

  private final static String                        TAG                   = MainActivity.class.getSimpleName();
  private final static String                        TAG2                  = "BackStackChangeListener";
  private static final String                        SERVICENAME           = BlueThoothComService.class.getCanonicalName();
  private static final String                        PACKAGENAME           = BlueThoothComService.class.getPackage().getName();
  private static final String                        FIRSTTIME_KEY         = "keyFirstTimeInitiated";
  private static final String                        PREFVERSION_KEY       = "keyPreferencesVersion";
  private static final String                        PROG_DATA_DIR_KEY     = "keyProgDataDirectory";
  private static final String                        PROG_TIME_FORMAT_KEY  = "keyProgUnitsTimeFormat";
  @SuppressWarnings( "javadoc" )
  public static        DateTimeFormatter             localTimeFormatter    = DateTimeFormat.forPattern("yyyy-MM-dd - HH:mm:ss");
  /**
   * Global verfügbarer Alias Manager, Zuordnung Gerät <-> Alias
   */
  public static        SPX42AliasManager             aliasManager          = null;
  protected static     File                          databaseDir           = null;
  protected static     SPX42Config                   spxConfig             = new SPX42Config();                                   // Da werden SPX-Spezifische Sachen gespeichert
  protected static     BluetoothAdapter              mBtAdapter            = null;
  protected static     float                         ackuValue             = 0.0F;
  protected static     boolean                       wasRestartForNewTheme = false;                                               // War es ein restart mit neuem Thema?
  private static       Vector<String[]>              dirEntryCache         = new Vector<String[]>();
  private static       boolean                       dirCacheIsFilling     = true;
  private static       String[]                      deviceUnis            = null;
  private static       int                           currentStyleId        = R.style.AppDarkTheme;
  private static       int                           firstId               = -1;                                                  // Die ID des ersten Eintrages
  private static       int                           lastStatusConnected   = ProjectConst.CONN_STATE_NONE;                        // War der letzte Status Connected?
  private final        ArrayList<IBtServiceListener> serviceListener       = new ArrayList<IBtServiceListener>();
  private static final String[]                      storagePerm           = {
                                                                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                                                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                                                Manifest.permission.BLUETOOTH,
                                                                                Manifest.permission.BLUETOOTH_ADMIN,
                                                                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                                                                Manifest.permission.INTERNET
                                                                              };
  //
  // Ein Messagehandler, der vom Service kommende Messages bearbeitet
  //
  @SuppressLint( "HandlerLeak" )
  private final        Handler                       mHandler              = new Handler()
  {
    @Override
    public void handleMessage(Message msg)
    {
      if( !(msg.obj instanceof BtServiceMessage) )
      {
        Log.e(TAG, "Handler::handleMessage: Recived Message is NOT type of BtServiceMessage!");
        return;
      }
      BtServiceMessage smsg = ( BtServiceMessage ) msg.obj;

      // an alle Listener versenden!
      for( IBtServiceListener aServiceListener : serviceListener )
      {
        aServiceListener.handleMessages(msg.what, smsg);
      }
    }
  };
  private              BlueThoothComService          mService              = null;
  private              LocalBinder                   binder                = null;
  //
  //@formatter:off
  //
  // wird beim binden / unbinden benutzt
  //
  private final ServiceConnection mConnection = new ServiceConnection()
  {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
      if( BuildConfig.DEBUG )
      { Log.d(TAG, "onServiceConnected()..."); }
      binder = ( LocalBinder ) service;
      mService = binder.getService();
      binder.registerServiceHandler(mHandler);
    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {
      if( BuildConfig.DEBUG )
      { Log.d(TAG, "onServiceDisconnected..."); }
      if( mService != null && binder != null )
      {
        if( BuildConfig.DEBUG )
        { Log.d(TAG, "onServiceDisconnected...unregister Handler..."); }
        binder.unregisterServiceHandler(mHandler);
      }
      mService = null;
      binder = null;
    }
  };
  private volatile boolean mIsBound = false;
  private NavigatorFragment appNavigatorFragment;
  private CharSequence mTitle;                                                                      // die Titelzeile

  /**
   * Gib den Style der App zurück
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * Stand: 03.12.2013
   *
   * @return Style der App
   */
  public static final int getAppStyle()
  {
    return (currentStyleId);
  }
  //
  // @formatter:on
  //

  /**
   * Wenn ein Fragment die Nachrichten erhalten soll, muß es den listener übergben...
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 24.02.2013
   *
   * @param listener der Listener des rufenden Fragmentes
   */
  public void addServiceListener(IBtServiceListener listener)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "setServiceListener...");
    }
    if( !serviceListener.contains(listener) )
    {
      serviceListener.add(listener);
    }
    //
    // Es wird natürlich keine
    // neue Activity gestartet, d.h. es wird dann auch kein onConnect erzeugt.
    // Somit muss ich da etwas nachhelfen.
    //
    if( mService != null )
    {
      BtServiceMessage msg;
      // Iterator<IBtServiceListener> it = serviceListener.iterator();
      //
      int state = mService.getConnectionState();
      // welche Message muss ich machen?
      switch( state )
      {
        default:
        case ProjectConst.CONN_STATE_NONE:
          msg = new BtServiceMessage(ProjectConst.MESSAGE_DISCONNECTED);
          listener.msgDisconnected(msg);
          break;
        case ProjectConst.CONN_STATE_CONNECTING:
          msg = new BtServiceMessage(ProjectConst.MESSAGE_CONNECTING);
          listener.msgConnecting(msg);
          break;
        case ProjectConst.CONN_STATE_CONNECTED:
          msg = new BtServiceMessage(ProjectConst.MESSAGE_CONNECTED);
          listener.msgConnected(msg);
          break;
      }
    }
  }

  /**
   * Frag den SPX nach den Masseinheiten
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 15.08.2013
   */
  public void aksForUnitsFromSPX42()
  {
    if( mService != null )
    {
      if( deviceUnis != null )
      {
        if( BuildConfig.DEBUG )
        {
          Log.i(TAG, "read units from cache...");
        }
        BtServiceMessage msg = new BtServiceMessage(ProjectConst.MESSAGE_UNITS_READ, deviceUnis);
        mHandler.obtainMessage(ProjectConst.MESSAGE_UNITS_READ, msg).sendToTarget();
      }
      else
      {
        mService.aksForUnitsFromSPX42();
      }
    }
  }

  /**
   * Frage, ob BR erlaubt werden sollte Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   */
  private void askEnableBT()
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "askEnableBT...");
    }
    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    startActivityForResult(enableIntent, ProjectConst.REQUEST_ENABLE_BT);
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "askEnableBT...OK");
    }
  }

  /**
   * Frage den SPX nach der Konfiguration
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 02.06.2013
   */
  public void askForConfigFromSPX42()
  {
    if( mService != null )
    {
      mService.askForConfigFromSPX42();
    }
  }

  /**
   * Frage nach der DECO-Konfiguration
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 13.07.2013
   */
  public void askForDecoConfig()
  {
    if( mService != null )
    {
      mService.askForDecoConfig();
    }
  }

  /**
   * Grad beim SPX nach der Firmwareversion
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 02.06.2013
   */
  public void askForFirmwareVersion()
  {
    if( mService != null )
    {
      mService.askForFirmwareVersion();
    }
  }

  /**
   * frage den SPX nach den Gaseinstellungen
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 18.07.2013
   */
  public void askForGasFromSPX()
  {
    if( mService != null )
    {
      mService.askForGasFromSPX();
    }
  }

  /**
   * Frag nach der Lizenz des SPX
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 04.06.2013
   */
  public void askForLicenseFromSPX()
  {
    if( mService != null )
    {
      mService.askForLicenseFromSPX();
    }
  }

  /**
   * Frage den SPX nach den schmutzigen Details eines Logs
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 07.08.2013
   *
   * @param numberOnSPX Die Nummer des Eintrages auf dem SPX
   */
  public void askForLogDetail(int numberOnSPX)
  {
    if( mService != null )
    {
      mService.askForLogDetail(numberOnSPX);
    }
  }

  /**
   * frage den SPX nach dem Logverzeichnis
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 06.08.2013
   */
  public void askForLogDirectoryFromSPX()
  {
    BtServiceMessage msg;
    //
    if( mService != null )
    {
      // der SPX ist verbunden, wenn Daten im Cache sind
      // und das Flag sagt, der Cach muss gefüllt werden
      if( dirEntryCache.isEmpty() && dirCacheIsFilling )
      {
        mService.askForLogDirectoryFromSPX();
      }
      else
      {
        if( BuildConfig.DEBUG )
        {
          Log.i(TAG, "read directory from cache...");
        }
        // hole Daten aus dem Cache
        // und sende diese dann in die Queue
        Iterator<String[]> it = dirEntryCache.iterator();
        while( it.hasNext() )
        {
          msg = new BtServiceMessage(ProjectConst.MESSAGE_DIRENTRY_READ, it.next());
          mHandler.obtainMessage(ProjectConst.MESSAGE_DIRENTRY_READ, msg).sendToTarget();
        }
        msg = new BtServiceMessage(ProjectConst.MESSAGE_DIRENTRY_END);
        mHandler.obtainMessage(ProjectConst.MESSAGE_DIRENTRY_END, msg).sendToTarget();
      }
    }
  }

  /**
   * Frage den SPX nach seinem Hersteller
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 04.06.2013
   */
  public void askForManufacturer()
  {
    if( mService != null )
    {
      mService.askForManufacturer();
    }
  }

  /**
   * Frage nach der Seriennummer
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 28.05.2013
   */
  public void askForSerialNumber()
  {
    if( mService != null )
    {
      mService.askForSerialNumber();
    }
  }

  /**
   * Frage den SPX ob er am Leben ist und wie seine Ackuspannung ist
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 02.06.2013
   */
  public void askForSPXAlive()
  {
    if( mService != null )
    {
      mService.askForSPXAlive();
    }
  }

  /**
   * Service binden, ggf starten
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 23.02.2013
   */
  private void doBindService()
  {
    //
    // Service starten, wenn er nicht schon läuft
    //
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "doBindService()...");
    }
    final ActivityManager                          activityManager = ( ActivityManager ) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
    final List<ActivityManager.RunningServiceInfo> services        = activityManager.getRunningServices(Integer.MAX_VALUE);
    boolean                                        isServiceFound  = false;
    //
    for( int i = 0; i < services.size(); i++ )
    {
      if( (services.get(i).service.getPackageName()).matches(PACKAGENAME) )
      {
        if( SERVICENAME.equals(services.get(i).service.getClassName()) )
        {
          if( BuildConfig.DEBUG )
          {
            Log.d(TAG, "Service is running, need not start...");
          }
          isServiceFound = true;
        }
      }
    }
    if( !isServiceFound )
    {
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "Starting Service (package:<" + PACKAGENAME + ">)...");
      }
      Intent service = new Intent(this, BlueThoothComService.class);
      startService(service);
    }
    //
    // binde Service
    //
    Log.i(TAG, "bind  BT service...");
    Intent intent = new Intent(this, BlueThoothComService.class);
    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    mIsBound = true;
  }

  /**
   * Verbinde Blutethooth Gerät
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 04.03.2013
   *
   * @param device
   */
  public void doConnectBtDevice(String device)
  {
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, String.format(Locale.ENGLISH, "recived do connect device <%s>", device));
    }
    if( mIsBound )
    {
      mService.connect(device);
    }
    // Cache für Directory leeren!
    dirEntryCache.clear();
    dirCacheIsFilling = true;
    deviceUnis = null;
  }

  /**
   * Trenne Bluethooth Gerät
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 04.03.2013
   */
  public void doDisconnectBtDevice()
  {
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "recived do disconnect device ");
    }
    if( mIsBound )
    {
      mService.disconnect();
    }
    // Cache für Directory leeren!
    dirEntryCache.clear();
    dirCacheIsFilling = true;
    deviceUnis = null;
  }

  /**
   * Service unbinden
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 23.02.2013
   */
  private void doUnbindService(boolean isNowStopping)
  {
    if( mIsBound )
    {
      //
      // wenn der Service gebunen ist, muss er wieder "entbunden" werden
      //
      if( mService != null )
      {
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "doUnbindService...");
        }
        if( mService != null )
        {
          if( mService != null && binder != null )
          {
            if( BuildConfig.DEBUG )
            {
              Log.d(TAG, "doUnbindService...unregister Handler...");
            }
            binder.unregisterServiceHandler(mHandler, isNowStopping);
          }
          unbindService(mConnection);
          mService = null;
          binder = null;
        }
        mIsBound = false;
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "doUnbindService...OK");
        }
      }
    }
  }

  @Override
  public void finishFromChild(Activity child)
  {
    // wenn eine child-App beendet hat
    finish();
  }

  /**
   * Mit welchem Gerät (Addr) bin ich verbunden?
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 28.05.2013
   *
   * @return MAC-Addr des Gerätes
   */
  public String getConnectedDevice()
  {
    if( mService != null )
    {
      return (mService.getConnectedDevice());
    }
    return (null);
  }

  /**
   * erfrage die MAC des verbundenen Gerätes
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * Stand: 01.12.2013
   *
   * @return MAC oder "0"
   */
  public String getConnectedMac()
  {
    if( mService != null )
    {
      if( mService.getConnectedDevice() != null )
      {
        return (mService.getConnectedDevice());
      }
    }
    return ("0");
  }

  /**
   * Verbindungsstatus erfragen
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 13.03.2013
   *
   * @return der Verbindungsstatus
   */
  public int getConnectionStatus()
  {
    if( mService != null )
    {
      return (mService.getConnectionState());
    }
    return (ProjectConst.CONN_STATE_NONE);
  }

  /**
   * das Database Directory finden
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * Stand: 09.11.2014
   *
   * @return Das Datenbankverzeichnis
   */
  private File getDatabaseDir()
  {
    File databaseDir;
    //
    // bei neueren Androiden ist das sehr restriktiv, versuche den Standart
    //
    databaseDir = getBaseContext().getFilesDir().getAbsoluteFile();
    //
    // der Standartpfad
    //
    if( databaseDir.exists() && databaseDir.isDirectory() && databaseDir.canWrite() )
    {
      databaseDir = getBaseContext().getDir( ProjectConst.DATABASE_SUBDIR, 0 );
      if( databaseDir.exists() && databaseDir.isDirectory() && databaseDir.canWrite() )
      {
        return( databaseDir );
      }
      //
      // doch den Standart nutzen
      //
      databaseDir = getBaseContext().getFilesDir().getAbsoluteFile();
      Log.i(TAG, String.format("use system files dir <%s>...", databaseDir.getAbsolutePath()));
      return (databaseDir);
    }
    //
    // nichts hat geklappt: NULL zurück
    //
    Log.e(TAG, "datastore Directory not found!");
    return (null);
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
      // Computer wird gerade verbunden
      // ################################################################
      case ProjectConst.MESSAGE_CONNECTING:
        msgConnecting(smsg);
        break;
      // ################################################################
      // Computer wurde verbunden
      // ################################################################
      case ProjectConst.MESSAGE_CONNECTED:
        // die Menüs anpassen
        if( appNavigatorFragment != null )
        {
          if( BuildConfig.DEBUG )
          {
            Log.v(TAG, "ICONS auf CONNECTED stellen...");
          }
          appNavigatorFragment.setListAdapterForOnlinestatus(true);
        }
        else
        {
          if( BuildConfig.DEBUG )
          {
            Log.v(TAG, "no fragment found: ICONS auf CONNECTED... ");
          }
        }
        msgConnected(smsg);
        break;
      // ################################################################
      // Computer wurde getrennt
      // ################################################################
      case ProjectConst.MESSAGE_DISCONNECTED:
        // die Menüs anpassen
        if( appNavigatorFragment != null )
        {
          if( BuildConfig.DEBUG )
          {
            Log.v(TAG, "ICONS auf DISCONNECTED stellen");
          }
          appNavigatorFragment.setListAdapterForOnlinestatus(false);
        }
        else
        {
          if( BuildConfig.DEBUG )
          {
            Log.v(TAG, "no fragment found: ICONS auf DISCONNECTED... ");
          }
        }
        msgDisconnected(smsg);
        break;
      // ################################################################
      // Computer wurde getrennt
      // ################################################################
      case ProjectConst.MESSAGE_CONNECTERROR:
        // die Menüs anpassen
        if( appNavigatorFragment != null )
        {
          if( BuildConfig.DEBUG )
          {
            Log.v(TAG, "ICONS auf DISCONNECTED stellen");
          }
          appNavigatorFragment.setListAdapterForOnlinestatus(false);
        }
        else
        {
          if( BuildConfig.DEBUG )
          {
            Log.v(TAG, "no fragment found: ICONS auf DISCONNECTED... ");
          }
        }
        msgConnectError(smsg);
        break;
      // ################################################################
      // Seriennummer des Computers wurde gelesen
      // ################################################################
      case ProjectConst.MESSAGE_SERIAL_READ:
        msgRecivedSerial(smsg);
        break;
      // ################################################################
      // Firmwareversion des ccomputers wurde gelesen
      // ################################################################
      case ProjectConst.MESSAGE_FWVERSION_READ:
        msgRecivedFwVersion(smsg);
        break;
      // ################################################################
      // SPX sendet "ALIVE" und Ackuspannung
      // ################################################################
      case ProjectConst.MESSAGE_SPXALIVE:
        msgRecivedAlive(smsg);
        break;
      // ################################################################
      // SPX sendet Herstellerkennung
      // ################################################################
      case ProjectConst.MESSAGE_MANUFACTURER_READ:
        msgReciveManufacturer(smsg);
        break;
      // ################################################################
      // SPX Lizenz lesen
      // ################################################################
      case ProjectConst.MESSAGE_LICENSE_STATE_READ:
        msgReciveLicenseState(smsg);
        break;
      // ################################################################
      // Units vom SPX emnpfangen
      // ################################################################
      case ProjectConst.MESSAGE_UNITS_READ:
        if( deviceUnis == null )
        {
          if( smsg.getContainer() instanceof String[] )
          {
            // auch cachen...
            deviceUnis = ( String[] ) smsg.getContainer();
          }
        }
        break;
      // ################################################################
      // Verzeichniseintrag gefunden
      // ################################################################
      case ProjectConst.MESSAGE_DIRENTRY_READ:
        if( dirCacheIsFilling )
        {
          // nur, wenn die Daten vom SPX kommen
          msgComputeDirentry(smsg);
        }
        break;
      // ################################################################
      // Verzeichnis zuende
      // ################################################################
      case ProjectConst.MESSAGE_DIRENTRY_END:
        dirCacheIsFilling = false;
        break;
      // ################################################################
      // Sonst....
      // ################################################################
      default:
        if( BuildConfig.DEBUG )
        {
          Log.d(TAG, "unknown message with id <" + smsg.getId() + "> recived!");
        }
    }
  }

  /**
   * Erzeuge die Menüeinträge Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   */
  private void initStaticContenSwitcher()
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "initStaticContent...");
    }
    // zuerst aufräumen
    ContentSwitcher.clearItems();
    //
    // irgendeine Kennung muss der String bekommen, also gibts halt die String-ID
    //@formatter:off
    ContentSwitcher.addItem(
        new ProgItem(R.string.progitem_connect, R.drawable.bluetooth_icon_bw, R.drawable.bluetooth_icon_color, getString(R.string.progitem_connect), true, false));
    ContentSwitcher.addItem(
        new ProgItem(R.string.progitem_spx_status, R.drawable.spx_health_icon, R.drawable.spx_health_icon, getString(R.string.progitem_spx_status), false, false));
    ContentSwitcher.addItem(
        new ProgItem(R.string.progitem_config, R.drawable.spx_toolbox_offline, R.drawable.spx_toolbox_online, getString(R.string.progitem_config), false, false));
    ContentSwitcher.addItem(
        new ProgItem(R.string.progitem_gaslist, R.drawable.gasedit_offline, R.drawable.gasedit_online, getString(R.string.progitem_gaslist), false, false));
    ContentSwitcher.addItem(
        new ProgItem(R.string.progitem_logging, R.drawable.logging_offline, R.drawable.logging_online, getString(R.string.progitem_logging), false, false));
    ContentSwitcher.addItem(
        new ProgItem(R.string.progitem_loggraph, R.drawable.graphsbar_online, R.drawable.graphsbar_online, getString(R.string.progitem_loggraph), true, false));
    ContentSwitcher.addItem(
        new ProgItem(R.string.progitem_loggraph_detail, R.drawable.graphsbar_online, R.drawable.graphsbar_online, getString(R.string.progitem_loggraph_detail), true, true));
    ContentSwitcher.addItem(
        new ProgItem(R.string.progitem_export, R.drawable.export_offline, R.drawable.export_online, getString(R.string.progitem_export), true, false));
    ContentSwitcher.addItem(
        new ProgItem(R.string.progitem_progpref, R.drawable.app_toolbox_offline, R.drawable.app_toolbox_online, getString(R.string.progitem_progpref), true, false));
    ContentSwitcher.addItem(
        new ProgItem(R.string.progitem_about, R.drawable.yin_yang, R.drawable.yin_yang, getString(R.string.progitem_about), true, false));
    ContentSwitcher.addItem(
        new ProgItem(R.string.progitem_help, R.drawable.ask_blue, R.drawable.ask_blue, getString(R.string.progitem_help), true, false));
    ContentSwitcher.addItem(
        new ProgItem(R.string.progitem_exit, R.drawable.shutoff, R.drawable.shutoff, getString(R.string.progitem_exit), true, false));
    //@formatter:on
  }

  /**
   * Wenn die Daten vom SPX kommen auch erst einmal cachen, die ändern sich nicht während der verbunden ist
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * <p/>
   * Stand: 21.01.2014
   *
   * @param smsg die Nachricht vom BT Moul
   */
  private void msgComputeDirentry(BtServiceMessage smsg)
  {
    //
    // ist das Objekt ein StringArray?
    //
    if( !(smsg.getContainer() instanceof String[]) )
    {
      return;
    }
    //
    // Ein Stringarray kommt in den DIR-Cache
    //
    dirEntryCache.add(( String[] ) smsg.getContainer());
  }

  /**
   * Das Gerät wurde verbunden
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 23.02.2013
   *
   * @param msg
   */
  @Override
  public void msgConnected(BtServiceMessage msg)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "connected...");
    }
    //
    // if( !spxConfig.isInitialized() && ( mService != null ) && ( mService.getConnectionState() == ProjectConst.MESSAGE_CONNECTED ) )
    if( !spxConfig.isInitialized() )
    {
      //
      // wenn das Gerät noch nicht ausgelesen wurde alles abfragen
      //
      spxConfig.setConnectedDeviceMac(getConnectedDevice());
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "msgConnected(): ask for manufacturer number...");
      }
      askForManufacturer();
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "msgConnected(): ask for serial number...");
      }
      askForSerialNumber();
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "msgConnected(): ask for SPX license...");
      }
      askForLicenseFromSPX();
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "msgConnected(): ask for Firmware version...");
      }
      askForFirmwareVersion();
    }
    lastStatusConnected = ProjectConst.CONN_STATE_CONNECTED;
  }

  @Override
  public void msgConnectError(BtServiceMessage msg)
  {
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "connection error (device not online?)");
    }
    // Cache für Directory leeren!
    dirEntryCache.clear();
    dirCacheIsFilling = true;
    deviceUnis = null;
  }

  /**
   * Wenn das Gerät verbunden wird (beim Verbinden)
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 23.02.2013
   *
   * @param msg
   */
  @Override
  public void msgConnecting(BtServiceMessage msg)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "connecting...");
    }
    lastStatusConnected = ProjectConst.CONN_STATE_CONNECTING;
  }

  /**
   * Das Gerät wurde getrennt
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 23.02.2013
   *
   * @param msg
   */
  @Override
  public void msgDisconnected(BtServiceMessage msg)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "disconnected...");
    }
    spxConfig.clear();
    // Cache für Directory leeren!
    dirEntryCache.clear();
    dirCacheIsFilling = true;
    deviceUnis = null;
    ackuValue = 0.0F;
    if( lastStatusConnected != ProjectConst.CONN_STATE_NONE )
    {
      //
      // Alle Einträge auf dem Stack löschen, aber nur einmal ;-)
      //
      lastStatusConnected = ProjectConst.CONN_STATE_NONE;
      if( BuildConfig.DEBUG )
      {
        Log.e(TAG, "disconnected, cleaning Fragment stack!");
      }
      getFragmentManager().popBackStack(firstId, 0);
    }
  }

  @Override
  public void msgRecivedAlive(BtServiceMessage msg)
  {
    String amsg;
    int    val = 0;
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "message alive with acku voltage recived");
    }
    //
    // Ist die Nachricht ein String?
    //
    if( msg.getContainer() instanceof String )
    {
      //
      // Ja, ein String, der beinhaltete die aktuelle Ackuspannung
      //
      amsg = ( String ) msg.getContainer();
      try
      {
        val = Integer.parseInt(amsg, 16);
      }
      catch( NumberFormatException ex )
      {
        Log.e(TAG, "Acku-Value not an Number: <" + ex.getLocalizedMessage() + ">");
        val = 0;
      }
      ackuValue = ( float ) (val / 100.0);
      // Hauptfenster
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, String.format(Locale.ENGLISH, "Acku value: %02.02f", ackuValue));
      }
    }
    else
    {
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "Acku value was not an STRING");
      }
    }
  }

  /**
   * Nachricht über die Firmwareversion wurde empfangen
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   *
   * @param msg Messageobjekt
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * <p/>
   * Stand: 10.11.2013
   */
  private void msgRecivedFwVersion(BtServiceMessage msg)
  {
    spxConfig.setFirmwareVersion(( String ) msg.getContainer());
    if( BuildConfig.DEBUG )
    {
      Log.i(TAG, "license: custom: " + spxConfig.getCustomEnabled());
      Log.i(TAG, "fahrenheid bug: " + spxConfig.hasFahrenheidBug());
      Log.i(TAG, "can set DateTime: " + spxConfig.canSetDateTime());
      Log.i(TAG, "has six custom values: " + spxConfig.hasSixValuesIndividual());
      Log.i(TAG, "is firmware supported: " + spxConfig.isFirmwareSupported());
      Log.i(TAG, "is old param order: " + spxConfig.isOldParamSorting());
      Log.i(TAG, "is newer display brightness: " + spxConfig.isNewerDisplayBrigthness());
      Log.i(TAG, "has six meters first autosetpoint: " + spxConfig.isSixMetersAutoSetpoint());
      Log.i(TAG, "has deactivated autosetpoint to switch off: " + spxConfig.isAutoSetpointWithoutOff());
    }
    //
    // Jetzt währe es an der Zeit, auch an Datum und Zeit zu denken
    //
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "msgConnected(): set Date and Time to Device (if possible)...");
    }
    try
    {
      writeDateTimeToDevice();
    }
    catch( FirmwareNotSupportetException ex )
    {
    }
  }

  /**
   * Nachricht mit der Seriennummer des SPX empfangen
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 17.07.2013
   *
   * @param msg
   */
  public void msgRecivedSerial(BtServiceMessage msg)
  {
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "serial <" + msg.getContainer() + "> recived");
    }
    spxConfig.setSerial(new String(( String ) msg.getContainer()));
  }

  @Override
  public void msgRecivedTick(BtServiceMessage msg)
  {
    // if( BuildConfig.DEBUG ) Log.d( TAG, String.format( Locale.ENGLISH, "recived Tick <%x08x>", msg.getTimeStamp() ) );
  }

  /**
   * Nachricht mit dem Lizenzstatus des SPX empfangen
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 17.07.2013
   *
   * @param msg
   */
  public void msgReciveLicenseState(BtServiceMessage msg)
  {
    // LS : License State 0=Nitrox,1=Normoxic Trimix,2=Full Trimix
    // CE : Custom Enabled 0= disabled, 1=enabled
    String[] lic = ( String[] ) msg.getContainer();
    spxConfig.setLicenseStatus(lic);
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "SPX License state <" + lic[ 0 ] + "," + lic[ 1 ] + "> recived");
    }
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "SPX License INDIVIDUAL: <" + spxConfig.getCustomEnabled() + ">");
      switch( spxConfig.getLicenseState() )
      {
        case ProjectConst.SPX_LICENSE_NITROX:
          Log.d(TAG, "SPX MIX License : NITROX");
          break;
        case ProjectConst.SPX_LICENSE_NORMOXICTX:
          Log.d(TAG, "SPX MIX License : NORMOXIX TRIMIX");
          break;
        case ProjectConst.SPX_LICENSE_FULLTX:
          Log.d(TAG, "SPX MIX License : FULL TRIMIX");
          break;
        default:
          Log.d(TAG, "SPX MIX License : UNKNOWN");
      }
    }
  }

  /**
   * Nachricht über den Hersteller des Gerätes empfangen
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 17.07.2013
   *
   * @param msg
   */
  public void msgReciveManufacturer(BtServiceMessage msg)
  {
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "SPX Manufacturer <" + msg.getContainer() + "> recived. Ignore.");
    }
  }

  @Override
  public void msgReciveWriteTmeout(BtServiceMessage msg)
  {
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onActivityResult()... ");
    }
    switch( requestCode )
    {
      //
      // Bluethooth erlauben
      //
      case ProjectConst.REQUEST_ENABLE_BT:
        // Wenn BT eingeschaltet wurde
        if( resultCode == Activity.RESULT_OK )
        {
          if( BuildConfig.DEBUG )
          {
            Log.v(TAG, "REQUEST_ENABLE_BT => BT Device ENABLED");
          }
          Toast.makeText(this, R.string.toast_bt_enabled, Toast.LENGTH_SHORT).show();
          // Service starten und binden (bei onResume())
        }
        else
        {
          // Der User hats verboten oder ein Fehler trat auf
          if( BuildConfig.DEBUG )
          {
            Log.v(TAG, "REQUEST_ENABLE_BT => BT Device NOT ENABLED");
          }
          Toast.makeText(this, R.string.toast_exit_nobt, Toast.LENGTH_LONG).show();
          finish();
        }
        break;
      default:
        Log.w(TAG, "unknown Request code for activity result");
    }
  }

  /**
   * Die Activity startet
   *
   * @param savedInstanceState
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onCreate...");
    }
    if( getIntent().getBooleanExtra("EXIT", false) )
    {
      finish();
    }
    initStaticContenSwitcher();
    serviceListener.clear();
    serviceListener.add(this);
    if( BuildConfig.DEBUG )
    {
      //
      // zum DEBUGGEN den Stack überwachen
      //
      getFragmentManager().addOnBackStackChangedListener(this);
    }
    // den defaultadapter lesen
    mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onCreate: setContentView...");
    }
    //
    // Das gewünschte Thema setzen
    //
    SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
    setAppPreferences(sPref);
    if( sPref.contains("keyProgOthersThemeIsDark") )
    {
      boolean whishedTheme = sPref.getBoolean("keyProgOthersThemeIsDark", false);
      if( whishedTheme )
      {
        if( BuildConfig.DEBUG )
        {
          Log.d(TAG, "onCreate: select DARK theme while preference was set");
        }
        currentStyleId = R.style.AppDarkTheme;
        setTheme(R.style.AppDarkTheme);
      }
      else
      {
        if( BuildConfig.DEBUG )
        {
          Log.d(TAG, "onCreate: select Blue theme while preference was set");
        }
        currentStyleId = R.style.AppLightTheme;
        setTheme(R.style.AppLightTheme);
      }
    }
    //
    // den Inhalsbehälter setzten
    //
    setContentView(R.layout.activity_main);
    //
    // finde raus, ob es ein Restart für ein neues Theme war
    //
    if( wasRestartForNewTheme )
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "onCreate: it was an restart for new Theme..");
      }
      //
      // ja, jetzt muss ich auch drauf reagieren und das Preferenz-Fragment neu aufbauen
      //
      wasRestartForNewTheme = false;
      appNavigatorFragment = ( NavigatorFragment ) getFragmentManager().findFragmentById(R.id.navi_drawer);
      mTitle = getTitle();
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "onCreate: restart for new Theme: set navigation drawer...");
      }
      // Initialisiere den Navigator
      appNavigatorFragment.setUp(R.id.navi_drawer, ( DrawerLayout ) findViewById(R.id.drawer_layout), R.string.progitem_progpref);
      Log.i(TAG, "onCreate: set program preferences after switch theme...");
      //
      // das Programmeinstellungsfragment
      //
      Bundle   arguments = new Bundle();
      ProgItem pItem     = ContentSwitcher.getProgItemForId(R.string.progitem_about);
      arguments.putString(ProjectConst.ARG_ITEM_CONTENT, pItem.content);
      arguments.putInt(ProjectConst.ARG_ITEM_ID, pItem.nId);
      ProgramPreferencesFragment ppFragment = new ProgramPreferencesFragment();
      ppFragment.setArguments(arguments);
      getActionBar().setTitle(R.string.conf_prog_headline);
      getActionBar().setLogo(R.drawable.properties);
      FragmentTransaction fTrans = getFragmentManager().beginTransaction();
      fTrans.replace(R.id.main_container, ppFragment);
      fTrans.addToBackStack("ProgramPreferencesFragment");
      fTrans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE /*| FragmentTransaction.TRANSIT_FRAGMENT_OPEN*/);
      firstId = fTrans.commit();
    }
    else
    {
      //
      // Kein Neustart mit neuem Thema
      //
      appNavigatorFragment = ( NavigatorFragment ) getFragmentManager().findFragmentById(R.id.navi_drawer);
      mTitle = getTitle();
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "onCreate: set navigation drawer...");
      }
      // Initialisiere den Navigator
      appNavigatorFragment.setUp(R.id.navi_drawer, ( DrawerLayout ) findViewById(R.id.drawer_layout), R.string.progitem_about);
      //
      // Als erstes das Connect-Fragment...
      //
      Log.i(TAG, "onCreate: default Fragnment initialising...");
      Bundle   arguments = new Bundle();
      ProgItem pItem     = ContentSwitcher.getProgItemForId(R.string.progitem_about);
      arguments.putString(ProjectConst.ARG_ITEM_CONTENT, pItem.content);
      arguments.putInt(ProjectConst.ARG_ITEM_ID, pItem.nId);
      Log.i(TAG, "onCreate: create ProgramAbountFragment");
      ProgramAboutFragment defaultFragment = new ProgramAboutFragment();
      mTitle = getString(R.string.about_headline);
      defaultFragment.setArguments(arguments);
      FragmentTransaction fTrans = getFragmentManager().beginTransaction();
      fTrans.replace(R.id.main_container, defaultFragment);
      fTrans.addToBackStack("FIRSTFragment");
      fTrans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE /*| FragmentTransaction.TRANSIT_FRAGMENT_OPEN*/);
      firstId = fTrans.commit();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    if( !appNavigatorFragment.isDrawerOpen() )
    {
      // Only show items in the action bar relevant to this screen
      // if the drawer is not showing. Otherwise, let the drawer
      // decide what to show in the action bar.
      // getMenuInflater().inflate( R.menu.main, menu );
      restoreActionBar();
      return true;
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
    if( BuildConfig.DEBUG )
    {
      //
      // zum DEBUGGEN den Stack überwachen
      //
      getFragmentManager().removeOnBackStackChangedListener(this);
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "onDestroy:...");
      }
    }
  }

  /**
   * Wird ein Dialog negativ beendet (nein oder Abbruch)
   *
   * @param dialog
   */
  @Override
  public void onDialogNegativeClick(DialogFragment dialog)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "Negative dialog click!");
    }
    if( dialog.getTag().matches("set_database_directory") )
    {
      dialog.dismiss();
      recreate();
      return;
    }
    if( dialog.getTag().matches("firmware_not_supportet_error") )
    {
      dialog.dismiss();
      return;
    }
    mHandler.obtainMessage(ProjectConst.MESSAGE_DIALOG_NEGATIVE, new BtServiceMessage(ProjectConst.MESSAGE_DIALOG_NEGATIVE, dialog)).sendToTarget();
  }

  /**
   * Eine positive Erwiderung auf ein Formular
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * <p/>
   * Stand: 09.11.2014
   *
   * @param dialog
   */
  @Override
  public void onDialogPositiveClick(DialogFragment dialog)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "Positive dialog click!");
    }
    //
    // war es ein AreYouSureDialogFragment Dialog?
    //
    if( dialog instanceof AreYouSureDialogFragment )
    {
      AreYouSureDialogFragment aDial = ( AreYouSureDialogFragment ) dialog;
      //
      // War der Tag für den Dialog zum Exit des Programmes?
      //
      if( aDial.getTag().equals("programexit") )
      {
        Log.i(TAG, "User will close app...");
        Toast.makeText(this, R.string.toast_exit, Toast.LENGTH_SHORT).show();
        if( mService != null )
        {
          mService.destroyService();
        }
        if( BluetoothAdapter.getDefaultAdapter() != null )
        {
          // Preferences -> Programmeinstellungen soll das automatisch passieren?
          SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
          if( sPref.getBoolean("keyProgOthersDisableBTOnExit", true) )
          {
            if( BuildConfig.DEBUG )
            {
              Log.d(TAG, "disable BT Adapter on exit!");
            }
            BluetoothAdapter.getDefaultAdapter().disable();
          }
        }
        // Code nach stackoverflow
        // online geht das nicht....
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
        if( aliasManager != null )
        {
          aliasManager.close();
          aliasManager = null;
        }
        finish();
      }
    }
    //
    // soll der ALIAS eines Gerätes bearbeitet werden?
    //
    else if( dialog instanceof EditAliasDialogFragment )
    {
      Log.i(TAG, "User will edit alias...");
      EditAliasDialogFragment editDialog = ( EditAliasDialogFragment ) dialog;
      mHandler.obtainMessage(ProjectConst.MESSAGE_DEVALIAS_SET, new BtServiceMessage(ProjectConst.MESSAGE_DEVALIAS_SET, new String[]{editDialog.getDeviceName(), editDialog.getAliasName(), editDialog.getMac(), editDialog.getPin()})).sendToTarget();
    }
    //
    // ALERT Dialog?
    //
    else if( dialog instanceof UserAlertDialogFragment )
    {
      if( dialog.getTag().matches("notingSelectedWarning") )
      {
        dialog.dismiss();
        // Warung wegen keine Auswahl (leer) getrroffen, ignorieren
        return;
      }
      if( dialog.getTag().matches("noMailaddrWarning") )
      {
        dialog.dismiss();
        // Warung wegen fehlender Mailadresse, einfach diese Meldung ignorieren
        return;
      }
      if( dialog.getTag().matches("abortProgram") )
      {
        //
        // Die App soll beendet werden (ABORT)
        //
        Log.e(TAG, "will close app...");
        Toast.makeText(this, R.string.toast_exit, Toast.LENGTH_SHORT).show();
        if( BluetoothAdapter.getDefaultAdapter() != null )
        {
          // Preferences -> Programmeinstellungen soll das automatisch passieren?
          SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
          if( sPref.getBoolean("keyProgOthersDisableBTOnExit", true) )
          {
            if( BuildConfig.DEBUG )
            {
              Log.d(TAG, "disable BT Adapter on exit!");
            }
            BluetoothAdapter.getDefaultAdapter().disable();
          }
        }
        // nach stackoverflow
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
        if( aliasManager != null )
        {
          aliasManager.close();
          aliasManager = null;
        }
        finish();
      }
    }
    // hat sonst irgendwer Verwendung dafür?
    mHandler.obtainMessage(ProjectConst.MESSAGE_DIALOG_POSITIVE, new BtServiceMessage(ProjectConst.MESSAGE_DIALOG_POSITIVE, dialog)).sendToTarget();
  }

  /**
   * Reagiere auf die BACK-Taste, entweder Navigator öffnen opder Programm beenden
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event)
  {
    //
    // Ist es ZURÜCK?
    //
    if( (keyCode == KeyEvent.KEYCODE_BACK) )
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "onKeyDown: BACK pressed!");
      }
      if( getFragmentManager().getBackStackEntryCount() <= 1 )
      {
        // in diesem Fall frag mal den user ob er wirklich beenden will
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "onKeyDown:BACK pressed => backstack is empty, ask user for exit.");
        }
        AreYouSureDialogFragment sureDial = new AreYouSureDialogFragment(getString(R.string.dialog_sure_exit));
        sureDial.show(getFragmentManager().beginTransaction(), "programexit");
      }
      else
      {
        getFragmentManager().popBackStack();
      }
    }
    return false;
  }

  /**
   * Callback vom Navigator welcher Eintrag ausgewählt wurde
   *
   * @param pItem Der Programmeintrag Eintrages (hier gleichzeitig mit Resourcen-Id für den String)
   */
  @Override
  public void onNavigationDrawerItemSelected(ContentSwitcher.ProgItem pItem)
  {
    Bundle              arguments = new Bundle();
    boolean             isOnline  = false;
    FragmentTransaction fTrans    = null;
    Fragment            newFrag   = null;
    //
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, String.format(Locale.ENGLISH, "onNavigationDrawerItemSelected: id: <%d>, content: <%s>...", pItem.nId, pItem.content));
    }
    //
    // sind wir online?
    //
    if( getConnectionStatus() == ProjectConst.CONN_STATE_CONNECTED )
    {
      isOnline = true;
    }
    //
    // das richtige Icon setzen
    //
    if( isOnline )
    {
      getActionBar().setLogo(ContentSwitcher.getProgItemForId(pItem.nId).resIdOnline);
    }
    else
    {
      // wenn der SPX OFFLINE ist, nur OFFLINE Funktionen freigeben
      getActionBar().setLogo(ContentSwitcher.getProgItemForId(pItem.nId).resIdOffline);
    }
    //
    // Argumente für die Fragmente füllen
    //
    arguments.putString(ProjectConst.ARG_ITEM_CONTENT, pItem.content);
    arguments.putInt(ProjectConst.ARG_ITEM_ID, pItem.nId);
    //
    // jetzt das richtige Fragment auswählen und aktivieren
    //
    switch( pItem.nId )
    {
      case R.string.progitem_exit:
        // ENDE
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "onNavigationDrawerItemSelected: make dialog for USER...");
        }
        AreYouSureDialogFragment sureDial = new AreYouSureDialogFragment(getString(R.string.dialog_sure_exit));
        sureDial.show(getFragmentManager().beginTransaction(), "programexit");
        break;
      //
      case R.string.progitem_config:
        if( isOnline )
        {
          //
          // Der Benutzer wählt den Konfigurationseintrag für den SPX
          //
          Log.i(TAG, "onNavigationDrawerItemSelected: create SPX42PreferencesFragment...");
          newFrag = new SPX42PreferencesFragment();
          newFrag.setArguments(arguments);
          mTitle = getString(R.string.conf_headline);
          fTrans = getFragmentManager().beginTransaction();
          fTrans.addToBackStack("SPX42PreferencesFragment");
        }
        break;
      //
      case R.string.progitem_progpref:
        //
        // der Benutzer will Programmeinstellungen setzen
        //
        Log.i(TAG, "onNavigationDrawerItemSelected: create ProgramPreferencesFragment...");
        newFrag = new ProgramPreferencesFragment();
        newFrag.setArguments(arguments);
        mTitle = getString(R.string.conf_prog_headline);
        fTrans = getFragmentManager().beginTransaction();
        fTrans.addToBackStack("ProgramPreferencesFragment");
        break;
      //
      case R.string.progitem_gaslist:
        if( isOnline )
        {
          //
          // der Benutzer wählt den Gaslisten Editmode
          //
          Log.i(TAG, "onNavigationDrawerItemSelected: create SPX42GaslistPreferencesFragment...");
          newFrag = new SPX42GaslistPreferencesFragment();
          newFrag.setArguments(arguments);
          mTitle = getString(R.string.gaslist_headline);
          fTrans = getFragmentManager().beginTransaction();
          fTrans.addToBackStack("SPX42GaslistPreferencesFragment");
        }
        break;
      //
      case R.string.progitem_about:
        //
        // Das ÜBER das Programm-Ding
        //
        Log.i(TAG, "onNavigationDrawerItemSelected: create ProgramAboutFragment...");
        newFrag = new ProgramAboutFragment();
        newFrag.setArguments(arguments);
        mTitle = getString(R.string.about_headline);
        fTrans = getFragmentManager().beginTransaction();
        fTrans.addToBackStack("ProgramAboutFragment");
        break;
      //
      case R.string.progitem_logging:
        //
        // Log vom SPX-lesen
        //
        if( isOnline )
        {
          Log.i(TAG, "onNavigationDrawerItemSelected: create SPX42ReadLogFragment...");
          newFrag = new SPX42ReadLogFragment();
          newFrag.setArguments(arguments);
          mTitle = getString(R.string.logread_headline);
          fTrans = getFragmentManager().beginTransaction();
          fTrans.addToBackStack("SPX42ReadLogFragment");
        }
        break;
      //
      case R.string.progitem_loggraph:
        //
        // Logs grafisch darstellen
        //
        Log.i(TAG, "onNavigationDrawerItemSelected: create SPX42LogGraphSelectFragment...");
        newFrag = new SPX42LogGraphSelectFragment();
        newFrag.setArguments(arguments);
        mTitle = getString(R.string.graphlog_header);
        fTrans = getFragmentManager().beginTransaction();
        fTrans.addToBackStack("SPX42LogGraphSelectFragment");
        break;
      //
      case R.string.progitem_loggraph_detail:
        //
        // Logs detailiert darstellen
        //
        Log.i(TAG, "onNavigationDrawerItemSelected: create SPX42LogGraphDetailFragment...");
        newFrag = new SPX42LogGraphSelectFragment();
        newFrag.setArguments(arguments);
        mTitle = getString(R.string.graphlog_header);
        fTrans = getFragmentManager().beginTransaction();
        fTrans.addToBackStack("SPX42LogGraphDetailFragment");
        break;
      //
      case R.string.progitem_export:
        //
        // Logs exportieren
        //
        Log.i(TAG, "onNavigationDrawerItemSelected: startSPXExportLogFragment...");
        newFrag = new SPX42ExportLogFragment();
        newFrag.setArguments(arguments);
        mTitle = getString(R.string.export_header);
        fTrans = getFragmentManager().beginTransaction();
        fTrans.addToBackStack("SPX42ExportLogFragment");
        break;
      //
      case R.string.progitem_spx_status:
        if( isOnline )
        {
          //
          // Eine Statussetie des SPX anzeigen
          //
          Log.i(TAG, "onNavigationDrawerItemSelected: create SPX42HealthFragment...");
          newFrag = new SPX42HealthFragment();
          newFrag.setArguments(arguments);
          mTitle = getString(R.string.health_header);
          fTrans = getFragmentManager().beginTransaction();
          fTrans.addToBackStack("SPX42HealthFragment");
        }
        break;
      case R.string.progitem_help:
        //
        // Online hilfe aufrufen, wenn es in den Einstellungen erlaubt wurde
        // sonst Sperrbildschirm
        //
        Log.i(TAG, "onNavigationDrawerItemSelected: Create OnlineHelpFragment...");
        newFrag = new OnlineHelpFragment();
        newFrag.setArguments(arguments);
        fTrans = getFragmentManager().beginTransaction();
        fTrans.addToBackStack("OnlineHelpFragment");
        mTitle = getString(R.string.progitem_help);
        if( PreferenceManager.getDefaultSharedPreferences(this).contains("keyProgOthersOnlineHelpEnabled") && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("keyProgOthersOnlineHelpEnabled", false) )
        {
          Log.i(TAG, "Online Help ist TRUE");
          (( OnlineHelpFragment ) newFrag).setUrl(getResources().getString(R.string.online_help_remote_url));
        }
        else
        {
          Log.i(TAG, "Online Help ist FALSE");

          (( OnlineHelpFragment ) newFrag).setUrl(getResources().getString(R.string.online_help_local_url));
        }
        break;
      default:
        Log.w(TAG, "Not programitem found for <" + pItem.nId + ">");
      case R.string.progitem_connect:
        Log.i(TAG, "onNavigationDrawerItemSelected: create SPX42ConnectFragment");
        newFrag = new SPX42ConnectFragment();
        newFrag.setArguments(arguments);
        mTitle = getString(R.string.connect_headline);
        fTrans = getFragmentManager().beginTransaction();
        fTrans.addToBackStack("SPX42ConnectFragment");
    }
    if( fTrans != null )
    {
      fTrans.replace(R.id.main_container, newFrag);
      fTrans.setTransition( FragmentTransaction.TRANSIT_FRAGMENT_FADE);
      fTrans.commit();
    }
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onNavigationDrawerItemSelected:...OK");
    }
  }

  @Override
  public void onResume()
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onResume...");
    }
    super.onResume();
    //
    if( mBtAdapter == null )
    {
      if( ProjectConst.CHECK_PHYSICAL_BT )
      {
        // es gibt gar keinen Adapter!
        Toast.makeText(this, R.string.toast_exit_nobt, Toast.LENGTH_LONG).show();
        finish();
        return;
      }
      else
      {
        Toast.makeText(this, R.string.toast_exit_nobt, Toast.LENGTH_LONG).show();
        return;
      }
    }
    if( android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
    {
      //
      // ab Android 6 erst mal die Rechte checken
      //
      if( !checkPermissions() )
      {
        //
        // Check initiieren
        //
        requestPermissions( storagePerm, ProjectConst.REQUEST_MAIN_ACCESS);
        return;
      }
      //
      // hier weiter wenn alles klar ist
      //
    }
    //
    // Ist das Datenbankverzeichnis da?
    //
    if( databaseDir == null || !databaseDir.canWrite() )
    {
      Toast.makeText(this, R.string.toast_main_no_databasedir, Toast.LENGTH_LONG).show();
      finish();
      return;
    }
    //
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

  boolean checkPermissions()
  {
    boolean result = true;

    if( android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
    {
      if( PackageManager.PERMISSION_DENIED == checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) )
      {
        result = false;
      }
      if( PackageManager.PERMISSION_DENIED == checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) )
      {
        result = false;
      }
      if( PackageManager.PERMISSION_DENIED == checkSelfPermission(Manifest.permission.BLUETOOTH) )
      {
        result = false;
      }
      if( PackageManager.PERMISSION_DENIED == checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) )
      {
        result = false;
      }
    }
    return( result );
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
  {
    boolean result = true;
    //
    switch (requestCode)
    {
      case ProjectConst.REQUEST_MAIN_ACCESS:
        // If request is cancelled, the result arrays are empty.
        for( int number = 0; number < grantResults.length; number++ )
        {
          Log.v( TAG, "Permission" + permissions[number] + " is " + grantResults[number] );
          if( grantResults[number] == PackageManager.PERMISSION_DENIED )
          {
            result = false;
          }
        }
        if( result )
        {
          //
          // da ich onResume unterbrochen habe, hier weitermachen, wenn erlaubt!
          // Ist das Datenbankverzeichnis da?
          //
          if( databaseDir == null || !databaseDir.canWrite() )
          {
            Toast.makeText(this, R.string.toast_main_no_databasedir, Toast.LENGTH_LONG).show();
            finish();
            return;
          }
          //
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
        else
        {
          //
          // wenn die Rechte nicht eingeräumt wurden
          //
          Log.e(TAG, "onRequestPermissionsResult: not enough rights for APP!");
          Toast.makeText(this, R.string.toast_main_no_rights, Toast.LENGTH_LONG).show();
          finish();
          return;
        }
        break;

      case ProjectConst.REQUEST_BT_PRIVELEGED_LOCATION:
        // If request is cancelled, the result arrays are empty.
        for( int number = 0; number < grantResults.length; number++ )
        {
          Log.v( TAG, "Permission" + permissions[number] + " is " + grantResults[number] );
          if( grantResults[number] == PackageManager.PERMISSION_DENIED )
          {
            result = false;
          }
        }
        break;

      case ProjectConst.REQUEST_FINE_LOCATION:
        // If request is cancelled, the result arrays are empty.
        for( int number = 0; number < grantResults.length; number++ )
        {
          Log.v( TAG, "Permission" + permissions[number] + " is " + grantResults[number] );
          if( grantResults[number] == PackageManager.PERMISSION_DENIED )
          {
            result = false;
          }
        }
        break;

      default:
        Log.e( TAG, "onRequestPermissionsResult: wrong intent number: " + requestCode );
    }
  }
  /**
   * Den Programmtitel entsprechend der Selektion setzen, Callback des aufgerufenen Fragmentes
   * <p/>
   * Project: NaviTest Package: com.example.navitest.gui
   * <p/>
   * Stand: 06.11.2014
   *
   * @param pItem ProgrammItem Eintrag des selektieren Menüpunktes
   */
  public void onSectionAttached(String pItem)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, String.format(Locale.ENGLISH, "onSectionAttached: fragment for  <%s>...", pItem));
    }
    getActionBar().setDisplayShowTitleEnabled(true);
    mTitle = pItem;
    getActionBar().setTitle(mTitle);
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onSectionAttached: OK");
    }
  }

  @Override
  public void onStop()
  {
    super.onStop();
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onStop:...");
    }
    doUnbindService(true);
  }

  /**
   * Wann immer ain Alias Manager gebraucht wird...
   * <p/>
   * Project: SubmatixBTAndroid4 Package: de.dmarcini.submatix.android4.full.gui
   * <p/>
   * Stand: 09.12.2014
   *
   * @return Wahr, wenn er erzeugt wurde
   */
  public boolean openAliasManager()
  {
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "openAliasManager...");
    }
    //
    // wenn da einer geöffnet ist, erst mal schliessen
    //
    if( aliasManager != null )
    {
      aliasManager.close();
      aliasManager = null;
    }
    //
    // Ist ein Pfad vorhanden, ein Dir und schreibfähig
    //
    if( databaseDir == null || !databaseDir.isDirectory() || !databaseDir.canWrite() )
    {
      UserAlertDialogFragment alrt = new UserAlertDialogFragment(getString(R.string.dialog_error_database_dir_header), getString(R.string.dialog_error_database_no_dir) + "/" + getString(R.string.dialog_error_database_not_writable));
      alrt.show(getFragmentManager(), "database_directory_alert");
      return (false);
    }
    try
    {
      //
      // die Datenbank öffnen, wenn erforderlich
      //
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "openAliasManager: create SQLite helper...");
      }
      DataSQLHelper sqlHelper = new DataSQLHelper(getApplicationContext(), databaseDir.getAbsolutePath() + File.separator + ProjectConst.DATABASE_NAME);
      //
      // einen Alias Manager öffnen
      //
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "openAliasManager: open Database...");
      }
      aliasManager = new SPX42AliasManager(sqlHelper.getWritableDatabase());
    }
    catch( NullPointerException ex )
    {
      //
      // Beim File Objekt ist was falsch gelaufen -> Abort
      //
      Log.e(TAG, "NullPointerException: <" + ex.getLocalizedMessage() + ">");
      UserAlertDialogFragment uad = new UserAlertDialogFragment(getString(R.string.dialog_sqlite_error_header), getString(R.string.dialog_sqlite_nodatabase_error));
      uad.show(getFragmentManager(), "abortProgram");
      return (false);
    }
    catch( NoDatabaseException ex )
    {
      //
      // Es gibt keine Database -> Programm beenden
      //
      Log.e(TAG, "NoDatabaseException: <" + ex.getLocalizedMessage() + ">");
      UserAlertDialogFragment uad = new UserAlertDialogFragment(getString(R.string.dialog_sqlite_error_header), getString(R.string.dialog_sqlite_nodatabase_error));
      uad.show(getFragmentManager(), "abortProgram");
      return (false);
    }
    //
    // wenn alles geklappt hat
    //
    return (true);
  }

  /**
   * Den Listener löschen, d.h. die Activity macht das wieder selber
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 24.02.2013
   *
   * @param listener
   */
  public void removeServiceListener(IBtServiceListener listener)
  {
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "clearServiceListener()...");
    }
    //
    // wenn der listener vorhanden ist, entferne ihn aus der Liste
    //
    int index = serviceListener.indexOf(listener);
    if( index > -1 )
    {
      serviceListener.remove(index);
    }
  }

  /**
   * ActionBar restaurieren bei onCreateOptionsMenu
   * <p/>
   * Project: NaviTest Package: com.example.navitest
   * <p/>
   * Stand: 06.11.2014
   */
  @SuppressWarnings( "deprecation" )
  public void restoreActionBar()
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "restoreActionBar:...");
    }
    ActionBar actionBar = getActionBar();
    // deprecated since API 21
    if( Build.VERSION.SDK_INT < 21 )
    {
      actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setTitle(mTitle);
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "restoreActionBar:...OK");
    }
  }

  /**
   * App Preferenzen setzen, wenn First Time -> Standarts setzen
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * <p/>
   * Stand: 09.11.2014
   *
   * @param sPref
   */
  private void setAppPreferences(SharedPreferences sPref)
  {
    if( !sPref.contains(FIRSTTIME_KEY) )
    {
      Log.w(TAG, "onCreate: not found firsttime key == make first time preferences...");
      if( !sPref.getBoolean(FIRSTTIME_KEY, false) )
      {
        setDefaultPreferences();
      }
    }
    //
    // sind die Preferenzen in der richtigen version?
    //
    if( sPref.contains(PREFVERSION_KEY) )
    {
      if( ProjectConst.PREF_VERSION != sPref.getInt(PREFVERSION_KEY, 0) )
      {
        //FIXME: alte Datenbank übernehmen, sofern vorhanden
        Log.w(TAG, "onCreate: pref version to old == make first time preferences...");
        setDefaultPreferences();
      }
    }
    else
    {
      Log.w(TAG, "onCreate: pref version not found == make first time preferences...");
      setDefaultPreferences();
    }
    if( sPref.contains(PROG_TIME_FORMAT_KEY) )
    {
      localTimeFormatter = DateTimeFormat.forPattern(sPref.getString(PROG_TIME_FORMAT_KEY, "yyyy/dd/MM - hh:mm:ss a"));
    }
    //
    // Verzeichnis für Datenbanken etc
    // Suche den Datenbankpfad aus den Preferenzen oder, falls nicht vorhanden aus getDatabaseDir
    //
    databaseDir = new File(sPref.getString( PROG_DATA_DIR_KEY, getDatabaseDir().getAbsolutePath() ));
  }

  /**
   * Erzeuge Preferenzen für den SPX42 Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   */
  public void setDefaultPreferences()
  {
    String gasKeyTemplate = getResources().getString(R.string.conf_gaslist_gas_key_template);
    String gasListDefault = getResources().getString(R.string.conf_gaslist_default);
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "setDefaultPreferences: make default preferences...");
    }
    PreferenceManager.setDefaultValues(this, R.xml.config_spx42_preference_individual, true);
    PreferenceManager.setDefaultValues(this, R.xml.config_program_preference, true);
    //
    // workarround um defaults zu setzen
    //
    SharedPreferences        sPref  = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor editor = sPref.edit();
    editor.remove(FIRSTTIME_KEY);
    editor.putBoolean(FIRSTTIME_KEY, true);
    editor.remove(PREFVERSION_KEY);
    editor.putInt(PREFVERSION_KEY, ProjectConst.PREF_VERSION);
    //
    // Gaslistenpresets eintragen
    //
    for( int i = 1; i < 9; i++ )
    {
      editor.putString(String.format(Locale.getDefault(), gasKeyTemplate, i), gasListDefault);
    }
    //
    // Storage eintragen
    //
    databaseDir = getDatabaseDir();
    //
    // Wenn nix gefunden, dann current eintragen
    //
    if( databaseDir != null )
    {
      editor.putString(PROG_DATA_DIR_KEY, databaseDir.getAbsolutePath());
    }
    else
    {
      editor.putString(PROG_DATA_DIR_KEY, ".");
    }
    //
    // alles in die Propertys
    //
    if( editor.commit() )
    {
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "setDefaultPreferences: wrote preferences to storeage.");
      }
    }
    else
    {
      Log.e(TAG, "setDefaultPreferences: CAN'T wrote preferences to storage.");
    }
  }

  /**
   * schreibe den Setpoint ins Gerät
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 02.06.2013
   *
   * @param auto
   * @param pressure
   * @throws FirmwareNotSupportetException
   */
  public void writeAutoSetpoint(int auto, int pressure) throws FirmwareNotSupportetException
  {
    if( mService != null )
    {
      mService.writeAutoSetpoint(spxConfig, auto, pressure);
    }
  }

  /**
   * Schreibe nach dem verbinden in das Gerät die Korrekte Uhrzeit und das Datum
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 27.10.2013
   *
   * @throws FirmwareNotSupportetException
   */
  private void writeDateTimeToDevice() throws FirmwareNotSupportetException
  {
    if( mService != null )
    {
      mService.writeDateTimeToDevice(spxConfig, new DateTime());
    }
  }

  /**
   * schreibe DECO-Preferenzen auf das Gerät
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 13.07.2013
   *
   * @param logG
   * @param highG
   * @param deepSt
   * @param dynGr
   * @param lastStop
   * @throws FirmwareNotSupportetException
   */
  public void writeDecoPrefs(int logG, int highG, int deepSt, int dynGr, int lastStop) throws FirmwareNotSupportetException
  {
    if( mService != null )
    {
      mService.writeDecoPrefs(spxConfig, logG, highG, deepSt, dynGr, lastStop);
    }
  }

  /**
   * schreibe Display Preferenzen in den SPX
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 14.07.2013
   *
   * @param lumin  Helligkeit
   * @param orient Orientierung
   * @throws FirmwareNotSupportetException
   */
  public void writeDisplayPrefs(int lumin, int orient) throws FirmwareNotSupportetException
  {
    if( mService != null )
    {
      mService.writeDisplayPrefs(spxConfig, lumin, orient);
    }
  }

  /**
   * schreibe ein Gas setup in den SPX42
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 18.07.2013
   *
   * @param gasUpdates
   * @throws FirmwareNotSupportetException
   */
  public void writeGasSetup(Vector<GasUpdateEntity> gasUpdates) throws FirmwareNotSupportetException
  {
    if( mService != null )
    {
      mService.writeGasSetup(spxConfig, gasUpdates);
    }
  }

  /**
   * schreibe INDIVIDUAL Einstellungen in den SPX42
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 14.07.2013
   *
   * @param sensorsOff
   * @param pscrOff
   * @param sensorsCount
   * @param soundOn
   * @param logInterval
   * @param tempStick    TemopStick typ (bei neuerer Firmware, sonst 0)
   * @throws FirmwareNotSupportetException
   */
  public void writeIndividualPrefs(int sensorsOff, int pscrOff, int sensorsCount, int soundOn, int logInterval, int tempStick) throws FirmwareNotSupportetException
  {
    if( mService != null )
    {
      mService.writeIndividualPrefs(spxConfig, sensorsOff, pscrOff, sensorsCount, soundOn, logInterval, tempStick);
    }
  }

  /**
   * schreibe die Einstellungen für Masseinheiten in den SPX42
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 14.07.2013
   *
   * @param isTempMetric
   * @param isDepthMetric
   * @param isFreshwater
   * @throws FirmwareNotSupportetException
   */
  public void writeUnitPrefs(int isTempMetric, int isDepthMetric, int isFreshwater) throws FirmwareNotSupportetException
  {
    if( mService != null )
    {
      mService.writeUnitPrefs(spxConfig, isTempMetric, isDepthMetric, isFreshwater);
    }
  }

  @Override
  public void onBackStackChanged()
  {
    //
    // Monitoring für den FragmentStack
    int count = getFragmentManager().getBackStackEntryCount();
    int idx;
    //
    if( count > 0 )
    {
      for( idx = 0; idx < count; idx++ )
      {
        FragmentManager.BackStackEntry entr = getFragmentManager().getBackStackEntryAt(idx);
        Log.i(TAG2, String.format(Locale.ENGLISH, "Stack %02d Fragment <%s>", idx, entr.getName()));
      }
    }
    //
    // Monitoring ENDE
    //
  }

}
