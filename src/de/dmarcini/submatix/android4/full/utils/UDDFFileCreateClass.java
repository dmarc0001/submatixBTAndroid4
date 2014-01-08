/**
 * Helferklasse zum erzeugen von UDDF 2.2 Files
 */
package de.dmarcini.submatix.android4.full.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.util.Log;
import de.dmarcini.submatix.android4.full.ApplicationDEBUG;
import de.dmarcini.submatix.android4.full.exceptions.XMLFileCreatorException;

/**
 * 
 * Klasse zum Erzeugen von UDDF Version 2.0 Dateien
 * 
 * Project: SubmatixXMLTest Package: de.dmarcini.bluethooth.submatix.xml
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 08.01.2014
 */
public class UDDFFileCreateClass
{
  @SuppressWarnings( "javadoc" )
  public static final String TAG             = UDDFFileCreateClass.class.getSimpleName();
  private Document           uddfDoc         = null;
  private SPX42LogManager    logManager      = null;
  private Transformer        transformer     = null;
  private DocumentBuilder    builder         = null;
  private ArrayList<String>  gases           = null;
  private final Pattern      fieldPatternDp  = Pattern.compile( ":" );
  private int                diveTimeCurrent = 0;

  /**
   * 
   * Der Konstruktor der Helperklasse
   * 
   * Project: SubmatixXMLTest Package: de.dmarcini.bluethooth.submatix.xml
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 08.01.2014
   * @param lm
   *          geöffnete Datenbank
   * @throws ParserConfigurationException
   * @throws TransformerException
   * @throws TransformerFactoryConfigurationError
   * @throws XMLFileCreatorException
   */
  public UDDFFileCreateClass( final SPX42LogManager lm ) throws ParserConfigurationException, TransformerException, TransformerFactoryConfigurationError, XMLFileCreatorException
  {
    // initialisiere die Klasse
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreate()..." );
    logManager = lm;
    try
    {
      // So den XML-Erzeuger Creieren
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "LogXMLCreator: create Transformer..." );
      transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "no" );
      transformer.setOutputProperty( OutputKeys.STANDALONE, "yes" );
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "LogXMLCreator: create factory..." );
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware( false );
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "LogXMLCreator: create builder..." );
      builder = factory.newDocumentBuilder();
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "LogXMLCreator: ...OK" );
    }
    catch( TransformerConfigurationException ex )
    {
      Log.e( TAG, "LogXMLCreator: transformer <" + ex.getLocalizedMessage() + ">" );
      throw new XMLFileCreatorException( ex.getMessage() );
    }
    catch( TransformerFactoryConfigurationError ex )
    {
      Log.e( TAG, "LogXMLCreator: transformer <" + ex.getLocalizedMessage() + ">" );
      throw new XMLFileCreatorException( ex.getMessage() );
    }
    catch( ParserConfigurationException ex )
    {
      Log.e( TAG, "LogXMLCreator: builder <" + ex.getLocalizedMessage() + ">" );
      throw new XMLFileCreatorException( ex.getMessage() );
    }
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreate()...OK" );
  }

  /**
   * 
   * Erzeuge die XML-Datei für einen Logeintrag
   * 
   * Project: SubmatixXMLTest Package: de.dmarcini.bluethooth.submatix.xml
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 08.01.2014
   * @param file
   *          Datei, in die das Ergebnis nachher kommt
   * @param rlo
   * @param zipped
   *          Komprimieren?
   * @return true oder false
   */
  public File createXML( File file, ReadLogItemObj rlo, boolean zipped )
  {
    Element rootNode = null;
    String msg = null;
    File retFile = file;
    Log.v( TAG, "createXML()..." );
    // Erzeuge Dokument neu
    uddfDoc = builder.newDocument();
    // Root-Element erzeugen
    rootNode = uddfDoc.createElement( "uddf" );
    rootNode.setAttribute( "version", ProjectConst.UDDFVERSION );
    uddfDoc.appendChild( rootNode );
    // Kommentiere mal TODO: Programmname einfügen
    rootNode.appendChild( uddfDoc.createComment( "Genereted von: SubmatixBluethoothLogger" ) );
    // Appliziere Generator
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "generator node..." );
    rootNode.appendChild( makeGeneratorNode( uddfDoc ) );
    // appliziere Gasdefinitionen
    // if( ApplicationDEBUG.DEBUG ) Log.d( TAG,"gasdefinitions node...");
    // TODO: rootNode.appendChild( makeGasdefinitions( uddfDoc, rlo ) );
    // appliziere profiledata
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "profiles node..." );
    rootNode.appendChild( makeProfilesData( uddfDoc, rlo ) );
    uddfDoc.normalizeDocument();
    try
    {
      retFile = domToFile( file, uddfDoc, zipped );
    }
    catch( TransformerException ex )
    {
      msg = "transformer Exception " + ex.getLocalizedMessage();
      Log.e( TAG, "createXML: <" + msg + ">" );
      return( null );
    }
    catch( IOException ex )
    {
      msg = "IOException " + ex.getLocalizedMessage();
      Log.e( TAG, "createXML: <" + msg + ">" );
      return( null );
    }
    catch( Exception ex )
    {
      msg = "allgemeine Exception " + ex.getLocalizedMessage();
      Log.e( TAG, "createXML: <" + msg + ">" );
      return( null );
    }
    return( retFile );
  }

  /**
   * 
   * Teilbaum profilesData erzeugen
   * 
   * Project: SubmatixXMLTest Package: de.dmarcini.bluethooth.submatix.xml
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 25.10.2011
   * @param doc
   *          Dokument Objekt
   * @return Teilbam -Rootelement
   */
  private Node makeProfilesData( Document doc, ReadLogItemObj rlo )
  {
    Element profileNode;
    int repNumber = 1;
    profileNode = doc.createElement( "profiledata" );
    profileNode.appendChild( makeRepetitiongroup( doc, repNumber, rlo ) );
    return( profileNode );
  }

  /**
   * 
   * Teilbaum Wiederholungsgruppe einbauen
   * 
   * Project: SubmatixXMLTest Package: de.dmarcini.bluethooth.submatix.xml
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 25.10.2011
   * @param doc
   *          Dokument Objekt
   * @param repNumber
   *          Nummer des Repetivtauchgangee (bei mir immer 1 :-( )
   * @param number
   *          Nummer des Logs in der Datenbank
   * @return Teilbaum Repetitiongroup
   */
  private Node makeRepetitiongroup( Document doc, int repNumber, ReadLogItemObj rlo )
  {
    Element repNode;
    repNode = doc.createElement( "repetitiongroup" );
    repNode.setAttribute( "id", String.valueOf( repNumber ) );
    repNode.appendChild( makeDiveNode( doc, rlo ) );
    return( repNode );
  }

  /**
   * 
   * Tauchgang Teibaum bauen
   * 
   * Project: SubmatixXMLTest Package: de.dmarcini.bluethooth.submatix.xml
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 25.10.2011
   * @param doc
   *          Document Objekt
   * @param diveNumber
   *          Nummer des Tauchganges in der Datenbank
   * @return Teilbaum Tauchgang
   * 
   *         TODO Süßwasser/Salzwasser Dichte eintragen (Datenbankfeld einrichten)
   */
  private Node makeDiveNode( Document doc, ReadLogItemObj rlo )
  {
    Element diveNode, dateNode, yNode, mNode, dNode;
    Element timeNode, hNode, minNode;
    Element dnNode, atNode, ltNode, gdNode, deNode, noNode, txNode;
    String year, month, day, hour, minute;
    String temperature, lowesttemp;
    String greatestdepth;
    String density;
    //
    DateTime startDateTime = new DateTime( rlo.startTimeMilis );
    day = String.format( Locale.ENGLISH, "%02d", startDateTime.getDayOfMonth() );
    month = String.format( Locale.ENGLISH, "%02d", startDateTime.getMonthOfYear() );
    year = String.format( Locale.ENGLISH, "%04d", startDateTime.getYear() );
    hour = String.format( Locale.ENGLISH, "02d", startDateTime.getHourOfDay() );
    minute = String.format( Locale.ENGLISH, "%02d", startDateTime.getMinuteOfHour() );
    temperature = String.format( Locale.ENGLISH, "%.1f", rlo.firstTemp + ProjectConst.KELVIN );
    lowesttemp = String.format( Locale.ENGLISH, "%.1f", rlo.lowTemp + ProjectConst.KELVIN );
    greatestdepth = String.format( Locale.ENGLISH, "%.1f", rlo.maxDepth / 10.0f );
    density = "1034.0";
    diveNode = doc.createElement( "dive" );
    diveNode.setAttribute( "id", String.valueOf( rlo ) );
    // # date
    dateNode = doc.createElement( "date" );
    // ## date -> year
    yNode = doc.createElement( "year" );
    yNode.appendChild( doc.createTextNode( year ) );
    dateNode.appendChild( yNode );
    // ## date -> month
    mNode = doc.createElement( "month" );
    mNode.appendChild( doc.createTextNode( month ) );
    dateNode.appendChild( mNode );
    // ## date -> day
    dNode = doc.createElement( "day" );
    dNode.appendChild( doc.createTextNode( day ) );
    dateNode.appendChild( dNode );
    diveNode.appendChild( dateNode );
    // # time
    timeNode = doc.createElement( "time" );
    // ## time -> hour
    hNode = doc.createElement( "hour" );
    hNode.appendChild( doc.createTextNode( hour ) );
    timeNode.appendChild( hNode );
    // ## time -> minute
    minNode = doc.createElement( "minute" );
    minNode.appendChild( doc.createTextNode( minute ) );
    timeNode.appendChild( minNode );
    diveNode.appendChild( timeNode );
    // # divenumber
    dnNode = doc.createElement( "divenumber" );
    dnNode.appendChild( doc.createTextNode( String.valueOf( rlo ) ) );
    diveNode.appendChild( dnNode );
    // # airtemp
    atNode = doc.createElement( "airtemperature" );
    atNode.appendChild( doc.createTextNode( temperature ) );
    diveNode.appendChild( atNode );
    // # lowesttemp
    ltNode = doc.createElement( "lowesttemperature" );
    ltNode.appendChild( doc.createTextNode( lowesttemp ) );
    diveNode.appendChild( ltNode );
    // # greatestdepth
    gdNode = doc.createElement( "greatestdepth" );
    gdNode.appendChild( doc.createTextNode( greatestdepth ) );
    diveNode.appendChild( gdNode );
    // # density
    deNode = doc.createElement( "density" );
    deNode.appendChild( doc.createTextNode( density ) );
    diveNode.appendChild( deNode );
    // # notes
    noNode = doc.createElement( "notes" );
    txNode = doc.createElement( "text" );
    txNode.appendChild( doc.createTextNode( rlo.notes ) );
    noNode.appendChild( txNode );
    diveNode.appendChild( noNode );
    // Teilbaum einhängen
    diveNode.appendChild( makeSamplesForDive( doc, rlo ) );
    return( diveNode );
  }

  /**
   * Erzeuge das eigentliche Profil für den Tauchgang
   * 
   * Project: SubmatixXMLTest Package: de.dmarcini.bluethooth.submatix.xml
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 25.10.2011
   * @param doc
   *          Document Objekt
   * @param diveNumber
   *          Nummer des Tauchgangs in der Datenbank
   * @param dAttr
   *          Objekt für Tauchgangseigenschaften
   * @return Teilbaum für das Tauchprofil
   */
  private Node makeSamplesForDive( final Document doc, ReadLogItemObj rlo )
  {
    SAXParserFactory spf = null;
    SAXParser sp = null;
    ContentHandler myHandler = null;
    XMLReader xr = null;
    File xmlFile = null;
    final Element sampleNode;
    int diveSamples = 0;
    sampleNode = doc.createElement( "samples" );
    // der erste waypoint hat immer Zeit 0, tiefe 0 und switchmix
    diveSamples = rlo.countSamples;
    if( diveSamples == 0 ) return( sampleNode );
    xmlFile = new File( rlo.fileOnMobile );
    // Liste des Tauchganges machen
    Log.v( TAG, "getDiveList()...scan XML..." );
    // versuchen wir es...
    // SAX Parser erzeugen
    spf = SAXParserFactory.newInstance();
    try
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "getDiveList()...NEW SAX Parser..." );
      sp = spf.newSAXParser();
      xr = sp.getXMLReader();
    }
    catch( ParserConfigurationException ex )
    {
      Log.e( TAG, "getDiveList() => " + ex.getLocalizedMessage() );
      return( null );
    }
    catch( SAXException ex )
    {
      Log.e( TAG, "getDiveList() => " + ex.getLocalizedMessage() );
      return( null );
    }
    // einen nagenleuen Handler erzeugen
    // als implizite Klasse
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "getDiveList()...create ContentHandler..." );
    //
    // Lokaler Handler
    //
    myHandler = new ContentHandler() {
      private boolean             readBetween;
      private boolean             scanActive    = false;
      private String              stringBetween = null;
      private final static String rootTag       = "spx42Log";
      private ExportLogEntry      entry         = null;
      private String              gasSample     = "";
      private double              setpoint      = 0;

      @Override
      public void startDocument() throws SAXException
      {
        /* Wird aufgerufen am Anfang des Dokumentes */
        readBetween = false;
        scanActive = false;
        stringBetween = "";
      }

      @Override
      public void endDocument() throws SAXException
      {
        /* Wird aufgerufen am Ende des Dokumentes */
      }

      @Override
      public void startElement( String namespaceURI, String localName, String qName, Attributes atts ) throws SAXException
      {
        // Wird aufgerufen bei einem Start-Tag
        if( localName.equalsIgnoreCase( rootTag ) )
        {
          if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "PARSER: rootElement <" + localName + "> Opened!" );
          scanActive = true;
          return;
        }
        if( scanActive == false )
        {
          // nix zu tun hier!
          return;
        }
        if( localName.equals( "logEntry" ) )
        {
          // Logeintrag ANFANG
          entry = new ExportLogEntry();
          return;
        }
        if( entry == null )
        {
          Log.e( TAG, "PARSER startElement(): object for entry is null!" );
          return;
        }
        if( localName.equals( "presure" ) )
        {
          readBetween = true;
          return;
        }
        if( localName.equals( "step" ) )
        {
          readBetween = true;
          return;
        }
        if( localName.equals( "depth" ) )
        {
          readBetween = true;
          return;
        }
        if( localName.equals( "temp" ) )
        {
          readBetween = true;
          return;
        }
        if( localName.equals( "acku" ) )
        {
          readBetween = true;
          return;
        }
        if( localName.equals( "ppo2" ) )
        {
          readBetween = true;
          return;
        }
        if( localName.equals( "n2" ) )
        {
          readBetween = true;
          return;
        }
        if( localName.equals( "he" ) )
        {
          readBetween = true;
          return;
        }
        if( localName.equals( "setpoint" ) )
        {
          readBetween = true;
          return;
        }
        if( localName.equals( "zerotime" ) )
        {
          readBetween = true;
          return;
        }
      }

      @Override
      public void endElement( String uri, String localName, String qName ) throws SAXException
      {
        // root Tag geschlossen? Dann ist Finito
        if( localName.equalsIgnoreCase( rootTag ) )
        {
          if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "PARSER: rootElement <" + localName + "> Closed!" );
          scanActive = false;
          return;
        }
        if( entry == null )
        {
          Log.e( TAG, "PARSER endElement(): object for entry is null!" );
          return;
        }
        if( localName.equals( "logEntry" ) )
        {
          // Logeintrag ENDE
          if( entry.whereAlDataThere() )
          {
            // Jetzt mach ich einen Waypoint Konten aus dem Teil
            // gab es einen Gaswechsel?
            if( !entry.gasSample.equals( gasSample ) )
            {
              entry.gasswitch = true;
              gasSample = entry.gasSample;
            }
            if( entry.setpoint != setpoint )
            {
              entry.ppo2switch = true;
              setpoint = entry.setpoint;
            }
            // und papp den dran
            sampleNode.appendChild( makeWaypoint( doc, entry ) );
          }
          else
          {
            Log.e( TAG, "PARSER endElement(): entry is FAILED. END" );
          }
          return;
        }
        try
        {
          if( localName.equals( "presure" ) )
          {
            readBetween = false;
            entry.presure = Integer.parseInt( stringBetween );
            stringBetween = "";
            return;
          }
          if( localName.equals( "step" ) )
          {
            readBetween = false;
            diveTimeCurrent += Integer.parseInt( stringBetween );
            entry.time = diveTimeCurrent;
            stringBetween = "";
            return;
          }
          if( localName.equals( "depth" ) )
          {
            readBetween = false;
            entry.depth = ( Float.parseFloat( stringBetween ) / 10 );
            stringBetween = "";
            return;
          }
          if( localName.equals( "temp" ) )
          {
            readBetween = false;
            entry.temp = ( Float.parseFloat( stringBetween ) + ProjectConst.KELVIN );
            stringBetween = "";
            return;
          }
          if( localName.equals( "acku" ) )
          {
            readBetween = false;
            entry.acku = Float.parseFloat( stringBetween );
            stringBetween = "";
            return;
          }
          if( localName.equals( "ppo2" ) )
          {
            readBetween = false;
            entry.ppo2 = Float.parseFloat( stringBetween );
            stringBetween = "";
            return;
          }
          if( localName.equals( "n2" ) )
          {
            readBetween = false;
            entry.n2 = ( Float.parseFloat( stringBetween ) / 100.0 );
            stringBetween = "";
            return;
          }
          if( localName.equals( "he" ) )
          {
            readBetween = false;
            entry.he = ( Float.parseFloat( stringBetween ) / 100.0 );
            stringBetween = "";
            return;
          }
          if( localName.equals( "setpoint" ) )
          {
            readBetween = false;
            entry.setpoint = ( Float.parseFloat( stringBetween ) / 10.0 );
            stringBetween = "";
            return;
          }
          if( localName.equals( "zerotime" ) )
          {
            readBetween = false;
            entry.zerotime = Integer.parseInt( stringBetween );
            stringBetween = "";
            return;
          }
        }
        catch( NumberFormatException ex )
        {
          Log.e( TAG, "PARSER: <" + ex.getLocalizedMessage() + ">" );
          return;
        }
      }

      @Override
      public void characters( char ch[], int start, int length ) throws SAXException
      {
        // Wird aufgerufen für den Content zwischen Start und End-Tag
        String text = new String( ch, start, length );
        if( readBetween )
        {
          stringBetween += text;
        }
      }

      @Override
      public void endPrefixMapping( String prefix ) throws SAXException
      {}

      @Override
      public void ignorableWhitespace( char[] ch, int start, int length ) throws SAXException
      {}

      @Override
      public void processingInstruction( String target, String data ) throws SAXException
      {}

      @Override
      public void setDocumentLocator( Locator locator )
      {}

      @Override
      public void skippedEntity( String name ) throws SAXException
      {}

      @Override
      public void startPrefixMapping( String prefix, String uri ) throws SAXException
      {}
    };
    // Jetzt parse mal lustig
    try
    {
      xr.setContentHandler( myHandler );
      xr.parse( new InputSource( "file:" + xmlFile.getAbsolutePath() ) );
    }
    catch( SAXException ex )
    {
      Log.e( TAG, "getDiveList() => : " + ex.getLocalizedMessage() );
      return( null );
    }
    catch( IOException ex )
    {
      Log.e( TAG, "getDiveList() => : " + ex.getLocalizedMessage() );
      return( null );
    }
    return( sampleNode );
  }

  /**
   * 
   * Node für einen Wegpunkt machen
   * 
   * Project: SubmatixBTLogger Package: de.dmarcini.bluethooth.support
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 13.12.2011
   * @param doc
   * @param entry
   * @return Kompletter waypoint Konten
   */
  private Node makeWaypoint( Document doc, ExportLogEntry entry )
  {
    Element wpNode, dNode, dtNode, tNode, sNode, po2Node;
    // # waypoint
    wpNode = doc.createElement( "waypoint" );
    // ## waypoint -> depth
    dNode = doc.createElement( "depth" );
    dNode.appendChild( doc.createTextNode( String.format( Locale.ENGLISH, "%.2f", entry.depth ) ) );
    wpNode.appendChild( dNode );
    // ## waypoint -> divetime
    dtNode = doc.createElement( "divetime" );
    dtNode.appendChild( doc.createTextNode( String.format( Locale.ENGLISH, "%d.0", entry.time ) ) );
    wpNode.appendChild( dtNode );
    // ## waypoint -> temperature
    tNode = doc.createElement( "temperature" );
    tNode.appendChild( doc.createTextNode( String.format( Locale.ENGLISH, "%.1f", entry.temp ) ) );
    wpNode.appendChild( tNode );
    // wenn sich das Gas geändert hat oder am anfang IMMER
    if( entry.gasswitch == true )
    {
      // ## waypoint -> switch
      sNode = doc.createElement( "switchmix" );
      sNode.setAttribute( "ref", entry.gasSample.replaceAll( "(\\:|\\.|\\,)0*", "" ) );
      wpNode.appendChild( sNode );
    }
    // wenn sich der Setpoint ge�ndert hat...
    if( entry.ppo2switch )
    {
      // ## waypoint -> setpo2
      po2Node = doc.createElement( "setpo2" );
      po2Node.appendChild( doc.createTextNode( String.format( Locale.ENGLISH, "%.2f", entry.setpoint ) ) );
      wpNode.appendChild( po2Node );
    }
    return( wpNode );
  }

  /**
   * 
   * Erzeuge den Teilbaum Gasdefinitionen
   * 
   * Project: SubmatixXMLTest Package: de.dmarcini.bluethooth.submatix.xml
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 24.10.2011
   * @param doc
   *          Document Objekt
   * @param diveAttrs
   * @return Teilbaum f�r Gasdefinitionen in diesem Tauchgang
   */
  private Node makeGasdefinitions( Document doc, ReadLogItemObj dive )
  {
    Element gasNode, mixNode, nameNode, o2Node, n2Node, heNode, arNode, h2Node;
    String gasName;
    String[] fields;
    // gases füllen mit stringliste a'la O2:N2:HE:AR:H2 als Strings "%.3f"
    makeGasListForDive( dive );
    // # gasdefinitions
    gasNode = doc.createElement( "gasdefinitions" );
    if( gases == null )
    {
      // Notbremse, falls es keine Gaase gibt
      return( gasNode );
    }
    for( String gas : gases )
    {
      fields = fieldPatternDp.split( gas );
      // ## gasdefinitions -> mix
      mixNode = doc.createElement( "mix" );
      gasName = gas.replaceAll( "(\\:|\\.|\\,)0*", "" );
      mixNode.setAttribute( "id", gasName );
      gasNode.appendChild( mixNode );
      // ### gasdefinitions -> mix -> name
      nameNode = doc.createElement( "name" );
      nameNode.appendChild( doc.createTextNode( gasName ) );
      mixNode.appendChild( nameNode );
      // ### gasdefinitions -> mix -> O2
      o2Node = doc.createElement( "o2" );
      o2Node.appendChild( doc.createTextNode( fields[0] ) );
      mixNode.appendChild( o2Node );
      // ### gasdefinitions -> mix -> n2
      n2Node = doc.createElement( "n2" );
      n2Node.appendChild( doc.createTextNode( fields[1] ) );
      mixNode.appendChild( n2Node );
      // ### gasdefinitions -> mix -> he
      heNode = doc.createElement( "he" );
      heNode.appendChild( doc.createTextNode( fields[2] ) );
      mixNode.appendChild( heNode );
      // ### gasdefinitions -> mix -> he
      arNode = doc.createElement( "ar" );
      arNode.appendChild( doc.createTextNode( fields[3] ) );
      mixNode.appendChild( arNode );
      // ### gasdefinitions -> mix -> ar
      h2Node = doc.createElement( "h2" );
      h2Node.appendChild( doc.createTextNode( fields[4] ) );
      mixNode.appendChild( h2Node );
    }
    return gasNode;
  }

  /**
   * 
   * Erzeuge Teilbaum "generator" (Erzeuger der Datei)
   * 
   * Project: SubmatixXMLTest Package: de.dmarcini.bluethooth.submatix.xml
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 24.10.2011
   * @param doc
   *          Referenz zum Dokument
   * @return Der erzeugte Teilbaum.
   */
  private Node makeGeneratorNode( Document doc )
  {
    Element genNode, nameNode, mNameNode, manuNode, contactNode, mailNode, hpNode, versionNode, dateNode, yNode, mNode, dNode;
    // Wurzel dieser Ebene
    genNode = doc.createElement( "generator" );
    // Creators Name einf�gen
    nameNode = doc.createElement( "name" );
    nameNode.appendChild( doc.createTextNode( ProjectConst.CREATORNAME ) );
    genNode.appendChild( nameNode );
    // # Hersteller
    manuNode = doc.createElement( "manufacturer" );
    // ## Hersteller -> Name
    mNameNode = doc.createElement( "name" );
    mNameNode.appendChild( doc.createTextNode( ProjectConst.MANUFACTNAME ) );
    manuNode.appendChild( mNameNode );
    // ## Hersteller -> contact
    contactNode = doc.createElement( "contact" );
    // ### hersteller -> contact -> mail
    mailNode = doc.createElement( "email" );
    mailNode.appendChild( doc.createTextNode( ProjectConst.MANUFACTMAIL ) );
    contactNode.appendChild( mailNode );
    // ### hersteller -> contact -> homepagel
    hpNode = doc.createElement( "homepage" );
    hpNode.appendChild( doc.createTextNode( ProjectConst.MANUFACTHOME ) );
    contactNode.appendChild( hpNode );
    manuNode.appendChild( contactNode );
    genNode.appendChild( manuNode );
    // ## version
    versionNode = doc.createElement( "version" );
    versionNode.appendChild( doc.createTextNode( ProjectConst.MANUFACTVERS ) );
    genNode.appendChild( versionNode );
    // ## date
    dateNode = doc.createElement( "date" );
    // ### date -> year
    yNode = doc.createElement( "year" );
    yNode.appendChild( doc.createTextNode( ProjectConst.GENYEAR ) );
    dateNode.appendChild( yNode );
    // ### date -> month
    mNode = doc.createElement( "month" );
    mNode.appendChild( doc.createTextNode( ProjectConst.GENMONTH ) );
    dateNode.appendChild( mNode );
    // ### date -> day
    dNode = doc.createElement( "day" );
    dNode.appendChild( doc.createTextNode( ProjectConst.GENDAY ) );
    dateNode.appendChild( dNode );
    genNode.appendChild( dateNode );
    return( genNode );
  }

  /**
   * 
   * Erzeuge die XML-Datei aus dem DOM-Baum im speicher
   * 
   * Project: SubmatixXMLTest Package: de.dmarcini.bluethooth.submatix.xml
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 27.10.2011
   * @param file
   *          File Objekt f�r die Zieldatei
   * @param document
   *          Document Objekt
   * @return Ok oder nicht OK
   * @throws IOException
   * @throws TransformerException
   */
  private File domToFile( File file, Document document, boolean zipped ) throws IOException, TransformerException
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "domToFile()... " );
    // die Vorbereitungen treffen
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "create writer..." );
    StringWriter writer = new StringWriter();
    DOMSource doc = new DOMSource( document );
    StreamResult res = new StreamResult( writer );
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "transform... " );
    transformer.transform( doc, res );
    // nun zur Frage: gezippt oder nicht
    if( zipped )
    {
      // gezipptes File erzeugen
      Log.i( TAG, "write to zipped file... " );
      file = new File( file.getAbsoluteFile() + ".gz" );
      if( file.exists() )
      {
        // Datei ist da, ich will sie ueberschreiben
        file.delete();
      }
      OutputStream fos = new FileOutputStream( file );
      OutputStream zipOut = new GZIPOutputStream( fos );
      zipOut.write( writer.toString().getBytes() );
      zipOut.close();
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "domToString()...ok " );
      return( file );
    }
    else
    {
      // ungezipptes file erzeugen
      Log.i( TAG, "write to unzipped file... " );
      if( file.exists() )
      {
        // Datei ist da, ich will sie ueberschreiben
        file.delete();
      }
      RandomAccessFile xmlFile = new RandomAccessFile( file, "rw" );
      xmlFile.writeBytes( writer.toString() );
      xmlFile.close();
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "domToString()...ok " );
      return( file );
    }
  }

  /**
   * 
   * Erzeuge die Gasliste für diesen Tauchgang
   * 
   * Project: SubmatixXMLTest Package: de.dmarcini.bluethooth.submatix.xml
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 27.10.2011
   * @param dive
   *          Nummer des Logs in der Datenbank
   * 
   *          TODO: Gasliste erzeugen
   */
  private void makeGasListForDive( ReadLogItemObj dive )
  {
    int n2, he, o2;
    String entry;
    Log.v( TAG, "makeGasListForDive()..." );
    // keine Datenbank, nix zu tun
    if( logManager == null ) return;
    if( gases == null ) gases = new ArrayList<String>( 1 );
    gases.clear();
    Log.v( TAG, "makeGasListForDive()...execute SQL..." );
    // versuchen wir es...
  }
}
