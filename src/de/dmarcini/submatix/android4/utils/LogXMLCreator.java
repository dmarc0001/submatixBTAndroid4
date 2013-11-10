package de.dmarcini.submatix.android4.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.util.Log;
import de.dmarcini.submatix.android4.BuildConfig;
import de.dmarcini.submatix.android4.exceptions.XMLFileCreatorException;

/**
 * 
 * Klasse soll die Logs als XML-File erzeugen
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class LogXMLCreator
{
  private static final String TAG         = LogXMLCreator.class.getSimpleName();
  private SPX42DiveHeadData   diveHeader  = null;
  private Transformer         transformer = null;
  private DocumentBuilder     builder     = null;
  private Document            logXmlFile  = null;
  private final Element       rootNode;

  public LogXMLCreator( SPX42DiveHeadData _diveHeader ) throws XMLFileCreatorException
  {
    diveHeader = _diveHeader;
    diveHeader.diveLength = 0;
    diveHeader.airTemp = -1.0D;
    diveHeader.countSamples = 0;
    try
    {
      // So den XML-Erzeuger Creieren
      if( BuildConfig.DEBUG ) Log.d( TAG, "LogXMLCreator: create Transformer..." );
      transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "no" );
      transformer.setOutputProperty( OutputKeys.STANDALONE, "yes" );
      if( BuildConfig.DEBUG ) Log.d( TAG, "LogXMLCreator: create factory..." );
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware( false );
      if( BuildConfig.DEBUG ) Log.d( TAG, "LogXMLCreator: create builder..." );
      builder = factory.newDocumentBuilder();
      if( BuildConfig.DEBUG ) Log.d( TAG, "LogXMLCreator: ...OK" );
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
    // Erzeuge Dokument neu
    logXmlFile = builder.newDocument();
    // Root-Element erzeugen
    rootNode = logXmlFile.createElement( "spx42log" );
    rootNode.setAttribute( "version", ProjectConst.XML_FILEVERSION );
    logXmlFile.appendChild( rootNode );
    // Kommentiere mal
    rootNode.appendChild( logXmlFile.createComment( String.format( "created from: %s, vers: %s %s-%s-%s", ProjectConst.CREATORPROGRAM, ProjectConst.MANUFACTVERS,
            ProjectConst.GENYEAR, ProjectConst.GENMONTH, ProjectConst.GENDAY ) ) );
    //
    // und ab hier DATEN
    //
  }

  /**
   * 
   * Eine Logline der XML-Datei zufügen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 13.08.2013
   * 
   * @param fields
   */
  public void appendLogLine( final String[] fields )
  {
    Element genNode;
    Element presNode, depthNode, tempNode, ackuNode, ppo2Node, setpointNode, nitroNode, heliumNode, zeroTimeNode, nextStepNode;
    int stepDiff = 60000;
    //
    try
    {
      diveHeader.countSamples++;
      diveHeader.checkLowestTemp( Double.parseDouble( fields[2].trim() ) );
      diveHeader.checkMaxDepth( Integer.parseInt( fields[1].trim() ) );
      stepDiff = Integer.parseInt( fields[24].trim() );
      diveHeader.diveLength += stepDiff;
      if( diveHeader.airTemp == -1.0D )
      {
        diveHeader.airTemp = Double.parseDouble( fields[2].trim() );
      }
    }
    catch( NumberFormatException ex )
    {}
    // Wurzel dieser Ebene
    genNode = logXmlFile.createElement( "logEntry" );
    // Druck
    presNode = logXmlFile.createElement( "presure" );
    presNode.appendChild( logXmlFile.createTextNode( fields[0].trim() ) );
    genNode.appendChild( presNode );
    // Tiefe
    depthNode = logXmlFile.createElement( "depth" );
    depthNode.appendChild( logXmlFile.createTextNode( fields[1].trim() ) );
    genNode.appendChild( depthNode );
    // Temperatur
    tempNode = logXmlFile.createElement( "temp" );
    tempNode.appendChild( logXmlFile.createTextNode( fields[2].trim() ) );
    genNode.appendChild( tempNode );
    // Ackuspannung
    ackuNode = logXmlFile.createElement( "acku" );
    ackuNode.appendChild( logXmlFile.createTextNode( fields[3].trim() ) );
    genNode.appendChild( ackuNode );
    // Partialdruck O2
    ppo2Node = logXmlFile.createElement( "ppo2" );
    ppo2Node.appendChild( logXmlFile.createTextNode( fields[5].trim() ) );
    genNode.appendChild( ppo2Node );
    // Setpoint
    setpointNode = logXmlFile.createElement( "setpoint" );
    setpointNode.appendChild( logXmlFile.createTextNode( fields[6].trim() ) );
    genNode.appendChild( setpointNode );
    // Stickstoffanteil
    nitroNode = logXmlFile.createElement( "n2" );
    nitroNode.appendChild( logXmlFile.createTextNode( fields[16].trim() ) );
    genNode.appendChild( nitroNode );
    // HE Anteil
    heliumNode = logXmlFile.createElement( "he" );
    heliumNode.appendChild( logXmlFile.createTextNode( fields[17].trim() ) );
    genNode.appendChild( heliumNode );
    // Nullzeit
    zeroTimeNode = logXmlFile.createElement( "zerotime" );
    zeroTimeNode.appendChild( logXmlFile.createTextNode( fields[20].trim() ) );
    genNode.appendChild( zeroTimeNode );
    // Sekunden bis zum nächsten Eintrag
    nextStepNode = logXmlFile.createElement( "step" );
    nextStepNode.appendChild( logXmlFile.createTextNode( fields[24].trim() ) );
    genNode.appendChild( nextStepNode );
    // und in die RootNode
    rootNode.appendChild( genNode );
  }

  /**
   * 
   * Die Logdatei abschliessen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 13.08.2013
   * 
   * @return alles klar oder nicht?
   * @throws XMLFileCreatorException
   */
  public boolean closeLog() throws XMLFileCreatorException
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "closeLog: normalize DOM Object..." );
    logXmlFile.normalizeDocument();
    if( BuildConfig.DEBUG ) Log.d( TAG, "closeLog: create writer..." );
    try
    {
      StringWriter writer = new StringWriter();
      DOMSource doc = new DOMSource( logXmlFile );
      StreamResult res = new StreamResult( writer );
      if( BuildConfig.DEBUG ) Log.d( TAG, "closeLog: transform... " );
      transformer.transform( doc, res );
      // ungezipptes file erzeugen
      Log.v( TAG, "domToFile()...write to unzipped file... " );
      if( BuildConfig.DEBUG ) Log.d( TAG, "closeLog: fileName:<" + diveHeader.xmlFile.getAbsoluteFile() + ">" );
      if( diveHeader.xmlFile.exists() )
      {
        // Datei ist da, ich will sie ueberschreiben
        diveHeader.xmlFile.delete();
      }
      RandomAccessFile xmlFile = new RandomAccessFile( diveHeader.xmlFile, "rw" );
      xmlFile.writeBytes( writer.toString() );
      xmlFile.close();
    }
    catch( TransformerException ex )
    {
      Log.e( TAG, "closeLog: Transformer <" + ex.getLocalizedMessage() + ">" );
      throw new XMLFileCreatorException( ex.getMessage() );
    }
    catch( FileNotFoundException ex )
    {
      Log.e( TAG, "closeLog: FileNotFound <" + ex.getLocalizedMessage() + ">" );
      throw new XMLFileCreatorException( ex.getMessage() );
    }
    catch( IOException ex )
    {
      Log.e( TAG, "closeLog: IO-Exception <" + ex.getLocalizedMessage() + ">" );
      throw new XMLFileCreatorException( ex.getMessage() );
    }
    return( true );
  }
}
