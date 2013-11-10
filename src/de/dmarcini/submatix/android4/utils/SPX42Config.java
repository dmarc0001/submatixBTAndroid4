/**
 * Implementation der Konfiguration für SPX42 Android
 * 
 * 
 * 
 * Stand: 10.10.13
 */
package de.dmarcini.submatix.android4.utils;

/**
 * 
 * Objekt zur Sicherung der SPX42 Konfiguration
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)#
 * 
 *         Stand: 10.11.2013
 */
//@formatter:off
public class SPX42Config
{
  public static final int                       UNITS_METRIC = 1;
  public static final int                     UNITS_IMPERIAL = 2;
  //
  protected boolean                    wasCorrectInitialized = false;
  protected String                                deviceName = "no name";
  protected String                           firmwareVersion = "0";
  protected String                              serialNumber = "0";
  protected int                                 licenseState = 0;
  protected boolean                            customEnabled = false;
  protected boolean                         hasFahrenheidBug = false;
  protected boolean                               canSetDate = true;
  protected boolean                   hasSixValuesIndividual = false;
  protected boolean                      isFirmwareSupported = false;
  protected boolean                        isOldParamSorting = false;  // bei alter Firmware war die Reihenfolge der Paramete anders
  protected boolean                 isNewerDisplayBrightness = false; // ab FW FIRMWARE_2_7H_R83CE 20 % Schritte
  
  //
  //@formatter:on
  /**
   * Der Konstruktor, füllt falls notwendig sinnvolle Anfangswerte Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclg.utils
   * 
   */
  public SPX42Config()
  {}

  /**
   * Kopierkonstruktor Project: SubmatixBTConfigPC Package: de.dmarcini.submatix.pclg.utils
   * 
   * @param cf
   */
  /**
   * 
   * Kopierkonstruktor
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @param cf
   *          Das zu kopierende Config-Element
   */
  public SPX42Config( SPX42Config cf )
  {
    serialNumber = cf.serialNumber;
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
  }

  /**
   * 
   * Kann die Firmware Dartum setzen?
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @return Kann die Firmware ein Datum setzen
   */
  public boolean canSetDate()
  {
    return( canSetDate );
  }

  /**
   * 
   * Config-Objekt ungültig setzen
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   */
  public void clear()
  {
    wasCorrectInitialized = false;
  }

  /**
   * 
   * Vergleiche mit anderer Konfiguration
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @param cf
   * @return Wahr, wenn die Configs gleich sind
   */
  public boolean compareWith( SPX42Config cf )
  {
    // immer wenn was nicht übereinstimmt ist Übertragung norwendig
    if( !wasCorrectInitialized ) return( false );
    if( !serialNumber.equals( cf.serialNumber ) ) return( false );
    if( !deviceName.equals( cf.deviceName ) ) return( false );
    if( !firmwareVersion.equals( cf.firmwareVersion ) ) return( false );
    if( licenseState != cf.licenseState ) return( false );
    if( customEnabled != cf.customEnabled ) return( false );
    if( hasFahrenheidBug != cf.hasFahrenheidBug ) return( false );
    if( canSetDate != cf.canSetDate ) return( false );
    if( hasSixValuesIndividual != cf.hasSixValuesIndividual ) return( false );
    if( isFirmwareSupported != cf.isFirmwareSupported ) return( false );
    if( isOldParamSorting != cf.isOldParamSorting ) return( false );
    if( isNewerDisplayBrightness != cf.isNewerDisplayBrightness ) return( false );
    return( true );
  }

  /**
   * 
   * Ist CUSTOM lizensiert?
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @return "1" wenn custom (individual) liuzensiert ist
   */
  public int getCustomEnabled()
  {
    if( customEnabled )
    {
      return( 1 );
    }
    return( 0 );
  }

  /**
   * 
   * Gerätename zurückgeben
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @return Den Gerätenamen zurückgeben
   */
  public String getDeviceName()
  {
    return( deviceName );
  }

  /**
   * 
   * Firmwareversion zurückgeben
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @return Gib die Firmwareversion zurück
   */
  public String getFirmwareVersion()
  {
    return( firmwareVersion );
  }

  /**
   * 
   * Gib Lizenzstatus zurück
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @return Gib den Lizenzstatus zurück
   */
  public int getLicenseState()
  {
    return( licenseState );
  }

  /**
   * 
   * Gib Seriennummer zurück
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @return Gib die Geräte-Is (Seriennummer) zurück
   */
  public String getSerial()
  {
    return( serialNumber );
  }

  /**
   * 
   * Hat die Firmware den Fahrenheid-Bug (ganz alte Firmware)
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @return Hat die (alte) Firmware den "Fahrenheid" Bug
   */
  public boolean hasFahrenheidBug()
  {
    return( hasFahrenheidBug );
  }

  /**
   * 
   * Hat die Firmware/die Lizenz sechs Parameter?
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @return Hat die Firmware sechs Parameter bei der Custom / individual Einstellung
   */
  public boolean hasSixValuesIndividual()
  {
    return( hasSixValuesIndividual );
  }

  /**
   * 
   * Ist die Firmware vom Programm unterstützt
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @return Wahr, wenn die Firmware unterstützt wird
   */
  public boolean isFirmwareSupported()
  {
    return( isFirmwareSupported );
  }

  /**
   * 
   * Wurde das config-Objekt bereits initialisiert?
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @return Wahr, wenn das Objekt mit der Seriennummer initialisiert wurde
   */
  public boolean isInitialized()
  {
    return( wasCorrectInitialized );
  }

  /**
   * 
   * Ist es die "Alte" Parameter Sortierung oder eie Neue
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @return Wahr, wenn die Parameterreihenfolge der Alten Firmware zutrifft
   */
  public boolean isOldParamSorting()
  {
    return( isOldParamSorting );
  }

  /**
   * 
   * Setze, ob CUSTOM lizensiert wurde
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @param en
   */
  public void setCustomEnabled( boolean en )
  {
    customEnabled = en;
  }

  /**
   * 
   * Setze den Gerätenamen
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @param name
   */
  public void setDeviceName( String name )
  {
    deviceName = name;
  }

  /**
   * 
   * Setze die Firmwareversion und setze Flags
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @param version
   */
  public void setFirmwareVersion( String version )
  {
    firmwareVersion = version;
    // ich gehe mal vom sicheren Fall aus
    hasFahrenheidBug = true;
    canSetDate = false;
    hasSixValuesIndividual = false;
    isFirmwareSupported = false;
    isOldParamSorting = false;
    //
    // versuch mal die Eigenschaften rauszufinden
    //
    // Beginne bei einer gaaaanz alten Version
    if( firmwareVersion.matches( "V2\\.6.*" ) )
    {
      hasFahrenheidBug = true;
      isFirmwareSupported = true;
      isOldParamSorting = true;
      wasCorrectInitialized = true;
    }
    // Versionen NACH 2.7_V
    if( firmwareVersion.matches( "V2\\.7_V.*" ) )
    {
      hasFahrenheidBug = false;
      canSetDate = false;
      hasSixValuesIndividual = false;
      isFirmwareSupported = true;
      wasCorrectInitialized = true;
    }
    // Versionen NACH 2.7_H
    if( firmwareVersion.matches( "V2\\.7_H.*" ) )
    {
      hasFahrenheidBug = false;
      canSetDate = false;
      isFirmwareSupported = true;
      wasCorrectInitialized = true;
      if( firmwareVersion.matches( "V2\\.7_H r83.*" ) )
      {
        //
        // hier kommt bestimmt noch irgendwas nach :-(
        hasSixValuesIndividual = true;
        isNewerDisplayBrightness = true;
      }
    }
  }

  /**
   * 
   * Setze den Lizenzstatus/Custom mit dem Kommadostring vom Gerät
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @param lic
   *          String[] 0=License, 1=Custom enabled
   * @return Lizenzstatus korrekt erkannt?
   */
  public boolean setLicenseStatus( String[] lic )
  {
    // Kommando SPX_LICENSE_STATE
    // <LS:CE>
    // LS : License State 0=Nitrox,1=Normoxic Trimix,2=Full Trimix
    // CE : Custom Enabled 0= disabled, 1=enabled
    // TODO: 3. Parameter MIL?
    int[] vals = new int[2];
    try
    {
      vals[0] = Integer.parseInt( lic[0] );
      vals[1] = Integer.parseInt( lic[1] );
    }
    catch( NumberFormatException ex )
    {
      return false;
    }
    licenseState = vals[0];
    if( vals[1] == 0 )
    {
      customEnabled = false;
    }
    else
    {
      customEnabled = true;
    }
    return( true );
  }

  /**
   * 
   * Setze den Lizenzstatus direkt
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @param status
   */
  public void setLizenseStatus( int status )
  {
    licenseState = status;
  }

  /**
   * 
   * Setze die Seriennummer
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @param serial
   */
  public void setSerial( String serial )
  {
    if( serial != null )
    {
      serialNumber = serial;
    }
  }

  /**
   * 
   * Setze den Wert isInit direkt
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @param wasInit
   */
  public void setWasInit( boolean wasInit )
  {
    wasCorrectInitialized = wasInit;
  }
}
