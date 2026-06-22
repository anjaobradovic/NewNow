package rs.ftn.newnow.search;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

@Component
@Slf4j
public class PdfTextExtractor {

    private static final int MAX_TEXT_CHARS = 5_000_000;

    public String extract(InputStream pdfStream) throws IOException {
        BodyContentHandler handler = new BodyContentHandler(MAX_TEXT_CHARS);
        Metadata metadata = new Metadata();
        AutoDetectParser parser = new AutoDetectParser();
        try {
            parser.parse(pdfStream, handler, metadata, new ParseContext());
        } catch (TikaException | SAXException e) {
            throw new IOException("PDF text extraction failed", e);
        }
        String text = handler.toString();
        return text == null ? "" : text.trim();
    }
}
