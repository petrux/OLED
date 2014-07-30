package br.ufes.inf.nemo.ontouml2sbvr.core;

import RefOntoUML.Class;
import RefOntoUML.Package;
import RefOntoUML.Association;

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
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	boolean hasAssociations(Class c);
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	Iterable<Association> getAssociations(Class c);
}
