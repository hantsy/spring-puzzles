package com.example.demo;

import com.example.demo.jdbc.JdbcConfig;
import com.example.demo.jpa.JpaConfig;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.ServletRegistration;

public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class[] {
				AppConfig.class,//
				DataSourceConfig.class,
				JpaConfig.class, //
				JdbcConfig.class,//
				Jackson2ObjectMapperConfig.class
		};
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class[] {
				WebConfig.class, //
		};
	}

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        registration.setInitParameter("enableLoggingRequestDetails", "true");
        super.customizeRegistration(registration);
    }

    @Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}



/*	@Override
	protected Filter[] getServletFilters() {
		HiddenHttpMethodFilter httpMethodFilter = new HiddenHttpMethodFilter();

		CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
		encodingFilter.setEncoding("UTF-8");
		encodingFilter.setForceEncoding(true);

		return new Filter[] { httpMethodFilter, encodingFilter };
	}*/
}

/*public class AppInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        // Load Spring web application configuration
        AnnotationConfigWebApplicationContext ac = new AnnotationConfigWebApplicationContext();
        ac.register(
                AppConfig.class,
                DataSourceConfig.class,
                JpaConfig.class,
                Jackson2ObjectMapperConfig.class,
                WebConfig.class
        );
        ac.refresh();

        // Create and register the DispatcherServlet
        DispatcherServlet servlet = new DispatcherServlet(ac);
        ServletRegistration.Dynamic registration = servletContext.addServlet("app", servlet);
        registration.setInitParameter("enableLoggingRequestDetails", "true");
        registration.setLoadOnStartup(1);
        registration.addMapping("/*");
    }

}*/
