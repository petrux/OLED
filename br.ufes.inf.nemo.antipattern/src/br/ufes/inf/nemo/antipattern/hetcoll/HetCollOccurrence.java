package br.ufes.inf.nemo.antipattern.hetcoll;

import java.util.ArrayList;

import org.eclipse.emf.ecore.EObject;

import RefOntoUML.Association;
import RefOntoUML.Classifier;
import RefOntoUML.Collective;
import RefOntoUML.Phase;
import RefOntoUML.Property;
import RefOntoUML.Role;
import RefOntoUML.SubKind;
import RefOntoUML.memberOf;
import br.ufes.inf.nemo.antipattern.AntipatternOccurrence;
import br.ufes.inf.nemo.common.ontoumlparser.OntoUMLParser;

public class HetCollOccurrence extends AntipatternOccurrence {

	public Classifier getWhole() {
		return whole;
	}

	public ArrayList<Property> getMemberEnds() {
		return memberEnds;
	}

	private Classifier whole;
	private ArrayList<Property> memberEnds;
	
	public HetCollOccurrence(Classifier whole, ArrayList<Property> memberEnds, HetCollAntipattern ap) throws Exception {
		super(ap);
		
		if(whole==null || memberEnds==null || parser==null)
			throw new NullPointerException("HetColl: null inputs!");
		if(memberEnds.size()<2)
			throw new Exception("HetColl: more than two memberOfs are required!");
		if(!(whole instanceof Collective) && !(whole instanceof SubKind) && !(whole instanceof Role) && !(whole instanceof Phase))
			throw new Exception("HetColl: whole type not acceptable. Required to be Collective, Subkind, Role or Phase");
		
		for (Property p : memberEnds) {
			if (!(p.getAssociation() instanceof memberOf))
				throw new Exception("HetColl: All properties must refer to memberOf relations.");
		}
		
		this.whole = whole;
		this.memberEnds = memberEnds;
	}

	@Override
	public OntoUMLParser setSelected() {
		ArrayList<EObject> selection = new ArrayList<EObject>();
		
		selection.add(this.whole);
		for (Property p : this.memberEnds) {
			selection.add(p.getAssociation());
			selection.add(p.getType());
		}
		
		parser.selectThisElements(selection,true);
		parser.autoSelectDependencies(OntoUMLParser.SORTAL_ANCESTORS, false);

		return parser;
	}
	
	@Override
	public String toString(){
		String result = "Whole: "+parser.getStringRepresentation(this.whole)+"\n";
		
		result+="Members: ";
		for (Property p : this.memberEnds)
			result+="\n\t"+parser.getStringRepresentation(p);
		
		return result;
		
	}

	@Override
	public String getShortName() {
		return parser.getStringRepresentation(whole);
	}

	// OUTCOMING FIXES ===============================================
	
	public void changeAllToComponentOf(ArrayList<Association> partOfList) 
	{
	
		fix.addAll(fixer.changeAllToComponentOf(partOfList));
	}

	public void changeAllToCollectionAndSubCollectionOf(ArrayList<Association> partOfList) 
	{
		fix.addAll(fixer.changeAllToCollectionAndSubCollectionOf(partOfList));
	}

	public void changeAllToOneSuperMember(ArrayList<Association> partOfList) 
	{		
		fix.addAll(fixer.changeAllToOneSuperMember(partOfList));
	}

}
