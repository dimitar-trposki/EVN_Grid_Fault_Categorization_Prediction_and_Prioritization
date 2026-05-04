package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.constants;

public class JwtConstants {
    private static final String DEFAULT_SECRET_KEY = "dGhpc0lzQVZlcnlMb25nU2VjcmV0S2V5Rm9yRXZuR3JpZEFwcFRoYXRJc0F0TGVhc3QzMkJ5dGVz";
    public static final String SECRET_KEY = System.getenv("SECRET_KEY") != null
            ? System.getenv("SECRET_KEY")
            : DEFAULT_SECRET_KEY;
    public static final Long EXPIRATION_TIME = 864000000L;
    public static final String HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
}
