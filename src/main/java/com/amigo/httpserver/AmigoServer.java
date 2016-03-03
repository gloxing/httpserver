/**  
 * @Title: AmigoServer.java
 * @Package com.amigo.httpserver
 * @Description: TODO
 * @author wenglx
 * @date 2016-3-2
 */
package com.amigo.httpserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * ClassName: AmigoServer
 * 
 * @Description: TODO
 * @author wenglx
 * @date 2016-3-2
 */
public class AmigoServer {
	/**
	 * @Description: TODO
	 * @throws
	 * @author wenglx
	 * @date 2016-3-2
	 */
	public static void main(String[] args) {
		try {
			HttpServer httpServer = HttpServer.create(
					new InetSocketAddress(444), 5);
			HttpContext aContext = httpServer
					.createContext("/a", new Handler());
			HttpContext amigoContext = httpServer.createContext("/amigo",
					new AmigoHandler());

			// 加filter
			amigoContext.getFilters().add(new AmigoFilter());
			amigoContext.getFilters().add(new AmigoFilter2());
			amigoContext.getFilters().add(
					new LogFilter(new File("http_server_log.txt")));

			System.out.println(httpServer.getAddress().getHostName());
			System.out.println(httpServer.getAddress().getHostString());
			httpServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

class AmigoHandler implements HttpHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.net.httpserver.HttpHandler#handle(com.sun.net.httpserver.HttpExchange
	 * )
	 */
	@Override
	public void handle(HttpExchange arg0) throws IOException {
		// TODO Auto-generated method stub
		System.out.println(arg0.getRequestURI().getQuery());

		Map<String, String> map = new HashMap();
		String[] strings = arg0.getRequestURI().getQuery().split("&");
		for (String s : strings) {
			String[] strs = s.split("=");
			map.put(strs[0], strs[1]);
		}
		System.out.println(map.toString());

		System.out.println(this.getClass() + "\t start:");
		Map<String, Object> maps = arg0.getHttpContext().getAttributes();
		System.out.println(maps.get("P1"));
		System.out.println(arg0.getRequestURI());
		System.out.println(arg0.getRequestMethod());
		System.out.println(arg0.getRequestBody());
		String str = "hello httpserver!";
		arg0.sendResponseHeaders(200, str.length());
		OutputStream ost = arg0.getResponseBody();
		ost.write(str.getBytes());
		ost.close();
	}

}

class Handler implements HttpHandler {
	public void handle(HttpExchange xchg) throws IOException {
		Headers headers = xchg.getRequestHeaders();
		Set<Map.Entry<String, List<String>>> entries = headers.entrySet();

		StringBuffer response = new StringBuffer();
		for (Map.Entry<String, List<String>> entry : entries)
			response.append(entry.toString() + "\n");

		xchg.sendResponseHeaders(200, response.length());
		OutputStream os = xchg.getResponseBody();
		os.write(response.toString().getBytes());
		os.close();
	}
}

class AmigoFilter extends Filter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.net.httpserver.Filter#description()
	 */
	@Override
	public String description() {
		// TODO Auto-generated method stub
		return "amigo 第一个 filter";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.net.httpserver.Filter#doFilter(com.sun.net.httpserver.HttpExchange
	 * , com.sun.net.httpserver.Filter.Chain)
	 */
	@Override
	public void doFilter(HttpExchange arg0, Chain arg1) throws IOException {
		// TODO Auto-generated method stub
		System.out.println(this.description());
		arg1.doFilter(arg0);
	}

}

class AmigoFilter2 extends Filter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.net.httpserver.Filter#description()
	 */
	@Override
	public String description() {
		// TODO Auto-generated method stub
		return "amigo 第 二个 filter";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.net.httpserver.Filter#doFilter(com.sun.net.httpserver.HttpExchange
	 * , com.sun.net.httpserver.Filter.Chain)
	 */
	@Override
	public void doFilter(HttpExchange arg0, Chain arg1) throws IOException {
		// TODO Auto-generated method stub
		System.out.println(this.description());
		arg1.doFilter(arg0);
	}

}

class LogFilter extends Filter {

	PrintStream ps;
	DateFormat df;

	LogFilter(File file) throws IOException {
		ps = new PrintStream(new FileOutputStream(file));
		df = DateFormat.getDateTimeInstance();
	}

	/**
	 * The filter's implementation, which is invoked by the serve r
	 */
	public void doFilter(HttpExchange t, Filter.Chain chain) throws IOException {
		// 先将要做的事做完，就是post-filter
		chain.doFilter(t);
		HttpContext context = t.getHttpContext();
		Headers rmap = t.getRequestHeaders();
		String s = df.format(new Date());
		s = s + " " + t.getRequestMethod() + " " + t.getRequestURI() + " ";
		s = s + " " + t.getResponseCode() + " " + t.getRemoteAddress();
		ps.println(s);
	}

	public void init(HttpContext ctx) {
	}

	public String description() {
		return "Request logger";
	}

	public void destroy(HttpContext c) {
	}
}