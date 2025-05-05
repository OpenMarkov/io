package org.openmarkov.io.probmodel;


import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Disabled;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VersionValidatorTest {

    private static String absolutePath;

    @Disabled("Jenkins compilation failure")
    @Test
    public void Validator() throws Exception {
        String[] files;

        Path resourceDirectory = Paths.get("src","test","resources");
        absolutePath = resourceDirectory.toFile().getAbsolutePath();

        File f = new File(absolutePath);

        files = f.list();

        for ( String path : files ){
            if (path.contains(".pgmx")){
                Version(path);
            }
        }

    }

    private static void Version(String name) throws Exception {

        DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = parser.parse(new File(absolutePath+"\\"+name));

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        Source schemaFile = new StreamSource(new File(absolutePath+"\\version.xsd"));
        Schema schema = factory.newSchema(schemaFile);

        Validator validator = schema.newValidator();

        try {
            validator.validate(new DOMSource(document));
        } catch (SAXException e) {
            System.out.println("Archivo "+name+" Version no soportada");
            throw e;
        }
    }

}
