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
 * Implementation der Konfiguration für SPX42 Android
 * <p>
 * <p>
 * <p>
 * Stand: 10.10.13
 */
package de.dmarcini.submatix.android4.full.utils;

/**
 * Objekt zur Sicherung der SPX42 Konfiguration
 * <p/>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)#
 *         <p/>
 *         Stand: 10.11.2013
 */
//@formatter:off
public class SPX42Config
{
  @SuppressWarnings( "javadoc" )
  public static final int UNITS_METRIC = 1;
  @SuppressWarnings( "javadoc" )
  public static final int UNITS_IMPERIAL = 2;
  //
  protected boolean wasCorrectInitialized = false;
  protected String deviceName = "no name";
  protected String firmwareVersion = "0";
  protected String serialNumber = "0";
  protected String connectedDeviceMac = "0";
  protected int licenseState = 0;
  protected boolean customEnabled = false;
  protected boolean hasFahrenheidBug = false;
  protected boolean canSetDate = true;
  protected boolean hasSixValuesIndividual = false;
  protected boolean isFirmwareSupported = false;
  protected boolean isOldParamSorting = false;  // bei alter Firmware war die Reihenfolge der Paramete anders
  protected boolean isNewerDisplayBrightness = false; // ab FW FIRMWARE_2_7H_R83CE 20 % Schritte
  protected boolean isSixMetersAutoSetpoint = false; // ab FW FIRMWARE_2_7H_R83CE autosetpoint ab 6 Meter
  protected boolean isAutoSetpointWithoutOff = false; // ab FW FIRMWARE_2_7H_R83CE autosetpoint nicht abschaltbar (Parameter beim read/write veränderter Wert)


  //
  //@formatter:on

  /**
   * Der Konstruktor, füllt falls notwendig sinnvolle Anfangswerte Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclg.utils
   */
  public SPX42Config()
  {
    clear();
  }

  /**
   * Kopierkonstruktor Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclg.utils
   *
   * @param cf
   */
  /**
   * Kopierkonstruktor
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 10.11.2013
   *
   * @param cf Das zu kopierende Config-Element
   */
  public SPX42Config(SPX42Config cf)
  {
    serialNumber = cf.serialNumber;
    connectedDeviceMac = cf.connectedDeviceMac;
    deviceName = cf.deviceName;
    firmwareVersion = cf.firmwareVersion;
    licenseState = cf.licenseState;
    customEnabled = cf.customEnabled;
    hasFahrenheidBug = cf.hasFahrenheidBug;
    canSetDate = cf.canSetDate;
    hasSixValuesIndividual = cf.hasSixValuesIndividual;
    isFirmwareSupported = cf.isFirmwareSupported;
    isOldParamSorting = cf.isOldParamSorting;
    isNewerDisplayBrightness = cf.isNewerDisplayBrightness;
    isSixMetersAutoSetpoint = cf.isSixMetersAutoSetpoint;
    isAutoSetpointWithoutOff = cf.isAutoSetpointWithoutOff;
  }

  /**
   * Kann die Firmware Dartum setzen?
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 10.11.2013
   *
   * @return Kann die Firmware ein Datum setzen
   */
  public boolean canSetDateTime()
  {
    return (canSetDate);
  }

  /**
   * Config-Objekt ungültig setzen
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 10.11.2013
   */
  public void clear()
  {
    wasCorrectInitialized = false;
    deviceName = "no name";
    firmwareVersion = "0";
    serialNumber = "0";
    connectedDeviceMac = "0";
    licenseState = 0;
    customEnabled = false;
    hasFahrenheidBug = false;
    canSetDate = true;
    hasSixValuesIndividual = false;
    isFirmwareSupported = false;
    isOldParamSorting = false; // bei alter Firmware war die Reihenfolge der Paramete anders
    isNewerDisplayBrightness = false; // ab FW FIRMWARE_2_7H_R83CE 20 % Schritte
    isSixMetersAutoSetpoint = false;
    isAutoSetpointWithoutOff = false;
  }

  /**
   * Vergleiche mit anderer Konfiguration
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 10.11.2013
   *
   * @param cf
   * @return Wahr, wenn die Configs gleich sind
   */
  public boolean compareWith(SPX42Config cf)
  {
    // immer wenn was nicht übereinstimmt ist Übertragung norwendig
    if( !wasCorrectInitialized )
    {
      return (false);
    }
    if( !serialNumber.equals(cf.serialNumber) )
    {
      return (false);
    }
    if( connectedDeviceMac.equals(cf.connectedDeviceMac) )
    {
      return (false);
    }
    if( !deviceName.equals(cf.deviceName) )
    {
      return (false);
    }
    if( !firmwareVersion.equals(cf.firmwareVersion) )
    {
      return (false);
    }
    if( licenseState != cf.licenseState )
    {
      return (false);
    }
    if( customEnabled != cf.customEnabled )
    {
      return (false);
    }
    if( hasFahrenheidBug != cf.hasFahrenheidBug )
    {
      return (false);
    }
    if( canSetDate != cf.canSetDate )
    {
      return (false);
    }
    if( hasSixValuesIndividual != cf.hasSixValuesIndividual )
    {
      return (false);
    }
    if( isFirmwareSupported != cf.isFirmwareSupported )
    {
      return (false);
    }
    if( isOldParamSorting != cf.isOldParamSorting )
    {
      return (false);
    }
    if( isNewerDisplayBrightness != cf.isNewerDisplayBrightness )
    {
      return (false);
    }
    if( isSixMetersAutoSetpoint != cf.isSixMetersAutoSetpoint )
    {
      return (false);
    }
    return isAutoSetpointWithoutOff == cf.isAutoSetpointWithoutOff;
  }

  /**
   * Ist CUSTOM lizensiert?
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 10.11.2013
   *
   * @return "1" wenn custom (individual) liuzensiert ist
   */
  public int getCustomEnabled()
  {
    if( customEnabled )
    {
      return (1);
    }
    return (0);
  }

  /**
   * Setze, ob CUSTOM lizensiert wurde
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 10.11.2013
   *
   * @param en
   */
  public void setCustomEnabled(boolean en)
  {
    customEnabled = en;
  }

  /**
   * Gerätename zurückgeben
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 10.11.2013
   *
   * @return Den Gerätenamen zurückgeben
   */
  public String getDeviceName()
  {
    return (deviceName);
  }

  /**
   * Setze den Gerätenamen
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 10.11.2013
   *
   * @param name
   */
  public void setDeviceName(String name)
  {
    deviceName = name;
  }

  /**
   * Firmwareversion zurückgeben
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 10.11.2013
   *
   * @return Gib die Firmwareversion zurück
   */
  public String getFirmwareVersion()
  {
    return (firmwareVersion);
  }

  /**
   * Setze die Firmwareversion und setze Flags (Unterscheide die Firmwareversionen)
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 27.09.2016
   *
   * @param version
   */
  public void setFirmwareVersion(String version)
  {
    firmwareVersion = version;
    // ich gehe mal vom sicheren Fall aus
    hasFahrenheidBug = true;
    canSetDate = false;
    hasSixValuesIndividual = false;
    isFirmwareSupported = false;
    isOldParamSorting = false;
    isNewerDisplayBrightness = false;
    isSixMetersAutoSetpoint = false;
    isAutoSetpointWithoutOff = false;
    //
    // versuch mal die Eigenschaften rauszufinden
    //
    // Beginne bei einer gaaaanz alten Version
    if( firmwareVersion.matches("V2\\.6.*") )
    {
      hasFahrenheidBug = true;
      isFirmwareSupported = true;
      isOldParamSorting = true;
      wasCorrectInitialized = true;
    }
    // Versionen NACH 2.7_V
    if( firmwareVersion.matches("V2\\.7_?V.*") )
    {
      wasCorrectInitialized = true;
      isFirmwareSupported = true;
      hasFahrenheidBug = false;
      // Build 198
      if( (firmwareVersion.matches("V2\\.7_?V r83.*")) )
      {
        hasSixValuesIndividual = true;
        isNewerDisplayBrightness = true;
        isSixMetersAutoSetpoint = true;
        canSetDate = true;
        isAutoSetpointWithoutOff = true;
      }
      else
      {
        canSetDate = false;
        hasSixValuesIndividual = false;
      }
    }
    // Versionen NACH 2.7_H
    if( firmwareVersion.matches("V2\\.7_?H.*") )
    {
      hasFahrenheidBug = false;
      canSetDate = false;
      isFirmwareSupported = true;
      wasCorrectInitialized = true;
      if( firmwareVersion.matches("V2\\.7_?H r83.*") )
      {
        // Build 197
        // hier kommt bestimmt noch irgendwas nach :-(
        hasSixValuesIndividual = true;
        isNewerDisplayBrightness = true;
        isSixMetersAutoSetpoint = true;
        canSetDate = true;
        isAutoSetpointWithoutOff = true;
      }
    }
  }

  /**
   * Gib Lizenzstatus zurück
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 10.11.2013
   *
   * @return Gib den Lizenzstatus zurück
   */
  public int getLicenseState()
  {
    return (licenseState);
  }

  /**
   * Gib Seriennummer zurück
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 10.11.2013
   *
   * @return Gib die Geräte-Is (Seriennummer) zurück
   */
  public String getSerial()
  {
    return (serialNumber);
  }

  /**
   * Setze die Seriennummer
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 10.11.2013
   *
   * @param serial
   */
  public void setSerial(String serial)
  {
    if( serial != null )
    {
      serialNumber = serial;
    }
  }

  /**
   * Hat die Firmware den Fahrenheid-Bug (ganz alte Firmware)
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 10.11.2013
   *
   * @return Hat die (alte) Firmware den "Fahrenheid" Bug
   */
  public boolean hasFahrenheidBug()
  {
    return (hasFahrenheidBug);
  }

  /**
   * Hat die Firmware/die Lizenz sechs Parameter?
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 10.11.2013
   *
   * @return Hat die Firmware sechs Parameter bei der Custom / individual Einstellung
   */
  public boolean hasSixValuesIndividual()
  {
    return (hasSixValuesIndividual);
  }

  /**
   * Ist die Firmware vom Programm unterstützt
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 10.11.2013
   *
   * @return Wahr, wenn die Firmware unterstützt wird
   */
  public boolean isFirmwareSupported()
  {
    return (isFirmwareSupported);
  }

  /**
   * Wurde das config-Objekt bereits initialisiert?
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 10.11.2013
   *
   * @return Wahr, wenn das Objekt mit der Seriennummer initialisiert wurde
   */
  public boolean isInitialized()
  {
    if( !wasCorrectInitialized )
    {
      if( firmwareVersion != "0" && serialNumber != "0" && deviceName != "no name" && licenseState != 0 && connectedDeviceMac != "0" )
      {
        wasCorrectInitialized = true;
      }
    }
    return (wasCorrectInitialized);
  }

  /**
   * Neue Helligkeitssteuerung (20% Schritte)
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   *
   * @return Ist es die neuere Abstufung in 20%
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * <p/>
   * Stand: 10.11.2013
   */
  public boolean isNewerDisplayBrigthness()
  {
    return (isNewerDisplayBrightness);
  }

  /**
   * Ist es die "Alte" Parameter Sortierung oder eie Neue
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 10.11.2013
   *
   * @return Wahr, wenn die Parameterreihenfolge der Alten Firmware zutrifft
   */
  public boolean isOldParamSorting()
  {
    return (isOldParamSorting);
  }

  /**
   * Ist in der Firmware sechs Meter der erste Autosetpoint?
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * Stand: 30.11.2013
   *
   * @return Ist der Autosetpoint nun bei 6 Metern?
   */
  public boolean isSixMetersAutoSetpoint()
  {
    return (isSixMetersAutoSetpoint);
  }

  /**
   * Setze den Lizenzstatus/Custom mit dem Kommadostring vom Gerät
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 10.11.2013
   *
   * @param lic String[] 0=License, 1=Custom enabled
   * @return Lizenzstatus korrekt erkannt?
   */
  public boolean setLicenseStatus(String[] lic)
  {
    // Kommando SPX_LICENSE_STATE
    // <LS:CE>
    // LS : License State 0=Nitrox,1=Normoxic Trimix,2=Full Trimix
    // CE : Custom Enabled 0= disabled, 1=enabled
    // TODO: 3. Parameter MIL?
    int[] vals = new int[ 2 ];
    try
    {
      vals[ 0 ] = Integer.parseInt(lic[ 0 ]);
      vals[ 1 ] = Integer.parseInt(lic[ 1 ]);
    }
    catch( NumberFormatException ex )
    {
      return false;
    }
    licenseState = vals[ 0 ];
    customEnabled = vals[ 1 ] != 0;
    return (true);
  }

  /**
   * Ist das Ausschalten des Autosetpoints deaktiviert (neuere Firmware)
   *
   * @return ist es so?
   */
  public boolean isAutoSetpointWithoutOff()
  {
    return (isAutoSetpointWithoutOff);
  }


  /**
   * Setze den Lizenzstatus direkt
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 10.11.2013
   *
   * @param status
   */
  public void setLizenseStatus(int status)
  {
    licenseState = status;
  }

  /**
   * Setze den Wert isInit direkt
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 10.11.2013
   *
   * @param wasInit
   */
  public void setWasInit(boolean wasInit)
  {
    wasCorrectInitialized = wasInit;
  }

  /**
   * erfrage die MAC des verbundenen Gerätes
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * Stand: 01.12.2013
   *
   * @return MAc des verbundenen Gerätes
   */
  public String getConnectedDeviceMac()
  {
    return (connectedDeviceMac);
  }

  /**
   * Setze die MAC des verbundenen Devices
   * <p/>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * Stand: 01.12.2013
   *
   * @param mac
   */
  public void setConnectedDeviceMac(String mac)
  {
    connectedDeviceMac = mac;
  }
}
