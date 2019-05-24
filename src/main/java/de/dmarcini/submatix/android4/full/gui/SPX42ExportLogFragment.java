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
/**
 * Code für Grundformular exportLog
 * <p/>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * <p/>
 * <p/>
 * Stand: 28.08.2013
 */
package de.dmarcini.submatix.android4.full.gui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.support.v4.content.FileProvider;

import org.joda.time.DateTime;
import org.w3c.dom.DOMException;

import java.io.File;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import de.dmarcini.submatix.android4.full.BuildConfig;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.full.dialogs.AreYouSureToDeleteFragment;
import de.dmarcini.submatix.android4.full.dialogs.SelectDeviceDialogFragment;
import de.dmarcini.submatix.android4.full.dialogs.UserAlertDialogFragment;
import de.dmarcini.submatix.android4.full.dialogs.WaitProgressFragmentDialog;
import de.dmarcini.submatix.android4.full.exceptions.NoDatabaseException;
import de.dmarcini.submatix.android4.full.exceptions.NoXMLDataFileFoundException;
import de.dmarcini.submatix.android4.full.exceptions.XMLFileCreatorException;
import de.dmarcini.submatix.android4.full.interfaces.IBtServiceListener;
import de.dmarcini.submatix.android4.full.utils.CommToast;
import de.dmarcini.submatix.android4.full.utils.DataSQLHelper;
import de.dmarcini.submatix.android4.full.utils.ProjectConst;
import de.dmarcini.submatix.android4.full.utils.ReadLogItemObj;
import de.dmarcini.submatix.android4.full.utils.SPX42LogManager;
import de.dmarcini.submatix.android4.full.utils.SPX42ReadLogListArrayAdapter;
import de.dmarcini.submatix.android4.full.utils.UDDFFileCreateClass;

/**
 * Klasse für das Grundformular zum exportieren der Logs
 * <p/>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p/>
 *         Stand: 10.11.2013
 */
public class SPX42ExportLogFragment extends Fragment implements IBtServiceListener, OnItemClickListener, OnClickListener
{
  private static final String                     TAG                 = SPX42ExportLogFragment.class.getSimpleName();
  private final        Pattern                    mailPattern         = Pattern.compile(ProjectConst.PATTERN_EMAIL);
  private final        Pattern                    uddfUnzipPattern    = Pattern.compile(ProjectConst.PATTERN_UDDF_UNZIP);
  private final        Pattern                    uddfZipPattern      = Pattern.compile(ProjectConst.PATTERN_UDDF_ZIPPED);
  private              MainActivity               runningActivity     = null;
  private              ListView                   mainListView        = null;
  private              SPX42LogManager            logManager          = null;
  private              int                        selectedDeviceId    = -1;
  private              String                     selectedDeviceAlias = null;
  private              Button                     changeDeviceButton  = null;
  private              Button                     exportLogsButton    = null;
  private              Button                     exportDeleteButton  = null;
  private              CommToast                  theToast            = null;
  private              boolean                    isFileZipped        = false;
  private              WaitProgressFragmentDialog pd                  = null;
  private              File                       tempDir             = null;
  private              String                     mailMainAddr        = null;
  @SuppressLint( "HandlerLeak" )
  private final        Handler                    mHandler            = new Handler()
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
      handleMessages(msg.what, smsg);
    }
  };
  private              String                     fragmentTitle       = "unknown";
  private              int                        themeId             = R.style.AppDarkTheme;
  private              String                     uddfFileName        = "";

  /**
   * vernichte rekursiv den Ordner mit allen Dateien darin
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * <p/>
   * Stand: 08.01.2014
   *
   * @param fileToDelete
   */
  private void deleteUddfFromDir(File fileToDelete)
  {
    if( fileToDelete.exists() && fileToDelete.isFile() )
    {
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "deleteUddfFromDir <" + fileToDelete.getAbsolutePath());
      }
      if( ! fileToDelete.delete() )
      {
        Log.w( TAG, String.format( "file <%s> can't delete, try delete on exit...", fileToDelete.getName() ));
        fileToDelete.deleteOnExit();
      }
      else
      {
        if( BuildConfig.DEBUG )
        {
          Log.d( TAG, String.format( "file <%s> deleted...", fileToDelete.getName() ));
        }
      }
    }
  }

  /**
   * Exportiere einen Eintrag in eigenem Thread
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * <p/>
   * Stand: 14.3.2017
   *
   * @param lItems Obkete der zu exportierenden Logs
   * @param _tempDir das temporäre Verzeichnis
   */
  private void exportLogItemsAsThread(final Vector<ReadLogItemObj> lItems, File _tempDir)
  {
    final Vector<ReadLogItemObj> rlos         = new Vector<ReadLogItemObj>(lItems);
    final File                   tempDir      = _tempDir;
    Thread                       exportThread = null;
    //
    //
    exportThread = new Thread()
    {
      @Override
      public void run()
      {
        UDDFFileCreateClass uddfClass    = null;
        //String              uddfFileName = null;
        //
        try
        {
          // erzeuge eine Klasse zum generieren der Exort-UDDF-Files
          uddfClass = new UDDFFileCreateClass();
          //
          // Lass dir einen Namen einfallen
          //
          if( rlos.size() == 0 )
          {
            Log.e(TAG, "exportThread: not selected divelog in parameters found! ABORT EXPORT!");
            mHandler.obtainMessage(ProjectConst.MESSAGE_LOCAL_EXPORTERR, new BtServiceMessage(ProjectConst.MESSAGE_LOCAL_EXPORTERR)).sendToTarget();
            return;
          }
          if( rlos.size() == 1 )
          {
            if( BuildConfig.DEBUG )
            {
              Log.i(TAG, String.format("exportThread: export dive %d db-id: %d...", rlos.firstElement().numberOnSPX, rlos.firstElement().dbId));
            }
            DateTime st = new DateTime(rlos.firstElement().startTimeMilis);
            uddfFileName = String.format(Locale.ENGLISH, "%s%sdive_%07d_at_%04d%02d%02d%02d%02d%02d.uddf", tempDir.getAbsolutePath(), File.separator, rlos.firstElement().numberOnSPX, st.getYear(), st.getMonthOfYear(), st.getDayOfMonth(), st.getHourOfDay(), st.getMinuteOfHour(), st.getSecondOfMinute());
          }
          else
          {
            if( BuildConfig.DEBUG )
            {
              Log.i(TAG, String.format("exportThread: export %d dives ...", rlos.size()));
            }
            DateTime st = new DateTime(rlos.firstElement().startTimeMilis);
            uddfFileName = String.format(Locale.ENGLISH, "%s%sdive_%07d_at_%04d%02d%02d%02d%02d%02d-plus-%03d.uddf",
                                         tempDir.getAbsolutePath(),
                                         File.separator,
                                         rlos.firstElement().numberOnSPX,
                                         st.getYear(),
                                         st.getMonthOfYear(),
                                         st.getDayOfMonth(),
                                         st.getHourOfDay(),
                                         st.getMinuteOfHour(),
                                         st.getSecondOfMinute(),
                                         rlos.size());
          }
          //
          // erzeuge die XML...
          //
          if( BuildConfig.DEBUG )
          {
            Log.d(TAG, "create uddf-file: <" + uddfFileName + ">");
          }
          uddfClass.createXML(new File(uddfFileName), mHandler, rlos, isFileZipped);
          //
          // melde das Ende an den UI-Thread
          //
          mHandler.obtainMessage(ProjectConst.MESSAGE_LOCAL_LOGEXPORTED, new BtServiceMessage(ProjectConst.MESSAGE_LOCAL_LOGEXPORTED)).sendToTarget();
        }
        catch( ParserConfigurationException ex )
        {
          theToast.showConnectionToastAlert(ex.getLocalizedMessage());
          Log.e(TAG, ex.getLocalizedMessage());
          mHandler.obtainMessage(ProjectConst.MESSAGE_LOCAL_EXPORTERR, new BtServiceMessage(ProjectConst.MESSAGE_LOCAL_EXPORTERR)).sendToTarget();
          return;
        }
        catch( TransformerException ex )
        {
          theToast.showConnectionToastAlert(ex.getLocalizedMessage());
          Log.e(TAG, ex.getLocalizedMessage());
          mHandler.obtainMessage(ProjectConst.MESSAGE_LOCAL_EXPORTERR, new BtServiceMessage(ProjectConst.MESSAGE_LOCAL_EXPORTERR)).sendToTarget();
          return;
        }
        catch( TransformerFactoryConfigurationError ex )
        {
          theToast.showConnectionToastAlert(ex.getLocalizedMessage());
          Log.e(TAG, ex.getLocalizedMessage());
          mHandler.obtainMessage(ProjectConst.MESSAGE_LOCAL_EXPORTERR, new BtServiceMessage(ProjectConst.MESSAGE_LOCAL_EXPORTERR)).sendToTarget();
          return;
        }
        catch( XMLFileCreatorException ex )
        {
          theToast.showConnectionToastAlert(ex.getLocalizedMessage());
          Log.e(TAG, ex.getLocalizedMessage());
          mHandler.obtainMessage(ProjectConst.MESSAGE_LOCAL_EXPORTERR, new BtServiceMessage(ProjectConst.MESSAGE_LOCAL_EXPORTERR)).sendToTarget();
          return;
        }
        catch( DOMException ex )
        {
          theToast.showConnectionToastAlert(runningActivity.getResources().getString(R.string.toast_export_internal_xml_error));
          Log.e(TAG, ex.getLocalizedMessage());
          mHandler.obtainMessage(ProjectConst.MESSAGE_LOCAL_EXPORTERR, new BtServiceMessage(ProjectConst.MESSAGE_LOCAL_EXPORTERR)).sendToTarget();
          return;
        }
        catch( NoXMLDataFileFoundException ex )
        {
          theToast.showConnectionToastAlert(runningActivity.getResources().getString(R.string.toast_export_cant_find_xmldata));
          Log.e(TAG, ex.getLocalizedMessage());
          mHandler.obtainMessage(ProjectConst.MESSAGE_LOCAL_EXPORTERR, new BtServiceMessage(ProjectConst.MESSAGE_LOCAL_EXPORTERR)).sendToTarget();
          return;
        }
      }
    };
    exportThread.setName("log_export_thread");
    exportThread.start();
  }

  /**
   * Exportiere die markierten Einträge, falls welche markiert sind
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * Stand: 04.01.2014
   *
   * @param rAdapter Der LogListAdapter
   */
  private void exportSelectedLogItems(SPX42ReadLogListArrayAdapter rAdapter)
  {
    int                    itemIndex = 0;
    Vector<ReadLogItemObj> lItems    = new Vector<ReadLogItemObj>();
    //
    if( rAdapter == null || rAdapter.getMarkedItems().isEmpty() )
    {
      Log.i(TAG, "exportSelectedLogItems: not selected items");
      UserAlertDialogFragment uad = new UserAlertDialogFragment(runningActivity.getResources().getString(R.string.dialog_not_selected_items_header), runningActivity.getResources().getString(R.string.dialog_not_selected_items));
      uad.show(getFragmentManager(), "noSelectedLogitems");
      return;
    }
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, String.format("exportSelectedLogItems: export %d selected items...", rAdapter.getMarkedItems().size()));
    }
    // die LogObjekte in den Vector kopieren
    Iterator<Integer> it = rAdapter.getMarkedItems().iterator();
    while( it.hasNext() )
    {
      // den ersten index bitteschön!
      itemIndex = it.next();
      // das Objekt kopiern
      ReadLogItemObj rlo = rAdapter.getItem(itemIndex);
      // noch die Positon merken, damit ich dann die Markierung löschen kann
      rlo.tagId = itemIndex;
      lItems.add(rlo);
    }
    lItems.trimToSize();
    //
    // so, jetzt hab ich die infrage kommenden Einträge
    // jetz starte ich den Thread für den ersten Eintrag
    //
    if( !lItems.isEmpty() )
    {
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "temporary path: " + tempDir.getAbsolutePath());
      }
      //
      // stelle sicher, dass ein exportverzeichnis existiert
      //
      if( !tempDir.exists() || !tempDir.isDirectory() )
      {
        theToast.showConnectionToastAlert(String.format(getResources().getString(R.string.toast_export_cant_create_dir), tempDir.getAbsolutePath()));
        return;
      }
      //
      // den warten.Dialog zeigen
      //
      openWaitDial(lItems.size(), String.format("file nr %d", lItems.firstElement().numberOnSPX));
      // export!
      exportLogItemsAsThread(lItems, tempDir);
    }
  }

  /**
   * Fülle den ListAdapter mit den Einträgen für Tauchgänge aus der Datenbank
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * Stand: 04.01.2014
   *
   * @param diveId
   * @return
   */
  private boolean fillListAdapter(int diveId)
  {
    Vector<ReadLogItemObj>       diveList;
    SPX42ReadLogListArrayAdapter logListAdapter;
    Resources                    res;
    //
    res = runningActivity.getResources();
    //
    // Creiere einen Adapter
    //
    logListAdapter = new SPX42ReadLogListArrayAdapter(runningActivity, R.layout.read_log_array_adapter_view, MainActivity.getAppStyle());
    logListAdapter.setShowSavedStatus(false);
    mainListView.setAdapter(logListAdapter);
    mainListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
    //
    // lese eine Liste der Tauchgänge ein
    //
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "read divelist for dbId: <" + diveId + ">...");
    }
    diveList = logManager.getDiveListForDevice(diveId, res, true);
    if( diveList != null && diveList.size() > 0 )
    {
      Iterator<ReadLogItemObj> it = diveList.iterator();
      //
      // Die Liste in den Adapter implementieren
      //
      while( it.hasNext() )
      {
        ReadLogItemObj rlo = it.next();
        // Eintrag einbauen
        logListAdapter.add(rlo);
      }
    }
    return (true);
  }

  /**
   * Lese die Mailadresse aus den Preferences
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * <p/>
   * Stand: 10.01.2014
   *
   * @return
   */
  private String getMainMailFromPrefs()
  {
    String  mailAddr    = null;
    boolean isPrefError = false;
    //
    // ist eine Zieladresse zum Versand vorgesehen?
    //
    SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(runningActivity);
    if( !sPref.contains("keyProgMailMain") )
    {
      // Das wird nix, keine Mail angegeben
      isPrefError = true;
      Log.w(TAG, "there is not preference key for mailadress!");
    }
    if( !isPrefError )
    {
      // der Schlüssel ist da, ist da auch eine Mailadresse hinterlegt?
      mailAddr = sPref.getString("keyProgMailMain", "");
      Matcher m = mailPattern.matcher(mailAddr);
      if( !m.find() )
      {
        // Das wird nix, keine Mail angegeben
        isPrefError = true;
        Log.w(TAG, "there is not an valid mailadress! saved was :<" + mailAddr + ">");
      }
      else
      {
        return (mailAddr);
      }
    }
    return (null);
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see de.dmarcini.submatix.android4.gui.IBtServiceListener#handleMessages(int, de.dmarcini.submatix.android4.comm.BtServiceMessage)
   */
  @Override
  public void handleMessages(int what, BtServiceMessage msg)
  {
    switch( what )
    {
      case ProjectConst.MESSAGE_TICK:
        break;
      // ################################################################
      // JA, Positive Antwort
      // ################################################################
      case ProjectConst.MESSAGE_DIALOG_POSITIVE:
        onDialogPositive(( DialogFragment ) msg.getContainer());
        break;
      // ################################################################
      // NEIN, negative antwort
      // ################################################################
      case ProjectConst.MESSAGE_DIALOG_NEGATIVE:
        onDialogNegative(( DialogFragment ) msg.getContainer());
        break;
      // ################################################################
      // Logeintrag erfolgreich exportiert
      // ################################################################
      case ProjectConst.MESSAGE_LOCAL_ONE_PROTO_OK:
        msgExportOneProtocolOk(msg);
        break;
      // ################################################################
      // Alle Logeinträge erfolgreich exportiert
      // ################################################################
      case ProjectConst.MESSAGE_LOCAL_LOGEXPORTED:
        msgExportOk(msg);
        break;
      // ################################################################
      // Logeintrag erfolgreich exportiert
      // ################################################################
      case ProjectConst.MESSAGE_LOCAL_EXPORTERR:
        msgExportError(msg);
        break;
      // ################################################################
      // DEFAULT
      // ################################################################
      default:
        if( BuildConfig.DEBUG )
        {
          Log.i(TAG, "unknown messsage with id <" + what + "> recived!");
        }
    }
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see de.dmarcini.submatix.android4.gui.IBtServiceListener#msgConnected(de.dmarcini.submatix.android4.comm.BtServiceMessage)
   */
  @Override
  public void msgConnected(BtServiceMessage msg)
  {
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see de.dmarcini.submatix.android4.gui.IBtServiceListener#msgConnectError(de.dmarcini.submatix.android4.comm.BtServiceMessage)
   */
  @Override
  public void msgConnectError(BtServiceMessage msg)
  {
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see de.dmarcini.submatix.android4.gui.IBtServiceListener#msgConnecting(de.dmarcini.submatix.android4.comm.BtServiceMessage)
   */
  @Override
  public void msgConnecting(BtServiceMessage msg)
  {
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see de.dmarcini.submatix.android4.gui.IBtServiceListener#msgDisconnected(de.dmarcini.submatix.android4.comm.BtServiceMessage)
   */
  @Override
  public void msgDisconnected(BtServiceMessage msg)
  {
  }

  /**
   * Wenn der Export schief ging, Ende und aufräumen
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * <p/>
   * Stand: 09.01.2014
   *
   * @param msg
   */
  private void msgExportError(BtServiceMessage msg)
  {
    // Messagebox verschwinden lassen
    if( pd != null )
    {
      pd.dismiss();
      pd = null;
    }
    //
    // Aufräumen
    //
    if( !uddfFileName.isEmpty() )
    {
      deleteUddfFromDir(new File(uddfFileName));
    }
  }

  /**
   * Export Ok, Mail senden?
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * <p/>
   * Stand: 09.01.2014
   *
   * @param msg
   */
  private void msgExportOk(BtServiceMessage msg)
  {
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "export ok");
    }
    //
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "send message (" + uddfFileName + ")...");
    }
    //
    // alle Elemente exortiert, jetzt bitte Mail fertig machen und versenden
    //
    sendMailToAddr( new File(uddfFileName), new String[]{mailMainAddr}, selectedDeviceAlias);
    //
    // Aufräumen
    //
    if( pd != null )
    {
      pd.dismiss();
      pd = null;
    }
  }

  /**
   * Nachricht vom Thread, dass wieder einmal ein Protokoll exportiert ist
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * <p/>
   * Stand: 16.01.2014
   *
   * @param msg
   */
  private void msgExportOneProtocolOk(BtServiceMessage msg)
  {
    ReadLogItemObj rlo;
    //
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "export ok, check next entry...");
    }
    //
    // es sind noch Elemente zu exportieren
    //
    if( msg.getContainer() != null && (msg.getContainer() instanceof ReadLogItemObj) )
    {
      rlo = ( ReadLogItemObj ) msg.getContainer();
      //
      // die Markierung umkehren
      //
      rlo.isMarked = false;
      int firstPos = mainListView.getFirstVisiblePosition();
      mainListView.setAdapter(mainListView.getAdapter());
      View v   = mainListView.getChildAt(rlo.tagId);
      int  top = (v == null) ? 0 : v.getTop();
      mainListView.setSelectionFromTop(firstPos, top);
      if( pd != null )
      {
        pd.setMessage(String.format(getResources().getString(R.string.logread_file_message), rlo.numberOnSPX));
      }
    }
    else
    {
      //
      // da ist ein Missgeschick passiert, kein ReadLogItemObj mitgesendet!
      //
      if( pd != null )
      {
        pd.setMessage(getResources().getString(R.string.logread_file_message_empty));
      }
    }
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see de.dmarcini.submatix.android4.gui.IBtServiceListener#msgRecivedAlive(de.dmarcini.submatix.android4.comm.BtServiceMessage)
   */
  @Override
  public void msgRecivedAlive(BtServiceMessage msg)
  {
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see de.dmarcini.submatix.android4.gui.IBtServiceListener#msgRecivedTick(de.dmarcini.submatix.android4.comm.BtServiceMessage)
   */
  @Override
  public void msgRecivedTick(BtServiceMessage msg)
  {
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see de.dmarcini.submatix.android4.gui.IBtServiceListener#msgReciveWriteTmeout(de.dmarcini.submatix.android4.comm.BtServiceMessage)
   */
  @Override
  public void msgReciveWriteTmeout(BtServiceMessage msg)
  {
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState)
  {
    super.onActivityCreated(savedInstanceState);
    runningActivity = ( MainActivity ) getActivity();
    themeId = MainActivity.getAppStyle();
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onActivityCreated: ACTIVITY ATTACH");
    }
    //
    // temporaeres Verzeichnis für die zu exportierenden Dateien
    //
    tempDir = getTempFilesDir();
    try
    {
      mainListView = ( ListView ) runningActivity.findViewById(R.id.exportLogsListView);
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
  }

  /**
   * versuche ein temporäres Verzeichnis für Daten zu finden
   * @return
   */
  private File getTempFilesDir()
  {
    File tempFilesDir;
    //
    // bei neueren Androiden ist das sehr restriktiv, versuche den Standart
    //
    tempFilesDir = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS );
    //
    // der Standartpfad
    //
    if( tempFilesDir.exists() && tempFilesDir.isDirectory() && tempFilesDir.canWrite() )
    {
      Log.i(TAG, String.format("use export dir <%s>...", tempFilesDir.getAbsolutePath()));
      return (tempFilesDir);
    }
    //
    // doch den Standart nutzen
    //
    tempFilesDir = runningActivity.getBaseContext().getFilesDir().getAbsoluteFile();
    Log.i(TAG, String.format("use system files dir <%s>...", tempFilesDir.getAbsolutePath()));
    return (tempFilesDir);
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
      case ProjectConst.REQUEST_SEND_MAIL:
        if( BuildConfig.DEBUG )
        {
          Log.d(TAG, "send logs via mail...OK");
          if( !uddfFileName.isEmpty())
          {
            deleteUddfFromDir(new File(uddfFileName));
          }
        }
        break;
      default:
        Log.e(TAG, "unknown activity result...");
    }
  }

  @Override
  public void onAttach(Activity activity)
  {
    super.onAttach(activity);
    runningActivity = ( MainActivity ) activity;
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onAttach: ATTACH");
    }
    //
    // die Haupt-Mailadresse holen, wenn vorhanden
    //
    mailMainAddr = getMainMailFromPrefs();
    //
    // soll die Mail komprimiert übertreagen werden?
    //
    isFileZipped = shouldMailComressed();
    //
    // die Datenbank öffnen
    //
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onAttach: create SQLite helper...");
    }
    DataSQLHelper sqlHelper = new DataSQLHelper(getActivity().getApplicationContext(), MainActivity.databaseDir.getAbsolutePath() + File.separator + ProjectConst.DATABASE_NAME);
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onAttach: open Database...");
    }
    try
    {
      logManager = new SPX42LogManager(sqlHelper.getWritableDatabase());
    }
    catch( NoDatabaseException ex )
    {
      Log.e(TAG, "NoDatabaseException: <" + ex.getLocalizedMessage() + ">");
      UserAlertDialogFragment uad = new UserAlertDialogFragment(runningActivity.getResources().getString(R.string.dialog_sqlite_error_header), runningActivity.getResources().getString(R.string.dialog_sqlite_nodatabase_error));
      uad.show(getFragmentManager(), "abortProgram");
    }
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
   */
  @Override
  public void onClick(View v)
  {
    SPX42ReadLogListArrayAdapter rAdapter;
    Button                       button = null;
    //
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "Click");
    }
    //
    // Wurde ein Button geklickt?
    //
    if( v instanceof Button )
    {
      button = ( Button ) v;
      //
      // soll das angezeigte Gerät gewechselt werden?
      //
      if( button == changeDeviceButton )
      {
        // Hier wird dann ein Dialog gebraucht!
        if( BuildConfig.DEBUG )
        {
          Log.d(TAG, "onClick: call changeDeviceDialog!");
        }
        SelectDeviceDialogFragment dialog = new SelectDeviceDialogFragment();
        dialog.setDeviceList(logManager.getDeviceNameIdList());
        dialog.show(getFragmentManager(), "selectDeviceForExport");
      }
      //
      // oder sollen Daten exportiert werden?
      //
      else if( button == exportLogsButton )
      {
        if( BuildConfig.DEBUG )
        {
          Log.d(TAG, "onClick: EXPORT selected Items");
        }
        //
        // wenn keine Mailadresse da ist, hat das eh keinen Zweck
        //
        if( mailMainAddr == null )
        {
          // Das wird nix, keine Mail angegeben
          if( BuildConfig.DEBUG )
          {
            Log.w(TAG, "not valid mail -> note to user");
          }
          UserAlertDialogFragment uad = new UserAlertDialogFragment(runningActivity.getResources().getString(R.string.dialog_not_mail_exist_header), runningActivity.getResources().getString(R.string.dialog_not_mail_exist));
          uad.show(getFragmentManager(), "noMailaddrWarning");
          return;
        }
        rAdapter = ( SPX42ReadLogListArrayAdapter ) mainListView.getAdapter();
        exportSelectedLogItems(rAdapter);
      }
      //
      // oder sollen Daten aus der Datenbank gelöscht werden?
      //
      else if( v == exportDeleteButton )
      {
        rAdapter = ( SPX42ReadLogListArrayAdapter ) mainListView.getAdapter();
        //
        // wenn der Adapter nicht existiert, gibt es keine Auswahl
        //
        if( rAdapter == null )
        {
          // Das wird nix, kein Gerät ausgewählt
          if( BuildConfig.DEBUG )
          {
            Log.w(TAG, "not valid mail -> note to user");
          }
          UserAlertDialogFragment uad = new UserAlertDialogFragment(runningActivity.getResources().getString(R.string.dialog_delete_header), runningActivity.getResources().getString(R.string.dialog_delete_nothing_selected));
          uad.show(getFragmentManager(), "nothingSelectedToDelete");
          return;
        }
        //
        // wenn keine Logs ausgewählt sind, könnte noch ALLES gemeint sein
        //
        if( rAdapter.getCountMarkedItems() <= 0 )
        {
          //
          // Meinst Du das wirklich?
          //
          String                     msg                = String.format(runningActivity.getResources().getString(R.string.dialog_delete_device), selectedDeviceAlias);
          AreYouSureToDeleteFragment sureToDeleteDialog = new AreYouSureToDeleteFragment(msg);
          sureToDeleteDialog.show(getFragmentManager().beginTransaction(), "sureToDeleteDeviceData");
          return;
        }
        //
        // Also meint der die selektierten Logs?
        //
        String                     msg                = runningActivity.getResources().getString(R.string.dialog_delete_selected_logs);
        AreYouSureToDeleteFragment sureToDeleteDialog = new AreYouSureToDeleteFragment(msg);
        sureToDeleteDialog.show(getFragmentManager().beginTransaction(), "sureToDeleteSelectedLogs");
      }
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View rootView = null;
    //
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onCreateView...");
    }
    theToast = new CommToast(getActivity());
    //
    // wenn kein Container vorhanden ist, dann gibts auch keinen View
    //
    if( container == null )
    {
      Log.e(TAG, "onCreateView: container is NULL ...");
      return (null);
    }
    //
    // View aus Resource laden
    //
    rootView = inflater.inflate(R.layout.fragment_export_log, container, false);
    setTitleString("?");
    //
    // Objekte lokalisieren
    //
    mainListView = ( ListView ) rootView.findViewById(R.id.exportLogsListView);
    mainListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
    changeDeviceButton = ( Button ) rootView.findViewById(R.id.changeDeviceButton);
    exportLogsButton = ( Button ) rootView.findViewById(R.id.exportLogsButton);
    exportDeleteButton = ( Button ) runningActivity.findViewById(R.id.exportDeleteButton);
    return (rootView);
  }

  /**
   * Wenn der Dialog Positiv abgeschlossen wurde (OKO oder ähnlich)
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * Stand: 01.12.2013
   *
   * @param dialog
   */
  public void onDialogNegative(DialogFragment dialog)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "Negative dialog click!");
    }
  }

  /**
   * Wenn der Dialog negativ abgeschlossen wurde (Abbruchg o.ä.)
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * Stand: 01.12.2013
   *
   * @param dialog
   */
  public void onDialogPositive(DialogFragment dialog)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "Positive dialog click!");
    }
    if( dialog instanceof SelectDeviceDialogFragment )
    {
      SelectDeviceDialogFragment deviceDialog = ( SelectDeviceDialogFragment ) dialog;
      selectedDeviceId = deviceDialog.getSelectedDeviceId();
      selectedDeviceAlias = logManager.getAliasForId(selectedDeviceId);
      if( BuildConfig.DEBUG )
      {
        Log.i(TAG, "onDialogNegative: selected Device Alias: <" + deviceDialog.getSelectedDeviceName() + "> Device-ID <" + deviceDialog.getSelectedDeviceId() + ">");
      }
      getActivity().getActionBar().setTitle(String.format(getResources().getString(R.string.export_header_device), deviceDialog.getSelectedDeviceName()));
      //
      // ist eigentlich ein Gerät ausgewählt?
      //
      if( selectedDeviceId > 0 )
      {
        fillListAdapter(selectedDeviceId);
      }
      else
      {
        // Das wird nix, kein Gerät ausgewählt
        if( BuildConfig.DEBUG )
        {
          Log.w(TAG, "no device selected -> note to user");
        }
        UserAlertDialogFragment uad = new UserAlertDialogFragment(runningActivity.getResources().getString(R.string.dialog_no_device_selected_header), runningActivity.getResources().getString(R.string.dialog_no_device_selected));
        uad.show(getFragmentManager(), "noSelectedDevice");
        return;
      }
      //
      // Wenn keine Einträge in der Datenbank waren
      //
      if( mainListView.getAdapter().isEmpty() )
      {
        // Das wird auch nix, Keine Einträge in der Datenbank
        if( BuildConfig.DEBUG )
        {
          Log.w(TAG, "no logs foir device -> note to user");
        }
        UserAlertDialogFragment uad = new UserAlertDialogFragment(runningActivity.getResources().getString(R.string.dialog_not_log_entrys_header), runningActivity.getResources().getString(R.string.dialog_not_log_entrys));
        uad.show(getFragmentManager(), "noLogsForDevice");
        return;
      }
    }
    //
    // ist das der Bist-Du-Sicher Dialog gewesen?
    //
    else if( dialog instanceof AreYouSureToDeleteFragment )
    {
      if( dialog.getTag().matches("sureToDeleteDeviceData") )
      {
        //
        // ALLE Daten des Gerätes (einschliesslich ALIAS und Datendateien) löschen
        //
        if( selectedDeviceId >= 0 )
        {
          if( BuildConfig.DEBUG )
          {
            Log.w(TAG, "DELETE ALL Data for device: <" + selectedDeviceAlias + "...");
          }
          //
          // Alle Daten verschwinden lassen
          //
          logManager.deleteAllDataForDevice(selectedDeviceId);
        }
        selectedDeviceAlias = null;
        selectedDeviceId = -1;
        mainListView.setAdapter(null);
      }
      else if( dialog.getTag().matches("sureToDeleteSelectedLogs") )
      {
        //
        // Selektierte Daten aus der Datenbank und die entsprechenden Dateien löschen
        //
        if( BuildConfig.DEBUG )
        {
          Log.w(TAG, "DELETE selectred logs...");
        }
        //
        // die markierten Einträge suchen
        //
        SPX42ReadLogListArrayAdapter rAdapter = ( SPX42ReadLogListArrayAdapter ) mainListView.getAdapter();
        if( rAdapter != null && selectedDeviceId >= 0 )
        {
          // gibt es Einträge?
          if( rAdapter.getCountMarkedItems() > 0 )
          {
            Vector<Integer> markedItems = rAdapter.getMarkedItems();
            //
            // hier sinnvoll löschen
            //
            Iterator<Integer> it = markedItems.iterator();
            while( it.hasNext() )
            {
              int idx  = it.next();
              int dbId = rAdapter.getItem(idx).dbId;
              logManager.deleteDataForDevice(dbId);
            }
          }
        }
        //
        // und dann die Liste wieder neu füllen
        //
        fillListAdapter(selectedDeviceId);
      }
    }
    else
    {
      Log.i(TAG, "onDialogNegative: UNKNOWN dialog type");
    }
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
   */
  @Override
  public void onItemClick(AdapterView<?> parent, View clickedView, int position, long id)
  {
    SPX42ReadLogListArrayAdapter rlAdapter = null;
    //
    // ist eine Zieladresse zum Versand vorgesehen?
    //
    if( mailMainAddr == null )
    {
      // Das wird nix, keine Mail angegeben
      if( BuildConfig.DEBUG )
      {
        Log.w(TAG, "not valid mail -> note to user");
      }
      UserAlertDialogFragment uad = new UserAlertDialogFragment(runningActivity.getResources().getString(R.string.dialog_not_mail_exist_header), runningActivity.getResources().getString(R.string.dialog_not_mail_exist));
      uad.show(getFragmentManager(), "noMailaddrWarning");
      return;
    }
    if( parent.equals(mainListView) )
    {
      rlAdapter = ( SPX42ReadLogListArrayAdapter ) mainListView.getAdapter();
      //
      // invertiere die Markierung im Adapter
      //
      rlAdapter.setMarked(position, !rlAdapter.getMarked(position));
      //
      // mache die Markierung auch im View (das wird ja sonst nicht automatisch aktualisiert)
      //
      ImageView ivMarked = ( ImageView ) clickedView.findViewById(R.id.readLogMarkedIconView);
      if( ivMarked != null )
      {
        if( rlAdapter.getMarked(position) )
        {
          if( this.themeId == R.style.AppDarkTheme )
          {
            ivMarked.setImageResource(R.drawable.circle_full_yellow);
          }
          else
          {
            ivMarked.setImageResource(R.drawable.circle_full_green);
          }
        }
        else
        {
          if( this.themeId == R.style.AppDarkTheme )
          {
            ivMarked.setImageResource(R.drawable.circle_empty_yellow);
          }
          else
          {
            ivMarked.setImageResource(R.drawable.circle_empty_green);
          }
        }
      }
    }
  }

  @Override
  public void onPause()
  {
    super.onPause();
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onPause...");
    }
    // Listener abmelden
    runningActivity.removeServiceListener(this);
  }

  @Override
  public synchronized void onResume()
  {
    super.onResume();
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onResume...");
    }
    // Listener aktivieren
    runningActivity.addServiceListener(this);
    mainListView.setOnItemClickListener(this);
    changeDeviceButton = ( Button ) runningActivity.findViewById(R.id.changeDeviceButton);
    changeDeviceButton.setOnClickListener(this);
    exportLogsButton = ( Button ) runningActivity.findViewById(R.id.exportLogsButton);
    exportLogsButton.setOnClickListener(this);
    exportDeleteButton = ( Button ) runningActivity.findViewById(R.id.exportDeleteButton);
    exportDeleteButton.setOnClickListener(this);
    //
    // Liste füllen
    //
    if( selectedDeviceId > 0 )
    {
      fillListAdapter(selectedDeviceId);
    }
    else
    {
      selectedDeviceAlias = null;
    }
    // Listener aktivieren
    runningActivity.addServiceListener(this);
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState)
  {
    super.onSaveInstanceState(savedInstanceState);
    fragmentTitle = savedInstanceState.getString(ProjectConst.ARG_ITEM_CONTENT);
    savedInstanceState.putString(ProjectConst.ARG_ITEM_CONTENT, fragmentTitle);
  }

  /**
   * Öffne einen wartedialog
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 08.01.2014
   *
   * @param maxevents
   * @param msg
   */
  public void openWaitDial(int maxevents, String msg)
  {
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "openWaitDial()...");
    }
    //
    // wenn ein Dialog da ist, erst mal aus den Fragmenten entfernen
    //
    FragmentTransaction ft   = runningActivity.getFragmentManager().beginTransaction();
    Fragment            prev = runningActivity.getFragmentManager().findFragmentByTag("dialog");
    if( prev != null )
    {
      ft.remove(prev);
    }
    if( pd != null )
    {
      pd.dismiss();
    }
    pd = new WaitProgressFragmentDialog();
    pd.setTitle(runningActivity.getResources().getString(R.string.logread_please_patient));
    pd.setMessage(msg);
    pd.setCancelable(true);
    pd.setMax(maxevents);
    pd.setProgress(0);
    ft.addToBackStack(null);
    pd.show(ft, "dialog");
  }

  /**
   * Sende die UDDF-Files an den geneigten Benutzer
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * <p/>
   * Stand: 10.01.2014
   *
   * @param uddfFile
   * @param deviceAlias
   * @param mailAddr
   */
  private void sendMailToAddr(final File uddfFile, final String[] mailAddr, String deviceAlias)
  {
    // String diveTime = null;
    String         diveMessage;
    //ArrayList<Uri> uddfURIs = new ArrayList<Uri>();
    Uri uddfURI;
    Vector<File>   filtered;
    //
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "send logs via mail...");
    }
    //
    // Dateien zusammensuchen
    // OHNE Unterverzeichnisse
    //
    if( uddfFile.exists() && uddfFile.canRead()  )
    {
      Log.i(TAG, "uddf-file exist und is readable...");
    }
    else
    {
      UserAlertDialogFragment uad = new UserAlertDialogFragment(runningActivity.getResources().getString(R.string.dialog_no_uddfdir_header), runningActivity.getResources().getString(R.string.dialog_no_uddfdir));
      uad.show(getFragmentManager(), "noUddfDirectory");
      return;
    }
    //
    // Mailabsicht kundtun als INTEND
    //
    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
    // die Anhänge sind gezippt/nicht gezippt
    if( isFileZipped )
    {
      emailIntent.setType("application/x-gzip");
    }
    else
    {
      emailIntent.setType("text/xml");
    }
    //
    // URIS der Dateien in das Array tun
    //
    diveMessage = getResources().getString(R.string.export_mail_bodytext) + "\n\n" + getResources().getString(R.string.app_name);
    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, mailAddr);
    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, new String("Divelog (" + deviceAlias + ")" ) );
    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, diveMessage);
    uddfURI = FileProvider.getUriForFile(getContext(), "de.dmarcini.submatix.android4.full.provider", uddfFile );
    //uddfURI = Uri.parse("file://" + uddfFile.getAbsoluteFile());
    emailIntent.setAction(Intent.ACTION_SEND);
    emailIntent.putExtra(Intent.EXTRA_STREAM, uddfURI);
    startActivityForResult( Intent.createChooser( emailIntent, "Send mail..."), ProjectConst.REQUEST_SEND_MAIL);
  }

  /**
   * Setze den Titel in der Action Bar mit Test und Name des gelisteten Gerätes
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * Stand: 01.12.2013
   *
   * @param devName
   */
  private void setTitleString(String devName)
  {
    String titleString;
    //
    titleString = String.format(getResources().getString(R.string.export_header_device), devName);
    runningActivity.getActionBar().setTitle(titleString);
  }

  /**
   * Ist in den Prefs Komprimierung eingestellt?
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * <p/>
   * Stand: 20.01.2014
   *
   * @return koprimiert oder nicht
   */
  private boolean shouldMailComressed()
  {
    boolean isMailCompressed = false;
    boolean isPrefError      = false;
    //
    // ist eine Zieladresse zum Versand vorgesehen?
    //
    SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(runningActivity);
    if( !sPref.contains("keyProgMailCompressed") )
    {
      // Das wird nix, keine Mail angegeben
      isPrefError = true;
      Log.w(TAG, "there is not preference key for mailadress!");
    }
    if( !isPrefError )
    {
      // der Schlüssel ist da, ist da auch eine Mailadresse hinterlegt?
      isMailCompressed = sPref.getBoolean("keyProgMailCompressed", false);
    }
    return (isMailCompressed);
  }
}
