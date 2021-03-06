package br.ufes.inf.nemo.antipattern.wizard.undefphase;

import java.util.ArrayList;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import RefOntoUML.Classifier;
import br.ufes.inf.nemo.antipattern.undefphase.UndefPhaseOccurrence;
import br.ufes.inf.nemo.common.ontoumlparser.ParsingElement;

public class UndefPhaseFourthPage  extends UndefPhasePage{
	
	Composite parent;
	private Label lblPhasesCanAlso;
	private Button btnNo;
	private Button btnYes;
	private CreateModeComposite createModeComposite;
	
	/**
	 * Create the wizard.
	 */
	public UndefPhaseFourthPage(UndefPhaseOccurrence up) 
	{
		super(up);
		setDescription((new ParsingElement(up.getPartition(),true,"")).toString());
	}
	
	@Override
	public void createControl(Composite parent) 
	{
		this.parent = parent;
		Composite container = new Composite(parent, SWT.NULL);
		
		setControl(container);
		
		lblPhasesCanAlso = new Label(container, SWT.WRAP);
		lblPhasesCanAlso.setBounds(10, 10, 554, 62);
		lblPhasesCanAlso.setText("Phases can also be defined by the appearance of modes. For example, a kind Person may own a partition containing the Sick and Healthy phases. A Sick Person is one that has a mode Disease. Is that is the case?");
		
		btnNo = new Button(container, SWT.RADIO);
		btnNo.setBounds(10, 78, 554, 16);
		btnNo.setText("No");
		
		btnYes = new Button(container, SWT.RADIO);
		btnYes.setBounds(10, 102, 554, 16);
		btnYes.setText("Yes");
		
		createModeComposite = new CreateModeComposite(container, SWT.NONE, (UndefPhaseOccurrence) up);
		createModeComposite.setBounds(10, 134, 554, 215);
		createModeComposite.setVisible(false);
		SelectionAdapter listener = new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		    	  if(btnYes.getSelection()) createModeComposite.setVisible(true);
		    	  if(btnNo.getSelection()) createModeComposite.setVisible(false);
		      }
		};				
		btnNo.addSelectionListener(listener);
		btnYes.addSelectionListener(listener);
	}
	
	@Override
	public IWizardPage getNextPage() {
		if(btnYes.getSelection())
		{
			ArrayList<String> names = new ArrayList<String>();
			ArrayList<String> cardinalities = new ArrayList<String>();
			names.addAll(createModeComposite.getValues().keySet());
			cardinalities.addAll(createModeComposite.getValues().values());
			ArrayList<Classifier> phases = createModeComposite.getPhases();
			
			//Action =============================
			UndefPhaseAction newAction = new UndefPhaseAction(up);			
			newAction.setCreateModes(names, cardinalities,phases);
			getUndefPhaseWizard().replaceAction(0,newAction);	
			//======================================
			
			return getUndefPhaseWizard().getFinishing();
		}
		if(btnNo.getSelection())
		{
			return getUndefPhaseWizard().getFifthPage();
		}
		
		return getUndefPhaseWizard().getFinishing();
	}
}
