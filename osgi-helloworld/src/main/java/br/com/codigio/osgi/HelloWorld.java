package br.com.codigio.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class HelloWorld implements BundleActivator {
	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("Hello world.");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		System.out.println("Goodbye world.");
	}	
}
