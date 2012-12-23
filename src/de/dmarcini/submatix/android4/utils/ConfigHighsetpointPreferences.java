package de.dmarcini.submatix.android4.utils;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import de.dmarcini.submatix.android4.R;

public class ConfigHighsetpointPreferences extends PreferenceActivity
{
  @Override
  protected void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    addPreferencesFromResource( R.xml.config_high_setpoint_preference );
  }
}
