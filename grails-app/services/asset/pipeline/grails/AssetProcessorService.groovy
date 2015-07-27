package asset.pipeline.grails


import org.codehaus.groovy.grails.web.mapping.DefaultLinkGenerator

import static asset.pipeline.AssetHelper.fileForFullName


class AssetProcessorService {

	static transactional = false


	def grailsApplication


	/**
	 * Retrieves the asset path from the property [grails.assets.mapping] which is used by the url mapping and the
	 * taglib.  The property cannot contain <code>/</code>, and must be one level deep
	 *
	 * @return the path
	 * @throws IllegalArgumentException if the path contains <code>/</code>
	 */
	String getAssetMapping() {
		def path = grailsApplication.config?.grails?.assets?.mapping ?: 'assets'
		if (path.contains('/')) {
			throw new IllegalArgumentException(
				'The property [grails.assets.mapping] can only be one level deep.  ' +
				"For example, 'foo' and 'bar' would be acceptable values, but 'foo/bar' is not"
			)
		}

		return path
	}


	String getAssetPath(final String path, final ConfigObject conf = grailsApplication.config.grails.assets) {
		path && conf.precompiled \
			? conf.manifest.getProperty(path) ?: path
			: path
	}


	boolean isAssetPath(final String path, final ConfigObject conf = grailsApplication.config.grails.assets) {
		conf.precompiled \
			? conf.manifest.getProperty(path)
			: fileForFullName(path) != null
	}


	String asset(final Map attrs, final DefaultLinkGenerator linkGenerator) {
		final def absolutePath = linkGenerator.handleAbsolute(attrs)

		final ConfigObject conf = grailsApplication.config.grails.assets

		def url = attrs.file ?: attrs.src

		url \
			? conf.precompiled \
				? conf.manifest.getProperty(url)
				: fileForFullName(url) != null \
					? url
					: null
			: null

		if (!url) {
			return null
		}

		url = assetUriRootPath() + url

		if (!url.startsWith('http')) {
			if (absolutePath == null) {
				final contextPathAttribute = attrs.contextPath?.toString()
				final cp = contextPathAttribute == null ? linkGenerator.contextPath : contextPathAttribute
				absolutePath = \
					cp == null \
						? linkGenerator.handleAbsolute(absolute: true)
						: cp
			}
			url = (absolutePath ?: '') + (url ?: '')
		}
		return url
	}

	private String assetUriRootPath() {
		final def url = grailsApplication.config.grails.assets.url
		url instanceof Closure \
			? url.call(null)
			: url ?: "/$assetMapping/"
	}
}
