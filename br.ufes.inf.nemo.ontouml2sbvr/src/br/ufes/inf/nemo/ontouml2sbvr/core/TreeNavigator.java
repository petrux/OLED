package br.ufes.inf.nemo.ontouml2sbvr.core;

import RefOntoUML.Package;

/**
 * @author petrux
 * @since 29 July 2014
 */
public interface TreeNavigator {
	
	/**
	 * Build an inner representation for the given {@code roopPackage}
	 * allowing client classes to navigate it.
	 * 
	 * @param rootPackage
	 * @since 29 July 2014
 	 */
	void build(Package rootPackage);
}
