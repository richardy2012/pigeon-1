package payne.framework.pigeon.integration.web;

import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import payne.framework.pigeon.core.annotation.Open;
import payne.framework.pigeon.core.detector.ClassFilter;
import payne.framework.pigeon.core.factory.stream.InternalStreamFactory;
import payne.framework.pigeon.core.factory.stream.StreamFactory;
import payne.framework.pigeon.integration.spring.SpringBeanFactory;
import payne.framework.pigeon.server.DefaultInvocationProcessorRegistry;
import payne.framework.pigeon.server.InvocationContext;
import payne.framework.pigeon.server.InvocationContextAware;
import payne.framework.pigeon.server.InvocationProcessorRegistry;

/**
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Company: 广州市俏狐信息科技有限公司
 * </p>
 * 
 * @author yangchangpei 646742615@qq.com
 *
 * @date 2015年11月9日 上午10:41:08
 *
 * @version 1.0.0
 */
public class SpringInvocationContextFilter extends WebInvocationContextFilter implements InvocationContext, Filter, ClassFilter {
	public static final String BEAN_CONFIGURATION_PATH = "bean-configuration-path";

	@Override
	public void init(FilterConfig config) throws ServletException {
		try {
			this.charset = config.getInitParameter(CHARSET) == null ? "UTF-8" : config.getInitParameter(CHARSET);
			String beanConfigurationPath = config.getInitParameter(BEAN_CONFIGURATION_PATH);
			beanConfigurationPath = beanConfigurationPath == null ? "pigeon.properties" : beanConfigurationPath;
			ApplicationContext applicationContext = (ApplicationContext) config.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
			beanFactory = new SpringBeanFactory(applicationContext, beanConfigurationPath);
			streamFactory = config.getInitParameter(STREAM_FACTORY) == null ? new InternalStreamFactory() : (StreamFactory) Class.forName(config.getInitParameter(STREAM_FACTORY)).newInstance();
			String registry = config.getInitParameter(REGISTRY);
			invocationProcessorRegistry = registry == null ? new DefaultInvocationProcessorRegistry(beanFactory, streamFactory) : (InvocationProcessorRegistry) Class.forName(registry).newInstance();
			Map<String, Object> map = applicationContext.getBeansWithAnnotation(Open.class);
			for (Object implementation : map.values()) {
				this.register(implementation);
				if (implementation instanceof InvocationContextAware) {
					InvocationContextAware aware = (InvocationContextAware) implementation;
					aware.setInvocationContext(this);
				}
			}
		} catch (Exception e) {
			throw new ServletException(e);
		} finally {
			config.getServletContext().setAttribute(WEB_INVOCATION_CONTEXT_ATTRIBUTE_KEY, this);
		}
	}

}
