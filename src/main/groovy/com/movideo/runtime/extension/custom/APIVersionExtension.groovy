package com.movideo.runtime.extension.custom

import groovyx.net.http.RESTClient

import java.lang.annotation.Annotation
import java.util.regex.Pattern

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.SpecInfo

/**
 * API Version Extension
 *
 */
class APIVersionExtension extends AbstractAnnotationDrivenExtension<APIVersion> {

	/**
	 * Logger
	 */
	private static final Log LOG = LogFactory.getLog(getClass())

	private static final defaultConfig = new File('src/test/resources/SpockConfig.groovy')

	/**
	 * Version REGX pattern
	 */
	private static final def VERSION_PATTERN = Pattern.compile(".", Pattern.LITERAL)

	/**
	 * Max version length
	 */
	private static final def MAX_VERSION_LENGTH = 4

	/**
	 * 
	 */
	private static def configFile

	/**
	 * env environment variable
	 * <p>
	 * Defaults to {@code LOCAL_END_POINT}
	 */
	private static String envString

	/**
	 * Current API Version
	 */
	private static def CURRENT_API_VERSION

	static 
	{
		if(System.getProperties().getProperty("spockConfig") != null || System.getenv("spockConfig") != null)
		{
			if(System.getProperties().getProperty("spockConfig") != null)
			{
				configFile = new ConfigSlurper().parse(new File(System.getProperties().getProperty("spockConfig")).toURL())

			}
			else
			{
				configFile = new ConfigSlurper().parse(new File(System.getenv("spockConfig")).toURL())
			}
		}
		else
		{
			configFile = new ConfigSlurper().parse(new File('src/test/resources/SpockConfig.groovy').toURL())
		}

		envString = "http://" + System.getProperties().getProperty("env", configFile.envHost)

		CURRENT_API_VERSION = getDeployedAPIVersion()
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void visitFeatureAnnotation(APIVersion annotation, FeatureInfo feature) 
	{
		if(!isApiVersionGreaterThanMinApiVersion(annotation, feature.name)) 
		{
			feature.setSkipped(true)
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitSpecAnnotation(APIVersion annotation, SpecInfo spec) 
	{
		if(!isApiVersionGreaterThanMinApiVersion(annotation, spec.name)) 
		{
			spec.setSkipped(true)
		}
	}

	/**
	 * Get the current deployed API version
	 * <p>
	 * Performs a HTTP request to the current deployed API version. Parses the returned data and get the {@code version} node data.
	 * @return current deployed API version
	 */
	private static String getDeployedAPIVersion() 
	{
		def apiVersion = null

		try 
		{
			def client = new RESTClient(envString)
			def resp = client.get(path : configFile.versionServiceUri)

			apiVersion = resp.data.version

			LOG.info("Current deployed API version [" + apiVersion + "]");
		} 
		catch (ex) 
		{
			APIVersionError apiVersionError = new APIVersionError("Error occurred attempting to get current deployed API version from %s", envString + configFile.versionServiceUri);
			apiVersionError.setStackTrace(ex.stackTrace);
			
			throw apiVersionError;
		}

		return apiVersion
	}

	/**
	 * 
	 * @param annotation
	 * @param infoName
	 * @return
	 */
	private boolean isApiVersionGreaterThanMinApiVersion(APIVersion annotation, String infoName) 
	{
		def isApiVersionGreaterThanMinApiVersion = true

		def minApiVersionRequired = annotation.minimimApiVersion();

		// normalise both version id's
		def apiVersionNormalised = normaliseVersion(CURRENT_API_VERSION);
		def minApiVersionRequiredNormalised = normaliseVersion(minApiVersionRequired);

		// compare version id's
		int cmp = apiVersionNormalised.compareTo(minApiVersionRequiredNormalised);

		// if the comparison is less than 0, min API version is greater than the deployed API version
		if(cmp < 0) 
		{
			LOG.info("min api version [" + minApiVersionRequired + "] greater than api version [" + CURRENT_API_VERSION + "], skipping [" + infoName + "]")
			isApiVersionGreaterThanMinApiVersion = false
		}

		return isApiVersionGreaterThanMinApiVersion
	}

	/**
	 * 
	 * @param version
	 * @return
	 */
	private String normaliseVersion(String version) 
	{
		String[] split = VERSION_PATTERN.split(version);
		StringBuilder sb = new StringBuilder();

		for (String s : split) 
		{
			sb.append(String.format("%" + MAX_VERSION_LENGTH + 's', s));
		}

		return sb.toString();
	}
}