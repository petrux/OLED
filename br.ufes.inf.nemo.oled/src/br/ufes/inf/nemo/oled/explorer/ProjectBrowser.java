package br.ufes.inf.nemo.oled.explorer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import br.ufes.inf.nemo.assistant.ModellingAssistant;
import br.ufes.inf.nemo.common.ontoumlparser.OntoUMLParser;
import br.ufes.inf.nemo.oled.AppFrame;
import br.ufes.inf.nemo.oled.model.AlloySpecification;
import br.ufes.inf.nemo.oled.model.AntiPatternList;
import br.ufes.inf.nemo.oled.model.InferenceList;
import br.ufes.inf.nemo.oled.model.OCLDocument;
import br.ufes.inf.nemo.oled.model.UmlProject;
import br.ufes.inf.nemo.oled.ui.diagram.DiagramEditorWrapper;
import br.ufes.inf.nemo.oled.umldraw.structure.StructureDiagram;
import br.ufes.inf.nemo.ontouml2alloy.OntoUML2AlloyOptions;
import br.ufes.inf.nemo.tocl.tocl2alloy.TOCL2AlloyOption;
import javax.swing.border.EtchedBorder;

public class ProjectBrowser extends JPanel{

	private static final long serialVersionUID = 5598591779372431118L;
	
	//Keeps track of the trees instantiated in order to not re-instantite them 
	private static Map<UmlProject, ProjectBrowser> treeMap = new HashMap<UmlProject, ProjectBrowser>();
	public static AppFrame frame;
	
	private JScrollPane scroll;
	private ProjectTree tree; 
	
	//Find in Tree Feature
	private ArrayList<DefaultMutableTreeNode> resultFindList;	
	private int indexActualFind=0;
	private String oldText = new String();
	
	//Models
	private UmlProject project;	
	private OntoUMLParser refparser;	
	private AlloySpecification alloySpec;
	private OCLDocument oclmodel;
	private AntiPatternList antipatterns;	
	private InferenceList inferences;
	private OntoUML2AlloyOptions refOptions;
	private TOCL2AlloyOption oclOptions;	
	private ModellingAssistant assistant;

	private ProjectToolBar ptoolbar;
				
	public void setProject(UmlProject project)
	{
		this.project = project;

		DefaultMutableTreeNode root = new DefaultMutableTreeNode(project);
		refparser = new OntoUMLParser(project.getModel());
		tree = new ProjectTree(frame, root,project,refparser);
		tree.setBorder(new EmptyBorder(2,2,2,2));
		tree.addTreeSelectionListener(new ProjectTreeSelectionListener());
		
		String name = ((RefOntoUML.Package)project.getResource().getContents().get(0)).getName();
		if (name==null || name.isEmpty()) name = "model";
		alloySpec = new AlloySpecification(project.getTempDir()+File.separator+name.toLowerCase()+".als");
		
		oclmodel = new OCLDocument();
		oclOptions = new TOCL2AlloyOption();
		refOptions = new OntoUML2AlloyOptions();
		antipatterns = new AntiPatternList();
		inferences = new InferenceList();
		
		//VICTOR comentar
		assistant = new ModellingAssistant(project.getModel());
	
		ptoolbar = new ProjectToolBar(tree,frame.getDiagramManager());
		add(ptoolbar, BorderLayout.NORTH);
		
		scroll.setViewportView(tree);
		
		treeMap.put(project, this);
		updateUI();
	}
	
	public void eraseProject()
	{
		this.project = null;
		
		JPanel emptyTempPanel = new JPanel();
		emptyTempPanel.setBackground(Color.WHITE);
		emptyTempPanel.setBorder(new EmptyBorder(0,0, 0, 0));
		scroll.setViewportView(emptyTempPanel);
		
		emptyTempPanel.setPreferredSize(new Dimension(200,250));
		
		updateUI();
	}
	
	public void select()
	{
		if (indexActualFind >= resultFindList.size())
		{
			indexActualFind=0;			
		}
		if(resultFindList.size()>0){
			getTree().select(resultFindList.get(indexActualFind));
			indexActualFind++;
		}
				
	}
	
	public void find(String text)
	{
		if (!oldText.equals(text))
		{
			resultFindList = getTree().find(text);		
			indexActualFind=0;
			oldText = text;
		}
		if (oldText.equals(text))
		{
			select();
		}
	}
	
	public ProjectBrowser(AppFrame appframe, UmlProject project)
	{
		super(new BorderLayout());
		setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		this.project = project;
		frame = appframe;
		
		scroll = new JScrollPane();
		
		if (project!=null)
		{
			setProject(project);
		}
		
		add(scroll, BorderLayout.CENTER);	
		
		JPanel emptyTempPanel = new JPanel();
		emptyTempPanel.setBackground(Color.WHITE);
		emptyTempPanel.setBorder(new EmptyBorder(0,0, 0, 0));
		scroll.setViewportView(emptyTempPanel);
		
		emptyTempPanel.setPreferredSize(new Dimension(200,250));
		scroll.setPreferredSize(new Dimension(200,250));
		setPreferredSize(new Dimension(216, 317));
	}
	
	public static ProjectBrowser getProjectBrowserFor(AppFrame frame, UmlProject project) 
	{
		ProjectBrowser browser = treeMap.get(project);
		if(browser == null)
		{
			browser = new ProjectBrowser(frame, project);
			treeMap.put(project, browser);			
		}
		return browser;
	}
	
	public static OntoUMLParser getParserFor(UmlProject project) 
	{		
		return ProjectBrowser.getProjectBrowserFor(frame, project).refparser;
	}
	
	public static void setParserFor(UmlProject project, OntoUMLParser refparser) 
	{		
		ProjectBrowser.getProjectBrowserFor(frame,project).refparser = refparser;
	}
	
	public static AlloySpecification getAlloySpecFor(UmlProject project) 
	{		
		return ProjectBrowser.getProjectBrowserFor(frame,project).alloySpec;
	}
	
	public static void setAlloySpecFor(UmlProject project, AlloySpecification alloySpec) 
	{		
		ProjectBrowser.getProjectBrowserFor(frame,project).alloySpec = alloySpec;
	}
	
	public static OCLDocument getOCLModelFor(UmlProject project)
	{
		return ProjectBrowser.getProjectBrowserFor(frame,project).oclmodel;
	}
	
	public static void setOCLOptionsFor(UmlProject project, TOCL2AlloyOption oclOptions)
	{
		ProjectBrowser.getProjectBrowserFor(frame,project).oclOptions = oclOptions;
	}
	
	public static TOCL2AlloyOption getOCLOptionsFor(UmlProject project)
	{
		return ProjectBrowser.getProjectBrowserFor(frame,project).oclOptions;
	}

	public static OntoUML2AlloyOptions getOntoUMLOptionsFor(UmlProject project)
	{
		return ProjectBrowser.getProjectBrowserFor(frame,project).refOptions;
	}

	public static void setOntoUMLOptionsFor(UmlProject project, OntoUML2AlloyOptions refOptions)
	{
		ProjectBrowser.getProjectBrowserFor(frame,project).refOptions = refOptions;
	}
	
	public static AntiPatternList getAntiPatternListFor(UmlProject project)
	{
		return ProjectBrowser.getProjectBrowserFor(frame,project).antipatterns;
	}

	public static void setAntiPatternListFor(UmlProject project, AntiPatternList antipatterns)
	{
		ProjectBrowser.getProjectBrowserFor(frame,project).antipatterns = antipatterns;
	}
	
	public static ModellingAssistant getAssistantFor(UmlProject project)
	{
		return ProjectBrowser.getProjectBrowserFor(frame,project).assistant;
	}	
	
	public static InferenceList getInferences(UmlProject project) {
		return ProjectBrowser.getProjectBrowserFor(frame,project).inferences;
	}

	
	public static void setDerivations(UmlProject project, InferenceList inferences) {
		ProjectBrowser.getProjectBrowserFor(frame,project).inferences = inferences;
	}
	
	/**
	 * Refresh the Project Browser.
	 */
	public static void refreshTree(UmlProject project)
	{
		ProjectBrowser browser = ProjectBrowser.getProjectBrowserFor(frame,project);		
		browser.tree.updateUI();				
		browser.validate();
		browser.repaint();		
	}
	
	public void setTree(ProjectTree tree)
	{
		remove(scroll);
		
		this.tree = tree;
		this.tree.setBorder(new EmptyBorder(2,2,2,2));
		
		this.addTreeSelectionListener(new ProjectTreeSelectionListener());
	
		scroll = new JScrollPane();
		scroll.setViewportView(tree);
		add(scroll, BorderLayout.CENTER);		
		
		scroll.validate();
		scroll.repaint();
		this.validate();
		this.repaint();
	}
	
	class ProjectTreeSelectionListener implements TreeSelectionListener 
	 {
		@Override
		public void valueChanged(TreeSelectionEvent e) 
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
			if(node!=null)
			{
				if (node.getUserObject() instanceof OntoUMLElement){
					
				}
				else if (node.getUserObject()!=null && node.getParent() != null && (node.getUserObject() instanceof StructureDiagram) && !(((DefaultMutableTreeNode)node.getParent()).getUserObject() instanceof UmlProject))
				{
					 StructureDiagram diagram = ((StructureDiagram)node.getUserObject());
					 for(Component c: frame.getDiagramManager().getComponents())
					 {
						 if (c instanceof DiagramEditorWrapper){
							 if (((DiagramEditorWrapper)c).getDiagram().equals(diagram)) frame.getDiagramManager().setSelectedComponent(c);
						 }
					 }
				}
			}
		}		
	 }
	
	public void setParser(OntoUMLParser refparser)
	{
		this.refparser = refparser;
	}
	
	public void addTreeSelectionListener(TreeSelectionListener selectionListener)
	{
		tree.addTreeSelectionListener(selectionListener);
	}	
	
	public ProjectTree getTree() 
	{
		return tree;
	}

	public UmlProject getProject() {
		return project;
	}
}
