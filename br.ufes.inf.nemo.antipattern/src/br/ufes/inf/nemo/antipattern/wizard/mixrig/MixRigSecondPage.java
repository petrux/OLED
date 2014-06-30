package br.ufes.inf.nemo.antipattern.wizard.mixrig;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import br.ufes.inf.nemo.antipattern.mixrig.MixRigAntipattern;
import br.ufes.inf.nemo.antipattern.mixrig.MixRigOccurrence;

public class MixRigSecondPage extends MixRigPage{

	//GUI
	private Button btnYes;
	private Button btnNo;
	private ChangeSubtypeRigidityComposite tableComposite;
	
	private SelectionAdapter listener = new SelectionAdapter() {
		
		public void widgetSelected(SelectionEvent event) {
						
			if (btnYes.getSelection()){
				tableComposite.setEnabledToAllContents(false);
				setPageComplete(true);
			}
			if(btnNo.getSelection()){
				tableComposite.setEnabledToAllContents(true);
				if(tableComposite.getModifiedSubtypes().size()>0)
					setPageComplete(true);
				else
					setPageComplete(false);
			}
		}
	};
	
	private SelectionListener comboListener = new SelectionAdapter(){
	
		@Override
		public void widgetSelected(SelectionEvent event) {
        	System.out.println("Called composite listener");
        	if(btnNo.getSelection()){
        		System.out.println("No is Selected!");
        		System.out.println("Modif. Subt: "+tableComposite.getModifiedSubtypes().size());
        		if(tableComposite.getModifiedSubtypes().size()>0)
        			setPageComplete(true);
    			else
    				setPageComplete(false);			
        	}
				
		}
	};
	
	public MixRigSecondPage(MixRigOccurrence mixRig) {
		super(mixRig);	
	}
		
	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		
		
		setTitle(MixRigAntipattern.getAntipatternInfo().getName());
		setDescription("Mixin: "+mixRig.getMixin().getName()+"\r\nSubtypes: "+getSubtypeList(3));
		
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		
		setPageComplete(false);
		
		StyledText styledText = new StyledText(container, SWT.READ_ONLY | SWT.WRAP);
		styledText.setBounds(10, 10, 554, 35);
		styledText.setText(	"Is the rigidity meta-property of all subtypes of "+mixRig.getMixin().getName()+" correct? If not, use the table below to change the stereotype:");
		styledText.setJustify(true);
		styledText.setBackground(styledText.getParent().getBackground());
		
		btnYes = new Button(container, SWT.RADIO);
		btnYes.setBounds(10, 51, 554, 16);
		btnYes.setText("Yes");
		btnYes.addSelectionListener(listener);
		
		btnNo = new Button(container, SWT.RADIO);
		btnNo.setBounds(10, 73, 554, 16);
		btnNo.setText("No");
		btnNo.addSelectionListener(listener);
		
		tableComposite = new ChangeSubtypeRigidityComposite(container, SWT.NONE, mixRig, getMixRigWizard().allowedStereotypes(), comboListener);
		tableComposite.setBounds(10, 103, 569, 168);
		tableComposite.setEnabledToAllContents(false);
	}
	
	@Override
	public IWizardPage getNextPage() 
	{	
		getMixRigWizard().removeAllActions();
		
		if(btnYes.getSelection())
			return getMixRigWizard().getThirdPage();
		if(btnNo.getSelection()) {
			MixRigAction action = new MixRigAction(mixRig);
			action.setChangeSubtypesStereotype(tableComposite.getModifiedSubtypes());
			getMixRigWizard().addAction(0, action);
			return getMixRigWizard().getFinishing();
		}
		
		return super.getNextPage();
	}
}
