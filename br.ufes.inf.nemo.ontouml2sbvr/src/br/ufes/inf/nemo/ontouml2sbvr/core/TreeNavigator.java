package br.ufes.inf.nemo.ontouml2sbvr.core;

import RefOntoUML.Class;
import RefOntoUML.Package;
import RefOntoUML.Classifier;
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
	 * @return
	 */
	Iterable<Class> getClasses();
	
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
	
	/**
	 *
	 * @param c
	 * @return
	 */
	boolean hasSolitaryChildren(Class c);
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	Iterable<Classifier> getSolitatyChildren(Class c);
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	boolean hasOwnedAssociations(Class c);
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	Iterable<Association> getOwnedAssociations(Class c);
}
