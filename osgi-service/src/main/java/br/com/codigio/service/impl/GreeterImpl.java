package br.com.codigio.service.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import br.com.codigio.service.api.Greeter;

public class GreeterImpl implements Greeter, BundleActivator {
	@SuppressWarnings("unused")
	private ServiceReference<Greeter> reference;
    private ServiceRegistration<Greeter> registration;
	
	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("Registering service.");
        registration = context.registerService(
          Greeter.class, 
          new GreeterImpl(), 
          new Hashtable<String, String>());
        reference = registration
          .getReference();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		System.out.println("Unregistering service.");
        registration.unregister();
	}

	@Override
	public String sayHiTo(String name) {		
		return "Hello " + name;
	}
}
