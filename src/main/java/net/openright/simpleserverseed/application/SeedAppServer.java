package net.openright.simpleserverseed.application;

import java.io.File;
import java.net.URI;

import net.openright.infrastructure.server.EmbeddedWebAppContext;
import net.openright.infrastructure.server.ServerUtil;
import net.openright.infrastructure.server.StatusHandler;
import net.openright.infrastructure.util.IOUtil;
import net.openright.infrastructure.util.LogUtil;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ShutdownHandler;

public class SeedAppServer {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SeedAppServer.class);
	private SeedAppConfig config;
	private Server server;

	public SeedAppServer(SeedAppConfig config) {
		this.config = config;
	}

	public static void main(String[] args) throws Exception {
		new File("logs").mkdirs();
		LogUtil.setupLogging("logging-seedapp.xml");
		IOUtil.extractResourceFile("seedapp.properties");

		SeedAppConfig config = new SeedAppConfigFile("seedapp.properties");

		SeedAppServer server = new SeedAppServer(config);
        server.start(config.getHttpPort());

        if (System.getProperty("startBrowser") != null) {
            Runtime.getRuntime().exec("cmd /c \"start " + server.getURI() + "\"");
        }
	}

	public void start(int port) throws Exception {
		new EnvEntry("jdbc/seedappDs", config.createDataSource());

		server = new Server(port);
		server.setHandler(createHandlers());
		server.start();

		log.info("Started server " + server.getURI());
	}

	private Handler createHandlers() {
		HandlerList handlers = new HandlerList();
		handlers.addHandler(new ShutdownHandler("sgds", false, true));
		handlers.addHandler(new EmbeddedWebAppContext("/seedapp"));
		handlers.addHandler(new StatusHandler());

		return ServerUtil.createStatisticsHandler(
				ServerUtil.createRequestLogHandler(handlers));
	}

	public URI getURI() {
		return server.getURI();
	}


}
