package hello;

import java.io.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import tech.blueglacier.email.Attachment;
import tech.blueglacier.email.Email;
import tech.blueglacier.parser.CustomContentHandler;
import tech.blueglacier.util.MimeWordDecoder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.message.DefaultBodyDescriptorBuilder;
import org.apache.james.mime4j.parser.ContentHandler;

import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.BodyDescriptorBuilder;
import org.apache.james.mime4j.stream.MimeConfig;


@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World001") String name) {
        return new Greeting(counter.incrementAndGet()+100,
                            String.format(template, name));
    }


    @RequestMapping("/mailevent/s3")
    public Greeting greeting(@RequestParam(value="bucket", defaultValue="~~~") String bucket,@RequestParam(value="key", defaultValue="~~~") String key) {
        Greeting retObj = new Greeting();
        retObj.setId(counter.incrementAndGet()+9218);
        final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
        try {
            S3Object o = s3.getObject(bucket, key);
            S3ObjectInputStream s3is = o.getObjectContent();
            ContentHandler contentHandler = new CustomContentHandler();

            MimeConfig mime4jParserConfig = MimeConfig.DEFAULT;
            BodyDescriptorBuilder bodyDescriptorBuilder = new DefaultBodyDescriptorBuilder();
            MimeStreamParser mime4jParser = new MimeStreamParser(mime4jParserConfig,DecodeMonitor.SILENT,bodyDescriptorBuilder);
            mime4jParser.setContentDecoding(true);
            mime4jParser.setContentHandler(contentHandler);

            mime4jParser.parse(s3is);

            Email email = ((CustomContentHandler) contentHandler).getEmail();

            List<Attachment> attachments =  email.getAttachments();

            Attachment calendar = email.getCalendarBody();
            Attachment htmlBody = email.getHTMLEmailBody();
            Attachment plainText = email.getPlainTextEmailBody();

            String to = email.getToEmailHeaderValue();
            String cc = email.getCCEmailHeaderValue();
            String from = email.getFromEmailHeaderValue();


            StringWriter writer = new StringWriter();
            IOUtils.copy(email.getPlainTextEmailBody().getIs(), writer, "UTF-8");
            String theString = writer.toString();
            //String theString = IOUtils.toString(inputStream, encoding);

            //StringBuilder retContent = new StringBuilder();
            //email.getHeader()
            StringBuilder retContent = new StringBuilder();
            retContent.append("bucket ->"+bucket).append("~~~key->"+key);
            retContent.append("~~~to->"+to)
                    .append("~~~from->"+from)
                    .append("~~~subject->"+email.getEmailSubject())
                    .append("~~~plainText->"+theString)
                    ;
            retObj.setContent(retContent.toString());
        }catch(Exception e){
            e.printStackTrace();
        }

        return retObj;
    }
}
