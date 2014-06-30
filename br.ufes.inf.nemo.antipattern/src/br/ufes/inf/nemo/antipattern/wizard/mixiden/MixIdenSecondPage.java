package br.ufes.inf.nemo.antipattern.wizard.mixiden;

import java.util.ArrayList;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import RefOntoUML.Classifier;
import br.ufes.inf.nemo.antipattern.mixiden.MixIdenAntipattern;
import br.ufes.inf.nemo.antipattern.mixiden.MixIdenOccurrence;

public class MixIdenSecondPage  extends MixIdenPage {

	//GUI
	ChangeIdentityProviderComposite changeIdentityComposite;
	private Button btnNo;
	private Button btnYes;	
		
	public MixIdenSecondPage(MixIdenOccurrence mixIdenOccurrence) {
		super(mixIdenOccurrence);	
	}
	
	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		
		setTitle(MixIdenAntipattern.getAntipatternInfo().getName());
		setDescription(	"Mixin: "+mixIden.getMixin().getName()+
						"\r\nSubtypes: "+getSubtypeList(3)+
						"\r\nCommon Identity Provider: "+mixIden.getIdentityProvider().getName());
		
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);	
		
		setPageComplete(false);
		
		StyledText styledText = new StyledText(container, SWT.READ_ONLY | SWT.WRAP);
		styledText.setBounds(10, 10, 554, 30);
		styledText.setText(	"Do all subtypes indeed inherits their identity principle from <"+mixIden.getIdentityProvider().getName()+">? " +
							"If No, use the table below to change the identity principle suppliers.");
		styledText.setJustify(true);
		styledText.setBackground(styledText.getParent().getBackground());
		
		btnYes = new Button(container, SWT.RADIO);
		btnYes.setBounds(10, 46, 554, 16);
		btnYes.setText("Yes, they all inherit from it");
		btnYes.addSelectionListener(btnYesListener);
		
		btnNo = new Button(container, SWT.RADIO);
		btnNo.setBounds(10, 68, 554, 16);
		btnNo.setText("No");
		btnNo.addSelectionListener(btnNoListener);
		
		try {
			ArrayList<Classifier> forbbidenTypes = new ArrayList<Classifier>(mixIden.getIdentityProvider().allChildren());
			forbbidenTypes.add(mixIden.getIdentityProvider());
			
			changeIdentityComposite = new ChangeIdentityProviderComposite(container, SWT.BORDER, mixIden,btnNoListener);
			changeIdentityComposite.setBounds(10, 121, 554, 255);
			changeIdentityComposite.setAllEnabled(false);

		} catch (Exception e) {
			System.out.println("ERROR!");
		}
		
	}
	
	private SelectionAdapter btnYesListener = new SelectionAdapter() {
		
		public void widgetSelected(SelectionEvent event) {
			if(btnYes.getSelection()){
				if(!isPageComplete()) setPageComplete(true);
				changeIdentityComposite.setAllEnabled(false);
			}
		}
	};
	
	private SelectionAdapter btnNoListener = new SelectionAdapter() {
		
		public void widgetSelected(SelectionEvent event) {
			if(btnNo.getSelection()){
				if(!changeIdentityComposite.isAllEnabled())
					changeIdentityComposite.setAllEnabled(true);
				
				if(changeIdentityComposite.getSubtypesToFix().size()>0)
					setPageComplete(true);
				else
					setPageComplete(false);
				
			}
		}
	};
	
	@Override
	public IWizardPage getNextPage() 
	{	
		getMixIdenWizard().removeAllActions();

		if(btnNo.getSelection()) {
			MixIdenAction action = new MixIdenAction(mixIden);
			action.setChangeSubtypesIdentity(changeIdentityComposite.getSubtypesToFix());
			getMixIdenWizard().addAction(0, action);
			return getMixIdenWizard().getFinishing();
		}
		else if(btnYes.getSelection())
			getMixIdenWizard().getThirdPage();
		
		return super.getNextPage();
	
	}
}
