/**
 * Copyright 2007 Wei-ju Wu
 *
 * This file is part of TinyUML.
 *
 * TinyUML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * TinyUML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TinyUML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package br.ufes.inf.nemo.oled.ui.diagram;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.util.EcoreUtil.Copier;

import br.ufes.inf.nemo.common.ontoumlparser.OntoUMLParser;
import br.ufes.inf.nemo.oled.AppCommandListener;
import br.ufes.inf.nemo.oled.AppFrame;
import br.ufes.inf.nemo.oled.AppMenu;
import br.ufes.inf.nemo.oled.DiagramManager;
import br.ufes.inf.nemo.oled.antipattern.AntiPatternSearchDialog;
import br.ufes.inf.nemo.oled.dialog.AutoCompletionDialog;
import br.ufes.inf.nemo.oled.explorer.ProjectBrowser;
import br.ufes.inf.nemo.oled.model.ElementType;
import br.ufes.inf.nemo.oled.model.RelationEndType;
import br.ufes.inf.nemo.oled.model.RelationType;
import br.ufes.inf.nemo.oled.util.MethodCall;

/**
 * This class receives BaseEditor related AppCommands and dispatches them to
 * the right places. This offloads editor related commands from the
 * AppFrame object, while AppFrame handles commands on a global level,
 * DiagramEditorCommandDispatcher handles it on the level of the current editor.
 *
 * @author Wei-ju Wu
 * @version 1.0
 */
public class DiagramEditorCommandDispatcher implements AppCommandListener {

	@SuppressWarnings("unused")
	private AppFrame frame;
	private DiagramManager manager;
	private Map<String, MethodCall> selectorMap = new HashMap<String, MethodCall>();

	/**
	 * Constructor.
	 * @param aFrame the application frame
	 */
	public DiagramEditorCommandDispatcher(DiagramManager manager, AppFrame frame) {
		this.manager = manager;
		initSelectorMap();
		this.frame = frame;
	}

	/**
	 * Initializes the selector map.
	 */
	private void initSelectorMap() {
		try {
			selectorMap.put("SELECT_MODE", new MethodCall(
					DiagramEditor.class.getMethod("setSelectionMode")));

			selectorMap.put("REDO", new MethodCall(
					getClass().getMethod("redo")));

			selectorMap.put("FIND", new MethodCall(
					getClass().getMethod("find")));
			
			selectorMap.put("UNDO", new MethodCall(
					getClass().getMethod("undo")));
			
			selectorMap.put("REDRAW", new MethodCall(
					DiagramEditor.class.getMethod("redraw")));

			selectorMap.put("SELECT_ALL", new MethodCall(
					DiagramEditor.class.getMethod("selectAll")));

			selectorMap.put("BRING_TO_FRONT", new MethodCall(
					DiagramEditor.class.getMethod("bringToFront")));

			selectorMap.put("PUT_TO_BACK", new MethodCall(
					DiagramEditor.class.getMethod("putToBack")));

			selectorMap.put("EDIT_PROPERTIES", new MethodCall(
					DiagramEditor.class.getMethod("editProperties")));

			selectorMap.put("DELETE", new MethodCall(
					DiagramEditor.class.getMethod("deleteSelection")));

			selectorMap.put("EXCLUDE", new MethodCall(
					DiagramEditor.class.getMethod("excludeSelection")));

			//Commands for creating classes
			selectorMap.put("CREATE_KIND", new MethodCall(
					DiagramEditor.class.getMethod("setCreationMode", ElementType.class),
					ElementType.KIND));

			selectorMap.put("CREATE_QUANTITY", new MethodCall(
					DiagramEditor.class.getMethod("setCreationMode", ElementType.class),
					ElementType.QUANTITY));

			selectorMap.put("CREATE_COLLECTIVE", new MethodCall(
					DiagramEditor.class.getMethod("setCreationMode", ElementType.class),
					ElementType.COLLECTIVE));

			selectorMap.put("CREATE_SUBKIND", new MethodCall(
					DiagramEditor.class.getMethod("setCreationMode", ElementType.class),
					ElementType.SUBKIND));

			selectorMap.put("CREATE_PHASE", new MethodCall(
					DiagramEditor.class.getMethod("setCreationMode", ElementType.class),
					ElementType.PHASE));

			selectorMap.put("CREATE_ROLE", new MethodCall(
					DiagramEditor.class.getMethod("setCreationMode", ElementType.class),
					ElementType.ROLE));

			selectorMap.put("CREATE_CATEGORY", new MethodCall(
					DiagramEditor.class.getMethod("setCreationMode", ElementType.class),
					ElementType.CATEGORY));

			selectorMap.put("CREATE_ROLEMIXIN", new MethodCall(
					DiagramEditor.class.getMethod("setCreationMode", ElementType.class),
					ElementType.ROLEMIXIN));

			selectorMap.put("CREATE_MIXIN", new MethodCall(
					DiagramEditor.class.getMethod("setCreationMode", ElementType.class),
					ElementType.MIXIN));

			selectorMap.put("CREATE_MODE", new MethodCall(
					DiagramEditor.class.getMethod("setCreationMode", ElementType.class),
					ElementType.MODE));

			selectorMap.put("CREATE_RELATOR", new MethodCall(
					DiagramEditor.class.getMethod("setCreationMode", ElementType.class),
					ElementType.RELATOR));

			selectorMap.put("CREATE_DATATYPE", new MethodCall(
					DiagramEditor.class.getMethod("setCreationMode", ElementType.class),
					ElementType.DATATYPE));

			//Commands for creating relations		
			selectorMap.put("CREATE_GENERALIZATION", new MethodCall(
					DiagramEditor.class.getMethod("setCreateConnectionMode",
							RelationType.class), RelationType.GENERALIZATION));

			selectorMap.put("CREATE_CHARACTERIZATION", new MethodCall(
					DiagramEditor.class.getMethod("setCreateConnectionMode",
							RelationType.class), RelationType.CHARACTERIZATION));

			selectorMap.put("CREATE_FORMAL", new MethodCall(
					DiagramEditor.class.getMethod("setCreateConnectionMode",
							RelationType.class), RelationType.FORMAL));

			selectorMap.put("CREATE_MATERIAL", new MethodCall(
					DiagramEditor.class.getMethod("setCreateConnectionMode",
							RelationType.class), RelationType.MATERIAL));

			selectorMap.put("CREATE_MEDIATION", new MethodCall(
					DiagramEditor.class.getMethod("setCreateConnectionMode",
							RelationType.class), RelationType.MEDIATION));

			selectorMap.put("CREATE_MEMBEROF", new MethodCall(
					DiagramEditor.class.getMethod("setCreateConnectionMode",
							RelationType.class), RelationType.MEMBEROF));

			selectorMap.put("CREATE_SUBQUANTITYOF", new MethodCall(
					DiagramEditor.class.getMethod("setCreateConnectionMode",
							RelationType.class), RelationType.SUBQUANTITYOF));

			selectorMap.put("CREATE_SUBCOLLECTIONOF", new MethodCall(
					DiagramEditor.class.getMethod("setCreateConnectionMode",
							RelationType.class), RelationType.SUBCOLLECTIONOF));

			selectorMap.put("CREATE_COMPONENTOF", new MethodCall(
					DiagramEditor.class.getMethod("setCreateConnectionMode",
							RelationType.class), RelationType.COMPONENTOF));

			selectorMap.put("CREATE_DERIVATION", new MethodCall(
					DiagramEditor.class.getMethod("setCreateConnectionMode",
							RelationType.class), RelationType.DERIVATION));

			selectorMap.put("CREATE_ASSOCIATION", new MethodCall(
					DiagramEditor.class.getMethod("setCreateConnectionMode",
							RelationType.class), RelationType.ASSOCIATION));

			//selectorMap.put("CREATE_CONDITION", new MethodCall(
			//		DiagramEditor.class.getMethod("setCreateConnectionMode",
			//				RelationType.class), RelationType.ASSOCIATION));//Asso

			//selectorMap.put("CREATE_DERIVATIONRULE", new MethodCall(
			//		DiagramEditor.class.getMethod("setCreateConnectionMode",
			//				RelationType.class), RelationType.ASSOCIATION));//Asso

			//selectorMap.put("CREATE_CONCLUSION", new MethodCall(
			//		DiagramEditor.class.getMethod("setCreateConnectionMode",
			//				RelationType.class), RelationType.ASSOCIATION));//Asso

			//selectorMap.put("CREATE_NOTE", new MethodCall(
			//		DiagramEditor.class.getMethod("setCreationMode", ElementType.class),
			//		ElementType.NOTE));

			selectorMap.put("CREATE_NOTE_CONNECTION", new MethodCall(
					DiagramEditor.class.getMethod("setCreateConnectionMode",
							RelationType.class), RelationType.NOTE_CONNECTOR));

			selectorMap.put("RESET_POINTS", new MethodCall(
					DiagramEditor.class.getMethod("resetConnectionPoints")));

			selectorMap.put("RECT_TO_DIRECT", new MethodCall(
					DiagramEditor.class.getMethod("toDirect")));

			selectorMap.put("DIRECT_TO_RECT", new MethodCall(
					DiagramEditor.class.getMethod("toRectilinear")));

			selectorMap.put("TREE_STYLE_VERTICAL", new MethodCall(
					DiagramEditor.class.getMethod("toTreeStyleVertical")));
			
			selectorMap.put("TREE_STYLE_HORIZONTAL", new MethodCall(
					DiagramEditor.class.getMethod("toTreeStyleHorizontal")));
			
			selectorMap.put("NAVIGABLE_TO_SOURCE", new MethodCall(
					DiagramEditor.class.getMethod("setNavigability", RelationEndType.class),
					RelationEndType.SOURCE));

			selectorMap.put("NAVIGABLE_TO_TARGET", new MethodCall(
					DiagramEditor.class.getMethod("setNavigability", RelationEndType.class),
					RelationEndType.TARGET));

			// Self-calls

			selectorMap.put("CREATE_DERIVATION_BY_UNION", new MethodCall(
					DiagramEditor.class.getMethod("setPatternCreationMode")));

			selectorMap.put("CREATE_DERIVATION_BY_EXCLUSION", new MethodCall(
					DiagramEditor.class.getMethod("setPatternCreationModeEx")));

			selectorMap.put("SHOW_GRID", new MethodCall(
					getClass().getMethod("showGrid")));

			selectorMap.put("TOOLBOX", new MethodCall(
					getClass().getMethod("showToolbox")));
			
			selectorMap.put("ZOOM_IN", new MethodCall(
					getClass().getMethod("zoomIn"))
					);

			selectorMap.put("ZOOM_OUT", new MethodCall(
					getClass().getMethod("zoomOut"))
					);
			
			selectorMap.put("SNAP_TO_GRID", new MethodCall(
					getClass().getMethod("snapToGrid")));

			selectorMap.put("PARSE_OCL", new MethodCall(
					getClass().getMethod("parseOCL")));

			selectorMap.put("ASSISTANT", new MethodCall(
					getClass().getMethod("enableAssistant")));

			selectorMap.put("AUTO_SELECTION", new MethodCall(
					getClass().getMethod("autoComplete")));
			
			selectorMap.put("VERIFY_MODEL", new MethodCall(
					getClass().getMethod("verifyModel")));
			
			selectorMap.put("GENERATE_ALLOY", new MethodCall(
					getClass().getMethod("generateAlloy")));

			selectorMap.put("ANTIPATTERN", new MethodCall(
					getClass().getMethod("manageAntiPatterns")));

			selectorMap.put("GENERATE_OWL_SETTINGS", new MethodCall(
					getClass().getMethod("generateOwl")));

			selectorMap.put("GENERATE_SBVR", new MethodCall(
					getClass().getMethod("generateSbvr")));

			selectorMap.put("GENERATE_TEXT", new MethodCall(
					getClass().getMethod("callGlossary")));

			selectorMap.put("ERROR", new MethodCall(
					getClass().getMethod("searchErrors")));

			selectorMap.put("WARNING", new MethodCall(
					getClass().getMethod("searchWarnings")));
			
			selectorMap.put("OCLEDITOR", new MethodCall(
					getClass().getMethod("showOclEditor")));

			selectorMap.put("OUTPUT", new MethodCall(
					getClass().getMethod("showOutputPane")));

			selectorMap.put("DERIVERELATIONS", new MethodCall(
					getClass().getMethod("deriveRelations")));

			selectorMap.put("DERIVED_BY_UNION", new MethodCall(
					getClass().getMethod("derivedByUnion")));

			selectorMap.put("DERIVED_BY_EXCLUSION", new MethodCall(
					getClass().getMethod("derivedByExclusion")));

			selectorMap.put("PATTERN_CREATION_SUBKIND", new MethodCall(
					DiagramEditor.class.getMethod("setPatternMode",ElementType.class),ElementType.SUBKINDPATTERN));
			
			selectorMap.put("PATTERN_CREATION_SUBKIND_PARTITION", new MethodCall(
					DiagramEditor.class.getMethod("setPatternMode",ElementType.class),ElementType.SUBKINDPARTITIONPATTERN));

			selectorMap.put("PATTERN_CREATION_ROLEMIXIN_PATTERN", new MethodCall(
					DiagramEditor.class.getMethod("setPatternMode",ElementType.class),ElementType.ROLEMIXIN));
			
			selectorMap.put("PATTERN_CREATION_PHASE_PARTITION", new MethodCall(
					DiagramEditor.class.getMethod("setPatternMode",ElementType.class),ElementType.PHASEPARTITION));
			
			selectorMap.put("PATTERN_CREATION_RELATOR", new MethodCall(
					DiagramEditor.class.getMethod("setPatternMode",ElementType.class),ElementType.RELATORPATTERN));
			
			selectorMap.put("PATTERN_CREATION_ROLE", new MethodCall(
					DiagramEditor.class.getMethod("setPatternMode",ElementType.class),ElementType.ROLEPATTERN));
						
			selectorMap.put("CREATE_GEN_SET", new MethodCall(
					DiagramEditor.class.getMethod("createGeneralizationSet")));
			selectorMap.put("DELETE_GEN_SET", new MethodCall(
					DiagramEditor.class.getMethod("deleteGeneralizationSet")));
			
			

		} catch (NoSuchMethodException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void handleCommand(String command) {
		MethodCall methodcall = selectorMap.get(command);
		if (methodcall != null) {
			Object target = manager.getCurrentDiagramEditor();			 
			// in order to catch the self calling methods
			if (methodcall.getMethod().getDeclaringClass() == DiagramEditorCommandDispatcher.class) {
				target = this;
			}
			if(target != null) methodcall.call(target);			  
		} 
	}

	public void undo()
	{		
		if (manager.isProjectLoaded()==false) return;
		if(manager.getCurrentDiagramEditor()!=null){
			if(manager.getCurrentDiagramEditor().canUndo()){
				manager.getCurrentDiagramEditor().undo();
			}else{				
				manager.getFrame().showInformationMessageDialog("Cannot Undo", "No other action to be undone.\n\n");
			}
		}
	}

	public void redo()
	{
		if (manager.isProjectLoaded()==false) return;

		if(manager.getCurrentDiagramEditor()!=null) 
		{			
			if(manager.getCurrentDiagramEditor().canRedo()){
				manager.getCurrentDiagramEditor().redo();
			}else{
				manager.getFrame().showInformationMessageDialog("Cannot Redo", "No other action to be redone.\n\n");
			}
		}
	}

	public void find()
	{
		if (manager.isProjectLoaded()==false) return;
		manager.getFrame().focusOnFinder();		
	}

	public void showGrid() {
		if (manager.isProjectLoaded()==false) return;

		manager.getCurrentDiagramEditor().showGrid(getMenuManager().isSelected("SHOW_GRID"));
		manager.getCurrentWrapper().getToolBar().update();
	}

	public void showToolbox() {
		manager.getFrame().showToolBox(getMenuManager().isSelected("TOOLBOX"));
		manager.getCurrentWrapper().getToolBar().update();
	}
	
	public void zoomOut()
	{
		if (manager.isProjectLoaded()==false) return;
		manager.getCurrentDiagramEditor().zoomOut();
		manager.getCurrentWrapper().getToolBar().update();
	}
	
	public void zoomIn()
	{
		if (manager.isProjectLoaded()==false) return;
		manager.getCurrentDiagramEditor().zoomIn();
		manager.getCurrentWrapper().getToolBar().update();
	}
	
	public void callGlossary()
	{
		if (manager.isProjectLoaded()==false) return;
		manager.workingOnlyWithChecked();
		manager.callGlossary();
	}
	
	public void generateSbvr()
	{
		if (manager.isProjectLoaded()==false) return;
		manager.workingOnlyWithChecked();
		OntoUMLParser refparser = ProjectBrowser.getParserFor(manager.getCurrentProject());
		manager.generateSbvr((RefOntoUML.Model)refparser.createPackageFromSelections(new Copier()));
	}
		
	public void generateAlloy()
	{
		if (manager.isProjectLoaded()==false) return;
		manager.workingOnlyWithChecked();		
		manager.openAlloySettings();
	}

	public void generateOwl() 
	{
		if (manager.isProjectLoaded()==false) return;
		manager.workingOnlyWithChecked();
		manager.openOwlSettings();
	}
	
	public void verifyModel() 
	{
		if (manager.isProjectLoaded()==false) return;
		manager.verifyCurrentProject();
	}
	
	public void parseOCL()
	{
		if (manager.isProjectLoaded()==false) return;
		manager.parseOCL(true);		
	}
	public void derivedByUnion()
	{
		if (manager.isProjectLoaded()==false) return;
		manager.deriveByUnion();
	}

	public void derivedByExclusion()
	{
		if (manager.isProjectLoaded()==false) return;
		manager.deriveByExclusion();
	}

	public void manageAntiPatterns()
	{			
		if (manager.isProjectLoaded()==false) return;
		manager.workingOnlyWithChecked();
		AntiPatternSearchDialog.open(manager.getFrame());		
	}
	
	public void searchWarnings()
	{
		if (manager.isProjectLoaded()==false) return;
		manager.searchWarnings();
		manager.getFrame().focusOnWarnings();
	}

	public void searchErrors()
	{
		if (manager.isProjectLoaded()==false) return;
		manager.searchErrors();
		manager.getFrame().focusOnErrors();
	}
	
	public void enableAssistant() 
	{
		
	}
		
	public void showOutputPane()
	{	
		manager.showOutputPane();
	}

	public void showOclEditor()
	{
		manager.showOclEditor();
	}

	public void autoComplete()
	{
		if (manager.isProjectLoaded()==false) return;
		AutoCompletionDialog.open(manager.getFrame(),manager.getCurrentProject());
	}

	public void deriveRelations() 
	{
		if (manager.isProjectLoaded()==false) return;
		//manager.deriveRelations();
	}

	/**
	 * Returns the application's menu manager.
	 * @return the menu manager
	 */
	private AppMenu getMenuManager() {
		return manager.getMainMenu();
	}

	/**
	 * Activates snapping depending on the selection state of the menu item.
	 */
	public void snapToGrid() {
		manager.getCurrentDiagramEditor().snapToGrid(getMenuManager().isSelected("SNAP_TO_GRID"));
	}
}
