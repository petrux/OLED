package br.ufes.inf.nemo.antipattern.freerole;

import java.util.ArrayList;
import java.util.Map;

import RefOntoUML.Classifier;
import RefOntoUML.Package;
import RefOntoUML.Property;
import RefOntoUML.Role;
import br.ufes.inf.nemo.antipattern.AntiPatternIdentifier;
import br.ufes.inf.nemo.antipattern.Antipattern;
import br.ufes.inf.nemo.antipattern.AntipatternInfo;
import br.ufes.inf.nemo.common.ontoumlparser.OntoUMLParser;

public class FreeRoleAntipattern extends Antipattern<FreeRoleOccurrence> {

	public FreeRoleAntipattern(OntoUMLParser parser) throws NullPointerException {
		super(parser);
	}
	
	public FreeRoleAntipattern(Package pack) throws NullPointerException {
		this(new OntoUMLParser(pack));
	}

	private static final String oclQuery = 	
		"let mediatedProperties : Bag (Property) = "+
		"	Mediation.allInstances().memberEnd "+
		"		->select( p: Property |  "+
		"			not p.type.oclIsTypeOf(Relator) "+ // mediado não é um relator 
		"			and  "+
		"			p.type.oclAsType(Classifier).allParents()->forAll( parent : Classifier | not parent.oclIsTypeOf(Relator))) "+ // mediado não é subtipo de nenhum relator (e.g. phase de relator) 
		"	in mediatedProperties "+ // expressão para agrupar mediados e as propriedades opostas conectas a relators
		"		->collect( p : Property | Tuple { "+
		"			mediated: Classifier=p.type.oclAsType(Classifier),  "+
		"			relatorEnds: Set(Property)=mediatedProperties->select(p2 : Property | p.type=p2.type).opposite->asSet()} "+
		"		)->asSet() "+
		"		->select ( x |  "+
		"			x.mediated.oclIsTypeOf(Role) "+ // mediado é um role
		"			and "+
		"			x.mediated.allChildren() "+
		"			->exists( child: Classifier | "+ // existe pelo menos um subtipo do role que: 
		"				child.oclIsTypeOf(Role) "+ // é instância de um role 
		"				and  "+
		"				mediatedProperties.type->excludes(child.oclAsType(Type)) "+  //não está conectado diretamente a nenhuma mediação
		"				and "+
		"				child.allParents() "+
		"				->forAll( parent: Classifier |  "+
		"					(parent<>x.mediated "+ // todos os demais pais não estão diretamente conectados a uma mediation 
		"					and "+
		"					x.mediated.allParents()->excludes(parent)) "+
		"					implies "+
		"					mediatedProperties.type->excludes(parent.oclAsType(Type)) "+ 
		"				) "+
		"			) "+ 
		"		)";
				
				
				
		
	private static final AntipatternInfo info = new AntipatternInfo("Free Role Specialization", 
			"FreeRole", 
			"This anti-pattern identifies occurrences when a «role» type connected to a «relator» through a «mediation» association, " +
			"is specialized in another «role» type, which in turn is not connected to an additional «mediation» association.",
			oclQuery); 
		
	public static AntipatternInfo getAntipatternInfo(){
		return info;
	}
	
	@Override
	public ArrayList<FreeRoleOccurrence> identify() {
		Map<Classifier, ArrayList<Property>> query_result;
		
		query_result = AntiPatternIdentifier.runOCLQuery(parser, oclQuery, Classifier.class, Property.class, "mediated", "relatorEnds");
			
		for (Classifier role : query_result.keySet()) 
		{
			try {
				if (role instanceof Role){
					FreeRoleOccurrence occurrence = new FreeRoleOccurrence((Role) role, query_result.get(role), this);
					super.occurrence.add(occurrence);
				}else throw new Exception();
			} catch (Exception e) {
				System.out.println("FreeRole: Provided information does not characterize an occurrence of the anti-pattern!");
			}
		}
		
		return this.getOccurrences();
	}

}
