package br.ufes.inf.nemo.antipattern.wizard.homofunc;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import RefOntoUML.Category;
import RefOntoUML.Kind;
import RefOntoUML.Mixin;
import RefOntoUML.Phase;
import RefOntoUML.Role;
import RefOntoUML.RoleMixin;
import RefOntoUML.SubKind;
import br.ufes.inf.nemo.antipattern.homofunc.HomoFuncOccurrence;

public class CreatePartComposite extends Composite {

	public HomoFuncOccurrence homoFunc;
	public boolean isSubpart;
	
	public CreatePartComposite(Composite parent, int style, HomoFuncOccurrence homoFunc, boolean isSubpart) 
	{
		super(parent, style);
		this.isSubpart=isSubpart;
		setSize(new Point(538, 164));
		this.homoFunc=homoFunc;
		createPartControl();
	}

	private Text partNameField;
	private Text componentOfNameField;
	private Combo stereoCombo;
	private Button btnIsShareable;
	private Button btnIsEssential;
	private Button btnIsInseparable;
	private Button btnIsImmutablePart;
	private Button btnIsImmutableWhole;
	private Label label;
	private Label label_1;
	private Label lblComponentofName;
	private Button btnCreateNewPart;
	private Button btnDeletePart;	
	private List partsList;
	public ArrayList<PartElement> parts = new ArrayList<PartElement>();
		
	public String getPartName()
	{
		return partNameField.getText();
	}
	
	public String getComponentOfName()
	{
		return componentOfNameField.getText();
	}
	
	public String getPartStereotype()
	{
		if(stereoCombo.getSelectionIndex()>0)
			return stereoCombo.getItem(stereoCombo.getSelectionIndex());
		else
			return "";
	}
	 
	public boolean isShareable()
	{
		return btnIsShareable.getSelection();
	}
	
	public boolean isEssential()
	{
		return btnIsEssential.getSelection();
	}
	
	public boolean isInseparable()
	{
		return btnIsInseparable.getSelection();
	}
	
	public boolean isImmutablePart()
	{
		return btnIsImmutablePart.getSelection();
	}
	
	public boolean isImmutableWhole(){
		return btnIsImmutableWhole.getSelection();
	}
	
	public void enableAll(boolean value)
	{
		label.setEnabled(value);		
		label_1.setEnabled(value);		
		lblComponentofName.setEnabled(value);		
		stereoCombo.setEnabled(value);		
		partNameField.setEnabled(value);		
		componentOfNameField.setEnabled(value);		
		btnIsShareable.setEnabled(value);		
		btnIsEssential.setEnabled(value);		
		btnIsInseparable.setEnabled(value);		
		btnIsImmutablePart.setEnabled(value);		
		btnIsImmutableWhole.setEnabled(value);
	}
	
	public void createPartControl() 
	{		
		setLayout(null);
		
		setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		
		label = new Label(this, SWT.NONE);
		if(!isSubpart)
			label.setText("Part stereotype:");
		else
			label.setText("Subart stereotype:");
		label.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		label.setBounds(10, 13, 122, 21);
		
		label_1 = new Label(this, SWT.NONE);
		if(!isSubpart)
			label_1.setText("Part name:");
		else
			label_1.setText("Subpart name:");
		label_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		label_1.setBounds(10, 40, 122, 21);
		
		lblComponentofName = new Label(this, SWT.NONE);
		lblComponentofName.setText("ComponentOf name:");
		lblComponentofName.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblComponentofName.setBounds(10, 67, 122, 21);
		
		stereoCombo = new Combo(this, SWT.NONE);
		if(!isSubpart)
			stereoCombo.setItems(new String[] {"Kind", "SubKind", "Role", "Phase", "Mixin", "RoleMixin", "Category"});
		else {
			if(homoFunc.getPartEnd().getType() instanceof Kind || homoFunc.getPartEnd().getType() instanceof SubKind){
				stereoCombo.setItems(new String[] {"SubKind", "Role", "Phase"});
			}else if (homoFunc.getPartEnd().getType() instanceof Role){
				stereoCombo.setItems(new String[] {"Role"});
			}else if (homoFunc.getPartEnd().getType() instanceof Phase){
				stereoCombo.setItems(new String[] {"Role","Phase"});				
			}else if (homoFunc.getPartEnd().getType() instanceof Category){
				stereoCombo.setItems(new String[] {"Category","RoleMixin","Kind"});
			}else if (homoFunc.getPartEnd().getType() instanceof Mixin){
				stereoCombo.setItems(new String[] {"Category","RoleMixin","Mixin","Kind"});
			}else if (homoFunc.getPartEnd().getType() instanceof RoleMixin){
				stereoCombo.setItems(new String[] {"RoleMixin"});
			}else{
				stereoCombo.setItems(new String[] {"Kind", "Collective", "Quantity", "SubKind", "Role", "Phase", "Mixin", "RoleMixin", "Category", "Relator", "Mode", "DataType"});
			}
		}
		stereoCombo.setBounds(138, 10, 256, 23);
		
		partNameField = new Text(this, SWT.BORDER);
		partNameField.setBounds(138, 37, 256, 21);
		
		componentOfNameField = new Text(this, SWT.BORDER);
		componentOfNameField.setBounds(138, 64, 256, 21);
		
		btnIsShareable = new Button(this, SWT.CHECK);
		btnIsShareable.setText("isShareable");
		btnIsShareable.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		btnIsShareable.setBounds(400, 118, 122, 21);
		
		btnIsEssential = new Button(this, SWT.CHECK);
		btnIsEssential.setText("isEssential");
		btnIsEssential.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		btnIsEssential.setBounds(400, 12, 122, 16);
		
		btnIsInseparable = new Button(this, SWT.CHECK);
		btnIsInseparable.setText("isInseparable");
		btnIsInseparable.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		btnIsInseparable.setBounds(400, 66, 122, 16);
		
		btnIsImmutablePart = new Button(this, SWT.CHECK);
		btnIsImmutablePart.setText("isImmutablePart");
		btnIsImmutablePart.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		btnIsImmutablePart.setBounds(400, 39, 122, 16);
		
		btnIsImmutableWhole = new Button(this, SWT.CHECK);		
		btnIsImmutableWhole.setText("isImmutableWhole");
		btnIsImmutableWhole.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		btnIsImmutableWhole.setBounds(400, 96, 122, 16);
		
		btnCreateNewPart = new Button(this, SWT.NONE);
		btnCreateNewPart.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {	
				if (getPartStereotype() !=null && !getPartStereotype().isEmpty())
				{
					//check admissible stereotypes
					if(!(getPartStereotype().compareToIgnoreCase("Kind")==0))
					{
						String message = "by creating a part stereotyped as " + getPartStereotype()+", please notice that your model will be incomplete."+"\n\n";						
						MessageDialog.openInformation(getShell(), "WARNING", message);	
					}
					
					PartElement newpart = new PartElement(getPartStereotype(), getPartName(), getComponentOfName(), 
							isShareable(), isEssential(), isImmutablePart(), isImmutableWhole(), isInseparable());
					parts.add(newpart);
					if (!contains(partsList,newpart.toString())) partsList.add(newpart.toString());				
				}
			}
		});
		btnCreateNewPart.setText("New");
		btnCreateNewPart.setBounds(43, 94, 43, 25);
		
		setVisible(false);
		
		btnDeletePart = new Button(this, SWT.NONE);
		btnDeletePart.setText("Delete");
		btnDeletePart.setBounds(89, 94, 43, 25);
		
		partsList = new List(this, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		partsList.setBounds(138, 94, 256, 60);
		
		btnDeletePart.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				removePart(partsList.getItem(partsList.getSelectionIndex()));
				
				partsList.remove(partsList.getSelectionIndex());
			}
		});
	}		
	
	public ArrayList<PartElement> getParts()
	{
		return parts;
	}
	
	public void enableCreation(boolean value)
	{
		enableAll(value);
		btnCreateNewPart.setEnabled(value);
		btnDeletePart.setEnabled(value);
		partsList.setEnabled(value);
		setVisible(value);
	}
	
	public boolean contains(List list, String part)
	{
		for(int i=0;i< list.getItemCount();i++) 
		{
			if (partsList.getItem(i).compareToIgnoreCase(part)==0){
				return true;
			}
		}
		return false;
	}
	
	public void removePart(String part)
	{
		ArrayList<PartElement> deletion = new ArrayList<PartElement>();
		for(PartElement p: parts) {
			if (p.toString().compareToIgnoreCase(part)==0){
				deletion.add(p);
			}
		}
		parts.remove(part);
	}
}
