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
	TreeProcessor myprocessor;
	TreeNavigator treeNavigator;
	
	public Transformation (File sourceFile)
	{
		myfile = new FileManager(sourceFile);
		myprocessor = new TreeProcessor();
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
		Tree(p);
		
		myfile.Done();
	}
	
	public void Tree (Package p)
	{
		// Pre Process all classes
		for (PackageableElement pe : p.getPackagedElement())
		{			
			if (pe instanceof Package)
			{
				this.Tree((Package)pe);
			}
		}
	
		
		for (PackageableElement pe : p.getPackagedElement())
		{			
			if (pe instanceof Class)
			{
				myprocessor.ProcessClass((Class) pe);
			}
		}
	
		
		
		// Process all associations
		for (PackageableElement pe : p.getPackagedElement())
		{			
			if (pe instanceof Association)
			{
				myprocessor.ProcessAssociation((Association) pe);
			}
		}
		
		// Set up the specialization tree
		myprocessor.ProcessNodes();
		
		List<Class> mainClasses = new LinkedList<>();
		for (Class c : this.treeNavigator.getClasses())
			if (c.parents().size() == 0)
				mainClasses.add(c);
		for (Class c : mainClasses)
			myfile.DealNode(c, !myfile.serial);
		
		// Deal the main nodes
//		List<Node> mainNodes = myprocessor.getMainNodes();
//		for (Node n : mainNodes)
//		{
//			myfile.DealNode(n, !myfile.serial);
//		}
		
		// Deal the DataTypes
		for (PackageableElement pe : p.getPackagedElement())
		{			
			if (pe instanceof DataType)
			{
				myfile.DealDataType((DataType) pe);
			}
		}
		
		// Deal Association Roles
		for (Map.Entry<String, Classifier> entry : myprocessor.getAssociationRoles().entrySet())
		{
			myfile.DealAssociationRole(entry.getKey(), entry.getValue());
		}

	}
}
