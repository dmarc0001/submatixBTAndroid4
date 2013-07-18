/**
 * Gaslisten im SPX42 editieren
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 01.01.2013
 */
package de.dmarcini.submatix.android4.gui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import de.dmarcini.submatix.android4.BuildConfig;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.utils.CommToast;
import de.dmarcini.submatix.android4.utils.GasPickerPreference;
import de.dmarcini.submatix.android4.utils.GasUtilitys;
import de.dmarcini.submatix.android4.utils.ProjectConst;
import de.dmarcini.submatix.android4.utils.SPX42GasParms;

/**
 * Editor für die Gaslisten
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 01.01.2013
 */
public class SPX42GaslistPreferencesFragment extends PreferenceFragment implements IBtServiceListener, OnSharedPreferenceChangeListener
{
  private static final String          TAG              = SPX42GaslistPreferencesFragment.class.getSimpleName();
  private static final int             maxEvents        = 8;
  private String                       gasKeyTemplate   = null;
  private Activity                     runningActivity  = null;
  private boolean                      ignorePrefChange = false;
  private final FragmentProgressDialog pd               = null;
  private CommToast                    theToast         = null;
  private String                       diluent1String, diluent2String, noDiluent, bailoutString;

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
      // ein Timeout beim Schreiben eines Kommandos trat auf!
      // ################################################################
      case ProjectConst.MESSAGE_COMMTIMEOUT:
        msgReciveWriteTmeout( smsg );
        break;
      // ################################################################
      // ein Timeout beim Schreiben eines Kommandos trat auf!
      // ################################################################
      case ProjectConst.MESSAGE_GAS_READ:
        msgReciveGasSetup( smsg );
        break;
      // ################################################################
      // Sonst....
      // ################################################################
      default:
        if( BuildConfig.DEBUG ) Log.i( TAG, "unhandled message with id <" + smsg.getId() + "> recived!" );
    }
  }

  @Override
  public void msgConnected( BtServiceMessage msg )
  {
    Log.v( TAG, "msgConnected()...ask for SPX config..." );
    FragmentCommonActivity fActivity = ( FragmentCommonActivity )runningActivity;
    if( BuildConfig.DEBUG ) Log.d( TAG, "msgConnected(): ask for SPX config..." );
    // Dialog schliesen, wenn geöffnet
    theToast.dismissDial();
    theToast.openWaitDial( maxEvents, getActivity().getResources().getString( R.string.dialog_please_wait_read_config ) );
    try
    {
      Thread.yield();
      Thread.sleep( 100 );
      Thread.yield();
      Thread.sleep( 100 );
      Thread.yield();
    }
    catch( InterruptedException ex )
    {}
    fActivity.askForGasFromSPX();
    ignorePrefChange = false;
  }

  @Override
  public void msgConnectError( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgConnecting( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgDisconnected( BtServiceMessage msg )
  {
    Log.v( TAG, "msgDisconnected" );
    Intent intent = new Intent( getActivity(), areaListActivity.class );
    intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
    startActivity( intent );
  }

  @Override
  public void msgRecivedAlive( BtServiceMessage msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "SPX Alive <" + ( String )msg.getContainer() + "> recived" );
    theToast.dismissDial();
  }

  @Override
  public void msgRecivedTick( BtServiceMessage msg )
  {}

  /**
   * 
   * Empfange eine Nachricht über eine Gaseinstellung (0..7)
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 18.07.2013
   * @param smsg
   */
  private void msgReciveGasSetup( BtServiceMessage msg )
  {
    String[] gasParm;
    int gasNr = 0, dil = 0, cg = 0;
    String gasKey;
    SPX42GasParms gasParms = new SPX42GasParms();
    //
    // GET_SETUP_GASLIST
    // ~39:NR:ST:HE:BA:AA:CG
    // NR: Numer des Gases
    // ST Stickstoff in Prozent (hex)
    // HELIUM
    // Bailout
    // AA Diluent 1 oder 2 oder keins
    // CG curent Gas
    if( msg.getContainer() instanceof String[] )
    {
      gasParm = ( String[] )msg.getContainer();
    }
    else
    {
      Log.e( TAG, "msgReciveGasSetup: message object not an String[] !" );
      return;
    }
    try
    {
      // "O2:HE:N2:D1:D2:BA"
      gasNr = Integer.parseInt( gasParm[0], 16 ); // Gasnr
      dil = Integer.parseInt( gasParm[4], 16 ); // diluent
      gasParms.n2 = Integer.parseInt( gasParm[1], 16 ); // n2
      gasParms.he = Integer.parseInt( gasParm[2], 16 ); // he
      gasParms.o2 = 100 - gasParms.he - gasParms.n2; // O2
      gasParms.d1 = ( dil == 1 ) ? true : false; // Diluent 1
      gasParms.d2 = ( dil == 2 ) ? true : false; // diluent 2?
      gasParms.bo = ( ( Integer.parseInt( gasParm[3], 16 ) > 0 ) ? true : false ); // bailout?
      cg = Integer.parseInt( gasParm[5], 16 ); // current gas
    }
    catch( IndexOutOfBoundsException ex )
    {
      Log.e( TAG, "msgReciveGasSetup: gas setup object has not enough elements! (" + ex.getLocalizedMessage() + ")" );
      return;
    }
    catch( NumberFormatException ex )
    {
      Log.e( TAG, "msgReciveGasSetup: gas setup object is not an correct integer! (" + ex.getLocalizedMessage() + ")" );
      return;
    }
    if( BuildConfig.DEBUG )
      Log.d( TAG, String.format( "msgReciveGasSetup: gas: %d, n2:%02d%%, he:%02d%%, bailout:%b, dil: %d, currentGas: %d", gasNr, gasParms.n2, gasParms.he, gasParms.bo, dil, cg ) );
    // Jetz in die Preference und damit in die GUI meisseln
    gasKey = String.format( gasKeyTemplate, gasNr );
    if( getPreferenceScreen().findPreference( gasKey ) instanceof GasPickerPreference )
    {
      GasPickerPreference gpp = ( GasPickerPreference )getPreferenceScreen().findPreference( gasKey );
      if( gpp == null )
      {
        Log.e( TAG, "msgReciveGasSetup: Key <" + gasKey + "> was not found an GasPickerPreference! abort!" );
        return;
      }
      ignorePrefChange = true;
      //
      // jetzt die Werte für Gas übernehmen
      //
      gpp.setValue( gasParms );
      setGasSummary( gasNr, gpp );
      ignorePrefChange = false;
    }
    else
    {
      Log.e( TAG, "msgReciveGasSetup: can't set gas setup value to preference..." );
    }
  }

  @Override
  public void msgReciveWriteTmeout( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void onAttach( Activity activity )
  {
    super.onAttach( activity );
    runningActivity = activity;
    Log.w( TAG, "ATTACH" );
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate()..." );
    theToast = new CommToast( getActivity() );
    Log.v( TAG, "onCreate: add Resouce id <" + R.xml.config_spx42_gaslist_preference + ">..." );
    gasKeyTemplate = getResources().getString( R.string.conf_gaslist_gas_key_template );
    diluent1String = getResources().getString( R.string.conf_gaslist_summary_diluent1 );
    diluent2String = getResources().getString( R.string.conf_gaslist_summary_diluent2 );
    noDiluent = getResources().getString( R.string.conf_gaslist_summary_no_diluent );
    bailoutString = getResources().getString( R.string.conf_gaslist_summary_bailout );
    addPreferencesFromResource( R.xml.config_spx42_gaslist_preference );
    //
    // initiiere die notwendigen summarys
    //
    setAllSummarys();
    Log.v( TAG, "onCreate: add Resouce...OK" );
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
    //
    // den Change-Listener abbestellen ;-)
    //
    getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener( this );
  }

  @Override
  public boolean onOptionsItemSelected( MenuItem item )
  {
    switch ( item.getItemId() )
    {
      case android.R.id.home:
        Log.v( TAG, "onOptionsItemSelected: HOME" );
        Intent intent = new Intent( getActivity(), areaListActivity.class );
        intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        startActivity( intent );
        return true;
    }
    return super.onOptionsItemSelected( item );
  }

  @Override
  public void onPause()
  {
    super.onPause();
    Log.v( TAG, "onPause..." );
    //
    // lösche Listener, der überwacht, wenn Preferenzen geändert wurden
    //
    getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener( this );
    if( BuildConfig.DEBUG ) Log.d( TAG, "onPause(): clear service listener for preferences fragment..." );
    ( ( FragmentCommonActivity )runningActivity ).removeServiceListener( this );
  }

  @Override
  public synchronized void onResume()
  {
    super.onResume();
    Log.v( TAG, "onResume..." );
    //
    // setze Listener, der überwacht, wenn Preferenzen geändert wurden
    //
    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener( this );
    ignorePrefChange = true;
    // Service Listener setzen
    FragmentCommonActivity fActivity = ( FragmentCommonActivity )runningActivity;
    if( BuildConfig.DEBUG ) Log.d( TAG, "onResume(): set service listener for preferences fragment..." );
    fActivity.addServiceListener( this );
  }

  @Override
  public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key )
  {
    GasPickerPreference gP = null;
    String gasProperty, gasName, gasExt;
    String[] fields;
    int o2, he;
    boolean d1 = false, d2 = false, bo = false;
    //
    Log.v( TAG, "onSharedPreferenceChanged()...." );
    if( BuildConfig.DEBUG ) Log.d( TAG, "onSharedPreferenceChanged: key = <" + key + ">" );
    if( ignorePrefChange )
    {
      if( BuildConfig.DEBUG ) Log.d( TAG, "onSharedPreferenceChanged: ignore change..." );
      return;
    }
    //
    // Wenn das von der GasPickergeschichte kommt
    //
    if( getPreferenceScreen().findPreference( key ) instanceof GasPickerPreference )
    {
      gP = ( GasPickerPreference )getPreferenceScreen().findPreference( key );
      if( gP == null )
      {
        Log.e( TAG, "onSharedPreferenceChanged: Key <" + key + "> was not found an GradientPickerPreference! Abort!" );
        return;
      }
      if( !sharedPreferences.contains( key ) )
      {
        Log.e( TAG, "onSharedPreferenceChanged: for Key <" + key + "> was not found an preference value! Abort!" );
        return;
      }
      //
      // erst mal auf alle Fälle den String laden und aufarbeiten
      //
      gasProperty = sharedPreferences.getString( key, getResources().getString( R.string.conf_gaslist_default ) );
      fields = gasProperty.split( ":" );
      if( fields.length < 3 )
      {
        Log.e( TAG, "onSharedPreferenceChanged: for Key <" + key + "> the preference value was not correct (" + gasProperty + ") ! Abort!" );
        return;
      }
      //
      // konvertiere die Parameter nach Int zur weiteren Verwendung
      //
      try
      {
        o2 = Integer.parseInt( fields[0] );
        he = Integer.parseInt( fields[1] );
        if( fields.length >= 6 )
        {
          d1 = Boolean.parseBoolean( fields[3] );
          d2 = Boolean.parseBoolean( fields[4] );
          bo = Boolean.parseBoolean( fields[5] );
        }
        //
        // baue den String für das Summary zusammen
        //
        gasExt = noDiluent;
        if( d1 ) gasExt = diluent1String;
        if( d2 ) gasExt = diluent2String;
        if( bo ) gasExt += bailoutString;
        gasName = GasUtilitys.getNameForGas( o2, he ) + gasExt;
      }
      catch( NumberFormatException ex )
      {
        Log.e( TAG, String.format( "onSharedPreferenceChanged: for key <%s> raised an NumberFormatException (%s)", key, ex.getLocalizedMessage() ) );
        return;
      }
      catch( Exception ex )
      {
        Log.e( TAG, String.format( "onSharedPreferenceChanged: for key <%s> raised an Exception (%s)", key, ex.getLocalizedMessage() ) );
        return;
      }
      for( int idx = 1; idx < 9; idx++ )
      {
        if( key.equals( String.format( gasKeyTemplate, idx ) ) )
        {
          // frag mal die resource ab
          gP.setSummary( String.format( getResources().getString( R.string.conf_gaslist_summary_first ), idx, gasName ) );
          break;
        }
      }
    }
    Log.v( TAG, "onSharedPreferenceChanged()....OK" );
  }

  @Override
  public void onViewCreated( View view, Bundle savedInstanceState )
  {
    super.onViewCreated( view, savedInstanceState );
    Log.v( TAG, "onViewCreated..." );
    PreferenceScreen ps = getPreferenceScreen();
    if( BuildConfig.DEBUG ) Log.d( TAG, "this preferencescreen has <" + ps.getPreferenceCount() + "> preferenes." );
    for( int prefIdx = 0; prefIdx < ps.getPreferenceCount(); prefIdx++ )
    {
      Preference pref = ps.getPreference( prefIdx );
      if( BuildConfig.DEBUG ) Log.d( TAG, String.format( "The Preference <%s> is number %d", pref.getTitle(), prefIdx ) );
      // jede ungerade Zeile färben
      if( prefIdx % 2 > 0 )
      {
        if( FragmentCommonActivity.getAppStyle() == R.style.AppDarkTheme )
        {
          // dunkles Thema
          pref.setLayoutResource( R.layout.preference_dark );
        }
        else
        {
          // helles Thema
          pref.setLayoutResource( R.layout.preference_light );
        }
      }
      else
      {
        pref.setLayoutResource( R.layout.preference );
      }
    }
  }

  /**
   * 
   * setze eine Gas-summary (Beschreibung in der Preference)
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 18.07.2013
   * @param gasNr
   * @param gpp
   *          GasPickerPreference Referenz
   * @param gasParms
   */
  private void setGasSummary( int gasNr, GasPickerPreference gpp )
  {
    String gasName, gasExt;
    SPX42GasParms gasParms;
    //
    // hole mal die Gaseinstellungen
    //
    gasParms = gpp.getValue();
    //
    // baue den String für das Summary zusammen
    //
    gasExt = noDiluent;
    if( gasParms.d1 ) gasExt = diluent1String;
    if( gasParms.d2 ) gasExt = diluent2String;
    if( gasParms.bo ) gasExt += bailoutString;
    gasName = GasUtilitys.getNameForGas( gasParms.o2, gasParms.he ) + gasExt;
    // schreib schön!
    gpp.setSummary( String.format( getResources().getString( R.string.conf_gaslist_summary_first ), gasNr, gasName ) );
  }

  /**
   * 
   * Setze alle summarys
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.01.2013
   */
  @SuppressLint( "DefaultLocale" )
  private void setAllSummarys()
  {
    //
    // alle Gase generisch durch (8 Gase sind im SPX42)
    //
    for( int idx = 0; idx < 8; idx++ )
    {
      String key = String.format( gasKeyTemplate, idx );
      GasPickerPreference gP = ( GasPickerPreference )getPreferenceScreen().findPreference( key );
      setGasSummary( idx, gP );
    }
  }
}
