/* Renoverb is a program aimed at theater engineers that predicts the effect
   of sound absorbing materials on the reverberation of a room.
   Copyright 2008: Anthony Heathcoat, Nicholas Roth, Jim Simon, Yusuke Hasegawa

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.sql.SQLException;

import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.Range;
import org.jfree.data.RangeType;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class MainWindow extends JFrame implements ActionListener
{
	public static final long serialVersionUID = 1;
	public static CalcSQL database = null;
	public static DatabaseBrowser browser = null;
	public static About about = null;
	public static JPanel matList;		//shows all active materials
	
	private JButton uApply;		//universal Apply button
	private JButton matLoad;	//load material from database
	private JButton matNew;		//create new blank material slot to be filled in manually
	private JComboBox chartType;//Select a renderer for the chart

	private JMenuItem menuAbout;	//bring up About dialog
	private JMenuItem dbAttach;
	private JMenuItem dbImportDb;
	private JMenuItem dbImportTxt;
	private JMenuItem dbExportDb;

	private JTable mrTable;		//Measured RT
	private JTable outputTable;	//Output
	private JTextField inSA, inVol;	//user specified room surface area and volume
	private JTextField inRT;		//desired reverb time
	
	private XYSeries measuredReverb = new XYSeries("Measured",true,false);
	private XYSeries desiredReverb = new XYSeries("Desired",true,false);
	private XYSeries projectedReverb = new XYSeries("Projected",true,false);
	private XYPlot chartplot;	//the combination of all the datasets that go into the chart

	private XYItemRenderer[] renderers;
	
	public MainWindow()
	{
		//set basic window properties
		super("Renoverb");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setIconImage(Images.graph().getImage());
		setResizable(true);
		setMinimumSize(new Dimension(400,300));
		setSize(640,480);

		//find the class path of where program is running from
		String fileSeparator = System.getProperty("file.separator");
		String rawClasspath = System.getProperty("java.class.path");
		String pathSeparator = System.getProperty("path.separator");
		int firstSC = rawClasspath.indexOf(pathSeparator);
		if(firstSC == -1) firstSC = rawClasspath.length();
		String firstPath = rawClasspath.substring(0, firstSC);
		int lastFS = firstPath.lastIndexOf(fileSeparator);
		String classpath;
		if(lastFS == -1)
		{
			classpath = "";
		}else{
			classpath = firstPath.substring(0, lastFS)+fileSeparator; //should now contain the directory of where the program is running from
		}

		/* initialize seperate windows (database browser, about box)
		   but don't show them until the user asks for them
		 */
		browser = new DatabaseBrowser();
		about = new About();

		//start up material database subsystem
		attachDatabase(classpath+"materials.db");
		
		//some default/common objects to use throughout the program
		JLabel[] spacers = new JLabel[6];
		for(int x=0;x<spacers.length;x++)
		{
			spacers[x] = new JLabel(" ");
			spacers[x].setFont(new Font("Arial",Font.PLAIN,2));
		}
				
		//Create contents of tabs
		//Room
		JPanel room = new JPanel(new BorderLayout());
		JLabel rmTitle = new JLabel("Room",SwingConstants.CENTER);
//		rmTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD,14));
				
		JLabel markSA = new JLabel("Surface Area:",SwingConstants.LEFT);
		markSA.setFont(new Font("Arial",Font.PLAIN,14));
		JLabel unitSA = new JLabel("square feet",SwingConstants.LEFT);
		unitSA.setFont(new Font("Arial",Font.PLAIN,14));
		inSA = new JTextField(15);
		JLabel markVol = new JLabel("Volume:",SwingConstants.LEFT);
		markVol.setFont(new Font("Arial",Font.PLAIN,14));
		JLabel unitVol = new JLabel("cubic feet",SwingConstants.LEFT);
		unitVol.setFont(new Font("Arial",Font.PLAIN,14));
		inVol = new JTextField(15);
		JLabel markRT = new JLabel("Desired RT:",SwingConstants.LEFT);
		markRT.setFont(new Font("Arial",Font.PLAIN,14));
		JLabel unitRT = new JLabel("seconds",SwingConstants.LEFT);
		unitRT.setFont(new Font("Arial",Font.PLAIN,14));
		inRT = new JTextField(15);
		
		JPanel fieldTitles = new JPanel();
		fieldTitles.setLayout(new BoxLayout(fieldTitles, BoxLayout.Y_AXIS));
		fieldTitles.add(markSA);
		fieldTitles.add(spacers[2]);
		fieldTitles.add(markVol);
		fieldTitles.add(spacers[3]);
		fieldTitles.add(markRT);
		JPanel fieldInputs = new JPanel();
		fieldInputs.setLayout(new BoxLayout(fieldInputs, BoxLayout.Y_AXIS));
		fieldInputs.add(inSA);
		fieldInputs.add(inVol);
		fieldInputs.add(inRT);
		JPanel fieldUnits = new JPanel();
		fieldUnits.setLayout(new BoxLayout(fieldUnits, BoxLayout.Y_AXIS));
		fieldUnits.add(unitSA);
		fieldUnits.add(spacers[4]);
		fieldUnits.add(unitVol);
		fieldUnits.add(spacers[5]);
		fieldUnits.add(unitRT);
		JPanel fields = new JPanel(new BorderLayout());
		fields.add(fieldTitles, BorderLayout.WEST);
		fields.add(fieldInputs, BorderLayout.CENTER);
		fields.add(fieldUnits, BorderLayout.EAST);
		
		JLabel mrTitle = new JLabel("Measured Reverb Times",SwingConstants.CENTER);
//		mrTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD,14));
		mrTable = new JTable(new MeasuredRtTable());
		mrTable.getTableHeader().setReorderingAllowed(false);
		
		JPanel roomMeas = new JPanel();
		roomMeas.setLayout(new BoxLayout(roomMeas, BoxLayout.Y_AXIS));
		roomMeas.add(rmTitle);
		roomMeas.add(fields);
		roomMeas.add(spacers[0]);
		roomMeas.add(mrTitle);
		room.add(roomMeas,BorderLayout.NORTH);
		room.add(new JScrollPane(mrTable),BorderLayout.CENTER);
		
		//Materials
		JPanel materials = new JPanel(new BorderLayout());
		
		JPanel matLoadNew = new JPanel(new BorderLayout());
		matLoad = new JButton("Browse...");
		matNew = new JButton("New");
		matLoadNew.add(matLoad,BorderLayout.WEST);
		matLoadNew.add(matNew,BorderLayout.EAST);
		matLoadNew.add(spacers[1],BorderLayout.SOUTH);
		matLoad.addActionListener(this);
		matNew.addActionListener(this);
		
		matList = new JPanel();
		matList.setLayout(new BoxLayout(matList, BoxLayout.Y_AXIS));
		JScrollPane matListScroller = new JScrollPane(matList);
		
		materials.add(matLoadNew,BorderLayout.NORTH);
		materials.add(matListScroller,BorderLayout.CENTER);
		
		//Options
		JPanel options = new JPanel();
		options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
		
		//File the tabs together
		JTabbedPane tabArea = new JTabbedPane();
		tabArea.addTab("Room", null, room, "Room properties and Desired Reverb");
		tabArea.setMnemonicAt(0, KeyEvent.VK_I);
		tabArea.addTab("Materials", null, materials, "Absorption materials");
		tabArea.setMnemonicAt(1, KeyEvent.VK_M);
//		tabArea.addTab("Options", null, options, null);
//		tabArea.setMnemonicAt(2, KeyEvent.VK_O);
		
		//Create Input Area (Tab area + universal Apply button)
		JPanel inputArea = new JPanel(new BorderLayout());
		uApply = new JButton("Apply");
		uApply.addActionListener(this);
		inputArea.add(tabArea, BorderLayout.CENTER);
		inputArea.add(uApply, BorderLayout.SOUTH);
		
		//Create Output Table
		JPanel opTable = new JPanel(new BorderLayout());
		outputTable = new JTable(new OutputTable());
		outputTable.getTableHeader().setReorderingAllowed(false);
		opTable.add(new JScrollPane(outputTable), BorderLayout.CENTER);
		
		//Create Chart Area
		JPanel chartArea = new JPanel(new BorderLayout());
		String[] chartTypes = {"Spline Renderer","Line Renderer"};
		renderers = new XYItemRenderer[chartTypes.length];
		renderers[0] = new XYSplineRenderer();
		renderers[1] = new XYLineAndShapeRenderer(true,true);
		chartType = new JComboBox(chartTypes);
		chartType.setEditable(false);
		chartType.addActionListener(this);
		measuredReverb = new XYSeries("Measured",true,false);
		desiredReverb = new XYSeries("Desired",true,false);
		projectedReverb = new XYSeries("Projected",true,false);
		XYSeriesCollection reverbLines = new XYSeriesCollection();
		reverbLines.addSeries(measuredReverb);
		reverbLines.addSeries(desiredReverb);
		reverbLines.addSeries(projectedReverb);

		LogAxis domainAxis = new LogAxis("Frequency (Hz)");
		domainAxis.setBase(2);
		domainAxis.setMinorTickCount(1);
		domainAxis.setSmallestValue(32);
		domainAxis.setDefaultAutoRange(new Range(32,8000));
		
		NumberAxis rangeAxis = new NumberAxis("Seconds");
		rangeAxis.setAutoRangeIncludesZero(true);
		rangeAxis.setRangeType(RangeType.POSITIVE);
		
		chartplot = new XYPlot(reverbLines,domainAxis,rangeAxis,new XYSplineRenderer());
		JFreeChart chart = new JFreeChart("Reverb", chartplot);

		ChartPanel chartDisp = new ChartPanel(chart);
		chartArea.add(chartType, BorderLayout.NORTH);
		chartArea.add(chartDisp, BorderLayout.CENTER);		

		//build the top level layout
		JSplitPane inner = new JSplitPane(JSplitPane.VERTICAL_SPLIT,opTable,chartArea);
		JSplitPane outer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,inputArea,inner);
		setLayout(new BorderLayout());
		add(outer, BorderLayout.CENTER);
		getRootPane().setDefaultButton(uApply);
		
		//create menu bar
		JMenuBar menuBar = new JMenuBar();
		
		JMenu mnuData = new JMenu("Database");
		menuBar.add(mnuData);
		dbAttach = new JMenuItem("Switch to");
		mnuData.add(dbAttach);
		dbAttach.addActionListener(this);
		dbImportDb = new JMenuItem("Import data from .db");
		mnuData.add(dbImportDb);
		dbImportDb.addActionListener(this);
		dbImportTxt = new JMenuItem("Import data from .txt");
		mnuData.add(dbImportTxt);
		dbImportTxt.addActionListener(this);
		dbExportDb = new JMenuItem("Export data to existing .db");
		mnuData.add(dbExportDb);
		dbExportDb.addActionListener(this);
		
		JMenu mnuHelp = new JMenu("Help");
		menuBar.add(mnuHelp);
		menuAbout = new JMenuItem("About...");
		mnuHelp.add(menuAbout);
		menuAbout.addActionListener(this);
		
		setJMenuBar(menuBar);
		
		//finalize display
		pack();
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(uApply.equals(e.getSource())) apply();		//Apply button
		if(matNew.equals(e.getSource())) addMaterial();	//"New" button on Materials tab
		if(matLoad.equals(e.getSource())) browseData();	//"Load" or "Browse" button on Materials tab
		if(chartType.equals(e.getSource())) updateChartRenderer();	//Choosing a chart renderer in the drop down
		if(menuAbout.equals(e.getSource())) about.setVisible(true); //Show "About" dialog
		if(dbAttach.equals(e.getSource())) attachDatabase();	//ask user for a database to switch to
		if(dbImportDb.equals(e.getSource())) importDb();		//ask user for a database to dump into this one
		if(dbImportTxt.equals(e.getSource())) importTxt();		//parse a text file into the database
		if(dbExportDb.equals(e.getSource())) exportDb();		//ask user for a database and insert the current one into it
	}

	private void exportDb()
	{
		FileDialog external = new FileDialog(this, "Select database that will recieve the contents of the current one",FileDialog.LOAD);
		external.setVisible(true);
		if(external.getDirectory() == null || external.getFile() == null) return;
		try
		{
			database.exportToDatabase(external.getDirectory() + external.getFile());
		}catch(SQLException e){
			JOptionPane.showMessageDialog(null, "Unable to export current database into selected existing database.\nException message: "+e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		if(database != null && browser.isVisible()) browser.dbSearch();
	}	
	
	private void importTxt()
	{
		FileDialog addition = new FileDialog(this, "Select text file to parse and import to current database",FileDialog.LOAD);
		addition.setVisible(true);
		if(addition.getDirectory() == null || addition.getFile() == null) return;
		try
		{
			database.importFromFile(addition.getDirectory() + addition.getFile());
		}catch(Exception e){
			JOptionPane.showMessageDialog(null, "Unable to import Text Format materials list.\nException message: "+e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		if(database != null && browser.isVisible()) browser.dbSearch();
	}
	
	private void importDb()
	{
		FileDialog addition = new FileDialog(this, "Select database to dump into current database",FileDialog.LOAD);
		addition.setVisible(true);
		if(addition.getDirectory() == null || addition.getFile() == null) return;
		try
		{
			database.importFromDatabase(addition.getDirectory() + addition.getFile());
		}catch(SQLException e){
			JOptionPane.showMessageDialog(null, "Unable to import Material Database.\nException message: "+e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		if(database != null && browser.isVisible()) browser.dbSearch();
	}
	
	private void attachDatabase()
	{
		FileDialog dbSwitch = new FileDialog(this, "Select database or type name to create new database",FileDialog.LOAD);
		dbSwitch.setVisible(true);
		if(dbSwitch.getDirectory() == null || dbSwitch.getFile() == null) return;
		attachDatabase(dbSwitch.getDirectory() + dbSwitch.getFile());
	}
	
	private void attachDatabase(String file)
	{
		try{database = new CalcSQL(file);}catch(SQLException e)
		{
			JOptionPane.showMessageDialog(null, "Unable to start Material Database! You can continue, but Material Properties loading and saving will not work.\nException message: "+e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
			database = null;
			return;
		}
		if(database != null && browser.isVisible()) browser.dbSearch();
	}
	
	private void updateChartRenderer()
	{
		chartplot.setRenderer(renderers[chartType.getSelectedIndex()]);
	}
	
	private void apply()
	{
		double measured[] = new double[Renoverb.FREQUENCIES.length];

		//get the materials out of the GUI component
		Component[] compMaterials = matList.getComponents();
		MaterialTable[] materials = new MaterialTable[compMaterials.length];
		for(int x = 0; x<compMaterials.length; x++)		//we need this loop because stupid java can't cast arrays
			materials[x] = (MaterialTable)compMaterials[x];

		//deselect any selections in the material table to force update the data
		for(int x=0; x<materials.length; x++) materials[x].stopEditingAll();
		
		//update Measured Reverb in the chart
		for(int x = 0; x < Renoverb.FREQUENCIES.length; x++)
		{
			Object inobj = mrTable.getValueAt(x,1);
			if(inobj == null)
			{
				measured[x] = 0;
				outputTable.setValueAt(null,0,x+1);
				measuredReverb.addOrUpdate(Renoverb.FREQUENCIES[x], 0);
			}else{
				measured[x] = ((Double)inobj).doubleValue();
				outputTable.setValueAt(new Double(measured[x]),0,x+1);
				measuredReverb.addOrUpdate(Renoverb.FREQUENCIES[x], measured[x]);
			}
		}
		
		//update Desired Reverb in the chart
		for(int x = 0; x < Renoverb.FREQUENCIES.length; x++)
		{
			desiredReverb.addOrUpdate(Renoverb.FREQUENCIES[x], Renoverb.fieldToDouble(inRT));
		}
		
		//update projected reverb in the chart
		double volume, surfaceArea;	//room properties
		volume = Renoverb.fieldToDouble(inVol);
		surfaceArea = Renoverb.fieldToDouble(inSA);
		Vector<double[]> materialAbsorptionCoefficients = new Vector<double[]>(materials.length); //Parallel vectors... what a joke. Unfortunately it is needed for compatibility with the Calculator.
		Vector<Double> materialSurfaceAreas = new Vector<Double>(materials.length); 
		for(int x = 0; x<materials.length; x++)
		{
			materialAbsorptionCoefficients.add(materials[x].getAbsorptions());
			materialSurfaceAreas.add(new Double(materials[x].getSurfaceArea()));
		}
		double[] projected = Calculator.newRTcurve(volume, surfaceArea, measured, materialAbsorptionCoefficients, materialSurfaceAreas);
		for(int x = 0; x<projected.length; x++)
		{
			if(Double.isNaN(projected[x])) projected[x] = 0;
			projectedReverb.addOrUpdate(Renoverb.FREQUENCIES[x],projected[x]);
			outputTable.setValueAt(projected[x], 1, x+1);
		}
	}
	
	private void addMaterial()
	{
		matList.add(new MaterialTable());
		matList.validate();
	}
	
	private void browseData()
	{
		browser.setVisible(true);
	}
}

/*
 * Each one of these is a visible entry in the materials list. 
 */
class MaterialTable extends JPanel implements ActionListener
{
	public static final long serialVersionUID = 1;
	private JButton del, saveAs;
	private JTextField surfaceArea;
	private JTable absTable,matName;

	public MaterialTable()
	{
		this("","",new double[Renoverb.FREQUENCIES.length]);
	}
	
	public MaterialTable(String name, String manufacturer, double[] absorption)
	{
		super(new BorderLayout());
		matName = new JTable(new MatNameTable());
		matName.getTableHeader().setReorderingAllowed(false);
		matName.setValueAt(name,0,0);
		matName.setValueAt(manufacturer,0,1);
		JPanel matNamePanel = new JPanel(new BorderLayout());
		matNamePanel.add(matName,BorderLayout.CENTER);
		matNamePanel.add(matName.getTableHeader(),BorderLayout.NORTH);
		del = new JButton(Images.close());
		saveAs = new JButton(Images.save());
		del.addActionListener(this);
		saveAs.addActionListener(this);
		surfaceArea = new JTextField(0);
		
		JPanel actionBar = new JPanel(new BorderLayout());
		actionBar.add(saveAs,BorderLayout.WEST);
		actionBar.add(matNamePanel,BorderLayout.CENTER);
		actionBar.add(del,BorderLayout.EAST);
		
		JLabel[] spacers = {new JLabel(" "),new JLabel(" ")};
		for(int x=0;x<spacers.length;x++) spacers[x].setFont(new Font("Arial",Font.PLAIN,2));
		
		JPanel saField = new JPanel(new BorderLayout());
		saField.add(new JLabel("Surface Area:"),BorderLayout.WEST);
		saField.add(surfaceArea,BorderLayout.CENTER);
		saField.add(new JLabel("square feet"),BorderLayout.EAST);
		saField.add(spacers[0],BorderLayout.SOUTH);
		saField.add(spacers[1],BorderLayout.NORTH);
		
		JPanel titleBar = new JPanel(new BorderLayout());
		titleBar.add(actionBar, BorderLayout.NORTH);
		titleBar.add(saField, BorderLayout.SOUTH);
		
		absTable = new JTable(new AbsorptionTable());
		absTable.getTableHeader().setReorderingAllowed(false);
		for(int x=0; x<absorption.length; x++) absTable.setValueAt(absorption[x], x, 1);
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(absTable.getTableHeader(),BorderLayout.NORTH);
		tablePanel.add(absTable,BorderLayout.CENTER);
		
		/*We put all the content in an inner JPanel and engulf it with spacer JLabels
		  to provide a nice gray space that separates the material entries.
		  Otherwise, it is extremely hard to read.
		 */
		JPanel inner = new JPanel(new BorderLayout());
		inner.setBorder(new LineBorder(Color.BLACK,2,true));
		inner.add(titleBar, BorderLayout.NORTH);
		inner.add(tablePanel, BorderLayout.CENTER);
		
		//Using NORTH instead of CENTER prevents the JTable from expanding needlessly 
		add(inner,BorderLayout.NORTH);
		add(new JLabel("   "),BorderLayout.EAST);
		add(new JLabel("   "),BorderLayout.WEST);
	}
	
	public double getSurfaceArea()
	{
		return Renoverb.fieldToDouble(surfaceArea);
	}
	
	public double[] getAbsorptions()
	{
		double[] retval = new double[Renoverb.FREQUENCIES.length];
		for(int x = 0; x < Renoverb.FREQUENCIES.length; x++)
		{
			Double oneCoefficient = (Double)(absTable.getValueAt(x,1));
			if(oneCoefficient == null) retval[x] = 0;
			else retval[x] = oneCoefficient.doubleValue();
		}
		return retval;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(del.equals(e.getSource()))
		{
			JPanel parent = (JPanel)getParent();
			parent.remove(this);
			parent.revalidate();
			parent.repaint();
		}
		if(saveAs.equals(e.getSource())) saveNewMat();
	}
	
	public void stopEditingAll()
	{
		if(absTable.getCellEditor() != null) absTable.getCellEditor().stopCellEditing();
		if(matName.getCellEditor() != null) matName.getCellEditor().stopCellEditing();
	}
	
	private void saveNewMat()
	{
		stopEditingAll();
		if(MainWindow.database == null)
		{
			JOptionPane.showMessageDialog(null, "No database is selected. Go to \"Database > Select...\" in the menu to attach to a database to enable saving/loading of materials.", "Unable to save", JOptionPane.WARNING_MESSAGE);
		}else{
			double[] matAbs = new double[9];
			for(int x=0; x<9; x++)
			{
				Object fromTab = absTable.getValueAt(x,1);
				matAbs[x] = (fromTab==null)?0:((Double)fromTab).doubleValue();	//TODO: handle nulls properly instead of using zero. Can't be done until CalcSQL is fixed to allow adding nulls to database.
			}
			
			MainWindow.database.addMaterial(new Material((String)(matName.getValueAt(0,0)),(String)(matName.getValueAt(0,1)),matAbs[0],matAbs[1],matAbs[2],matAbs[3],matAbs[4],matAbs[5],matAbs[6],matAbs[7],matAbs[8]));
			if(MainWindow.browser.isVisible()) MainWindow.browser.dbSearch();
			JOptionPane.showMessageDialog(null, "Material saved.","Material List",JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	/*
	 * This is for the "material name" input table on each material list entry. 
	 */
	private class MatNameTable extends AbstractTableModel
	{
		public static final long serialVersionUID = 1;
		private String[] columnNames = {"Material Name","Manufacturer"};
		private Object[][] rowData = {{"New Material","None"}};
		public MatNameTable() {super();}
		
	    public String getColumnName(int col)
	    {
	        return columnNames[col];
	    }
	    public int getRowCount() { return rowData.length; }
	    public int getColumnCount() { return columnNames.length; }
	    public Object getValueAt(int row, int col)
	    {
	        return rowData[row][col];
	    }
	    public boolean isCellEditable(int row, int col) {return true;}
	    public void setValueAt(Object value, int row, int col)
	    {
	        rowData[row][col] = value;
	        fireTableCellUpdated(row, col);
	    }
	    public Class<?> getColumnClass(int c)
	    {
	        return "".getClass();
	    }
	}
	
	/*
	 * This table lists the absorption data for a material in the material list.
	 */
	private class AbsorptionTable extends AbstractTableModel
	{
		public static final long serialVersionUID = 1;
	    private String[] columnNames = {"Freqency (Hz)","Absorption Coefficient"};
	    private Object[][] rowData = {	{new Integer(32), null},
	    								{new Integer(65), null},
	    								{new Integer(125), null},
	    								{new Integer(250), null},
	    								{new Integer(500), null},
	    								{new Integer(1000), null},
	    								{new Integer(2000), null},
	    								{new Integer(4000), null},
	    								{new Integer(8000), null} };
	    public String getColumnName(int col)
	    {
	        return columnNames[col];
	    }
	    public int getRowCount() { return rowData.length; }
	    public int getColumnCount() { return columnNames.length; }
	    public Object getValueAt(int row, int col)
	    {
	        return rowData[row][col];
	    }
	    public boolean isCellEditable(int row, int col)
	    {
	    	return col != 0;
	    }
	    public void setValueAt(Object value, int row, int col)
	    {
	        rowData[row][col] = value;
	        fireTableCellUpdated(row, col);
	    }
	    public Class<?> getColumnClass(int c)
	    {
	        if(c == 0) return "".getClass();
	        else return (new Double(0)).getClass();
	    }
	}
}

/*
 * This model is for the "measured reverb" table in the room properties.
 */
class MeasuredRtTable extends AbstractTableModel
{
	public static final long serialVersionUID = 1;
    private String[] columnNames = {"Freqency (Hz)","RT"};
    private Object[][] rowData = {	{new Integer(32), null},
    								{new Integer(65), null},
    								{new Integer(125), null},
    								{new Integer(250), null},
    								{new Integer(500), null},
    								{new Integer(1000), null},
    								{new Integer(2000), null},
    								{new Integer(4000), null},
    								{new Integer(8000), null} };
    public String getColumnName(int col)
    {
        return columnNames[col];
    }
    public int getRowCount() { return rowData.length; }
    public int getColumnCount() { return columnNames.length; }
    public Object getValueAt(int row, int col)
    {
        return rowData[row][col];
    }
    public boolean isCellEditable(int row, int col)
    {
    	return col != 0;
    }
    public void setValueAt(Object value, int row, int col)
    {
        rowData[row][col] = value;
        fireTableCellUpdated(row, col);
    }
    public Class<?> getColumnClass(int c)
    {
        if(c == 0) return "".getClass();
        else return (new Double(0)).getClass();
    }
}

/*
 * This model is for the table that displays the raw data represented in the chart.
 */
class OutputTable extends AbstractTableModel
{
	public static final long serialVersionUID = 1;
    private String[] columnNames = {"Chart Line","32","65","125","250","500","1000","2000","4000","8000"};
    private Object[][] rowData = {	{"Measured",null,null,null,null,null,null,null,null,null},
    								//{"Desired",null,null,null,null,null,null,null,null,null},
    								{"Projected",null,null,null,null,null,null,null,null,null} };
    public String getColumnName(int col)
    {
        return columnNames[col];
    }
    public int getRowCount() { return rowData.length; }
    public int getColumnCount() { return columnNames.length; }
    public Object getValueAt(int row, int col)
    {
        return rowData[row][col];
    }
    public boolean isCellEditable(int row, int col)
    {
    	return false;
    }
    public void setValueAt(Object value, int row, int col)
    {
        rowData[row][col] = value;
        fireTableCellUpdated(row, col);
    }
    public Class<?> getColumnClass(int c)
    {
        if(c == 0) return "".getClass();
        else return (new Double(0)).getClass();
    }
}
