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
package de.dmarcini.submatix.android4.full.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

import de.dmarcini.submatix.android4.full.BuildConfig;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.gui.MainActivity;
import de.dmarcini.submatix.android4.full.interfaces.INoticeDialogListener;
import de.dmarcini.submatix.android4.full.utils.FileSelectorArrayAdapterWithPics;

/**
 * Fragment als Selektor
 * <p>
 * Project: SubmatixBTAndroid4 Package: de.dmarcini.submatix.android4.full.dialogs
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p>
 *         Stand: 07.12.2014
 */
public class DatabaseFileDialog extends DialogFragment implements OnItemClickListener
{
  private static final String TAG = DatabaseFileDialog.class.getSimpleName();
  private View rootView;
  private ListView              dirListView = null;
  private TextView              dirNameView = null;
  private File                  currDir     = null;
  private INoticeDialogListener mListener   = null;

  /**
   * Privater Konstruktor (gesperrt)
   * <p>
   * Project: SubmatixBTAndroid4 Package: de.dmarcini.submatix.android4.full.dialogs
   * <p>
   * Stand: 07.12.2014
   */
  @SuppressWarnings( "unused" )
  private DatabaseFileDialog()
  {
  }

  /**
   * Konstruktor mit START Directory
   * <p>
   * Project: SubmatixBTAndroid4 Package: de.dmarcini.submatix.android4.full.dialogs
   * <p>
   * Stand: 07.12.2014
   *
   * @param startDirectory
   */
  public DatabaseFileDialog(File startDirectory)
  {
    if( startDirectory == null )
    {
      //
      // Es wurde ein Nullzeiger übergeben
      //
      if( BuildConfig.DEBUG )
      {
        Log.w(TAG, "no directory was selected on constructor!");
      }
      this.currDir = Environment.getDataDirectory();
    }
    else
    {
      //
      // es wurde ein File Objekt übergeben, jetzt abchecken was dran ist
      //
      if( startDirectory.exists() )
      {
        // Das Dateiobjekt existiert, jetzt geneuer gucken
        if( startDirectory.isFile() )
        {
          Log.w(TAG, "the selected directory was an file, get directory!");
          this.currDir = new File(startDirectory.getParent());
        }
        else if( startDirectory.isDirectory() )
        {
          this.currDir = startDirectory;
        }
        else
        {
          //
          // Es ist keine Datei,kein Verzeichnis -> PANIK
          //
          Log.e(TAG, "the selected directory was not an directory or an file! Fallback to default.");
          this.currDir = Environment.getDataDirectory();
        }
      }
      else
      {
        //
        // Das zum Fileobjekt gehörende Objekt existiert nicht
        //
        if( BuildConfig.DEBUG )
        {
          Log.w(TAG, "the selected directory was not exist on constructor!");
        }
        this.currDir = Environment.getDataDirectory();
      }
    }
  }

  // Überschreibe onAttach für meine Zwecke mit dem Listener
  @Override
  public void onAttach(Activity activity)
  {
    super.onAttach(activity);
    setStyle(STYLE_NORMAL, 0);
    // Implementiert die Activity den Listener?
    try
    {
      // Instanziere den Listener, wenn möglich, ansonsten wirft das eine exception
      mListener = ( INoticeDialogListener ) activity;
    }
    catch( ClassCastException ex )
    {
      // Die activity implementiert den Listener nicht, werfe eine Exception
      throw new ClassCastException(activity.toString() + " must implement INoticeDialogListener");
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    boolean isDirListFilles = false;
    //
    // Benutze die Builderklasse zum erstellen des Dialogs
    //
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    // Get the layout inflater
    LayoutInflater inflater = getActivity().getLayoutInflater();
    // Inflate and set the layout for the dialog
    // Pass null as the parent view because its going in the dialog layout
    rootView = inflater.inflate(R.layout.fragment_dialog_file_main, ( ViewGroup ) null);
    //
    // Die Inhalte in das ListView einlesen
    //
    dirListView = ( ListView ) rootView.findViewById(R.id.fileDialogListView);
    dirNameView = ( TextView ) rootView.findViewById(R.id.fileDialogPath);
    // Überschrift
    dirNameView.setText(currDir.getAbsolutePath());
    isDirListFilles = setFillesArrayAdapterToList(dirListView, currDir);
    //
    // jetzt dem Builder das View übergeben
    //
    builder.setView(rootView);
    //
    // Dialog Reaktionen bei Klick integrieren
    //
    if( isDirListFilles )
    {
      builder.setPositiveButton(R.string.dialog_save_button, new DialogInterface.OnClickListener()
      {
        @Override
        public void onClick(DialogInterface dialog, int id)
        {
          //
          // Den Pfad zwischenspeichern
          //
          mListener.onDialogPositiveClick(DatabaseFileDialog.this);
        }
      });
    }
    builder.setNegativeButton(R.string.dialog_cancel_button, new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int id)
      {
        // Gib in der App bescheid, ich will es so!
        mListener.onDialogNegativeClick(DatabaseFileDialog.this);
      }
    });
    //
    // Dialog erzeugen und Objekt zurück geben
    //
    return (builder.create());
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    File clickedFileObj;
    //
    //
    // ist der Eintrag ein File Objekt
    //
    if( dirListView.getAdapter().getItem(position) instanceof File )
    {
      clickedFileObj = ( File ) dirListView.getAdapter().getItem(position);
      if( clickedFileObj.isDirectory() )
      {
        //
        // ist es ein Verzeichnis? dann geht es
        //
        if( clickedFileObj.getPath().equals("..") )
        {
          // Parent, wenn möglich
          clickedFileObj = currDir.getParentFile();
          if( clickedFileObj != null )
          {
            currDir = clickedFileObj;
          }
          else
          {
            Log.i(TAG, "not parent at root folder!");
          }
        }
        else
        {
          // einfach zum gekennzeichneten wechseln
          currDir = clickedFileObj;
        }
        dirNameView.setText(currDir.getAbsolutePath());
        setFillesArrayAdapterToList(dirListView, currDir);
      }
      else
      {
        // wenn debug, dann informiere dem Programmierer
        if( BuildConfig.DEBUG )
        {
          Log.d(TAG, "onItemClick: clicked Object not an FOLDER");
        }
      }
    }
    else
    {
      // Warnung für den Programmierer
      Log.w(TAG, "onItemClick: clicked Objekct not an FILE Object!");
    }
  }

  /**
   * Füllt den ArrayAdapter zum Anzeigen der Dateien/Ordner
   * <p>
   * Project: SubmatixBTAndroid4 Package: de.dmarcini.submatix.android4.full.dialogs
   * <p>
   * Stand: 07.12.2014
   *
   * @param dirListView
   * @param cd
   * @return Füllen hat geklappt oder nicht
   */
  private boolean setFillesArrayAdapterToList(ListView dirListView, File cd)
  {
    FileSelectorArrayAdapterWithPics sArrayAdapter = null;
    //
    if( cd.exists() )
    {
      sArrayAdapter = new FileSelectorArrayAdapterWithPics(getActivity().getApplicationContext(), R.layout.array_with_pic_adapter_view, MainActivity.getAppStyle(), cd);
      dirListView.setAdapter(sArrayAdapter);
      // jetzt noch den Listener beim Click setzen
      dirListView.setOnItemClickListener(this);
      return (true);
    }
    return (false);
  }

  /**
   * Wenn das Dialogfeld erfolgreich beendet ist, gibt das das Directory zurück
   * <p>
   * Project: SubmatixBTAndroid4 Package: de.dmarcini.submatix.android4.full.dialogs
   * <p>
   * Stand: 07.12.2014
   *
   * @return das ausgewählte Verzeichnis
   */
  public File getCurrDir()
  {
    return (currDir);
  }
}
