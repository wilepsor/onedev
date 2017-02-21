package com.gitplex.server.web;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.core.request.mapper.ResourceMapper;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.mapper.ICompoundRequestMapper;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletMapping;

import com.gitplex.server.util.ReflectionUtils;
import com.gitplex.server.util.validation.AccountNameReservation;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

public class WebAccountNameReservation implements AccountNameReservation {

	private final Provider<ServletContextHandler> servletContextHandlerProvider;
	
	private final WicketConfig webApp;
	
	@Inject
	public WebAccountNameReservation(Provider<ServletContextHandler> servletContextHandlerProvider, WicketConfig webApp) {
		this.servletContextHandlerProvider = servletContextHandlerProvider;
		this.webApp = webApp;
	}
	
	@Override
	public Set<String> getReserved() {
		Set<String> reserved = new HashSet<String>();
		for (ServletMapping mapping: servletContextHandlerProvider.get().getServletHandler().getServletMappings()) {
			for (String pathSpec: mapping.getPathSpecs()) {
				pathSpec = StringUtils.stripStart(pathSpec, "/");
				pathSpec = StringUtils.substringBefore(pathSpec, "/");
				if (pathSpec.trim().length() != 0)
					reserved.add(pathSpec.trim());
			}
		}
		
		reserved.add("wicket");
		reserved.add("new");
		reserved.add(".");
		
		for (IRequestMapper mapper: webApp.getRequestMappers()) {
			Set<String> set = getReservedNames(mapper);
			reserved.addAll(set);
		}
		
		return reserved;
	}

	private Set<String> getReservedNames(IRequestMapper mapper) {
		if (mapper instanceof MountedMapper || mapper instanceof ResourceMapper) {
			try {
				Field field = ReflectionUtils.findField(mapper.getClass(), "mountSegments");
				Preconditions.checkNotNull(field);
				field.setAccessible(true);
				String[] mountSegments = (String[]) field.get(mapper);
				if (mountSegments != null && mountSegments.length != 0 && mountSegments[0] != null)
					return Collections.singleton(mountSegments[0]);
				else
					return Collections.emptySet();
			} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
		if (mapper instanceof ICompoundRequestMapper) {
			ICompoundRequestMapper m = (ICompoundRequestMapper) mapper;
			Set<String> result = Sets.newHashSet();
			for (IRequestMapper each : m) {
				Set<String> set = getReservedNames(each);
				result.addAll(set);
			}
			
			return result;
		}
		
		return Collections.emptySet();
	}
}