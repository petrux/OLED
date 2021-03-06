package br.ufes.inf.nemo.antipattern.wizard.relspec;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.SWTResourceManager;

import br.ufes.inf.nemo.antipattern.relspec.RelSpecOccurrence;
import br.ufes.inf.nemo.common.ontoumlfixer.OutcomeFixer;

/**
 * @author Tiago Sales
 * @author John Guerson
 *
 */

public class RelSpecFirstPage extends RelSpecPage {

	//GUI
	public Button btnRequired;
	public Button btnForbidden;
	public Button btnPossible;
	
	/**
	 * Create the wizard.
	 */
	public RelSpecFirstPage(RelSpecOccurrence relSpec) 
	{
		super(relSpec);
		setDescription("Associations: "+relSpec.getParser().getStringRepresentation(relSpec.getGeneral())+" and "+relSpec.getParser().getStringRepresentation(relSpec.getSpecific()));
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		
		StyledText styledText = new StyledText(container, SWT.WRAP | SWT.V_SCROLL);
		styledText.setMarginColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		styledText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		styledText.setText(			
			"General-Relation: <<"+OutcomeFixer.getStereotype(relSpec.getSpecific())+">> "+relSpec.getGeneral().getMemberEnd().get(0).getType().getName()+"->"+relSpec.getGeneral().getMemberEnd().get(1).getType().getName()+"\n"+
			"Specific-Relation: <<"+OutcomeFixer.getStereotype(relSpec.getSpecific())+">> "+relSpec.getSpecific().getMemberEnd().get(0).getType().getName()+"->"+relSpec.getSpecific().getMemberEnd().get(1).getType().getName()+"\n\n"+
					
			"Consider an instance ‘x’ of "+relSpec.getAlignedSpecificSource().getName()+" that is related to an instance ‘y’ of " +
			relSpec.getAlignedSpecificTarget().getName()+", through relation Specific-Relation. What can be said" +
			" about ‘x’ being connected to ‘y’ through General-Relation?"
		);
		styledText.setEditable(false);
		styledText.setBounds(10, 10, 554, 109);
		
		SelectionAdapter listener = new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent e) {
	        if (isPageComplete()==false) setPageComplete(true);
	      }
	    };
		    
	    setPageComplete(false);

		btnRequired = new Button(container, SWT.RADIO);
		btnRequired.setBounds(10, 125, 90, 16);
		btnRequired.setText("Required");		
		btnRequired.addSelectionListener(listener);
		
		btnForbidden = new Button(container, SWT.RADIO);
		btnForbidden.setText("Forbbiden");
		btnForbidden.setBounds(10, 147, 90, 16);		
		btnForbidden.addSelectionListener(listener);
		
		btnPossible = new Button(container, SWT.RADIO);
		btnPossible.setText("Possible, but not required");
		btnPossible.setBounds(10, 169, 156, 16);
		btnPossible.addSelectionListener(listener);
	}
	
	@Override
	public IWizardPage getNextPage() {
		
		if(btnPossible.getSelection()) {
			
			getRelSpecWizard().removeAllActions();
			
			return getRelSpecWizard().getFinishing();
			
		}else if(btnForbidden.getSelection()){			
						
			RelSpecAction newAction = new RelSpecAction(relSpec);
			newAction.setDisjoint();
			getRelSpecWizard().replaceAction(0,newAction);
			
			return getRelSpecWizard().getFinishing();
		}
		else if(btnRequired.getSelection()){
			
			return getRelSpecWizard().getSecondPage();
		}
		return super.getNextPage();
	}
	
}
