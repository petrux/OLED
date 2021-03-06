package br.ufes.inf.nemo.antipattern.wizard.hetcoll;

import java.text.Normalizer;
import java.util.ArrayList;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import RefOntoUML.Association;
import RefOntoUML.Property;
import RefOntoUML.Type;
import br.ufes.inf.nemo.antipattern.hetcoll.HetCollOccurrence;
import br.ufes.inf.nemo.antipattern.wizard.AntipatternWizard;

public class HetCollSecondPage extends HetCollPage {

	public Button btnYes;
	public Button btnNo;
	public Button btnArrowLeft;
	public Button btnArrowRight;
	public List noList;
	public List yesList;
	
	public HetCollSecondPage(HetCollOccurrence hetColl) 
	{
		super(hetColl);		
		setDescription("Whole: <"+getStereotype(hetColl.getWhole())+"> "+hetColl.getWhole().getName());
	}

	public static String getStereotype(EObject element)
	{
		String type = element.getClass().toString().replaceAll("class RefOntoUML.impl.","");
	    type = type.replaceAll("Impl","");
	    type = Normalizer.normalize(type, Normalizer.Form.NFD);
	    if (!type.equalsIgnoreCase("association")) type = type.replace("Association","");
	    return type;
	}
	
	public boolean contains(List list, String elem){
		for(String str: list.getItems()){
			if (str.equals(elem)) return true;
		}
		return false;
	}
	
	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) 
	{
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		
		Text lblAreTheParts = new Text(container, SWT.WRAP | SWT.V_SCROLL);
		lblAreTheParts.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		lblAreTheParts.setBounds(10, 10, 554, 37);
		lblAreTheParts.setText("Are the parts of the "+hetColl.getWhole().getName()+"’s parts also a part of "+hetColl.getWhole().getName()+"? ");
		
		Text lblNewLabel = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		lblNewLabel.setBounds(10, 63, 227, 78);
		lblNewLabel.setText("Yes.\nThe following parts are members of "+hetColl.getWhole().getName()+":");
		
		yesList = new List(container, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		yesList.setBounds(10, 147, 227, 125);
		
		for(Property p: hetColl.getMemberEnds())
		{		
			Type type = p.getType();
			yesList.add(getStereotype(type)+" "+type.getName());			
		}
		yesList.setSelection(0);
		
		btnArrowRight = new Button(container, SWT.NONE);
		btnArrowRight.setBounds(243, 182, 37, 25);
		btnArrowRight.setText("->");
		btnArrowRight.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				for(String str: yesList.getSelection()){
					if(!contains(noList,str)) { noList.add(str); noList.select(noList.indexOf(str)); } 
				}
				if(yesList.getSelectionIndex()>=0) { 
					int prev = yesList.getSelectionIndex()-1;
					yesList.remove(yesList.getSelectionIndex());
					yesList.select(prev); 
				}				
			}
		});
				
		btnArrowLeft = new Button(container, SWT.NONE);
		btnArrowLeft.setBounds(243, 213, 37, 25);
		btnArrowLeft.setText("<-");
		btnArrowLeft.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				for(String str: noList.getSelection()){
					if(!contains(yesList,str)) { yesList.add(str); yesList.select(yesList.indexOf(str));  } 
				}
				if(noList.getSelectionIndex()>=0) {
					int prev = noList.getSelectionIndex()-1;
					noList.remove(noList.getSelectionIndex());
					noList.select(prev);					 
				}
			}
		});
				
		noList = new List(container, SWT.BORDER);
		noList.setBounds(286, 147, 227, 125);
		
		Text lblNewLabel_3 = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		lblNewLabel_3.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		lblNewLabel_3.setBounds(286, 63, 227, 78);
		lblNewLabel_3.setText("No.\nThe following parts are subcollections of "+hetColl.getWhole().getName()+":");
	}
	
	public Property getProperty (String typeName){
		for(Property p: hetColl.getMemberEnds()){
			if(p.getType().getName().compareToIgnoreCase(typeName)==0) return p;			
		}
		return null;
	}
		
	public ArrayList<Property> getYesList()
	{
		ArrayList<Property> result = new ArrayList<Property>();
		for(String str: yesList.getItems()){
			Property p = getProperty(str.substring(str.indexOf(" ")+1));
			if (p!=null) result.add(p);
		}
		return result;
	}
	
	public ArrayList<Property> getNoList()
	{
		ArrayList<Property> result = new ArrayList<Property>();
		for(String str: noList.getItems()){
			Property p = getProperty(str.substring(str.indexOf(" ")+1));
			if (p!=null) result.add(p);
		}
		return result;
	}
	
	@Override
	public IWizardPage getNextPage() 
	{
		if(getYesList().size()>0){
			ArrayList<Association> assocList = new ArrayList<Association>();
			for(Property p: getNoList()) { if (p!=null) assocList.add(p.getAssociation()); }
			if(assocList.size()>=2){
				//Action =============================
				HetCollAction newAction = new HetCollAction(hetColl);
				newAction.setChangeAllToOneSuperMember(assocList); 
				if (getYesList().size()>0) getHetCollWizard().replaceAction(1,newAction);
				else getHetCollWizard().replaceAction(0,newAction);
				//======================================
			}
		}
		if(getNoList().size()>0){
			ArrayList<Association> assocList = new ArrayList<Association>();
			for(Property p: getYesList()) { if (p!=null) assocList.add(p.getAssociation()); }		
			if(assocList.size()>0){
				//Action =============================
				HetCollAction newAction = new HetCollAction(hetColl);
				newAction.setChangeAllToCollectionAndSubCollectionOf(assocList); 
				getHetCollWizard().replaceAction(0,newAction);	
				//======================================
			}						
		}
		return ((AntipatternWizard)getWizard()).getFinishing();		
	}	
}
