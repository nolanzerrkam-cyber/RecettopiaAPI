package be.recettopia.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/** JAX-RS entry point: every resource is served under /api */
@ApplicationPath("/api")
public class RecettopiaApplication extends Application {
}