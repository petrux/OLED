package br.ufes.inf.nemo.common.ontoumlparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;

import RefOntoUML.AggregationKind;
import RefOntoUML.AntiRigidSortalClass;
import RefOntoUML.Association;
import RefOntoUML.Category;
import RefOntoUML.Characterization;
import RefOntoUML.Class;
import RefOntoUML.Classifier;
import RefOntoUML.Collective;
import RefOntoUML.Comment;
import RefOntoUML.Constraintx;
import RefOntoUML.DataType;
import RefOntoUML.Derivation;
import RefOntoUML.Element;
import RefOntoUML.Enumeration;
import RefOntoUML.EnumerationLiteral;
import RefOntoUML.Generalization;
import RefOntoUML.GeneralizationSet;
import RefOntoUML.Kind;
import RefOntoUML.MaterialAssociation;
import RefOntoUML.Mediation;
import RefOntoUML.Meronymic;
import RefOntoUML.MixinClass;
import RefOntoUML.Mode;
import RefOntoUML.MomentClass;
import RefOntoUML.NamedElement;
import RefOntoUML.Package;
import RefOntoUML.PackageableElement;
import RefOntoUML.Phase;
import RefOntoUML.PrimitiveType;
import RefOntoUML.Property;
import RefOntoUML.Quantity;
import RefOntoUML.Relationship;
import RefOntoUML.Relator;
import RefOntoUML.RigidSortalClass;
import RefOntoUML.Role;
import RefOntoUML.RoleMixin;
import RefOntoUML.SubKind;
import RefOntoUML.SubstanceSortal;
import RefOntoUML.Type;
import br.ufes.inf.nemo.common.resource.ResourceUtil;

/** 
 * This class represents a parser for analyzing and keeping useful informations about the ontoUML model. 
 * It associates an alias for every Element, as well as if
 * this Element is selected or not (useful for transformation purposes).
 * 
 * @author John Guerson
 * @author Thiago Sales
 * @author Vinicius Sobral
 *
 */
public class OntoUMLParser {

	/** Root Package of OntoUML Model. */
	private Package model;	
	
	/** Unique Name of Root Package. */
	private String refmodelname;	
	
	/** 
	 *  HashMap containing Every Element of the model associating with the custom parsing element. The parsing Element contains all 
	 *  the useful information about the OntoUML Element. 
	 */
	private HashMap<EObject,ParsingElement> elementsHash;
	private NameHandler nameHandler = new NameHandler();
	
	/** Options for complete element selections in the model (transformation purposes). */
	public static int NO_HIERARCHY = 0, SORTAL_ANCESTORS = 1, ALL_ANCESTORS = 2, ALL_DESCENDANTS = 3, COMPLETE_HIERARCHY = 4;		
	
	/**
	 * This constructor creates a parser from a root ontoUML Package.
	 * 
	 * @param refmodel
	 */
	public OntoUMLParser(RefOntoUML.Package refmodel)
	{
		this.model = refmodel;	
		
		elementsHash = new HashMap<EObject,ParsingElement>();	
		
		NameHandler h1 = new NameHandler();
		this.refmodelname = h1.treatName(model);	
				
		initMap(model, nameHandler);
	}
		
	/**
	 * This constructor creates a parser from a absolute ontoUML file in file system.
	 * 
	 * @param refontoumlPath
	 * @throws IOException
	 */
	public OntoUMLParser(String refontoumlPath) throws IOException
	{
		Resource resource = ResourceUtil.loadReferenceOntoUML(refontoumlPath);
		Package refmodel = (Package)resource.getContents().get(0);	
		
		this.model = refmodel;		
		
		elementsHash = new HashMap<EObject,ParsingElement>();	
		
		NameHandler h1 = new NameHandler();
		this.refmodelname = h1.treatName(model);
				
		initMap(model, nameHandler);
	}	
	
	/**
	 * Add a new Element to this Parser. 
	 * But note that this new element must already have been added in the Model (i.e., in the root Package or file in which this parser was created). 
	 * Also, this method do not verify dependences in addition. If the argument is a class, than it will add
	 * all its attributes, generalization and comments, and so on for associations,  etc.
	 * 
	 */
	public void addElement(EObject obj)
	{
		// only add if it is not there already
		if (this.elementsHash.get(obj)==null)
		{			
			if (obj instanceof RefOntoUML.Comment){
				ParsingElement e = new ParsingElement(obj,true,"");
				this.elementsHash.put(obj,e);
			
			}else if (obj instanceof RefOntoUML.Generalization){
				
				ParsingElement e = new ParsingElement(obj,true,"");
				this.elementsHash.put(obj,e);
				
			}else if (obj instanceof RefOntoUML.Property){
				ParsingElement e = new ParsingElement(obj,true,nameHandler.treatName((NamedElement)obj));
				this.elementsHash.put(obj,e);
				
			}else if (obj instanceof PackageableElement){
				addToMap((PackageableElement)obj, nameHandler);
			}
		}				
	}

	/**
	 * Remove an Element from this Parser. 
	 * But note that this element must already have been removed from the Model (i.e., in the root Package or file in which this parser was created).
	 * Also, this method do not verify dependences in deletion. If the argument is a class, than it will delete
	 * all its attributes, generalization and comments, and so on for associations,  etc.
	 */
	public void removeElement(EObject obj)
	{	
		// Comments
		if (obj instanceof PackageableElement) 
		{ 
			for (Comment c: ((PackageableElement)obj).getOwnedComment()) removeFromMap(c); 
		} 
			
		// Constraintx
		if (obj instanceof Constraintx) 
		{ 
			removeFromMap((Constraintx)obj); 
		} 
		
		// Class and DataType
		if (obj instanceof RefOntoUML.Class || ((obj instanceof DataType)&&!(obj instanceof PrimitiveType)&&!(obj instanceof Enumeration)))
		{			
			//Generalization
			for (Generalization g : ((Classifier)obj).getGeneralization()) removeFromMap(g);
			//Attributes
			if (obj instanceof Class){ for(Property p: ((Class)obj).getOwnedAttribute()) removeFromMap(p); }
			if (obj instanceof DataType){ for(Property p: ((DataType)obj).getOwnedAttribute()) removeFromMap(p); }
			removeFromMap(obj);
		}		
		//Association
		else if(obj instanceof Association)
		{
			removeFromMap(obj);					
			//Properties			
			for(Property p: ((Association)obj).getMemberEnd()) { removeFromMap(p); }
			//Generalization
			for (Generalization g : ((Classifier)obj).getGeneralization()) removeFromMap(g);
		}		
		//Enumeration
		else if (obj instanceof Enumeration)
		{						
			//Enumeration Literals
			for(EnumerationLiteral p: ((Enumeration)obj).getOwnedLiteral()) removeFromMap(p);			
			//Enumeration can also have attributes
			for(Property p: ((Enumeration)obj).getOwnedAttribute()) removeFromMap(p);
			removeFromMap(obj);
		}else{
			removeFromMap(obj);
		}
	}
	
	private void removeFromMap(EObject obj)
	{
		ParsingElement e = elementsHash.get(obj);
		if (e!=null) {
			nameHandler.remove(e.getAlias());
			this.elementsHash.remove(obj);			
		}	
	}
	
	public void updateElement(EObject obj)
	{
		ParsingElement e = elementsHash.get(obj);
		if (e!=null) {
			nameHandler.remove(e.getAlias());		
			String alias = nameHandler.treatName((NamedElement)obj);
			e.setAlias(alias);
		}
	}
	
	/**
	 * Get Root Model Package.
	 */
	public RefOntoUML.Package getModel()
	{
		return model;
	}
	
	/**
	 * This private method initialize the HashMap used for keeping the mappings between
	 * ontoUML model elements.
	 * 
	 * @param rootpack
	 * @param h2
	 */
	private void initMap (PackageableElement rootpack, NameHandler h2) 
	{
		ParsingElement e = new ParsingElement(rootpack, true, h2.treatName(rootpack));
		this.elementsHash.put(rootpack,e);
		
		for(PackageableElement p : ((Package) rootpack).getPackagedElement())
		{
			addToMap(p, h2);
			
			if(p instanceof Package) initMap(p, h2);
		}
	}		

	/**
	 * This private method add a Element to the HashMap. It associate an ontoUML Element
	 * with an unique alias and by default with boolean value selected=true.
	 *
	 * @param pe
	 * @param h2
	 */
	private void addToMap(PackageableElement pe, NameHandler h2)
	{
		ParsingElement e;
		
		//Comments
		for (Comment c: pe.getOwnedComment()) 
		{
			e = new ParsingElement(c, true, "");
			this.elementsHash.put(c,e);
		}
		
		//Constraintx
		if(pe instanceof Constraintx)
		{
			e = new ParsingElement(pe, true, h2.treatName(pe));
			this.elementsHash.put(pe,e);
		}
		
		//Class and DataType
		if(pe instanceof Class || ((pe instanceof DataType)&&!(pe instanceof PrimitiveType)&&!(pe instanceof Enumeration)) )
		{
			e = new ParsingElement(pe, true, h2.treatName(pe));
			this.elementsHash.put(pe,e);
			
			//Generalization
			for (Generalization g : ((Classifier)pe).getGeneralization()) 
			{
				e = new ParsingElement(g, true, "");
				this.elementsHash.put(g,e);
			}
			
			if (pe instanceof Class){			
				//Attributes
				for(Property p: ((Class)pe).getOwnedAttribute())
				{				
					e = new ParsingElement(p, true, h2.treatName(p));
					this.elementsHash.put(p,e);				
				}
			}
			if (pe instanceof DataType){			
				//Attributes
				for(Property p: ((DataType)pe).getOwnedAttribute())
				{				
					e = new ParsingElement(p, true, h2.treatName(p));
					this.elementsHash.put(p,e);				
				}
			}
		}
		
		//Association
		else if(pe instanceof Association)
		{
			e = new ParsingElement(pe, true, h2.treatName(pe));
			this.elementsHash.put(pe,e);
			
			//Properties			
			if(((Association)pe).getMemberEnd().size()>=1)
			{
				Property property0 = ((Association)pe).getMemberEnd().get(0);							
				e = new ParsingElement(property0, true, h2.treatName(property0));
				this.elementsHash.put(property0,e);
			}			
			if(((Association)pe).getMemberEnd().size()>=2)
			{
				Property property1 = ((Association)pe).getMemberEnd().get(1);
				e = new ParsingElement(property1, true, h2.treatName(property1));
				this.elementsHash.put(property1,e);
			}
			
			//Generalization
			for (Generalization g : ((Classifier)pe).getGeneralization()) 
			{
				e = new ParsingElement(g, true, "");
				this.elementsHash.put(g,e);
			}							
		}
		
		//Enumeration
		else if (pe instanceof Enumeration)
		{
			e = new ParsingElement(pe, true, h2.treatName(pe));
			this.elementsHash.put(pe,e);
			
			//Enumeration Literals
			for(EnumerationLiteral p: ((Enumeration)pe).getOwnedLiteral())
			{
				e = new ParsingElement(p, true, h2.treatName(p));
				this.elementsHash.put(p,e);
			}
			//Enumeration can also have attributes
			for(Property p: ((Enumeration)pe).getAttribute())
			{
				e = new ParsingElement(p, true, h2.treatName(p));
				this.elementsHash.put(p,e);
			}
			
		}else {
			e = new ParsingElement(pe, true, h2.treatName(pe));
			this.elementsHash.put(pe,e);			
		}
		
	}
	
	/**
	 * Get OntoUML Element from a alias name.
	 * 
	 * @param alias
	 * @return
	 */
	public EObject getElement(String alias)
	{	 
		for (Entry<EObject,ParsingElement> entry : elementsHash.entrySet()) 
        {
            String name = ((ParsingElement)entry.getValue()).getAlias(); 
            if (alias.equals(name)) return entry.getKey();            
        }
        return null;	    
	}
	
	/**
	 * This method gets the alias of a given ontoUML Element.
	 * 
	 * @param elem
	 * @return
	 */
	public String getAlias(EObject elem) 
	{
		return elementsHash.get(elem).getAlias();
	}
	
	/**
	 * This method gets the String representation of a given ontoUML Element.
	 * 
	 * @param elem
	 * @return
	 */
	public String getStringRepresentation(EObject elem)
	{
		ParsingElement parsingElem = elementsHash.get(elem);
		
		if (parsingElem == null)
			return "Unknown Element";
		else return parsingElem.toString();
	}

	public String getStereotype(EObject elem)
	{
		ParsingElement parsingElem = elementsHash.get(elem);
		
		if (parsingElem == null)
			return "Unknown Element";
		else return parsingElem.getType();
	}
	
	@Override
	public String toString()
	{
		String result = new String();
		
		for(EObject obj: getElements())
		{
			result += elementsHash.get(obj)+"\n";
		}
		return result;
	}
	
	/**
	 * Get String Representation of All Elements in the model.
	 * 
	 * @return
	 */
	public String getStringRepresentations()
	{
		String result = new String();
	
		for (EObject obj: getElements()) { result+= getStringRepresentation(obj)+"\n"; }
		
		return result;		
	}
	
	/**
	 * Get Aliases of a List of OntoUML Elements.
	 * 
	 * @param list
	 * @return
	 */
	public ArrayList<String> getAlias (ArrayList<EObject> list)
	{
		ArrayList<String> result = new ArrayList<String>();
		for(EObject obj: getElements())
		{
			if (list.contains(obj)) result.add(getAlias(obj));
		}
		return result;
	}
	
	/**
	 * Get String Representation of a List of OntoUML Elements.
	 * 
	 * @return
	 */
	public String getStringRepresentations(ArrayList<EObject> list)
	{
		String result = new String();
	
		for (EObject obj: getElements()) { if (list.contains(obj)) result+= getStringRepresentation(obj)+"\n"; }
		
		return result;		
	}
	
	public ArrayList<ParsingElement> getParsingElements(ArrayList<Element> list)
	{
		ArrayList<ParsingElement> result = new ArrayList<ParsingElement>();
		for(EObject eobj: list){
			if (elementsHash.get(eobj)!=null){
				result.add(elementsHash.get(eobj));
			}
		}
		return result;
	}
	
	/**
	 * Verifies if a given OntoUML Element is selected or not.
	 * 
	 * @param elem
	 * @return
	 */
	public Boolean isSelected (EObject elem) 
	{		
		if (elem!=null){
			if (elementsHash.get(elem)!=null)
				return elementsHash.get(elem).getSelected();
			else{
				try {
					throw new Exception("Element not contained in OntoUML parser.");
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}
		}else{
			return false;
		}
	}
	
	/**
	 * Get Unique Name of OntoUML Root Package.
	 *  
	 * @return
	 */
	public String getModelName()
	{
		return refmodelname;
	}	
		
	/**
	 * Get OntoUML Elements of the Model.
	 * @return
	 */
	public Set<EObject> getElements()
	{
		Set<EObject> list = new HashSet<EObject>(); 
		
		for (ParsingElement pe : elementsHash.values()) 
		{
			if(pe.getSelected()) { list.add(pe.getElement()); }
		}
		
		return list;
	}
	
	/**
	 * Select this elements in the model. If 'unselectOthers' is true, the other elements are unselected (i.e. selected=false). 
	 * Otherwise, if 'unselectOther' is false, nothing is made with the others elements, they maybe selected or not. i.e. selected = true or false.
	 * 
	 * @param list
	 */
	public void selectThisElements(ArrayList<EObject> selected, boolean unselectOthers)
	{
		for (ParsingElement pe : elementsHash.values()) 
		{
			if(selected.contains(pe.getElement())) pe.setSelected(true);
			else if (unselectOthers) pe.setSelected(false);
		}
	}
	
	public void unselectThisElements(ArrayList<EObject> unselected)
	{
		for (ParsingElement pe : elementsHash.values()) 
		{
			if(unselected.contains(pe.getElement())) pe.setSelected(false);
		}		
	}
	
	/**
	 * This method selects all Elements of the model.
	 */
	public void selectAllElements()
	{
		for (ParsingElement pe : elementsHash.values()) 
		{
			pe.setSelected(true);
		}
	}
	
	/**
	 * This method returns all elements from the input list that are selected in the parser.
	 * 
	 * @param elements: list of elements of any type.
	 */
	public <T> Set<T> retainSelected(List<T> elements)
	{
		Set<T> result = new HashSet<T>();		
		for (T elem : elements) 
		{
			if(elem instanceof EObject && isSelected((EObject) elem)) result.add(elem);
		}		
		return result;
	}
			
	/**
	 * This method gets rigid Classes of the model.
	 * 
	 * @return
	 */
	public Set<Classifier> getRigidClasses()
	{
		Set<Classifier> list = new HashSet<Classifier>();		
		for (EObject obj : getElements())
		{			
			if (
				(obj instanceof RigidSortalClass) || (obj instanceof Category) || 
				(obj instanceof MomentClass) || ((obj instanceof DataType)&&!(obj instanceof PrimitiveType))
			)				
			list.add((Classifier) obj);			
		}		
		return list;
	}
			
	/**
	 * This method gets anti rigid Classes of the model.
	 * 
	 * @return
	 */
	public Set<Classifier> getAntiRigidClasses()
	{
		Set<Classifier> list = new HashSet<Classifier>();		
		for (EObject obj : getElements())
		{
			if((obj instanceof Role) || (obj instanceof RoleMixin) || (obj instanceof Phase))				
			list.add((Class)obj);			
		}		
		return list;
	}
	
	public Set<Property> getAttributes()
	{
		Set<Property> list = new HashSet<Property>();		
		for (EObject obj : getElements())
		{			
			if( (obj instanceof Property) && ((Property)obj).getAssociation()==null )				
				list.add((Property)obj);			
		}		
		return list;
	}
		
	/**
	 * Get all descendants (direct or indirect) of Classifier c.
	 * 
	 * @param c
	 */
	public Set<Classifier> getAllChildren(Classifier c)
	{
		Set<Classifier> result = new HashSet<Classifier>();		
		for (Classifier classifier : c.allChildren()) 
		{
			if(isSelected(classifier)) result.add(classifier);
		}		
		return result;
	}
	
	/**
	 * Get all direct descendants of Classifier c.
	 * 
	 * @param c
	 */
	public Set<Classifier> getChildren(Classifier c)
	{
		Set<Classifier> result = new HashSet<Classifier>();		
		for (Classifier classifier : c.children()) 
		{
			if(isSelected(classifier)) result.add(classifier);
		}		
		return result;
	} 
	
	/**
	 * Get all ancestors (direct or indirect) of Classifier c.
	 * 
	 * @param c
	 */
	public Set<Classifier> getAllParents(Classifier c)
	{
		Set<Classifier> result = new HashSet<Classifier>();		
		for (Classifier classifier : c.allParents()) 
		{
			if(isSelected(classifier)) result.add(classifier);
		}		
		return result;
	}
	
	/**
	 * Get all direct ancestors of Classifier c.
	 * 
	 * @param c
	 */
	public Set<Classifier> getParents(Classifier c)
	{
		Set<Classifier> result = new HashSet<Classifier>();		
		for (Classifier classifier : c.parents()) 
		{
			if(isSelected(classifier)) result.add(classifier);
		}		
		return result;
	} 
	
	/**
	 * Get all non-abstract descendants of Classifier c.
	 * 
	 * @param c
	 */
		
	public Set<Classifier> getAllConcreteChildren(Classifier c)
	{
		Set<Classifier> result = new HashSet<Classifier>();		
		for (Classifier classifier : getAllChildren(c)) 
		{
			if(isSelected(classifier)) result.add(classifier);
		}
		return result;
	}
	
	/**
	 * Get all instances of a given class in the OntoUMLParser 
	 * 
	 * @param type: class from the OntoUML metamodel. use �class_name�.class
	 * @return all instances of the type in the parameter 
	 */
	@SuppressWarnings("unchecked")
	public <T> Set<T> getAllInstances(java.lang.Class<T> type)
	{
		Set<T> result = new HashSet<T>();		
		for (EObject o : getElements()) 
		{
			if(isSelected(o) && type.isInstance(o)) result.add((T) o);
		}
		return result;
	}
	
	/**
	 * Get Top Level Instances of the Model.
	 * 
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> Set<T> getTopLevelInstances(java.lang.Class<T> type)
	{
		Set<T> result = new HashSet<T>();		
		for (EObject o : getElements()) 
		{
			if(isSelected(o) && type.isInstance(o))
			{
				if(o instanceof Classifier && ((Classifier)o).getGeneralization().size()==0)
				{
					if(o instanceof DataType && ! (o instanceof PrimitiveType)) result.add((T) o);
					else if (!(o instanceof DataType)) result.add((T) o);					
				}					
			}
		}
		return result;
	}	
	
	/**
	 * Get all Meronymic relations that have as a Whole the Classifier 'c' or one of its Super Types.
	 * 
	 * @param c
	 * @param result
	 */
	public void getAllMeronymics(Classifier c, ArrayList<Meronymic> result)
	{
		for(EObject obj : getElements())
		{
			if(obj instanceof Meronymic)
			{
				for( Property p : ((Meronymic)obj).getMemberEnd())
				{
					if (!p.getAggregation().equals(AggregationKind.NONE))
					{					
						if (isSelected(p.getType()) && p.getType().equals(c))
						{
							result.add((Meronymic)obj);
						}						
					}
				}
			}
		}
		for(Generalization gen : c.getGeneralization())
		{
			if (isSelected(gen.getGeneral()) && !gen.getGeneral().equals(c)) getAllMeronymics(gen.getGeneral(),result);			
		}
	}
			
	/**
	 * Get all Mediations that have as a source the Relator 'r' or one of its Super Types.
	 * 
	 * @param relator
	 * @param result
	 * @throws Exception 
	 */
	public void getAllMediations(Classifier relator, ArrayList<Mediation> result)
	{
		result.addAll(getRelatorsMediations(relator));
		
		for(Generalization gen : relator.getGeneralization())
		{						
			if(isSelected(gen))	{
				if (gen.getGeneral() instanceof Relator) getAllMediations((Relator)gen.getGeneral(),result);
			}
		}
	}
	
	/**
	 * Get all Mediations that have as a source the Relator 'r' or one of its Super Types.
	 * 
	 * @param relator
	 * @param result
	 * @throws Exception 
	 */
	public ArrayList<Mediation> getRelatorsMediations(Classifier relator) 
	{
		ArrayList<Mediation> result = new ArrayList<Mediation>();
		
		for (Mediation m : getAllInstances(Mediation.class)) {
			if(getRelator(m)!=null && getRelator(m).equals(relator))
				result.add(m);
		}
		
		return result;
	}
	
	
	
	/**
	 * Verify if a Classifier 'c' is a General Classifier in a GeneralizationSet that is Disjoint and Complete
	 * Which means that this Classifier is an Abstract Classifier.
	 * 
	 * @param c
	 * @return
	 */
	public boolean isAbstractFromGeneralizationSet(Classifier c) 
	{
		for(GeneralizationSet gs : getAllInstances(GeneralizationSet.class))
		{			
			if(gs.isIsCovering())
			{
				for(Generalization gen : gs.getGeneralization())
				{
					if (isSelected(gen)) 
					{
						if (isSelected(gen.getGeneral()) && gen.getGeneral().equals(c)) return true;
					}
				}
			}
		}		
		return false;
	}
		 
	/**
	 * Auto Select Elements : Mandatory Dependencies.
	 * 
	 * In Generalizations, the general and specific classifiers must be selected. 
	 * In Associations, the source and target types must be selected.
	 * 
	 * @return
	 */
	private ArrayList<EObject> autoSelectMandatoryDependencies()
	{
		ArrayList<EObject> objectsToAdd = new ArrayList<EObject>();
		
		for (EObject obj : getElements()) 
		{
			if(obj instanceof Generalization)
			{
				Generalization g = (Generalization) obj;
				// general
				if (!isSelected(g.getGeneral()) && !objectsToAdd.contains(g.getGeneral()) ) 
				{
					objectsToAdd.add(g.getGeneral());										
				}
				//specific
				if (!isSelected(g.getSpecific()) && !objectsToAdd.contains(g.getSpecific()) ) 
				{
					objectsToAdd.add(g.getSpecific());					
				}
			}
			if(obj instanceof Association) 
			{
				Association a = (Association)obj;	
				if (a.getMemberEnd().size()>=1)
				{
					Type source = a.getMemberEnd().get(0).getType();
					//source
					if(!isSelected(source) && !objectsToAdd.contains(source))
					{
						objectsToAdd.add(source);					
					}
				}
				
				if (a.getMemberEnd().size()>=2)
				{				
					//	target
					Type target = a.getMemberEnd().get(1).getType();
					if(!isSelected(target) && !objectsToAdd.contains(target))
					{
						objectsToAdd.add(target);					
					}
				}
			}			
		}
		
		// add this elements to selection...
		selectThisElements(objectsToAdd,false);
		
		return objectsToAdd;
	}
	
	/**
	 * Auto Select Elements : Packages Dependencies.
	 *
	 * In PackageableElements or Packages their container must be selected. 
	 * 
	 * @return
	 */
	private ArrayList<EObject> autoSelectPackagesDependencies()
	{
		ArrayList<EObject> objectsToAdd = new ArrayList<EObject>();
		
		for (ParsingElement obj : elementsHash.values()) 
		{	
			if(obj.getElement() instanceof PackageableElement || obj.getElement() instanceof Package) 
			{
				// packages
				if((obj.getElement().eContainer()!=null) && !isSelected(obj.getElement().eContainer()) && !objectsToAdd.contains(obj.getElement().eContainer())) 
				{
					objectsToAdd.add(obj.getElement().eContainer());
				}
			}
		}
		
		// add this elements to selection...
		selectThisElements(objectsToAdd,false);
		
		return objectsToAdd;
	}
	
	/**
	 * Auto Select Elements : Generalization.
	 * 
	 * In Generalizations, their Generalizations Set must be selected.
	 * 
	 * @return
	 */
	private ArrayList<EObject> autoSelectGeneralizationSetFromGeneralization()
	{
		ArrayList<EObject> objectsToAdd = new ArrayList<EObject>();
		
		for (Generalization g : getAllInstances(Generalization.class))
		{
			for (GeneralizationSet gs : g.getGeneralizationSet()) 
	        {
	            if(!isSelected(gs) && !objectsToAdd.contains(gs))
	            {
	                objectsToAdd.add(gs);                                        
	            }
	        }
		}
		
		// add this elements to selection...
		selectThisElements(objectsToAdd,false);
		
		return objectsToAdd;
	}
	
	//Select all associations connected to the inputed classifiers
	public ArrayList<EObject> autoSelectRelatedElements(ArrayList<Classifier> classifiers)
	{
		ArrayList<EObject> objectsToAdd = new ArrayList<EObject>();
		
		for (Association a : getAllInstances(Association.class))
		{
			if (!isSelected(a))
			{
				//try clause added because the model may be inconsistent and generate exceptios.
				try {
					Type source = a.getMemberEnd().get(0).getType();
					Type target = a.getMemberEnd().get(1).getType();
					
					if (!isSelected(source) && !objectsToAdd.contains(source) && !classifiers.contains(source))
						objectsToAdd.add(source);
					if (!isSelected(target) && !objectsToAdd.contains(target) && !classifiers.contains(target))
						objectsToAdd.add(target);
				}
				//ignores association and unselect it.
				catch (Exception e) {
				
					this.elementsHash.get(a).setSelected(false);
				}
			}
		}
		
		// add this elements to selection...
		selectThisElements(objectsToAdd,false);
		
		return objectsToAdd;
	}
	
	
	/**
	 * Auto Select Elements: Generalization Set.
	 * 
	 * In Generalization Set, the Generalizations and the general and specific classifiers, must be selected.
	 * 
	 * @return
	 */
	private ArrayList<EObject> autoSelectGeneralizationSet() 
	{		
		ArrayList<EObject> objectsToAdd = new ArrayList<EObject>();
		
		for(EObject obj : getElements()){
			if(obj instanceof GeneralizationSet) 
			{
				GeneralizationSet gs = (GeneralizationSet)obj;				
				//generalizations
				for(Generalization g: gs.getGeneralization())
				{
					if(!isSelected(g))
					{
						objectsToAdd.add(g);						
					}
					//specific
					if (!isSelected(g.getSpecific()) && !objectsToAdd.contains(g.getSpecific())) 
					{
						objectsToAdd.add(g.getSpecific());						
					}
					//general
					if (!isSelected(g.getGeneral()) && !objectsToAdd.contains(g.getGeneral())) 
					{
						objectsToAdd.add(g.getGeneral());						
					}
				}								
			}
		}
		
		// add this elements to selection...
		selectThisElements(objectsToAdd,false);
		
		return objectsToAdd;
	}
	
	/**
	 * Auto Select Elements : Hierarchies.
	 * 
	 * @param option
	 * @return
	 */
	private ArrayList<EObject> autoSelectHierarchy(int option) 
	{
		ArrayList<EObject> objectsToAdd = new ArrayList<EObject>();
		
		if (option==NO_HIERARCHY) return objectsToAdd;

		for (EObject o : getElements()) 
		{
			if (o instanceof Class) 
			{
				if (option==SORTAL_ANCESTORS || option==ALL_ANCESTORS || option==COMPLETE_HIERARCHY)
					
					getAncestorsHierarchy((Classifier) o, objectsToAdd, option);
				
				if (option==ALL_DESCENDANTS || option==COMPLETE_HIERARCHY)
					
					getDescendantsHierarchy((Classifier) o, objectsToAdd);
			}
		}
		
		// add this elements to selection...
		selectThisElements(objectsToAdd,false);
	
		return objectsToAdd;
	}

	/**
	 * Get all the descendants hierarchy of a Classifier. i.e. Children and Generalizations.
	 * 
	 * @param o
	 * @param objectsToAdd
	 * @param option
	 */
	private void getDescendantsHierarchy (Classifier o, ArrayList<EObject> objectsToAdd){
		
		for (Classifier c : o.children()) 
		{
			if(!isSelected(c) && !objectsToAdd.contains(c))
				objectsToAdd.add(c);				
			
			for (Generalization g : c.getGeneralization()) 
			{
				if(g.getGeneral().equals(o) && !isSelected(g) && !objectsToAdd.contains(g)) 
					objectsToAdd.add(g);
			}
			getDescendantsHierarchy(c, objectsToAdd);
			
		}
	}
	
	/**
	 *Get all the ancestors hierarchy of a Classifier. i.e. Parents and Generalizations.
	 * 
	 * @param o
	 * @param objectsToAdd
	 * @param option
	 */
	private void getAncestorsHierarchy (Classifier o, ArrayList<EObject> objectsToAdd, int option){
		
		if(o instanceof SubstanceSortal && option==SORTAL_ANCESTORS) return;
		
		for (Generalization g : ((Class) o).getGeneralization()) 
		{
			if(!isSelected(g) && !objectsToAdd.contains(g))
				objectsToAdd.add(g);
			if(!isSelected(g.getGeneral()) && !objectsToAdd.contains(g.getGeneral()))
				objectsToAdd.add(g.getGeneral());
			
			getAncestorsHierarchy(g.getGeneral(), objectsToAdd,option);
		}
	}	
	
	/**
	 * Auto Select Elements :  Explicit dependencies through parameters.
	 * 
	 * @param hierarchyOption
	 * @param includeGeneralizationSet
	 * @return
	 */
	public ArrayList<EObject> autoSelectDependencies(int hierarchyOption, boolean includeGeneralizationSet)
	{		
		ArrayList<EObject> objectsAdded = new ArrayList<EObject>();
		
		objectsAdded.addAll(autoSelectMandatoryDependencies());		
		objectsAdded.addAll(autoSelectPackagesDependencies());						
		objectsAdded.addAll(autoSelectHierarchy(hierarchyOption));		
		objectsAdded.addAll(autoSelectGeneralizationSetFromGeneralization());
		
		if (includeGeneralizationSet) objectsAdded.addAll(autoSelectGeneralizationSet());
		
		return objectsAdded;
	}

	/**
	 * Create a new Package Model from the Selections.
	 * 
	 * @param log
	 * @param includeHierarchy
	 * @param copier
	 * @return
	 */
	public Package createPackageFromSelections (Copier copier)
	{		
		Package pack_copy;
		ArrayList<EObject> selected_copy = new ArrayList<EObject>();				
		pack_copy = (Package) copier.copy(model);
		copier.copyReferences();		
		for (EObject element : getElements()) 
		{
			selected_copy.add(copier.get(element));
		}		
		deleteElement(pack_copy, selected_copy);		
		return pack_copy;
	}
	
	/**
	 * Delete Elements of the Package that aren't selected.
	 * 
	 * @param pack
	 * @param selected
	 */
	private void deleteElement (Package pack, ArrayList<EObject> selected)
	{
		ArrayList<EObject> delete_list = new ArrayList<EObject>();		
		for (PackageableElement eo : pack.getPackagedElement()) 
		{
			if(!selected.contains(eo)) delete_list.add(eo);			
			else {				
				if (eo instanceof Package) deleteElement((Package) eo, selected);				
				else {
					for (EObject c : eo.eContents()) 
					{
						if(!selected.contains(c)) delete_list.add(c);
					}
				}					
			}		
		}		
		for(int i = 0; i<delete_list.size(); i++) EcoreUtil.remove(delete_list.get(i));
	}
	
	/*TODO: Temos algum método que pega o lado do relator corretamente?? Caso tenhamos, remover o método criado abaixo. Criado pois mediations podem vir erradas ou invertidas*/
	
	public Property getRelatorEnd (Mediation m)
	{		
		if (m.sourceEnd().getType() instanceof Relator)
			return m.sourceEnd();
		else if (m.targetEnd().getType() instanceof Relator)
			return m.targetEnd();
		else {
			if(m.sourceEnd().getType()!=null){
				for (Classifier c : ((Classifier)m.sourceEnd().getType()).allParents()) {
					if (c instanceof Relator) return m.sourceEnd();
				}
				for (Classifier c : ((Classifier)m.targetEnd().getType()).allParents()) {
					if (c instanceof Relator) return m.targetEnd();
				}
			}
		}		
		return m.sourceEnd();
		
	}
	
	public Property getMediatedEnd (Mediation m)
	{		
		return getRelatorEnd(m).getOpposite();
	}
	
	public Classifier getRelator(Mediation m) 
	{
		return (Classifier) getRelatorEnd(m).getType();
	}
	
	public Derivation getDerivation (MaterialAssociation material)
	{
		for (Derivation d: getAllInstances(Derivation.class))
		{
			for(Property prop : d.getMemberEnd())
			{
				if(prop.getType().equals(material)) return d;
			}
		}
		return null;
	}
	
	public Classifier getMediated(Mediation m) throws Exception
	{		
		return (Classifier) getMediatedEnd(m).getType();
	}
	
	/**
	 * All the model types that does not have an identity. This method will return all the sortals
	 * that does not have a Substance Sortal as its ancestors.
	 * 
	 * @return
	 */
	public ArrayList<RefOntoUML.Classifier> getElementsWithIdentityMissing ()
	{
		ArrayList<RefOntoUML.Classifier> list = new ArrayList<RefOntoUML.Classifier>();
		boolean hasIdentity;
		for (RefOntoUML.Classifier c: getAllInstances(RefOntoUML.Class.class))
		{
			hasIdentity=false;
			if (c instanceof RefOntoUML.Role || c instanceof RefOntoUML.Phase || c instanceof RefOntoUML.SubKind)
			{
				for (Classifier s: getAllParents(c)) if (s instanceof RefOntoUML.SubstanceSortal) hasIdentity=true;
				if (!hasIdentity) { list.add(c); }
			}			
		}
		return list;
	}
	
	/**
	 * Get the relators that have the sum of their mediations lower cardinality
	 * less than 2. 
	 */
	public ArrayList<Relator> getRelatorsWithInvalidAxiom ()
	{
		ArrayList<Relator> relators = new ArrayList<Relator>();
		for (Relator r: getAllInstances(Relator.class))
		{
			ArrayList<Mediation> mediations = new ArrayList<Mediation>();
			try {  getAllMediations(r, mediations); } catch (Exception e) {e.printStackTrace();}					
			
			int sum=0;
			for(Mediation m: mediations)
			{
				if (m.getMemberEnd().get(1).getUpper()==-1) sum += 2;
				else sum += m.getMemberEnd().get(1).getUpper();
			}
			
			if (sum <2) relators.add(r);
		}
		return relators;
	}
	
	/**
	 * Get wholes that have the sum of their parts lower cardinality
	 * less than 2. 
	 */
	public ArrayList<RigidSortalClass> getWholesWithInvalidWeakSupplementation ()
	{
		ArrayList<RigidSortalClass> classifiers = new ArrayList<RigidSortalClass>();
		for (RigidSortalClass r: getAllInstances(RigidSortalClass.class))
		{
			ArrayList<Meronymic> meronymics = new ArrayList<Meronymic>();
			try {  getAllMeronymics(r, meronymics); } catch (Exception e) {e.printStackTrace();}					
			
			int sum=0;
			for(Meronymic m: meronymics)
			{
				if (m.getMemberEnd().get(1).getUpper()==-1) sum += 2;
				else sum += m.getMemberEnd().get(1).getUpper();
			}
			
			if (sum <2 && meronymics.size()>=1) classifiers.add(r);
		}
		return classifiers;
	}
	
	public ArrayList<GeneralizationSet> getGeneralizationSet(Generalization g){
		ArrayList<GeneralizationSet> genSets = new ArrayList<>();
		
		for (GeneralizationSet gs : getAllInstances(GeneralizationSet.class)) {
			if(gs.getGeneralization().contains(g))
				genSets.add(gs);
		}
		
		return genSets;
	}
	
	/** Get Generalizations which the type participates */
	public ArrayList<Generalization> getGeneralizations(RefOntoUML.Classifier type)
	{
		ArrayList<RefOntoUML.Generalization> genList = new ArrayList<RefOntoUML.Generalization>();
		for(RefOntoUML.Generalization gen: getAllInstances(RefOntoUML.Generalization.class))
		{			
			if(type.equals(gen.getGeneral())) genList.add(gen); 
			if(type.equals(gen.getSpecific())) genList.add(gen);
		}
		return genList;
	}
	

	/** Get all the generalizations in which this type is the General type*/
	public ArrayList<Generalization> getSpecializations(RefOntoUML.Classifier type)
	{
		ArrayList<RefOntoUML.Generalization> genList = new ArrayList<RefOntoUML.Generalization>();
		for(RefOntoUML.Generalization gen: getAllInstances(RefOntoUML.Generalization.class))
		{			
			if(type.equals(gen.getGeneral())) genList.add(gen);	
		}
		return genList;
	}
	
	/**
	 *	Return all (selected) direct relationships (generalizations and associations) of the classifier c  
	 */
	public ArrayList<Relationship> getDirectRelationships(EObject eObject)
	{
		ArrayList<Relationship> relations = new ArrayList<>();		
		for (EObject a : getAllInstances(Relationship.class)) {
			if (a instanceof Generalization){
				Generalization g = (Generalization)a;
				if(g.getGeneral()!=null && g.getSpecific()!=null){
					if(g.getGeneral().equals(eObject) || g.getSpecific().equals(eObject)) relations.add(g);
				}				
			}else if (a instanceof Association){
				Association assoc = (Association)a;				
				RefOntoUML.Type Src = assoc.getMemberEnd().get(0).getType();
				RefOntoUML.Type Tgt = assoc.getMemberEnd().get(1).getType();
				if (Src !=null && Src.equals(eObject) || Tgt!=null && Tgt.equals(eObject)) relations.add(assoc);					
			}
		}
		return relations;
	}
		
	public ArrayList<Association> getDirectAssociations(EObject eObject)
	{
		ArrayList<Association> relations = new ArrayList<>();		
		for (EObject a : getAllInstances(Association.class)) 
		{			
			Association assoc = (Association)a;
			RefOntoUML.Type Src = assoc.getMemberEnd().get(0).getType();
			RefOntoUML.Type Tgt = assoc.getMemberEnd().get(1).getType();
			if (Src !=null && Src.equals(eObject) || Tgt!=null && Tgt.equals(eObject)) relations.add(assoc);			
		}
		return relations;
	}
	
	public ArrayList<Generalization> getDirectGeneralizations(EObject eObject)
	{
		ArrayList<Generalization> generalizations = new ArrayList<>();		
		for (EObject a : getAllInstances(Generalization.class)) 
		{			
			Generalization gen = (Generalization)a;
			RefOntoUML.Type general = gen.getGeneral();
			RefOntoUML.Type specific = gen.getSpecific();
			if (general !=null && general.equals(eObject) || specific!=null && specific.equals(eObject)) generalizations.add(gen);				
		}
		return generalizations;
	}
	
	public ArrayList<Association> getIndirectAssociations(EObject eObject)
	{
		ArrayList<Association> relations = new ArrayList<>();		
		for (EObject a : getAllInstances(Association.class)) 
		{			
			Association assoc = (Association)a;
			RefOntoUML.Type Src = assoc.getMemberEnd().get(0).getType();
			RefOntoUML.Type Tgt = assoc.getMemberEnd().get(1).getType();
			if (((RefOntoUML.Classifier)eObject).allParents().contains(Src)) relations.add(assoc);
			if(((RefOntoUML.Classifier)eObject).allParents().contains(Tgt)) relations.add(assoc);					
		}
		return relations;
	}
	

	public ArrayList<Generalization> getIndirectGeneralizations(EObject eObject)
	{
		ArrayList<Generalization> generalizations = new ArrayList<>();		
		for (EObject a : getAllInstances(Generalization.class)) 
		{			
			Generalization gen = (Generalization)a;			
			RefOntoUML.Type specific = gen.getSpecific();			
			if(((RefOntoUML.Classifier)eObject).allParents().contains(specific)) generalizations.add(gen);					
		}
		return generalizations;
	}
	
	public Property getWholeEnd(Meronymic m){
		if (m.targetEnd().getAggregation()!=AggregationKind.NONE && m.sourceEnd().getAggregation()==AggregationKind.NONE)
			return m.targetEnd();
		else
			return m.sourceEnd();
	}
	
	public Property getPartEnd(Meronymic m){
		return getWholeEnd(m).getOpposite();
	}
	
	public Property getCharacterizingEnd(Characterization c){
		if (c.getMemberEnd().get(1).getType() instanceof Mode && !(c.getMemberEnd().get(0).getType() instanceof Mode))
			return c.getMemberEnd().get(1);
		return c.getMemberEnd().get(0);
	}
	
	public Property getCharacterizedEnd(Characterization c){
		return getCharacterizingEnd(c).getOpposite();
	}
	
	/** Get the identity provider for a given OntoUML class.
	 *  If c is a Kind, Quantity or Collective, then he is the proper identity provider.
	 *  If c is Phase or Role then search for the identity provider in all parents
	 *  If c is a Category, roleMixin or Mixin: (i) search in children for the identity providers and (ii) search in parents for the identity providers.
	 *  
	 * @param c: Classifier
	 * @return
	 */
	public ArrayList<Classifier> getIdentityProvider(Classifier c)
	{
		ArrayList<Classifier> result = new ArrayList<Classifier>();
		if (c instanceof SubstanceSortal) result.add(c);
		
		if (c instanceof AntiRigidSortalClass || c instanceof SubKind)
		{
			for(Classifier p: c.allParents())
			{
				if(p instanceof SubstanceSortal) result.add(p);
			}
		}
		
		if (c instanceof MixinClass)
		{
			for(Classifier child: c.allChildren())
			{
				if(child instanceof SubstanceSortal) result.add(child);
				if(child instanceof AntiRigidSortalClass || child instanceof SubKind){
					for(Classifier childParent: child.allParents()){
						if (childParent instanceof SubstanceSortal) result.add(childParent);
					}
				}
			}
			for(Classifier parent: c.allParents())
			{
				for(Classifier parentChild: parent.allChildren()){
					if (parentChild instanceof SubstanceSortal) result.add(parentChild);
					if (parentChild instanceof AntiRigidSortalClass || parentChild instanceof SubKind) {
						for(Classifier p: parentChild.allParents()){
							if (p instanceof SubstanceSortal) result.add(p);
						}
					}
				}
			}
		}
			
		return result;		
	}

	public static MaterialAssociation getMaterial(Derivation d){
		Type source = d.getMemberEnd().get(0).getType();
		Type target = d.getMemberEnd().get(1).getType();
		
		if(source instanceof MaterialAssociation)
			return (MaterialAssociation) source;
		if(target instanceof MaterialAssociation)
			return (MaterialAssociation) target;
		
		return null;
	}
	
	public static Relator getRelator(Derivation d){
		Type source = d.getMemberEnd().get(0).getType();
		Type target = d.getMemberEnd().get(1).getType();
		
		if(source instanceof Relator)
			return (Relator) source;
		if(target instanceof Relator)
			return (Relator) target;
		
		return null;
	}
	
	public boolean isFunctionalComplex(Classifier c){
		
		if(c instanceof Kind)
			return true;
		
		if(c instanceof SubKind || c instanceof AntiRigidSortalClass){
			ArrayList<Classifier> identityProviders = getIdentityProvider(c);
			if(identityProviders.size()==1 && identityProviders.get(0) instanceof Kind)
				return true;
		}
		
		if(c instanceof MixinClass){
			if(getChildren(c).size()==0)
				return false;
			for (Classifier child : getChildren(c)) {
				if(!isFunctionalComplex(child))
					return false;
			}
			return true;
		}
		
		return false;
	}
	
	public boolean isQuantity(Classifier c){

		if(c instanceof Quantity)
			return true;
		
		if(c instanceof SubKind || c instanceof AntiRigidSortalClass){
			ArrayList<Classifier> identityProviders = getIdentityProvider(c);
			if(identityProviders.size()==1 && identityProviders.get(0) instanceof Quantity)
				return true;
		}
		
		if(c instanceof MixinClass){
			if(getChildren(c).size()==0)
				return false;
			
			for (Classifier child : getChildren(c)) {
				if(!isQuantity(child))
					return false;
			}
			return true;
		}
		
		return false;
	}
	
	public boolean isCollective(Classifier c){
		
		if(c instanceof Collective)
			return true;
		
		if(c instanceof SubKind || c instanceof AntiRigidSortalClass){
			ArrayList<Classifier> identityProviders = getIdentityProvider(c);
			if(identityProviders.size()==1 && identityProviders.get(0) instanceof Collective)
				return true;
		}
		
		if(c instanceof MixinClass){
			if(getChildren(c).size()==0)
				return false;
			
			for (Classifier child : getChildren(c)) {
				if(!isCollective(child))
					return false;
			}
			return true;
		}
		
		return false;
	}
	
	public boolean hasFunctionalComplexChild(MixinClass mixin){
		for (Classifier child : getAllChildren(mixin)) {
			if(isFunctionalComplex(child))
				return true;
		}
		
		return false;
	}
	
	public boolean hasCollectiveChild(MixinClass mixin){
		for (Classifier child : getAllChildren(mixin)) {
			if(isCollective(child))
				return true;
		}
		
		return false;
	}
	
	public boolean hasQuantityChild(MixinClass mixin){
		for (Classifier child : getAllChildren(mixin)) {
			if(isQuantity(child))
				return true;
		}
		
		return false;
	}
}
