package br.ufes.inf.nemo.ontouml2sbvr.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import RefOntoUML.AntiRigidMixinClass;
import RefOntoUML.AntiRigidSortalClass;
import RefOntoUML.Association;
import RefOntoUML.Characterization;
import RefOntoUML.Class;
import RefOntoUML.Classifier;
import RefOntoUML.DataType;
import RefOntoUML.DependencyRelationship;
import RefOntoUML.FormalAssociation;
import RefOntoUML.GeneralizationSet;
import RefOntoUML.MaterialAssociation;
import RefOntoUML.Mediation;
import RefOntoUML.Meronymic;
import RefOntoUML.Property;
import RefOntoUML.RefOntoUMLPackage;
import RefOntoUML.SemiRigidMixinClass;
import RefOntoUML.SubstanceSortal;
import RefOntoUML.componentOf;
import RefOntoUML.memberOf;
import RefOntoUML.subCollectionOf;
import RefOntoUML.subQuantityOf;

public class FileManager
{
	Writer output;
	HtmlHelper myhelper;
	
	boolean serial;
	LinkedList<Node> done;
	
	public FileManager (File sourceFile)
	{
		myhelper = new HtmlHelper();
		
		try
		{
			String name = sourceFile.getName().replace(".refontouml", "");
			
			File f1 = new File(sourceFile.getAbsolutePath());
												
			// Create a folder
			File folder = new File(f1.getParent());
						
			// Create the html file
			File f2 = new File(folder.getAbsolutePath() + File.separatorChar + name + ".html");
			//f2.deleteOnExit();
			
			output = new BufferedWriter(new FileWriter(f2));
			output.write(myhelper.StartDocument("SBVR Documentation: " + name));
						
			// Copy the show and hide images
			try
			{
				CopyImage (folder.getAbsolutePath(), "show.png");
				CopyImage (folder.getAbsolutePath(), "hide.png");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		serial = false;
		done = new LinkedList<Node>();
	}
	
	private void CopyImage (String path, String imgname) throws IOException
	{

		File myimgcpy = new File(path + File.separatorChar + imgname);
		myimgcpy.deleteOnExit();
				
		InputStream in = getClass().getClassLoader().getResourceAsStream("resources/br/ufes/inf/nemo/ontouml2sbvr/core/" + imgname);
		OutputStream out = new FileOutputStream(myimgcpy);
		
		byte[] buf = new byte[1024];
		int len;
		
		while ((len = in.read(buf)) > 0)
		{
			out.write(buf, 0, len);
		}
		
		in.close();
		out.close();
		
		in = null;
		out = null;
		
	}
	
	private LinkedList<String> getGeneralConcept (Class c)
	{
		LinkedList<String> parents = new LinkedList<String>();
		
		if (c.parents().size() > 0)
		{
			for (Iterator<Classifier> it = c.parents().iterator(); it.hasNext();)
			{
				parents.add(it.next().getName());
			}
			
			return parents;
		}
		
		return null;
	}
	
	private String getAssociationName (Association a)
	{
		if (a instanceof FormalAssociation || a instanceof MaterialAssociation)
		{
			return a.getName();
		}
		else if (a instanceof Mediation)
		{
			return "mediates";
		}
		else if (a instanceof Characterization)
		{
			return "characterizes";
		}
		else if (a instanceof Meronymic)
		{
			if (a instanceof componentOf)
			{
				return "is component of";
			}
			else if (a instanceof subCollectionOf)
			{
				return "is sub collection of";
			}
			else if (a instanceof subQuantityOf)
			{
				return "is sub quantity of";
			}
			else if (a instanceof memberOf)
			{
				return "is member of";	
			}
		}
		
		if (a.getName() == "" || a.getName() == null)
			return "has";
				
		return a.getName();
	}
	
	private String getRelataName (Property p)
	{		
		if ((p.getName() != null) && (p.getName().length() != 0))
		{
			return p.getName();
		}
		else
			return p.getType().getName();		
	}
	
	private String getAssociationConcept (Association a)
	{	
		if (a instanceof MaterialAssociation || a instanceof FormalAssociation || a instanceof Mediation)
		{
			return "associative fact type";
		}
		else if (a instanceof Characterization)
		{
			return "is-property-of fact type";
		}
		else if (a instanceof Meronymic)
		{
			return "partitive fact type";
		}

		return "is-property-of fact type";				
	}
	
	private boolean isAttributeAssociation (Association a)
	{
		return !(a instanceof MaterialAssociation || a instanceof FormalAssociation || a instanceof DependencyRelationship || a instanceof Meronymic);
	}
		
	private String getClassConcept (Classifier c)
	{
		if (c instanceof SubstanceSortal)
		{
			return "fundamental concept";
		}
		else if (c instanceof AntiRigidSortalClass ||
				 c instanceof AntiRigidMixinClass ||
				 c instanceof SemiRigidMixinClass)
		{
			return "role";
		}
		else
		{
			return "object type";
		}
	}
	
	private void DealChildPartition (Node n, ChildPartition cp, boolean hasNext)
	{
		// categorization scheme
		DealCategorization(cp.getGS());
		
		// Children
		LinkedList<Node> specifics = cp.getChildren();
		
		for (Iterator<Node> itc = specifics.iterator(); itc.hasNext();)
		{
			Node child = itc.next();
			DealNode(child, (itc.hasNext() || hasNext) && !serial);
		}
	}
	
	private void collapsibleSection (Node n)
	{
		try
		{
			if (!serial)
			{
				output.write(myhelper.StartCollapsibleSection(n.getRelatedClass().getName()));
			
				// Partitions
				for (Iterator<ChildPartition> itp = n.getChildPartitions().iterator(); itp.hasNext();)
				{
					DealChildPartition (n, itp.next(), itp.hasNext() || n.hasSChildren() || n.hasAssociations());
				}
				
				// Solitary Children
				for (Iterator<Node> it = n.getSChildren().iterator(); it.hasNext();)
				{
					Node child = it.next();
					DealNode(child, it.hasNext() || n.hasAssociations());	
				}
				
				// Associations
				for (Iterator<Association> it = n.getAssociations().iterator(); it.hasNext();)
				{
					Association a = it.next();
					DealAssociation(a, it.hasNext());
				}
			
				output.write(myhelper.EndCollapsibleSection());
			}
			else
			{
				// Owned Associations
				for (Iterator<Association> it = n.getOwnedAssociations().iterator(); it.hasNext();)
				{
					Association a = it.next();
					DealAssociation(a, true);
				}
				
				// Partitions
				for (Iterator<ChildPartition> itp = n.getChildPartitions().iterator(); itp.hasNext();)
				{
					DealChildPartition (n, itp.next(), false);
				}
				
				// Solitary Children
				for (Iterator<Node> it = n.getSChildren().iterator(); it.hasNext();)
				{
					Node child = it.next();
					DealNode(child, false);	
				}			
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void dealNodeBasic (Node n)
	{
		try
		{			
			Class c = n.getRelatedClass();
			boolean toggle = n.hasToggle();
			
			// Noun concept name
			output.write(myhelper.NounConcept(c.getName(), toggle && !serial, serial));
						
			// General Concept
			LinkedList<String> parents = getGeneralConcept(c);
			if (parents != null)
				output.write(myhelper.GeneralConcept(parents, serial));
			
			// Concept Type
			output.write(myhelper.ConceptType(getClassConcept(c)));
			
			createDescription(n);
			
			// Generalization Sets (as Specific)
			LinkedList<String> gsets = RefOntoUMLUtil.IncludedInCs(c);
			if (gsets != null)
			{
				output.write(myhelper.IncludedInCs(c.getName(), gsets));
			}
			
			if (serial) SectionBreaker();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Create an expressive description for a given {@code Node} instance, 
	 * containing reference to its specializations and relations it is involved in, appending
	 * it to the final HTML file.
	 * 
	 * @param node the {@link Node} instance to be described.
	 * @throws IOException if file writing doesn't end successfully.
	 * @author petrux 
	 * @since 10 July 2014
	 */
	//TODO: CREATE DESCRIPTION
	private void createDescription(Node node) throws IOException {
		String superClassName = node.getRelatedClass().getName();
		StringBuilder descriptionBuilder = new StringBuilder();
		
		//Child partitions
		List<ChildPartition> partitions = node.getChildPartitions();
		if (partitions != null && partitions.size() > 0) {
			for (ChildPartition partition : partitions) {
				GeneralizationSet gs = partition.getGS();
				String gsName = RefOntoUMLUtil.getGSetName(gs);
				boolean isDisjoint = gs.isIsDisjoint();
				boolean isComplete = gs.isIsCovering();
				LinkedList<String> sNames = RefOntoUMLUtil.getGSetSpecificsName(gs);
				
				/*
				 * According to the 
				 * [categorization scheme|segmentation] CS, 
				 * a SUPERCLASS
				 * 1. {}: may be one of
				 * 2. {disjoint}: may be at most one of
				 * 3. {complete}: must be at least one of
				 * 4. {disjoint, complete}: must be exactly one of
				 * a SUBCLASS_1 or a SUBCLASS_2 or... a SUBCLASS_N
				 */
				
				/*
				 * check if we are dealing with a segmentation 
				 * (i.e. a {disjoint, complete} generalization
				 * set) or not.
				 */
				String categorizationType = (isDisjoint && isComplete) 
						? "segmentation" 
						: "categorization scheme"; 
				
				/*
				 * Verbalize the subclassing according to the (eventual)
				 * disjointness and/or completeness of the generalization set 
				 */
				String being = " may be one of ";
				if (isDisjoint && isComplete) being = " must be exactly one of ";
				if (isDisjoint && !isComplete) being = " may be at most one of ";
				if (isComplete && !isDisjoint) being = " must be at least one of ";
				
				/*
				 * Translate the list of subclass names into a list 
				 * of alternatives, according to the proper SBVR
				 * typographical conventions.
				 */
				boolean isFirst = true;
				StringBuilder subclassListBuilder = new StringBuilder();
				for (String subclassName : sNames) {
					if (isFirst) {
						subclassListBuilder.append(myhelper.Text("a "));
						isFirst = false;
					}
					else {
						subclassListBuilder.append(myhelper.Text(" or a "));
					}
					subclassListBuilder.append(myhelper.Term(subclassName));
				}
				
				descriptionBuilder.append(myhelper.Text("According to the "));
				descriptionBuilder.append(myhelper.Text(categorizationType));
				descriptionBuilder.append(myhelper.Text(" "));
				descriptionBuilder.append(myhelper.individual(gsName));
				descriptionBuilder.append(myhelper.Text(" a "));
				descriptionBuilder.append(myhelper.Term(superClassName));
				descriptionBuilder.append(myhelper.Text(being));
				descriptionBuilder.append(subclassListBuilder.toString());
				descriptionBuilder.append(myhelper.Text(".")); 
				descriptionBuilder.append(myhelper.lineBreak());
			}
		}
		
		//Solitary children.
		List<Node> solitaryChildren = node.getSChildren();
		if (solitaryChildren != null && solitaryChildren.size() > 0) {
			for (Node child : solitaryChildren) {
				descriptionBuilder.append(myhelper.Text("A ")); 	
				descriptionBuilder.append(myhelper.Term(superClassName)); 
				descriptionBuilder.append(myhelper.Text(" may be a ")); 
				descriptionBuilder.append(myhelper.Term(child.getRelatedClass().getName())); 
				descriptionBuilder.append(myhelper.Text(".")); 
				descriptionBuilder.append(myhelper.lineBreak());
			}
		}
		
		String description = descriptionBuilder.toString();
		if (description.length() > 0)
			output.write(myhelper.Description(description));
	}
	
	public void DealNode (Node n, boolean sectionbreak)
	{
		/*
		 * If the 'serial' flag has been set and the node
		 * has already been processed, then skip it and 
		 * return, otherwise add to the list of the processed
		 * nodes and go on.
		 */
		if (serial)
		{
			if (done.contains(n))
				return;
			done.add(n);
		}
		
		try
		{
			/*
			 * Get the Class instance related to the
			 * argument Node and if the 'serial' flag
			 * is unset, create a section starter tag. 
			 */
			Class c = n.getRelatedClass();
			if (!serial) 
				output.write(
						myhelper.StartSection(
								c.getName()));
			
			/*
			 * First add the content for the Class
			 * associated to the node, then if the Node
			 * hasToggle() (i.e. has children or associations)
			 * create also a collapsible section.
			 */
			dealNodeBasic(n);
			if (n.hasToggle()) 
				collapsibleSection(n);
			
			/*
			 * If the 'serial' flag is unset, close
			 * the current section with a section
			 * ending tag. If the 'sectionbreak' argument
			 * is set to true, add a section break tag.
			 */
			if (!serial) 
				output.write(
						myhelper.EndSection());
			if (sectionbreak) 
				SectionBreaker();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void DealAssociation (Association a, boolean sectionbreak)
	{
		Property p1 = a.getMemberEnd().get(0);
		Property p2 = a.getMemberEnd().get(1);
		String relata1 = getRelataName(p1);
		String relata2 = getRelataName(p2);
		String assocName = getAssociationName(a);
		String conceptType = getAssociationConcept(a);
		boolean datatypeAttribute = isAttributeAssociation(a);
		boolean reverse = a instanceof Meronymic;
		Property p;
						
		try
		{
			// Verb Concept
			output.write(myhelper.VerbConcept(relata1, assocName, relata2, reverse, serial));
			// Concept Type
			output.write(myhelper.ConceptType(conceptType));

			// Necessity 1
			p = reverse ? p1 : p2;
			output.write(myhelper.AssociationNecessity(relata1, assocName, relata2, p.getLower(), p.getUpper(), reverse));
			
			// Necessity 2
			if (!datatypeAttribute)
			{
				reverse = ! reverse;
				p = reverse ? p1 : p2;									
				output.write(myhelper.AssociationNecessity(relata1, "is related to ", relata2, p.getLower(), p.getUpper(), reverse));
			}

			// Section Breaker
			if (sectionbreak) SectionBreaker();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	// TODO: enum
	public void DealDataType (DataType dt)
	{
		try
		{
			// Noun concept name
			output.write(myhelper.NounConcept(dt.getName(), false, serial));
									
			// Concept Type
			output.write(myhelper.ConceptType(getClassConcept(dt)));
						
			// Section Breaker
			SectionBreaker();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void DealAssociationRole (String role, Classifier c)
	{
		try
		{
			// Noun concept (role name)
			output.write(myhelper.NounConcept(role, false, serial));
			
			// General Concept
			LinkedList<String> parents = new LinkedList<String>();
			parents.add(c.getName());
			output.write(myhelper.GeneralConcept(parents, serial));
			
			// Concept Type
			output.write(myhelper.ConceptType("role"));
									
			// Section Breaker
			SectionBreaker();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void DealCategorization (GeneralizationSet gs)
	{
		try
		{
			String gsname = RefOntoUMLUtil.getGSetName(gs);
			
			// Categorization Scheme Name
			output.write(myhelper.CategorizationScheme(gsname));
			
			// Definition
			String concept;
			if (gs.isIsDisjoint() && gs.isIsCovering())
			{
				concept = "segmentation";
			}
			else
			{
				concept = "categorization scheme";
			}
			output.write(myhelper.CSDefinition(concept, RefOntoUMLUtil.getGSetGeneral(gs).getName()));
			
			// Necessity
			output.write(myhelper.CSNecessity(gsname, RefOntoUMLUtil.getGSetSpecificsName(gs)));
			
			// Section Breaker
			SectionBreaker();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
		
	private void SectionBreaker()
	{
		try
		{
			output.write(myhelper.SectionBreaker());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}	
	}
	
	public void Done ()
	{
		try
		{
			output.write(myhelper.EndDocument());
			output.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
