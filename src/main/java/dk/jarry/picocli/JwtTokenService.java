package dk.jarry.picocli;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtSignatureBuilder;
import jakarta.enterprise.context.Dependent;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;

@Dependent
public class JwtTokenService {

  public final static String INFO_BEGIN = "\033[0;32m";
  public final static String WARN_BEGIN = "\033[0;31m";
  public final static String ERROR_BEGIN = "\033[1;31m";
  public final static String END = "\033[0;39m";

  public void printToken(String token, int outputFormat) {
    var partsAsJsonObject = getJsonObjects(token);
    JsonObject header = partsAsJsonObject.get(0);
    JsonObject payload = partsAsJsonObject.get(1);
    printToken(token, header, payload, outputFormat);
  }

  public String generateToken(String kid, String payloadBase64, int outputFormat) {

    String payload;
    try {
      var decode = Base64.getDecoder().decode(payloadBase64);
      payload = new String(decode);
    } catch (IllegalArgumentException e) {
      throw e;
    }

    JsonReader createReader = Json.createReader(new StringReader(new String(payload)));
    JsonObject jwtObject = createReader.readObject();

    JsonObjectBuilder builder = Json.createObjectBuilder(jwtObject);
    long iat = ZonedDateTime.now().toEpochSecond();
    builder.add("exp", iat + 300);
    builder.add("iat", iat);

    JwtSignatureBuilder jwtSignatureBuilder = Jwt
      .claims(builder.build())
      .jws();
    if(kid != null){
      jwtSignatureBuilder = jwtSignatureBuilder.keyId(kid);
    }
    String token = jwtSignatureBuilder.sign();

    printToken(token, outputFormat);

    return token;
  }

  List<JsonObject> getJsonObjects(String token) {

    var parts = token.split("\\.");
    if (parts.length != 3) {
      throw new IllegalArgumentException("Not a validt token - do not have tree dots in token - but " + parts.length);
    }

    return Stream.of(parts)
        .limit(2)
        .map(Base64.getDecoder()::decode)
        .map(String::new)
        .map(StringReader::new)
        .map(Json::createReader)
        .map(reader -> reader.readObject())
        .collect(Collectors.toList());
  }

  private void printToken(String token, JsonObject header, JsonObject payload, int outputFormat) {

    if(outputFormat == 1){
      JsonObjectBuilder builder = Json.createObjectBuilder();
      builder.add("token", token);
      builder.add("header", header);
      builder.add("payload", payload);
      System.out.print(prettyPrint(builder.build()));
    }else{
      System.out.println("Token ::");
      System.out.println(token);

      System.out.println("Header :: ");
      System.out.println(INFO_BEGIN + prettyPrint(header) + END);

      System.out.println("Payload :: ");
      System.out.println(INFO_BEGIN + prettyPrint(payload) + END);
    }
  }

  private String prettyPrint(JsonObject jsonObject) {
    String pretty = "";
    try (StringWriter sw = new StringWriter()) {
      Map<String, Object> map = new HashMap<>();
      map.put(JsonGenerator.PRETTY_PRINTING, true);
      JsonWriterFactory writerFactory = Json.createWriterFactory(map);
      try (JsonWriter jsonWriter = writerFactory.createWriter(sw)) {
        jsonWriter.writeObject(jsonObject);
      }
      pretty = sw.toString();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return pretty;
  }

}
