package br.ufes.inf.nemo.ontouml2sbvr.core;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import RefOntoUML.*;
import RefOntoUML.Class;
import RefOntoUML.Package;

public class Transformation
{
	FileManager myfile;
	TreeNavigator treeNavigator;
	
	public Transformation (File sourceFile)
	{
		myfile = new FileManager(sourceFile);
	}
	
	public void Transform (EObject o, boolean serial)
	{		
		if (!(o instanceof Package))
			return;
		
		Package p = (Package) o;
		
		myfile.serial = serial;
		treeNavigator = new TreeNavigatorImpl();
		treeNavigator.build((RefOntoUML.Package)p);
		myfile.addTreeNavigator(treeNavigator);

		List<Classifier> mainClasses = new LinkedList<>();
		for (Classifier c : this.treeNavigator.getClasses())
			if (c.parents().size() == 0)
				mainClasses.add(c);
		for (Classifier c : mainClasses)
			myfile.DealNode((Class)c, !myfile.serial);
		
		for (DataType dt : treeNavigator.getDataTypes())
			myfile.DealDataType(dt);

		for (Map.Entry<String, Classifier> ar : treeNavigator.getAssociationRoles())
			myfile.DealAssociationRole(ar.getKey(), ar.getValue());

		myfile.Done();
	}
}
