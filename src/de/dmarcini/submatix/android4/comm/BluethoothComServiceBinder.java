package de.dmarcini.submatix.android4.comm;

import java.util.ArrayList;


import android.bluetooth.BluetoothAdapter;
import android.os.Binder;
import android.os.Handler;
import android.util.Log;

/**
 * 
 * Binder zwischen Service und Activity
 * 
 * Project: SubmatixBTLog-Service Package: de.dmarcini.bluethooth.submatix.btService
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 19.10.2011
 */
public class BluethoothComServiceBinder extends Binder
{
  private static final String  TAG = BluethoothComServiceBinder.class.getSimpleName();
  private BluethoothComService srv = null;

  public void setService( BluethoothComService sv )
  {
    this.srv = sv;
  }

  /**
   * 
   * Lösche den Servicehandler
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.11.2012
   */
  public void clearServiceHandler()
  {
    if( srv.mIsBusy ) return;
    Log.v( TAG, "BINDER: LOESCHE HANDLER" );
    srv.mHandler = null;
  }

  /**
   * 
   * Gib den BT Adapter zurück, wenn vorhanden
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.11.2012
   * @return BT Adapter
   */
  public BluetoothAdapter getBTADapter()
  {
    return srv.mAdapter;
  }

  /**
   * 
   * Gib die beim Gerät bereits gepaarten BT-Geräte zurück
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.11.2012
   * @return ArrayList mit bereits gepaarten Geräten
   */
  public ArrayList<String[]> getPairedDevices()
  {
    if( srv.mIsBusy ) return( null );
    // return( srv.getPairedBTDevices() );
    return( null );
  }

  /**
   * 
   * Gib die eigene Instanz zurück
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.11.2012
   * @return Service selbst
   */
  public BluethoothComServiceBinder getService()
  {
    Log.v( TAG, "getService()..." );
    return( this );
  }

  /**
   * 
   * Gib den Handler mal zurück
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.11.2012
   * @return Handler
   */
  public Handler getServiceHandler()
  {
    return srv.mHandler;
  }

  /**
   * 
   * Ist der BT Adapter eingeschaltet
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.11.2012
   * @return Ist der BT Adapter angeschaltet?
   */
  public boolean isEnabledBTAdapter()
  {
    if( ( srv.mAdapter != null ) && srv.mAdapter.isEnabled() ) return( true );
    return false;
  }

  /**
   * 
   * Setze den Handler für Nachrichten an die App
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.comm
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.11.2012
   * @param mH
   */
  public void setServiceHandler( Handler mH )
  {
    Log.v( TAG, "BINDER: setze HANDLER" );
    srv.mHandler = mH;
    // srv.sendState();
  }
}
