package br.ufes.inf.nemo.common.ontoumlparser;

import org.eclipse.emf.ecore.EObject;

import RefOntoUML.Association;
import RefOntoUML.Category;
import RefOntoUML.Characterization;
import RefOntoUML.Class;
import RefOntoUML.Classifier;
import RefOntoUML.Collective;
import RefOntoUML.Comment;
import RefOntoUML.DataType;
import RefOntoUML.Derivation;
import RefOntoUML.Enumeration;
import RefOntoUML.FormalAssociation;
import RefOntoUML.Generalization;
import RefOntoUML.GeneralizationSet;
import RefOntoUML.Kind;
import RefOntoUML.MaterialAssociation;
import RefOntoUML.Mediation;
import RefOntoUML.Mixin;
import RefOntoUML.Mode;
import RefOntoUML.Model;
import RefOntoUML.NamedElement;
import RefOntoUML.Package;
import RefOntoUML.Phase;
import RefOntoUML.PrimitiveType;
import RefOntoUML.Property;
import RefOntoUML.Quantity;
import RefOntoUML.Relator;
import RefOntoUML.Role;
import RefOntoUML.RoleMixin;
import RefOntoUML.SubKind;
import RefOntoUML.componentOf;
import RefOntoUML.memberOf;
import RefOntoUML.subCollectionOf;
import RefOntoUML.subQuantityOf;

public class OntoUMLNameHelper {

	public static String getTypeName(EObject elem){
		if(elem instanceof Kind)
			return "Kind";
		if(elem instanceof Quantity)
			return "Quantity";
		if(elem instanceof Collective)
			return "Collective";
		if(elem instanceof SubKind)
			return "SubKind";
		if(elem instanceof Role)
			return "Role";
		if(elem instanceof Phase)
			return "Phase";
		if(elem instanceof RoleMixin)
			return "RoleMixin";
		if(elem instanceof Category)
			return "Category";
		if(elem instanceof Mixin)
			return "Mixin";
		if(elem instanceof Relator)
			return "Relator";
		if(elem instanceof Mode)
			return "Mode";
		if(elem instanceof PrimitiveType)
			return "Primitive Type";
		if(elem instanceof Enumeration)
			return "Enumeration";
		if(elem instanceof DataType)
			return "Datatype";
		if(elem instanceof Class)
			return "Class";
		if(elem instanceof FormalAssociation)
			return "Formal";
		if(elem instanceof MaterialAssociation)
			return "Material";
		if(elem instanceof componentOf)
			return "ComponentOf";
		if(elem instanceof subQuantityOf)
			return "SubQuantityOf";
		if(elem instanceof memberOf)
			return "MemberOf";
		if(elem instanceof subCollectionOf)
			return "SubCollectionOf";
		if(elem instanceof Mediation)
			return "Mediation";
		if(elem instanceof Characterization)
			return "Characterization";
		if(elem instanceof Derivation)
			return "Derivation";
		if(elem instanceof Association)
			return "Association";
		if(elem instanceof Generalization)
			return "Generalization";
		if(elem instanceof GeneralizationSet)
			return "GeneralizationSet";
		if(elem instanceof Model)
			return "Model";
		if(elem instanceof Package)
			return "Package";
		
		if(elem instanceof Property){
			if(((Property) elem).getAssociation() instanceof Association)
				return "Association End";
			else
				return "Attribute";
		}
		
		if(elem instanceof Comment)
			return "Comment";
		
		return "Unknown Type";
	}
	
	public static String getTypeName(EObject elem, boolean addGuillemets){
		if(addGuillemets)
			return "«"+getTypeName(elem)+"»";
		
		return getTypeName(elem);
	}
	
	public static String getName(EObject elem){
		if(elem==null)
			return "null";
		
		if(elem instanceof NamedElement){
			String name = ((NamedElement) elem).getName();
			
			if(name == null)
				return "unnamed";
			
			return name;
		}
		
		return "nameless";
	}
	
	public static String getName(EObject elem, boolean addSingleQuote, boolean addLowerUpper){
		if(addSingleQuote)
			return "'"+getName(elem)+"'";
		
		if(addLowerUpper)
			return "<"+getName(elem)+">";
		
		return getName(elem);
	}
	
	public static String getTypeAndName(EObject elem, boolean addGuillemets, boolean addSingleQuotes){
		
		String name = "";
		
		if(elem instanceof NamedElement)
			name = " "+getName(elem,addSingleQuotes,false);
		
		return getTypeName(elem,addGuillemets)+name;
	}
	
	public static String getCommonName(EObject elem) 
	{		
		
		if (elem instanceof Package){
			return getTypeAndName(elem, false, false);
		}
		
		if (elem instanceof Generalization)	{			
			return getTypeName(elem, false) +" " + getTypeAndName(((Generalization)elem).getGeneral(), false, false);
		}
		
		if (elem instanceof Classifier) {
			return getTypeAndName(elem, true, false);
		}
		
		if (elem instanceof GeneralizationSet)
		{	
			String result = new String();
		    Classifier general = null;
		    GeneralizationSet genset = (RefOntoUML.GeneralizationSet)elem;
		    
		    if(genset.getGeneralization()!=null && !genset.getGeneralization().isEmpty())
		    	general = genset.getGeneralization().get(0).getGeneral();
		    
		    result += getTypeAndName(elem, false, false) + " / "+getName(general)+" { ";
		   	    
		    int i=1;
		    for(Generalization gen: genset.getGeneralization())
		    {
		    	if(i < genset.getGeneralization().size()) 
		    		result += getName(gen.getSpecific())+", ";
		    	else 
		    		result += getName(gen.getSpecific()) + " } ";
		    	i++;
		    }
		    return result;		    
		}
		
		if (elem instanceof Property)
		{
			Property p = (Property)elem;
			return getTypeName(p)+" "+getName(p.getType())+" ("+getName(p)+")"+" ["+getMultiplicity(p,true,"..")+"]";			
		}
		
		return getTypeAndName(elem, true, false);
	}
	
	public static String getCompleteName(EObject elem){
		if (elem instanceof Package){
			return getTypeAndName(elem, true, false);
		}
		
		if (elem instanceof Generalization)	{
			Generalization g = (Generalization) elem;
			return getTypeName(elem, true) +" {" + getTypeAndName(g.getSpecific(), true, false) + " -> "+getTypeAndName(g.getGeneral(), true, false)+ "}";
		}
		
		if (elem instanceof Class || elem instanceof DataType) {
			return getTypeAndName(elem, true, false);
		}
		
		if (elem instanceof Association){
			Association a = (Association) elem;
			return getTypeAndName(elem,true, false)+" {"+getCommonName(a.getMemberEnd().get(0).getType())+" -> "+getCommonName(a.getMemberEnd().get(1).getType()) + "}";
		}
		
		if(elem instanceof GeneralizationSet){
			GeneralizationSet gs = (GeneralizationSet) elem;
			return getCommonName(elem)+" isCovering: "+gs.isIsCovering()+", isDisjoint: "+gs.isIsDisjoint();
		}
		
		if(elem instanceof Property)
		{
			Property p = (Property)elem;
			return getTypeName(p,true)+" "+getName(p.eContainer())+"::"+getName(p)+" ("+getName(p.getType())+")"+" ["+getMultiplicity(p,true,"..")+"]";			
		}
		
		return getTypeAndName(elem, true, false);
	}
	
	public static String getMultiplicity(Property p, boolean alwaysShowLowerAndUpper, String separator){
		if(p==null)
			return "null";
		
		Integer lower = p.getLower();
		Integer upper = p.getUpper();
		String lowerString = lower.toString();
		String upperString = upper.toString();
		
		if (lower == -1) 
			lowerString = "*";
		if (upper == -1) 
			upperString = "*";
		
		if(!alwaysShowLowerAndUpper && lower==upper)
			return lowerString;
		
		return lowerString+separator+upperString;
	}
	
	public static String getNameAndType(Property p){
		return getName(p, true, false)+" ("+getName(p.getType())+")";
	}
}
