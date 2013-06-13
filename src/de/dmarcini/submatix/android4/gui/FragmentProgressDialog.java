/**
 * Ein Dialog zum WARTERN
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 13.06.2013
 */
package de.dmarcini.submatix.android4.gui;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.dmarcini.submatix.android4.R;

/**
 * Klasse für den Wartedialog
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 13.06.2013
 */
public class FragmentProgressDialog extends DialogFragment
{
  private TextView msgView  = null;
  private String   vMessage = "none";

  public FragmentProgressDialog( String msg )
  {
    this.vMessage = msg;
  }

  //
  @Override
  public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
  {
    View v = inflater.inflate( R.layout.fragment_dialog_please_wait, container, false );
    msgView = ( TextView )v.findViewById( R.id.dialogMessageString );
    if( ( vMessage != null ) && ( !vMessage.isEmpty() ) )
    {
      msgView.setText( vMessage );
    }
    // gleich neu zeichnen!
    v.invalidate();
    return v;
  }

  public void setMax( int maxevents )
  {
    // TODO Automatisch generierter Methodenstub
  }

  public void setProgress( int i )
  {
    // TODO Automatisch generierter Methodenstub
  }

  public void setTitle( String title )
  {
    // super.setTitle( title );
  }

  public void setMessage( String msg )
  {
    vMessage = msg;
    if( msgView != null )
    {
      msgView.setText( msg );
    }
  }
}
