package de.dmarcini.submatix.android4.utils;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.dmarcini.submatix.android4.R;

public class SPX42ReadLogListArrayAdapter extends ArrayAdapter<ReadLogItemObj>
{
  private static final String TAG     = SPX42ReadLogListArrayAdapter.class.getSimpleName();
  private int                 themeId = R.style.AppDarkTheme;

  /* private view holder class */
  private class ViewHolder
  {
    public ImageView imageView;
    @SuppressWarnings( "unused" )
    public TextView  numberAndNameTextView;
    @SuppressWarnings( "unused" )
    public TextView  fileDetailsTextView;
  }

  public SPX42ReadLogListArrayAdapter( Context context, int textViewResourceId, int themeId )
  {
    super( context, textViewResourceId );
    this.themeId = themeId;
  }

  private SPX42ReadLogListArrayAdapter( Context context, int resource, int textViewResourceId, int themeId )
  {
    super( context, resource, textViewResourceId );
    this.themeId = themeId;
  }

  private SPX42ReadLogListArrayAdapter( Context context, int textViewResourceId, List<ReadLogItemObj> objects, int themeId )
  {
    super( context, textViewResourceId, objects );
    this.themeId = themeId;
  }

  @Override
  public void add( ReadLogItemObj items )
  {
    super.add( items );
  }

  @Override
  public View getView( int position, View convertView, ViewGroup parent )
  {
    View cView = convertView;
    ReadLogItemObj rlio;
    ViewHolder holder = null;
    LayoutInflater mInflater;
    //
    mInflater = ( ( Activity )getContext() ).getLayoutInflater();
    rlio = getItem( position );
    //
    // guck mal, ob es das View gibt
    //
    if( cView == null )
    {
      cView = mInflater.inflate( R.layout.read_log_array_adapter_view, parent, false );
    }
    //
    // verorte die Objekte
    //
    ImageView iv = ( ImageView )cView.findViewById( R.id.readLogListIconView );
    TextView tvName = ( TextView )cView.findViewById( R.id.readLogNameListTextView );
    TextView tvDetail = ( TextView )cView.findViewById( R.id.readLogDetailsTextView );
    try
    {
      //
      // Icon setzen
      //
      if( rlio.isSaved )
      {
        iv.setImageResource( R.drawable.saved_log );
      }
      else
      {
        iv.setImageResource( R.drawable.unsaved_log );
      }
      //
      // Beschriftung setzen
      //
      tvName.setText( rlio.itemName );
      tvDetail.setText( rlio.itemDetail );
    }
    catch( NullPointerException ex )
    {
      // MACHWAS
    }
    return( cView );
  }
}
