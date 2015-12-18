package sync_chart;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.StdErrLog;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/sync_chart")
public class SyncChart {
	private static Cache cache = Cache.getInstance();
	private static SimpleDateFormat SDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS z");
	private static Logger LOG = LoggerFactory.getLogger(SyncChart.class);
	static {
		StdErrLog stdErrLog = new StdErrLog();
		stdErrLog.setSource(true);
		stdErrLog.setPrintLongNames(false);
		stdErrLog.setLevel(StdErrLog.LEVEL_WARN);
		Log.setLog(stdErrLog);
	}

	public static void main(String[] args) throws Exception {
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");

		String port = "8080";
		if (1 <= args.length) {
			String argPort = args[0];
			if (argPort != null && argPort.matches("\\d+$")) {
				port = argPort;
			}
		} else if (System.getProperties().containsKey("port")){
			String propPort = System.getProperty("port");
			if (propPort != null && propPort.matches("\\d+$")) {
				port = propPort;
			}
		}

		Server server = new Server(Integer.parseInt(port));
		server.setHandler(context);
		ServletHolder sh = context.addServlet(ServletContainer.class, "/*");
		sh.setInitOrder(0);

		// Tells the Jersey Servlet which REST service/class to load.
		sh.setInitParameter("jersey.config.server.provider.classnames", SyncChart.class.getCanonicalName());

		try {
			LOG.warn("Starting sync_chart with port {}", port);
			server.start();
			server.join();
		} finally {
			server.destroy();
		}
	}

	@GET
	@Path("get")
	@Produces(MediaType.TEXT_PLAIN)
	public String get(@QueryParam("broaker") String broaker, @QueryParam("symbol") String symbol) {
		Price price = cache.getPrice(broaker, symbol);
		String ts = SDF.format(new java.util.Date(price.getAcceptTime()));
		String result = String.format("%s,%s,bid=%f,ask=%f,close=%f,%s", broaker, symbol, price.getBid(), price.getAsk(), price.getClose(), ts);
		LOG.info("GET  {}", result);
		return result;
	}

	@GET
	@Path("save")
	@Produces(MediaType.TEXT_PLAIN)
	public String save(@QueryParam("broaker") String broaker, @QueryParam("symbol") String symbol,
			@QueryParam("bid") double bid, @QueryParam("ask") double ask, @QueryParam("close") double close) {
		long acceptTime = System.currentTimeMillis();
		String ts = SDF.format(new java.util.Date(acceptTime));
		cache.save(broaker, symbol, bid, ask, close, acceptTime);
		String result = String.format("%s,%s,b=%f,a=%f,c=%f,%s", broaker, symbol, bid, ask, close, ts);
		LOG.info("SAVE {}", result);
		return result;
	}

	public static class Cache {
		private static Cache instance = new Cache();
		private HashMap<String, HashMap<String, Price>> broaker_symbol_price = new HashMap<>();

		public static Cache getInstance() {
			return instance;
		}

		public Price getPrice(String broaker, String symbol) {
			HashMap<String, Price> symbol_price = instance.broaker_symbol_price.get(broaker);
			if (symbol_price == null) {
				symbol_price = new HashMap<>();
				instance.broaker_symbol_price.put(broaker, symbol_price);
			}

			Price price = symbol_price.get(symbol);
			if (price == null) {
				price = new Price(0.0, 0.0, 0.0, 0L);
				symbol_price.put(symbol, price);
			}
			return price;
		}

		public void save(String broaker, String symbol, double bid, double ask, double close, long acceptTime) {
			HashMap<String, Price> symbol_price = instance.broaker_symbol_price.get(broaker);
			if (symbol_price == null) {
				symbol_price = new HashMap<>();
				instance.broaker_symbol_price.put(broaker, symbol_price);
			}

			Price price = symbol_price.get(symbol);
			if (price == null) {
				price = new Price(0.0, 0.0, 0.0, 0L);
				symbol_price.put(symbol, price);
			}
			price.setClose(close);
			price.setBid(bid);
			price.setAsk(ask);
			price.setAcceptTime(acceptTime);
			symbol_price.put(symbol, price);
		}
	}
}

