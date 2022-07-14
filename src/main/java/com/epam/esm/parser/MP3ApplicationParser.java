package com.epam.esm.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import com.epam.esm.model.SongMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

@Component
@Slf4j
public class MP3ApplicationParser {

    public SongMetadata parse(ByteArrayResource resource) {
        BodyContentHandler handler = new BodyContentHandler();
        try (InputStream input = new ByteArrayInputStream(resource.getByteArray())) {
            Metadata metadata = new Metadata();
            Parser parser = new Mp3Parser();
            ParseContext parseCtx = new ParseContext();
            parser.parse(input, handler, metadata, parseCtx);
            input.close();
            return SongMetadata.builder()
                    .songName(metadata.get("dc:title"))
                    .artist(metadata.get("xmpDM:artist"))
                    .album(metadata.get("xmpDM:album"))
                    .length(convertTimeFormat(metadata.get("xmpDM:duration")))
                    .build();
        } catch (IOException | TikaException | SAXException e) {
            log.error("Can't parse song metadata", e);
            throw new RuntimeException("Can't parse song metadata", e);
        }
    }

    private String convertTimeFormat(String length) {
        long duration = (long) Double.parseDouble(length);
        long minutes = TimeUnit.SECONDS.toMinutes(duration);
        long sec = duration - minutes * 60;
        return String.format("%d:%d", minutes, sec);
    }
}
