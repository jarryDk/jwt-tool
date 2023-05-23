package dk.jarry.picocli;

import jakarta.inject.Inject;
import picocli.CommandLine;

@CommandLine.Command(mixinStandardHelpOptions = true, version = "jwt-tool version 1.0.0")
public class JwtTokenCommand implements Runnable {

    @CommandLine.Option( //
            names = { "-t", "--token" }, //
            description = "Input will have to be a JWT!", //
            defaultValue = "")
    String token;

    @CommandLine.Option( //
            names = { "-k", "--kid" }, //
            description = "Input will be kid in header", //
            defaultValue = "")
    String kid;

    @CommandLine.Option( //
            names = { "-p", "--payload" }, //
            description = "Input will have to be base64 encoded Json!", //
            defaultValue = "")
    String payload;

    @CommandLine.Option( //
            names = { "-f", "--format" }, //
            description = "Output format json or plantext (default)", //
            defaultValue = "")
    String format;

    @Inject
    private JwtTokenService jwtTokenService;

    public JwtTokenCommand(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public void run() {

        if ((token == null || token.isEmpty())
                &&
                (payload == null || payload.isEmpty())) {
            System.out.println(JwtTokenService.WARN_BEGIN + "I need input to work" + JwtTokenService.END);
            System.out.println("");
            System.out.println("Sample input");
            System.out.println(
                    " -t eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
            System.out.println(" -k ucJxucWD1VfqR5NzBkJfx6FsYbbGxG18y9mVkk1XabY");
            System.out.println(" -p $(echo \"{\\\"id\\\":42,\\\"name\\\":\\\"Duke\\\"}\" | base64)");
            return;
        }

        int outputFormat = "json".equalsIgnoreCase(format.toLowerCase())?1:0;

        if (token != null && !token.isEmpty()) {
            try {
                jwtTokenService.printToken(token,outputFormat);
            } catch (IllegalArgumentException e) {
                System.err.println(JwtTokenService.ERROR_BEGIN + e.getMessage() + JwtTokenService.END);
            }
        }

        if("".equals(kid)){
            kid = null;
        }

        if (payload != null && !payload.isEmpty()) {
            try {
                jwtTokenService.generateToken(kid, payload,outputFormat);
            } catch (IllegalArgumentException e) {
                System.err.println(JwtTokenService.ERROR_BEGIN + e.getMessage() + JwtTokenService.END);
                System.err.println(JwtTokenService.WARN_BEGIN + "Hint: -p | --payload need to be base64 encoded" + JwtTokenService.END);
                System.err.println(JwtTokenService.WARN_BEGIN + "Sample : -p $(echo \"{\\\"id\\\":42,\\\"name\\\":\\\"Duke\\\"}\" | base64)" + JwtTokenService.END);
            }
        }
    }

}
