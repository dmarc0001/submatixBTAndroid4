package de.dmarcini.submatix.android4.comm;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.gui.areaListActivity;
import de.dmarcini.submatix.android4.utils.ProjectConst;

public class BlueThoothComService extends Service
{
  private static final String TAG               = BlueThoothComService.class.getSimpleName();
  private static final long   msToEndService    = 10000L;
  private long                tickToCounter     = 0L;
  private long                timeToStopService = 0L;
  private NotificationManager nm;
  static int                  NOTIFICATION      = 815;
  private final Timer         timer             = new Timer();
  private int                 counter           = 0;
  private final int           incrementby       = 1;
  private boolean             isRunning         = false;
  ArrayList<IComServiceToApp> mClients          = new ArrayList<IComServiceToApp>();         // Keeps track of all current registered clients.
  int                         mValue            = 0;                                         // Holds last value set by a client.
  private final IBinder       mBinder           = new LocalBinder();

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
  }

  /**
   * Wird immer beim Binden eines Clienten aufgerufen
   */
  @Override
  public IBinder onBind( Intent intent )
  {
    Log.d( TAG, "onBind..." );
    showNotification( getText( R.string.notify_service ), getText( R.string.notify_service_connected ) );
    return mBinder;
  }

  @Override
  public boolean onUnbind( Intent intent )
  {
    Log.d( TAG, "onUnbind..." );
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
                                  .setSmallIcon( android.R.drawable.ic_btn_speak_now )
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
        Log.i( TAG, "Service stopping..." );
        stopSelf();
      }
    }
    if( System.currentTimeMillis() > tickToCounter )
    {
      Log.d( TAG, "Timer :<" + counter + ">" );
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
    for( int i = mClients.size() - 1; i >= 0; i-- )
    {
      try
      {
        mClients.get( i ).sendMessage( msg );
      }
      catch( NullPointerException ex )
      {
        // das ging schief, den Clienten NICHT mehr benutzen
        mClients.remove( i );
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
   * Eine App / einen Clioenten zur Benachrichtigung registrieren
   * 
   * Project: BtServiceVersuch Package: de.dmarcini.android.btservive
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 21.02.2013
   * @param msgr
   */
  public void registerClient( IComServiceToApp mActivity )
  {
    Log.i( TAG, "Client register" );
    mClients.add( mActivity );
    isRunning = true;
    timeToStopService = 0L;
  }

  /**
   * 
   * Eine Registrierung aufheben!
   * 
   * Project: BtServiceVersuch Package: de.dmarcini.android.btservive
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 21.02.2013
   * @param msgr
   */
  public void unregisterClient( IComServiceToApp mActivity )
  {
    Log.i( TAG, "Client unregister" );
    mClients.remove( mActivity );
    if( mClients.isEmpty() )
    {
      Log.i( TAG, "last Client ist removed..." );
      isRunning = false;
      // zeit bis zum Ende des Service setzen
      timeToStopService = System.currentTimeMillis() + msToEndService;
    }
  }
}
