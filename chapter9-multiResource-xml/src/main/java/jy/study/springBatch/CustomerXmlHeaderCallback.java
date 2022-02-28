package jy.study.springBatch;

import org.springframework.batch.item.xml.StaxWriterCallback;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

@Component
public class CustomerXmlHeaderCallback implements StaxWriterCallback {

    @Override
    public void write(XMLEventWriter writer) throws IOException {
        XMLEventFactory factory = XMLEventFactory.newFactory();

        try {
            writer.add(factory.createStartElement("", "", "identification"));
            writer.add(factory.createStartElement("", "", "author"));
            writer.add(factory.createAttribute("name", "Kim Joonyeop"));
            writer.add(factory.createEndElement("", "", "author"));
            writer.add(factory.createEndElement("", "", "identification"));
        } catch (XMLStreamException e) {
            System.out.println("An error occured: " + e.getMessage());
            e.printStackTrace(System.err);
        }

    }
}
